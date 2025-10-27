window.addEventListener("DOMContentLoaded", () => {
    const errorBox = document.getElementById("error-box");
    const message = errorBox?.dataset?.message;
    if (message) {
        alert(message);
    }

    const savedCenter = localStorage.getItem("centerCode");
    const savedUser = localStorage.getItem("userId");

    if (savedCenter) document.querySelector("#center-code").value = savedCenter;
    if (savedUser) document.querySelector("#user-id").value = savedUser;

    if (savedCenter || savedUser) {
        document.querySelector("#remember").checked = true;
    }

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