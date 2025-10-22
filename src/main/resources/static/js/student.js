

document.addEventListener('DOMContentLoaded', applyTfootStripe);

// 사용: initHeaderSort('main', '#main-student-tbody');
function initHeaderSort(prefix, tbodySelector) {
    const h = document.getElementById(`${prefix}-sort-header`);
    if (!h) return;

    const sel = `img[id^="${prefix}-sort-"].svg-sort`;
    const orderMap = {};
    const colType = {2: 'text', 3: 'text', 4: 'date', 5: 'text', 6: 'text', 7: 'text'};
    const koCmp = new Intl.Collator('ko-KR', {numeric: true, sensitivity: 'base'});

    h.addEventListener('click', e => {
        const th = e.target.closest('th');
        if (!th || !h.contains(th)) return;

        const target = th.querySelector(sel);
        if (!target) return;

        // 아이콘 리셋 + 선택
        h.querySelectorAll(sel).forEach(i => i.src = (i.dataset.normal || i.src).replace('sort_checked.svg', 'sort.svg'));
        target.src = (target.dataset.checked || target.src).replace('sort.svg', 'sort_checked.svg');

        // 정렬할 컬럼 번호 (id 예: main-sort-4)
        const col = parseInt(target.id.replace(`${prefix}-sort-`, ''), 10);
        if (!col || col === 1 || col === 8) return;

        orderMap[col] = orderMap[col] === 'asc' ? 'desc' : 'asc';

        sortTbody(tbodySelector, col, colType[col] || 'text', orderMap[col], koCmp);
    });

    function sortTbody(tbodySel, col, type, dir, collator) {
        const tbody = document.querySelector(tbodySel);
        if (!tbody) return;
        const rows = Array.from(tbody.querySelectorAll('tr'));

        const getVal = tr => {
            const td = tr.querySelector(`td:nth-child(${col})`);
            if (!td) return '';
            const raw = (td.getAttribute('data-sort-value') ?? td.textContent ?? '').trim();
            if (type === 'number') {
                const n = Number(raw.replace(/[^\d.-]/g, ''));
                return isNaN(n) ? Number.NEGATIVE_INFINITY : n;
            }
            if (type === 'date') {
                const t = Date.parse(raw.replaceAll('.', '-').replaceAll('/', '-'));
                return isNaN(t) ? -8640000000000000 : t;
            }
            return raw;
        };

        rows.sort((a, b) => {
            const va = getVal(a), vb = getVal(b);
            let cmp = 0;
            if (type === 'number' || type === 'date') cmp = va < vb ? -1 : va > vb ? 1 : 0;
            else cmp = collator.compare(String(va), String(vb));
            return dir === 'asc' ? cmp : -cmp;
        });

        rows.forEach(r => tbody.appendChild(r));

        Array.from(tbody.querySelectorAll('tr')).forEach((tr, i) => {
            const noTd = tr.querySelector('td:nth-child(1)');
            if (noTd) noTd.textContent = String(i + 1);
        });
    }
}

// 페이지에서 호출
document.addEventListener('DOMContentLoaded', () => {
    initHeaderSort('main', '#main-student-tbody');
});

// document.addEventListener('DOMContentLoaded', function () {
//     const form = document.getElementById('inout-form');
//     if (!form) {
//         return;
//     }
//     form.addEventListener('submit', function (e) {
//         e.preventDefault();
//
//         const formData = new FormData(form);
//
//         fetch(form.action, {
//             method: 'POST',
//             body: formData,
//         })
//             .then(response => {
//                 if (response.ok) {
//                     alert("저장되었습니다.");
//                     location.reload();
//                 } else {
//                     alert("저장에 실패했습니다.");
//                 }
//             })
//             .catch(error => {
//                 console.error('오류 발생:', error);
//                 alert("통신 중 오류가 발생했습니다.");
//             });
//     });
// });

let chartInstance = null;

