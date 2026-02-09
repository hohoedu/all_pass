/* global CryptoJS */

/* ===============================
    전역 상태
=============================== */
let currentFeeView = 'edu';
let currentStudents = [];
let currentSort = {key: null, order: 'asc'};
let fetchStudentsGlobal;

document.addEventListener('DOMContentLoaded', () => {

    /* ===============================
        DOM 캐싱
    =============================== */
    const tbody = document.getElementById('student-tbody');
    const monthInput = document.querySelector('.hidden-picker');
    const monthBtn = document.querySelector('.calendar-open');
    const monthDisplay = document.querySelector('.current-month');
    const teacherSelect = document.getElementById('student-filter');
    const selectAll = document.getElementById('pay-edu-select-all');
    const searchBtn = document.querySelector('.explore');
    const searchInput = document.getElementById('search-name');

    /* ===============================
        초기 바인딩
    =============================== */
    initMonthFromUrl();
    bindMonthPicker();
    bindTeacherChange();
    bindFeeViewRadio();
    bindSearch();
    bindSelectAllCheckbox();

    const [yy, mm] = monthInput.value.split('-');
    fetchStudents(yy, mm, teacherSelect.value);

    /* ===============================
        월 선택 달력
    =============================== */
    function bindMonthPicker() {
        if (!monthInput || !monthBtn) return;

        monthBtn.addEventListener('click', () => {
            if (typeof monthInput.showPicker === 'function') {
                monthInput.showPicker();       // Chrome
            } else {
                monthInput.focus();
                monthInput.click();            // Safari fallback
            }
        });

        monthInput.addEventListener('change', () => {
            const [y, m] = monthInput.value.split('-');
            monthDisplay.textContent = `${y}년 ${parseInt(m, 10)}월`;

            const url = new URL(location.href);
            url.searchParams.set('year', y);
            url.searchParams.set('month', m);
            location.href = url.toString();
        });
    }

    /* ===============================
     데이터 조회
 =============================== */
    async function fetchStudents(year, month, teacherCode) {
        try {
            tbody.style.visibility = 'hidden';
            const itemType = document.querySelector('input[name="feeView"]:checked').value;

            const res = await fetch('/pay/students', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    year,
                    month: String(month).padStart(2, '0'),
                    userCode: teacherCode,
                    itemType: itemType
                })
            });

            if (!res.ok) throw new Error();

            const json = await res.json();
            currentStudents = json.response || json;
            console.log("학생 데이터:", currentStudents);
            renderStudentTable(currentStudents);

        } catch (e) {
            console.error(e);
            alert('학생 목록을 불러오지 못했습니다.');
        }
    }

    fetchStudentsGlobal = fetchStudents;

    /* ===============================
        렌더링 (필터링/정렬 제거)
    =============================== */
    function renderStudentTable(list) {
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            tbody.innerHTML = `
        <tr>
            <td colspan="10" style="text-align:center;">
                등록된 학생 데이터가 없습니다.
            </td>
        </tr>`;
            tbody.style.visibility = 'visible';
            return;
        }

        const fragment = document.createDocumentFragment();

        list.forEach((s, i) => {
            const lastPhone4 = s.parentPhone
                ? s.parentPhone.replace(/[^0-9]/g, '').slice(-4)
                : '';

            const tr = document.createElement('tr');

            tr.dataset.studentId = s.studentId;
            tr.dataset.studentName = s.studentName;
            tr.dataset.paymentKey = s.paymentKey || '';
            tr.dataset.billId = s.billId || '';
            tr.dataset.issuanceStatus = s.issuanceStatus || '';
            tr.dataset.payStatus = s.payStatus || '';
            tr.dataset.otherSubject = s.otherSubject || '';
            tr.dataset.otherTeacher = s.otherTeacher || '';
            tr.dataset.samePhoneStudents = s.samePhoneStudents || '';
            tr.dataset.lastPhone4 = lastPhone4;

            const isPriceModified = s.isPriceModified === 1;
            const billPriceStyle = isPriceModified
                ? 'color: red;'
                : '';
            const billPriceTitle = isPriceModified && s.standardFee
                ? `표준 금액: ${Number(s.standardFee).toLocaleString()}원`
                : '';
            const otherSubjectDisplay = s.otherSubject && s.otherSubject.trim() !== ''
                ? s.otherSubject
                : '';


            tr.innerHTML = `
        <td class="checkbox-group">
            <input type="checkbox" class="row-checkbox">
        </td>
        <td>${i + 1}</td>
        <td class="student-name-cell" style="position: relative;">
            ${s.studentName}
        </td>
        <td>${s.subject || '-'}</td>
        
        <td style="${billPriceStyle}" title="${billPriceTitle}">
            ${Number(s.billPrice || 0).toLocaleString()}
        </td>
         <td class="other-subject-cell" style="${otherSubjectDisplay ? 'cursor: help;' : ''}">
        ${otherSubjectDisplay}
    </td>
        <td>${renderIssueStatus(s.issuanceStatus)}</td>
        <td>${renderPayStatus(s.payStatus)}</td>
        <td id="personal-pay-info" style="cursor:pointer;">
            <span style="font-size:18px;">🔍</span>
        </td>
    `;

            fragment.appendChild(tr);
        });

        tbody.appendChild(fragment);
        bindStudentNameTooltip();
        bindOtherSubjectTooltip();

        selectAll.checked = false;
        tbody.style.visibility = 'visible';
    }

