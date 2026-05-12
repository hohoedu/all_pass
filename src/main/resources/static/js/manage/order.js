document.addEventListener('DOMContentLoaded', () => {

    if (document.getElementById('reportTitle')) {
        initPrintPage();
        return;
    }
    
    function getUrlParam(key) {
        return new URLSearchParams(window.location.search).get(key);
    }

    function setUrlParams(year, month) {
        const params = new URLSearchParams(window.location.search);
        params.set('year', year);
        params.set('month', month);
        history.replaceState(null, '', '?' + params.toString());
    }

    /* ======= *
     *   LEFT  *
     * ======= */
    const monthInput = document.querySelector('.hidden-picker');
    const monthBtn = document.querySelector('.calendar-open');
    const monthDisplay = document.querySelector('.day-display');

    initMonthFromUrl();

    monthBtn.addEventListener('click', () => monthInput.showPicker());
    monthInput.addEventListener('change', onMonthChange);

    function initMonthFromUrl() {
        const urlYear = getUrlParam('year');
        const urlMonth = getUrlParam('month');

        let y, m;
        if (urlYear && urlMonth) {
            y = urlYear;
            m = String(urlMonth).padStart(2, '0');
        } else {
            const now = new Date();
            y = now.getFullYear();
            m = String(now.getMonth() + 1).padStart(2, '0');
        }

        monthInput.value = `${y}-${m}`;
        monthDisplay.insertAdjacentText('afterbegin', `${y}년 ${m}월`);
    }

    async function onMonthChange() {
        try {
            const date = new Date(monthInput.value);
            if (isNaN(date)) return;

            const year = date.getFullYear();
            const month = date.getMonth() + 1;
            const monthStr = String(month).padStart(2, "0");

            monthDisplay.childNodes[0].textContent = `${year}년 ${monthStr}월`;
            setUrlParams(year, monthStr); // ✅ URL 업데이트

            const response = await fetch(`/manage/order/base/list`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({yy: String(year), mm: monthStr})
            });

            if (!response.ok) return;
            const data = await response.json();
            renderLeftTable(data.response);
            checkOrderDeadline();

        } catch (e) {
            console.error("월 변경 처리 중 오류:", e);
        }
    }

    function renderLeftTable(list) {
        const tbody = document.getElementById("order-left-body");
        tbody.innerHTML = "";

        if (!list || list.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="3" style="text-align:center; padding:20px;">
                        등록된 수업이 없습니다.
                    </td>
                </tr>`;
            calculateTotal();
            return;
        }

        list.forEach(item => {
            tbody.insertAdjacentHTML("beforeend", `
                <tr data-class-key="${item.classKey}"
                    data-unit-key="${item.unitKey}">
                    <td>${item.className}</td>
                    <td>${item.unitName}</td>
                    <td>${item.baseCount}</td>
                </tr> 
            `);
        });

        calculateTotal();
    }

    const totalSpan = document.querySelector(".all-order span");

    function calculateTotal() {
        const rows = document.querySelectorAll("#order-left-body tr");
        let total = 0;

        rows.forEach(row => {
            const baseTd = row.querySelector("td:nth-child(3)");
            if (!baseTd) return;
            const baseCount = parseInt(baseTd.innerText) || 0;
            total += (baseCount);
        });

        if (totalSpan) {
            totalSpan.innerText = total;
        }
    }

    if (window.initialBaseList) {
        calculateTotal();
        renderLeftTable(window.initialBaseList);
    } else {
        calculateTotal();
    }

    /* ======= *
     *  RIGHT  *
     * ======= */
    const yearSelect = document.getElementById("order-year");
    const monthSelect = document.getElementById("order-month");

    initYearOptions();
    renderMonthOptions(yearSelect.value);
    setUrlParams(yearSelect.value, monthSelect.value);

    loadSavedOrder();
    loadOrderDetail();

    yearSelect.addEventListener("change", async () => {
        renderMonthOptions(yearSelect.value);
        setUrlParams(yearSelect.value, monthSelect.value);
        await loadSavedOrder();
        await loadOrderDetail();
    });

    monthSelect.addEventListener("change", async () => {
        setUrlParams(yearSelect.value, monthSelect.value);
        await loadSavedOrder();
        await loadOrderDetail();
    });

    function initYearOptions() {
        const ty = new Date().getFullYear();

        yearSelect.innerHTML = "";
        for (let y = ty - 1; y <= ty + 1; y++) {
            const opt = document.createElement("option");
            opt.value = y;
            opt.textContent = `${y}년`;
            if (y === ty) opt.selected = true;
            yearSelect.appendChild(opt);
        }
    }

// ===== 월 옵션 =====
    function renderMonthOptions(selectedYear) {
        const now = new Date();
        const ty = now.getFullYear();
        const tm = now.getMonth() + 1;

        monthSelect.innerHTML = "";
        for (let m = 1; m <= 12; m++) {
            const opt = document.createElement("option");
            opt.value = String(m).padStart(2, "0");
            opt.textContent = `${String(m).padStart(2, "0")}월`;

            const defaultMonth = Number(selectedYear) === ty ? tm : 1;
            if (m === defaultMonth) opt.selected = true;

            monthSelect.appendChild(opt);
        }
    }

    const rightBody = document.getElementById("order-right-body");

    async function loadSavedOrder() {
        try {
            const yy = yearSelect.value;
            const mm = monthSelect.value;

            const res = await fetch("/manage/order/list", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({yy, mm})
            });

            if (!res.ok) return;

            const data = await res.json();
            renderSavedOrder(data.response);

        } catch (e) {
            console.error("loadSavedOrder Error:", e);
        }
    }

    function renderSavedOrder(list) {
        rightBody.innerHTML = "";

        if (!list || list.length === 0) {
            rightBody.innerHTML = `
                <tr>
                    <td colspan="4" style="text-align:center; padding:20px;">
                        주문 내역이 없습니다.
                    </td>
                </tr>`;
            return;
        }

        list.forEach(item => {

            rightBody.insertAdjacentHTML("beforeend", `
                <tr>
                    <td>${item.className}</td>
                    <td>${item.unitName}</td>
                    <td>${item.totalCount}</td>
                    <td>${item.totalCount}</td>
                </tr>
            `);
        });
    }

    async function loadOrderDetail() {
        const bottomWrap = document.getElementById("preorder-bottom-wrap");
        if (!bottomWrap) return;

        try {
            const yy = yearSelect.value;
            const mm = monthSelect.value;
            if (!yy || !mm) return;

            // 로딩 표시
            bottomWrap.innerHTML = `
                <div class="bottom-loading">
                    <span>불러오는 중...</span>
                </div>`;

            const res = await fetch("/manage/order/detail/list", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({yy, mm})
            });

            if (!res.ok) {
                bottomWrap.innerHTML = `<p class="bottom-empty">데이터를 불러오지 못했습니다.</p>`;
                return;
            }

            const data = await res.json();
            renderOrderDetail(data.response, yy, mm);

        } catch (e) {
            console.error("loadOrderDetail Error:", e);
            bottomWrap.innerHTML = `<p class="bottom-empty">오류가 발생했습니다.</p>`;
        }
    }

    function renderOrderDetail(detail, yy, mm) {
        const bottomWrap = document.getElementById("preorder-bottom-wrap");
        if (!bottomWrap) return;

        if (!detail || detail.length === 0) {
            bottomWrap.innerHTML = `<p class="bottom-empty">${yy}년 ${mm}월 주문 내역이 없습니다.</p>`;
            return;
        }

        // className + unitName 기준으로 그룹핑
        const groupMap = new Map();

        detail.forEach(item => {
            const key = `${item.classKey}_${item.unitKey}`;
            const count = parseInt(item.count) || 0;

            if (!groupMap.has(key)) {
                groupMap.set(key, {
                    className: item.className,
                    unitName: item.unitName,
                    studentCount: 0,   // base
                    teacherCount: 0,   // (없으면 0)
                    addCount: 0,   // add - return
                    totalCount: 0
                });
            }

            const group = groupMap.get(key);

            if (item.type === 'base') {
                group.studentCount += count;
            } else if (item.type === 'ADD') {
                group.addCount += count;
            } else if (item.type === 'RETURN') {
                group.addCount -= count;  // 반품은 마이너스
            }
        });

        // 합계 계산 및 총합
        let sumStudent = 0, sumTeacher = 0, sumAdd = 0, sumTotal = 0;

        let rows = "";
        groupMap.forEach(group => {
            group.totalCount = group.studentCount + group.teacherCount + group.addCount;

            sumStudent += group.studentCount;
            sumTeacher += group.teacherCount;
            sumAdd += group.addCount;
            sumTotal += group.totalCount;

            // 추가 표시: 음수면 -N, 양수면 N
            const addDisplay = group.addCount < 0
                ? `<span style="color:red;">${group.addCount}</span>`
                : group.addCount;

            rows += `
            <tr>
                <td>${group.className}</td>
                <td>${group.unitName}</td>
                <td>${group.studentCount}</td>
                <td>${group.teacherCount}</td>
                <td>${addDisplay}</td>
                <td>${group.totalCount}</td>
            </tr>`;
        });

        const html = `
        <div class="list-code2">
            <h2>${yy}년 ${mm}월 선생님 주문 내역</h2>
            <table class="listRed">
                <colgroup>
                    <col width="20%">
                    <col width="20%">
                    <col width="15%">
                    <col width="15%">
                    <col width="15%">
                    <col width="15%">
                </colgroup>
                <thead>
                    <tr> 
                        <th rowspan="2">단계</th>
                        <th rowspan="2">교재</th>
                        <th colspan="4">수량</th>
                    </tr>
                    <tr>
                        <th>학생</th>
                        <th>선생님</th>
                        <th>추가</th>
                        <th>합계</th>
                    </tr>
                </thead>
                <tbody>
                    ${rows}
                    <tr class="listSum">
                        <td colspan="2">합계</td>
                        <td>${sumStudent}</td>
                        <td>${sumTeacher}</td>
                        <td>${sumAdd}</td>
                        <td>${sumTotal}</td>
                    </tr>
                </tbody>
            </table>
        </div>`;

        bottomWrap.innerHTML = html;
    }

    /* =============================== *
     *  주문 마감 기능 (NEW)
     * =============================== */
    const saveBtn = document.querySelector(".save-btn");

    async function fetchDeadlineDay() {
        const res = await fetch('/manage/order/deadline', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!res.ok) return 10;

        const data = await res.json();
        console.log(data.response); // ✅ "20" 같은 문자열
        return parseInt(data.response) || 10; // ✅ 숫자로 변환, 실패 시 기본값 15
    }

    async function checkOrderDeadline() {
        const deadlineDay = await fetchDeadlineDay();

        const now = new Date();
        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth();

        const deadlineStart = new Date(currentYear, currentMonth, deadlineDay, 23, 59, 59);
        const deadlineEnd = new Date(currentYear, currentMonth + 1, 0, 23, 59, 59);

        const isDeadlinePeriod = now >= deadlineStart && now <= deadlineEnd;

        saveBtn.disabled = isDeadlinePeriod;
        saveBtn.classList.toggle('disabled', isDeadlinePeriod);
        saveBtn.style.opacity = isDeadlinePeriod ? '0.5' : '1';
        saveBtn.style.cursor = isDeadlinePeriod ? 'not-allowed' : 'pointer';

        return isDeadlinePeriod;
    }

    function showDeadlineModal() {
        // 모달 HTML 생성
        const modalHTML = `
            <div id="deadline-modal" style="
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background-color: rgba(0, 0, 0, 0.5);
                display: flex;
                justify-content: center;
                align-items: center;
                z-index: 9999;
            ">
                <div style="
                    background: white;
                    padding: 30px;
                    border-radius: 8px;
                    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                    text-align: center;
                    max-width: 400px;
                ">
                    <h3 style="margin-bottom: 20px; color: #333;">주문 마감 안내</h3>
                    <p style="margin-bottom: 25px; line-height: 1.6; color: #666;">
                        주문이 마감되었습니다.<br>
                        <strong>추가 주문 / 반품</strong> 탭을 이용해주세요.
                    </p>
                    <button id="modal-close-btn" style="
                        background-color: #007bff;
                        color: white;
                        border: none;
                        padding: 10px 30px;
                        border-radius: 4px;
                        cursor: pointer;
                        font-size: 16px;
                    ">확인</button>
                </div>
            </div>
        `;

        // 모달을 body에 추가
        document.body.insertAdjacentHTML('beforeend', modalHTML);

        // 확인 버튼 클릭 시 모달 제거
        document.getElementById('modal-close-btn').addEventListener('click', () => {
            document.getElementById('deadline-modal').remove();
        });

        // 배경 클릭 시에도 모달 제거
        document.getElementById('deadline-modal').addEventListener('click', (e) => {
            if (e.target.id === 'deadline-modal') {
                document.getElementById('deadline-modal').remove();
            }
        });
    }

    // 페이지 로드 시 마감 여부 체크
    checkOrderDeadline();

    saveBtn.addEventListener("click", async () => {
        // ✅ await 추가 (없으면 Promise 객체가 반환되어 항상 truthy)
        if (await checkOrderDeadline()) {
            showDeadlineModal();
            return;
        }

        if (!confirm("교재를 주문하시겠습니까?")) return;


        try {
            const ymRaw = monthInput.value;
            if (!ymRaw) {
                alert("주문년월을 선택해주세요.");
                return;
            }

            const ym = ymRaw.replace("-", "");
            const rows = document.querySelectorAll("#order-left-body tr");
            const insertOrders = [];

            rows.forEach(row => {
                const baseCount = parseInt(row.querySelector("td:nth-child(3)").innerText);

                insertOrders.push({
                    classKey: row.dataset.classKey,
                    unitKey: row.dataset.unitKey,
                    baseCount: baseCount,
                });
            });

            const requestData = {
                yy: ym.substring(0, 4),
                mm: ym.substring(4, 6),
                insertOrders: insertOrders
            };

            const res = await fetch("/manage/order/save", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(requestData)
            });

            if (!res.ok) {
                alert("저장에 실패했습니다.");
                return;
            }
            alert("저장 되었습니다.");
            window.location.reload();

        } catch (e) {
            alert("에러 발생");
        }
    });


    /* =============================== *
 *  주문 내역 조회 (인쇄 페이지)
 * =============================== */
    const centerOrderListBtn = document.getElementById('center-order-list');

    if (centerOrderListBtn) {
        centerOrderListBtn.addEventListener('click', () => {
            const yy = yearSelect.value;
            const mm = monthSelect.value;

            if (!yy || !mm) {
                alert('년월을 선택해주세요.');
                return;
            }

            window.open(
                `/manage/order/print?yy=${yy}&mm=${mm}`,
                '_blank',
                'width=900,height=800,scrollbars=yes'
            );
        });
    }

    /* =============================== *
    *  인쇄 페이지 (print-order.html)
    * =============================== */
    function initPrintPage() {
        const params = new URLSearchParams(window.location.search);
        const yy = params.get('yy');
        const mm = params.get('mm');

        // 캘린더 피커 초기화
        const picker = document.getElementById('print-month-picker');
        const calBtn = document.getElementById('print-calendar-btn');

        picker.value = `${yy}-${mm}`;

        const now = new Date();
        picker.max = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;

        calBtn.addEventListener('click', () => picker.showPicker());

        picker.addEventListener('change', () => {
            const [year, month] = picker.value.split('-');
            fetchAndRender(year, month);
        });
    }

    function fetchAndRender(yy, mm) {
        fetch('/manage/order/print/data', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({yy, mm})
        })
            .then(res => res.json())
            .then(data => {

                const table = document.querySelector('.report-table');
                table.querySelectorAll('tbody').forEach(tb => tb.remove());

                document.getElementById('tableFoot').innerHTML = '';

                const titleEl = document.getElementById('reportTitle');
                titleEl.textContent = `${yy}년 ${mm}월 ${titleEl.dataset.center} 주문 내역서 집계`;

                // URL 업데이트 (새로고침 없이)
                const params = new URLSearchParams(window.location.search);
                params.set('yy', yy);
                params.set('mm', mm);
                history.replaceState(null, '', '?' + params.toString());

                renderPrintReport(data.response);
            })
            .catch(err => console.error('데이터 로드 실패:', err));
    }

    function renderPrintReport(list) {
        if (!list || list.length === 0) return;

        const table = document.querySelector('.report-table');
        const tfoot = document.getElementById('tableFoot');

        function tdCell(val, cls = '') {
            const d = val === 0 ? `<span class="zero">0</span>` : val;
            return `<td class="${cls}">${d}</td>`;
        }

        // 선생님별 그룹핑
        const teacherMap = new Map();
        list.forEach(item => {
            if (!teacherMap.has(item.userName)) {
                teacherMap.set(item.userName, []);
            }
            teacherMap.get(item.userName).push([
                item.className,
                item.unitName,
                item.studentCount || 0,
                item.teacherCount || 0,
                item.addCount || 0,
                item.totalCount || 0,
                item.timeTable || 0
            ]);
        });

        const total = [0, 0, 0, 0, 0];

        teacherMap.forEach((rows, teacherName) => {
            const tbody = document.createElement('tbody');
            table.insertBefore(tbody, tfoot);

            rows.forEach((row, idx) => {
                const [단계, 교재, 학생, 선생님, 추가, 합계, 시간표] = row;
                const tr = document.createElement('tr');
                tr.className = 'row-data';
                tr.innerHTML = `
                ${idx === 0 ? `<td class="cell-teacher" rowspan="${rows.length}">${teacherName}</td>` : ''}
                <td class="cell-left">${단계}</td>
                <td>${교재}</td>
                ${tdCell(학생)} ${tdCell(선생님)} ${tdCell(추가)}
                ${tdCell(합계, 'col-total')}
                ${tdCell(시간표, 'col-timetable')}
            `;
                tbody.appendChild(tr);
            });

            // 소계
            const s = i => rows.reduce((a, r) => a + r[i], 0);
            const sr = document.createElement('tr');
            sr.className = 'row-subtotal';
            sr.innerHTML = `
            <td colspan="3" class="cell-label">&nbsp;&nbsp;${teacherName} 계</td>
            ${tdCell(s(2))} ${tdCell(s(3))} ${tdCell(s(4))}
            ${tdCell(s(5), 'col-total')}
            ${tdCell(s(6), 'col-timetable')}
        `;
            tbody.appendChild(sr);

            [2, 3, 4, 5, 6].forEach((col, i) => total[i] += s(col));
        });

        // 총계 → tfoot
        const gr = document.createElement('tr');
        gr.className = 'row-grand-total';
        gr.innerHTML = `
        <td colspan="3" class="cell-label">총   계</td>
        ${tdCell(total[0])} ${tdCell(total[1])} ${tdCell(total[2])}
        ${tdCell(total[3], 'col-total')}
        ${tdCell(total[4], 'col-timetable')}
    `;
        tfoot.appendChild(gr);
    }
});