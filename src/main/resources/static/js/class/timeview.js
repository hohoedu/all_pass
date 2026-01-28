let draggedData = null;

document.addEventListener("DOMContentLoaded", () => {

    const monthInput = document.getElementById("monthPickerInput");
    const openBtn = document.getElementById("openMonthPicker");
    const currentMonthLabel = document.getElementById("currentMonth");
    const printBtn = document.getElementById("time-view-print");
    if (!monthInput || !openBtn || !currentMonthLabel) return;

    initCurrentMonth();
    bindEvents();

    /** 초기 월 세팅 */
    function initCurrentMonth() {
        try {
            // URL 파라미터 확인
            const urlParams = new URLSearchParams(window.location.search);
            const urlYear = urlParams.get('year');
            const urlMonth = urlParams.get('month');

            let year, month;

            if (urlYear && urlMonth) {
                // URL에 파라미터가 있으면 사용
                year = urlYear;
                month = String(urlMonth).padStart(2, "0");
            } else {
                // 없으면 현재 날짜 사용
                const now = new Date();
                year = now.getFullYear();
                month = String(now.getMonth() + 1).padStart(2, "0");
            }

            monthInput.value = `${year}-${month}`;
            updateMonthLabel(year, month);

            // 초기 데이터 로드 (URL 파라미터가 있는 경우에만)
            if (urlYear && urlMonth) {
                loadMonthlyData(year, month);
            }
        } catch (err) {
            console.error("initCurrentMonth error:", err);
        }
    }


    printBtn.addEventListener("click", () => {
        const ym = document.getElementById('monthPickerInput').value.replace('-', '');
        const userCode = document.getElementById('teacher-select')?.value;
        const centerCode = document.getElementById('')
        // window.open(`/class/print-timeview?ym=${ym}&userCode=${userCode}`);
        printTimeView(ym, userCode);
    });

    function printTimeView(ym, userCode) {
        const iframe = document.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = `/class/print-timeview?ym=${ym}&userCode=${userCode}`;

        iframe.onload = () => {
            iframe.contentWindow.print();
        };

        document.body.appendChild(iframe);
    }

    /** 이벤트 바인딩 */
    function bindEvents() {

        // 달력 열기
        openBtn.addEventListener("click", () => {
            monthInput.showPicker();
        });

        // 월 변경 → 서버에서 해당 월 시간표 + 회원현황 다시 가져오기
        monthInput.addEventListener("change", async () => {
            const selected = new Date(monthInput.value);
            if (isNaN(selected)) return;

            const year = selected.getFullYear();
            const month = String(selected.getMonth() + 1).padStart(2, "0");

            updateMonthLabel(year, month);

            // URL 파라미터 업데이트
            updateURLParams(year, month);

            await loadMonthlyData(year, month);
        });
    }

    function updateMonthLabel(year, month) {
        currentMonthLabel.textContent = `${year}년 ${month}월`;
    }

    /**
     * URL 파라미터 업데이트
     */
    function updateURLParams(year, month) {
        const url = new URL(window.location);
        url.searchParams.set('year', year);
        url.searchParams.set('month', month);

        // 브라우저 히스토리에 추가 (뒤로가기 가능)
        window.history.pushState({year, month}, '', url);

        // 또는 히스토리에 추가하지 않고 URL만 변경하려면:
        // window.history.replaceState({year, month}, '', url);
    }

    document.getElementById("teacher-select")
        ?.addEventListener("change", () => {
            const selected = new Date(monthInput.value);
            if (isNaN(selected)) return;

            const year = selected.getFullYear();
            const month = String(selected.getMonth() + 1).padStart(2, "0");

            loadMonthlyData(year, month);
        });

    /**
     * 서버에서 월별 데이터 조회
     * (시간표 테이블 전체 + 회원 현황 테이블 HTML 받아서 교체)
     */
    async function loadMonthlyData(year, month) {
        try {
            const teacher = document.getElementById("teacher-select")?.value;

            const res = await fetch(`/class/timetable/view`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify({
                    userCode: teacher,
                    year: year,
                    month: month
                })
            });

            if (!res.ok) {
                console.error("조회 실패:", res.status);
                return;
            }

            const data = await res.json();
            const tables = data.response;

            console.log(tables);
            renderTimetableTable(tables);
        } catch (err) {
            console.error("loadMonthlyData error:", err);
        }

        function renderTimetableTable(tables) {

            // 요일을 항상 고정으로 렌더링
            const dayOrder = ["mon", "tue", "wed", "thu", "fri", "sat"];
            const dayLabel = {
                mon: "월",
                tue: "화",
                wed: "수",
                thu: "목",
                fri: "금",
                sat: "토",
            };

            // 교시도 항상 1~6 고정
            const periods = [1, 2, 3, 4, 5, 6];

            let html = `
        <thead>
            <tr>
                <th style="padding:0;">교시</th>
                ${dayOrder.map(d => `<th>${dayLabel[d]}</th>`).join("")}
            </tr>
        </thead>
        <tbody>
    `;

            periods.forEach(period => {
                html += `<tr>`;
                html += `<td>${period}</td>`;

                // 요일 칸 7개 모두 렌더링
                dayOrder.forEach(day => {

                    // 해당 요일 + 교시 데이터 찾기
                    const tt = tables.find(t => t.dayname === day && Number(t.periodNo) === period);

                    // 데이터 없으면 빈 칸
                    if (!tt) {
                        html += `<td style="height:150px;"></td>`;
                        return;
                    }

                    const boxColor = tt.classType === "1" ? "pink" : "blue";

                    html += `
                <td style="height:150px;">
                    <div class="timetable-lookup-box ${boxColor}">
                        <div class="header">
                            ${tt.startTime} ~ ${tt.endTime}<br>
                            <strong>${tt.className} ${tt.unitName ?? ""}</strong>
                        </div>
                        <div class="inner-grid" draggable="true">
                            ${tt.students.map(s => `
                                <div data-name="${s.studentName}">
                                    ${s.studentName} ${s.gradeName ? `(${s.gradeName})` : ``}
                                </div>
                            `).join("")}
                        </div>
                    </div>
                </td>
            `;
                });

                html += `</tr>`;
            });

            html += `</tbody>`;

            document.querySelector(".timetable-lookup-table").innerHTML = html;
        }
    }


    document.addEventListener("dragstart", (e) => {
        const grid = e.target.closest(".inner-grid");
        if (!grid) return;

        const timeTableKey = grid.dataset.timeTableKey;

        const [yy, mm] = document.getElementById('currentMonth')
            .textContent.trim()
            .match(/(\d{4})년\s*(\d{1,2})월/)
            .slice(1, 3)
            .map((v, i) => i === 1 ? v.padStart(2, '0') : v);

        dragAssignments = [...grid.children].map(el => ({
            studentId: el.dataset.studentId,
            weekNo: el.dataset.weekNo,   // 기존 배정 정보
            yy,
            mm
        }));

        e.dataTransfer.setData("text/plain", "students");
    });

    document.addEventListener("dragover", (e) => {
        if (e.target.closest(".inner-grid")) {
            e.preventDefault();
        }
    });

    document.addEventListener("drop", async (e) => {
        const targetGrid = e.target.closest(".inner-grid");
        if (!targetGrid || !dragAssignments) return;

        e.preventDefault();

        const targetTimeTableKey = targetGrid.dataset.timeTableKey;

        const assignments = dragAssignments.map(a => ({
            ...a,
            timeTableKey: targetTimeTableKey
        }));

        console.log('assignments=' + assignments);
        await fetch('/class/add_student', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({assignments})
        });

        dragAssignments = null;

        // window.location.reload();
    });

    // 브라우저 뒤로가기/앞으로가기 처리
    window.addEventListener('popstate', (event) => {
        if (event.state && event.state.year && event.state.month) {
            const {year, month} = event.state;
            monthInput.value = `${year}-${month}`;
            updateMonthLabel(year, month);
            loadMonthlyData(year, month);
        }
    });
});