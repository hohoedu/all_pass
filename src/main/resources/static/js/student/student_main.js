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

            fetch(`/student/api/label?userCode=${encodeURIComponent(userCode)}`)
                .then(res => res.json())
                .then(data => {
                    subjectFilter.innerHTML = `<option value="all">전체</option>`;
                    data.response.forEach(item => {
                        subjectFilter.innerHTML += `<option value="${item.timeTableKey}">${item.classLabel}</option>`;
                    });
                })
                .catch(() => {
                    subjectFilter.innerHTML = `<option value="all">전체</option>`;
                });

            fetch(`/student/api/students?userCode=${encodeURIComponent(userCode)}`)
                .then(res => res.json())
                .then(data => {
                    renderStudents(tbody, data.response);

                    // ✅ 추가: 검색어가 남아있으면 새 목록에서 자동 재검색
                    if (typeof window.doSearch === "function") {
                        window.doSearch();
                    }
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
// ============ 검색 ============ //
document.addEventListener("DOMContentLoaded", () => {
    const searchBtn = document.getElementById("search-main-student");
    const searchInput = document.getElementById("search-name");
    const filterSelect = document.getElementById("stu-name");
    const tbody = document.getElementById("main-student-tbody");

    if (!searchBtn || !searchInput || !tbody) return;

    const getRows = () => Array.from(tbody.querySelectorAll("tr"));

    window.doSearch = function () {
        const keyword = searchInput.value.trim().toLowerCase();
        const filterType = filterSelect?.value || "name"; // ✅ "all" → "name"
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
                case "name":   // ✅ "all" → "name"
                    textToSearch = tds[1]?.innerText.toLowerCase() || "";
                    break;
                case "class":
                    textToSearch = tds[4]?.innerText.toLowerCase() || "";
                    break;
                case "phone":
                    const rawPhone = tr.dataset.phone ?? "";
                    const normalizedPhone = rawPhone.replace(/-/g, "");
                    const normalizedKeyword = keyword.replace(/-/g, "");
                    if (normalizedPhone.includes(normalizedKeyword)) {
                        tr.style.display = "";
                        matchedCount++;
                    } else {
                        tr.style.display = "none";
                    }
                    return;
                case "school":
                    textToSearch = tds[6]?.innerText.toLowerCase() || "";
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

        if (matchedCount === 0) {
            rows.forEach(tr => (tr.style.display = ""));
        }
    };

    searchBtn.addEventListener("click", window.doSearch);

    searchInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            window.doSearch();
        }
    });

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
        tr.setAttribute("data-phone", (s.appId ?? "").substring(0, 8));
        const appIcon = s.hasApp === "Y"
            ? `<div class="tooltip-container">
                 <img src="/image/in_app.png" alt="앱 연결" class="app-icon" 
                 style="width: 25px; height: 25px;">
                 <div class="tooltip-text">${s.appId}</div>
               </div>`
            : `<div class="tooltip-container">
                 <img src="/image/no_app.png" alt="앱 연결" class="app-icon" 
                 style="width: 25px; height: 25px;">
                 <div class="tooltip-text">${s.appId}</div>
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
    if (!birth || typeof birth !== 'string') return "";

    let year, month, day;

    // 1️⃣ yyyy년 mm월 dd일
    let match = birth.match(/^(\d{4})\s*년\s*(\d{1,2})\s*월\s*(\d{1,2})\s*일$/);
    if (match) {
        [, year, month, day] = match;
    }

    // 2️⃣ yyyy-mm-dd | yyyy/mm/dd | yyyy.mm.dd
    if (!year) {
        match = birth.match(/^(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})$/);
        if (match) {
            [, year, month, day] = match;
        }
    }

    // 3️⃣ yyyymmdd
    if (!year) {
        match = birth.match(/^(\d{4})(\d{2})(\d{2})$/);
        if (match) {
            [, year, month, day] = match;
        }
    }

    // 4️⃣ yymmdd → 20yy 기준 (필요 시 기준 변경 가능)
    if (!year) {
        match = birth.match(/^(\d{2})(\d{2})(\d{2})$/);
        if (match) {
            const yy = parseInt(match[1], 10);
            year = yy >= 30 ? `19${match[1]}` : `20${match[1]}`;
            month = match[2];
            day = match[3];
        }
    }

    if (!year || !month || !day) return "";

    month = month.padStart(2, "0");
    day = day.padStart(2, "0");

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
    const consult = data.studentCounsult;

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
    setValue("#tab2 .s_birth", formatBirthDisplay(info.birth));
    setValue("#tab2 .s_billing_phone", formatPhone(info.billingPhone));


    // ---------------- TAB3: 수업 정보 ----------------
    setValue("#tab3 .s_han_class", payment.hanClassName);
    setValue("#tab3 .s_book_class", payment.bookClassName);

    setValue("#tab3 .p_han_teacher", payment.hanTeacher);
    setValue("#tab3 .p_book_teacher", payment.bookTeacher);

    setValue("#tab3 .p_han_fee", formatMoney(payment.hanFee));
    setValue("#tab3 .p_book_fee", formatMoney(payment.bookFee));

    setValue("#tab3 .p_han_material_fee", formatMoney(payment.hanMaterialPrice));
    setValue("#tab3 .p_book_material_fee", formatMoney(payment.bookMaterialPrice));

    setValue("#tab3 .s_entry_han_date", formatDate(payment.entryHanDate) ?? '날짜를 선택해주세요.');
    setValue("#tab3 .s_entry_book_date", formatDate(payment.entryBookDate) ?? '날짜를 선택해주세요.');

    // ---------------- TAB4: 출결 정보 ----------------
    let currentAttendanceYear = new Date().getFullYear();
    let currentAttendanceMonth = new Date().getMonth() + 1;
    renderCalendar(currentAttendanceYear, currentAttendanceMonth);
    function renderCalendar(year, month) {
        const titleEl = document.getElementById("calendar-title");
        if (titleEl) {
            titleEl.textContent = `${year}년 ${String(month).padStart(2, "0")}월`;
        }

        const firstDay = new Date(year, month - 1, 1).getDay(); // 1일 요일
        const lastDate = new Date(year, month, 0).getDate();    // 마지막 날

        const tbody = document.querySelector("#tab4 .calendar-table tbody");
        if (!tbody) return;

        tbody.innerHTML = "";

        let day = 1;
        for (let row = 0; row < 6; row++) {
            if (day > lastDate) break;

            const tr = document.createElement("tr");

            for (let col = 0; col < 7; col++) {
                const td = document.createElement("td");

                if (row === 0 && col < firstDay) {
                    td.innerHTML = "";
                } else if (day > lastDate) {
                    td.innerHTML = "";
                } else {
                    td.textContent = day;
                    day++;
                }

                tr.appendChild(td);
            }

            tbody.appendChild(tr);
        }
    }

// 이전달 / 다음달
    document.addEventListener("DOMContentLoaded", () => {
        const prevBtn = document.getElementById("calendar-prev");
        const nextBtn = document.getElementById("calendar-next");

        if (prevBtn) {
            prevBtn.addEventListener("click", () => {
                currentAttendanceMonth--;
                if (currentAttendanceMonth < 1) {
                    currentAttendanceMonth = 12;
                    currentAttendanceYear--;
                }
                renderCalendar(currentAttendanceYear, currentAttendanceMonth);
            });
        }

        if (nextBtn) {
            nextBtn.addEventListener("click", () => {
                currentAttendanceMonth++;
                if (currentAttendanceMonth > 12) {
                    currentAttendanceMonth = 1;
                    currentAttendanceYear++;
                }
                renderCalendar(currentAttendanceYear, currentAttendanceMonth);
            });
        }
    });
    // ---------------- TAB5: 상담 정보 ----------------
    renderConsult(consult ?? []);

    // 5) 수강 상태 버튼 표시 + 비활성화
    setCourseState("han", payment.hanState);
    setCourseState("book", payment.bookState);

    // 6) 시작일자
    setCourseDate("han", payment.entryHanDate);
    setCourseDate("book", payment.entryBookDate);

    // 7) 회비 합계
    updateTotalFee(payment);
    renderFeeTable(payment);

    // 8) 초기 수강 상태 저장 - 날짜도 함께 전달
    if (typeof window.saveInitialCourseState === 'function') {
        window.saveInitialCourseState(payment.hanState, payment.bookState);
    }

    const birthInput = document.querySelector("#tab2 #birth-date");
    if (birthInput) {
        const formattedBirth = formatBirthInput(info.birth);

        if (formattedBirth) {
            birthInput.value = formattedBirth;
        } else {
            birthInput.value = info.birth || "";
        }

    }

    const hanInput = document.querySelector("#entry-han-date");
    if (hanInput) {
        hanInput.value = payment.entryHanDate || '';
    }

    const bookInput = document.querySelector("#entry-book-date");
    if (bookInput) {
        bookInput.value = payment.entryBookDate || '';
    }
}

function renderConsult(consultList = []) {
    const tbodies = document.querySelectorAll("#consult-tbody");
    if (!tbodies.length) return;

    // null, 공백 제외
    const filtered = consultList.filter(c => c.consultContent?.trim());

    if (!filtered.length) {
        tbodies.forEach(tbody => {
            tbody.innerHTML = `<tr><td colspan="2" style="text-align:center;">상담 내역이 없습니다.</td></tr>`;
        });
        return;
    }
    const rows = filtered.map(c => {
        const dateOnly = c.consultDate?.replace(/(\d{4}년 \d{2}월 \d{2}일).*/, '$1') ?? "";
        return `
        <tr>
            <td>${dateOnly}</td>
            <td>${c.consultContent}</td>
        </tr>
    `;
    }).join("");

    tbodies.forEach(tbody => tbody.innerHTML = rows);
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

function formatDate(value) {
    if (!value || typeof value !== 'string') return '';

    // 이미 "yyyy년 MM월 dd일" 형식이면 그대로 반환
    if (/^\d{4}년\s?\d{1,2}월\s?\d{1,2}일$/.test(value)) {
        return value;
    }

    // "yyyy-MM-dd" 형식이면 변환
    const match = value.match(/^(\d{4})-(\d{2})-(\d{2})$/);
    if (match) {
        const [, y, m, d] = match;
        return `${y}년 ${m}월 ${d}일`;
    }

    return '';
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
            birth: getValue("#tab2 #birth-date"),
            genderKey: getValue("#tab2 .gender-hidden"),     // 0 / 1
            school: getValue("#tab2 .s_school"),
            address: getValue("#tab2 .s_address"),
            addressDetail: getValue("#tab2 .s_address_detail"),
            parentPhone: unformatPhone(getValue("#tab2 .s_phone")),
            gradeKey: getValue("#tab2 .s_grade"),
            relationKey: getValue("#tab2 .relation-hidden"),
            billingPhone: unformatPhone(getValue("#tab2 .s_billing_phone")),
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
    let initialEntryHanDate = null;
    let initialEntryBookDate = null;

    // 한자 달력 아이콘 클릭
    const hanCalendarBtn = document.querySelector('.han-calendar-open');
    if (hanCalendarBtn) {
        hanCalendarBtn.addEventListener('click', function () {
            const dateInput = document.getElementById('han-inactive-date');
            if (dateInput) {
                dateInput.focus();
                dateInput.click();
            }
        });
    }

    // 독서 달력 아이콘 클릭
    const bookCalendarBtn = document.querySelector('.book-calendar-open');
    if (bookCalendarBtn) {
        bookCalendarBtn.addEventListener('click', function () {
            const dateInput = document.getElementById('book-inactive-date');
            if (dateInput) {
                dateInput.focus();
                dateInput.click();
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

            const hanGroup = document.querySelector('.choose-group[data-type="han"]');
            const hanHidden = hanGroup?.querySelector('input[type="hidden"]');
            const currentHanState = hanHidden?.value;

            const bookGroup = document.querySelector('.choose-group[data-type="book"]');
            const bookHidden = bookGroup?.querySelector('input[type="hidden"]');
            const currentBookState = bookHidden?.value;

            const entryHanInput = document.getElementById('entry-han-date');
            const entryBookInput = document.getElementById('entry-book-date');
            const hanReasonInput = document.getElementById('han-inactive-reason');
            const hanDateInput = document.getElementById('han-inactive-date');
            const bookReasonInput = document.getElementById('book-inactive-reason');
            const bookDateInput = document.getElementById('book-inactive-date');

            // 상태 변경 또는 날짜 변경 감지
            const hanStateChanged = initialHanState !== null && initialHanState !== currentHanState;
            const hanDateChanged = initialEntryHanDate !== entryHanInput?.value;
            const hanChanged = hanStateChanged || hanDateChanged;

            const bookStateChanged = initialBookState !== null && initialBookState !== currentBookState;
            const bookDateChanged = initialEntryBookDate !== entryBookInput?.value;
            const bookChanged = bookStateChanged || bookDateChanged;

            console.log('initialHanState:', initialHanState, 'currentHanState:', currentHanState);
            console.log('initialBookState:', initialBookState, 'currentBookState:', currentBookState);
            console.log('initialEntryHanDate:', initialEntryHanDate, 'currentEntryHanDate:', entryHanInput?.value);
            console.log('initialEntryBookDate:', initialEntryBookDate, 'currentEntryBookDate:', entryBookInput?.value);
            console.log('hanChanged:', hanChanged, 'bookChanged:', bookChanged);

            const requestBody = {
                studentId: currentStudentId,
                hanState: currentHanState === 'active' ? 1 : 0,
                bookState: currentBookState === 'active' ? 1 : 0,
                hanChanged: hanChanged,
                bookChanged: bookChanged,
                entryHanDate: entryHanInput?.value || null,
                entryBookDate: entryBookInput?.value || null,
                inactiveHanDate: hanDateInput?.value || null,
                inactiveBookDate: bookDateInput?.value || null,
                inactiveHanReason: hanReasonInput?.value?.trim() || null,
                inactiveBookReason: bookReasonInput?.value?.trim() || null
            };

            if (currentHanState === 'inactive' && currentBookState === 'inactive') {
                const shouldChangeStudentStatus = await showStudentStatusModal();
                if (!shouldChangeStudentStatus) {
                    return;
                }
                requestBody.changeStudentStatus = true;
            }

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
                initialEntryHanDate = entryHanInput?.value;
                initialEntryBookDate = entryBookInput?.value;

                // 입력 초기화
                const hanDisplay = document.querySelector('.han-date-display');
                const bookDisplay = document.querySelector('.book-date-display');

                if (hanReasonInput) hanReasonInput.value = '';
                if (hanDateInput) hanDateInput.value = '';
                if (hanDisplay) hanDisplay.value = '';
                if (bookReasonInput) bookReasonInput.value = '';
                if (bookDateInput) bookDateInput.value = '';
                if (bookDisplay) bookDisplay.value = '';

                document.querySelector('.han-inactive')?.classList.add('hide-input');
                document.querySelector('.book-inactive')?.classList.add('hide-input');

                await refreshStudentList();

            } catch (err) {
                console.error(err);
                alert('수강상태 변경 중 오류가 발생했습니다.');
            }
        });
    }


    async function refreshStudentList() {
        try {
            const teacherFilter = document.getElementById("main-teacher-filter");
            const subjectFilter = document.getElementById("main-subject-filter");
            const tbody = document.getElementById("main-student-tbody");

            if (!tbody) return;

            // 현재 선택된 필터 값 가져오기
            const userCode = teacherFilter?.value || 'all';
            const timeTableKey = subjectFilter?.value || 'all';

            // API 호출
            let url = '/student/api/students?';

            if (timeTableKey !== 'all') {
                url += `timeTableKey=${encodeURIComponent(timeTableKey)}&`;
            }

            url += `userCode=${encodeURIComponent(userCode)}`;

            const res = await fetch(url);

            if (!res.ok) {
                throw new Error('학생 목록 조회 실패');
            }

            const data = await res.json();

            // 학생 목록 다시 렌더링
            renderStudents(tbody, data.response);

            console.log('학생 목록 최신화 완료');

        } catch (err) {
            console.error('학생 목록 최신화 실패:', err);
        }
    }

    function showStudentStatusModal() {
        return new Promise((resolve) => {
            const modalId = 'status-change-modal-' + Date.now(); // 고유 ID

            const modal = `
            <div class="modal-overlay" id="${modalId}" style="
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.5);
                display: flex;
                justify-content: center;
                align-items: center;
                z-index: 10000;
            ">
                <div class="modal-content" style="
                    background: white;
                    padding: 30px;
                    border-radius: 8px;
                    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                    max-width: 400px;
                    text-align: center;
                ">
                    <h3 style="margin-bottom: 15px;">학생 상태 변경</h3>
                    <p style="margin-bottom: 20px; line-height: 1.6;">
                        한자와 독서 모두 미수강으로 변경됩니다.<br>
                        학생 상태도 함께 변경하시겠습니까?
                    </p>
                    <button class="confirm-btn" style="
                        padding: 10px 20px;
                        margin: 0 5px;
                        background: #007bff;
                        color: white;
                        border: none;
                        border-radius: 4px;
                        cursor: pointer;
                    ">확인</button>
                    <button class="cancel-btn" style="
                        padding: 10px 20px;
                        margin: 0 5px;
                        background: #6c757d;
                        color: white;
                        border: none;
                        border-radius: 4px;
                        cursor: pointer;
                    ">취소</button>
                </div>
            </div>
        `;

            document.body.insertAdjacentHTML('beforeend', modal);

            const modalElement = document.getElementById(modalId);
            console.log('모달 생성됨:', modalElement);

            // 이벤트 위임 방식으로 변경
            modalElement.addEventListener('click', (e) => {
                console.log('클릭됨:', e.target);

                if (e.target.classList.contains('confirm-btn')) {
                    console.log('확인 버튼 클릭');
                    modalElement.remove();
                    resolve(true);
                } else if (e.target.classList.contains('cancel-btn')) {
                    console.log('취소 버튼 클릭');
                    modalElement.remove();
                    resolve(false);
                }
            });
        });
    }

    // renderStudentModal이 호출될 때 초기 상태 저장
    window.saveInitialCourseState = function (hanState, bookState) {
        // 0 또는 1을 'active' 또는 'inactive'로 변환
        initialHanState = (hanState === 1 || hanState === '1') ? 'active' : 'inactive';
        initialBookState = (bookState === 1 || bookState === '1') ? 'active' : 'inactive';

        // input에서 직접 읽어오기
        const hanInput = document.getElementById('entry-han-date');
        const bookInput = document.getElementById('entry-book-date');
        initialEntryHanDate = hanInput?.value || null;
        initialEntryBookDate = bookInput?.value || null;

        console.log('초기 수강 상태 저장:', {
            initialHanState,
            initialBookState,
            initialEntryHanDate,
            initialEntryBookDate
        });
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