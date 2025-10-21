window.addEventListener("DOMContentLoaded", () => {
    const errorBox = document.getElementById("error-box");
    const message = errorBox?.dataset?.message;

    if (message) {
        alert(message);
        console.log(message);
    }

});

window.addEventListener("DOMContentLoaded", () => {
    // 🔹 1. 에러 메시지 처리
    const errorBox = document.getElementById("error-box");
    const message = errorBox?.dataset?.message;
    if (message) {
        alert(message);
        console.log(message);
    }

    // 🔹 2. 저장된 지점코드 / 아이디 자동 입력
    const savedCenter = localStorage.getItem("centerCode");
    const savedUser = localStorage.getItem("userId");

    if (savedCenter) document.querySelector("#center-code").value = savedCenter;
    if (savedUser) document.querySelector("#user-id").value = savedUser;

    if (savedCenter || savedUser) {
        document.querySelector("#remember").checked = true;
    }

    // 🔹 3. 로그인 폼 제출 시 저장/삭제 처리
    document.querySelector("#login-form").addEventListener("submit", () => {
        const center = document.querySelector("#center-code").value.trim();
        const user = document.querySelector("#user-id").value.trim();
        const remember = document.querySelector("#remember").checked;

        if (remember) {
            localStorage.setItem("centerCode", center);
            localStorage.setItem("userId", user);
        } else {
            localStorage.removeItem("centerCode");
            localStorage.removeItem("userId");
        }
    });
});