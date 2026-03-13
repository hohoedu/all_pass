/**
 * app_id_temp.js
 * App ID 조회 팝업 전용 스크립트 (Vanilla JS)
 *
 * API
 *  1. 학생 검색  : GET  /student/search?studentName={studentName}
 *     응답: { response: [{ studentId, studentName, parentPhone, currentAppId, prevAppId }, ...] }
 *
 *  2. App ID 저장: POST /api/app-id/save
 *     바디: { studentId, currentAppId, prevAppId, userCode }
 *     응답: { success: true }
 */

document.addEventListener("DOMContentLoaded", function () {

    const searchInput = document.getElementById("search-input");
    const btnSearch = document.getElementById("btn-search");
    const resultTbody = document.getElementById("result-tbody");
    const toast = document.getElementById("toast");

    /* ══════════════════════════════════════════
       이벤트 바인딩
    ══════════════════════════════════════════ */

    btnSearch.addEventListener("click", searchStudent);

    searchInput.addEventListener("keydown", function (e) {
        if (e.key === "Enter") searchStudent();
    });

    // 저장 버튼 이벤트 위임 (동적 생성 요소)
    resultTbody.addEventListener("click", function (e) {
        const btn = e.target.closest(".btn-save-id");
        if (!btn) return;
        saveAppId(btn);
    });

    searchInput.focus();


    /* ══════════════════════════════════════════
       1. 학생 검색
       GET /student/search?studentName={studentName}
    ══════════════════════════════════════════ */

    function searchStudent() {
        const keyword = searchInput.value.trim();

        if (!keyword) {
            setState("학생 이름을 입력해 주세요.");
            return;
        }

        setState("검색 중...");

        fetch("/student/search?" + new URLSearchParams({studentName: keyword}), {
            method: "GET"
        })
            .then(handleResponse)
            .then(function (data) {
                console.log(data.response);
                renderResults(data.response);
            })
            .catch(function (err) {
                setState(err.message || "검색 중 오류가 발생했습니다.");
                console.error("[AppId] 검색 실패", err);
            });
    }


    /* ══════════════════════════════════════════
       결과 렌더링
       - 현재 App ID : id-chip (읽기 전용)
       - 이전 App ID : input  (카드에 적힌 App ID 직접 입력)
    ══════════════════════════════════════════ */

    function renderResults(data) {
        if (!Array.isArray(data) || data.length === 0) {
            setState("검색 결과가 없습니다.");
            return;
        }

        const rows = data.map(function (item) {
            const currChip = item.currentAppId
                ? `<span class="id-chip">${item.currentAppId}</span>`
                : `<span class="id-chip none">없음</span>`;

            const prevInput = `
               <input
                    type="text"
                    class="input-prev-app-id"
                    placeholder="카드 ID 입력"
                    value="${item.prevAppId || ''}"
                    inputmode="numeric"
                    oninput="this.value = this.value.replace(/[^0-9]/g, '')"
                >`;

            const saveBtn = `
                <button
                    class="btn-save-id"
                    data-student-id="${item.studentId}"
                    data-current-app-id="${item.currentAppId || ''}">
                    저장
                </button>`;

            return `
                <tr>
                    <td title="${item.studentName}">${item.studentName}</td>
                    <td>${currChip}</td>
                    <td>${prevInput}</td>
                    <td>${saveBtn}</td>
                </tr>`;
        });

        resultTbody.innerHTML = rows.join("");
    }


    /* ══════════════════════════════════════════
       2. App ID 저장
       POST /api/app-id/save
       body: { studentId, currentAppId, prevAppId, userCode }
    ══════════════════════════════════════════ */

    function saveAppId(btn) {
        const studentId = btn.dataset.studentId;
        const currentAppId = btn.dataset.currentAppId;  // 현재 chip에 표시된 값

        // 같은 행(tr)에서 input 값 읽기
        const row = btn.closest("tr");
        const input = row.querySelector(".input-prev-app-id");
        const prevAppId = input ? input.value.trim() : "";

        // userCode — 로그인 세션에서 가져오는 방식으로 교체 필요
        // 예: const userCode = document.querySelector("meta[name='user-code']").content;
        // const userCode = window.LOGIN_USER_CODE || "";

        if (!prevAppId) {
            showToast("App ID를 입력해 주세요.");
            if (input) input.focus();
            return;
        }

        if (!/^\d+$/.test(prevAppId)) {
            alert("App ID는 숫자만 입력해 주세요.");
            if (input) input.focus();
            return;
        }

        // if (!userCode) {
        //     showToast("선생님 코드를 확인할 수 없습니다.");
        //     return;
        // }

        btn.disabled = true;
        btn.textContent = "저장 중...";

        fetch("/student/app_id/update", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                studentId: studentId,
                currentAppId: currentAppId,
                prevAppId: prevAppId
                // userCode    : userCode
            })
        })
            .then(handleResponse)
            .then(function (res) {
                if (!res.success) throw new Error("저장에 실패했습니다.");

                // 저장 성공 → 현재 App ID chip을 새로 저장된 값으로 업데이트
                const currChip = row.querySelector(".id-chip");
                if (currChip) {
                    currChip.textContent = prevAppId;
                    currChip.classList.remove("none");
                }

                btn.textContent = "✔ 완료";
                btn.classList.add("saved");
                showToast("App ID가 저장되었습니다.");

                setTimeout(function () {
                    btn.disabled = false;
                    btn.textContent = "저장";
                    btn.classList.remove("saved");
                }, 2500);
            })
            .catch(function (err) {
                showToast(err.message || "저장 중 오류가 발생했습니다.");
                btn.disabled = false;
                btn.textContent = "저장";
                console.error("[AppId] 저장 실패", err);
            });
    }


    /* ══════════════════════════════════════════
       공통 유틸
    ══════════════════════════════════════════ */

    function handleResponse(res) {
        if (res.ok) return res.json();
        return res.json().then(function (err) {
            throw new Error(err.message || "서버 오류가 발생했습니다.");
        });
    }

    function setState(message) {
        resultTbody.innerHTML =
            `<tr class="state-row"><td colspan="5">${message}</td></tr>`;
    }

    function showToast(msg) {
        toast.textContent = msg;
        toast.classList.add("show");
        setTimeout(function () {
            toast.classList.remove("show");
        }, 2200);
    }

});