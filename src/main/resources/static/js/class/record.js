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

function buildByClassBody(userCode, timeTableKey, date, classKey, unitKey) {
    return {
        userCode: userCode ?? null,
        timeTableKey: timeTableKey ?? null,
        date: date ?? null,
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

function setupSelectAllCheckbox() {
    const selectAllCheckbox = document.getElementById('select-all-students');
    if (!selectAllCheckbox) return;

    selectAllCheckbox.addEventListener('change', function () {
        const checkboxes = document.querySelectorAll('#record_tbody tr input[type="checkbox"]');
        checkboxes.forEach(checkbox => {
            checkbox.checked = this.checked;
        });
    });
}

function updateSelectAllCheckbox() {
    const selectAllCheckbox = document.getElementById('select-all-students');
    if (!selectAllCheckbox) return;

    const checkboxes = document.querySelectorAll('#record_tbody tr input[type="checkbox"]');
    const checkedCount = Array.from(checkboxes).filter(cb => cb.checked).length;

    selectAllCheckbox.checked = checkboxes.length > 0 && checkedCount === checkboxes.length;
    selectAllCheckbox.indeterminate = checkedCount > 0 && checkedCount < checkboxes.length;
}

async function loadUnsentStatus(userCode) {
    try {
        const res = await fetch('/class/api/record/unsent-check', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({userCode: userCode ?? ''})
        });
        const data = await res.json();
        const ids = data.response || [];
        const bar = document.getElementById('unsent-alert-bar');
        const text = document.getElementById('unsent-alert-text');
        if (!bar) return;

        if (ids.length > 0) {
            if (text) text.textContent = `${ids.length}명의 학생에게 수업 후 알림이 발송되지 않았습니다.`;
            bar.style.display = 'flex';
        } else {
            bar.style.display = 'none';
        }
    } catch (e) {
        console.error('[loadUnsentStatus] 실패:', e);
    }
}

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
        state.classType = initActiveClass.dataset.classType || null;
        toggleCommentBtn();
    }

    const initActiveWeek = weekWrap?.querySelector('.week-btn.active');
    if (initActiveWeek) state.week = initActiveWeek.dataset.week || null;

    setupSelectAllCheckbox();
    loadInitialData();

    if (teacherSel) {
        teacherSel.addEventListener('change', () => {
            state.teacherId = teacherSel.value || null;
            loadOverviewFromState();
            loadUnsentStatus(state.teacherId);
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
        state.classType = el?.dataset?.classType || null;
        toggleCommentBtn();
        loadStudentList();
    };

    if (classList) {
        classList.addEventListener('click', (e) => {
            const li = e.target.closest('.class-btn');
            if (!li) return;
            window.loadClassData(li);
        });
    }

    if (weekWrap) {
        weekWrap.querySelectorAll('.week-btn').forEach((btn) => {
            btn.addEventListener('click', () => {
                state.week = btn.dataset.week || null;
                const activeKey = getActiveTimeTableKey();
                if (!activeKey) {
                    renderRecordStudentList([]);
                    return;
                }
                loadStudentList(activeKey);
            });
        });
    }

    document.addEventListener('change', (e) => {
        if (e.target.matches('#record_tbody tr input[type="checkbox"]')) {
            updateSelectAllCheckbox();
        }
    });
});

async function loadInitialData() {
    const timeTableKey = state.timeTableKey;
    if (!timeTableKey) {
        console.warn('초기 timeTableKey가 없습니다.');
        return;
    }
    await loadStudentList(timeTableKey);
}

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
        const firstClassType = list1[0].classType;

        state.timeTableKey = String(firstTimeTableKey);
        state.classKey = String(firstClassKey);
        state.unitKey = String(firstUnitKey);
        state.classType = String(firstClassType);
        toggleCommentBtn();

        await loadStudentList(firstTimeTableKey);

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

        const body = buildByClassBody(state.teacherId, timeTableKey, state.date, state.classKey, state.unitKey);
        console.log(JSON.stringify(body));
        const res = await fetch('/class/api/record/student', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body),
            signal
        });

        if (!res.ok) throw new Error('서버 응답 오류(학생)');
        const data = await res.json();

        if (data.response?.week) {
            state.week = data.response.week;
        }

        const list = Array.isArray(data?.response?.students) ? data.response.students : [];
        const afterClassList = Array.isArray(data?.response?.afterClass) ? data.response.afterClass : [];

        renderRecordStudentList(list, afterClassList);
    } catch (e) {
        if (e.name === 'AbortError') return;
        console.error('[loadStudentList] 실패:', e);
        renderRecordStudentList([]);
    }
}

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
        li.dataset.classType = item.classType;
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

