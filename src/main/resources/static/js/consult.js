document.addEventListener('DOMContentLoaded', () => {

    /* =================== *
     *   참조              *
     * =================== */
    const currentUserCode = document.getElementById('current-user-code')?.value || '';
    const startDateInput = document.getElementById('start-date');
    const endDateInput = document.getElementById('end-date');
    const consultListBox = document.getElementById('consult-list-box');
    const progressFilter = document.getElementById('progress-filter');
    const keywordInput = document.getElementById('keyword-input');
    const consultModal = document.getElementById('consult-modal');

    /* =================== *
     *   헬퍼 함수         *
     * =================== */
    function getDateRange(months) {
        const today = new Date();
        const past = new Date(today);
        past.setMonth(today.getMonth() - months);
        const fmt = d =>
            `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
        return {startDate: fmt(past), endDate: fmt(today)};
    }

    function getTodayString() {
        const today = new Date();
        return `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
    }

    function getProgressText(progressKey) {
        const map = {confirmed: '입회', waiting: '대기', counseling: '문의', ended: '종료'};
        return map[progressKey] || '문의';
    }

    function getStatusClass(progressKey) {
        const map = {confirmed: 'is-join', waiting: 'is-wait', counseling: 'is-consult', ended: 'is-end'};
        return map[progressKey] || 'is-consult';
    }

    function getTypeTag(item) {
        let tags = '';
        if (item.inquiryHoho) tags += '<span class="tag tag-yellow">호호스쿨</span>';
        if (item.inquiryHan) tags += '<span class="tag tag-pink">한스쿨</span>';
        if (item.inquiryBook) tags += '<span class="tag tag-mint">북스쿨</span>';
        if (item.inquiryDoc) tags += '<span class="tag tag-blue">독서클리닉</span>';
        return tags;
    }

    function formatPhone(phone) {
        if (!phone) return '';
        const p = phone.replace(/-/g, '');
        if (p.length === 11) return p.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
        if (p.length === 10) return p.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3');
        return phone;
    }

    function getTypeTagModal(type) {
        return getTypeTag(type);
    }

    function groupByConsultKey(data) {
        const seen = new Map();
        const order = [];
        data.forEach(item => {
            const key = item.consultKey || '';
            if (!seen.has(key)) {
                seen.set(key, []);
                order.push(key);
            }
            seen.get(key).push(item);
        });
        return order.map(k => seen.get(k));
    }

    function updateSummary(groups) {
        const counts = {confirmed: 0, waiting: 0, counseling: 0, ended: 0};
        groups.forEach(records => {
            const progressKey = records[0].progressKey || 'counseling';
            if (counts[progressKey] !== undefined) counts[progressKey]++;
        });
        document.getElementById('cnt-all').innerHTML = `${groups.length}<em>건</em>`;
        document.getElementById('cnt-confirmed').innerHTML = `${counts.confirmed}<em>건</em>`;
        document.getElementById('cnt-waiting').innerHTML = `${counts.waiting}<em>건</em>`;
        document.getElementById('cnt-counseling').innerHTML = `${counts.counseling}<em>건</em>`;
        document.getElementById('cnt-ended').innerHTML = `${counts.ended}<em>건</em>`;
    }

    document.getElementById('myConsult')?.addEventListener('change', async () => {
        await fetchConsults(startDateInput.value, endDateInput.value);
    });

    /* =================== *
     *   데이터 조회        *
     * =================== */
    async function fetchConsults(startDate, endDate) {
        const progress = progressFilter?.value || 'all';
        const keyword = keywordInput?.value.trim() || '';
        const myConsultOnly = document.getElementById('myConsult')?.checked ?? false; // 추가
        try {
            const res = await fetch('/consult/search', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    startDate,
                    endDate,
                    userCode: myConsultOnly ? currentUserCode : '',  // 수정
                    progress,
                    keyword,
                    sortColumn: sortColumn ?? 'consultDate',
                    sortDir: sortDir ?? 'desc'
                })
            });
            const data = await res.json();
            renderConsultTable(data.response ?? []);
        } catch (err) {
            console.log('조회 실패: ', err);
        }
    }

    /* =================== *
     *   렌더링             *
     * =================== */
    function renderConsultTable(data) {
        consultListBox.querySelectorAll('.consult-item, .empty-msg').forEach(el => el.remove());

        if (!data || data.length === 0) {
            updateSummary([]);
            consultListBox.insertAdjacentHTML('beforeend',
                `<div class="empty-msg">조회된 데이터가 없습니다.</div>`
            );
            return;
        }

        const groups = groupByConsultKey(data);
        updateSummary(groups);

        groups.forEach((records, idx) => {
            const main = records[0];
            const history = records.slice(1);
            const progressKey = main.progressKey || 'counseling';
            const escapedMemo = (main.content || '').replace(/"/g, '&quot;');

            const lastColHtml = main.registeredAt
                ? `<div>${main.registeredAt}</div>`
                : main.sendAt
                    ? `<div>${main.sendAt}<br><small>발송일</small></div>`
                    : `<div class="link-area">
                           <button type="button" class="join-link">가입 링크 발송</button>
                       </div>`;

            const historyHtml = (history.length > 0 || main.endReason)
                ? `<div class="history">
           ${history.length > 0 ? `
               <h3>이전 상담 이력</h3>
               <ul>
                   ${history.map(h => `
                       <li class="history-item"
                           data-id="${h.id || ''}"
                           data-student-name="${h.studentName || ''}"
                           data-consult-date="${h.consultDate || ''}"
                           data-school="${h.school || ''}"
                           data-grade-key="${h.gradeKey || ''}"
                           data-phone="${h.phone || ''}"
                           data-inflow-route-key="${h.inflowRouteKey || ''}"
                           data-content="${(h.content || '').replace(/"/g, '&quot;')}"
                           data-progress-key="${h.progressKey || ''}"
                           data-inquiry-hoho="${h.inquiryHoho || false}"
                           data-inquiry-han="${h.inquiryHan || false}"
                           data-inquiry-book="${h.inquiryBook || false}"
                           data-inquiry-doc="${h.inquiryDoc || false}">
                           <span class="history-dot orange"></span>
                           <time>${h.consultDate || ''}</time>
                           <span>${h.userName || ''}</span>
                           <p>${h.content || ''}</p>
                       </li>`).join('')}
               </ul>` : ''}
           ${main.endReason ? `
                <h3>상담 종료 사유</h3>
                <ul>
                   <li class="history-item end-item">
                       <span class="history-dot red"></span>
                       <time>${main.endedAt || ''}</time>
                       <span>${main.endUserName || ''}</span>
                       <p>${main.endReason}</p>
                   </li>
               </ul>` : ''}
       </div>`
                : '';

            consultListBox.insertAdjacentHTML('beforeend', `
                <article class="consult-item"
                    data-id="${main.id || ''}"
                    data-student-name="${main.studentName || ''}"
                    data-consult-date="${main.consultDate || ''}"
                    data-school="${main.school || ''}"
                    data-grade-key="${main.gradeKey || ''}"
                    data-phone="${main.phone || ''}"
                    data-inflow-route-key="${main.inflowRouteKey || ''}"
                    data-content="${escapedMemo}"
                    data-progress-key="${progressKey}"
                    data-type="${main.type || ''}"
                    data-end-reason="${main.endReason || ''}"
                    data-inquiry-hoho="${main.inquiryHoho || false}"
                    data-inquiry-han="${main.inquiryHan || false}"
                    data-inquiry-book="${main.inquiryBook || false}"
                    data-inquiry-doc="${main.inquiryDoc || false}">

                    <div class="consult-row">
                        <div>${idx + 1}</div>
                        <div>${main.consultDate || ''}</div>
                        <div class="student">
                            <strong>${main.studentName || ''}</strong>
                            <span>${formatPhone(main.phone) || ''}</span>
                        </div>
                        <div>${main.gradeName || ''}</div>
                        <div>${getTypeTag(main)}</div>
                        <div>${main.userName || ''}</div>
                        <div class="memo-wrap">
                            <textarea class="memo">${main.content || ''}</textarea>
                            <div class="memo-actions">
                                <button type="button" class="memo-save-btn" style="display:none;">수정</button>
                            </div>
                        </div>
                        <div>
                            <div class="status-dropdown">
                                <button type="button" class="status-current ${getStatusClass(progressKey)}">
                                    <span>${getProgressText(progressKey)}</span>
                                    <i class="fa-solid fa-chevron-down"></i>
                                </button>
                                <div class="status-menu">
                                    <button type="button" class="status-option" data-status="confirmed"  data-label="입회">입회</button>
                                    <button type="button" class="status-option" data-status="waiting"    data-label="대기">대기</button>
                                    <button type="button" class="status-option" data-status="counseling" data-label="문의">문의</button>
                                    <button type="button" class="status-option" data-status="ended"      data-label="종료">종료</button>
                                </div>
                            </div>
                        </div>
                        ${lastColHtml}
                        ${(history.length > 0 || main.endReason)
                ? `<button type="button" class="accordion-btn"><i class="fa-solid fa-chevron-down"></i></button>`
                : `<div></div>`}
                    </div>
                    ${historyHtml}
                </article>
            `);
        });

        attachAccordionEvents();
        attachDropdownEvents();
        attachJoinLinkEvents();
        attachEditEvents();
        attachMemoEditEvents();
    }

    /* =================== *
     *   이벤트 바인딩      *
     * =================== */
    function attachAccordionEvents() {
        consultListBox.querySelectorAll('.accordion-btn').forEach(btn => {
            btn.addEventListener('click', function (e) {
                e.stopPropagation();
                this.closest('.consult-item').classList.toggle('open');
            });
        });
    }

    function attachDropdownEvents() {
        const statusClassNames = ['is-join', 'is-wait', 'is-consult', 'is-end'];

        consultListBox.querySelectorAll('.status-current').forEach(button => {
            button.addEventListener('click', function (e) {
                e.stopPropagation();
                const dropdown = this.closest('.status-dropdown');
                document.querySelectorAll('.status-dropdown').forEach(dd => {
                    if (dd !== dropdown) dd.classList.remove('open');
                });
                dropdown.classList.toggle('open');
            });
        });

        consultListBox.querySelectorAll('.status-option').forEach(item => {
            item.addEventListener('click', async function (e) {
                e.stopPropagation();
                const dropdown = this.closest('.status-dropdown');
                const button = dropdown.querySelector('.status-current');
                const status = this.dataset.status;
                const label = this.dataset.label;
                const article = this.closest('article.consult-item');
                const id = article?.dataset.id;

                if (!id) return;

                let endReason = null;
                if (status === 'ended') {
                    endReason = prompt('종료 사유를 입력해주세요.');
                    if (endReason === null) return;
                    if (!endReason.trim()) {
                        alert('종료 사유를 입력해주세요.');
                        return;
                    }
                    endReason = endReason.trim();
                }

                button.querySelector('span').textContent = label;
                button.classList.remove(...statusClassNames);
                button.classList.add(getStatusClass(status));
                dropdown.classList.remove('open');

                try {
                    const res = await fetch('/consult/update-progress', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify({id, progressKey: status, endReason})
                    });
                    if (res.ok) alert('상담 진행상태가 변경되었습니다.');
                    else alert('진행상황 변경 실패');
                } catch (err) {
                    console.error(err);
                    alert('서버 통신 오류가 발생했습니다.');
                }
            });
        });

        document.addEventListener('click', () => {
            document.querySelectorAll('.status-dropdown').forEach(dd => dd.classList.remove('open'));
        });
    }

    function attachJoinLinkEvents() {
        consultListBox.querySelectorAll('.join-link').forEach(btn => {
            btn.addEventListener('click', async function (e) {
                e.stopPropagation();
                const article = this.closest('article.consult-item');
                const rawPhone = article.dataset.phone || '';
                const studentName = article.dataset.studentName || '';
                const gradeKey = article.dataset.gradeKey || '';
                const consultId = article.dataset.id || '';
                const type = article.dataset.type || '';

                let phone = rawPhone.replace(/-/g, '').trim();
                if (!/^[0-9]+$/.test(phone)) {
                    alert('전화번호는 숫자만 입력해 주세요.');
                    return;
                }
                if (phone.length === 8) phone = '010' + phone;
                else if (!(phone.startsWith('010') && phone.length === 11)) {
                    alert('올바른 전화번호가 아닙니다.');
                    return;
                }

                if (!confirm(`${studentName} 학생에게 가입링크를 발송하시겠습니까?`)) return;

                try {
                    const res = await fetch('/popbill/send-join', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify({
                            source: 'CONSULT', phone, name: studentName,
                            gradeKey, consultId,
                            subHoho: type === 'hoho',
                            subHan: type === 'han',
                            subBook: type === 'book'
                        })
                    });
                    const result = await res.json();
                    alert(result.success ? '알림톡이 발송되었습니다.' : '발송 실패: ' + (result.message || ''));
                } catch (e) {
                    console.error(e);
                    alert('알림톡 발송 중 오류가 발생했습니다.');
                }
            });
        });
    }

    function attachEditEvents() {
        // 메인 row 클릭
        consultListBox.querySelectorAll('.consult-item').forEach(article => {
            article.addEventListener('click', function (e) {
                if (e.target.closest('.status-dropdown, .accordion-btn, .join-link, .memo-wrap, .history')) return;
                openModalForEdit(this);
            });
        });

        // 아코디언 history 아이템 클릭
        consultListBox.querySelectorAll('.history-item').forEach(li => {
            li.addEventListener('click', function (e) {
                e.stopPropagation();
                openModalForEdit(this);
            });
        });
    }

    function attachMemoEditEvents() {
        consultListBox.querySelectorAll('.memo').forEach(textarea => {
            textarea.dataset.original = textarea.value;
            const saveBtn = textarea.closest('.memo-wrap').querySelector('.memo-save-btn');

            textarea.addEventListener('input', function () {
                saveBtn.style.display = this.value !== this.dataset.original ? 'block' : 'none';
            });

            saveBtn.addEventListener('click', async function (e) {
                e.stopPropagation();
                const article = textarea.closest('.consult-item');
                const id = article.dataset.id;
                const content = textarea.value.trim();

                try {
                    const res = await fetch('/consult/update-memo', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify({id, content})
                    });
                    const data = await res.json();
                    if (data.success) {
                        textarea.dataset.original = textarea.value;
                        article.dataset.content = textarea.value;
                        saveBtn.style.display = 'none';
                    } else {
                        alert('메모 저장 실패');
                    }
                } catch (err) {
                    console.error(err);
                    alert('서버 통신 오류가 발생했습니다.');
                }
            });
        });
    }

    function openModalForEdit(article) {
        document.getElementById('modal-student-name').value = article.dataset.studentName || '';
        document.getElementById('modal-consult-date').value = article.dataset.consultDate || '';
        document.getElementById('modal-school').value = article.dataset.school || '';
        document.getElementById('modal-grade-key').value = article.dataset.gradeKey || '';
        document.getElementById('modal-phone').value = article.dataset.phone || '';
        document.getElementById('modal-inflow-route').value = article.dataset.inflowRouteKey || '';
        document.getElementById('modal-content').value = article.dataset.content || '';
        document.getElementById('modal-progress-key').value = article.dataset.progressKey || '';
        document.getElementById('modal-consult-id').value = article.dataset.id || '';

        // 과목 체크박스
        const cb = (id, val) => {
            const el = document.getElementById(id);
            if (!el) return;
            el.checked = val === 'true';
            el.closest('.subject-chip')?.classList.toggle('active', el.checked);
        };
        cb('modal-type-hoho', article.dataset.inquiryHoho);
        cb('modal-type-han', article.dataset.inquiryHan);
        cb('modal-type-book', article.dataset.inquiryBook);
        cb('modal-type-clinic', article.dataset.inquiryDoc);

        // 수정 모드
        const saveBtn = document.getElementById('modal-save-btn');
        saveBtn.textContent = '수정';
        saveBtn.dataset.mode = 'update';

        document.querySelector('.modal-title').textContent = '상담기록 수정';
        document.getElementById('modal-search-input').value = '';
        document.getElementById('modal-search-result').innerHTML = '';

        consultModal.classList.add('show');
    }

    /* =================== *
     *   모달              *
     * =================== */
    document.getElementById('consult-add')?.addEventListener('click', () => {
        document.getElementById('modal-consult-date').value = getTodayString();
        consultModal.classList.add('show');
    });

    document.getElementById('modal-close-btn')?.addEventListener('click', () => {
        consultModal.classList.remove('show');
        resetModal();
    });

    /* =================== *
     *   기간 라디오        *
     * =================== */
    document.querySelectorAll('input[name="period"]').forEach(radio => {
        radio.addEventListener('change', async (e) => {
            if (e.target.value === 'custom') return;
            const months = e.target.value === '6month' ? 6 : e.target.value === '3month' ? 3 : 1;
            const range = getDateRange(months);
            startDateInput.value = range.startDate;
            endDateInput.value = range.endDate;
            await fetchConsults(range.startDate, range.endDate);
        });
    });

    progressFilter?.addEventListener('change', async () => {
        await fetchConsults(startDateInput.value, endDateInput.value);
    });

    /* =================== *
     *   조회 버튼          *
     * =================== */
    document.getElementById('search-btn')?.addEventListener('click', async () => {
        const startDate = startDateInput.value;
        const endDate = endDateInput.value;
        if (!startDate || !endDate) {
            alert('조회 기간을 선택해주세요.');
            return;
        }
        if (startDate > endDate) {
            alert('시작일이 종료일보다 늦을 수 없습니다.');
            return;
        }
        await fetchConsults(startDate, endDate);
    });

    keywordInput?.addEventListener('keydown', async (e) => {
        if (e.key !== 'Enter') return;
        const startDate = startDateInput.value;
        const endDate = endDateInput.value;
        if (!startDate || !endDate) {
            alert('조회 기간을 선택해주세요.');
            return;
        }
        await fetchConsults(startDate, endDate);
    });

    /* =================== *
     *   필터 초기화        *
     * =================== */
    document.getElementById('reset-btn')?.addEventListener('click', async () => {
        const range = getDateRange(1);
        startDateInput.value = range.startDate;
        endDateInput.value = range.endDate;
        if (progressFilter) progressFilter.value = 'all';
        if (keywordInput) keywordInput.value = '';
        document.querySelector('input[name="period"][value="1month"]').checked = true;
        document.getElementById('myConsult').checked = false;
        await fetchConsults(range.startDate, range.endDate);
    });

    /* =================== *
     *   정렬 상태          *
     * =================== */
    let sortColumn = 'consultDate';
    let sortDir = 'desc';

    function updateSortIcons(col) {
        document.querySelectorAll('.sort-col').forEach(el => {
            const icon = el.querySelector('.sort-icon');
            if (!icon) return;
            if (el.dataset.col === col) {
                icon.src = sortDir === 'asc' ? '/image/sort_checked_up.svg' : '/image/sort_checked_down.svg';
            } else {
                icon.src = '/image/sort.svg';
            }
        });
    }

    document.querySelectorAll('.sort-col').forEach(el => {
        el.addEventListener('click', async () => {
            const col = el.dataset.col;
            if (sortColumn === col) {
                if (sortDir === 'asc') {
                    sortDir = 'desc';
                } else if (sortDir === 'desc') {
                    sortColumn = null;
                    sortDir = null;
                }
            } else {
                sortColumn = col;
                sortDir = 'asc';
            }
            updateSortIcons(sortColumn);
            await fetchConsults(startDateInput.value, endDateInput.value);
        });
    });

    /* =================== *
     *   초기 로드          *
     * =================== */
    (async () => {
        const range = getDateRange(1);
        startDateInput.value = range.startDate;
        endDateInput.value = range.endDate;
        await fetchConsults(range.startDate, range.endDate);
    })();

    /* =================== *
     *   모달 검색          *
     * =================== */
    async function searchModalConsults() {
        const keyword = document.getElementById('modal-search-input')?.value.trim() || '';
        if (!keyword) {
            alert('이름 또는 연락처를 입력해주세요.');
            return;
        }
        try {
            const res = await fetch('/consult/modal/search', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({keyword})
            });
            const data = await res.json();
            renderModalSearchResult(data.response ?? []);
        } catch (err) {
            console.error('모달 조회 실패:', err);
        }
    }

    function renderModalSearchResult(data) {
        const resultBox = document.getElementById('modal-search-result');
        resultBox.innerHTML = '';

        if (!data || data.length === 0) {
            resultBox.innerHTML = '<div class="empty-msg">조회된 이력이 없습니다.</div>';
            return;
        }

        data.forEach(item => {
            const card = document.createElement('article');
            card.className = 'history-result-card normal';
            card.dataset.studentName = item.studentName || '';
            card.dataset.phone = item.phone || '';
            card.dataset.school = item.school || '';
            card.dataset.gradeKey = item.gradeKey || '';
            card.dataset.inflowRouteKey = item.inflowRouteKey || '';
            card.dataset.type = item.type || '';
            card.dataset.content = (item.content || '').replace(/"/g, '&quot;');

            card.innerHTML = `
                <div class="result-top">
                    <strong class="result-name">${item.studentName || ''}</strong>
                   <span class="result-phone">${formatPhone(item.phone)}</span>
                </div>
                <div class="result-info">
                    <div><span>학년</span><strong>${item.gradeName || ''}</strong></div>
                    <div><span>최근 상담일</span><strong>${item.lastConsultDate || ''}</strong></div>
                    <div>
                        <span>문의 과목</span>
                        <div class="result-subject-tags">${getTypeTagModal(item)}</div>
                    </div>
                    <div><span>상담 횟수</span><strong>${item.consultCount || 0}회</strong></div>
                </div>
                <div class="result-memo">${item.content || ''}</div>
            `;

            card.addEventListener('click', () => {
                resultBox.querySelectorAll('.history-result-card').forEach(c => c.classList.add('normal'));
                card.classList.remove('normal');

                // 기본 정보만 채우기
                document.getElementById('modal-student-name').value = item.studentName || '';
                document.getElementById('modal-consult-date').value = getTodayString();
                document.getElementById('modal-school').value = item.school || '';
                document.getElementById('modal-grade-key').value = item.gradeKey || '';
                document.getElementById('modal-phone').value = item.phone || '';
                document.getElementById('modal-inflow-route').value = item.inflowRouteKey || '';

                // 문의 과목 초기화
                document.querySelectorAll('.subject-checks input[type="checkbox"]').forEach(cb => {
                    cb.checked = false;
                    cb.closest('.subject-chip')?.classList.remove('active');
                });

                // 메모 초기화
                document.getElementById('modal-content').value = '';

                // consultId 비워서 신규 저장으로
                document.getElementById('modal-progress-key').value = '';
                document.getElementById('modal-consult-id').value = '';

                // 버튼 저장 모드 유지
                const saveBtn = document.getElementById('modal-save-btn');
                saveBtn.textContent = '저장';
                saveBtn.dataset.mode = 'save';
            });

            resultBox.appendChild(card);
        });
    }

    /* =================== *
     *   모달 초기화        *
     * =================== */
    function resetModal() {
        document.getElementById('modal-search-input').value = '';
        document.getElementById('modal-search-result').innerHTML = '';
        document.getElementById('modal-student-name').value = '';
        document.getElementById('modal-consult-date').value = getTodayString();
        document.getElementById('modal-school').value = '';
        document.getElementById('modal-grade-key').value = '';
        document.getElementById('modal-phone').value = '';
        document.getElementById('modal-inflow-route').value = '';
        document.getElementById('modal-content').value = '';
        document.getElementById('modal-progress-key').value = '';
        document.getElementById('modal-consult-id').value = '';

        document.querySelectorAll('.subject-checks input[type="checkbox"]').forEach(cb => {
            cb.checked = false;
            cb.closest('.subject-chip')?.classList.remove('active');
        });

        const saveBtn = document.getElementById('modal-save-btn');
        saveBtn.textContent = '저장';
        saveBtn.dataset.mode = 'save';
    }

    document.getElementById('modal-reset-btn')?.addEventListener('click', resetModal);
    /* =================== *
     *   과목 체크박스       *
     * =================== */
    document.querySelectorAll('.subject-checks input[type="checkbox"]').forEach(cb => {
        cb.addEventListener('change', function () {
            this.closest('.subject-chip')?.classList.toggle('active', this.checked);
        });
    });

    /* =================== *
     *   모달 저장/수정      *
     * =================== */
    document.getElementById('modal-save-btn')?.addEventListener('click', async () => {
        const studentName = document.getElementById('modal-student-name').value.trim();
        const consultDate = document.getElementById('modal-consult-date').value;
        const school = document.getElementById('modal-school').value.trim();
        const gradeKey = document.getElementById('modal-grade-key').value;
        const phone = document.getElementById('modal-phone').value.trim();
        const inflowRouteKey = document.getElementById('modal-inflow-route').value;
        const content = document.getElementById('modal-content').value.trim();
        const progressKey = document.getElementById('modal-progress-key').value || 'counseling';
        const consultId = document.getElementById('modal-consult-id').value || null;
        const mode = document.getElementById('modal-save-btn').dataset.mode || 'save';
        const url = mode === 'update' ? '/consult/update' : '/consult/save';

        const inquiryHoho = document.getElementById('modal-type-hoho')?.checked ?? false;
        const inquiryHan = document.getElementById('modal-type-han')?.checked ?? false;
        const inquiryBook = document.getElementById('modal-type-book')?.checked ?? false;
        const inquiryDoc = document.getElementById('modal-type-clinic')?.checked ?? false;

        if (!studentName) {
            alert('이름을 입력해주세요.');
            return;
        }

        if (!phone) {
            alert('연락처를 입력해주세요.');
            return;
        }

        if (!inquiryHoho && !inquiryHan && !inquiryBook && !inquiryDoc) {
            alert('문의 과목을 선택해주세요.');
            return;
        }

        try {
            const res = await fetch(url, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    consultId, studentName, consultDate, school, gradeKey,
                    phone, inflowRouteKey, content, progressKey,
                    inquiryHoho, inquiryHan, inquiryBook, inquiryDoc
                })
            });
            const data = await res.json();
            if (data.success) {
                alert(mode === 'update' ? '수정되었습니다.' : '저장되었습니다.');
                document.getElementById('modal-close-btn').click();
                await fetchConsults(startDateInput.value, endDateInput.value);
            } else {
                alert('저장 실패');
            }
        } catch (err) {
            console.error(err);
            alert('서버 통신 오류가 발생했습니다.');
        }
    });

    document.getElementById('modal-search-btn')?.addEventListener('click', searchModalConsults);
    document.getElementById('modal-search-input')?.addEventListener('keydown', async (e) => {
        if (e.key === 'Enter') await searchModalConsults();
    });

    /* =================== *
     *   인쇄 / 엑셀        *
     * =================== */
    function buildPrintParams() {
        return new URLSearchParams({
            startDate: startDateInput.value,
            endDate: endDateInput.value,
            progress: progressFilter?.value || 'all',
            keyword: keywordInput?.value.trim() || '',
            sortColumn,
            sortDir
        });
    }

    document.getElementById('consult-print')?.addEventListener('click', () => {
        const iframe = document.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = `/consult/print-consult?${buildPrintParams()}`;
        document.body.appendChild(iframe);
        iframe.onload = () => {
            iframe.contentWindow.print();
            setTimeout(() => document.body.removeChild(iframe), 2000);
        };
    });

    document.getElementById('consult-excel')?.addEventListener('click', () => {
        window.location.href = `/consult/excel-consult?${buildPrintParams()}`;
    });
});