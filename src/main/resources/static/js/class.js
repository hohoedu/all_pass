// 정렬
document.addEventListener('DOMContentLoaded', function () {
    addHeadSort('remedialLeftHead', 'student-tbody-left');
    addHeadSort('remedialRightHead', 'student-tbody-right');
});

// URL에서 날짜 가져오기
document.addEventListener('DOMContentLoaded', function () {
    const openMonthPicker = document.getElementById('openMonthPicker');
    const monthPickerInput = document.getElementById('monthPickerInput');
    const currentMonthElement = document.getElementById('currentMonth');
    if (!openMonthPicker || !monthPickerInput || !currentMonthElement) return;

    const url = new URL(window.location.href);
    const urlParams = url.searchParams;

    let year = urlParams.get('year');
    let month = urlParams.get('month');
    if (year && month) {
        currentMonthElement.textContent = `${year}년 ${parseInt(month, 10)}월`;
    }
    openMonthPicker.addEventListener('click', () => {
        monthPickerInput.showPicker?.() || monthPickerInput.click();
    });

    monthPickerInput.addEventListener('change', () => {
        console.log(monthPickerInput.value);
        const [selectedYear, selectedMonth] = monthPickerInput.value.split('-');

        let newUrl = '';

        if (url.pathname.includes('timetable')) {
            newUrl = `/class/timetable?year=${selectedYear}&month=${selectedMonth}`;
        } else if (url.pathname.includes('timeview')) {
            newUrl = `/class/timeview?year=${selectedYear}&month=${selectedMonth}&user=2`;
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

    btn.addEventListener('click', () => {
        fetch(`/class/api/load_time_table`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
        })
            .then(res => res.json()
            )
            .then(data => {
                renderTimeTable(data.response);
            })
            .catch(err => {
                console.log('오류 발생');
            });

    })
});

// 전월 데이터로 갈아끼우기
function renderTimeTable(tables) {
    tables.forEach(entry => {
        const row = document.querySelector(
            `.time-row[data-day="${entry.dayname}"][data-period-no="${entry.periodNo}"]`
        );
        if (!row) return;

        const startInput = row.querySelector(".start-time");
        const endInput = row.querySelector(".end-time");
        if (startInput) startInput.value = entry.startTime ?? "";
        if (endInput) endInput.value = entry.endTime ?? "";

        const classSelect = row.querySelector(".class-select");
        const unitSelect = row.querySelector(".unit-select");

        if (classSelect) {
            classSelect.value = entry.classKey ?? "";
        }

        if (classSelect && unitSelect) {
            const classKey = entry.classKey ?? "";
            const classUnits = getClassUnits();

            fillUnitSelect(unitSelect, classUnits, classKey, null);
        }
    });
}

// 시간표 등록 로직
document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('table_register');
    if (!btn) return;
    const savedPeriod = sessionStorage.getItem('selectedPeriod');
    if (savedPeriod) {
        const activeTab = document.querySelector('.time-tab-content.active');
        const rows = activeTab.querySelectorAll('tr.time-row');
        rows.forEach(row => {
            const p = row.querySelector('td:nth-child(2)').innerText.trim();
            if (p === savedPeriod) {
                row.querySelector('input[type=radio]').checked = true;
            }
        });
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
                console.log(periodNo);
                console.log('periodNo' + periodNo);
                const startTime = row.querySelector('.time-start input').value;
                const endTime = row.querySelector('.time-end input').value;
                const classKey = row.querySelector('select[name="classKey"]').value;
                const unitKey = row.querySelector('select[name="unitKey"]').value;
                const gradeKey = row.querySelector('select[name="gradeKey"]').value;

                const isComClass = classKey === "COM";
                const hasRequired =
                    startTime && endTime && classKey && gradeKey && (isComClass || unitKey);

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

        fetch('/class/register', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payloadList)
        })
            .then(res => {
                if (!res.ok) throw new Error(res.json.message + res.status);
                return res.json();
            })
            .then(json => {
                if (json.response === "200") {
                    // showAlert({icon: 'success', title: '시간표가 저장되었습니다.'})
                    //     .then(() => window.location.reload());
                } else {
                    showAlert({icon: 'error', title: json.error?.message, text: '오류코드: ' + json.error?.status});
                }
            })
            .catch(err => {
                console.error(err);
                showAlert({icon: 'error', title: '오류', text: err.message});
            });
    });
});