function renderRecordStudentList(list, afterClassList = [], tbodySel = '#record_tbody') {
    const tbody = typeof tbodySel === 'string' ? document.querySelector(tbodySel) : tbodySel;
    if (!tbody) {
        console.error('[renderStudents] tbody를 찾을 수 없습니다:', tbodySel);
        return;
    }

    const items = Array.isArray(list) ? list : [];
    const afterClasses = Array.isArray(afterClassList) ? afterClassList : [];
    const frag = document.createDocumentFragment();

    items.forEach((s, idx) => {
        const afterClass = afterClasses[idx] || {};

        const tr = document.createElement('tr');
        tr.dataset.studentId = s.studentId ?? '';
        tr.dataset.appToken = s.appToken ?? '';
        tr.dataset.centerCode = s.centerCode ?? '';
        tr.dataset.afterClassKey = afterClass.afterClassKey ?? '';
        tr.dataset.attendanceKey = s.attendanceKey ?? '';

        tr.innerHTML += `<td class="checkbox-group"><input type="checkbox" /></td>`;
        tr.innerHTML += `<td>${idx + 1}</td>`;
        tr.innerHTML += `<td class="studentName">${s.studentName ?? ''}</td>`;

        const remarks = Array.isArray(s.remarks) ? s.remarks.filter(r => r != null) : [];
        const tagItems = remarks.slice(0, 3).map(name => '<li>' + name + '</li>').join("");
        const attendance = s.attendanceName ?? '결석';
        let statusClass;
        switch (attendance) {
            case '출석 완료': statusClass = 'attend'; break;
            case '지각':      statusClass = 'late';   break;
            case '수업 전':   statusClass = 'before'; break;
            default:          statusClass = 'absent';
        }

        tr.innerHTML += `
        <td>
            <div>
                <select class="status-badge ${statusClass} stbox"
                        data-student-id="${s.studentId}"
                        data-attendance-key="${statusClass ?? ''}"
                        data-original="${attendance}"
                        style="width: 100%; text-align: center; cursor: pointer;">
                    <option value="present" ${attendance === '출석 완료' ? 'selected' : ''}>출석 완료</option>
                    <option value="late"    ${attendance === '지각'     ? 'selected' : ''}>지각</option>
                    <option value="absent"  ${attendance === '결석'     ? 'selected' : ''}>결석</option>
                    <option value="before"  ${attendance === '수업 전'  ? 'selected' : ''}>수업 전</option>
                </select>
                <div class="time-boxes">
                    <div class="time-start"> 등원
                        <input type="text" class="time_input start-time"
                               value="${s.inTime ?? ''}" data-original="${s.inTime ?? ''}"
                               placeholder="00:00" inputmode="numeric" maxlength="5"/>
                    </div>
                    <div class="time-end"> 하원
                        <input type="text" class="time_input end-time"
                               value="${s.outTime ?? ''}" data-original="${s.outTime ?? ''}"
                               placeholder="00:00" inputmode="numeric" maxlength="5"/>
                    </div>
                </div>
                <button type="button"
                        class="btn-update-attendance stbox"
                        data-student-id="${s.studentId}"
                        data-attendance-key="${s.attendanceKey ?? ''}"
                        style="width: 100%; margin-top: 8px; padding: 6px; background: #c8c8c8; color: white; border: none; border-radius: 4px; cursor: pointer;">
                    변경
                </button>
            </div>
        </td>`;

        tr.innerHTML += `
        <td>
            ${!s.remedialDate
            ? '--'
            : `<div class="icon-field time-input cal-adjust" style="margin-bottom: 0; text-align:center;">
                       <span class="selected-datetime">${s.remedialDate === '9999-12-31' ? '날짜를 선택하세요' : s.remedialDate}</span>
                       <input type="date" class="datetime-input hidden-picker" value="${s.remedialDate === '9999-12-31' ? '' : s.remedialDate}">
                       <button type="button" class="icon-btn calendar-btn" style="background:transparent;">
                           <img src="/image/calendar.png" alt="달력 아이콘">
                       </button>
                   </div>`
        }
        </td>`;

        tr.innerHTML += `
        <td>
            <ul class="tag-list">
                ${tagItems}
                <li class="remarks">
                    <img src="/image/add.png" alt="">
                </li>
            </ul>
        </td>`;

        const counselType = afterClass?.counselType || '';
        const counselContent = afterClass?.counselContent || '';

        tr.innerHTML += `
        <td class="cell-middle">
            <div class="counsel-box">
                <div class="counsel-type">
                    <button class="${counselType === '전화' ? 'active' : ''}">전화</button>
                    <button class="${counselType === '문자' ? 'active' : ''}">문자</button>
                    <button class="${counselType === '대면' ? 'active' : ''}">대면</button>
                </div>
                <textarea placeholder="내용을 입력해주세요.">${counselContent}</textarea>
            </div>
        </td>`;

        const originalContent = afterClass?.content ? afterClass.content.replace(/<br\s*\/?>/gi, "\n") : "";
        tr.innerHTML += `
        <td>
            <div class="cell-middle">
                <div class="after-comment">
                    <textarea class="comment-text record-content"
                              placeholder="내용을 입력해주세요."
                              data-original-content="${originalContent.replace(/"/g, '&quot;')}">${originalContent}</textarea>
                </div>
            </div>
            <input type="hidden" class="record-word" value="${afterClass?.word ?? ''}">
        </td>`;

        const originalReview = afterClass?.review ? afterClass.review.replace(/<br\s*\/?>/gi, "\n") : "";
        tr.innerHTML += `
        <td>
            <div class="cell-middle">
                <div class="after-comment">
                    <textarea class="comment-text record-review"
                              placeholder="선생님의 코멘트를 입력해주세요.">${originalReview}</textarea>
                </div>
            </div>
        </td>`;

        const beforeSendSrc = s.isBeforeSend == '1' ? '/image/send2.png' : '/image/send1.png';
        const afterSendSrc  = s.isAfterSend  == '1' ? '/image/send3.png' : '/image/send1.png';
        const isInfant = String(state.classKey).length === 1;

        if (s.appToken == null || s.appToken == '') {
            tr.innerHTML += `
            <td class="send-ornot">
                <img src="/image/no-smartphones.png" alt="">
            </td>`;
        } else {
            tr.innerHTML += `
            <td class="send-ornot">
                <span>
                    <img src="${beforeSendSrc}" alt="">
                    ${isInfant ? '' : `<img src="${afterSendSrc}" alt="">`}
                </span>
            </td>`;
        }

        frag.appendChild(tr);
    });

    if (items.length === 0) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 10;
        td.style.textAlign = 'center';
        td.style.padding = '20px';
        td.textContent = '등록된 학생이 없습니다.';
        tr.appendChild(td);
        tbody.replaceChildren(tr);
    } else {
        tbody.replaceChildren(frag);
    }

    updateSelectAllCheckbox();
}

