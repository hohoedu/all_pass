function getYearMonthFromURL() {
    const params = new URLSearchParams(window.location.search);
    return {year: params.get('year'), month: params.get('month')};
}

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

    const year  = monthDisplay.dataset.year;
    const month = monthDisplay.dataset.month;

    monthInput.value = `${year}-${month}`;
    monthDisplay.textContent = `${year}년 ${month}월`;
    setYearMonthToURL(year, month);  // URL을 올바른 월로 초기화

    loadRemedialData(year, month);

    calendarBtn.addEventListener("click", () => {
        monthInput.showPicker?.();
        monthInput.click();
    });

    monthInput.addEventListener("change", () => {
        if (!monthInput.value) return;
        const [y, m] = monthInput.value.split("-");
        monthDisplay.textContent = `${y}년 ${m}월`;
        setYearMonthToURL(y, m);
        loadRemedialData(y, m);
    });
});

async function loadRemedialData(year, month, userCode) {
    try {
        if (!userCode) {
            userCode = document.getElementById("remedial-teacher-filter")?.value ?? "";
        }

        const response = await fetch('/class/remedial/list', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({year, month, userCode})
        });

        if (!response.ok) throw new Error('데이터 로드 실패');

        const data = await response.json();
        const remedials = data.response.remedials || [];
        const absentFlags = data.response.absentFlags || [];
        console.log(absentFlags);
        const flagMap = {};
        absentFlags.forEach(f => {
            flagMap[f.studentId] = f.absentFlag;
        });

        const leftRemedials = remedials.filter(item => !item.action);
        const rightRemedials = remedials.filter(item => item.action);

        renderLeftTable(leftRemedials, flagMap);
        renderRightTable(rightRemedials);

    } catch (error) {
        console.error('데이터 로드 실패:', error);
        alert('데이터를 불러오는데 실패했습니다.');
    }
}

function renderLeftTable(data, flagMap = {}) {
    const tbody = document.getElementById('student-tbody-left');
    const flagTooltip = {
        1: '결석 2주',
        2: '10일 이내 미보강',
        3: '결석 2주 / 10일 이내 미보강'
    };

    tbody.innerHTML = '';
    data.forEach((item, index) => {
        const displayDate = item.remedialDate === '9999-12-31' ? '날짜를 선택하세요' : item.remedialDate;
        const flag = flagMap[item.studentId];
        const flagBadge = flag
            ? `<span class="absent-flag-badge" data-flag="${flag}" data-tooltip="${flagTooltip[flag] || ''}">${flag}</span>`
            : '';

        const row = document.createElement('tr');
        row.setAttribute('data-id', item.remedialKey);
        row.innerHTML = `
            <td>${index + 1}</td>
            <td>${item.studentName} ${flagBadge}</td>
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
                    <div class="time-start" style="margin-bottom: 0">
                        시작
                        <input type="text" class="time_input start-time"
                               value="${item.stime ?? ''}"
                               placeholder="00:00"
                               pattern="^([01]\\d|2[0-3]):[0-5]\\d$"
                               inputmode="numeric" maxlength="5"
                               title="시간은 24시간 기준으로 입력하세요"/>
                    </div>
                </div>
            </td>
            <td>${item.userName}</td>
            <td class="checkbox-group"><input type="checkbox"/></td>
            <td>
                <button type="button" class="icon-btn delete-btn" style="background: transparent;">
                    <img src="/image/cancel-icon.png" alt="삭제" style="width:20px; height:20px;"/>
                </button>
            </td>
        `;
        tbody.appendChild(row);

        bindDatePickerEvents(row);
        bindTimeInputEvents(row);
        bindDeleteEvents(row);
    });
}

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
            <td class="checkbox-group"><input type="checkbox" checked/></td>
        `;
        tbody.appendChild(row);
    });
}

document.addEventListener('DOMContentLoaded', () => {
    document.body.addEventListener('input', e => {
        if (!e.target.matches('.time_input')) return;
        let v = e.target.value.replace(/[^0-9]/g, '');
        if (v.length >= 3) v = v.slice(0, 2) + ':' + v.slice(2, 4);
        e.target.value = v;
    });
});

document.addEventListener('DOMContentLoaded', () => {
    document.body.addEventListener("change", (e) => {
        if (!e.target.matches(".checkbox-group input[type=checkbox]")) return;

        const row = e.target.closest("tr");
        const remedialKey = row.dataset.id;
        const action = e.target.checked;
        const {year, month} = getYearMonthFromURL();

        fetch(`/class/remedial/update?year=${year}&month=${month}`, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({remedialKey, action})
        })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    loadRemedialData(year, month);
                } else {
                    alert("저장 실패");
                    e.target.checked = !action;
                }
            })
            .catch(err => {
                console.error(err);
                e.target.checked = !action;
            });
    });

    const teacherFilter = document.getElementById("remedial-teacher-filter");
    if (teacherFilter) {
        teacherFilter.addEventListener("change", () => {
            const {year, month} = getYearMonthFromURL();
            loadRemedialData(year, month, teacherFilter.value);
        });
    }
});

function bindDatePickerEvents(row) {
    const calendarBtn = row.querySelector(".calendar-btn");
    const dateInput = row.querySelector(".datetime-input");
    const selectedSpan = row.querySelector(".selected-datetime");

    if (!calendarBtn || !dateInput || !selectedSpan) return;

    calendarBtn.addEventListener("click", () =>
        dateInput.showPicker?.() || dateInput.click()
    );

    dateInput.addEventListener("change", () => {
        if (!dateInput.value) return;
        selectedSpan.textContent = dateInput.value;

        fetch("/class/remedial/updateDate", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({remedialKey: row.dataset.id, remedialDate: dateInput.value})
        })
            .then(res => res.json())
            .then(data => {
                if (!data.success) alert("저장 실패");
            })
            .catch(err => console.error("서버 오류:", err));
    });
}

function bindTimeInputEvents(row) {
    const timeInput = row.querySelector(".start-time");
    if (!timeInput) return;

    timeInput.addEventListener("change", () => {
        const value = timeInput.value;
        const pattern = /^([01]\d|2[0-3]):[0-5]\d$/;
        if (!pattern.test(value)) {
            alert("시간 형식이 올바르지 않습니다. (예: 09:30)");
            timeInput.value = '';
            return;
        }

        fetch("/class/remedial/updateTime", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({remedialKey: row.dataset.id, startTime: value})
        })
            .then(res => res.json())
            .then(data => {
                if (!data.success) alert("시간 저장 실패");
            })
            .catch(err => console.error("서버 오류:", err));
    });
}

function bindDeleteEvents(row) {
    const deleteBtn = row.querySelector(".delete-btn");
    if (!deleteBtn) return;

    deleteBtn.addEventListener("click", () => {
        if (!confirm("해당 보강 항목을 삭제하시겠습니까?")) return;

        fetch("/class/remedial/delete", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({remedialKey: row.dataset.id})
        })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    const {year, month} = getYearMonthFromURL();
                    loadRemedialData(year, month);
                } else {
                    alert("삭제 실패");
                }
            })
            .catch(err => console.error("서버 오류:", err));
    });
}