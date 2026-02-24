// Sidebar

// 사이드바 패치

// 무조건 처음에는 닫힌 상태로 시작, localStorage 초기화
localStorage.removeItem("activeMenu");

document.addEventListener("DOMContentLoaded", function () {
    const menu = document.querySelectorAll(".menu-link");
    const toggles = document.querySelectorAll(".menu-toggle");
    const submenus = document.querySelectorAll(".submenu");
    const sidebarRight = document.querySelector(".sidebar-right");
    const sidebar = document.querySelector(".sidebar"); // 전체 sidebar
    let isSidebarLocked = false;
    let hoverTimeout = null;

    // [1] 메뉴 클릭 시 서브메뉴 열기 + 기억하기
    toggles.forEach(toggle => {
        toggle.addEventListener("click", function (e) {
            const href = this.getAttribute("href");
            if (!href || href === "#") e.preventDefault();

            const target = this.dataset.target;
            localStorage.setItem("activeMenu", target);
            isSidebarLocked = true;

            toggles.forEach(el => el.classList.remove("active"));
            submenus.forEach(el => el.classList.remove("active"));

            this.classList.add("active");
            const submenu = document.querySelector(`.submenu[data-menu="${target}"]`);
            if (submenu) submenu.classList.add("active");

            sidebarRight.classList.remove("collapsed");
        });
    });

    // [2] hover 시 submenu 임시 열기 (sidebar 전체 기준)
    sidebar.addEventListener("mouseover", function (e) {
        const hovered = e.target.closest(".menu-toggle");
        if (!hovered || isSidebarLocked) return;

        clearTimeout(hoverTimeout); // 닫힘 타이머 취소
        sidebarRight.classList.remove("collapsed");
        submenus.forEach(el => el.classList.remove("active"));
        toggles.forEach(el => el.classList.remove("hover"));

        const target = hovered.dataset.target;
        const submenu = document.querySelector(`.submenu[data-menu="${target}"]`);
        if (submenu) submenu.classList.add("active");

        hovered.classList.add("hover");
    });

    sidebar.addEventListener("mouseleave", function () {
        if (isSidebarLocked) return;

        // 일정 시간 뒤에 닫히게 (마우스 순간 이탈 방지)
        hoverTimeout = setTimeout(() => {
            sidebarRight.classList.add("collapsed");
            submenus.forEach(el => el.classList.remove("active"));
            toggles.forEach(el => el.classList.remove("hover"));
        }, 200); // ms 단위 (원하면 0으로 해도 됨)
    });

    // [3] 외부 클릭 시 sidebar 접기 + 잠금 해제
    document.addEventListener("click", function (e) {
        const isInsideSidebar = e.target.closest(".sidebar");
        if (!isInsideSidebar) {
            sidebarRight.classList.add("collapsed");
            isSidebarLocked = false;
            toggles.forEach(el => el.classList.remove("active"));
            submenus.forEach(el => el.classList.remove("active"));
            toggles.forEach(el => el.classList.remove("hover"));
        }
    });

    // [4] 현재 페이지 링크 활성화
    const current = window.location.pathname.split("/").pop();
    menu.forEach(link => {
        if (link.getAttribute("href") === current) {
            link.classList.add("active");
        }
        link.addEventListener("click", function () {
            menu.forEach(l => l.classList.remove("active"));
            this.classList.add("active");
        });
    });

    // [5] 페이지 로드시 기억된 메뉴만 열기 (없으면 collapsed 유지)
    const savedTarget = localStorage.getItem("activeMenu");
    if (savedTarget) {
        const savedToggle = document.querySelector(`.menu-toggle[data-target="${savedTarget}"]`);
        const savedSubmenu = document.querySelector(`.submenu[data-menu="${savedTarget}"]`);
        if (savedToggle && savedSubmenu) {
            savedToggle.classList.add("active");
            savedSubmenu.classList.add("active");
            sidebarRight.classList.remove("collapsed");
            isSidebarLocked = true;
        }
    } else {
        sidebarRight.classList.add("collapsed"); // 기억된 메뉴 없으면 기본은 닫힌 상태
    }
})

//================시간표 등록================//
document.addEventListener('DOMContentLoaded', function () {
    const link = document.getElementById('timetableLink');
    if (!link) return;

    link.addEventListener('click', function (e) {
        e.preventDefault();

        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');

        window.location.href = `/class/timetable?year=${year}&month=${month}`;
    });
});

// //================시간표 조회================//
document.addEventListener('DOMContentLoaded', function () {
    const link = document.getElementById('tableViewLink');
    if (!link) return;

    link.addEventListener('click', function (e) {

        e.preventDefault();
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');

        window.location.href = `/class/timeview?year=${year}&month=${month}`;

    });
});

document.addEventListener('DOMContentLoaded', function () {
    const link = document.getElementById('remedial');
    if (!link) return;

    link.addEventListener('click', function (e) {

        e.preventDefault();
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');

        window.location.href = `/class/remedial?year=${year}&month=${month}`;
    });
});

document.addEventListener('DOMContentLoaded', function () {
    const link = document.getElementById('studentWithdraw');
    if (!link) return;

    link.addEventListener('click', function (e) {
        e.preventDefault();

        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');

        window.location.href = `/student/withdraw?year=${year}&month=${month}`;
    });
});

function getNextYearMonth() {
    const now = new Date();
    let year = now.getFullYear();
    let month = now.getMonth() + 2; // 현재 월 + 1 = 다음달

    if (month > 12) {
        month = 1;
        year += 1;
    }

    return {
        year: year.toString(),
        month: month.toString().padStart(2, '0')
    };
}

function getYearMonthByDay20() {
    const today = new Date();
    const day = today.getDate();

    let year = today.getFullYear();
    let month = today.getMonth() + 1; // 0-based이므로 +1

    // 20일 이후면 다음 달로
    if (day >= 20) {
        month += 1;
        if (month > 12) {
            month = 1;
            year += 1;
        }
    }

    return {
        year: year.toString(),
        month: month.toString().padStart(2, '0')
    };
}

// ================ 수강료 청구 ============ //
document.addEventListener('DOMContentLoaded', function () {
    const link = document.getElementById('payEduLink');
    if (!link) return;

    link.addEventListener('click', function (e) {
        e.preventDefault();

        // 항상 20일 기준으로 계산 (URL 파라미터 무시)
        const target = getYearMonthByDay20();
        const year = target.year;
        const month = target.month;

        window.location.href = `/pay/pay-edu?year=${year}&month=${month}`;
    });
});

// ================ 월별 결제 내역 ============ //
document.addEventListener('DOMContentLoaded', function () {
    const link = document.getElementById('payListLink');
    if (!link) return;

    link.addEventListener('click', function (e) {
        e.preventDefault();

        // 항상 20일 기준으로 계산 (URL 파라미터 무시)
        const target = getYearMonthByDay20();
        const year = target.year;
        const month = target.month;

        window.location.href = `/pay/pay-list?year=${year}&month=${month}`;
    });
});

// ================ 교재 주문 ============ //
document.addEventListener('DOMContentLoaded', function () {
    const link = document.getElementById('orderLink');
    if (!link) return;

    link.addEventListener('click', function (e) {
        e.preventDefault();

        // 항상 다음달
        const next = getNextYearMonth();
        const year = next.year;
        const month = next.month;

        window.location.href = `/manage/order?year=${year}&month=${month}`;
    });
});
