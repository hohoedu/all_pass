// URL에서 날짜 가져오기
document.addEventListener('DOMContentLoaded', function () {
    const openMonthPicker = document.getElementById('openMonthPicker');
    const monthPickerInput = document.getElementById('monthPickerInput');
    const currentMonthElement = document.getElementById('currentMonth');
    if (!openMonthPicker || !monthPickerInput || !currentMonthElement) return;

    const url = new URL(window.location.href);
    const urlParams = url.searchParams;

    let year = urlParams.get('year');
    let month = urlParams.get('month');
    if (year && month) {
        currentMonthElement.textContent = `${year}년 ${parseInt(month, 10)}월`;
    }
    openMonthPicker.addEventListener('click', () => {
        monthPickerInput.showPicker?.() || monthPickerInput.click();
    });

    monthPickerInput.addEventListener('change', () => {
        console.log(monthPickerInput.value);
        const [selectedYear, selectedMonth] = monthPickerInput.value.split('-');

        let newUrl = '';

        if (url.pathname.includes('timetable')) {
            newUrl = `/class/timetable?year=${selectedYear}&month=${selectedMonth}`;
        } else if (url.pathname.includes('timeview')) {
            newUrl = `/class/timeview?year=${selectedYear}&month=${selectedMonth}&user=2`;
        }
        console.log(newUrl);
        window.location.href = newUrl;
    });
});

// 시간 입력
document.addEventListener('DOMContentLoaded', () => {

    document.body.addEventListener('input', e => {
        if (!e.target.matches('.time_input')) return;

        const input = e.target;
        let v = input.value.replace(/[^0-9]/g, '');
        if (v.length >= 3) {
            v = v.slice(0, 2) + ':' + v.slice(2, 4);
        }
        input.value = v;
    });
});

// 왼쪽 수업이름 파싱
document.addEventListener('DOMContentLoaded', () => {
    const span = document.getElementById('current-selected-class');

    document.querySelectorAll('.time-tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.time-tab-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            updateLabel();
        });
    });

    document.body.addEventListener('change', e => {
        if (!e.target.closest('.time-tab-content')) return;
        if (e.target.matches('input[type=radio]') || e.target.tagName === 'SELECT') {
            updateLabel();
        }
    });

    function updateLabel() {
        const activeTab = document.querySelector('.time-tab-btn.active');
        if (!activeTab) return;

        const dayLabel = activeTab.textContent;
        const container = document.getElementById(activeTab.getAttribute('data-tab'));
        if (!container) return;

        const checked = container.querySelector('input[type=radio]:checked');
        if (!checked) return;
        const row = checked.closest('tr.time-row');
        if (!row) return;

        const subjectSelect = row.querySelector('.basic-select select');
        const unitSelect = row.querySelector('.mini-select select');
        const subject = subjectSelect?.value
            ? subjectSelect.selectedOptions[0].textContent
            : '';
        const unit = unitSelect?.value
            ? unitSelect.selectedOptions[0].textContent
            : '';
        span.textContent = `${dayLabel}요일 ${subject} ${unit}`;
    }

    updateLabel();
});

