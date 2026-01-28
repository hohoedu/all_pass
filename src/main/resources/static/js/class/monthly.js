// =================================== //
// ==                               == //
// ==        월간평가(초등)         == //
// ==                               == //
// =================================== //

document.addEventListener("DOMContentLoaded", () => {
    const monthInput = document.getElementById("monthly_calendar");
    const monthDisplay = document.getElementById("monthly_current");
    const teacherSelect = document.getElementById("monthly-teacher-select");
    const calendarBtn = document.querySelector(".calendar-open");
    const dayBtns = document.querySelectorAll(".day-btn");

    if (!monthInput || !teacherSelect) return;

    // 오늘 날짜 기본값 세팅
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");
    monthInput.value = `${yyyy}-${mm}`;
    monthDisplay.textContent = `${yyyy}년 ${mm}월`;

    // 현재 선택된 요일 가져오기
    function getCurrentDayName() {
        const activeBtn = document.querySelector(".day-btn.active");
        return activeBtn ? activeBtn.dataset.week : "mon"; // 기본값 월요일
    }

    // 공통 데이터 로딩 함수
    function loadMonthlyData() {
        const [yy, mm] = monthInput.value.split("-");
        const userCode = teacherSelect.value;
        const dayname = getCurrentDayName();

        const requestBody = {yy, mm, userCode, dayname};

        fetch("/class/api/monthly/classes", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(requestBody)

        })
            .then(res => res.json())
            .then(data => {

                // 수업 리스트 랜더링
                renderMonthlyClassList(data.response);

                if (!data.response || data.response.length === 0) {
                    // 학생 리스트 랜더링
                    renderMonthlyStudentList([]);
                    return;
                }

                // 수업이 있을 때만 첫 번째 시간표로 학생 목록 조회
                const timeTableKey = data.response[0].timeTableKey;
                fetch("/class/api/monthly/timeTableKey", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({timeTableKey})
                })
                    .then(res => res.json())
                    .then(data => renderMonthlyStudentList(data.response))
                    .catch(err => console.error(err));

            })
            .catch(err => console.error("조회 에러:", err));
    }

    // 달력
    calendarBtn.addEventListener("click", () => {
        monthInput.showPicker?.();
        monthInput.click();
    });

    // 월 변경
    monthInput.addEventListener("change", () => {
        if (monthInput.value) {
            const [year, month] = monthInput.value.split("-");
            monthDisplay.textContent = `${year}년 ${month}월`;
            loadMonthlyData();
        }
    });

    // 선생님 변경
    teacherSelect.addEventListener("change", () => {
        loadMonthlyData();
    });

    // 요일 버튼 변경
    dayBtns.forEach(btn => {
        btn.addEventListener("click", () => {

            dayBtns.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            loadMonthlyData();
        });
    });
});

// 수업 시간표 변경 시 데이터 변경
document.addEventListener("DOMContentLoaded", () => {
    const classList = document.getElementById("monthly-class-list");
    if (!classList) {
        return;
    }
    classList.addEventListener("click", (e) => {
        const target = e.target.closest("li");
        if (!target) return;

        document.querySelectorAll("#monthly-class-list li").forEach(li => li.classList.remove("active"));
        target.classList.add("active");

        const timeTableKey = target.dataset.classId;

        // fetch 요청
        const requestBody = {timeTableKey: timeTableKey};
        fetch("/class/api/monthly/timeTableKey", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(requestBody)
        })
            .then(res => res.json())
            .then(data => {
                console.log("응답 데이터:", data);
                renderMonthlyStudentList(data.response);
            })
            .catch(err => console.error("에러:", err));
    });

});

