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

    // 랜더링
    function renderStudentTable(students) {
        const tbody = document.querySelector('#student-tbody');
        tbody.innerHTML = '';
        tbody.addEventListener('click', (e) => {
            const row = e.target.closest('tr');
            if (!row || !tbody.contains(row)) return;

            if (e.target.classList.contains('row-checkbox')) {
                return;
            }

            row.classList.toggle('selected');
        });
        if (!students || students.length === 0) {
            tbody.innerHTML = `
                <tr><td colspan="9" style="text-align:center;">등록된 학생 데이터가 없습니다.</td></tr>
            `;
            return;
        }

        students.forEach((student, index) => {
            const tr = document.createElement('tr');
            tr.dataset.billId = student.billId || '';
            tr.dataset.parentPhone = student.parentPhone || '';
            tr.dataset.studentId = student.studentId || '';
            tr.dataset.studentName = student.studentName || '';
            tr.dataset.totalPrice = student.totalPrice || 0;
            tr.dataset.totalFee = student.totalFee || 0;
            tr.dataset.totalMaterialFee = student.totalMaterialFee || 0;
            tr.dataset.eduStatus = student.eduStatus;
            tr.dataset.materialStatus = student.materialStatus;
            tr.dataset.totalStatus = student.totalStatus;
            console.log('materialStatus = ' + student.materialStatus);
            const formattedPrice = Number(student.totalPrice || 0).toLocaleString();
            const formattedUnpaid = Number(student.unpaidAmount || 0).toLocaleString();

            const hanTeacherText = student.hanTeacher ? `${student.hanTeacher}(한)` : '';
            const bookTeacherText = student.bookTeacher ? `${student.bookTeacher}(독)` : '';
            const teacherSeparator = hanTeacherText && bookTeacherText ? ', ' : '';
            const teacherText = `${hanTeacherText}${teacherSeparator}${bookTeacherText}`;

            let statusHtml = '';
            const eduIssued = student.eduStatus != null;
            const materialIssued = student.materialStatus != null;

            if (!eduIssued && !materialIssued) {
                statusHtml = `<span class="unissued">미발행</span>`;
            } else if (eduIssued && !materialIssued) {
                statusHtml = `<span class="edu-issued">발행(교육)</span>`;
            } else if (!eduIssued && materialIssued) {
                statusHtml = `<span class="material-issued">발행(교재)</span>`;
            } else {
                statusHtml = `<span class="issued">발행</span>`;
            }

            let payStatus = '';

            if (!student.totalStatus) {
                payStatus = `<span class="pay-box">-</span>`;
            } else if (student.totalStatus !== 'approved') {
                payStatus = `<span class="pay-box pay-late">미결제</span>`;
            } else if (student.totalStatus === 'partiel') {
                payStatus = `<span class="pay-box pay-partial">부분 결제</span>`;
            } else if (student.totalStatus === 'approved') {
                payStatus = `<span class="pay-box pay-done">결제완료</span>`;
            }
            tr.innerHTML = `
            <td class="checkbox-group">
                <input type="checkbox" class="row-checkbox" value="${student.studentId}">
            </td>
            <td>${index + 1}</td>
            <td>${student.studentName || '-'}</td>
            <td>${student.subject || '-'}</td>
            <td>${teacherText}</td>
            <td class="charge">${formattedPrice}</td>
            <td>${formattedUnpaid}</td>
            <td>${statusHtml}</td>
            <td>${payStatus}</td>
        `;

            tbody.appendChild(tr);
        });
        bindSelectAllCheckbox();
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

    const getLastDayOfMonth = (year, month) => {
        return new Date(year, month, 0);
    };

    const billingMonth = document.querySelector('.hidden-picker')?.value;
    let baseDate = new Date();

    if (billingMonth) {
        const [yy, mm] = billingMonth.split('-').map(Number);
        baseDate = getLastDayOfMonth(yy, mm);
    } else {
        baseDate = getLastDayOfMonth(
            baseDate.getFullYear(),
            baseDate.getMonth() + 1
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

    function openModal(modalType) {
        document.querySelectorAll('.modal').forEach(modal => {
            modal.style.display = 'none';
        });

        const targetModal = document.querySelector(`.${modalType}-modal`);
        if (targetModal) {
            targetModal.style.display = 'block';
        }
    }

    // 전체 조회
    const btnTuition = document.querySelector('#btn-tuition');
    if (!btnTuition) return;
    btnTuition.addEventListener('click', async () => {

        const studentId = 'all';

        try {

            const response = await fetch('/pay/edu-personal', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({studentId})
            });

            if (!response.ok) {
                throw new Error('데이터 조회 실패');
            }

            const data = await response.json();

            fillModal(data.response, 'tuition');
            openModal('tuition');

        } catch (error) {
            console.error('❌ 청구 데이터 조회 오류:', error);
            alert('청구 내역을 불러오지 못했습니다.');
        }
    });


    // 개별 조회
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
            if (index === 0) return; // 첫 번째 셀(체크박스) 클릭은 무시

            const studentId = row.dataset.studentId;
            if (!studentId) return;

            const url = new URL(window.location.href);

            const yy = url.searchParams.get('year');
            const mm = url.searchParams.get('month');

            const body = {
                studentId: studentId,
                yy: yy,
                mm: mm
            };
            try {

                const response = await fetch('/pay/edu-personal', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(body)
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

    function fillModal(data, type) {
        const tbody = document.getElementById(`${type}-tbody`);
        tbody.innerHTML = '';

        if (!data || data.length === 0) {
            tbody.innerHTML = `
            <tr>
                <td colspan="10" style="text-align:center;">
                    등록된 데이터가 없습니다.
                </td>
            </tr>
        `;
            return;
        }

        /* =========================
           탭1 채우기
        ========================= */
        const firstItem = data[0];
        const modal = document.querySelector(`.${type}-modal`);
        if (modal) {

            // 제목
            modal.querySelector('.m-t-line .title').textContent =
                `${firstItem.studentName}의 수강료 상세 내역`;

            modal.querySelector('.pay-details h4 span').textContent =
                firstItem.studentName;

            const toNumber = (v) => Number(v || 0);

            const hanEduFee = toNumber(firstItem.hanFee);
            const bookEduFee = toNumber(firstItem.bookFee);

            // ✅ 교재비
            const hanMaterialFee = toNumber(firstItem.hanMaterialFee);
            console.log(hanMaterialFee);
            const bookMaterialFee = toNumber(firstItem.bookMaterialFee);

            const tableRows = modal.querySelectorAll('.pay-edu-table tbody tr');

            if (tableRows.length >= 5) {
                // 한자 교육비
                const hanEduInput = tableRows[0].querySelector('.edu-input');
                hanEduInput.value = hanEduFee.toLocaleString();
                hanEduInput.dataset.rawValue = hanEduFee;

                // 한자 교재비
                tableRows[1].querySelector('td:last-child').textContent =
                    hanMaterialFee.toLocaleString();

                // 독서 교육비 (조회값 없음 → 0)
                const bookEduInput = tableRows[2].querySelector('.edu-input');
                bookEduInput.value = bookEduFee.toLocaleString();
                bookEduInput.dataset.rawValue = bookEduFee;

                // 독서 교재비
                tableRows[3].querySelector('td:last-child').textContent =
                    bookMaterialFee.toLocaleString();

                // 합계
                const total =
                    hanEduFee +
                    bookEduFee +
                    hanMaterialFee +
                    bookMaterialFee;

                tableRows[4]
                    .querySelector('.edu-sum')
                    .textContent = total.toLocaleString();
            }

            setupInputListeners(modal);
            setupSaveButton(modal, firstItem.studentId);
        }

        data.forEach((item, index) => {
            const amount = Number(item.amount || 0).toLocaleString();
            const paidDate = item.paidDate ? item.paidDate.split(' ')[0] : '-';

            const row = document.createElement('tr');
            row.innerHTML = `
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
            tbody.appendChild(row);
        });
    }

    // 입력 필드 리스너 설정
    function setupInputListeners(modal) {
        const inputs = modal.querySelectorAll('.edu-input');

        inputs.forEach(input => {
            input.addEventListener('input', (e) => {
                // 숫자와 쉼표만 허용
                let value = e.target.value.replace(/[^0-9]/g, '');

                // 숫자를 저장
                e.target.dataset.rawValue = value;

                // 천단위 구분자 추가
                e.target.value = Number(value).toLocaleString();

                // 합계 업데이트
                updateTotal(modal);
            });
        });
    }

    // 합계 계산 함수
    function updateTotal(modal) {
        const tableRows = modal.querySelectorAll('.pay-edu-table tbody tr');

        let total = 0;

        // 한자 교육비
        const hanFeeInput = tableRows[0]?.querySelector('.edu-input');
        if (hanFeeInput) {
            total += Number(hanFeeInput.dataset.rawValue || 0);
        }

        // 한자 교재비
        const hanMaterialText = tableRows[1]?.querySelector('td:last-child').textContent;
        total += Number(hanMaterialText.replace(/,/g, '') || 0);

        // 독서 교육비
        const bookFeeInput = tableRows[2]?.querySelector('.edu-input');
        if (bookFeeInput) {
            total += Number(bookFeeInput.dataset.rawValue || 0);
        }

        // 독서 교재비
        const bookMaterialText = tableRows[3]?.querySelector('td:last-child').textContent;
        total += Number(bookMaterialText.replace(/,/g, '') || 0);

        // 합계 표시
        const sumCell = tableRows[4]?.querySelector('.edu-sum');
        if (sumCell) {
            sumCell.textContent = total.toLocaleString();
        }
    }

    // 저장 버튼 설정
    function setupSaveButton(modal, studentId) {
        const saveBtn = modal.querySelector('.save-btn');
        if (!saveBtn) return;

        // 기존 이벤트 제거
        const newSaveBtn = saveBtn.cloneNode(true);
        saveBtn.parentNode.replaceChild(newSaveBtn, saveBtn);

        newSaveBtn.addEventListener('click', async () => {
            const tableRows = modal.querySelectorAll('.pay-edu-table tbody tr');

            const hanFee = Number(
                tableRows[0]?.querySelector('.edu-input')?.dataset.rawValue || 0
            );
            const bookFee = Number(
                tableRows[2]?.querySelector('.edu-input')?.dataset.rawValue || 0
            );

            const url = new URL(window.location.href);
            const yy = url.searchParams.get('year');
            const mm = url.searchParams.get('month');

            if (!yy || !mm) {
                alert('년월 정보가 없습니다.');
                return;
            }

            if (!confirm('교육비를 저장하시겠습니까?')) return;

            try {
                const response = await fetch('/pay/update-fee', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({
                        studentId: studentId,
                        yy: yy,
                        mm: mm,
                        hanEduFee: hanFee,
                        bookEduFee: bookFee
                    })
                });

                if (!response.ok) throw new Error('저장 실패');

                const result = await response.json();

                if (result.success) {
                    alert('교육비가 저장되었습니다.');
                    location.reload();
                } else {
                    alert(result.message || '저장에 실패했습니다.');
                }

            } catch (error) {
                console.error('❌ 교육비 저장 오류:', error);
                alert('서버와의 통신에 실패했습니다.');
            }
        });
    }

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
                return 'pending'
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
        const checkedBoxes = document.querySelectorAll('#student-tbody input[type="checkbox"]:checked');
        if (checkedBoxes.length === 0) return alert('학생을 선택하세요.');

        const eduChecked = document.querySelector('input[name="eduFee"]').checked;
        const bookChecked = document.querySelector('input[name="bookFee"]').checked;
        if (!eduChecked && !bookChecked) return alert('청구 종류를 선택하세요.');

        const selectedMonth = document.querySelector('.hidden-date.hidden-picker').value;
        const [yy, mm] = selectedMonth.split("-");
        const expireDate = document.querySelector('.expire-input').value;

        const now = new Date();
        let issuedCount = 0;
        let hasError = false;

        for (const [index, box] of checkedBoxes.entries()) {
            const row = box.closest('tr');
            const issuedCell = row.querySelector('.unissued, .issued');

            if (issuedCell?.classList.contains('issued')) {
                alert(`⚠️ ${row.dataset.studentName} 학생의 ${mm}월 청구서는 이미 발행되었습니다.`);
                continue;
            }

            const student = {
                id: row.dataset.studentId,
                name: row.dataset.studentName,
                phone: row.dataset.parentPhone,
                hanFee: Number(row.dataset.hanFee),
                bookFee: Number(row.dataset.bookFee),
                hanMaterial: Number(row.dataset.hanMaterial),
                bookMaterial: Number(row.dataset.bookMaterial),
                totalFee: Number(row.dataset.totalFee),
                totalMaterialFee: Number(row.dataset.totalMaterialFee),
                paymentKey: row.dataset.paymentKey,
            };

            const indexStr = String(index).padStart(2, "0");

            // EDU
            if (eduChecked && student.totalFee > 0) {
                const ok = await requestSendBill("edu", student, student.totalFee,
                    expireDate, now, yy, mm, indexStr);

                if (ok) issuedCount++;
                else hasError = true;
            }

            // MATERIAL
            if (bookChecked && student.totalMaterialFee > 0) {
                const ok = await requestSendBill("material", student, student.totalMaterialFee,
                    expireDate, now, yy, mm, indexStr);

                if (ok) issuedCount++;
                else hasError = true;
            }
        }
        if (hasError) {
            alert(`일부 청구서 발행 실패가 있습니다.\n성공: ${issuedCount}건`);
            return;
        }

        alert(`총 ${issuedCount}개의 청구서가 성공적으로 발행되었습니다.`);
        window.location.reload();
    });

    async function requestSendBill(type, student, price, expireDt, now, yy, mm, index) {

        const body = {
            type,
            studentId: student.id,
            studentName: student.name,
            phone: student.phone,
            price: price,
            message: `${student.name} ${type === 'edu' ? '교육비' : '교재비'} 청구`,
            expireDt,
            index,
            yy,
            mm
        };

        try {
            const res = await fetch("/pay/send", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(body)
            });
            if (!res.ok) {
                const errorData = await res.json();
                alert(`❌ ${student.name} ${type.toUpperCase()} 청구 실패: ${errorData.response || '서버 오류'}`);
                return;
            }

            const data = await res.json();


            if (!data.success) {

                alert(`❌ ${student.name} ${type.toUpperCase()} 청구 실패: ${data.response}`);
                return;
            }
            console.log("✓ 발행 완료:", data.response);
            return true;

        } catch (err) {
            console.error("❌ sendBill 오류:", err);
            alert("서버 오류로 청구서를 발행할 수 없습니다.");
            return;
        }
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

        if (!eduChecked && !bookChecked) {
            alert('취소할 결제 종류를 선택하세요.');
            return;
        }

        if (eduChecked) {
            if (row.dataset.eduStatus !== 'approved') {
                alert('교육비 결제 완료 건만 취소할 수 있습니다.');
                return;
            }
        }

        if (bookChecked) {
            if (row.dataset.materialStatus !== 'approved') {
                alert('교재비 결제 완료 건만 취소할 수 있습니다.');
                return;
            }
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

        // ✅ cancelTypes 구성
        // const cancelTypes = [];
        // if (eduChecked) cancelTypes.push('EDU');
        // if (bookChecked) cancelTypes.push('BOOK');

        const body = {
            studentId: row.dataset.studentId,
            paymentKey: row.dataset.paymentKey,
            billId: row.dataset.billId,
            yy,
            mm,
            cancelEdu: eduChecked,
            cancelBook: bookChecked,
            cancelReason: cancelReason.trim()
        };

        try {
            const res = await fetch('/pay/cancel', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(body)
            });

            const data = await res.json();

            if (data.success) {
                alert('결제가 취소되었습니다.');
                location.reload();
            } else {
                alert(data.msg || '결제 취소에 실패했습니다.');
            }

        } catch (err) {
            console.error('❌ 결제 취소 오류:', err);
            alert('결제 취소 중 오류가 발생했습니다.');
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

        const row = checked[0].closest('tr');
        let price = "";
        if (eduChecked) {
            price = row.dataset.totalFee;
        }
        if (bookChecked) {
            price = row.dataset.totalMaterialFee;
        }
        const billId = row.dataset.billId;
        const studentId = row.dataset.studentId;
        const paymentKey = row.dataset.paymentKey;

        if (!billId || !price) {
            alert('파기 가능한 청구서가 없습니다.');
            return;
        }

        if (!confirm(`${row.dataset.studentName} 학생의 청구서를 파기하시겠습니까?`)) {
            return;
        }

        try {
            const res = await fetch('/pay/destroy/bill', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    billId,
                    price: Number(price),
                    studentId,
                    paymentKey
                })
            });

            const data = await res.json();

            if (data.success) {
                alert('청구서가 파기되었습니다.');
                location.reload();
            } else {
                alert(data.msg || '청구서 파기에 실패했습니다.');
            }

        } catch (err) {
            console.error('❌ 청구서 파기 오류:', err);
            alert('서버 오류로 청구서를 파기할 수 없습니다.');
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