// 형제 이름 보이게 하기
    function bindStudentNameTooltip() {
        const nameCells = tbody.querySelectorAll('.student-name-cell');

        nameCells.forEach(cell => {
            const row = cell.closest('tr');
            const samePhoneStudents = row.dataset.samePhoneStudents;

            if (!samePhoneStudents ||
                samePhoneStudents === 'null' ||
                samePhoneStudents === 'undefined' ||
                samePhoneStudents.trim() === '') {
                return;
            }

            cell.style.cursor = 'help';

            cell.addEventListener('mouseenter', (e) => {
                const existingTooltip = document.querySelector('.phone-tooltip');
                if (existingTooltip) {
                    existingTooltip.remove();
                }

                const tooltip = document.createElement('div');
                tooltip.className = 'phone-tooltip';
                tooltip.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 5px;">같은 전화번호 형제:</div>
            ${samePhoneStudents}
        `;

                document.body.appendChild(tooltip);

                const rect = cell.getBoundingClientRect();
                tooltip.style.left = rect.left + 'px';
                tooltip.style.top = (rect.bottom + 5) + 'px';
            });

            cell.addEventListener('mouseleave', () => {
                const tooltip = document.querySelector('.phone-tooltip');
                if (tooltip) {
                    tooltip.remove();
                }
            });
        });
    }

    function bindOtherSubjectTooltip() {
        const otherSubjectCells = tbody.querySelectorAll('.other-subject-cell');

        otherSubjectCells.forEach(cell => {
            const row = cell.closest('tr');
            const otherSubject = row.dataset.otherSubject;
            const otherTeacher = row.dataset.otherTeacher;  // ⭐ 변경

            if (!otherSubject ||
                otherSubject === 'null' ||
                otherSubject === 'undefined' ||
                otherSubject.trim() === '') {
                return;
            }

            cell.addEventListener('mouseenter', (e) => {
                const existingTooltip = document.querySelector('.other-subject-tooltip');
                if (existingTooltip) {
                    existingTooltip.remove();
                }

                const tooltip = document.createElement('div');
                tooltip.className = 'other-subject-tooltip';
                tooltip.style.cssText = `
                position: fixed;
                background: rgba(0, 0, 0, 0.85);
                color: white;
                padding: 8px 12px;
                border-radius: 4px;
                font-size: 13px;
                z-index: 10000;
                pointer-events: none;
                white-space: nowrap;
            `;
                // ⭐ " 선생님" 붙여서 표시
                tooltip.textContent = otherTeacher ? otherTeacher + ' 선생님' : '';

                document.body.appendChild(tooltip);

                const rect = cell.getBoundingClientRect();
                tooltip.style.left = rect.left + 'px';
                tooltip.style.top = (rect.bottom + 5) + 'px';
            });

            cell.addEventListener('mouseleave', () => {
                const tooltip = document.querySelector('.other-subject-tooltip');
                if (tooltip) {
                    tooltip.remove();
                }
            });
        });
    }

    function formatTeacher(s) {
        return `${s.hanTeacher ? s.hanTeacher + '(한)' : ''}`
            + (s.hanTeacher && s.bookTeacher ? ', ' : '')
            + `${s.bookTeacher ? s.bookTeacher + '(독)' : ''}`;
    }

    function renderIssueStatus(issuanceStatus) {
        if (!issuanceStatus || issuanceStatus === 'nonIssue') {
            return `<span class="unissued">미발행</span>`;
        }
        if (issuanceStatus === 'issued') {
            return `<span class="issued">발행</span>`;
        }
        if (issuanceStatus === 'nonIssue_off') {
            return `<span class="edu-issued">현장결제</span>`;
        }
        if (issuanceStatus === 'destroyed_off') {
            return `<span class="edu-issued">현장결제</span>`;
        }
        if (issuanceStatus === 'canceled_off') {
            return `<span class="edu-issued">현장결제</span>`;
        }
        if (issuanceStatus === 'destroyed') {
            return `<span class="destroyed">파기</span>`;
        }
        if (issuanceStatus === 'canceled') {
            return `<span class="canceled">취소</span>`;
        }
        if (issuanceStatus === 'discount') {
            return `<span class="discount">할인</span>`;
        }
        return '-';
    }

    function renderPayStatus(totalStatus) {
        if (totalStatus === '결제완료') {
            return `<span class="pay-box pay-done">결제완료</span>`;
        }
        if (totalStatus === '부분결제') {
            return `<span class="pay-box pay-partial">부분결제</span>`;
        }
        if (totalStatus === '미결제') {
            return `<span class="pay-box pay-late">미결제</span>`;
        }
        if (totalStatus === '할인') {
            return `<span class="pay-box pay-discount">할인</span>`;
        }
        return '-';
    }

    /* ===============================
        체크박스 전체 선택
    =============================== */
    function bindSelectAllCheckbox() {
        selectAll.addEventListener('change', e => {
            tbody.querySelectorAll('.row-checkbox')
                .forEach(cb => cb.checked = e.target.checked);
        });

        tbody.addEventListener('change', e => {
            if (!e.target.classList.contains('row-checkbox')) return;

            const total = tbody.querySelectorAll('.row-checkbox').length;
            const checked = tbody.querySelectorAll('.row-checkbox:checked').length;

            selectAll.checked = total > 0 && total === checked;
        });
    }

    /* ===============================
        필터 / 검색
    =============================== */
    function bindFeeViewRadio() {
        document.querySelectorAll('input[name="feeView"]').forEach(radio => {
            radio.addEventListener('change', e => {
                const [y, m] = monthInput.value.split('-');
                currentFeeView = e.target.value;
                fetchStudents(y, m, teacherSelect.value, currentFeeView);
            });
        });
    }

    function bindTeacherChange() {
        teacherSelect.addEventListener('change', e => {
            const [y, m] = monthInput.value.split('-');
            currentFeeView = e.target.value;
            fetchStudents(y, m, teacherSelect.value, currentFeeView);
        });
    }

    function bindSearch() {

        const doSearch = () => {
            const keyword = searchInput.value.trim();
            const searchType = document.getElementById('stu-name').value;

            tbody.querySelectorAll('tr').forEach(tr => {
                let targetValue = '';

                if (searchType === 'name') {
                    targetValue = tr.dataset.studentName || '';
                } else if (searchType === 'phone') {
                    targetValue = tr.dataset.lastPhone4 || '';
                }

                const matched =
                    !keyword || targetValue.includes(keyword);

                tr.style.display = matched ? '' : 'none';
            });
        };
        document.getElementById('stu-name').addEventListener('change', () => {
            searchInput.value = '';
        });
        searchBtn.addEventListener('click', doSearch);

        searchInput.addEventListener('keydown', e => {
            if (e.isComposing) return;

            if (e.key === 'Enter') {
                e.preventDefault();
                doSearch();
            }
        });
    }

    /* ===============================
        URL 월 초기화
    =============================== */
    function initMonthFromUrl() {
        const params = new URL(location.href).searchParams;
        const y = params.get('year') || new Date().getFullYear();
        const m = params.get('month')
            || String(new Date().getMonth() + 1).padStart(2, '0');

        monthInput.value = `${y}-${m}`;
        monthDisplay.textContent = `${y}년 ${parseInt(m, 10)}월`;
    }

});

// ========== 청구서 유효기간 세팅 ========== //
document.addEventListener('DOMContentLoaded', () => {
    const expireInput = document.querySelector('.expire-input');
    const expireBtn = document.querySelector('.expire-btn');
    const expireDisplay = document.querySelector('.day-picker .day-display');

    const formatYYYYMMDD = (dt) => {
        const y = dt.getFullYear();
        const m = String(dt.getMonth() + 1).padStart(2, '0');
        const d = String(dt.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    };

    const setDisplay = (dt) => {
        expireDisplay.textContent =
            `${dt.getFullYear()}년 ${dt.getMonth() + 1}월 ${dt.getDate()}일`;
    };

    // 🔥 이번 달 5일 계산 함수
    const getCurrentMonth5th = (year, month) => {
        // month는 1~12 기준
        return new Date(year, month - 1, 5);
    };

    const billingMonth = document.querySelector('.hidden-picker')?.value;
    let baseDate;

    if (billingMonth) {
        const [yy, mm] = billingMonth.split('-').map(Number);
        // 🔥 청구하는 달 5일
        baseDate = getCurrentMonth5th(yy, mm);
    } else {
        const today = new Date();
        // 🔥 현재 달 5일
        baseDate = getCurrentMonth5th(
            today.getFullYear(),
            today.getMonth() + 1
        );
    }

    expireInput.value = formatYYYYMMDD(baseDate);
    setDisplay(baseDate);

    const today = new Date();
    expireInput.min = formatYYYYMMDD(today);

    expireBtn.addEventListener('click', () => {
        if (typeof expireInput.showPicker === 'function') {
            expireInput.showPicker();
        } else {
            expireInput.click();
        }
    });

    expireInput.addEventListener('change', () => {
        const [yy, mm, dd] = expireInput.value.split('-').map(Number);
        if (!yy || !mm || !dd) return;

        const selected = new Date(yy, mm - 1, dd);
        if (isNaN(selected)) return;

        setDisplay(selected);
    });
});

document.addEventListener('DOMContentLoaded', () => {
    const claimTbody = document.getElementById('claim-detail-tbody');

    if (claimTbody) {
        claimTbody.addEventListener('change', (e) => {
            if (e.target.type === 'checkbox' && e.target.closest('.claim-frame')) {
                const clickedCheckbox = e.target;

                claimTbody.querySelectorAll('input[type="checkbox"]').forEach(cb => {
                    if (cb !== clickedCheckbox) {
                        cb.checked = false;
                    }
                });
            }
        });
    }
});

// ========== 모달 열기 ========== //
document.addEventListener('DOMContentLoaded', () => {

    /* =========================
        공통: 모달 열기
    ========================= */
    function openModal(modalType) {
        document.querySelectorAll('.modal').forEach(modal => {
            modal.style.display = 'none';
        });

        const targetModal = document.querySelector(`.${modalType}-modal`);
        if (targetModal) {
            targetModal.style.display = 'block';
        }
    }

    /* =========================
        전체 조회
    ========================= */
    const btnTuition = document.querySelector('#btn-tuition');
    if (!btnTuition) return;

    btnTuition.addEventListener('click', async () => {
        try {
            const response = await fetch('/pay/edu-personal', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({studentId: 'all'})
            });

            if (!response.ok) throw new Error('데이터 조회 실패');

            const data = await response.json();
            fillModal(data.response, 'tuition');
            openModal('tuition');

        } catch (error) {
            console.error('❌ 청구 데이터 조회 오류:', error);
            alert('청구 내역을 불러오지 못했습니다.');
        }
    });

    /* =========================
        개별 조회
    ========================= */
    const tbody = document.querySelector('#student-tbody');
    const claimDetailSection = document.querySelector('.claim-frame');
    const claimDivider = document.getElementById('claim-divider');
    const claimButtons = document.getElementById('claim-buttons');

    if (tbody && claimDetailSection) {
        tbody.addEventListener('click', async (e) => {
            const row = e.target.closest('tr');
            if (!row || !tbody.contains(row)) return;

            const targetCell = e.target.closest('td');
            if (!targetCell) return;

            const cellIndex = Array.from(row.children).indexOf(targetCell);

            if (cellIndex === 0) return;

            const studentId = row.dataset.studentId;
            if (!studentId) return;

            const url = new URL(window.location.href);
            const yy = url.searchParams.get('year');
            const mm = url.searchParams.get('month');

            if (e.target.closest('#personal-pay-info')) {
                try {
                    const response = await fetch('/pay/edu-personal', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify({studentId, yy, mm})
                    });

                    if (!response.ok) throw new Error('데이터 조회 실패');

                    const data = await response.json();
                    fillModal(data.response, 'personal');
                    openModal('personal');

                } catch (error) {
                    console.error('❌ 개인 청구 데이터 조회 오류:', error);
                    alert('청구 내역을 불러오지 못했습니다.');
                }
                return;
            }

            // 🔥 일반 row 클릭 시 → 하단 테이블 토글
            if (row.classList.contains('selected-for-detail')) {
                row.classList.remove('selected-for-detail');
                claimDetailSection.style.display = 'none';
                if (claimDivider) claimDivider.style.display = 'none';
                if (claimButtons) claimButtons.style.display = 'none';
                return;
            }

            tbody.querySelectorAll('tr.selected-for-detail').forEach(tr => {
                tr.classList.remove('selected-for-detail');
            });

            row.classList.add('selected-for-detail');
            await fetchBillDetail(studentId, yy, mm);
        });
    }

    /* =========================
        청구 상세 데이터 로드 (개선됨)
    ========================= */
    async function fetchBillDetail(studentId, yy, mm) {
        const claimDetailSection = document.querySelector('.claim-frame');
        const claimDivider = document.getElementById('claim-divider');
        const claimButtons = document.getElementById('claim-buttons');

        try {
            const response = await fetch('/pay/detail/bill', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({studentId, yy, mm})
            });

            if (!response.ok) throw new Error('데이터 조회 실패');

            const data = await response.json();
            console.log(data);
            fillClaimDetailTable(data.response);

            claimDetailSection.style.display = 'block';
            if (claimDivider) claimDivider.style.display = 'block';
            if (claimButtons) claimButtons.style.display = 'flex';

        } catch (error) {
            console.error('❌ 청구 데이터 조회 오류:', error);
            alert('청구 내역을 불러오지 못했습니다.');
        }
    }

    /* =========================
        하단 청구 내역 테이블 채우기
    ========================= */
    function fillClaimDetailTable(data) {
        const claimTbody = document.querySelector('.claim-frame tbody');
        if (!claimTbody) return;

        claimTbody.innerHTML = '';

        if (!data || data.length === 0) {
            claimTbody.innerHTML = `
        <tr>
            <td colspan="8" style="text-align:center;">
                등록된 청구 내역이 없습니다.
            </td>
        </tr>`;
            return;
        }

        data.forEach(item => {
            const tr = document.createElement('tr');

            const paidDate = item.paidDate ? item.paidDate.split(' ')[0] : '-';
            const amount = item.amount ? Number(item.amount).toLocaleString() + '원' : '-';
            const paymentMethodText = item.type === 'bill' ? '온라인 카드' : '현장결제';

            const hasCardName = item.type === 'bill' && item.cardName;
            const tooltipStyle = hasCardName ? 'position: relative;' : '';
            const tooltipAttr = hasCardName ? `data-card-name="${item.cardName}"` : '';

            // 🔥 데이터 속성 추가
            tr.dataset.billId = item.billId || '';
            tr.dataset.studentId = item.studentId || '';
            tr.dataset.paymentKey = item.paymentKey || '';
            tr.dataset.billType = item.billType || '';
            tr.dataset.status = item.status || '';

            tr.innerHTML = `
        <td class="checkbox-group">
            <input type="checkbox" class="claim-checkbox">
        </td>
        <td>${item.studentName || '-'}</td>
        <td>${item.billType === 'EDU_FEE' ? '교육비' : '교재비'}</td>
        <td>${item.expireDate || '-'}</td>
        <td>${paidDate}</td>
        <td class="payment-method-cell" style="${tooltipStyle}" ${tooltipAttr}>
            ${paymentMethodText}
        </td>
        <td>${amount}</td>
        <td>${getStatusText(item.status)}</td>
    `;

            claimTbody.appendChild(tr);
        });

        bindPaymentMethodTooltip();
    }

    function bindPaymentMethodTooltip() {
        const paymentCells = document.querySelectorAll('.payment-method-cell[data-card-name]');

        paymentCells.forEach(cell => {
            const cardName = cell.getAttribute('data-card-name');

            if (!cardName) return;

            cell.addEventListener('mouseenter', (e) => {
                const existingTooltip = document.querySelector('.payment-tooltip');
                if (existingTooltip) {
                    existingTooltip.remove();
                }

                const tooltip = document.createElement('div');
                tooltip.className = 'payment-tooltip';
                tooltip.textContent = cardName;

                document.body.appendChild(tooltip);

                const rect = cell.getBoundingClientRect();
                const tooltipWidth = tooltip.offsetWidth;

                const leftPosition = rect.left + (rect.width / 2) - (tooltipWidth / 2);

                tooltip.style.left = leftPosition + 'px';
                tooltip.style.top = (rect.bottom + 5) + 'px';
            });

            // 마우스 벗어났을 때
            cell.addEventListener('mouseleave', () => {
                const tooltip = document.querySelector('.payment-tooltip');
                if (tooltip) {
                    tooltip.remove();
                }
            });
        });
    }

    function getStatusText(status) {
        switch (status) {
            case 'issued':
                return '결제대기';
            case 'approved':
                return '결제완료';
            case 'canceled':
                return '결제취소';
            case 'destroyed':
                return '청구서파기';
            default:
                return '-';
        }
    }

    /* =========================
        모달 데이터 채우기
    ========================= */
    function fillModal(data, type) {
        const tbody = document.getElementById(`${type}-tbody`);
        tbody.innerHTML = '';

        if (!data || data.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="10" style="text-align:center;">
                        등록된 데이터가 없습니다.
                    </td>
                </tr>`;
            return;
        }

        const firstItem = data[0];
        console.log(firstItem);
        const modal = document.querySelector(`.${type}-modal`);
        if (!modal) return;

        // 제목
        modal.querySelector('.m-t-line .title').textContent =
            `${firstItem.studentName}의 수강료 상세 내역`;
        modal.querySelector('.pay-details h4 span').textContent =
            firstItem.studentName;

        const toNumber = v => Number(v || 0);

        const hanEduFee = toNumber(firstItem.hanFee);
        const hanMaterialFee = toNumber(firstItem.hanMaterialFee);
        const bookEduFee = toNumber(firstItem.bookFee);
        const bookMaterialFee = toNumber(firstItem.bookMaterialFee);

        const rows = modal.querySelectorAll('.pay-edu-table tbody tr');

        // 값 세팅
        rows[0].querySelector('.edu-input').value = hanEduFee.toLocaleString();
        rows[0].querySelector('.edu-input').dataset.rawValue = hanEduFee;

        rows[1].querySelector('.material-input').value = hanMaterialFee.toLocaleString();
        rows[1].querySelector('.material-input').dataset.rawValue = hanMaterialFee;

        rows[2].querySelector('.edu-input').value = bookEduFee.toLocaleString();
        rows[2].querySelector('.edu-input').dataset.rawValue = bookEduFee;

        rows[3].querySelector('.material-input').value = bookMaterialFee.toLocaleString();
        rows[3].querySelector('.material-input').dataset.rawValue = bookMaterialFee;

        updateTotal(modal);

        setupInputListeners(modal);
        setupSaveButton(modal, firstItem.studentId);

        /* =========================
            하단 리스트
        ========================= */
        data.forEach((item, index) => {
            const amount = Number(item.amount || 0).toLocaleString();
            const paidDate = item.paidDate ? item.paidDate.split(' ')[0] : '-';
            const teachers = [item.hanTeacher, item.bookTeacher].filter(Boolean).join(', ');

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="checkbox-group">
                    <input type="checkbox"
                            data-student-id="${item.studentId}"
                            data-payment-key="${item.paymentKey}">
                </td>
                <td>${index + 1}</td>
                <td>${item.classDate}</td>
                <td>${item.studentName}</td>
                <td>${item.subject}</td>
                <td>${teachers}</td>   
                <td>${paidDate}</td>
                <td class="payment">${amount}</td>
                <td class="middle">
                    <div class="state-box ${getStatusClass(item.status)}">
                        ${getStatus(item.status)}
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    /* =========================
        입력 리스너
    ========================= */
    function setupInputListeners(modal) {
        modal.querySelectorAll('.edu-input').forEach(input => {
            input.addEventListener('input', e => {
                const value = e.target.value.replace(/[^0-9]/g, '');
                e.target.dataset.rawValue = value;
                e.target.value = Number(value || 0).toLocaleString();
                updateTotal(modal);
            });
        });
    }

    /* =========================
        합계 계산
    ========================= */
    function updateTotal(modal) {
        let total = 0;

        modal.querySelectorAll('.edu-input').forEach(input => {
            total += Number(input.dataset.rawValue || 0);
        });

        const sumCell = modal.querySelector('.edu-sum');
        if (sumCell) {
            sumCell.textContent = total.toLocaleString();
        }
    }

    /* =========================
        저장 버튼
    ========================= */
    function setupSaveButton(modal, studentId) {
        const saveBtn = modal.querySelector('.save-btn');
        if (!saveBtn) return;

        const newBtn = saveBtn.cloneNode(true);
        saveBtn.parentNode.replaceChild(newBtn, saveBtn);

        newBtn.addEventListener('click', async () => {
            const rows = modal.querySelectorAll('.pay-edu-table tbody tr');

            const hanEduFee = Number(rows[0].querySelector('.edu-input').dataset.rawValue || 0);
            const hanMaterialFee = Number(rows[1].querySelector('.material-input').dataset.rawValue || 0);
            const bookEduFee = Number(rows[2].querySelector('.edu-input').dataset.rawValue || 0);
            const bookMaterialFee = Number(rows[3].querySelector('.material-input').dataset.rawValue || 0);

            const url = new URL(window.location.href);
            const yy = url.searchParams.get('year');
            const mm = url.searchParams.get('month');

            if (!yy || !mm) {
                alert('년월 정보가 없습니다.');
                return;
            }

            if (!confirm('수강료를 저장하시겠습니까?')) return;
            console.log(JSON.stringify({
                studentId,
                yy,
                mm,
                hanEduFee,
                hanMaterialFee,
                bookEduFee,
                bookMaterialFee
            }));
            try {
                const response = await fetch('/pay/update-fee', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({
                        studentId,
                        yy,
                        mm,
                        hanEduFee,
                        hanMaterialFee,
                        bookEduFee,
                        bookMaterialFee
                    })
                });

                if (!response.ok) throw new Error('저장 실패');

                const result = await response.json();
                if (result.success) {
                    alert('수강료가 저장되었습니다.');
                } else {
                    alert(result.message || '저장에 실패했습니다.');
                }

            } catch (error) {
                console.error('❌ 수강료 저장 오류:', error);
                alert('서버와의 통신에 실패했습니다.');
            }
        });
    }

    /* =========================
        상태 텍스트 / 클래스
    ========================= */
    function getStatus(status) {
        switch (status) {
            case 'pending':
                return '청구서 미발행';
            case 'issued':
                return '결제 대기';
            case 'approved':
                return '결제 완료';
            case 'canceled':
                return '결제 취소';
            case 'destroyed':
                return '청구서 파기';
            default:
                return '';
        }
    }

    function getStatusClass(status) {
        switch (status) {
            case 'pending':
                return 'pending';
            case 'issued':
                return 'standby';
            case 'approved':
                return 'complete';
            case 'canceled':
                return 'cancellation';
            case 'destroyed':
                return 'destroy';
            default:
                return '';
        }
    }
});


// ========== 금액 직접 입력하기 ========== //
document.addEventListener('DOMContentLoaded', () => {
    const eduFee = document.getElementById("eduFee");
    const bookFee = document.getElementById("bookFee");
    const priceDiv = document.querySelector(".price");
    const priceInput = document.querySelector(".edu-input");

    function updatePriceState() {
        if (eduFee.checked) {
            priceDiv.classList.add("disabled");
            priceDiv.classList.remove("enabled");
        } else {
            priceDiv.classList.remove("disabled");
            priceDiv.classList.add("enabled");
            priceInput.value = "";
        }
    }

    function formatNumber(value) {
        const num = value.replace(/[^0-9]/g, "");
        if (!num) return "";
        return num.replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }

    priceInput.addEventListener("input", (e) => {
        const cursorPos = e.target.selectionStart;
        const formatted = formatNumber(e.target.value);
        e.target.value = formatted;
        e.target.setSelectionRange(e.target.value.length, e.target.value.length);
    });

    eduFee.addEventListener("change", updatePriceState);
    bookFee.addEventListener("change", updatePriceState);

    updatePriceState();
});

// ========== 우측 버튼 클릭 ========== //
document.addEventListener("DOMContentLoaded", () => {
    const payIssue = document.querySelector('#pay-issue');
    const payCancel = document.querySelector('#pay-cancel');
    const payDestroy = document.querySelector('#pay-destory');
    const payReissue = document.querySelector('#pay-reissue');

    // ================================
    // 🔥 1. 청구서 발행 (상단 테이블 체크박스 사용)
    // ================================
    payIssue.addEventListener('click', async () => {
        const checkedBoxes = document.querySelectorAll(
            '#student-tbody input[type="checkbox"]:checked'
        );

        if (checkedBoxes.length === 0) {
            return alert('학생을 선택하세요.');
        }

        const eduChecked = document.querySelector('input[name="eduFee"]').checked;
        const bookChecked = document.querySelector('input[name="bookFee"]').checked;

        if (!eduChecked && !bookChecked) {
            return alert('청구 종류를 선택하세요.');
        }

        const selectedMonth = document.querySelector('.hidden-date.hidden-picker').value;
        const [yy, mm] = selectedMonth.split('-');
        const expireDt = document.querySelector('.expire-input').value;

        const studentIds = Array.from(checkedBoxes).map(
            box => box.closest('tr').dataset.studentId
        );

        const includeSibling = document.querySelector('input[name="includeSibling"]').checked;

        const customPriceInput = document.querySelector('.edu-input').value;
        const customPrice = customPriceInput ? parseInt(customPriceInput.replace(/,/g, '')) : null;


        try {
            if (eduChecked) {

                if (customPrice !== null && customPrice <= 0) {
                    return alert('올바른 금액을 입력하세요.');
                }

                await sendBills({
                    studentIds,
                    type: 'edu',
                    message: `${mm}월 교육비 청구`,
                    expireDt,
                    yy,
                    mm,
                    includeSibling,
                    customPrice
                });
            }

            if (bookChecked) {
                if (customPrice !== null && customPrice <= 0) {
                    return alert('올바른 금액을 입력하세요.');
                }
                await sendBills({
                    studentIds,
                    type: 'material',
                    message: `${mm}월 교재비 청구`,
                    expireDt,
                    yy,
                    mm,
                    includeSibling,
                    customPrice
                });
            }

            alert('청구서 발행이 완료되었습니다.');

            const monthInput = document.querySelector('.hidden-picker');
            const teacherSelect = document.getElementById('student-filter');
            const [year, month] = monthInput.value.split('-');
            await fetchStudentsGlobal(year, month, teacherSelect.value);

        } catch (e) {
            console.error(e);
            alert(e.message || '청구서 발행 중 오류가 발생했습니다.');
        }
    });

    async function sendBills(body) {
        const res = await fetch('/pay/send', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body)
        });

        const data = await res.json();

        if (!res.ok || !data.success) {
            throw new Error(data.msg || data.response || '청구 실패');
        }

        console.log('✓ 발행 결과:', data.response);
    }

    // ================================
    // 🔥 2. 결제 취소 (하단 테이블 체크박스 사용)
    // ================================
    payCancel.addEventListener('click', async () => {
        const checkedBox = document.querySelector('.claim-frame input[type="checkbox"]:checked');

        if (!checkedBox) {
            alert('취소할 청구서를 선택하세요.');
            return;
        }

        const row = checkedBox.closest('tr');
        const billId = row.dataset.billId;
        const studentId = row.dataset.studentId;
        const paymentKey = row.dataset.paymentKey;
        const billType = row.dataset.billType;
        const status = row.dataset.status;

        if (status !== 'approved') {
            alert('결제 완료된 청구서만 취소할 수 있습니다.');
            return;
        }

        const studentName = row.querySelector('td:nth-child(2)').textContent;
        const cancelReason = prompt(`${studentName} 학생의 취소 사유를 입력해주세요.`);

        if (!cancelReason || cancelReason.trim() === '') {
            alert('취소 사유는 필수입니다.');
            return;
        }

        if (!confirm(
            `${studentName} 학생의 결제를 정말 취소하시겠습니까?\n\n` +
            `※ 이 작업은 되돌릴 수 없습니다.`
        )) {
            return;
        }

        const body = {
            paymentKey: paymentKey,
            cancelType: billType,
            cancelReason: cancelReason.trim()
        };

        try {
            const res = await fetch('/pay/cancel', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(body)
            });

            const data = await res.json();

            if (!res.ok) {
                throw new Error(data.msg || '결제 취소에 실패했습니다.');
            }

            alert(data.data || '결제가 취소되었습니다.');

            // ✅ CSR 업데이트
            const url = new URL(window.location.href);
            const yy = url.searchParams.get('year');
            const mm = url.searchParams.get('month');
            await fetchBillDetail(studentId, yy, mm);

        } catch (err) {
            console.error('❌ 결제 취소 오류:', err);
            alert(err.message);
        }
    });

    // ================================
    // 🔥 3. 청구서 파기 (하단 테이블 체크박스 사용)
    // ================================
    payDestroy.addEventListener('click', async () => {
        const checkedBox = document.querySelector('.claim-frame input[type="checkbox"]:checked');

        if (!checkedBox) {
            alert('파기할 청구서를 선택하세요.');
            return;
        }

        const row = checkedBox.closest('tr');
        const billId = row.dataset.billId;
        const studentId = row.dataset.studentId;
        const paymentKey = row.dataset.paymentKey;
        const billType = row.dataset.billType;
        const studentName = row.querySelector('td:nth-child(2)').textContent;

        if (!billId || !paymentKey) {
            alert('파기 정보를 확인할 수 없습니다.');
            return;
        }

        if (!confirm(`${studentName} 학생의 청구서를 파기하시겠습니까?`)) {
            return;
        }

        try {
            console.log("billId" + billId, "billType" + billType)
            const res = await fetch('/pay/destroy/bill', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    billId: billId,
                    studentId: studentId,
                    paymentKey: paymentKey,
                    destroyType: billType
                })
            });

            const data = await res.json();

            if (!res.ok) {
                throw new Error(data.msg || '청구서 파기에 실패했습니다.');
            }

            alert(data.data || '청구서가 파기되었습니다.');

            // ✅ CSR 업데이트
            const url = new URL(window.location.href);
            const yy = url.searchParams.get('year');
            const mm = url.searchParams.get('month');
            await fetchBillDetail(studentId, yy, mm);

        } catch (err) {
            console.error('❌ 청구서 파기 오류:', err);
            alert(err.message);
        }
    });

    // ================================
    // 🔥 4. 청구서 재발행 (하단 테이블 체크박스 사용)
    // ================================
    payReissue.addEventListener('click', async () => {
        const checkedBox = document.querySelector('.claim-frame input[type="checkbox"]:checked');

        if (!checkedBox) {
            alert('재발행할 청구서를 선택하세요.');
            return;
        }

        const row = checkedBox.closest('tr');
        const billId = row.dataset.billId;
        const studentId = row.dataset.studentId;

        if (!billId || billId === 'null' || billId === 'undefined') {
            alert('재발행할 수 있는 청구서가 아닙니다.');
            return;
        }

        try {
            const res = await fetch('/pay/reissue', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({billIds: [billId]})
            });

            const data = await res.json();

            if (!data.success) {
                return alert(data.msg || '재발행 실패');
            }

            alert('재발행이 완료되었습니다.');

            // ✅ CSR 업데이트
            const url = new URL(window.location.href);
            const yy = url.searchParams.get('year');
            const mm = url.searchParams.get('month');
            await fetchBillDetail(studentId, yy, mm);

        } catch (e) {
            console.error(e);
            alert('재발행 중 오류가 발생했습니다.');
        }
    });

    // ✅ fetchBillDetail 함수를 여기서도 접근 가능하도록 외부에 선언
    async function fetchBillDetail(studentId, yy, mm) {
        const claimDetailSection = document.querySelector('.claim-frame');
        const claimDivider = document.getElementById('claim-divider');
        const claimButtons = document.getElementById('claim-buttons');

        try {
            const response = await fetch('/pay/detail/bill', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({studentId, yy, mm})
            });

            if (!response.ok) throw new Error('데이터 조회 실패');

            const data = await response.json();
            console.log(data);
            fillClaimDetailTable(data.response);

            claimDetailSection.style.display = 'block';
            if (claimDivider) claimDivider.style.display = 'block';
            if (claimButtons) claimButtons.style.display = 'flex';

        } catch (error) {
            console.error('❌ 청구 데이터 조회 오류:', error);
            alert('청구 내역을 불러오지 못했습니다.');
        }
    }

    function fillClaimDetailTable(data) {
        const claimTbody = document.querySelector('.claim-frame tbody');
        if (!claimTbody) return;

        claimTbody.innerHTML = '';

        if (!data || data.length === 0) {
            claimTbody.innerHTML = `
        <tr>
            <td colspan="8" style="text-align:center;">
                등록된 청구 내역이 없습니다.
            </td>
        </tr>`;
            return;
        }

        data.forEach(item => {
            const tr = document.createElement('tr');

            const paidDate = item.paidDate ? item.paidDate.split(' ')[0] : '-';
            const amount = item.amount ? Number(item.amount).toLocaleString() + '원' : '-';
            const paymentMethodText = item.type === 'bill' ? '온라인 카드' : '현장결제';

            const hasCardName = item.type === 'bill' && item.cardName;
            const tooltipStyle = hasCardName ? 'position: relative;' : '';
            const tooltipAttr = hasCardName ? `data-card-name="${item.cardName}"` : '';

            tr.dataset.billId = item.billId || '';
            tr.dataset.studentId = item.studentId || '';
            tr.dataset.paymentKey = item.paymentKey || '';
            tr.dataset.billType = item.billType || '';
            tr.dataset.status = item.status || '';

            tr.innerHTML = `
        <td class="checkbox-group">
            <input type="checkbox" class="claim-checkbox">
        </td>
        <td>${item.studentName || '-'}</td>
        <td>${item.billType === 'EDU_FEE' ? '교육비' : '교재비'}</td>
        <td>${item.expireDate || '-'}</td>
        <td>${paidDate}</td>
        <td class="payment-method-cell" style="${tooltipStyle}" ${tooltipAttr}>
            ${paymentMethodText}
        </td>
        <td>${amount}</td>
        <td>${getStatusText(item.status)}</td>
    `;

            claimTbody.appendChild(tr);
        });

        bindPaymentMethodTooltip();
    }

    function bindPaymentMethodTooltip() {
        const paymentCells = document.querySelectorAll('.payment-method-cell[data-card-name]');

        paymentCells.forEach(cell => {
            const cardName = cell.getAttribute('data-card-name');

            if (!cardName) return;

            cell.addEventListener('mouseenter', (e) => {
                const existingTooltip = document.querySelector('.payment-tooltip');
                if (existingTooltip) {
                    existingTooltip.remove();
                }

                const tooltip = document.createElement('div');
                tooltip.className = 'payment-tooltip';
                tooltip.textContent = cardName;

                document.body.appendChild(tooltip);

                const rect = cell.getBoundingClientRect();
                const tooltipWidth = tooltip.offsetWidth;

                const leftPosition = rect.left + (rect.width / 2) - (tooltipWidth / 2);

                tooltip.style.left = leftPosition + 'px';
                tooltip.style.top = (rect.bottom + 5) + 'px';
            });

            cell.addEventListener('mouseleave', () => {
                const tooltip = document.querySelector('.payment-tooltip');
                if (tooltip) {
                    tooltip.remove();
                }
            });
        });
    }

    function getStatusText(status) {
        switch (status) {
            case 'issued':
                return '결제대기';
            case 'approved':
                return '결제완료';
            case 'canceled':
                return '결제취소';
            case 'destroyed':
                return '청구서파기';
            default:
                return '-';
        }
    }
});


