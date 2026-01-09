// ============================ 학생관리 메인 ============================ //
let currentStudentId = null;

// ====== 선생님 별 학생 필터링 ====== //
document.addEventListener("DOMContentLoaded", () => {
    const teacherFilter = document.getElementById("main-teacher-filter");
    const subjectFilter = document.getElementById("main-subject-filter");
    const tbody = document.getElementById("main-student-tbody");
    if (teacherFilter) {
        teacherFilter.addEventListener("change", function () {
            const userCode = this.value;
            console.log(userCode);
            fetch(`/student/api/label?userCode=${encodeURIComponent(userCode)}`)
                .then(res => {
                    return res.json();
                })
                .then(data => {
                    subjectFilter.innerHTML = `<option value="all">전체</option>`;
                    data.response.forEach(item => {
                        subjectFilter.innerHTML += `<option value="${item.timeTableKey}">${item.classLabel}</option>`;
                    });
                })
                .catch(err => {
                    subjectFilter.innerHTML = `<option value="all">전체</option>`;
                });

            fetch(`/student/api/students?userCode=${encodeURIComponent(userCode)}`)
                .then(res => {
                    return res.json();

                })
                .then(data => {
                    renderStudents(tbody, data.response);
                });
        });
    }

    if (subjectFilter) {
        subjectFilter.addEventListener("change", function () {
            const timeTableKey = this.value;
            console.log("subject 변경:", timeTableKey);

            const userCodeSelect = document.getElementById("userFilter"); // select#userFilter 등
            const userCode = userCodeSelect ? userCodeSelect.value : "all";

            fetch(`/student/api/students?timeTableKey=${encodeURIComponent(timeTableKey)}&userCode=${encodeURIComponent(userCode)}`)
                .then(res => {
                    return res.json();
                })
                .then(data => {
                    console.log('data = ', data);
                    renderStudents(tbody, data.response);
                })
                .catch(err => {
                    subjectFilter.innerHTML = `<option value="all">전체</option>`;
                });
        });
    }
});

// ============ 검색 ============ //
document.addEventListener("DOMContentLoaded", () => {
    const searchBtn = document.getElementById("search-main-student");
    const searchInput = document.getElementById("search-name");
    const filterSelect = document.getElementById("stu-name");
    const tbody = document.getElementById("main-student-tbody");

    if (!searchBtn || !searchInput || !tbody) return;

    const getRows = () =>
        Array.from(tbody.querySelectorAll("tr"));

    function doSearch() {
        const keyword = searchInput.value.trim().toLowerCase();
        const filterType = filterSelect?.value || "all";
        const rows = getRows();

        if (!keyword) {
            rows.forEach(tr => (tr.style.display = ""));
            return;
        }

        let matchedCount = 0;

        rows.forEach(tr => {
            const tds = tr.querySelectorAll("td");
            if (tds.length === 0) return;

            let textToSearch = "";

            switch (filterType) {
                case "all": // 이름
                    textToSearch = tds[1]?.innerText.toLowerCase() || "";
                    break;
                default:
                    textToSearch = tr.innerText.toLowerCase();
            }

            if (textToSearch.includes(keyword)) {
                tr.style.display = "";
                matchedCount++;
            } else {
                tr.style.display = "none";
            }
        });

        // 🔹 검색 결과가 하나도 없으면 전체 복구
        if (matchedCount === 0) {
            rows.forEach(tr => (tr.style.display = ""));
        }
    }

    // 버튼 클릭
    searchBtn.addEventListener("click", doSearch);

    // 🔥 Enter 키 검색
    searchInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            doSearch();
        }
    });

    // 🔹 입력값 지우면 즉시 전체 복구
    searchInput.addEventListener("input", () => {
        if (searchInput.value.trim() === "") {
            getRows().forEach(tr => (tr.style.display = ""));
        }
    });
});