// 수업 시간표 변경 함수
function renderMonthlyClassList(classes) {
    const classList = document.querySelector("#monthly-class-list");
    classList.innerHTML = "";

    classes.forEach((cls, idx) => {
        const li = document.createElement("li");
        li.className = "class-btn" + (idx === 0 ? " active" : "");

        classList.addEventListener("click", (e) => {
            const target = e.target.closest("li.class-btn");
            if (target) {
                console.log("Clicked:", target.dataset.classId);
                classList.querySelectorAll("li").forEach(li => li.classList.remove("active"));
                target.classList.add("active");
            }
        });

        li.dataset.classId = cls.timeTableKey;
        li.dataset.time = cls.classTime;
        li.dataset.subject = cls.classSubject;

        const spanTime = document.createElement("span");
        spanTime.textContent = cls.classTime;

        const br = document.createElement("br");

        const spanSubject = document.createElement("span");
        spanSubject.textContent = cls.classSubject;

        li.appendChild(spanTime);
        li.appendChild(br);
        li.appendChild(spanSubject);

        classList.appendChild(li);
    });
}

// 본문 변경
function renderMonthlyStudentList(students) {
    const tbody = document.querySelector("#monthly_student_tbody");
    tbody.innerHTML = ""; // 기존 행 초기화

    if (!students || students.length === 0) {
        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = 8;
        td.textContent = "등록된 학생이 없습니다.";
        td.style.textAlign = "center";
        td.style.color = "#666";
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }

    students.forEach((stu, idx) => {

        const tr = document.createElement("tr");
        tr.dataset.studentId = stu.studentId ?? "";
        tr.dataset.timeTableKey = stu.timeTableKey ?? "";
        tr.dataset.appToken = stu.appToken ?? "";
        const scores = (stu.scores && stu.scores.length > 0) ? stu.scores[0] : {};
        console.log(stu.send);
        let statusImg;
        if (stu.appToken == null || stu.appToken === '') {
            // 1. 앱 미등록
            statusImg = `<img src="/image/no-smartphones.png" title="앱 미등록">`;
        } else if (stu.send === true || stu.send === 1) {
            // 2. 발행됨
            statusImg = `<img src="/image/send2.png" title="발행완료">`;
        } else {
            // 3. 미발행 (저장만 함 or 아직 저장 안함)
            statusImg = `<img src="/image/send1.png" title="미발행">`;
        }

        const feedbackText = (stu.feedback == null)
            ? "점수를 선택하고 결과보기를 눌러주세요"
            : `${stu.studentName} 학생은 ${stu.feedback}`;

        const bottomCommentText = (stu.bottomComment == null)
            ? "점수를 선택하고 결과보기를 눌러주세요"
            : stu.bottomComment;

        tr.innerHTML = `
            <td class="checkbox-group"><input type="checkbox" /></td>
            <td>${idx + 1}</td>
            <td>${stu.studentName}</td>
            <td>
                <div class="evaluation">
                    <div class="number-grid">
                        ${Array.from({length: 8}, (_, i) => {
            const qKey = "question" + (i + 1);
            const isActive = scores[qKey] === true ? "active" : "";
            const value = scores[qKey] === true ? "true" : "false";
            return `<button class="btn-number ${isActive}" value="${value}">${i + 1}번</button>`;
        }).join("")}
                    </div>
                    <div class="action-buttons">
                        <button class="btn-reset">초기화</button>
                        <button class="btn-result" id="monthly-result-btn">결과보기</button>
                    </div>
                </div>
            </td>
            <td class="cell-middle">
                <div class="after-comment">
                    <textarea class="comment-text" readonly placeholder="내용을 입력해주세요.">${feedbackText}</textarea>
                </div>
            </td>
            <td>
                <div class="cell-middle">
                    <div class="after-comment">
                        <textarea class="comment-text bottom-comment" placeholder="내용을 입력해주세요.">${bottomCommentText}</textarea>
                    </div>
                </div>
            </td>
            <td class="send-ornot">
                ${statusImg}
            </td>
            <td class="pre-search"><img src="/image/pre-search.png" alt=""></td>
        `;

        tbody.appendChild(tr);
    });
}

