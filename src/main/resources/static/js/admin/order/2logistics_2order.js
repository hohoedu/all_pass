document.addEventListener('DOMContentLoaded', () => {

    // ────────────────────────────────────────
    // DOM 참조
    // ────────────────────────────────────────
    const appShell = document.getElementById('appShell');
    const navButtons = document.querySelectorAll('.nav-icon');
    const depthMenus = document.querySelectorAll('.depth-menu');

    const orderManualModal = document.getElementById('order-manual-modal');
    const itemTableBody = document.getElementById('modal-item-table-body');
    const addItemBtn = document.getElementById('modal-add-item-btn');
    const totalAmountEl = document.getElementById('modal-total-amount');

    const orderAddModal = document.getElementById('order-add-modal');
    const addItemTableBody = document.getElementById('add-modal-item-table-body');
    const addModalAddItemBtn = document.getElementById('add-modal-add-item-btn');
    const addModalTotalAmountEl = document.getElementById('add-modal-total-amount');

    const statementTabs = document.getElementById('statementTabs');
    const statementPrevBtn = document.getElementById('statementPrevBtn');
    const statementNextBtn = document.getElementById('statementNextBtn');

    const monthPicker = document.getElementById('monthPicker');
    const monthDisplay = document.getElementById('monthDisplay');
    const deadlinePicker = document.getElementById('deadlinePicker');
    const deadlineDisplay = document.getElementById('deadlineDisplay');

    // ────────────────────────────────────────
    // 상태
    // ────────────────────────────────────────
    let openedMenu = null;
    let currentCenterInfo = null;
    let lastAggregateSegment = null;
    let lastAggregateCenterCodes = [];
    const checkedCenters = new Set();

    // ────────────────────────────────────────
    // 유틸리티
    // ────────────────────────────────────────
    const formatNumber = (n) => Math.abs(n).toLocaleString('ko-KR');

    const formatBizNum = (val) => {
        const n = val.replace(/\D/g, '');
        return n.length === 10 ? `${n.slice(0, 3)}-${n.slice(3, 5)}-${n.slice(5)}` : val;
    };

    const openPicker = (picker) => {
        if (typeof picker.showPicker === 'function') picker.showPicker();
        else picker.focus();
    };

    // ────────────────────────────────────────
    // 사이드바
    // ────────────────────────────────────────
    navButtons.forEach((button) => {
        button.addEventListener('click', () => {
            const target = button.dataset.menu;
            const isSameOpenMenu = openedMenu === target && appShell.classList.contains('depth-open');

            if (isSameOpenMenu) {
                appShell.classList.remove('depth-open');
                button.classList.remove('active');
                openedMenu = null;
                return;
            }

            openedMenu = target;
            appShell.classList.add('depth-open');
            navButtons.forEach((item) => item.classList.toggle('active', item === button));
            depthMenus.forEach((menu) => menu.classList.toggle('active', menu.dataset.depth === target));
        });
    });

    // ────────────────────────────────────────
    // 신규 수기 명세서 등록 모달
    // ────────────────────────────────────────
    document.getElementById('order-manual')?.addEventListener('click', () => {
        document.getElementById('modal-invoice-yymm').value = monthPicker.value;
        orderManualModal.classList.add('show');
    });

    document.getElementById('modal-close-btn')?.addEventListener('click', () => {
        orderManualModal.classList.remove('show');
    });

    function createItemRow() {
        const tr = document.createElement('tr');
        tr.className = 'modal-item-row';
        tr.innerHTML = `
            <td><input type="date" class="item-order-date"></td>
            <td><input type="text" class="item-name"></td>
            <td><input type="text" class="item-spec"></td>
            <td><input type="number" class="item-qty" min="0"></td>
            <td><input type="number" class="item-price" min="0"></td>
            <td><input type="text" class="item-amount" readonly></td>
            <td><input type="text" class="item-note"></td>
            <td><input type="date" class="item-approve-date"></td>
            <td>
                <button type="button" class="item-delete-btn" aria-label="행 삭제">
                    <i class="fa-regular fa-trash-can"></i>
                </button>
            </td>
        `;
        return tr;
    }

    function calculateRowAmount(row) {
        const qty = Number(row.querySelector('.item-qty').value) || 0;
        const price = Number(row.querySelector('.item-price').value) || 0;
        const amount = qty * price;
        row.querySelector('.item-amount').value = amount.toLocaleString('ko-KR');
        return amount;
    }

    function calculateTotalAmount() {
        let total = 0;
        itemTableBody.querySelectorAll('.modal-item-row').forEach((row) => {
            const qty = Number(row.querySelector('.item-qty').value) || 0;
            const price = Number(row.querySelector('.item-price').value) || 0;
            total += qty * price;
        });
        totalAmountEl.textContent = total.toLocaleString('ko-KR') + '원';
    }

    addItemBtn?.addEventListener('click', () => {
        itemTableBody.appendChild(createItemRow());
    });

    itemTableBody?.addEventListener('input', (e) => {
        if (e.target.classList.contains('item-qty') || e.target.classList.contains('item-price')) {
            const row = e.target.closest('.modal-item-row');
            calculateRowAmount(row);
            calculateTotalAmount();
        }
    });

    itemTableBody?.addEventListener('click', (e) => {
        const deleteBtn = e.target.closest('.item-delete-btn');
        if (!deleteBtn) return;

        const rows = itemTableBody.querySelectorAll('.modal-item-row');
        if (rows.length <= 1) {
            deleteBtn.closest('.modal-item-row').querySelectorAll('input').forEach((input) => (input.value = ''));
            calculateTotalAmount();
            return;
        }

        deleteBtn.closest('.modal-item-row').remove();
        calculateTotalAmount();
    });

    document.getElementById('modal-save-btn')?.addEventListener('click', async () => {
        const yymm = document.getElementById('modal-invoice-yymm').value;
        if (!yymm) {
            alert('거래연월을 입력해주세요.');
            return;
        }
        const [yy, mm] = yymm.split('-');

        const title = document.getElementById('modal-invoice-title').value.trim();
        const centerCode = document.getElementById('modal-invoice-center-code').value;
        const writerName = document.getElementById('modal-invoice-writer').value.trim();

        if (!title || !centerCode || !writerName) {
            alert('기본 정보를 모두 입력해주세요.');
            return;
        }

        const items = Array.from(itemTableBody.querySelectorAll('.modal-item-row'))
            .map((row) => ({
                orderDate: row.querySelector('.item-order-date').value,
                classKey: row.querySelector('.item-name').value.trim(),
                unitKey: row.querySelector('.item-spec').value.trim(),
                qty: row.querySelector('.item-qty').value,
                price: row.querySelector('.item-price').value,
                amount: String(calculateRowAmount(row)),
                note: row.querySelector('.item-note').value.trim(),
                approveDate: row.querySelector('.item-approve-date').value
            }))
            .filter((item) => item.classKey !== '');

        if (items.length === 0) {
            alert('품목을 한 건 이상 입력해주세요.');
            return;
        }

        const totalAmount = String(items.reduce((sum, item) => sum + Number(item.amount), 0));

        try {
            const res = await fetch('/logis/order/manual/save', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({yy, mm, title, centerCode, writerName, totalAmount, items})
            });
            const data = await res.json();

            if (data.success) {
                alert('명세서가 저장되었습니다.');
                orderManualModal.classList.remove('show');
            } else {
                alert('저장에 실패했습니다.');
            }
        } catch (e) {
            alert('저장 중 오류가 발생했습니다.');
        }
    });

    // ────────────────────────────────────────
    // 현재 명세서에 수기로 추가 모달
    // ────────────────────────────────────────
    let addModalMode = 'regular';
    let addModalManualId = null;
    let regularOptionsCache = null;

    const addRegularItemTableBody = document.getElementById('add-regular-item-table-body');
    const addRegularAddItemBtn = document.getElementById('add-regular-add-item-btn');

    function buildOptionsHtml(list, valueKey, textKey, placeholder) {
        const opts = (list || []).map(item => `<option value="${item[valueKey]}">${item[textKey]}</option>`).join('');
        return `<option value="">${placeholder}</option>${opts}`;
    }

    function createRegularItemRow() {
        const tr = document.createElement('tr');
        tr.className = 'modal-item-row';
        const classOptionsHtml = buildOptionsHtml(regularOptionsCache.classCodes, 'classKey', 'className', '품명 선택');
        const userOptionsHtml = buildOptionsHtml(regularOptionsCache.users, 'userCode', 'userName', '선생님 선택');

        tr.innerHTML = `
        <td><select class="item-class-key modal-select">${classOptionsHtml}</select></td>
        <td><select class="item-unit-key modal-select" disabled><option value="">품명 먼저 선택</option></select></td>
        <td><input type="text" class="item-price" value="15,000" readonly></td>
        <td><input type="text" class="item-qty"></td>
        <td><input type="text" class="item-amount" readonly></td>
        <td><select class="item-user-code modal-select">${userOptionsHtml}</select></td>
        <td><input type="text" class="item-reason"></td>
        <td>
            <button type="button" class="item-delete-btn" aria-label="행 삭제">
                <i class="fa-regular fa-trash-can"></i>
            </button>
        </td>
    `;
        return tr;
    }

    function calculateRegularRowAmount(row) {
        const qty = Number(row.querySelector('.item-qty').value) || 0;
        const amount = 15000 * qty;
        row.querySelector('.item-amount').value = amount.toLocaleString('ko-KR');
        return amount;
    }

    function calculateRegularTotalAmount() {
        let total = 0;
        addRegularItemTableBody.querySelectorAll('.modal-item-row').forEach((row) => {
            const qty = Number(row.querySelector('.item-qty').value) || 0;
            total += 15000 * qty;
        });
        addModalTotalAmountEl.textContent = total.toLocaleString('ko-KR') + '원';
    }

    async function fillRegularRow(row, item) {
        const classSelect = row.querySelector('.item-class-key');
        const unitSelect = row.querySelector('.item-unit-key');
        const userSelect = row.querySelector('.item-user-code');

        classSelect.value = item.classKey || '';

        if (item.classKey) {
            const res = await fetch('/logis/order/unit-codes-by-class', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({classKey: item.classKey})
            });
            const units = (await res.json()).response;
            unitSelect.innerHTML = buildOptionsHtml(units, 'unitKey', 'unitName', '규격 선택');
            unitSelect.disabled = false;
            unitSelect.value = item.unitKey || '';
        }

        userSelect.value = item.userCode || '';
        row.querySelector('.item-qty').value = item.qty || '';
        row.querySelector('.item-reason').value = item.reason || '';
        calculateRegularRowAmount(row);
    }

    addRegularItemTableBody?.addEventListener('input', (e) => {
        if (e.target.classList.contains('item-qty')) {
            calculateRegularRowAmount(e.target.closest('.modal-item-row'));
            calculateRegularTotalAmount();
        }
    });

    addRegularItemTableBody?.addEventListener('click', (e) => {
        const deleteBtn = e.target.closest('.item-delete-btn');
        if (!deleteBtn) return;

        const rows = addRegularItemTableBody.querySelectorAll('.modal-item-row');
        deleteBtn.closest('.modal-item-row').remove();

        if (rows.length <= 1) {
            addRegularItemTableBody.appendChild(createRegularItemRow());
        }
        calculateRegularTotalAmount();
    });

    addRegularAddItemBtn?.addEventListener('click', () => {
        addRegularItemTableBody.appendChild(createRegularItemRow());
    });

    addRegularItemTableBody?.addEventListener('input', (e) => {
        if (e.target.classList.contains('item-qty')) {
            calculateRegularRowAmount(e.target.closest('.modal-item-row'));
        }
    });

    addRegularItemTableBody?.addEventListener('change', async (e) => {
        if (!e.target.classList.contains('item-class-key')) return;

        const row = e.target.closest('.modal-item-row');
        const unitSelect = row.querySelector('.item-unit-key');
        const classKey = e.target.value;

        if (!classKey) {
            unitSelect.innerHTML = '<option value="">품명 먼저 선택</option>';
            unitSelect.disabled = true;
            return;
        }

        const res = await fetch('/logis/order/unit-codes-by-class', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({classKey})
        });
        const data = await res.json();

        unitSelect.innerHTML = buildOptionsHtml(data.response, 'unitKey', 'unitName', '규격 선택');
        unitSelect.disabled = false;
    });

    document.getElementById('order-add')?.addEventListener('click', async () => {
        const activeCenter = document.querySelector('.center-item.active');
        const centerCode = activeCenter ? activeCenter.dataset.centerCode : null;

        if (!centerCode) {
            alert('먼저 센터를 선택해주세요.');
            return;
        }

        const activeCard = document.querySelector('.statement-card.active');
        const manualId = activeCard ? activeCard.dataset.manualId : null;

        if (manualId) {
            // ── 수기 명세서 편집 ──
            addModalMode = 'manual';
            addModalManualId = manualId;
            document.getElementById('add-modal-title').textContent = `${activeCard.querySelector('strong').textContent} 수정`;
            document.getElementById('add-regular-section').style.display = 'none';
            document.getElementById('add-manual-section').style.display = '';

            const res = await fetch('/logis/order/manual/items', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({manualId})
            });
            const list = (await res.json()).response || [];

            addItemTableBody.innerHTML = '';
            if (list.length === 0) {
                addItemTableBody.appendChild(createAddItemRow());
            } else {
                list.forEach((item) => {
                    const row = createAddItemRow();
                    row.querySelector('.item-order-date').value = item.orderDate || '';
                    row.querySelector('.item-name').value = item.classKey || '';
                    row.querySelector('.item-spec').value = item.unitKey || '';
                    row.querySelector('.item-qty').value = item.qty || '';
                    row.querySelector('.item-price').value = item.price || '';
                    row.querySelector('.item-note').value = item.note || '';
                    row.querySelector('.item-approve-date').value = item.approveDate || '';
                    calculateAddRowAmount(row);
                    addItemTableBody.appendChild(row);
                });
            }
            calculateAddTotalAmount();
        } else {
            // ── 정기 주문 명세서 편집 ──
            addModalMode = 'regular';
            addModalManualId = null;
            document.getElementById('add-modal-title').textContent = '정기 주문 명세서 수정';
            document.getElementById('add-manual-section').style.display = 'none';
            document.getElementById('add-regular-section').style.display = '';

            const [year, month] = monthPicker.value.split('-');

            const optRes = await fetch('/logis/order/manual-add/options', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({centerCode})
            });
            regularOptionsCache = (await optRes.json()).response;

            const listRes = await fetch('/logis/order/reorder/sugi-list', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({year, month, centerCode})
            });
            const list = (await listRes.json()).response || [];

            addRegularItemTableBody.innerHTML = '';
            if (list.length === 0) {
                addRegularItemTableBody.appendChild(createRegularItemRow());
            } else {
                for (const item of list) {
                    const row = createRegularItemRow();
                    addRegularItemTableBody.appendChild(row);
                    await fillRegularRow(row, item);
                }
            }
            calculateRegularTotalAmount();
        }

        orderAddModal.classList.add('show');
    });

    document.getElementById('add-modal-close-btn')?.addEventListener('click', () => {
        orderAddModal.classList.remove('show');
    });

    function createAddItemRow() {
        const tr = document.createElement('tr');
        tr.className = 'modal-item-row';
        tr.innerHTML = `
            <td><input type="date" class="item-order-date"></td>
            <td><input type="text" class="item-name"></td>
            <td><input type="text" class="item-spec"></td>
            <td><input type="text" class="item-qty"></td>
            <td><input type="text" class="item-price"></td>
            <td><input type="text" class="item-amount" readonly></td>
            <td><input type="text" class="item-note"></td>
            <td><input type="date" class="item-approve-date"></td>
            <td>
                <button type="button" class="item-delete-btn" aria-label="행 삭제">
                    <i class="fa-regular fa-trash-can"></i>
                </button>
            </td>
        `;
        return tr;
    }

    function calculateAddRowAmount(row) {
        const qty = Number(row.querySelector('.item-qty').value) || 0;
        const price = Number(row.querySelector('.item-price').value) || 0;
        const amount = qty * price;
        row.querySelector('.item-amount').value = amount.toLocaleString('ko-KR');
        return amount;
    }

    function calculateAddTotalAmount() {
        let total = 0;
        addItemTableBody.querySelectorAll('.modal-item-row').forEach((row) => {
            const qty = Number(row.querySelector('.item-qty').value) || 0;
            const price = Number(row.querySelector('.item-price').value) || 0;
            total += qty * price;
        });
        addModalTotalAmountEl.textContent = total.toLocaleString('ko-KR') + '원';
    }

    addModalAddItemBtn?.addEventListener('click', () => {
        addItemTableBody.appendChild(createAddItemRow());
    });

    addItemTableBody?.addEventListener('input', (e) => {
        if (e.target.classList.contains('item-qty') || e.target.classList.contains('item-price')) {
            const row = e.target.closest('.modal-item-row');
            calculateAddRowAmount(row);
            calculateAddTotalAmount();
        }
    });

    addItemTableBody?.addEventListener('click', (e) => {
        const deleteBtn = e.target.closest('.item-delete-btn');
        if (!deleteBtn) return;

        const rows = addItemTableBody.querySelectorAll('.modal-item-row');
        if (rows.length <= 1) {
            deleteBtn.closest('.modal-item-row').querySelectorAll('input').forEach((input) => (input.value = ''));
            calculateAddTotalAmount();
            return;
        }

        deleteBtn.closest('.modal-item-row').remove();
        calculateAddTotalAmount();
    });

    document.getElementById('add-modal-save-btn')?.addEventListener('click', async () => {
        if (addModalMode === 'regular') {
            const activeCenter = document.querySelector('.center-item.active');
            const centerCode = activeCenter.dataset.centerCode;
            const [year, month] = monthPicker.value.split('-');

            const items = Array.from(addRegularItemTableBody.querySelectorAll('.modal-item-row'))
                .map((row) => ({
                    classKey: row.querySelector('.item-class-key').value,
                    unitKey: row.querySelector('.item-unit-key').value,
                    qty: row.querySelector('.item-qty').value,
                    userCode: row.querySelector('.item-user-code').value,
                    reason: row.querySelector('.item-reason').value.trim()
                }))
                .filter((item) => item.classKey && item.unitKey && item.qty && item.userCode);

            if (items.length === 0) {
                if (!confirm('입력된 품목이 없습니다. 기존 추가분을 모두 삭제하시겠습니까?')) return;
            }

            try {
                const res = await fetch('/logis/order/reorder/manual-replace', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({year, month, centerCode, items})
                });
                const data = await res.json();

                if (data.success) {
                    alert('정기 주문 명세서가 수정되었습니다.');
                    orderAddModal.classList.remove('show');
                    fetchReorderList();
                } else {
                    alert('수정에 실패했습니다.');
                }
            } catch (e) {
                alert('수정 중 오류가 발생했습니다.');
            }
        } else {
            const items = Array.from(addItemTableBody.querySelectorAll('.modal-item-row'))
                .map((row) => ({
                    orderDate: row.querySelector('.item-order-date').value,
                    classKey: row.querySelector('.item-name').value.trim(),
                    unitKey: row.querySelector('.item-spec').value.trim(),
                    qty: row.querySelector('.item-qty').value,
                    price: row.querySelector('.item-price').value,
                    amount: String(calculateAddRowAmount(row)),
                    note: row.querySelector('.item-note').value.trim(),
                    approveDate: row.querySelector('.item-approve-date').value
                }))
                .filter((item) => item.classKey !== '');

            if (items.length === 0) {
                alert('품목을 한 건 이상 입력해주세요.');
                return;
            }

            try {
                const editedId = addModalManualId;
                const res = await fetch('/logis/order/manual/items/replace', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({manualId: editedId, items})
                });
                const data = await res.json();

                if (data.success) {
                    alert('수기 명세서가 수정되었습니다.');
                    orderAddModal.classList.remove('show');

                    await fetchManualList();
                    const card = statementTabs.querySelector(`.statement-card[data-manual-id="${editedId}"]`);
                    if (card) {
                        statementTabs.querySelectorAll('.statement-card').forEach((c) => c.classList.remove('active'));
                        card.classList.add('active');
                        fetchManualInvoice(editedId, card.querySelector('strong').textContent, card.querySelector('p').textContent);
                    }
                } else {
                    alert('수정에 실패했습니다.');
                }
            } catch (e) {
                alert('수정 중 오류가 발생했습니다.');
            }
        }
    });

    // ────────────────────────────────────────
    // 명세서 탭 (정기 + 수기)
    // ────────────────────────────────────────
    const updateStatementArrows = () => {
        const hasOverflow = statementTabs.scrollWidth > statementTabs.clientWidth + 1;
        statementPrevBtn.classList.toggle('hidden', !hasOverflow);
        statementNextBtn.classList.toggle('hidden', !hasOverflow);
    };

    const renderStatementCards = (manualList) => {
        statementTabs.querySelectorAll('.statement-card[data-manual-id]').forEach((card) => card.remove());

        manualList.forEach((manual) => {
            const card = document.createElement('button');
            card.className = 'statement-card';
            card.type = 'button';
            card.dataset.manualId = manual.manualId;
            card.innerHTML = `
                <i class="fa-regular fa-trash-can delete-icon"></i>
                <strong>${manual.title}</strong>
                <p>${manual.yy}. ${manual.mm}</p>
                <small>품목 ${manual.itemCount}건</small>
            `;
            statementTabs.appendChild(card);
        });

        updateStatementArrows();
    };

    statementPrevBtn.addEventListener('click', () => {
        statementTabs.scrollBy({left: -240, behavior: 'smooth'});
    });

    statementNextBtn.addEventListener('click', () => {
        statementTabs.scrollBy({left: 240, behavior: 'smooth'});
    });

    statementTabs.addEventListener('click', (e) => {
        const card = e.target.closest('.statement-card');
        if (!card) return;

        statementTabs.querySelectorAll('.statement-card').forEach((c) => c.classList.remove('active'));
        card.classList.add('active');

        const manualId = card.dataset.manualId;
        if (manualId) {
            fetchManualInvoice(manualId, card.querySelector('strong').textContent, card.querySelector('p').textContent);
        } else {
            fetchInvoice();
        }
    });

    statementTabs.addEventListener('click', async (e) => {
        const deleteIcon = e.target.closest('.delete-icon');
        if (deleteIcon) {
            const card = deleteIcon.closest('.statement-card');
            const manualId = card ? card.dataset.manualId : null;
            if (!manualId) return;

            if (!confirm('이 수기 명세서를 삭제하시겠습니까?')) return;

            try {
                const res = await fetch('/logis/order/manual/delete', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({manualId})
                });
                const data = await res.json();

                if (data.success) {
                    alert('삭제되었습니다.');
                    statementTabs.querySelectorAll('.statement-card').forEach((c, idx) => c.classList.toggle('active', idx === 0));
                    fetchInvoice();
                    fetchManualList();
                } else {
                    alert('삭제에 실패했습니다.');
                }
            } catch (err) {
                alert('삭제 중 오류가 발생했습니다.');
            }
            return;
        }

        const card = e.target.closest('.statement-card');
        if (!card) return;

        statementTabs.querySelectorAll('.statement-card').forEach((c) => c.classList.remove('active'));
        card.classList.add('active');

        const manualId = card.dataset.manualId;
        if (manualId) {
            fetchManualInvoice(manualId, card.querySelector('strong').textContent, card.querySelector('p').textContent);
        } else {
            fetchInvoice();
        }
    });

    const renderManualInvoiceTable = (list) => {
        const tbody = document.getElementById('invoiceTableBody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:#9a9da7;">데이터가 없습니다.</td></tr>`;
            document.getElementById('summaryOrder').textContent = '-';
            document.getElementById('summaryReturn').textContent = '-';
            document.getElementById('summaryAdd').textContent = '-';
            document.getElementById('summaryTotal').textContent = '-';
            return;
        }

        let totalAmount = 0;

        list.forEach((item) => {
            totalAmount += Number(item.amount) || 0;

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${item.orderDate || ''}</td>
                <td><b>${item.classKey || ''}</b></td>
                <td>${item.unitKey || ''}</td>
                <td>${item.qty || ''}</td>
                <td>${formatNumber(Number(item.price) || 0)}</td>
                <td>${formatNumber(Number(item.amount) || 0)}</td>
                <td class="muted">${item.note || ''}</td>
            `;
            tbody.appendChild(tr);
        });

        document.getElementById('summaryOrder').textContent = '-';
        document.getElementById('summaryReturn').textContent = '-';
        document.getElementById('summaryAdd').textContent = '-';
        document.getElementById('summaryTotal').textContent = `${formatNumber(totalAmount)}원`;
    };

    const fetchManualInvoice = async (manualId, title, yymmText) => {
        document.querySelector('.invoice-table thead tr').innerHTML = `
            <th>주문일시</th>
            <th>품명</th>
            <th>규격</th>
            <th>수량</th>
            <th>단가</th>
            <th>금액</th>
            <th>비고</th>
        `;
        setInvoiceColgroup([20, 20, 10, 10, 12, 12, 16]);

        const res = await fetch('/logis/order/manual/items', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({manualId})
        });
        const data = await res.json();

        showInvoiceMode();
        document.getElementById('invoice-heading').textContent = title;
        document.getElementById('invoiceYyMm').textContent = yymmText;
        if (currentCenterInfo) renderCenterInfo(currentCenterInfo);

        renderManualInvoiceTable(data.response);
    };

    const fetchManualList = async () => {
        const [year, month] = monthPicker.value.split('-');
        const activeCenter = document.querySelector('.center-item.active');
        const centerCode = activeCenter ? activeCenter.dataset.centerCode : null;

        if (!centerCode) {
            renderStatementCards([]);
            return;
        }

        const res = await fetch('/logis/order/manual/list', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({year, month, centerCode})
        });
        const data = await res.json();

        renderStatementCards(data.response || []);
    };

    // ────────────────────────────────────────
    // 체크박스 & 센터 선택 & 검색
    // ────────────────────────────────────────
    document.querySelectorAll('.center-item').forEach((center) => {
        const checkbox = center.querySelector('.check-input');
        const checkLabel = center.querySelector('.check-label');

        checkLabel.addEventListener('click', (e) => {
            e.stopPropagation();
            setTimeout(() => {
                const centerCode = center.dataset.centerCode;
                if (checkbox.checked) checkedCenters.add(centerCode);
                else checkedCenters.delete(centerCode);
            }, 0);
        });

        center.addEventListener('click', (event) => {
            if (event.target.closest('.check-label')) return;
            document.querySelectorAll('.center-item').forEach((item) => item.classList.remove('active'));
            center.classList.add('active');
            applyDeadlineToDisplay(center.dataset.centerCode);
            fetchReorderList();
        });
    });

    document.querySelector('.search-box input').addEventListener('input', (e) => {
        const keyword = e.target.value.trim().toLowerCase();
        document.querySelectorAll('.center-item').forEach(item => {
            const name = item.querySelector('.center-name').textContent.toLowerCase();
            item.style.display = name.includes(keyword) ? '' : 'none';
        });
    });

    // ────────────────────────────────────────
    // 달력 피커 & 마감일
    // ────────────────────────────────────────
    document.querySelectorAll('.calendar-btn').forEach((button) => {
        button.addEventListener('click', () => {
            const picker = document.getElementById(button.dataset.picker);
            openPicker(picker);
        });
    });

    // 헤더 벨 뱃지 = 화면에 보이는 전체 센터의 해당 월 미승인 건수 합
    const refreshWaitCountBadge = () => {
        const badge = document.getElementById('waitCountBadge');
        if (!badge) return;

        const total = Array.from(document.querySelectorAll('.center-item'))
            .reduce((sum, item) => sum + (parseInt(item.dataset.waitCount) || 0), 0);

        badge.textContent = total;
        badge.style.display = total > 0 ? '' : 'none';
    };

    const refreshCenterRequestCounts = async () => {
        const [year, month] = monthPicker.value.split('-');
        const res = await fetch('/logis/order/reorder-status-map', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({year, month})
        });
        const data = await res.json();
        const statusMap = data.response || {};

        document.querySelectorAll('.center-item').forEach(item => {
            const centerCode = item.dataset.centerCode;
            const status = statusMap[centerCode];
            const requestCount = status ? status.requestCount : 0;
            const confirmed = status ? status.confirmed : false;

            item.querySelector('.request').textContent = `요청 ${requestCount}건`;
            item.dataset.waitCount = status ? status.waitCount : 0;

            const badge = item.querySelector('.badge');
            if (!status) {
                badge.className = 'badge none';
                badge.textContent = '-';
            } else if (confirmed) {
                badge.className = 'badge done';
                badge.textContent = '승인완료';
            } else {
                badge.className = 'badge wait';
                badge.textContent = '승인대기';
            }
        });

        refreshWaitCountBadge();
    };

    monthPicker.addEventListener('change', () => {
        const [year, month] = monthPicker.value.split('-');
        monthDisplay.value = `${year}. ${month}`;

        const activeCenter = document.querySelector('.center-item.active');
        if (activeCenter) applyDeadlineToDisplay(activeCenter.dataset.centerCode);

        refreshCenterRequestCounts();
        fetchReorderList();
    });

    deadlinePicker.addEventListener('change', () => {
        const [year, month, day] = deadlinePicker.value.split('-');
        deadlineDisplay.value = `${year}. ${month}. ${day}`;
        deadlineDisplay.classList.remove('danger');
    });

    document.getElementById('deadlineSaveBtn')?.addEventListener('click', async () => {
        const activeCenter = document.querySelector('.center-item.active');
        if (!activeCenter) {
            alert('먼저 센터를 선택해주세요.');
            return;
        }

        const [year, month, day] = deadlinePicker.value.split('-');
        if (!day) {
            alert('마감일을 선택해주세요.');
            return;
        }

        const newDay = parseInt(day);
        const originalDay = activeCenter.dataset.deadline ? parseInt(activeCenter.dataset.deadline) : null;
        const centerCode = activeCenter.dataset.centerCode;
        const centerName = activeCenter.querySelector('.center-name').textContent;

        if (originalDay === newDay) {
            alert('변경된 내용이 없습니다.');
            return;
        }

        const beforeText = originalDay ? `${originalDay}일` : '없음';
        const confirmed = confirm(
            `${centerName}의 주문마감일을 변경하시겠습니까?\n\n변경 전: ${beforeText}\n변경 후: ${newDay}일`
        );
        if (!confirmed) return;

        try {
            const res = await fetch('/logis/order/deadline', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({centerCode, deadlineAt: newDay})
            });
            const data = await res.json();

            if (data.success) {
                activeCenter.dataset.deadline = newDay;
                alert(`${centerName}의 주문마감일이 변경되었습니다.`);
            } else {
                alert(`${centerName}의 주문마감일 변경에 실패했습니다.`);
            }
        } catch (e) {
            alert(`${centerName}의 주문마감일 변경 중 오류가 발생했습니다.`);
        }
    });

    const applyDeadlineToDisplay = (centerCode) => {
        const centerEl = document.querySelector(`.center-item[data-center-code="${centerCode}"]`);
        const day = centerEl ? centerEl.dataset.deadline : null;
        if (!day) return;

        const [year, month] = monthPicker.value.split('-');

        // 마감일은 대상 월의 '전월' 기준 (예: 8월 주문 → 7월 20일)
        const deadlineDate = new Date(Number(year), Number(month) - 2, 1);
        const deadlineYear = deadlineDate.getFullYear();
        const deadlineMonth = String(deadlineDate.getMonth() + 1).padStart(2, '0');
        const paddedDay = String(day).padStart(2, '0');

        deadlinePicker.value = `${deadlineYear}-${deadlineMonth}-${paddedDay}`;
        deadlineDisplay.value = `${deadlineYear}. ${deadlineMonth}. ${paddedDay}`;
        deadlineDisplay.classList.remove('danger');
    };

    // ────────────────────────────────────────
    // 승인 상태 select 색상
    // ────────────────────────────────────────
    const syncStatusSelectColor = (select) => {
        select.classList.remove('wait', 'approved', 'hold', 'rejected');
        if (select.value === 'unchecked') select.classList.add('wait');
        if (select.value === 'checked') select.classList.add('approved');
        if (select.value === 'user_cancel') select.classList.add('hold');
        if (select.value === 'acancel') select.classList.add('rejected');
    };

    // ────────────────────────────────────────
    // 렌더링
    // ────────────────────────────────────────
    const renderCenterInfo = (info) => {
        if (!info) return;
        document.getElementById('infoCenterName').textContent = info.centerName;
        document.getElementById('infoBizNum').textContent = formatBizNum(info.bizNum);
        document.getElementById('infoDirectorName').textContent = info.directorName;
        document.getElementById('infoAddress').textContent = info.address;
        document.getElementById('infoManagerName').textContent = info.managerName;
        document.getElementById('infoManagerTel').textContent = info.managerTel;
    };

    const renderStatementTabs = (summaryList) => {
        if (!summaryList || summaryList.length === 0) return;
        const summary = summaryList[0];
        const firstCard = document.querySelector('.statement-card');
        firstCard.querySelector('p').textContent = summary.yyMm;
        firstCard.querySelector('small').textContent = `품목 ${summary.itemCount || 0}건 | 전체 ${summary.totalCount || 0}권`;

        const [year, month] = summary.yyMm.split('-');
        document.getElementById('invoiceYyMm').textContent = `${year}. ${month}`;
    };

    const renderReorderTable = (list) => {
        const tbody = document.getElementById('reorderTableBody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            const onlyWait = document.getElementById('onlyWaitCheck').checked;
            const msg = onlyWait ? '미승인 건이 없습니다.' : '데이터가 없습니다.';
            tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:#9a9da7;">${msg}</td></tr>`;
            return;
        }

        list.forEach(item => {
            const stateClass = item.state === 'RETURN' ? 'return' : 'add';
            const stateText = item.state === 'RETURN' ? '반품' : '추가';
            const hasReason = item.reason && item.reason.trim() !== '';
            const tooltipClass = hasReason ? 'has-tooltip' : '';
            const tooltipAttr = hasReason ? `data-tooltip="${item.reason}"` : '';

            const confirmedClass = item.confirmed === 'checked' ? 'approved' :
                item.confirmed === 'unchecked' ? 'wait' :
                    item.confirmed === 'user_cancel' ? 'hold' : 'rejected';

            const tr = document.createElement('tr');
            tr.innerHTML = `
                    <td>
                        <label class="check-label">
                            <input class="check-input" type="checkbox">
                            <span class="check-mark"></span>
                        </label>
                    </td>
                    <td><span class="badge ${stateClass} ${tooltipClass}" ${tooltipAttr}>${stateText}</span></td>
                    <td class="muted">${item.createdAt}</td>
                    <td>${item.userName}</td>
                    <td>${item.className}</td>
                    <td>${item.unitName}</td>
                    <td>${item.cnt}</td>
                    <td>
                        <select class="status-select ${confirmedClass}" data-id="${item.id}">
                            <option value="unchecked"   ${item.confirmed === 'unchecked' ? 'selected' : ''}>미승인</option>
                            <option value="checked"     ${item.confirmed === 'checked' ? 'selected' : ''}>승인</option>
                            <option value="acancel"     ${item.confirmed === 'acancel' ? 'selected' : ''}>관리자 취소</option>
                            <option value="user_cancel" ${item.confirmed === 'user_cancel' ? 'selected' : ''}>사용자 취소</option>
                        </select>
                    </td>
                `;
            tbody.appendChild(tr);
        });

        tbody.querySelectorAll('.status-select').forEach(select => {
            select.addEventListener('change', async () => {
                syncStatusSelectColor(select);

                const id = select.dataset.id;
                const confirmed = select.value;
                const activeCenter = document.querySelector('.center-item.active');
                const centerCode = activeCenter ? activeCenter.dataset.centerCode : null;

                const labelMap = {
                    checked: '승인',
                    unchecked: '미승인',
                    user_cancel: '사용자 취소',
                    acancel: '관리자 취소'
                };

                try {
                    const res = await fetch('/logis/order/confirmed', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify({id, confirmed, centerCode})
                    });
                    const data = await res.json();

                    if (data.success) {
                        alert(`${labelMap[confirmed]}되었습니다.`);
                        // 좌측 센터 목록의 승인 상태/요청 건수 실시간 갱신
                        await refreshCenterRequestCounts();
                    } else {
                        alert('변경에 실패했습니다.');
                    }
                } catch (e) {
                    alert('변경 중 오류가 발생했습니다.');
                }
            });
        });
    };

    const renderSummaryBox = (list) => {
        const orderTotal = list.filter(i => i.rowType === 'order').reduce((s, i) => s + i.totalPrice, 0);
        const returnTotal = list.filter(i => i.rowType === 'return').reduce((s, i) => s + Math.abs(i.totalPrice), 0);
        const addTotal = list.filter(i => i.rowType === 'add').reduce((s, i) => s + i.totalPrice, 0);
        const finalTotal = orderTotal - returnTotal + addTotal;

        document.getElementById('summaryOrder').textContent = `${formatNumber(orderTotal)}원`;
        document.getElementById('summaryReturn').textContent = `${formatNumber(returnTotal)}원`;
        document.getElementById('summaryAdd').textContent = `${formatNumber(addTotal)}원`;
        document.getElementById('summaryTotal').textContent = `${formatNumber(finalTotal)}원`;
    };

    const renderInvoiceTable = (list) => {
        const tbody = document.getElementById('invoiceTableBody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:#9a9da7;">데이터가 없습니다.</td></tr>`;
            renderSummaryBox([]);
            return;
        }

        let prevDate = null;
        list.forEach(item => {
            const isNeg = item.totalCount < 0;
            const showDate = item.orderDate !== prevDate ? item.orderDate : '';
            prevDate = item.orderDate;

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${showDate}</td>
                <td><b>${item.className}</b></td>
                <td>${item.unitName}</td>
                <td ${isNeg ? 'class="neg"' : ''}>${item.totalCount}</td>
                <td>${formatNumber(item.unitPrice)}</td>
                <td ${isNeg ? 'class="neg"' : ''}>${isNeg ? '-' : ''}${formatNumber(item.totalPrice)}</td>
                <td class="muted">${item.userName}</td>
            `;
            tbody.appendChild(tr);
        });

        renderSummaryBox(list);
    };

    const renderAggregateLoading = () => {
        showAggregateMode();

        document.getElementById('invoice-heading').textContent = '집계 조회중';
        document.getElementById('invoiceYyMm').textContent = '-';

        ['infoCenterName', 'infoBizNum', 'infoDirectorName', 'infoAddress', 'infoManagerName', 'infoManagerTel']
            .forEach(id => document.getElementById(id).textContent = '-');

        document.getElementById('invoiceTableBody').innerHTML = `
            <tr>
                <td colspan="7" style="text-align:center; color:#9b009b; font-weight:800; padding:60px 0;">
                    <i class="fa-solid fa-spinner fa-spin" style="margin-right:8px;"></i>집계 작업중
                </td>
            </tr>
        `;

        ['summaryOrder', 'summaryReturn', 'summaryAdd', 'summaryTotal']
            .forEach(id => document.getElementById(id).textContent = '-');
    };

    const setInvoiceColgroup = (widths) => {
        const colgroup = document.querySelector('.invoice-table colgroup');
        colgroup.innerHTML = widths.map(w => `<col style="width: ${w}%">`).join('');
    };

    const renderCenterAggregate = (list, segmentType) => {
        const isTeacherMode = segmentType === '센터별 선생님 집계';
        const isAllMode = segmentType === '전체 집계';

        if (isTeacherMode) {
            document.querySelector('.invoice-table thead tr').innerHTML = `
                <th>선생님</th>
                <th>단계</th>
                <th>교재</th>
                <th>학생 수량</th>
                <th>선생님 수량</th>
                <th>추가수량</th>
                <th>합계</th>
                <th>시간표 수량</th>
            `;
            setInvoiceColgroup([12, 12, 16, 14, 14, 12, 10, 10]);
        } else if (isAllMode) {
            document.querySelector('.invoice-table thead tr').innerHTML = `
                <th>단계</th>
                <th>교재</th>
                <th>학생 수량</th>
                <th>선생님 수량</th>
                <th>추가수량</th>
                <th>합계</th>
            `;
            setInvoiceColgroup([15, 25, 18, 14, 14, 14]);
        } else {
            document.querySelector('.invoice-table thead tr').innerHTML = `
                <th>단계</th>
                <th>교재</th>
                <th>학생 수량</th>
                <th>선생님 수량</th>
                <th>추가수량</th>
                <th>합계</th>
                <th>시간표 수량</th>
            `;
            setInvoiceColgroup([13, 22, 15, 14, 12, 12, 12]);
        }

        const colCount = isTeacherMode ? 8 : isAllMode ? 6 : 7;
        const tbody = document.getElementById('invoiceTableBody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="${colCount}" style="text-align:center; color:#9a9da7; padding:40px 0;">데이터가 없습니다.</td></tr>`;
            return;
        }

        list.forEach(center => {
            const centerTr = document.createElement('tr');
            centerTr.innerHTML = `
                <td colspan="${colCount}"
                    style="position:sticky; top:40px; z-index:1;
                           background:#f3f4f8; color:#555864;
                           font-weight:800; text-align:left; padding-left:14px;">
                    ${center.centerName}
                </td>
            `;
            tbody.appendChild(centerTr);

            if (!center.items || center.items.length === 0) {
                const emptyTr = document.createElement('tr');
                emptyTr.innerHTML = `<td colspan="${colCount}" style="text-align:center; color:#9a9da7;">데이터가 없습니다.</td>`;
                tbody.appendChild(emptyTr);
                return;
            }

            let totalBaseCount = 0;
            let totalTeacherCount = 0;
            let totalReorderCount = 0;
            let totalCount = 0;
            let totalTimeTable = 0;

            center.items.forEach((item, idx) => {
                if (isTeacherMode) {
                    const isFirstOfTeacher = idx === 0 || center.items[idx - 1].userName !== item.userName;
                    if (isFirstOfTeacher) {
                        const teacherTr = document.createElement('tr');
                        teacherTr.innerHTML = `
                            <td colspan="${colCount}"
                                style="position:sticky; top:80px; z-index:1;
                                       background:#fbf6ff; color:#9b009b;
                                       font-weight:800; text-align:left; padding-left:24px;">
                                ${item.userName} 선생님
                            </td>
                        `;
                        tbody.appendChild(teacherTr);
                    }
                }

                const isNeg = item.reorderCount < 0;
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    ${isTeacherMode ? `<td>${item.userName}</td>` : ''}
                    <td><b>${item.className}</b></td>
                    <td>${item.unitName}</td>
                    <td>${item.baseCount}</td>
                    <td>${item.teacherCount}</td>
                    <td ${isNeg ? 'class="neg"' : ''}>${item.reorderCount}</td>
                    <td>${item.totalCount}</td>
                    ${isAllMode ? '' : `<td>${item.timeTableCount}</td>`}
                `;
                tbody.appendChild(tr);

                totalBaseCount += item.baseCount;
                totalTeacherCount += item.teacherCount;
                totalReorderCount += item.reorderCount;
                totalCount += item.totalCount;
                totalTimeTable += item.timeTableCount;

                if (isTeacherMode) {
                    const isLastOfTeacher = idx === center.items.length - 1 || center.items[idx + 1].userName !== item.userName;
                    if (isLastOfTeacher) {
                        const sameTeacherItems = center.items.filter(i => i.userName === item.userName);
                        const tBase = sameTeacherItems.reduce((s, i) => s + i.baseCount, 0);
                        const tTeacher = sameTeacherItems.reduce((s, i) => s + i.teacherCount, 0);
                        const tReorder = sameTeacherItems.reduce((s, i) => s + i.reorderCount, 0);
                        const tTotal = sameTeacherItems.reduce((s, i) => s + i.totalCount, 0);
                        const tTime = sameTeacherItems.reduce((s, i) => s + i.timeTableCount, 0);

                        const subTr = document.createElement('tr');
                        subTr.innerHTML = `
                            <td colspan="3"
                                style="font-weight:700; color:#9b009b; background:#fbf6ff; text-align:left; padding-right:14px;">
                                ${item.userName} 합계
                            </td>
                            <td style="font-weight:700; background:#fbf6ff;">${tBase}</td>
                            <td style="font-weight:700; background:#fbf6ff;">${tTeacher}</td>
                            <td style="font-weight:700; background:#fbf6ff; ${tReorder < 0 ? 'color:red;' : ''}">${tReorder}</td>
                            <td style="font-weight:700; background:#fbf6ff;">${tTotal}</td>
                            ${isAllMode ? '' : `<td style="font-weight:700; background:#fbf6ff;">${tTime}</td>`}
                        `;
                        tbody.appendChild(subTr);
                    }
                }
            });

            const totalTr = document.createElement('tr');
            totalTr.innerHTML = `
                <td colspan="${isTeacherMode ? 3 : 2}"
                    style="font-weight:800; color:#555; background:#f8f8fb; text-align:left; padding-right:14px;">
                    ${center.centerName} 합계
                </td>
                <td style="font-weight:800; background:#f8f8fb;">${totalBaseCount}</td>
                <td style="font-weight:800; background:#f8f8fb;">${totalTeacherCount}</td>
                <td style="font-weight:800; background:#f8f8fb; ${totalReorderCount < 0 ? 'color:red;' : ''}">${totalReorderCount}</td>
                <td style="font-weight:800; background:#f8f8fb;">${totalCount}</td>
                ${isAllMode ? '' : `<td style="font-weight:800; background:#f8f8fb;">${totalTimeTable}</td>`}
            `;
            tbody.appendChild(totalTr);
        });
    };

    // ────────────────────────────────────────
    // fetch
    // ────────────────────────────────────────
    const fetchReorderList = async () => {
        const [year, month] = monthPicker.value.split('-');
        const activeCenter = document.querySelector('.center-item.active');
        const centerCode = activeCenter ? activeCenter.dataset.centerCode : null;
        const onlyWait = document.getElementById('onlyWaitCheck').checked;

        document.querySelectorAll('.statement-card').forEach((c, idx) => {
            c.classList.toggle('active', idx === 0);
        });

        const res = await fetch('/logis/order/reorder-list', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({year, month, centerCode, onlyWait})
        });
        const data = await res.json();

        currentCenterInfo = data.response.centerInfo;
        renderStatementTabs(data.response.summaryInvoice);
        renderReorderTable(data.response.reorderList);
        renderCenterInfo(currentCenterInfo);
        fetchInvoice();
        fetchManualList();
    };

    const fetchInvoice = async () => {
        document.querySelector('.invoice-table thead tr').innerHTML = `
            <th>주문일시</th>
            <th>품명</th>
            <th>규격</th>
            <th>수량</th>
            <th>단가</th>
            <th>금액</th>
            <th>비고</th>
        `;

        setInvoiceColgroup([20, 20, 10, 10, 12, 12, 16]);
        const [year, month] = monthPicker.value.split('-');
        const activeCenter = document.querySelector('.center-item.active');
        const centerCode = activeCenter ? activeCenter.dataset.centerCode : null;

        const res = await fetch('/logis/order/invoice', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({year, month, centerCode})
        });
        const data = await res.json();

        showInvoiceMode();
        document.getElementById('invoice-heading').textContent = '거래명세서';
        if (currentCenterInfo) renderCenterInfo(currentCenterInfo);
        renderInvoiceTable(data.response);
    };

    const fetchAggregate = async (segmentType, centerCodes) => {
        lastAggregateSegment = segmentType;
        lastAggregateCenterCodes = centerCodes;

        const [year, month] = monthPicker.value.split('-');

        const res = await fetch('/logis/order/aggregate', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({year, month, segmentType, centerCodes})
        });
        const data = await res.json();

        document.getElementById('invoice-heading').textContent = segmentType;
        document.getElementById('invoiceYyMm').textContent = `${year}. ${month}`;

        renderCenterAggregate(data.response, segmentType);
    };

    // ────────────────────────────────────────
    // 조회 세그먼트 / 조회 버튼
    // ────────────────────────────────────────
    document.querySelectorAll('.segmented button').forEach((button) => {
        button.addEventListener('click', () => {
            button.parentElement.querySelectorAll('button').forEach((item) => item.classList.remove('active'));
            button.classList.add('active');
        });
    });

    document.querySelector('.primary-btn')?.addEventListener('click', () => {
        const activeSegment = document.querySelector('.segmented button.active');

        if (!activeSegment) {
            alert('조회 기준을 선택해주세요.');
            return;
        }

        const segmentType = activeSegment.textContent.trim();
        const isAll = segmentType === '전체 집계';

        if (!isAll && checkedCenters.size === 0) {
            alert('조회하실 센터를 체크해주세요.');
            return;
        }

        const orderedCenterCodes = Array.from(document.querySelectorAll('.center-item'))
            .map(item => item.dataset.centerCode)
            .filter(code => checkedCenters.has(code));

        const centerCodes = isAll ? ['all'] : orderedCenterCodes;

        document.querySelectorAll('.statement-card').forEach(c => c.classList.remove('active'));
        renderAggregateLoading();
        fetchAggregate(segmentType, centerCodes);
    });

    document.getElementById('onlyWaitCheck').addEventListener('change', fetchReorderList);

    // ────────────────────────────────────────
    // 모드 전환
    // ────────────────────────────────────────
    const showInvoiceMode = () => {
        document.getElementById('companyInfo').style.display = '';
        document.getElementById('summaryBox').style.display = '';
        document.querySelector('.invoice .table-wrap').style.maxHeight = '400px';
    };

    const showAggregateMode = () => {
        document.getElementById('companyInfo').style.display = 'none';
        document.getElementById('summaryBox').style.display = 'none';
        document.querySelector('.invoice .table-wrap').style.maxHeight = '800px';
    };

    // ────────────────────────────────────────
    // 인쇄 / 엑셀
    // ────────────────────────────────────────
    function printInvoice(year, month, centerCode) {
        const iframe = document.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = `/admin/order/print-invoice?year=${year}&month=${month}&centerCode=${centerCode}`;
        iframe.onload = () => iframe.contentWindow.print();
        document.body.appendChild(iframe);
    }


    function printAggregate(year, month, segmentType, centerCodes) {
        const iframe = document.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = `/admin/order/print-aggregate?year=${year}&month=${month}&segmentType=${encodeURIComponent(segmentType)}&centerCodes=${centerCodes.join(',')}`;
        iframe.onload = () => iframe.contentWindow.print();
        document.body.appendChild(iframe);
    }

    function printManualInvoice(manualId) {
        const iframe = document.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = `/admin/order/print-manual-invoice?manualId=${manualId}`;
        iframe.onload = () => iframe.contentWindow.print();
        document.body.appendChild(iframe);
    }

    document.querySelector('.print-btn')?.addEventListener('click', () => {
        const isAggregateMode = document.getElementById('companyInfo').style.display === 'none';

        if (isAggregateMode) {
            if (!lastAggregateSegment) {
                alert('먼저 조회를 진행해주세요.');
                return;
            }
            const [year, month] = monthPicker.value.split('-');
            printAggregate(year, month, lastAggregateSegment, lastAggregateCenterCodes);
            return;
        }

        const activeCard = document.querySelector('.statement-card.active');
        const manualId = activeCard ? activeCard.dataset.manualId : null;

        if (manualId) {
            printManualInvoice(manualId);
            return;
        }

        const [year, month] = monthPicker.value.split('-');
        const activeCenter = document.querySelector('.center-item.active');
        const centerCode = activeCenter ? activeCenter.dataset.centerCode : null;

        if (!centerCode) {
            alert('조회하실 센터를 선택해주세요.');
            return;
        }

        printInvoice(year, month, centerCode);
    });

    document.querySelector('.excel-btn')?.addEventListener('click', () => {
        const isAggregateMode = document.getElementById('companyInfo').style.display === 'none';

        if (isAggregateMode) {
            if (!lastAggregateSegment) {
                alert('먼저 조회를 진행해주세요.');
                return;
            }
            const [year, month] = monthPicker.value.split('-');
            window.location.href = `/admin/order/aggregate-excel?year=${year}&month=${month}&segmentType=${encodeURIComponent(lastAggregateSegment)}&centerCodes=${lastAggregateCenterCodes.join(',')}`;
            return;
        }

        const activeCard = document.querySelector('.statement-card.active');
        const manualId = activeCard ? activeCard.dataset.manualId : null;

        if (manualId) {
            window.location.href = `/admin/order/manual-excel?manualId=${manualId}`;
            return;
        }

        const [year, month] = monthPicker.value.split('-');
        const activeCenter = document.querySelector('.center-item.active');
        const centerCode = activeCenter ? activeCenter.dataset.centerCode : null;

        if (!centerCode) {
            alert('조회하실 센터를 선택해주세요.');
            return;
        }

        window.location.href = `/admin/order/invoice-excel?year=${year}&month=${month}&centerCode=${centerCode}`;
    });

    // ────────────────────────────────────────
    // 초기 상태
    // ────────────────────────────────────────
    document.getElementById('reorderTableBody').innerHTML =
        `<tr><td colspan="8" style="text-align:center; color:#9a9da7;">조회하실 센터를 선택해주세요.</td></tr>`;

    refreshWaitCountBadge();
    document.getElementById('invoiceTableBody').innerHTML =
        `<tr><td colspan="7" style="text-align:center; color:#9a9da7;">조회하실 센터를 선택해주세요.</td></tr>`;
});