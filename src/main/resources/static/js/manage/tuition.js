document.addEventListener("DOMContentLoaded", () => {
    const saveBtn = document.getElementById("fee-save-btn");

    saveBtn.addEventListener("click", async () => {
        const inputs = document.querySelectorAll("input.edu-input");

        const feeMapList = [];
        inputs.forEach(input => {
            const name = input.getAttribute("name"); // fee_HAN001 또는 fee_HL_H01
            const value = input.value.trim();
            const centerCode = document.querySelector("#centerCode").value;

            if (!name || !name.startsWith("fee_")) return;

            const parts = name.split("_");
            // ["fee", "HAN001"] or ["fee", "HL", "H01"]

            let classKey = null;
            let unitKey = null;

            if (parts.length === 2) {
                // 일반 과목: fee_HAN001
                classKey = parts[1];
            } else if (parts.length === 3) {
                // 급수 과목: fee_HL_H01
                classKey = parts[1];
                unitKey = parts[2];
            }

            const fee = value === "" ? 0 : parseInt(value);

            feeMapList.push({
                centerCode: centerCode,
                classKey: classKey,
                unitKey: unitKey, // 일반 과목이면 null
                fee: fee
            });
        });

        if (feeMapList.length === 0) {
            alert("저장할 데이터가 없습니다.");
            return;
        }

        const data = { classFeeMap: feeMapList };
        console.log(data);

        try {
            const response = await fetch("/manage/fee/insert", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(data)
            });

            const result = await response.json();
            alert(result.response);
        } catch (error) {
            console.error("저장 중 오류:", error);
            alert("저장에 실패했습니다.");
        }
    });
});