// 학생 추가 로직
document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('add_student_timetable');
    const pendingRows = document.querySelectorAll('.timetable-pending-list');
    if (!btn && !pendingRows === 0) return;

    const savedPeriod = sessionStorage.getItem('selectedPeriod');
    if (savedPeriod) {
        const activeTab = document.querySelector('.time-tab-content.active');
        activeTab.querySelectorAll('tr.time-row').forEach(row => {
            const p = row.querySelector('td:nth-child(2)').innerText.trim();
            if (p === savedPeriod) {
                row.querySelector('input[type=radio]').checked = true;
            }
        });
        sessionStorage.removeItem('selectedPeriod');
    }

    const addStudents = () => {
        const activeContent = document.querySelector('.time-tab-content.active');
        const selRow = activeContent.querySelector('input[type=radio]:checked').closest('tr.time-row');

        if (!selRow) {
            showAlert({icon: "warning", text: "수업을 선택해주세요."})
            return;
        }


        const timeTableKey = selRow.dataset.timeTableKey;
        console.log('timeTableKey = ' + timeTableKey);
        if (!timeTableKey) {
            showAlert({icon: "warning", text: "수업 정보를 찾을 수 없습니다."});
            return;
        }

        const studentRows = Array.from(
            document.querySelectorAll('.stu-chocie-table tbody tr')
        ).filter(tr => tr.querySelector('input[type="checkbox"]').checked);

        if (studentRows.length === 0) {
            showAlert({icon: 'warning', text: '추가할 학생을 선택해주세요.'});
            return;
        }

        const assignments = studentRows.map(tr => {
            const studentId = tr.querySelector('input[type="checkbox"]').value;
            const weekNo = tr.querySelector('input[name^="weeks-"]:checked').value;
            return {timeTableKey, studentId, weekNo};
        });

        console.log(assignments);

        fetch('/class/add_student', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({assignments})
        })
            .then(res => {
                if (!res.ok) throw new Error('추가 요청 실패: ' + res.status);
                return res.json();
            })
            .then(apiResult => {
                // const periodNo = selRow.querySelector('td:nth-child(2)').innerText.trim();
                // sessionStorage.setItem('selectedPeriod', periodNo);
                return showAlert({
                    icon: apiResult.response ? 'success' : 'error',
                    title: apiResult.response ? '학생이 추가되었습니다.' : '등록 실패',
                    text: apiResult.error?.message,
                    showConfirmButton: true,
                    allowOutsideClick: false,
                    allowEscapeKey: false
                });
            }).then(result => {
            if (result.isConfirmed) {
                window.location.reload();
            }
        })
            .catch(err => {
                console.error(err);
                showAlert({
                    icon: 'error',
                    title: '오류가 발생했습니다.',
                    text: err.message
                });
                return;
            });

    }

    if (btn) {
        btn.addEventListener('click', addStudents);
    }

    pendingRows.forEach(row => {
        row.addEventListener('dblclick', () => {
            document.querySelectorAll('.stu-chocie-table input[type="checkbox"]').forEach(cb => cb.checked = false);
            const checkbox = row.querySelector('input[type="checkbox"]');
            if (checkbox) checkbox.checked = true;

            addStudents();
        });
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
                    headers: {
                        'Content-Type': 'application/json;charset=UTF-8'
                    },
                    body: JSON.stringify(requestBody)
                })
                    .then(res => {
                        if (!res.ok) throw new Error('삭제 요청 실패: ' + res.status);
                        return res.json();
                    })
                    .then(apiResult => {
                        return showAlert({
                            icon: apiResult.response ? 'success' : 'error',
                            title: apiResult.response ? '삭제되었습니다.' : '삭제 실패',
                            text: apiResult.error?.message,
                            showConfirmButton: true,
                            allowOutsideClick: false
                        });
                    })
                    .then(res => {
                        if (res.isConfirmed) {
                            window.location.reload();
                        }
                    })
                    .catch(err => {
                        console.error(err);
                        showAlert({
                            icon: 'error',
                            title: '오류 발생',
                            text: err.message
                        });
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
                            window.location.reload();
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

                modal.dataset.timeTableKey = timeTableKey;
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
    const btn = document.getElementById("comclassSaveBtn");
    const modal = document.getElementById("comclassModal");

    btn.addEventListener("click", async function () {
        const rows = modal.querySelectorAll(".comclass-table tbody tr");
        const updateList = [];

        rows.forEach(tr => {
            const studentId = tr.dataset.studentId;
            const classKey = tr.querySelector("select[name='classKey']").value;
            const unitKey = tr.querySelector("select[name='unitKey']").value;

            updateList.push({
                studentId: studentId || null,
                classKey: classKey || null,
                unitKey: unitKey || null
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
            console.log("응답:", result);

            modal.style.display = "none";

        } catch (err) {
            alert("저장 중 오류가 발생했습니다.");
        }
    });
});

document.addEventListener("DOMContentLoaded", () => {
    const jsonString = document.getElementById("comclassInfos").value;
    const comclassInfos = JSON.parse(jsonString);

    console.log(comclassInfos);
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

// ===========================수업일지===========================//

const DAY_EN = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];
const DAY_KR = ['일', '월', '화', '수', '목', '금', '토'];
const toYmd = (d) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
const toK = (d) =>
    `${d.getFullYear()}년 ${d.getMonth() + 1}월 ${d.getDate()}일 (${DAY_KR[d.getDay()]})`;

const state = {teacherId: null, date: null, timeTableKey: null, week: null};

function buildByDateBody(dateStr, teacherId) {
    const d = new Date(dateStr + 'T00:00:00');
    return {
        yy: String(d.getFullYear()),
        mm: String(d.getMonth() + 1).toString().padStart(2, '0'),
        day: DAY_EN[d.getDay()],
        date: toYmd(d),
        userCode: (teacherId && teacherId !== 'all') ? teacherId : 'all',
    };
}

function buildByClassBody(timeTableKey, week, classKey, unitKey) {
    return {
        timeTableKey: timeTableKey ?? null,
        week: week ?? null,
        classKey: classKey ?? null,
        unitKey: unitKey ?? null,
    };
}

function getActiveTimeTableKey() {
    const el = document.querySelector('#record-class-list .class-btn.active');
    return el?.dataset.timeTableKey || el?.dataset.classId || null;
}

function abortInFlight() {
    let inFlightController = null;

    if (inFlightController) inFlightController.abort();
    inFlightController = new AbortController();
    return inFlightController.signal;
}

// 이벤트 감지
document.addEventListener('DOMContentLoaded', () => {
    const teacherSel = document.getElementById('teacher-select');
    const dateInput = document.getElementById('record_calendar');
    const dateLabel = document.getElementById('record_current');
    const calBtn = document.querySelector('.month-title .calendar-open');
    const weekWrap = document.querySelector('.week-selector');
    const classList = document.querySelector('#record-class-list');

    let initDate = null;
    const m = dateLabel?.textContent.match(/(\d{4})년\s*(\d{1,2})월\s*(\d{1,2})일/);
    if (m) initDate = new Date(+m[1], +m[2] - 1, +m[3]);
    if (!initDate || isNaN(initDate)) initDate = new Date();
    state.date = toYmd(initDate);
    if (dateInput && !dateInput.value) dateInput.value = state.date;
    if (dateLabel && !dateLabel.textContent.trim()) dateLabel.textContent = toK(initDate);

    if (teacherSel && teacherSel.value) state.teacherId = teacherSel.value;

    const initActiveClass = classList?.querySelector('.class-btn.active');
    if (initActiveClass) {
        state.timeTableKey = initActiveClass.dataset.timeTableKey || initActiveClass.dataset.classId || null;
        state.classKey = initActiveClass.dataset.classKey || null;
        state.unitKey = initActiveClass.dataset.unitKey || null;
    }

    const initActiveWeek = weekWrap?.querySelector('.week-btn.active');
    if (initActiveWeek) state.week = initActiveWeek.dataset.week || null;

    if (teacherSel) {
        teacherSel.addEventListener('change', () => {
            state.teacherId = teacherSel.value || null;

            loadOverviewFromState();

        });
    }

    if (calBtn && dateInput) {
        calBtn.addEventListener('click', () => {
            if (typeof dateInput.showPicker === 'function') {
                try {
                    dateInput.showPicker();
                } catch {
                    dateInput.focus();
                    dateInput.click();
                }
            } else {
                dateInput.focus();
                dateInput.click();
            }
        });
    }

    if (dateInput) {
        dateInput.addEventListener('change', () => {
            if (!dateInput.value) return;

            state.date = dateInput.value;

            const d = new Date(state.date + 'T00:00:00');
            if (!Number.isNaN(d.getTime())) dateLabel.textContent = toK(d);

            loadOverviewFromState();

        });
    }

    window.loadClassData = function (el) {
        state.timeTableKey = el?.dataset?.timeTableKey || el?.dataset?.classId || null;
        state.classKey = el?.dataset?.classKey || null;
        state.unitKey = el?.dataset?.unitKey || null;
        loadStudentList();
    };

    if (classList) {
        classList.addEventListener('click', (e) => {
            const li = e.target.closest('.class-btn');
            if (!li) return;
            window.loadClassData(li); // 동일 로직
        });
    }


    if (weekWrap) {
        weekWrap.querySelectorAll('.week-btn').forEach((btn) => {
            btn.addEventListener('click', () => {
                state.week = btn.dataset.week || null;

                const activeKey = getActiveTimeTableKey(); // ← DOM에서 읽기
                if (!activeKey) {
                    renderRecordStudentList([]);
                    return;
                }
                loadStudentList(activeKey);
            });
        });
    }
});

// 수업 리스트 조회 후 학생 리스트 조회
async function loadOverviewFromState() {
    const signal = abortInFlight();

    try {
        const body1 = buildByDateBody(state.date, state.teacherId);
        const res1 = await fetch('/class/api/record/label', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body1),
            signal
        });
        if (!res1.ok) throw new Error('서버 응답 오류(라벨)');
        const data1 = await res1.json();

        const list1 = Array.isArray(data1?.response) ? data1.response : [];
        renderRecordClassList(list1);

        if (list1.length === 0) {
            state.timeTableKey = null;
            renderRecordStudentList([]);
            return;
        }

        const firstTimeTableKey = list1[0].timeTableKey;
        const firstClassKey = list1[0].classKey;
        const firstUnitKey = list1[0].unitKey;

        state.timeTableKey = String(firstTimeTableKey);
        state.classKey = String(firstClassKey)
        state.unitKey = String(firstUnitKey)

        const body2 = buildByClassBody(firstTimeTableKey, state.week, firstClassKey, firstUnitKey);
        const res2 = await fetch('/class/api/record/student', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body2),
            signal
        });
        if (!res2.ok) throw new Error('서버 응답 오류(학생)');
        const data2 = await res2.json();
        const list2 = Array.isArray(data2?.response?.students) ? data2.response.students : [];
        const content = data2.response.afterClass
        console.log(content);
        renderRecordStudentList(list2, content);
    } catch (e) {
        if (e.name === 'AbortError') return;
        console.error('[loadOverviewFromState] 실패:', e);
        renderRecordStudentList([]);
    }
}

