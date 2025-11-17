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

// 줄바꿈
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("textarea.record-content").forEach(el => {
        if (el.value) {
            el.value = el.value.replace(/\\n/g, "\n");
        }
    });
});

document.addEventListener("click", function (e) {
    const btn = e.target.closest(".counsel-type button");
    if (!btn) return;

    const parent = btn.closest(".counsel-type");
    if (!parent) return;

    parent.querySelectorAll("button").forEach(b => b.classList.remove("active"));

    btn.classList.add("active");
});

document.addEventListener("click", function (e) {
    const target = e.target.closest(".remarks img");
    if (!target) return;

    document.querySelector(".remarks-modal").style.display = "block";
});

// 수업 전 알림 발송
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

        const tokens = checkedRows
            .map(row => row.getAttribute("data-app-token"))
            .filter(token => token);

        const requestBody = {
            tokens: tokens,
            title: "test",
            body: "test"
        };


        fetch("/api/push/before", {
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
        const checkedRows = Array.from(document.querySelectorAll("#record_tbody tr"))
            .filter(row => {
                const checkbox = row.querySelector("input[type=checkbox]");
                return checkbox && checkbox.checked;
            });

        if (checkedRows.length === 0) {
            alert("학생을 선택해주세요.");
            return;
        }

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