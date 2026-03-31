document.addEventListener('DOMContentLoaded', () => {

    /* =================== *
     *   유틸리티 함수      *
     * =================== */
    function formatYM(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        return `${year}-${month}`;
    }

    /* =================== *
     *   검색 기능          *
     * =================== */
    const searchTypeSelect = document.getElementById('stu-name');
    const searchInput      = document.getElementById('search-name');
    const searchBtn        = document.querySelector('.explore');

    searchBtn?.addEventListener('click', () => {
        applySearch();
    });

    searchInput?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') applySearch();
    });

    // ★ student.js처럼 input 이벤트에도 실시간 검색
    searchInput?.addEventListener('input', () => {
        applySearch();
    });

    searchTypeSelect?.addEventListener('change', () => {
        applySearch();
    });

    // ★ student.js 방식: 데이터 재조회 없이 DOM 행을 hide/show
    function applySearch() {
        const type    = searchTypeSelect?.value || 'name';
        const keyword = searchInput?.value.trim().toLowerCase();

        // 1) 진행상황 정렬 기준으로 먼저 렌더링 (항상 최신 정렬 유지)
        applyProgressSort();

        // 2) 키워드 없으면 전체 표시 후 종료
        if (!keyword) {
            consultTableBody.querySelector('.empty-search-row')?.remove();
            return;
        }

        // 3) 렌더링된 DOM 행에서 직접 hide/show
        const rows = Array.from(consultTableBody.querySelectorAll('tr.consult-row'));

        // 데이터 자체가 없으면 renderConsultTable이 이미 "데이터 없음" 표시 → 중복 방지
        if (rows.length === 0) return;

        // 검색 조건에 맞는 전화번호 수집 (동일 번호 행 전부 표시)
        const matchedPhones = new Set();
        rows.forEach(tr => {
            let match = false;
            switch (type) {
                case 'name':
                    match = (tr.dataset.studentName || '').toLowerCase().includes(keyword);
                    break;
                case 'school':
                    match = (tr.dataset.school || '').toLowerCase().includes(keyword);
                    break;
                case 'phone': {
                    const phone = (tr.dataset.phone || '').replace(/-/g, '');
                    match = phone.includes(keyword.replace(/-/g, ''));
                    break;
                }
            }
            if (match) matchedPhones.add(tr.dataset.phone);
        });

        let visibleCount = 0;
        rows.forEach(tr => {
            if (matchedPhones.has(tr.dataset.phone)) {
                tr.style.display = '';
                visibleCount++;
            } else {
                tr.style.display = 'none';
            }
        });

        // 결과 없음 처리
        consultTableBody.querySelector('.empty-search-row')?.remove();
        if (visibleCount === 0) {
            const emptyTr = document.createElement('tr');
            emptyTr.className = 'empty-search-row';
            emptyTr.innerHTML = `<td colspan="10" style="text-align:center; padding:20px;">검색결과가 없습니다.</td>`;
            consultTableBody.appendChild(emptyTr);
        }
    }

    function getDateRange(monthsAgo) {
        const today = new Date();
        const past  = new Date(today);
        past.setMonth(today.getMonth() - monthsAgo);

        function formatDate(d) {
            return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
        }

        return {
            startDate: formatDate(past),
            endDate:   formatDate(today)
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
            case 'waiting':   return '대기';
            case 'confirmed': return '입회';
            case 'ended':     return '종료';
            default:          return '문의';
        }
    }

    const printBtn = document.getElementById("consult-print");
    const excelBtn = document.getElementById("consult-excel");

    printBtn.addEventListener("click", () => {
        const displayText = periodDisplay.textContent;
        const matches = displayText.match(/(\d{4}-\d{2}-\d{2})\s*~\s*(\d{4}-\d{2}-\d{2})/);
        const userCode = teacherFilter.value;

        let url = `/consult/print-consult?userCode=${userCode}`;
        if (matches) {
            url += `&startDate=${matches[1]}&endDate=${matches[2]}`;
        }
        printConsult(url);
    });

    function printConsult(url) {
        const iframe = document.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = url;
        iframe.onload = () => { iframe.contentWindow.print(); };
        document.body.appendChild(iframe);
    }

    excelBtn.addEventListener("click", () => {
        const displayText = periodDisplay.textContent;
        const matches = displayText.match(/(\d{4}-\d{2})\s*~\s*(\d{4}-\d{2})/);
        const userCode = teacherFilter.value;

        let url = `/consult/excel-consult?userCode=${userCode}`;
        if (matches) {
            url += `&startYm=${matches[1]}&endYm=${matches[2]}`;
        }
        window.location.href = url;
    });

    /* =================== *
     *   모달 관리          *
     * =================== */
    const modal = document.querySelector('.consult-modal');
    const consultAddBtn = document.querySelector('.consult-add');
    const closeBtn = modal?.querySelector('.btn-close');
    const saveBtn = modal?.querySelector('.save-btn');
    const modalContent = modal?.querySelector('.consult-record');

    modal?.addEventListener('click', (e) => { e.stopPropagation(); e.preventDefault(); return false; });
    modalContent?.addEventListener('click', (e) => { e.stopPropagation(); });

    let isEditMode = false;
    let editingId = null;

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

    function closeModal() {
        modal.style.display = 'none';
        clearModalData();
        isEditMode = false;
        editingId = null;
    }

    function fillModalData(data) {
        modal.querySelector('[name="studentName"]').value   = data.studentName || '';
        modal.querySelector('[name="consultDate"]').value   = data.consultDate || '';
        modal.querySelector('[name="school"]').value        = data.school || '';
        modal.querySelector('[name="gradeKey"]').value      = data.gradeKey || '';
        modal.querySelector('[name="parentPhone"]').value   = data.phone || '';
        modal.querySelector('[name="inflowRouteKey"]').value = data.inflowRouteKey || '';
        modal.querySelector('[name="content"]').value       = data.content || '';

        if (data.consultDate) {
            const [year, month, day] = data.consultDate.split('-');
            dateDisplay.textContent = `${year}년 ${month}월 ${day}일`;
        }
    }

    function clearModalData() {
        modal.querySelector('[name="studentName"]').value   = '';
        modal.querySelector('[name="consultDate"]').value   = '';
        modal.querySelector('[name="school"]').value        = '';
        modal.querySelector('[name="gradeKey"]').value      = '';
        modal.querySelector('[name="parentPhone"]').value   = '';
        modal.querySelector('[name="inflowRouteKey"]').value = '';
        modal.querySelector('[name="content"]').value       = '';
        dateDisplay.textContent = '';
    }

    consultAddBtn?.addEventListener('click', () => { openModal(); });
    closeBtn?.addEventListener('click', (e) => { e.preventDefault(); closeModal(); });

    const dateInput   = modal?.querySelector('input[name="consultDate"]');
    const dateDisplay = modal?.querySelector('.day-display');
    const calendarBtn = modal?.querySelector('.birth-btn');

    calendarBtn?.addEventListener('click', () => { dateInput.showPicker?.(); dateInput.click(); });

    dateInput?.addEventListener('change', () => {
        const selected = dateInput.value;
        if (selected) {
            const [year, month, day] = selected.split('-');
            dateDisplay.textContent = `${year}년 ${month}월 ${day}일`;
        } else {
            dateDisplay.textContent = '';
        }
    });

    const phoneInput = modal?.querySelector('input[name="parentPhone"]');

    phoneInput?.addEventListener('focus', () => {
        if (!phoneInput.value.trim()) phoneInput.value = '010-';
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

    saveBtn?.addEventListener('click', async () => {
        try {
            const studentName    = modal.querySelector('[name="studentName"]')?.value.trim();
            const consultDate    = modal.querySelector('[name="consultDate"]')?.value;
            const school         = modal.querySelector('[name="school"]')?.value.trim();
            const consultType    = modal.querySelector('[name="consultType"]:checked')?.value || '';
            const gradeKey       = modal.querySelector('[name="gradeKey"]')?.value;
            const phone          = modal.querySelector('[name="parentPhone"]')?.value.replace(/-/g, '');
            const inflowRouteKey = modal.querySelector('[name="inflowRouteKey"]')?.value;
            const content        = modal.querySelector('[name="content"]')?.value.trim();

            if (!studentName)  { alert('학생명을 입력해주세요.'); modal.querySelector('[name="studentName"]')?.focus(); return; }
            if (!consultDate)  { alert('상담일을 선택해주세요.'); modal.querySelector('[name="consultDate"]')?.focus(); return; }
            if (!school)       { alert('학교명을 입력해주세요.'); modal.querySelector('[name="school"]')?.focus(); return; }
            if (!gradeKey)     { alert('학년을 선택해주세요.'); modal.querySelector('[name="gradeKey"]')?.focus(); return; }
            if (!phone)        { alert('전화번호를 입력해주세요.'); modal.querySelector('[name="parentPhone"]')?.focus(); return; }
            if (!/^[0-9]{10,11}$/.test(phone)) {
                alert('올바른 전화번호 형식이 아닙니다. (10-11자리 숫자)');
                modal.querySelector('[name="parentPhone"]')?.focus();
                return;
            }
            if (!content)      { alert('상담 내용을 입력해주세요.'); modal.querySelector('[name="content"]')?.focus(); return; }

            const data = { studentName, consultDate, school, gradeKey, phone, inflowRouteKey, content, consultType };
            if (isEditMode && editingId) data.id = editingId;

            const url = isEditMode ? '/consult/update' : '/consult/save';
            const res = await fetch(url, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(data)
            });

            if (!res.ok) { alert(isEditMode ? '수정 실패' : '저장 실패'); return; }

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
            const progressKey  = item.progressKey || 'counseling';
            const progressText = getProgressText(progressKey);
            // ★ data-original 속성을 렌더링 시점에 세팅 (textarea 수정 감지에 필요)
            const escapedContent = (item.content || '').replace(/"/g, '&quot;');

            consultTableBody.insertAdjacentHTML('beforeend', `
                <tr data-id="${item.id || ""}"
                    data-student-name="${item.studentName || ""}"
                    data-consult-date="${item.consultDate || ""}"
                    data-school="${item.school || ""}"
                    data-grade-key="${item.gradeKey || ""}"
                    data-phone="${item.phone || ""}"
                    data-inflow-route-key="${item.inflowRouteKey || ""}"
                    data-content="${escapedContent}"
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
                            <textarea class="comment-text"
                                data-original="${escapedContent}">${item.content || ""}</textarea>
                            <div class="common-btn consult-fix" style="display:none;">수정</div>
                        </div>
                    </td>
                    <td onclick="event.stopPropagation()">
                        <div class="select-arrow">
                            <button class="select-status" data-status="${progressKey}">
                                ${progressText}
                            </button>
                            <ul class="dropdown-status">
                                <li data-status="confirmed">입회</li>
                                <li data-status="waiting">대기</li>
                                <li data-status="counseling">문의</li>
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
        attachTextareaEvents();
        attachJoinLinkEvents();
    }

    function attachJoinLinkEvents() {
        consultTableBody.querySelectorAll('.join-link').forEach(btn => {
            btn.addEventListener('click', async function (e) {
                e.stopPropagation();

                const row         = this.closest('tr');
                const rawPhone    = row.dataset.phone || '';
                const studentName = row.dataset.studentName || '';
                const gradeKey    = row.dataset.gradeKey || '';
                const consultId   = row.dataset.id || '';

                let phone = rawPhone.replace(/-/g, '').trim();

                if (!/^[0-9]+$/.test(phone))               { alert('전화번호는 숫자만 입력해 주세요.'); return; }
                if (phone.startsWith('010') && phone.length === 11) { /* 정상 */ }
                else if (phone.length === 8)                { phone = '010' + phone; }
                else                                        { alert('올바른 전화번호가 아닙니다.'); return; }

                if (!confirm(`${studentName} 학생에게 가입링크를 발송하시겠습니까?`)) return;

                try {
                    const response = await fetch('/popbill/send-join', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify({
                            source: 'CONSULT', phone, name: studentName,
                            gradeKey, consultId, subHoho: false, subHan: false, subBook: false
                        })
                    });
                    const result = await response.json();
                    alert(result.success ? '알림톡이 발송되었습니다.' : '발송 실패: ' + (result.message || ''));
                } catch (e) {
                    console.error(e);
                    alert('알림톡 발송 중 오류가 발생했습니다.');
                }
            });
        });
    }

    function attachTextareaEvents() {
        consultTableBody.querySelectorAll('.comment-text').forEach(textarea => {
            textarea.addEventListener('input', function () {
                const originalValue = this.getAttribute('data-original') || '';
                const updateBtn = this.nextElementSibling;
                updateBtn.style.display = (originalValue !== this.value) ? 'inline-block' : 'none';
            });

            const updateBtn = textarea.nextElementSibling;
            updateBtn.addEventListener('click', function () {
                const row        = this.closest('.consult-row');
                const consultId  = row.getAttribute('data-id');
                const consultDate = row.getAttribute('data-consult-date');
                const newContent = textarea.value;

                if (!confirm('변경된 상담내용을 저장하시겠습니까?')) return;
                updateConsultContent(consultId, consultDate, newContent, textarea, this);
            });
        });
    }

    async function updateConsultContent(consultId, consultDate, content, textarea, button) {
        try {
            const response = await fetch('/consult/content-update', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({ consultId, consultDate, content })
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

    function attachRowClickEvents() {
        consultTableBody.querySelectorAll('.consult-row').forEach(row => {
            row.addEventListener('click', function () {
                openModal({
                    id:             this.dataset.id,
                    studentName:    this.dataset.studentName,
                    consultDate:    this.dataset.consultDate,
                    school:         this.dataset.school,
                    gradeKey:       this.dataset.gradeKey,
                    phone:          this.dataset.phone,
                    inflowRouteKey: this.dataset.inflowRouteKey,
                    content:        this.dataset.content,
                    progressKey:    this.dataset.progressKey
                });
            });
        });
    }

    function attachDropdownEvents() {
        consultTableBody.querySelectorAll('.select-status').forEach(button => {
            button.addEventListener('click', function (e) {
                e.stopPropagation();
                const dropdown = this.nextElementSibling;
                document.querySelectorAll('.dropdown-status').forEach(dd => { dd.style.display = 'none'; });
                dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
            });
        });

        consultTableBody.querySelectorAll('.dropdown-status li').forEach(item => {
            item.addEventListener('click', async function (e) {
                e.stopPropagation();

                const selectWrap = this.closest('.select-arrow');
                const button     = selectWrap.querySelector('.select-status');
                const status     = this.dataset.status;
                const text       = this.textContent;
                const tr         = this.closest('tr');
                const id         = tr?.dataset.id;

                if (!id) { console.warn("data-id 없음"); return; }

                button.textContent = text;
                button.setAttribute('data-status', status);
                this.parentElement.style.display = 'none';

                try {
                    const res = await fetch('/consult/update-progress', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify({id, progressKey: status})
                    });
                    if (!res.ok) alert('진행상황 변경 실패');
                } catch (err) {
                    console.error(err);
                    alert('서버 통신 오류가 발생했습니다.');
                }
            });
        });

        document.addEventListener('click', () => {
            document.querySelectorAll('.dropdown-status').forEach(dd => { dd.style.display = 'none'; });
        });
    }

    let allConsultData = [];

    async function fetchConsults(startDate, endDate, userCode) {
        try {
            const res = await fetch('/consult/search', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({ startDate, endDate, userCode })
            });
            if (!res.ok) { console.error("서버 조회 실패:", res.status); return; }

            const data = await res.json();
            allConsultData = data.response ?? [];
            applySearch();   // 검색 키워드 상태를 유지하면서 렌더링
        } catch (err) {
            console.error("조회 실패:", err);
        }
    }

    // applyProgressSort 는 순수하게 정렬 후 렌더링만 담당
    function applyProgressSort() {
        const sortVal = document.getElementById('progress-sort')?.value || 'all';

        let sorted;
        if (sortVal === 'all') {
            sorted = [...allConsultData];
        } else {
            const top    = allConsultData.filter(item => (item.progressKey || 'counseling') === sortVal);
            const others = allConsultData.filter(item => (item.progressKey || 'counseling') !== sortVal);
            sorted = [...top, ...others];
        }

        renderConsultTable(sorted);
    }

    // ★ 진행상황 정렬 변경 → 검색 상태 유지하면서 재정렬
    document.getElementById('progress-sort')?.addEventListener('change', () => {
        applySearch();
    });

    /* =================== *
     *   기간 필터          *
     * =================== */
    const radios            = document.querySelectorAll('input[name="period"]');
    const periodDisplay     = document.getElementById('period-display');
    const periodDisplayWrap = document.getElementById('period-display-wrap');
    const customRangeWrap   = document.getElementById('custom-range-wrap');
    const customStart       = document.getElementById('custom-start');
    const customEnd         = document.getElementById('custom-end');
    const customSearchBtn   = document.getElementById('custom-search-btn');
    const teacherFilter     = document.getElementById('consult-teacher-filter');

    radios.forEach(radio => {
        radio.addEventListener('change', async (e) => {
            const val = e.target.value;

            if (val === 'custom') {
                periodDisplayWrap.style.display = 'none';
                customRangeWrap.style.display   = 'flex';
                return;
            }

            periodDisplayWrap.style.display = '';
            customRangeWrap.style.display   = 'none';

            const months = val === '1y' ? 12 : val === '6m' ? 6 : 3;
            const range  = getDateRange(months);
            periodDisplay.textContent = `${range.startDate} ~ ${range.endDate}`;
            await fetchConsults(range.startDate, range.endDate, teacherFilter.value);
        });
    });

    customSearchBtn?.addEventListener('click', async () => {
        const startDate = customStart.value;
        const endDate   = customEnd.value;

        if (!startDate || !endDate) { alert('시작일과 종료일을 선택해주세요.'); return; }
        if (startDate > endDate)    { alert('시작일이 종료일보다 늦을 수 없습니다.'); return; }

        periodDisplay.textContent = `${startDate} ~ ${endDate}`;
        await fetchConsults(startDate, endDate, teacherFilter.value);
    });

    teacherFilter?.addEventListener('change', async () => {
        const val = document.querySelector('input[name="period"]:checked')?.value;
        if (!val) return;

        if (val === 'custom') {
            const startDate = customStart.value;
            const endDate   = customEnd.value;
            if (startDate && endDate) await fetchConsults(startDate, endDate, teacherFilter.value);
            return;
        }

        const months = val === '1y' ? 12 : val === '6m' ? 6 : 3;
        const range  = getDateRange(months);
        periodDisplay.textContent = `${range.startDate} ~ ${range.endDate}`;
        await fetchConsults(range.startDate, range.endDate, teacherFilter.value);
    });

    // 초기 로드 (3개월 기본)
    (async () => {
        const defaultRadio = document.querySelector('input[name="period"][value="3m"]');
        if (defaultRadio) {
            defaultRadio.checked = true;
            const range = getDateRange(3);
            periodDisplay.textContent = `${range.startDate} ~ ${range.endDate}`;
            await fetchConsults(range.startDate, range.endDate, teacherFilter.value);
        }
    })();

    /* =================== *
     *   체크박스 관리       *
     * =================== */
    const table          = document.querySelector('.consult-table');
    const headerCheckbox = table?.querySelector('thead input[type="checkbox"]');

    headerCheckbox?.addEventListener('change', () => {
        table.querySelectorAll('tbody input[type="checkbox"]')
            .forEach(cb => cb.checked = headerCheckbox.checked);
    });

    table?.addEventListener('change', (e) => {
        if (e.target.matches('tbody input[type="checkbox"]')) {
            const all = table.querySelectorAll('tbody input[type="checkbox"]');
            headerCheckbox.checked = Array.from(all).every(cb => cb.checked);
        }
    });

    /* =================== *
     *   삭제 기능          *
     * =================== */
    const deleteBtn = document.querySelector('.select-del');

    deleteBtn?.addEventListener('click', async () => {
        const checked = document.querySelectorAll('.consult-table tbody input[type="checkbox"]:checked');

        if (checked.length === 0) { alert('삭제할 상담기록을 선택하세요.'); return; }

        const ids = Array.from(checked)
            .map(chk => chk.closest('tr')?.dataset.id)
            .filter(Boolean);

        if (ids.length === 0) { alert('선택된 데이터에 ID가 없습니다.'); return; }
        if (!confirm(`${ids.length}건의 상담기록을 삭제하시겠습니까?`)) return;

        try {
            const res = await fetch('/consult/delete', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(ids)
            });

            if (!res.ok) { alert('삭제 실패: ' + await res.text()); return; }

            alert('삭제 완료');
            location.reload();
        } catch (err) {
            console.error('삭제 중 오류:', err);
            alert('서버 통신 오류가 발생했습니다.');
        }
    });

});