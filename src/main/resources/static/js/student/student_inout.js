// ============================학생관리 전입/전출============================ //

// ====== 선생님 별 필터링 ====== //
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

// ====== 모달 오픈 ====== //
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

// ====== 모달 오픈 ====== //
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
        })
        .catch(err => {
            loading.style.display = 'none';
            errBox.style.display = 'block';
            console.error('transfer fetch error:', err);
        });
}