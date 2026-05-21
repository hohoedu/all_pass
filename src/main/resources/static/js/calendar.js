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

  for (let i = 0; i < firstDay; i++) {
    calendarGrid.appendChild(createEmptyCell());
  }

  for (let day = 1; day <= lastDate; day++) {
    const date = new Date(year, month - 1, day);
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

    const found = dateMap[dateKey];
    if (found) {
      const weekLabel = found.week.split("_")[1] + "주";

      const marker = document.createElement("div");
      marker.className = "marker";

      const box = document.createElement("div");
      box.className = `marker-box ${found.weekday}`;
      box.textContent = weekLabel;

      marker.appendChild(box);
      cell.appendChild(marker);
    }

    calendarGrid.appendChild(cell);
  }

  renderSchedule(monthKey);
}

async function loadScheduleData(monthKey) {
  const [year, month] = monthKey.split("-");

  const params = new URLSearchParams(window.location.search);
  const centerCode = params.get('centerCode');

  const res = await fetch("/app/calendar", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      year: year,
      month: month,
      centerCode: centerCode
    })
  });

  const data = await res.json();

  if (!data.success || !data.response) {
    dateMap = {};

    return;
  }

  const weekData = data.response;

  const fieldToClass = {
    mon: "mon", tue: "tue", wed: "wed",
    thu: "thu", fri: "fri", sat: "satc", sun: "sun"
  };

  dateMap = {};
  const fields = ["mon", "tue", "wed", "thu", "fri", "sat", "sun"];

  weekData.forEach(row => {
    fields.forEach(field => {
      if (row[field]) {
        dateMap[row[field]] = {
          week: row.week,
          weekday: fieldToClass[field]
        };
      }
    });
  });


}
function renderSchedule(monthKey) {
  const [year, month] = monthKey.split("-").map(Number);
  const lastDate = new Date(year, month, 0).getDate();

  let firstClassDay = null;
  let lastClassDay = null;

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