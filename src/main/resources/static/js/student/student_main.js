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
    const tbody = document.getElementById("main-student-tbody");

    if (!searchBtn || !tbody) return;

    searchBtn.addEventListener("click", () => {
        const filterType = document.getElementById("stu-name")?.value || "all";
        const keyword = document.getElementById("search-name")?.value.trim().toLowerCase();

        // 검색어 없으면 전체 표시
        if (!keyword) {
            [...tbody.querySelectorAll("tr")].forEach(tr => tr.style.display = "");
            return;
        }

        [...tbody.querySelectorAll("tr")].forEach(tr => {
            const tds = tr.querySelectorAll("td");
            if (tds.length < 8) return;

            let textToSearch = "";

            switch (filterType) {
                case "이름":
                case "name":
                    textToSearch = tds[1].innerText.toLowerCase();  // 이름
                    break;

                case "수강과목":
                    textToSearch = tds[4].innerText.toLowerCase();  // 수강과목
                    break;

                case "학교/유치원":
                    textToSearch = tds[6].innerText.toLowerCase();  // 학교
                    break;

                default:
                    // 전체에서 검색
                    textToSearch = tr.innerText.toLowerCase();
                    break;
            }

            // 포함 여부 확인
            tr.style.display = textToSearch.includes(keyword) ? "" : "none";
        });
    });
});

// ====== 학생 리스트 변경 ====== //
function renderStudents(tbody, students = []) {
    tbody.innerHTML = "";

    students.forEach((s, i) => {
        const tr = document.createElement("tr");
        tr.setAttribute("data-id", s.studentId);
        tr.setAttribute("onclick", "openModal(this)");

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
    const payment = data.studentPayment;
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


    function setCourseState(type, state) {
        const group = document.querySelector(
            `#tab3 .choose-group[data-type="${type}"]`
        );
        if (!group) return;

        const buttons = group.querySelectorAll(".btn-choose");
        const hidden = group.querySelector('input[type="hidden"]');

        buttons.forEach(btn => {
            btn.classList.remove("active");
            btn.disabled = true; // 클릭 방지

            const isActive =
                (state === "1" && btn.dataset.value === "active") ||
                (state === "0" && btn.dataset.value === "inactive");

            if (isActive) {
                btn.classList.add("active");
                hidden.value = btn.dataset.value;
            }
        });
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

// ====== 학생 상태 변경 로직 ====== //
document.addEventListener('DOMContentLoaded', function () {
    const exceptCurrentContainer = document.querySelector('.status-buttons[data-visibility="except-current"]');
    const reasonInputBox = document.querySelector('.reason-input');
    const reasonField = reasonInputBox?.querySelector('input, textarea'); // 사유 입력 필드
    const submitBtn = document.getElementById('status-change'); // 상태 변경 버튼

    if (!exceptCurrentContainer) return;

    exceptCurrentContainer.addEventListener('click', function (e) {
        const btn = e.target.closest('.s_status');
        if (!btn || !exceptCurrentContainer.contains(btn) || !reasonInputBox) return;

        exceptCurrentContainer.querySelectorAll('.s_status')
            .forEach(b => b.classList.remove('selected'));

        btn.classList.add('selected');

        const status = btn.dataset.status;

        reasonInputBox.classList.add('active');

        if (status === 'ACTIVE') {
            reasonField.classList.add('hide-input');
            reasonField.value = "";
        } else {
            reasonField.classList.remove('hide-input');
        }
    });

    // 변경 버튼 클릭 서버로 전송
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
            if (statusKey !== "ACTIVE") {
                const reason = reasonField?.value.trim();
                if (!reason) {
                    alert('사유를 입력해주세요.');
                    return;
                }
            }

            if (!currentStudentId) {
                alert('학생 ID를 찾을 수 없습니다.');
                return;
            }

            const requestBody = {
                studentId: currentStudentId,
                statusKey: statusKey,
                reason: reason
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
                    exceptCurrentContainer.querySelectorAll('.s_status').forEach(b => b.classList.remove('selected'));
                    reasonInputBox.classList.remove('active');
                })
                .catch(err => {
                    alert('오류가 발생했습니다.');
                });
        });
    }
});