// ====== 학생 리스트 변경 ====== //
function renderStudents(tbody, students = []) {
    tbody.innerHTML = "";

    students.forEach((s, i) => {
        const tr = document.createElement("tr");
        tr.setAttribute("data-id", s.studentId);
        tr.setAttribute("onclick", "openModal(this)");
        const appIcon = s.hasApp === "Y"
            ? `<div class="tooltip-container">
                 <img src="/image/in_app.png" alt="앱 연결" class="app-icon" 
                 style="width: 25px; height: 25px;">
                 <div class="tooltip-text">앱 사용</div>
               </div>`
            : `<div class="tooltip-container">
                 <img src="/image/no_app.png" alt="앱 연결" class="app-icon" 
                 style="width: 25px; height: 25px;">
                 <div class="tooltip-text">앱 미사용</div>
               </div>`;

        tr.innerHTML = `
      <td>${i + 1}</td>
      <td>${s.studentName ?? ""}</td>
      <td>${s.statusName ?? ""}</td>
      <td>${s.entryDate ?? ""}</td>
      <td>${[s.hanClass, s.bookClass].filter(Boolean).join(", ")}</td>
      <td>${s.gradeName ?? ""}</td>
      <td>${s.school ?? ""}</td>
      <td>
        <div class="tooltip-container">
        <img src="/image/link.png" alt="link" class="link">
            <div class="tooltip-text">${s.isSibling === "Y" ? "형제 있음" : "형제 없음"}</div>
        </div>
      </td>
      <td>${appIcon}</td>
    `;

        tbody.appendChild(tr);
    });

}


// ============================================= 학생관리 모달 ============================================= //
function formatPhone(phone) {
    if (!phone || phone.length !== 11) return "";
    const first = phone.substring(0, 3);
    const middle = phone.substring(3, 7);
    const last = phone.substring(7, 11);
    return `${first}-${middle}-${last}`;
}

function formatBirthDisplay(birth) {
    if (!birth) return "";

    const [year, month, day] = birth.split("-");

    if (!year || !month || !day) return "";

    return `${year}년 ${month}월 ${day}일`;
}

// date input용: YYMMDD -> "YYYY-MM-DD"
function formatBirthInput(birth) {
    if (!birth || birth.length !== 6) return "";

    const yy = birth.substring(0, 2);
    const mm = birth.substring(2, 4);
    const dd = birth.substring(4, 6);

    const currentYY = new Date().getFullYear() % 100;
    const fullYear = yy > currentYY ? `19${yy}` : `20${yy}`;

    return `${fullYear}-${mm}-${dd}`;
}

function unformatBirth(dateString) {
    if (!dateString) return "";
    const p = dateString.split("-");
    if (p.length !== 3) return "";
    return p[0].substring(2) + p[1] + p[2];
}

function unformatPhone(phone) {
    return phone ? phone.replace(/-/g, "") : "";
}

function getValue(selector) {
    const el = document.querySelector(selector);
    return el ? el.value : "";
}

document.addEventListener("DOMContentLoaded", () => {
    const tbody = document.querySelector("#main-student-tbody");
    if (tbody) {
        tbody.addEventListener("click", async (e) => {
            const row = e.target.closest("tr");
            if (!row) return;

            const studentId = row.dataset.id;
            if (!studentId) return;

            await loadStudentDetail(studentId);
        });
    }

    const updateBtn = document.getElementById("update-btn");
    if (updateBtn) {
        updateBtn.addEventListener("click", updateStudentInfo);
    }
});

async function loadStudentDetail(studentId) {
    try {
        const res = await fetch(`/student/${studentId}`);

        if (!res.ok) {
            throw new Error("학생 정보 조회 실패");
        }

        currentStudentId = studentId;

        const data = await res.json();
        console.log("학생 상세:", data.response);

        renderStudentModal(data.response);
        openStudentModal();

    } catch (err) {
        console.error(err);
        alert("학생 정보를 가져오는 중 오류가 발생했습니다.");
    }
}

function openStudentModal() {
    const modal = document.querySelector(".student-modal");
    if (!modal) return;

    modal.style.display = "block";
    document.body.classList.add("modal-open");

    const closeBtn = modal.querySelector(".btn-close");
    if (closeBtn) {
        closeBtn.onclick = () => {
            modal.style.display = "none";
            document.body.classList.remove("modal-open");
        };
    }
}

