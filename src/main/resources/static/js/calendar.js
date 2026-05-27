const monthSwitch = document.getElementById("monthSwitch");
const calendarGrid = document.getElementById("calendarGrid");
const scheduleBox = document.getElementById("scheduleBox");

const today = new Date();
const todayKey = formatDateKey(today);

const currentMonthKey = getMonthKey(today.getFullYear(), today.getMonth() + 1);
const nextMonthDate = new Date(today.getFullYear(), today.getMonth() + 1, 1);
const nextMonthKey = getMonthKey(nextMonthDate.getFullYear(), nextMonthDate.getMonth() + 1);

const months = [currentMonthKey, nextMonthKey];
let activeMonth = months[0];
let dateMap = {};

const FIELD_TO_CLASS = {
    mon: "mon", tue: "tue", wed: "wed",
    thu: "thu", fri: "fri", sat: "satc", sun: "sun"
};
const FIELDS = ["mon", "tue", "wed", "thu", "fri", "sat", "sun"];

init();

function init() {
    renderTabs();
    renderCalendar(activeMonth);
}

function renderTabs() {
    monthSwitch.innerHTML = "";

    months.forEach((monthKey) => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "month-tab" + (monthKey === activeMonth ? " is-active" : "");
        btn.textContent = formatMonth(monthKey);

        btn.addEventListener("click", () => {
            activeMonth = monthKey;
            renderTabs();
            renderCalendar(activeMonth);
        });

        monthSwitch.appendChild(btn);
    });
}

async function renderCalendar(monthKey) {
    calendarGrid.innerHTML = "";

    await loadScheduleData(monthKey);

    const [year, month] = monthKey.split("-").map(Number);
    const firstDay = new Date(year, month - 1, 1).getDay();
    const lastDate = new Date(year, month, 0).getDate();

    // 앞쪽: 이전 달 overflow 셀
    for (let i = 0; i < firstDay; i++) {
        const offset = firstDay - i;
        const overflowDate = new Date(year, month - 1, 1 - offset);
        const dateKey = formatDateKey(overflowDate);

        calendarGrid.appendChild(
            dateMap[dateKey]
                ? createDayCell(overflowDate.getDate(), dateKey, true, monthKey)
                : createEmptyCell()
        );
    }

    // 현재 달 셀
    for (let day = 1; day <= lastDate; day++) {
        const date = new Date(year, month - 1, day);
        const dateKey = formatDateKey(date);
        calendarGrid.appendChild(createDayCell(day, dateKey, false, monthKey));
    }

    // 뒤쪽: 다음 달 overflow 셀
    const totalCells = firstDay + lastDate;
    const trailingCount = totalCells % 7 === 0 ? 0 : 7 - (totalCells % 7);

    for (let i = 1; i <= trailingCount; i++) {
        const overflowDate = new Date(year, month, i);
        const dateKey = formatDateKey(overflowDate);

        calendarGrid.appendChild(
            dateMap[dateKey]
                ? createDayCell(overflowDate.getDate(), dateKey, true, monthKey)
                : createEmptyCell()
        );
    }

    renderSchedule(monthKey);
}

async function loadScheduleData(monthKey) {
    const [year, month] = monthKey.split("-").map(Number);

    const prevDate = new Date(year, month - 2, 1);
    const nextDate = new Date(year, month, 1);

    const prevMonthKey = getMonthKey(prevDate.getFullYear(), prevDate.getMonth() + 1);
    const nextMonthKey = getMonthKey(nextDate.getFullYear(), nextDate.getMonth() + 1);

    const [prevRows, currRows, nextRows] = await Promise.all([
        fetchMonthData(prevDate.getFullYear(), prevDate.getMonth() + 1),
        fetchMonthData(year, month),
        fetchMonthData(nextDate.getFullYear(), nextDate.getMonth() + 1),
    ]);

    const tagRows = (rows, sourceMonth) => rows.map(row => ({...row, sourceMonth}));

    dateMap = {};

    // 현재 달이 마지막에 덮어쓰도록 순서 조정
    [...tagRows(prevRows, prevMonthKey), ...tagRows(nextRows, nextMonthKey), ...tagRows(currRows, monthKey)]
        .forEach(row => {
            FIELDS.forEach(field => {
                if (row[field]) {
                    dateMap[row[field]] = {
                        week: row.week,
                        weekday: FIELD_TO_CLASS[field],
                        sourceMonth: row.sourceMonth
                    };
                }
            });
        });
}

async function fetchMonthData(year, month) {
    const params = new URLSearchParams(window.location.search);
    const centerCode = params.get("centerCode");

    try {
        const res = await fetch("/app/calendar", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                year: String(year),
                month: String(month).padStart(2, "0"),
                centerCode: centerCode
            })
        });
        const data = await res.json();
        return (data.success && data.response) ? data.response : [];
    } catch {
        return [];
    }
}

function createDayCell(day, dateKey, isOverflowDate, currentMonthKey) {
    const cell = document.createElement("div");
    cell.className = "day-cell" + (isOverflowDate ? " other-month" : "");

    if (dateKey === todayKey) cell.classList.add("today");

    const num = document.createElement("div");
    num.className = "day-num";
    num.textContent = day;
    cell.appendChild(num);

    const found = dateMap[dateKey];
    if (found) {
        const weekLabel = found.week.split("_")[1] + "주";
        const isOtherSchedule = found.sourceMonth !== currentMonthKey;

        const marker = document.createElement("div");
        marker.className = "marker";

        const box = document.createElement("div");
        box.className = `marker-box ${found.weekday}`;
        box.textContent = weekLabel;

        // CSS 클래스 대신 JS에서 직접 처리
        if (isOtherSchedule) {
            box.style.opacity = "0.3";
        }

        marker.appendChild(box);
        cell.appendChild(marker);
    }

    return cell;
}

function renderSchedule(monthKey) {
    const [year, month] = monthKey.split("-").map(Number);
    const lastDate = new Date(year, month, 0).getDate();

    let firstClassDay = null;
    let lastClassDay = null;

    // 현재 달 날짜만 범위 계산 (overflow 제외)
    for (let day = 1; day <= lastDate; day++) {
        const date = new Date(year, month - 1, day);
        const dateKey = formatDateKey(date);

        if (dateMap[dateKey]) {
            if (firstClassDay === null) firstClassDay = day;
            lastClassDay = day;
        }
    }

    if (firstClassDay === null) {
        scheduleBox.textContent = `${month}월의 수업 일정 : 등록된 수업 일정이 없습니다.`;
    } else {
        scheduleBox.textContent =
            `${month}월의 수업 일정 : ${month}월 ${firstClassDay}일 ~ ${month}월 ${lastClassDay}일`;
    }
}

function createEmptyCell() {
    const cell = document.createElement("div");
    cell.className = "day-cell is-empty";
    return cell;
}

function getMonthKey(year, month) {
    const date = new Date(year, month - 1, 1);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
}

function formatMonth(monthKey) {
    return `${Number(monthKey.split("-")[1])}월`;
}

function formatDateKey(date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}