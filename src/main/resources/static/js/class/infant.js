// 알림 전송 및 데이터 전송할 학생 리스트
let selectedStudents = [];

document.addEventListener("DOMContentLoaded", () => {

    const monthInput    = document.querySelector(".hidden-date");
    const monthDisplay  = document.querySelector(".current-month");
    const monthBtn      = document.querySelector(".calendar-open");
    const teacherSelect = document.getElementById("infant-teacher-select");
    const teacherValue  = document.getElementById("teacher-value");

    infantSelectAllCheckbox();

    const today = new Date();
    const yy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");
    monthInput.value = `${yy}-${mm}`;
    monthDisplay.textContent = `${yy}년 ${mm}월`;

    monthBtn.addEventListener("click", () => monthInput.showPicker());

    monthInput.addEventListener("change", () => {
        const [year, month] = monthInput.value.split("-");
        monthDisplay.textContent = `${year}년 ${Number(month)}월`;
        requestClassLabels(year, month, teacherValue.value);
    });

    if (teacherSelect) {
        teacherSelect.addEventListener("change", (e) => {
            teacherValue.value = e.target.value;
            const [year, month] = monthInput.value.split("-");
            if (!year || !month) return;
            requestClassLabels(year, month, teacherValue.value);
        });
    }

    document.querySelectorAll(".class-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            const [year] = monthInput.value.split("-");
            requestInfantDetail({
                classKey: btn.dataset.classKey,
                unitKey: btn.dataset.unitKey,
                timeTableKey: btn.dataset.timeTableKey,
                classSubject: btn.dataset.classSubject
            }, year);
        });
    });
});

// ✅ 해시태그 라디오 토글 + 텍스트 교체
document.addEventListener("click", (e) => {
    if (!e.target.classList.contains("hashtag")) return;

    const clicked = e.target;
    const hashBlock = clicked.closest(".hash-block");
    const tagDesc = clicked.closest(".tag-desc");
    const textSpan = tagDesc.querySelector("span:first-child");
    const colorClasses = ["hash-blue", "hash-orange", "hash-pink", "hash-purple", "hash-green"];

    // 원본 컬러 태그 + 텍스트 최초 1회 저장
    if (!hashBlock.dataset.originalTag) {
        const originalColored = hashBlock.querySelector(".hashtag[class*='hash-']");
        if (originalColored) {
            const foundColor = colorClasses.find(c => originalColored.classList.contains(c));
            hashBlock.dataset.colorClass = foundColor;
            hashBlock.dataset.originalTag = originalColored.innerText.trim();
        }
    }
    if (!tagDesc.dataset.originalText) {
        tagDesc.dataset.originalText = textSpan.innerText;
    }

    const colorClass = hashBlock.dataset.colorClass;
    const isAlreadyActive = clicked.classList.contains("active");

    // 같은 hash-block 내 전체 초기화
    hashBlock.querySelectorAll(".hashtag").forEach(tag => {
        tag.classList.remove("active", colorClass);
    });

    if (isAlreadyActive) {
        // 해제 → 원본 복원
        const originalTag = [...hashBlock.querySelectorAll(".hashtag")]
            .find(t => t.innerText.trim() === hashBlock.dataset.originalTag);
        if (originalTag) originalTag.classList.add(colorClass);
        textSpan.innerText = tagDesc.dataset.originalText;
    } else {
        // 선택 → 컬러 이동 + 텍스트 교체
        clicked.classList.add(colorClass, "active");
        const content = clicked.dataset.content;
        textSpan.innerText = (content !== undefined && content !== "")
            ? content
            : tagDesc.dataset.originalText;
    }
});

