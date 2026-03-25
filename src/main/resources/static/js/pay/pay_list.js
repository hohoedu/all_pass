/* ===============================
    전역 상태
=============================== */
let currentPayments = [];
let unpaidStudents = [];
let cashPaymentStudents = [];
let selectedYear = '';
let selectedMonth = '';
let selectedStudentsForPayment = [];

document.addEventListener("DOMContentLoaded", () => {

    /* ===============================
        DOM 캐싱
    =============================== */
    const tbody = document.getElementById("payment-list-body");
    const monthInput = document.querySelector(".hidden-date");
    const calendarBtn = document.querySelector(".calendar-open");
    const currentMonthEl = document.querySelector(".current-month");

    const btnAddPayment = document.querySelector("#btn-add-payment");
    const btnAddCashbill = document.querySelector("#btn-add-cashbill");
    const modalPayment = document.querySelector(".add-payment-modal");
    const modalCashbill = document.querySelector(".add-cashbill-modal");

    const studentTableBody = document.querySelector("#unpaid-student-list");
    const cashbillStudentTableBody = document.querySelector("#cashbill-student-list");
    const cashbillSearchInput = document.querySelector(".add-cashbill-modal .add-inform .basic-input");
    const cashbillSearchBtn = document.querySelector("#search-cashbill-student");
    const manualSearchInput = document.querySelector(".add-payment-modal .student-content .basic-input");
    const manualSearchBtn = document.querySelector("#search-unpaid-student");

    /* ===============================
        초기 바인딩
    =============================== */
    initMonthFromUrl();
    bindMonthPicker();

    const [yy, mm] = monthInput.value.split('-');
    fetchPayments(yy, mm);

    /* ===============================
        URL 월 초기화
    =============================== */
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
            const next = new Date(now.getFullYear(), now.getMonth() + 1, 1);
            y = next.getFullYear();
            m = String(next.getMonth() + 1).padStart(2, '0');
        }

        monthInput.value = `${y}-${m}`;
        currentMonthEl.textContent = `${y}년 ${parseInt(m)}월`;
        selectedYear = y;
        selectedMonth = m;
    }

    /* ===============================
        월 선택 달력
    =============================== */
    function bindMonthPicker() {
        calendarBtn?.addEventListener('click', () => {
            if (typeof monthInput.showPicker === 'function') {
                monthInput.showPicker();
            } else {
                monthInput.focus();
                monthInput.click();
            }
        });

        monthInput?.addEventListener('change', () => {
            const [y, m] = monthInput.value.split('-');
            currentMonthEl.textContent = `${y}년 ${parseInt(m)}월`;

            const url = new URL(location.href);
            url.searchParams.set('year', y);
            url.searchParams.set('month', m);
            location.href = url.toString();
        });

        // 모달 내 캘린더 버튼 처리
        document.addEventListener("click", e => {
            const btn = e.target.closest(".calendar-open");
            if (!btn) return;

            const targetId = btn.dataset.target;
            if (!targetId) return;

            const input = document.getElementById(targetId);
            if (!input) return;

            if (typeof input.showPicker === "function") {
                input.showPicker();
            } else {
                input.focus();
                input.click();
            }
        });
    }

    /* ===============================
        유틸 함수
    =============================== */
    const formatKoreanDate = (dateStr) => {
        if (!dateStr) return '';
        const [y, m, d] = dateStr.split('-');
        return `${y}년 ${Number(m)}월 ${Number(d)}일`;
    };

    const getTodayForDateInput = () => {
        const d = new Date();
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    };

    const closeAllModals = () =>
        document.querySelectorAll(".modal").forEach(m => m.style.display = "none");

    const initPayDate = () => {
        const input = document.getElementById('manual-pay-date');
        const text = document.getElementById('pay-date-text');
        if (!input || !text) return;

        if (!input.value) input.value = getTodayForDateInput();
        text.textContent = formatKoreanDate(input.value);
    };

    document.getElementById('manual-pay-date')?.addEventListener('change', e => {
        const text = document.getElementById('pay-date-text');
        if (text) text.textContent = formatKoreanDate(e.target.value);
    });

    /* ===============================
        데이터 조회
    =============================== */
    async function fetchPayments(year, month) {
        try {
            tbody.style.visibility = 'hidden';

            const res = await fetch('/pay/list/payments', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    year,
                    month: String(month).padStart(2, '0')
                })
            });

            if (!res.ok) throw new Error();

            const json = await res.json();
            currentPayments = json.response || json;
            renderPaymentList(currentPayments);

        } catch (e) {
            console.error(e);
            alert('결제 목록을 불러오지 못했습니다.');
        }
    }

    /* ===============================
        렌더링
    =============================== */
    function renderPaymentList(list) {
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            tbody.innerHTML = `
            <tr>
                <td colspan="9" style="text-align: center; padding: 40px;">
                    조회된 결제 내역이 없습니다.
                </td>
            </tr>`;
            tbody.style.visibility = 'visible';
            return;
        }

        const fragment = document.createDocumentFragment();

        list.forEach((payment, index) => {
            const tr = document.createElement('tr');

            /* ── 수납방법 판별 ── */
            const cash = Number(payment.cash || 0);
            const card = Number(payment.card || 0);
            const transfer = Number(payment.transfer || 0);
            const totalPaid = cash + card + transfer;

            const methodParts = [];
            if (card > 0) methodParts.push({label: '카드', cls: 'card-offline', amount: card});
            if (cash > 0) methodParts.push({label: '현금', cls: 'cash-only', amount: cash});
            if (transfer > 0) methodParts.push({label: '계좌이체', cls: 'account-transfer', amount: transfer});

            const methodBtns = methodParts.map(m => `
            <button class="method-box ${m.cls}">
                <span>${m.label}</span>
                <span class="total-amount">${m.amount.toLocaleString('ko-KR')}</span>
            </button>
        `).join('');

            const totalBtn = `
            <button class="method-box mthod-total">
                <span>합계</span>
                <span class="total-amount">${totalPaid.toLocaleString('ko-KR')}</span>
            </button>`;

            /* ── 결제금액 셀 ── */
            const paidPriceCell = totalPaid > 0
                ? `<span class="origin">${totalPaid.toLocaleString('ko-KR')}</span>`
                : `<input type="number" name="overpayment" value="0" class="td-input">`;

            /* ── 현금영수증 아이콘 ── */
            const cashbillIcon = payment.apprCashNum
                ? `<img src="/image/bill1.png" class="bill-icon" alt="발급완료" title="${payment.apprCashNum}">`
                : (cash > 0 || transfer > 0)
                    ? `<img src="/image/bill_empty.png" class="bill-icon" alt="미발급" title="현금영수증 미발급">`
                    : '';

            tr.innerHTML = `
            <td class="checkbox-group">
                <input type="checkbox" data-id="${payment.id}">
            </td>
            <td>${index + 1}</td>
            <td class="pay-day">${payment.paidDate || ''}</td>
            <td class="stu-name">${payment.studentName || ''}</td>
         
            <td>
                <div class="method-boxes">
                    ${methodBtns}
                    ${totalBtn}
                </div>
            </td>
            <td><div class="td-inputs">${paidPriceCell}</div></td>
            <td><span>-</span></td>
            <td>${cashbillIcon}</td>
            <td>
        <div class="common-btn btn-edit pay-desc" 
             data-id="${payment.id}"
             style="margin-inline: 10px; padding-block: 0px;">수정</div>
    </td>
    <td>
        <div class="common-btn btn-delete pay-remind" 
             data-id="${payment.id}" 
             data-name="${payment.studentName}" 
             data-date="${payment.paidDate}" 
             data-amount="${totalPaid}"
             style="margin-inline: 10px; padding-block: 0px;">삭제</div>
    </td>
        `;

            fragment.appendChild(tr);
        });

        tbody.appendChild(fragment);
        tbody.style.visibility = 'visible';
    }

    function bindRowActions() {
        tbody.addEventListener('click', e => {

            /* ── 삭제 버튼 ── */
            const deleteBtn = e.target.closest('.btn-delete');
            if (deleteBtn) {
                const {id, name, date, amount} = deleteBtn.dataset;

                Swal.fire({
                    title: '결제 내역 삭제',
                    html: `
                    <div style="text-align:left; line-height:2;">
                        <b>학생:</b> ${name}<br>
                        <b>결제일:</b> ${date}<br>
                        <b>금액:</b> ${Number(amount).toLocaleString('ko-KR')}원
                    </div>
                    <p style="color:#e74c3c; margin-top:12px;">정말 삭제하시겠습니까?</p>
                `,
                    icon: 'warning',
                    showCancelButton: true,
                    confirmButtonColor: '#e74c3c',
                    cancelButtonColor: '#aaa',
                    confirmButtonText: '삭제',
                    cancelButtonText: '취소'
                }).then(async result => {
                    if (result.isConfirmed) {
                        await deletePayment(id);
                    }
                });
                return;
            }

            /* ── 수정 버튼 ── */
            const editBtn = e.target.closest('.btn-edit');
            if (editBtn) {
                const id = editBtn.dataset.id;
                const payment = currentPayments.find(p => String(p.id) === String(id));
                if (payment) openEditModal(payment);
            }
        });
    }

    bindRowActions();

    function openEditModal(payment) {
        const cash = Number(payment.cash || 0);
        const card = Number(payment.card || 0);
        const transfer = Number(payment.transfer || 0);
        const total = cash + card + transfer;

        // 모달 요소
        const modal = document.getElementById('edit-payment-modal');

        // 학생명 / 청구금액
        document.getElementById('edit-student-name-cell').textContent = payment.studentName || '-';
        document.getElementById('edit-bill-amount-cell').textContent = total.toLocaleString('ko-KR') + ' 원';

        // 결제일
        const paidDateInput = document.getElementById('edit-paid-date');
        const paidDateText = document.getElementById('edit-pay-date-text');
        paidDateInput.value = payment.paidDate || '';
        paidDateText.textContent = formatKoreanDate(payment.paidDate);

        // 결제일 변경 이벤트
        paidDateInput.onchange = (e) => {
            paidDateText.textContent = formatKoreanDate(e.target.value);
        };

        // 금액
        document.getElementById('edit-card-amount').value = card || '';
        document.getElementById('edit-cash-amount').value = cash || '';
        document.getElementById('edit-transfer-amount').value = transfer || '';

        // 카드사 (card_name 필드가 있으면 세팅)
        const cardSelect = document.getElementById('edit-card-select');
        if (cardSelect && payment.cardName) {
            cardSelect.value = payment.cardName;
        }

        // 현재 수정 중인 id
        modal.dataset.currentId = payment.id;

        // 닫기 / 취소 / 저장 이벤트 (중복 방지)
        const closeBtn = document.getElementById('edit-modal-close');
        const saveBtn = document.getElementById('edit-modal-save');

        closeBtn.onclick = () => modal.style.display = 'none';
        saveBtn.onclick = () => saveEditPayment();

        modal.style.display = 'block';
    }

    async function deletePayment(id) {
        try {
            const res = await fetch(`/pay/manual/delete/${id}`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'}
            });

            const result = await res.json();

            if (result.success) {
                Swal.fire('삭제 완료', '결제 내역이 삭제되었습니다.', 'success')
                    .then(() => {
                        const [yy, mm] = monthInput.value.split('-');
                        fetchPayments(yy, mm);
                    });
            } else {
                Swal.fire('실패', result.message || '삭제 중 오류가 발생했습니다.', 'error');
            }
        } catch (err) {
            console.error(err);
            Swal.fire('오류', '삭제 처리 중 오류가 발생했습니다.', 'error');
        }
    }

    /* ── 수정 저장 API ── */
    async function saveEditPayment() {
        const modal = document.getElementById('edit-payment-modal');
        const id = modal.dataset.currentId;

        const paidDate = document.getElementById('edit-paid-date').value;
        const cardAmount = Number(document.getElementById('edit-card-amount').value || 0);
        const cashAmount = Number(document.getElementById('edit-cash-amount').value || 0);
        const transferAmount = Number(document.getElementById('edit-transfer-amount').value || 0);
        const cardName = document.getElementById('edit-card-select').value || '';

        if (!paidDate) {
            Swal.fire('알림', '결제일을 선택해주세요.', 'warning');
            return;
        }

        if (cardAmount + cashAmount + transferAmount === 0) {
            Swal.fire('알림', '결제 금액을 입력해주세요.', 'warning');
            return;
        }

        if (cardAmount > 0 && !cardName) {
            Swal.fire('알림', '카드사를 선택해주세요.', 'warning');
            return;
        }

        const dto = {paidDate, cardAmount, cashAmount, transferAmount, cardName};

        try {
            const res = await fetch(`/pay/manual/update/${id}`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(dto)
            });

            const result = await res.json();

            if (result.success) {
                Swal.fire('수정 완료', '결제 내역이 수정되었습니다.', 'success')
                    .then(() => {
                        modal.style.display = 'none';
                        const [yy, mm] = monthInput.value.split('-');
                        fetchPayments(yy, mm);
                    });
            } else {
                Swal.fire('실패', result.message || '수정 중 오류가 발생했습니다.', 'error');
            }
        } catch (err) {
            console.error(err);
            Swal.fire('오류', '수정 처리 중 오류가 발생했습니다.', 'error');
        }
    }

    /* ===============================
        검색 / 필터
    =============================== */
    function bindSearch() {
        const searchBtn = document.querySelector(".explore");
        const searchInput = document.getElementById("search-name");
        const stuNameSelect = document.getElementById("stu-name");

        const doSearch = () => {
            const keyword = searchInput?.value.trim() || '';

            tbody.querySelectorAll('tr').forEach(tr => {
                const name = tr.querySelector('.stu-name')?.textContent || '';
                tr.style.display = (!keyword || name.includes(keyword)) ? '' : 'none';
            });
        };

        stuNameSelect?.addEventListener('change', () => {
            if (searchInput) searchInput.value = '';
        });

        searchBtn?.addEventListener('click', doSearch);

        searchInput?.addEventListener('keydown', e => {
            if (e.isComposing) return;
            if (e.key === 'Enter') {
                e.preventDefault();
                doSearch();
            }
        });
    }

    bindSearch();

    let savedManualReceiptNumber = '';
    const manualReceiptTypeRadios = document.querySelectorAll('.add-payment-modal input[name="receipt-type"]');
    const manualReceiptNumberInput = document.querySelector('.add-payment-modal #receipt-number');

    manualReceiptTypeRadios.forEach(radio => {
        radio.addEventListener('change', function () {
            if (this.value === 'self') {
                if (manualReceiptNumberInput.value !== '0100001234') {
                    savedManualReceiptNumber = manualReceiptNumberInput.value;
                }
                manualReceiptNumberInput.value = '0100001234';
                manualReceiptNumberInput.readOnly = true;
                manualReceiptNumberInput.style.backgroundColor = '#f5f5f5';
            } else {
                if (manualReceiptNumberInput.value === '0100001234') {
                    manualReceiptNumberInput.value = savedManualReceiptNumber;
                }
                manualReceiptNumberInput.readOnly = false;
                manualReceiptNumberInput.style.backgroundColor = '';
                manualReceiptNumberInput.placeholder = '휴대폰번호(01012345678) 또는 사업자번호';
            }
        });
    });
    /* ========================================
        📝 수기 결제 모달 - 학생 검색 및 선택
    ======================================== */
    // ⭐ 빈 리스트 렌더링 함수 추가
    const renderEmptyStudentList = () => {
        if (!studentTableBody) return;
        studentTableBody.innerHTML = `
            <tr>
                <td colspan="4" style="text-align:center; padding: 30px; color: #666;">
                    학생 이름을 검색해주세요.
                </td>
            </tr>`;
    };

    const renderStudentList = list => {
        if (!studentTableBody) return;
        studentTableBody.innerHTML = "";

        if (!list.length) {
            studentTableBody.innerHTML =
                `<tr><td colspan="4" class="empty">결제 가능 학생이 없습니다.</td></tr>`;
            return;
        }

        list.forEach(s => {
            const tr = document.createElement("tr");
            tr.dataset.billId = s.billId;
            tr.dataset.studentId = s.studentId;
            tr.dataset.paymentKey = s.paymentKey;
            tr.dataset.phoneNumber = s.phoneNumber;

            tr.innerHTML = `
                <td>${s.studentName}</td>
                <td>${s.gradeName || "-"}</td>
                <td>${[s.hanTeacher, s.bookTeacher].filter(v => v?.trim()).join(",")}</td>
                <td>${s.phoneNumber.substring(3, 12)}</td>
            `;

            tr.addEventListener("click", async () => {
                document.querySelectorAll("#unpaid-student-list tr").forEach(r => r.classList.remove("selected"));
                tr.classList.add("selected");

                const siblings = unpaidStudents.filter(student =>
                    student.phoneNumber === s.phoneNumber &&
                    student.studentId !== s.studentId
                );

                let totalAmount = Number(s.amount);
                let displayName = s.studentName;

                selectedStudentsForPayment = [s];

                if (siblings.length > 0) {
                    const siblingNames = siblings.map(sib => sib.studentName).join(", ");
                    const confirmMessage = `형제가 있습니다.(${siblingNames})\n함께 결제하시겠습니까?`;

                    if (confirm(confirmMessage)) {
                        totalAmount = Number(s.amount) + siblings.reduce((sum, sib) => sum + Number(sib.amount), 0);
                        displayName = [s.studentName, ...siblings.map(sib => sib.studentName)].join(", ");
                        // ⭐ 형제도 함께 저장
                        selectedStudentsForPayment = [s, ...siblings];
                    }
                }

                const nameCell = document.querySelector("#paid-student td:nth-child(2)");
                if (nameCell) nameCell.textContent = displayName;

                const amountCell = document.querySelector("#paid-student td:nth-child(4)");
                if (amountCell) amountCell.textContent = totalAmount.toLocaleString('ko-KR') + ' 원';

                const receiptNumberInput = document.querySelector(".add-payment-modal #receipt-number");
                if (receiptNumberInput && s.phoneNumber) {
                    const cleanPhone = s.phoneNumber.replace(/[^0-9]/g, '');
                    receiptNumberInput.value = cleanPhone;
                }
            });

            studentTableBody.appendChild(tr);
        });
    };

    // ⭐ 검색 로직 수정
    const filterStudents = keyword => {
        keyword = keyword.trim();

        // 공백이거나 빈 값이면 검색 안내 메시지 표시
        if (!keyword) {
            renderEmptyStudentList();
            return;
        }

        const matchedStudents = [];
        const phoneNumbersToInclude = new Set();

        // 숫자만 추출 (전화번호 검색용)
        const numericKeyword = keyword.replace(/[^0-9]/g, '');

        // ⭐ 전화번호 검색인지 판단 (숫자만 있고 4자리 이상)
        const isPhoneSearch = numericKeyword.length >= 4 && numericKeyword === keyword.replace(/[^0-9]/g, '');

        if (isPhoneSearch) {
            // ========== 전화번호 검색 ==========
            unpaidStudents.forEach(s => {
                if (!s.phoneNumber) return;

                // 학생 전화번호에서 숫자만 추출
                const cleanPhone = s.phoneNumber.replace(/[^0-9]/g, '');

                // ⭐ 4자리 이상 일치하는지 확인
                // 예: 입력 "1234" → "01012345678"에서 "1234" 찾기
                // 예: 입력 "01012345678" → "01012345678"과 완전 일치
                if (cleanPhone.includes(numericKeyword)) {
                    phoneNumbersToInclude.add(s.phoneNumber);
                }
            });
        } else {
            // ========== 이름 검색 ==========
            unpaidStudents.forEach(s => {
                if (s.studentName?.includes(keyword)) {
                    phoneNumbersToInclude.add(s.phoneNumber);
                }
            });
        }

        // 같은 전화번호를 가진 모든 학생들 포함
        unpaidStudents.forEach(s => {
            if (phoneNumbersToInclude.has(s.phoneNumber)) {
                matchedStudents.push(s);
            }
        });

        // 결과가 없으면 메시지 표시
        if (matchedStudents.length === 0) {
            studentTableBody.innerHTML = `
            <tr>
                <td colspan="4" style="text-align:center; padding: 30px; color: #666;">
                    검색 결과가 없습니다.
                </td>
            </tr>`;
            return;
        }

        renderStudentList(matchedStudents);
    };

    if (manualSearchInput) {
        manualSearchInput.addEventListener("keydown", e => {
            if (e.key === "Enter") {
                e.preventDefault();
                filterStudents(manualSearchInput.value);
            }
        });
    }

    if (manualSearchBtn) {
        manualSearchBtn.addEventListener("click", () => filterStudents(manualSearchInput.value));
    }

    /* ========================================
        📝 수기 결제 모달 - 현금영수증 자동 입력
    ======================================== */
    const setupManualPaymentCashbill = () => {
        const payInputs = document.querySelectorAll('.add-payment-modal .pay-input');
        const payRadios = document.querySelectorAll('.add-payment-modal .pay-radio');
        const cashPriceInput = document.querySelector(".add-payment-modal #cash-price");
        const supplyPriceInput = document.querySelector(".add-payment-modal #supply-price");
        const taxPriceInput = document.querySelector(".add-payment-modal #tax-price");
        const receiptTable = document.querySelector(".add-payment-modal .cashbill-section");

        const manualReceiptDateInput = document.getElementById('manual-receipt-date');
        const manualReceiptDateText = document.getElementById('manual-receipt-date-text');

        if (manualReceiptDateInput && manualReceiptDateText) {
            const today = new Date();
            const yyyy = today.getFullYear();
            const mm = String(today.getMonth() + 1).padStart(2, '0');
            const dd = String(today.getDate()).padStart(2, '0');
            manualReceiptDateInput.value = `${yyyy}-${mm}-${dd}`;
            manualReceiptDateText.textContent = `${yyyy}년 ${today.getMonth() + 1}월 ${today.getDate()}일`;

            manualReceiptDateInput.addEventListener('change', function () {
                if (this.value) {
                    const date = new Date(this.value);
                    const year = date.getFullYear();
                    const month = date.getMonth() + 1;
                    const day = date.getDate();
                    manualReceiptDateText.textContent = `${year}년 ${month}월 ${day}일`;
                }
            });
        }

        const calculateTax = () => {
            if (!cashPriceInput || !supplyPriceInput || !taxPriceInput) return;

            const totalAmount = Number(cashPriceInput.value.replace(/,/g, '')) || 0;

            if (totalAmount === 0) {
                supplyPriceInput.value = '';
                taxPriceInput.value = '';
                return;
            }

            const taxType = document.querySelector('.add-payment-modal input[name="tax-type"]:checked')?.value;

            if (taxType === 'taxable') {
                const supplyPrice = Math.round(totalAmount / 1.1);
                const taxPrice = totalAmount - supplyPrice;
                supplyPriceInput.value = supplyPrice.toLocaleString('ko-KR');
                taxPriceInput.value = taxPrice.toLocaleString('ko-KR');
            } else {
                supplyPriceInput.value = totalAmount.toLocaleString('ko-KR');
                taxPriceInput.value = '0';
            }
        };

        payInputs.forEach((input, index) => {
            input.addEventListener('input', (e) => {
                const amount = Number(e.target.value) || 0;

                if (amount > 0) {
                    payRadios[index].checked = true;
                    payRadios[index].dispatchEvent(new Event('change'));
                }

                if ((index === 1 || index === 2) && amount > 0) {
                    if (cashPriceInput) {
                        cashPriceInput.value = amount.toLocaleString('ko-KR');
                        calculateTax();
                    }
                    if (receiptTable) {
                        receiptTable.classList.add('show');
                    }
                } else if (index === 0) {
                    if (receiptTable) {
                        receiptTable.classList.remove('show');
                    }
                }
            });
        });

        if (cashPriceInput) {
            cashPriceInput.addEventListener('input', (e) => {
                let value = e.target.value.replace(/[^0-9]/g, '');
                if (value) {
                    e.target.value = parseInt(value).toLocaleString('ko-KR');
                } else {
                    e.target.value = '';
                }
                calculateTax();
            });
        }

        const taxTypeRadios = document.querySelectorAll('.add-payment-modal input[name="tax-type"]');
        taxTypeRadios.forEach(radio => {
            radio.addEventListener('change', calculateTax);
        });
    };

    /* ========================================
        💰 현금영수증 발급 모달 - 학생 목록
    ======================================== */
    const renderCashbillStudentList = list => {
        if (!cashbillStudentTableBody) return;
        cashbillStudentTableBody.innerHTML = "";

        if (!list.length) {
            cashbillStudentTableBody.innerHTML =
                `<tr><td colspan="3" style="text-align: center; padding: 20px;">현금/계좌이체 결제 내역이 없습니다.</td></tr>`;
            return;
        }

        list.forEach((s, index) => {
            const tr = document.createElement("tr");
            tr.style.cursor = "pointer";
            tr.dataset.index = index;
            tr.dataset.studentId = s.studentId;
            tr.dataset.paymentKey = s.paymentKey;
            tr.dataset.amount = s.amount;
            tr.dataset.phoneNumber = s.phoneNumber || '';

            tr.innerHTML = `
                <td>${s.studentName}</td>
                <td>${s.gradeName || "-"}</td>
                <td>${[s.hanTeacher, s.bookTeacher].filter(v => v?.trim()).join(",") || "-"}</td>
            `;

            tr.addEventListener("click", () => {
                document.querySelectorAll("#cashbill-student-list tr").forEach(r => {
                    r.classList.remove("selected");
                    r.style.backgroundColor = "";
                });

                tr.classList.add("selected");
                tr.style.backgroundColor = "#e3f2fd";

                window.selectedCashbillStudent = {
                    studentId: s.studentId,
                    paymentKey: s.paymentKey,
                    name: s.studentName,
                    grade: s.gradeName,
                    teacher: [s.hanTeacher, s.bookTeacher].filter(v => v?.trim()).join(","),
                    amount: s.amount,
                    phoneNumber: s.phoneNumber || ''
                };

                const nameCell = document.getElementById("student-name-cell");
                const amountCell = document.getElementById("cash-amount-cell");
                if (nameCell) nameCell.textContent = s.studentName;
                if (amountCell) amountCell.textContent = parseInt(s.amount).toLocaleString() + "원";

                const cashPriceInput = document.getElementById("cash-price");
                if (cashPriceInput) {
                    cashPriceInput.value = parseInt(s.amount).toLocaleString();
                    cashPriceInput.dispatchEvent(new Event('input'));
                }

                const receiptNumberInput = document.getElementById("receipt-number");
                const receiptType = document.querySelector('input[name="receipt-type"]:checked').value;

                if (receiptNumberInput && s.phoneNumber && receiptType === 'personal') {
                    const cleanPhone = s.phoneNumber.replace(/[^0-9]/g, '');
                    receiptNumberInput.value = cleanPhone;
                }
            });

            cashbillStudentTableBody.appendChild(tr);
        });
    };

    const filterCashbillStudents = keyword => {
        keyword = keyword.trim();
        if (!keyword) {
            renderCashbillStudentList(cashPaymentStudents);
            return;
        }
        const filtered = cashPaymentStudents.filter(s =>
            s.studentName?.includes(keyword) ||
            s.gradeName?.includes(keyword) ||
            s.hanTeacher?.includes(keyword) ||
            s.bookTeacher?.includes(keyword)
        );
        renderCashbillStudentList(filtered);
    };

    if (cashbillSearchInput) {
        cashbillSearchInput.addEventListener("keydown", e => {
            if (e.key === "Enter") {
                e.preventDefault();
                filterCashbillStudents(cashbillSearchInput.value);
            }
        });
    }

    if (cashbillSearchBtn) {
        cashbillSearchBtn.addEventListener("click", () => filterCashbillStudents(cashbillSearchInput.value));
    }

    /* -----------------------------
        모달 열기: 수기 결제 추가
    ----------------------------- */
    document.getElementById('manual-pay-date')?.addEventListener('change', e => {
        const text = document.getElementById('pay-date-text');
        if (!text) return;
        text.textContent = formatKoreanDate(e.target.value);
    });

    const openAddPaymentModal = async () => {
        try {
            closeAllModals();
            const yearMonth = {
                year: monthInput.value.split("-")[0],
                month: monthInput.value.split("-")[1]
            }
            const res = await fetch("/pay/list/students", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(yearMonth)
            });

            if (!res.ok) throw new Error("학생 조회 실패");

            const students = await res.json();
            unpaidStudents = students.response || [];

            selectedStudentsForPayment = [];

            initPayDate();
            renderEmptyStudentList();

            if (manualSearchInput) {
                manualSearchInput.value = '';
            }

            const payDateInput = document.getElementById("manual-pay-date");
            if (payDateInput) {
                payDateInput.value = getTodayForDateInput();
            }

            const cardCodeSelect = document.getElementById('cardCodeSelect');
            if (cardCodeSelect) cardCodeSelect.value = '';

            const payRadios = document.querySelectorAll('.add-payment-modal .pay-radio');
            payRadios.forEach(radio => radio.checked = false);

            const payInputs = document.querySelectorAll('.add-payment-modal .pay-input');
            payInputs.forEach(input => input.value = '');

            const nameCell = document.querySelector("#paid-student td:nth-child(2)");
            const amountCell = document.querySelector("#paid-student td:nth-child(4)");
            if (nameCell) nameCell.textContent = '학생을 선택해주세요.';
            if (amountCell) amountCell.textContent = '학생을 선택해주세요.';

            const manualReceiptNumber = document.querySelector('.add-payment-modal #receipt-number');
            const manualCashPrice = document.querySelector('.add-payment-modal #cash-price');
            const manualSupplyPrice = document.querySelector('.add-payment-modal #supply-price');
            const manualTaxPrice = document.querySelector('.add-payment-modal #tax-price');

            if (manualReceiptNumber) {
                manualReceiptNumber.value = '';
                manualReceiptNumber.readOnly = false;
                manualReceiptNumber.style.backgroundColor = '';
            }
            if (manualCashPrice) manualCashPrice.value = '';
            if (manualSupplyPrice) manualSupplyPrice.value = '';
            if (manualTaxPrice) manualTaxPrice.value = '';

            const manualPersonalRadio = document.querySelector('.add-payment-modal input[name="receipt-type"][value="personal"]');
            if (manualPersonalRadio) manualPersonalRadio.checked = true;

            const manualTaxableRadio = document.querySelector('.add-payment-modal input[name="tax-type"][value="tax-free"]');
            if (manualTaxableRadio) manualTaxableRadio.checked = true;

            const receiptSection = document.querySelector('.add-payment-modal .cashbill-section');
            if (receiptSection) receiptSection.classList.remove('show');

            document.querySelectorAll("#unpaid-student-list tr").forEach(r => r.classList.remove("selected"));

            modalPayment.style.display = "block";

            setupManualPaymentCashbill();

        } catch (err) {
            alert("납부내역 추가 중 오류가 발생했습니다.");
        }
    };

    btnAddPayment?.addEventListener("click", openAddPaymentModal);

    /* -----------------------------
        수기 결제 저장
    ----------------------------- */
    document.querySelector("#save-pay").addEventListener("click", async () => {
        const payDateInput = document.getElementById("manual-pay-date");
        const selectedRow = document.querySelector("#unpaid-student-list tr.selected");

        if (!selectedRow) {
            alert("학생을 선택하세요.");
            return;
        }

        if (selectedStudentsForPayment.length === 0) {
            alert("학생 데이터를 다시 선택해주세요.");
            return;
        }

        const cardAmount = Number(document.querySelector('.border-green')?.value || 0);
        const cashAmount = Number(document.querySelector('.border-blue')?.value || 0);
        const transferAmount = Number(document.querySelector('.border-olive')?.value || 0);
        const cardCode = document.getElementById('cardCodeSelect')?.value || '';

        if (cardAmount > 0 && !cardCode) {
            alert("카드사를 선택해주세요.");
            return;
        }

        const displayName = document.querySelector("#paid-student td:nth-child(2)")?.textContent || '';
        const studentNames = displayName.split(', ');

        const students = selectedStudentsForPayment.map(s => ({
            studentId: s.studentId,
            paymentKey: s.paymentKey,
            originalAmount: Number(s.amount)
        }));

        let cashbillInfo = null;

        console.log('🔍 cashAmount:', cashAmount);
        console.log('🔍 transferAmount:', transferAmount);

        if (cashAmount > 0 || transferAmount > 0) {
            console.log('✅ 현금/계좌이체 결제 확인됨');

            const receiptSection = document.querySelector(".add-payment-modal .cashbill-section");
            console.log('🔍 receiptSection:', receiptSection);
            console.log('🔍 has show class:', receiptSection?.classList.contains('show'));

            const receiptNumber = document.querySelector('.add-payment-modal #receipt-number')?.value || '';
            const receiptType = document.querySelector('.add-payment-modal input[name="receipt-type"]:checked')?.value || '';
            const taxType = document.querySelector('.add-payment-modal input[name="tax-type"]:checked')?.value || '';
            const cashPrice = document.querySelector('.add-payment-modal #cash-price')?.value.replace(/,/g, '') || '0';
            const supplyPrice = document.querySelector('.add-payment-modal #supply-price')?.value.replace(/,/g, '') || '0';
            const taxPrice = document.querySelector('.add-payment-modal #tax-price')?.value.replace(/,/g, '') || '0';
            const receiptDate = document.getElementById('manual-receipt-date')?.value || '';

            console.log('🔍 receiptNumber:', receiptNumber);
            console.log('🔍 cashPrice:', cashPrice);

            if (receiptNumber && parseInt(cashPrice) > 0) {

                let traderValue = '0';
                if (receiptType === 'business') {
                    traderValue = '1';
                }

                cashbillInfo = {
                    studentId: students[0].studentId,
                    paymentKey: students[0].paymentKey,
                    receiptNumber: receiptNumber,
                    issueDate: receiptDate,
                    price: cashPrice,
                    receiptType: receiptType,
                    supplyPrice: supplyPrice,
                    tax: taxPrice,
                    taxType: taxType,
                    trader: traderValue
                };

                console.log('✅ cashbillInfo 생성됨:', cashbillInfo);
            } else {
                console.warn('❌ 현금영수증 정보 부족 - receiptNumber:', receiptNumber, 'cashPrice:', cashPrice);
            }
        }

        const dto = {
            students: students,
            cardAmount: cardAmount,
            cashAmount: cashAmount,
            transferAmount: transferAmount,
            cardName: cardCode,
            paidDate: payDateInput.value,
            yy: monthInput.value.split("-")[0],
            mm: monthInput.value.split("-")[1],
            cashbillInfo: cashbillInfo
        };

        console.log('📤 최종 전송 데이터:', JSON.stringify(dto, null, 2));

        // ⭐ 결제 확인 메시지 구성
        const confirmStudentNames = selectedStudentsForPayment.map(s => s.studentName).join(', ');
        const totalAmount = (cardAmount + cashAmount + transferAmount).toLocaleString('ko-KR');

        let payMethodText = '';
        if (cardAmount > 0) payMethodText = `카드(${cardCode})`;
        else if (cashAmount > 0) payMethodText = '현금';
        else if (transferAmount > 0) payMethodText = '계좌이체';

        const [y, m, d] = payDateInput.value.split('-');
        const payDateText = `${parseInt(m)}월 ${parseInt(d)}일`;

        const confirmMessage =
            `학생 이름: ${confirmStudentNames}\n` +
            `금액: ${totalAmount}원\n` +
            `결제 방법: ${payMethodText}\n` +
            `결제일: ${payDateText}\n\n` +
            `추가하시겠습니까?`;

        if (!confirm(confirmMessage)) return;

        try {
            const res = await fetch("/pay/manual", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(dto)
            });

            const result = await res.json();

            if (result.success) {
                alert(result.response.message || "수기 결제가 완료되었습니다.");
                location.reload();
            } else {
                alert("수기 결제 실패");
            }
        } catch (err) {
            alert("오류가 발생했습니다.");
        }
    });

    /* -----------------------------
        모달 열기: 현금영수증 발급
    ----------------------------- */
    const openCashbillModal = async () => {
        try {
            closeAllModals();

            const yearMonth = {
                year: monthInput.value.split("-")[0],
                month: monthInput.value.split("-")[1]
            };

            // 1. 발급 대상 학생 조회
            const studentsRes = await fetch("/pay/api/cashbill/students", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(yearMonth)
            });

            if (!studentsRes.ok) throw new Error("현금영수증 발급 대상 학생 조회 실패");

            const studentsResult = await studentsRes.json();
            cashPaymentStudents = studentsResult.response || [];

            renderCashbillStudentList(cashPaymentStudents);

            if (cashbillSearchInput) {
                cashbillSearchInput.value = '';
            }

            // 2. 발행내역 월 선택기 초기화 (최초 1회만 이벤트 바인딩)
            const historyMonthInput = document.getElementById('cashbill-history-month');
            const historyMonthText = document.getElementById('cashbill-history-month-text');
            const historyCalendarBtn = document.getElementById('cashbill-history-calendar-btn');

            if (historyMonthInput && !historyMonthInput._initialized) {
                historyMonthInput._initialized = true;

                historyCalendarBtn?.addEventListener('click', () => {
                    if (typeof historyMonthInput.showPicker === 'function') {
                        historyMonthInput.showPicker();
                    } else {
                        historyMonthInput.focus();
                        historyMonthInput.click();
                    }
                });

                historyMonthInput.addEventListener('change', async () => {
                    const [y, m] = historyMonthInput.value.split('-');
                    historyMonthText.textContent = `${y}년 ${parseInt(m)}월`;

                    try {
                        const res = await fetch("/pay/api/cashbill/history", {
                            method: "POST",
                            headers: {"Content-Type": "application/json"},
                            body: JSON.stringify({year: y, month: m})
                        });
                        if (res.ok) {
                            const result = await res.json();
                            renderCashbillHistory(result.response || []);
                        }
                    } catch (err) {
                        console.error('발행내역 조회 오류:', err);
                    }
                });
            }

            // 모달 열릴 때마다 현재 페이지 월로 초기값 세팅
            if (historyMonthInput) {
                historyMonthInput.value = monthInput.value;
                const [hy, hm] = historyMonthInput.value.split('-');
                if (historyMonthText) historyMonthText.textContent = `${hy}년 ${parseInt(hm)}월`;
            }

            // 3. 발행내역 조회
            try {
                const historyRes = await fetch("/pay/api/cashbill/history", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify(yearMonth)
                });

                if (historyRes.ok) {
                    const historyResult = await historyRes.json();
                    renderCashbillHistory(historyResult.response || []);
                } else {
                    console.warn("발행내역 조회 실패");
                    renderCashbillHistory([]);
                }
            } catch (historyErr) {
                console.error("발행내역 조회 중 오류:", historyErr);
                renderCashbillHistory([]);
            }

            modalCashbill.style.display = "block";

        } catch (err) {
            console.error(err);
            alert("현금영수증 모달을 여는 중 오류가 발생했습니다.");
        }
    };

    btnAddCashbill?.addEventListener("click", openCashbillModal);

    /* -----------------------------
        결제 방법 변경 시 테이블 표시
    ----------------------------- */
    const payMethodRadios = document.querySelectorAll('input[name="pay-method"]');

    payMethodRadios.forEach(radio => {
        radio.addEventListener('change', function () {
            const labelText = this.nextElementSibling.textContent.trim();
            const receiptTable = document.getElementById('receipt-table');

            if (labelText === '현금' || labelText === '계좌이체') {
                receiptTable.classList.add('show');
            } else {
                receiptTable.classList.remove('show');
            }
        });
    });
});


