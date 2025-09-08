// 모달 띄우기
function openModal(row) {
    const studentId = row.getAttribute("data-id");
    document.querySelector('.modal').style.display = 'block';

    fetch(`/student/${studentId}`)
        .then(res => {
            if (!res.ok) throw new Error("서버 오류");
            return res.json();
        })
        .then(data => {
            const s = data.response;
            console.log(s);
            const setElementValue = (selector, value) => {
                document.querySelectorAll(selector).forEach(el => {
                    if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
                        el.value = value;
                    } else {
                        el.innerText = value;
                    }
                });
            };

            setElementValue(".s_student_no", s.studentNo || '');
            setElementValue(".s_name", s.studentName || '');
            setElementValue(".s_subjects", [s.hanClass, s.bookClass].filter(Boolean).join(', '));
            setElementValue(".s_phone", s.parentTel || '');

            const latestDate = new Date(s.entryHanDate) > new Date(s.entryBookDate)
                ? s.entryHanDate : s.entryBookDate;
            setElementValue(".s_entry", formatDateKorean(latestDate));
            setElementValue(".s_school", s.school || '');
            setElementValue(".s_grade", s.grade || '');
            setElementValue(".s_birth", formatDateKorean(s.birth));
            setElementValue(".s_address", s.address || '');
            setElementValue(".s_address_detail", s.addressDetail || '');

            updateStatusButton(s.status);
            const genderButtons = document.querySelectorAll(".s_gender");
            genderButtons.forEach(btn => btn.classList.remove("active"));
            if (s.gender === 'TRUE') {
                genderButtons[0].classList.add("active");
            } else if (s.gender === 'FALSE') {
                genderButtons[1].classList.add("active");
            } else {
                console.log(s.gender);
            }

            fetch("/student/gradeCodes")
                .then(res => res.json())
                .then(codeData => {
                    const gradeSelect = document.querySelector('.grade-select');
                    gradeSelect.innerHTML = "";

                    if (codeData.success && Array.isArray(codeData.response)) {
                        codeData.response.forEach(grade => {
                            const option = document.createElement("option");
                            option.value = grade.gradeNo;
                            option.textContent = grade.grade;

                            if (grade.grade === s.grade) {
                                option.selected = true;
                            }

                            gradeSelect.appendChild(option);
                        });

                    } else {
                        console.error("학년 데이터 오류");
                    }
                })
        })
        .catch(err => {
            console.error(err);
        });
}

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

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('inout-form');
    if (!form) {
        return;
    }
    form.addEventListener('submit', function (e) {
        e.preventDefault();

        const formData = new FormData(form);

        fetch(form.action, {
            method: 'POST',
            body: formData,
        })
            .then(response => {
                if (response.ok) {
                    alert("저장되었습니다.");
                    location.reload();
                } else {
                    alert("저장에 실패했습니다.");
                }
            })
            .catch(error => {
                console.error('오류 발생:', error);
                alert("통신 중 오류가 발생했습니다.");
            });
    });
});


document.addEventListener('click', function (e) {
    const target = e.target.closest('#status-change');
    if (!target) return;

    e.preventDefault();

    const exceptCurrentContainer = document.querySelector('.status-buttons[data-visibility="except-current"]');
    const selectedBtn = exceptCurrentContainer?.querySelector('.select-btn.selected');

    if (!selectedBtn) {
        alert("상태를 선택해주세요.");
        return;
    }

    const studentNo = document.querySelector('.s_student_no').innerText;
    const resonInput = document.getElementById('reason');
    const reason = resonInput?.value;

    if (!reason) {
        alert("사유를 입력해주세요.");
        return;
    }

    const statusNo = selectedBtn.getAttribute('data-status');

    const requestBody = {
        studentNo: studentNo,
        statusNo: statusNo,
        reason: reason
    };

    fetch("/student/status", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(requestBody)
    })
        .then(res => {
            if (!res.ok) throw new Error("요청 실패!");
            return res.json();
        })
        .then(data => {
            alert("상태가 성공적으로 변경되었습니다.");
            document.querySelector('.reason-input')?.classList.remove('active');
            resonInput.value = '';
            document.querySelectorAll('.select-btn').forEach(btn => btn.classList.remove('selected'));

            fetch(`/student/${studentNo}`)
                .then(res => res.json())
                .then(updated => {
                    const s = updated.response;
                    const row = document.querySelector(`[data-id='${studentNo}']`);
                    if (!row) return;
                    openModal(row);
                    const tds = row.querySelectorAll("td");
                    if (tds.length < 8) return;
                    tds[1].textContent = s.studentName;
                    tds[2].textContent = s.statusName;
                    tds[3].textContent = formatDateDot(s.createdAt);
                    tds[4].textContent = [s.hanClass, s.bookClass].filter(Boolean).join(", ");
                    tds[5].textContent = s.grade;
                    tds[6].textContent = s.school;
                    tds[7].textHTML = s.reason == null
                        ? `<img src="image/link.png" class="link" alt="링크">`
                        : '';
                });
        }).catch(err => {
        console.error("요청 오류:", err);
        alert("오류가 발생했습니다.");
    });
});


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

