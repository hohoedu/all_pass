// =================================== //
// ==                               == //
// ==          보강 관리            == //
// ==                               == //
// =================================== //

// URL에서 year, month 파라미터 가져오기
function getYearMonthFromURL() {
    const params = new URLSearchParams(window.location.search);
    return {
        year: params.get('year'),
        month: params.get('month')
    };
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

// URL에 year, month 파라미터 설정
function setYearMonthToURL(year, month) {
    const url = new URL(window.location);
    url.searchParams.set('year', year);
    url.searchParams.set('month', month);
    window.history.pushState({}, '', url);
}

document.addEventListener("DOMContentLoaded", () => {
    const monthInput = document.getElementById("remedial_calender");
    const monthDisplay = document.getElementById("remedial_current");
    const calendarBtn = document.querySelector(".remedial-calendar-open");

    if (!monthInput) return;

    // URL에서 year, month 먼저 확인
    const urlParams = getYearMonthFromURL();
    let year, month;

    if (urlParams.year && urlParams.month) {
        year = urlParams.year;
        month = urlParams.month;
    } else {
        // URL에 없으면 현재 날짜 사용
        const today = new Date();
        year = today.getFullYear();
        month = String(today.getMonth() + 1).padStart(2, "0");
        // 초기 URL 설정
        setYearMonthToURL(year, month);
    }

    monthInput.value = `${year}-${month}`;
    monthDisplay.textContent = `${year}년 ${month}월`;

    // 초기 데이터 로드
    loadRemedialData(year, month);

    calendarBtn.addEventListener("click", () => {
        monthInput.showPicker?.();
        monthInput.click();
    });

    // 월 변경
    monthInput.addEventListener("change", () => {
        if (monthInput.value) {
            const [year, month] = monthInput.value.split("-");
            monthDisplay.textContent = `${year}년 ${month}월`;

            // URL 업데이트
            setYearMonthToURL(year, month);

            // 데이터 호출 함수
            loadRemedialData(year, month);
        }
    });
});

async function loadRemedialData(year, month) {
    try {
        const response = await fetch('/class/remedial/list', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                year: year,
                month: month
            })
        });

        if (!response.ok) {
            throw new Error('데이터 로드 실패');
        }

        const data = await response.json();

        // response 배열에서 데이터 가져오기
        const remedials = data.response || [];
        console.log(remedials[0]);
        // action 값에 따라 좌측/우측 데이터 분리
        const leftRemedials = remedials.filter(item => !item.action);
        const rightRemedials = remedials.filter(item => item.action);

        // 좌측 테이블 렌더링 (보강 대기)
        renderLeftTable(leftRemedials);
        // 우측 테이블 렌더링 (보강 완료)
        renderRightTable(rightRemedials);

    } catch (error) {
        console.error('데이터 로드 실패:', error);
        alert('데이터를 불러오는데 실패했습니다.');
    }
}

// 좌측 테이블 렌더링
function renderLeftTable(data) {
    const tbody = document.getElementById('student-tbody-left');
    tbody.innerHTML = '';

    data.forEach((item, index) => {
        const displayDate = item.remedialDate === '9999-12-31' ? '날짜를 선택하세요' : item.remedialDate;

        const row = document.createElement('tr');
        row.setAttribute('data-id', item.remedialKey);
        row.innerHTML = `
            <td>${index + 1}</td>
            <td>${item.studentName}</td>
            <td>${item.absenceDate}</td>
            <td>${item.remedialSubject}</td>
            <td class="cal-content">
                <div class="icon-field time-input cal-adjust" style="margin-bottom: 0;">
                    <span class="selected-datetime">${displayDate}</span>
                    <input type="date" class="datetime-input hidden-picker"/>
                    <button type="button" class="icon-btn calendar-btn" style="background: transparent;">
                        <img src="/image/calendar.png" alt="달력 아이콘"/>
                    </button>
                </div>
            </td>
            <td>
                <div class="time-boxes">
                    <div class="time-start"style="margin-bottom: 0">
                        시작
                        <input type="text" class="time_input start-time"
                        value="${item.stime ?? ''}"
                        placeholder="00:00" pattern="^([01]\\d|2[0-3]):[0-5]\\d$"
                        inputmode="numeric" maxlength="5"title="시간은 24시간 기준으로 입력하세요"/>
                    </div>
                </div>
            </td>
            <td>${item.userName}</td>
            <td class="checkbox-group">
                <input type="checkbox"/>
            </td>
            <td>
                <button type="button" class="icon-btn delete-btn" style="background: transparent;">
                    <img src="/image/cancel-icon.png" alt="삭제" style="width:20px; height:20px;"/>
                </button>
            </td>
        `;
        tbody.appendChild(row);

        // 렌더링 후 이벤트 바인딩
        bindDatePickerEvents(row);
        bindTimeInputEvents(row);
        bindDeleteEvents(row);
    });
}

// 우측 테이블 렌더링
function renderRightTable(data) {
    const tbody = document.getElementById('student-tbody-right');
    tbody.innerHTML = '';

    data.forEach((item, index) => {
        const displayDate = item.remedialDate === '9999-12-31' ? '날짜를 선택하세요.' : item.remedialDate;

        const row = document.createElement('tr');
        row.setAttribute('data-id', item.remedialKey);
        row.innerHTML = `
            <td>${index + 1}</td>
            <td>${item.studentName}</td>
            <td>${item.absenceDate}</td>
            <td>${item.remedialSubject}</td>
            <td class="cal-content">${displayDate}</td>
            <td>${item.userName}</td>
            <td class="checkbox-group">
                <input type="checkbox" checked/>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// 보강 여부 수정
document.addEventListener('DOMContentLoaded', () => {
    document.body.addEventListener("change", (e) => {
        if (e.target.matches(".checkbox-group input[type=checkbox]")) {
            const row = e.target.closest("tr");
            const remedialKey = row.dataset.id;
            const action = e.target.checked;

            // URL에서 year, month 가져오기
            const {year, month} = getYearMonthFromURL();

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
                        // 데이터 다시 로드 (새로고침 대신)
                        loadRemedialData(year, month);
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

// 보강 날짜 변경 이벤트 바인딩
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

                fetch("/class/remedial/updateDate", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({
                        remedialKey: row.dataset.id,
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

function bindTimeInputEvents(row) {
    const timeInput = row.querySelector(".start-time");

    if (!timeInput) return;

    timeInput.addEventListener("change", () => {
        const value = timeInput.value;
        console.log(value);
        // 패턴 검증
        const pattern = /^([01]\d|2[0-3]):[0-5]\d$/;
        if (!pattern.test(value)) {
            alert("시간 형식이 올바르지 않습니다. (예: 09:30)");
            timeInput.value = '';
            return;
        }

        fetch("/class/remedial/updateTime", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                remedialKey: row.dataset.id,
                startTime: value
            })
        })
            .then(res => res.json())
            .then(data => {
                if (!data.success) {
                    alert("시간 저장 실패");
                }
            })
            .catch(err => console.error("서버 오류:", err));
    });
}

function bindDeleteEvents(row) {
    const deleteBtn = row.querySelector(".delete-btn");
    if (!deleteBtn) return;

    deleteBtn.addEventListener("click", () => {
        if (!confirm("해당 보강 항목을 삭제하시겠습니까?")) return;

        const remedialKey = row.dataset.id;

        fetch("/class/remedial/delete", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ remedialKey: remedialKey })
        })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    const { year, month } = getYearMonthFromURL();
                    loadRemedialData(year, month);
                } else {
                    alert("삭제 실패");
                }
            })
            .catch(err => console.error("서버 오류:", err));
    });
}