function renderStudentModal(data) {
    if (!data) {
        console.error("studentInfo 없음:", data);
        return;
    }

    const info = data.studentInfo;
    const payment = data.studentPayment ?? {};
    const grade = data.gradeCodes;

    const setValue = (selector, value) => {
        document.querySelectorAll(selector).forEach(el => {
            if (['INPUT', 'TEXTAREA', 'SELECT'].includes(el.tagName)) {
                el.value = value ?? '';
            } else {
                el.innerText = value ?? '';
            }
        });
    };

    if (typeof updateStatusButton === "function") {
        updateStatusButton(info.statusKey);
    }

    renderGender(info.genderKey);
    renderParent(info.parentRelation);
    renderGradeDropdown(grade, info.gradeKey);

    // ---------------- TAB1: 전체 ----------------
    setValue("#tab1 .s_name", info.studentName);
    setValue("#tab1 .s_subjects", [info.hanClass, info.bookClass].filter(Boolean).join(", "));
    setValue("#tab1 .s_phone", formatPhone(info.parentPhone));
    setValue("#tab1 .s_entry_date", ""); // TODO: entryHanDate/entryBookDate 합쳐서 넣을 거면 여기 처리
    setValue("#tab1 .s_school", info.school);
    setValue("#tab1 .s_grade_name", info.gradeName);
    setValue("#tab1 .s_birth", formatBirthDisplay(info.birth));
    setValue("#tab1 .s_address", info.address);
    setValue("#tab1 .s_address_detail", info.addressDetail);


    // ---------------- TAB2: 기본 정보 ----------------
    setValue("#tab2 .s_name", info.studentName);
    setValue("#tab2 .s_school", info.school);
    setValue("#tab2 .s_address", info.address);
    setValue("#tab2 .s_address_detail", info.addressDetail);
    setValue("#tab2 .s_phone", formatPhone(info.parentPhone));
    setValue("#tab2 .s_birth", formatBirthDisplay(info.birth)); // 텍스트 영역
    setValue("#tab2 .s_entry_han_date", info.entryHanDate);
    setValue("#tab2 .s_entry_book_date", info.entryBookDate);

    // ---------------- TAB3: 수업 정보 ----------------
    setValue("#tab3 .s_han_class", payment.hanClassName);
    setValue("#tab3 .s_book_class", payment.bookClassName);

    setValue("#tab3 .p_han_teacher", payment.hanTeacher);
    setValue("#tab3 .p_book_teacher", payment.bookTeacher);

    setValue("#tab3 .p_han_fee", formatMoney(payment.hanFee));
    setValue("#tab3 .p_book_fee", formatMoney(payment.bookFee));

    setValue("#tab3 .p_han_material_fee", formatMoney(payment.hanMaterialPrice));
    setValue("#tab3 .p_book_material_fee", formatMoney(payment.bookMaterialPrice));

    // 5) 수강 상태 버튼 표시 + 비활성화
    setCourseState("han", payment.hanState);
    setCourseState("book", payment.bookState);

    // 6) 시작일자
    setCourseDate("han", payment.entryHanDate);
    setCourseDate("book", payment.entryBookDate);

    // 7) 회비 합계
    updateTotalFee(payment);
    renderFeeTable(payment);

    // 8) 초기 수강 상태 저장
    if (typeof window.saveInitialCourseState === 'function') {
        window.saveInitialCourseState(payment.hanState, payment.bookState);
    }

    const birthInput = document.querySelector("#tab2 #birth-date");
    if (birthInput) {
        birthInput.value = formatBirthInput(info.birth); // date input용
    }

    const hanInput = document.querySelector("#entry-han-date");
    if (hanInput) {
        hanInput.value = info.entryHanDate;
    }

    const bookInput = document.querySelector("#entry-book-date");
    if (bookInput) {
        bookInput.value = info.entryBookDate;
    }
}

function setCourseState(type, state) {
    const group = document.querySelector(
        `#tab3 .choose-group[data-type="${type}"]`
    );
    if (!group) return;

    const buttons = group.querySelectorAll(".btn-choose");
    const hidden = group.querySelector('input[type="hidden"]');

    buttons.forEach(btn => {
        btn.classList.remove("active");

        const isActive =
            (state === "1" && btn.dataset.value === "active") ||
            (state === "0" && btn.dataset.value === "inactive") ||
            (state === 1 && btn.dataset.value === "active") ||
            (state === 0 && btn.dataset.value === "inactive");

        if (isActive) {
            btn.classList.add("active");
            hidden.value = btn.dataset.value;
        }
    });
}

