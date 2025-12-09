document.addEventListener('DOMContentLoaded', () => {

    /* ======= *
     *   LEFT  *
     * ======= */
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
            console.error("initCurrentMonth Error:", e);
        }
    }

    async function onMonthChange() {
        try {
            const date = new Date(monthInput.value);
            if (isNaN(date)) return;

            const year = date.getFullYear();
            const month = date.getMonth() + 1;

            monthDisplay.childNodes[0].textContent = `${year}년 ${month}월`;

            const response = await fetch(`/manage/order/base/list`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({
                    yy: String(year),
                    mm: String(month).padStart(2, "0")
                })
            });

            if (!response.ok) {
                console.error("서버 조회 실패:", response.status);
                return;
            }

            const data = await response.json();
            renderLeftTable(data.response);

        } catch (e) {
            console.error("월 변경 처리 중 오류:", e);
        }
    }

    function renderLeftTable(list) {
        const tbody = document.getElementById("order-left-body");
        tbody.innerHTML = "";

        if (!list || list.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align:center; padding:20px;">
                        등록된 수업이 없습니다.
                    </td>
                </tr>`;
            calculateTotal();
            return;
        }

        list.forEach(item => {
            tbody.insertAdjacentHTML("beforeend", `
                <tr data-class-key="${item.classKey}"
                    data-unit-key="${item.unitKey}">
                    <td>${item.className}</td>
                    <td>${item.unitName}</td>
                    <td>${item.baseCount}</td>
                    <td>
                        <div class="spinner-frame">
                            <div class="custom-spinner">
                                <input type="number" value="${item.addCount || 0}" step="1">
                                <div class="spinner-buttons">
                                    <button type="button" class="increment-button">▲</button>
                                    <button type="button" class="decrement-button">▼</button>
                                </div>
                            </div>
                        </div>
                    </td>
                </tr>
            `);
        });

        bindLeftTableEvents();
        calculateTotal();
    }

    function bindLeftTableEvents() {
        const rows = document.querySelectorAll("#order-left-body tr");

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
                input.value = Number(input.value || 0) - 1;
                calculateTotal();
            });
        });
    }

    const totalSpan = document.querySelector(".all-order span");

    function calculateTotal() {
        const rows = document.querySelectorAll("#order-left-body tr");
        let total = 0;

        rows.forEach(row => {
            const base = parseInt(row.querySelector("td:nth-child(3)")?.innerText || 0);
            const addInput = row.querySelector("input[type='number']");
            const add = parseInt(addInput?.value || 0);
            total += (base + add);
        });

        totalSpan.innerText = total;
    }

    if (window.initialBaseList) {
        renderLeftTable(window.initialBaseList);
    } else {
        calculateTotal(); // 최소한 총합 표시
    }

    /* ======= *
     *  RIGHT  *
     * ======= */
    const yearSelect = document.getElementById("order-year");
    const monthSelect = document.getElementById("order-month");

    initYearOptions();
    renderMonthOptions(yearSelect.value);

    yearSelect.addEventListener("change", async () => {
        renderMonthOptions(yearSelect.value);
        await loadSavedOrder();
    });

    monthSelect.addEventListener("change", async () => {
        await loadSavedOrder();
    });

    function initYearOptions() {
        const now = new Date();
        const ty = now.getFullYear();
        const tm = now.getMonth() + 1;

        const start = new Date(ty, tm - 7, 1);
        const end = new Date(ty, tm, 1);

        const startYear = start.getFullYear();
        const endYear = end.getFullYear();

        yearSelect.innerHTML = "";

        for (let y = startYear; y <= endYear; y++) {
            const opt = document.createElement("option");
            opt.value = y;
            opt.textContent = `${y}년`;
            if (y === ty) opt.selected = true;  // 현재년 선택
            yearSelect.appendChild(opt);
        }
    }

    function renderMonthOptions(selectedYear) {
        const now = new Date();
        const ty = now.getFullYear();
        const tm = now.getMonth() + 1;

        const start = new Date(ty, tm - 7, 1);
        const end = new Date(ty, tm, 1);

        monthSelect.innerHTML = "";

        let d = new Date(start);

        while (d <= end) {
            const yy = d.getFullYear();
            const mm = d.getMonth() + 1;

            if (yy == selectedYear) {
                const opt = document.createElement("option");
                opt.value = String(mm).padStart(2, "0");
                opt.textContent = `${String(mm).padStart(2, "0")}월`;

                if (yy === ty && mm === tm) {
                    opt.selected = true; // 현재월 자동 선택
                }

                monthSelect.appendChild(opt);
            }

            d.setMonth(d.getMonth() + 1); // 다음달로 이동
        }
    }

    const rightBody = document.getElementById("order-right-body");

    async function loadSavedOrder() {
        try {
            const yy = yearSelect.value;
            const mm = monthSelect.value;
            if (!yy || !mm) return;

            const res = await fetch("/manage/order/list", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({yy, mm})
            });

            if (!res.ok) return;

            const data = await res.json();
            renderSavedOrder(data.response);

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
                </tr>`;
            return;
        }

        list.forEach(item => {
            const add =
                item.addCount > 0
                    ? `<span class="increase">+${item.addCount}</span>`
                    : item.addCount < 0
                        ? `<span class="decrease">${item.addCount}</span>`
                        : `<span></span>`;

            rightBody.insertAdjacentHTML("beforeend", `
                <tr>
                    <td>${item.className}</td>
                    <td>${item.unitName}</td>
                    <td>${item.baseCount}</td>
                    <td>${add}</td>
                    <td>${item.totalCount}</td>
                </tr>
            `);
        });
    }

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
                orderList.push({
                    classKey: row.dataset.classKey,
                    unitKey: row.dataset.unitKey,
                    baseCount: parseInt(row.querySelector("td:nth-child(3)").innerText),
                    addCount: parseInt(row.querySelector("input").value),
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
            alert("에러 발생");
        }
    });

});