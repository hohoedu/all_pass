document.addEventListener('DOMContentLoaded', () => {
    initAllMonthPickers();

    function initAllMonthPickers() {
        const pickers = document.querySelectorAll(".day-display");

        pickers.forEach(display => {
            const monthInput = display.querySelector(".hidden-picker");
            const monthBtn = display.querySelector(".calendar-open");

            if (!monthInput || !monthBtn) return;

            initCurrentMonth(display, monthInput);

            monthBtn.addEventListener("click", () => monthInput.showPicker());
            monthInput.addEventListener("change", () => onMonthChange(display, monthInput));
        });
    }

    function initCurrentMonth(display, monthInput) {
        try {
            const now = new Date();
            const year = now.getFullYear();
            const month = now.getMonth() + 1;

            monthInput.value = `${year}-${String(month).padStart(2, '0')}`;

            // 기존 span 삭제
            const old = display.querySelector(".month-text");
            if (old) old.remove();

            display.insertAdjacentHTML(
                "afterbegin",
                `<span class="month-text">${year}년 ${month}월</span>`
            );

        } catch (e) {
            console.log('initCurrentMonth Error', e);
        }
    }

    function onMonthChange(display, monthInput) {
        const date = new Date(monthInput.value);
        if (isNaN(date)) return;

        const year = date.getFullYear();
        const month = date.getMonth() + 1;

        const span = display.querySelector(".month-text");
        if (span) {
            span.textContent = `${year}년 ${month}월`;
        }
    }

    const addView = document.querySelector(".add-order-view");
    const returnView = document.querySelector(".return-order-view");

    const radios = document.querySelectorAll('input[name="orderType"]');

    const defaultAdd = document.querySelector('input[name="orderType"][value="ADD"]');
    if (defaultAdd) {
        defaultAdd.checked = true;
        addView.style.display = "block";
        returnView.style.display = "none";
    }

    radios.forEach(radio => {
        radio.addEventListener("change", () => {
            if (radio.value === "ADD") {
                addView.style.display = "block";
                returnView.style.display = "none";
            } else {
                addView.style.display = "none";
                returnView.style.display = "block";
            }

            const addContainer = document.querySelector(".all-add");
            const firstExtra = addContainer.querySelector(".add-extra");

            // 모든 add-extra 삭제
            addContainer.innerHTML = "";

            // 첫 기본 add-extra 다시 추가 (원본을 클론해서 만드는 방식)
            const cloned = firstExtra.cloneNode(true);
            addContainer.appendChild(cloned);

            const inputNum = cloned.querySelectorAll("input[type='number']");
            inputNum.forEach(input => input.value = 0);
            const inputText = cloned.querySelectorAll("input[type='text']");
            inputText.forEach(input => input.value = "");

            const selects = cloned.querySelectorAll("select");
            selects.forEach(sel => sel.selectedIndex = 0);

            document.querySelector(".addition").innerText = "0";
            document.querySelector(".takeback").innerText = "0";

            applySpinnerEvents(addContainer);
        });
    });
    const addContainer = document.querySelector(".all-add");
    const addButton = document.querySelector(".book-add");

    addButton.addEventListener("click", () => {

        const extraTemplate = `
            <div class="add-extra">
                <div class="basic-select">
                    <select>
                        <option value="">단계 선택</option>
                    </select>
                </div>
                <div class="basic-select">
                    <select>
                        <option value="">권수 선택</option>
                    </select>
                </div>
                <div class="custom-spinner">
                    <input type="number" min="0" max="100" step="1" value="0">
                    <div class="spinner-buttons">
                        <button type="button" class="increment-btn">▲</button>
                        <button type="button" class="decrement-btn">▼</button>
                    </div>
                </div>
                <div class="reason-box">
                    <div>사유 입력:</div>
                    <input type="text" placeholder="사유를 입력해주세요.">
                </div>
            </div>
        `;

        addContainer.insertAdjacentHTML("beforeend", extraTemplate);

        const lastExtra = addContainer.lastElementChild;

        applySpinnerEvents(lastExtra);
    });

    function applySpinnerEvents(root) {
        const spinners = root.querySelectorAll(".custom-spinner");

        spinners.forEach(spinner => {
            const input = spinner.querySelector("input[type='number']");
            const incBtn = spinner.querySelector(".increment-btn");
            const decBtn = spinner.querySelector(".decrement-btn");

            incBtn?.addEventListener("click", () => {
                input.value = Number(input.value || 0) + 1;
            });

            decBtn?.addEventListener("click", () => {
                const now = Number(input.value || 0);
                input.value = now > 0 ? now - 1 : 0;
            });

            input?.addEventListener("input", () => {
                if (input.value === "" || isNaN(input.value)) input.value = 0;
            });
        });
    }
});