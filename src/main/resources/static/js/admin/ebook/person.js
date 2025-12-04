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

        for (let m = 1; m <= 12; m++) {
            const month = String(m).padStart(2, '0');

            html += `<tr>
                        <td>${month}</td>`;

            for (let i = 1; i <= 6; i++) {
                html += `
                <td>
                    <div class="select-wrap">
                        <select name="bookiLevel[]" class="styled-select">
                            ${unitOptionsHtml}
                        </select>
                    </div>
                </td>`;
            }

            html += `</tr>`;
        }

        tbody.innerHTML = html;
    }


    /* ----------------------------------- //
    // 6. 서버 조회 데이터로 테이블 채우기 //
    // ----------------------------------- */
    function fillTable(data) {
        if (!data || !data.classes) return;

        const classMap = {};
        data.classes.forEach(c => classMap[c.class_key] = c.months);

        const selects = document.querySelectorAll('select[name="bookiLevel[]"]');

        let index = 0;

        for (let m = 1; m <= 12; m++) {
            const month = String(m).padStart(2, '0');

            ['K', 'M', 'J'].forEach(classKey => {
                const rows = classMap[classKey];
                const row = rows ? rows.find(r => r.month === month) : null;

                if (row) {
                    selects[index].value = row.unit_key || "";
                    selects[index + 1].value = row.sub_unit_key || "";
                }

                index += 2;
            });
        }
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

        try {
            const res = await fetch("/admin/save/person", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(body)
            });

            const data = await res.json();
            alert(data.message);

        } catch (e) {
            console.error(e);
            alert("저장 실패!");
        }
    });


    /* ------------------------------- //
    // 9. 테이블 값 → JSON 구조로 변환 //
    // ------------------------------- */
    function collectTableData() {
        const selects = document.querySelectorAll('select[name="bookiLevel[]"]');
        const arr = Array.from(selects).map(s => s.value || null);

        const classKeys = ["K", "M", "J"];
        const classes = [];
        let index = 0;

        for (let c = 0; c < classKeys.length; c++) {
            const months = [];

            for (let m = 1; m <= 12; m++) {
                const monthStr = String(m).padStart(2, '0');
                months.push({
                    month: monthStr,
                    unit_key: arr[index++],
                    sub_unit_key: arr[index++]
                });
            }

            classes.push({
                class_key: classKeys[c],
                months: months
            });
        }

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
