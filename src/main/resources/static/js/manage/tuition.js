document.addEventListener("DOMContentLoaded", () => {
    const saveBtn = document.getElementById("fee-save-btn");

    saveBtn.addEventListener("click", async () => {
        const inputs = document.querySelectorAll("input.edu-input");

        const feeMapList = [];
        inputs.forEach(input => {
            const name = input.getAttribute("name"); // 예: fee_HAN001
            const value = input.value.trim();

            if (name && name.startsWith("fee_")) {
                const classKey = name.replace("fee_", "");
                const fee = value === "" ? 0 : parseInt(value);
                const centerCode = document.querySelector("#centerCode").value;
                if (!centerCode) {
                    alert("지점코드가 없습니다. 다시 로그인해주세요.");
                }

                feeMapList.push({
                    centerCode: centerCode,
                    classKey: classKey,
                    fee: fee
                });
            }
        });

        if (feeMapList.length === 0) {
            alert("저장할 데이터가 없습니다.");
            return;
        }


        const data = {
            classFeeMap: feeMapList
        };
        console.log(data);
        console.log(JSON.stringify(data));

        try {
            const response = await fetch("/manage/insert", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(data)
            });

            const result = await response.json();
            console.log(result.response);
            console.log(result["response"]);
            alert(result.response);
        } catch (error) {
            console.error("저장 중 오류:", error);
            alert("저장에 실패했습니다.");
        }
    });
});