document.addEventListener("DOMContentLoaded", () => {

    const calendarBtn = document.getElementById("calendarBtn");
    const calendarInput = document.getElementById("calendarInput");
    const selectedRange = document.getElementById("selectedDateRange");
    const teacherSelect = document.getElementById("teacher-select");

    if (!calendarBtn || !calendarInput || !selectedRange || !teacherSelect || !exploreBtn) {
        return;
    }

    let startMonth = null;
    let endMonth = null;

    function fetchSnapshotData(params) {
        const userNo = teacherSelect.value;
        if (userNo !== 'all') {
            params.userNo = userNo;
        }

        const queryString = new URLSearchParams(params).toString();

        fetch(`/student/overview/data?${queryString}`)
            .then(res => res.json())
            .then(data => {
                updateTable(data);
                applyTfootStripe();
                updateChartWithTableData(data);
            });
    }


    fetchSnapshotData({period: "1y"});

    document.querySelectorAll("input[name='period']").forEach((radio) => {
        radio.addEventListener("change", function () {
            const period = this.value;
            if (period === "custom") return;
            fetchSnapshotData({period});
        });
    });

    // 📌 달력 버튼 클릭
    calendarBtn.addEventListener("click", () => {
        calendarInput.showPicker?.();
        calendarInput.click();
    });

    // 📌 달력 월 선택 2회 → custom fetch
    calendarInput.addEventListener("change", () => {
        const selected = calendarInput.value;

        if (!startMonth) {
            startMonth = selected;
            selectedRange.textContent = `${startMonth} ~ ?`;
        } else {
            endMonth = selected;

            if (startMonth > endMonth) [startMonth, endMonth] = [endMonth, startMonth];

            selectedRange.textContent = `${startMonth} ~ ${endMonth}`;
            fetchSnapshotData({startYm: startMonth, endYm: endMonth});

            startMonth = null;
            endMonth = null;
        }
    });

    document.querySelector(".explore.common-btn").addEventListener("click", () => {
        const period = document.querySelector("input[name='period']:checked").value;

        if (period === "custom") {
            if (!startMonth || !endMonth) {
                alert("시작월과 종료월을 모두 선택해주세요.");
                return;
            }
            fetchSnapshotData({startYm: startMonth, endYm: endMonth});
        } else {
            fetchSnapshotData({period});
        }
    });
});

function updateTable(data) {
    const tbody = document.getElementById("student-tbody");
    tbody.innerHTML = "";

    data.forEach(item => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
      <td>${item.snapshotYm}</td>
      <td>${item.activeCount}</td>
      <td>${item.entryCount}</td>
      <td>${item.moveInCount}</td>
      <td>${item.restCount}</td>
      <td>${item.moveOutCount}</td>
      <td>${item.withdrawnCount}</td>
    `;
        tbody.appendChild(tr);
    });
}

function updateChartWithTableData(data) {
    const labels = data.map(item => item.snapshotYm);
    const active = data.map(item => item.activeCount);
    const entry = data.map(item => item.entryCount);
    const leave = data.map(item => item.withdrawnCount);

    const datasets = [
        {
            label: "재원",
            data: active,
            borderColor: "#06a645",
            backgroundColor: "#06a645",
            tension: 0,
            pointRadius: 3,
        },
        {
            label: "입회",
            data: entry,
            borderColor: "#35c3e7",
            backgroundColor: "#35c3e7",
            tension: 0,
            pointRadius: 3,
        },
        {
            label: "탈퇴",
            data: leave,
            borderColor: "#ee4d79",
            backgroundColor: "#ee4d79",
            tension: 0,
            pointRadius: 3,
        },
    ];

    if (chartInstance) {
        chartInstance.destroy();
    }

    const ctx = document.getElementById("studentChart").getContext("2d");
    chartInstance = new Chart(ctx, {
        type: "line",
        data: {
            labels: labels,
            datasets: datasets,
        },
        options: {
            responsive: true,
            plugins: {
                legend: {display: false},
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return `${context.dataset.label}: ${context.parsed.y}`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {stepSize: 10},
                    grid: {display: false}
                }
            }
        }
    });
}

function applyTfootStripe() {
    const tbody = document.getElementById('student-tbody');
    const tfootRow = document.querySelector('.table-foot tfoot tr');

    if (tbody && tfootRow) {
        const rowCount = tbody.querySelectorAll('tr').length;
        if (rowCount % 2 === 1) {
            tfootRow.style.backgroundColor = '#ffffff';
        } else {
            tfootRow.style.backgroundColor = '#f4f5f8';
        }
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("joinForm");

    form.addEventListener("submit", async (e) => {
        e.preventDefault(); // 기본 form submit 막기

        // form 데이터를 수집
        const formData = new FormData(form);

        try {
            const response = await fetch(form.action, {
                method: "POST",
                body: formData
            });

            if (!response.ok) throw new Error("서버 오류 발생");

            const result = await response.json();

            // 백엔드의 ApiUtils.success("ok") 응답 기준
            if (result.success && result.response === "ok") {
                alert("가입이 완료되었습니다.");
                window.close(); // 창 닫기
            } else {
                alert("가입 중 오류가 발생했습니다. 다시 시도해 주세요.");
            }

        } catch (error) {
            console.error("가입 요청 실패:", error);
            alert("서버와의 통신에 실패했습니다.");
        }
    });
});
