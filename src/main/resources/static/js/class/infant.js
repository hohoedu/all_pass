document.addEventListener("DOMContentLoaded", () => {

    const monthInput = document.querySelector(".hidden-date");
    const monthDisplay = document.querySelector(".current-month");
    const monthBtn = document.querySelector(".calendar-open");
    const teacherSelect = document.getElementById("teacher-select");
    const classButtons = document.querySelectorAll(".class-btn");

    const today = new Date();
    const yy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");

    monthInput.value = `${yy}-${mm}`;

    monthDisplay.textContent = `${yy}년 ${mm}월`;

    monthBtn.addEventListener("click", () => {
        console.log("[달력 버튼 클릭]");
        monthInput.showPicker();
    });

    monthInput.addEventListener("change", () => {
        const [year, month] = monthInput.value.split("-");
        monthDisplay.textContent = `${year}년 ${Number(month)}월`;
        console.log("[월 변경됨]", year, month);
    });

    teacherSelect.addEventListener("change", (e) => {
        const teacher = e.target.value;
        console.log("[선생님 변경됨]", teacher);
    });

    classButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            const classId = btn.dataset.classId;
            const time = btn.dataset.time;

        });
    });

});
