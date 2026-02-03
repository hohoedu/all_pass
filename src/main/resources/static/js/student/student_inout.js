// ============================학생관리 전입/전출============================ //
const transferSortState = {
    key: null,      // name | grade | subject | moveAt | teacher
    order: 'asc'    // asc | desc
};

document.addEventListener('DOMContentLoaded', () => {
// 이름
    document.getElementById("transfer-sort-3")
        .addEventListener("click", () => sortTransferTable("name"));

    // 학년
    document.getElementById("transfer-sort-4")
        .addEventListener("click", () => sortTransferTable("grade"));

    // 수강과목
    document.getElementById("transfer-sort-5")
        .addEventListener("click", () => sortTransferTable("subject"));

    // 전입일
    document.getElementById("transfer-sort-6")
        .addEventListener("click", () => sortTransferTable("moveAt"));

    // 담임
    document.getElementById("transfer-sort-7")
        .addEventListener("click", () => sortTransferTable("teacher"));

    document.getElementById("search-btn")
        .addEventListener("click", filterStudentTable);

    document.getElementById("search-name")
        .addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                filterStudentTable();
            }
        });

    document.addEventListener("change", (e) => {
        if (e.target.id === "check-all") {
            const checked = e.target.checked;
            document.querySelectorAll(".row-checkbox").forEach(cb => {
                cb.checked = checked;
            });
        }
    });

// 개별 체크박스 해제 시 전체 선택 해제
    document.addEventListener("change", (e) => {
        if (e.target.classList.contains("row-checkbox")) {
            const all = document.querySelectorAll(".row-checkbox");
            const checked = document.querySelectorAll(".row-checkbox:checked");
            document.getElementById("check-all").checked = (all.length === checked.length);
        }
    });

// ====== 선생님 별 필터링 ====== //
    document.getElementById("transfer-teacher-filter").addEventListener("change", async (e) => {

        const userCode = e.target.value;

        try {
            const response = await fetch("/student/api/transfer/list", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    userCode: userCode
                })
            });

            if (!response.ok) {
                throw new Error("서버 요청 실패");
            }

            const data = await response.json();

            renderStudentTable(data.response);

        } catch (error) {
            console.error(error);
            alert("목록을 불러오는 중 오류가 발생했습니다.");
        }
    });


});

