document.addEventListener('DOMContentLoaded', () => {

    const rawCodes = document.getElementById("classCodes")?.value;
    const rawUnits = document.getElementById("classUnits")?.value;
    if (!rawCodes || !rawUnits) return;

    const classCodes = JSON.parse(rawCodes);
    const classUnits = JSON.parse(rawUnits);

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
        if (!date) return;

        const year = date.getFullYear();
        const month = date.getMonth() + 1;

        const span = display.querySelector(".month-text");
        if (span) {
            span.textContent = `${year}년 ${month}월`;
        }

        loadReorderList(year, month);

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

            addContainer.innerHTML = "";

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

            attachSelectEvents(cloned);
            applySpinnerEvents(addContainer);
        });
    });

    const addContainer = document.querySelector(".all-add");
    const addButton = document.querySelector(".book-add");

    addButton.addEventListener("click", () => {

        const extraTemplate = `
            <div class="add-extra">
                <div class="basic-select">
                    <select name="bookStep">
                        <option value="">단계 선택</option>
                    </select>
                </div>
                <div class="basic-select" >
                    <select name="bookChoice">
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
        attachSelectEvents(lastExtra);
        applySpinnerEvents(lastExtra);
    });


    // 스피너
    function applySpinnerEvents(root) {
        const spinners = root.querySelectorAll(".custom-spinner");

        spinners.forEach(spinner => {
            const input = spinner.querySelector("input[type='number']");
            const incBtn = spinner.querySelector(".increment-btn");
            const decBtn = spinner.querySelector(".decrement-btn");

            incBtn?.addEventListener("click", () => {
                input.value = Number(input.value || 0) + 1;
                updateTotals();
            });

            decBtn?.addEventListener("click", () => {
                const now = Number(input.value || 0);
                input.value = now > 0 ? now - 1 : 0;
                updateTotals();
            });

            input?.addEventListener("input", () => {
                if (input.value === "" || isNaN(input.value)) input.value = 0;
            });
            updateTotals();
        });
    }

    function updateTotals() {
        const orderType = document.querySelector('input[name="orderType"]:checked').value;

        let total = 0;

        document.querySelectorAll(".all-add .add-extra").forEach(box => {
            const count = Number(box.querySelector("input[type='number']").value || 0);
            total += count;
        });

        if (orderType === "ADD") {
            document.querySelector(".addition").innerText = total;
            document.querySelector(".takeback").innerText = 0;
        } else {
            document.querySelector(".takeback").innerText = total;
            document.querySelector(".addition").innerText = 0;
        }
    }

    function attachSelectEvents(extraBox) {
        const stepSelect = extraBox.querySelector("select[name='bookStep']");
        const choiceSelect = extraBox.querySelector("select[name='bookChoice']");

        if (!stepSelect || !choiceSelect) return;

        // 단계 목록 초기화
        fillStepSelect(stepSelect, classCodes);

        // 단계 변경 → 권수 목록 로딩
        stepSelect.addEventListener("change", () => {
            const classKey = stepSelect.value;
            fillChoiceSelect(choiceSelect, classUnits, classKey);
        });
    }

    function fillStepSelect(stepSelect, classCodes) {
        stepSelect.options.length = 0;
        stepSelect.add(new Option("단계 선택", ""));

        classCodes.forEach(c => {
            stepSelect.add(new Option(c.className, c.classKey));
        });
    }

    function fillChoiceSelect(choiceSelect, classUnits, classKey) {
        choiceSelect.options.length = 0;
        choiceSelect.add(new Option("권수 선택", ""));

        if (!classKey || !classUnits[classKey]) return;

        let units = [...classUnits[classKey]];

        units.forEach(u => {
            choiceSelect.add(new Option(u.unitName, u.unitKey));
        });
    }

    applySpinnerEvents(document.querySelector(".all-add"));
    attachSelectEvents(document.querySelector(".add-extra"));

    // ======================== 오른쪽 주문 내역 불러오기 ======================== //
    async function loadReorderList(year, month) {
        const body = {
            yy: year,
            mm: month
        };

        try {
            const res = await fetch("/manage/reorder/list", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(body)
            });

            const data = await res.json();

            renderReorderRows(data.response);

        } catch (e) {
            console.error("loadReorderList Error:", e);
        }
    }

    function renderReorderRows(list) {
        const tbody = document.querySelector("#reorder-tbody");
        tbody.innerHTML = "";

        list.forEach(item => {
            const html = `
            <tr data-class-key="${item.classKey}" data-unit-key="${item.unitKey}">
                <td>${item.reorderType === 'add' ? '추가주문' : '반품'}</td>
                <td>${item.className}</td>
                <td>${item.unitName}</td>
                <td>${item.count}</td>
                <td>${item.reason}</td>
                <td>${item.createdAt}</td>
                <td>${item.confirmed === 'checked' ? '승인' : '미승인'}</td>
                <td>아이콘</td>
            </tr>
        `;

            tbody.insertAdjacentHTML("beforeend", html);
        });
    }

    document.querySelector("#saveReorderBtn").addEventListener("click", async () => {

        const orderType = document.querySelector('input[name="orderType"]:checked').value;
        const extras = Array.from(document.querySelectorAll(".all-add .add-extra")).map(box => {
            return {
                classKey: box.querySelector("select[name='bookStep']").value,
                unitKey: box.querySelector("select[name='bookChoice']").value,
                count: Number(box.querySelector("input[type='number']").value),
                reason: box.querySelector("input[type='text']").value
            };
        });

        const body = {
            reorderType: orderType,   // ADD or RETURN
            items: extras
        };

        try {
            const res = await fetch("/manage/reorder/save", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(body)
            });

            const data = await res.json();
            if (data.response === '저장되었습니다.') {
                alert(data.response);
                window.location.reload();
            } else {
                alert('저장을 실패했습니다.');
            }
        } catch (e) {
            console.error("saveReorder Error:", e);
        }
    });

});