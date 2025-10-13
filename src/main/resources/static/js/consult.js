// 상담 기록 추가 모달 띄우기
document.addEventListener('DOMContentLoaded', () => {
    const consultAddBtn = document.querySelector('.consult-add');
    const modal = document.querySelector('.consult-modal');
    const closeBtn = modal.querySelector('.btn-close');

    consultAddBtn.addEventListener('click', () => {
        modal.style.display = 'block';
    });

    closeBtn.addEventListener('click', (e) => {
        e.preventDefault();
        modal.style.display = 'none';
    });

    window.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.style.display = 'none';
        }
    });
});

// 상담 기록 추가
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.consult-modal .save-btn').forEach(button => {
        button.addEventListener('click', () => {
            const modal = button.closest('.consult-modal');
            const data = {
                studentName: modal.querySelector('[name="studentName"]')?.value.trim(),
                consultDate: modal.querySelector('[name="consultDate"]')?.value,
                school: modal.querySelector('[name="school"]')?.value.trim(),
                gradeKey: modal.querySelector('[name="gradeKey"]')?.value,
                phone: modal.querySelector('[name="parentPhone"]').value.replace(/-/g, ''),
                inflowRouteKey: modal.querySelector('[name="inflowRouteKey"]')?.value,
                content: modal.querySelector('[name="content"]')?.value.trim()
            };
            console.log(data);

            fetch('/consult/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
                .then(res => {
                    if (res.ok) {
                        alert('저장 완료');
                        location.reload();
                    } else {
                        alert('저장 실패');
                    }
                })
                .catch(err => {
                    console.error(err);
                    alert('에러 발생');
                });
        });
    });
});

// 상담 기록 날짜 선택
document.addEventListener('DOMContentLoaded', () => {
    const modal = document.querySelector('.consult-modal');
    const dateInput = modal.querySelector('input[name="consultDate"]');
    const display = modal.querySelector('.day-display');
    const calendarBtn = modal.querySelector('.birth-btn');

    calendarBtn.addEventListener('click', () => {
        dateInput.showPicker?.();
        dateInput.click();
    });

    dateInput.addEventListener('change', () => {
        const selected = dateInput.value;
        if (selected) {
            const [year, month, day] = selected.split('-');
            display.textContent = `${year}년 ${month}월 ${day}일`;
        } else {
            display.textContent = '';
        }
    });
});

// 상담기록 전화번호 포맷 변경
// 상담기록 전화번호 입력 (자동 010 붙이기 + 포맷)
document.addEventListener('DOMContentLoaded', () => {
    const modal = document.querySelector('.consult-modal');
    const phoneInput = modal.querySelector('input[name="parentPhone"]');

    phoneInput.addEventListener('focus', () => {
        // 포커스 시 자동 010 붙이기
        if (!phoneInput.value.trim()) {
            phoneInput.value = '010-';
        }
    });

    phoneInput.addEventListener('input', () => {
        // 숫자만 추출
        let raw = phoneInput.value.replace(/\D/g, '');

        // 010이 없으면 자동으로 추가
        if (!raw.startsWith('010')) {
            raw = '010' + raw;
        }

        // 최대 11자리로 제한
        if (raw.length > 11) raw = raw.slice(0, 11);

        // 하이픈 자동 추가
        let formatted = raw;
        if (raw.length <= 7) {
            formatted = raw.replace(/(\d{3})(\d{0,4})/, '$1-$2');
        } else {
            formatted = raw.replace(/(\d{3})(\d{4})(\d{0,4})/, '$1-$2-$3');
        }

        phoneInput.value = formatted;
    });

    // 백스페이스로 010 전체 삭제 방지
    phoneInput.addEventListener('keydown', (e) => {
        if ((e.key === 'Backspace' || e.key === 'Delete') && phoneInput.value.replace(/\D/g, '').length <= 3) {
            e.preventDefault();
        }
    });
});