async function updateAttendance(button) {
    const studentId = button.getAttribute('data-student-id');
    const row = button.closest('tr');

    const statusDropdown = row.querySelector('.status-badge');
    const startTimeInput = row.querySelector('.start-time');
    const endTimeInput   = row.querySelector('.end-time');

    const newStatus    = statusDropdown.value;
    const newStartTime = startTimeInput.value.trim();
    const newEndTime   = endTimeInput.value.trim();

    const timePattern = /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/;

    if (newStartTime && !timePattern.test(newStartTime)) {
        alert('시작 시간 형식이 올바르지 않습니다. (예: 09:00)');
        startTimeInput.focus();
        return;
    }

    if (newEndTime && !timePattern.test(newEndTime)) {
        alert('종료 시간 형식이 올바르지 않습니다. (예: 18:00)');
        endTimeInput.focus();
        return;
    }

    const originalStatus    = statusDropdown.getAttribute('data-original');
    const originalStartTime = startTimeInput.getAttribute('data-original') || '';
    const originalEndTime   = endTimeInput.getAttribute('data-original') || '';

    if (newStatus === originalStatus &&
        newStartTime === originalStartTime &&
        newEndTime === originalEndTime) {
        alert('변경된 내용이 없습니다.');
        return;
    }

    button.disabled = true;
    button.textContent = '처리중...';

    const [yy, mm] = state.date.split('-');
    const data = {
        attendanceKey: newStatus,
        studentId: studentId,
        timeTableKey: state.timeTableKey,
        week: state.week,
        yy: yy,
        mm: mm,
        centerCode: row.dataset.centerCode,
        attendanceName: newStatus,
        inTime: newStartTime || null,
        outTime: newEndTime || null,
        absenceDate: state.date
    };

    try {
        const res = await fetch('/student/update/attendance', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        });

        if (!res.ok) throw new Error('서버 오류');

        alert('출석 정보가 변경되었습니다.');

        statusDropdown.setAttribute('data-original', newStatus);
        startTimeInput.setAttribute('data-original', newStartTime);
        endTimeInput.setAttribute('data-original', newEndTime);

        statusDropdown.className = 'status-badge stbox';
        if (newStatus === '출석 완료')     statusDropdown.classList.add('attend');
        else if (newStatus === '지각')     statusDropdown.classList.add('late');
        else if (newStatus === '수업 전')  statusDropdown.classList.add('before');
        else                               statusDropdown.classList.add('absent');

    } catch (error) {
        console.error('출석 정보 변경 실패:', error);
        alert('출석 정보 변경에 실패했습니다: ' + error.message);
    } finally {
        button.disabled = false;
        button.textContent = '변경';
    }
}

