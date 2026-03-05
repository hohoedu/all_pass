// ============================================= //
//           신규 입회 (student_new.js)            //
// ============================================= //

document.addEventListener('DOMContentLoaded', () => {

    /* =================== *
     *   전역 변수           *
     * =================== */
    const tbody  = document.getElementById('student-tbody');
    const modal  = document.querySelector('.student-new-modal');
    let currentStudentId = null;


    /* =================== *
     *   유틸 함수           *
     * =================== */
    function getSubjectText(student) {
        const subjects = [];
        if (student.subHoho) subjects.push('호호스쿨');
        if (student.subHan)  subjects.push('한스쿨');
        if (student.subBook) subjects.push('북스쿨');
        return subjects.length > 0 ? subjects.join(', ') : '-';
    }

    function getStatusBadge(status) {
        switch (status) {
            case 'REGISTERED': return `<span class="color-box cb-tu">가입완료</span>`;
            case 'LINK_SENT':  return `<span class="color-box cb-gr">대기중</span>`;
            case 'ASSIGNED':   return `<span class="color-box cb-bl">배정완료</span>`;
            default:           return `<span class="color-box">${status}</span>`;
        }
    }

    function formatPhone(phone) {
        if (!phone || phone.length !== 11) return phone || '';
        return `${phone.substring(0, 3)}-${phone.substring(3, 7)}-${phone.substring(7, 11)}`;
    }

    function unformatPhone(phone) {
        return phone ? phone.replace(/-/g, '') : '';
    }

    function formatDate(dateStr) {
        if (!dateStr) return '-';
        return dateStr.substring(0, 10);
    }

    function formatBirthDisplay(birth) {
        if (!birth || typeof birth !== 'string') return '';
        let year, month, day, match;

        match = birth.match(/^(\d{4})\s*년\s*(\d{1,2})\s*월\s*(\d{1,2})\s*일$/);
        if (match) { [, year, month, day] = match; }

        if (!year) {
            match = birth.match(/^(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})$/);
            if (match) { [, year, month, day] = match; }
        }

        if (!year) {
            match = birth.match(/^(\d{4})(\d{2})(\d{2})$/);
            if (match) { [, year, month, day] = match; }
        }

        if (!year) return '';
        return `${year}년 ${month.padStart(2, '0')}월 ${day.padStart(2, '0')}일`;
    }

    function formatBirthInput(birth) {
        if (!birth || birth.length !== 6) return '';
        const yy = birth.substring(0, 2);
        const mm = birth.substring(2, 4);
        const dd = birth.substring(4, 6);
        const currentYY = new Date().getFullYear() % 100;
        const fullYear  = yy > currentYY ? `19${yy}` : `20${yy}`;
        return `${fullYear}-${mm}-${dd}`;
    }

    function formatMoney(value) {
        if (value === null || value === undefined || value === '') return '';
        const num = Number(value.toString().replace(/,/g, ''));
        return isNaN(num) ? '' : num.toLocaleString('ko-KR');
    }

    function formatDateDisplay(value) {
        if (!value || typeof value !== 'string') return '';
        if (/^\d{4}년\s?\d{1,2}월\s?\d{1,2}일$/.test(value)) return value;
        const match = value.match(/^(\d{4})-(\d{2})-(\d{2})$/);
        if (match) {
            const [, y, m, d] = match;
            return `${y}년 ${m}월 ${d}일`;
        }
        return '';
    }

    function getValue(selector) {
        const el = modal.querySelector(selector);
        return el ? el.value : '';
    }

    function setValue(selector, value) {
        modal.querySelectorAll(selector).forEach(el => {
            if (['INPUT', 'TEXTAREA', 'SELECT'].includes(el.tagName)) {
                el.value = value ?? '';
            } else {
                el.innerText = value ?? '';
            }
        });
    }


    // ============================================= //
    //                  테이블 렌더링                   //
    // ============================================= //

    function renderTable(data) {
        tbody.innerHTML = '';

        if (!data || data.length === 0) {
            tbody.innerHTML = `<tr><td colspan="10" style="text-align:center;">조회된 데이터가 없습니다.</td></tr>`;
            return;
        }

        data.forEach((student, index) => {
            const isRegistered = student.status === 'REGISTERED';
            tbody.insertAdjacentHTML('beforeend', `
                <tr data-student-id="${student.studentId || ''}"
                    data-pending-id="${student.id || ''}"
                    data-phone="${student.phone || ''}"
                    data-name="${student.name || ''}"
                    style="${isRegistered ? 'cursor:pointer;' : 'cursor:default;'}">
                    <td>${index + 1}</td>
                    <td>${student.name || '-'}</td>
                    <td>${student.gradeName || '-'}</td>
                    <td>${getSubjectText(student)}</td>
                    <td>${formatPhone(student.phone)}</td>
                    <td>${student.userName ? student.userName + ' 선생님' : '-'}</td>
                    <td>${getStatusBadge(student.status)}</td>
                    <td>${formatDate(student.registeredAt)}</td>
                    <td class="action-cell">
                        ${isRegistered ? '' : `<button class="class-action-btn resend-btn">재발송</button>`}
                    </td>
                    <td class="action-cell">
                        <i class="fa-regular fa-circle-xmark cancel-btn" style="cursor:pointer;"></i>
                    </td>
                </tr>
            `);
        });
    }


    // ============================================= //
    //              이벤트 위임 (tbody)                //
    // ============================================= //

    tbody.addEventListener('click', async function (e) {

        // 재발송
        if (e.target.closest('.resend-btn')) {
            const row   = e.target.closest('tr');
            const phone = row.dataset.phone;
            const name  = row.dataset.name;
            if (!confirm(`${name} 학생에게 가입링크를 재발송하시겠습니까?`)) return;
            try {
                const res    = await fetch('/popbill/send-join', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ phone, source: 'RESEND' })
                });
                const result = await res.json();
                alert(result.success ? '재발송되었습니다.' : '발송 실패: ' + (result.message || ''));
            } catch { alert('오류가 발생했습니다.'); }
            return;
        }

        // 취소
        if (e.target.closest('.cancel-btn')) {
            const row  = e.target.closest('tr');
            const id   = row.dataset.pendingId;
            const name = row.dataset.name;
            if (!confirm(`${name} 학생의 입회를 취소하시겠습니까?`)) return;
            try {
                const res    = await fetch('/student/pending/cancel', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ id })
                });
                const result = await res.json();
                if (result.success) {
                    alert('취소되었습니다.');
                    row.remove();
                } else {
                    alert('취소 실패: ' + (result.message || ''));
                }
            } catch { alert('오류가 발생했습니다.'); }
            return;
        }

        // action-cell 무시
        if (e.target.closest('.action-cell')) return;

        // row 클릭
        const row = e.target.closest('tr');
        if (!row) return;

        const studentId = row.dataset.studentId;
        if (!studentId) {
            alert('등록된 학생 정보가 없습니다.');
            return;
        }

        await loadStudentDetail(studentId);
    });


    // ============================================= //
    //              학생 상세 조회                     //
    // ============================================= //

    async function loadStudentDetail(studentId) {
        try {
            const res = await fetch(`/student/${studentId}`);
            if (!res.ok) throw new Error('조회 실패');

            currentStudentId = studentId;
            const data = await res.json();
            renderStudentModal(data.response);
            openModal();

        } catch (err) {
            console.error(err);
            alert('학생 정보를 가져오는 중 오류가 발생했습니다.');
        }
    }


    // ============================================= //
    //                  모달 렌더링                    //
    // ============================================= //

    function renderStudentModal(data) {
        if (!data) return;

        const info    = data.studentInfo    ?? {};
        const payment = data.studentPayment ?? {};
        const grade   = data.gradeCodes;

        // 상단 이름
        setValue('.s_name', info.studentName);

        // TAB2 기본정보
        setValue('#new-tab2 .s_name',          info.studentName);
        setValue('#new-tab2 .s_school',         info.school);
        setValue('#new-tab2 .s_address',        info.address);
        setValue('#new-tab2 .s_address_detail', info.addressDetail);
        setValue('#new-tab2 .s_phone',          formatPhone(info.parentPhone));
        setValue('#new-tab2 .s_birth',          formatBirthDisplay(info.birth));
        setValue('#new-tab2 .s_billing_phone',  formatPhone(info.billingPhone));

        renderGender(info.genderKey);
        renderParent(info.parentRelation);
        renderGradeDropdown(grade, info.gradeKey);
        renderStatusButton(info.statusKey);

        const birthInput = modal.querySelector('#birth-date');
        if (birthInput) birthInput.value = formatBirthInput(info.birth) || info.birth || '';

        // TAB3 수강정보
        setValue('#new-tab3 .s_han_class',         payment.hanClassName);
        setValue('#new-tab3 .s_book_class',        payment.bookClassName);
        setValue('#new-tab3 .p_han_teacher',       payment.hanTeacher);
        setValue('#new-tab3 .p_book_teacher',      payment.bookTeacher);
        setValue('#new-tab3 .p_han_fee',           formatMoney(payment.hanFee));
        setValue('#new-tab3 .p_book_fee',          formatMoney(payment.bookFee));
        setValue('#new-tab3 .p_han_material_fee',  formatMoney(payment.hanMaterialPrice));
        setValue('#new-tab3 .p_book_material_fee', formatMoney(payment.bookMaterialPrice));
        setValue('#new-tab3 .s_entry_han_date',    formatDateDisplay(payment.entryHanDate) || '날짜를 선택하세요.');
        setValue('#new-tab3 .s_entry_book_date',   formatDateDisplay(payment.entryBookDate) || '날짜를 선택하세요.');

        const hanInput  = modal.querySelector('#entry-han-date');
        const bookInput = modal.querySelector('#entry-book-date');
        if (hanInput)  hanInput.value  = payment.entryHanDate  || '';
        if (bookInput) bookInput.value = payment.entryBookDate || '';

        setCourseState('han',  payment.hanState);
        setCourseState('book', payment.bookState);
        renderFeeTable(payment);
        updateTotalFee();

        if (typeof window.saveInitialCourseState === 'function') {
            window.saveInitialCourseState(payment.hanState, payment.bookState);
        }
    }

    function renderStatusButton(statusKey) {
        const statusStr = String(statusKey ?? '');
        modal.querySelectorAll('.status-buttons').forEach(group => {
            const mode = group.getAttribute('data-visibility');
            group.querySelectorAll('.s_status').forEach(btn => {
                const btnStatus = btn.getAttribute('data-status');
                if (mode === 'current-status') {
                    btn.style.display = (btnStatus === statusStr) ? 'inline-block' : 'none';
                } else if (mode === 'except-current') {
                    btn.style.display = (btnStatus === statusStr) ? 'none' : 'inline-block';
                }
            });
        });
    }

    function renderGender(genderKey) {
        const wrapper = modal.querySelector('.gender-group');
        if (!wrapper) return;
        const buttons = wrapper.querySelectorAll('.s_gender');
        const hidden  = wrapper.querySelector('.gender-hidden');
        if (!hidden) return;
        buttons.forEach(btn => btn.classList.toggle('active', btn.dataset.value === genderKey));
        hidden.value = genderKey;
        buttons.forEach(btn => {
            const clone = btn.cloneNode(true);
            btn.replaceWith(clone);
        });
        wrapper.querySelectorAll('.s_gender').forEach(btn => {
            btn.addEventListener('click', () => {
                wrapper.querySelectorAll('.s_gender').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                hidden.value = btn.dataset.value;
            });
        });
    }

    function renderParent(initialValue) {
        const group = modal.querySelector('.relation-group');
        if (!group) return;
        const buttons = group.querySelectorAll('.btn-choose');
        const hidden  = group.querySelector('.relation-hidden');
        if (!hidden) return;
        buttons.forEach(btn => btn.classList.toggle('active', btn.dataset.value === initialValue));
        hidden.value = initialValue ?? '';
        buttons.forEach(btn => {
            const clone = btn.cloneNode(true);
            btn.replaceWith(clone);
        });
        group.querySelectorAll('.btn-choose').forEach(btn => {
            btn.addEventListener('click', () => {
                group.querySelectorAll('.btn-choose').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                hidden.value = btn.dataset.value;
            });
        });
    }

    function renderGradeDropdown(gradeCodes, selectedKey) {
        const select = modal.querySelector('.s_grade');
        if (!select || !gradeCodes) return;
        select.innerHTML = '<option value="">학년 선택</option>';
        gradeCodes.forEach(code => {
            const opt = document.createElement('option');
            opt.value = code.gradeKey;
            opt.textContent = code.gradeName;
            if (String(code.gradeKey) === String(selectedKey)) opt.selected = true;
            select.appendChild(opt);
        });
    }

    function setCourseState(type, state) {
        const group = modal.querySelector(`#new-tab3 .choose-group[data-type="${type}"]`);
        if (!group) return;
        const buttons = group.querySelectorAll('.btn-choose');
        const hidden  = group.querySelector('input[type="hidden"]');
        buttons.forEach(btn => {
            const isActive =
                ((state === '1' || state === 1) && btn.dataset.value === 'active') ||
                ((state === '0' || state === 0) && btn.dataset.value === 'inactive');
            btn.classList.toggle('active', isActive);
            if (isActive && hidden) hidden.value = btn.dataset.value;
        });
    }

    function renderFeeTable(payment) {
        const feeTbody = modal.querySelector('#fee-tbody');
        if (!feeTbody) return;
        feeTbody.innerHTML = '';
        const addRow = (label, amount) => {
            if (!amount || amount <= 0) return;
            const tr = document.createElement('tr');
            tr.innerHTML = `<td>${label}</td><td>${formatMoney(amount)}</td>`;
            feeTbody.appendChild(tr);
        };
        addRow('한자(교육비)', payment.hanFee);
        addRow('한자(교재비)', payment.hanMaterialPrice);
        addRow('독서(교육비)', payment.bookFee);
        addRow('독서(교재비)', payment.bookMaterialPrice);
        if (feeTbody.children.length === 0) {
            feeTbody.innerHTML = `<tr><td colspan="2" style="text-align:center;">회비 정보 없음</td></tr>`;
        }
    }

    function updateTotalFee() {
        const removeComma = v => (v ? v.toString().replace(/,/g, '') : '0');
        const hanFee  = parseInt(removeComma(modal.querySelector('#hanFee')?.value))  || 0;
        const bookFee = parseInt(removeComma(modal.querySelector('#bookFee')?.value)) || 0;
        const hanMat  = parseInt(removeComma(modal.querySelector('#hanMaterialFee')?.value))  || 0;
        const bookMat = parseInt(removeComma(modal.querySelector('#bookMaterialFee')?.value)) || 0;
        const total   = hanFee + bookFee + hanMat + bookMat;
        const sumEl   = modal.querySelector('.dues-sum span');
        if (sumEl) sumEl.innerText = ' ' + formatMoney(total);
    }


    // ============================================= //
    //                  모달 열기/닫기/탭               //
    // ============================================= //

    function openModal() {
        if (!modal) return;
        switchTab('new-tab2');
        modal.style.display = 'block';
        document.body.classList.add('modal-open');
    }

    function closeModal() {
        if (!modal) return;
        modal.style.display = 'none';
        document.body.classList.remove('modal-open');
        currentStudentId = null;
    }

    function switchTab(targetTabId) {
        modal.querySelectorAll('.new-tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.tab === targetTabId);
        });
        modal.querySelectorAll('.info-tab-content .tab').forEach(tabEl => {
            tabEl.style.display = (tabEl.id === targetTabId) ? 'block' : 'none';
        });
    }


    // ============================================= //
    //               모달 이벤트 초기화              //
    // ============================================= //

    function initModal() {
        if (!modal) return;

        // 닫기
        modal.querySelector('.new-btn-close')?.addEventListener('click', (e) => {
            e.preventDefault();
            closeModal();
        });

        // 탭 전환
        modal.querySelectorAll('.new-tab-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                switchTab(btn.dataset.tab);
            });
        });

        // 달력 아이콘 클릭
        modal.addEventListener('click', (e) => {
            const iconBtn = e.target.closest('.icon-btn');
            if (!iconBtn) return;
            const td = iconBtn.closest('td');
            if (!td) return;
            const dateInput = td.querySelector('input[type="date"]');
            if (!dateInput) return;
            if (typeof dateInput.showPicker === 'function') {
                dateInput.showPicker();
            } else {
                dateInput.click();
            }
        });

        // 날짜 변경 시 display 업데이트
        modal.addEventListener('change', (e) => {
            const input = e.target;
            if (input.type !== 'date') return;
            const td = input.closest('td');
            if (!td) return;
            const display = td.querySelector('.birth-display');
            if (!display || !input.value) return;
            const [y, m, d] = input.value.split('-');
            display.textContent = `${y}년 ${m}월 ${d}일`;
        });

        // ★ 수강/미수강 버튼 클릭
        modal.addEventListener('click', (e) => {
            const btn = e.target.closest('#new-tab3 .choose-group .btn-choose');
            if (!btn) return;

            const group  = btn.closest('.choose-group');
            const hidden = group.querySelector('input[type="hidden"]');
            if (!hidden) return;

            group.querySelectorAll('.btn-choose').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            hidden.value = btn.dataset.value;

            // 미수강 입력 영역 show/hide
            const type           = group.dataset.type; // 'han' or 'book'
            const status         = btn.dataset.value;  // 'active' or 'inactive'
            const inactiveRow    = modal.querySelector(`.${type}-inactive`);
            if (inactiveRow) {
                inactiveRow.classList.toggle('hide-input', status !== 'inactive');
            }
        });
    }


    // ============================================= //
    //              학생 상태 변경 (TAB2)              //
    // ============================================= //

    function initStatusChange() {
        const exceptCurrentContainer = modal.querySelector('.status-buttons[data-visibility="except-current"]');
        const reasonInputBox         = modal.querySelector('.reason-input');
        const reasonField            = modal.querySelector('#reason');
        const submitBtn              = modal.querySelector('#status-change');
        const withdrawDateDiv        = modal.querySelector('.withdraw-date');

        if (!exceptCurrentContainer) return;

        // 달력 클릭
        modal.querySelector('.calendar-open')?.addEventListener('click', () => {
            const dateInput = modal.querySelector('#withdraw-date');
            if (!dateInput) return;
            if (typeof dateInput.showPicker === 'function') {
                dateInput.showPicker();
            } else {
                dateInput.click();
            }
        });

        // 날짜 선택 시 표시
        modal.querySelector('#withdraw-date')?.addEventListener('change', function () {
            const display = modal.querySelector('.withdraw-date .day-display');
            if (display && this.value) {
                const [y, m, d] = this.value.split('-');
                display.textContent = `${y}년 ${m}월 ${d}일`;
            }
        });

        // 상태 버튼 클릭
        exceptCurrentContainer.addEventListener('click', (e) => {
            const btn = e.target.closest('.s_status');
            if (!btn || !exceptCurrentContainer.contains(btn)) return;

            exceptCurrentContainer.querySelectorAll('.s_status').forEach(b => b.classList.remove('selected'));
            btn.classList.add('selected');

            const status = btn.dataset.status;
            reasonInputBox?.classList.add('active');

            if (status === 'ACTIVE') {
                reasonField?.classList.add('hide-input');
                if (reasonField) reasonField.value = '';
                withdrawDateDiv?.classList.add('hide-input');
            } else if (status === 'WITHDRAWN') {
                reasonField?.classList.remove('hide-input');
                withdrawDateDiv?.classList.remove('hide-input');
            } else {
                reasonField?.classList.remove('hide-input');
                withdrawDateDiv?.classList.add('hide-input');
            }
        });

        // 변경 버튼
        submitBtn?.addEventListener('click', async () => {
            const selectedBtn = exceptCurrentContainer.querySelector('.s_status.selected');
            if (!selectedBtn) { alert('상태를 선택해주세요.'); return; }

            const statusKey = selectedBtn.dataset.status;
            let reason      = '';
            let withdrawDate = null;

            if (statusKey !== 'ACTIVE') {
                reason = reasonField?.value.trim() || '';
                if (!reason) { alert('사유를 입력해주세요.'); return; }
                if (statusKey === 'WITHDRAWN') {
                    withdrawDate = modal.querySelector('#withdraw-date')?.value;
                    if (!withdrawDate) { alert('탈퇴 날짜를 입력해주세요.'); return; }
                }
            }

            if (!currentStudentId) { alert('학생 ID를 찾을 수 없습니다.'); return; }

            try {
                const res = await fetch('/student/status', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ studentId: currentStudentId, statusKey, reason, withdrawDate })
                });
                if (!res.ok) throw new Error('요청 실패');
                const data = await res.json();
                alert('상태가 성공적으로 변경되었습니다.');

                const s = data.response;
                if (s?.statusKey) renderStatusButton(s.statusKey);

                // 입력 초기화
                if (reasonField) reasonField.value = '';
                const dateInput = modal.querySelector('#withdraw-date');
                const display   = modal.querySelector('.withdraw-date .day-display');
                if (dateInput) dateInput.value = '';
                if (display)   display.textContent = '날짜를 선택해주세요';
                exceptCurrentContainer.querySelectorAll('.s_status').forEach(b => b.classList.remove('selected'));
                reasonInputBox?.classList.remove('active');

            } catch { alert('오류가 발생했습니다.'); }
        });
    }


    // ============================================= //
    //              기본정보 저장 (TAB2)               //
    // ============================================= //

    function initUpdateBtn() {
        modal.querySelector('#update-btn')?.addEventListener('click', async () => {
            if (!currentStudentId) { alert('학생 ID를 찾을 수 없습니다.'); return; }

            const req = {
                studentId:     currentStudentId,
                studentName:   getValue('#new-tab2 .s_name'),
                birth:         getValue('#birth-date'),
                genderKey:     getValue('#new-tab2 .gender-hidden'),
                school:        getValue('#new-tab2 .s_school'),
                address:       getValue('#new-tab2 .s_address'),
                addressDetail: getValue('#new-tab2 .s_address_detail'),
                parentPhone:   unformatPhone(getValue('#new-tab2 .s_phone')),
                gradeKey:      getValue('#new-tab2 .s_grade'),
                relationKey:   getValue('#new-tab2 .relation-hidden'),
                billingPhone:  unformatPhone(getValue('#new-tab2 .s_billing_phone')),
                entryHanDate:  getValue('#entry-han-date')?.trim() || null,
                entryBookDate: getValue('#entry-book-date')?.trim() || null,
            };

            try {
                const res = await fetch('/student/update/info', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(req)
                });
                if (!res.ok) { alert('오류가 발생했습니다.'); return; }
                alert('저장되었습니다.');
            } catch { alert('저장 중 오류가 발생했습니다.'); }
        });
    }


    // ============================================= //
    //         수강상태 저장 (TAB3)                    //
    // ============================================= //

    function initCourseStatusSave() {
        let initialHanState    = null;
        let initialBookState   = null;
        let initialEntryHanDate  = null;
        let initialEntryBookDate = null;

        window.saveInitialCourseState = function (hanState, bookState) {
            initialHanState  = (hanState  === 1 || hanState  === '1') ? 'active' : 'inactive';
            initialBookState = (bookState === 1 || bookState === '1') ? 'active' : 'inactive';
            initialEntryHanDate  = modal.querySelector('#entry-han-date')?.value  || null;
            initialEntryBookDate = modal.querySelector('#entry-book-date')?.value || null;
        };

        modal.querySelector('#course-status-save')?.addEventListener('click', async () => {
            if (!currentStudentId) { alert('학생 ID를 찾을 수 없습니다.'); return; }

            const hanHidden  = modal.querySelector('.choose-group[data-type="han"] input[type="hidden"]');
            const bookHidden = modal.querySelector('.choose-group[data-type="book"] input[type="hidden"]');
            const currentHanState  = hanHidden?.value;
            const currentBookState = bookHidden?.value;

            const entryHanInput  = modal.querySelector('#entry-han-date');
            const entryBookInput = modal.querySelector('#entry-book-date');

            const hanChanged  = (initialHanState  !== null && initialHanState  !== currentHanState)  || (initialEntryHanDate  !== entryHanInput?.value);
            const bookChanged = (initialBookState !== null && initialBookState !== currentBookState) || (initialEntryBookDate !== entryBookInput?.value);

            const requestBody = {
                studentId:        currentStudentId,
                hanState:         currentHanState  === 'active' ? 1 : 0,
                bookState:        currentBookState === 'active' ? 1 : 0,
                hanChanged,
                bookChanged,
                entryHanDate:     entryHanInput?.value  || null,
                entryBookDate:    entryBookInput?.value || null,
                inactiveHanDate:  modal.querySelector('#han-inactive-date')?.value  || null,
                inactiveBookDate: modal.querySelector('#book-inactive-date')?.value || null,
                inactiveHanReason:  modal.querySelector('#han-inactive-reason')?.value?.trim()  || null,
                inactiveBookReason: modal.querySelector('#book-inactive-reason')?.value?.trim() || null,
            };

            try {
                const res = await fetch('/student/update/course-status', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(requestBody)
                });
                if (!res.ok) throw new Error('수강상태 변경 실패');
                alert('수강상태가 성공적으로 변경되었습니다.');

                initialHanState      = currentHanState;
                initialBookState     = currentBookState;
                initialEntryHanDate  = entryHanInput?.value;
                initialEntryBookDate = entryBookInput?.value;

            } catch { alert('수강상태 변경 중 오류가 발생했습니다.'); }
        });
    }


    // ============================================= //
    //              교재비 저장 (TAB3)                 //
    // ============================================= //

    function initPayInfo() {
        modal.querySelector('#pay-info')?.addEventListener('click', async () => {
            if (!currentStudentId) { alert('학생을 먼저 선택해주세요.'); return; }

            const payload = {
                studentId:       currentStudentId,
                entryHanDate:    getValue('#entry-han-date')?.trim()  || null,
                entryBookDate:   getValue('#entry-book-date')?.trim() || null,
                hanMaterialFee:  parseInt((getValue('#hanMaterialFee')  || '0').replace(/,/g, '')) || 0,
                bookMaterialFee: parseInt((getValue('#bookMaterialFee') || '0').replace(/,/g, '')) || 0,
            };

            try {
                const res = await fetch('/student/update/payment', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                if (!res.ok) throw new Error('결제 정보 저장 실패');
                alert('교재비가 저장되었습니다.');
                updateTotalFee();
            } catch { alert('저장 중 오류가 발생했습니다.'); }
        });
    }


    // ============================================= //
    //                  초기 실행                     //
    // ============================================= //

    initModal();
    initStatusChange();
    initUpdateBtn();
    initCourseStatusSave();
    initPayInfo();
    renderTable(pendingStudents);
});