function renderFeeTable(payment) {
    const tbody = document.getElementById('fee-tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    const addRow = (label, amount) => {
        if (!amount || amount <= 0) return;

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${label}</td>
            <td>${formatMoney(amount)}</td>
        `;
        tbody.appendChild(tr);
    };

    // 한자
    addRow('한자(교육비)', payment.hanFee);
    addRow('한자(교재비)', payment.hanMaterialPrice);

    // 독서
    addRow('독서(교육비)', payment.bookFee);
    addRow('독서(교재비)', payment.bookMaterialPrice);

    // 아무 것도 없을 경우
    if (tbody.children.length === 0) {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td colspan="2" style="text-align:center;">회비 정보 없음</td>
        `;
        tbody.appendChild(tr);
    }
}

function formatMoney(value) {
    if (value === null || value === undefined || value === '') {
        return '';
    }

    const num = Number(value.toString().replace(/,/g, ''));
    if (isNaN(num)) {
        return '';
    }

    return num.toLocaleString('ko-KR');
}

function updateTotalFee() {
    function removeComma(value) {
        if (!value) return '0';
        return value.toString().replace(/,/g, '');
    }

    const hanFee = parseInt(removeComma(
        document.querySelector("#hanFee")?.value
    )) || 0;

    const bookFee = parseInt(removeComma(
        document.querySelector("#bookFee")?.value
    )) || 0;

    const hanMat = parseInt(removeComma(
        document.querySelector("#hanMaterialFee")?.value
    )) || 0;

    const bookMat = parseInt(removeComma(
        document.querySelector("#bookMaterialFee")?.value
    )) || 0;

    const total = hanFee + bookFee + hanMat + bookMat;

    document.querySelector(".dues-sum span").innerText =
        formatMoney(total);
}

function setCourseDate(type, value) {
    const input = document.querySelector(
        `#tab3 input[type="date"][data-type="${type}"]`
    );
    if (input && value) {
        input.value = value;
    }
}

function renderGender(genderKey) {
    const wrapper = document.querySelector("#tab2 .gender-group");
    if (!wrapper) return;

    const buttons = wrapper.querySelectorAll(".s_gender");
    const hidden = wrapper.querySelector(".gender-hidden");
    if (!hidden) return;

    buttons.forEach(btn => {
        btn.classList.toggle("active", btn.dataset.value === genderKey);
    });
    hidden.value = genderKey;

    buttons.forEach(btn => {
        const clone = btn.cloneNode(true);
        btn.replaceWith(clone);
    });

    const freshButtons = wrapper.querySelectorAll(".s_gender");
    freshButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            const val = btn.dataset.value;

            freshButtons.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");
            hidden.value = val;
        });
    });
}

function renderParent(initialValue) {
    const group = document.querySelector(".relation-group");
    if (!group) return;

    const buttons = group.querySelectorAll(".btn-choose");
    const hidden = group.querySelector(".relation-hidden");
    if (!hidden) return;

    // 초기 상태
    buttons.forEach(btn => {
        const val = btn.dataset.value;
        btn.classList.toggle("active", val === initialValue);
    });
    hidden.value = initialValue;

    // 기존 이벤트 제거용 clone
    buttons.forEach(btn => {
        const newBtn = btn.cloneNode(true);
        btn.replaceWith(newBtn);
    });

    const freshButtons = group.querySelectorAll(".btn-choose");
    freshButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            const val = btn.dataset.value;
            freshButtons.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");
            hidden.value = val;
        });
    });
}

function renderGradeDropdown(gradeCodes, selectedKey) {
    try {
        const select = document.querySelector("#tab2 .s_grade");
        if (!select) return;

        select.innerHTML = "";

        const defaultOption = document.createElement("option");
        defaultOption.value = "";
        defaultOption.textContent = "학년 선택";
        select.appendChild(defaultOption);

        gradeCodes.forEach(code => {
            const opt = document.createElement("option");
            opt.value = code.gradeKey;
            opt.textContent = code.gradeName;

            if (String(code.gradeKey) === String(selectedKey)) {
                opt.selected = true;
            }

            select.appendChild(opt);
        });

    } catch (e) {
        console.error("renderGradeDropdown 오류:", e);
    }
}

document.addEventListener("change", (e) => {
    const input = e.target;
    if (input.type !== "date") return;

    const td = input.closest("td");
    if (!td) return;

    const display = td.querySelector(".day-display");
    if (!display) return;

    if (!input.value) {
        display.textContent = "날짜를 선택해주세요.";
        return;
    }

    const [y, m, d] = input.value.split("-");
    display.textContent = `${y}년 ${m}월 ${d}일`;
});

