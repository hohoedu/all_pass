document.addEventListener("DOMContentLoaded", () => {

    // 월 선택 버튼 (.class-btn2)
    const monthButtons = document.querySelectorAll(".class-btn2");
    monthButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            monthButtons.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");
        });
    });

    const classButtons = document.querySelectorAll(".class-btn");
    classButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            classButtons.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");
        });
    });


    for (let week = 1; week <= 4; week++) {
        const fileInput = document.getElementById(`file-${week}`);
        const fileNameSpan = document.getElementById(`file-name-${week}`);
        const imagePathHidden = document.getElementById(`imagePath-${week}`);
        const fileRemoveBtn = document.querySelector(`table[data-week="${week}"] .file-remove`);

        fileInput.addEventListener("change", async (e) => {
            const file = e.target.files[0];
            if (!file) return;

            const safeFileName = file.name.replace(/\s+/g, "_");
            const renamedFile = new File([file], safeFileName, {type: file.type});
            const formData = new FormData();
            formData.append("file", renamedFile);
            formData.append("week", week); // 필요하면 서버에서 사용

            try {
                const res = await fetch("/admin/upload/book-image", {
                    method: "POST",
                    body: formData
                });

                if (!res.ok) throw new Error("업로드 API 오류");

                const uploadedPath = await res.json();

                console.log("업로드된 경로:", uploadedPath.response);

                fileNameSpan.textContent = safeFileName;
                imagePathHidden.value = uploadedPath.response;

            } catch (err) {
                console.error(err);
                alert("이미지 업로드 실패");
            }
        });
        fileRemoveBtn.addEventListener("click", async () => {
            const imageUrl = imagePathHidden.value;
            console.log(imageUrl);
            if (!imageUrl) {
                alert("삭제할 이미지가 없습니다.");
                return;
            }

            const confirmDelete = confirm("이 이미지를 삭제하시겠습니까?");
            if (!confirmDelete) return;

            try {
                const res = await fetch("/admin/delete/book-image", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({url: imageUrl})
                });

                const result = await res.json();

                if (result.success) {
                    alert("이미지 삭제 완료");

                    // UI 초기화
                    imagePathHidden.value = "";
                    fileNameSpan.textContent = "이미지";
                    fileInput.value = "";
                } else {
                    alert("삭제 실패 : " + result.message);
                }

            } catch (err) {
                console.error(err);
                alert("삭제 중 오류 발생");
            }
        });

    }

    document.getElementById("book-save").addEventListener("click", async () => {
        console.log("저장 클릭");

        // 1) 단계(classKey)
        const selectedClass = document.querySelector(".class-btn.active");
        if (!selectedClass) {
            alert("단계를 선택해주세요.");
            return;
        }
        const classKey = document.querySelector(".class-btn.active").dataset.key;

        // 2) 월(mm)
        const selectedMonth = document.querySelector(".class-btn2.active");
        if (!selectedMonth) {
            alert("월을 선택해주세요.");
            return;
        }
        const mm = selectedMonth.dataset.classId.replace("월", "");

        // 3) 연도(yy)
        const yy = new Date().getFullYear().toString();

        // 4) 입력된 week만 수집
        const weeks = [];
        for (let week = 1; week <= 4; week++) {

            const subject = document.getElementById(`subject-${week}`).value;
            const publisher = document.getElementById(`publisher-${week}`).value.trim();
            const bookName = document.getElementById(`bookName-${week}`).value.trim();
            const imageUrl = document.getElementById(`imagePath-${week}`).value.trim();

            // **입력된 게 하나라도 있으면 해당 week 저장**
            const hasData =
                (subject && subject !== "") ||
                publisher !== "" ||
                bookName !== "" ||
                imageUrl !== "";

            if (!hasData) {
                continue;  // 아무 값도 없으면 skip
            }

            weeks.push({
                week: week,
                subjectKey: subject || null,
                publisher: publisher,
                bookName: bookName,
                imageUrl: imageUrl
            });
        }

        if (weeks.length === 0) {
            alert("입력된 도서 정보가 없습니다.");
            return;
        }

        // 최종 JSON 구조
        const payload = {
            classKey: classKey,
            yy: yy,
            mm: mm.padStart(2, "0"),
            weeks: weeks
        };

        console.log("보내는 JSON → ", payload);

        // 서버 전송
        try {
            const res = await fetch("/admin/save/book", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(payload)
            });

            const result = await res.json();
            console.log("서버 응답:", result);

            if (result.success) {
                alert("저장되었습니다!");
            } else {
                alert("저장 실패 : " + result.message);
            }
        } catch (err) {
            console.error(err);
            alert("저장 중 오류 발생");
        }
    });

});
