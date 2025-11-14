// 알림 전송 및 데이터 전송할 학생 리스트
let selectedStudents = [];

// 데이터 필터링
document.addEventListener("DOMContentLoaded", () => {

    const monthInput = document.querySelector(".hidden-date");
    const monthDisplay = document.querySelector(".current-month");
    const monthBtn = document.querySelector(".calendar-open");
    const teacherSelect = document.getElementById("teacher-select");
    const classButtons = document.querySelectorAll(".class-btn");

    infantSelectAllCheckbox();

    const today = new Date();
    const yy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");

    monthInput.value = `${yy}-${mm}`;

    monthDisplay.textContent = `${yy}년 ${mm}월`;

    monthBtn.addEventListener("click", () => {
        monthInput.showPicker();
    });

    monthInput.addEventListener("change", () => {
        const [year, month] = monthInput.value.split("-");
        monthDisplay.textContent = `${year}년 ${Number(month)}월`;

        requestClassLabels(year, month, teacherSelect.value);

    });

    teacherSelect.addEventListener("change", (e) => {
        const teacher = e.target.value;

        const [year, month] = monthInput.value.split("-");
        if (!year || !month) return;

        requestClassLabels(year, month, teacher);

    });

    classButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            const classId = btn.dataset.classId;
            const time = btn.dataset.time;
            const label = {
                classKey: btn.dataset.classKey,
                unitKey: btn.dataset.unitKey,
                timeTableKey: btn.dataset.timeTableKey,
                classSubject: btn.dataset.classSubject
            };
            const [year, month] = monthInput.value.split("-");

            requestInfantDetail(label, year);
        });
    });


});

function infantSelectAllCheckbox() {

    const selectAll = document.querySelector("#infant-select-all");
    const checkboxes = document.querySelectorAll(".infant-row-checkbox");

    if (!selectAll || checkboxes.length === 0) return;

    // 전체 선택
    selectAll.addEventListener("change", () => {
        checkboxes.forEach(chk => chk.checked = selectAll.checked);

        updateSelectedStudents();
    });

    // 각각 선택
    checkboxes.forEach(chk => {
        chk.addEventListener("change", () => {

            const allChecked = [...checkboxes].every(c => c.checked);
            selectAll.checked = allChecked;

            updateSelectedStudents();
        });
    });

    function updateSelectedStudents() {
        selectedStudents = [...checkboxes]
            .filter(c => c.checked)
            .map(c => ({
                id: c.dataset.id,
                name: c.dataset.name,
                appToken: c.dataset.token
            }));

        console.log("📌 선택된 학생 목록:", selectedStudents);
    }
}

function requestClassLabels(yy, mm, teacher) {

    const requestBody = {
        yy: yy,
        mm: mm,
        userCode: teacher,
    };

    fetch(`/class/infant/labels`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(requestBody)
    })
        .then(res => res.json())
        .then(data => {
            const listEl = document.querySelector(".class-list");
            listEl.innerHTML = "";

            const labels = data.response;

            if (!labels || labels.length === 0) {
                listEl.innerHTML = `
                        <li style="text-align:center; padding:20px; color:#777;">
                            조회된 수업이 없습니다.
                        </li>`;

                renderStudents([]);
                renderIntroSection(null, null);
                return;
            }
            const arr = Object.values(labels).filter(v => typeof v === "object");

            arr.forEach((l, idx) => {
                const li = `
                            <li class="class-btn ${idx === 0 ? 'active' : ''}"
                                data-index="${idx}"
                                data-class-key="${l.classKey}"
                                data-unit-key="${l.unitKey}"
                                data-time-table-key="${l.timeTableKey}"
                                data-class-subject="${l.classSubject}">
                                <span>${l.classTime}</span><br>
                                <span>${l.classSubject}</span>
                            </li>
                        `;
                listEl.insertAdjacentHTML("beforeend", li);
            });

            bindClassClickEvents();
            const firstBtn = listEl.querySelector(".class-btn.active");
            if (firstBtn) {
                const label = {
                    classKey: firstBtn.dataset.classKey,
                    unitKey: firstBtn.dataset.unitKey,
                    timeTableKey: firstBtn.dataset.timeTableKey,
                    classSubject: firstBtn.dataset.classSubject
                };

                requestInfantDetail(label, yy);
            }
        })
        .catch(err => {
            console.error("❌ 클래스 라벨 조회 오류:", err);
        });
}

