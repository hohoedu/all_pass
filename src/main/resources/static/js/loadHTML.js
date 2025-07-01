// header.html load
// fetch('header.html')
//   .then(response => response.text())
//   .then(data => {
//     document.getElementById('header-load').innerHTML = data;
//   })
//   .catch(error => console.error('Error loading header:', error));

// Sidebar load

/*
fetch('sidebar.html')
.then(response => response.text())
.then(data => {
 document.getElementById('sidebar-load').innerHTML = data;

 // [0] submenu 링크 (menu-link) 클릭 시 파란색 표시
 const menu = document.querySelectorAll(".menu-link");
 menu.forEach(link => {
   link.addEventListener("click", function () {
     menu.forEach(l => l.classList.remove("active"));
     this.classList.add("active");
   });
 });

 // [0-2] 현재 페이지와 일치하는 메뉴 (active)
 const current = window.location.pathname.split("/").pop();
 menu.forEach(link => {
   if (link.getAttribute("href") === current) {
     link.classList.add("active");
   }
 });

 // [1] menu-toggle 클릭 시 서브메뉴 열기 + 기억하기 (사라지지 않게 유지)
 const toggles = document.querySelectorAll(".menu-toggle");
 const submenus = document.querySelectorAll(".submenu");

 toggles.forEach(toggle => {
   toggle.addEventListener("click", function (e) {
     const href = this.getAttribute("href");
     if (!href || href === "#") {
       e.preventDefault();
     }

     const target = this.dataset.target;
     localStorage.setItem("activeMenu", target); // 기억

     // 기존 메뉴 상태 초기화
     toggles.forEach(el => el.classList.remove("active"));
     submenus.forEach(el => el.classList.remove("active"));

     // 현재 메뉴 열기
     this.classList.add("active");
     const submenu = document.querySelector(`.submenu[data-menu="${target}"]`);
     if (submenu) submenu.classList.add("active");

     // sidebar-right 열기 (만약 오른쪽 사용 시)
     document.querySelector(".sidebar-right")?.classList.remove("collapsed");
   });
 });


 // [1-2] 페이지 로드 시 기억된 메뉴 자동 열기
 const savedTarget = localStorage.getItem("activeMenu");
 if (savedTarget) {
   const savedToggle = document.querySelector(`.menu-toggle[data-target="${savedTarget}"]`);
   const savedSubmenu = document.querySelector(`.submenu[data-menu="${savedTarget}"]`);
   if (savedToggle && savedSubmenu) {
     savedToggle.classList.add("active");
     savedSubmenu.classList.add("active");
   }
 }

 // [2] sidebar-right 접기 버튼 (있는 경우만)
 const closeBtn = document.querySelector(".sidebar-close");
 if (closeBtn) {
   closeBtn.addEventListener("click", function () {
     document.querySelector(".sidebar-right")?.classList.toggle("collapsed");
   });
 }
});
*/
// 무조건 처음에는 닫힌 상태로 시작, localStorage 초기화
localStorage.removeItem("activeMenu");

fetch('/sidebar.html')
  .then(response => response.text())
  .then(data => {
    document.getElementById('sidebar-load').innerHTML = data;

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
  .catch(error => console.error('Sidebar load error:', error));

