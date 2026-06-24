// URL에서 날짜 가져오기
let year = null;
let month = null;

document.addEventListener('DOMContentLoaded', function () {
    const openMonthPicker = document.getElementById('openMonthPicker');
    const monthPickerInput = document.getElementById('monthPickerInput');
    const currentMonthElement = document.getElementById('currentMonth');
    if (!openMonthPicker || !monthPickerInput || !currentMonthElement) return;

    const url = new URL(window.location.href);
    const urlParams = url.searchParams;

    year = urlParams.get('year');
    month = urlParams.get('month');

    if (year && month) {
        currentMonthElement.textContent = `${year}년 ${parseInt(month, 10)}월`;
    }
    openMonthPicker.addEventListener('click', () => {
        monthPickerInput.showPicker?.() || monthPickerInput.click();
    });

    monthPickerInput.addEventListener('change', () => {
        const [selectedYear, selectedMonth] = monthPickerInput.value.split('-');

        let newUrl = '';

        if (url.pathname.includes('timetable')) {
            newUrl = `/class/timetable?year=${selectedYear}&month=${selectedMonth}`;
        } else if (url.pathname.includes('timeview')) {
            newUrl = `/class/timeview?year=${selectedYear}&month=${selectedMonth}`;
        } else if (url.pathname.includes('remedial')) {
            newUrl = `/class/remedial?year=${selectedYear}&month=${selectedMonth}`;
        }
        console.log(newUrl);
        window.location.href = newUrl;
    });
});

// 시간 입력
document.addEventListener('DOMContentLoaded', () => {

    document.body.addEventListener('input', e => {
        if (!e.target.matches('.time_input')) return;

        const input = e.target;
        let v = input.value.replace(/[^0-9]/g, '');
        if (v.length >= 3) {
            v = v.slice(0, 2) + ':' + v.slice(2, 4);
        }
        input.value = v;
    });
});

