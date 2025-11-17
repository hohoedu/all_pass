document.addEventListener("DOMContentLoaded", function () {

    const dateInput = document.querySelector("#record_calendar");
    const dateText = document.querySelector("#record_current");
    const calendarBtn = document.querySelector(".calendar-open");

    if (!dateInput || !dateText || !calendarBtn) return;

    calendarBtn.addEventListener("click", () => {
        try {
            dateInput.showPicker();
        } catch (e) {
            dateInput.click();
        }
    });

    dateInput.addEventListener("change", () => {
        const selected = dateInput.value;
        if (!selected) return;

        dateText.textContent = selected;

        loadRecordData(selected);
    });

    // 3) 최초 기본 값 표시
    if (dateInput.value) {
        dateText.textContent = dateInput.value;
    }
});