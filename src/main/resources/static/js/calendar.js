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

function renderCalendar(monthKey) {
  calendarGrid.innerHTML = "";

  const [year, month] = monthKey.split("-").map(Number);
  const firstDay = new Date(year, month - 1, 1).getDay();
  const lastDate = new Date(year, month, 0).getDate();

  for (let i = 0; i < firstDay; i++) {
    calendarGrid.appendChild(createEmptyCell());
  }

  for (let day = 1; day <= lastDate; day++) {
    const date = new Date(year, month - 1, day);
    const weekday = date.getDay();
    const dateKey = formatDateKey(date);

    const cell = document.createElement("div");
    cell.className = "day-cell";

    if (dateKey === todayKey) {
      cell.classList.add("today");
    }

    const num = document.createElement("div");
    num.className = "day-num";
    num.textContent = day;
    cell.appendChild(num);

    // 일요일은 수업 없음
    if (weekday !== 0) {
      const classWeekNumber = getClassWeekNumber(year, month, weekday, day);

      // 주 1회, 월 4회 수업 기준: 1~4주만 표시
      if (classWeekNumber <= 4) {
        const marker = document.createElement("div");
        marker.className = "marker";

        const box = document.createElement("div");
        box.className = `marker-box ${getColorClass(weekday)}`;
        box.textContent = `${classWeekNumber}주`;

        marker.appendChild(box);
        cell.appendChild(marker);
      }
    }

    calendarGrid.appendChild(cell);
  }

  renderSchedule(monthKey);
}

function renderSchedule(monthKey) {
  const [year, month] = monthKey.split("-").map(Number);
  const firstClassDate = getFirstClassDate(year, month);
  const lastClassDate = getLastClassDate(year, month);

  if (!firstClassDate || !lastClassDate) {
    scheduleBox.textContent = `${month}월의 수업 일정 : 등록된 수업 일정이 없습니다.`;
    return;
  }

  scheduleBox.textContent =
    `${month}월의 수업 일정 : ${month}월 ${firstClassDate}일 ~ ${month}월 ${lastClassDate}일`;
}

function getFirstClassDate(year, month) {
  const lastDate = new Date(year, month, 0).getDate();

  for (let day = 1; day <= lastDate; day++) {
    const date = new Date(year, month - 1, day);
    const weekday = date.getDay();

    if (weekday !== 0) {
      const classWeekNumber = getClassWeekNumber(year, month, weekday, day);

      if (classWeekNumber === 1) {
        return day;
      }
    }
  }

  return null;
}

function getLastClassDate(year, month) {
  const lastDate = new Date(year, month, 0).getDate();
  let lastClassDate = null;

  for (let day = 1; day <= lastDate; day++) {
    const date = new Date(year, month - 1, day);
    const weekday = date.getDay();

    if (weekday === 0) continue;

    const classWeekNumber = getClassWeekNumber(year, month, weekday, day);

    if (classWeekNumber <= 4) {
      lastClassDate = day;
    }
  }

  return lastClassDate;
}

function getClassWeekNumber(year, month, weekday, currentDay) {
  let count = 0;

  for (let day = 1; day <= currentDay; day++) {
    const date = new Date(year, month - 1, day);

    if (date.getDay() === weekday) {
      count++;
    }
  }

  return count;
}

function getColorClass(weekday) {
  return {
    1: "mon",
    2: "tue",
    3: "wed",
    4: "thu",
    5: "fri",
    6: "satc"
  }[weekday];
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