// 센터별 수업 주차 설정하기
document.addEventListener("DOMContentLoaded", () => {
    try {
        function getUrlParams() {
            const url = new URL(window.location.href);
            return {
                year: url.searchParams.get("year"),
                month: url.searchParams.get("month")
            };
        }

        const {year: baseYear, month: baseMonth} = getUrlParams();

        const modal = document.getElementById("weekModal");
        const openBtn = document.getElementById("openWeekModalBtn");
        const closeBtn = document.querySelector(".week-modal-close");

        const calendarGrid = document.getElementById("calendarGrid");
        const calendarMonthLabel = document.getElementById("calendarMonthLabel");
        const prevBtn = document.getElementById("calendarPrevBtn");
        const nextBtn = document.getElementById("calendarNextBtn");

        if (!modal || !openBtn || !closeBtn || !calendarGrid) {
            return;
        }

        const weekColors = {
            1: "#fecec8",
            2: "#c1e6c4",
            3: "#d5e4f7",
            4: "#d3c7e9"
        };

        const weekData = {
            year: baseYear,
            month: baseMonth,
            week: {
                1: {sun: "", mon: "", tue: "", wed: "", thu: "", fri: "", sat: ""},
                2: {sun: "", mon: "", tue: "", wed: "", thu: "", fri: "", sat: ""},
                3: {sun: "", mon: "", tue: "", wed: "", thu: "", fri: "", sat: ""},
                4: {sun: "", mon: "", tue: "", wed: "", thu: "", fri: "", sat: ""}
            }
        };

        document.getElementById("week-modal-save-btn").addEventListener("click", async () => {
            try {
                const res = await fetch("/class/week/save", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify(weekData)
                });

                if (res.ok) {
                    alert("저장되었습니다.");
                    modal.style.display = "none";
                } else {
                    alert("저장 실패");
                }
            } catch (err) {
                console.error("저장 오류:", err);
            }
        });

        openBtn.addEventListener("click", async () => {
            modal.style.display = "flex";
            resetWeekData();
            try {
                const res = await fetch("/class/week/get", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({
                        year: baseYear,
                        month: baseMonth
                    })
                });

                const json = await res.json();
                const list = json.response;

                weekData.week = {
                    1: {},
                    2: {},
                    3: {},
                    4: {}
                };

                list.forEach(row => {
                    const weekNum = parseInt(row.week.replace("ju_", ""));

                    weekData.week[weekNum] = {
                        mon: row.mon,
                        tue: row.tue,
                        wed: row.wed,
                        thu: row.thu,
                        fri: row.fri,
                        sat: row.sat,
                        sun: row.sun
                    };
                });

            } catch (err) {
                console.error("주차 데이터 조회 오류:", err);
            }

            currentYear = parseInt(baseYear);
            currentMonth = parseInt(baseMonth);
            renderCalendar(new Date(currentYear, currentMonth - 1));
        });

        closeBtn.addEventListener("click", () => {
            resetWeekData();
            modal.style.display = "none";
        });

        modal.addEventListener("click", e => {
            if (e.target === modal) {
                resetWeekData();
                modal.style.display = "none";
            }
        });

        function resetWeekData() {
            weekData.week = {};
            for (let i = 1; i <= 4; i++) {
                weekData.week[i] = {};
            }
        }

        let selectedWeek = null;

        const weekButtons = document.querySelectorAll(".week-modal-week-btn");
        weekButtons.forEach(btn => {
            btn.addEventListener("click", () => {
                weekButtons.forEach(b => b.classList.remove("active"));
                btn.classList.add("active");
                selectedWeek = parseInt(btn.dataset.week);
            });
        });

        const weekdayMap = ["sun", "mon", "tue", "wed", "thu", "fri", "sat"];

        function getWeekday(year, month, day) {
            return weekdayMap[new Date(year, month - 1, day).getDay()];
        }

        function onDateClick(cell, day, year, month) {
            if (!selectedWeek) {
                alert("주차를 먼저 선택해주세요.");
                return;
            }
            if (cell.classList.contains("disabled")) return;

            const yyyyMMDD = `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
            const weekday = getWeekday(year, month, day);

            if (weekData.week[selectedWeek][weekday] === yyyyMMDD) {
                weekData.week[selectedWeek][weekday] = "";
                cell.style.background = "#fafafa";
                return;
            }

            for (let w = 1; w <= 4; w++) {
                for (let key in weekData.week[w]) {
                    if (weekData.week[w][key] === yyyyMMDD) {
                        alert(`${yyyyMMDD}은 이미 ${w}주차에서 선택되었습니다.`);
                        return;
                    }
                }
            }

            if (weekData.week[selectedWeek][weekday]) {
                const weekdayToNumber = {
                    mon: '월',
                    tue: '화',
                    wed: '수',
                    thu: '목',
                    fri: '금',
                    sat: '토',
                    sun: '일'
                };
                const dayname = weekdayToNumber[weekday]
                alert(`${selectedWeek}주차의 ${dayname}요일은 이미 선택되었습니다.`);
                return;
            }

            weekData.week[selectedWeek][weekday] = yyyyMMDD;
            cell.style.background = weekColors[selectedWeek];
        }

        function repaintSelectedDates(year, month) {
            const cells = document.querySelectorAll(".week-modal-calendar-cell");

            cells.forEach(cell => {
                const day = parseInt(cell.textContent);
                if (!day || cell.classList.contains("disabled")) return;

                const yyyyMMDD = `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;

                for (let w = 1; w <= 4; w++) {
                    for (let key in weekData.week[w]) {
                        if (weekData.week[w][key] === yyyyMMDD) {
                            cell.style.background = weekColors[w];
                        }
                    }
                }
            });
        }


        function renderCalendar(date) {
            const year = date.getFullYear();
            const month = date.getMonth() + 1;

            calendarMonthLabel.textContent = `${year}년 ${month}월`;

            const firstDay = new Date(year, month - 1, 1).getDay();
            const daysInMonth = new Date(year, month, 0).getDate();
            const prevDays = new Date(year, month - 1, 0).getDate();

            calendarGrid.innerHTML = "";

            // 이전달
            for (let i = 0; i < firstDay; i++) {
                const cell = document.createElement("div");
                cell.className = "week-modal-calendar-cell disabled";
                cell.textContent = prevDays - (firstDay - i - 1);
                calendarGrid.appendChild(cell);
            }

            // 현재달
            for (let d = 1; d <= daysInMonth; d++) {
                const cell = document.createElement("div");
                cell.className = "week-modal-calendar-cell";
                cell.textContent = d;
                cell.addEventListener("click", () => onDateClick(cell, d, year, month));
                calendarGrid.appendChild(cell);
            }

            // 다음달
            const total = firstDay + daysInMonth;
            const remaining = 42 - total;

            for (let i = 1; i <= remaining; i++) {
                const cell = document.createElement("div");
                cell.className = "week-modal-calendar-cell disabled";
                cell.textContent = i;
                calendarGrid.appendChild(cell);
            }

            repaintSelectedDates(year, month);
        }

        let currentYear = parseInt(baseYear);
        let currentMonth = parseInt(baseMonth);

        prevBtn.addEventListener("click", () => {
            currentMonth--;
            if (currentMonth < 1) {
                currentMonth = 12;
                currentYear--;
            }
            renderCalendar(new Date(currentYear, currentMonth - 1));
        });

        nextBtn.addEventListener("click", () => {
            currentMonth++;
            if (currentMonth > 12) {
                currentMonth = 1;
                currentYear++;
            }
            renderCalendar(new Date(currentYear, currentMonth - 1));
        });


    } catch (err) {
        console.error("모달 초기화 오류:", err);
    }
});


// 수업별 진도 가져오기
function getClassUnits() {
    const rawJson = document.getElementById("classUnits").value;
    try {
        return JSON.parse(rawJson);
    } catch (e) {
        console.error("classUnits 파싱 실패", e);
        return {};
    }
}

