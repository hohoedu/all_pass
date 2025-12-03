document.addEventListener('DOMContentLoaded', () => {
    const monthInput = document.querySelector('.hidden-picker');
    const monthBtn = document.querySelector('.calendar-open');
    const monthDisplay = document.querySelector('.day-display');

    initCurrentMonth();

    monthBtn.addEventListener('click', () => monthInput.showPicker());
    monthInput.addEventListener('change', onMonthChange);

    function initCurrentMonth() {
        try {
            const now = new Date();
            const year = now.getFullYear();
            const month = now.getMonth() + 1;

            monthInput.value = `${year}-${String(month).padStart(2, '0')}`;
            monthDisplay.insertAdjacentText('afterbegin', `${year}년 ${month}월`);
        } catch (e) {
            console.log('initCurrentMonth Error', e);
        }
    }

    // 월 변경
    async function onMonthChange() {
        const date = new Date(monthInput.value);
        if (isNaN(date)) return;

        const year = date.getFullYear();
        const month = date.getMonth() + 1;

        monthDisplay.childNodes[0].textContent = `${year}년 ${month}월`;

    }

    try {
        const yearSelect = document.getElementById('order-year');
        const monthSelect = document.getElementById('order-month');

        const now = new Date();
        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth() + 1;

        yearSelect.innerHTML = "";
        for (let i = 0; i < 3; i++) {
            const y = currentYear - i;
            const opt = document.createElement('option');
            opt.value = y;
            opt.textContent = `${y}년`;
            if (i === 0) opt.selected = true;
            yearSelect.appendChild(opt);
        }

        function renderMonthOptions(selectedYear) {
            monthSelect.innerHTML = "";

            let lastMonth = 12;

            if (parseInt(selectedYear) === currentYear) {
                lastMonth = currentMonth;
            }

            for (let m = 1; m <= lastMonth; m++) {
                const mm = String(m).padStart(2, "0");
                const opt = document.createElement('option');
                opt.value = mm;
                opt.textContent = `${mm}월`;
                if (m === currentMonth && selectedYear == currentYear) {
                    opt.selected = true;
                }
                monthSelect.appendChild(opt);

            }
        }

        renderMonthOptions(currentYear);

        yearSelect.addEventListener('change', async () => {
            const selectedYear = yearSelect.value;
            renderMonthOptions(selectedYear);
            await loadSavedOrder();   // ★ 여기에 추가
        });

        monthSelect.addEventListener('change', async () => {
            await loadSavedOrder();   // ★ 여기에 추가
        });

    } catch (e) {
        console.error("년도/월 select 렌더링 오류:", e);
    }

    const rightBody = document.getElementById("order-right-body");

    async function loadSavedOrder() {
        try {
            const yy = document.getElementById("order-year")?.value;
            const mm = document.getElementById("order-month")?.value;

            if (!yy || !mm) {
                console.warn("년도 또는 월이 선택되지 않았습니다.");
                return;
            }

            const payload = {
                yy: yy,
                mm: mm
            };

            const res = await fetch("/manage/order/list", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            if (!res.ok) {
                console.error("서버 응답 오류:", res.status);
                return;
            }

            const data = await res.json();
            const response = data.response
            if (!Array.isArray(response)) {
                console.error("서버 응답 형식 오류:", response);
                return;
            }

            renderSavedOrder(response);

        } catch (e) {
            console.error("loadSavedOrder Error:", e);
        }
    }

    function renderSavedOrder(list) {
        rightBody.innerHTML = "";

        if (!list || list.length === 0) {
            rightBody.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align:center; padding:20px;">
                        주문 내역이 없습니다.
                    </td>
                </tr>
            `;
            return;
        }

        list.forEach(item => {
            const add =
                item.addCount > 0
                    ? `<span class="increase">+${item.addCount}</span>`
                    : item.addCount < 0
                        ? `<span class="decrease">${item.addCount}</span>`
                        : `<span></span>`;

            const row = `
                <tr>
                    <td>${item.className}</td>
                    <td>${item.unitName}</td>
                    <td>${item.baseCount}</td>
                    <td>${add}</td>
                    <td>${item.totalCount}</td>
                </tr>
            `;
            rightBody.insertAdjacentHTML("beforeend", row);
        });
    }

    const totalSpan = document.querySelector(".all-order span");
    const rows = document.querySelectorAll("#order-left-body tr");

    function calculateTotal() {
        let total = 0;
        rows.forEach(row => {
            try {
                const base = parseInt(row.querySelector("td:nth-child(3)")?.innerText.trim() || "0");
                const addInput = row.querySelector("input[type='number']");
                const add = parseInt(addInput?.value || "0");
                total += (base + add);
            } catch (e) {
                console.error("합산 오류:", e);
            }
        });
        totalSpan.innerText = total;
    }

    rows.forEach(row => {
        const input = row.querySelector("input[type='number']");
        const incBtn = row.querySelector(".increment-button");
        const decBtn = row.querySelector(".decrement-button");

        input?.addEventListener("input", () => {
            if (input.value === "" || isNaN(input.value)) input.value = 0;
            calculateTotal();
        });

        incBtn?.addEventListener("click", () => {
            input.value = Number(input.value || 0) + 1;
            calculateTotal();
        });

        decBtn?.addEventListener("click", () => {
            const now = Number(input.value || 0);
            input.value = now - 1;
            calculateTotal();
        });
    });

    calculateTotal(); // 초기 계산


    // 주문 입력
    const saveBtn = document.querySelector(".save-btn");

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
            window.location.reload();
        } catch (e) {
            console.error(e);
            alert("에러 발생");
        }
    });
});