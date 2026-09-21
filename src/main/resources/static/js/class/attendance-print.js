/* 출석부 출력 — 선생님/연월 기준 수강생 명단 조회 및 인쇄 */
(function () {
  var DEFAULT_ROWS_PER_PAGE = 21;  // 측정 실패 시 사용할 행 수
  var PAGE_SLACK = 4;              // 인쇄 시 넘침 방지용 여유(px)
  var WEEK_COUNT = 4;              // 주차 수
  var WEEK_SUB_COLS = 2;           // 주차당 기입 칸 수

  /* [{ userCode, teacherName, groups: [{ subject, students: [] }] }] */
  var teachers = [];
  var headerInfo = { yy: '', mm: '' };

  /* ────────── 조회 ────────── */
  document.getElementById('btn-search').addEventListener('click', function () {
    var teacherEl = document.getElementById('filter-teacher');
    var selected = teacherEl.value;
    if (!selected) { alert('선생님을 선택해 주세요.'); return; }

    var yy = document.getElementById('filter-year').value;
    var mm = document.getElementById('filter-month').value;
    headerInfo = { yy: yy, mm: mm };

    /* ALL = 전체 선생님 — 서버에서 선생님 조건 없이 조회한다 */
    var userCode = (selected === 'ALL') ? '' : selected;

    fetch('/class/attendance-print/data', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userCode: userCode, yy: yy, mm: mm })
    })
      .then(function (r) { if (!r.ok) throw r; return r.json(); })
      .then(function (data) {
        teachers = groupByTeacher(data.response || []);
        renderSummary();
        renderPreview();
      })
      .catch(function () { alert('조회 중 오류가 발생했습니다.'); });
  });

  /* 선생님 → 과정(subject) 순서로 묶는다.
     같은 과정이 여러 반으로 나뉘어 있어도 한 과정으로 합산하고,
     중복 배정된 학생은 한 번만 넣는다. */
  function groupByTeacher(rows) {
    var map = {};
    var order = [];

    rows.forEach(function (r) {
      var tKey = r.userCode || '';
      if (!map[tKey]) {
        map[tKey] = {
          userCode: tKey,
          teacherName: r.teacherName || '',
          groups: {},
          groupOrder: []
        };
        order.push(tKey);
      }
      var t = map[tKey];

      var sKey = (r.subject || '').trim();
      if (!t.groups[sKey]) {
        t.groups[sKey] = { subject: sKey, students: [], seen: {} };
        t.groupOrder.push(sKey);
      }
      var g = t.groups[sKey];

      var id = r.studentId || r.studentName;
      if (g.seen[id]) return;
      g.seen[id] = true;
      g.students.push(r.studentName);
    });

    return order.map(function (k) {
      var t = map[k];
      return {
        userCode: t.userCode,
        teacherName: t.teacherName,
        groups: t.groupOrder
          .map(function (s) { return t.groups[s]; })
          .sort(function (a, b) { return compareSubject(a.subject, b.subject); })
      };
    });
  }

  /* 과정명을 논술1 → 논술6 순으로 — 이름 안의 숫자는 숫자로 비교한다 */
  function compareSubject(a, b) {
    var ta = tokenizeSubject(a);
    var tb = tokenizeSubject(b);

    for (var i = 0; i < Math.max(ta.length, tb.length); i++) {
      var x = ta[i];
      var y = tb[i];
      if (x === undefined) return -1;
      if (y === undefined) return 1;

      if (typeof x === 'number' && typeof y === 'number') {
        if (x !== y) return x - y;
      } else {
        var sx = String(x);
        var sy = String(y);
        if (sx !== sy) return sx < sy ? -1 : 1;
      }
    }
    return 0;
  }

  function tokenizeSubject(v) {
    return String(v || '').match(/\d+|\D+/g) ?
      String(v || '').match(/\d+|\D+/g).map(function (part) {
        return /^\d+$/.test(part) ? parseInt(part, 10) : part;
      }) : [];
  }

  /* ────────── 좌측 요약 ────────── */
  function renderSummary() {
    var panel = document.getElementById('summary-panel');

    if (teachers.length === 0) {
      panel.innerHTML = '<div class="att-empty">조회된 데이터가 없습니다.</div>';
      return;
    }

    panel.innerHTML = teachers.map(function (t) {
      var total = t.groups.reduce(function (sum, g) { return sum + g.students.length; }, 0);

      var head = '<div class="att-summary-head">' +
        esc(t.teacherName || t.userCode) + ' 선생님 <span>(' + total + '명)</span></div>';

      var items = t.groups.map(function (g) {
        return '<div class="att-summary-item">' +
          '<span class="s-name">' + esc(g.subject) + '</span>' +
          '<span class="s-count">' + g.students.length + '명</span>' +
          '</div>';
      }).join('');

      return '<div class="att-summary-block">' + head + items + '</div>';
    }).join('');
  }

  /* ────────── 미리보기 렌더링 ────────── */
  function renderPreview() {
    var wrap = document.getElementById('pcPreviewWrap');
    var empty = document.getElementById('pcEmptyState');

    if (teachers.length === 0) {
      wrap.style.display = 'none';
      empty.style.display = 'flex';
      empty.innerHTML = '<i class="fa fa-list-alt"></i><p>조회된 데이터가 없습니다.</p>';
      return;
    }

    empty.style.display = 'none';
    wrap.style.display = 'block';

    var perPage = measureRowsPerPage(wrap);
    var html = '<div id="pcPrintBtnWrap"><button type="button" id="btnPrint">인쇄하기</button></div>';

    /* 출석부는 선생님별로 따로 — 전체 조회여도 장이 섞이지 않는다 */
    teachers.forEach(function (t) {
      var rows = [];
      t.groups.forEach(function (g) {
        g.students.forEach(function (name, i) {
          rows.push({ subject: g.subject, isGroupStart: i === 0, studentName: name });
        });
      });

      var totalPages = Math.max(1, Math.ceil(rows.length / perPage));
      for (var p = 0; p < totalPages; p++) {
        html += buildAttendancePage(
          t, rows.slice(p * perPage, (p + 1) * perPage),
          p * perPage, rows.length, perPage);
      }
    });

    wrap.innerHTML = html;
    document.getElementById('btnPrint').addEventListener('click', openPrintWindow);
  }

  /* A4 한 장에 들어가는 행 수를 실제로 재서 구한다 — 남는 자리는 빈 행으로 채워 페이지를 꽉 채운다 */
  function measureRowsPerPage(wrap) {
    /* 시트 CSS가 #pcPreviewWrap 하위로만 걸려 있어, 측정용 시트도 반드시 그 안에 붙여야 한다 */
    var probe = document.createElement('div');
    probe.style.cssText = 'position:absolute; left:-10000px; top:0; visibility:hidden;';
    probe.innerHTML = buildAttendancePage(
      { teacherName: '측정' }, [{ subject: '측정', studentName: '측정' }], 0, 1, 1);
    wrap.appendChild(probe);

    var rowsPerPage = DEFAULT_ROWS_PER_PAGE;

    try {
      var page = probe.querySelector('.att-page');
      var pageStyle = window.getComputedStyle(page);
      var inner = page.clientHeight
        - parseFloat(pageStyle.paddingTop) - parseFloat(pageStyle.paddingBottom);

      var used = outerHeight(page.querySelector('.att-title'))
        + outerHeight(page.querySelector('.att-meta'))
        + page.querySelector('thead').offsetHeight;

      var rowHeight = page.querySelector('tbody tr').offsetHeight;

      if (rowHeight > 0) {
        var fit = Math.floor((inner - used - PAGE_SLACK) / rowHeight);
        if (fit > 0) rowsPerPage = fit;
      }
    } catch (e) {
      /* 측정 실패 시 기본값 사용 */
    }

    wrap.removeChild(probe);
    return rowsPerPage;
  }

  function outerHeight(el) {
    if (!el) return 0;
    var st = window.getComputedStyle(el);
    return el.offsetHeight + parseFloat(st.marginTop || 0) + parseFloat(st.marginBottom || 0);
  }

  function buildAttendancePage(teacher, pageRows, offset, totalCount, perPage) {
    var body = '';

    for (var i = 0; i < perPage; i++) {
      var row = pageRows[i];
      /* 과정명은 과정이 바뀌는 첫 행에, 그리고 장이 넘어가면 그 장의 첫 행에 다시 찍는다 */
      var showSubject = row && (row.isGroupStart || i === 0);
      body += '<tr>' +
        '<td class="td-no">' + (offset + i + 1) + '</td>' +
        '<td class="td-subject">' + esc(showSubject ? row.subject : '') + '</td>' +
        '<td class="td-name">' + esc(row ? row.studentName : '') + '</td>' +
        repeat('<td></td>', WEEK_COUNT * WEEK_SUB_COLS) +
        '</tr>';
    }

    var weekTh = '';
    var weekSubTh = '';
    for (var w = 1; w <= WEEK_COUNT; w++) {
      weekTh += '<th colspan="' + WEEK_SUB_COLS + '">' + w + '주</th>';
      weekSubTh += repeat('<th></th>', WEEK_SUB_COLS);
    }

    var cellCount = WEEK_COUNT * WEEK_SUB_COLS;
    var cellWidth = (60 / cellCount).toFixed(2);

    return '<div class="att-page">' +
      '<h2 class="att-title">' + esc(headerInfo.yy) + '년 ' + esc(headerInfo.mm) + '월 출석부</h2>' +
      '<div class="att-meta">' +
      '<span class="subject-info">' + esc(teacher.teacherName || teacher.userCode) + ' 선생님' +
      ' <span class="count">(총 인원: ' + totalCount + '명)</span></span>' +
      '</div>' +
      '<table>' +
      '<colgroup>' +
      '<col style="width:8%"><col style="width:17%"><col style="width:15%">' +
      repeat('<col style="width:' + cellWidth + '%">', cellCount) +
      '</colgroup>' +
      '<thead>' +
      '<tr><th rowspan="2">번호</th><th rowspan="2">과정</th><th rowspan="2">성명</th>' + weekTh + '</tr>' +
      '<tr>' + weekSubTh + '</tr>' +
      '</thead>' +
      '<tbody>' + body + '</tbody>' +
      '</table>' +
      '</div>';
  }

  /* ────────── 인쇄 ────────── */
  document.getElementById('btn-print').addEventListener('click', function () {
    if (teachers.length === 0) { alert('조회된 데이터가 없습니다.'); return; }
    openPrintWindow();
  });

  function openPrintWindow() {
    var pages = Array.from(document.querySelectorAll('#pcPreviewWrap .att-page'))
      .map(function (el) { return el.outerHTML; }).join('');

    var iframe = document.createElement('iframe');
    iframe.style.cssText = 'position:fixed;top:0;left:0;width:1px;height:1px;border:0;opacity:0;';
    document.body.appendChild(iframe);

    iframe.onload = function () {
      iframe.contentWindow.focus();
      iframe.contentWindow.print();
      iframe.contentWindow.addEventListener('afterprint', function () {
        document.body.removeChild(iframe);
      });
    };

    var iDoc = iframe.contentDocument || iframe.contentWindow.document;
    iDoc.open();
    iDoc.write(
      '<!DOCTYPE html><html><head>' +
      '<meta charset="UTF-8">' +
      '<style>' + getPrintCss() + '</style>' +
      '</head><body>' + pages + '</body></html>'
    );
    iDoc.close();
  }

  /* ────────── 헬퍼 ────────── */
  function repeat(str, n) {
    var out = '';
    for (var i = 0; i < n; i++) out += str;
    return out;
  }

  function esc(v) {
    if (v == null) return '';
    return String(v)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function getPrintCss() {
    return [
      '@page { size: A4 portrait; margin: 10mm 12mm; }',
      'html, body { margin: 0; padding: 0; background: #fff; }',
      'body { font-family: "Noto Sans KR", sans-serif; color: #111;',
      '       -webkit-print-color-adjust: exact; print-color-adjust: exact; }',
      '.att-page { width: 100%; padding: 0; box-sizing: border-box;',
      '            page-break-after: always; break-after: page; }',
      '.att-page:last-child { page-break-after: avoid; break-after: avoid; }',
      '.att-title { text-align: center; font-size: 24px; font-weight: 900;',
      '             letter-spacing: 2px; margin: 0 0 14px; }',
      '.att-meta { display: flex; justify-content: flex-end; font-size: 13px; margin-bottom: 8px; }',
      '.att-meta .subject-info { font-weight: 700; font-size: 15px; }',
      '.att-meta .count { color: #c0392b; font-size: 13px; }',
      '.att-meta .page-info { color: #6b7280; }',
      'table { width: 100%; border-collapse: collapse; font-size: 12px; }',
      'th, td { border: 1px solid #374151; padding: 6px 4px; text-align: center;',
      '         vertical-align: middle; line-height: 1.4; height: 26px; }',
      'th { background: #d9d9d9; font-weight: 700; }',
      'td.td-no { background: #d9d9d9; font-weight: 700; }',
      'td.td-subject, td.td-name { font-weight: 600; }'
    ].join('\n');
  }

})();