// 학생 리스트 조회
async function loadStudentList(timeTableKey = getActiveTimeTableKey()) {
    const signal = abortInFlight();

    try {
        if (!timeTableKey) {
            renderRecordStudentList([]);
            return;
        }
        const body = buildByClassBody(timeTableKey, state.week, state.classKey, state.unitKey);
        const res = await fetch('/class/api/record/student', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body),
            signal
        });
        if (!res.ok) throw new Error('서버 응답 오류(학생)');
        const data = await res.json();
        console.log(data.response);
        const list = Array.isArray(data?.response?.students) ? data.response.students : [];
        const content = data.response.afterClass;

        renderRecordStudentList(list, content);
    } catch (e) {
        if (e.name === 'AbortError') return;
        console.error('[loadStudentList] 실패:', e);
        renderRecordStudentList([]);
    }
}

// 수업리스트 랜더링
function renderRecordClassList(list) {
    const ul = document.querySelector('#record-class-list');
    if (!ul) return;

    ul.innerHTML = '';

    list.forEach((item, idx) => {
        const li = document.createElement('li');
        li.className = 'class-btn' + (idx === 0 ? ' active' : '');
        li.dataset.timeTableKey = item.timeTableKey;
        li.dataset.unitKey = item.unitKey;
        li.dataset.classKey = item.classKey;
        li.onclick = function () {
            ul.querySelectorAll('.class-btn').forEach(el => el.classList.remove('active'));
            this.classList.add('active');
            loadClassData(this);
        };

        const span = document.createElement('span');
        span.textContent = item.classLabel;
        li.appendChild(span);
        ul.appendChild(li);
    });

    if (list.length > 0) {
        state.timeTableKey = String(list[0].timeTableKey);
    }
}

