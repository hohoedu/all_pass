document.addEventListener('DOMContentLoaded', function () {

    /* ════════════════════════════════
       서버 데이터
    ════════════════════════════════ */
    var body = document.body;
    var initYy = body.dataset.yy || '';
    var initMm = body.dataset.mm || '';

    /* ════════════════════════════════
       월 표시 초기화
    ════════════════════════════════ */
    if (initYy && initMm) {
        document.getElementById('currentMonth').textContent = initYy + '년 ' + initMm + '월';
        document.getElementById('monthPickerInput').value = initYy + '-' + initMm;
    }

    /* ════════════════════════════════
       월 피커
    ════════════════════════════════ */
    document.getElementById('openMonthPicker').addEventListener('click', function () {
        var picker = document.getElementById('monthPickerInput');
        picker.showPicker ? picker.showPicker() : picker.click();
    });

    document.getElementById('monthPickerInput').addEventListener('change', function () {
        var parts = this.value.split('-');
        var yy = parts[0];
        var mm = parts[1];
        var search = document.getElementById('search-name').value.trim();
        var url = '/pay/pay-receipt?yy=' + yy + '&mm=' + mm
            + (search ? '&search=' + encodeURIComponent(search) : '');
        window.location.href = url;
    });

    /* ════════════════════════════════
       전체 체크박스
    ════════════════════════════════ */
    document.getElementById('check-all').addEventListener('change', function () {
        document.querySelectorAll('#receipt-list-body input[type="checkbox"]')
            .forEach(function (cb) {
                cb.checked = this.checked;
            }, this);
    });

    /* ════════════════════════════════
       검색 → fetch 실시간 조회
    ════════════════════════════════ */
    document.getElementById('btn-search').addEventListener('click', function () {
        fetchStudentList();
    });

    document.getElementById('search-name').addEventListener('keydown', function (e) {
        if (e.key === 'Enter') fetchStudentList();
    });

    function fetchStudentList() {
        var search = document.getElementById('search-name').value.trim();
        var parts = document.getElementById('monthPickerInput').value.split('-');
        var yy = parts[0];
        var mm = parts[1];

        fetch('/pay/pay-receipt/search', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({yy: yy, mm: mm, search: search})
        })
            .then(function (res) {
                if (!res.ok) throw new Error('서버 오류: ' + res.status);
                return res.json();
            })
            .then(function (data) {
                renderStudentList(data.response);
            })
            .catch(function (err) {
                console.error(err);
                alert('조회 중 오류가 발생했습니다.');
            });
    }

    function renderStudentList(students) {
        var tbody = document.getElementById('receipt-list-body');
        var html = '';

        if (!students || students.length === 0) {
            html = '<tr>'
                + '<td colspan="8" style="text-align:center; padding:30px; color:#bbb; font-size:14px;">'
                + '조회된 데이터가 없습니다.'
                + '</td>'
                + '</tr>';
            tbody.innerHTML = html;
            return;
        }

        students.forEach(function (item, index) {
            html += '<tr class="receipt-row" data-key="' + item.rowKey + '">'
                + '<td class="checkbox-group"><input type="checkbox" value="' + item.rowKey + '"></td>'
                + '<td>' + (index + 1) + '</td>'
                + '<td>' + (item.students[0].studentName || '') + '</td>'
                + '<td>' + (item.students[0].className || '') + '</td>'
                + '<td>' + formatMoney(item.amount) + '원</td>'
                + '<td>' + (item.cardName || '') + '</td>'
                + '<td>' + (item.paidDate || '') + '</td>'
                + '<td>' + (item.payType || '') + '</td>'
                + '</tr>';

            item.students.slice(1).forEach(function (sub) {
                html += '<tr class="receipt-sub-row" data-key="' + item.rowKey + '">'
                    + '<td></td>'
                    + '<td class="sub-icon">└</td>'
                    + '<td>' + (sub.studentName || '') + '</td>'
                    + '<td>' + (sub.className || '') + '</td>'
                    + '<td colspan="4"></td>'
                    + '</tr>';
            });
        });

        tbody.innerHTML = html;
    }

    /* ════════════════════════════════
       미리보기 버튼 → fetch POST
    ════════════════════════════════ */
    document.getElementById('btn-preview').addEventListener('click', function () {
        var selected = Array.from(
            document.querySelectorAll('#receipt-list-body input[type="checkbox"]:checked')
        )
            .map(function (cb) {
                return cb.value;
            })
            .filter(function (v) {
                return !!v;
            });

        if (selected.length === 0) {
            alert('선택된 항목이 없습니다.');
            return;
        }

        fetch('/pay/receipt/print-data', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({rowKeys: selected})
        })
            .then(function (res) {
                if (!res.ok) throw new Error('서버 오류: ' + res.status);
                return res.json();
            })
            .then(function (data) {
                renderReceipts(data.response.printDataList, data.response.centerInfo);
            })
            .catch(function (err) {
                console.error(err);
                alert('조회 중 오류가 발생했습니다.');
            });
    });

    /* ════════════════════════════════
       금액 포맷
    ════════════════════════════════ */
    function formatMoney(val) {
        return Number(val).toLocaleString('ko-KR');
    }

    /* ════════════════════════════════
       사업자등록번호 포맷
    ════════════════════════════════ */
    function formatBizNo(bizNo) {
        if (!bizNo) return '';
        var clean = bizNo.replace(/[^0-9]/g, '');
        return clean.replace(/(\d{3})(\d{2})(\d{5})/, '$1-$2-$3');
    }

    /* ════════════════════════════════
       영수증 셀 1개 HTML 생성
    ════════════════════════════════ */
    function buildReceiptCell(item, centerInfo) {
        var isCard = item.payType === '결제선생' || item.payType === '오프라인 단말기결제';
        var isCash = item.payType === '현금' || item.payType === '계좌이체';
        var monthlyTxt = (item.monthly && parseInt(item.monthly, 10) !== 0)
            ? parseInt(item.monthly, 10) + '개월'
            : (isCard ? '일시불' : '');

        return '<div class="cell">'
            + '<section class="receipt">'
            + '<h1 class="title">매출전표</h1>'
            + '<div class="infoBox">'
            + '<div class="infoLabel">학생정보</div>'
            + '<div class="infoText">' + item.studentInfo + '</div>'
            + '</div>'
            + '<div class="rule"></div>'
            + '<div class="kv"><div class="k">결제수단</div><div class="v">' + item.payType + '</div></div>'
            + '<div class="kv"><div class="k">거래일자</div><div class="v">' + item.paidDate + '</div></div>'
            + (item.cardNum ? '<div class="kv"><div class="k">카드번호</div><div class="v">' + item.cardNum + '</div></div>' : '')
            + (item.apprNum ? '<div class="kv"><div class="k">승인번호</div><div class="v">' + item.apprNum + '</div></div>' : '')
            + '<div class="dash"></div>'
            + '<div class="kv"><div class="k">품목명</div><div class="v">교육비</div></div>'
            + (item.acquirerName ? '<div class="kv"><div class="k">매입사명</div><div class="v">' + item.acquirerName + '</div></div>' : '')
            + (item.cardName ? '<div class="kv"><div class="k">카드사명</div><div class="v">' + item.cardName + '</div></div>' : '')
            + (monthlyTxt ? '<div class="kv"><div class="k">할부개월</div><div class="v">' + monthlyTxt + '</div></div>' : '')
            + (isCash ? '<div class="kv"><div class="k">결제구분</div><div class="v">현금성</div></div>' : '')
            + '<div class="dash"></div>'
            + '<div class="kv"><div class="k">공급가액</div><div class="v">' + formatMoney(item.totalAmount) + '원</div></div>'
            + '<div class="kv"><div class="k">부가세</div><div class="v">0원</div></div>'
            + '<div class="kv"><div class="k">합계</div><div class="v" style="font-weight:900;">' + formatMoney(item.totalAmount) + '원</div></div>'
            + '<div class="rule"></div>'
            + '<div class="footer">'
            + '<div>[가맹점명] ' + (centerInfo.centerName || '') + '</div>'
            + '<div>[대표자명] ' + (centerInfo.directorName || '') + '</div>'
            + '<div>[사업자등록번호] ' + formatBizNo(centerInfo.bizNo) + '</div>'
            + '<div>[주소] ' + (centerInfo.centerAddr || '') + '</div>'
            + '</div>'
            + '</section>'
            + '</div>';
    }

    /* ════════════════════════════════
       전체 렌더링
    ════════════════════════════════ */
    function renderReceipts(data, centerInfo) {
        var pageSize = 4;
        var html = '<div id="receipt-print-btn-wrap">'
            + '<button type="button" id="btn-print-receipt">인쇄하기</button>'
            + '</div>';

        for (var i = 0; i < data.length; i += pageSize) {
            var chunk = data.slice(i, i + pageSize);
            while (chunk.length < pageSize) chunk.push(null);

            html += '<div class="sheet">'
                + chunk.map(function (item) {
                    return item
                        ? buildReceiptCell(item, centerInfo)
                        : '<div class="cell"><section class="receipt"></section></div>';
                }).join('')
                + '</div>';
        }

        var wrap = document.getElementById('receiptSheetWrap');
        var emptyState = document.getElementById('emptyState');

        if (!wrap) {
            console.error('receiptSheetWrap 요소를 찾을 수 없습니다.');
            return;
        }

        wrap.innerHTML = html;
        wrap.style.display = 'block';
        if (emptyState) emptyState.style.display = 'none';

        document.getElementById('btn-print-receipt')
            .addEventListener('click', function () {
                printReceipts(centerInfo);
            });
    }

    /* ════════════════════════════════
       iframe 인쇄
    ════════════════════════════════ */
    function printReceipts(centerInfo) {
        var sheetsHtml = Array.from(
            document.querySelectorAll('#receiptSheetWrap .sheet')
        ).map(function (el) {
            return el.outerHTML;
        }).join('');

        var iframe = document.createElement('iframe');
        iframe.style.cssText = 'position:absolute;width:0;height:0;border:0;';
        document.body.appendChild(iframe);

        var doc = iframe.contentDocument || iframe.contentWindow.document;
        doc.open();
        doc.write(
            '<!DOCTYPE html><html><head>'
            + '<meta charset="UTF-8">'
            + '<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@100..900&display=swap">'
            + '<style>' + getPrintStyle() + '</style>'
            + '</head><body>'
            + sheetsHtml
            + '</body></html>'
        );
        doc.close();

        iframe.contentWindow.focus();
        iframe.contentWindow.print();

        iframe.contentWindow.addEventListener('afterprint', function () {
            document.body.removeChild(iframe);
        });
    }

    /* ════════════════════════════════
       인쇄용 CSS
    ════════════════════════════════ */
    function getPrintStyle() {
        return [
            '@page { size: A4; margin: 0; }',
            'html, body { height: 100%; margin: 0; }',
            'body { font-family: "Noto Sans KR", sans-serif; color: #111;',
            '       -webkit-print-color-adjust: exact; print-color-adjust: exact; }',
            '.sheet { width: 210mm; height: 297mm; box-sizing: border-box; position: relative;',
            '         display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr;',
            '         gap: 6mm; padding: 8mm; page-break-after: always; }',
            '.sheet::before { content: ""; position: absolute; left: 8mm; right: 8mm; top: 50%;',
            '                 border-top: 1px dashed rgba(0,0,0,.35); pointer-events: none; }',
            '.sheet::after  { content: ""; position: absolute; top: 8mm; bottom: 8mm; left: 50%;',
            '                 border-left: 1px dashed rgba(0,0,0,.35); pointer-events: none; }',
            '.cell    { box-sizing: border-box; display: flex; overflow: hidden; }',
            '.receipt { width: 100%; height: 100%; box-sizing: border-box; padding: 4mm 4mm 3.5mm; }',
            '.title   { text-align: center; font-size: 15px; font-weight: 800;',
            '           margin: 0 0 3mm 0; letter-spacing: 3px; }',
            '.infoBox   { background: #f3f4f6; border-radius: 3px; padding: 2.8mm; margin-bottom: 3mm; }',
            '.infoLabel { font-size: 10px; font-weight: 500; margin: 0 0 1.5mm 0; }',
            '.infoText  { font-size: 11px; line-height: 1.5; font-weight: 700; word-break: keep-all; }',
            '.rule { height: 1px; background: rgba(0,0,0,.4); margin: 2mm 0; }',
            '.dash { border-top: 1px dashed rgba(0,0,0,.30); margin: 2mm 0; }',
            '.kv   { display: flex; justify-content: space-between; gap: 3mm;',
            '        font-size: 11px; line-height: 1.7; }',
            '.k    { font-weight: 800; min-width: 16mm; }',
            '.v    { font-weight: 500; text-align: right; word-break: break-all; }',
            '.footer { margin-top: 2mm; font-size: 9px; line-height: 1.6; }'
        ].join('\n');
    }


    /* ════════════════════════════════
        현금 출납부 다운로드
    ════════════════════════════════ */
    document.getElementById('btn-ledger-download').addEventListener('click', function () {
        const parts = document.getElementById('monthPickerInput').value.split('-');
        const yy    = parts[0];
        const mm    = parts[1];
        const centerCode = document.body.dataset.centerCode;

        console.log('현금출납부 다운로드 요청 - yy:', yy, 'mm:', mm);

        fetch('/pay/ledger/download', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ yy: yy, mm: mm, centerCode: centerCode })
        })
            .then(function (res) {
                if (!res.ok) throw new Error('서버 오류: ' + res.status);
                return res.blob();
            })
            .then(function (blob) {
                const url  = URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href     = url;
                link.download = '현금출납부_' + yy + '_' + mm + '.xlsx';
                link.click();
                URL.revokeObjectURL(url);
            })
            .catch(function (err) {
                console.error(err);
                alert('다운로드 중 오류가 발생했습니다.');
            });
    });

});