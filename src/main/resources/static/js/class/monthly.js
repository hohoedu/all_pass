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
                console.log("서버 응답:", data);
                renderMonthlyClassList(data.response);

                if (data.response && data.response.length > 0) {
                    const timeTableKey = data.response[0].timeTableKey ?? "";
                    if (timeTableKey) {
                        fetch("/class/api/monthly/timeTableKey", {
                            method: "POST",
                            headers: {"Content-Type": "application/json"},
                            body: JSON.stringify({timeTableKey})
                        })
                            .then(res => res.json())
                            .then(data => renderMonthlyStudentList(data.response))
                            .catch(err => console.error(err));
                    }
                }
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
                // 필요하면 active 클래스 토글
                classList.querySelectorAll("li").forEach(li => li.classList.remove("active"));
                target.classList.add("active");
            }
        });

        li.dataset.classId = cls.classCode;
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


        const scores = (stu.scores && stu.scores.length > 0) ? stu.scores[0] : {};

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
        <textarea class="comment-text" placeholder="내용을 입력해주세요.">
${stu.studentName} 학생은 어휘의 정의를 정확히 이해하고, 유사 단어 사이에서도 핵심 의미를 잘 구분 했습니다. 
다만, 비슷한 자형의 한자들이 함께 제시될 때는 의미를 중심으로 구별하는 연습이 더 필요합니다.
        </textarea>
      </div>
    </td>
    <td>
      <div class="cell-middle">
        <div class="after-comment">
          <textarea class="comment-text" placeholder="내용을 입력해주세요."></textarea>
        </div>
      </div>
    </td>
    <td class="send-ornot"><img src="/image/send2.png" alt=""></td>
    <td class="pre-search"><img src="/image/pre-search.png" alt=""></td>
  `;

        tbody.appendChild(tr);
    });
}

document.addEventListener("DOMContentLoaded", () => {
    const tbody = document.getElementById("monthly_student_tbody");
    if (!tbody) return;

    tbody.addEventListener("click", (e) => {
        const target = e.target;

        if (target.classList.contains("btn-number")) {
            target.classList.toggle("active");

            if (target.classList.contains("active")) {
                target.value = "true";
            } else {
                target.value = "false";
            }

            console.log(`${target.textContent} 상태:`, target.value);
        }

        if (target.classList.contains("btn-reset")) {
            const row = target.closest("tr");
            if (!row) return;

            row.querySelectorAll(".btn-number").forEach(btn => {
                btn.classList.remove("active");
                btn.value = "false";
            });
        }
    });
});

document.addEventListener("DOMContentLoaded", () => {
    const tbody = document.getElementById("monthly_student_tbody");
    if (!tbody) return;

    tbody.addEventListener("click", (e) => {
        const target = e.target;

        if (target.classList.contains("btn-result")) {
            const row = target.closest("tr");
            const studentId = row.dataset.studentId;
            const classCode = row.dataset.classCode;
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
                classCode: classCode,
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
                    console.log("✅ 서버 응답:", data);
                })
                .catch(err => {
                    console.error("❌ 오류:", err);
                });
        }
    });
});

// 모달 오픈
document.addEventListener("DOMContentLoaded", function () {
    const preSearchBtns = document.querySelectorAll(".pre-search img");
    const modal = document.querySelector(".pre-modal");
    if (preSearchBtns.length === 0 || !modal) {
        return;
    }
    const closeBtn = modal.querySelector(".btn-close");

    // 열기 (모든 버튼에 대해 이벤트 등록)
    preSearchBtns.forEach((btn) => {
        btn.addEventListener("click", function () {
            modal.style.display = "block";
        });
    });

    // 닫기
    closeBtn.addEventListener("click", function (e) {
        e.preventDefault();
        modal.style.display = "none";
    });

    // 모달 바깥 클릭 시 닫기
    window.addEventListener("click", function (e) {
        if (e.target === modal) {
            modal.style.display = "none";
        }
    });
});