// 학생리스트 랜더링
function renderRecordStudentList(list, content, tbodySel = '#record_tbody') {
    const tbody = typeof tbodySel === 'string' ? document.querySelector(tbodySel) : tbodySel;
    if (!tbody) {
        console.error('[renderStudents] tbody를 찾을 수 없습니다:', tbodySel);
        return;
    }

    const items = Array.isArray(list) ? list : [];
    const frag = document.createDocumentFragment();

    items.forEach((s, idx) => {
        const tr = document.createElement('tr');
        tr.dataset.studentId = s.studentId ?? '';
        tr.dataset.appToken = s.appToken ?? '';
        tr.dataset.centerCode = s.centerCode ?? '';
        tr.dataset.afterClassKey = s.afterClassKey ?? '';
        tr.innerHTML += `<td class="checkbox-group"><input type="checkbox" /></td>`;
        tr.innerHTML += `<td>${idx + 1}</td>`;
        tr.innerHTML += `<td class="studentName">${s.studentName ?? ''}</td>`;

        const attendance = s.attendanceName ?? '결석';
        let statusClass;
        switch (attendance) {
            case '출석 완료':
                statusClass = 'attend';
                break;
            case '지각':
                statusClass = 'late';
                break;
            case '수업 전':
                statusClass = 'before';
                break;
            default:
                statusClass = 'absent';
        }
        // 출석
        tr.innerHTML += `
            <td>
                <span class="status-badge ${statusClass} stbox">${attendance}</span>
                <div class="time in stbox">
                    등원
                    <span class="time-set">
                        <span class="display-time">${s.inTime ?? '--:--'}</span>
                        <input type="time" class="timepicker" value="${s.inTime ?? ''}">
                    </span>
                </div>
                <div class="time out stbox">
                    하원
                    <span class="time-set">
                        <span class="display-time">${s.outTime ?? '--:--'}</span>
                        <input type="time" class="timepicker" value="${s.outTime ?? ''}">
                    </span>
                </div>
            </td>`;

        // 보강일자
        tr.innerHTML += `
          <td>
            ${!s.remedialDate || s.remedialDate === '9999-12-31'
            ? '--'
            : `<div class="icon-field time-input cal-adjust" style="margin-bottom: 0; text-align:center;">
                   <span class="selected-datetime">${s.remedialDate}</span>
                   <input type="date" class="datetime-input hidden-picker" value="${s.remedialDate}">
                   <button type="button" class="icon-btn calendar-btn" style="background:transparent;">
                     <img src="/image/calendar.png" alt="달력 아이콘">
                   </button>
                 </div>`
        }
          </td>`;

        // 특이사항
        tr.innerHTML += `
          <td>
            <ul class="tag-list">
              <li>#숙제</li>
              <li>#교재준비</li>
              <li class="remarks"><img src="/image/add.png" alt=""></li>
            </ul>
          </td>`;

        // 상담기록
        tr.innerHTML += `
          <td class="cell-middle">
            <div class="counsel-box">
              <div class="counsel-type">
                <button class="active">전화</button>
                <button>문자</button>
                <button>대면</button>
              </div>
              <textarea placeholder="내용을 입력해주세요."></textarea>
            </div>
          </td>`;

        // 수업 후 코멘트
        tr.innerHTML += `
          <td>
            <div class="cell-middle">
              <div class="after-comment">
                <textarea class="comment-text record-content" placeholder="내용을 입력해주세요.">${content ? content.content.replace(/<br\s*\/?>/gi, "\n") : ""}</textarea>
              </div>
            </div>
          </td>`;

        // 발송여부
        const beforeSendSrc = s.isBeforeSend == '1' ? '/image/send2.png' : '/image/send1.png';
        const afterSendSrc = s.isAfterSend == '1' ? '/image/send3.png' : '/image/send1.png';

        tr.innerHTML += `
        <td class="send-ornot">
          <img src="${beforeSendSrc}" alt="">
          <img src="${afterSendSrc}" alt="">
        </td>`;

        frag.appendChild(tr);
    });

    // 학생이 없을 때
    if (items.length === 0) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 10; // 9 → 10
        td.style.textAlign = 'center';
        td.style.padding = '20px';
        td.textContent = '등록된 학생이 없습니다.';
        tr.appendChild(td);
        tbody.replaceChildren(tr);
    } else {
        tbody.replaceChildren(frag);
    }
}