document.addEventListener('click', function (e) {
    if (e.target.closest('.btn-update-attendance')) {
        updateAttendance(e.target.closest('.btn-update-attendance'));
    }
});

document.addEventListener('blur', function (e) {
    if (!e.target.matches('.start-time') && !e.target.matches('.end-time')) return;

    const value = e.target.value.trim();
    if (!value) return;

    if (/^\d{3,4}$/.test(value)) {
        const digits = value.padStart(4, '0');
        const hours   = digits.substring(0, 2);
        const minutes = digits.substring(2, 4);
        if (parseInt(hours) <= 23 && parseInt(minutes) <= 59) {
            e.target.value = `${hours}:${minutes}`;
            return;
        }
    }

    const timePattern = /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/;
    if (!timePattern.test(value)) {
        alert('올바른 시간 형식이 아닙니다. (예: 09:00)');
        e.target.value = e.target.getAttribute('data-original') || '';
        return;
    }

    const parts = value.split(':');
    if (parts.length === 2) {
        e.target.value = `${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}`;
    }
}, true);

document.addEventListener('keydown', function (e) {
    if (e.key === 'Enter' && (e.target.matches('.start-time') || e.target.matches('.end-time'))) {
        const row = e.target.closest('tr');
        const updateBtn = row?.querySelector('.btn-update-attendance');
        if (updateBtn) updateBtn.click();
    }
});

document.addEventListener('change', function (e) {
    if (!e.target.matches('.status-badge')) return;

    const select = e.target;
    const value  = select.value;

    select.classList.remove('attend', 'late', 'before', 'absent');
    if (value === '출석 완료')    select.classList.add('attend');
    else if (value === '지각')    select.classList.add('late');
    else if (value === '수업 전') select.classList.add('before');
    else                          select.classList.add('absent');
});

document.addEventListener('click', function (e) {
    if (!e.target.closest('.class-guide')) return;

    const activeClass = document.querySelector('#record-class-list .class-btn.active');
    if (!activeClass) return;

    const selectedNames = Array.from(document.querySelectorAll('#record_tbody tr'))
        .filter(tr => tr.querySelector('input[type="checkbox"]:checked'))
        .map(tr => tr.querySelector('.studentName')?.textContent.trim() || '');

    fetch('/class/api/record/before-class', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            unitKey: activeClass.dataset.unitKey,
            classKey: activeClass.dataset.classKey,
            timeTableKey: activeClass.dataset.timeTableKey,
            week: state.week
        })
    })
        .then(res => res.json())
        .then(json => {
            bindRecordModal(json?.response ?? json, selectedNames);
            document.querySelector('.class-guide-modal').style.display = 'block';
        })
        .catch(err => {
            console.error('[GUIDE MODAL]', err);
            bindRecordModal({}, selectedNames);
            document.querySelector('.class-guide-modal').style.display = 'block';
        });
});