document.addEventListener("DOMContentLoaded", () => {
    const theadCheckbox = document.querySelector("thead .checkbox-group input[type='checkbox']");
    const tbody = document.getElementById("monthly_student_tbody");

    if (!theadCheckbox || !tbody) return;

    theadCheckbox.addEventListener("change", () => {
        const checkboxes = tbody.querySelectorAll("input[type='checkbox']");
        checkboxes.forEach(checkbox => {
            checkbox.checked = theadCheckbox.checked;
        });
    });

    tbody.addEventListener("change", (e) => {
        if (e.target.type === "checkbox") {
            const allCheckboxes = tbody.querySelectorAll("input[type='checkbox']");
            const checkedCount = tbody.querySelectorAll("input[type='checkbox']:checked").length;

            theadCheckbox.checked = (checkedCount === allCheckboxes.length && allCheckboxes.length > 0);
        }
    });
});

// 버튼 클릭 이벤트
document.addEventListener("DOMContentLoaded", () => {
    const tbody = document.getElementById("monthly_student_tbody");
    if (!tbody) return;

    tbody.addEventListener("click", (e) => {
        const target = e.target;

        // 번호 버튼 토글
        if (target.classList.contains("btn-number")) {
            target.classList.toggle("active");

            if (target.classList.contains("active")) {
                target.value = "true";
            } else {
                target.value = "false";
            }

            console.log(`${target.textContent} 상태:`, target.value);
        }

        // 초기화 버튼
        if (target.classList.contains("btn-reset")) {
            const row = target.closest("tr");
            if (!row) return;

            row.querySelectorAll(".btn-number").forEach(btn => {
                btn.classList.add("active");
                btn.value = "true";
            });
        }
    });
});


// 월간 평가 점수 결과보기 클릭
document.addEventListener("DOMContentLoaded", () => {
    const tbody = document.getElementById("monthly_student_tbody");
    if (!tbody) return;

    tbody.addEventListener("click", (e) => {
        const target = e.target;

        if (target.classList.contains("btn-result")) {
            const row = target.closest("tr");
            const studentId = row.dataset.studentId;
            const timeTableKey = row.dataset.timeTableKey;
            const monthValue = document.getElementById("monthly_calendar").value;
            let yy, mm;

            if (monthValue) {
                [yy, mm] = monthValue.split("-");
            } else {
                const today = new Date();
                yy = today.getFullYear().toString();
                mm = String(today.getMonth() + 1).padStart(2, "0");
            }
            const scores = {};
            row.querySelectorAll(".btn-number").forEach((btn, idx) => {
                const qKey = "question" + (idx + 1);
                scores[qKey] = (btn.value === "true");
            });


            const requestBody = {
                studentId: studentId,
                timeTableKey: timeTableKey,
                scores: [scores],
                yy: yy,
                mm: mm
            };

            fetch("/class/api/monthly/update_score", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify(requestBody)
            })
                .then(res => res.json())
                .then(data => {
                    console.log("✅ 서버 응답:", data.response);
                    const newFeedback = data.response?.scoreResult
                        ? `${data.response.studentName} 학생은 ${data.response.scoreResult}`
                        : "점수를 선택하고 결과보기를 눌러주세요";
                    const newBottomComment = data.response?.bottomComment ? `${data.response.bottomComment}`
                        : "점수를 선택하고 결과보기를 눌러주세요";
                    updateFeedbackOnly(row, newFeedback, newBottomComment);

                })
                .catch(err => {
                    console.error("❌ 오류:", err);
                });
        }
    });
});

// 월간평가 총평 변경
function updateFeedbackOnly(row, feedbackText, comment) {
    const feedbackTextarea = row.querySelector("td:nth-child(5) .comment-text");
    const commentTextarea = row.querySelector("td:nth-child(6) .comment-text");

    if (feedbackTextarea) {
        feedbackTextarea.value = feedbackText ?? "점수를 선택하고 결과보기를 눌러주세요";
    }
    if (commentTextarea) {
        commentTextarea.value = comment ?? "점수를 선택하고 결과보기를 눌러주세요";
    }
}

// 모달 오픈
document.addEventListener("DOMContentLoaded", function () {
    const modal = document.querySelector(".pre-modal");
    const closeBtn = modal.querySelector(".btn-close");

    document.body.addEventListener("click", function (e) {
        const btn = e.target.closest(".pre-search img");
        if (!btn) return;

        const row = btn.closest("tr");
        const studentId = row.dataset.studentId;
        const timeTableKey = row.dataset.timeTableKey;

        openPreviewModal(studentId, timeTableKey);
    });

    closeBtn.addEventListener("click", function (e) {
        e.preventDefault();
        modal.style.display = "none";
    });

    window.addEventListener("click", function (e) {
        if (e.target === modal) {
            modal.style.display = "none";
        }
    });
});