// 수업 안내 발송 모달 오픈
document.addEventListener('click', function (e) {
    if (e.target.closest('.class-guide')) {
        const activeClass = document.querySelector('#record-class-list .class-btn.active');
        const activeWeek = document.querySelector('.week-selector .week-btn.active');

        if (!activeClass) {
            console.warn('[GUIDE MODAL] 활성화된 클래스가 없습니다.');
            return;
        }

        const selectedNames = Array.from(document.querySelectorAll('#record_tbody tr'))
            .filter(tr => tr.querySelector('input[type="checkbox"]:checked'))
            .map(tr => tr.querySelector('.studentName')?.textContent.trim() || '');

        const unitKey = activeClass.dataset.unitKey;    // data-class-unitKey
        const classKey = activeClass.dataset.classKey;  // data-class-classKey
        const timeTableKey = activeClass.dataset.timeTableKey;  // data-class-code
        const week = activeWeek?.dataset.week;    // data-week
        console.log('[GUIDE MODAL] unitKey:', unitKey, 'classKey:', classKey, 'week:', week, 'timeTableKey:', timeTableKey);

        const requestBody = ({});

        fetch(`/class/api/record/before-class`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({unitKey: unitKey, classKey: classKey, timeTableKey: timeTableKey, week: week})
            }
        )
            .then(res => {
                if (!res.ok) throw new Error('서버 오류');
                return res.json();
            })
            .then(json => {
                const body = json?.response ?? json;
                bindRecordModal(body, selectedNames);
                document.querySelector('.class-guide-modal').style.display = 'block';
            })
            .catch(err => {
                console.error('[GUIDE MODAL] fetch error:', err);
                bindRecordModal({}, selectedNames);
                document.querySelector('.class-guide-modal').style.display = 'block';
            });

        document.querySelector('.class-guide-modal').style.display = 'block';
    }
});

// 모달 데이터 갈아끼우기
function bindRecordModal(data, selectedNames) {
    const timeTableLabel = data.timeTableLabel || '-';
    const userName = data.userName || '';
    const content = data.content || '-';

    const dateEl = document.getElementById('record-date');
    const teacherEl = document.getElementById('record-teacher');
    const studentsEl = document.getElementById('record-students');
    const contentEl = document.getElementById('record-content');

    if (dateEl) dateEl.textContent = timeTableLabel;
    if (teacherEl) teacherEl.textContent = userName ? `${userName} 선생님` : '-';
    if (studentsEl) studentsEl.textContent = selectedNames.length ? selectedNames.join(', ') : '선택된 학생이 없습니다';
    if (contentEl) contentEl.textContent = content;
}

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("textarea.record-content").forEach(el => {
        if (el.value) {
            el.value = el.value.replace(/\\n/g, "\n");
        }
    });
});


document.addEventListener("DOMContentLoaded", () => {
    const sendbtn = document.getElementById("send-before-record");

    if (!sendbtn) {
        return;
    }

    sendbtn.addEventListener("click", () => {

        const checkedRows = Array.from(document.querySelectorAll("#record_tbody tr"))
            .filter(row => {
                const checkbox = row.querySelector("input[type=checkbox]");
                return checkbox && checkbox.checked;
            });

        if (checkedRows.length === 0) {
            alert("학생을 선택해주세요.");
            return;
        }

        // 선택된 학생 중 토큰만 추출
        const tokens = checkedRows
            .map(row => row.getAttribute("data-app-token"))
            .filter(token => token);

        const requestBody = {
            tokens: tokens,
            title: "test",
            body: "test"
        };

        console.log('tokens = ', tokens);

        fetch("/api/push/send", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(requestBody)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("서버 오류: " + response.status);
                }
                return response.json();
            })
            .then(async data => {
                console.log("성공:", data);
                alert("수업 전 안내가 발송되었습니다.");
                await insertBeforeClassNotice(checkedRows);

                await insertStudentAttendance();

            })
            .catch(error => {
                console.error("실패:", error);
                alert("발송을 실패했습니다. " + error.message);
            });
    });
});

// 수업 전 알림 발송 후 내용 저장
async function insertBeforeClassNotice(checkedRows) {
    const modalContent = document.querySelector("#record-content")?.textContent.trim() || "";

    const rows = Array.from(checkedRows || []);

    const notices = rows.map(row => ({
        studentId: row.getAttribute("data-student-id"),
        timeTableKey: document.querySelector(".class-btn.active")?.getAttribute("data-time-table-key"),
        week: document.querySelector(".week-btn.active")?.getAttribute("data-week"),
        classDate: document.getElementById("record_calendar")?.value || "",
        content: modalContent,
    }));
    console.log(notices);

    try {
        const response = await fetch("/class/api/before-notice/insert", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(notices)
        });

        if (!response.ok) {
            throw new Error("서버 오류: " + response.status);
        }

        console.log("발송 로그 저장 성공");
    } catch (error) {
        console.error("발송 로그 저장 실패:", error);
    }
}

// 수업 전 알림 발송 후 출결 칼럼 생성
async function insertStudentAttendance() {
    const selectedStudents = Array.from(document.querySelectorAll("#record_tbody tr"))
        .filter(row => row.querySelector("input[type=checkbox]")?.checked)
        .map(row => ({
            studentId: row.dataset.studentId,
            timeTableKey: state.timeTableKey,
            centerCode: row.dataset.centerCode,
            week: state.week,
            attendanceDate: new Date().toISOString().slice(0, 10) // yyyy-MM-dd
        }));

    if (selectedStudents.length === 0) {
        console.warn("선택된 학생이 없습니다.");
        return;
    }

    console.log('selectedStudents = ', selectedStudents);

    try {
        const res = await fetch("/class/api/attendance/insert", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(selectedStudents)
        });

        if (!res.ok) {
            throw new Error("출결 저장 실패 (status " + res.status + ")");
        }

        const data = await res.json();
        console.log("출결 insert 성공:", data);

    } catch (err) {
        console.error("출결 insert 에러:", err);

    }
}

