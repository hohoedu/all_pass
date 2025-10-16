// 학생 상세정보 오픈 엔트리 포인트
let currentStudentId = null;

function openModal(row) {
    const studentId = row.getAttribute("data-id");
    loadStudentData(studentId)
        .then(renderStudentInfo)
        .then(loadGradeCodes)
        .catch(console.error);

    showModal();
}

// 학생 상세정보 모달 오픈
function showModal() {
    document.querySelector('.modal').style.display = 'block';
}

// 학생 정보 불러오기
async function loadStudentData(studentId) {
    const res = await fetch(`/student/${studentId}`);
    if (!res.ok) throw new Error("서버 오류");
    const data = await res.json();
    return data.response;
}

// 모달창에 데이터 갈아끼우기
function renderStudentInfo(s) {
    const setValue = (selector, value) => {
        document.querySelectorAll(selector).forEach(el => {
            if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') el.value = value;
            else el.innerText = value;
        });
    };
    console.log('student_id = ' + s.studentId);
    setValue(".s_student_id", s.studentId || '');
    currentStudentId = s.studentId;
    setValue(".s_name", s.studentName || '');
    setValue(".s_subjects", [s.hanClass, s.bookClass].filter(Boolean).join(', '));
    setValue(".s_phone", s.parentTel || '');
    setValue(".s_school", s.school || '');
    setValue(".s_grade", s.grade || '');
    setValue(".s_birth", formatDateKorean(s.birth));
    setValue(".s_address", s.address || '');
    setValue(".s_address_detail", s.addressDetail || '');

    // 입학일자
    const latestDate =
        new Date(s.entryHanDate) > new Date(s.entryBookDate)
            ? s.entryHanDate
            : s.entryBookDate;
    setValue(".s_entry", formatDateKorean(latestDate));

    // 상태 & 성별
    updateStatusButton(s.status);
    updateGenderButtons(s.gender);

    // 다음 체이닝을 위해 grade 값 리턴
    return s.grade;
}

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

function updateGenderButtons(gender) {
    const buttons = document.querySelectorAll(".s_gender");
    buttons.forEach(btn => btn.classList.remove("active"));

    if (gender === true || gender === 1 || gender === "TRUE" || gender === "1") {
        buttons[0].classList.add("active"); // 남자
    } else if (gender === false || gender === 0 || gender === "FALSE" || gender === "0") {
        buttons[1].classList.add("active"); // 여자
    } else {
        console.warn("Unknown gender value:", gender);
    }
}

// ====== [6] 학년 코드 불러오기 & 셀렉트 세팅 ======
async function loadGradeCodes(selectedGradeName) {
    const res = await fetch("/student/gradeCodes");
    const data = await res.json();
    const gradeSelect = document.querySelector('.grade-select');
    gradeSelect.innerHTML = "";

    if (data.success && Array.isArray(data.response)) {
        data.response.forEach(grade => {
            const option = document.createElement("option");
            option.value = grade.gradeKey;
            option.textContent = grade.gradeName;
            if (grade.gradeName === selectedGradeName) option.selected = true;
            gradeSelect.appendChild(option);
        });
    } else {
        console.error("학년 데이터 오류");
    }
}

// 상태 변경 로직

// document.addEventListener('DOMContentLoaded', function () {
//     const exceptCurrentContainer = document.querySelector('.status-buttons[data-visibility="except-current"]');
//     const reasonInputBox = document.querySelector('.reason-input');
//
//     if (!exceptCurrentContainer) return;
//
//     exceptCurrentContainer.addEventListener('click', function (e) {
//         const btn = e.target.closest('.s_status');
//         if (!btn || !exceptCurrentContainer.contains(btn)) return;
//
//         exceptCurrentContainer.querySelectorAll('.s_status').forEach(b => b.classList.remove('selected'));
//
//         btn.classList.add('selected');
//
//         if (reasonInputBox) {
//             reasonInputBox.classList.add('active');
//         }
//     });
// });

document.addEventListener('DOMContentLoaded', function () {
    const exceptCurrentContainer = document.querySelector('.status-buttons[data-visibility="except-current"]');
    const reasonInputBox = document.querySelector('.reason-input');
    const reasonField = reasonInputBox?.querySelector('input, textarea'); // 사유 입력 필드
    const submitBtn = document.getElementById('status-change'); // 상태 변경 버튼

    if (!exceptCurrentContainer) return;

    // ✅ 상태 버튼 클릭 시 선택
    exceptCurrentContainer.addEventListener('click', function (e) {
        const btn = e.target.closest('.s_status');
        if (!btn || !exceptCurrentContainer.contains(btn)) return;

        exceptCurrentContainer.querySelectorAll('.s_status').forEach(b => b.classList.remove('selected'));
        btn.classList.add('selected');

        if (reasonInputBox) {
            reasonInputBox.classList.add('active');
        }

        console.log('🟦 선택된 상태:', btn.dataset.status);
    });

    // ✅ 상태 변경 버튼 클릭 시 서버로 전송
    if (submitBtn) {
        submitBtn.addEventListener('click', function (e) {
            e.preventDefault();

            const selectedBtn = exceptCurrentContainer.querySelector('.s_status.selected');
            if (!selectedBtn) {
                alert('상태를 선택해주세요.');
                return;
            }

            const reason = reasonField?.value.trim();
            if (!reason) {
                alert('사유를 입력해주세요.');
                return;
            }

            if (!currentStudentId) {
                alert('학생 ID를 찾을 수 없습니다.');
                return;
            }

            const statusKey = selectedBtn.dataset.status;

            const requestBody = {
                studentId: currentStudentId,
                statusKey: statusKey,
                reason: reason
            };

            console.log('📦 전송 데이터:', requestBody);

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
                    console.log('✅ 서버 응답:', data);
                    alert('상태가 성공적으로 변경되었습니다.');

                    const s = data.response;
                    if (!s || !s.statusKey) {
                        return;
                    }

                    updateStatusButton(s.statusKey);

                    // 입력 초기화
                    reasonField.value = '';
                    exceptCurrentContainer.querySelectorAll('.s_status').forEach(b => b.classList.remove('selected'));
                    reasonInputBox.classList.remove('active');
                })
                .catch(err => {
                    console.error('🚨 요청 오류:', err);
                    alert('오류가 발생했습니다.');
                });
        });
    }
});


