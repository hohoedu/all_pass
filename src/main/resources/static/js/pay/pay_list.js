// 날짜 선택
document.addEventListener("DOMContentLoaded", () => {
    const monthInput = document.querySelector(".hidden-date"); // <input type="month">
    const calendarBtn = document.querySelector(".calendar-open");
    const currentMonth = document.querySelector(".current-month");

    if (calendarBtn && monthInput) {
        calendarBtn.addEventListener("click", () => {
            monthInput.showPicker();
        });
    }

    if (monthInput) {
        monthInput.addEventListener("change", () => {
            const [year, month] = monthInput.value.split("-");
            const displayText = `${year}년 ${parseInt(month)}월`;

            if (currentMonth) {
                currentMonth.textContent = displayText;
            }
        });
    }

    const now = new Date();
    const yy = now.getFullYear();
    const mm = String(now.getMonth() + 1).padStart(2, "0");
    monthInput.value = `${yy}-${mm}`;
    if (currentMonth) {
        currentMonth.textContent = `${yy}년 ${parseInt(mm)}월`;
    }
});

document.addEventListener("DOMContentLoaded", () => {
    const btnAddPayment = document.querySelector("#btn-add-payment");
    const modal = document.querySelector(".add-payment-modal");
    const studentTableBody = document.querySelector("#student-list");

    if (btnAddPayment) {
        btnAddPayment.addEventListener("click", async () => {
            try {
                const now = new Date();
                const yy = now.getFullYear().toString();
                const mm = String(now.getMonth() + 1).padStart(2, "0");

                console.log("현재 연월:", yy, mm);

                document.querySelectorAll(".modal").forEach(m => m.style.display = "none");

                    const res = await fetch("/pay/list/students", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({yy, mm})
                });

                if (!res.ok) throw new Error("학생 조회 실패");
                const students = await res.json();

                // 4️⃣ 학생 테이블 렌더링
                if (studentTableBody) {
                    studentTableBody.innerHTML = "";

                    if (students.length === 0) {
                        studentTableBody.innerHTML = `<tr><td colspan="3" class="empty">해당 월의 결제 가능 학생이 없습니다.</td></tr>`;
                    } else {
                        students.forEach((s) => {
                            const tr = document.createElement("tr");
                            tr.dataset.billId = s.billId;
                            tr.dataset.studentId = s.studentId;
                            tr.innerHTML = `
                <td>${s.studentName}</td>
                <td>${s.grade || "-"}</td>
                <td>${s.teacherName || "-"}</td>
              `;
                            studentTableBody.appendChild(tr);
                        });
                    }
                }

                // 5️⃣ 모달 열기
                if (modal) modal.style.display = "block";

            } catch (err) {
                console.error("❌ 수기결제 모달 열기 오류:", err);
                alert("납부내역 추가 중 오류가 발생했습니다.");
            }
        });
    }
});


// 납부내역 추가 모달 오픈
document.addEventListener("DOMContentLoaded", () => {

    const btnAddPayment = document.querySelector('#btn-add-payment');
    if (btnAddPayment) {
        btnAddPayment.addEventListener('click', () => {
            console.log('버튼 클릭!!')
            openModal();
        });
    }

    function openModal() {
        document.querySelectorAll('.modal').forEach(modal => {
            modal.style.display = 'none';
        });

        const targetModal = document.querySelector(`.add-payment-modal`);
        console.log("targetModal" + targetModal);
        if (targetModal) {
            targetModal.style.display = 'block';
        }
    }
});

document.addEventListener("DOMContentLoaded", () => {
    const addBtn = document.querySelector(".charge-add");
    const tableBody = document.querySelector(".manual-payment-table tbody");

    if (!addBtn || !tableBody) return;

    addBtn.addEventListener("click", () => {
        const existingRow = tableBody.querySelector(".prepay-row");

        if (existingRow) {
            // 이미 선결제 행이 있으면 제거
            existingRow.remove();
            console.log("선결제 행 제거됨 ❌");
        } else {
            // 없으면 새로 추가
            console.log("선 결제 추가 버튼 클릭 ✅");

            const newRow = document.createElement("tr");
            newRow.classList.add("prepay-row");

            newRow.innerHTML = `
        <td class="label-cell">선결제</td>
        <td class="content-cell white-bg" colspan="2">
          <div class="prepay-input-group">
            <div class="prepay-period">
              <span>기간 선택 :</span>
              <label><input type="radio" name="prepay-period" value="2"> 2개월</label>
              <label><input type="radio" name="prepay-period" value="3"> 3개월</label>
              <label><input type="radio" name="prepay-period" value="4"> 4개월</label>
              <label><input type="radio" name="prepay-period" value="5"> 5개월</label>
              <label><input type="radio" name="prepay-period" value="6"> 6개월</label>
            </div>

            <div class="prepay-month-row">
              <label>시작 월</label>
              <input type="month" class="prepay-start-month">
            </div>

            <div class="prepay-note-row">
              <label>비고</label>
              <input type="text" class="prepay-note" placeholder="비고 입력">
            </div>
          </div>
        </td>
      `;

            tableBody.appendChild(newRow);
        }
    });
});



