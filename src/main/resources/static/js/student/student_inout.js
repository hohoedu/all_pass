// ============================학생관리 전입/전출============================ //

document.addEventListener('DOMContentLoaded', () => {
    initHeaderSort('transfer', '#student-tbody');
});

// ====== 선생님 별 필터링 ====== //
document.addEventListener("DOMContentLoaded", () => {
    const teacherFilter = document.getElementById("transfer-teacher-filter");
    const subjectFilter = document.getElementById("transfer-subject-filter");

    if (teacherFilter)
        teacherFilter.addEventListener("change", function () {
            const userCode = this.value;
            fetch(`/student/api/label?userCode=${encodeURIComponent(userCode)}`)
                .then(res => {
                    return res.json();
                })
                .then(data => {
                    subjectFilter.innerHTML = `<option value="all">전체</option>`;
                    data.response.forEach(item => {
                        subjectFilter.innerHTML += `<option value="${item.timeTableCode}">${item.classLabel}</option>`;
                    });
                })
                .catch(err => {
                    subjectFilter.innerHTML = `<option value="all">전체</option>`;
                });
        })
})

// ====== 모달 오픈 ====== //
function openTransferModal(row) {
    const studentId = row.getAttribute("data-id");


    showTransferModal();
}

function showTransferModal() {
    const modal = document.getElementById("transfer-modal");
    modal.style.display = "block";
    document.body.style.overflow = "hidden";

    modal.querySelector(".btn-close").addEventListener("click", closeTransferModal);
}

// 전입 전출 날짜 선택
document.addEventListener("DOMContentLoaded", () => {
    const dateInput = document.querySelector(".hidden-inout-date");
    const calendarBtn = document.querySelector(".calendar-open");
    const displayDate = document.querySelector(".display-date");

    calendarBtn.addEventListener("click", () => {
        dateInput.showPicker();
    });
})

// 전입 전출 저장
document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("inout-form");
    const submitBtn = form.querySelector(".save-btn");

    submitBtn.addEventListener("click", async (e) => {
        e.preventDefault();
        alert("현재 전입/전출은 작업 중입니다!");
        return;

        const selectedStudents = Array.from(
            document.querySelectorAll('.row-checkbox:checked')
        ).map(cb => cb.value);

        if (selectedStudents.length === 0) {
            alert("학생을 최소 한 명 이상 선택하세요.");
            return;
        }

        const selectedHan = document.getElementById("inout-han").checked
            ? document.getElementById("inout-han").value
            : null;

        const selectedBook = document.getElementById("inout-book").checked
            ? document.getElementById("inout-book").value
            : null;

        if (!selectedHan && !selectedBook) {
            alert("전출 과목을 선택하세요.");
            return;
        }

        const moveAt = form.querySelector('input[name="moveAt"]').value;
        const teacherCode = form.querySelector('#teacher-filter').value;
        const reason = form.querySelector('textarea[name="transferReason"]').value.trim();

        if (!moveAt) {
            alert("전입일을 선택하세요.");
            return;
        }

        if (!reason) {
            alert("사유를 입력해주세요.");
            return;
        }

        const requestBody = {
            students: selectedStudents,
            selectedHan: selectedHan,
            selectedBook: selectedBook,
            userCode: teacherCode,
            moveAt: moveAt,
            transferReason: reason
        };

        try {
            const response = await fetch("/student/inout", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(requestBody)
            });

            const result = await response.json();

            if (!response.ok || result.success === false) {

                throw new Error(result.error.message || "처리 중 오류가 발생했습니다.");
            }

            alert("전입/전출 처리가 완료되었습니다.");
            location.reload();

        } catch (err) {
            console.error(err);
            alert(err.message);
        }
    });
});