// 수업 변경 시 진도 변경
document.addEventListener("DOMContentLoaded", () => {
    const rawJson = document.getElementById("classUnits")?.value;
    if (!rawJson) return;

    const classUnits = JSON.parse(rawJson);

    document.querySelectorAll("tr.time-row").forEach(row => {
        const classSelect = row.querySelector(".class-select");
        const unitSelect = row.querySelector(".unit-select");

        if (!classSelect || !unitSelect) return;

        const initialClassKey = classSelect.value;
        const initialUnitKey = unitSelect.getAttribute("data-selected");

        // ✅ 최초 로딩 시
        fillUnitSelect(unitSelect, classUnits, initialClassKey, initialUnitKey);

        // ✅ 변경 이벤트 시
        classSelect.addEventListener("change", function () {
            const classKey = this.value;
            fillUnitSelect(unitSelect, classUnits, classKey, null);
        });
    });
    initClassUnitSelects(document.querySelectorAll("#comclassModal select.comclass-select[name='classKey']"), classUnits, "comclass-select", "comclass-select");
});


function initClassUnitSelects(classSelectNodes, classUnits, classClass, unitClass) {
    classSelectNodes.forEach(classSelect => {
        const parent = classSelect.closest("tr");
        const unitSelect = parent.querySelector(`.${unitClass}[name='unitKey']`);
        if (!unitSelect) return;

        const initialClassKey = classSelect.value;
        const initialUnitKey = unitSelect.getAttribute("data-selected");

        // ✅ 최초 로딩 시
        fillUnitSelect(unitSelect, classUnits, initialClassKey, initialUnitKey);

        // ✅ 변경 이벤트 시
        classSelect.addEventListener("change", function () {
            const classKey = this.value;
            fillUnitSelect(unitSelect, classUnits, classKey, null);
        });
    });
}

// 진도 데이터 갈아끼우기
function fillUnitSelect(unitSelect, classUnits, classKey, selectedUnitKey) {
    unitSelect.options.length = 0; // 초기화
    unitSelect.add(new Option("선택 안함", ""));

    if (!classKey || !classUnits[classKey]) return;

    // ✅ unitType === "peo"만 ㄱㄴㄷ순 정렬
    let units = [...classUnits[classKey]];
    if (units.length > 0 && units[0].unitType === "peo") {
        units.sort((a, b) => a.unitName.localeCompare(b.unitName, "ko"));
    }

    units.forEach(u => {
        const opt = new Option(u.unitName, u.unitKey);
        if (selectedUnitKey && selectedUnitKey === u.unitKey) {
            opt.selected = true; // DB 값 유지
        }
        unitSelect.add(opt);
    });
}


// 왼쪽 수업이름 파싱
document.addEventListener('DOMContentLoaded', () => {
    const span = document.getElementById('current-selected-class');

    document.querySelectorAll('.time-tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.time-tab-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            updateLabel();
        });
    });

    document.body.addEventListener('change', e => {
        if (!e.target.closest('.time-tab-content')) return;
        if (e.target.matches('input[type=radio]') || e.target.tagName === 'SELECT') {
            updateLabel();
        }
    });

    function updateLabel() {
        const activeTab = document.querySelector('.time-tab-btn.active');
        if (!activeTab) return;

        const dayLabel = activeTab.textContent;
        const container = document.getElementById(activeTab.getAttribute('data-tab'));
        if (!container) return;

        const checked = container.querySelector('input[type=radio]:checked');
        if (!checked) return;
        const row = checked.closest('tr.time-row');
        if (!row) return;

        const subjectSelect = row.querySelector('.basic-select select');
        const unitSelect = row.querySelector('.mini-select select');
        const subject = subjectSelect?.value
            ? subjectSelect.selectedOptions[0].textContent
            : '';
        const unit = unitSelect?.value
            ? unitSelect.selectedOptions[0].textContent
            : '';
        span.textContent = `${dayLabel}요일 ${subject} ${unit}`;
    }

    updateLabel();
});

// 전월 시간표 불러오기
document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('load-last-time-table');
    if (!btn) return;

    btn.addEventListener('click', async () => {
        if (!confirm('전월 시간표와 학생을 그대로 복사하시겠습니까?')) return;
        try {
            const res = await fetch('/class/api/copy/last-timetable', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({year, month})
            });
            const json = await res.json();
            if (!res.ok || json.success === false) {
                showAlert({icon: 'error', title: json.error?.message || '처리 중 오류가 발생했습니다.'});
                return;
            }
            showAlert({
                icon: 'success',
                title: '전월 시간표가 복사되었습니다.',
                text: json.response ?? undefined, // ✅ 마감일 메시지
                showConfirmButton: true,
                allowOutsideClick: false
            }).then(() => location.reload());
        } catch (e) {
            showAlert({icon: 'error', title: '서버와 통신 중 오류가 발생했습니다.'});
        }
    });
});