// ===============================
// 업데이트 (저장 버튼)
// ===============================
async function updateStudentInfo() {
    try {
        const req = {
            studentId: currentStudentId,                     // 필수
            studentName: getValue("#tab2 .s_name"),
            birth: unformatBirth(getValue("#tab2 #birth-date")), // YYYY-MM-DD -> YYMMDD
            genderKey: getValue("#tab2 .gender-hidden"),     // 0 / 1
            school: getValue("#tab2 .s_school"),
            address: getValue("#tab2 .s_address"),
            addressDetail: getValue("#tab2 .s_address_detail"),
            parentPhone: unformatPhone(getValue("#tab2 .s_phone")),
            gradeKey: getValue("#tab2 .s_grade"),
            relationKey: getValue("#tab2 .relation-hidden"),
            entryHanDate: getValue("#entry-han-date")?.trim() === "" ? null : getValue("#entry-han-date"),
            entryBookDate: getValue("#entry-book-date")?.trim() === "" ? null : getValue("#entry-book-date"),
        };

        const res = await fetch("/student/update/info", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(req)
        });

        if (!res.ok) {
            alert("오류가 발생했습니다.");
            return;
        }

        alert("저장되었습니다.");
        // window.location.reload();
    } catch (e) {
        console.error("updateStudentInfo 오류:", e);
        alert("저장 중 오류가 발생했습니다.");
    }
}

document.addEventListener("click", (e) => {
    const iconBtn = e.target.closest(".icon-btn");
    if (!iconBtn) return;

    const td = iconBtn.closest("td");
    if (!td) return;

    const dateInput = td.querySelector("input[type='date']");
    if (!dateInput) return;

    if (typeof dateInput.showPicker === "function") {
        dateInput.showPicker();
    } else {
        dateInput.click();
    }
});

document.addEventListener("change", (e) => {
    const input = e.target;
    if (input.type !== "date") return;

    const td = input.closest("td");
    if (!td) return;

    const display = td.querySelector(".birth-display");
    if (!display) return;

    // 화면에 보여줄용 YYYY년 MM월 DD일
    const [y, m, d] = input.value.split("-");
    display.textContent = `${y}년 ${m}월 ${d}일`;
});


// ====== 상태 변경 시 버튼 업데이트 ====== //
function updateStatusButton(statusCode) {
    const statusStr = String(statusCode);
    document.querySelectorAll('.status-buttons').forEach(group => {
        const mode = group.getAttribute('data-visibility');

        group.querySelectorAll('.s_status').forEach(btn => {
            const btnStatus = btn.getAttribute('data-status');

            if (mode === 'current-status') {

                btn.style.display = (btnStatus === statusStr) ? 'inline-block' : 'none';
            } else if (mode === 'except-current') {

                btn.style.display = (btnStatus === statusStr) ? 'none' : 'inline-block';
            }
        });
    });
}