// 수업 후 코멘트 전송
document.addEventListener("DOMContentLoaded", () => {
    const sendBtn = document.querySelector(".class-comment");

    if (!sendBtn) return;

    sendBtn.addEventListener("click", () => {
        // 체크된 행만 가져오기
        const checkedRows = Array.from(document.querySelectorAll("#record_tbody tr"))
            .filter(row => {
                const checkbox = row.querySelector("input[type=checkbox]");
                return checkbox && checkbox.checked;
            });

        if (checkedRows.length === 0) {
            alert("학생을 선택해주세요.");
            return;
        }

        // 체크된 학생들의 appToken 수집
        const tokens = checkedRows
            .map(row => row.getAttribute("data-app-token"))
            .filter(token => token && token.trim() !== "");

        if (tokens.length === 0) {
            alert("선택된 학생 중 발송 가능한 앱 토큰이 없습니다.");
            return;
        }

        // 요청 바디
        const requestBody = {
            tokens: tokens,
            title: "수업 후 코멘트",
            body: "오늘 수업 후 코멘트가 등록되었습니다."
        };

        console.log("after-class tokens = ", tokens);

        // 서버로 전송
        fetch("/api/push/after", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(requestBody)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("서버 오류: " + response.status);
                }
                return response.json();
            })
            .then(async data => {
                console.log("성공:", data);
                alert("수업 후 코멘트 발송이 완료되었습니다.");
                await insertAfterClassNotice(checkedRows);
                await updateAfterSend();
            })
            .catch(error => {
                console.error("실패:", error);
                alert("발송 실패: " + error.message);
            });
    });
});

// 수업 후 문자 전송 후 로그 저장
async function insertAfterClassNotice(checkedRows) {
    const notices = checkedRows.map(row => {
        return {
            studentId: row.getAttribute("data-student-id"),
            timeTableKey: document.querySelector(".class-btn.active")?.getAttribute("data-time-table-key"),
            afterClassKey: row.getAttribute("data-after-class-key"),
            week: document.querySelector(".week-btn.active")?.getAttribute("data-week"),
            content: document.querySelector(".record-content").value,
        }
    });

    try {
        const response = fetch("/class/api/after-notice/insert", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(notices)
        });

        if (!response.ok) {
            throw new Error("서버 오류: " + response.status);
        }

        console.log("발송 로그 저장 성공");

    } catch (error) {
        console.error("발송 로그 저장 실패:", error);
    }
}

async function updateAfterSend() {
    const selectedStudents = Array.from(document.querySelectorAll("#record_tbody tr"))
        .filter(row => row.querySelector("input[type=checkbox]")?.checked)
        .map(row => ({
            studentId: row.dataset.studentId,
            timeTableKey: state.timeTableKey,
            centerCode: row.dataset.centerCode,
            week: state.week,
        }));

    if (selectedStudents.length === 0) {
        console.warn("선택된 학생이 없습니다.");
        return;
    }

    try {
        const res = await fetch("/class/api/afterSend/update", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(selectedStudents)
        });

        if (!res.ok) {
            throw new Error("발송내역 저장 실패 (status " + res.status + ")");
        }

        const data = await res.json();
        console.log("발송 내역 업데이트 성공:", data);

    } catch (err) {
        console.error("발송 내역 업데이트 에러:", err);

    }
}


// =================================== //
// ==                               == //
// ==          보강 관리            == //
// ==                               == //
// =================================== //

// 보강 여부 수정
document.addEventListener('DOMContentLoaded', () => {
    document.body.addEventListener("change", (e) => {
        if (e.target.matches(".checkbox-group input[type=checkbox]")) {
            const row = e.target.closest("tr");
            const remedialKey = row.dataset.id;
            const action = e.target.checked;
            const m = document.getElementById('currentMonth')?.textContent.trim().match(/(\d{4})\D+(\d{1,2})/);
            const year = m?.[1], month = m ? m[2].padStart(2, '0') : null;
            fetch(`/class/remedial/update?year=${year}&month=${month}`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({
                    remedialKey: remedialKey,
                    action: action
                })
            })
                .then((res) => res.json())
                .then((data) => {
                    if (data.success) {
                        console.log(data.response);
                        renderTables(data.response);
                    } else {
                        alert("저장 실패");
                        e.target.checked = !action;
                    }
                })
                .catch((err) => {
                    console.error(err);
                    e.target.checked = !action;
                });
        }
    });
});

function renderTables(data) {
    const $left = $(".sup-left tbody");
    $left.empty();
    data.leftRemedials.forEach((item, idx) => {
        const html = `
          <tr data-id="${item.remedialKey}">
            <td>${idx + 1}</td>
            <td>${item.studentName}</td>
            <td>${item.absenceDate}</td>
            <td>${item.remedialSubject}</td>
            <td class="cal-content">
                <div class="icon-field time-input cal-adjust" style="margin-bottom: 0;">
                    <span class="selected-datetime"> ${item.remedialDate === '9999-12-31' ? "날짜를 선택하세요" : item.remedialDate} </span>
                    <input type="date" class="datetime-input hidden-picker" />
                    <button type="button" class="icon-btn calendar-btn" style="background: transparent;">
                        <img src="/image/calendar.png" alt="달력 아이콘" />
                    </button>
                </div>
            </td>
            <td>${item.userName}</td>
            <td class="checkbox-group">
              <input type="checkbox" />
            </td>
          </tr>
        `;
        $left.append(html);
        // 추가된 행에 이벤트 바인딩
        const newRow = $left.find("tr").last()[0];
        bindDatePickerEvents(newRow);
    });

    const $right = $(".sup-right tbody");
    $right.empty();
    data.rightRemedials.forEach((item, idx) => {
        $right.append(`
          <tr data-id="${item.remedialKey}">
            <td>${idx + 1}</td>
            <td>${item.studentName}</td>
            <td>${item.absenceDate}</td>
            <td>${item.remedialSubject}</td>
            <td class="cal-content">${item.remedialDate === '9999-12-31' ? "날짜를 선택하세요" : item.remedialDate}</td>
            <td>${item.userName}</td>
            <td class="checkbox-group">
              <input type="checkbox" checked />
            </td>
          </tr>
        `);
    });
}

