document.addEventListener('DOMContentLoaded', () => {

    /* =================== *
     *   유틸리티 함수      *
     * =================== */
    function formatYM(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        return `${year}-${month}`;
    }


    function getMonthRange(monthsAgo) {
        const today = new Date();
        const past = new Date(today);
        past.setMonth(today.getMonth() - monthsAgo);
        return {
            startYm: formatYM(past),
            endYm: formatYM(today)
        };
    }

    function formatPhoneNumber(raw) {
        let digits = raw.replace(/\D/g, '');
        if (!digits.startsWith('010')) digits = '010' + digits;
        if (digits.length > 11) digits = digits.slice(0, 11);

        if (digits.length <= 7) {
            return digits.replace(/(\d{3})(\d{0,4})/, '$1-$2');
        }
        return digits.replace(/(\d{3})(\d{4})(\d{0,4})/, '$1-$2-$3');
    }

    function getProgressText(progressKey) {
        switch (progressKey) {
            case 'waiting':
                return '수업대기';
            case 'confirmed':
                return '수업확정';
            case 'ended':
                return '종료';
            default:
                return '상담진행';
        }
    }

    /* =================== *
     *   모달 관리          *
     * =================== */
    const modal = document.querySelector('.consult-modal');
    const consultAddBtn = document.querySelector('.consult-add');
    const closeBtn = modal?.querySelector('.btn-close');
    const saveBtn = modal?.querySelector('.save-btn');
    const modalContent = modal?.querySelector('.consult-record');
    modal?.addEventListener('click', (e) => {
        e.stopPropagation();
        e.preventDefault();
        return false;
    });

    modalContent?.addEventListener('click', (e) => {
        e.stopPropagation();
    });

    let isEditMode = false;
    let editingId = null;

    // 모달 열기
    function openModal(data = null) {
        if (data) {
            isEditMode = true;
            editingId = data.id;
            fillModalData(data);
        } else {
            isEditMode = false;
            editingId = null;
            clearModalData();
        }
        modal.style.display = 'block';
    }

    // 모달 닫기
    function closeModal() {
        modal.style.display = 'none';
        clearModalData();
        isEditMode = false;
        editingId = null;
    }

    // 모달 데이터 채우기 (수정용)
    function fillModalData(data) {
        modal.querySelector('[name="studentName"]').value = data.studentName || '';
        modal.querySelector('[name="consultDate"]').value = data.consultDate || '';
        modal.querySelector('[name="school"]').value = data.school || '';
        modal.querySelector('[name="gradeKey"]').value = data.gradeKey || '';
        modal.querySelector('[name="parentPhone"]').value = data.phone || '';
        modal.querySelector('[name="inflowRouteKey"]').value = data.inflowRouteKey || '';
        modal.querySelector('[name="content"]').value = data.content || '';

        // 날짜 디스플레이 업데이트
        if (data.consultDate) {
            const [year, month, day] = data.consultDate.split('-');
            dateDisplay.textContent = `${year}년 ${month}월 ${day}일`;
        }
    }

    // 모달 데이터 초기화
    function clearModalData() {
        modal.querySelector('[name="studentName"]').value = '';
        modal.querySelector('[name="consultDate"]').value = '';
        modal.querySelector('[name="school"]').value = '';
        modal.querySelector('[name="gradeKey"]').value = '';
        modal.querySelector('[name="parentPhone"]').value = '';
        modal.querySelector('[name="inflowRouteKey"]').value = '';
        modal.querySelector('[name="content"]').value = '';
        dateDisplay.textContent = '';
    }

    // 모달 열기/닫기 이벤트
    consultAddBtn?.addEventListener('click', () => {
        openModal();
    });

    closeBtn?.addEventListener('click', (e) => {
        e.preventDefault();
        closeModal();
    });

    // 날짜 선택
    const dateInput = modal?.querySelector('input[name="consultDate"]');
    const dateDisplay = modal?.querySelector('.day-display');
    const calendarBtn = modal?.querySelector('.birth-btn');

    calendarBtn?.addEventListener('click', () => {
        dateInput.showPicker?.();
        dateInput.click();
    });

    dateInput?.addEventListener('change', () => {
        const selected = dateInput.value;
        if (selected) {
            const [year, month, day] = selected.split('-');
            dateDisplay.textContent = `${year}년 ${month}월 ${day}일`;
        } else {
            dateDisplay.textContent = '';
        }
    });

    // 전화번호 입력
    const phoneInput = modal?.querySelector('input[name="parentPhone"]');

    phoneInput?.addEventListener('focus', () => {
        if (!phoneInput.value.trim()) {
            phoneInput.value = '010-';
        }
    });

    phoneInput?.addEventListener('input', () => {
        phoneInput.value = formatPhoneNumber(phoneInput.value);
    });

    phoneInput?.addEventListener('keydown', (e) => {
        if ((e.key === 'Backspace' || e.key === 'Delete') &&
            phoneInput.value.replace(/\D/g, '').length <= 3) {
            e.preventDefault();
        }
    });

    // 상담 기록 저장/수정
    saveBtn?.addEventListener('click', async () => {
        try {
            const studentName = modal.querySelector('[name="studentName"]')?.value.trim();
            const consultDate = modal.querySelector('[name="consultDate"]')?.value;
            const school = modal.querySelector('[name="school"]')?.value.trim();
            const gradeKey = modal.querySelector('[name="gradeKey"]')?.value;
            const phone = modal.querySelector('[name="parentPhone"]')?.value.replace(/-/g, '');
            const inflowRouteKey = modal.querySelector('[name="inflowRouteKey"]')?.value;
            const content = modal.querySelector('[name="content"]')?.value.trim();

            if (!studentName) {
                alert('학생명을 입력해주세요.');
                modal.querySelector('[name="studentName"]')?.focus();
                return;
            }

            if (!consultDate) {
                alert('상담일을 선택해주세요.');
                modal.querySelector('[name="consultDate"]')?.focus();
                return;
            }

            if (!school) {
                alert('학교명을 입력해주세요.');
                modal.querySelector('[name="school"]')?.focus();
                return;
            }

            if (!gradeKey) {
                alert('학년을 선택해주세요.');
                modal.querySelector('[name="gradeKey"]')?.focus();
                return;
            }

            if (!phone) {
                alert('전화번호를 입력해주세요.');
                modal.querySelector('[name="parentPhone"]')?.focus();
                return;
            }

            // 전화번호 형식 검증 (숫자만, 10-11자리)
            const phoneRegex = /^[0-9]{10,11}$/;
            if (!phoneRegex.test(phone)) {
                alert('올바른 전화번호 형식이 아닙니다. (10-11자리 숫자)');
                modal.querySelector('[name="parentPhone"]')?.focus();
                return;
            }

            if (!content) {
                alert('상담 내용을 입력해주세요.');
                modal.querySelector('[name="content"]')?.focus();
                return;
            }

            const data = {
                studentName,
                consultDate,
                school,
                gradeKey,
                phone,
                inflowRouteKey,
                content
            };
            // 수정 모드일 경우 id 추가
            if (isEditMode && editingId) {
                data.id = editingId;
            }

            const url = isEditMode ? '/consult/update' : '/consult/save';
            const res = await fetch(url, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(data)
            });

            if (!res.ok) {
                alert(isEditMode ? '수정 실패' : '저장 실패');
                return;
            }

            alert(isEditMode ? '수정 완료' : '저장 완료');
            window.location.reload();

        } catch (err) {
            console.error(err);
            alert('에러 발생');
        }
    });

    /* =================== *
     *   테이블 관리         *
     * =================== */
    const consultTableBody = document.querySelector('.consult-table tbody');

    // 테이블 렌더링
    function renderConsultTable(data) {
        consultTableBody.innerHTML = "";

        if (!data || data.length === 0) {
            consultTableBody.innerHTML = `
                <tr>
                    <td colspan="10" style="text-align:center;">
                        조회된 데이터가 없습니다.
                    </td>
                </tr>`;
            return;
        }

        data.forEach((item, index) => {
            const progressKey = item.progressKey || 'counseling';
            const progressText = getProgressText(progressKey);

            consultTableBody.insertAdjacentHTML('beforeend', `
                <tr data-id="${item.id || ""}" 
                    data-student-name="${item.studentName || ""}"
                    data-consult-date="${item.consultDate || ""}"
                    data-school="${item.school || ""}"
                    data-grade-key="${item.gradeKey || ""}"
                    data-phone="${item.phone || ""}"
                    data-inflow-route-key="${item.inflowRouteKey || ""}"
                    data-content="${item.content || ""}"
                    data-progress-key="${progressKey}"
                    class="consult-row">
                    <td class="checkbox-group" onclick="event.stopPropagation()">
                        <input type="checkbox">
                    </td>
                    <td>${index + 1}</td>
                    <td>${item.consultDate || ""}</td>
                    <td>${item.studentName || ""}</td>
                    <td>${item.school || ""}</td>
                    <td>${item.gradeName || ""}</td>
                    <td>${item.phone || ""}</td>
                    <td onclick="event.stopPropagation()">
                        <div class="memo-etc text-middle consult-memo">
                            <textarea class="comment-text">${item.content || ""}</textarea>
                            <div class="common-btn consult-fix" style="display:none;">수정</div>
                        </div>
                        
                    </td>
                    <td onclick="event.stopPropagation()">
                        <div class="select-arrow">
                            <button class="select-status" data-status="${progressKey}">
                                ${progressText}
                            </button>
                            <ul class="dropdown-status">
                                <li data-status="counseling">상담진행</li>
                                <li data-status="waiting">수업대기</li>
                                <li data-status="confirmed">수업확정</li>
                                <li data-status="ended">종료</li>
                            </ul>
                        </div>
                    </td>
                    <td onclick="event.stopPropagation()">
                        <div class="join-link common-btn">가입링크 발송</div>
                    </td>
                </tr>
            `);
        });

        attachRowClickEvents();
        attachDropdownEvents();
        attachTextareaEvents(); // 새로 추가
    }

// textarea 변경 감지 함수 추가
    function attachTextareaEvents() {
        const textareas = consultTableBody.querySelectorAll('.comment-text');

        textareas.forEach(textarea => {
            textarea.addEventListener('input', function () {
                const originalValue = this.getAttribute('data-original');
                const currentValue = this.value;
                const updateBtn = this.nextElementSibling;

                if (originalValue !== currentValue) {
                    updateBtn.style.display = 'inline-block';
                } else {
                    updateBtn.style.display = 'none';
                }
            });

            // 수정 버튼 클릭 이벤트
            const updateBtn = textarea.nextElementSibling;
            updateBtn.addEventListener('click', function () {
                const row = this.closest('.consult-row');
                const consultId = row.getAttribute('data-id');
                const consultDate = row.getAttribute('data-consult-date');
                const newContent = textarea.value;

                if (!confirm('변경된 상담내용을 저장하시겠습니까?')) {
                    return;
                }
                updateConsultContent(consultId, consultDate, newContent, textarea, this);
            });
        });
    }

// 상담 내용 업데이트 함수
    async function updateConsultContent(consultId, consultDate, content, textarea, button) {
        try {
            const response = await fetch('/consult/content-update', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    consultId: consultId,
                    consultDate: consultDate,
                    content: content
                })
            });

            if (response.ok) {
                const result = await response.json();

                if (result.success) {
                    textarea.setAttribute('data-original', content);
                    button.style.display = 'none';
                    alert('수정되었습니다.');
                } else {
                    alert('수정에 실패했습니다.');
                }
            } else {
                alert('수정에 실패했습니다.');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('오류가 발생했습니다.');
        }
    }

    // 행 클릭 이벤트
    function attachRowClickEvents() {
        document.querySelectorAll('.consult-row').forEach(row => {
            row.addEventListener('click', function () {
                const data = {
                    id: this.dataset.studentId,
                    studentName: this.dataset.studentName,
                    consultDate: this.dataset.consultDate,
                    school: this.dataset.school,
                    gradeKey: this.dataset.gradeKey,
                    phone: this.dataset.phone,
                    inflowRouteKey: this.dataset.inflowRouteKey,
                    content: this.dataset.content,
                    progressKey: this.dataset.progressKey
                };
                openModal(data);
            });
        });
    }

    // 드롭다운 이벤트
    function attachDropdownEvents() {
        // 드롭다운 열기/닫기
        document.querySelectorAll('.select-status').forEach(button => {
            button.addEventListener('click', function (e) {
                e.stopPropagation();
                const dropdown = this.nextElementSibling;

                // 모든 드롭다운 닫기
                document.querySelectorAll('.dropdown-status').forEach(dd => {
                    dd.style.display = 'none';
                });

                // 현재 드롭다운 토글
                dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
            });
        });

        // 항목 선택
        document.querySelectorAll('.dropdown-status li').forEach(item => {
            item.addEventListener('click', async function (e) {
                e.stopPropagation();

                const selectWrap = this.closest('.select-arrow');
                const button = selectWrap.querySelector('.select-status');
                const status = this.dataset.status;
                const text = this.textContent;

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

                try {
                    const res = await fetch('/consult/update-progress', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify({id, progressKey: status})
                    });

                    if (!res.ok) {
                        const msg = await res.text();
                        console.error("❌ 서버 오류:", msg);
                        alert('진행상황 변경 실패');
                        return;
                    }

                } catch (err) {
                    console.error("🚨 통신 오류:", err);
                    alert('서버 통신 오류가 발생했습니다.');
                }
            });
        });

        // 외부 클릭 시 닫기
        document.addEventListener('click', () => {
            document.querySelectorAll('.dropdown-status').forEach(dd => {
                dd.style.display = 'none';
            });
        });
    }

    // 데이터 조회
    async function fetchConsults(startYm, endYm, userCode) {
        try {

            const requestBody = {
                startYm: startYm,
                endYm: endYm,
                userCode: userCode
            };

            const res = await fetch('/consult/search', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(requestBody)
            });

            if (!res.ok) {
                console.error("서버 조회 실패:", res.status);
                return;
            }

            const data = await res.json();
            renderConsultTable(data.response);
        } catch (err) {
            console.error("조회 실패:", err);
        }
    }

    /* =================== *
     *   기간 필터          *
     * =================== */
    const radios = document.querySelectorAll('input[name="period"]');
    const periodDisplay = document.getElementById('period-display');
    const calendarFilterBtn = document.getElementById('calendar-btn');
    const calendarInput = document.getElementById('calendar-input');
    const teacherFilter = document.getElementById('consult-teacher-filter');

    // 초기 로드 시 기본 기간으로 조회
    async function initPeriodFilter() {
        const defaultRadio = document.querySelector('input[name="period"][value="3m"]');
        const userCode = teacherFilter.value;

        if (defaultRadio) {
            defaultRadio.checked = true;
            const range = getMonthRange(3);
            periodDisplay.textContent = `${range.startYm} ~ ${range.endYm}`;
            await fetchConsults(range.startYm, range.endYm, userCode);
        }
    }

    // 라디오 버튼 변경
    radios.forEach(radio => {
        radio.addEventListener("change", async (e) => {
            const val = e.target.value;
            const userCode = teacherFilter.value;
            let range;

            if (val === "1y") {
                range = getMonthRange(12);
            } else if (val === "6m") {
                range = getMonthRange(6);
            } else if (val === "3m") {
                range = getMonthRange(3);
            } else if (val === "custom") {
                periodDisplay.textContent = "직접 날짜를 선택하세요";
                return;
            }

            periodDisplay.textContent = `${range.startYm} ~ ${range.endYm}`;
            await fetchConsults(range.startYm, range.endYm, userCode);
        });
    });

    // 일자지정 버튼
    calendarFilterBtn?.addEventListener("click", async () => {
        const customRadio = document.getElementById("period-custom");
        if (!customRadio.checked) {
            customRadio.checked = true;
            customRadio.dispatchEvent(new Event("change"));
        }
        calendarInput.showPicker();
    });

    // 달력 입력
    calendarInput?.addEventListener("change", async (e) => {
        const selectedMonth = e.target.value;
        const userCode = teacherFilter.value;
        if (!selectedMonth) return;

        const today = new Date();
        const endYm = formatYM(today);
        const startYm = selectedMonth;

        periodDisplay.textContent = `${startYm} ~ ${endYm}`;
        await fetchConsults(startYm, endYm, userCode);
    });


    teacherFilter?.addEventListener('change', async () => {
        const userCode = teacherFilter.value;

        // 현재 선택된 기간 가져오기
        const checkedRadio = document.querySelector('input[name="period"]:checked');
        if (!checkedRadio) return;

        const val = checkedRadio.value;
        let range;

        if (val === "1y") {
            range = getMonthRange(12);
        } else if (val === "6m") {
            range = getMonthRange(6);
        } else if (val === "3m") {
            range = getMonthRange(3);
        } else if (val === "custom") {
            // 커스텀 기간인 경우 periodDisplay에서 파싱
            const displayText = periodDisplay.textContent;
            const matches = displayText.match(/(\d{4}-\d{2})\s*~\s*(\d{4}-\d{2})/);
            if (matches) {
                range = {
                    startYm: matches[1],
                    endYm: matches[2]
                };
            } else {
                return;
            }
        }

        await fetchConsults(range.startYm, range.endYm, userCode);
    });
    // 초기 데이터 로드
    initPeriodFilter();

    /* =================== *
     *   체크박스 관리       *
     * =================== */
    const table = document.querySelector('.consult-table');
    const headerCheckbox = table?.querySelector('thead input[type="checkbox"]');

    // 전체 선택/해제
    headerCheckbox?.addEventListener('change', () => {
        const bodyCheckboxes = table.querySelectorAll('tbody input[type="checkbox"]');
        bodyCheckboxes.forEach(cb => cb.checked = headerCheckbox.checked);
    });

    // 개별 체크박스 변경 시
    table?.addEventListener('change', (e) => {
        if (e.target.matches('tbody input[type="checkbox"]')) {
            const bodyCheckboxes = table.querySelectorAll('tbody input[type="checkbox"]');
            const allChecked = Array.from(bodyCheckboxes).every(cb => cb.checked);
            headerCheckbox.checked = allChecked;
        }
    });

    /* =================== *
     *   삭제 기능          *
     * =================== */
    const deleteBtn = document.querySelector('.select-del');

    deleteBtn?.addEventListener('click', async () => {
        const checked = document.querySelectorAll('.consult-table tbody input[type="checkbox"]:checked');

        if (checked.length === 0) {
            alert('삭제할 상담기록을 선택하세요.');
            return;
        }

        const ids = Array.from(checked)
            .map(chk => chk.closest('tr')?.dataset.id)
            .filter(Boolean);

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
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(ids)
            });

            if (!res.ok) {
                const msg = await res.text();
                alert('삭제 실패: ' + msg);
                return;
            }

            alert('삭제 완료');
            location.reload();
        } catch (err) {
            console.error('삭제 중 오류:', err);
            alert('서버 통신 오류가 발생했습니다.');
        }
    });

});