function infantSelectAllCheckbox() {
    const selectAll = document.querySelector("#infant-select-all");
    const checkboxes = document.querySelectorAll(".infant-row-checkbox");

    if (!selectAll || checkboxes.length === 0) return;

    checkboxes.forEach(cb => {
        if (!cb.dataset.token) {
            cb.disabled = true;
            cb.closest("tr").classList.add("disabled-row");
        }
    });

    const enabledCheckboxes = [...checkboxes].filter(cb => !cb.disabled);

    selectAll.addEventListener("change", () => {
        enabledCheckboxes.forEach(chk => (chk.checked = selectAll.checked));
        updateSelectedStudents();
    });

    enabledCheckboxes.forEach(chk => {
        chk.addEventListener("change", () => {
            selectAll.checked = enabledCheckboxes.every(c => c.checked);
            updateSelectedStudents();
        });
    });

    function updateSelectedStudents() {
        selectedStudents = enabledCheckboxes
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
    fetch(`/class/infant/labels`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({yy, mm, userCode: teacher})
    })
        .then(res => res.json())
        .then(data => {
            const listEl = document.querySelector(".class-list");
            listEl.innerHTML = "";

            const labels = data.response;
            if (!labels || labels.length === 0) {
                listEl.innerHTML = `<li style="text-align:center; padding:20px; color:#777;">조회된 수업이 없습니다.</li>`;
                renderStudents([]);
                renderIntroSection(null, null);
                return;
            }

            const arr = Object.values(labels).filter(v => typeof v === "object");
            arr.forEach((l, idx) => {
                listEl.insertAdjacentHTML("beforeend", `
                    <li class="class-btn ${idx === 0 ? 'active' : ''}"
                        data-index="${idx}"
                        data-class-key="${l.classKey}"
                        data-unit-key="${l.unitKey}"
                        data-time-table-key="${l.timeTableKey}"
                        data-class-subject="${l.classSubject}">
                        <span>${l.classTime}</span><br>
                        <span>${l.classSubject}</span>
                    </li>
                `);
            });

            bindClassClickEvents();

            const firstBtn = listEl.querySelector(".class-btn.active");
            if (firstBtn) {
                requestInfantDetail({
                    classKey: firstBtn.dataset.classKey,
                    unitKey: firstBtn.dataset.unitKey,
                    timeTableKey: firstBtn.dataset.timeTableKey,
                    classSubject: firstBtn.dataset.classSubject
                }, yy);
            }
        })
        .catch(err => console.error("❌ 클래스 라벨 조회 오류:", err));
}

