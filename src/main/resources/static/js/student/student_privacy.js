document.addEventListener("DOMContentLoaded", function () {
  /* ════════════════════════════════
       초기화
    ════════════════════════════════ */
  var centerCode = document.body.dataset.centerCode || "";
  var initYy = document.body.dataset.yy || "";
  var initMm = document.body.dataset.mm || "";

  document.getElementById("currentMonth").textContent =
    initYy + "년 " + initMm + "월";
  document.getElementById("monthPicker").value = initYy + "-" + initMm;

  /* ════════════════════════════════
       월 피커
    ════════════════════════════════ */
  document
    .getElementById("btnOpenPicker")
    .addEventListener("click", function () {
      var picker = document.getElementById("monthPicker");
      picker.showPicker ? picker.showPicker() : picker.click();
    });

  document
    .getElementById("monthPicker")
    .addEventListener("change", function () {
      var parts = this.value.split("-");
      window.location.href =
        "/student/privacy-print?yy=" + parts[0] + "&mm=" + parts[1];
    });

  /* ════════════════════════════════
       전체 체크박스
    ════════════════════════════════ */
  document.getElementById("checkAll").addEventListener("change", function () {
    var checked = this.checked;
    document
      .querySelectorAll('#pcListBody input[type="checkbox"]')
      .forEach(function (cb) {
        cb.checked = checked;
      });
  });

  /* ════════════════════════════════
       검색
    ════════════════════════════════ */
  document
    .getElementById("btnSearch")
    .addEventListener("click", requestStudentList);
  document
    .getElementById("searchInput")
    .addEventListener("keydown", function (e) {
      if (e.key === "Enter") requestStudentList();
    });

  function requestStudentList() {
    var parts = document.getElementById("monthPicker").value.split("-");
    var search = document.getElementById("searchInput").value.trim();

    fetch("/student/consent/search", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ yy: parts[0], mm: parts[1], search: search }),
    })
      .then(function (res) {
        if (!res.ok) throw new Error(res.status);
        return res.json();
      })
      .then(function (data) {
        renderStudentList(data.response);
      })
      .catch(function () {
        alert("조회 중 오류가 발생했습니다.");
      });
  }

  function renderStudentList(rows) {
    var tbody = document.getElementById("pcListBody");

    if (!rows || rows.length === 0) {
      tbody.innerHTML =
        '<tr><td colspan="6" class="empty-row">조회된 데이터가 없습니다.</td></tr>';
      return;
    }

    var html = "";
    rows.forEach(function (row, idx) {
      html +=
        '<tr class="pc-row" data-id="' +
        row.studentId +
        '">' +
        '<td class="checkbox-group"><input type="checkbox" value="' +
        row.studentId +
        '"></td>' +
        "<td>" +
        (idx + 1) +
        "</td>" +
        "<td>" +
        (row.studentName || "") +
        "</td>" +
        "<td>" +
        (row.parentTel || "") +
        "</td>" +
        "<td>" +
        (row.regDate || "") +
        "</td>" +
        "<td>" +
        (row.className || "") +
        "</td>" +
        "</tr>";
    });
    tbody.innerHTML = html;
  }

  /* ════════════════════════════════
       미리보기
    ════════════════════════════════ */
  document.getElementById("btnPreview").addEventListener("click", function () {
    var selectedIds = Array.from(
      document.querySelectorAll('#pcListBody input[type="checkbox"]:checked'),
    )
      .map(function (cb) {
        return cb.value;
      })
      .filter(Boolean);

    if (selectedIds.length === 0) {
      alert("선택된 항목이 없습니다.");
      return;
    }

    fetch("/student/consent/print-data", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ studentIds: selectedIds }),
    })
      .then(function (res) {
        if (!res.ok) throw new Error(res.status);
        return res.json();
      })
      .then(function (data) {
        renderPreview(data.response.printDataList, data.response.centerInfo);
      })
      .catch(function () {
        alert("조회 중 오류가 발생했습니다.");
      });
  });

  /* ════════════════════════════════
       미리보기 렌더링
    ════════════════════════════════ */
  function renderPreview(docList, centerInfo) {
    var wrap = document.getElementById("pcPreviewWrap");
    var emptyState = document.getElementById("pcEmptyState");

    var html =
      '<div id="pcPrintBtnWrap">' +
      '<button type="button" id="btnPrint">인쇄하기</button>' +
      "</div>";

    docList.forEach(function (doc) {
      html += buildConsentSheet(doc, centerInfo);
    });

    wrap.innerHTML = html;
    wrap.style.display = "block";
    emptyState.style.display = "none";

    document
      .getElementById("btnPrint")
      .addEventListener("click", openPrintWindow);
  }

  /* ════════════════════════════════
       동의서 1장 HTML
    ════════════════════════════════ */
  function buildConsentSheet(doc, centerInfo) {
    var signHtml =
      doc.signatureState === "1" && doc.signaturePath
        ? '<img src="https://hohoeduimg.speedgabia.com/all_pass/' +
          doc.signaturePath +
          '" class="sign-img">'
        : "";

    return (
      '<div class="page">' +
      "<h1>개인정보 수집·활용 동의서</h1>" +
      "<table>" +
      "<colgroup>" +
      '<col style="width:50px"><col style="width:150px">' +
      '<col><col style="width:165px"><col>' +
      "</colgroup>" +
      '<tr class="desc-row"><td colspan="5">' +
      "1. 목적 : 학원 수강, 상담, 학습정보 제공, 공지사항 및 소식 전달" +
      "</td></tr>" +
      '<tr class="desc-row"><td colspan="5">' +
      "2. 수집 항목 : 아래 항목은 학원 등록을 위한 필수적인 항목으로 위의 목적으로만 사용하며<br>" +
      "법령의 근거 없이 제 3자에게 제공하지 않습니다." +
      "</td></tr>" +
      "<tr>" +
      '<td class="vertical" rowspan="6">필수정보</td>' +
      '<td class="label">학생 이름</td>' +
      "<td>" +
      v(doc.studentName) +
      "</td>" +
      '<td class="label">생년월일</td>' +
      "<td>" +
      v(doc.birthDate) +
      "</td>" +
      "</tr>" +
      "<tr>" +
      '<td class="label">학생 연락처</td>' +
      "<td>-</td>" +
      '<td class="label" style="padding:0;">소속<br>' +
      '<span style="font-size:11px;font-weight:normal;">(학교/유치원)</span></td>' +
      '<td><div class="split-inline">' +
      "<span>" +
      v(doc.school) +
      "</span>" +
      (doc.gradeKey
        ? '<span style="font-size:12px;">(' +
          parseInt(doc.gradeKey, 10) +
          "세)</span>"
        : "") +
      "</div>" +
      "</td>" +
      "</tr>" +
      "<tr>" +
      '<td class="label">수강 과목</td>' +
      '<td colspan="3">' +
      '<span class="checkbox ' +
      (doc.subHoho === "1" ? "checked" : "") +
      '"></span> 호호스쿨&nbsp;&nbsp;&nbsp;&nbsp;' +
      '<span class="checkbox ' +
      (doc.subHan === "1" ? "checked" : "") +
      '"></span> 한스쿨&nbsp;&nbsp;&nbsp;&nbsp;' +
      '<span class="checkbox ' +
      (doc.subBook === "1" ? "checked" : "") +
      '"></span> 북스쿨' +
      "</td>" +
      "</tr>" +
      "<tr>" +
      '<td class="label">부모님 연락처</td>' +
      '<td colspan="3">' +
      v(doc.parentTel) +
      "</td>" +
      "</tr>" +
      "<tr>" +
      '<td class="label">주 소</td>' +
      '<td colspan="3">' +
      v(doc.address) +
      "</td>" +
      "</tr>" +
      "<tr>" +
      '<td colspan="4" style="font-family:\'Noto Serif KR\';line-height:1.7;">' +
      "다음 항목은 학원생의 상담, 학습정보의 제공, 공지사항 전달, 소식지 전달 등의 목적으로 사용하며<br>" +
      "제공에 동의하지 않을 경우 사용 목적 서비스가 제한됩니다." +
      '<div class="agree-line">' +
      '선택정보 수집에 동의함 <span class="checkbox checked"></span>' +
      '&nbsp;/&nbsp;동의하지 않음 <span class="checkbox"></span>' +
      "</div>" +
      "</td>" +
      "</tr>" +
      '<tr class="desc-row"><td colspan="5">' +
      "3. 개인정보 보유 및 이용기간<br>" +
      "- 수집된 개인정보는 학원 퇴원 후 1년까지 보유하며 그 이후는 파기합니다.<br>" +
      "- 정보 이용 동의를 철회할 경우 학원 수강중이라도 해당 정보는 삭제합니다." +
      "</td></tr>" +
      "</table>" +
      "<div style=\"margin:12px 0 10px;font-size:13px;font-family:'Noto Serif KR';\">" +
      "※ 만 14세 미만 아동인 경우 반드시 학부모 등 법정대리인의 동의가 필요함." +
      "</div>" +
      "<table>" +
      '<colgroup><col style="width:250px"><col><col style="width:200px"></colgroup>' +
      "<tr>" +
      '<td class="desc-row" colspan="3" style="background:#fafafa;">' +
      "<strong>[법정대리인 동의서]</strong><br>" +
      "본인은 위 미성년자의 법정대리인으로 학원 서비스 이용을 위해 위와 같이<br>" +
      "개인정보를 이용하는데 대하여 동의합니다." +
      "</td>" +
      "</tr>" +
      "<tr>" +
      '<td class="label">법정대리인 성명</td>' +
      '<td colspan="2"><div class="split-inline">' +
      "<span>" +
      v(doc.parentName) +
      "</span>" +
      "<span>(인 / 성명) " +
      signHtml +
      "</span>" +
      "</div></td>" +
      "</tr>" +
      "<tr>" +
      '<td class="label">법정대리인 연락처</td>' +
      '<td colspan="2">' +
      v(doc.parentTel) +
      "</td>" +
      "</tr>" +
      "<tr>" +
      '<td class="label">법정대리인과의 관계</td>' +
      '<td colspan="2">' +
      formatRelation(doc.relationState) +
      "</td>" +
      "</tr>" +
      "</table>" +
      '<div class="bottom-area">' +
      "<div style=\"font-size:13px;font-family:'Noto Serif KR';line-height:1.7;margin-top:12px;\">" +
      "※ 개인정보 제공자가 동의한 내용 외에 다른 목적으로 활용하지 않으며, " +
      "제공된 개인정보는 학원에 열람, 정정, 삭제를 요구할 수 있음.<br>" +
      "「개인정보보호법」 등 관련 법규에 의거 위와 같이 개인정보 수집 및 활용에 동의함." +
      "</div>" +
      '<div class="date">' +
      formatDate(doc.createdAtState) +
      "</div>" +
      '<div class="brand">' +
      '<img src="/image/logo_' +
      centerCode +
      '.png" onerror="this.style.display=\'none\'">' +
      "</div>" +
      "</div>" +
      "</div>"
    );
  }

  /* ════════════════════════════════
       인쇄
    ════════════════════════════════ */
  function openPrintWindow() {
    var sheetsHtml = Array.from(
      document.querySelectorAll("#pcPreviewWrap .page"),
    )
      .map(function (el) {
        return el.outerHTML;
      })
      .join("");

    var iframe = document.createElement("iframe");
    iframe.style.cssText = "position:absolute;width:0;height:0;border:0;";
    document.body.appendChild(iframe);

    var iDoc = iframe.contentDocument || iframe.contentWindow.document;
    iDoc.open();
    iDoc.write(
      [
        "<!DOCTYPE html><html><head>",
        '<meta charset="UTF-8">',
        '<link rel="stylesheet" href="https://fonts.googleapis.com/css2?',
        "family=Noto+Sans+KR:wght@400;600;700;800",
        '&family=Noto+Serif+KR:wght@400;600;700&display=swap">',
        "<style>",
        getPrintCss(),
        "</style>",
        "</head><body>",
        sheetsHtml,
        "</body></html>",
      ].join(""),
    );
    iDoc.close();

    iframe.contentWindow.focus();
    iframe.contentWindow.print();
    iframe.contentWindow.addEventListener("afterprint", function () {
      document.body.removeChild(iframe);
    });
  }

  /* ════════════════════════════════
       헬퍼
    ════════════════════════════════ */
  function v(val) {
    return val != null ? val : "";
  }

  function formatDate(dateStr) {
    if (!dateStr) return "";
    var parts = dateStr.split("-");
    if (parts.length !== 3) return dateStr;
    return parts[0] + "년 " + parts[1] + "월 " + parts[2] + "일";
  }

  function formatRelation(rel) {
    if (!rel) return "";
    if (rel === "mother") return "모";
    if (rel === "father") return "부";
    if (rel === "other") return "기타";
    return rel;
  }

  /* ════════════════════════════════
       인쇄 전용 CSS
    ════════════════════════════════ */
  function getPrintCss() {
    return [
      "@page { size: A4 portrait; margin: 0; }",
      "html, body { margin: 0; padding: 0; background: #fff; }",
      'body { font-family: "Noto Sans KR", sans-serif; color: #111;',
      "       -webkit-print-color-adjust: exact; print-color-adjust: exact; }",
      ".page { width: 794px; min-height: 1123px; padding: 44px; background: #fff;",
      "        box-sizing: border-box; display: flex; flex-direction: column;",
      "        page-break-after: always; }",
      ".page h1 { margin: 0 0 22px; text-align: center; font-size: 30px; font-weight: 800; }",
      ".page table { width: 100%; border-collapse: collapse; table-layout: fixed; margin-bottom: 0; }",
      ".page td { border: 1px solid #666; padding: 12px 14px; vertical-align: middle; font-size: 14px; }",
      '.desc-row td { font-family: "Noto Serif KR", serif; line-height: 1.65;',
      "               background: #fafafa; padding: 12px 16px; }",
      ".vertical { writing-mode: vertical-rl; text-orientation: upright;",
      "             text-align: center; font-weight: 700; background: #f2f2f2; width: 50px; }",
      ".label { background: #f2f2f2; font-weight: 700; text-align: center; }",
      ".checkbox { display: inline-block; width: 16px; height: 16px; border: 1px solid #000;",
      "             vertical-align: -3px; margin-right: 5px; position: relative; background: #fff; }",
      ".checkbox.checked { background: #000; }",
      '.checkbox.checked::after { content: ""; position: absolute; left: 3px; top: 2px;',
      "                           width: 7px; height: 4px;",
      "                           border-left: 2px solid #fff; border-bottom: 2px solid #fff;",
      "                           transform: rotate(-45deg); }",
      ".agree-line { text-align: right; font-weight: 700; margin-top: 10px; }",
      ".split-inline { display: flex; justify-content: space-between;",
      "                align-items: center; position: relative; }",
      ".sign-img { position: absolute; right: 0; top: 50%; transform: translateY(-50%);",
      "             width: 80px; filter: brightness(0) saturate(100%); }",
      ".bottom-area { margin-top: auto; }",
      ".date { margin-top: 18px; text-align: center; font-size: 18px; font-weight: 700; }",
      ".brand { margin-top: 16px; display: flex; justify-content: flex-end; align-items: center; }",
      ".brand img { width: 150px; }",
    ].join("\n");
  }
});
