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

// 전체 선택 체크박스 이벤트 핸들러
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

// 개별 체크박스 상태에 따라 전체 선택 체크박스 업데이트
function updateSelectAllCheckbox() {
    const selectAllCheckbox = document.getElementById('select-all-students');
    if (!selectAllCheckbox) return;

    const checkboxes = document.querySelectorAll('#record_tbody tr input[type="checkbox"]');
    const checkedCount = Array.from(checkboxes).filter(cb => cb.checked).length;

    selectAllCheckbox.checked = checkboxes.length > 0 && checkedCount === checkboxes.length;
    selectAllCheckbox.indeterminate = checkedCount > 0 && checkedCount < checkboxes.length;
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

    // 전체 선택 체크박스 설정
    setupSelectAllCheckbox();
    loadInitialData();
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

    // 개별 체크박스 변경 시 전체 선택 체크박스 업데이트
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
        state.classKey = String(firstClassKey);
        state.unitKey = String(firstUnitKey);

        // 학생 목록 조회 (한 번만 호출)
        await loadStudentList(firstTimeTableKey);

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

        // date 전달
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

        // 서버에서 계산한 week를 state에 저장
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
        // 해당 학생의 afterClass 데이터 가져오기 (인덱스 매칭)
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

// 출석 - 드롭다운과 변경 버튼 추가
        tr.innerHTML += `
    <td>
        <div>
            <!-- 출석 상태 드롭다운 -->
            <select class="status-badge ${statusClass} stbox" 
                    data-student-id="${s.studentId}" 
                     data-attendance-key="${statusClass ?? ''}"
                    data-original="${attendance}"
                    style="width: 100%; text-align: center; cursor: pointer;">
                <option value="present" ${attendance === '출석 완료' ? 'selected' : ''}>출석 완료</option>
                <option value="late" ${attendance === '지각' ? 'selected' : ''}>지각</option>
                <option value="absent" ${attendance === '결석' ? 'selected' : ''}>결석</option>
                <option value="before" ${attendance === '수업 전' ? 'selected' : ''}>수업 전</option>
            </select>

            <div class="time-boxes">
                <div class="time-start">
                    등원
                    <input type="text" 
                           class="time_input start-time"
                           value="${s.inTime ?? ''}"
                           data-original="${s.inTime ?? ''}"
                           placeholder="00:00"
                           inputmode="numeric" 
                           maxlength="5"/>
                </div>
                <div class="time-end">
                    하원
                    <input type="text" 
                           class="time_input end-time"
                           value="${s.outTime ?? ''}"
                           data-original="${s.outTime ?? ''}"
                           placeholder="00:00"
                           inputmode="numeric" 
                           maxlength="5"/>
                </div>
            </div>
            
            <!-- 변경 버튼 -->
            <button type="button" 
                    class="btn-update-attendance stbox"
                    data-student-id="${s.studentId}"
                    data-attendance-key="${s.attendanceKey ?? ''}"
                    style="width: 100%; margin-top: 8px; padding: 6px; background: #c8c8c8; color: white; border: none; border-radius: 4px; cursor: pointer;">
                변경
            </button>
        </div>
    </td>`;

        // 보강일자
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
        const counselType = afterClass?.counselType || '전화';
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
  </td>
`;

        // 수업 후 코멘트 (각 학생별 afterClass 데이터 사용)
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
                <input type="hidden" class="record-word" value="${afterClass?.homework ?? ''}">
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

        // 발송여부
        const beforeSendSrc = s.isBeforeSend == '1' ? '/image/send2.png' : '/image/send1.png';
        const afterSendSrc = s.isAfterSend == '1' ? '/image/send3.png' : '/image/send1.png';
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
        <img src="${afterSendSrc}" alt="">
      </span>
    </td>`;
        }
        frag.appendChild(tr);
    });

    // 학생이 없을 때
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

    // 렌더링 후 전체 선택 체크박스 상태 업데이트
    updateSelectAllCheckbox();
}