// ============================학생관리 메인============================ //
// 선생님 별 학생 필터링
document.addEventListener("DOMContentLoaded", () => {
    const teacherFilter = document.getElementById("main-teacher-filter");
    const subjectFilter = document.getElementById("main-subject-filter");
    const tbody = document.getElementById("main-student-tbody");
    if (teacherFilter) {
        teacherFilter.addEventListener("change", function () {
            const teacherNo = this.value;

            fetch(`/student/api/label?teacherNo=${encodeURIComponent(teacherNo)}`)
                .then(res => {
                    return res.json();
                })
                .then(data => {
                    subjectFilter.innerHTML = `<option value="all">전체</option>`;
                    data.response.forEach(item => {
                        subjectFilter.innerHTML += `<option value="${item.timeTableCode}">${item.classLabel}</option>`;
                    });
                })
                .catch(err => {
                    subjectFilter.innerHTML = `<option value="all">전체</option>`;
                });

            fetch(`/student/api/students?teacherNo=${encodeURIComponent(teacherNo)}`)
                .then(res => {
                    return res.json();

                })
                .then(data => {
                    console.log('data = ', data.response);
                    console.log('tbody = ', tbody);
                    renderStudents(tbody, data.response);
                });
        });
    }

    if (subjectFilter) {
        subjectFilter.addEventListener("change", function () {
            const classCode = this.value;
            console.log("subject 변경:", classCode);

            fetch(`/student/api/students?classCode=${encodeURIComponent(classCode)}`)
                .then(res => {
                    return res.json();
                })
                .then(data => {
                    console.log('data = ', data);
                    renderStudents(tbody, data.response);
                })
                .catch(err => {
                    subjectFilter.innerHTML = `<option value="all">전체</option>`;
                });
        });
    }
});

// 학생 리스트 변경
function renderStudents(tbody, students = []) {
    tbody.innerHTML = ""; // 기존 내용 초기화

    students.forEach((s, i) => {
        const tr = document.createElement("tr");
        tr.setAttribute("data-id", s.studentNo);
        tr.setAttribute("onclick", "openModal(this)");

        tr.innerHTML = `
      <td>${i + 1}</td>
      <td>${s.studentName ?? ""}</td>
      <td>${s.status ?? ""}</td>
      <td>${s.entryDate ?? ""}</td>
      <td>${[s.hanClass, s.bookClass].filter(Boolean).join(", ")}</td>
      <td>${s.grade ?? ""}</td>
      <td>${s.school ?? ""}</td>
      <td>
        <div class="tooltip-container">
          <img src="/image/link.png" alt="link" class="link">
          <div class="tooltip-text">${s.isSibling === "Y" ? "형제 있음" : "형제 없음"}</div>
        </div>
      </td>
    `;

        tbody.appendChild(tr);
    });
}

// ============================학생관리 전입/전출============================ //
// 선생님 별 필터링 
document.addEventListener("DOMContentLoaded", () => {
    const teacherFilter = document.getElementById("transfer-teacher-filter");
    const subjectFilter = document.getElementById("transfer-subject-filter");

    if (teacherFilter)
        teacherFilter.addEventListener("change", function () {
            const teacherNo = this.value;
            fetch(`/student/api/label?teacherNo=${encodeURIComponent(teacherNo)}`)
                .then(res => {
                    return res.json();
                })
                .then(data => {
                    subjectFilter.innerHTML = `<option value="all">전체</option>`;
                    data.response.forEach(item => {
                        subjectFilter.innerHTML += `<option value="${item.timeTableCode}">${item.classLabel}</option>`;
                    });
                })
                .catch(err => {
                    subjectFilter.innerHTML = `<option value="all">전체</option>`;
                });
        })
})