// 보강 날짜 변경
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("tr[data-id]").forEach(bindDatePickerEvents);
});

function bindDatePickerEvents(row) {
    const calendarBtn = row.querySelector(".calendar-btn");
    const dateInput = row.querySelector(".datetime-input");
    const selectedSpan = row.querySelector(".selected-datetime");

    if (calendarBtn && dateInput && selectedSpan) {
        calendarBtn.addEventListener("click", () =>
            dateInput.showPicker?.() || dateInput.click()
        );

        dateInput.addEventListener("change", () => {
            if (dateInput.value) {
                selectedSpan.textContent = dateInput.value;
                console.log(selectedSpan.closest('tr').dataset.id);
                fetch("/class/remedial/updateDate", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({
                        remedialKey: selectedSpan.closest("tr").dataset.id,
                        remedialDate: dateInput.value
                    })
                })
                    .then(res => res.json())
                    .then(data => {
                        if (!data.success) {
                            alert("저장 실패");
                        }
                    })
                    .catch(err => console.error("서버 오류:", err));
            }
        });
    }
}


// =================================== //
// ==                               == //
// ==        월간평가(초등)         == //
// ==                               == //
// =================================== //

document.addEventListener("DOMContentLoaded", () => {
    const monthInput = document.getElementById("monthly_calendar");
    const monthDisplay = document.getElementById("monthly_current");
    const teacherSelect = document.getElementById("monthly-teacher-select");
    const calendarBtn = document.querySelector(".calendar-open");
    const dayBtns = document.querySelectorAll(".day-btn");

    if (!monthInput || !teacherSelect) return;

    // 오늘 날짜 기본값 세팅
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");
    monthInput.value = `${yyyy}-${mm}`;
    monthDisplay.textContent = `${yyyy}년 ${mm}월`;

    // 현재 선택된 요일 가져오기
    function getCurrentDayName() {
        const activeBtn = document.querySelector(".day-btn.active");
        return activeBtn ? activeBtn.dataset.week : "mon"; // 기본값 월요일
    }

    // 공통 데이터 로딩 함수
    function loadMonthlyData() {
        const [yy, mm] = monthInput.value.split("-");
        const userCode = teacherSelect.value;
        const dayname = getCurrentDayName();

        const requestBody = {yy, mm, userCode, dayname};

        fetch("/class/api/monthly/classes", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(requestBody)
        })
            .then(res => res.json())
            .then(data => {
                console.log("서버 응답:", data);
                renderMonthlyClassList(data.response);

                if (data.response && data.response.length > 0) {
                    const timeTableKey = data.response[0].timeTableKey ?? "";
                    if (timeTableKey) {
                        fetch("/class/api/monthly/timeTableKey", {
                            method: "POST",
                            headers: {"Content-Type": "application/json"},
                            body: JSON.stringify({timeTableKey})
                        })
                            .then(res => res.json())
                            .then(data => renderMonthlyStudentList(data.response))
                            .catch(err => console.error(err));
                    }
                }
            })
            .catch(err => console.error("조회 에러:", err));
    }

    // 달력
    calendarBtn.addEventListener("click", () => {
        monthInput.showPicker?.();
        monthInput.click();
    });

    // 월 변경
    monthInput.addEventListener("change", () => {
        if (monthInput.value) {
            const [year, month] = monthInput.value.split("-");
            monthDisplay.textContent = `${year}년 ${month}월`;
            loadMonthlyData();
        }
    });

    // 선생님 변경
    teacherSelect.addEventListener("change", () => {
        loadMonthlyData();
    });

    // 요일 버튼 변경
    dayBtns.forEach(btn => {
        btn.addEventListener("click", () => {

            dayBtns.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            loadMonthlyData();
        });
    });
});

// 수업 시간표 변경 시 데이터 변경
document.addEventListener("DOMContentLoaded", () => {
    const classList = document.getElementById("monthly-class-list");
    if (!classList) {
        return;
    }
    classList.addEventListener("click", (e) => {
        const target = e.target.closest("li");
        if (!target) return;

        document.querySelectorAll("#monthly-class-list li").forEach(li => li.classList.remove("active"));
        target.classList.add("active");

        const timeTableKey = target.dataset.classId;

        // fetch 요청
        const requestBody = {timeTableKey: timeTableKey};
        fetch("/class/api/monthly/timeTableKey", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(requestBody)
        })
            .then(res => res.json())
            .then(data => {
                console.log("응답 데이터:", data);
                renderMonthlyStudentList(data.response);
            })
            .catch(err => console.error("에러:", err));
    });

});

// 수업 시간표 변경 함수
function renderMonthlyClassList(classes) {
    const classList = document.querySelector("#monthly-class-list");
    classList.innerHTML = "";

    classes.forEach((cls, idx) => {
        const li = document.createElement("li");
        li.className = "class-btn" + (idx === 0 ? " active" : "");

        classList.addEventListener("click", (e) => {
            const target = e.target.closest("li.class-btn");
            if (target) {
                console.log("Clicked:", target.dataset.classId);
                // 필요하면 active 클래스 토글
                classList.querySelectorAll("li").forEach(li => li.classList.remove("active"));
                target.classList.add("active");
            }
        });

        li.dataset.classId = cls.classCode;
        li.dataset.time = cls.classTime;
        li.dataset.subject = cls.classSubject;

        const spanTime = document.createElement("span");
        spanTime.textContent = cls.classTime;

        const br = document.createElement("br");

        const spanSubject = document.createElement("span");
        spanSubject.textContent = cls.classSubject;

        li.appendChild(spanTime);
        li.appendChild(br);
        li.appendChild(spanSubject);

        classList.appendChild(li);
    });
}

