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
        console.log('전월 데이터 불러오기!!');
        fetch(`/class/api/load_time_table`)
            .then(res => {
                return res.json();
            })
            .then(data => {
                console.log(data.response);
            })
            .catch(err => {
                console.log('오류 발생');
            });

    })
});

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
            const userCode = 'DAE001cos';
            const rows = tab.querySelectorAll('tr.time-row');
            for (const row of rows) {
                const periodNo = row.querySelector('td:nth-child(2)').innerText.trim();
                const startTime = row.querySelector('.time-start input').value;
                const endTime = row.querySelector('.time-end input').value;
                const classKey = row.querySelector('select[name="classKey"]').value;
                const unitKey = row.querySelector('select[name="unitKey"]').value;
                const gradeKey = row.querySelector('select[name="gradeKey"]').value;

                if (!startTime || !endTime || !classKey || !unitKey || !gradeKey) {
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
                    showAlert({icon: 'success', title: '시간표가 저장되었습니다.'})
                        .then(() => window.location.reload());
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
                fetch('/class/delete_student', {
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

// ===========================수업일지===========================//

const DAY_EN = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];
const DAY_KR = ['일', '월', '화', '수', '목', '금', '토'];
const toYmd = (d) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
const toK = (d) =>
    `${d.getFullYear()}년 ${d.getMonth() + 1}월 ${d.getDate()}일 (${DAY_KR[d.getDay()]})`;
const state = {
    teacherId: null,   // 'all' 또는 userCode
    date: null,        // 'YYYY-MM-DD'
    classCode: null,   // timeTableKey
    week: null,        // 'ju_1'..'ju_4'
};

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

function buildByClassBody(classCode, week) {
    return {
        timeTableKey: classCode ?? null,
        week: week ?? null,
    };
}

/* ---------- 콘솔 출력 헬퍼 ---------- */
function logRequestBody(title, body) {
    try {
        console.groupCollapsed(`%c${title}`, 'color:#1976d2;font-weight:bold;');
        console.log('requestBody:', body);
    } finally {
        console.groupEnd();
    }
}

function getActiveTimeTableKey() {
    const el = document.querySelector('.class-list .class-btn.active');
    return el?.dataset.classCode || el?.dataset.classId || null;
}

// window.loadClassData = function (el) {
//     const code = el?.dataset?.classCode || el?.dataset?.classId || null;
//     state.classCode = code; // 상태도 유지(선택사항이지만 권장)
//
//     const requestBody = buildByClassBody(code, state.week);
//     logRequestBody('수업 변경 → by-class requestBody', requestBody);
//     loadStudentList(code); // ← 키 전달
// };

function abortInFlight() {
    let inFlightController = null;

    if (inFlightController) inFlightController.abort();
    inFlightController = new AbortController();
    return inFlightController.signal;
}

document.addEventListener('DOMContentLoaded', () => {
    const teacherSel = document.getElementById('teacher-select');
    const dateInput = document.getElementById('record_calendar');
    const dateLabel = document.getElementById('record_current');
    const calBtn = document.querySelector('.month-title .calendar-open');
    const weekWrap = document.querySelector('.week-selector');
    const classList = document.querySelector('.class-list');

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
        state.classCode = initActiveClass.dataset.classCode || initActiveClass.dataset.classId || null;
    }

    const initActiveWeek = weekWrap?.querySelector('.week-btn.active');
    if (initActiveWeek) state.week = initActiveWeek.dataset.week || null;

    /* -------- 이벤트: 선생님 변경 -------- */
    if (teacherSel) {
        teacherSel.addEventListener('change', () => {
            state.teacherId = teacherSel.value || null;
            // 선생님 변경 → by-date 요청 바디를 콘솔에 출력
            const requestBody = buildByDateBody(state.date, state.teacherId);

            loadOverviewFromState();

            logRequestBody('선생님 변경 → by-date requestBody', requestBody);
        });
    }

    /* -------- 이벤트: 날짜 변경 -------- */
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
            const requestBody = buildByDateBody(state.date, state.teacherId);

            loadOverviewFromState();

            logRequestBody('날짜 변경 → by-date requestBody', requestBody);
        });
    }

    /* -------- 이벤트: 수업 변경 -------- */
    // HTML에 inline onclick="loadClassData(this)"가 있으므로, 덮어씁니다(네트워크X, 로그만).
    window.loadClassData = function (el) {
        const code = el?.dataset?.classCode || el?.dataset?.classId || null;
        state.classCode = code;

        const requestBody = buildByClassBody(state.classCode, state.week, state.date);

        loadStudentList();

        logRequestBody('수업 변경 → by-class requestBody', requestBody);
    };

    // (보강) 혹시 위임 방식으로 클릭되는 경우도 지원
    if (classList) {
        classList.addEventListener('click', (e) => {
            const li = e.target.closest('.class-btn');
            if (!li) return;
            window.loadClassData(li); // 동일 로직
        });
    }

    /* -------- 이벤트: 주차 변경 -------- */
    if (weekWrap) {
        weekWrap.querySelectorAll('.week-btn').forEach((btn) => {
            btn.addEventListener('click', () => {
                state.week = btn.dataset.week || null;

                const activeKey = getActiveTimeTableKey(); // ← DOM에서 읽기
                if (!activeKey) {
                    renderRecordStudentList([]);
                    logRequestBody('주차 변경 → but no active class', { week: state.week });
                    return;
                }

                const requestBody = buildByClassBody(activeKey, state.week);
                logRequestBody('주차 변경 → by-class requestBody', requestBody);
                loadStudentList(activeKey); // ← 키 전달
            });
        });
    }
});

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
            // ---- 상태/뷰 초기화 ----
            state.classCode = null;
            renderRecordStudentList([]);
            return;
        }

        // 첫 수업 기준으로 학생 목록
        const firstKey = list1[0].timeTableKey;
        state.classCode = String(firstKey); // 상태도 갱신

        const body2 = buildByClassBody(firstKey, state.week);
        const res2 = await fetch('/class/api/record/student', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body2),
            signal
        });
        if (!res2.ok) throw new Error('서버 응답 오류(학생)');
        const data2 = await res2.json();

        const list2 = Array.isArray(data2?.response) ? data2.response : [];
        renderRecordStudentList(list2);
    } catch (e) {
        if (e.name === 'AbortError') return;
        console.error('[loadOverviewFromState] 실패:', e);
        renderRecordStudentList([]);
    }
}

