// =================================== //
// ==                               == //
// ==          보강 관리            == //
// ==                               == //
// =================================== //

document.addEventListener("DOMContentLoaded", () => {
    const monthInput = document.getElementById("remedial_calender");
    const monthDisplay = document.getElementById("remedial_current");
    // const teacherSelect = document.getElementById("remedial-teacher-select");
    const calendarBtn = document.querySelector(".remedial-calendar-open");

    if (!monthInput) return;

    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");
    monthInput.value = `${yyyy}-${mm}`;
    monthDisplay.textContent = `${yyyy}년 ${mm}월`;

    calendarBtn.addEventListener("click", () => {
        monthInput.showPicker?.();
        monthInput.click();
    });

    // 월 변경
    monthInput.addEventListener("change", () => {
        if (monthInput.value) {
            const [year, month] = monthInput.value.split("-");
            monthDisplay.textContent = `${year}년 ${month}월`;
            // 데이터 호출 함수
        }
    });
})


// 보강 여부 수정
document.addEventListener('DOMContentLoaded', () => {
    document.body.addEventListener("change", (e) => {
        if (e.target.matches(".checkbox-group input[type=checkbox]")) {
            const row = e.target.closest("tr");
            const remedialKey = row.dataset.id;
            const action = e.target.checked;
            const m = document.getElementById('currentMonth')?.textContent.trim().match(/(\d{4})\D+(\d{1,2})/);
            const year = m?.[1], month = m ? m[2].padStart(2, '0') : null;
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
          <tr data-id="${item.remedialKey}">
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
          <tr data-id="${item.remedialKey}">
            <td>${idx + 1}</td>
            <td>${item.studentName}</td>
            <td>${item.absenceDate}</td>
            <td>${item.remedialSubject}</td>
            <td class="cal-content">${item.remedialDate === '9999-12-31' ? "날짜를 선택하세요" : item.remedialDate}</td>
            <td>${item.userName}</td>
            <td class="checkbox-group">
              <input type="checkbox" checked />
            </td>
          </tr>
        `);
    });
}

// 보강 날짜 변경
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("tr[data-id]").forEach(bindDatePickerEvents);
});

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
                        remedialKey: selectedSpan.closest("tr").dataset.id,
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