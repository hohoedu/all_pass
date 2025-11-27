document.addEventListener('DOMContentLoaded', () => {
    const monthInput = document.querySelector('.hidden-picker');
    const monthBtn = document.querySelector('.calendar-open');
    const monthDisplay = document.querySelector('.day-display');

    initCurrentMonth();

    monthBtn.addEventListener('click', () => monthInput.showPicker());
    monthInput.addEventListener('change', onMonthChange);

    function initCurrentMonth() {
        const now = new Date();
        const year = now.getFullYear();
        const month = now.getMonth() + 1;

        monthInput.value = `${year}-${String(month).padStart(2, '0')}`;
        monthDisplay.insertAdjacentText('afterbegin', `${year}년 ${month}월`);
    }

    // 월 변경
    async function onMonthChange() {
        const date = new Date(monthInput.value);
        if (isNaN(date)) return;

        const year = date.getFullYear();
        const month = date.getMonth() + 1;

        monthDisplay.childNodes[0].textContent = `${year}년 ${month}월`;

    }
})

document.addEventListener("DOMContentLoaded", () => {

    const saveBtn = document.querySelector(".save-btn");
    const monthInput = document.querySelector(".hidden-date");

    saveBtn.addEventListener("click", async () => {
        try {
            const ymRaw = monthInput.value;
            if (!ymRaw) {
                alert("주문년월을 선택해주세요.");
                return;
            }
            const ym = ymRaw.replace("-", "");

            const rows = document.querySelectorAll("#order-left-body tr");
            const orderList = [];

            rows.forEach(row => {

                const classKey = row.dataset.classKey;
                const unitKey = row.dataset.unitKey;

                const tds = row.querySelectorAll("td");
                const baseCount = parseInt(tds[2].innerText.trim());
                const addCount = parseInt(row.querySelector("input").value);

                orderList.push({
                    classKey: classKey,
                    unitKey: unitKey,
                    baseCount: baseCount,
                    addCount: addCount,
                    yy: ym.substring(0, 4),
                    mm: ym.substring(4, 6)
                });
            });

            const res = await fetch("/manage/order/save", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(orderList)
            });

            if (!res.ok) {
                alert("저장 실패");
                return;
            }

            alert("저장 완료!");

        } catch (e) {
            console.error(e);
            alert("에러 발생");
        }
    });

});