// 전월 데이터로 갈아끼우기
function renderTimeTable(tables) {
    tables.forEach(entry => {
        const row = document.querySelector(
            `.time-row[data-day="${entry.dayname}"][data-period-no="${entry.periodNo}"]`
        );
        if (!row) return;

        row.querySelector(".start-time").value = entry.startTime ?? "";
        row.querySelector(".end-time").value = entry.endTime ?? "";

        const classSelect = row.querySelector(".class-select");
        const unitSelect = row.querySelector(".unit-select");
        const gradeSelect = row.querySelector("select[name='gradeKey']");

        if (classSelect) classSelect.value = entry.classKey ?? "";
        if (gradeSelect) gradeSelect.value = entry.gradeKey ?? "";

        if (unitSelect && classSelect) {
            const classKey = entry.classKey;
            const classUnits = getClassUnits();

            fillUnitSelect(unitSelect, classUnits, classKey, null);

            // 🔒 무조건 두 번째 옵션 선택 (첫 번째는 "")
            if (unitSelect.options.length > 1) {
                unitSelect.selectedIndex = 1;
            }
        }

        const studentTd = row.querySelector(".student-cell");
        if (!studentTd) return;

        studentTd.querySelectorAll(".student-badge").forEach(el => el.remove());

        entry.students.forEach(stu => {
            const div = document.createElement("div");
            div.className = "student-badge comclass-badge-name";
            div.dataset.studentId = stu.studentId;
            div.dataset.timeTableKey = entry.timeTableKey;

            div.innerHTML = `
                <span class="badge-name">${stu.studentName}</span>
                ${
                stu.week && parseInt(stu.week, 10) <= 3
                    ? `<span class="count-num">(${stu.week})</span>`
                    : ""
            }
                <span class="badge-close"
                      id="assign_delete"
                      data-student-id="${stu.studentId}"
                      data-time-table-key="${entry.timeTableKey}"
                      title="삭제"></span>
            `;

            studentTd.appendChild(div);
        });

        row.dataset.timeTableKey = entry.timeTableKey ?? "";
        row.dataset.timeTableCode = entry.timeTableKey ?? "";
    });
}

async function saveStudents(list) {
    const assignments = list.map(s => ({
        timeTableKey: s.timeTableKey,
        studentId: s.studentId,
        weekNo: s.weekNo,
        yy: year,
        mm: month
    }));

    const res = await fetch('/class/add_student', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({assignments})
    });

    if (!res.ok) {
        throw new Error("학생 등록 실패");
    }
}

// 시간표 등록 로직
document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('table_register');
    if (!btn) return;
    const savedPeriod = sessionStorage.getItem('selectedPeriod');

    if (savedPeriod) {
        const activeTab = document.querySelector('.time-tab-content.active');

        if (activeTab) {
            const rows = activeTab.querySelectorAll('tr.time-row');

            rows.forEach(row => {
                const periodCell = row.querySelector('td:nth-child(2)');
                const periodText = periodCell?.innerText?.trim();

                if (periodText === savedPeriod) {
                    const radio = row.querySelector('input[type=radio]');
                    if (radio) radio.checked = true;
                }
            });
        }

        sessionStorage.removeItem('selectedPeriod');
    }


    btn.addEventListener('click', () => {
        const [yy, mm] = document.getElementById('currentMonth')
            .textContent.trim().match(/(\d{4})년\s*(\d{1,2})월/).slice(1, 3)
            .map((v, i) => i === 1 ? v.padStart(2, '0') : v);

        const allTabs = document.querySelectorAll('.time-tab-content');
        const payloadList = [];
        const dayIndexMap = {mon: 1, tue: 2, wed: 3, thu: 4, fri: 5, sat: 6};

        for (const tab of allTabs) {
            const dayname = tab.id;
            const daynameNo = dayIndexMap[dayname];
            const userCode = document.getElementById("tableUserCode").value;
            const rows = tab.querySelectorAll('tr.time-row');
            for (const row of rows) {
                const periodNo = row.dataset.periodNo;
                const startTime = row.querySelector('.time-start input').value;
                const endTime = row.querySelector('.time-end input').value;
                const classKey = row.querySelector('select[name="classKey"]').value;
                const unitKey = row.querySelector('select[name="unitKey"]').value;
                const gradeKey = row.querySelector('select[name="gradeKey"]').value;

                const isComClass = classKey === "COM";
                const hasRequired =
                    startTime && endTime && classKey && (isComClass || (unitKey && gradeKey));

                if (!hasRequired) {
                    continue;
                }

                payloadList.push({
                    yy, mm, dayname, daynameNo, periodNo,
                    startTime, endTime, classKey, unitKey, gradeKey, userCode
                });
            }
        }

        if (payloadList.length === 0) {
            showAlert({icon: 'warning', title: '입력된 수업 정보가 없습니다.'});
            return;
        }

        console.log(payloadList);

        fetch('/class/register', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payloadList)
        })
            .then(res => {
                if (!res.ok) throw new Error(res.status);
                return res.json();
            })
            .then(json => {
                if (json.success) { // ✅ "200" 체크 → success 체크로 변경
                    showAlert({
                        icon: 'success',
                        title: '시간표가 저장되었습니다.',
                        text: json.response ?? undefined // ✅ 마감일 메시지
                    }).then(() => window.location.reload());
                } else {
                    showAlert({icon: 'error', title: json.error?.message, text: '오류코드: ' + json.error?.status});
                }
            })
            .catch(err => {
                showAlert({icon: 'error', title: '오류', text: err.message});
            });
    });
});

