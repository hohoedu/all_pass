document.addEventListener('DOMContentLoaded', () => {

    const monthPicker = document.getElementById('monthPicker');
    const monthDisplay = document.getElementById('monthDisplay');
    const centerTableBody = document.getElementById('centerTableBody');
    const unpaidTableBody = document.getElementById('unpaidTableBody');
    const unpaidSearch = document.getElementById('unpaidSearch');
    const unpaidCenterSelect = document.getElementById('unpaidCenterSelect');
    const selectedCenterName = document.getElementById('selectedCenterName');
    const excelDownloadBtn = document.getElementById('excelDownloadBtn');

    const STATUS_LABEL = {
        unbilled: '미청구',
        issued: '청구중',
        expired: '기한만료'
    };

    let centers = [];
    let unpaidStudents = [];
    let selectedCenterCode = '';
    let sortKey = 'centerName';
    let sortDir = 'asc';

    // ────────────────────────────────────────
    // 데이터 조회
    // ────────────────────────────────────────
    const loadData = async () => {
        const [year, month] = monthPicker.value.split('-');

        try {
            const res = await fetch(`/admin/center/status/data?year=${year}&month=${month}`);
            const json = await res.json();
            const data = json.response || {};
            centers = data.centers || [];
            unpaidStudents = data.unpaidStudents || [];
        } catch (e) {
            console.error(e);
            alert('센터별 현황을 불러오지 못했습니다.');
            centers = [];
            unpaidStudents = [];
        }

        if (!centers.some(c => c.centerCode === selectedCenterCode)) selectedCenterCode = '';

        renderSummary();
        renderCenterSelect();
        renderCenterTable();
        renderUnpaidTable();
    };

    // ────────────────────────────────────────
    // 공통
    // ────────────────────────────────────────
    const fmt = (n) => Number(n || 0).toLocaleString('ko-KR');
    const fmtRate = (r) => `${(r || 0).toFixed(1)}%`;

    // ────────────────────────────────────────
    // 요약 카드
    // ────────────────────────────────────────
    const renderSummary = () => {
        const totalStudent = centers.reduce((s, c) => s + c.studentCount, 0);
        const totalNew = centers.reduce((s, c) => s + c.newCount, 0);
        const totalLeave = centers.reduce((s, c) => s + c.leaveCount, 0);
        const totalUnpaid = centers.reduce((s, c) => s + c.unpaidCount, 0);
        const totalAmount = centers.reduce((s, c) => s + c.unpaidAmount, 0);
        const rate = totalStudent ? totalUnpaid / totalStudent * 100 : 0;

        document.getElementById('sumCenterCount').innerHTML = `${fmt(centers.length)}<small>곳</small>`;
        document.getElementById('sumStudentCount').innerHTML = `${fmt(totalStudent)}<small>명</small>`;
        document.getElementById('sumStudentSub').textContent = `신규 ${fmt(totalNew)} · 퇴원 ${fmt(totalLeave)}`;
        document.getElementById('sumUnpaidCount').innerHTML = `${fmt(totalUnpaid)}<small>명</small>`;
        document.getElementById('sumUnpaidRate').textContent = `미납률 ${fmtRate(rate)}`;
        document.getElementById('sumUnpaidAmount').innerHTML = `${fmt(totalAmount)}<small>원</small>`;

        document.getElementById('footStudent').textContent = fmt(totalStudent);
        document.getElementById('footNew').textContent = fmt(totalNew);
        document.getElementById('footLeave').textContent = fmt(totalLeave);
        document.getElementById('footUnpaid').textContent = fmt(totalUnpaid);
        document.getElementById('footRate').textContent = fmtRate(rate);
    };

    // ────────────────────────────────────────
    // 센터별 인원수 테이블
    // ────────────────────────────────────────
    const sortedCenters = () => {
        const dir = sortDir === 'asc' ? 1 : -1;
        return [...centers].sort((a, b) => {
            const av = a[sortKey];
            const bv = b[sortKey];
            if (typeof av === 'string') return av.localeCompare(bv, 'ko') * dir;
            return (av - bv) * dir;
        });
    };

    const renderCenterTable = () => {
        centerTableBody.innerHTML = sortedCenters().map(c => {
            const high = c.unpaidRate >= 5;
            return `
                <tr data-center-code="${c.centerCode}" class="${c.centerCode === selectedCenterCode ? 'active' : ''}">
                    <td>${c.centerName}</td>
                    <td>${fmt(c.studentCount)}</td>
                    <td>${fmt(c.newCount)}</td>
                    <td>${fmt(c.leaveCount)}</td>
                    <td class="${c.unpaidCount > 0 ? 'cs-num-warn' : ''}">${fmt(c.unpaidCount)}</td>
                    <td>
                        <div class="cs-rate">
                            <div class="cs-rate-bar ${high ? 'high' : ''}">
                                <span style="width:${Math.min(c.unpaidRate * 5, 100)}%"></span>
                            </div>
                            <span class="cs-rate-text">${fmtRate(c.unpaidRate)}</span>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        document.querySelectorAll('.cs-sortable').forEach(th => {
            th.classList.remove('asc', 'desc');
            if (th.dataset.sort === sortKey) th.classList.add(sortDir);
        });
    };

    const renderCenterSelect = () => {
        unpaidCenterSelect.innerHTML = '<option value="">전체 센터</option>' +
            centers.map(c => `<option value="${c.centerCode}">${c.centerName}</option>`).join('');
        unpaidCenterSelect.value = selectedCenterCode;
    };

    // ────────────────────────────────────────
    // 교재비 미납 학생 테이블
    // ────────────────────────────────────────
    const renderUnpaidTable = () => {
        const keyword = unpaidSearch.value.trim();
        const rows = unpaidStudents
            .filter(u => !selectedCenterCode || u.centerCode === selectedCenterCode)
            .filter(u => !keyword || (u.studentName || '').includes(keyword) || (u.bookName || '').includes(keyword));

        const center = centers.find(c => c.centerCode === selectedCenterCode);
        selectedCenterName.textContent = center ? center.centerName : '전체 센터';

        if (rows.length === 0) {
            unpaidTableBody.innerHTML = '<tr class="cs-empty"><td colspan="9">교재비 미납 학생이 없습니다.</td></tr>';
        } else {
            unpaidTableBody.innerHTML = rows.map((u, i) => `
                <tr>
                    <td>${i + 1}</td>
                    <td>${u.centerName}</td>
                    <td>${u.studentName}</td>
                    <td>${u.gradeName || '-'}</td>
                    <td title="${u.teacherName || ''}">${u.teacherName || '-'}</td>
                    <td title="${u.bookName || ''}">${u.bookName || '-'}</td>
                    <td class="cs-amount">${fmt(u.amount)}원</td>
                    <td>${u.ym}</td>
                    <td><span class="cs-badge ${u.status}">${STATUS_LABEL[u.status]}</span></td>
                </tr>
            `).join('');
        }

        document.getElementById('unpaidListCount').textContent = fmt(rows.length);
        document.getElementById('unpaidListAmount').textContent = fmt(rows.reduce((s, u) => s + u.amount, 0));
    };

    const selectCenter = (centerCode) => {
        selectedCenterCode = centerCode;
        unpaidCenterSelect.value = centerCode;
        renderCenterTable();
        renderUnpaidTable();
    };

    // ────────────────────────────────────────
    // 이벤트
    // ────────────────────────────────────────
    centerTableBody.addEventListener('click', (e) => {
        const tr = e.target.closest('tr[data-center-code]');
        if (!tr) return;
        const code = tr.dataset.centerCode;
        selectCenter(code === selectedCenterCode ? '' : code);
    });

    document.querySelectorAll('.cs-sortable').forEach(th => {
        th.addEventListener('click', () => {
            const key = th.dataset.sort;
            if (sortKey === key) {
                sortDir = sortDir === 'asc' ? 'desc' : 'asc';
            } else {
                sortKey = key;
                sortDir = key === 'centerName' ? 'asc' : 'desc';
            }
            renderCenterTable();
        });
    });

    unpaidCenterSelect.addEventListener('change', () => selectCenter(unpaidCenterSelect.value));
    unpaidSearch.addEventListener('input', renderUnpaidTable);

    excelDownloadBtn.addEventListener('click', () => {
        // TODO: 데이터 연동 시 엑셀 다운로드 구현
        alert('엑셀 다운로드는 데이터 연동 후 제공됩니다.');
    });

    // ────────────────────────────────────────
    // 달력 피커
    // ────────────────────────────────────────
    document.querySelectorAll('.calendar-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const picker = document.getElementById(btn.dataset.picker);
            if (typeof picker.showPicker === 'function') picker.showPicker();
            else picker.focus();
        });
    });

    monthPicker.addEventListener('change', () => {
        const [year, month] = monthPicker.value.split('-');
        monthDisplay.textContent = `${year}. ${month}`;
        loadData();
    });

    loadData();
});
