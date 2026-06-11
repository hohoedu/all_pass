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
        });
    });

    const syncStatusSelectColor = (select) => {
        select.classList.remove('wait', 'approved', 'hold', 'rejected');
        const value = select.value;
        if (value === '승인대기') select.classList.add('wait');
        if (value === '승인') select.classList.add('approved');
        if (value === '보류') select.classList.add('hold');
        if (value === '반려') select.classList.add('rejected');
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
});