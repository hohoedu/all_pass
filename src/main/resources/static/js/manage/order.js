document.addEventListener('DOMContentLoaded', () => {
    /* ======= *
     *   LEFT  *
     * ======= */
    const monthInput = document.querySelector('.hidden-picker');
    const monthBtn = document.querySelector('.calendar-open');
    const monthDisplay = document.querySelector('.day-display');

    initMonthFromUrl();

    monthBtn.addEventListener('click', () => monthInput.showPicker());
    monthInput.addEventListener('change', onMonthChange);

    function initMonthFromUrl() {
        const params = new URLSearchParams(window.location.search);
        const urlYear = params.get('year');
        const urlMonth = params.get('month');

        let y, m;

        if (urlYear && urlMonth) {
            y = urlYear;
            m = String(urlMonth).padStart(2, '0');
        } else {
            const now = new Date();
            const next = new Date(now.getFullYear(), now.getMonth(), 1);
            y = next.getFullYear();
            m = String(next.getMonth() + 1).padStart(2, '0');
        }

        monthInput.value = `${y}-${m}`;
        monthDisplay.insertAdjacentText('afterbegin', `${y}년 ${m}월`);
    }

    async function onMonthChange() {
        try {
            const date = new Date(monthInput.value);
            if (isNaN(date)) return;

            const year = date.getFullYear();
            const month = date.getMonth() + 1;

            const monthStr = String(month).padStart(2, "0");
            monthDisplay.childNodes[0].textContent = `${year}년 ${monthStr}월`;


            const response = await fetch(`/manage/order/base/list`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({yy: String(year), mm: monthStr})
            });

            if (!response.ok) {
                console.error("서버 조회 실패:", response.status);
                return;
            }

            const data = await response.json();
            renderLeftTable(data.response);

            // 월 변경 시 마감 여부 체크
            checkOrderDeadline();

        } catch (e) {
            console.error("월 변경 처리 중 오류:", e);
        }
    }

    function renderLeftTable(list) {
        const tbody = document.getElementById("order-left-body");
        tbody.innerHTML = "";

        if (!list || list.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="3" style="text-align:center; padding:20px;">
                        등록된 수업이 없습니다.
                    </td>
                </tr>`;
            calculateTotal();
            return;
        }

        list.forEach(item => {
            tbody.insertAdjacentHTML("beforeend", `
                <tr data-class-key="${item.classKey}"
                    data-unit-key="${item.unitKey}">
                    <td>${item.className}</td>
                    <td>${item.unitName}</td>
                    <td>${item.baseCount}</td>
                </tr> 
            `);
        });

        calculateTotal();
    }

    const totalSpan = document.querySelector(".all-order span");

    function calculateTotal() {
        const rows = document.querySelectorAll("#order-left-body tr");
        let total = 0;

        rows.forEach(row => {
            const baseTd = row.querySelector("td:nth-child(3)");
            const baseCount = parseInt(baseTd.innerText) || 0;
            total += (baseCount);
        });

        if (totalSpan) {
            totalSpan.innerText = total;
        }
    }

    if (window.initialBaseList) {
        calculateTotal();
        renderLeftTable(window.initialBaseList);
    } else {
        calculateTotal();
    }

    /* ======= *
     *  RIGHT  *
     * ======= */
    const yearSelect = document.getElementById("order-year");
    const monthSelect = document.getElementById("order-month");

    initYearOptions();
    renderMonthOptions(yearSelect.value);

    loadSavedOrder();
    loadOrderDetail();

    yearSelect.addEventListener("change", async () => {
        renderMonthOptions(yearSelect.value);
        await loadSavedOrder();
        await loadOrderDetail();
    });

    monthSelect.addEventListener("change", async () => {
        await loadSavedOrder();
        await loadOrderDetail();
    });

    function initYearOptions() {
        const now = new Date();
        const ty = now.getFullYear();
        const tm = now.getMonth() + 1;

        const start = new Date(ty, tm - 7, 1);
        const end = new Date(ty, tm, 1);


        const startYear = start.getFullYear();
        const endYear = end.getFullYear();

        yearSelect.innerHTML = "";

        for (let y = startYear; y <= endYear; y++) {
            const opt = document.createElement("option");
            opt.value = y;
            opt.textContent = `${y}년`;
            if (y === ty) opt.selected = true;
            yearSelect.appendChild(opt);
        }
    }

    function renderMonthOptions(selectedYear) {
        const now = new Date();
        const ty = now.getFullYear();
        const tm = now.getMonth() + 1;
        const nextMonth = tm + 1;

        const start = new Date(ty, tm - 7, 1);
        const end = new Date(ty, tm, 1);

        monthSelect.innerHTML = "";

        let d = new Date(start);

        while (d <= end) {
            const yy = d.getFullYear();
            const mm = d.getMonth() + 1;

            if (yy == selectedYear) {
                const opt = document.createElement("option");
                opt.value = String(mm).padStart(2, "0");
                opt.textContent = `${String(mm).padStart(2, "0")}월`;

                if (yy === ty && mm === nextMonth) {
                    opt.selected = true;
                }

                monthSelect.appendChild(opt);
            }

            d.setMonth(d.getMonth() + 1);
        }
    }

    const rightBody = document.getElementById("order-right-body");

    async function loadSavedOrder() {
        try {
            const yy = yearSelect.value;
            const mm = monthSelect.value;
            if (!yy || !mm) return;

            const res = await fetch("/manage/order/list", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({yy, mm})
            });

            if (!res.ok) return;

            const data = await res.json();
            renderSavedOrder(data.response);

        } catch (e) {
            console.error("loadSavedOrder Error:", e);
        }
    }

    function renderSavedOrder(list) {
        rightBody.innerHTML = "";

        if (!list || list.length === 0) {
            rightBody.innerHTML = `
                <tr>
                    <td colspan="4" style="text-align:center; padding:20px;">
                        주문 내역이 없습니다.
                    </td>
                </tr>`;
            return;
        }

        list.forEach(item => {

            rightBody.insertAdjacentHTML("beforeend", `
                <tr>
                    <td>${item.className}</td>
                    <td>${item.unitName}</td>
                    <td>${item.baseCount}</td>
                    <td>${item.totalCount}</td>
                </tr>
            `);
        });
    }

    async function loadOrderDetail() {
        const bottomWrap = document.getElementById("preorder-bottom-wrap");
        if (!bottomWrap) return;

        try {
            const yy = yearSelect.value;
            const mm = monthSelect.value;
            if (!yy || !mm) return;

            // 로딩 표시
            bottomWrap.innerHTML = `
                <div class="bottom-loading">
                    <span>불러오는 중...</span>
                </div>`;

            const res = await fetch("/manage/order/detail/list", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({yy, mm})
            });

            if (!res.ok) {
                bottomWrap.innerHTML = `<p class="bottom-empty">데이터를 불러오지 못했습니다.</p>`;
                return;
            }

            const data = await res.json();
            renderOrderDetail(data.response, yy, mm);

        } catch (e) {
            console.error("loadOrderDetail Error:", e);
            bottomWrap.innerHTML = `<p class="bottom-empty">오류가 발생했습니다.</p>`;
        }
    }

    function renderOrderDetail(detail, yy, mm) {
        const bottomWrap = document.getElementById("preorder-bottom-wrap");
        if (!bottomWrap) return;

        if (!detail || detail.length === 0) {
            bottomWrap.innerHTML = `<p class="bottom-empty">${yy}년 ${mm}월 주문 내역이 없습니다.</p>`;
            return;
        }

        // className + unitName 기준으로 그룹핑
        const groupMap = new Map();

        detail.forEach(item => {
            const key = `${item.classKey}_${item.unitKey}`;
            const count = parseInt(item.count) || 0;

            if (!groupMap.has(key)) {
                groupMap.set(key, {
                    className:    item.className,
                    unitName:     item.unitName,
                    studentCount: 0,   // base
                    teacherCount: 0,   // (없으면 0)
                    addCount:     0,   // add - return
                    totalCount:   0
                });
            }

            const group = groupMap.get(key);

            if (item.type === 'base') {
                group.studentCount += count;
            } else if (item.type === 'ADD') {
                group.addCount += count;
            } else if (item.type === 'RETURN') {
                group.addCount -= count;  // 반품은 마이너스
            }
        });

        // 합계 계산 및 총합
        let sumStudent = 0, sumTeacher = 0, sumAdd = 0, sumTotal = 0;

        let rows = "";
        groupMap.forEach(group => {
            group.totalCount = group.studentCount + group.teacherCount + group.addCount;

            sumStudent += group.studentCount;
            sumTeacher += group.teacherCount;
            sumAdd     += group.addCount;
            sumTotal   += group.totalCount;

            // 추가 표시: 음수면 -N, 양수면 N
            const addDisplay = group.addCount < 0
                ? `<span style="color:red;">${group.addCount}</span>`
                : group.addCount;

            rows += `
            <tr>
                <td>${group.className}</td>
                <td>${group.unitName}</td>
                <td>${group.studentCount}</td>
                <td>${group.teacherCount}</td>
                <td>${addDisplay}</td>
                <td>${group.totalCount}</td>
            </tr>`;
        });

        const html = `
        <div class="list-code2">
            <h2>${yy}년 ${mm}월 선생님 주문 내역</h2>
            <table class="listRed">
                <colgroup>
                    <col width="20%">
                    <col width="20%">
                    <col width="15%">
                    <col width="15%">
                    <col width="15%">
                    <col width="15%">
                </colgroup>
                <thead>
                    <tr>
                        <th rowspan="2">단계</th>
                        <th rowspan="2">교재</th>
                        <th colspan="4">수량</th>
                    </tr>
                    <tr>
                        <th>학생</th>
                        <th>선생님</th>
                        <th>추가</th>
                        <th>합계</th>
                    </tr>
                </thead>
                <tbody>
                    ${rows}
                    <tr class="listSum">
                        <td colspan="2">합계</td>
                        <td>${sumStudent}</td>
                        <td>${sumTeacher}</td>
                        <td>${sumAdd}</td>
                        <td>${sumTotal}</td>
                    </tr>
                </tbody>
            </table>
        </div>`;

        bottomWrap.innerHTML = html;
    }

    /* =============================== *
     *  주문 마감 기능 (NEW)
     * =============================== */
    const saveBtn = document.querySelector(".save-btn");

    function checkOrderDeadline() {
        const now = new Date();
        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth(); // 0~11

        // 마감 시작일: 매월 11일 00:00
        const deadlineStart = new Date(currentYear, currentMonth, 12, 0, 0, 0);

        // 마감 종료일: 해당 월의 마지막 날 23:59:59
        const deadlineEnd = new Date(currentYear, currentMonth + 1, 0, 23, 59, 59);

        const isDeadlinePeriod = now >= deadlineStart && now <= deadlineEnd;

        if (isDeadlinePeriod) {
            saveBtn.disabled = true;
            saveBtn.classList.add('disabled');
            saveBtn.style.opacity = '0.5';
            saveBtn.style.cursor = 'not-allowed';
        } else {
            saveBtn.disabled = false;
            saveBtn.classList.remove('disabled');
            saveBtn.style.opacity = '1';
            saveBtn.style.cursor = 'pointer';
        }

        return isDeadlinePeriod;
    }

    function showDeadlineModal() {
        // 모달 HTML 생성
        const modalHTML = `
            <div id="deadline-modal" style="
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background-color: rgba(0, 0, 0, 0.5);
                display: flex;
                justify-content: center;
                align-items: center;
                z-index: 9999;
            ">
                <div style="
                    background: white;
                    padding: 30px;
                    border-radius: 8px;
                    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                    text-align: center;
                    max-width: 400px;
                ">
                    <h3 style="margin-bottom: 20px; color: #333;">주문 마감 안내</h3>
                    <p style="margin-bottom: 25px; line-height: 1.6; color: #666;">
                        주문이 마감되었습니다.<br>
                        <strong>추가 주문 / 반품</strong> 탭을 이용해주세요.
                    </p>
                    <button id="modal-close-btn" style="
                        background-color: #007bff;
                        color: white;
                        border: none;
                        padding: 10px 30px;
                        border-radius: 4px;
                        cursor: pointer;
                        font-size: 16px;
                    ">확인</button>
                </div>
            </div>
        `;

        // 모달을 body에 추가
        document.body.insertAdjacentHTML('beforeend', modalHTML);

        // 확인 버튼 클릭 시 모달 제거
        document.getElementById('modal-close-btn').addEventListener('click', () => {
            document.getElementById('deadline-modal').remove();
        });

        // 배경 클릭 시에도 모달 제거
        document.getElementById('deadline-modal').addEventListener('click', (e) => {
            if (e.target.id === 'deadline-modal') {
                document.getElementById('deadline-modal').remove();
            }
        });
    }

    // 페이지 로드 시 마감 여부 체크
    checkOrderDeadline();

    saveBtn.addEventListener("click", async () => {
        // 마감 기간인지 체크
        if (checkOrderDeadline()) {
            showDeadlineModal();
            return;
        }
        if (!confirm("교재를 주문하시겠습니까?")) {
            return;
        }


        try {
            const ymRaw = monthInput.value;
            if (!ymRaw) {
                alert("주문년월을 선택해주세요.");
                return;
            }

            const ym = ymRaw.replace("-", "");
            const rows = document.querySelectorAll("#order-left-body tr");
            const insertOrders = [];

            rows.forEach(row => {
                const baseCount = parseInt(row.querySelector("td:nth-child(3)").innerText);

                insertOrders.push({
                    classKey: row.dataset.classKey,
                    unitKey: row.dataset.unitKey,
                    baseCount: baseCount,
                });
            });

            const requestData = {
                yy: ym.substring(0, 4),
                mm: ym.substring(4, 6),
                insertOrders: insertOrders
            };

            const res = await fetch("/manage/order/save", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(requestData)
            });

            if (!res.ok) {
                alert("저장에 실패했습니다.");
                return;
            }
            alert("저장 되었습니다.");
            window.location.reload();

        } catch (e) {
            alert("에러 발생");
        }
    });

});