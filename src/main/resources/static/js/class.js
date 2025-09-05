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
        const userNo = 2;
        const dayIndexMap = {mon: 1, tue: 2, wed: 3, thu: 4, fri: 5, sat: 6};
        for (const tab of allTabs) {
            const dayname = tab.id;
            const daynameNo = dayIndexMap[dayname];

            const rows = tab.querySelectorAll('tr.time-row');
            for (const row of rows) {
                const periodNo = row.querySelector('td:nth-child(2)').innerText.trim();
                const startTime = row.querySelector('.time-start input').value;
                const endTime = row.querySelector('.time-end input').value;
                const classNo = row.querySelector('select[name="classNo"]').value;
                const unitNo = row.querySelector('select[name="unitNo"]').value;
                const gradeNo = row.querySelector('select[name="gradeNo"]').value;

                if (!startTime || !endTime || !classNo || !unitNo || !gradeNo) {
                    continue;
                }

                payloadList.push({
                    yy, mm, dayname, daynameNo, periodNo,
                    startTime, endTime, classNo, unitNo, gradeNo, userNo
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

        const timeTableNo = selRow.dataset.timetableNo;
        const timeTableCode = selRow.dataset.timeTableCode;
        console.log('timeTableCode = ' + timeTableCode);
        if (!timeTableNo) {
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
            const studentNo = tr.querySelector('input[type="checkbox"]').value;
            const weekNo = tr.querySelector('input[name^="weeks-"]:checked').value;
            return {timeTableNo, studentNo, weekNo, timeTableCode};
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
            const assignNo = btn.dataset.assignNo;
            if (!assignNo) return;
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

                const params = new URLSearchParams();
                params.append('timeTableAssignNo', assignNo);

                fetch('/class/delete_student', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                    },
                    body: params.toString()
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
// 수업 선택 시 데이터 변경
function loadClassData(element) {
    console.log('수업 선택');
    const classCode = element.dataset.classCode || element.getAttribute('data-class-code');

    const dateInput = document.getElementById("record_calendar");
    const selectedDate = dateInput?.value || "";

    const requestBody = {
        classCode: classCode,
        date: selectedDate
    };

    fetch(`/class/api/record/by-class`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(requestBody)
    })
        .then(res => {
            if (!res.ok) throw new Error("서버 응답 오류");
            return res.json();
        })
        .then(data => {
            const list = Array.isArray(data?.response) ? data.response : [];
            renderStudentsFromResponse(list, '#record_tbody');
        })
        .catch(err => console.error('[loadClassData] 실패:', err));
}

// 주차 선택 시 데이터 변경

// 선생님 선택 시 데이터 변경

// 화면 다시 그리기
function renderStudentsFromResponse(list, tbodySel = '#record_tbody') {
    const tbody = typeof tbodySel === 'string' ? document.querySelector(tbodySel) : tbodySel;
    if (!tbody) {
        console.error('[renderStudents] tbody를 찾을 수 없습니다:', tbodySel);
        return;
    }

    const items = Array.isArray(list) ? list : [];
    const frag = document.createDocumentFragment();

    items.forEach((s, idx) => {
        const tr = document.createElement('tr');
        tr.dataset.studentId = s.studentNo ?? '';

        // 1) 체크박스
        tr.innerHTML += `<td class="checkbox-group"><input type="checkbox" /></td>`;
        // 2) 번호
        tr.innerHTML += `<td>${idx + 1}</td>`;
        // 3) 이름
        tr.innerHTML += `<td class="studentName">${s.studentName ?? ''}</td>`;

        // 4) 출결
        const attendance = s.attendance ?? '결석';
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

    // 학생이 없을 때
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

    bindTimepickers(tbody);
}


function bindTimepickers(scope = document) {
    scope.querySelectorAll('.timepicker').forEach(input => {

        if (input.dataset.bound === '1') return;
        input.dataset.bound = '1';

        input.addEventListener('input', () => {
            const wrap = input.closest('.time');
            const display = wrap?.querySelector('.display-time');
            if (display) display.textContent = input.value || '--:--';
        });
    });
}

// 날짜 선택 시 데이터 변경
document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('record_calendar');
    const label = document.getElementById('record_current');
    const btn = document.querySelector('.month-title .calendar-open');
    if (!input || !label || !btn) return;

    const DAY = ['일', '월', '화', '수', '목', '금', '토'];
    const ENDAY = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];
    const toK = d => `${d.getFullYear()}년 ${d.getMonth() + 1}월 ${d.getDate()}일 (${DAY[d.getDay()]})`;
    const toYmd = d => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

    const init = label.textContent.match(/(\d{4})년\s*(\d{1,2})월\s*(\d{1,2})일/);
    const initDate = init ? new Date(+init[1], +init[2] - 1, +init[3]) : new Date();
    if (!input.value) input.value = toYmd(initDate);
    if (!init) label.textContent = toK(initDate);

    const sync = () => {
        if (!input.value) return;
        const d = new Date(input.value + 'T00:00:00');
        if (isNaN(d)) return;

        label.textContent = toK(d);

        const yy = String(d.getFullYear());
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dayName = ENDAY[d.getDay()];
        const selectedDate = toYmd(d);

        const requestBody = {
            yy: yy,
            mm: mm,
            day: dayName,
            date: selectedDate
        };

        fetch(`/class/api/record/by-date`, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(requestBody)
        })
            .then(async (res) => {
                if (res.status === 401) {
                    await res.json().catch(() => ({}));
                    alert('세션이 만료되었습니다.', location.href = '/login');
                    throw new Error('UNAUTHORIZED');
                }
                if (!res.ok) throw new Error("서버 응답 오류");
                return res.json();
            })
            .then(data => {
                const list = Array.isArray(data?.response) ? data.response : [];
                renderRecordClassList(list);
                if (list.length > 0) {
                    const body = {
                        classCode: list[0].classCode,
                        date: selectedDate
                    }
                    fetch(`/class/api/record/by-class`, {
                        method: "POST",
                        headers: {"Content-Type": "application/json"},
                        body: JSON.stringify(body)
                    })
                        .then(r => {
                            if (!r.ok) throw new Error("서버 응답 오류 (by-class)");
                            return r.json();
                        })
                        .then(data2 => {
                            console.log('by-class 결과:', data2);
                            renderStudentsFromResponse(data2.response);
                        })
                        .catch(err => console.error('[by-class 실패]', err));

                }
            })
            .catch(err => console.error('[sync] 실패:', err));
    };

    btn.addEventListener('click', () => {
        if (typeof input.showPicker === 'function') {
            try {
                input.showPicker();
            } catch {
                input.focus();
                input.click();
            }
        } else {
            input.focus();
            input.click();
        }
    });

    input.addEventListener('change', sync);
});


// 수업 리스트 변경 함수
function renderRecordClassList(list) {
    const ul = document.querySelector('.class-list');
    if (!ul) return;

    ul.innerHTML = '';

    list.forEach((item, idx) => {
        const li = document.createElement('li');
        li.className = 'class-btn' + (idx === 0 ? ' active' : '');
        li.dataset.classId = item.classNo;
        li.dataset.classCode = item.classCode;

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
}


document.addEventListener('DOMContentLoaded', () => {
    document.body.addEventListener("change", (e) => {
        if (e.target.matches(".checkbox-group input[type=checkbox]")) {
            const row = e.target.closest("tr");
            const remedialId = row.dataset.id;
            const action = e.target.checked;
            const m = document.getElementById('currentMonth')?.textContent.trim().match(/(\d{4})\D+(\d{1,2})/);
            const year = m?.[1], month = m ? m[2].padStart(2, '0') : null;

            fetch(`/class/remedial/update?year=${year}&month=${month}`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({
                    remedialNo: remedialId,
                    action: action
                })
            })
                .then((res) => res.json())
                .then((data) => {
                    if (data.success) {
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
          <tr data-id="${item.remedialNo}">
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
          <tr data-id="${item.remedialNo}">
            <td>${idx + 1}</td>
            <td>${item.studentName}</td>
            <td>${item.absenceDate}</td>
            <td>${item.remedialSubject}</td>
            <td class="cal-content">${item.remedialDate || ""}</td>
            <td>${item.userName}</td>
            <td class="checkbox-group">
              <input type="checkbox" checked />
            </td>
          </tr>
        `);
    });
}

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
                        remedialNo: selectedSpan.closest("tr").dataset.id,
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

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("tr[data-id]").forEach(bindDatePickerEvents);
});


// =========================== 월간평가(초등) =========================== //
// 월 변경 시 데이터 변경
document.addEventListener("DOMContentLoaded", () => {
    const monthInput = document.getElementById("monthly_calendar");
    const monthDisplay = document.getElementById("monthly_current");
    const calendarBtn = document.querySelector(".calendar-open");

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
                            renderStudentList(data.response);
                        })
                        .catch(err => console.error(err));
                })
        }
    });
});

// 선생님 변경 시 데이터 변경
document.addEventListener("DOMContentLoaded", () => {
    const teacherSelect = document.getElementById("monthly-teacher-select");

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
                        renderStudentList(data.response);
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
                renderStudentList(data.response);
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
function renderStudentList(students) {
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