// 본문 변경
function renderMonthlyStudentList(students) {
    const tbody = document.querySelector("#monthly_student_tbody");
    tbody.innerHTML = ""; // 기존 행 초기화

    if (!students || students.length === 0) {
        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = 8;
        td.textContent = "등록된 학생이 없습니다.";
        td.style.textAlign = "center";
        td.style.color = "#666";
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }

    students.forEach((stu, idx) => {
        const tr = document.createElement("tr");
        tr.dataset.studentId = stu.studentId ?? "";
        tr.dataset.timeTableKey = stu.timeTableKey ?? "";


        const scores = (stu.scores && stu.scores.length > 0) ? stu.scores[0] : {};

        tr.innerHTML = `
      <td class="checkbox-group"><input type="checkbox" /></td>
    <td>${idx + 1}</td>
    <td>${stu.studentName}</td>
    <td>
      <div class="evaluation">
        <div class="number-grid">
          ${Array.from({length: 8}, (_, i) => {
            const qKey = "question" + (i + 1);
            const isActive = scores[qKey] === true ? "active" : "";
            const value = scores[qKey] === true ? "true" : "false";
            return `<button class="btn-number ${isActive}" value="${value}">${i + 1}번</button>`;
        }).join("")}
        </div>
        <div class="action-buttons">
          <button class="btn-reset">초기화</button>
          <button class="btn-result" id="monthly-result-btn">결과보기</button>
        </div>
      </div>
    </td>
    <td class="cell-middle">
      <div class="after-comment">
        <textarea class="comment-text" placeholder="내용을 입력해주세요.">
${stu.studentName} 학생은 어휘의 정의를 정확히 이해하고, 유사 단어 사이에서도 핵심 의미를 잘 구분 했습니다. 
다만, 비슷한 자형의 한자들이 함께 제시될 때는 의미를 중심으로 구별하는 연습이 더 필요합니다.
        </textarea>
      </div>
    </td>
    <td>
      <div class="cell-middle">
        <div class="after-comment">
          <textarea class="comment-text" placeholder="내용을 입력해주세요."></textarea>
        </div>
      </div>
    </td>
    <td class="send-ornot"><img src="/image/send2.png" alt=""></td>
    <td class="pre-search"><img src="/image/pre-search.png" alt=""></td>
  `;

        tbody.appendChild(tr);
    });
}

document.addEventListener("DOMContentLoaded", () => {
    const tbody = document.getElementById("monthly_student_tbody");
    if (!tbody) return;

    tbody.addEventListener("click", (e) => {
        const target = e.target;

        if (target.classList.contains("btn-number")) {
            target.classList.toggle("active");

            if (target.classList.contains("active")) {
                target.value = "true";
            } else {
                target.value = "false";
            }

            console.log(`${target.textContent} 상태:`, target.value);
        }

        if (target.classList.contains("btn-reset")) {
            const row = target.closest("tr");
            if (!row) return;

            row.querySelectorAll(".btn-number").forEach(btn => {
                btn.classList.remove("active");
                btn.value = "false";
            });
        }
    });
});

document.addEventListener("DOMContentLoaded", () => {
    const tbody = document.getElementById("monthly_student_tbody");
    if (!tbody) return;

    tbody.addEventListener("click", (e) => {
        const target = e.target;

        if (target.classList.contains("btn-result")) {
            const row = target.closest("tr");
            const studentId = row.dataset.studentId;
            const classCode = row.dataset.classCode;
            const monthValue = document.getElementById("monthly_calendar").value;
            let yy, mm;

            if (monthValue) {
                [yy, mm] = monthValue.split("-");
            } else {
                const today = new Date();
                yy = today.getFullYear().toString();
                mm = String(today.getMonth() + 1).padStart(2, "0");
            }
            const scores = {};
            row.querySelectorAll(".btn-number").forEach((btn, idx) => {
                const qKey = "question" + (idx + 1);
                scores[qKey] = (btn.value === "true");
            });

            const requestBody = {
                studentId: studentId,
                classCode: classCode,
                scores: [scores],
                yy: yy,
                mm: mm
            };

            fetch("/class/api/monthly/update_score", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify(requestBody)
            })
                .then(res => res.json())
                .then(data => {
                    console.log("✅ 서버 응답:", data);
                })
                .catch(err => {
                    console.error("❌ 오류:", err);
                });
        }
    });
});

// 모달 오픈
document.addEventListener("DOMContentLoaded", function () {
    const preSearchBtns = document.querySelectorAll(".pre-search img");
    const modal = document.querySelector(".pre-modal");
    if (preSearchBtns.length === 0 || !modal) {
        return;
    }
    const closeBtn = modal.querySelector(".btn-close");

    // 열기 (모든 버튼에 대해 이벤트 등록)
    preSearchBtns.forEach((btn) => {
        btn.addEventListener("click", function () {
            modal.style.display = "block";
        });
    });

    // 닫기
    closeBtn.addEventListener("click", function (e) {
        e.preventDefault();
        modal.style.display = "none";
    });

    // 모달 바깥 클릭 시 닫기
    window.addEventListener("click", function (e) {
        if (e.target === modal) {
            modal.style.display = "none";
        }
    });
});