// 출석 변경
async function updateAttendance(button) {
    const studentId = button.getAttribute('data-student-id');
    const attendanceKey = button.getAttribute('data-attendance-key');
    const row = button.closest('tr');

    const statusDropdown = row.querySelector('.status-badge'); // select 요소
    const startTimeInput = row.querySelector('.start-time');
    const endTimeInput = row.querySelector('.end-time');

    const newStatus = statusDropdown.value;
    const newStartTime = startTimeInput.value.trim();
    const newEndTime = endTimeInput.value.trim();


    // 시간 형식 검증
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

    // 변경 사항 확인
    const originalStatus = statusDropdown.getAttribute('data-original');
    const originalStartTime = startTimeInput.getAttribute('data-original') || '';
    const originalEndTime = endTimeInput.getAttribute('data-original') || '';

    if (newStatus === originalStatus &&
        newStartTime === originalStartTime &&
        newEndTime === originalEndTime) {
        alert('변경된 내용이 없습니다.');
        return;
    }

    button.disabled = true;
    button.textContent = '처리중...';

    const data = {
        attendanceKey: newStatus,
        studentId: studentId,
        timeTableKey: state.timeTableKey,
        week: state.week,
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

        const result = await res.json();
        console.log('서버 응답:', result);

        alert('출석 정보가 변경되었습니다.');

        // 원본 값 업데이트
        statusDropdown.setAttribute('data-original', newStatus);
        startTimeInput.setAttribute('data-original', newStartTime);
        endTimeInput.setAttribute('data-original', newEndTime);

        // 드롭다운 클래스 업데이트 (색상 변경)
        statusDropdown.className = 'status-badge stbox';
        if (newStatus === '출석 완료') {
            statusDropdown.classList.add('attend');
        } else if (newStatus === '지각') {
            statusDropdown.classList.add('late');
        } else if (newStatus === '수업 전') {
            statusDropdown.classList.add('before');
        } else {
            statusDropdown.classList.add('absent');
        }

    } catch (error) {
        console.error('출석 정보 변경 실패:', error);
        alert('출석 정보 변경에 실패했습니다: ' + error.message);
    } finally {
        button.disabled = false;
        button.textContent = '변경';
    }
}

// 변경 버튼 이벤트 위임
document.addEventListener('click', function (e) {
    if (e.target.closest('.btn-update-attendance')) {
        const button = e.target.closest('.btn-update-attendance');
        updateAttendance(button);
    }
});

// 시간 입력 필드 포커스 아웃 시 검증 및 자동 포맷팅
document.addEventListener('blur', function (e) {
    if (e.target.matches('.start-time') || e.target.matches('.end-time')) {
        const value = e.target.value.trim();

        // 빈 값은 허용
        if (!value) return;

        // 숫자만 입력된 경우 자동 포맷팅
        if (/^\d{3,4}$/.test(value)) {
            const digits = value.padStart(4, '0');
            const hours = digits.substring(0, 2);
            const minutes = digits.substring(2, 4);

            if (parseInt(hours) <= 23 && parseInt(minutes) <= 59) {
                e.target.value = `${hours}:${minutes}`;
                return;
            }
        }

        // HH:MM 형식 검증
        const timePattern = /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/;

        if (!timePattern.test(value)) {
            alert('올바른 시간 형식이 아닙니다. (예: 09:00)');
            e.target.value = e.target.getAttribute('data-original') || '';
            return;
        }

        // 자동으로 2자리 포맷팅
        const parts = value.split(':');
        if (parts.length === 2) {
            const hours = parts[0].padStart(2, '0');
            const minutes = parts[1].padStart(2, '0');
            e.target.value = `${hours}:${minutes}`;
        }
    }
}, true);

// Enter 키로 변경 버튼 클릭
document.addEventListener('keydown', function (e) {
    if (e.key === 'Enter' && (e.target.matches('.start-time') || e.target.matches('.end-time'))) {
        const row = e.target.closest('tr');
        const updateBtn = row.querySelector('.btn-update-attendance');
        if (updateBtn) {
            updateBtn.click();
        }
    }
});