function openPreviewModal(studentId, timeTableKey) {
    const modal = document.querySelector(".pre-modal");

    const monthValue = document.getElementById("monthly_calendar").value;
    let yy, mm;

    if (monthValue) {
        [yy, mm] = monthValue.split("-");
    } else {
        const today = new Date();
        yy = today.getFullYear().toString();
        mm = String(today.getMonth() + 1).padStart(2, '0');
    }

    const requestBody = {studentId, timeTableKey, yy, mm};

    fetch("/class/api/monthly/preview", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(requestBody)
    })
        .then(res => res.json())
        .then(data => {
            console.log(data);
            console.log("📌 미리보기 데이터:", data.response);
            renderPreviewModal(data.response);
            modal.style.display = "block";
        })
        .catch(err => {
            console.error("❌ 미리보기 불러오기 오류:", err);
        });
}

function renderPreviewModal(data) {

    const title = document.querySelector(".pre-title");
    if (title) {
        title.textContent = `${data.studentName} 학생의 월간평가 미리보기`;
    }

    const topCommentEl = document.querySelector(".pre-sub-t");
    topCommentEl.innerHTML = "";

    const converted = convertStrong(data.topComment);

    // 문장을 <li> 단위로 자르고 strong 적용
    converted.split("\n").forEach(line => {
        if (line.trim() !== "") {
            const li = document.createElement("li");
            li.innerHTML = line;
            topCommentEl.appendChild(li);
        }
    });

    const tbody = document.querySelector(".pre-table tbody");
    if (tbody) {
        tbody.innerHTML = "";

        for (let i = 0; i < 8; i++) {
            const tr = document.createElement("tr");

            const num = i + 1;
            const comp = data.competency?.[i] ?? "-";
            const diff = data.difficultly?.[i] ?? "-";
            const score = data.scores?.[i] === "true" ? "○" : "X";

            tr.innerHTML = `
                <td>${num}</td>
                <td>${comp}</td>
                <td>${diff}</td>
                <td>${score}</td>
            `;

            tbody.appendChild(tr);
        }
    }

    const feedbackP = document.querySelector(".overall-top p");
    if (feedbackP) {
        const name = data.studentName ?? "";
        const fb = data.feedback ?? "총평을 불러올 수 없습니다.";

        // 학생명 + 공백 처리
        feedbackP.textContent = `${name} 학생은 ${fb}`;
    }

    const bottomDiv = document.querySelector(".overall-bottom");
    if (bottomDiv) {
        let bottomP = bottomDiv.querySelector("p");
        if (!bottomP) {
            bottomP = document.createElement("p");
            bottomDiv.appendChild(bottomP);
        }
        bottomP.textContent =
            data.bottomComment ?? "코멘트를 불러올 수 없습니다.";
    }
}


// FCM 발송
document.addEventListener("DOMContentLoaded", () => {
    const sendBtn = document.getElementById("monthly-send-btn");

    if (!sendBtn) return;

    sendBtn.addEventListener("click", () => {
        const checkedRows = [];
        const tokens = [];
        const checkboxes = document.querySelectorAll("#monthly_student_tbody input[type='checkbox']:checked");

        if (checkboxes.length === 0) {
            alert("발송할 학생을 선택해주세요.");
            return;
        }

        const monthValue = document.getElementById("monthly_calendar").value;
        let yy, mm;

        if (monthValue) {
            [yy, mm] = monthValue.split("-");
        } else {
            const today = new Date();
            yy = today.getFullYear().toString();
            mm = String(today.getMonth() + 1).padStart(2, "0");
        }

        checkboxes.forEach(checkbox => {
            const row = checkbox.closest("tr");
            if (row && row.dataset.studentId) {
                const appToken = row.dataset.appToken;

                if (appToken && appToken !== "null" && appToken !== "") {
                    tokens.push(appToken);
                    checkedRows.push({
                        studentId: row.dataset.studentId,
                        studentName: row.querySelector("td:nth-child(3)").textContent,
                        timeTableKey: row.dataset.timeTableKey
                    });
                }
            }
        });

        if (tokens.length === 0) {
            alert("발송할 학부모가 없습니다. (FCM 토큰 없음)");
            return;
        }

        sendMonthlyFCM(tokens, checkedRows, yy, mm);
    });
});