// 시간표 등록 로직
document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('table_register');
    if (!btn) return;
    const savedPeriod = sessionStorage.getItem('selectedPeriod');
    if (savedPeriod) {
        const activeTab = document.querySelector('.time-tab-content.active');
        const rows = activeTab.querySelectorAll('tr.time-row');
        rows.forEach(row => {
            const p = row.querySelector('td:nth-child(2)').innerText.trim();
            if (p === savedPeriod) {
                row.querySelector('input[type=radio]').checked = true;
            }
        });
        sessionStorage.removeItem('selectedPeriod');
    }
    // 개별 저장
    // btn.addEventListener('click', () => {
    //     const params = new URLSearchParams();

    //     const [yy, mm] = document.getElementById('currentMonth')
    //         .textContent.trim().match(/(\d{4})년\s*(\d{1,2})월/).slice(1, 3)
    //         .map((v, i) => i === 1 ? v.padStart(2, '0') : v);
    //     const activeTab = document.querySelector('.time-tab-content.active');
    //     const dayname = activeTab.id;
    //     const selRadio = activeTab.querySelector('input[type=radio]:checked');
    //     const row = selRadio.closest('tr.time-row');
    //     const periodNo = row.querySelector('td:nth-child(2)').innerText.trim();
    //     const startTime = row.querySelector('.time-start input').value;
    //     const endTime = row.querySelector('.time-end input').value;
    //     const classNo = row.querySelector('select[name="classNo"]').value;
    //     const unitNo = row.querySelector('select[name="unitNo"]').value;
    //     const gradeNo = row.querySelector('select[name="gradeNo"]').value;
    //     const userNo = 2;

    //     params.append('yy', yy);
    //     params.append('mm', mm);
    //     params.append('dayname', dayname);
    //     params.append('periodNo', periodNo);
    //     params.append('startTime', startTime);
    //     params.append('endTime', endTime);
    //     params.append('classNo', classNo);
    //     params.append('unitNo', unitNo);
    //     params.append('gradeNo', gradeNo);
    //     params.append('userNo', userNo);

    //     if (!startTime) { showAlert({ title: '시작 시간을 입력해주세요.' }); return; }
    //     if (!endTime) { showAlert({ title: '종료 시간을 입력해주세요.' }); return; }
    //     if (!classNo) { showAlert({ title: '과목을 선택해주세요.' }); return; }
    //     if (!unitNo) { showAlert({ title: '호수를 선택해주세요.' }); return; }
    //     if (!gradeNo) { showAlert({ title: '연령 혹은 학년을 선택해주세요.' }); return; }

    //     const payload = {
    //         yy,
    //         mm,
    //         dayname,
    //         periodNo,
    //         startTime,
    //         endTime,
    //         classNo,
    //         unitNo,
    //         gradeNo,
    //         userNo
    //     };

    //     fetch('/class/register', {
    //         method: 'POST',
    //         headers: {
    //             'Content-Type': 'application/json',
    //         },
    //         body: JSON.stringify(payload)
    //     })
    //         .then(res => {
    //             if (!res.ok) throw new Error('요청 실패: ' + res.status);
    //             return res.json();
    //         })
    //         .then(json => {
    //             let msg;
    //             if (json.response === 'success-register') {
    //                 msg = '시간표가 생성되었습니다.';
    //             } else if (json.response === 'success-update') {
    //                 msg = '시간표가 수정되었습니다.';
    //             } else {
    //                 return showAlert({ icon: 'error', title: '처리 실패', text: json.error?.message });
    //             }

    //             sessionStorage.setItem('selectedPeriod', periodNo);

    //             return showAlert({ icon: 'success', title: msg })
    //                 .then(() => window.location.reload());
    //         })
    //         .catch(err => {
    //             console.error(err);
    //             showAlert({ icon: 'error', title: '오류', text: err.message });
    //         });
    // });

    btn.addEventListener('click', () => {
        const [yy, mm] = document.getElementById('currentMonth')
            .textContent.trim().match(/(\d{4})년\s*(\d{1,2})월/).slice(1, 3)
            .map((v, i) => i === 1 ? v.padStart(2, '0') : v);

        const allTabs = document.querySelectorAll('.time-tab-content');
        const payloadList = [];
        const userNo = 2;

        for (const tab of allTabs) {
            const dayname = tab.id;

            const rows = tab.querySelectorAll('tr.time-row');
            for (const row of rows) {
                const periodNo = row.querySelector('td:nth-child(2)').innerText.trim();
                const startTime = row.querySelector('.time-start input').value;
                const endTime = row.querySelector('.time-end input').value;
                const classNo = row.querySelector('select[name="classNo"]').value;
                const unitNo = row.querySelector('select[name="unitNo"]').value;
                const gradeNo = row.querySelector('select[name="gradeNo"]').value;

                if (!startTime || !endTime || !classNo || !unitNo || !gradeNo) { continue; }

                payloadList.push({
                    yy, mm, dayname, periodNo,
                    startTime, endTime, classNo, unitNo, gradeNo, userNo
                });
            }
        }

        if (payloadList.length === 0) {
            showAlert({ icon: 'warning', title: '입력된 수업 정보가 없습니다.' });
            return;
        }

        fetch('/class/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payloadList)
        })
            .then(res => {
                if (!res.ok) throw new Error('요청 실패: ' + res.status);
                return res.json();
            })
            .then(json => {
                if (json.response === 'success') {
                    window.clearTimetableSession(); // ✅ 저장 성공 시 sessionStorage 초기화
                    showAlert({ icon: 'success', title: '시간표가 저장되었습니다.' })
                        .then(() => window.location.reload());
                } else {
                    showAlert({ icon: 'error', title: '저장 실패', text: json.error?.message });
                }
            })
            .catch(err => {
                console.error(err);
                showAlert({ icon: 'error', title: '오류', text: err.message });
            });
    });
});