/* ========================================
    💰 현금영수증 발급 모달 전용 로직
======================================== */
document.addEventListener('DOMContentLoaded', function () {
    const taxRadios = document.querySelectorAll('.add-cashbill-modal input[name="tax-type"]');
    const receiptTypeRadios = document.querySelectorAll('.add-cashbill-modal input[name="receipt-type"]');
    const cashPriceInput = document.querySelector('.add-cashbill-modal #cash-price');
    const supplyPriceInput = document.querySelector('.add-cashbill-modal #supply-price');
    const taxPriceInput = document.querySelector('.add-cashbill-modal #tax-price');
    const receiptNumberInput = document.querySelector('.add-cashbill-modal #receipt-number');
    const receiptDateInput = document.getElementById('cashbill-receipt-date');
    const receiptDateText = document.getElementById('cashbill-receipt-date-text');
    const calendarBtn = document.querySelector('.calendar-open[data-target="cashbill-receipt-date"]');
    const issueCashbillBtn = document.getElementById('issue-cashbill');

    let savedReceiptNumber = '';

    if (calendarBtn) {
        calendarBtn.addEventListener('click', function () {
            receiptDateInput.showPicker();
        });
    }

    receiptDateInput.addEventListener('change', function () {
        if (this.value) {
            const date = new Date(this.value);
            const year = date.getFullYear();
            const month = date.getMonth() + 1;
            const day = date.getDate();
            receiptDateText.textContent = `${year}년 ${month}월 ${day}일`;
        }
    });

    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    receiptDateInput.value = `${yyyy}-${mm}-${dd}`;
    receiptDateText.textContent = `${yyyy}년 ${today.getMonth() + 1}월 ${today.getDate()}일`;

    receiptTypeRadios.forEach(radio => {
        radio.addEventListener('change', function () {
            if (this.value === 'self') {
                if (receiptNumberInput.value !== '0100001234') {
                    savedReceiptNumber = receiptNumberInput.value;
                }
                receiptNumberInput.value = '0100001234';
                receiptNumberInput.readOnly = true;
                receiptNumberInput.style.backgroundColor = '#f5f5f5';
            } else {
                if (receiptNumberInput.value === '0100001234') {
                    receiptNumberInput.value = savedReceiptNumber;
                }
                receiptNumberInput.readOnly = false;
                receiptNumberInput.style.backgroundColor = '';
                receiptNumberInput.placeholder = '휴대폰번호(01012345678) 또는 사업자번호';
            }
        });
    });

    receiptNumberInput.addEventListener('blur', function () {
        validateReceiptNumber();
    });

    function validateReceiptNumber() {
        const receiptType = document.querySelector('.add-cashbill-modal input[name="receipt-type"]:checked').value;
        const receiptNumber = receiptNumberInput.value.replace(/[^0-9]/g, '');

        if (receiptType === 'self' || !receiptNumber) {
            return true;
        }

        const isPhoneNumber = /^010\d{7,8}$/.test(receiptNumber);
        const isBusinessNumber = /^\d{10}$/.test(receiptNumber) && !receiptNumber.startsWith('010');

        if (receiptType === 'personal' && !isPhoneNumber) {
            return false;
        }

        if (receiptType === 'business' && !isBusinessNumber) {
            return false;
        }

        return true;
    }

    cashPriceInput.addEventListener('input', function (e) {
        let value = this.value.replace(/[^0-9]/g, '');

        if (value) {
            this.value = parseInt(value).toLocaleString();
        } else {
            this.value = '';
        }

        calculateAmounts();
    });

    taxRadios.forEach(radio => {
        radio.addEventListener('change', function () {
            calculateAmounts();
        });
    });

    function calculateAmounts() {
        const totalAmount = parseInt(cashPriceInput.value.replace(/,/g, '')) || 0;

        if (totalAmount === 0) {
            supplyPriceInput.value = '';
            taxPriceInput.value = '';
            return;
        }

        const isTaxable = document.querySelector('.add-cashbill-modal input[name="tax-type"]:checked').value === 'taxable';
        let supplyPrice, tax;

        if (isTaxable) {
            supplyPrice = Math.round(totalAmount / 1.1);
            tax = totalAmount - supplyPrice;
        } else {
            supplyPrice = totalAmount;
            tax = 0;
        }

        supplyPriceInput.value = supplyPrice.toLocaleString();
        taxPriceInput.value = tax.toLocaleString();
    }

    issueCashbillBtn.addEventListener('click', function () {
        if (!validateAllFields()) {
            return;
        }

        const requestData = collectFormData();
        sendCashbillRequest(requestData);
    });

    function validateAllFields() {
        if (!window.selectedCashbillStudent) {
            alert('학생을 선택해주세요.');
            return false;
        }

        const receiptNumber = receiptNumberInput.value.trim();
        if (!receiptNumber) {
            alert('발급번호를 입력해주세요.');
            receiptNumberInput.focus();
            return false;
        }

        const receiptType = document.querySelector('.add-cashbill-modal input[name="receipt-type"]:checked').value;
        const receiptNumberOnly = receiptNumber.replace(/[^0-9]/g, '');

        if (receiptType === 'personal') {
            const isPhoneNumber = /^010\d{7,8}$/.test(receiptNumberOnly);
            if (!isPhoneNumber) {
                alert('개인 소득공제용은 올바른 휴대폰번호(010-XXXX-XXXX)를 입력해주세요.');
                receiptNumberInput.focus();
                return false;
            }
        } else if (receiptType === 'business') {
            const isBusinessNumber = /^\d{10}$/.test(receiptNumberOnly) && !receiptNumberOnly.startsWith('010');
            if (!isBusinessNumber) {
                alert('사업자 지출증빙용은 올바른 사업자등록번호(10자리)를 입력해주세요.');
                receiptNumberInput.focus();
                return false;
            }
        }

        const cashPrice = cashPriceInput.value.replace(/,/g, '').trim();
        if (!cashPrice || parseInt(cashPrice) <= 0) {
            alert('총 금액을 입력해주세요.');
            cashPriceInput.focus();
            return false;
        }

        const supplyPrice = supplyPriceInput.value.replace(/,/g, '').trim();
        if (!supplyPrice) {
            alert('공급가액이 계산되지 않았습니다.');
            return false;
        }

        const taxPrice = taxPriceInput.value.replace(/,/g, '').trim();
        if (taxPrice === '') {
            alert('세금이 계산되지 않았습니다.');
            return false;
        }

        if (!receiptDateInput.value) {
            alert('발행일자를 선택해주세요.');
            return false;
        }

        return true;
    }

    function collectFormData() {
        const receiptType = document.querySelector('.add-cashbill-modal input[name="receipt-type"]:checked').value;
        const taxType = document.querySelector('.add-cashbill-modal input[name="tax-type"]:checked').value;

        let traderValue = '0';
        if (receiptType === 'business') {
            traderValue = '1';
        } else if (receiptType === 'self') {
            traderValue = '0';
        }

        return {
            studentId: window.selectedCashbillStudent.studentId,
            paymentKey: window.selectedCashbillStudent.paymentKey,
            studentInfo: {
                name: window.selectedCashbillStudent.name,
                grade: window.selectedCashbillStudent.grade,
                teacher: window.selectedCashbillStudent.teacher
            },
            receiptNumber: receiptNumberInput.value.replace(/[^0-9]/g, ''),
            receiptType: receiptType,
            taxType: taxType,
            trader: traderValue,
            price: cashPriceInput.value.replace(/,/g, ''),
            supplyPrice: supplyPriceInput.value.replace(/,/g, ''),
            tax: taxPriceInput.value.replace(/,/g, ''),
            issueDate: receiptDateInput.value
        };
    }

    function sendCashbillRequest(data) {
        console.log(data);
        fetch('/pay/api/cashbill/issue', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('서버 응답 오류');
                }
                return response.json();
            })
            .then(result => {
                alert('현금영수증이 발행되었습니다.');
                resetForm();
                document.querySelector('.add-cashbill-modal').style.display = 'none';
                const monthInput = document.querySelector(".hidden-date");
                const yearMonth = {
                    year: monthInput.value.split("-")[0],
                    month: monthInput.value.split("-")[1]
                };

                fetch("/pay/api/cashbill/history", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify(yearMonth)
                })
                    .then(res => res.json())
                    .then(data => renderCashbillHistory(data.response || []));
            })
            .catch(error => {
                console.error('Error:', error);
                alert('현금영수증 발행 중 오류가 발생했습니다.');
            });
    }

    function resetForm() {
        window.selectedCashbillStudent = null;
        document.getElementById('student-name-cell').textContent = '학생을 선택해주세요.';
        document.getElementById('cash-amount-cell').textContent = '-';
        receiptNumberInput.value = '';
        cashPriceInput.value = '';
        supplyPriceInput.value = '';
        taxPriceInput.value = '';
        document.querySelector('.add-cashbill-modal input[name="receipt-type"][value="personal"]').checked = true;
        document.querySelector('.add-cashbill-modal input[name="tax-type"][value="tax-free"]').checked = true;

        document.querySelectorAll('#cashbill-student-list tr').forEach(tr => {
            tr.classList.remove('selected');
            tr.style.backgroundColor = '';
        });
    }

    document.getElementById('cashbill-print')?.addEventListener('click', () => {
        const historyMonthInput = document.getElementById('cashbill-history-month');
        const monthInput = document.querySelector(".hidden-date");
        const targetMonth = historyMonthInput?.value || monthInput.value;
        const [year, month] = targetMonth.split("-");
        const ym = year + month;

        const selectedIds = [...document.querySelectorAll('.cashbill-checkbox:checked')]
            .map(cb => cb.dataset.cashbillId)
            .filter(Boolean);

        // 선택된 항목 없으면 ids 파라미터 없이 전체 출력
        const idsParam = selectedIds.length > 0 ? selectedIds.join(',') : null;

        printCashbillHistory(ym, idsParam);
    });

    function printCashbillHistory(ym, ids) {
        const iframe = document.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = ids
            ? `/pay/print-cashbill?ym=${ym}&ids=${ids}`
            : `/pay/print-cashbill?ym=${ym}`;

        iframe.onload = () => {
            iframe.contentWindow.print();
        };

        document.body.appendChild(iframe);
    }
});

