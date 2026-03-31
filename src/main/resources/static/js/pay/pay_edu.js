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
                monthInput.showPicker();
            } else {
                monthInput.focus();
                monthInput.click();
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
        렌더링
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
            tr.dataset.otherSubjectType = s.otherSubjectType || '';

            const isPriceModified = currentFeeView === 'edu' && s.isPriceModified === 1;
            const billPriceStyle = isPriceModified ? 'color: red;' : '';
            const billPriceTitle = isPriceModified && s.standardFee
                ? `표준 금액: ${Number(s.standardFee).toLocaleString()}원`
                : '';
            const otherSubjectDisplay = s.otherSubject && s.otherSubject.trim() !== ''
                ? s.otherSubject
                : '';

            const renderBadges = (subjectType) => {
                if (!subjectType) return '';
                const badges = [];
                if (subjectType.includes('hoho')) badges.push('<span class="badge-hoho">호</span>');
                if (subjectType.includes('han')) badges.push('<span class="badge-han">한</span>');
                if (subjectType.includes('book')) badges.push('<span class="badge-book">독</span>');
                return badges.join('');
            };

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
        <td class="other-subject-cell" style="${otherSubjectDisplay ? 'cursor: pointer;' : ''}">
            <div style="display: flex; justify-content: center; align-items: center; gap: 4px;">
                ${otherSubjectDisplay ? renderBadges(s.otherSubjectType) : ''}
            </div>
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

    /* ===============================
        형제 이름 툴팁
    =============================== */
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

            cell.addEventListener('mouseenter', () => {
                const existingTooltip = document.querySelector('.phone-tooltip');
                if (existingTooltip) existingTooltip.remove();

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
                if (tooltip) tooltip.remove();
            });
        });
    }

    function bindOtherSubjectTooltip() {
        const otherSubjectCells = tbody.querySelectorAll('.other-subject-cell');

        otherSubjectCells.forEach(cell => {
            const row = cell.closest('tr');
            const otherSubject = row.dataset.otherSubject;
            const otherTeacher = row.dataset.otherTeacher;

            if (!otherSubject ||
                otherSubject === 'null' ||
                otherSubject === 'undefined' ||
                otherSubject.trim() === '') {
                return;
            }

            cell.addEventListener('mouseenter', () => {
                const existingTooltip = document.querySelector('.other-subject-tooltip');
                if (existingTooltip) existingTooltip.remove();

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
                tooltip.textContent = otherTeacher ? otherTeacher + ' 선생님' : '';

                document.body.appendChild(tooltip);

                const rect = cell.getBoundingClientRect();
                tooltip.style.left = rect.left + 'px';
                tooltip.style.top = (rect.bottom + 5) + 'px';
            });

            cell.addEventListener('mouseleave', () => {
                const tooltip = document.querySelector('.other-subject-tooltip');
                if (tooltip) tooltip.remove();
            });
        });
    }

    function renderIssueStatus(issuanceStatus) {
        if (!issuanceStatus || issuanceStatus === 'nonIssue') {
            return `<span class="unissued">미발행</span>`;
        }
        if (issuanceStatus === 'issued') return `<span class="issued">발행</span>`;
        if (issuanceStatus === 'nonIssue_off') return `<span class="edu-issued">현장결제</span>`;
        if (issuanceStatus === 'destroyed_off') return `<span class="edu-issued">현장결제</span>`;
        if (issuanceStatus === 'canceled_off') return `<span class="edu-issued">현장결제</span>`;
        if (issuanceStatus === 'destroyed') return `<span class="destroyed">파기</span>`;
        if (issuanceStatus === 'canceled') return `<span class="canceled">취소</span>`;
        if (issuanceStatus === 'discount') return `<span class="discount">할인</span>`;
        return '-';
    }

    function renderPayStatus(totalStatus) {
        if (totalStatus === '결제완료') return `<span class="pay-box pay-done">결제완료</span>`;
        if (totalStatus === '부분결제') return `<span class="pay-box pay-partial">부분결제</span>`;
        if (totalStatus === '미결제') return `<span class="pay-box pay-late">미결제</span>`;
        if (totalStatus === '할인') return `<span class="pay-box pay-discount">할인</span>`;
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

                if (searchType === 'name') targetValue = tr.dataset.studentName || '';
                else if (searchType === 'phone') targetValue = tr.dataset.lastPhone4 || '';

                tr.style.display = (!keyword || targetValue.includes(keyword)) ? '' : 'none';
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

    const getCurrentMonth5th = (year, month) => new Date(year, month - 1, 5);

    const billingMonth = document.querySelector('.hidden-picker')?.value;
    let baseDate;

    if (billingMonth) {
        const [yy, mm] = billingMonth.split('-').map(Number);
        baseDate = getCurrentMonth5th(yy, mm);
    } else {
        const today = new Date();
        baseDate = getCurrentMonth5th(today.getFullYear(), today.getMonth() + 1);
    }

    expireInput.value = formatYYYYMMDD(baseDate);
    setDisplay(baseDate);

    expireInput.min = formatYYYYMMDD(new Date());

    expireBtn.addEventListener('click', () => {
        if (typeof expireInput.showPicker === 'function') expireInput.showPicker();
        else expireInput.click();
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
                    if (cb !== clickedCheckbox) cb.checked = false;
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
        if (targetModal) targetModal.style.display = 'block';
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

            // 일반 row 클릭 → 하단 테이블 토글
            if (row.classList.contains('selected-for-detail')) {
                row.classList.remove('selected-for-detail');
                claimDetailSection.style.display = 'none';
                if (claimDivider) claimDivider.style.display = 'none';
                if (claimButtons) claimButtons.style.display = 'none';
                const nameEl = document.getElementById('claim-student-name');
                if (nameEl) nameEl.style.display = 'none';
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
        청구 상세 데이터 로드
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
        const nameEl = document.getElementById('claim-student-name');
        if (nameEl) {
            const studentName = data && data.length > 0 ? data[0].studentName : '';
            if (studentName) {
                nameEl.textContent = `${studentName} 청구 내역`;
                nameEl.style.display = 'block';
            } else {
                nameEl.style.display = 'none';
            }
        }

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
        <td>${formatYm(item.ym)}</td>
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

    function formatYm(ym) {
        if (!ym) return "-";

        const [year, m] = ym.split("-");
        const month = m?.padStart(2, "0");

        return `${year}년 ${month}월`;
    }

    function bindPaymentMethodTooltip() {
        const paymentCells = document.querySelectorAll('.payment-method-cell[data-card-name]');

        paymentCells.forEach(cell => {
            const cardName = cell.getAttribute('data-card-name');
            if (!cardName) return;

            cell.addEventListener('mouseenter', () => {
                const existingTooltip = document.querySelector('.payment-tooltip');
                if (existingTooltip) existingTooltip.remove();

                const tooltip = document.createElement('div');
                tooltip.className = 'payment-tooltip';
                tooltip.textContent = cardName;

                document.body.appendChild(tooltip);

                const rect = cell.getBoundingClientRect();
                const tooltipWidth = tooltip.offsetWidth;
                tooltip.style.left = (rect.left + rect.width / 2 - tooltipWidth / 2) + 'px';
                tooltip.style.top = (rect.bottom + 5) + 'px';
            });

            cell.addEventListener('mouseleave', () => {
                const tooltip = document.querySelector('.payment-tooltip');
                if (tooltip) tooltip.remove();
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

        // ✅ 인덱스 방식 대신 data-type 속성으로 안전하게 조회
        const hanEduInput = modal.querySelector('.edu-input[data-type="hanEdu"]');
        const hanMaterialInput = modal.querySelector('.edu-input[data-type="hanMaterial"]');
        const bookEduInput = modal.querySelector('.edu-input[data-type="bookEdu"]');
        const bookMaterialInput = modal.querySelector('.edu-input[data-type="bookMaterial"]');

        hanEduInput.value = hanEduFee.toLocaleString();
        hanEduInput.dataset.rawValue = hanEduFee;

        hanMaterialInput.value = hanMaterialFee.toLocaleString();
        hanMaterialInput.dataset.rawValue = hanMaterialFee;

        bookEduInput.value = bookEduFee.toLocaleString();
        bookEduInput.dataset.rawValue = bookEduFee;

        bookMaterialInput.value = bookMaterialFee.toLocaleString();
        bookMaterialInput.dataset.rawValue = bookMaterialFee;

        updateTotal(modal);
        setupInputListeners(modal);
        setupSaveButton(modal, firstItem.studentId);

        // ✅ 전화번호 수정 세팅
        setupPhoneEditor(modal, firstItem.studentId, firstItem.billingPhone);

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
        전화번호 수정
    ========================= */
    function setupPhoneEditor(modal, studentId, currentPhone) {
        const phoneInput = modal.querySelector('#modal-phone-input');
        const phoneSaveBtn = modal.querySelector('#modal-phone-save-btn');
        if (!phoneInput || !phoneSaveBtn) return;

        phoneInput.value = formatPhoneDisplay(currentPhone || '');

        phoneInput.addEventListener('input', (e) => {
            const raw = e.target.value.replace(/[^0-9]/g, '');
            e.target.value = formatPhoneDisplay(raw);
        });

        // 중복 바인딩 방지 - 버튼 교체
        const newBtn = phoneSaveBtn.cloneNode(true);
        phoneSaveBtn.parentNode.replaceChild(newBtn, phoneSaveBtn);

        newBtn.addEventListener('click', async () => {
            const rawPhone = phoneInput.value.replace(/[^0-9]/g, '');

            if (!rawPhone || rawPhone.length < 10) {
                alert('올바른 전화번호를 입력하세요.');
                return;
            }

            if (!confirm(`전화번호를 ${phoneInput.value} 으로 변경하시겠습니까?`)) return;

            try {
                const res = await fetch('/pay/update-phone', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({studentId, phone: rawPhone})
                });

                const result = await res.json();

                if (!res.ok || !result.success) {
                    throw new Error(result.msg || '전화번호 변경에 실패했습니다.');
                }

                alert('전화번호가 변경되었습니다.');

                // 상단 테이블 갱신
                const monthInput = document.querySelector('.hidden-picker');
                const teacherSelect = document.getElementById('student-filter');
                const [year, month] = monthInput.value.split('-');
                await fetchStudentsGlobal(year, month, teacherSelect.value);

            } catch (err) {
                console.error('❌ 전화번호 변경 오류:', err);
                alert(err.message);
            }
        });
    }

    // 전화번호 자동 하이픈 포맷 (010-1234-5678)
    function formatPhoneDisplay(phone) {
        const raw = String(phone).replace(/[^0-9]/g, '');
        if (raw.length <= 3) return raw;
        if (raw.length <= 7) return `${raw.slice(0, 3)}-${raw.slice(3)}`;
        return `${raw.slice(0, 3)}-${raw.slice(3, 7)}-${raw.slice(7, 11)}`;
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
        if (sumCell) sumCell.textContent = total.toLocaleString();
    }

    /* =========================
        저장 버튼
    ========================= */
    function setupSaveButton(modal, studentId) {
        const saveBtn = modal.querySelector('#modal-fee-save-btn');
        if (!saveBtn) return;

        const newBtn = saveBtn.cloneNode(true);
        saveBtn.parentNode.replaceChild(newBtn, saveBtn);

        newBtn.addEventListener('click', async () => {
            console.log('저장버튼 클릭');
            // ✅ data-type 속성으로 안전하게 조회 (행 인덱스 불필요)
            const hanEduFee = Number(modal.querySelector('.edu-input[data-type="hanEdu"]').dataset.rawValue || 0);
            const hanMaterialFee = Number(modal.querySelector('.edu-input[data-type="hanMaterial"]').dataset.rawValue || 0);
            const bookEduFee = Number(modal.querySelector('.edu-input[data-type="bookEdu"]').dataset.rawValue || 0);
            const bookMaterialFee = Number(modal.querySelector('.edu-input[data-type="bookMaterial"]').dataset.rawValue || 0);

            const url = new URL(window.location.href);
            const yy = url.searchParams.get('year');
            const mm = url.searchParams.get('month');

            if (!yy || !mm) {
                alert('년월 정보가 없습니다.');
                return;
            }

            if (!confirm('수강료를 저장하시겠습니까?')) return;

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

                    modal.style.display = 'none';

                    const monthInput = document.querySelector('.hidden-picker');
                    const teacherSelect = document.getElementById('student-filter');
                    const [year, month] = monthInput.value.split('-');
                    await fetchStudentsGlobal(year, month, teacherSelect.value);

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
    // 1. 청구서 발행
    // ================================
    payIssue.addEventListener('click', async () => {

        const checkedBoxes = document.querySelectorAll(
            '#student-tbody input[type="checkbox"]:checked'
        );

        if (checkedBoxes.length === 0) return alert('학생을 선택하세요.');

        const eduChecked = document.querySelector('input[name="eduFee"]').checked;
        const bookChecked = document.querySelector('input[name="bookFee"]').checked;

        if (!eduChecked && !bookChecked) return alert('청구 종류를 선택하세요.');

        const selectedMonth = document.querySelector('.hidden-date.hidden-picker').value;
        const [yy, mm] = selectedMonth.split('-');
        const expireDt = document.querySelector('.expire-input').value;

        const studentIds = Array.from(checkedBoxes).map(
            box => box.closest('tr').dataset.studentId
        );

        const includeSibling = document.querySelector('input[name="includeSibling"]').checked;
        const customPriceInput = document.querySelector('.edu-input').value;
        const customPrice = customPriceInput ? parseInt(customPriceInput.replace(/,/g, '')) : null;
        const customMessage = document.querySelector('input[name="message"]')?.value?.trim();

        if (customPrice !== null && customPrice <= 0) return alert('올바른 금액을 입력하세요.');

        const types = [];
        if (eduChecked) types.push({type: 'edu', label: '교육비'});
        if (bookChecked) types.push({type: 'material', label: '교재비'});

        const jobId = crypto.randomUUID();
        const overlay = showProgressOverlay();
        const evtSource = new EventSource(`/pay/progress/${jobId}`);

        evtSource.addEventListener('progress', (e) => {
            try {
                const data = JSON.parse(e.data);
                updateOverlayProgress(overlay, data);
                if (data.status === 'done') evtSource.close();
            } catch (err) {
                console.error('[SSE] 파싱 오류:', err, '원본 데이터:', e.data);
            }
        });

        evtSource.onerror = () => evtSource.close();

        await new Promise((resolve) => {
            evtSource.onopen = () => resolve();
            setTimeout(() => resolve(), 1500);
        });

        let totalSuccessCount = 0;
        let totalFailCount = 0;
        const failMessages = [];

        for (let i = 0; i < types.length; i++) {
            const {type, label} = types[i];

            updateOverlayLabel(overlay, `${label} 청구서 발행 중...`);

            const result = await sendBillsSafe({
                studentIds,
                type,
                message: customMessage || `${mm}월 ${label} 청구`,
                expireDt,
                yy,
                mm,
                includeSibling,
                customPrice,
                jobId
            });

            if (result.success) {
                totalSuccessCount += result.successCount;
                totalFailCount += result.failCount;
            } else {
                totalFailCount++;
                failMessages.push(`${label}: ${result.error}`);
            }
        }

        evtSource.close();
        hideProgressOverlay(overlay);

        let resultMsg = `✅ 발행 완료: ${totalSuccessCount}건\n❌ 실패: ${totalFailCount}건`;
        if (failMessages.length > 0) resultMsg += `\n\n실패 내용:\n${failMessages.join('\n')}`;
        alert(resultMsg);

        const monthInput = document.querySelector('.hidden-picker');
        const teacherSelect = document.getElementById('student-filter');
        const [year, month] = monthInput.value.split('-');
        await fetchStudentsGlobal(year, month, teacherSelect.value);
    });

    async function sendBillsSafe(body) {
        try {
            const res = await fetch('/pay/send', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(body)
            });

            const data = await res.json();

            if (!res.ok || !data.success) {
                return {success: false, error: data.msg || data.response || '청구 실패'};
            }

            return {
                success: true,
                successCount: data.response?.successCount || 0,
                failCount: data.response?.failCount || 0
            };
        } catch (e) {
            return {success: false, error: e.message || '네트워크 오류'};
        }
    }

    function showProgressOverlay() {
        if (!document.getElementById('spinner-style')) {
            const style = document.createElement('style');
            style.id = 'spinner-style';
            style.textContent = `@keyframes spin { to { transform: rotate(360deg); } }`;
            document.head.appendChild(style);
        }

        const overlay = document.createElement('div');
        overlay.id = 'progress-overlay';
        overlay.style.cssText = `
        position: fixed; inset: 0;
        background: rgba(0, 0, 0, 0.45);
        display: flex; align-items: center; justify-content: center;
        z-index: 99999;
    `;

        overlay.innerHTML = `
        <div style="
            background: white; border-radius: 12px;
            padding: 32px 48px; text-align: center;
            box-shadow: 0 8px 32px rgba(0,0,0,0.2); min-width: 340px;
        ">
            <div style="
                width: 40px; height: 40px;
                border: 4px solid #e5e7eb; border-top-color: #6366f1;
                border-radius: 50%; animation: spin 0.8s linear infinite;
                margin: 0 auto 20px;
            "></div>
            <p id="overlay-label" style="margin: 0 0 6px; font-size: 15px; font-weight: 600; color: #374151;">청구서 발행 중...</p>
            <p id="overlay-student" style="margin: 0 0 16px; font-size: 13px; color: #6b7280; min-height: 18px;">준비 중...</p>
            <div style="background: #e5e7eb; border-radius: 999px; height: 8px; overflow: hidden; margin-bottom: 10px;">
                <div id="overlay-bar" style="height: 100%; width: 0%; background: #6366f1; border-radius: 999px; transition: width 0.3s ease;"></div>
            </div>
            <p id="overlay-count" style="margin: 0; font-size: 13px; color: #9ca3af;">0 / 0 (0%)</p>
        </div>
    `;

        document.body.appendChild(overlay);
        return overlay;
    }

    function updateOverlayLabel(overlay, text) {
        const el = overlay.querySelector('#overlay-label');
        if (el) el.textContent = text;
    }

    function updateOverlayProgress(overlay, data) {
        const {current, total, studentName, successCount, failCount, status} = data;

        const bar = overlay.querySelector('#overlay-bar');
        const count = overlay.querySelector('#overlay-count');
        const student = overlay.querySelector('#overlay-student');
        const pct = total > 0 ? Math.round((current / total) * 100) : 0;

        if (bar) bar.style.width = `${pct}%`;

        if (count) {
            count.textContent = status === 'done'
                ? `완료 ✅ (성공: ${successCount}건 / 실패: ${failCount}건)`
                : `${current} / ${total} (${pct}%)`;
        }

        if (student) {
            if (status === 'done') student.textContent = '발행 완료!';
            else if (studentName) student.textContent = `처리 중: ${studentName}`;
        }
    }

    function hideProgressOverlay(overlay) {
        if (overlay && overlay.parentNode) overlay.parentNode.removeChild(overlay);
    }

    // ================================
    // 2. 결제 취소
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

        if (!confirm(`${studentName} 학생의 결제를 정말 취소하시겠습니까?\n\n※ 이 작업은 되돌릴 수 없습니다.`)) return;

        try {
            const res = await fetch('/pay/cancel', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({paymentKey, cancelType: billType, cancelReason: cancelReason.trim()})
            });

            const data = await res.json();
            if (!res.ok) throw new Error(data.msg || '결제 취소에 실패했습니다.');

            alert(data.data || '결제가 취소되었습니다.');

            const url = new URL(window.location.href);
            await fetchBillDetail(studentId, url.searchParams.get('year'), url.searchParams.get('month'));

        } catch (err) {
            console.error('❌ 결제 취소 오류:', err);
            alert(err.message);
        }
    });

    // ================================
    // 3. 청구서 파기
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
        if (!confirm(`${studentName} 학생의 청구서를 파기하시겠습니까?`)) return;

        try {
            const res = await fetch('/pay/destroy/bill', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({billId, studentId, paymentKey, destroyType: billType})
            });

            const data = await res.json();
            if (!res.ok) throw new Error(data.msg || '청구서 파기에 실패했습니다.');

            alert(data.data || '청구서가 파기되었습니다.');

            const url = new URL(window.location.href);
            await fetchBillDetail(studentId, url.searchParams.get('year'), url.searchParams.get('month'));

        } catch (err) {
            console.error('❌ 청구서 파기 오류:', err);
            alert(err.message);
        }
    });

    // ================================
    // 4. 청구서 재발행 (단건)
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
            if (!data.success) return alert(data.msg || '재발행 실패');

            alert('재발행이 완료되었습니다.');

            const url = new URL(window.location.href);
            await fetchBillDetail(studentId, url.searchParams.get('year'), url.searchParams.get('month'));

        } catch (e) {
            console.error(e);
            alert('재발행 중 오류가 발생했습니다.');
        }
    });

    // ================================
    // 5. 체크된 학생 일괄 재발행
    // ================================
    const payCheckedReissue = document.querySelector('#pay-checked-reissue');

    payCheckedReissue.addEventListener('click', async () => {
        const checkedBoxes = document.querySelectorAll(
            '#student-tbody input[type="checkbox"]:checked'
        );

        if (checkedBoxes.length === 0) return alert('재발행할 학생을 선택하세요.');

        const billIds = Array.from(checkedBoxes)
            .map(cb => cb.closest('tr').dataset.billId)
            .filter(id => id && id !== 'null' && id !== 'undefined' && id !== '');

        if (billIds.length === 0) {
            return alert('선택된 학생 중 발행된 청구서가 없습니다.\n(미발행 상태인 학생은 재발행 불가)');
        }

        if (!confirm(`총 ${billIds.length}건의 청구서를 재발행하시겠습니까?`)) return;

        try {
            const res = await fetch('/pay/reissue', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({billIds})
            });

            const data = await res.json();
            if (!data.success) return alert(data.msg || '재발행 실패');

            alert(`재발행이 완료되었습니다. (${billIds.length}건)`);

            const monthInput = document.querySelector('.hidden-picker');
            const teacherSelect = document.getElementById('student-filter');
            const [year, month] = monthInput.value.split('-');
            await fetchStudentsGlobal(year, month, teacherSelect.value);

        } catch (e) {
            console.error('❌ 재발행 오류:', e);
            alert('재발행 중 오류가 발생했습니다.');
        }
    });

    // ================================
    // fetchBillDetail / fillClaimDetailTable (버튼 스코프용 중복 선언)
    // ================================
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
        const nameEl = document.getElementById('claim-student-name');
        if (nameEl) {
            const studentName = data && data.length > 0 ? data[0].studentName : '';
            if (studentName) {
                nameEl.textContent = `${studentName} 청구 내역`;
                nameEl.style.display = 'block';
            } else {
                nameEl.style.display = 'none';
            }
        }
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
        <td>${item.ym || '-'}</td>
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

            cell.addEventListener('mouseenter', () => {
                const existingTooltip = document.querySelector('.payment-tooltip');
                if (existingTooltip) existingTooltip.remove();

                const tooltip = document.createElement('div');
                tooltip.className = 'payment-tooltip';
                tooltip.textContent = cardName;

                document.body.appendChild(tooltip);

                const rect = cell.getBoundingClientRect();
                const tooltipWidth = tooltip.offsetWidth;
                tooltip.style.left = (rect.left + rect.width / 2 - tooltipWidth / 2) + 'px';
                tooltip.style.top = (rect.bottom + 5) + 'px';
            });

            cell.addEventListener('mouseleave', () => {
                const tooltip = document.querySelector('.payment-tooltip');
                if (tooltip) tooltip.remove();
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

// ========== 엑셀 다운로드 ========== //
async function exportFilteredDataToExcel() {
    try {
        const button = event.target;
        button.disabled = true;
        button.textContent = '다운로드 중...';

        const url = new URL(window.location.href);
        const yy = url.searchParams.get('year');
        const mm = url.searchParams.get('month');
        const userCode = document.getElementById('student-filter')?.value || '';

        const filters = {
            userCode,
            centerCode: 'PUS002',
            yy,
            mm,
            itemType: 'EDU_FEE'
        };

        const response = await fetch('/pay/api/claim/export', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(filters)
        });

        if (!response.ok) throw new Error('데이터 조회 실패');

        const result = await response.json();
        const allData = result.response || result;

        if (!allData || allData.length === 0) {
            alert('다운로드할 데이터가 없습니다.');
            button.disabled = false;
            button.textContent = '엑셀 다운로드';
            return;
        }

        const wb = XLSX.utils.book_new();

        const groupedByTeacher = groupDataByTeacher(allData);

        Object.keys(groupedByTeacher).sort().forEach(teacherName => {
            const teacherData = groupedByTeacher[teacherName];
            const title = `${mm}월 ${teacherName} 결제 기록`;
            const sheetData = teacherData.map(item => {
                const paidAmount = Number(item.paidAmount || 0);
                const billPrice = Number(item.billPrice || 0);
                const calculatedPaid = item.payStatus === '미결제' ? 0 : (paidAmount - (paidAmount - billPrice));
                return {
                    '이름': item.studentName || '',
                    '수강과목': item.subject || '',
                    '청구금액': billPrice,
                    '결제금액': calculatedPaid,
                    '미납금액': Number(item.unpaidAmount || 0),
                    '상태': item.payStatus || ''
                };
            });

            const sheet = createSheetWithTitle(sheetData, title);
            const sheetName = teacherName.length > 31 ? teacherName.substring(0, 31) : teacherName;
            XLSX.utils.book_append_sheet(wb, sheet, sheetName);
        });

        const allSheetData = allData.map(item => {
            const paidAmount = Number(item.paidAmount || 0);
            const billPrice = Number(item.billPrice || 0);
            const calculatedPaid = item.payStatus === '미결제' ? 0 : (paidAmount - (paidAmount - billPrice));
            return {
                '선생님': item.teacherName || item.hanTeacher || item.bookTeacher || '미지정',
                '이름': item.studentName || '',
                '수강과목': item.subject || '',
                '청구금액': billPrice,
                '결제금액': calculatedPaid,
                '미납금액': Number(item.unpaidAmount || 0),
                '상태': item.payStatus || ''
            };
        });

        const allSheet = createSheetWithTitle(allSheetData, `${mm}월 전체 결제 기록`, true);
        XLSX.utils.book_append_sheet(wb, allSheet, "전체");

        XLSX.writeFile(wb, `청구내역_${yy}년${mm}월_${new Date().toISOString().split('T')[0]}.xlsx`);

        button.disabled = false;
        button.textContent = '엑셀 다운로드';

    } catch (error) {
        console.error('엑셀 다운로드 실패:', error);
        alert('엑셀 다운로드에 실패했습니다.');
        event.target.disabled = false;
        event.target.textContent = '엑셀 다운로드';
    }
}

function groupDataByTeacher(data) {
    return data.reduce((acc, item) => {
        const teacherName = item.teacherName || item.hanTeacher || item.bookTeacher || '미지정';
        if (!acc[teacherName]) acc[teacherName] = [];
        acc[teacherName].push(item);
        return acc;
    }, {});
}

function createSheetWithTitle(data, title, isAllSheet = false) {
    const sheetData = [[title], []];

    let totalCount = data.length;
    let totalBill = 0;
    let totalPaid = 0;
    let totalUnpaid = 0;

    if (data.length > 0) {
        const headers = ['번호', ...Object.keys(data[0])];
        sheetData.push(headers);

        data.forEach((row, index) => {
            const originalHeaders = Object.keys(data[0]);
            sheetData.push([index + 1, ...originalHeaders.map(h => row[h])]);
            totalBill += row['청구금액'] || 0;
            totalPaid += row['결제금액'] || 0;
            totalUnpaid += row['미납금액'] || 0;
        });

        if (isAllSheet) {
            sheetData.push([`총원:`, `${totalCount}명`, '', '', totalBill, totalPaid, totalUnpaid, '']);
        } else {
            sheetData.push([`총원:`, `${totalCount}명`, '', totalBill, totalPaid, totalUnpaid, '']);
        }
    }

    const ws = XLSX.utils.aoa_to_sheet(sheetData);
    const range = XLSX.utils.decode_range(ws['!ref']);
    const amountColumns = isAllSheet ? [4, 5, 6] : [3, 4, 5];

    for (let R = range.s.r; R <= range.e.r; R++) {
        for (let C = range.s.c; C <= range.e.c; C++) {
            const cellAddress = XLSX.utils.encode_cell({r: R, c: C});
            if (!ws[cellAddress]) ws[cellAddress] = {t: 's', v: ''};
            if (!ws[cellAddress].s) ws[cellAddress].s = {};

            ws[cellAddress].s = {alignment: {horizontal: 'center', vertical: 'center', wrapText: false}};

            if (R > 2 && amountColumns.includes(C)) {
                const cellValue = ws[cellAddress].v;
                if (typeof cellValue === 'number' || !isNaN(cellValue)) {
                    ws[cellAddress].t = 'n';
                    ws[cellAddress].v = Number(cellValue);
                    ws[cellAddress].z = '#,##0';
                    ws[cellAddress].s = {alignment: {horizontal: 'center', vertical: 'center'}, numFmt: '#,##0'};
                }
            }
        }
    }

    if (!ws['!merges']) ws['!merges'] = [];
    ws['!merges'].push({s: {r: 0, c: 0}, e: {r: 0, c: isAllSheet ? 7 : 6}});

    if (!ws['!rows']) ws['!rows'] = [];
    ws['!rows'][0] = {hpt: 30, hpx: 30};

    ws['!cols'] = isAllSheet
        ? [{wch: 10}, {wch: 12}, {wch: 12}, {wch: 20}, {wch: 15}, {wch: 15}, {wch: 15}, {wch: 12}]
        : [{wch: 10}, {wch: 12}, {wch: 20}, {wch: 15}, {wch: 15}, {wch: 15}, {wch: 12}];

    return ws;
}

// ========== 미납 알림톡 발송 ========== //
document.addEventListener('DOMContentLoaded', () => {
    const btnPayRemind = document.querySelector("#pay-remind");
    if (!btnPayRemind) return;

    const formatPhone = (phone) => {
        if (!phone) return '-';
        const cleaned = phone.replace(/[^0-9]/g, '');
        if (cleaned.length === 11) {
            return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(7)}`;
        }
        return phone;
    };

    btnPayRemind.addEventListener("click", async () => {
        try {
            const monthInput = document.querySelector('.hidden-date.hidden-picker');
            const yearMonth = {
                year: monthInput.value.split("-")[0],
                month: monthInput.value.split("-")[1]
            };

            const res = await fetch("/pay/api/remind/unpaid-students", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(yearMonth)
            });

            if (!res.ok) throw new Error("미납 학생 조회 실패");

            const result = await res.json();
            const unpaidList = result.response || [];

            if (!unpaidList.length) {
                alert("미납 학생이 없습니다.");
                return;
            }

            const phoneGroupMap = new Map();
            unpaidList.forEach(s => {
                const phone = s.parentPhone?.replace(/[^0-9]/g, '') || 'unknown';
                if (!phoneGroupMap.has(phone)) {
                    phoneGroupMap.set(phone, {
                        parentPhone: s.parentPhone,
                        students: [],
                        totalUnpaidAmount: 0,
                        paymentKeys: []
                    });
                }
                const group = phoneGroupMap.get(phone);
                group.students.push(s.studentName);
                group.totalUnpaidAmount += Number(s.totalUnpaidAmount || 0);
                group.paymentKeys.push(s.paymentKey);
            });

            const groupedList = Array.from(phoneGroupMap.values());

            const confirmMessage =
                `알림톡 발송 대상: ${groupedList.length}건\n\n` +
                groupedList.map(g =>
                    `• ${g.students.join(", ")} / ${formatPhone(g.parentPhone)} / ${g.totalUnpaidAmount.toLocaleString('ko-KR')}원`
                ).join('\n') +
                `\n\n발송하시겠습니까?`;

            if (!confirm(confirmMessage)) return;

            const sendRes = await fetch("/popbill/remind/send", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({
                    year: yearMonth.year,
                    month: yearMonth.month,
                    students: groupedList.map(g => ({
                        studentName: g.students.join(", "),
                        parentPhone: g.parentPhone,
                        totalUnpaidAmount: g.totalUnpaidAmount,
                        paymentKeys: g.paymentKeys
                    }))
                })
            });

            if (!sendRes.ok) throw new Error("알림톡 발송 실패");

            const sendResult = await sendRes.json();

            if (sendResult.success) {
                alert(sendResult.response?.message || `알림톡 발송이 완료되었습니다. (${unpaidList.length}건)`);
            } else {
                alert(sendResult.message || "알림톡 발송 중 오류가 발생했습니다.");
            }

        } catch (err) {
            console.error("미납 알림톡 발송 오류:", err);
            alert("알림톡 발송 중 오류가 발생했습니다.");
        }
    });
});