async function exportFilteredDataToExcel() {
    try {
        // 로딩 표시
        const button = event.target;
        button.disabled = true;
        button.textContent = '다운로드 중...';

        // URL에서 년/월 가져오기
        const url = new URL(window.location.href);
        const yy = url.searchParams.get('year');
        const mm = url.searchParams.get('month');

        // 선생님 필터
        const teacherSelect = document.getElementById('student-filter');
        const userCode = teacherSelect?.value || '';

        // 교육비/교재비 라디오 버튼
        // const itemTypeRadio = document.querySelector('input[name="feeView"]:checked');
        const itemType = 'EDU_FEE';

        const centerCode =  'PUS002';

        // 필터 조건 수집
        const filters = {
            userCode: userCode,
            centerCode: centerCode,  // 필요시 추가
            yy: yy,
            mm: mm,
            itemType: itemType
        };

        console.log('전송 데이터:', filters);

        // 백엔드에서 데이터 가져오기
        const response = await fetch('/pay/api/claim/export', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(filters)
        });

        if (!response.ok) {
            throw new Error('데이터 조회 실패');
        }

        const result = await response.json();
        const allData = result.response || result;

        if (!allData || allData.length === 0) {
            alert('다운로드할 데이터가 없습니다.');
            button.disabled = false;
            button.textContent = '📥 엑셀 다운로드';
            return;
        }

        // 워크북 생성
        const wb = XLSX.utils.book_new();

        // 상태별로 시트 분리
        groupByStatus(allData, wb);

        // 파일 다운로드
        const fileName = `청구내역_${yy}년${mm}월_${new Date().toISOString().split('T')[0]}.xlsx`;
        XLSX.writeFile(wb, fileName);

        button.disabled = false;
        button.textContent = '📥 엑셀 다운로드';

    } catch (error) {
        console.error('엑셀 다운로드 실패:', error);
        alert('엑셀 다운로드에 실패했습니다.');
        button.disabled = false;
        button.textContent = '📥 엑셀 다운로드';
    }
}