/* ========================================
    💰 현금영수증 발행내역 렌더링
======================================== */
const renderCashbillHistory = (historyList) => {
    const historyTbody = document.getElementById('cashbill-history-tbody');
    if (!historyTbody) return;
    const theadCheckboxCell = document.querySelector('#cashbill-history-tbody')
        ?.closest('table')
        ?.querySelector('thead tr th:first-child');
    if (theadCheckboxCell) {
        theadCheckboxCell.innerHTML = `<input type="checkbox" id="cashbill-check-all" title="전체선택">`;
    }
    historyTbody.innerHTML = "";

    if (!historyList || historyList.length === 0) {
        historyTbody.innerHTML = `
            <tr>
                <td colspan="11" style="text-align:center; padding: 30px; color: #666;">
                    발행내역이 없습니다.
                </td>
            </tr>`;
        return;
    }

    historyList.forEach((item, index) => {
        const tr = document.createElement("tr");

        // 발급구분 텍스트 변환
        let receiptTypeText = '';
        if (item.receiptType === 'personal') {
            receiptTypeText = '개인 소득공제용';
        } else if (item.receiptType === 'business') {
            receiptTypeText = '사업자 지출증빙용';
        } else if (item.receiptType === 'self') {
            receiptTypeText = '자진발급';
        }

        // 상태 텍스트 및 색상
        let statusText = '';
        let statusColor = '';
        let isCanceled = false;

        if (item.status === 'ISSUED' || item.status === '발행완료') {
            statusText = '발행완료';
            statusColor = '#28a745';
        } else if (item.status === 'CANCELED' || item.status === '취소') {
            statusText = '취소';
            statusColor = '#dc3545';
            isCanceled = true;
        } else {
            statusText = item.status || '-';
            statusColor = '#666';
        }

        tr.innerHTML = `
            <td class="checkbox-group">
                <input type="checkbox" class="cashbill-checkbox" data-cashbill-id="${item.billId || ''}" ${isCanceled ? 'disabled style="cursor: not-allowed;"' : ''}>
            </td>
            <td>${historyList.length - index}</td>
            <td>${item.issueDate || item.createdAt || '-'}</td>
            <td>${item.studentName || '-'}</td>
            <td>${receiptTypeText}</td>
            <td>${item.billId || '-'}</td>
            <td>${item.apprNum || '-'}</td>
            <td>${parseInt(item.supplyPrice || 0).toLocaleString()}원</td>
            <td>${parseInt(item.taxPrice || 0).toLocaleString()}원</td>
            <td class="cashbill-price">${parseInt(item.price || 0).toLocaleString()}원</td>
            <td><span style="color: ${statusColor}; font-weight: 500;">${statusText}</span></td>
        `;

        historyTbody.appendChild(tr);
    });
    setupMultiCheckbox();
};

