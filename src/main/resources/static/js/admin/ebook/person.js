document.addEventListener('DOMContentLoaded', () => {

    /* ---------------------------------- //
    // 1. 기본 상태 변수 & DOM 요소 등록  //
    // ---------------------------------- */

    let selectedCenter = null;
    let selectedYear = null;

    const monthInput = document.querySelector('.hidden-picker');
    const monthBtn = document.querySelector('.calendar-open');
    const monthDisplay = document.querySelector('.day-display');

    initCurrentMonth();

    /* ------------------------------------- //
    // 2. 월 선택 이벤트 (input[type=month]) //
    // ------------------------------------- */

    monthBtn.addEventListener('click', () => monthInput.showPicker());
    monthInput.addEventListener('change', async () => {
        await onMonthChange();
        selectedYear = getSelectedYear();
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

    function renderTable() {
        const tbody = document.querySelector('.ebook-person-table tbody');

        let html = "";

        const classKeys = ["K", "M", "J"];     // 추가: 3개 class 순서 정의
        const types = ["unit", "sub"];        // 메인, 서브

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

        renderTable();

        const body = {
            centerCode: selectedCenter,
            year: selectedYear
        };

        try {
            const res = await fetch("/admin/load/person", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(body)
            });

            const data = await res.json();

            if (data && data.classes) {
                fillTable(data);
            }
        } catch (e) {
            console.error("조회 실패:", e);
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

        const year = getSelectedYear();
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
        const classKeys = ["K", "M", "J"];
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
    function initCurrentMonth() {
        const now = new Date();
        const year = now.getFullYear();
        const month = now.getMonth() + 1;

        monthInput.value = `${year}-${String(month).padStart(2, '0')}`;
        monthDisplay.insertAdjacentText('afterbegin', `${year}년`);

        selectedYear = year.toString();
    }

    async function onMonthChange() {
        const date = new Date(monthInput.value);
        if (isNaN(date)) return;
        const year = date.getFullYear();
        monthDisplay.childNodes[0].textContent = `${year}년`;
    }

    function getSelectedYear() {
        const yearMonth = document.querySelector('.hidden-date').value;
        return yearMonth.split('-')[0];
    }

});