// 학생 추가 로직
document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('add_student_timetable');
    const pendingRows = document.querySelectorAll('.timetable-pending-list');
    if (!btn && !pendingRows === 0) return;

    const savedPeriod = sessionStorage.getItem('selectedPeriod');
    if (savedPeriod) {
        const activeTab = document.querySelector('.time-tab-content.active');
        activeTab.querySelectorAll('tr.time-row').forEach(row => {
            const p = row.querySelector('td:nth-child(2)').innerText.trim();
            if (p === savedPeriod) {
                row.querySelector('input[type=radio]').checked = true;
            }
        });
        sessionStorage.removeItem('selectedPeriod');
    }

    const addStudents = () => {
        const activeContent = document.querySelector('.time-tab-content.active');
        const selRow = activeContent.querySelector('input[type=radio]:checked').closest('tr.time-row');

        if (!selRow) {
            showAlert({ icon: "warning", text: "수업을 선택해주세요." })
        }

        const timeTableNo = selRow.dataset.timetableNo;
        if (!timeTableNo) {
            showAlert({ icon: "warning", text: "수업 정보를 찾을 수 없습니다." })
        }

        const studentRows = Array.from(
            document.querySelectorAll('.stu-chocie-table tbody tr')
        ).filter(tr => tr.querySelector('input[type="checkbox"]').checked);

        if (studentRows.length === 0) {
            showAlert({ icon: 'warning', text: '추가할 학생을 선택해주세요.' });
            return;
        }

        const assignments = studentRows.map(tr => {
            const studentNo = tr.querySelector('input[type="checkbox"]').value;
            const weekNo = tr.querySelector('input[name^="weeks-"]:checked').value;
            return { timeTableNo, studentNo, weekNo };
        });


        fetch('/class/add_student', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ assignments })
        })
            .then(res => {
                if (!res.ok) throw new Error('추가 요청 실패: ' + res.status);
                return res.json();
            })
            .then(apiResult => {
                const periodNo = selRow.querySelector('td:nth-child(2)').innerText.trim();
                sessionStorage.setItem('selectedPeriod', periodNo);
                return showAlert({
                    icon: apiResult.response ? 'success' : 'error',
                    title: apiResult.response ? '학생이 추가되었습니다.' : '등록 실패',
                    text: apiResult.error?.message,
                    showConfirmButton: true,
                    allowOutsideClick: false,
                    allowEscapeKey: false
                });
            }).then(result => {
                if (result.isConfirmed) {
                    window.location.reload();
                }
            })
            .catch(err => {
                console.error(err);
                showAlert({
                    icon: 'error',
                    title: '오류가 발생했습니다.',
                    text: err.message
                });
            });

    }
    if (btn) {
        btn.addEventListener('click', addStudents);
    }

    pendingRows.forEach(row => {
        row.addEventListener('dblclick', () => {

            document.querySelectorAll('.stu-chocie-table input[type="checkbox"]').forEach(cb => cb.checked = false);
            const checkbox = row.querySelector('input[type="checkbox"]');
            if (checkbox) checkbox.checked = true;

            addStudents();
        });
    });
});