function bindClassClickEvents() {
    document.querySelectorAll(".class-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            document.querySelectorAll(".class-btn").forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            const [yy] = document.querySelector(".hidden-date").value.split("-");
            requestInfantDetail({
                classKey: btn.dataset.classKey,
                unitKey: btn.dataset.unitKey,
                timeTableKey: btn.dataset.timeTableKey,
                classSubject: btn.dataset.classSubject
            }, yy);
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

    console.log("body = ", body);

    fetch(`/class/infant/details`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(body)
    })
        .then(res => res.json())
        .then(data => {
            const type = data.response.type;
            const detail = data.response.data;
            renderStudents(detail.students);
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
        tbody.innerHTML = `<tr><td colspan="3" style="text-align:center;">학생 데이터가 없습니다.</td></tr>`;
        return;
    }

    students.forEach(s => {
        const disabled = s.appToken == null ? "disabled" : "";
        const disabledRowClass = s.appToken == null ? "disabled-row" : "";

        let statusImg;
        if (s.appToken == null) {
            statusImg = `<img src="/image/no-smartphones.png" title="앱 미등록">`;
        } else if (s.isSend === true || s.isSend === 1) {
            statusImg = `<img src="/image/send2.png" title="발행완료">`;
        } else {
            statusImg = `<img src="/image/send1.png" title="미발행">`;
        }

        tbody.insertAdjacentHTML("beforeend", `
            <tr class="${disabledRowClass}">
                <td class="checkbox-group">
                    <input type="checkbox" class="infant-row-checkbox"
                        data-id="${s.studentId}"
                        data-name="${s.studentName}"
                        data-token="${s.appToken}"
                        ${disabled}/>
                </td>
                <td>${s.studentName}</td>
                <td class="send-ornot">${statusImg}</td>
            </tr>
        `);
    });

    infantSelectAllCheckbox();
}

function renderTags(tags) {
    return tags.map((t) => `
        <div class="tag-row">
            <span class="tag-box ${t.color}">${t.title}</span>
            <div class="tag-desc">
                <span>${t.text || ""}</span>
                <div class="hash-block">
                    ${t.hashtags.map((h) =>
        `<span class="hashtag ${h.color || ""}" data-content="${h.content || ""}">${h.text}</span>`
    ).join("")}
                </div>
            </div>
        </div>
    `).join("");
}

function renderIntroSection(type, detail) {
    const container = document.querySelector(".third-frame");
    const existingBook = container.querySelector(".book-intro-lesson");
    const existingHan = container.querySelector(".han-intro-lesson");

    if (!type || !detail) {
        const emptyHTML = `
            <div class="intro-inner">
                <div class="no-class-msg" style="padding:25px; text-align:center; color:#777;">
                    조회된 수업 내용이 없습니다.
                </div>
            </div>`;
        if (existingBook) existingBook.innerHTML = emptyHTML;
        if (existingHan) existingHan.innerHTML = emptyHTML;
        return;
    }

    existingBook?.remove();
    existingHan?.remove();

    const wrap = document.createElement("div");
    wrap.className = type === "BOOK" ? "book-intro-lesson" : "han-intro-lesson";

    const topHTML = `
        <div class="intro-inner">
            <div class="${type === "BOOK" ? "book-intro-book" : "han-intro-book"}">
            ${type === "BOOK" ? `
                <div class="book-name">
                    <span class="book-kind">${detail.classLabel}</span><br>
                    <span>${detail.subject}</span>
                </div>
                <img src="https://sunandtree2.cafe24.com/report/bukiimg/J/${detail.imagePath}" alt="">
            ` : `
                <img src="https://sunandtree2.cafe24.com/report/haniimg/rimg/${detail.imagePath}" alt="">
                <div class="book-name">
                    <span class="book-kind">${detail.classLabel}</span><br>
                    <span>${detail.subject}</span>
                </div>
            `}
            </div>
    `;

    const hanjaHTML = type === "BOOK"
        ? `
            <div class="hanja-list">
                <div class="hanja-row">
                    <span class="hanja-text">${detail.content}</span>
                </div>
            </div>`
        : `
            <div class="hanja-list">
                <div class="hanja-row">
                    <span class="hanja-label">신습한자</span>
                    <span class="hanja-text">${detail.newWord}</span>
                </div>
                <div class="hanja-row">
                    <span class="hanja-label">한자동화</span>
                    <span class="hanja-text">${detail.story}(${detail.subStory})</span>
                </div>
                <div class="hanja-row">
                    <span class="hanja-label">인성 한자성어</span>
                    <span class="hanja-text">${detail.idiom}(${detail.subIdiom})</span>
                </div>
            </div>`;

    // ✅ BOOK 해시태그 - DB 필드 정확히 매핑
    const bookTags = [
        {
            title: "동화이해 활동", color: "tag-blue", text: detail.story,
            hashtags: [
                {text: "#위인동화", color: "hash-blue", content: detail.story},
                {text: "#한글알기", content: detail.hangul},
            ],
        },
        {
            title: "지식탐구 활동", color: "tag-orange", text: detail.knowledgeBoard,
            hashtags: [
                {text: "#워크북", content: detail.workbook},
                {text: "#지식활동", color: "hash-orange", content: detail.knowledgeBoard},
            ],
        },
        {
            title: "창의표현 활동", color: "tag-pink", text: detail.thinkTalk,
            hashtags: [
                {text: "#스토리동요", content: detail.song},
                {text: "#생각말하기", color: "hash-pink", content: detail.thinkTalk},
                {text: "#공감독서", content: detail.empathy},
            ],
        },
        {
            title: "통합사고 활동", color: "tag-purple", text: detail.goldenbell,
            hashtags: [
                {text: "#골든벨", color: "hash-purple", content: detail.goldenbell},
            ],
        },
        {
            title: "스마트놀이 활동", color: "tag-green", text: detail.findDiff,
            hashtags: [
                {text: "#다른그림찾기", color: "hash-green", content: detail.findDiff},
                {text: "#동화꾸미기", content: detail.makeStory},
                {text: "#그림맞추기", content: detail.picMatch},
            ],
        },
    ];

    // ✅ HAN 해시태그 - DB 필드 정확히 매핑
    const hanTags = [
        {
            title: "의미표현 활동", color: "tag-blue", text: detail.hanjaSong,
            hashtags: [
                {text: "#워크북", color: "hash-blue", content: detail.workBook},
                {text: "#한글놀이터", content: detail.hangulPlayground},
            ],
        },
        {
            title: "어휘활용 활동", color: "tag-orange", text: detail.workBook,
            hashtags: [
                {text: "#자원송", color: "hash-orange", content: detail.resourceSong},
                {text: "#한자송", content: detail.hanjaSong},
            ],
        },
        {
            title: "문장이해 활동", color: "tag-pink", text: detail.storyComment,
            hashtags: [
                {text: "#한자동화", color: "hash-pink", content: detail.story},
                {text: "#뜻을 알아요", content: detail.storyComment},
            ],
        },
        {
            title: "창의놀이 활동", color: "tag-purple", text: detail.clean,
            hashtags: [
                {text: "#인성이야기", color: "hash-purple", content: detail.insung},
                {text: "#바른약속", content: detail.promise},
            ],
        },
        {
            title: "바른인성 활동", color: "tag-green", text: detail.insung,
            hashtags: [
                {text: "#쓱싹쓱싹", color: "hash-green", content: detail.clean},
                {text: "#한자창조", content: detail.note},
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

    const frag = document.createDocumentFragment();
    frag.appendChild(wrap);
    container.appendChild(frag);
}

function getLessonType() {
    if (document.querySelector(".han-intro-lesson")) return "HAN";
    if (document.querySelector(".book-intro-lesson")) return "BOOK";
    return null;
}

// 헬퍼 추가
function getTagsOrdered(wrap, rowIndex) {
    const row = wrap.querySelector(`.tag-row:nth-of-type(${rowIndex})`);
    if (!row) return [null, null, null];

    const colorClasses = ["hash-blue", "hash-orange", "hash-pink", "hash-purple", "hash-green"];
    const tags = [...row.querySelectorAll(".hashtag")];

    const hasActive = tags.some(t => t.classList.contains("active"));

    let active, inactive;

    if (hasActive) {
        active = tags.filter(t => t.classList.contains("active"));
        inactive = tags.filter(t => !t.classList.contains("active"));
    } else {
        active = tags.filter(t => colorClasses.some(c => t.classList.contains(c)));
        inactive = tags.filter(t => !colorClasses.some(c => t.classList.contains(c)));
    }

    const ordered = [...active, ...inactive];
    return [
        ordered[0] ? "c" + ordered[0].innerText.trim() : null,
        ordered[1] ? ordered[1].innerText.trim() : null,
        ordered[2] ? ordered[2].innerText.trim() : null,
    ];
}

function collectBookData() {
    const wrap = document.querySelector(".book-intro-lesson");
    if (!wrap) return null;
    const text = (sel) => wrap.querySelector(sel)?.innerText.trim() || "";
    const p1 = getTagsOrdered(wrap, 1);
    const p2 = getTagsOrdered(wrap, 2);
    const p3 = getTagsOrdered(wrap, 3);
    const p4 = getTagsOrdered(wrap, 4);
    const p5 = getTagsOrdered(wrap, 5);
    return {
        classLabel: text(".book-kind"),
        subject: text(".book-name span:nth-of-type(2)"),
        imagePath: wrap.querySelector(".book-intro-book img")?.src?.split("/").pop() || "",
        content: text(".hanja-row .hanja-text"),
        story: text(".tag-row:nth-of-type(1) .tag-desc span"),
        knowledgeBoard: text(".tag-row:nth-of-type(2) .tag-desc span"),
        thinkTalk: text(".tag-row:nth-of-type(3) .tag-desc span"),
        goldenbell: text(".tag-row:nth-of-type(4) .tag-desc span"),
        findDiff: text(".tag-row:nth-of-type(5) .tag-desc span"),
        part1Tag1: p1[0], part1Tag2: p1[1], part1Tag3: p1[2],
        part2Tag1: p2[0], part2Tag2: p2[1], part2Tag3: p2[2],
        part3Tag1: p3[0], part3Tag2: p3[1], part3Tag3: p3[2],
        part4Tag1: p4[0], part4Tag2: p4[1], part4Tag3: p4[2],
        part5Tag1: p5[0], part5Tag2: p5[1], part5Tag3: p5[2],
    };
}

function collectHanData() {
    const wrap = document.querySelector(".han-intro-lesson");
    if (!wrap) return null;
    const text = (sel) => wrap.querySelector(sel)?.innerText.trim() || "";
    const p1 = getTagsOrdered(wrap, 1);
    const p2 = getTagsOrdered(wrap, 2);
    const p3 = getTagsOrdered(wrap, 3);
    const p4 = getTagsOrdered(wrap, 4);
    const p5 = getTagsOrdered(wrap, 5);
    return {
        classLabel: text(".book-kind"),
        subject: text(".book-name span:nth-of-type(2)"),
        imagePath: wrap.querySelector(".han-intro-book img")?.src?.split("/").pop() || "",
        newWord: text(".hanja-row:nth-of-type(1) .hanja-text"),
        story: text(".hanja-row:nth-of-type(2) .hanja-label"),
        subStory: text(".hanja-row:nth-of-type(2) .hanja-text").replace(/.*\(|\)/g, ""),
        idiom: text(".hanja-row:nth-of-type(3) .hanja-text").split("(")[0].trim(),
        subIdiom: text(".hanja-row:nth-of-type(3) .hanja-text").replace(/.*\(|\)/g, ""),
        hanjaSong: text(".tag-row:nth-of-type(1) .tag-desc span"),
        workBook: text(".tag-row:nth-of-type(2) .tag-desc span"),
        storyComment: text(".tag-row:nth-of-type(3) .tag-desc span"),
        clean: text(".tag-row:nth-of-type(4) .tag-desc span"),
        insung: text(".tag-row:nth-of-type(5) .tag-desc span"),
        part1Tag1: p1[0], part1Tag2: p1[1], part1Tag3: p1[2],
        part2Tag1: p2[0], part2Tag2: p2[1], part2Tag3: p2[2],
        part3Tag1: p3[0], part3Tag2: p3[1], part3Tag3: p3[2],
        part4Tag1: p4[0], part4Tag2: p4[1], part4Tag3: p4[2],
        part5Tag1: p5[0], part5Tag2: p5[1], part5Tag3: p5[2],
    };
}

function collectLessonData() {
    const type = getLessonType();
    if (!type) return null;
    return {type, detail: type === "HAN" ? collectHanData() : collectBookData()};
}

function collectSelectedStudents() {
    return [...document.querySelectorAll(".infant-row-checkbox:checked")].map(cb => ({
        studentId: cb.dataset.id,
        studentName: cb.dataset.name,
        appToken: cb.dataset.token || null
    }));
}

async function saveInfantContent(isSend) {
    try {
        const lesson = collectLessonData();
        const students = collectSelectedStudents();
        if (!lesson) {
            alert("수업 정보가 없습니다.");
            return;
        }
        if (!students.length) {
            alert("학생을 선택하세요.");
            return;
        }

        const saveRes = await fetch("/class/infant/save", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                type: lesson.type,
                detail: lesson.detail,
                students,
                classKey: document.querySelector(".class-btn.active")?.dataset.classKey,
                unitKey: document.querySelector(".class-btn.active")?.dataset.unitKey,
                timeTableKey: document.querySelector(".class-btn.active")?.dataset.timeTableKey,
                yy: document.querySelector(".hidden-date").value.split("-")[0],
                mm: document.querySelector(".hidden-date").value.split("-")[1],
                isSend
            })
        });
        const saveData = await saveRes.json();
        return saveRes.ok && saveData.response === "success";
    } catch (err) {
        console.error("❌ 저장 오류:", err);
        return false;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const sendBtn = document.getElementById("send-infant-btn");
    if (!sendBtn) return;

    sendBtn.addEventListener("click", async () => {
        try {
            const lesson = collectLessonData();
            const students = collectSelectedStudents();
            if (!lesson) {
                alert("수업 정보가 없습니다.");
                return;
            }
            if (!students.length) {
                alert("학생을 선택하세요.");
                return;
            }
            if (!confirm(`${students.length}명의 학생에게 알림을 발송하시겠습니까?`)) return;

            const sendRequestBody = {
                students: selectedStudents.map(s => ({studentId: s.id, token: s.appToken})),
                classType: lesson.type,
                timeTableKey: document.querySelector(".class-btn.active")?.dataset.timeTableKey,
                title: "학습 내용",
                body: "내용이 입력되었습니다."
            };

            let sendSuccess = false;
            const appTargets = sendRequestBody.students.filter(s => s.token);

            if (appTargets.length > 0) {
                try {
                    const sendRes = await fetch("/api/push/infant", {
                        method: "POST",
                        headers: {"Content-Type": "application/json"},
                        body: JSON.stringify(sendRequestBody)
                    });
                    const sendData = await sendRes.json();
                    if (sendRes.ok && sendData.response === "success") sendSuccess = true;
                } catch (e) {
                    console.error("❌ SEND ERROR:", e);
                }
            }

            if (!sendSuccess) {
                alert("알림 전송에 실패했습니다.");
                return;
            }

            const saveSuccess = await saveInfantContent(true);
            if (saveSuccess) {
                alert("알림이 정상적으로 전송되었습니다.");
                updateStudentImages();
            } else {
                alert("알림은 전송되었으나 저장에 실패했습니다.");
            }
        } catch (err) {
            console.error("❌ 전체 오류:", err);
            alert("오류가 발생했습니다.");
        }
    });
});

function updateStudentImages() {
    document.querySelectorAll(".infant-row-checkbox:checked").forEach(checkbox => {
        checkbox.dataset.isSend = "1";
        const imgCell = checkbox.closest("tr").querySelector(".send-ornot");
        if (imgCell) imgCell.innerHTML = `<img src="/image/send2.png" title="발행완료">`;
    });
    console.log("✅ 학생 이미지 업데이트 완료");
}

document.addEventListener("DOMContentLoaded", () => {
    const saveBtn = document.getElementById("save-infant-btn");
    if (!saveBtn) return;

    saveBtn.addEventListener("click", async () => {
        try {
            const lesson = collectLessonData();
            const students = collectSelectedStudents();
            if (!lesson) {
                alert("수업 정보가 없습니다.");
                return;
            }
            if (!students.length) {
                alert("학생을 선택하세요.");
                return;
            }
            if (!confirm("내용을 저장하시겠습니까?")) return;

            const saveSuccess = await saveInfantContent(false);
            alert(saveSuccess ? "저장되었습니다." : "저장에 실패했습니다.");
        } catch (err) {
            console.error("❌ 저장 오류:", err);
            alert("오류가 발생했습니다.");
        }
    });
});