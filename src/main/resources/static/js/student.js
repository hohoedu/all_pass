document.addEventListener('DOMContentLoaded', applyTfootStripe);

// 사용: initHeaderSort('main', '#main-student-tbody');


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

    if (!calendarBtn || !calendarInput || !selectedRange || !teacherSelect) {
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

    calendarBtn.addEventListener("click", () => {
        calendarInput.showPicker?.();
        calendarInput.click();
    });

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

// //기존 가입 로직
// document.addEventListener("DOMContentLoaded", () => {
//     const form = document.getElementById("joinForm");
//
//     form.addEventListener("submit", async (e) => {
//         e.preventDefault(); // 기본 제출 막기
//
//         // ---------------------------------------------------------
//         // 1) 서명 PNG 변환 (Canvas -> PNG dataURL -> Blob)
//         // ---------------------------------------------------------
//         const canvas = document.getElementById("signature-pad");
//         const dataURL = canvas.toDataURL("image/png"); // PNG 생성
//         const blob = await (await fetch(dataURL)).blob(); // Blob 변환
//
//         // ---------------------------------------------------------
//         // 2) 서버로 PNG 업로드
//         // ---------------------------------------------------------
//         const uploadForm = new FormData();
//         uploadForm.append("file", blob, "signature.png");
//
//         let signaturePath = null;
//
//         try {
//             const uploadResponse = await fetch("/student/upload/signature", {
//                 method: "POST",
//                 body: uploadForm
//             });
//
//             if (!uploadResponse.ok) {
//                 alert("서명 업로드 중 오류가 발생했습니다.");
//                 return;
//             }
//
//             const result = await uploadResponse.json();
//
//             if (!result.success) {
//                 alert("서명 업로드에 실패했습니다.");
//                 return;
//             }
//
//             signaturePath = result.url;
//         } catch (err) {
//             console.error("업로드 오류:", err);
//             alert("서명 업로드 오류");
//             return;
//         }
//
//         // ---------------------------------------------------------
//         // 3) URL을 formData에 포함하여 최종 가입 요청 전송
//         // ---------------------------------------------------------
//         const formData = new FormData(form);
//         formData.append("signature", signaturePath);
//
//         try {
//             const response = await fetch(form.action, {
//                 method: "POST",
//                 body: formData
//             });
//
//             if (!response.ok) {
//                 alert("서버 응답 오류");
//                 return;
//             }
//
//             const joinResult = await response.json();
//
//             if (joinResult.success && joinResult.response === "ok") {
//                 alert("가입이 완료되었습니다.");
//
//                 if (window.opener && !window.opener.closed) {
//                     window.opener.location.reload();
//                 }
//                 window.close(); // 팝업 닫기
//             } else {
//                 alert("가입 중 오류가 발생했습니다.");
//             }
//
//         } catch (err) {
//             console.error("가입 요청 실패:", err);
//             alert("가입 요청 중 오류 발생");
//         }
//     });
// });

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("joinForm");

    function isEmpty(value) {
        return !value || value.trim() === "";
    }


    function isValidBirth(birth) {
        if (!/^[0-9]{6}$/.test(birth)) {
            return {valid: false, msg: "생년월일은 숫자 6자리로 입력해 주세요."};
        }

        const yy = parseInt(birth.substring(0, 2));
        const month = parseInt(birth.substring(2, 4));
        const day = parseInt(birth.substring(4, 6));

        if (month < 1 || month > 12) {
            return {valid: false, msg: "생년월일이 올바르지 않습니다."};
        }

        if (day < 1) {
            return {valid: false, msg: "생년월일이 올바르지 않습니다."};
        }

        const currentYY = new Date().getFullYear() % 100;
        const fullYear = yy <= currentYY ? 2000 + yy : 1900 + yy;

        const lastDay = new Date(fullYear, month, 0).getDate();
        if (day > lastDay) {
            return {valid: false, msg: `${month}월은 ${lastDay}일까지만 있습니다.`};
        }

        const inputDate = new Date(fullYear, month - 1, day);
        if (inputDate > new Date()) {
            return {valid: false, msg: "미래 날짜는 입력할 수 없습니다."};
        }

        return {valid: true};
    }

    function isValidPhone(p1, p2, p3) {
        return p1.length === 3 && p2.length === 4 && p3.length === 4;
    }

    function isSignatureEmpty(canvas) {
        const blank = document.createElement("canvas");
        blank.width = canvas.width;
        blank.height = canvas.height;
        return canvas.toDataURL() === blank.toDataURL();
    }

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const studentName = form.studentName.value;
        const birth = form.birth.value;
        const school = form.school.value;
        const gradeKey = form.gradeKey.value;
        const address = form.address.value;

        const parentName = form.parentName.value;
        const phone1 = form.parentTelFirst.value;
        const phone2 = form.parentTelMiddle.value;
        const phone3 = form.parentTelLast.value;
        const relationKey = form.relationKey.value;

        const privacyAgree = form.studentPrivacyAgree.checked;
        const parentAgree = form.parentPrivacyAgree.checked;

        const canvas = document.getElementById("signature-pad");

        // 학생 정보
        if (isEmpty(studentName)) {
            alert("학생 이름을 입력해 주세요.");
            form.studentName.focus();
            return;
        }

        const birthResult = isValidBirth(birth);
        if (!birthResult.valid) {
            alert(birthResult.msg);
            form.birth.focus();
            return;
        }

        if (isEmpty(school)) {
            alert("학교/유치원명을 입력해 주세요.");
            form.school.focus();
            return;
        }

        if (isEmpty(gradeKey)) {
            alert("연령/학년을 선택해 주세요.");
            form.gradeKey.focus();
            return;
        }

        if (isEmpty(address)) {
            alert("주소를 입력해 주세요.");
            form.address.focus();
            return;
        }

        if (!privacyAgree) {
            alert("학생 개인정보 수집 및 활용에 동의해 주세요.");
            return;
        }

        // 보호자 정보
        if (isEmpty(parentName)) {
            alert("법정대리인 성명을 입력해 주세요.");
            form.parentName.focus();
            return;
        }

        if (!isValidPhone(phone1, phone2, phone3)) {
            alert("법정대리인 연락처를 정확히 입력해 주세요.");
            form.parentTelFirst.focus();
            return;
        }

        if (isEmpty(relationKey)) {
            alert("법정대리인과의 관계를 선택해 주세요.");
            form.relationKey.focus();
            return;
        }

        if (!parentAgree) {
            alert("법정대리인 개인정보 수집 및 활용에 동의해 주세요.");
            return;
        }

        // 서명 체크
        if (isSignatureEmpty(canvas)) {
            alert("서명을 입력해 주세요.");
            return;
        }
        const formData = new FormData(form);

        let studentId = null;

        try {
            const joinResponse = await fetch(form.action, {
                method: "POST",
                body: formData
            });

            const result = await joinResponse.json();
            console.log(JSON.stringify(result, null, 2));
            if (!joinResponse.ok) {
                alert(result.msg || "가입 요청 중 오류가 발생했습니다.");
                return;
            }

            if (!result.success) {
                alert(result.msg || "가입 처리 중 오류가 발생했습니다.");
                return;
            }

            studentId = result.response.studentId;

        } catch (err) {
            console.error("가입 요청 실패:", err);
            alert("가입 요청 중 오류가 발생했습니다.");
            return;
        }

        // studentId 없으면 서명 업로드 불가
        if (!studentId) {
            alert("가입 처리 중 오류가 발생했습니다. 다시 시도해 주세요.");
            return;
        }

        // ---------------------------------------------------------
        // 2) 서명 PNG 변환 (Canvas -> PNG Blob)
        // ---------------------------------------------------------
        const dataURL = canvas.toDataURL("image/png");
        const blob = await (await fetch(dataURL)).blob();

        // ---------------------------------------------------------
        // 3) 서명 업로드
        // ---------------------------------------------------------
        const uploadForm = new FormData();
        uploadForm.append("file", blob, `${studentId}_signature.png`);
        uploadForm.append("studentId", studentId);

        try {
            const uploadResponse = await fetch("/student/upload/signature", {
                method: "POST",
                body: uploadForm
            });

            if (!uploadResponse.ok) {
                alert("서명 업로드 오류");
                return;
            }

            const uploadResult = await uploadResponse.json();

            if (!uploadResult.success) {
                alert("서명 업로드에 실패했습니다.");
                return;
            }

        } catch (err) {
            console.error("서명 업로드 실패:", err);
            alert("서명 업로드 중 오류가 발생했습니다.");
            return;
        }

        alert("가입이 완료되었습니다.");

        if (window.opener && !window.opener.closed) {
            window.opener.location.reload();
        }
        window.close();
    });
});
