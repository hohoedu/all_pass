const DAY_ORDER = ['mon', 'tue', 'wed', 'thu', 'fri', 'sat'];
const DAY_LABEL = { mon: '월', tue: '화', wed: '수', thu: '목', fri: '금', sat: '토' };

let currentReqDTO = null; // 현재 선택된 조회 조건 저장

// 조회 버튼
function handleSearch() {
    const userCode = document.getElementById('teacher-select').value;
    const monthVal = document.getElementById('monthPicker').value;

    if (!userCode) { alert('선생님을 선택해주세요.'); return; }
    if (!monthVal) { alert('수강월을 선택해주세요.'); return; }

    const [yy, mm] = monthVal.split('-');
    currentReqDTO = { userCode, yy, mm };

    fetch('/class/edu-check', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(currentReqDTO)
    })
        .then(res => res.json())
        .then(data => {
            const exists = data.response.existingUserCodes.length > 0;

            if (exists) {
                const choice = confirm('해당 선생님의 수업 정보가 이미 존재합니다.' +
                    '\n\n[확인] → 현재 저장된 정보로 시간표를 조회합니다.' +
                    '\n[취소] → 기존 정보를 삭제하고 새로 생성(복사)합니다.');
                console.log(choice);
                if (choice) {
                    handleView();
                } else {
                    generate();
                }
            } else {
                generate();  // 바로 생성
            }
        });
}



// 재생성
function handleRegenerate() {
    closeEduConfirmModal();
    generate();
}

// 보기
function handleView() {
    closeEduConfirmModal();
    fetch('/class/edu-timeview/data', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(currentReqDTO)
    })
        .then(res => res.json())
        .then(data => renderTimetable(data.response));
}

// 생성
function generate() {
    fetch('/class/edu-generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(currentReqDTO)
    })
        .then(res => res.json())
        .then(data => renderTimetable(data.response));
}

// 시간표 렌더링
function renderTimetable(tables) {
    if (!tables || tables.length === 0) {
        document.getElementById('edu-timetable-body').innerHTML =
            '<tr><td colspan="7" style="text-align:center; color:#999; padding:40px;">시간표 데이터가 없습니다.</td></tr>';
        return;
    }

    // periodNo 재정렬 (요일별 startTime 순)
    const byDay = {};
    tables.forEach(tt => {
        if (!byDay[tt.dayname]) byDay[tt.dayname] = [];
        byDay[tt.dayname].push(tt);
    });
    Object.values(byDay).forEach(list => {
        list.sort((a, b) => a.startTime.localeCompare(b.startTime));
        list.forEach((tt, i) => tt.periodNo = String(i + 1));
    });

    const maxPeriod = Math.max(...tables.map(t => Number(t.periodNo)));

    let html = '';
    for (let p = 1; p <= maxPeriod; p++) {
        html += `<tr><td>${p}</td>`;
        DAY_ORDER.forEach(day => {
            const tt = (byDay[day] || []).find(t => Number(t.periodNo) === p);
            if (!tt) {
                html += `<td style="height:150px;"></td>`;
                return;
            }
            const color = tt.classType === '1' ? 'pink' : 'blue';
            html += `
                <td style="height:150px;">
                    <div class="timetable-lookup-box ${color}">
                        <div class="header">
                            ${tt.startTime} ~ ${tt.endTime}<br>
                            <strong>${tt.className}</strong>
                        </div>
                        <div class="inner-grid">
                            ${(tt.students || []).map(s => `<div>${s.studentName}</div>`).join('')}
                        </div>
                    </div>
                </td>`;
        });
        html += `</tr>`;
    }

    document.getElementById('edu-timetable-body').innerHTML = html;
}

function closeEduConfirmModal() {
    document.getElementById('eduConfirmModal').style.display = 'none';
}

function printEduTimetable() {
    if (!currentReqDTO) {
        alert('먼저 시간표를 조회해주세요.');
        return;
    }
    const { userCode, yy, mm } = currentReqDTO;
    // window.open(`/class/edu-print-timeview?userCode=${userCode}&yy=${yy}&mm=${mm}`, '_blank');
    printEduTimeView(yy, mm, userCode);
}

function printEduTimeView(yy, mm, userCode) {
    const iframe = document.createElement('iframe');
    iframe.style.display = 'none';
    iframe.src = `/class/edu-print-timeview?yy=${yy}&mm=${mm}&userCode=${userCode}`;

    iframe.onload = () => {
        iframe.contentWindow.print();
    };

    document.body.appendChild(iframe);
}