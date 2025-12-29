/* global CryptoJS */

document.addEventListener('DOMContentLoaded', () => {
    const monthInput = document.querySelector('.hidden-picker');
    const monthBtn = document.querySelector('.calendar-open');
    const monthDisplay = document.querySelector('.current-month');
    const teacherSelect = document.getElementById('student-filter');
    const tbody = document.getElementById('student-tbody');

    bindSelectAllCheckbox();
    initMonthFromUrl();

    monthBtn.addEventListener('click', () => {
        if (typeof monthInput.showPicker === 'function') {
            monthInput.showPicker();
        } else {
            monthInput.click();
        }
    });

    monthInput.addEventListener('change', onMonthChange);
    teacherSelect.addEventListener('change', onTeacherChange);

    const searchBtn = document.querySelector('.explore');
    const searchInput = document.getElementById('search-name');

    if (searchBtn && searchInput) {
        searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                searchBtn.click();
            }
        });
        searchBtn.addEventListener('click', () => {
            const keyword = searchInput.value.trim();

            const rows = document.querySelectorAll('#student-tbody tr');

            rows.forEach(row => {
                // 데이터 없는 안내 row 제외
                if (!row.dataset.studentName) return;

                const studentName = row.dataset.studentName || '';

                if (keyword === '' || studentName.includes(keyword)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    }

    // 체크박스 전체 선택
    function bindSelectAllCheckbox() {
        const selectAll = document.querySelector('#pay-edu-select-all');
        const checkboxes = document.querySelectorAll('#student-tbody .row-checkbox');

        if (!selectAll) return;

        // 전체 선택 클릭 시
        selectAll.addEventListener('change', () => {
            checkboxes.forEach(chk => (chk.checked = selectAll.checked));
        });

        // 개별 체크박스 상태 변경 시 헤더 체크박스 상태 갱신
        checkboxes.forEach(chk => {
            chk.addEventListener('change', () => {
                const allChecked = [...checkboxes].every(c => c.checked);
                selectAll.checked = allChecked;
            });
        });
    }

    function initMonthFromUrl() {
        const url = new URL(window.location.href);
        const params = url.searchParams;

        let year = params.get('year');
        let month = params.get('month');

        if (!year || !month) {
            const now = new Date();
            year = now.getFullYear();
            month = String(now.getMonth() + 1).padStart(2, '0');
        } else {
            month = String(month).padStart(2, '0');
        }

        // input 값 세팅 (달력용)
        monthInput.value = `${year}-${month}`;

        // 화면 표시용 (span)
        monthDisplay.textContent = `${year}년 ${parseInt(month, 10)}월`;
    }

    // 월 변경
    function onMonthChange() {
        const [year, month] = monthInput.value.split('-');

        // 화면 표시 갱신
        monthDisplay.textContent = `${year}년 ${parseInt(month, 10)}월`;

        // URL 갱신
        const url = new URL(window.location.href);
        url.searchParams.set('year', year);
        url.searchParams.set('month', month);

        window.location.href = url.toString();
    }

    // 선생님 변경
    async function onTeacherChange() {
        const [year, month] = monthInput.value.split('-');
        const teacherCode = teacherSelect.value || null;

        await fetchStudents(year, month, teacherCode);
    }


    // 학생 조회
    async function fetchStudents(year, month, teacherCode) {
        const requestBody = {
            year: year,
            month: String(month).padStart(2, '0'),
            userCode: teacherCode
        };

        try {
            const response = await fetch("/pay/students", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify(requestBody),
            });

            if (!response.ok) throw new Error("서버 오류 발생");

            const result = await response.json();

            const students = result.response || result;
            console.log(students)
            renderStudentTable(students);

        } catch (error) {
            console.error("학생 목록 로드 실패:", error);
            alert("서버와의 통신에 실패했습니다.");
        }
    }

    let currentStudents = [];
    let currentSort = {
        key: null,
        order: 'asc'
    };

    /* ===============================
     * 학생 조회
     * =============================== */
    async function fetchStudents(year, month, teacherCode) {
        const requestBody = {
            year,
            month: String(month).padStart(2, '0'),
            userCode: teacherCode
        };

        try {
            const response = await fetch("/pay/students", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify(requestBody)
            });

            if (!response.ok) throw new Error("서버 오류");

            const result = await response.json();
            currentStudents = result.response || result;

            renderStudentTable(currentStudents);
            bindSortEvents();

        } catch (e) {
            console.error(e);
            alert("학생 목록을 불러오지 못했습니다.");
        }
    }

    /* ===============================
     * 정렬 이벤트 바인딩
     * =============================== */
    function bindSortEvents() {
        document.querySelectorAll('th[data-sort]').forEach(th => {
            th.onclick = () => handleSort(th.dataset.sort);
        });
    }

    /* ===============================
     * 정렬 처리
     * =============================== */
    function handleSort(key) {
        if (currentSort.key === key) {
            currentSort.order = currentSort.order === 'asc' ? 'desc' : 'asc';
        } else {
            currentSort.key = key;
            currentSort.order = 'asc';
        }

        const sorted = [...currentStudents].sort((a, b) => {
            let v1, v2;

            switch (key) {
                case 'name':
                    v1 = a.studentName || '';
                    v2 = b.studentName || '';
                    break;
                case 'eduFee':
                    v1 = Number(a.totalFee || 0);
                    v2 = Number(b.totalFee || 0);
                    break;
                case 'bookFee':
                    v1 = Number(a.totalMaterialFee || 0);
                    v2 = Number(b.totalMaterialFee || 0);
                    break;
                case 'unpaid':
                    v1 = Number(a.unpaidAmount || 0);
                    v2 = Number(b.unpaidAmount || 0);
                    break;
                case 'issued':
                    v1 = issueRank(a);
                    v2 = issueRank(b);
                    break;
                case 'payment':
                    v1 = paymentRank(a.totalStatus);
                    v2 = paymentRank(b.totalStatus);
                    break;
                default:
                    return 0;
            }

            if (v1 < v2) return currentSort.order === 'asc' ? -1 : 1;
            if (v1 > v2) return currentSort.order === 'asc' ? 1 : -1;
            return 0;
        });

        updateSortIcons();
        renderStudentTable(sorted);
    }

    /* ===============================
     * 상태 우선순위
     * =============================== */
    function issueRank(s) {
        if (!s.eduStatus && !s.materialStatus) return 0;
        if (s.eduStatus && s.materialStatus) return 3;
        return 2;
    }

    function paymentRank(status) {
        switch (status) {
            case 'approved':
                return 3;
            case 'partial':
                return 2;
            default:
                return 1;
        }
    }

    /* ===============================
     * 아이콘 갱신
     * =============================== */
    function updateSortIcons() {
        document.querySelectorAll('.svg-sort').forEach(i => {
            i.src = '/image/sort.svg';
        });

        const th = document.querySelector(`th[data-sort="${currentSort.key}"]`);
        if (!th) return;

        const icon = th.querySelector('.svg-sort');
        if (!icon) return;

        icon.src =
            currentSort.order === 'asc'
                ? '/image/sort_checked.svg'
                : '/image/sort_checked.svg';
    }

    // 랜더링
    function renderStudentTable(students) {
        const tbody = document.querySelector('#student-tbody');
        tbody.innerHTML = '';

        if (!students || students.length === 0) {
            tbody.innerHTML =
                `<tr><td colspan="10" style="text-align:center;">등록된 학생 데이터가 없습니다.</td></tr>`;
            return;
        }

        students.forEach((s, i) => {
            const tr = document.createElement('tr');

            const teacherText =
                `${s.hanTeacher ? s.hanTeacher + '(한)' : ''}`
                + (s.hanTeacher && s.bookTeacher ? ', ' : '')
                + `${s.bookTeacher ? s.bookTeacher + '(독)' : ''}`;

            tr.innerHTML = `
            <td class="checkbox-group">
                <input type="checkbox" class="row-checkbox" value="${s.studentId}">
            </td>
            <td>${i + 1}</td>
            <td>${s.studentName || '-'}</td>
            <td>${s.subject || '-'}</td>
            <td>${teacherText}</td>
            <td class="charge">${Number(s.totalFee || 0).toLocaleString()}</td>
            <td class="material-charge">${Number(s.totalMaterialFee || 0).toLocaleString()}</td>
            <td>${Number(s.unpaidAmount || 0).toLocaleString()}</td>
            <td>${renderIssueStatus(s)}</td>
            <td>${renderPayStatus(s)}</td>
        `;

            tbody.appendChild(tr);
        });

        bindSelectAllCheckbox();
    }

    function renderIssueStatus(s) {
        if (!s.eduStatus && !s.materialStatus) return `<span class="unissued">미발행</span>`;
        if (s.eduStatus && !s.materialStatus) return `<span class="edu-issued">발행(교육)</span>`;
        if (!s.eduStatus && s.materialStatus) return `<span class="material-issued">발행(교재)</span>`;
        return `<span class="issued">발행</span>`;
    }

    function renderPayStatus(s) {
        if (!s.totalStatus) return `<span class="pay-box">-</span>`;
        if (s.totalStatus === 'partial') return `<span class="pay-box pay-partial">부분 결제</span>`;
        if (s.totalStatus === 'approved') return `<span class="pay-box pay-done">결제완료</span>`;
        return `<span class="pay-box pay-late">미결제</span>`;
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

    const getLastDayOfPrevMonth = (year, month) => {
        // month는 1~12 기준
        return new Date(year, month - 1, 0);
    };

    const billingMonth = document.querySelector('.hidden-picker')?.value;
    let baseDate;

    if (billingMonth) {
        const [yy, mm] = billingMonth.split('-').map(Number);
        // ✅ 전월 기준 마지막 날
        baseDate = getLastDayOfPrevMonth(yy, mm);
    } else {
        const today = new Date();
        // billingMonth가 없을 경우 → 이번 달 기준 전월 마지막 날
        baseDate = getLastDayOfPrevMonth(
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
    if (tbody) {

        tbody.addEventListener('mouseenter', e => {
            const row = e.target.closest('tr');
            if (row) row.style.cursor = 'pointer';
        }, true);

        tbody.addEventListener('click', async (e) => {
            const row = e.target.closest('tr');
            if (!row || !tbody.contains(row)) return;

            const targetCell = e.target.closest('td');
            if (!targetCell) return;

            const index = Array.from(row.children).indexOf(targetCell);
            if (index === 0) return; // 체크박스 클릭 무시

            const studentId = row.dataset.studentId;
            if (!studentId) return;

            const url = new URL(window.location.href);
            const yy = url.searchParams.get('year');
            const mm = url.searchParams.get('month');

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
        });
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
                <td>${item.hanTeacher || ''}</td>
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
                    location.reload();
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
            priceInput.value = "";
        } else {
            priceDiv.classList.remove("disabled");
            priceDiv.classList.add("enabled");
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
    const payReissue = document.querySelector('#pay-reissue')


    // 청구서 발행 버튼
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

        const selectedMonth =
            document.querySelector('.hidden-date.hidden-picker').value;
        const [yy, mm] = selectedMonth.split('-');

        const expireDt = document.querySelector('.expire-input').value;

        // ✅ 선택된 학생 ID만 수집
        const studentIds = Array.from(checkedBoxes).map(
            box => box.closest('tr').dataset.studentId
        );

        const includeSibling =
            document.querySelector('input[name = "includeSibling"]').checked;

        try {
            // EDU
            if (eduChecked) {
                await sendBills({
                    studentIds,
                    type: 'edu',
                    message: `${mm}월 교육비 청구`,
                    expireDt,
                    yy,
                    mm,
                    includeSibling
                });
            }

            // MATERIAL
            if (bookChecked) {
                await sendBills({
                    studentIds,
                    type: 'material',
                    message: `${mm}월 교재비 청구`,
                    expireDt,
                    yy,
                    mm,
                    includeSibling
                });
            }

            alert('청구서 발행이 완료되었습니다.');
            window.location.reload();

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
            throw new Error(data.response || '청구 실패');
        }

        console.log('✓ 발행 결과:', data.response);
    }

// 결제 취소 버튼
    payCancel.addEventListener('click', async () => {

        const checkedBoxes = document.querySelectorAll(
            '#student-tbody input[type="checkbox"]:checked'
        );

        if (checkedBoxes.length === 0) {
            alert('결제 취소할 학생을 선택하세요.');
            return;
        }

        if (checkedBoxes.length !== 1) {
            alert('결제 취소는 한 명씩만 가능합니다.');
            return;
        }

        const row = checkedBoxes[0].closest('tr');

        const eduChecked = document.querySelector('input[name="eduFee"]').checked;
        const bookChecked = document.querySelector('input[name="bookFee"]').checked;

        const cancelType = eduChecked ? 'EDU_FEE' : 'BOOK_FEE';

        if (!eduChecked && !bookChecked) {
            alert('취소할 결제 종류를 선택하세요.');
            return;
        }

        if (cancelType === 'EDU_FEE' && row.dataset.eduStatus !== 'approved') {
            alert('교육비 결제 완료 건만 취소할 수 있습니다.');
            return;
        }

        if (cancelType === 'BOOK_FEE' && row.dataset.materialStatus !== 'approved') {
            alert('교재비 결제 완료 건만 취소할 수 있습니다.');
            return;
        }

        const cancelReason = prompt(
            `${row.dataset.studentName} 학생의 취소 사유를 입력해주세요.`
        );

        if (!cancelReason || cancelReason.trim() === '') {
            alert('취소 사유는 필수입니다.');
            return;
        }

        if (!confirm(
            `${row.dataset.studentName} 학생의 결제를 정말 취소하시겠습니까?\n\n` +
            `※ 이 작업은 되돌릴 수 없습니다.`
        )) {
            return;
        }

        const selectedMonth = document.querySelector('.hidden-date.hidden-picker').value;
        const [yy, mm] = selectedMonth.split('-');


        const body = {
            paymentKey: row.dataset.paymentKey,
            cancelType: cancelType,
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
            location.reload();

        } catch (err) {
            console.error('❌ 결제 취소 오류:', err);
            alert(err.message);
        }
    });

// ================================
// 🔥 3. 청구서 파기
// ================================
    payDestroy.addEventListener('click', async () => {

        const checked = document.querySelectorAll(
            '#student-tbody .row-checkbox:checked'
        );

        if (checked.length === 0) {
            alert('청구서를 파기할 학생을 선택하세요.');
            return;
        }

        if (checked.length > 1) {
            alert('청구서 파기는 한 명씩만 가능합니다.');
            return;
        }

        const eduChecked = document.querySelector('input[name="eduFee"]').checked;
        const bookChecked = document.querySelector('input[name="bookFee"]').checked;
        if (!eduChecked && !bookChecked) return alert('청구 종류를 선택하세요.');
        alert('educhecked = ' + eduChecked);
        const destroyType = eduChecked ? 'EDU_FEE' : 'BOOK_FEE';

        const row = checked[0].closest('tr');
        const billId = row.dataset.billId;
        const studentId = row.dataset.studentId;
        const paymentKey = row.dataset.paymentKey;
        const studentName = row.dataset.studentName;

        if (!studentId || !paymentKey) {
            alert('파기 정보를 확인할 수 없습니다.');
            return;
        }

        if (!confirm(`${studentName} 학생의 청구서를 파기하시겠습니까?`)) {
            return;
        }

        try {
            const res = await fetch('/pay/destroy/bill', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    billId,
                    studentId,
                    paymentKey,
                    destroyType
                })
            });

            const data = await res.json();

            if (!res.ok) {
                throw new Error(data.msg || '청구서 파기에 실패했습니다.');
            }
            alert(data.data || '청구서가 파기되었습니다.');
            location.reload();

        } catch (err) {
            console.error('❌ 청구서 파기 오류:', err);
            alert(err.message);
        }
    });

// ================================
// 🔥 4. 청구서 재발행(재전송)
// ================================
    payReissue.addEventListener('click', async () => {

        const billId = prompt("재발행할 bill_id 입력");

        if (!billId) return;

        try {
            const res = await fetch("/pay/reissue", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({billId})
            });

            const data = await res.json();

            if (data.success) alert("재발행 완료");
            else alert(`재발행 실패: ${data.msg}`);

        } catch (err) {
            console.error("❌ 재발행 오류:", err);
        }
    });

});