document.addEventListener('DOMContentLoaded', () => {

    const rawCodes = document.getElementById("classCodes")?.value;
    const rawUnits = document.getElementById("classUnits")?.value;
    if (!rawCodes || !rawUnits) return;

    const classCodes = JSON.parse(rawCodes);
    const classUnits = JSON.parse(rawUnits);

    let returnOptions = [];

    initAllMonthPickers();
    attachCancelEvents();

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

            const old = display.querySelector(".month-text");
            if (old) old.remove();

            display.insertAdjacentHTML("afterbegin", `<span class="month-text">${year}년 ${month}월</span>`);

        } catch (e) {
            console.log('initCurrentMonth Error', e);
        }
    }

    function onMonthChange(display, monthInput) {
        const date = new Date(monthInput.value);
        if (isNaN(date.getTime())) return;

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');

        const span = display.querySelector(".month-text");
        if (span) {
            span.textContent = `${year}년 ${Number(month)}월`;
        }

        if (display.closest(".search-calendar")) {
            loadReorderList(year, month);
        }

        if (display.closest(".save-calendar")) {
            const orderType = document.querySelector('input[name="orderType"]:checked')?.value;
            if (orderType === "RETURN") {
                loadReturnOptionsAndRebuild(String(year), month);
            }
        }
    }

    async function loadReturnOptions(yy, mm) {
        try {
            const res = await fetch('/manage/reorder/return-options', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({yy, mm})
            });
            const data = await res.json();
            returnOptions = data.response || [];
        } catch (e) {
            console.error('loadReturnOptions Error:', e);
            returnOptions = [];
        }
    }

    async function loadReturnOptionsAndRebuild(yy, mm) {
        await loadReturnOptions(yy, mm);
        rebuildReturnForm();
    }

    function rebuildReturnForm() {
        const addContainer = document.querySelector(".all-add");
        const firstExtra = addContainer.querySelector(".add-extra");

        addContainer.innerHTML = "";

        const cloned = firstExtra ? firstExtra.cloneNode(true) : createExtraTemplate();
        addContainer.appendChild(cloned);

        resetExtraInputs(cloned);
        attachReturnSelectEvents(cloned);
        applySpinnerEvents(addContainer);
    }

    function resetExtraInputs(box) {
        box.querySelectorAll("input[type='number']").forEach(i => i.value = 0);
        box.querySelectorAll("input[type='text']").forEach(i => i.value = "");
        box.querySelectorAll("select").forEach(s => s.selectedIndex = 0);
        const hint = box.querySelector('.max-hint');
        if (hint) hint.textContent = '';
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
        radio.addEventListener("change", async () => {
            if (radio.value === "ADD") {
                addView.style.display = "block";
                returnView.style.display = "none";

                const addContainer = document.querySelector(".all-add");
                const firstExtra = addContainer.querySelector(".add-extra");
                addContainer.innerHTML = "";
                const cloned = firstExtra ? firstExtra.cloneNode(true) : createExtraTemplate();
                addContainer.appendChild(cloned);

                resetExtraInputs(cloned);
                attachSelectEvents(cloned);
                applySpinnerEvents(addContainer);

            } else {
                addView.style.display = "none";
                returnView.style.display = "block";

                const saveCalendar = document.querySelector(".save-calendar .hidden-picker");
                if (saveCalendar?.value) {
                    const [yy, mmRaw] = saveCalendar.value.split("-");
                    await loadReturnOptions(yy, mmRaw.padStart(2, '0'));
                }
                rebuildReturnForm();
            }

            document.querySelector(".addition").innerText = "0";
            document.querySelector(".takeback").innerText = "0";
        });
    });

    const addContainer = document.querySelector(".all-add");
    const addButton = document.querySelector(".book-add");

    addButton.addEventListener("click", () => {
        const orderType = document.querySelector('input[name="orderType"]:checked')?.value;
        const extraEl = createExtraTemplate();
        addContainer.appendChild(extraEl);

        if (orderType === "RETURN") {
            attachReturnSelectEvents(extraEl);
        } else {
            attachSelectEvents(extraEl);
        }
        applySpinnerEvents(addContainer);
    });

    function createExtraTemplate() {
        const div = document.createElement('div');
        div.className = 'add-extra';
        div.innerHTML = `
            <div class="basic-select">
                <select name="bookStep">
                    <option value="">단계 선택</option>
                </select>
            </div>
            <div class="basic-select">
                <select name="bookChoice">
                    <option value="">호수 선택</option>
                </select>
            </div>
            <div class="custom-spinner">
                <input type="number" min="0" max="100" step="1" value="0">
            </div>
            <div class="reason-box">
                <div>사유 입력:</div>
                <input type="text" placeholder="사유를 입력해주세요.">
            </div>
        `;
        return div;
    }

    function applySpinnerEvents(root) {
        const inputs = root.querySelectorAll(".custom-spinner input[type='number']");

        inputs.forEach(input => {
            input.addEventListener("input", () => {
                const val = parseInt(input.value, 10);
                const max = Number(input.max) || 100;
                if (isNaN(val) || val < 0) input.value = 0;
                else if (val > max) input.value = max;
                updateTotals();
            });

            input.addEventListener("keydown", e => {
                if (["e", "E", "+", "-", "."].includes(e.key)) e.preventDefault();
            });

            updateTotals();
        });
    }

    // 총합
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

    // ── 추가 모드 select ──
    function attachSelectEvents(extraBox) {
        const stepSelect = extraBox.querySelector("select[name='bookStep']");
        const choiceSelect = extraBox.querySelector("select[name='bookChoice']");

        if (!stepSelect || !choiceSelect) return;

        fillStepSelect(stepSelect, classCodes);

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
        choiceSelect.add(new Option("호수 선택", ""));

        if (!classKey || !classUnits[classKey]) return;

        let units = [...classUnits[classKey]];

        units.forEach(u => {
            choiceSelect.add(new Option(u.unitName, u.unitKey));
        });
    }

    // ── 반품 모드 select ──
    function attachReturnSelectEvents(extraBox) {
        const stepSelect = extraBox.querySelector("select[name='bookStep']");
        const choiceSelect = extraBox.querySelector("select[name='bookChoice']");
        const spinner = extraBox.querySelector("input[type='number']");

        if (!stepSelect || !choiceSelect) return;

        fillReturnStepSelect(stepSelect);

        stepSelect.addEventListener("change", () => {
            fillReturnChoiceSelect(choiceSelect, stepSelect.value, spinner);
        });

        choiceSelect.addEventListener("change", () => {
            applyReturnMax(stepSelect.value, choiceSelect.value, spinner);
        });
    }

    function fillReturnStepSelect(stepSelect) {
        stepSelect.options.length = 0;
        stepSelect.add(new Option("단계 선택", ""));

        const seen = new Set();
        returnOptions.forEach(o => {
            if (!seen.has(o.classKey)) {
                seen.add(o.classKey);
                stepSelect.add(new Option(o.className, o.classKey));
            }
        });
    }

    function fillReturnChoiceSelect(choiceSelect, classKey, spinner) {
        choiceSelect.options.length = 0;
        choiceSelect.add(new Option("호수 선택", ""));

        if (!classKey) return;

        const units = returnOptions.filter(o => o.classKey === classKey);
        units.forEach(o => {
            const label = o.maxReturnCount > 0
                ? `${o.unitName} (최대 ${o.maxReturnCount}권)`
                : `${o.unitName} (반품 불가)`;
            const opt = new Option(label, o.unitKey);
            if (o.maxReturnCount <= 0) opt.disabled = true;
            choiceSelect.add(opt);
        });

        if (spinner) {
            spinner.max = 100;
            spinner.value = 0;
        }
        updateTotals();
    }

    function applyReturnMax(classKey, unitKey, spinner) {
        if (!spinner || !classKey || !unitKey) return;

        const option = returnOptions.find(o => o.classKey === classKey && o.unitKey === unitKey);
        if (option) {
            spinner.max = option.maxReturnCount;
            if (Number(spinner.value) > option.maxReturnCount) {
                spinner.value = option.maxReturnCount;
            }
            updateTotals();
        }
    }

    applySpinnerEvents(document.querySelector(".all-add"));
    attachSelectEvents(document.querySelector(".add-extra"));

    // ======================== 오른쪽 주문 내역 불러오기 ======================== //
    async function loadReorderList(year, month) {
        const body = {
            yy: year, mm: month
        };

        try {
            const res = await fetch("/manage/reorder/list", {
                method: "POST", headers: {"Content-Type": "application/json"}, body: JSON.stringify(body)
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
        const confirmedTextMap = {
            checked: '승인', unchecked: '미승인', admin_cancel: '관리자 취소', user_cancel: '사용자 취소'
        };

        if (!list || list.length === 0) {
            tbody.innerHTML = `
        <tr>
            <td colspan="8" style="text-align:center;">
                추가/반품이 없습니다.
            </td>
        </tr>
    `;
            return;
        }

        list.forEach(item => {
            const canCancel = item.confirmed === 'unchecked';
            const cancelBtn = canCancel
                ? `<button class="btn-cancel" data-id="${item.id}">취소</button>`
                : '-';

            const html = `
            <tr data-class-key="${item.classKey}" data-unit-key="${item.unitKey}" data-class-id="${item.id}">
                <td>${item.reorderType === 'ADD' ? '추가주문' : '반품'}</td>
                <td>${item.className}</td>
                <td>${item.unitName}</td>
                <td>${item.count}</td>
                <td>${item.reason}</td>
                <td>${item.createdAt}</td>
                <td>${confirmedTextMap[item.confirmed] ?? '-'}</td>
                <td>${cancelBtn}</td>
            </tr>
        `;

            tbody.insertAdjacentHTML("beforeend", html);

            attachCancelEvents();
        });
    }

    function attachCancelEvents() {
        document.querySelectorAll('.btn-cancel').forEach(button => {
            button.addEventListener('click', async function () {
                const reorderId = this.dataset.id;

                if (!confirm('정말 취소하시겠습니까?')) {
                    return;
                }

                try {
                    const res = await fetch('/manage/reorder/cancel', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({id: reorderId})
                    });

                    const data = await res.json();
                    if (data.response === '0000') {
                        alert('취소되었습니다.');

                        const searchCalendar = document.querySelector(".search-calendar .hidden-picker");
                        if (searchCalendar.value) {
                            const date = new Date(searchCalendar.value);
                            const year = date.getFullYear();
                            const month = String(date.getMonth() + 1).padStart(2, '0');
                            loadReorderList(year, month);
                        }
                    } else {
                        alert('취소 실패했습니다.');
                    }
                } catch (e) {
                    console.error('cancelReorder Error:', e);
                    alert('취소 중 오류가 발생했습니다.');
                }
            });
        });
    }

    document.querySelector("#saveReorderBtn").addEventListener("click", async () => {

        const saveCalendar = document.querySelector(".save-calendar .hidden-picker");
        if (!saveCalendar.value) {
            alert("년월을 선택해 주세요.");
            return;
        }
        const [yy, mmRaw] = saveCalendar.value.split("-");
        const mm = mmRaw.padStart(2, "0");

        const orderType = document.querySelector('input[name="orderType"]:checked').value;
        if (!orderType) {
            alert("주문 유형을 선택해 주세요.");
            return;
        }
        const extras = Array.from(document.querySelectorAll(".all-add .add-extra")).map(box => ({
            classKey: box.querySelector("select[name='bookStep']").value,
            unitKey: box.querySelector("select[name='bookChoice']").value,
            count: Number(box.querySelector("input[type='number']").value),
            reason: box.querySelector("input[type='text']").value
        }));

        if (extras.length === 0) {
            alert("추가/반품 항목을 하나 이상 입력해 주세요.");
            return;
        }

        for (let i = 0; i < extras.length; i++) {
            const item = extras[i];

            if (!item.classKey) {
                alert(`(${i + 1}번째 항목) 단계가 선택되지 않았습니다.`);
                return;
            }
            if (!item.unitKey) {
                alert(`(${i + 1}번째 항목) 호수가 선택되지 않았습니다.`);
                return;
            }

            if (!item.reason) {
                alert(`(${i + 1}번째 항목) 사유를 입력해 주세요.`);
                return;
            }

            if (!Number.isInteger(item.count) || item.count <= 0) {
                alert(`(${i + 1}번째 항목) 권수는 1권 이상이어야 합니다.`);
                return;
            }

            // 반품 모드: 프론트 max 재검증
            if (orderType === "RETURN") {
                const option = returnOptions.find(o => o.classKey === item.classKey && o.unitKey === item.unitKey);
                if (option && item.count > option.maxReturnCount) {
                    alert(`(${i + 1}번째 항목) 반품 가능 수량(${option.maxReturnCount}권)을 초과했습니다.`);
                    return;
                }
            }
        }

        const totalCount = extras.reduce((sum, item) => sum + item.count, 0);
        if (totalCount === 0) {
            alert("총 권수는 0이 될 수 없습니다.");
            return;
        }

        const body = {
            yy: Number(yy), mm: mm, reorderType: orderType, items: extras
        };
        try {
            const res = await fetch("/manage/reorder/save", {
                method: "POST", headers: {"Content-Type": "application/json"}, body: JSON.stringify(body)
            });

            const data = await res.json();
            if (data.response === '저장되었습니다.') {
                alert(data.response);
                window.location.reload();
            } else {
                alert(data.msg || '저장을 실패했습니다.');
            }
        } catch (e) {
            console.error("saveReorder Error:", e);
        }
    });
});
