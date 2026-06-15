document.addEventListener('DOMContentLoaded', () => {
    const appShell = document.getElementById('appShell');
    const navButtons = document.querySelectorAll('.nav-icon');
    const depthMenus = document.querySelectorAll('.depth-menu');
    let openedMenu = null;

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
            depthMenus.forEach((menu) => {
                menu.classList.toggle('active', menu.dataset.depth === target);
            });
        });
    });

    document.querySelectorAll('.segmented button').forEach((button) => {
        button.addEventListener('click', () => {
            button.parentElement.querySelectorAll('button').forEach((item) => item.classList.remove('active'));
            button.classList.add('active');
        });
    });

    document.querySelectorAll('.statement-card').forEach((card) => {
        card.addEventListener('click', () => {
            card.parentElement.querySelectorAll('.statement-card').forEach((item) => item.classList.remove('active'));
            card.classList.add('active');
        });
    });

    document.querySelectorAll('.center-item').forEach((center) => {
        center.addEventListener('click', (event) => {
            if (event.target.closest('.check-label')) return;
            document.querySelectorAll('.center-item').forEach((item) => item.classList.remove('active'));
            center.classList.add('active');
            fetchReorderList();
        });
    });

    const syncStatusSelectColor = (select) => {
        select.classList.remove('wait', 'approved', 'hold', 'rejected');
        const value = select.value;
        if (value === 'unchecked') select.classList.add('wait');
        if (value === 'checked') select.classList.add('approved');
        if (value === 'user_cancel') select.classList.add('hold');
        if (value === 'acancel') select.classList.add('rejected');
    };

    document.querySelectorAll('.status-select').forEach((select) => {
        syncStatusSelectColor(select);
        select.addEventListener('change', () => syncStatusSelectColor(select));
    });

    const openPicker = (picker) => {
        if (typeof picker.showPicker === 'function') picker.showPicker();
        else picker.focus();
    };

    document.querySelectorAll('.calendar-btn').forEach((button) => {
        button.addEventListener('click', () => {
            const picker = document.getElementById(button.dataset.picker);
            openPicker(picker);
        });
    });

    const monthPicker = document.getElementById('monthPicker');
    const monthDisplay = document.getElementById('monthDisplay');
    monthPicker.addEventListener('change', () => {
        const [year, month] = monthPicker.value.split('-');
        monthDisplay.value = `${year}. ${month}`;
    });

    const deadlinePicker = document.getElementById('deadlinePicker');
    const deadlineDisplay = document.getElementById('deadlineDisplay');
    deadlinePicker.addEventListener('change', () => {
        const [year, month, day] = deadlinePicker.value.split('-');
        deadlineDisplay.value = `${year}. ${month}. ${day}`;
    });

    const renderReorderTable = (list) => {
        const tbody = document.getElementById('reorderTableBody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            const msg = onlyWait ? '미승인 건이 없습니다.' : '데이터가 없습니다.';
            tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:#9a9da7;">${msg}</td></tr>`;
            return;
        }

        list.forEach(item => {
            const stateClass = item.state === 'RETURN' ? 'return' : 'add';
            const stateText = item.state === 'RETURN' ? '반품' : '추가';

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
                <td><span class="badge ${stateClass}">${stateText}</span></td>
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
            select.addEventListener('change', () => syncStatusSelectColor(select));
        });
    };

    const fetchReorderList = async () => {
        const [year, month] = document.getElementById('monthPicker').value.split('-');
        const activeCenter = document.querySelector('.center-item.active');
        const centerCode = activeCenter ? activeCenter.dataset.centerCode : null;
        const onlyWait = document.getElementById('onlyWaitCheck').checked;

        const res = await fetch('/logis/order/reorder-list', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({year, month, centerCode, onlyWait})
        });
        const data = await res.json();
        renderReorderTable(data.response);
    };

    document.querySelector('.primary-btn').addEventListener('click', fetchReorderList);
    document.getElementById('onlyWaitCheck').addEventListener('change', fetchReorderList);

    fetchReorderList();
});