// ====== 학생 상태 변경 로직 (TAB2) ====== //
document.addEventListener('DOMContentLoaded', function () {
    const exceptCurrentContainer = document.querySelector('.status-buttons[data-visibility="except-current"]');
    const reasonInputBox = document.querySelector('.reason-input');
    const reasonField = document.getElementById('reason');
    const submitBtn = document.getElementById('status-change');

    if (!exceptCurrentContainer) return;

    // 달력 아이콘 클릭 이벤트
    const calendarBtn = document.querySelector('.withdraw-date .icon-btn.calendar-open');
    if (calendarBtn) {
        calendarBtn.addEventListener('click', function () {
            const dateInput = document.getElementById('withdraw-date');
            if (dateInput) {
                if (typeof dateInput.showPicker === 'function') {
                    dateInput.showPicker();
                } else {
                    dateInput.click();
                }
            }
        });
    }

    // 날짜 선택 시 표시 업데이트
    const withdrawDateInput = document.getElementById('withdraw-date');
    if (withdrawDateInput) {
        withdrawDateInput.addEventListener('change', function () {
            const display = document.querySelector('.withdraw-date .day-display');
            if (display && this.value) {
                const [y, m, d] = this.value.split('-');
                display.value = `${y}년 ${m}월 ${d}일`;
            }
        });
    }

    exceptCurrentContainer.addEventListener('click', function (e) {
        const btn = e.target.closest('.s_status');
        if (!btn || !exceptCurrentContainer.contains(btn) || !reasonInputBox) return;

        exceptCurrentContainer.querySelectorAll('.s_status')
            .forEach(b => b.classList.remove('selected'));

        btn.classList.add('selected');

        const status = btn.dataset.status;

        reasonInputBox.classList.add('active');

        const withdrawDateDiv = document.querySelector('.withdraw-date');

        if (status === 'ACTIVE') {
            reasonField.classList.add('hide-input');
            reasonField.value = "";
            if (withdrawDateDiv) withdrawDateDiv.classList.add('hide-input');
        } else if (status === 'WITHDRAWN') {
            reasonField.classList.remove('hide-input');
            if (withdrawDateDiv) withdrawDateDiv.classList.remove('hide-input');
        } else {
            reasonField.classList.remove('hide-input');
            if (withdrawDateDiv) withdrawDateDiv.classList.add('hide-input');
        }
    });

    if (submitBtn) {
        submitBtn.addEventListener('click', function (e) {
            e.preventDefault();

            const selectedBtn = exceptCurrentContainer.querySelector('.s_status.selected');
            if (!selectedBtn) {
                alert('상태를 선택해주세요.');
                return;
            }

            const statusKey = selectedBtn.dataset.status;

            let reason = "";
            let withdrawDate = null;

            if (statusKey !== "ACTIVE") {
                reason = reasonField.value.trim();
                if (!reason) {
                    alert('사유를 입력해주세요.');
                    return;
                }

                if (statusKey === "WITHDRAWN") {
                    const dateInput = document.getElementById('withdraw-date');
                    withdrawDate = dateInput?.value;
                    if (!withdrawDate) {
                        alert('탈퇴 날짜를 입력해주세요.');
                        return;
                    }
                }
            }

            if (!currentStudentId) {
                alert('학생 ID를 찾을 수 없습니다.');
                return;
            }

            const requestBody = {
                studentId: currentStudentId,
                statusKey: statusKey,
                reason: reason,
                withdrawDate: withdrawDate
            };

            fetch('/student/status', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            })
                .then(res => {
                    if (!res.ok) throw new Error('요청 실패!');
                    return res.json();
                })
                .then(data => {
                    alert('상태가 성공적으로 변경되었습니다.');

                    const s = data.response;
                    if (!s || !s.statusKey) {
                        return;
                    }

                    updateStatusButton(s.statusKey);

                    const row = document.querySelector(`#main-student-tbody tr[data-id='${s.studentId}']`);
                    if (row) {
                        const tds = row.querySelectorAll('td');
                        if (tds.length > 2) {
                            tds[2].textContent = s.statusName;
                        }
                    }

                    // 입력 초기화
                    reasonField.value = '';
                    const dateInput = document.getElementById('withdraw-date');
                    const display = document.querySelector('.withdraw-date .day-display');
                    if (dateInput) dateInput.value = '';
                    if (display) display.value = '';

                    exceptCurrentContainer.querySelectorAll('.s_status').forEach(b => b.classList.remove('selected'));
                    reasonInputBox.classList.remove('active');
                })
                .catch(err => {
                    alert('오류가 발생했습니다.');
                });
        });
    }
});

