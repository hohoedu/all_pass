const students = [
    {id: 1, name: '김민지', phone: '010-2222-3333', subjects: ['한스쿨', '북스쿨'], siblings: [2], tuition: 220000, arrears: 50000, textbookFee: 30000, textbookPaid: true},
    {id: 2, name: '김서준', phone: '010-2222-3334', subjects: ['북스쿨'], siblings: [1], tuition: 180000, arrears: 0, textbookFee: 20000, textbookPaid: false},
    {id: 3, name: '김민지', phone: '010-7777-1004', subjects: ['호호스쿨'], siblings: [], tuition: 160000, arrears: 30000, textbookFee: 15000, textbookPaid: true},
    {id: 4, name: '이서준', phone: '010-5555-1212', subjects: ['한스쿨'], siblings: [5, 6], tuition: 250000, arrears: 0, textbookFee: 30000, textbookPaid: false},
    {id: 5, name: '이서연', phone: '010-5555-1213', subjects: ['북스쿨'], siblings: [4, 6], tuition: 90000, arrears: 10000, textbookFee: 20000, textbookPaid: true},
    {id: 6, name: '이도윤', phone: '010-5555-1214', subjects: ['호호스쿨'], siblings: [4, 5], tuition: 210000, arrears: 20000, textbookFee: 15000, textbookPaid: false},
    {id: 7, name: '박지호', phone: '010-8888-0001', subjects: ['한스쿨'], siblings: [], tuition: 130000, arrears: 0, textbookFee: 30000, textbookPaid: true}
];

let currentList = [];
let searchTimer = null;
let selectedStudent = null;
let selectedFamily = [];
let selectedFinalTotal = 0;

const headerEl = document.getElementById('header');
const listEl = document.getElementById('list');
const searchInputEl = document.getElementById('searchInput');
const searchBtnEl = document.getElementById('searchBtn');
const sheetEl = document.getElementById('detailSheet');
const registerSheetEl = document.getElementById('registerSheet');
const backdropEl = document.getElementById('sheetBackdrop');
const closeSheetBtn = document.getElementById('closeSheetBtn');
const closeSheetBtnBottom = document.getElementById('closeSheetBtnBottom');
const openRegisterBtn = document.getElementById('openRegisterBtn');
const closeRegisterBtn = document.getElementById('closeRegisterBtn');
const backToDetailBtn = document.getElementById('backToDetailBtn');
const saveRegisterBtn = document.getElementById('saveRegisterBtn');

const useCard = document.getElementById('useCard');
const useCash = document.getElementById('useCash');
const useTransfer = document.getElementById('useTransfer');
const cardMethod = document.getElementById('cardMethod');
const cashMethod = document.getElementById('cashMethod');
const transferMethod = document.getElementById('transferMethod');
const cardCompany = document.getElementById('cardCompany');
const cardAmount = document.getElementById('cardAmount');
const cashAmount = document.getElementById('cashAmount');
const transferAmount = document.getElementById('transferAmount');
const enteredAmount = document.getElementById('enteredAmount');
const expectedAmount = document.getElementById('expectedAmount');
const registerStatus = document.getElementById('registerStatus');
const paymentDate = document.getElementById('paymentDate');
const cashTaxType = document.getElementById('cashTaxType');
const cashIssueType = document.getElementById('cashIssueType');
const cashReceiptDetail = document.getElementById('cashReceiptDetail');
const cashReceiptNumberWrap = document.getElementById('cashReceiptNumberWrap');
const cashReceiptNumber = document.getElementById('cashReceiptNumber');
const transferTaxType = document.getElementById('transferTaxType');
const transferIssueType = document.getElementById('transferIssueType');
const transferReceiptDetail = document.getElementById('transferReceiptDetail');
const transferReceiptNumberWrap = document.getElementById('transferReceiptNumberWrap');
const transferReceiptNumber = document.getElementById('transferReceiptNumber');

function formatWon(value) {
    return Number(value || 0).toLocaleString('ko-KR') + '원';
}

function digitsOnly(value) {
    return String(value || '').replace(/[^0-9]/g, '');
}

function formatMoneyInput(el) {
    const only = digitsOnly(el.value);
    el.value = only ? Number(only).toLocaleString('ko-KR') : '';
}

function getMoneyValue(el) {
    return Number(digitsOnly(el.value) || 0);
}