function bindRecordModal(data, selectedNames) {
    const dateEl     = document.getElementById('record-date');
    const teacherEl  = document.getElementById('record-teacher');
    const studentsEl = document.getElementById('record-students');
    const contentEl  = document.getElementById('record-content');

    if (dateEl)     dateEl.textContent     = data.timeTableLabel || '-';
    if (teacherEl)  teacherEl.textContent  = data.userName ? `${data.userName} 선생님` : '-';
    if (studentsEl) studentsEl.textContent = selectedNames.length ? selectedNames.join(', ') : '선택된 학생이 없습니다';
    if (contentEl)  contentEl.textContent  = data.content || '-';
}

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("textarea.record-content").forEach(el => {
        if (el.value) el.value = el.value.replace(/\\n/g, "\n");
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
    if (!e.target.closest(".remarks img")) return;
    document.querySelector(".remarks-modal").style.display = "block";
});

document.addEventListener("DOMContentLoaded", () => {
    document.querySelector(".class-all-guide")?.addEventListener("click", async () => {
        const date = document.getElementById("record_calendar")?.value;
        if (!date) {
            alert("날짜를 먼저 선택해주세요.");
            return;
        }

        const res = await fetch("/class/api/before/all/count", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({date, userCode: state.teacherId})
        });

        const data = await res.json();
        const count = data.response;
        if (count === 0) {
            alert("발송 대상 학생이 없습니다.");
            return;
        }

        if (!confirm(`${date}\n${count.studentCount}명에게 수업 전 안내를 일괄 발송하시겠습니까?(총 ${count.classCount}건)`)) return;

        try {
            const sendRes = await fetch("/class/api/before/all", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({date, userCode: state.teacherId})
            });

            if (!sendRes.ok) throw new Error("서버 오류: " + sendRes.status);

            const sendData = await sendRes.json();
            const {sentCount, skippedCount} = sendData.response;

            alert(`발송 완료\n- 발송: ${sentCount}명\n- 앱 미설치 제외: ${skippedCount}명`);
            await loadStudentList();
        } catch (e) {
            console.error(e);
            alert("발송 실패: " + e.message);
        }
    });
});

document.addEventListener("DOMContentLoaded", () => {
    const sendbtn = document.getElementById("send-before-record");
    if (!sendbtn) return;

    sendbtn.addEventListener("click", async () => {
        const checkedRows = Array.from(document.querySelectorAll("#record_tbody tr"))
            .filter(row => row.querySelector("input[type=checkbox]")?.checked);

        if (checkedRows.length === 0) {
            alert("학생을 선택해주세요.");
            return;
        }
        if (!confirm('수업 전 안내를 발송하시겠습니까?')) return;

        const tokens = checkedRows
            .map(row => row.getAttribute("data-app-token") || '')
            .filter(token => token !== '');

        try {
            const res = await fetch("/api/push/before", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({tokens, title: "수업 안내", body: "수업 전 안내가 등록되었습니다."})
            });
            if (!res.ok) throw new Error("서버 오류: " + res.status);

            await insertStudentAttendance();
            await insertBeforeClassNotice(checkedRows);

            alert("수업 전 안내가 발송되었습니다.");
            document.querySelector('.class-guide-modal').style.display = 'none';
            await loadStudentList();

        } catch (error) {
            console.error("실패:", error);
            alert("발송을 실패했습니다. " + error.message);
        }
    });
});

async function insertBeforeClassNotice(checkedRows) {
    const modalContent = document.querySelector("#record-content")?.textContent.trim() || "";

    const notices = Array.from(checkedRows).map(row => ({
        studentId: row.getAttribute("data-student-id"),
        timeTableKey: document.querySelector(".class-btn.active")?.getAttribute("data-time-table-key"),
        week: state.week,
        classDate: document.getElementById("record_calendar")?.value || "",
        content: modalContent,
    }));

    try {
        const response = await fetch("/class/api/before-notice/insert", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(notices)
        });
        if (!response.ok) throw new Error("서버 오류: " + response.status);
    } catch (error) {
        console.error("발송 로그 저장 실패:", error);
    }
}

async function insertStudentAttendance() {
    const selectedStudents = Array.from(document.querySelectorAll("#record_tbody tr"))
        .filter(row => row.querySelector("input[type=checkbox]")?.checked)
        .map(row => ({
            studentId: row.dataset.studentId,
            timeTableKey: state.timeTableKey,
            centerCode: row.dataset.centerCode,
            week: state.week,
            attendanceDate: new Date().toISOString().slice(0, 10)
        }));

    if (selectedStudents.length === 0) return;

    try {
        const res = await fetch("/class/api/attendance/insert", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(selectedStudents)
        });
        if (!res.ok) throw new Error("출결 저장 실패 (status " + res.status + ")");
    } catch (err) {
        console.error("출결 insert 에러:", err);
    }
}