// FCM 발송 함수
function sendMonthlyFCM(tokens, students, yy, mm) {

    const requestBody = {
        tokens: tokens,
        title: "월간평가 발송",
        body: `${yy}년 ${mm}월 월간평가가 발송되었습니다.`,
        students: students,  // 추가
        yy: yy,              // 추가
        mm: mm               // 추가
    };

    fetch("/api/push/monthly", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Accept": "application/json"
        },
        body: JSON.stringify(requestBody)
    })
        .then(res => res.json())
        .then(data => {
            console.log("✅ FCM 발송 성공:", data);

            alert(`${students.length}명의 학생에게 월간평가가 발송되었습니다.`);

            students.forEach(stu => {
                const row = document.querySelector(`tr[data-student-id="${stu.studentId}"]`);
                if (row) {
                    const sendImg = row.querySelector(".send-ornot img");
                    if (sendImg) {
                        sendImg.src = "/image/send2.png";
                    }
                }
            });

            document.querySelectorAll("#monthly_student_tbody input[type='checkbox']").forEach(cb => {
                cb.checked = false;
            });

            // thead 체크박스도 해제
            const theadCheckbox = document.querySelector("thead .checkbox-group input[type='checkbox']");
            if (theadCheckbox) {
                theadCheckbox.checked = false;
            }
        })
        .catch(err => {
            console.error("❌ FCM 발송 오류:", err);
            alert("발송 중 오류가 발생했습니다.");
        });
}

document.addEventListener("DOMContentLoaded", () => {
    const saveBtn = document.getElementById("monthly-save-btn");

    if (!saveBtn) return;

    saveBtn.addEventListener("click", () => {
        const checkedRows = [];
        const checkboxes = document.querySelectorAll("#monthly_student_tbody input[type='checkbox']:checked");

        if (checkboxes.length === 0) {
            alert("저장할 학생을 선택해주세요.");
            return;
        }

        const monthValue = document.getElementById("monthly_calendar").value;
        let yy, mm;

        if (monthValue) {
            [yy, mm] = monthValue.split("-");
        } else {
            const today = new Date();
            yy = today.getFullYear().toString();
            mm = String(today.getMonth() + 1).padStart(2, "0");
        }

        checkboxes.forEach(checkbox => {
            const row = checkbox.closest("tr");
            if (row && row.dataset.studentId) {
                const bottomComment = row.querySelector(".bottom-comment").value;

                checkedRows.push({
                    studentId: row.dataset.studentId,
                    timeTableKey: row.dataset.timeTableKey,
                    bottomComment: bottomComment,
                    yy: yy,
                    mm: mm
                });
            }
        });

        saveMonthlyData(checkedRows);
    });
});

// 저장 함수
function saveMonthlyData(students) {
    const requestBody = {
        students: students
    };

    fetch("/class/api/monthly/save", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Accept": "application/json"
        },
        body: JSON.stringify(requestBody)
    })
        .then(res => res.json())
        .then(data => {
            console.log("✅ 저장 성공:", data);
            alert(`${students.length}명의 코멘트가 저장되었습니다.`);

            // 체크박스 해제
            document.querySelectorAll("#monthly_student_tbody input[type='checkbox']").forEach(cb => {
                cb.checked = false;
            });

            // thead 체크박스도 해제
            const theadCheckbox = document.querySelector("thead .checkbox-group input[type='checkbox']");
            if (theadCheckbox) {
                theadCheckbox.checked = false;
            }
        })
        .catch(err => {
            console.error("❌ 저장 오류:", err);
            alert("저장 중 오류가 발생했습니다.");
        });
}


// FCM 발송 함수
