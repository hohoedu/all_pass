document.addEventListener("DOMContentLoaded", () => {

    const modal = document.querySelector(".notice-modal");
    const modalContent = modal.querySelector(".pre-view");
    const openCreateBtn = document.querySelector(".notice-regist");
    const closeBtn = modal.querySelector(".btn-close");
    const registBtn = modal.querySelector(".regist-btn");
    const titleElem = modal.querySelector(".pre-title");
    let mode = "create";
    let currentNoticeId = null; // 수정할 공지 ID 저장

    // ========== 필터 상태 ==========
    const activeFilters = {
        grades: new Set(),   // 다중 선택
        subject: null,
        class: null
    };

    // ========== classSelect 목록 구성 ==========
    function populateClassSelect(classType = null) {
        const classSelect = document.getElementById('classSelect');
        const rows = document.querySelectorAll('#student-tbody tr[data-grade]');

        const classNames = new Set();
        rows.forEach(row => {
            const hanType = row.dataset.hanClassType;
            const bookType = row.dataset.bookClassType;
            const hanName = row.dataset.hanClass;
            const bookName = row.dataset.bookClass;

            if (!classType) {
                if (hanName && hanName !== 'null') classNames.add(hanName);
                if (bookName && bookName !== 'null') classNames.add(bookName);
            } else {
                if (hanType === classType && hanName && hanName !== 'null') classNames.add(hanName);
                if (bookType === classType && bookName && bookName !== 'null') classNames.add(bookName);
            }
        });

        classSelect.innerHTML = '<option value="">전체</option>';
        classNames.forEach(name => {
            const opt = document.createElement('option');
            opt.value = name;
            opt.textContent = name;
            classSelect.appendChild(opt);
        });

        activeFilters.class = null;
    }

    // ========== 필터 적용 ==========
    function applyFilters() {
        const rows = document.querySelectorAll('#student-tbody tr');

        rows.forEach(row => {
            const rowGrade     = row.getAttribute('data-grade');
            const rowHanClass  = row.getAttribute('data-han-class');
            const rowHanType   = row.getAttribute('data-han-class-type');
            const rowBookClass = row.getAttribute('data-book-class');
            const rowBookType  = row.getAttribute('data-book-class-type');

            if (!rowGrade) return;

            const gradeMatch   = activeFilters.grades.size === 0 || activeFilters.grades.has(rowGrade);
            const subjectMatch = !activeFilters.subject
                || rowHanType === activeFilters.subject
                || rowBookType === activeFilters.subject;
            const classMatch   = !activeFilters.class
                || rowHanClass === activeFilters.class
                || rowBookClass === activeFilters.class;

            const visible = gradeMatch && subjectMatch && classMatch;
            row.style.display = visible ? '' : 'none';

            if (!visible) {
                const cb = row.querySelector('input[type="checkbox"]');
                if (cb) cb.checked = false;
            }
        });

        syncSelectAll();
    }

    // ========== 전체 선택 체크박스 동기화 ==========
    function syncSelectAll() {
        const selectAllCheckbox = document.getElementById('selectAll');
        if (!selectAllCheckbox) return;

        const visibleCheckboxes = Array.from(
            document.querySelectorAll('#student-tbody input[type="checkbox"]:not(:disabled)')
        ).filter(cb => cb.closest('tr').style.display !== 'none');

        if (visibleCheckboxes.length === 0) {
            selectAllCheckbox.checked = false;
            return;
        }

        selectAllCheckbox.checked = visibleCheckboxes.every(cb => cb.checked);
    }

    // ========== 학년별 필터 버튼 ==========
    document.querySelectorAll('.filter-group[data-group="grade"] .filter-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            const value = this.dataset.value;

            if (this.classList.contains('active')) {
                this.classList.remove('active');
                activeFilters.grades.delete(value);
            } else {
                this.classList.add('active');
                activeFilters.grades.add(value);
            }

            applyFilters();
        });
    });

    // ========== 과목별 필터 버튼 (단일 선택) ==========
    document.querySelectorAll('.filter-group[data-group="subject"] .filter-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            const isActive = this.classList.contains('active');

            document.querySelectorAll('.filter-group[data-group="subject"] .filter-btn')
                .forEach(b => b.classList.remove('active'));

            if (isActive) {
                activeFilters.subject = null;
                populateClassSelect();
            } else {
                this.classList.add('active');
                activeFilters.subject = this.dataset.value; // "1" or "2"
                populateClassSelect(this.dataset.value);
            }

            applyFilters();
        });
    });

    // ========== 수업별 셀렉트 ==========
    const classSelect = document.getElementById('classSelect');
    if (classSelect) {
        classSelect.addEventListener('change', function () {
            activeFilters.class = this.value || null;
            applyFilters();
        });
    }

    // ========== 함수 정의 먼저 ==========
    function clearForm() {
        currentNoticeId = null; // ID 초기화
        modal.querySelectorAll("input[type='text'], textarea").forEach((el) => (el.value = ""));
        $('#content').summernote('code', '');
        document.getElementById('imagePath').value = "";
        document.getElementById('imageNameWrapper').style.display = 'none';
        document.getElementById('linkUrl').value = "";
        const linkRow = document.getElementById('linkRow');
        if (linkRow) linkRow.style.display = 'none';

        // 아이콘 초기화
        const selected = document.querySelector('#noticeSelect .selected');
        if (selected) {
            selected.dataset.value = "1";
            selected.innerHTML = `<img src="/image/notice01.png" alt=""> 아이콘 1<span class="arrow"><i class="xi-angle-down"></i></span>`;
        }

        // 필터 초기화
        activeFilters.grade.clear();
        activeFilters.subject = null;
        activeFilters.class = null;
        document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('#student-tbody tr').forEach(r => r.style.display = '');
        document.querySelectorAll('#student-tbody input[type="checkbox"]').forEach(cb => cb.checked = false);
        const selectAllCheckbox = document.getElementById('selectAll');
        if (selectAllCheckbox) selectAllCheckbox.checked = false;
        populateClassSelect();
    }

    function fillForm(data) {
        currentNoticeId = data.id; // ID 저장
        const inputs = modal.querySelectorAll("input[type='text']");
        inputs[0].value = data.title || "";
        inputs[1].value = data.subTitle || "";
        $('#content').summernote('code', data.rawContent || data.content || "");

        // 아이콘 선택
        const selected = document.querySelector('#noticeSelect .selected');
        if (data.icon) {
            selected.dataset.value = data.icon;
            selected.innerHTML = `<img src="/image/notice0${data.icon}.png" alt=""> 아이콘 ${data.icon}<span class="arrow"><i class="xi-angle-down"></i></span>`;
        }

        // 링크 URL
        if (data.linkUrl) {
            document.getElementById('linkUrl').value = data.linkUrl;
            const linkRow = document.getElementById('linkRow');
            if (linkRow) linkRow.style.display = 'flex';
        }

        // 이미지
        if (data.image) {
            document.getElementById('imagePath').value = data.image;
            const fileName = data.image.split('/').pop();
            document.getElementById('fileName').textContent = fileName;
            document.getElementById('imageNameWrapper').style.display = 'flex';
        }
    }

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

    // ========== 삭제 함수 ==========
    async function handleDelete(e) {
        const id = e.target.dataset.id;
        if (!id) {
            console.error("❌ data-id가 없습니다:", e.target);
            return;
        }

        if (!confirm("정말 삭제하시겠습니까?")) return;

        try {
            const response = await fetch("/notice/center/delete", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({id: parseInt(id)})
            });

            const result = await response.json();
            if (result.success) {
                alert("공지사항이 삭제되었습니다.");
                location.reload();
            } else {
                alert("삭제 실패: " + result.message);
            }
        } catch (err) {
            console.error("❌ 삭제 중 오류:", err);
            alert("삭제 중 오류가 발생했습니다.");
        }
    }

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

        const deleteBtn = rightSection.querySelector(".notice-delete");
        deleteBtn.addEventListener("click", handleDelete);
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

    // ========== 초기 로드된 버튼에 이벤트 연결 ==========
    const initialDeleteBtn = document.querySelector(".edu-right .notice-delete");
    if (initialDeleteBtn) {
        console.log("✅ 초기 삭제 버튼 발견, 이벤트 연결");
        initialDeleteBtn.addEventListener("click", handleDelete);
    }

    const initialUpdateBtn = document.querySelector(".edu-right .notice-update");
    if (initialUpdateBtn) {
        console.log("✅ 초기 수정 버튼 발견, 이벤트 연결");
        initialUpdateBtn.addEventListener("click", async function () {
            const id = this.dataset.id;
            if (!id) return;

            try {
                const res = await fetch("/notice/detail", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({id})
                });
                if (!res.ok) throw new Error("공지 상세 조회 실패");
                const data = await res.json();
                openModal("edit", data.response);
            } catch (err) {
                console.error("❌ 공지 로드 실패:", err);
                alert("공지 정보를 불러오지 못했습니다.");
            }
        });
    }

    // ========== 모달 이벤트 ==========
    openCreateBtn.addEventListener("click", () => openModal("create"));
    closeBtn.addEventListener("click", closeModal);

    modal.addEventListener("click", (e) => {
        if (!modalContent.contains(e.target)) {
            e.stopImmediatePropagation();
            e.preventDefault();
        }
    });

    // ========== 체크박스 전체 선택 ==========
    const selectAllCheckbox = document.getElementById('selectAll');

    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function () {
            const visibleCheckboxes = Array.from(
                document.querySelectorAll('#student-tbody input[type="checkbox"]:not(:disabled)')
            ).filter(cb => cb.closest('tr').style.display !== 'none');

            visibleCheckboxes.forEach(cb => cb.checked = this.checked);
        });
    }

    const studentTbody = document.getElementById('student-tbody');
    if (studentTbody) {
        studentTbody.addEventListener('change', function (e) {
            if (e.target.type === 'checkbox') {
                syncSelectAll();
            }
        });
    }

    // ========== 테이블 정렬 기능 ==========
    const sortableHeaders = document.querySelectorAll('.sortable');
    let currentSort = {column: null, ascending: true};

    sortableHeaders.forEach(header => {
        header.addEventListener('click', function () {
            const column = this.dataset.column;
            const tbody = document.getElementById('student-tbody');
            if (!tbody) return;

            const rows = Array.from(tbody.querySelectorAll('tr:not([th\\:if])'));

            if (currentSort.column === column) {
                currentSort.ascending = !currentSort.ascending;
            } else {
                currentSort.column = column;
                currentSort.ascending = true;
            }

            sortableHeaders.forEach(h => {
                const img = h.querySelector('img');
                if (img) img.src = '/image/sort.svg';
            });
            const currentImg = this.querySelector('img');
            if (currentImg) {
                currentImg.src = currentSort.ascending ? '/image/sort_checked.svg' : '/image/sort.svg';
            }

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

            rows.forEach(row => tbody.appendChild(row));
        });
    });

    // ========== 이미지 업로드 ==========
    const uploadBtn = document.getElementById("uploadBtn");
    const imageInput = document.getElementById("imageInput");
    const fileName = document.getElementById("fileName");
    const imageNameWrapper = document.getElementById("imageNameWrapper");
    const imagePathInput = document.getElementById("imagePath");

    if (uploadBtn && imageInput) {
        uploadBtn.addEventListener("click", () => imageInput.click());

        imageInput.addEventListener("change", (e) => {
            const file = e.target.files[0];
            if (!file) return;

            const safeFileName = file.name.replace(/\s+/g, "_");
            if (fileName) fileName.textContent = safeFileName;
            if (imageNameWrapper) imageNameWrapper.style.display = "flex";

            const renamedFile = new File([file], safeFileName, {type: file.type});
            const formData = new FormData();
            formData.append("file", renamedFile);

            fetch("/notice/upload", {method: "POST", body: formData})
                .then(res => res.text())
                .then(path => {
                    if (imagePathInput) imagePathInput.value = path;
                })
                .catch(err => alert("업로드 실패"));
        });
    }

    const removeImageBtn = document.getElementById("removeImage");
    if (removeImageBtn) {
        removeImageBtn.addEventListener("click", (e) => {
            e.preventDefault();
            if (imageInput) imageInput.value = "";
            if (imageNameWrapper) imageNameWrapper.style.display = "none";
            if (imagePathInput) imagePathInput.value = "";
        });
    }

    // ========== 커스텀 셀렉트 ==========
    const select = document.getElementById("noticeSelect");
    if (select) {
        const selected = select.querySelector(".selected");
        const options = select.querySelectorAll(".select-options li");

        if (selected) {
            selected.onclick = () => select.classList.toggle("open");
        }

        options.forEach((li, index) => {
            li.onclick = () => {
                const img = li.querySelector("img").getAttribute("src");
                const text = li.textContent.trim();
                if (selected) {
                    selected.innerHTML = `<img src="${img}" alt=""> ${text}<span class="arrow"><i class="xi-angle-down"></i></span>`;
                    selected.dataset.value = index + 1;
                }
                select.classList.remove("open");
            };
        });

        document.addEventListener("click", (e) => {
            if (!select.contains(e.target)) select.classList.remove("open");
        });
    }

    // ========== Summernote 에디터 ==========
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
                                if (linkInput) linkInput.value = result.value;
                                if (linkRow) linkRow.style.display = 'flex';
                            }
                        });
                    }
                }).render();
            }
        }
    });

    // ========== 공지 저장/수정 ==========
    const saveBtn = document.querySelector("#saveNotice");
    if (saveBtn) {
        saveBtn.addEventListener("click", async () => {
            const title = document.querySelector('input[placeholder="제목을 입력해주세요."]').value.trim();
            const subTitle = document.querySelector('input[placeholder="부제목을 입력해주세요."]').value.trim();
            const icon = document.querySelector('#noticeSelect .selected').dataset.value;
            const content = $('#content').summernote('code');
            const linkUrlInput = document.getElementById('linkUrl');
            const linkUrl = linkUrlInput ? linkUrlInput.value.trim() : '';
            const imagePathElem = document.getElementById('imagePath');
            const image = imagePathElem ? imagePathElem.value : '';

            if (!title || !subTitle || !content) {
                alert("제목, 부제목, 내용을 모두 입력해주세요.");
                return;
            }

            // 선택된 학생들의 앱 토큰 수집 (등록 시에만)
            let tokens = [];
            if (mode === "create") {
                const checkedBoxes = document.querySelectorAll('#student-tbody input[type="checkbox"]:checked:not(:disabled)');
                tokens = Array.from(checkedBoxes)
                    .map(cb => cb.dataset.token)
                    .filter(token => token && token !== 'null');
            }

            const noticeData = {
                title,
                subTitle,
                icon,
                content,
                linkUrl,
                image,
                tokens: tokens
            };

            try {
                let url = "/notice/center/save";
                let requestData = {
                    title,
                    subTitle,
                    icon: parseInt(icon),
                    content,
                    linkUrl,
                    image,
                    tokens: tokens
                };

                // 수정 모드일 때
                if (mode === "edit") {
                    url = "/notice/center/update";
                    requestData = {
                        id: currentNoticeId,
                        title: title,
                        subTitle: subTitle,
                        icon: parseInt(icon),
                        content: content,
                        linkUrl: linkUrl,
                        image: image
                    };
                }

                const response = await fetch(url, {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify(requestData)
                });

                const result = await response.json();
                if (result.success) {
                    if (mode === "edit") {
                        alert("공지사항이 수정되었습니다!");
                    } else {
                        if (tokens.length > 0) {
                            alert(`공지사항이 등록되고 ${tokens.length}명의 학생에게 알림이 발송되었습니다!`);
                        } else {
                            alert("공지사항이 성공적으로 등록되었습니다!");
                        }
                    }
                    closeModal();
                    location.reload();
                } else {
                    alert("저장 실패: " + result.message);
                }
            } catch (err) {
                console.error("❌ 저장 중 오류:", err);
                alert("저장 중 오류가 발생했습니다.");
            }
        });
    }

    // ========== 초기 classSelect 구성 ==========
    populateClassSelect();

});