function getToday() {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const d = String(now.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

function getSubjectClass(subject) {
    if (subject === '한스쿨') return 'tag-hanschool';
    if (subject === '북스쿨') return 'tag-bookschool';
    if (subject === '호호스쿨') return 'tag-hohoschool';
    return 'tag-hanschool';
}

function getStudentById(id) {
    return students.find(student => student.id === id);
}

function updateHeaderState(isDefault) {
    if (isDefault) {
        headerEl.classList.add('centered');
        headerEl.classList.remove('compact');
    } else {
        headerEl.classList.remove('centered');
        headerEl.classList.add('compact');
    }
}

function render(list, isDefaultState = false) {
    currentList = list;
    updateHeaderState(isDefaultState);
    listEl.classList.toggle('is-empty', !list.length);

    if (!list.length) {
        listEl.innerHTML = searchInputEl.value.trim()
            ? '<div class="empty">이름에 맞는 학생을 찾고 있어요.</div>'
            : '<div class="empty">이름 또는 전화번호를 입력해 학생을 조회해주세요.</div>';
        return;
    }

    listEl.innerHTML = list.map((s, idx) => `
      <div class="item" onclick="selectStudent(${idx})">
        <div class="row">
          <div>
            <div class="name">${s.studentName}</div>
            <div class="phone">${s.phone}</div>
            <div class="sub">형제: ${s.siblings.length ? s.siblings.length + '명' : '없음'}</div>
          </div>
        </div>
        <div class="subjects">
          ${s.subjects.map((sub) => `<span class="tag ${getSubjectClass(sub)}">${sub}</span>`).join('')}
        </div>
      </div>
    `).join('');
}

function openBackdrop() {
    backdropEl.classList.add('open');
    document.body.classList.add('sheet-open');
}

function closeBackdrop() {
    backdropEl.classList.remove('open');
    document.body.classList.remove('sheet-open');
}

function openDetailSheet() {
    sheetEl.classList.add('open');
    sheetEl.setAttribute('aria-hidden', 'false');
    registerSheetEl.classList.remove('open');
    registerSheetEl.setAttribute('aria-hidden', 'true');
    openBackdrop();
}

function closeDetailSheet() {
    sheetEl.classList.remove('open');
    sheetEl.setAttribute('aria-hidden', 'true');
    if (!registerSheetEl.classList.contains('open')) closeBackdrop();
}

function openRegisterSheet() {
    registerSheetEl.classList.add('open');
    registerSheetEl.setAttribute('aria-hidden', 'false');
    sheetEl.classList.remove('open');
    sheetEl.setAttribute('aria-hidden', 'true');
    openBackdrop();
}

function closeRegisterSheet() {
    registerSheetEl.classList.remove('open');
    registerSheetEl.setAttribute('aria-hidden', 'true');
    if (!sheetEl.classList.contains('open')) closeBackdrop();
}

function closeAllSheets() {
    closeDetailSheet();
    closeRegisterSheet();
    closeBackdrop();
}

function getHeroColor(subjects) {
    if (subjects.includes('한스쿨')) return ['#ef4d8a', '#f472a1'];
    if (subjects.includes('북스쿨')) return ['#15a1cf', '#4cc3e8'];
    if (subjects.includes('호호스쿨')) return ['#f05a21', '#f78a5f'];
    return ['#4f46e5', '#6d63f3'];
}

function selectStudent(index) {
    const student = currentList[index];
    if (!student) return;

    const family = student.siblings.map(getStudentById).filter(Boolean);
    const ownTotal = student.tuition + student.arrears;
    const familyTotal = family.reduce((sum, member) => sum + member.tuition + member.arrears, 0);
    const finalTotal = ownTotal + familyTotal;

    selectedStudent = student;
    selectedFamily = family;
    selectedFinalTotal = finalTotal;

    document.getElementById('sheetStudentName').textContent = student.name;
    document.getElementById('sheetStudentPhone').textContent = student.phone;
    document.getElementById('sheetStudentSubjects').innerHTML = student.subjects.map(sub => `<span class="tag ${getSubjectClass(sub)}">${sub}</span>`).join('');
    document.getElementById('sheetStudentSiblings').textContent = family.length ? family.length + '명' : '없음';
    document.getElementById('heroFamilyCount').textContent = family.length ? family.length + '명' : '없음';
    document.getElementById('sheetFinalAmount').textContent = formatWon(finalTotal);

    const hero = document.querySelector('.hero');
    const [c1, c2] = getHeroColor(student.subjects);
    hero.style.background = `linear-gradient(135deg, ${c1} 0%, ${c2} 100%)`;

    document.getElementById('mainPriceList').innerHTML = `
      <div class="price-row meta">
        <div class="price-label">교재비</div>
        <div class="price-value">${formatWon(student.textbookFee)} · ${student.textbookPaid ? '결제완료' : '미결제'}</div>
      </div>
      <div class="price-row">
        <div class="price-label">해당월 수강료</div>
        <div class="price-value">${formatWon(student.tuition)}</div>
      </div>
      <div class="price-row">
        <div class="price-label">전월 미납금</div>
        <div class="price-value">${formatWon(student.arrears)}</div>
      </div>
      <div class="price-row total-row accent">
        <div class="price-label">본인 소계</div>
        <div class="price-value">${formatWon(ownTotal)}</div>
      </div>
    `;

    const familyList = document.getElementById('familyList');
    if (!family.length) {
        familyList.innerHTML = '<div class="empty" style="padding:20px 0;">형제 수강 정보가 없습니다.</div>';
    } else {
        familyList.innerHTML = family.map(member => {
            const memberTotal = member.tuition + member.arrears;
            return `
          <div class="family-card">
            <div class="family-top">
              <div>
                <div class="family-name">${member.name}</div>
                <div class="family-meta">${member.phone} · ${member.subjects.join(', ')}</div>
              </div>
              <div class="family-total-chip">${formatWon(memberTotal)}</div>
            </div>
            <div class="price-list" style="margin-top:12px;">
              <div class="price-row meta">
                <div class="price-label">교재비</div>
                <div class="price-value">${formatWon(member.textbookFee)} · ${member.textbookPaid ? '결제완료' : '미결제'}</div>
              </div>
              <div class="price-row">
                <div class="price-label">해당월 수강료</div>
                <div class="price-value">${formatWon(member.tuition)}</div>
              </div>
              <div class="price-row">
                <div class="price-label">전월 미납금</div>
                <div class="price-value">${formatWon(member.arrears)}</div>
              </div>
            </div>
          </div>
        `;
        }).join('');
    }

    document.getElementById('summaryPriceList').innerHTML = `
      <div class="price-row">
        <div class="price-label">본인 소계</div>
        <div class="price-value">${formatWon(ownTotal)}</div>
      </div>
      <div class="price-row">
        <div class="price-label">형제 포함 금액</div>
        <div class="price-value">${formatWon(familyTotal)}</div>
      </div>
      <div class="price-row total-row accent">
        <div class="price-label">최종 납부 금액</div>
        <div class="price-value">${formatWon(finalTotal)}</div>
      </div>
    `;

    prepareRegisterSheet();
    openDetailSheet();
}

function prepareRegisterSheet() {
    if (!selectedStudent) return;
    document.getElementById('registerStudentName').textContent = selectedStudent.name;
    document.getElementById('registerFinalAmount').textContent = formatWon(selectedFinalTotal);
    expectedAmount.textContent = formatWon(selectedFinalTotal);
    paymentDate.value = getToday();

    useCard.checked = false;
    useCash.checked = false;
    useTransfer.checked = false;
    cardCompany.value = '';
    cardAmount.value = '';
    cashAmount.value = '';
    transferAmount.value = '';
    cashTaxType.value = '비과세';
    cashIssueType.value = '개인';
    cashReceiptNumber.value = '';
    cashReceiptDetail.style.display = 'none';
    cashReceiptNumberWrap.style.display = 'block';
    transferTaxType.value = '비과세';
    transferIssueType.value = '개인';
    transferReceiptNumber.value = '';
    transferReceiptDetail.style.display = 'none';
    transferReceiptNumberWrap.style.display = 'block';

    syncMethodCard(cardMethod, useCard.checked);
    syncMethodCard(cashMethod, useCash.checked);
    syncMethodCard(transferMethod, useTransfer.checked);
    updateRegisterStatus();
}

function syncMethodCard(cardEl, isActive) {
    cardEl.classList.toggle('active', isActive);
}

function updateRegisterStatus() {
    const total = (useCard.checked ? getMoneyValue(cardAmount) : 0)
        + (useCash.checked ? getMoneyValue(cashAmount) : 0)
        + (useTransfer.checked ? getMoneyValue(transferAmount) : 0);

    enteredAmount.textContent = formatWon(total);
    expectedAmount.textContent = formatWon(selectedFinalTotal);

    if (total === 0) {
        registerStatus.textContent = '결제 방법을 선택하고 금액을 입력해주세요.';
        return;
    }
    if (total === selectedFinalTotal) {
        registerStatus.textContent = '최종 결제 금액과 일치합니다.';
        return;
    }
    if (total < selectedFinalTotal) {
        registerStatus.textContent = `최종 결제 금액보다 ${formatWon(selectedFinalTotal - total)} 부족합니다.`;
        return;
    }
    registerStatus.textContent = `최종 결제 금액보다 ${formatWon(total - selectedFinalTotal)} 초과되었습니다.`;
}

async function runSearch() {
    const keyword = searchInputEl.value.trim();
    listEl.classList.add('is-searching');
    window.clearTimeout(searchTimer);

    searchTimer = window.setTimeout(async () => {
        if (!keyword) {
            render([], true);
            listEl.classList.remove('is-searching');
            return;
        }

        try {
            const res = await fetch(`/app/pay/search?keyword=${encodeURIComponent(keyword)}`);
            const result = await res.json();
            console.log(result);
            render(result, false);
        } catch (e) {
            console.error('검색 실패', e);
            render([], false);
        } finally {
            listEl.classList.remove('is-searching');
        }
    }, 120);
}

render([], true);
paymentDate.value = getToday();

searchBtnEl.onclick = runSearch;
searchInputEl.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') runSearch();
});
searchInputEl.addEventListener('input', runSearch);
searchInputEl.addEventListener('focus', () => {
    if (searchInputEl.value.trim()) {
        headerEl.classList.add('compact');
        headerEl.classList.remove('centered');
    }
});

