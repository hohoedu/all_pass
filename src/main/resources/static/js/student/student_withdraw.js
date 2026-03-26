document.addEventListener("DOMContentLoaded", () => {

    /* ========================
     * 인쇄
     * ======================== */
    const printBtn = document.getElementById('print-withdraw');
    printBtn.addEventListener("click", () => {
        const ym = monthPickerInput.value;
        const userCode = document.getElementById('teacher-filter')?.value || 'all';
        const tab = activeTab;
        printWithdraw(ym, userCode, tab);
    });

    function printWithdraw(ym, userCode, tab) {
        const iframe = document.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = `/student/print-withdraw?ym=${ym}&userCode=${userCode}&tab=all`;
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

    monthPickerInput.addEventListener("change", () => {
        setMonthLabel(monthPickerInput.value);
        fetchCounts();
        fetchTabData(activeTab);
    });

    /* ========================
     * 선생님 필터
     * ======================== */
    document.getElementById("teacher-filter")?.addEventListener("change", () => {
        fetchCounts();
        fetchTabData(activeTab);
    });

    /* ========================
     * 탭 클릭
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
                    <td>${s.attendMonths + "개월" ?? ""}</td>
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

    /* ========================
     * 버튼 이벤트 위임
     * t2 탈퇴 탭 → 탈퇴 취소(복구)
     * ======================== */
    document.addEventListener("click", async (e) => {
        const btn = e.target.closest(".class-action-btn");
        if (!btn) return;

        const studentId = btn.dataset.id;
        const panel = btn.closest(".status-panel");
        const panelId = panel?.dataset.panel;

        if (panelId === "t1") {
            const result = await Swal.fire({
                title: "입회 취소",
                html: `
                <p>해당 학생의 <strong>모든 데이터가 삭제</strong>됩니다.</p>
                <p style="color:#e53e3e; margin-top:8px;">
                    ⚠️ 삭제된 데이터는 <strong>복구할 수 없습니다.</strong>
                </p>
            `,
                icon: "warning",
                showCancelButton: true,
                confirmButtonColor: "#e53e3e",
                cancelButtonColor: "#aaa",
                confirmButtonText: "삭제",
                cancelButtonText: "취소",
            });

            if (!result.isConfirmed) return;

            try {
                const res = await fetch("/student/cancel-join", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ studentId }),
                });

                const data = await res.json();

                if (!res.ok || data.success === false) {
                    Swal.fire("오류", data.msg ?? "삭제 중 오류가 발생했습니다.", "error");
                    return;
                }

                await Swal.fire("삭제 완료", "입회가 취소되었습니다.", "success");
                fetchCounts();
                fetchTabData("t1");

            } catch (err) {
                console.error("입회 취소 오류:", err);
                Swal.fire("오류", "삭제 중 오류가 발생했습니다.", "error");
            }
        }

        if (panelId === "t2") {
            if (!confirm("탈퇴를 취소하고 복구하시겠습니까?")) return;

            try {
                const res = await fetch("/student/restore", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({studentId})
                });

                const data = await res.json();

                if (!res.ok || data.success === false) {
                    alert(data.msg ?? "복구 중 오류가 발생했습니다.");
                    return;
                }

                alert("복구가 완료되었습니다.");
                fetchCounts();
                fetchTabData("t2");

            } catch (err) {
                console.error("복구 오류:", err);
                alert("복구 중 오류가 발생했습니다.");
            }
        }
    });

    fetchCounts();
    fetchTabData(activeTab);
});