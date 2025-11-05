// 공지사항 등록 모달 열기 (등록 / 수정)
document.addEventListener("DOMContentLoaded", () => {
    const modal = document.querySelector(".notice-modal");
    const openCreateBtn = document.querySelector(".notice-regist");
    const openEditBtn = document.querySelector(".notice-update");
    const closeBtn = modal.querySelector(".btn-close");
    const registBtn = modal.querySelector(".regist-btn");
    const titleElem = modal.querySelector(".pre-title");

    let mode = "create"; // 기본: 등록 모드

    // 모달 열기
    const openModal = (type, data) => {
        mode = type;
        modal.style.display = "flex";
        document.body.classList.add("modal-open");

        if (mode === "create") {
            titleElem.textContent = "공지 등록하기";
            registBtn.textContent = "등록";
            clearForm();
        } else if (mode === "edit") {
            titleElem.textContent = "공지 수정하기";
            registBtn.textContent = "수정";
            fillForm(data); // 기존 공지 데이터 채워넣기
        }
    };

    // 등록 모드
    openCreateBtn.addEventListener("click", () => openModal("create"));

    // 수정 모드 (예: 기존 데이터 전달)
    openEditBtn.addEventListener("click", () => {
        const dummyData = {
            title: "호호데이 참석하고 선물 받아가자!",
            subtitle: "4월 7일 호호데이 이벤트 안내",
            content: "호호데이가 4월 7일에 돌아옵니다!",
        };
        openModal("edit", dummyData);
    });

    function clearForm() {
        modal.querySelectorAll("input[type='text'], textarea").forEach((el) => (el.value = ""));
    }

    function fillForm(data) {
        const inputs = modal.querySelectorAll("input[type='text']");
        inputs[0].value = data.title || "";
        inputs[1].value = data.subtitle || "";
        modal.querySelector("textarea").value = data.content || "";
    }

    // 닫기
    closeBtn.addEventListener("click", () => {
        modal.style.display = "none";
        document.body.classList.remove("modal-open");
    });
});

// 공지사항 등록
document.addEventListener("DOMContentLoaded", () => {
    const select = document.getElementById("noticeSelect");
    const selected = select.querySelector(".selected");
    const options = select.querySelectorAll(".select-options li");

    selected.onclick = () => select.classList.toggle("open");

    options.forEach((li, index) => {
        li.onclick = () => {
            const img = li.querySelector("img").getAttribute("src");
            const text = li.textContent.trim();

            selected.innerHTML = `
        <img src="${img}" alt=""> ${text}
        <span class="arrow"><i class="xi-angle-down"></i></span>`;

            selected.dataset.value = index + 1;
            selected.dataset.src = img;

            select.classList.remove("open");
            console.log("선택된 아이콘 번호:", selected.dataset.value);
        };
    });

    // 공지사항 등록하기
    document.addEventListener("click", (e) => {
        if (!select.contains(e.target)) select.classList.remove("open");
    });

    $('#content').summernote({
        height: 300,
        lang: 'ko-KR',
        placeholder: '공지 내용을 입력해주세요.',
        toolbar: [
            ['style', ['bold', 'italic', 'underline', 'clear']],
            ['font', ['fontsize', 'color']],
            ['para', ['ul', 'ol', 'paragraph']],
            ['insert', ['customLink']]
        ],
        buttons: {
            customLink: function (context) {
                const ui = $.summernote.ui;
                return ui.button({
                    contents: '<i class="note-icon-link"></i>',
                    tooltip: '링크 직접 추가',
                    click: function () {
                        const url = prompt('이동할 링크를 입력하세요:', 'https://');
                        if (url) {
                            context.invoke('editor.createLink', {
                                text: url,
                                url: url,
                                isNewWindow: true
                            });
                        }
                    }
                }).render();
            }
        }
    });

    const saveBtn = document.querySelector("#save-notice");

    saveBtn.addEventListener("click", async () => {
        const title = document.querySelector('input[placeholder="제목을 입력해주세요."]').value.trim();
        const subTitle = document.querySelector('input[placeholder="부제목을 입력해주세요."]').value.trim();

        const icon = document.querySelector('#noticeSelect .selected').dataset.value

        const content = $('#content').summernote('code'); // HTML 형태의 내용

        if (!title || !subTitle || !content) {
            alert("제목, 부제목, 내용을 모두 입력해주세요.");
            return;
        }

        const noticeData = {
            title,
            subTitle,
            icon,
            content,
        };

        try {
            const response = await fetch("/notice/center/save", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(noticeData),
            });

            if (!response.ok) throw new Error("서버 응답 오류");

            const result = await response.json();
            if (result.success) {
                alert("공지사항이 성공적으로 등록되었습니다!");
                location.reload();
            } else {
                alert("공지 저장 실패: " + result.message);
            }
        } catch (err) {
            console.error("❌ 저장 중 오류:", err);
            alert("공지 저장 중 오류가 발생했습니다.");
        }
    });
});