closeSheetBtn.addEventListener('click', closeAllSheets);
closeSheetBtnBottom.addEventListener('click', closeAllSheets);
backdropEl.addEventListener('click', closeAllSheets);
openRegisterBtn.addEventListener('click', openRegisterSheet);
closeRegisterBtn.addEventListener('click', closeAllSheets);
backToDetailBtn.addEventListener('click', openDetailSheet);

[useCard, useCash, useTransfer].forEach((checkbox) => {
    checkbox.addEventListener('change', () => {
        syncMethodCard(cardMethod, useCard.checked);
        syncMethodCard(cashMethod, useCash.checked);
        syncMethodCard(transferMethod, useTransfer.checked);
        updateRegisterStatus();
    });
});

cashTaxType.addEventListener('change', () => {
    cashReceiptDetail.style.display = cashTaxType.value === '과세' ? 'grid' : 'none';
    if (cashTaxType.value === '비과세') {
        cashReceiptNumber.value = '';
        cashIssueType.value = '개인';
    }
});

cashIssueType.addEventListener('change', () => {
    cashReceiptNumberWrap.style.display = cashIssueType.value === '자진발급' ? 'none' : 'block';
    if (cashIssueType.value === '자진발급') cashReceiptNumber.value = '';
});

transferTaxType.addEventListener('change', () => {
    transferReceiptDetail.style.display = transferTaxType.value === '과세' ? 'grid' : 'none';
    if (transferTaxType.value === '비과세') {
        transferReceiptNumber.value = '';
        transferIssueType.value = '개인';
    }
});

transferIssueType.addEventListener('change', () => {
    transferReceiptNumberWrap.style.display = transferIssueType.value === '자진발급' ? 'none' : 'block';
    if (transferIssueType.value === '자진발급') transferReceiptNumber.value = '';
});

[cardAmount, cashAmount, transferAmount].forEach((input) => {
    input.addEventListener('input', () => {
        formatMoneyInput(input);
        updateRegisterStatus();
    });
});

cardCompany.addEventListener('change', updateRegisterStatus);

saveRegisterBtn.addEventListener('click', () => {
    const total = (useCard.checked ? getMoneyValue(cardAmount) : 0)
        + (useCash.checked ? getMoneyValue(cashAmount) : 0)
        + (useTransfer.checked ? getMoneyValue(transferAmount) : 0);

    const message = `필요 결제 금액: ${formatWon(selectedFinalTotal)}
  입력 금액 합계: ${formatWon(total)}
  
  저장하시겠습니까?`;

    if (confirm(message)) {
        alert('오프라인 납부내역이 등록되었습니다.');
    }
});

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeAllSheets();
});