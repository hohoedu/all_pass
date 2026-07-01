document.addEventListener('DOMContentLoaded', () => {

    const monthPicker = document.getElementById('monthPicker');
    const monthDisplay = document.getElementById('monthDisplay');
    const teacherSelect = document.getElementById('teacherSelect');
    const tbody = document.querySelector('.timetable-lookup-table tbody');

    const DAYS = ['mon', 'tue', 'wed', 'thu', 'fri', 'sat'];

    // ────────────────────────────────────────
    // 시간표 렌더링
    // ────────────────────────────────────────
    const renderTimetable = (timetables) => {
        const byPeriod = {};
        (timetables || []).forEach(tt => {
            if (!byPeriod[tt.periodNo]) byPeriod[tt.periodNo] = {};
            byPeriod[tt.periodNo][tt.dayname] = tt;
        });

        const maxPeriod = Math.max(6, ...Object.keys(byPeriod).map(Number));
        const periods = Array.from({ length: maxPeriod }, (_, i) => i + 1);

        tbody.innerHTML = '';
        periods.forEach(period => {
            const tr = document.createElement('tr');

            const tdPeriod = document.createElement('td');
            tdPeriod.textContent = period;
            tr.appendChild(tdPeriod);

            DAYS.forEach(day => {
                const td = document.createElement('td');
                const tt = byPeriod[period] && byPeriod[period][day];
                if (tt) {
                    const color = tt.classType === '1' ? 'blue' : 'pink';
                    const students = tt.students || [];
                    const count = students.length;
                    const slots = Array.from({ length: 8 }, (_, i) =>
                        `<div>${students[i] ? students[i].studentName : ''}</div>`
                    ).join('');
                    const box = document.createElement('div');
                    box.className = `timetable-lookup-box ${color}`;
                    const classLine = tt.unitName ? `<strong>${tt.className} ${tt.unitName}</strong>` : `<strong>${tt.className}</strong>`;
                    box.innerHTML = `
                        <div class="header">
                            <span class="header-left"></span>
                            <span class="header-center">${tt.startTime} ~ ${tt.endTime}<br>${classLine}</span>
                            <span class="header-right">(${count}명)</span>
                        </div>
                        <div class="inner-grid">${slots}</div>
                    `;
                    td.appendChild(box);
                }
                tr.appendChild(td);
            });

            tbody.appendChild(tr);
        });
    };

    // ────────────────────────────────────────
    // 선생님 목록 조회
    // ────────────────────────────────────────
    const fetchTeachers = async (centerCode, centerName) => {
        const res = await fetch(`/center/teachers?centerCode=${centerCode}`);
        const teachers = await res.json();

        teacherSelect.innerHTML = `<option value="">${centerName}</option>`;
        teachers.forEach(t => {
            const option = document.createElement('option');
            option.value = t.userCode;
            option.textContent = t.userName;
            teacherSelect.appendChild(option);
        });
    };

    // ────────────────────────────────────────
    // 시간표 조회
    // ────────────────────────────────────────
    const fetchTimetable = async () => {
        const [year, month] = monthPicker.value.split('-');
        const activeCenter = document.querySelector('.center-item.active');
        const centerCode = activeCenter ? activeCenter.dataset.centerCode : null;

        if (!centerCode) return;

        const teacherCode = teacherSelect.value || '';
        if (!teacherCode) {
            tbody.innerHTML = '';
            return;
        }

        const res = await fetch(`/center/timetable?centerCode=${centerCode}&userCode=${teacherCode}&year=${year}&month=${month}`);
        const timetables = await res.json();
        renderTimetable(timetables);
    };

    // ────────────────────────────────────────
    // 수업 주차 설정 모달
    // ────────────────────────────────────────
    (() => {
        const modal = document.getElementById('weekModal');
        const openBtn = document.getElementById('openWeekModalBtn');
        const closeBtn = modal.querySelector('.week-modal-close');
        const calendarGrid = document.getElementById('calendarGrid');
        const calendarMonthLabel = document.getElementById('calendarMonthLabel');
        const prevBtn = document.getElementById('calendarPrevBtn');
        const nextBtn = document.getElementById('calendarNextBtn');

        const weekColors = { 1: '#fecec8', 2: '#c1e6c4', 3: '#d5e4f7', 4: '#d3c7e9' };
        const weekdayMap = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];

        let currentYear, currentMonth, selectedWeek = null;
        let weekData = { year: '', month: '', centerCode: '', week: {} };

        function resetWeekData() {
            weekData.week = {};
            for (let i = 1; i <= 4; i++) weekData.week[i] = {};
        }

        function getActiveCenterCode() {
            const active = document.querySelector('.center-item.active');
            return active ? active.dataset.centerCode : null;
        }

        function renderCalendar(date) {
            const year = date.getFullYear();
            const month = date.getMonth() + 1;
            calendarMonthLabel.textContent = `${year}년 ${month}월`;

            const firstDay = new Date(year, month - 1, 1).getDay();
            const daysInMonth = new Date(year, month, 0).getDate();
            const prevDays = new Date(year, month - 1, 0).getDate();
            calendarGrid.innerHTML = '';

            for (let i = 0; i < firstDay; i++) {
                const cell = document.createElement('div');
                cell.className = 'week-modal-calendar-cell disabled';
                cell.textContent = prevDays - (firstDay - i - 1);
                calendarGrid.appendChild(cell);
            }
            for (let d = 1; d <= daysInMonth; d++) {
                const cell = document.createElement('div');
                cell.className = 'week-modal-calendar-cell';
                cell.textContent = d;
                calendarGrid.appendChild(cell);
            }
            const remaining = 42 - (firstDay + daysInMonth);
            for (let i = 1; i <= remaining; i++) {
                const cell = document.createElement('div');
                cell.className = 'week-modal-calendar-cell disabled';
                cell.textContent = i;
                calendarGrid.appendChild(cell);
            }
            repaintSelectedDates(year, month);
        }

        function repaintSelectedDates(year, month) {
            document.querySelectorAll('.week-modal-calendar-cell').forEach(cell => {
                const day = parseInt(cell.textContent);
                if (!day || cell.classList.contains('disabled')) return;
                const yyyyMMDD = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
                for (let w = 1; w <= 4; w++) {
                    for (let key in weekData.week[w]) {
                        if (weekData.week[w][key] === yyyyMMDD) cell.style.background = weekColors[w];
                    }
                }
            });
        }

        openBtn.addEventListener('click', async () => {
            const centerCode = getActiveCenterCode();
            if (!centerCode) { alert('센터를 먼저 선택해주세요.'); return; }

            const [year, month] = monthPicker.value.split('-');
            weekData.year = year;
            weekData.month = month;
            weekData.centerCode = centerCode;
            resetWeekData();

            modal.style.display = 'flex';
            currentYear = parseInt(year);
            currentMonth = parseInt(month);

            try {
                const res = await fetch('/center/week/get', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ year, month, centerCode })
                });
                const json = await res.json();
                (json.response || []).forEach(row => {
                    const weekNum = parseInt(row.week.replace('ju_', ''));
                    weekData.week[weekNum] = { mon: row.mon, tue: row.tue, wed: row.wed, thu: row.thu, fri: row.fri, sat: row.sat, sun: row.sun };
                });
            } catch (e) { console.error('주차 조회 오류:', e); }

            renderCalendar(new Date(currentYear, currentMonth - 1));
        });

        closeBtn.addEventListener('click', () => { resetWeekData(); modal.style.display = 'none'; });
        modal.addEventListener('click', e => { if (e.target === modal) { resetWeekData(); modal.style.display = 'none'; } });

        document.querySelectorAll('.week-modal-week-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.week-modal-week-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                selectedWeek = parseInt(btn.dataset.week);
            });
        });

        prevBtn.addEventListener('click', () => {
            currentMonth--;
            if (currentMonth < 1) { currentMonth = 12; currentYear--; }
            renderCalendar(new Date(currentYear, currentMonth - 1));
        });
        nextBtn.addEventListener('click', () => {
            currentMonth++;
            if (currentMonth > 12) { currentMonth = 1; currentYear++; }
            renderCalendar(new Date(currentYear, currentMonth - 1));
        });

    })();

    // ────────────────────────────────────────
    // 달력 피커
    // ────────────────────────────────────────
    document.querySelectorAll('.calendar-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const picker = document.getElementById(btn.dataset.picker);
            if (typeof picker.showPicker === 'function') picker.showPicker();
            else picker.focus();
        });
    });

    monthPicker.addEventListener('change', () => {
        const [year, month] = monthPicker.value.split('-');
        monthDisplay.textContent = `${year}. ${month}`;
        fetchTimetable();
    });

    // ────────────────────────────────────────
    // 센터 선택
    // ────────────────────────────────────────
    document.querySelectorAll('.center-item').forEach(item => {
        item.addEventListener('click', () => {
            document.querySelectorAll('.center-item').forEach(c => c.classList.remove('active'));
            item.classList.add('active');
            const centerName = item.querySelector('.center-name').textContent;
            fetchTeachers(item.dataset.centerCode, centerName);
            tbody.innerHTML = '';
        });
    });

    const searchInput = document.querySelector('.search-box input');
    if (searchInput) {
        searchInput.addEventListener('input', e => {
            const keyword = e.target.value.trim().toLowerCase();
            document.querySelectorAll('.center-item').forEach(item => {
                const name = item.querySelector('.center-name').textContent.toLowerCase();
                item.style.display = name.includes(keyword) ? '' : 'none';
            });
        });
    }

    teacherSelect.addEventListener('change', fetchTimetable);
});
