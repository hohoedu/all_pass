document.addEventListener('DOMContentLoaded', () => {

    /* ---------------------------------- //
    // 1. 기본 상태 변수 & DOM 요소 등록  //
    // ---------------------------------- */

    let selectedCenter = null;
    let rows = [];                 // 화면에 렌더링 중인 원본 데이터
    let sortKey = null;            // 'className' | 'unitName'
    let sortAsc = true;

    const monthInput = document.querySelector('.hidden-picker');
    const monthBtn = document.querySelector('.calendar-open');
    const monthDisplay = document.querySelector('.current-month');
    const tbody = document.querySelector('.ebook-keycode-table tbody');

    /* 세팅값 드롭다운을 노출할 교재 */
    const SETTING_CLASS_KEYS = ['Y', 'S', 'P', 'K', 'M', 'J'];

    /* 교재 타입별 세팅값 옵션 (value = 맨 앞 알파벳) */
    const SETTING_OPTIONS = {
        '1': [
            {value: 'X', label: 'X:분권:놀이북'},
            {value: 'Z', label: 'Z:온권:놀이터'}
        ],
        '2': [
            {value: 'X', label: 'X:22'},
            {value: 'Y', label: 'Y:23'},
            {value: 'Z', label: 'Z:24'},
            {value: 'A', label: 'A:25,26'}
        ]
    };

    /* 영재/수재의 5호·10호는 상/하반기 수업이라 세팅값이 하나뿐이다 */
    const HALF_TERM_CLASS_KEYS = ['Y', 'S'];
    const HALF_TERM_UNIT_KEYS = ['H05', 'H10'];
    const HALF_TERM_OPTIONS = [{value: 'X', label: 'X:22 ~ 26'}];

    initCurrentMonth();

    /* ------------------------------------- //
    // 2. 월 선택 이벤트 (input[type=month]) //
    // ------------------------------------- */

    monthBtn.addEventListener('click', () => monthInput.showPicker());
    monthInput.addEventListener('change', async () => {
        renderMonthText();
        await loadTableIfReady();
    });

    /* ------------------- //
    // 3. 학원 선택 이벤트 //
    // ------------------- */
    document.querySelectorAll('.stu-chocie-table tbody tr').forEach(row => {
        row.addEventListener('click', async () => {
            selectedCenter = row.dataset.centerCode;

            document.querySelectorAll('.stu-chocie-table tbody tr')
                .forEach(r => r.classList.remove('selected'));
            row.classList.add('selected');

            document.querySelector('.small-title').textContent =
                row.querySelector('.names').textContent.trim();

            await loadTableIfReady();
        });
    });

    /* --------------- //
    // 4. 정렬 이벤트  //
    // --------------- */
    document.querySelectorAll('.ebook-keycode-table .sort-th').forEach(th => {
        th.addEventListener('click', () => {
            const key = th.dataset.sortKey;
            sortAsc = (sortKey === key) ? !sortAsc : true;
            sortKey = key;
            renderTable();
        });
    });

    /* -------------------- //
    // 5. 세팅값 선택 이벤트 //
    // -------------------- */
    tbody.addEventListener('change', e => {
        const select = e.target.closest('.setting-value');
        if (!select) return;

        const tr = select.closest('tr');
        const row = findRow(tr);
        if (!row) return;

        row.settingValue = select.value;
        row.keyCode = select.value ? buildKeyCode(row, select.value) : null;

        tr.children[3].textContent = row.keyCode ?? '-';

        console.log('세팅값 선택:', {
            className: row.className,
            unitName: row.unitName,
            classType: row.classType,
            settingValue: row.settingValue,
            keyCode: row.keyCode
        });
    });

    /* 화면의 tr 에 대응하는 원본 데이터 찾기 (정렬돼도 안전하게 키로 찾는다) */
    function findRow(tr) {
        return rows.find(r => (r.classKey ?? '') === tr.dataset.classKey
            && (r.unitKey ?? '') === tr.dataset.unitKey);
    }

    /* ----------------------------------------------------------- //
    // 5-1. keyCode 생성                                            //
    // 세팅값 있는 교재: 교재문자 + 세팅값 + 호수 + '-' + 4자리 난수    //
    // 세팅값 없는 교재: class_key      + 호수 + '-' + 4자리 난수    //
    // ----------------------------------------------------------- */
    function buildKeyCode(row, settingValue) {
        // 접두어 2자리는 ocode 기준. 세팅값이 있으면 둘째 자리를 세팅값으로 바꾼다 (YA + X → YX)
        const ocode = row.ocode;
        const prefix = settingValue
                ? (ocode ? ocode.charAt(0) : '') + settingValue
                : (ocode ? ocode.slice(0, 2) : row.classKey);
        if (!prefix) return null;

        const head = `${prefix}${extractUnitNo(row.unitName, row.unitKey)}-`;

        // 이미 만들어진 코드와 겹치지 않을 때까지 다시 뽑는다
        const used = new Set(rows.filter(r => r !== row).map(r => r.keyCode).filter(Boolean));

        for (let i = 0; i < 50; i++) {
            const candidate = head + randomSuffix(4);
            if (!used.has(candidate)) return candidate;
        }

        console.warn('keyCode 난수 충돌이 반복됩니다:', head);
        return head + randomSuffix(4);
    }

    /* 세팅값이 없는 교재는 조회 직후 keyCode 를 자동 생성한다 */
    function fillAutoKeyCodes() {
        rows.forEach(r => {
            if (r.levelUnit) return;                              // 급수는 이북 코드 대상이 아니다
            if (SETTING_CLASS_KEYS.includes(r.classKey)) return;  // 세팅값 선택 후 생성
            if (r.keyCode) return;                                // 이미 있으면 유지
            r.keyCode = buildKeyCode(r, null);
        });
    }

    /*
     * 호수 2자리 뽑기
     * - '1호' → '01', '11월' → '11'
     * - 키움/만남/자람은 호수가 인물명이라 숫자가 없다. unitKey(A01~C10)의 숫자를 쓴다
     */
    function extractUnitNo(unitName, unitKey) {
        const fromName = (unitName ?? '').replace(/[^0-9]/g, '');
        const no = fromName || (unitKey ?? '').replace(/[^0-9]/g, '');
        return no ? no.slice(-2).padStart(2, '0') : '';
    }

    function randomSuffix(length) {
        const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ';
        let result = '';
        for (let i = 0; i < length; i++) {
            result += chars.charAt(Math.floor(Math.random() * chars.length));
        }
        return result;
    }

    /* ------------------------ //
    // 6. 행별 수정/삭제 버튼    //
    // ------------------------ */
    tbody.addEventListener('click', e => {
        const btn = e.target.closest('.row-edit, .row-delete');
        if (!btn) return;

        const tr = btn.closest('tr');
        const action = btn.classList.contains('row-edit') ? '수정' : '삭제';

        // TODO: 저장 로직 확정되면 연결
        console.log(`${action} 클릭:`, {
            classKey: tr.dataset.classKey,
            unitKey: tr.dataset.unitKey,
            settingValue: tr.querySelector('.setting-value')?.value ?? null
        });
    });

    /* ---------------- //
    // 7. 테이블 렌더링 //
    // ---------------- */
    function renderTable() {
        if (!rows.length) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align:center; padding:50px; color:#777; background-color:#fff;">
                        생성할 이북 코드 정보가 없습니다.
                    </td>
                </tr>`;
            return;
        }

        const list = [...rows];
        if (sortKey) {
            list.sort((a, b) => {
                const av = (a[sortKey] ?? '').toString();
                const bv = (b[sortKey] ?? '').toString();
                return sortAsc ? av.localeCompare(bv, 'ko') : bv.localeCompare(av, 'ko');
            });
        }

        tbody.innerHTML = list.map((r, i) => `
            <tr data-class-key="${r.classKey ?? ''}" data-unit-key="${r.unitKey ?? ''}"
                data-class-type="${r.classType ?? ''}">
                <td>${i + 1}</td>
                <td>${r.className ?? ''}</td>
                <td>${r.unitName ?? ''}</td>
                <td>${r.keyCode ?? '-'}</td>
                <td>${buildSettingCell(r)}</td>
                <td>${r.ggubun ?? ''}</td>
                <td>${r.mgubun ?? ''}</td>
                <td>
                    <button type="button" class="common-btn miniButton row-edit">수정</button>
                    <button type="button" class="common-btn miniButton row-delete">삭제</button>
                </td>
            </tr>`).join('');
    }

    /* 교재별 세팅값 옵션 (영재/수재의 5호·10호는 상/하반기라 따로 준다) */
    function settingOptionsOf(r) {
        if (HALF_TERM_CLASS_KEYS.includes(r.classKey) && HALF_TERM_UNIT_KEYS.includes(r.unitKey)) {
            return HALF_TERM_OPTIONS;
        }
        return SETTING_OPTIONS[r.classType] ?? [];
    }

    /* 지정된 교재만 세팅값 드롭다운을 노출 */
    function buildSettingCell(r) {
        if (r.levelUnit) return '-';                              // 급수는 이북 코드 대상이 아니다
        if (!SETTING_CLASS_KEYS.includes(r.classKey)) return '-';

        const options = settingOptionsOf(r);

        const optionHtml = ['<option value="">선택</option>']
            .concat(options.map(o =>
                `<option value="${o.value}"${o.value === r.settingValue ? ' selected' : ''}>${o.label}</option>`))
            .join('');

        return `<div class="select-wrap">
                    <select class="styled-select setting-value">${optionHtml}</select>
                </div>`;
    }

    /* ----------------------------------------- //
    // 6. 센터/연월 둘 다 있어야 서버 조회 실행  //
    // ----------------------------------------- */
    async function loadTableIfReady() {
        if (!selectedCenter) return;

        const {year, month} = getSelectedYearMonth();

        try {
            const res = await fetch("/admin/load/keycode", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({centerCode: selectedCenter, year, month})
            });

            const data = await res.json();
            rows = data?.response ?? [];
        } catch (e) {
            console.error("조회 실패:", e);
            rows = [];
        }

        fillAutoKeyCodes();
        renderTable();
    }

    /* ----------------- //
    // 7. 저장 버튼 클릭 //
    // ----------------- */
    document.querySelector('.record-save').addEventListener('click', async () => {

        if (!selectedCenter) {
            alert("학원을 선택해주세요!");
            return;
        }

        const {year, month} = getSelectedYearMonth();

        const body = {
            centerCode: selectedCenter,
            year: year,
            month: month,
            codes: collectTableData()
        };

        try {
            const res = await fetch("/admin/save/keycode", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(body)
            });

            const data = await res.json();
            if (!res.ok || data.success === false) {
                alert(data.error?.message ?? "저장 실패!");
                return;
            }
            alert(data.response ?? "저장되었습니다.");

        } catch (e) {
            console.error(e);
            alert("저장 실패!");
        }
    });

    /* ------------------------------- //
    // 8. 테이블 값 → JSON 구조로 변환 //
    // ------------------------------- */
    function collectTableData() {
        return rows.map(r => ({
            classKey: r.classKey ?? null,
            unitKey: r.unitKey ?? null,
            settingValue: r.settingValue ?? null,
            keyCode: r.keyCode ?? null
        }));
    }

    /* ------------------ //
    // 9. 기타 유틸 함수  //
    // ------------------ */
    function initCurrentMonth() {
        const now = new Date();
        monthInput.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
        renderMonthText();
    }

    function renderMonthText() {
        const {year, month} = getSelectedYearMonth();
        if (!year) return;
        monthDisplay.textContent = `${year}년 ${Number(month)}월`;
    }

    function getSelectedYearMonth() {
        const value = document.querySelector('.hidden-date').value; // yyyy-MM
        if (!value) return {year: null, month: null};
        const [year, month] = value.split('-');
        return {year, month};
    }

});