function renderStudentTable(students) {
    const tbody = document.getElementById("student-tbody");
    tbody.innerHTML = "";

    if (!students || students.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align:center;">조회된 학생이 없습니다.</td>
            </tr>
        `;
        return;
    }

    students.forEach((s, index) => {

        const subject =
            s.hanClass && s.bookClass
                ? `${s.hanClass}, ${s.bookClass}`
                : (s.hanClass ?? s.bookClass ?? '');

        const teacher =
            s.hanTeacher && s.bookTeacher
                ? `${s.hanTeacher}(한자), ${s.bookTeacher}(독서)`
                : (s.hanTeacher
                    ? `${s.hanTeacher}(한자)`
                    : (s.bookTeacher ? `${s.bookTeacher}(독서)` : ''));

        const tr = document.createElement("tr");
        tr.dataset.id = s.studentId;
        tr.dataset.name = s.studentName;
        tr.dataset.phone = (s.parentPhone ?? '').replace(/[^0-9]/g, '');
        tr.onclick = function () {
            openTransferModal(this);
        };

        tr.innerHTML = `
            <td class="checkbox-group">
                <input type="checkbox"
                       class="row-checkbox"
                       value="${s.studentId}"
                       name="studentNoList"
                       form="inout-form"
                       onclick="event.stopPropagation();">
            </td>
            <td>${index + 1}</td>
            <td>${s.studentName ?? ''}</td>
            <td>${s.gradeName ?? ''}</td>
            <td>${subject}</td>
            <td>${s.moveAt ?? '-'}</td>
            <td>${teacher}</td>
        `;

        tbody.appendChild(tr);
    });

    resetCheckAll();

    // 🔥 정렬 유지
    if (transferSortState.key) {
        sortTransferTable(transferSortState.key);
    }
    document.getElementById("search-name").value = "";
    document.getElementById("stu-name").value = "name";
}

function resetCheckAll() {
    const checkAll = document.getElementById("check-all");
    if (checkAll) {
        checkAll.checked = false;
        checkAll.disabled = document.querySelectorAll(".row-checkbox").length === 0;
    }
}

function sortTransferTable(key) {

    const tbody = document.getElementById("student-tbody");
    const rows = Array.from(tbody.querySelectorAll("tr"));

    // 데이터 없음 row 제외
    if (rows.length === 1 && rows[0].children.length === 1) return;

    // 같은 컬럼 클릭 시 방향 토글
    if (transferSortState.key === key) {
        transferSortState.order =
            transferSortState.order === "asc" ? "desc" : "asc";
    } else {
        transferSortState.key = key;
        transferSortState.order = "asc";
    }

    rows.sort((a, b) => {

        const getValue = (row) => {
            switch (key) {
                case "name":
                    return row.children[2].innerText.trim();
                case "grade":
                    return row.children[3].innerText.trim();
                case "subject":
                    return row.children[4].innerText.trim();
                case "moveAt":
                    return row.children[5].innerText.trim();
                case "teacher":
                    return row.children[6].innerText.trim();
                default:
                    return "";
            }
        };

        let v1 = getValue(a);
        let v2 = getValue(b);

        // 날짜 컬럼은 Date 비교
        if (key === "moveAt") {
            v1 = v1 === '-' ? 0 : new Date(v1).getTime();
            v2 = v2 === '-' ? 0 : new Date(v2).getTime();
        }

        if (v1 < v2) return transferSortState.order === "asc" ? -1 : 1;
        if (v1 > v2) return transferSortState.order === "asc" ? 1 : -1;
        return 0;
    });

    // No 컬럼 재정렬 + tbody 반영
    rows.forEach((row, idx) => {
        row.children[1].innerText = idx + 1;
        tbody.appendChild(row);
    });

    updateSortIcon(key);
}

function updateSortIcon(activeKey) {

    const map = {
        name: "transfer-sort-3",
        grade: "transfer-sort-4",
        subject: "transfer-sort-5",
        moveAt: "transfer-sort-6",
        teacher: "transfer-sort-7"
    };

    Object.entries(map).forEach(([key, id]) => {
        const img = document.getElementById(id);
        if (!img) return;

        img.src =
            key === activeKey
                ? "/image/sort_checked.svg"
                : "/image/sort.svg";
    });
}

function filterStudentTable() {

    const type = document.getElementById("stu-name").value; // name | phone
    let keyword = document.getElementById("search-name").value.trim();

    const rows = document.querySelectorAll("#student-tbody tr");

    // 빈 테이블 보호
    if (rows.length === 1 && rows[0].children.length === 1) return;

    if (!keyword) {
        rows.forEach(row => row.style.display = "");
        resetRowNumber();
        toggleEmptyMessage(rows.length);
        return;
    }

    let visibleCount = 0;

    rows.forEach(row => {

        let match = false;

        if (type === "name") {
            const name = row.dataset.name ?? '';
            match = name.includes(keyword);

        } else if (type === "phone") {

            // 🔥 전화번호 검색 핵심
            const phone = row.dataset.phone ?? '';
            const search = keyword.replace(/[^0-9]/g, '');

            // 숫자 4자리 이상일 때만 검색 허용
            if (search.length >= 4) {
                match = phone.includes(search);
            }
        }

        if (match) {
            row.style.display = "";
            visibleCount++;
        } else {
            row.style.display = "none";
        }
    });

    resetRowNumber();
    toggleEmptyMessage(visibleCount);
}

function resetRowNumber() {
    const rows = document.querySelectorAll("#student-tbody tr");
    let idx = 1;

    rows.forEach(row => {
        if (row.style.display !== "none") {
            row.children[1].innerText = idx++;
        }
    });
}

function toggleEmptyMessage(visibleCount) {

    const tbody = document.getElementById("student-tbody");
    let emptyRow = document.getElementById("empty-row");

    if (visibleCount === 0) {
        if (!emptyRow) {
            emptyRow = document.createElement("tr");
            emptyRow.id = "empty-row";
            emptyRow.innerHTML = `
                <td colspan="7" style="text-align:center;">
                    조회된 학생이 없습니다.
                </td>
            `;
            tbody.appendChild(emptyRow);
        }
    } else {
        if (emptyRow) emptyRow.remove();
    }
}

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
                throw new Error(result.msg || "처리 중 오류가 발생했습니다.");
            }


            if (response)
                alert("전입/전출 처리가 등록되었습니다.");
            location.reload();

        } catch (err) {
            console.error(err);
            alert(err.message);
        }
    });
});