document.addEventListener("DOMContentLoaded", () => {
    const radios = document.querySelectorAll('input[name="period"]');
    const periodDisplay = document.getElementById('period-display');
    const calendarBtn = document.getElementById('calendar-btn');
    const calendarInput = document.getElementById('calendar-input');
    const consultTableBody = document.querySelector('.consult-table tbody');

    // 📆 yyyy-MM 포맷
    function formatYM(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        return `${year}-${month}`;
    }

    // 📆 n개월 전 ~ 오늘
    function getMonthRange(monthsAgo) {
        const today = new Date();
        const past = new Date(today);
        past.setMonth(today.getMonth() - monthsAgo);
        return { startYm: formatYM(past), endYm: formatYM(today) };
    }

    // ✅ fetch POST 요청
    async function fetchConsults(startYm, endYm) {
        try {
            const res = await fetch('/consult/search', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ startYm, endYm })
            });

            if (!res.ok) throw new Error(`HTTP 오류: ${res.status}`);

            const data = await res.json();
            renderConsultTable(data);
        } catch (err) {
            console.error("조회 실패:", err);
        }
    }

    // ✅ 테이블 렌더링
    function renderConsultTable(data) {
        consultTableBody.innerHTML = "";

        if (!data || data.length === 0) {
            consultTableBody.innerHTML = `<tr><td colspan="10" style="text-align:center;">조회된 데이터가 없습니다.</td></tr>`;
            return;
        }

        data.forEach((c, i) => {
            const progressKey = c.progressKey || 'counseling';
            const progressText = (() => {
                switch (progressKey) {
                    case 'waiting': return '수업대기';
                    case 'confirmed': return '수업확정';
                    case 'ended': return '종료';
                    default: return '상담진행';
                }
            })();

            const row = document.createElement("tr");
            row.setAttribute("data-student-id", c.id || "");

            row.innerHTML = `
                <td class="checkbox-group"><input type="checkbox"></td>
                <td>${i + 1}</td>
                <td>${c.consultDate || ""}</td>
                <td>${c.studentName || ""}</td>
                <td>${c.school || ""}</td>
                <td>${c.gradeName || ""}</td>
                <td>${c.phone || ""}</td>
                <td>
                    <div class="memo-etc text-middle">
                        <textarea class="comment-text">${c.content || ""}</textarea>
                    </div>
                </td>
                <td>
                    <div class="select-arrow">
                        <button class="select-status" data-status="${progressKey}">${progressText}</button>
                        <ul class="dropdown-status">
                            <li data-status="counseling">상담진행</li>
                            <li data-status="waiting">수업대기</li>
                            <li data-status="confirmed">수업확정</li>
                            <li data-status="ended">종료</li>
                        </ul>
                    </div>
                </td>
                <td>
                    <div class="cell-middle">
                        <div class="icon-field basic-input" style="width: 150px;">
                            <span>${c.entryDate || ""}</span>
                            <button class="icon-btn" style="background: transparent;">
                                <img src="/image/calendar.png" alt="달력 아이콘">
                            </button>
                        </div>
                    </div>
                </td>
            `;

            consultTableBody.appendChild(row);
        });

        attachDropdownEvents();
    }

    // ✅ 드롭다운 동작
    function attachDropdownEvents() {
        // 드롭다운 열기/닫기
        document.querySelectorAll('.select-status').forEach(button => {
            button.addEventListener('click', function (e) {
                e.stopPropagation();
                const dropdown = this.nextElementSibling;
                dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
            });
        });

        // 항목 선택 시 진행상황 업데이트
        document.querySelectorAll('.dropdown-status li').forEach(item => {
            item.addEventListener('click', async function (e) {
                e.stopPropagation();
                const selectWrap = this.closest('.select-arrow');
                const button = selectWrap.querySelector('.select-status');
                const status = this.dataset.status;
                const text = this.textContent;

                // 선택한 행의 student id 가져오기
                const tr = this.closest('tr');
                const id = tr?.dataset.studentId;
                if (!id) {
                    console.warn("⚠️ data-student-id가 없습니다.");
                    return;
                }

                // UI 즉시 변경
                button.textContent = text;
                button.setAttribute('data-status', status);
                this.parentElement.style.display = 'none';

                console.log("🔹 진행상황 업데이트 요청:", { id, progressKey: status });

                try {
                    const res = await fetch('/consult/update-progress', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ id, progressKey: status })
                    });

                    if (!res.ok) {
                        const msg = await res.text();
                        console.error("❌ 서버 오류:", msg);
                        alert('진행상황 변경 실패');
                        return;
                    }

                    const msg = await res.text();
                    console.log("✅ 성공:", msg);
                } catch (err) {
                    console.error("🚨 통신 오류:", err);
                    alert('서버 통신 오류가 발생했습니다.');
                }
            });
        });

        // 드롭다운 외부 클릭 시 닫기
        document.addEventListener('click', () => {
            document.querySelectorAll('.dropdown-status').forEach(dd => dd.style.display = 'none');
        });
    }

    // ✅ 라디오 버튼 변경 시 기간 조회
    radios.forEach(radio => {
        radio.addEventListener("change", e => {
            const val = e.target.value;
            let range;

            if (val === "1y") range = getMonthRange(12);
            else if (val === "6m") range = getMonthRange(6);
            else if (val === "3m") range = getMonthRange(3);
            else if (val === "custom") {
                periodDisplay.textContent = "직접 날짜를 선택하세요";
                return;
            }

            periodDisplay.textContent = `${range.startYm} ~ ${range.endYm}`;
            fetchConsults(range.startYm, range.endYm);
        });
    });

    // ✅ 일자지정
    calendarBtn.addEventListener("click", () => {
        const isCustom = document.getElementById("period-custom").checked;
        if (!isCustom) return;
        calendarInput.showPicker();
    });

    calendarInput.addEventListener("change", e => {
        const selectedMonth = e.target.value;
        if (!selectedMonth) return;
        const today = new Date();
        const endYm = formatYM(today);
        const startYm = selectedMonth;
        periodDisplay.textContent = `${startYm} ~ ${endYm}`;
        fetchConsults(startYm, endYm);
    });

    // ✅ 기본 1년 조회
    const defaultRange = getMonthRange(12);
    periodDisplay.textContent = `${defaultRange.startYm} ~ ${defaultRange.endYm}`;
    fetchConsults(defaultRange.startYm, defaultRange.endYm);
});

