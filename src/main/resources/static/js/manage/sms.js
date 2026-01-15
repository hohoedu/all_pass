document.addEventListener("DOMContentLoaded", () => {

    const modal = document.querySelector(".notice-modal");
    const modalContent = modal.querySelector(".pre-view");
    const openCreateBtn = document.querySelector(".notice-regist");
    const closeBtn = modal.querySelector(".btn-close");
    const registBtn = modal.querySelector(".regist-btn");
    const titleElem = modal.querySelector(".pre-title");
    let mode = "create";

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
            fillForm(data);
        }
    };

    const closeModal = () => {
        modal.style.display = "none";
        document.body.classList.remove("modal-open");
    };

    openCreateBtn.addEventListener("click", () => openModal("create"));
    closeBtn.addEventListener("click", closeModal);

    modal.addEventListener("click", (e) => {
        if (!modalContent.contains(e.target)) {
            e.stopImmediatePropagation();
            e.preventDefault();
        }
    });

    const selectAllCheckbox = document.getElementById('selectAll');
    const studentCheckboxes = document.querySelectorAll('#student-tbody input[type="checkbox"]:not(:disabled)');

    selectAllCheckbox.addEventListener('change', function () {
        studentCheckboxes.forEach(checkbox => {
            checkbox.checked = this.checked;
        });
    });

    // 개별 체크박스 클릭 시 전체 선택 상태 업데이트
    document.getElementById('student-tbody').addEventListener('change', function (e) {
        if (e.target.type === 'checkbox') {
            const allChecked = Array.from(studentCheckboxes).every(cb => cb.checked);
            selectAllCheckbox.checked = allChecked;
        }
    });

    // ========== 테이블 정렬 기능 ==========
    const sortableHeaders = document.querySelectorAll('.sortable');
    let currentSort = {column: null, ascending: true};

    sortableHeaders.forEach(header => {
        header.addEventListener('click', function () {
            const column = this.dataset.column;
            const tbody = document.getElementById('student-tbody');
            const rows = Array.from(tbody.querySelectorAll('tr:not([th\\:if])'));

            // 정렬 방향 결정
            if (currentSort.column === column) {
                currentSort.ascending = !currentSort.ascending;
            } else {
                currentSort.column = column;
                currentSort.ascending = true;
            }

            // 정렬 아이콘 업데이트
            sortableHeaders.forEach(h => {
                const img = h.querySelector('img');
                img.src = '/image/sort.svg';
            });
            const currentImg = this.querySelector('img');
            currentImg.src = currentSort.ascending ? '/image/sort_checked.svg' : '/image/sort.svg';

            // 행 정렬
            rows.sort((a, b) => {
                let aValue, bValue;

                switch (column) {
                    case 'name':
                        aValue = a.cells[1].textContent.trim();
                        bValue = b.cells[1].textContent.trim();
                        break;
                    case 'subject':
                        aValue = a.cells[2].textContent.trim();
                        bValue = b.cells[2].textContent.trim();
                        break;
                    case 'grade':
                        aValue = a.cells[3].textContent.trim();
                        bValue = b.cells[3].textContent.trim();
                        break;
                }

                if (aValue < bValue) return currentSort.ascending ? -1 : 1;
                if (aValue > bValue) return currentSort.ascending ? 1 : -1;
                return 0;
            });

            // 정렬된 행 다시 추가
            rows.forEach(row => tbody.appendChild(row));
        });
    });

    function clearForm() {
        modal.querySelectorAll("input[type='text'], textarea").forEach((el) => (el.value = ""));
        $('#content').summernote('code', '');
    }

    function fillForm(data) {
        const inputs = modal.querySelectorAll("input[type='text']");
        inputs[0].value = data.title || "";
        inputs[1].value = data.subTitle || "";
        $('#content').summernote('code', data.rawContent || data.content || "");
    }

    window.loadNoticeDetail = async (element) => {
        const parent = element.closest(".app-style");
        const id = parent.dataset.id;

        try {
            const res = await fetch("/notice/detail", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({id})
            });
            if (!res.ok) throw new Error("공지 상세 조회 실패");
            const data = await res.json();

            renderNoticeDetail(data.response);
        } catch (err) {
            console.error("❌ 공지 상세 로드 실패:", err);
            alert("공지 상세정보를 불러오지 못했습니다.");
        }
    };

    function renderNoticeDetail(data) {
        const rightSection = document.querySelector(".edu-right");
        if (!rightSection) return;

        rightSection.innerHTML = `
            <div class="inout-head">
                <div class="common-title"><h2>상세 내용</h2></div>
                <div>
                    <div class="common-btn notice-update" data-id="${data.id}">수정</div>
                    <div class="common-btn notice-delete" data-id="${data.id}">삭제</div>
                </div>
            </div>
            <div class="hash-boxes">
                <div class="hash-box">#7세</div>
                <div class="hash-box">#1학년</div>
                <div class="hash-box">#2학년</div>
            </div>
            <div class="app-info">
                <div class="app-info-t">${data.title}</div>
                <img src="/image/notice0${data.icon ? data.icon : '1'}.png" alt="아이콘">
            </div>
            <div class="app-info-desc">${data.rawContent}</div>
            <div class="save-btn-frame"><button class="view-details">상세 보기</button></div>
        `;

        const openEditBtn = rightSection.querySelector(".notice-update");
        openEditBtn.addEventListener("click", () => openModal("edit", data));
    }

    const uploadBtn = document.getElementById("uploadBtn");
    const imageInput = document.getElementById("imageInput");
    const fileName = document.getElementById("fileName");
    const imageNameWrapper = document.getElementById("imageNameWrapper");
    const imagePathInput = document.getElementById("imagePath");

    uploadBtn.addEventListener("click", () => imageInput.click());

    imageInput.addEventListener("change", (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const safeFileName = file.name.replace(/\s+/g, "_");
        fileName.textContent = safeFileName;
        imageNameWrapper.style.display = "flex";

        const renamedFile = new File([file], safeFileName, {type: file.type});
        const formData = new FormData();
        formData.append("file", renamedFile);

        fetch("/notice/upload", {method: "POST", body: formData})
            .then(res => res.text())
            .then(path => imagePathInput.value = path)
            .catch(err => alert("업로드 실패"));
    });

    document.getElementById("removeImage").addEventListener("click", (e) => {
        e.preventDefault();
        imageInput.value = "";
        imageNameWrapper.style.display = "none";
        imagePathInput.value = "";
    });

    const select = document.getElementById("noticeSelect");
    const selected = select.querySelector(".selected");
    const options = select.querySelectorAll(".select-options li");

    selected.onclick = () => select.classList.toggle("open");

    options.forEach((li, index) => {
        li.onclick = () => {
            const img = li.querySelector("img").getAttribute("src");
            const text = li.textContent.trim();
            selected.innerHTML = `<img src="${img}" alt=""> ${text}<span class="arrow"><i class="xi-angle-down"></i></span>`;
            selected.dataset.value = index + 1;
            select.classList.remove("open");
        };
    });

    document.addEventListener("click", (e) => {
        if (!select.contains(e.target)) select.classList.remove("open");
    });

    $('#content').summernote({
        height: 250,
        lang: 'ko-KR',
        placeholder: '공지 내용을 입력해주세요.',
        disableResizeEditor: true,
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
                    click: function () {
                        Swal.fire({
                            title: '링크 추가하기',
                            input: 'url',
                            inputLabel: '추가할 링크를 입력하세요',
                            inputPlaceholder: 'https://',
                            confirmButtonText: '확인',
                            showCancelButton: true
                        }).then((result) => {
                            if (result.isConfirmed && result.value) {
                                const linkInput = document.getElementById('linkUrl');
                                const linkRow = document.getElementById('linkRow');
                                linkInput.value = result.value;
                                linkRow.style.display = 'flex';
                            }
                        });
                    }
                }).render();
            }
        }
    });

    const saveBtn = document.querySelector("#saveNotice");
    saveBtn.addEventListener("click", async () => {
        const title = document.querySelector('input[placeholder="제목을 입력해주세요."]').value.trim();
        const subTitle = document.querySelector('input[placeholder="부제목을 입력해주세요."]').value.trim();
        const icon = document.querySelector('#noticeSelect .selected').dataset.value;
        const content = $('#content').summernote('code');
        const linkUrl = document.getElementById('linkUrl').value.trim();
        const image = document.getElementById('imagePath').value;

        if (!title || !subTitle || !content) {
            alert("제목, 부제목, 내용을 모두 입력해주세요.");
            return;
        }

        // 선택된 학생들의 앱 토큰 수집
        const checkedBoxes = document.querySelectorAll('#student-tbody input[type="checkbox"]:checked:not(:disabled)');
        const tokens = Array.from(checkedBoxes)
            .map(cb => cb.dataset.token)
            .filter(token => token && token !== 'null');

        const noticeData = {
            title,
            subTitle,
            icon,
            content,
            linkUrl,
            image,
            tokens: tokens  // 앱 토큰 추가
        };

        try {
            const response = await fetch("/notice/center/save", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(noticeData)
            });

            const result = await response.json();
            if (result.success) {
                if (tokens.length > 0) {
                    alert(`공지사항이 등록되고 ${tokens.length}명의 학생에게 알림이 발송되었습니다!`);
                } else {
                    alert("공지사항이 성공적으로 등록되었습니다!");
                }
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
