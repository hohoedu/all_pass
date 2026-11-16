document.addEventListener('DOMContentLoaded', () => {
    // ────────────────────────────────────────
    // DOM 참조
    // ────────────────────────────────────────
    const appShell = document.getElementById('appShell');
    const navButtons = document.querySelectorAll('.nav-icon');
    const depthMenus = document.querySelectorAll('.depth-menu');

    const centerSelect = document.getElementById('centerSelect');
    const sourceSelect = document.getElementById('sourceSelect');
    const fileInput = document.getElementById('reconcileFile');
    const fileName = document.getElementById('reconcileFileName');
    const reconcileBtn = document.getElementById('reconcileBtn');
    const reconcileLoading = document.getElementById('reconcileLoading');
    const downloadBtn = document.getElementById('reconcileDownloadBtn');
    const dateRange = document.getElementById('reconcileDateRange');
    const missingDbBody = document.getElementById('missingDbBody');
    const missingFileBody = document.getElementById('missingFileBody');
    const mismatchBody = document.getElementById('mismatchBody');
    const infoBody = document.getElementById('infoBody');

    // ────────────────────────────────────────
    // 상태
    // ────────────────────────────────────────
    let openedMenu = null;
    let lastResult = null;

    // ────────────────────────────────────────
    // 유틸
    // ────────────────────────────────────────
    const won = (n) => `${Number(n || 0).toLocaleString('ko-KR')}원`;

    const escapeHtml = (value) => String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');

    // ────────────────────────────────────────
    // 사이드바
    // ────────────────────────────────────────
    navButtons.forEach((button) => {
        button.addEventListener('click', () => {
            const target = button.dataset.menu;
            const isSameOpenMenu = openedMenu === target && appShell.classList.contains('depth-open');

            if (isSameOpenMenu) {
                appShell.classList.remove('depth-open');
                button.classList.remove('active');
                openedMenu = null;
                return;
            }

            openedMenu = target;
            appShell.classList.add('depth-open');
            navButtons.forEach((item) => item.classList.toggle('active', item === button));
            depthMenus.forEach((menu) => menu.classList.toggle('active', menu.dataset.depth === target));
        });
    });

    // ────────────────────────────────────────
    // 결과 렌더링
    // ────────────────────────────────────────
    const setText = (id, value) => { document.getElementById(id).textContent = value; };

    const renderRows = (tbody, rows, toCells, colspan = 4, emptyText = '일치하지 않는 건이 없습니다.') => {
        tbody.innerHTML = rows && rows.length
            ? rows.map((row) => `<tr>${toCells(row).map((v) => `<td>${escapeHtml(v)}</td>`).join('')}</tr>`).join('')
            : `<tr class="empty-row"><td colspan="${colspan}">${emptyText}</td></tr>`;
    };

    // 금액 요약 : 엑셀 순매출 - DB = 차이 (차이 0 이면 초록, 아니면 빨강 + 설명)
    const renderAmounts = (r) => {
        setText('amtFileNet', won(r.fileNetAmount));
        setText('amtFileDetail', `승인 ${won(r.fileApprovedAmount)} − 취소 ${won(r.fileCanceledAmount)}`);
        setText('amtDb', won(r.dbAmount));
        setText('amtDiff', `${r.diffAmount > 0 ? '+' : ''}${won(r.diffAmount)}`);

        const notes = [];
        if (r.prevCancelAmount) notes.push(`이전 달 승인 취소 −${won(r.prevCancelAmount)} 포함`);
        if (r.unknownCenterAmount) notes.push(`센터 미확인 ${won(r.unknownCenterAmount)} 제외`);
        setText('amtDiffNote', r.diffAmount === 0 ? '엑셀과 DB가 일치합니다' : notes.join(' · '));

        const box = document.getElementById('amtDiffBox');
        box.classList.toggle('ok', r.diffAmount === 0);
        box.classList.toggle('ng', r.diffAmount !== 0);
    };

    const renderResult = (r) => {
        const mismatches = [
            ...(r.cancelNotReflected || []).map((m) => ({ ...m, type: '취소 미반영' })),
            ...(r.amountMismatch || []).map((m) => ({ ...m, type: '금액 불일치' })),
        ];

        renderAmounts(r);
        setText('sumFile', `${r.fileApproved}건${r.fileCanceled ? ` (취소 ${r.fileCanceled})` : ''}`);
        setText('sumDb', r.dbTotal);
        setText('sumMatched', r.matched);
        setText('sumMissingDb', r.missingInDb ? r.missingInDb.length : 0);
        setText('sumMissingFile', r.missingInFile ? r.missingInFile.length : 0);
        setText('sumMismatch', mismatches.length);
        dateRange.textContent = `대조 거래일자 범위 : ${r.dateFrom} ~ ${r.dateTo}`;

        renderRows(missingDbBody, r.missingInDb, (row) => [row.txDate, row.cardName, row.apprNum, row.amount]);
        renderRows(missingFileBody, r.missingInFile, (row) => [row.apprDate, row.apprIssuer, row.apprNum, row.apprState]);
        renderRows(mismatchBody, mismatches,
            (m) => [m.txDate, m.cardName, m.apprNum, won(m.fileAmount), won(m.dbAmount), `${m.type} · ${m.note}`], 6);

        const infos = [
            ...(r.prevMonthCancels || []).map((x) => ['이전 달 승인 취소', x.txDate, x.cardName, x.apprNum, x.amount]),
            ...(r.unknownCenter || []).map((x) => ['센터 미확인 (대조 제외)', x.apprDate, x.apprIssuer, x.apprNum, x.apprPrice]),
        ];
        renderRows(infoBody, infos, (row) => row, 5, '해당 건이 없습니다.');
    };

    // 센터·파일이 바뀌면 이전 결과는 더 이상 유효하지 않음
    const resetResult = () => {
        lastResult = null;
        ['sumFile', 'sumDb', 'sumMatched', 'sumMissingDb', 'sumMissingFile', 'sumMismatch',
            'amtFileNet', 'amtDb', 'amtDiff'].forEach((id) => setText(id, '-'));
        setText('amtFileDetail', '승인 - 취소');
        setText('amtDiffNote', '');
        document.getElementById('amtDiffBox').classList.remove('ok', 'ng');
        dateRange.textContent = '';
        const emptyRow = (colspan) => `<tr class="empty-row"><td colspan="${colspan}">센터와 엑셀 파일을 선택한 뒤 대조를 실행해주세요.</td></tr>`;
        missingDbBody.innerHTML = emptyRow(4);
        missingFileBody.innerHTML = emptyRow(4);
        mismatchBody.innerHTML = emptyRow(6);
        infoBody.innerHTML = emptyRow(5);
    };

    // ────────────────────────────────────────
    // 대조 실행
    // ────────────────────────────────────────
    fileInput.addEventListener('change', () => {
        fileName.textContent = fileInput.files.length ? fileInput.files[0].name : '파일을 선택해주세요 (.xls, .xlsx)';
        fileName.parentElement.classList.toggle('selected', fileInput.files.length > 0);
        resetResult();
    });

    centerSelect.addEventListener('change', resetResult);
    sourceSelect.addEventListener('change', resetResult);

    reconcileBtn.addEventListener('click', async () => {
        if (!centerSelect.value) { alert('센터를 선택해주세요.'); return; }
        if (!fileInput.files.length) { alert('엑셀 파일을 선택해주세요.'); return; }

        const formData = new FormData();
        formData.append('file', fileInput.files[0]);
        formData.append('source', sourceSelect.value);
        formData.append('centerCode', centerSelect.value);

        reconcileLoading.hidden = false;
        reconcileBtn.disabled = true;
        try {
            const res = await fetch('/admin/reconcile', { method: 'POST', body: formData });
            const data = await res.json();
            if (data.success === false) {
                alert(data.msg || '대조에 실패했습니다.');
                return;
            }
            lastResult = data.response;
            renderResult(lastResult);
        } catch (e) {
            alert('대조 처리 중 오류가 발생했습니다.');
        } finally {
            reconcileLoading.hidden = true;
            reconcileBtn.disabled = false;
        }
    });

    // ────────────────────────────────────────
    // 불일치 내역 엑셀 다운로드
    // ────────────────────────────────────────
    downloadBtn.addEventListener('click', async () => {
        if (!lastResult) { alert('먼저 대조를 실행해주세요.'); return; }

        try {
            const res = await fetch('/admin/reconcile/download', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ result: lastResult }),
            });
            if (!res.ok) throw new Error('download failed');

            const url = URL.createObjectURL(await res.blob());
            const centerName = centerSelect.selectedOptions[0]?.textContent.trim() || '';
            const a = document.createElement('a');
            a.href = url;
            a.download = `매출대조_불일치내역${centerName ? `_${centerName}` : ''}.xlsx`;
            document.body.appendChild(a);
            a.click();
            a.remove();
            URL.revokeObjectURL(url);
        } catch (e) {
            alert('엑셀 다운로드에 실패했습니다.');
        }
    });
});