// 학생 추가 로직
document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('add_student_timetable');
    const pendingRows = document.querySelectorAll('.timetable-pending-list');
    if (!btn && !pendingRows === 0) return;

    const savedDay = sessionStorage.getItem('selectedDay');
    const savedPeriod = sessionStorage.getItem('selectedPeriod');

    if (savedDay && savedPeriod) {
        const tabContent = document.getElementById(savedDay);
        if (tabContent) {
            document.querySelectorAll('.time-tab-btn').forEach(btn => btn.classList.remove('active'));
            document.querySelectorAll('.time-tab-content').forEach(ct => ct.classList.remove('active'));

            const tabBtn = document.querySelector(`.time-tab-btn[data-tab="${savedDay}"]`);
            if (tabBtn) tabBtn.classList.add('active');
            tabContent.classList.add('active');

            const targetRadio = tabContent.querySelector(`tr.time-row[data-period-no="${savedPeriod}"] input[type=radio]`);
            if (targetRadio) targetRadio.checked = true;
        }
    }


    const addStudents = () => {
        const activeContent = document.querySelector('.time-tab-content.active');
        if (!activeContent) {
            showAlert({icon: "warning", text: "요일을 선택해주세요."});
            return;
        }

        const selRadio = activeContent.querySelector('input[type=radio]:checked');
        if (!selRadio) {
            showAlert({icon: "warning", text: "수업을 선택해주세요."});
            return;
        }

        const selRow = selRadio.closest('tr.time-row');

        // 선택된 요일(탭 id) 저장
        const selectedDay = activeContent.getAttribute('id');
        sessionStorage.setItem('selectedDay', selectedDay);

        // 선택된 교시 저장
        const selectedPeriod = selRow.dataset.periodNo;
        sessionStorage.setItem('selectedPeriod', selectedPeriod);

        const timeTableKey = selRow.dataset.timeTableKey;
        if (!timeTableKey) {
            showAlert({icon: "warning", text: "수업 정보를 찾을 수 없습니다."});
            return;
        }

        const studentRows = Array.from(document.querySelectorAll('.stu-chocie-table tbody tr'))
            .filter(tr => tr.querySelector('input[type="checkbox"]').checked);

        if (studentRows.length === 0) {
            showAlert({icon: 'warning', text: '추가할 학생을 선택해주세요.'});
            return;
        }

        const [yy, mm] = document.getElementById('currentMonth')
            .textContent.trim().match(/(\d{4})년\s*(\d{1,2})월/).slice(1, 3)
            .map((v, i) => i === 1 ? v.padStart(2, '0') : v);

        const assignments = studentRows.map(tr => {
            const studentId = tr.querySelector('input[type="checkbox"]').value;
            const weekNo = tr.querySelector('input[name^="weeks-"]:checked').value;
            return {timeTableKey, studentId, weekNo, yy, mm};
        });

        fetch('/class/add_student', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({assignments})
        })
            .then(res => res.json().then(data => ({ok: res.ok, data})))
            .then(({ok, data}) => {
                if (!ok || !data.success) {
                    const msgParts = (data.error?.message || '등록 실패').split('\n');
                    return showAlert({
                        icon: 'error',
                        title: msgParts[0],
                        text: msgParts[1] ?? undefined,
                        showConfirmButton: true,
                        allowOutsideClick: false,
                        allowEscapeKey: false
                    });
                }
                return showAlert({
                    icon: 'success',
                    title: '학생이 추가되었습니다.',
                    text: data.response ?? undefined,
                    showConfirmButton: true,
                    allowOutsideClick: false,
                    allowEscapeKey: false
                });
            })
            .then(result => {
                if (result && result.isConfirmed) window.location.reload();
            })
            .catch(err => {
                showAlert({icon: 'error', title: '오류가 발생했습니다.', text: err.message});
            });
    };

    /* ---------------------------------------------------------
     * 이벤트 바인딩
     * --------------------------------------------------------- */
    if (btn) btn.addEventListener('click', addStudents);

    pendingRows.forEach(row => {
        row.addEventListener('dblclick', () => {
            document.querySelectorAll('.stu-chocie-table input[type="checkbox"]').forEach(cb => cb.checked = false);
            const checkbox = row.querySelector('input[type="checkbox"]');
            if (checkbox) checkbox.checked = true;

            addStudents();
        });
    });

});

