document.addEventListener("DOMContentLoaded", () => {
    const phoneInput = document.getElementById("new-member-phone");
    const sendBtn = document.getElementById("send-btn");

    // 전화번호 정규화 함수
    function normalizePhone(input) {
        let phone = input.replace(/-/g, "").trim(); // 하이픈 제거 + trim

        // 숫자가 아닌 경우
        if (!/^[0-9]+$/.test(phone)) {
            alert("전화번호는 숫자만 입력해 주세요.");
            return null;
        }

        // 010으로 시작하면서 11자리 → 그대로
        if (phone.startsWith("010") && phone.length === 11) {
            return phone;
        }

        // 8자리 → 010 붙여서 변환
        if (phone.length === 8) {
            return "010" + phone;
        }

        // 나머지는 잘못된 입력
        alert("올바른 전화번호를 입력해 주세요. (010으로 시작하거나 8자리 숫자)");
        return null;
    }

    sendBtn.addEventListener("click", async () => {
        console.log("버튼 클릭");

        const rawPhone = phoneInput.value;
        const phone = normalizePhone(rawPhone);

        if (!phone) return;

        if (!/^[0-9]{10,11}$/.test(phone)) {
            alert("올바른 전화번호를 입력해 주세요. (‘-’ 없이 10~11자리)");
            return;
        }

        try {
            const response = await fetch("/notice/send-join", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({phone})
            });

            const result = await response.json();

            if (result.success) {
                alert("알림톡이 발송되었습니다.");
            } else {
                alert("발송 실패: " + (result.message || ""));
            }

        } catch (e) {
            console.error(e);
            alert("알림톡 발송 중 오류가 발생했습니다.");
        }
    });
});