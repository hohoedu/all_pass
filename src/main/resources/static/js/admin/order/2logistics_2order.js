document.addEventListener('DOMContentLoaded', () => {
    const appShell = document.getElementById('appShell');
    const navButtons = document.querySelectorAll('.nav-icon');
    const depthMenus = document.querySelectorAll('.depth-menu');
    let openedMenu = null;
    let currentCenterInfo = null;
    let lastAggregateSegment = null;
    let lastAggregateCenterCodes = [];

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
    // 조회 세그먼트
    // ────────────────────────────────────────
    document.querySelectorAll('.segmented button').forEach((button) => {
        button.addEventListener('click', () => {
            button.parentElement.querySelectorAll('button').forEach((item) => item.classList.remove('active'));
            button.classList.add('active');
        });
    });

    // ────────────────────────────────────────
    // 명세서 탭
    // ────────────────────────────────────────
    document.querySelectorAll('.statement-card').forEach((card) => {
        card.addEventListener('click', () => {
            card.parentElement.querySelectorAll('.statement-card').forEach((item) => item.classList.remove('active'));
            card.classList.add('active');
            fetchInvoice();
        });
    });

    // ────────────────────────────────────────
    // 체크박스 & 센터 선택
    // ────────────────────────────────────────
    const checkedCenters = new Set();

    document.querySelectorAll('.center-item').forEach((center) => {
        const checkbox = center.querySelector('.check-input');
        const checkLabel = center.querySelector('.check-label');

        checkLabel.addEventListener('click', (e) => {
            e.stopPropagation();
            setTimeout(() => {
                const centerCode = center.dataset.centerCode;
                if (checkbox.checked) checkedCenters.add(centerCode);
                else checkedCenters.delete(centerCode);
            }, 0);
        });

        center.addEventListener('click', (event) => {
            if (event.target.closest('.check-label')) return;
            document.querySelectorAll('.center-item').forEach((item) => item.classList.remove('active'));
            center.classList.add('active');
            applyDeadlineToDisplay(center.dataset.centerCode);
            fetchReorderList();
        });
    });

    // ────────────────────────────────────────
    // 센터 검색
    // ────────────────────────────────────────
    document.querySelector('.search-box input').addEventListener('input', (e) => {
        const keyword = e.target.value.trim().toLowerCase();
        document.querySelectorAll('.center-item').forEach(item => {
            const name = item.querySelector('.center-name').textContent.toLowerCase();
            item.style.display = name.includes(keyword) ? '' : 'none';
        });
    });

    // ────────────────────────────────────────
    // 달력 피커
    // ────────────────────────────────────────
    const openPicker = (picker) => {
        if (typeof picker.showPicker === 'function') picker.showPicker();
        else picker.focus();
    };

    document.querySelectorAll('.calendar-btn').forEach((button) => {
        button.addEventListener('click', () => {
            const picker = document.getElementById(button.dataset.picker);
            openPicker(picker);
        });
    });

    const monthPicker = document.getElementById('monthPicker');
    const monthDisplay = document.getElementById('monthDisplay');
    const deadlinePicker = document.getElementById('deadlinePicker');
    const deadlineDisplay = document.getElementById('deadlineDisplay');

    monthPicker.addEventListener('change', () => {
        const [year, month] = monthPicker.value.split('-');
        monthDisplay.value = `${year}. ${month}`;

        const activeCenter = document.querySelector('.center-item.active');
        if (activeCenter) applyDeadlineToDisplay(activeCenter.dataset.centerCode);

        fetchReorderList();
    });

    deadlinePicker.addEventListener('change', async () => {
        const [year, month, day] = deadlinePicker.value.split('-');
        deadlineDisplay.value = `${year}. ${month}. ${day}`;
        deadlineDisplay.classList.remove('danger');

        const activeCenter = document.querySelector('.center-item.active');
        if (!activeCenter) return;

        const centerCode = activeCenter.dataset.centerCode;
        const centerName = activeCenter.querySelector('.center-name').textContent;
        activeCenter.dataset.deadline = parseInt(day);

        try {
            const res = await fetch('/logis/order/deadline', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({centerCode, deadlineAt: parseInt(day)})
            });
            const data = await res.json();

            if (data.success) alert(`${centerName}의 주문마감일이 변경되었습니다.`);
            else alert(`${centerName}의 주문마감일 변경에 실패했습니다.`);
        } catch (e) {
            alert(`${centerName}의 주문마감일 변경 중 오류가 발생했습니다.`);
        }
    });

    // ────────────────────────────────────────
    // 마감일 표시
    // ────────────────────────────────────────
    const applyDeadlineToDisplay = (centerCode) => {
        const centerEl = document.querySelector(`.center-item[data-center-code="${centerCode}"]`);
        const day = centerEl ? centerEl.dataset.deadline : null;
        if (!day) return;

        const [year, month] = monthPicker.value.split('-');
        const paddedDay = String(day).padStart(2, '0');

        deadlinePicker.value = `${year}-${month}-${paddedDay}`;
        deadlineDisplay.value = `${year}. ${month}. ${paddedDay}`;
        deadlineDisplay.classList.remove('danger');
    };

    // ────────────────────────────────────────
    // 승인 상태 select 색상
    // ────────────────────────────────────────
    const syncStatusSelectColor = (select) => {
        select.classList.remove('wait', 'approved', 'hold', 'rejected');
        if (select.value === 'unchecked') select.classList.add('wait');
        if (select.value === 'checked') select.classList.add('approved');
        if (select.value === 'user_cancel') select.classList.add('hold');
        if (select.value === 'acancel') select.classList.add('rejected');
    };

    // ────────────────────────────────────────
    // 포맷
    // ────────────────────────────────────────
    const formatBizNum = (val) => {
        const n = val.replace(/\D/g, '');
        return n.length === 10 ? `${n.slice(0, 3)}-${n.slice(3, 5)}-${n.slice(5)}` : val;
    };

    const formatNumber = (n) => Math.abs(n).toLocaleString('ko-KR');

    // ────────────────────────────────────────
    // 렌더링
    // ────────────────────────────────────────
    const renderCenterInfo = (info) => {
        if (!info) return;
        document.getElementById('infoCenterName').textContent = info.centerName;
        document.getElementById('infoBizNum').textContent = formatBizNum(info.bizNum);
        document.getElementById('infoDirectorName').textContent = info.directorName;
        document.getElementById('infoAddress').textContent = info.address;
        document.getElementById('infoManagerName').textContent = info.managerName;
        document.getElementById('infoManagerTel').textContent = info.managerTel;
    };

    const renderStatementTabs = (summaryList) => {
        if (!summaryList || summaryList.length === 0) return;
        const summary = summaryList[0];
        const firstCard = document.querySelector('.statement-card');
        firstCard.querySelector('p').textContent = summary.yyMm;
        firstCard.querySelector('small').textContent = `품목 ${summary.itemCount || 0}건 | 전체 ${summary.totalCount || 0}권`;

        const [year, month] = summary.yyMm.split('-');
        document.getElementById('invoiceYyMm').textContent = `${year}. ${month}`;
    };

    const renderReorderTable = (list) => {
        const tbody = document.getElementById('reorderTableBody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            const onlyWait = document.getElementById('onlyWaitCheck').checked;
            const msg = onlyWait ? '미승인 건이 없습니다.' : '데이터가 없습니다.';
            tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:#9a9da7;">${msg}</td></tr>`;
            return;
        }

        list.forEach(item => {
            const stateClass = item.state === 'RETURN' ? 'return' : 'add';
            const stateText = item.state === 'RETURN' ? '반품' : '추가';
            const hasReason = item.reason && item.reason.trim() !== '';
            const tooltipClass = hasReason ? 'has-tooltip' : '';
            const tooltipAttr = hasReason ? `data-tooltip="${item.reason}"` : '';

            const confirmedClass = item.confirmed === 'checked' ? 'approved' :
                item.confirmed === 'unchecked' ? 'wait' :
                    item.confirmed === 'user_cancel' ? 'hold' : 'rejected';

            const tr = document.createElement('tr');
            tr.innerHTML = `
                    <td>
                        <label class="check-label">
                            <input class="check-input" type="checkbox">
                            <span class="check-mark"></span>
                        </label>
                    </td>
                    <td><span class="badge ${stateClass} ${tooltipClass}" ${tooltipAttr}>${stateText}</span></td>
                    <td class="muted">${item.createdAt}</td>
                    <td>${item.userName}</td>
                    <td>${item.className}</td>
                    <td>${item.unitName}</td>
                    <td>${item.cnt}</td>
                    <td>
                        <select class="status-select ${confirmedClass}" data-id="${item.id}">
                            <option value="unchecked"   ${item.confirmed === 'unchecked' ? 'selected' : ''}>미승인</option>
                            <option value="checked"     ${item.confirmed === 'checked' ? 'selected' : ''}>승인</option>
                            <option value="acancel"     ${item.confirmed === 'acancel' ? 'selected' : ''}>관리자 취소</option>
                            <option value="user_cancel" ${item.confirmed === 'user_cancel' ? 'selected' : ''}>사용자 취소</option>
                        </select>
                    </td>
                `;
            tbody.appendChild(tr);
        });

        tbody.querySelectorAll('.status-select').forEach(select => {
            select.addEventListener('change', async () => {
                syncStatusSelectColor(select);

                const id = select.dataset.id;
                const confirmed = select.value;
                const activeCenter = document.querySelector('.center-item.active');
                const centerCode = activeCenter ? activeCenter.dataset.centerCode : null;

                const labelMap = {
                    checked: '승인',
                    unchecked: '미승인',
                    user_cancel: '사용자 취소',
                    acancel: '관리자 취소'
                };

                try {
                    const res = await fetch('/logis/order/confirmed', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify({id, confirmed, centerCode})
                    });
                    const data = await res.json();

                    if (data.success) {
                        alert(`${labelMap[confirmed]}되었습니다.`);
                    } else {
                        alert('변경에 실패했습니다.');
                    }
                } catch (e) {
                    alert('변경 중 오류가 발생했습니다.');
                }
            });
        });
    };

    const renderSummaryBox = (list) => {
        const orderTotal = list.filter(i => i.rowType === 'order').reduce((s, i) => s + i.totalPrice, 0);
        const returnTotal = list.filter(i => i.rowType === 'return').reduce((s, i) => s + Math.abs(i.totalPrice), 0);
        const addTotal = list.filter(i => i.rowType === 'add').reduce((s, i) => s + i.totalPrice, 0);
        const finalTotal = orderTotal - returnTotal + addTotal;

        document.getElementById('summaryOrder').textContent = `${formatNumber(orderTotal)}원`;
        document.getElementById('summaryReturn').textContent = `${formatNumber(returnTotal)}원`;
        document.getElementById('summaryAdd').textContent = `${formatNumber(addTotal)}원`;
        document.getElementById('summaryTotal').textContent = `${formatNumber(finalTotal)}원`;
    };

    const renderInvoiceTable = (list) => {
        const tbody = document.getElementById('invoiceTableBody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:#9a9da7;">데이터가 없습니다.</td></tr>`;
            renderSummaryBox([]);
            return;
        }

        let prevDate = null;
        list.forEach(item => {
            const isNeg = item.totalCount < 0;
            const showDate = item.orderDate !== prevDate ? item.orderDate : '';
            prevDate = item.orderDate;

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${showDate}</td>
                <td><b>${item.className}</b></td>
                <td>${item.unitName}</td>
                <td ${isNeg ? 'class="neg"' : ''}>${item.totalCount}</td>
                <td>${formatNumber(item.unitPrice)}</td>
                <td ${isNeg ? 'class="neg"' : ''}>${isNeg ? '-' : ''}${formatNumber(item.totalPrice)}</td>
                <td class="muted">${item.userName}</td>
            `;
            tbody.appendChild(tr);
        });

        renderSummaryBox(list);
    };

    const renderAggregateLoading = () => {
        showAggregateMode();

        document.getElementById('invoice-heading').textContent = '집계 조회중';
        document.getElementById('invoiceYyMm').textContent = '-';

        ['infoCenterName', 'infoBizNum', 'infoDirectorName', 'infoAddress', 'infoManagerName', 'infoManagerTel']
            .forEach(id => document.getElementById(id).textContent = '-');

        document.getElementById('invoiceTableBody').innerHTML = `
            <tr>
                <td colspan="7" style="text-align:center; color:#9b009b; font-weight:800; padding:60px 0;">
                    <i class="fa-solid fa-spinner fa-spin" style="margin-right:8px;"></i>집계 작업중
                </td>
            </tr>
        `;

        ['summaryOrder', 'summaryReturn', 'summaryAdd', 'summaryTotal']
            .forEach(id => document.getElementById(id).textContent = '-');
    };

    const setInvoiceColgroup = (widths) => {
        const colgroup = document.querySelector('.invoice-table colgroup');
        colgroup.innerHTML = widths.map(w => `<col style="width: ${w}%">`).join('');
    };

    const renderCenterAggregate = (list, segmentType) => {
        const isTeacherMode = segmentType === '센터별 선생님 집계';
        const isAllMode = segmentType === '전체 집계';

        if (isTeacherMode) {
            document.querySelector('.invoice-table thead tr').innerHTML = `
                <th>선생님</th>
                <th>단계</th>
                <th>교재</th>
                <th>학생 수량</th>
                <th>선생님 수량</th>
                <th>추가수량</th>
                <th>합계</th>
                <th>시간표 수량</th>
            `;
            setInvoiceColgroup([12, 12, 16, 14, 14, 12, 10, 10]);
        } else if (isAllMode) {
            document.querySelector('.invoice-table thead tr').innerHTML = `
                <th>단계</th>
                <th>교재</th>
                <th>학생 수량</th>
                <th>선생님 수량</th>
                <th>추가수량</th>
                <th>합계</th>
            `;
            setInvoiceColgroup([15, 25, 18, 14, 14, 14]);
        } else {
            document.querySelector('.invoice-table thead tr').innerHTML = `
                <th>단계</th>
                <th>교재</th>
                <th>학생 수량</th>
                <th>선생님 수량</th>
                <th>추가수량</th>
                <th>합계</th>
                <th>시간표 수량</th>
            `;
            setInvoiceColgroup([13, 22, 15, 14, 12, 12, 12]);
        }

        const colCount = isTeacherMode ? 8 : isAllMode ? 6 : 7;
        const tbody = document.getElementById('invoiceTableBody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="${colCount}" style="text-align:center; color:#9a9da7; padding:40px 0;">데이터가 없습니다.</td></tr>`;
            return;
        }

        list.forEach(center => {
            const centerTr = document.createElement('tr');
            centerTr.innerHTML = `
                <td colspan="${colCount}"
                    style="position:sticky; top:40px; z-index:1;
                           background:#f3f4f8; color:#555864;
                           font-weight:800; text-align:left; padding-left:14px;">
                    ${center.centerName}
                </td>
            `;
            tbody.appendChild(centerTr);

            if (!center.items || center.items.length === 0) {
                const emptyTr = document.createElement('tr');
                emptyTr.innerHTML = `<td colspan="${colCount}" style="text-align:center; color:#9a9da7;">데이터가 없습니다.</td>`;
                tbody.appendChild(emptyTr);
                return;
            }

            let totalBaseCount = 0;
            let totalTeacherCount = 0;
            let totalReorderCount = 0;
            let totalCount = 0;
            let totalTimeTable = 0;

            center.items.forEach((item, idx) => {
                if (isTeacherMode) {
                    const isFirstOfTeacher = idx === 0 || center.items[idx - 1].userName !== item.userName;
                    if (isFirstOfTeacher) {
                        const teacherTr = document.createElement('tr');
                        teacherTr.innerHTML = `
                            <td colspan="${colCount}"
                                style="position:sticky; top:80px; z-index:1;
                                       background:#fbf6ff; color:#9b009b;
                                       font-weight:800; text-align:left; padding-left:24px;">
                                ${item.userName} 선생님
                            </td>
                        `;
                        tbody.appendChild(teacherTr);
                    }
                }

                const isNeg = item.reorderCount < 0;
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    ${isTeacherMode ? `<td>${item.userName}</td>` : ''}
                    <td><b>${item.className}</b></td>
                    <td>${item.unitName}</td>
                    <td>${item.baseCount}</td>
                    <td>${item.teacherCount}</td>
                    <td ${isNeg ? 'class="neg"' : ''}>${item.reorderCount}</td>
                    <td>${item.totalCount}</td>
                    ${isAllMode ? '' : `<td>${item.timeTableCount}</td>`}
                `;
                tbody.appendChild(tr);

                totalBaseCount += item.baseCount;
                totalTeacherCount += item.teacherCount;
                totalReorderCount += item.reorderCount;
                totalCount += item.totalCount;
                totalTimeTable += item.timeTableCount;

                if (isTeacherMode) {
                    const isLastOfTeacher = idx === center.items.length - 1 || center.items[idx + 1].userName !== item.userName;
                    if (isLastOfTeacher) {
                        const sameTeacherItems = center.items.filter(i => i.userName === item.userName);
                        const tBase = sameTeacherItems.reduce((s, i) => s + i.baseCount, 0);
                        const tTeacher = sameTeacherItems.reduce((s, i) => s + i.teacherCount, 0);
                        const tReorder = sameTeacherItems.reduce((s, i) => s + i.reorderCount, 0);
                        const tTotal = sameTeacherItems.reduce((s, i) => s + i.totalCount, 0);
                        const tTime = sameTeacherItems.reduce((s, i) => s + i.timeTableCount, 0);

                        const subTr = document.createElement('tr');
                        subTr.innerHTML = `
                            <td colspan="3"
                                style="font-weight:700; color:#9b009b; background:#fbf6ff; text-align:left; padding-right:14px;">
                                ${item.userName} 합계
                            </td>
                            <td style="font-weight:700; background:#fbf6ff;">${tBase}</td>
                            <td style="font-weight:700; background:#fbf6ff;">${tTeacher}</td>
                            <td style="font-weight:700; background:#fbf6ff; ${tReorder < 0 ? 'color:red;' : ''}">${tReorder}</td>
                            <td style="font-weight:700; background:#fbf6ff;">${tTotal}</td>
                            ${isAllMode ? '' : `<td style="font-weight:700; background:#fbf6ff;">${tTime}</td>`}
                        `;
                        tbody.appendChild(subTr);
                    }
                }
            });

            const totalTr = document.createElement('tr');
            totalTr.innerHTML = `
                <td colspan="${isTeacherMode ? 3 : 2}"
                    style="font-weight:800; color:#555; background:#f8f8fb; text-align:left; padding-right:14px;">
                    ${center.centerName} 합계
                </td>
                <td style="font-weight:800; background:#f8f8fb;">${totalBaseCount}</td>
                <td style="font-weight:800; background:#f8f8fb;">${totalTeacherCount}</td>
                <td style="font-weight:800; background:#f8f8fb; ${totalReorderCount < 0 ? 'color:red;' : ''}">${totalReorderCount}</td>
                <td style="font-weight:800; background:#f8f8fb;">${totalCount}</td>
                ${isAllMode ? '' : `<td style="font-weight:800; background:#f8f8fb;">${totalTimeTable}</td>`}
            `;
            tbody.appendChild(totalTr);
        });
    };

    // ────────────────────────────────────────
    // fetch
    // ────────────────────────────────────────
    const fetchReorderList = async () => {
        const [year, month] = monthPicker.value.split('-');
        const activeCenter = document.querySelector('.center-item.active');
        const centerCode = activeCenter ? activeCenter.dataset.centerCode : null;
        const onlyWait = document.getElementById('onlyWaitCheck').checked;

        document.querySelectorAll('.statement-card').forEach((c, idx) => {
            c.classList.toggle('active', idx === 0);
        });

        const res = await fetch('/logis/order/reorder-list', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({year, month, centerCode, onlyWait})
        });
        const data = await res.json();

        currentCenterInfo = data.response.centerInfo;
        renderStatementTabs(data.response.summaryInvoice);
        renderReorderTable(data.response.reorderList);
        renderCenterInfo(currentCenterInfo);
        fetchInvoice();
    };

    const fetchInvoice = async () => {
        document.querySelector('.invoice-table thead tr').innerHTML = `
            <th>주문일시</th>
            <th>품명</th>
            <th>규격</th>
            <th>수량</th>
            <th>단가</th>
            <th>금액</th>
            <th>비고</th>
        `;

        setInvoiceColgroup([20, 20, 10, 10, 12, 12, 16]);
        const [year, month] = monthPicker.value.split('-');
        const activeCenter = document.querySelector('.center-item.active');
        const centerCode = activeCenter ? activeCenter.dataset.centerCode : null;

        const res = await fetch('/logis/order/invoice', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({year, month, centerCode})
        });
        const data = await res.json();

        showInvoiceMode();
        document.getElementById('invoice-heading').textContent = '거래명세서';
        if (currentCenterInfo) renderCenterInfo(currentCenterInfo);
        renderInvoiceTable(data.response);
    };

    const fetchAggregate = async (segmentType, centerCodes) => {
        lastAggregateSegment = segmentType;
        lastAggregateCenterCodes = centerCodes;

        const [year, month] = monthPicker.value.split('-');

        const res  = await fetch('/logis/order/aggregate', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({year, month, segmentType, centerCodes})
        });
        const data = await res.json();

        document.getElementById('invoice-heading').textContent = segmentType;
        document.getElementById('invoiceYyMm').textContent     = `${year}. ${month}`;

        renderCenterAggregate(data.response, segmentType);
    };

    // ────────────────────────────────────────
    // 조회 버튼
    // ────────────────────────────────────────
    document.querySelector('.primary-btn').addEventListener('click', () => {
        const activeSegment = document.querySelector('.segmented button.active');

        if (!activeSegment) {
            alert('조회 기준을 선택해주세요.');
            return;
        }

        const segmentType = activeSegment.textContent.trim();
        const isAll = segmentType === '전체 집계';

        if (!isAll && checkedCenters.size === 0) {
            alert('조회하실 센터를 체크해주세요.');
            return;
        }

        const orderedCenterCodes = Array.from(document.querySelectorAll('.center-item'))
            .map(item => item.dataset.centerCode)
            .filter(code => checkedCenters.has(code));

        const centerCodes = isAll ? ['all'] : orderedCenterCodes;

        document.querySelectorAll('.statement-card').forEach(c => c.classList.remove('active'));
        renderAggregateLoading();
        fetchAggregate(segmentType, centerCodes);
    });

    document.getElementById('onlyWaitCheck').addEventListener('change', fetchReorderList);

    // ────────────────────────────────────────
    // 모드 전환
    // ────────────────────────────────────────
    const showInvoiceMode = () => {
        document.getElementById('companyInfo').style.display = '';
        document.getElementById('summaryBox').style.display = '';
        document.querySelector('.invoice .table-wrap').style.maxHeight = '400px';
    };

    const showAggregateMode = () => {
        document.getElementById('companyInfo').style.display = 'none';
        document.getElementById('summaryBox').style.display = 'none';
        document.querySelector('.invoice .table-wrap').style.maxHeight = '800px';
    };

    // ────────────────────────────────────────
    // 인쇄
    // ────────────────────────────────────────
    const printBtn = document.querySelector('.print-btn');

    printBtn.addEventListener('click', () => {
        const isAggregateMode = document.getElementById('companyInfo').style.display === 'none';

        if (isAggregateMode) {
            if (!lastAggregateSegment) {
                alert('먼저 조회를 진행해주세요.');
                return;
            }
            const [year, month] = monthPicker.value.split('-');
            printAggregate(year, month, lastAggregateSegment, lastAggregateCenterCodes);
            return;
        }

        const [year, month] = monthPicker.value.split('-');
        const activeCenter  = document.querySelector('.center-item.active');
        const centerCode    = activeCenter ? activeCenter.dataset.centerCode : null;

        if (!centerCode) {
            alert('조회하실 센터를 선택해주세요.');
            return;
        }

        printInvoice(year, month, centerCode);
    });

    function printInvoice(year, month, centerCode) {
        const iframe = document.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = `/admin/order/print-invoice?year=${year}&month=${month}&centerCode=${centerCode}`;

        iframe.onload = () => {
            iframe.contentWindow.print();
        };

        document.body.appendChild(iframe);
    }

    function printAggregate(year, month, segmentType, centerCodes) {
        const iframe = document.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = `/admin/order/print-aggregate?year=${year}&month=${month}&segmentType=${encodeURIComponent(segmentType)}&centerCodes=${centerCodes.join(',')}`;

        iframe.onload = () => {
            iframe.contentWindow.print();
        };

        document.body.appendChild(iframe);
    }

    // ────────────────────────────────────────
    // 엑셀
    // ────────────────────────────────────────
    const excelBtn = document.querySelector('.excel-btn');

    excelBtn.addEventListener('click', () => {
        const isAggregateMode = document.getElementById('companyInfo').style.display === 'none';

        if (isAggregateMode) {
            if (!lastAggregateSegment) {
                alert('먼저 조회를 진행해주세요.');
                return;
            }
            const [year, month] = monthPicker.value.split('-');
            window.location.href = `/admin/order/aggregate-excel?year=${year}&month=${month}&segmentType=${encodeURIComponent(lastAggregateSegment)}&centerCodes=${lastAggregateCenterCodes.join(',')}`;
            return;
        }

        const [year, month] = monthPicker.value.split('-');
        const activeCenter  = document.querySelector('.center-item.active');
        const centerCode    = activeCenter ? activeCenter.dataset.centerCode : null;

        if (!centerCode) {
            alert('조회하실 센터를 선택해주세요.');
            return;
        }

        window.location.href = `/admin/order/invoice-excel?year=${year}&month=${month}&centerCode=${centerCode}`;
    });

    // ────────────────────────────────────────
    // 초기 상태
    // ────────────────────────────────────────
    document.getElementById('reorderTableBody').innerHTML =
        `<tr><td colspan="8" style="text-align:center; color:#9a9da7;">조회하실 센터를 선택해주세요.</td></tr>`;
    document.getElementById('invoiceTableBody').innerHTML =
        `<tr><td colspan="7" style="text-align:center; color:#9a9da7;">조회하실 센터를 선택해주세요.</td></tr>`;
});