// 출석 상태 드롭다운 변경 시 클래스 업데이트
document.addEventListener('change', function (e) {
    if (e.target.matches('.status-badge')) {
        const select = e.target;
        const value = select.value;

        // 기존 상태 클래스 제거
        select.classList.remove('attend', 'late', 'before', 'absent');

        // 새 상태 클래스 추가
        if (value === '출석 완료') {
            select.classList.add('attend');
        } else if (value === '지각') {
            select.classList.add('late');
        } else if (value === '수업 전') {
            select.classList.add('before');
        } else {
            select.classList.add('absent');
        }
    }
});

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

        const unitKey = activeClass.dataset.unitKey;
        const classKey = activeClass.dataset.classKey;
        const timeTableKey = activeClass.dataset.timeTableKey;
        const week = activeWeek?.dataset.week;
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

    sendbtn.addEventListener("click", async () => {

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
            .map(row => row.getAttribute("data-app-token") || '')
            .filter(token => token !== '');
        console.log(tokens);
        const requestBody = {
            tokens: tokens,
            title: "수업 안내",
            body: "수업 전 안내가 등록되었습니다."
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
                await insertStudentAttendance();
            })
            .catch(error => {
                console.error("실패:", error);
                alert("발송을 실패했습니다. " + error.message);
            });

        await insertBeforeClassNotice(checkedRows);
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

        alert("저장되었습니다.");
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
            attendanceDate: new Date().toISOString().slice(0, 10)
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
        if (!confirm(`${checkedRows.length}명의 학생에게 알림을 발송하시겠습니까?`)) {
            return;
        }


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

        const requestBody = {
            tokens: tokens,
            title: "수업 후 코멘트",
            body: "오늘 수업 후 코멘트가 등록되었습니다."
        };


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

// 저장만 수행
document.addEventListener("DOMContentLoaded", () => {
    const saveBtn = document.querySelector(".class-save");

    if (!saveBtn) return;

    saveBtn.addEventListener("click", async () => {
        const checkedRows = Array.from(document.querySelectorAll("#record_tbody tr"))
            .filter(row => {
                const checkbox = row.querySelector("input[type=checkbox]");
                return checkbox && checkbox.checked;
            });

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

// 수업 후 코멘트 저장만 수행 (푸시 없이)
async function saveAfterClassNotice(checkedRows) {
    const notices = checkedRows.map(row => {
        const contentTextarea = row.querySelector(".record-content");
        const reviewTextarea = row.querySelectorAll(".comment-text")[1]; // 두 번째 textarea가 리뷰
        const counselTextarea = row.querySelector(".counsel-box textarea");
        const counselType = row.querySelector(".counsel-type button.active")?.textContent || "전화";

        const originalContent = contentTextarea.getAttribute("data-original-content") || "";

        return {
            studentId: row.getAttribute("data-student-id"),
            timeTableKey: document.querySelector(".class-btn.active")?.getAttribute("data-time-table-key"),
            afterClassKey: row.getAttribute("data-after-class-key"),
            week: document.querySelector(".week-btn.active")?.getAttribute("data-week"),
            content: contentTextarea?.value,
            word: row.querySelector(".record-word")?.value || "",
            review: reviewTextarea?.value || "",
            counselType: counselType,
            counselContent: counselTextarea?.value || ""
        }
    });

    const response = await fetch("/class/api/after-notice/insert", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(notices)
    });

    if (!response.ok) {
        throw new Error("서버 오류: " + response.status);
    }

    console.log("저장 성공");
    return await response.json();
}

// 수업 후 문자 전송 후 로그 저장
async function insertAfterClassNotice(checkedRows) {
    const notices = checkedRows.map(row => {
        const contentTextarea = row.querySelector(".record-content");
        const reviewTextarea = row.querySelectorAll(".comment-text")[1]; // 두 번째 textarea가 리뷰
        const counselTextarea = row.querySelector(".counsel-box textarea");
        const counselType = row.querySelector(".counsel-type button.active")?.textContent || "전화";

        const originalContent = contentTextarea.getAttribute("data-original-content") || "";

        return {
            studentId: row.getAttribute("data-student-id"),
            timeTableKey: document.querySelector(".class-btn.active")?.getAttribute("data-time-table-key"),
            afterClassKey: row.getAttribute("data-after-class-key"),
            week: document.querySelector(".week-btn.active")?.getAttribute("data-week"),
            content: contentTextarea.value,
            word: row.querySelector(".record-word")?.value || "",
            review: reviewTextarea?.value || "",
            counselType: counselType,
            counselContent: counselTextarea?.value || ""
        }
    });

    try {
        const response = await fetch("/class/api/after-notice/insert", {
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