//상담 삭제
document.addEventListener('DOMContentLoaded', () => {
    const deleteBtn = document.querySelector('.select-del');

    if (!deleteBtn) return;

    deleteBtn.addEventListener('click', async () => {
        // 체크된 항목 찾기
        const checked = document.querySelectorAll('.consult-table tbody input[type="checkbox"]:checked');

        if (checked.length === 0) {
            alert('삭제할 상담기록을 선택하세요.');
            return;
        }

        // 선택된 ID 목록 수집
        const ids = Array.from(checked).map(chk => {
            const tr = chk.closest('tr');
            return tr?.dataset.studentId;
        }).filter(Boolean);

        if (ids.length === 0) {
            alert('선택된 데이터에 ID가 없습니다.');
            return;
        }

        if (!confirm(`${ids.length}건의 상담기록을 삭제하시겠습니까?`)) {
            return;
        }

        try {
            const res = await fetch('/consult/delete', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(ids)
            });

            if (res.ok) {
                alert('삭제 완료');
                location.reload();
            } else {
                const msg = await res.text();
                alert('삭제 실패: ' + msg);
            }
        } catch (err) {
            console.error('삭제 중 오류:', err);
            alert('서버 통신 오류가 발생했습니다.');
        }
    });
});

// ✅ 상담기록 전체 선택 / 해제 기능 (렌더링 후에도 동작)
document.addEventListener('DOMContentLoaded', () => {
    const table = document.querySelector('.consult-table');
    if (!table) return;

    const headerCheckbox = table.querySelector('thead input[type="checkbox"]');

    // ✅ 전체선택 버튼 클릭 시 tbody 전체 on/off
    function toggleAllCheckboxes(isChecked) {
        const bodyCheckboxes = table.querySelectorAll('tbody input[type="checkbox"]');
        bodyCheckboxes.forEach(cb => cb.checked = isChecked);
    }

    // ✅ 상단 체크박스 클릭 시
    headerCheckbox.addEventListener('change', () => {
        toggleAllCheckboxes(headerCheckbox.checked);
    });

    // ✅ 개별 체크박스 클릭 시 상단 상태 갱신
    function updateHeaderCheckbox() {
        const bodyCheckboxes = table.querySelectorAll('tbody input[type="checkbox"]');
        const allChecked = Array.from(bodyCheckboxes).every(cb => cb.checked);
        headerCheckbox.checked = allChecked;
    }

    // ✅ 테이블 내에서 change 이벤트 감지
    table.addEventListener('change', e => {
        if (e.target.matches('tbody input[type="checkbox"]')) {
            updateHeaderCheckbox();
        }
    });

    // ✅ 데이터 fetch 후 렌더링 시에도 다시 동작하도록 export
    window.attachCheckboxSync = () => {
        const bodyCheckboxes = document.querySelectorAll('.consult-table tbody input[type="checkbox"]');
        bodyCheckboxes.forEach(cb => {
            cb.addEventListener('change', updateHeaderCheckbox);
        });
    };
});