function setupMultiCheckbox() {
    const allCheckbox = document.getElementById('cashbill-check-all');
    const checkboxes = [...document.querySelectorAll('.cashbill-checkbox:not([disabled])')];

    if (!allCheckbox || checkboxes.length === 0) return;

    // 중복 리스너 방지: cloneNode로 초기화
    checkboxes.forEach(checkbox => checkbox.replaceWith(checkbox.cloneNode(true)));

    const freshCheckboxes = [...document.querySelectorAll('.cashbill-checkbox:not([disabled])')];

    const syncAllCheckbox = () => {
        const allChecked = freshCheckboxes.every(cb => cb.checked);
        allCheckbox.checked = allChecked;
    };

    freshCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', syncAllCheckbox);
    });

    allCheckbox.addEventListener('change', function () {
        freshCheckboxes.forEach(cb => { cb.checked = this.checked; });
    });
}
/* ========================================
    현금영수증 취소 기능
======================================== */
document.addEventListener('DOMContentLoaded', function () {
    const cancelCashbillBtn = document.getElementById('cancel-cashbill');

    if (cancelCashbillBtn) {
        cancelCashbillBtn.addEventListener('click', async function () {

            const selectedCheckbox = document.querySelector('#cashbill-history-tbody .cashbill-checkbox:checked');

            if (!selectedCheckbox) {
                alert('취소할 항목을 선택해주세요.');
                return;
            }

            const row = selectedCheckbox.closest('tr');
            const billId = selectedCheckbox.dataset.cashbillId;
            const studentNameCell = row.querySelector('td:nth-child(4)');
            const studentName = studentNameCell ? studentNameCell.textContent.trim() : '';

            if (!billId) {
                alert('취소할 수 있는 항목이 없습니다.');
                return;
            }

            // 확인 메시지
            const confirmMessage = `정말로 발행을 취소하시겠습니까?\n\n학생: ${studentName}`;

            if (confirm(confirmMessage)) {
                // 취소 사유 입력
                const reason = prompt('취소 사유를 입력해주세요:', '고객 요청');

                if (!reason || !reason.trim()) {
                    alert('취소 사유를 입력해야 합니다.');
                    return;
                }

                await cancelCashbill(billId, reason.trim());
            }
        });
    }
});

