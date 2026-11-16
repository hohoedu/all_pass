document.addEventListener('DOMContentLoaded', () => {
    const centerSelect = document.getElementById('centerSelect');
    const yearSelect = document.getElementById('yearSelect');
    const loading = document.getElementById('searchLoading');
    const area = document.getElementById('reportArea');

    const KINDS = [['join', '입회'], ['withdraw', '탈퇴'], ['in', '전입'], ['out', '전출'], ['graduate', '졸업'], ['member', '회원']];
    const fmt = (v) => (v ? Math.round(v * 100) / 100 : 0);
    const esc = (s) => String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
    const sumKind = (mon, kind) => Object.values(mon.cells).reduce((a, c) => a + (c[kind] || 0), 0);

    function requireCenter() {
        if (!centerSelect.value) {
            alert('센터를 선택해주세요.');
            return false;
        }
        return true;
    }

    let loaded = null; // 마지막 조회 { centerCode, year } : 저장은 조회한 센터·연도로
    const teacherBox = document.getElementById('teacherBox');
    const teacherBtn = document.getElementById('teacherBtn');

    // 드롭다운 열고 닫기 (바깥 클릭 시 닫힘)
    function toggleTeachers(open) {
        teacherBox.hidden = !open;
        teacherBtn.setAttribute('aria-expanded', String(open));
    }
    teacherBtn.addEventListener('click', () => toggleTeachers(teacherBox.hidden));
    document.addEventListener('click', (e) => {
        if (!e.target.closest('.mc-dd')) toggleTeachers(false);
    });

    // 버튼 문구 : 전체 / 선생님 1명 이름 / "이정애 외 2명"
    function updateTeacherBtn() {
        const boxes = [...teacherBox.querySelectorAll('.mc-teacher')];
        const picked = boxes.filter((b) => b.checked);
        teacherBtn.textContent = !boxes.length || picked.length === boxes.length ? '전체'
            : !picked.length ? '선택 안 함'
                : picked.length === 1 ? picked[0].dataset.name
                    : `${picked[0].dataset.name} 외 ${picked.length - 1}명`;
    }

    // 센터·연도를 바꾸면 그 센터 선생님 목록을 체크박스로 (기본 전체 선택)
    async function loadTeachers() {
        teacherBtn.textContent = '전체';
        if (!centerSelect.value) {
            teacherBox.textContent = '센터를 먼저 선택해주세요.';
            return;
        }
        teacherBox.textContent = '불러오는 중...';
        try {
            const params = new URLSearchParams({ centerCode: centerSelect.value, year: yearSelect.value });
            const res = await fetch(`/admin/center/member-change/teachers?${params}`);
            if (!res.ok) throw new Error(res.status);
            const teachers = (await res.json()).response || [];
            if (!teachers.length) {
                teacherBox.textContent = '해당 연도 시간표가 있는 선생님이 없어요.';
                return;
            }
            // all_pass 공통 체크박스 (2logistics_2order.css : check-label / check-input / check-mark)
            teacherBox.innerHTML = `<label class="check-label mc-dd-all"><input class="check-input" type="checkbox" id="teacherAll" checked><span class="check-mark"></span>전체</label>`
                + teachers.map((t) => `<label class="check-label"><input class="check-input mc-teacher" type="checkbox" value="${esc(t.userCode)}" data-name="${esc(t.userName)}" checked><span class="check-mark"></span>${esc(t.userName)}</label>`).join('');
            const all = document.getElementById('teacherAll');
            const boxes = [...teacherBox.querySelectorAll('.mc-teacher')];
            all.addEventListener('change', () => {
                boxes.forEach((b) => (b.checked = all.checked));
                updateTeacherBtn();
            });
            boxes.forEach((b) => b.addEventListener('change', () => {
                all.checked = boxes.every((x) => x.checked);
                updateTeacherBtn();
            }));
        } catch (e) {
            teacherBox.textContent = '선생님 목록을 불러오지 못했어요.';
        }
    }
    centerSelect.addEventListener('change', loadTeachers);
    yearSelect.addEventListener('change', loadTeachers);

    // 조회 조건 : 전체 선택이면 선생님 조건 없음
    function queryParams() {
        const params = new URLSearchParams({ centerCode: centerSelect.value, year: yearSelect.value });
        const boxes = [...teacherBox.querySelectorAll('.mc-teacher')];
        const picked = boxes.filter((b) => b.checked).map((b) => b.value);
        if (boxes.length && picked.length < boxes.length) params.set('userCodes', picked.join(','));
        return { params, picked, boxes };
    }

    // 우측 상단 알림 배너 (잠깐 떴다 사라짐)
    let toastTimer = null;
    function toast(message, isError) {
        let el = document.getElementById('mcToast');
        if (!el) {
            el = document.createElement('div');
            el.id = 'mcToast';
            el.className = 'mc-toast';
            el.setAttribute('role', 'status');
            el.setAttribute('aria-live', 'polite');
            document.body.appendChild(el);
        }
        el.textContent = message;
        el.classList.toggle('mc-toast-error', !!isError);
        el.classList.add('show');
        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => el.classList.remove('show'), isError ? 4000 : 2000);
    }

    async function saveEvent(input) {
        if (input.value.trim() === input.dataset.saved) return; // 바뀐 게 없으면 저장 안 함 (중복 요청 방지)
        input.classList.remove('mc-saved', 'mc-error');
        try {
            const res = await fetch('/admin/center/member-change/event', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ ...loaded, month: Number(input.dataset.month), content: input.value }),
            });
            if (!res.ok) {
                const body = await res.json().catch(() => ({}));
                throw new Error(`${res.status} ${body?.error?.message || body?.msg || ''}`);
            }
            input.dataset.saved = input.value.trim();
            input.classList.add('mc-saved');
            toast(`${input.dataset.month}월 행사가 ${input.dataset.saved ? '저장' : '삭제'}되었습니다.`);
        } catch (e) {
            input.classList.add('mc-error');
            toast(`${input.dataset.month}월 행사 저장에 실패했습니다. (${e.message})`, true);
        }
    }

    function summaryTable(report, events) {
        const subj = report.map((s) => esc(s.title.replace('스쿨', '')));
        const acc = new Array(7).fill(0);
        let rows = '';
        for (let m = 0; m < 12; m++) {
            const mons = report.map((s) => s.months[m]);
            const consult = mons.reduce((a, x) => a + x.consult, 0);
            const join = mons.map((x) => sumKind(x, 'join'));
            const withdraw = mons.map((x) => sumKind(x, 'withdraw'));
            const member = mons.map((x) => x.total); // 각 스쿨 전체총원
            acc[0] += consult;
            [...join, ...withdraw].forEach((v, i) => (acc[i + 1] += v));
            rows += `<tr><td class="mc-event-cell"><input class="mc-event" maxlength="100" data-month="${m + 1}" data-saved="${esc(events[m + 1] || '')}" value="${esc(events[m + 1] || '')}"></td><td>${m + 1}월</td><td>${fmt(consult)}</td><td>${fmt(member.reduce((a, b) => a + b, 0))}</td>`
                + [...join, ...withdraw, ...member].map((v) => `<td>${fmt(v)}</td>`).join('') + '</tr>';
        }
        return `<section class="panel mc-panel"><h2>전체 요약</h2><div class="mc-scroll"><table class="mc-table">
            <thead>
              <tr><th rowspan="2">행사 or 이슈</th><th rowspan="2">월</th><th rowspan="2">상담문의</th><th rowspan="2">전체총원</th>
                  <th colspan="3">입회</th><th colspan="3">탈퇴</th><th colspan="3">회원</th></tr>
              <tr>${[...subj, ...subj, ...subj].map((s) => `<th>${s}</th>`).join('')}</tr>
            </thead>
            <tbody>${rows}</tbody>
            <tfoot><tr><td></td><td>누적</td><td>${fmt(acc[0])}</td><td>ㅡ</td>${acc.slice(1).map((v) => `<td>${fmt(v)}</td>`).join('')}<td>ㅡ</td><td>ㅡ</td><td>ㅡ</td></tr></tfoot>
        </table></div></section>`;
    }

    function subjectTable(subject, idx) {
        const teachers = subject.teachers;
        const acc = teachers.map(() => new Array(KINDS.length).fill(0));
        let consultAcc = 0;
        const rows = subject.months.map((mon) => {
            consultAcc += mon.consult;
            const cells = teachers.map((t, ti) => KINDS.map(([k], ki) => {
                const v = (mon.cells[t] || {})[k] || 0;
                acc[ti][ki] += v;
                return `<td>${fmt(v)}</td>`;
            }).join('')).join('');
            return `<tr><td>${mon.month}월</td><td>${fmt(mon.consult)}</td><td>${fmt(mon.total)}</td>${cells}</tr>`;
        }).join('');
        const foot = acc.map((a) => a.map((v, ki) => `<td>${KINDS[ki][0] === 'member' ? 'ㅡ' : fmt(v)}</td>`).join('')).join('');
        return `<section class="panel mc-panel"><h2>${idx + 1}. ${esc(subject.title)} 회원 변동</h2><div class="mc-scroll"><table class="mc-table">
            <thead>
              <tr><th rowspan="2">월</th><th rowspan="2">상담문의</th><th rowspan="2">전체총원</th>
                  ${teachers.map((t) => `<th colspan="${KINDS.length}">${esc(t)}</th>`).join('')}</tr>
              <tr>${teachers.map(() => KINDS.map(([, n]) => `<th>${n}</th>`).join('')).join('')}</tr>
            </thead>
            <tbody>${rows}</tbody>
            <tfoot><tr><td>누적</td><td>${fmt(consultAcc)}</td><td>ㅡ</td>${foot}</tr></tfoot>
        </table></div></section>`;
    }

    document.getElementById('searchBtn').addEventListener('click', async () => {
        if (!requireCenter()) return;
        const { params, picked, boxes } = queryParams();
        if (boxes.length && !picked.length) {
            alert('선생님을 한 명 이상 선택해주세요.');
            return;
        }
        loading.hidden = false;
        try {
            const query = { centerCode: centerSelect.value, year: yearSelect.value };
            const evParams = new URLSearchParams(query);
            const [res, evRes] = await Promise.all([
                fetch(`/admin/center/member-change/data?${params}`),
                fetch(`/admin/center/member-change/events?${evParams}`),
            ]);
            if (!res.ok || !evRes.ok) throw new Error(res.status);
            const report = (await res.json()).response;
            const events = (await evRes.json()).response || {};
            loaded = query;
            area.innerHTML = summaryTable(report, events) + [1, 0, 2].map((i, n) => subjectTable(report[i], n)).join(''); // 한·북·호호 순
            area.querySelectorAll('.mc-event').forEach((input) => {
                input.addEventListener('change', () => saveEvent(input)); // 입력 후 포커스 이동 시 바로 저장
                input.addEventListener('keydown', (e) => {
                    // 한글 조합 중 Enter 는 무시 (마지막 글자가 두 번 들어가는 현상 방지)
                    if (e.isComposing || e.keyCode === 229) return;
                    if (e.key === 'Enter') input.blur();
                });
            });
        } catch (e) {
            alert('조회 중 오류가 발생했습니다.');
        } finally {
            loading.hidden = true;
        }
    });

    document.getElementById('excelBtn').addEventListener('click', () => {
        if (!requireCenter()) return;
        const { params, picked, boxes } = queryParams();
        if (boxes.length && !picked.length) {
            alert('선생님을 한 명 이상 선택해주세요.');
            return;
        }
        location.href = `/admin/center/member-change/excel?${params}`;
    });
});