function bindClassClickEvents() {
    document.querySelectorAll(".class-btn").forEach(btn => {
        btn.addEventListener("click", () => {

            document.querySelectorAll(".class-btn").forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            const label = {
                classKey: btn.dataset.classKey,
                unitKey: btn.dataset.unitKey,
                timeTableKey: btn.dataset.timeTableKey,
                classSubject: btn.dataset.classSubject
            };
            const [yy, mm] = monthInput.value.split("-");
            requestInfantDetail(label, yy);

        });
    });
}

function requestInfantDetail(label, year) {

    const body = {
        classKey: label.classKey,
        unitKey: label.unitKey,
        timeTableKey: label.timeTableKey,
        classSubject: label.classSubject,
        yy: year
    };

    fetch(`/class/infant/details`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(body)
    })
        .then(res => res.json())
        .then(data => {

            const type = data.response.type;
            const detail = data.response.data;

            // 학생 영역
            renderStudents(detail.students);

            // 수업 내용
            renderIntroSection(type, detail);

        })
        .catch(err => console.error("❌ infant detail error", err));
}

function renderStudents(students) {
    const tbody = document.querySelector(".learn-table tbody");
    tbody.innerHTML = "";

    const selectAll = document.querySelector("#infant-select-all");
    if (selectAll) selectAll.checked = false;

    if (!students || students.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="3" style="text-align:center;">학생 데이터가 없습니다.</td>
            </tr>
        `;
        return;
    }

    students.forEach(s => {
        const row = `
            <tr>
                <td class="checkbox-group">
                    <input type="checkbox"
                        class="infant-row-checkbox"
                        data-id="${s.studentId}"
                        data-name="${s.studentName}"
                        data-token="${s.appToken}"/>
                </td>
                <td>${s.studentName}</td>
                <td class="send-ornot">
                    ${s.appToken == null
            ? `<img src="/image/no-smartphones.png">`
            : `<img src="/image/send1.png">`
        }
                </td>
            </tr>
        `;
        tbody.insertAdjacentHTML("beforeend", row);
    });

    infantSelectAllCheckbox();
}

function renderIntroSection(type, detail) {
    const container = document.querySelector(".third-frame");

    const existingBook = container.querySelector(".book-intro-lesson");
    const existingHan = container.querySelector(".han-intro-lesson");

    if (!type || !detail) {

        const emptyHTML = `
            <div class="intro-inner">
                <div class="no-class-msg" style="padding:25px; text-align:center; color:#777;">
                    조회된 수업이 없습니다.
                </div>
            </div>
        `;

        if (existingBook) existingBook.innerHTML = emptyHTML;
        if (existingHan) existingHan.innerHTML = emptyHTML;

        return;
    }

    existingBook?.remove();
    existingHan?.remove();

    const wrap = document.createElement("div");
    wrap.className = type === "BOOK" ? "book-intro-lesson" : "han-intro-lesson";

    const frag = document.createDocumentFragment();

    const topHTML = `
            <div class="intro-inner">
                <div class="${type === "BOOK" ? "book-intro-book" : "han-intro-book"}">
                ${type === "BOOK" ? `
                <!-- BOOK: book-name 먼저 -->
                    <div class="book-name">
                        <span class="book-kind">${detail.classLabel}</span><br>
                        <span>${detail.subject}</span>
                    </div>
                    <img src="https://sunandtree2.cafe24.com/report/bukiimg/J/${detail.imagePath}" alt="">
                `
        : `
                <!-- HAN: 이미지 먼저 -->
                    <img src="https://sunandtree2.cafe24.com/report/haniimg/rimg/${detail.imagePath}" alt="">
                    <div class="book-name">
                        <span class="book-kind">${detail.classLabel}</span><br>
                        <span>${detail.subject}</span>
                    </div>
                `
    }
                </div>
            `;

    const hanjaHTML =
        type === "BOOK"
            ? `
            <div class="hanja-list">
                <div class="hanja-row">
                    <span class="hanja-text">${detail.content}</span>
                </div>
            </div>
        `
            : `
            <div class="hanja-list">
                <div class="hanja-row">
                    <span class="hanja-label">신습한자</span>
                    <span class="hanja-text">${detail.newWord}</span>
                </div>
                <div class="hanja-row">
                    <span class="hanja-label">${detail.story}</span>
                    <span class="hanja-text">${detail.story}(${detail.subStory})</span>
                </div>
                <div class="hanja-row">
                    <span class="hanja-label">인성 한자성어</span>
                    <span class="hanja-text">${detail.idiom}(${detail.subIdiom})</span>
                </div>
            </div>
        `;

    function renderTags(tags) {
        return tags
            .map(
                (t) => `
                <div class="tag-row">
                    <span class="tag-box ${t.color}">${t.title}</span>
                    <div class="tag-desc">
                        <span>${t.text}</span>
                        <div class="hash-block">
                            ${t.hashtags
                    .map(
                        (h) =>
                            `<span class="hashtag ${h.color || ""}">${h.text}</span>`
                    )
                    .join("")}
                        </div>
                    </div>
                </div>
            `
            )
            .join("");
    }

    const bookTags = [
        {
            title: "동화이해 활동",
            color: "tag-blue",
            text: detail.story,
            hashtags: [
                {text: "#위인동화", color: "hash-blue"},
                {text: "#한글알기"},
            ],
        },
        {
            title: "지식탐구 활동",
            color: "tag-orange",
            text: detail.knowledgeBoard,
            hashtags: [
                {text: "#워크북"},
                {text: "#지식활동", color: "hash-orange"},
            ],
        },
        {
            title: "창의표현 활동",
            color: "tag-pink",
            text: detail.thinkTalk,
            hashtags: [
                {text: "#스토리동요"},
                {text: "#생각말하기", color: "hash-pink"},
                {text: "#공감독서"},
            ],
        },
        {
            title: "통합사고 활동",
            color: "tag-purple",
            text: detail.goldenbell,
            hashtags: [{text: "#골든벨", color: "hash-purple"}],
        },
        {
            title: "스마트놀이 활동",
            color: "tag-green",
            text: detail.findDiff,
            hashtags: [
                {text: "#다른그림찾기", color: "hash-green"},
                {text: "#동화꾸미기"},
                {text: "#그림맞추기"},
            ],
        },
    ];

    const hanTags = [
        {
            title: "의미표현 활동",
            color: "tag-blue",
            text: detail.hanjaSong,
            hashtags: [
                {text: "#워크북", color: "hash-blue"},
                {text: "#한글놀이터"},
            ],
        },
        {
            title: "어휘활용 활동",
            color: "tag-orange",
            text: detail.workBook,
            hashtags: [
                {text: "#자원송", color: "hash-orange"},
                {text: "#한자송"},
            ],
        },
        {
            title: "문장이해 활동",
            color: "tag-pink",
            text: detail.storyComment,
            hashtags: [
                {text: "#한자동화", color: "hash-pink"},
                {text: "#뜻을 알아요"},
            ],
        },
        {
            title: "창의놀이 활동",
            color: "tag-purple",
            text: detail.clean,
            hashtags: [
                {text: "#인성이야기", color: "hash-purple"},
                {text: "#바른약속"},
            ],
        },
        {
            title: "바른인성 활동",
            color: "tag-green",
            text: detail.insung,
            hashtags: [
                {text: "#쓱싹쓱싹", color: "hash-green"},
                {text: "#한자창조"},
            ],
        },
    ];

    const tagsHTML = `
        <div class="voca-tendency">
            <div class="tag-list">
                ${renderTags(type === "BOOK" ? bookTags : hanTags)}
            </div>
        </div>
    `;

    wrap.innerHTML = topHTML + hanjaHTML + tagsHTML + "</div>";
    frag.appendChild(wrap);
    container.appendChild(frag);
}

// 발행버튼 클릭
document.addEventListener("DOMContentLoaded", () => {
    const sendBtn = document.getElementById("send-infant-btn");

    if (!sendBtn) return;

    sendBtn.addEventListener("click", async () => {
        try {
            // 선택된 학생 확인
            if (!selectedStudents || selectedStudents.length === 0) {
                alert("학생을 선택하세요.");
                return;
            }

            const targets = selectedStudents.filter(s => s.appToken);

            if (targets.length === 0) {
                alert("앱 등록된 학생이 없습니다.");
                return;
            }

            const requestBody = {
                tokens: targets.map(s => s.appToken),
                title: "학습 내용",
                body: "내용이 입력되었습니다."
            };

            console.log("📨 전송 요청 Body:", requestBody);

            const sendRes = await fetch("/api/push/infant", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(requestBody)
            });

            const sendData = await sendRes.json();

            if (!sendRes.ok || sendData.status !== "success") {
                alert("알림 전송에 실패했습니다.");
                console.error("❌ 전송 실패:", sendData);
                return;
            }

            const saveRes = await fetch("/class/infant/save-send-history", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(requestBody)
            });

            const saveData = await saveRes.json();

            if (!saveRes.ok || saveData.status !== "success") {
                alert("알림은 전송되었으나 기록 저장에 실패했습니다.");
                console.error("❌ 기록 저장 실패:", saveData);
                return;
            }


            alert("알림이 전송되고 기록이 저장되었습니다.");

        } catch (err) {
            console.error("❌ 발행 처리 중 오류:", err);
            alert("처리 중 오류가 발생했습니다.");
        }
    });
});