// 현금영수증 취소 처리 함수
async function cancelCashbill(billId, reason) {
    // 로딩 오버레이 생성 및 표시
    const loadingOverlay = createLoadingOverlay();

    try {
        // 로딩 표시
        document.body.appendChild(loadingOverlay);

        // 취소 버튼 비활성화
        const cancelBtn = document.getElementById('cancel-cashbill');
        if (cancelBtn) {
            cancelBtn.disabled = true;
        }

        // 취소 요청
        const response = await fetch('/pay/api/cashbill/cancel', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                billId: billId,
                reason: reason
            })
        });

        const result = await response.json();

        // 로딩 해제
        document.body.removeChild(loadingOverlay);
        if (cancelBtn) {
            cancelBtn.disabled = false;
        }

        // 결과 처리
        if (response.ok && result.success) {
            alert('현금영수증이 취소되었습니다.');

            // 발행내역 목록 새로고침
            await refreshCashbillHistory();

        } else {
            // 실패 처리
            alert(result.message || '현금영수증 취소 중 오류가 발생했습니다.');
        }

    } catch (error) {
        // 로딩 해제
        if (loadingOverlay && loadingOverlay.parentNode) {
            document.body.removeChild(loadingOverlay);
        }

        const cancelBtn = document.getElementById('cancel-cashbill');
        if (cancelBtn) {
            cancelBtn.disabled = false;
        }

        console.error('현금영수증 취소 중 오류:', error);
        alert('취소 처리 중 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.');
    }
}