function toggleCommentBtn() {
    const isInfant  = String(state.classKey).length === 1;
    const commentBtn = document.querySelector('.class-comment');
    if (commentBtn) commentBtn.style.display = isInfant ? 'none' : '';
}

document.addEventListener("DOMContentLoaded", () => {
    const sendBtn = document.querySelector(".class-comment");
    if (!sendBtn) return;

    sendBtn.addEventListener("click", () => {
        const checkedRows = Array.from(document.querySelectorAll("#record_tbody tr"))
            .filter(row => row.querySelector("input[type=checkbox]")?.checked);

        if (!confirm(`${checkedRows.length}명의 학생에게 알림을 발송하시겠습니까?`)) return;

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

        fetch("/api/push/after", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({tokens, title: "수업 후 코멘트", body: "오늘 수업 후 코멘트가 등록되었습니다."})
        })
            .then(response => {
                if (!response.ok) throw new Error("서버 오류: " + response.status);
                return response.json();
            })
            .then(async data => {
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

document.addEventListener("DOMContentLoaded", () => {
    const saveBtn = document.querySelector(".class-save");
    if (!saveBtn) return;

    saveBtn.addEventListener("click", async () => {
        const checkedRows = Array.from(document.querySelectorAll("#record_tbody tr"))
            .filter(row => row.querySelector("input[type=checkbox]")?.checked);

        if (checkedRows.length === 0) {
            alert("학생을 선택해주세요.");
            return;
        }

        try {
            await saveAfterClassNotice(checkedRows);
            alert("저장이 완료되었습니다.");
        } catch (error) {
            console.error("저장 실패:", error);
            alert("저장 실패: " + error.message);
        }
    });
});

async function saveAfterClassNotice(checkedRows) {
    const [yy, mm, dd] = state.date.split("-");

    const notices = checkedRows.map(row => {
        const contentTextarea = row.querySelector(".record-content");
        const reviewTextarea  = row.querySelectorAll(".comment-text")[1];
        const counselTextarea = row.querySelector(".counsel-box textarea");
        const counselType     = row.querySelector(".counsel-type button.active")?.textContent || "전화";
        const contentWord     = row.querySelector(".record-word");

        return {
            studentId:      row.getAttribute("data-student-id"),
            timeTableKey:   document.querySelector(".class-btn.active")?.getAttribute("data-time-table-key"),
            afterClassKey:  row.getAttribute("data-after-class-key"),
            year: yy,
            month: mm,
            day: dd,
            week:           state.week,
            content:        contentTextarea?.value,
            word:           contentWord?.value || "",
            review:         reviewTextarea?.value || "",
            counselType:    counselType,
            counselContent: counselTextarea?.value || ""
        };
    });

    const response = await fetch("/class/api/after-notice/insert", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(notices)
    });

    if (!response.ok) throw new Error("서버 오류: " + response.status);
    return await response.json();
}

async function insertAfterClassNotice(checkedRows) {
    const notices = checkedRows.map(row => {
        const contentTextarea = row.querySelector(".record-content");
        const reviewTextarea  = row.querySelectorAll(".comment-text")[1];
        const counselTextarea = row.querySelector(".counsel-box textarea");
        const counselType     = row.querySelector(".counsel-type button.active")?.textContent || "";

        return {
            studentId:      row.getAttribute("data-student-id"),
            timeTableKey:   document.querySelector(".class-btn.active")?.getAttribute("data-time-table-key"),
            afterClassKey:  row.getAttribute("data-after-class-key"),
            week:           state.week,
            content:        contentTextarea?.value,
            word:           row.querySelector(".record-word")?.value || "",
            review:         reviewTextarea?.value || "",
            counselType:    counselType,
            counselContent: counselTextarea?.value || ""
        };
    });

    try {
        const response = await fetch("/class/api/after-notice/insert", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(notices)
        });
        if (!response.ok) throw new Error("서버 오류: " + response.status);
    } catch (error) {
        console.error("발송 로그 저장 실패:", error);
    }
}

async function updateAfterSend() {
    const selectedStudents = Array.from(document.querySelectorAll("#record_tbody tr"))
        .filter(row => row.querySelector("input[type=checkbox]")?.checked)
        .map(row => ({
            studentId:    row.dataset.studentId,
            timeTableKey: state.timeTableKey,
            centerCode:   row.dataset.centerCode,
            week:         state.week,
        }));

    if (selectedStudents.length === 0) return;

    try {
        const res = await fetch("/class/api/afterSend/update", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(selectedStudents)
        });
        if (!res.ok) throw new Error("발송내역 저장 실패 (status " + res.status + ")");
    } catch (err) {
        console.error("발송 내역 업데이트 에러:", err);
    }
}

let currentRemarksStudent = {studentId: null, studentName: null};

document.addEventListener("click", async function (e) {
    const target = e.target.closest(".remarks img");
    if (!target) return;

    const row = target.closest("tr");
    if (!row) return;

    currentRemarksStudent.studentId   = row.dataset.studentId;
    currentRemarksStudent.studentName = row.querySelector(".studentName")?.textContent.trim() || "";

    const sub = document.querySelector(".remarks-modal .remark-sub");
    if (sub) sub.textContent = `${currentRemarksStudent.studentName} 학생의 해당 수업에 대한 특이사항이나 문제점을 체크해 주세요.`;

    await loadRemarksItems(currentRemarksStudent.studentId);
    document.querySelector(".remarks-modal").style.display = "block";
});

async function loadRemarksItems(studentId) {
    const tbody = document.querySelector(".remarks-table tbody");
    if (!tbody) return;

    const [yy, mm] = state.date.split("-");

    try {
        const res = await fetch("/class/api/remarks/list", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                studentId:    studentId,
                timeTableKey: state.timeTableKey,
                yy, mm,
                week: state.week
            })
        });

        if (!res.ok) throw new Error("서버 오류: " + res.status);

        const data = await res.json();
        const categories = Array.isArray(data?.response) ? data.response : [];
        renderRemarksModal(tbody, categories);

    } catch (err) {
        console.error("[loadRemarksItems] 실패:", err);
        tbody.innerHTML = `<tr><td colspan="2" style="text-align:center;">항목을 불러오지 못했습니다.</td></tr>`;
    }
}