async function loadStudentList(timeTableKey = getActiveTimeTableKey()) {
    const signal = abortInFlight();

    try {
        if (!timeTableKey) {
            renderRecordStudentList([]);
            return;
        }
        const body = buildByClassBody(timeTableKey, state.week);
        const res = await fetch('/class/api/record/student', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body),
            signal
        });
        if (!res.ok) throw new Error('서버 응답 오류(학생)');
        const data = await res.json();
        const list = Array.isArray(data?.response) ? data.response : [];
        renderRecordStudentList(list);
    } catch (e) {
        if (e.name === 'AbortError') return;
        console.error('[loadStudentList] 실패:', e);
        renderRecordStudentList([]);
    }
}

function renderRecordClassList(list) {
    const ul = document.querySelector('.class-list');
    if (!ul) return;

    ul.innerHTML = '';

    list.forEach((item, idx) => {
        const li = document.createElement('li');
        li.className = 'class-btn' + (idx === 0 ? ' active' : '');
        li.dataset.classCode = item.timeTableKey;

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

    // 첫 항목이 있다면 state.classCode 동기화
    if (list.length > 0) {
        state.classCode = String(list[0].timeTableKey);
    }
}

function renderRecordStudentList(list, tbodySel = '#record_tbody') {
    const tbody = typeof tbodySel === 'string' ? document.querySelector(tbodySel) : tbodySel;
    if (!tbody) {
        console.error('[renderStudents] tbody를 찾을 수 없습니다:', tbodySel);
        return;
    }

    const items = Array.isArray(list) ? list : [];
    const frag = document.createDocumentFragment();

    items.forEach((s, idx) => {
        console.log(s.attendanceName);
        const tr = document.createElement('tr');
        tr.dataset.studentId = s.studentNo ?? '';
        tr.innerHTML += `<td class="checkbox-group"><input type="checkbox" /></td>`;
        tr.innerHTML += `<td>${idx + 1}</td>`;
        tr.innerHTML += `<td class="studentName">${s.studentName ?? ''}</td>`;

        // 4) 출결
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

        // 5) 보강일자
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

        // 6) 특이사항
        tr.innerHTML += `
          <td>
            <ul class="tag-list">
              <li>#숙제</li>
              <li>#교재준비</li>
              <li class="remarks"><img src="/image/add.png" alt=""></li>
            </ul>
          </td>`;

        // 7) 상담기록
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

        // 8) 수업 후 코멘트
        tr.innerHTML += `
          <td>
            <div class="cell-middle">
              <div class="after-comment">
                <textarea class="comment-text" placeholder="내용을 입력해주세요."></textarea>
              </div>
            </div>
          </td>`;

        // 9) 발송여부
        tr.innerHTML += `
          <td class="send-ornot">
            <img src="/image/send3.png" alt="">
            <img src="/image/send2.png" alt="">
          </td>`;

        frag.appendChild(tr);
    });

    if (items.length === 0) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 9;
        td.style.textAlign = 'center';
        td.textContent = '학생이 없습니다';
        tr.appendChild(td);
        tbody.replaceChildren(tr);
    } else {
        tbody.replaceChildren(frag);
    }

}


// 수업 안내 발송 모달
document.addEventListener('click', function (e) {
    if (e.target.closest('.class-guide')) {
        const activeClass = document.querySelector('.class-list .class-btn.active');
        const activeWeek = document.querySelector('.week-selector .week-btn.active');

        if (!activeClass) {
            console.warn('[GUIDE MODAL] 활성화된 클래스가 없습니다.');
            return;
        }

        const selectedNames = Array.from(document.querySelectorAll('#record_tbody tr'))
            .filter(tr => tr.querySelector('input[type="checkbox"]:checked'))
            .map(tr => tr.querySelector('.studentName')?.textContent.trim() || '');

        const unitKey = activeClass.dataset.classUnitkey;    // data-class-unitKey
        const classKey = activeClass.dataset.classClasskey;  // data-class-classKey
        const timeTableKey = activeClass.dataset.classCode;  // data-class-code
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
                bindRecordModal(body, selectedNames); // 🔹 바인딩 함수 호출
                document.querySelector('.class-guide-modal').style.display = 'block';
            })
            .catch(err => {
                console.error('[GUIDE MODAL] fetch error:', err);
                bindRecordModal({}, selectedNames); // 실패 시 빈 데이터라도 반영
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

// 월 변경 시 데이터 변경
document.addEventListener("DOMContentLoaded", () => {
    const monthInput = document.getElementById("monthly_calendar");
    const monthDisplay = document.getElementById("monthly_current");
    const calendarBtn = document.querySelector(".calendar-open");

    if (!monthInput) return;

    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");
    monthInput.value = `${yyyy}-${mm}`;
    monthDisplay.textContent = `${yyyy}년 ${mm}월`;

    calendarBtn.addEventListener("click", () => {
        monthInput.showPicker?.();
        monthInput.click();
    });

    monthInput.addEventListener("change", () => {
        if (monthInput.value) {
            const [year, month] = monthInput.value.split("-");
            monthDisplay.textContent = `${year}년 ${month}월`;

            const requestBody = {
                yy: year,
                mm: month
            };

            fetch("/class/api/monthly/by-month", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(requestBody)
            })
                .then(res => res.json())
                .then(data => {
                    console.log("서버 응답:", data);
                    renderMonthlyClassList(data.response);

                    let classCode = "";

                    if (data.response && data.response.length > 0) {
                        classCode = data.response[0].classCode ?? "";
                    }

                    fetch("/class/api/monthly/by-classCode", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify({classCode: classCode})
                    })
                        .then(res => res.json())
                        .then(data => {
                            console.log("상세 응답:", data);
                            renderMonthlyStudentList(data.response);
                        })
                        .catch(err => console.error(err));
                })
        }
    });
});

// 선생님 변경 시 데이터 변경
document.addEventListener("DOMContentLoaded", () => {
    const teacherSelect = document.getElementById("monthly-teacher-select");
    if (!teacherSelect) return;

    teacherSelect.addEventListener("change", () => {
        const teacherNo = teacherSelect.value;

        if (teacherNo === "all") {
            console.log("전체 선택");
            return;
        }
        // GET + 쿼리스트링 방식
        fetch(`/class/api/monthly/${teacherNo}`, {
            method: "GET",
            headers: {
                "Accept": "application/json"
            }
        })
            .then(res => res.json())
            .then(data => {
                console.log("서버 응답:", data);
                renderMonthlyClassList(data.response);

                let classCode = "";

                if (data.response && data.response.length > 0) {
                    classCode = data.response[0].classCode ?? "";
                }

                fetch("/class/api/monthly/by-classCode", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({classCode: classCode})
                })
                    .then(res => res.json())
                    .then(data => {
                        console.log("상세 응답:", data);
                        renderMonthlyStudentList(data.response);
                    })
                    .catch(err => console.error(err));

            })
            .catch(err => console.error("조회 에러:", err));
    });
});

// 수업 시간표 변경 시 데이터 변경
document.addEventListener("DOMContentLoaded", () => {
    const classList = document.getElementById("class-list");
    if (!classList) {
        return;
    }
    classList.addEventListener("click", (e) => {
        const target = e.target.closest("li");
        if (!target) return;

        document.querySelectorAll("#class-list li").forEach(li => li.classList.remove("active"));
        target.classList.add("active");

        const classCode = target.dataset.classId;

        // fetch 요청
        const requestBody = {classCode: classCode};
        fetch("/class/api/monthly/by-classCode", {
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
    const classList = document.querySelector(".class-list");
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
        tr.dataset.studentId = stu.studentNo ?? "";
        tr.dataset.classCode = stu.classCode ?? "";


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

        // 번호 버튼 클릭 시
        if (target.classList.contains("btn-number")) {
            target.classList.toggle("active");

            // ✅ value를 true/false로 변경
            if (target.classList.contains("active")) {
                target.value = "true";
            } else {
                target.value = "false";
            }

            console.log(`${target.textContent} 상태:`, target.value);
        }

        // 초기화 버튼 클릭 시
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
                // 기본값: 오늘 날짜
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

