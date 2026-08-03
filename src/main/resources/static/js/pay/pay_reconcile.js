let lastResult = null;

$(function () {
    $("#btn-reconcile").on("click", runReconcile);
    $("#btn-reconcile-download").on("click", downloadResult);
});

function runReconcile() {
    const fileInput = document.getElementById("reconcile-file");
    if (!fileInput.files.length) {
        alert("엑셀 파일을 선택해주세요.");
        return;
    }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);
    formData.append("source", $("#source").val());

    $("#reconcile-loading").show();
    $("#btn-reconcile").prop("disabled", true);

    $.ajax({
        url: "/pay/reconcile",
        type: "POST",
        data: formData,
        processData: false,
        contentType: false
    }).done(function (data) {
        if (data.success === false) {
            alert(data.msg || "대조에 실패했습니다.");
            return;
        }
        lastResult = data.response;
        renderResult(lastResult);
    }).fail(function () {
        alert("대조 처리 중 오류가 발생했습니다.");
    }).always(function () {
        $("#reconcile-loading").hide();
        $("#btn-reconcile").prop("disabled", false);
    });
}

function renderResult(r) {
    $("#sum-file").text(r.fileApproved);
    $("#sum-db").text(r.dbTotal);
    $("#sum-matched").text(r.matched);
    $("#sum-missing-db").text(r.missingInDb ? r.missingInDb.length : 0);
    $("#sum-missing-file").text(r.missingInFile ? r.missingInFile.length : 0);
    $("#reconcile-daterange").text("대조 거래일자 범위: " + r.dateFrom + " ~ " + r.dateTo);

    const $db = $("#tbl-missing-db").empty();
    if (r.missingInDb && r.missingInDb.length) {
        r.missingInDb.forEach(function (row) {
            $db.append(
                "<tr><td>" + esc(row.txDate) + "</td><td>" + esc(row.cardName) +
                "</td><td>" + esc(row.apprNum) + "</td><td>" + esc(row.amount) + "</td></tr>");
        });
    } else {
        $db.append('<tr class="empty-row"><td colspan="4">일치하지 않는 건이 없습니다.</td></tr>');
    }

    const $file = $("#tbl-missing-file").empty();
    if (r.missingInFile && r.missingInFile.length) {
        r.missingInFile.forEach(function (row) {
            $file.append(
                "<tr><td>" + esc(row.apprDate) + "</td><td>" + esc(row.apprIssuer) +
                "</td><td>" + esc(row.apprNum) + "</td><td>" + esc(row.apprState) + "</td></tr>");
        });
    } else {
        $file.append('<tr class="empty-row"><td colspan="4">일치하지 않는 건이 없습니다.</td></tr>');
    }

    $("#reconcile-result").show();
}

function downloadResult() {
    if (!lastResult) {
        alert("먼저 대조를 실행해주세요.");
        return;
    }
    fetch("/pay/reconcile/download", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({result: lastResult})
    }).then(function (res) {
        if (!res.ok) throw new Error("download failed");
        return res.blob();
    }).then(function (blob) {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "매출대조_불일치내역.xlsx";
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
    }).catch(function () {
        alert("엑셀 다운로드에 실패했습니다.");
    });
}

function esc(s) {
    if (s == null) return "";
    return String(s)
        .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