// ====== TAB3 수강상태 변경 로직 ====== //
document.addEventListener('DOMContentLoaded', function () {
    // 초기 수강 상태 저장용 변수
    let initialHanState = null;
    let initialBookState = null;

    // 한자 달력 아이콘 클릭
    const hanCalendarBtn = document.querySelector('.han-calendar-open');
    if (hanCalendarBtn) {
        hanCalendarBtn.addEventListener('click', function () {
            const dateInput = document.getElementById('han-inactive-date');
            if (dateInput) {
                if (typeof dateInput.showPicker === 'function') {
                    dateInput.showPicker();
                } else {
                    dateInput.click();
                }
            }
        });
    }

    // 독서 달력 아이콘 클릭
    const bookCalendarBtn = document.querySelector('.book-calendar-open');
    if (bookCalendarBtn) {
        bookCalendarBtn.addEventListener('click', function () {
            const dateInput = document.getElementById('book-inactive-date');
            if (dateInput) {
                if (typeof dateInput.showPicker === 'function') {
                    dateInput.showPicker();
                } else {
                    dateInput.click();
                }
            }
        });
    }

    // 한자 날짜 선택 시 표시 업데이트
    const hanDateInput = document.getElementById('han-inactive-date');
    if (hanDateInput) {
        hanDateInput.addEventListener('change', function () {
            const display = document.querySelector('.han-date-display');
            if (display && this.value) {
                const [y, m, d] = this.value.split('-');
                display.value = `${y}년 ${m}월 ${d}일`;
            }
        });
    }

    // 독서 날짜 선택 시 표시 업데이트
    const bookDateInput = document.getElementById('book-inactive-date');
    if (bookDateInput) {
        bookDateInput.addEventListener('change', function () {
            const display = document.querySelector('.book-date-display');
            if (display && this.value) {
                const [y, m, d] = this.value.split('-');
                display.value = `${y}년 ${m}월 ${d}일`;
            }
        });
    }

    // 수강상태 버튼 클릭 이벤트
    document.addEventListener("click", (e) => {
        const btn = e.target.closest("#tab3 .choose-group .btn-choose");
        if (!btn) return;

        const group = btn.closest(".choose-group");
        const hidden = group.querySelector('input[type="hidden"]');
        if (!hidden) return;

        group.querySelectorAll(".btn-choose")
            .forEach(b => b.classList.remove("active"));

        btn.classList.add("active");
        hidden.value = btn.dataset.value;

        const type = group.dataset.type; // 'han' or 'book'
        const status = btn.dataset.value; // 'active' or 'inactive'

        // 미수강 입력창 표시/숨김
        const inactiveInput = document.querySelector(`.${type}-inactive`);
        if (inactiveInput) {
            if (status === 'inactive') {
                inactiveInput.classList.remove('hide-input');
            } else {
                inactiveInput.classList.add('hide-input');
            }
        }

        console.log(`[${type}] 상태 변경 →`, status);
    });

    // 수강상태 저장 버튼
    const courseStatusBtn = document.getElementById("course-status-save");
    if (courseStatusBtn) {
        courseStatusBtn.addEventListener('click', async function (e) {
            e.preventDefault();

            if (!currentStudentId) {
                alert('학생 ID를 찾을 수 없습니다.');
                return;
            }

            // 현재 상태 확인
            const hanGroup = document.querySelector('.choose-group[data-type="han"]');
            const hanHidden = hanGroup?.querySelector('input[type="hidden"]');
            const currentHanState = hanHidden?.value; // 'active' or 'inactive'

            const bookGroup = document.querySelector('.choose-group[data-type="book"]');
            const bookHidden = bookGroup?.querySelector('input[type="hidden"]');
            const currentBookState = bookHidden?.value;

            // 변경 여부 확인
            const hanChanged = initialHanState !== null && initialHanState !== currentHanState;
            const bookChanged = initialBookState !== null && initialBookState !== currentBookState;

            if (!hanChanged && !bookChanged) {
                alert('변경된 수강 상태가 없습니다.');
                return;
            }

            let hanInactiveDate = null;
            let hanInactiveReason = null;
            let entryHanDate = null;

            // 한자가 변경된 경우만 검증
            if (hanChanged) {
                if (currentHanState === 'inactive') {
                    // 미수강으로 변경: 사유 + 날짜 필수
                    const hanReasonInput = document.getElementById('han-inactive-reason');
                    const hanDateInput = document.getElementById('han-inactive-date');

                    hanInactiveReason = hanReasonInput?.value.trim();
                    hanInactiveDate = hanDateInput?.value;

                    if (!hanInactiveReason) {
                        alert('한자 미수강 사유를 입력해주세요.');
                        return;
                    }
                    if (!hanInactiveDate) {
                        alert('한자 미수강 날짜를 선택해주세요.');
                        return;
                    }
                } else if (currentHanState === 'active') {
                    // 수강으로 변경: 입회날짜 필수
                    const entryHanInput = document.getElementById('entry-han-date');
                    entryHanDate = entryHanInput?.value;

                    if (!entryHanDate) {
                        alert('한자 입회 날짜를 선택해주세요.');
                        return;
                    }
                }
            }

            let bookInactiveDate = null;
            let bookInactiveReason = null;
            let entryBookDate = null;

            // 독서가 변경된 경우만 검증
            if (bookChanged) {
                if (currentBookState === 'inactive') {
                    // 미수강으로 변경: 사유 + 날짜 필수
                    const bookReasonInput = document.getElementById('book-inactive-reason');
                    const bookDateInput = document.getElementById('book-inactive-date');

                    bookInactiveReason = bookReasonInput?.value.trim();
                    bookInactiveDate = bookDateInput?.value;

                    if (!bookInactiveReason) {
                        alert('독서 미수강 사유를 입력해주세요.');
                        return;
                    }
                    if (!bookInactiveDate) {
                        alert('독서 미수강 날짜를 선택해주세요.');
                        return;
                    }
                } else if (currentBookState === 'active') {
                    // 수강으로 변경: 입회날짜 필수
                    const entryBookInput = document.getElementById('entry-book-date');
                    entryBookDate = entryBookInput?.value;

                    if (!entryBookDate) {
                        alert('독서 입회 날짜를 선택해주세요.');
                        return;
                    }
                }
            }

            const requestBody = {
                studentId: currentStudentId,
                hanState: currentHanState === 'active' ? 1 : 0,
                bookState: currentBookState === 'active' ? 1 : 0,
                hanChanged: hanChanged,
                bookChanged: bookChanged,
                entryHanDate: entryHanDate,
                entryBookDate: entryBookDate,
                inactiveHanDate: hanInactiveDate,
                inactiveBookDate: bookInactiveDate,
                inactiveHanReason: hanInactiveReason,
                inactiveBookReason: bookInactiveReason
            };

            try {
                const res = await fetch('/student/update/course-status', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(requestBody)
                });

                if (!res.ok) {
                    throw new Error('수강상태 변경 실패');
                }

                alert('수강상태가 성공적으로 변경되었습니다.');

                // 초기 상태 업데이트
                initialHanState = currentHanState;
                initialBookState = currentBookState;

                // 입력 초기화
                const hanReasonInput = document.getElementById('han-inactive-reason');
                const hanDateInput = document.getElementById('han-inactive-date');
                const hanDisplay = document.querySelector('.han-date-display');
                const bookReasonInput = document.getElementById('book-inactive-reason');
                const bookDateInput = document.getElementById('book-inactive-date');
                const bookDisplay = document.querySelector('.book-date-display');

                if (hanReasonInput) hanReasonInput.value = '';
                if (hanDateInput) hanDateInput.value = '';
                if (hanDisplay) hanDisplay.value = '';
                if (bookReasonInput) bookReasonInput.value = '';
                if (bookDateInput) bookDateInput.value = '';
                if (bookDisplay) bookDisplay.value = '';

                document.querySelector('.han-inactive')?.classList.add('hide-input');
                document.querySelector('.book-inactive')?.classList.add('hide-input');

            } catch (err) {
                console.error(err);
                alert('수강상태 변경 중 오류가 발생했습니다.');
            }
        });
    }

    // renderStudentModal이 호출될 때 초기 상태 저장
    window.saveInitialCourseState = function(hanState, bookState) {
        // 0 또는 1을 'active' 또는 'inactive'로 변환
        initialHanState = (hanState === 1 || hanState === '1') ? 'active' : 'inactive';
        initialBookState = (bookState === 1 || bookState === '1') ? 'active' : 'inactive';
        console.log('초기 수강 상태 저장:', { initialHanState, initialBookState });
    };
});

// ===== 교재비 & 입회날짜 변경 ===== //
document.addEventListener("DOMContentLoaded", () => {
    const payBtn = document.getElementById("pay-info");
    if (!payBtn) return;

    payBtn.addEventListener("click", async (e) => {
        e.preventDefault();
        console.log('버튼 클릭')

        if (!currentStudentId) {
            alert("학생을 먼저 선택해주세요.");
            return;
        }

        const payload = {
            studentId: currentStudentId,

            entryHanDate: getValue("#entry-han-date")?.trim() || null,
            entryBookDate: getValue("#entry-book-date")?.trim() || null,

            hanMaterialFee: parseInt(
                (getValue("#hanMaterialFee") || "0").replace(/,/g, "")
            ) || 0,

            bookMaterialFee: parseInt(
                (getValue("#bookMaterialFee") || "0").replace(/,/g, "")
            ) || 0
        };

        try {
            const res = await fetch("/student/update/payment", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            if (!res.ok) {
                throw new Error("결제 정보 저장 실패");
            }

            alert("결제 정보가 저장되었습니다.");

            updateTotalFee();

        } catch (err) {
            console.error(err);
            alert("저장 중 오류가 발생했습니다.");
        }
    });
});