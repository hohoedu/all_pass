/* global CryptoJS */

document.addEventListener('DOMContentLoaded', () => {
    const monthInput = document.querySelector('.hidden-picker');
    const monthBtn = document.querySelector('.calendar-open');
    const monthDisplay = document.querySelector('.day-display');
    const teacherSelect = document.getElementById('student-filter');
    const tbody = document.getElementById('student-tbody');

    initCurrentMonth();
    bindSelectAllCheckbox();

    monthBtn.addEventListener('click', () => monthInput.showPicker());
    monthInput.addEventListener('change', onMonthChange);
    teacherSelect.addEventListener('change', onTeacherChange);

    function initCurrentMonth() {
        const now = new Date();
        const year = now.getFullYear();
        const month = now.getMonth() + 1;

        monthInput.value = `${year}-${String(month).padStart(2, '0')}`;
        monthDisplay.insertAdjacentText('afterbegin', `${year}년 ${month}월`);
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

    // 월 변경
    async function onMonthChange() {
        const date = new Date(monthInput.value);
        if (isNaN(date)) return;

        const year = date.getFullYear();
        const month = date.getMonth() + 1;

        monthDisplay.childNodes[0].textContent = `${year}년 ${month}월`;

        const teacherCode = teacherSelect.value || null;
        await fetchStudents(year, month, teacherCode);
    }

    // 선생님 변경
    async function onTeacherChange() {
        const date = new Date(monthInput.value);
        if (isNaN(date)) return;

        const year = date.getFullYear();
        const month = date.getMonth() + 1;
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

            tr.dataset.parentPhone = student.parentPhone || '';
            tr.dataset.studentId = student.studentId || '';
            tr.dataset.studentName = student.studentName || '';
            tr.dataset.totalPrice = student.totalPrice || 0;
            tr.dataset.totalFee = student.totalFee || 0;
            tr.dataset.totalMaterialFee = student.totalMaterialFee || 0;

            const formattedPrice = Number(student.totalPrice || 0).toLocaleString();

            const hanTeacherText = student.hanTeacher ? `${student.hanTeacher}(한)` : '';
            const bookTeacherText = student.bookTeacher ? `${student.bookTeacher}(독)` : '';
            const teacherSeparator = hanTeacherText && bookTeacherText ? ', ' : '';
            const teacherText = `${hanTeacherText}${teacherSeparator}${bookTeacherText}`;

            let statusHtml = '';
            const eduIssued = !student.eduStatus != null;
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
            } else if (student.totalStatus && !student.totalStatuss === 'approved') {
                payStatus = `<span class="pay-box">미결제</span>`;
            } else {
                payStatus = `<span class="pay-box">결제완료</span>`;
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
            <td>${student.unpaidAmount}</td>
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
    const now = new Date();
    const plus5 = new Date();
    plus5.setDate(now.getDate() + 5);

    const y = plus5.getFullYear();
    const m = plus5.getMonth() + 1;
    const d = plus5.getDate();

    expireInput.value = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
    expireDisplay.textContent = `${y}년 ${m}월 ${d}일`;

    const today = new Date();
    const minY = today.getFullYear();
    const minM = today.getMonth() + 1;
    const minD = today.getDate();
    expireInput.min = `${minY}-${String(minM).padStart(2, '0')}-${String(minD).padStart(2, '0')}`;

    expireBtn.addEventListener('click', () => {
        expireInput.showPicker();
    });

    expireInput.addEventListener('change', () => {
        const date = new Date(expireInput.value);
        if (isNaN(date)) return;
        expireDisplay.textContent = `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
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

            try {

                const response = await fetch('/pay/edu-personal', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({studentId})
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
        tbody.innerHTML = ''; // 초기화

        if (!data || data.length === 0) {
            tbody.innerHTML = `
            <tr>
                <td colspan="10" style="text-align:center;">등록된 데이터가 없습니다.</td>
            </tr>
        `;
            return;
        }

        data.forEach((item, index) => {
            const approvedDate = item.approvedDate ? item.approvedDate : '-';
            const amount = item.amount ? Number(item.amount).toLocaleString() : '0';

            const row = document.createElement('tr');
            row.innerHTML = `
            <td class="checkbox-group">
                <input type="checkbox" 
                       data-student-id="${item.studentId}" 
                       data-bill-id="${item.billId}">
            </td>
            <td>${index + 1}</td>
            <td>${item.classDate || ''}</td>
            <td>${item.studentName || ''}</td>
            <td>${item.subject || ''}</td>
            <td>${
                [
                    item.hanTeacher ? `${item.hanTeacher}(한)` : '',
                    item.bookTeacher ? `${item.bookTeacher}(북)` : ''
                ].filter(Boolean).join(', ')
            }</td>
            <td>${item.billType || '-'}</td>
            <td>${approvedDate.split(' ')[0]}</td>
            <td class="payment">${amount}</td>
            <td class="middle">
                <div class="state-box ${getStatusClass(item.status)}">${getStatus(item.status) || ''}</div>
            </td>
        `;
            tbody.appendChild(row);
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

function generateSendHash(billId, phone, price) {
    const input = `${billId},${phone},${price}`;
    return CryptoJS.SHA256(input).toString(CryptoJS.enc.Hex);
}

function generateCancelHash(billId, price) {
    const input = `${billId},${price}`;
    return CryptoJS.SHA256(input).toString(CryptoJS.enc.Hex);
}

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
        let hasError = false;   // ★ 실패 여부 저장

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
            return;   // 새로고침 금지
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
                const errorData = await res.json();    // ApiUtils.error 형태
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

        const checkedBoxes = document.querySelectorAll('#student-tbody input[type="checkbox"]:checked');
        if (checkedBoxes.length === 0) return alert('결제 취소할 학생을 선택하세요.');

        const eduChecked = document.querySelector('input[name="eduFee"]').checked;
        const bookChecked = document.querySelector('input[name="bookFee"]').checked;
        if (!eduChecked && !bookChecked) return alert('취소할 결제 종류를 선택하세요.');

        const selectedMonth = document.querySelector('.hidden-date.hidden-picker').value;
        const [yy, mm] = selectedMonth.split("-");

        const students = Array.from(checkedBoxes).map(box => ({
            studentId: box.closest('tr').dataset.studentId
        }));

        const body = {students, yy, mm, eduChecked, bookChecked};

        try {
            const res = await fetch("/pay/cancel", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(body)
            });

            const data = await res.json();

            alert(`취소 결과: 성공 ${data.successCount}명 / 실패 ${data.failCount}명`);
            // window.location.reload();

        } catch (err) {
            console.error("❌ 결제 취소 오류:", err);
            alert("결제 취소 중 오류가 발생했습니다.");
        }
    });

// ================================
// 🔥 3. 청구서 파기
// ================================
    payDestroy.addEventListener('click', async () => {

        const billId = prompt("파기할 bill_id 입력");

        if (!billId) return;

        try {
            const res = await fetch("/pay/destroy", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({billId})
            });

            const data = await res.json();

            if (data.success) alert("청구서가 파기되었습니다.");
            else alert(`파기 실패: ${data.msg}`);

        } catch (err) {
            console.error("❌ 파기 오류:", err);
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

})
;