function createLoadingOverlay() {
    const overlay = document.createElement('div');
    overlay.id = 'cashbill-loading-overlay';
    overlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.6);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 9999;
    `;

    const loadingBox = document.createElement('div');
    loadingBox.style.cssText = `
        background: white;
        padding: 40px 60px;
        border-radius: 12px;
        text-align: center;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    `;

    const spinner = document.createElement('div');
    spinner.style.cssText = `
        border: 4px solid #f3f3f3;
        border-top: 4px solid #3498db;
        border-radius: 50%;
        width: 50px;
        height: 50px;
        animation: spin 1s linear infinite;
        margin: 0 auto 20px;
    `;

    const text = document.createElement('div');
    text.textContent = '현금영수증 취소 처리중...';
    text.style.cssText = `
        font-size: 16px;
        font-weight: 500;
        color: #333;
    `;

    loadingBox.appendChild(spinner);
    loadingBox.appendChild(text);
    overlay.appendChild(loadingBox);

    // 스피너 애니메이션 추가
    if (!document.getElementById('spinner-animation')) {
        const style = document.createElement('style');
        style.id = 'spinner-animation';
        style.textContent = `
            @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
        `;
        document.head.appendChild(style);
    }

    return overlay;
}

// 발행내역 새로고침 함수
async function refreshCashbillHistory() {
    const monthInput = document.querySelector(".hidden-date");
    if (!monthInput) return;

    const [year, month] = monthInput.value.split("-");
    const yearMonth = {year, month};

    try {
        const historyRes = await fetch("/pay/api/cashbill/history", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(yearMonth)
        });

        if (historyRes.ok) {
            const historyResult = await historyRes.json();
            renderCashbillHistory(historyResult.response || []);
        }
    } catch (error) {
        console.error('발행내역 새로고침 중 오류:', error);
    }
}



