document.addEventListener("DOMContentLoaded", () => {


    const printBtn = document.getElementById('print-withdraw');

    printBtn.addEventListener("click", () => {
        const ym = monthPickerInput.value;
        const userCode = document.getElementById('teacher-filter')?.value || 'all';
        const tab = activeTab; // t1~t5

        window.open(`/student/print-withdraw?ym=${ym}&userCode=${userCode}&tab=${tab}`);
    });

    function printTimeView(ym, userCode) {
        const iframe = document.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = `/class/print-timeview?ym=${ym}&userCode=${userCode}`;

        iframe.onload = () => {
            iframe.contentWindow.print();
        };

        document.body.appendChild(iframe);
    }

    const monthPickerInput = document.getElementById("monthPickerInput");
    const currentMonthLabel = document.getElementById("currentMonth");

    const TAB_CONFIG = {
        t1: {url: "/student/api/withdraw/join", render: renderJoinTable},
        t2: {url: "/student/api/withdraw/withdraw", render: renderWithdrawTable},
        t3: {url: "/student/api/withdraw/transfer-in", render: renderTransferInTable},
        t4: {url: "/student/api/withdraw/transfer-out", render: renderTransferOutTable},
        t5: {url: "/student/api/withdraw/graduate", render: renderGraduateTable},
    };

    let activeTab = "t1";

    /* ========================
     * 달력
     * ======================== */
    function setMonthLabel(value) {
        if (!value) return;
        const [year, month] = value.split("-");
        currentMonthLabel.textContent = `${year}년 ${parseInt(month, 10)}월`;
    }

    const today = new Date();
    const defaultMonth = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}`;
    monthPickerInput.value = monthPickerInput.value || defaultMonth;
    setMonthLabel(monthPickerInput.value);

    document.getElementById("openMonthPicker")?.addEventListener("click", () => {
        if (typeof monthPickerInput.showPicker === "function") {
            monthPickerInput.showPicker();
        } else {
            monthPickerInput.click();
        }
    });

    // 월 변경 시 카운트 + 현재 탭 테이블 갱신
    monthPickerInput.addEventListener("change", () => {
        setMonthLabel(monthPickerInput.value);
        fetchCounts();
        fetchTabData(activeTab);
    });

    /* ========================
     * 선생님 필터
     * 변경 시 카운트 + 현재 탭 테이블 갱신
     * ======================== */
    document.getElementById("teacher-filter")?.addEventListener("change", () => {
        fetchCounts();
        fetchTabData(activeTab);
    });

    /* ========================
     * 탭 클릭 - 테이블만
     * ======================== */
    document.querySelectorAll(".status-tab").forEach(tab => {
        tab.addEventListener("click", () => {
            document.querySelectorAll(".status-tab").forEach(t => {
                t.classList.remove("is-active");
                t.setAttribute("aria-selected", "false");
            });
            document.querySelectorAll(".status-panel").forEach(p => {
                p.classList.remove("is-active");
            });

            tab.classList.add("is-active");
            tab.setAttribute("aria-selected", "true");
            document.querySelector(`.status-panel[data-panel="${tab.dataset.tab}"]`)
                ?.classList.add("is-active");

            activeTab = tab.dataset.tab;
            fetchTabData(activeTab);
        });
    });

    /* ========================
     * 공통 요청 파라미터
     * ======================== */
    function getReq() {
        return {
            userCode: document.getElementById("teacher-filter")?.value || "all",
            ym: monthPickerInput.value || defaultMonth,
        };
    }

    /* ========================
     * 카운트 fetch
     * ======================== */
    function fetchCounts() {
        fetch("/student/api/withdraw/counts", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(getReq()),
        })
            .then(res => res.json())
            .then(data => {
                console.log(data.response);
                const c = data.response;
                document.querySelector('.status-tab[data-tab="t1"] .count').textContent = `${c.joinCount}명`;
                document.querySelector('.status-tab[data-tab="t2"] .count').textContent = `${c.withdrawCount}명`;
                document.querySelector('.status-tab[data-tab="t3"] .count').textContent = `${c.transferInCount}명`;
                document.querySelector('.status-tab[data-tab="t4"] .count').textContent = `${c.transferOutCount}명`;
                document.querySelector('.status-tab[data-tab="t5"] .count').textContent = `${c.graduateCount}명`;
            })
            .catch(err => console.error("카운트 조회 실패:", err));
    }

    /* ========================
     * 테이블 fetch
     * ======================== */
    function fetchTabData(tabKey) {
        const config = TAB_CONFIG[tabKey];
        if (!config) return;

        fetch(config.url, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(getReq()),
        })
            .then(res => res.json())
            .then(data => config.render(data.response ?? []))
            .catch(err => console.error(`${tabKey} 조회 실패:`, err));
    }

    /* ========================
     * 렌더 함수
     * ======================== */
    function emptyRow(colspan) {
        return `<tr><td colspan="${colspan}" style="text-align:center;">데이터가 없습니다.</td></tr>`;
    }

    function renderJoinTable(list) {
        const tbody = document.querySelector('[data-panel="t1"] tbody');
        if (!tbody) return;
        tbody.innerHTML = list.length === 0 ? emptyRow(7)
            : list.map((s, i) => `
                <tr>
                    <td>${i + 1}</td>
                    <td>${s.studentName ?? ""}</td>
                    <td>${s.className ?? ""}</td>
                    <td>${s.teacherName ?? ""}</td>
                    <td>${s.gradeName ?? ""}</td>
                    <td>${s.joinDate ?? ""}</td>
                    <td><button class="class-action-btn" data-id="${s.studentId}">입회 취소</button></td>
                </tr>
            `).join("");
    }

    function renderWithdrawTable(list) {
        const tbody = document.querySelector('[data-panel="t2"] tbody');
        if (!tbody) return;
        tbody.innerHTML = list.length === 0 ? emptyRow(9)
            : list.map((s, i) => `
                <tr>
                    <td>${i + 1}</td>
                    <td>${s.studentName ?? ""}</td>
                    <td>${s.className ?? ""}</td>
                    <td>${s.teacherName ?? ""}</td>
                    <td>${s.gradeName ?? ""}</td>
                    <td>${s.joinDate ?? ""}</td>
                    <td>${s.withdrawDate ?? ""}</td>
                    <td><p>${s.reason ?? ""}</p></td>
                    <td><button class="class-action-btn" data-id="${s.studentId}">탈퇴 취소</button></td>
                </tr>
            `).join("");
    }

    function renderTransferInTable(list) {
        const tbody = document.querySelector('[data-panel="t3"] tbody');
        if (!tbody) return;
        tbody.innerHTML = list.length === 0 ? emptyRow(10)
            : list.map((s, i) => `
                <tr>
                    <td>${i + 1}</td>
                    <td>${s.studentName ?? ""}</td>
                    <td>${s.className ?? ""}</td>
                    <td>${s.transferInTeacher ?? ""}</td>
                    <td>${s.transferOutTeacher ?? ""}</td>
                    <td>${s.gradeName ?? ""}</td>
                    <td>${s.joinDate ?? ""}</td>
                    <td>${s.transferDate ?? ""}</td>
                    <td><p>${s.reason ?? ""}</p></td>
                    <td><button class="class-action-btn" data-id="${s.studentId}">전입 취소</button></td>
                </tr>
            `).join("");
    }

    function renderTransferOutTable(list) {
        const tbody = document.querySelector('[data-panel="t4"] tbody');
        if (!tbody) return;
        tbody.innerHTML = list.length === 0 ? emptyRow(10)
            : list.map((s, i) => `
                <tr>
                    <td>${i + 1}</td>
                    <td>${s.studentName ?? ""}</td>
                    <td>${s.className ?? ""}</td>
                    <td>${s.transferInTeacher ?? ""}</td>
                    <td>${s.transferOutTeacher ?? ""}</td>
                    <td>${s.gradeName ?? ""}</td>
                    <td>${s.joinDate ?? ""}</td>
                    <td>${s.transferDate ?? ""}</td>
                    <td><p>${s.reason ?? ""}</p></td>
                    <td><button class="class-action-btn" data-id="${s.studentId}">전출 취소</button></td>
                </tr>
            `).join("");
    }

    function renderGraduateTable(list) {
        const tbody = document.querySelector('[data-panel="t5"] tbody');
        if (!tbody) return;
        tbody.innerHTML = list.length === 0 ? emptyRow(8)
            : list.map((s, i) => `
                <tr>
                    <td>${i + 1}</td>
                    <td>${s.studentName ?? ""}</td>
                    <td>${s.className ?? ""}</td>
                    <td>${s.teacherName ?? ""}</td>
                    <td>${s.gradeName ?? ""}</td>
                    <td>${s.joinDate ?? ""}</td>
                    <td>${s.graduateDate ?? ""}</td>
                    <td><p>${s.reason ?? ""}</p></td>
                </tr>
            `).join("");
    }

    fetchCounts();
    fetchTabData(activeTab);
});