document.addEventListener('DOMContentLoaded', () => {

    /* ---------------------------------- //
    // 1. 기본 상태 변수 & DOM 요소 등록  //
    // ---------------------------------- */

    // 유곡점: 외부(secondary) DB 사용 지점 → 현재는 조회 전용
    const SECONDARY_CENTER_CODE = 'ULS001';

    let selectedCenter = null;
    let selectedYear = null;

    const yearSelect = document.querySelector('.year-select');

    initYearSelect();

    /* ------------------ //
    // 2. 연도 선택 이벤트 //
    // ------------------ */

    yearSelect.addEventListener('change', async () => {
        selectedYear = yearSelect.value;
        await loadTableIfReady();
    });

    /* ----------------------------------------- //
    // 3. unitKey / unitName 옵션 HTML 미리 확보 //
    // ----------------------------------------- */
    const unitOptionsHtml =
        document.querySelector("#unit-options-source").innerHTML;


    /* ------------------- //
    // 4. 학원 선택 이벤트 //
    // ------------------- */
    document.querySelectorAll('.stu-chocie-table tr').forEach(row => {
        row.addEventListener('click', async () => {
            selectedCenter = row.dataset.centerCode;

            const centerName = row.querySelector('.names').textContent.trim();
            document.querySelector('.small-title').textContent = centerName;

            await loadTableIfReady();
        });
    });


    /* ---------------- //
    // 5. 테이블 렌더링 //
    // ---------------- */

    const classKeys = ["K", "M", "J"];     // 3개 class 순서 정의
    const types = ["unit", "sub"];         // 메인, 서브

    function renderTable() {
        const tbody = document.querySelector('.ebook-person-table tbody');

        let html = "";

        for (let m = 1; m <= 12; m++) {
            const month = String(m).padStart(2, '0');

            html += `<tr>
                        <td>${month}</td>`;

            // 6개의 셀 생성 (K-unit, K-sub, M-unit, M-sub, J-unit, J-sub)
            classKeys.forEach(classKey => {
                types.forEach(type => {
                    html += `
                       <td>
                           <div class="select-wrap">
                               <!-- 수정: name 제거, data-class / data-month / data-type 추가 -->
                               <select 
                                   class="styled-select"
                                   data-class="${classKey}"
                                   data-month="${month}"
                                   data-type="${type}">
                                   ${unitOptionsHtml}
                               </select>
                           </div>
                       </td>`;
                });
            });

            html += `</tr>`;
        }

        tbody.innerHTML = html;
    }


    /* --------------------------------------------------------------- //
    // 5-1. 유곡점: 반 구분이 없어 K/M/J 를 같은 값으로 묶어서 편집한다 //
    // --------------------------------------------------------------- */
    document.querySelector('.ebook-person-table tbody')
        .addEventListener('change', (e) => {
            const changed = e.target;
            if (!changed.matches('select.styled-select')) return;
            if (selectedCenter !== SECONDARY_CENTER_CODE) return;

            const {month, type} = changed.dataset;

            classKeys.forEach(classKey => {
                const target = document.querySelector(
                    `select[data-class="${classKey}"][data-month="${month}"][data-type="${type}"]`
                );
                if (target && target !== changed) target.value = changed.value;
            });
        });


    /* ----------------------------------- //
    // 6. 서버 조회 데이터로 테이블 채우기 //
    // ----------------------------------- */
    function fillTable(data) {
        if (!data || !data.classes) return;

        data.classes.forEach(c => {
            const classKey = c.class_key;

            c.months.forEach(monthData => {
                const month = monthData.month;

                // main select
                const mainSel = document.querySelector(
                    `select[data-class="${classKey}"][data-month="${month}"][data-type="unit"]`
                );
                if (mainSel) mainSel.value = monthData.unit_key || "";

                // sub select
                const subSel = document.querySelector(
                    `select[data-class="${classKey}"][data-month="${month}"][data-type="sub"]`
                );
                if (subSel) subSel.value = monthData.sub_unit_key || "";
            });
        });
    }


    /* ---------------------------------------- //
    // 7. 연도/센터 둘 다 있어야 서버 조회 실행 //
    // ---------------------------------------- */
    async function loadTableIfReady() {
        if (!selectedCenter || !selectedYear) return;

        const body = {
            centerCode: selectedCenter,
            year: selectedYear
        };

        let data = null;

        try {
            const res = await fetch("/admin/load/person", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(body)
            });

            const text = await res.text();
            data = text ? JSON.parse(text) : null;

        } catch (e) {
            console.error("조회 실패:", e);
        }

        renderTable();

        if (data && data.classes) {
            fillTable(data);
        }
    }


    /* ----------------- //
    // 8. 저장 버튼 클릭 //
    // ----------------- */

    document.querySelector('.record-save').addEventListener('click', async () => {

        if (!selectedCenter) {
            alert("학원을 선택해주세요!");
            return;
        }

        // 유곡점은 외부(유곡) DB에 직접 반영되므로 한 번 더 확인
        if (selectedCenter === SECONDARY_CENTER_CODE
            && !confirm(`${selectedYear}년 울산 유곡점 세팅을 유곡 프로그램에 반영합니다. 저장할까요?`)) {
            return;
        }

        const year = selectedYear;
        const classes = collectTableData();

        const body = {
            centerCode: selectedCenter,
            year: year,
            classes: classes
        };

        console.log(JSON.stringify(body));

        try {
            const res = await fetch("/admin/save/person", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(body)
            });

            const data = await res.json();
            alert(data.response);

        } catch (e) {
            console.error(e);
            alert("저장 실패!");
        }
    });


    /* ------------------------------- //
    // 9. 테이블 값 → JSON 구조로 변환 //
    // ------------------------------- */
    function collectTableData() {
        const classes = [];

        classKeys.forEach(classKey => {
            const months = [];

            for (let m = 1; m <= 12; m++) {
                const month = String(m).padStart(2, '0');

                // 수정: data-class / data-month / data-type 기반으로 값 수집
                const mainSel = document.querySelector(
                    `select[data-class="${classKey}"][data-month="${month}"][data-type="unit"]`
                );
                const subSel = document.querySelector(
                    `select[data-class="${classKey}"][data-month="${month}"][data-type="sub"]`
                );

                months.push({
                    month: month,
                    unit_key: mainSel?.value || null,
                    sub_unit_key: subSel?.value || null
                });
            }

            classes.push({ class_key: classKey, months });
        });

        return classes;
    }


    /* ------------------ //
    // 10. 기타 유틸 함수 //
    // ------------------ */
    // 올해 기준 -5 ~ +1 년 목록 생성, 기본값은 올해
    function initYearSelect() {
        const thisYear = new Date().getFullYear();

        let html = "";
        for (let y = thisYear + 1; y >= thisYear - 5; y--) {
            html += `<option value="${y}">${y}년</option>`;
        }
        yearSelect.innerHTML = html;

        yearSelect.value = String(thisYear);
        selectedYear = String(thisYear);
    }

});