function renderRemarksModal(tbody, categories) {
    tbody.innerHTML = "";

    const classTypeMap = {"1": "han", "2": "book"};
    const currentType  = classTypeMap[state.classType];

    const filtered = categories
        .map(category => ({
            ...category,
            items: (category.items || []).filter(item =>
                item.remarkSubject === "all" || item.remarkSubject === currentType
            )
        }))
        .filter(category => category.items.length > 0);

    if (filtered.length === 0) {
        tbody.innerHTML = `<tr><td colspan="2" style="text-align:center;">등록된 항목이 없습니다.</td></tr>`;
        return;
    }

    filtered.forEach(category => {
        const tr         = document.createElement("tr");
        const tdCategory = document.createElement("td");
        tdCategory.textContent = category.categoryName;

        const tdItems      = document.createElement("td");
        const checkboxGroup = document.createElement("div");
        checkboxGroup.className = "checkbox-group";

        (category.items || []).forEach(item => {
            const div      = document.createElement("div");
            const checkbox = document.createElement("input");
            checkbox.type  = "checkbox";
            checkbox.dataset.remarksKey = item.remarksKey;
            checkbox.checked = item.checked || false;

            div.appendChild(checkbox);
            div.appendChild(document.createTextNode(" " + item.itemName));
            checkboxGroup.appendChild(div);
        });

        tdItems.appendChild(checkboxGroup);
        tr.appendChild(tdCategory);
        tr.appendChild(tdItems);
        tbody.appendChild(tr);
    });
}

document.addEventListener("click", async function (e) {
    const saveBtn = e.target.closest(".remarks-modal .save-btn");
    if (!saveBtn) return;

    const checkedItems = Array.from(document.querySelectorAll(".remarks-table input[type=checkbox]:checked"))
        .map(cb => cb.dataset.remarksKey)
        .filter(Boolean);

    const [yy, mm] = state.date.split("-");

    try {
        const res = await fetch("/class/api/remarks/save", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                studentId:    currentRemarksStudent.studentId,
                timeTableKey: state.timeTableKey,
                yy, mm,
                week:        state.week,
                remarksKeys: checkedItems
            })
        });

        if (!res.ok) throw new Error("서버 오류: " + res.status);

        alert("저장되었습니다.");
        document.querySelector(".remarks-modal").style.display = "none";
        await loadStudentList();
    } catch (err) {
        console.error("[remarks save] 실패:", err);
        alert("저장에 실패했습니다.");
    }
});