// 상태별로 시트 분리
function groupByStatus(data, wb) {
    // 상태별로 데이터 그룹화
    const grouped = data.reduce((acc, item) => {
        const status = item.payStatus || '미분류';
        if (!acc[status]) {
            acc[status] = [];
        }
        acc[status].push({
            '이름': item.studentName || '',
            '수강과목': item.subject || '',
            '한자 선생님': item.hanTeacher || '-',
            '독서 선생님': item.bookTeacher || '-',
            '청구금액': Number(item.billPrice || 0),
            '미납금액': Number(item.unpaidAmount || 0),
            '결제금액': Number(item.paidAmount || 0),
            '상태': item.payStatus || ''
        });
        return acc;
    }, {});

    // 각 상태별로 시트 생성
    Object.keys(grouped).forEach(status => {
        const ws = XLSX.utils.json_to_sheet(grouped[status]);

        // 컬럼 너비 설정
        ws['!cols'] = [
            { wch: 12 }, // 이름
            { wch: 15 }, // 수강과목
            { wch: 15 }, // 한자 선생님
            { wch: 15 }, // 독서 선생님
            { wch: 15 }, // 청구금액
            { wch: 15 }, // 미납금액
            { wch: 15 }, // 결제금액
            { wch: 12 }  // 상태
        ];

        XLSX.utils.book_append_sheet(wb, ws, status);
    });

    // 전체 데이터 시트도 추가
    const allSheetData = data.map(item => ({
        '이름': item.studentName || '',
        '수강과목': item.subject || '',
        '한자 선생님': item.hanTeacher || '-',
        '독서 선생님': item.bookTeacher || '-',
        '청구금액': Number(item.billPrice || 0),
        '미납금액': Number(item.unpaidAmount || 0),
        '결제금액': Number(item.paidAmount || 0),
        '상태': item.payStatus || ''
    }));

    const allSheet = XLSX.utils.json_to_sheet(allSheetData);
    allSheet['!cols'] = [
        { wch: 12 },
        { wch: 15 },
        { wch: 15 },
        { wch: 15 },
        { wch: 15 },
        { wch: 15 },
        { wch: 15 },
        { wch: 12 }
    ];
    XLSX.utils.book_append_sheet(wb, allSheet, "전체");
}
// 상태 한글 변환 헬퍼 함수
function getStatusKorean(status) {
    switch (status) {
        case '결제완료':
        case 'approved':
            return '결제완료';
        case '부분결제':
        case 'partial':
            return '부분결제';
        case '미결제':
        case 'pending':
        case 'issued':
            return '미결제';
        case '할인':
        case 'discount':
            return '할인';
        case 'canceled':
            return '취소';
        case 'destroyed':
            return '파기';
        default:
            return status || '미분류';
    }
}