// 학생 삭제 로직
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('#assign_delete').forEach(btn => {
        btn.addEventListener('click', () => {
            const assignNo = btn.dataset.assignNo;
            if (!assignNo) return;
            showAlert({
                icon: 'warning',
                title: '정말로 제외하시겠습니까?',
                showCancelButton: true,
                confirmButtonText: '삭제',
                cancelButtonText: '취소',
                allowOutsideClick: false,
                allowEscapeKey: false
            }).then(result => {
                if (!result.isConfirmed) return;

                const params = new URLSearchParams();
                params.append('timeTableAssignNo', assignNo);

                fetch('/class/delete_student', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                    },
                    body: params.toString()
                })
                    .then(res => {
                        if (!res.ok) throw new Error('삭제 요청 실패: ' + res.status);
                        return res.json();
                    })
                    .then(apiResult => {
                        return showAlert({
                            icon: apiResult.response ? 'success' : 'error',
                            title: apiResult.response ? '삭제되었습니다.' : '삭제 실패',
                            text: apiResult.error?.message,
                            showConfirmButton: true,
                            allowOutsideClick: false
                        });
                    })
                    .then(res => {
                        if (res.isConfirmed) {
                            window.location.reload();
                        }
                    })
                    .catch(err => {
                        console.error(err);
                        showAlert({
                            icon: 'error',
                            title: '오류 발생',
                            text: err.message
                        });
                    });
            });
        });
    });
});

(() => {
    let isFormDirty = false;

    function saveInputsToSession() {
        const allTabs = document.querySelectorAll('.time-tab-content');
        const saveData = {};

        for (const tab of allTabs) {
            const dayname = tab.id;
            saveData[dayname] = [];

            const rows = tab.querySelectorAll('tr.time-row');
            for (const row of rows) {
                const periodNo = row.querySelector('td:nth-child(2)').innerText.trim();
                const startTime = row.querySelector('.time-start input').value;
                const endTime = row.querySelector('.time-end input').value;
                const classNo = row.querySelector('select[name="classNo"]').value;
                const unitNo = row.querySelector('select[name="unitNo"]').value;
                const gradeNo = row.querySelector('select[name="gradeNo"]').value;

                saveData[dayname].push({ periodNo, startTime, endTime, classNo, unitNo, gradeNo });
            }
        }

        sessionStorage.setItem('timetableInputs', JSON.stringify(saveData));
        isFormDirty = true;
    }

    function restoreInputsFromSession() {
        const saved = sessionStorage.getItem('timetableInputs');
        if (!saved) return;

        const data = JSON.parse(saved);
        for (const [dayname, rows] of Object.entries(data)) {
            const tab = document.getElementById(dayname);
            if (!tab) continue;

            const trList = tab.querySelectorAll('tr.time-row');
            for (let i = 0; i < trList.length; i++) {
                const row = trList[i];
                const savedRow = rows[i];
                if (!savedRow) continue;

                row.querySelector('.time-start input').value = savedRow.startTime || '';
                row.querySelector('.time-end input').value = savedRow.endTime || '';
                row.querySelector('select[name="classNo"]').value = savedRow.classNo || '';
                row.querySelector('select[name="unitNo"]').value = savedRow.unitNo || '';
                row.querySelector('select[name="gradeNo"]').value = savedRow.gradeNo || '';
            }
        }
    }

    function clearInputsFromSession() {
        sessionStorage.removeItem('timetableInputs');
        isFormDirty = false;
    }

    // 입력 시 저장 감지
    document.addEventListener('input', e => {
        if (e.target.closest('.time-tab-content')) {
            saveInputsToSession();
        }
    });

    // 복원 실행
    document.addEventListener('DOMContentLoaded', restoreInputsFromSession);

    // 이탈 감지 (새로고침, F5, 뒤로가기 등)
    window.addEventListener('beforeunload', (e) => {
        if (isFormDirty) {
            e.preventDefault();
            e.returnValue = '';
        }
    });

    // 저장 성공 시 외부에서 호출할 수 있게 전역에 등록
    window.clearTimetableSession = clearInputsFromSession;
})();