document.addEventListener('DOMContentLoaded', () => {

    /* =================== *
     *   참조              *
     * =================== */
    const currentUserCode = document.getElementById('current-user-code')?.value || '';
    const startDateInput  = document.getElementById('start-date');
    const endDateInput    = document.getElementById('end-date');
    const consultListBox  = document.getElementById('consult-list-box');
    const progressFilter  = document.getElementById('progress-filter');
    const keywordInput    = document.getElementById('keyword-input');
    const consultModal    = document.getElementById('consult-modal');

    /* =================== *
     *   헬퍼 함수         *
     * =================== */
    function getDateRange(months) {
        const today = new Date();
        const past  = new Date(today);
        past.setMonth(today.getMonth() - months);
        const fmt = d =>
            `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
        return { startDate: fmt(past), endDate: fmt(today) };
    }

    function getProgressText(progressKey) {
        const map = { confirmed: '입회', waiting: '대기', counseling: '문의', ended: '종료' };
        return map[progressKey] || '문의';
    }

    function getStatusClass(progressKey) {
        const map = { confirmed: 'is-join', waiting: 'is-wait', counseling: 'is-consult', ended: 'is-end' };
        return map[progressKey] || 'is-consult';
    }

    function getTypeTag(type) {
        const map = {
            hoho: '<span class="tag tag-yellow">호호스쿨</span>',
            han:  '<span class="tag tag-pink">한스쿨</span>',
            book: '<span class="tag tag-mint">북스쿨</span>'
        };
        return map[type] || '';
    }

    function groupByPhone(data) {
        const seen  = new Map();
        const order = [];
        data.forEach(item => {
            const phone = item.phone || '';
            if (!seen.has(phone)) {
                seen.set(phone, []);
                order.push(phone);
            }
            seen.get(phone).push(item);
        });
        return order.map(p => seen.get(p));
    }

    function updateSummary(groups) {
        const counts = { confirmed: 0, waiting: 0, counseling: 0, ended: 0 };

        groups.forEach(records => {
            const progressKey = records[0].progressKey || 'counseling';
            if (counts[progressKey] !== undefined) counts[progressKey]++;
        });

        document.getElementById('cnt-all').innerHTML        = `${groups.length}<em>건</em>`;
        document.getElementById('cnt-confirmed').innerHTML  = `${counts.confirmed}<em>건</em>`;
        document.getElementById('cnt-waiting').innerHTML    = `${counts.waiting}<em>건</em>`;
        document.getElementById('cnt-counseling').innerHTML = `${counts.counseling}<em>건</em>`;
        document.getElementById('cnt-ended').innerHTML      = `${counts.ended}<em>건</em>`;
    }

    /* =================== *
     *   데이터 조회        *
     * =================== */
    async function fetchConsults(startDate, endDate, userCode) {
        try {
            const res  = await fetch('/consult/search', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ startDate, endDate, userCode })
            });
            const data = await res.json();
            renderConsultTable(data.response ?? []);
        } catch (err) {
            console.error('조회 실패:', err);
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

        const sorted = [...data].sort((a, b) => (b.consultDate || '').localeCompare(a.consultDate || ''));
        const groups = groupByPhone(sorted);
        updateSummary(groups);

        groups.forEach((records, idx) => {
            const main        = records[0];
            const history     = records.slice(1);
            const progressKey = main.progressKey || 'counseling';
            const escapedMemo = (main.content || '').replace(/"/g, '&quot;');

            const lastColHtml = main.registerDate
                ? `<div>${main.registerDate.split(' ')[0]}</div>`
                : `<div class="link-area">
                       <button type="button" class="join-link">가입 링크 발송</button>
                   </div>`;

            const historyHtml = history.length > 0
                ? `<div class="history">
                       <h3>이전 상담 이력</h3>
                       <ul>
                           ${history.map(h => `
                               <li>
                                   <time>${h.consultDate || ''}</time>
                                   <span>${h.userName || ''}</span>
                                   <p>${h.content || ''}</p>
                               </li>`).join('')}
                       </ul>
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
                    data-type="${main.type || ''}">

                    <div class="consult-row">
                        <div>${idx + 1}</div>
                        <div>${main.consultDate || ''}</div>
                        <div class="student">
                            <strong>${main.studentName || ''}</strong>
                            <span>${main.phone || ''}</span>
                        </div>
                        <div>${main.gradeName || ''}</div>
                        <div>${getTypeTag(main.type)}</div>
                        <div>${main.userName || ''}</div>
                        <div class="memo">${main.content || ''}</div>
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
                        ${history.length > 0
                ? `<button type="button" class="accordion-btn">
                                   <i class="fa-solid fa-chevron-down"></i>
                               </button>`
                : `<div></div>`}
                    </div>
                    ${historyHtml}
                </article>
            `);
        });

        attachAccordionEvents();
        attachDropdownEvents();
        attachJoinLinkEvents();
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
                const button   = dropdown.querySelector('.status-current');
                const status   = this.dataset.status;
                const label    = this.dataset.label;
                const article  = this.closest('article.consult-item');
                const id       = article?.dataset.id;

                if (!id) return;

                button.querySelector('span').textContent = label;
                button.classList.remove(...statusClassNames);
                button.classList.add(getStatusClass(status));
                dropdown.classList.remove('open');

                try {
                    const res = await fetch('/consult/update-progress', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ id, progressKey: status })
                    });
                    if (!res.ok) alert('진행상황 변경 실패');
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
                const article     = this.closest('article.consult-item');
                const rawPhone    = article.dataset.phone || '';
                const studentName = article.dataset.studentName || '';
                const gradeKey    = article.dataset.gradeKey || '';
                const consultId   = article.dataset.id || '';
                const type        = article.dataset.type || '';

                let phone = rawPhone.replace(/-/g, '').trim();
                if (!/^[0-9]+$/.test(phone)) { alert('전화번호는 숫자만 입력해 주세요.'); return; }
                if (phone.length === 8) phone = '010' + phone;
                else if (!(phone.startsWith('010') && phone.length === 11)) {
                    alert('올바른 전화번호가 아닙니다.'); return;
                }

                if (!confirm(`${studentName} 학생에게 가입링크를 발송하시겠습니까?`)) return;

                try {
                    const res    = await fetch('/popbill/send-join', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            source: 'CONSULT', phone, name: studentName,
                            gradeKey, consultId,
                            subHoho: type === 'hoho',
                            subHan:  type === 'han',
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

    /* =================== *
     *   모달              *
     * =================== */
    document.getElementById('consult-add')?.addEventListener('click', () => {
        consultModal.classList.add('show');
    });

    document.getElementById('modal-close-btn')?.addEventListener('click', () => {
        consultModal.classList.remove('show');
    });

    consultModal?.addEventListener('click', (e) => {
        if (e.target === consultModal) consultModal.classList.remove('show');
    });

    /* =================== *
     *   기간 라디오        *
     * =================== */
    document.querySelectorAll('input[name="period"]').forEach(radio => {
        radio.addEventListener('change', (e) => {
            if (e.target.value === 'custom') return;
            const months = e.target.value === '6month' ? 6 : e.target.value === '3month' ? 3 : 1;
            const range  = getDateRange(months);
            startDateInput.value = range.startDate;
            endDateInput.value   = range.endDate;
        });
    });

    /* =================== *
     *   조회 버튼          *
     * =================== */
    document.getElementById('search-btn')?.addEventListener('click', async () => {
        const startDate = startDateInput.value;
        const endDate   = endDateInput.value;
        if (!startDate || !endDate) { alert('조회 기간을 선택해주세요.'); return; }
        if (startDate > endDate)    { alert('시작일이 종료일보다 늦을 수 없습니다.'); return; }
        await fetchConsults(startDate, endDate, currentUserCode);
    });

    /* =================== *
     *   필터 초기화        *
     * =================== */
    document.getElementById('reset-btn')?.addEventListener('click', async () => {
        const range = getDateRange(1);
        startDateInput.value = range.startDate;
        endDateInput.value   = range.endDate;
        if (progressFilter) progressFilter.value = 'all';
        if (keywordInput)   keywordInput.value   = '';
        document.querySelector('input[name="period"][value="1month"]').checked = true;
        await fetchConsults(range.startDate, range.endDate, currentUserCode);
    });

    /* =================== *
     *   초기 로드          *
     * =================== */
    (async () => {
        const range = getDateRange(1);
        startDateInput.value = range.startDate;
        endDateInput.value   = range.endDate;
        await fetchConsults(range.startDate, range.endDate, currentUserCode);
    })();

});