document.addEventListener('DOMContentLoaded', () => {
    const searchBtn = document.getElementById('search-main-student');
    const searchInput = document.getElementById('search-name');
    const searchTypeSelect = document.getElementById('stu-name');
    const studentRows = document.querySelectorAll('.timetable-pending-list');

    if (!searchBtn || !searchInput || !searchTypeSelect) return;

    function performSearch() {
        const searchType = searchTypeSelect.value;
        const searchTerm = searchInput.value.trim().toLowerCase();

        // 검색어가 없으면 전체 표시
        if (searchTerm === '') {
            studentRows.forEach(row => row.style.display = '');
            return;
        }

        studentRows.forEach(row => {
            let shouldShow = false;

            // 이름으로 검색
            if (searchType === 'name') {
                const nameSpan = row.querySelector('.names');
                const studentName = nameSpan?.textContent.trim().toLowerCase() || '';

                if (studentName.includes(searchTerm)) {
                    shouldShow = true;
                }
            }

            // 전화번호로 검색 (data-phone 속성에서 가져옴)
            if (searchType === 'phone') {
                const phoneNumber = row.dataset.phone || '';
                const cleanPhone = phoneNumber.replace(/[^0-9]/g, ''); // 숫자만 추출
                const cleanSearchTerm = searchTerm.replace(/[^0-9]/g, '');

                if (cleanPhone.includes(cleanSearchTerm)) {
                    shouldShow = true;
                }
            }

            row.style.display = shouldShow ? '' : 'none';
        });

        // 검색 결과 개수
        const visibleCount = Array.from(studentRows).filter(row => row.style.display !== 'none').length;
        console.log(`검색 결과: ${visibleCount}명`);
    }

    // 검색 버튼 클릭
    searchBtn.addEventListener('click', performSearch);

    // 엔터키로 검색
    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            performSearch();
        }
    });

    // 검색 타입 변경 시 입력창 초기화
    searchTypeSelect.addEventListener('change', () => {
        searchInput.value = '';
        studentRows.forEach(row => row.style.display = '');
    });

    // 검색어 초기화 시 전체 목록 보기
    searchInput.addEventListener('input', (e) => {
        if (e.target.value.trim() === '') {
            studentRows.forEach(row => row.style.display = '');
        }
    });
});

// 학생 삭제 로직
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('#assign_delete').forEach(btn => {
        btn.addEventListener('click', () => {

            const studentId = btn.dataset.studentId;
            const timeTableKey = btn.dataset.timeTableKey;

            showAlert({
                icon: 'warning',
                title: '정말로 제외하시겠습니까?',
                showCancelButton: true,
                confirmButtonText: '삭제',
                cancelButtonText: '취소',
                allowOutsideClick: false,
                allowEscapeKey: false
            }).then(result => {
                if (!result.isConfirmed) return;


                const requestBody = {
                    timeTableKey: timeTableKey,
                    studentId: studentId
                };
                fetch('/class/delete/student', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json;charset=UTF-8'},
                    body: JSON.stringify(requestBody)
                })
                    .then(res => {
                        if (!res.ok) throw new Error('삭제 요청 실패: ' + res.status);
                        return res.json();
                    })
                    .then(apiResult => {
                        return showAlert({
                            icon: apiResult.success ? 'success' : 'error', // ✅ success 기준
                            title: apiResult.success ? '삭제되었습니다.' : '삭제 실패',
                            text: apiResult.response ?? apiResult.error?.message, // ✅ 마감일 메시지
                            showConfirmButton: true,
                            allowOutsideClick: false
                        });
                    })
                    .then(res => {
                        if (res.isConfirmed) window.location.reload();
                    })
                    .catch(err => {
                        showAlert({icon: 'error', title: '오류 발생', text: err.message});
                    });
            });
        });
    });
});

// 시간표 단일 row 삭제
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".badge-init").forEach(btn => {
        btn.addEventListener("click", function () {
            const row = btn.closest(".time-row");
            if (!row) return;
            const timeTableKey = row.dataset.timeTableKey;
            showAlert({
                icon: 'warning',
                title: '정말 이 교시의 데이터를\n초기화하시겠습니까?',
                showCancelButton: true,
                confirmButtonText: '삭제',
                cancelButtonText: '취소',
                allowOutsideClick: false,
                allowEscapeKey: false
            }).then(result => {

                if (!result.isConfirmed) return;

                const requestBody = {
                    timeTableKey: timeTableKey
                };
                fetch('/class/api/delete/timetable/row', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(requestBody)
                })
                    .then(res => res.json())
                    .then(data => {
                        if (data.success) {
                            showAlert({
                                icon: 'success',
                                title: '삭제되었습니다.',
                                text: data.response ?? undefined, // ✅ 마감일 메시지
                                showConfirmButton: true,
                                allowOutsideClick: false
                            }).then(() => window.location.reload());
                        }
                    });

                // 수업 삭제 후 랜더링
                function deleteTimetableRow() {
                    const startInput = row.querySelector(".start-time");
                    const endInput = row.querySelector(".end-time");
                    if (startInput) startInput.value = "";
                    if (endInput) endInput.value = "";

                    const classSelect = row.querySelector(".class-select");
                    if (classSelect) classSelect.value = "";

                    const unitSelect = row.querySelector(".unit-select");
                    if (unitSelect) {
                        unitSelect.value = "";
                        unitSelect.innerHTML = '<option value="">선택 안함</option>';
                    }

                    const gradeSelect = row.querySelector("select[name='gradeKey']");
                    if (gradeSelect) gradeSelect.value = "";

                    const studentCells = row.querySelectorAll(".student-badge");
                    studentCells.forEach(stu => stu.remove());

                    row.dataset.timeTableKey = "";
                    row.dataset.timeTableCode = "";
                }

            });
        });
    });
});