function clickInOutModal(row) {
    const studentId = row.getAttribute("data-id") || "00";
    console.log(studentId);
    const studentName = row.getAttribute("data-name");
    console.log(studentName);
    document.querySelector('.modal').style.display = 'block';
    const titleEl = document.querySelector('.inout-modal-title');
    titleEl.innerHTML = studentId === '00' ? '전체 전입/전출 내역' : titleEl.innerHTML = studentName + ' 학생 전입/전출 내역'

    fetch(`/student/inout/${studentId}`)
        .then(res => {
            if (!res.ok) throw new Error("서버 오류");
            return res.json();
        })
        .then(data => {
            const histories = data.response;
            console.log(histories);

            const tbody = document.querySelector(".inout-modal-body");
            tbody.innerHTML = '';
            if (!histories || histories.length === 0) {
                const tr = document.createElement("tr");
                tr.innerHTML = `<td colspan="7" style="text-align:center;">전입/전출 내역이 없습니다.</td>`;
                tbody.appendChild(tr);
                return;
            }

            titleEl.innerHTML = studentId === '00'
                ? '전체 전입/전출 내역'
                : `${studentName} 학생 전입/전출 내역`;

            histories.forEach((item, index) => {
                const tr = document.createElement("tr");
                tr.innerHTML = tr.innerHTML = `
          <td>${index + 1}</td>
          <td>${formatDateKorean(item.moveAt)}</td>
          <td>${item.studentName}</td>
          <td>${item.className}</td>
          <td>${item.fromTeacher}</td>
          <td>${item.toTeacher}</td>
          <td>${item.transferReason}</td>
        `;
                tbody.appendChild(tr);
            });
        })
        .catch(err => {
            console.error(err);
        });
}

function openTransferModal(rowEl) {
    const studentNo = rowEl?.dataset?.id;
    const studentName = rowEl?.dataset?.name || '';
    if (!studentNo) return;

    const modal = document.getElementById('transfer-modal');
    if (!modal) return;
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';

    fetch(`/student/transfer/${encodeURIComponent(studentNo)}`)
        .then(res => {
            if (!res.ok) throw new Error('HTTP ' + res.status);
            return res.json();
        })
        .then(data => {
            const list = (data && (data.response ?? data.data ?? data)) || [];
            loading.style.display = 'none';
            if (!Array.isArray(list) || list.length === 0) {
                emptyBox.style.display = 'block';
                return;
            }

            // 안전 렌더링(textContent 사용)
            //   list.forEach((item, idx) => {
            //     // 서버 필드명 예시 가정: move_type, move_at, from_center, to_center, reason
            //     // (다르면 아래 키만 바꿔주세요)
            //     const moveType   = item.moveType ?? item.move_type ?? item.type ?? '';
            //     const moveAt     = item.moveAt   ?? item.move_at   ?? item.movedAt ?? '';
            //     const fromCenter = item.fromCenter ?? item.from_center ?? item.prevCenter ?? '';
            //     const toCenter   = item.toCenter   ?? item.to_center   ?? item.nextCenter ?? '';
            //     const reason     = item.reason ?? '';

            //     const tr = document.createElement('tr');

            //     const tdIdx = document.createElement('td');
            //     tdIdx.textContent = String(idx + 1);

            //     const tdType = document.createElement('td');
            //     tdType.textContent = moveType; // '전입' / '전출' 등

            //     const tdDate = document.createElement('td');
            //     tdDate.textContent = fmtDate(moveAt);

            //     const tdFrom = document.createElement('td');
            //     tdFrom.textContent = fromCenter;

            //     const tdTo = document.createElement('td');
            //     tdTo.textContent = toCenter;

            //     const tdReason = document.createElement('td');
            //     tdReason.textContent = reason;

            //     tr.appendChild(tdIdx);
            //     tr.appendChild(tdType);
            //     tr.appendChild(tdDate);
            //     tr.appendChild(tdFrom);
            //     tr.appendChild(tdTo);
            //     tr.appendChild(tdReason);
            //     tbody.appendChild(tr);
            //   });
        })
        .catch(err => {
            loading.style.display = 'none';
            errBox.style.display = 'block';
            console.error('transfer fetch error:', err);
        });
}