function getHiddenJson(id) {
    const el = document.getElementById(id);
    if (!el) return null;
    try {
        return JSON.parse(el.value);
    } catch (e) {
        console.error(`❌ ${id} 파싱 실패`, e);
        return null;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const modal = document.getElementById("comclassModal");
    const tbody = modal.querySelector(".comclass-table tbody");
    const closeBtn = modal.querySelector(".comclass-close");

    document.querySelectorAll(".add-class").forEach(btn => {
        btn.addEventListener("click", async (e) => {
            const row = e.target.closest("tr.time-row");
            const timeTableKey = row?.dataset.timeTableKey;
            if (!timeTableKey) {
                alert("시간표를 조회할 수 없습니다.\n새로고침 후 시도해주세요.");
                return;
            }

            try {
                const res = await fetch("/class/comclass/students", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({timeTableKey}),
                });

                if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
                const data = await res.json();

                const currentMonthText = document.getElementById("currentMonth")?.textContent?.trim() || "";
                let yy = "";
                let mm = "";
                const match = currentMonthText.match(/(\d{4})년\s*(\d{1,2})월/);
                if (match) {
                    yy = match[1];
                    mm = match[2].padStart(2, "0");
                }

                // ✅ 모달에 값 저장
                modal.dataset.timeTableKey = timeTableKey;
                modal.dataset.yy = yy;
                modal.dataset.mm = mm;


                renderComclassTable(data.response, tbody);
                modal.style.display = "flex";

                const classUnits = getHiddenJson("classUnits");
                initClassUnitSelects(
                    modal.querySelectorAll("select.comclass-select[name='classKey']"),
                    classUnits,
                    "comclass-select",
                    "comclass-select"
                );

            } catch (err) {
                console.error("❌ 종합반 학생 조회 실패:", err);
                alert("학생 데이터를 불러오는 중 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.");
            }
        });
    });

    closeBtn.addEventListener("click", () => modal.style.display = "none");
    modal.addEventListener("click", (e) => {
        if (e.target === modal) modal.style.display = "none";
    });
});

function renderComclassTable(students, tbody) {
    tbody.innerHTML = "";

    const classCodes = getHiddenJson("classCodes");

    students.forEach((stu, idx) => {
        const tr = document.createElement("tr");
        tr.dataset.studentId = stu.studentId;
        tr.innerHTML = `
            <td>${idx + 1}</td>
            <td>${stu.studentName}</td>
            <td>${stu.gradeName}</td>
            <td>
                <div class="basic-select square size-100">
                    <select name="classKey" class="comclass-select">
                        <option value="">선택</option>
                        ${classCodes.map(c =>
            `<option value="${c.classKey}" ${stu.classKey === c.classKey ? "selected" : ""}>${c.className}</option>`
        ).join("")}
                    </select>
                </div>
            </td>
            <td>
                <div class="basic-select square size-50">
                    <select name="unitKey" class="comclass-select" data-selected="${stu.unitKey || ""}">
                        <option value="">선택</option>
                    </select>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

document.addEventListener("DOMContentLoaded", () => {
    const btn = document.getElementById("comclass-save-btn");
    const modal = document.getElementById("comclassModal");

    btn.addEventListener("click", async function () {
        alert("종합반 추가 기능 수정 중입니다.")
        const rows = modal.querySelectorAll(".comclass-table tbody tr");
        const updateList = [];

        rows.forEach(tr => {
            const studentId = tr.dataset.studentId;
            const classSelect = tr.querySelector("select[name='classKey']");
            const unitKey = tr.querySelector("select[name='unitKey']").value;
            const classKey = classSelect.value;
            const selectedOption = classSelect.selectedOptions[0];
            const classType = selectedOption?.dataset.classType || null;

            updateList.push({
                studentId: studentId || null,
                classKey: classKey || null,
                classType: classType || null,
                unitKey: unitKey || null,
                yy: modal.dataset.yy,
                mm: modal.dataset.mm
            });
        });

        const timeTableKey = modal.dataset.timeTableKey;
        if (!timeTableKey) {
            alert("시간표 키를 찾을 수 없습니다. 다시 시도해주세요.");
            return;
        }

        const payload = {
            timeTableKey: timeTableKey,
            studentInfos: updateList
        };


        try {
            const res = await fetch("/class/comclass/updateAssign", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(payload)
            });

            if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
            const result = await res.json();
            alert("✅ " + result.response + " 저장되었습니다!");
            window.location.reload();

        } catch (err) {
            alert("저장 중 오류가 발생했습니다.");
        }
    });
});

document.addEventListener("DOMContentLoaded", () => {
    const jsonString = document.getElementById("comclassInfos").value;
    const comclassInfos = JSON.parse(jsonString);

    document.querySelectorAll(".badge-name").forEach(badge => {
        badge.addEventListener("mouseenter", e => {
            const parent = badge.closest(".student-badge");
            const studentId = parent.dataset.studentId;
            const timeTableKey = parent.dataset.timeTableKey;

            // ✅ 두 조건 모두 일치해야 함
            const match = comclassInfos.find(info =>
                info.studentId === studentId && info.timeTableKey === timeTableKey
            );

            if (!match) return;
            if (!match.className || !match.unitName) return;

            // ✅ tooltip 생성
            const tooltip = document.createElement("div");
            tooltip.className = "comclass-tooltip";
            tooltip.textContent = `${match.className} ${match.unitName}`;

            // badge 바로 아래에 표시
            const rect = badge.getBoundingClientRect();
            tooltip.style.position = "absolute";
            tooltip.style.top = `${rect.bottom + window.scrollY + 4}px`;
            tooltip.style.left = `${rect.left + window.scrollX}px`;

            document.body.appendChild(tooltip);

            // ✅ mouseleave 시 제거
            badge.addEventListener("mouseleave", () => {
                tooltip.remove();
            }, {once: true});
        });
    });
});

// 시간표 내보내기 (어드민 전용)
document.addEventListener('DOMContentLoaded', () => {
    const openBtn = document.getElementById('open-copy-modal-btn');
    if (!openBtn) return;

    const modal = document.getElementById('copy-timetable-modal');
    const cancelBtn = document.getElementById('copy-modal-cancel');
    const confirmBtn = document.getElementById('copy-modal-confirm');
    const fromYmInput = document.getElementById('copy-from-ym');
    const toYmInput = document.getElementById('copy-to-ym');
    const teacherSelect = document.getElementById('copy-from-teacher');
    // 모달 열기 — 현재 선택된 연월을 기본값으로
    openBtn.addEventListener('click', async () => {
        if (year && month) {
            toYmInput.value = `${year}-${month.padStart(2, '0')}`;
        }

        // 선생님 목록 로드
        try {
            const res = await fetch('/user/active-users');
            const json = await res.json();
            const users = json.response ?? [];
            teacherSelect.innerHTML = '<option value="">선생님 선택</option>';
            users
                .filter(u => u.useYn !== false)
                .forEach(u => {
                    const opt = document.createElement('option');
                    opt.value = u.userCode;
                    opt.textContent = u.userName;
                    teacherSelect.appendChild(opt);
                });
        } catch (e) {
            showAlert({icon: 'error', title: '선생님 목록을 불러올 수 없습니다.'});
            return;
        }

        modal.style.display = 'flex';
    });

    // 모달 닫기
    cancelBtn.addEventListener('click', () => { modal.style.display = 'none'; });
    modal.addEventListener('click', e => { if (e.target === modal) modal.style.display = 'none'; });


    // 가져오기 실행
    confirmBtn.addEventListener('click', async () => {
        if (confirmBtn.disabled) return;
        confirmBtn.disabled = true;
        const fromYm = fromYmInput.value;
        const toYm = toYmInput.value;
        const fromUserCode = teacherSelect.value;

        if (!fromUserCode) { showAlert({icon: 'warning', text: '선생님을 선택해주세요.'}); return; }
        if (!fromYm) { showAlert({icon: 'warning', text: '복사할 연월을 선택해주세요.'}); return; }
        if (!toYm) { showAlert({icon: 'warning', text: '붙여넣을 연월을 선택해주세요.'}); return; }

        const [fromYy, fromMm] = fromYm.split('-');
        const [toYy, toMm] = toYm.split('-');

        const body = {fromUserCode, fromYy, fromMm, toUserCode: null, toYy, toMm};

        try {
            const res = await fetch('/class/api/copy/to-teacher', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(body)
            });
            const json = await res.json();

            if (json.success === false) {
                showAlert({icon: 'error', title: json.error?.message || '처리 중 오류가 발생했습니다.'});
                return;
            }

            modal.style.display = 'none';
            showAlert({icon: 'success', title: '시간표를 가져왔습니다.', showConfirmButton: true, allowOutsideClick: false})
                .then(() => location.reload());
        } catch (e) {
            showAlert({icon: 'error', title: '서버와 통신 중 오류가 발생했습니다.'});
        } finally {
            confirmBtn.disabled = false;
        }
    });
});