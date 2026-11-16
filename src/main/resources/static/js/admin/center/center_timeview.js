document.addEventListener('DOMContentLoaded', () => {
    // ────────────────────────────────────────
    // DOM 참조
    // ────────────────────────────────────────
    const appShell = document.getElementById('appShell');
    const navButtons = document.querySelectorAll('.nav-icon');
    const depthMenus = document.querySelectorAll('.depth-menu');

    const monthPicker = document.getElementById('monthPicker');
    const monthDisplay = document.getElementById('monthDisplay');
    const centerSelect = document.getElementById('centerSelect');
    const teacherSelect = document.getElementById('teacherSelect');
    const subjectSelect = document.getElementById('subjectSelect');
    const timetableTitleMonth = document.getElementById('timetableTitleMonth');
    const timetableTitleTeacher = document.getElementById('timetableTitleTeacher');
    const timetableBody = document.getElementById('timetableBody');
    const searchBtn = document.getElementById('searchBtn');
    const resetFilterBtn = document.getElementById('resetFilterBtn');
    const classStatusRows = document.querySelectorAll('#classStatusBody .status-row[data-gb]');
    const statusTotalStudents = document.getElementById('statusTotalStudents');
    const statusTotalClasses = document.getElementById('statusTotalClasses');
    const statusTotalBooks = document.getElementById('statusTotalBooks');
    const bookStatusBody = document.getElementById('bookStatusBody');

    // ────────────────────────────────────────
    // 상태
    // ────────────────────────────────────────
    let openedMenu = null;
    let currentTimetables = [];
    let allTeachers = []; // 선택된 센터의 전체 선생님 (정렬 완료)
    let timetableRequestSeq = 0; // 연속 변경 시 늦게 도착한 이전 응답 무시용

    // 선생님 드롭다운 우선 정렬 순서 (목록에 없는 선생님은 뒤에 기존 순서대로)
    const TEACHER_ORDER = ['이정애', '김도경', '장혜진', '이경은', '노수연', '성해랑', '이경진'];

    // ────────────────────────────────────────
    // 유틸
    // ────────────────────────────────────────
    const openPicker = (picker) => {
        if (typeof picker.showPicker === 'function') picker.showPicker();
        else picker.focus();
    };

    const escapeHtml = (value) => String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');

    // ────────────────────────────────────────
    // 사이드바
    // ────────────────────────────────────────
    navButtons.forEach((button) => {
        button.addEventListener('click', () => {
            const target = button.dataset.menu;
            const isSameOpenMenu = openedMenu === target && appShell.classList.contains('depth-open');

            if (isSameOpenMenu) {
                appShell.classList.remove('depth-open');
                button.classList.remove('active');
                openedMenu = null;
                return;
            }

            openedMenu = target;
            appShell.classList.add('depth-open');
            navButtons.forEach((item) => item.classList.toggle('active', item === button));
            depthMenus.forEach((menu) => menu.classList.toggle('active', menu.dataset.depth === target));
        });
    });

    // ────────────────────────────────────────
    // 달력 피커
    // ────────────────────────────────────────
    document.querySelectorAll('.calendar-btn').forEach((button) => {
        button.addEventListener('click', () => {
            const picker = document.getElementById(button.dataset.picker);
            openPicker(picker);
        });
    });

    // 'YYYY-MM' → 'YYYY년 M월'
    const formatYearMonth = (value) => {
        const [year, month] = value.split('-');
        return `${year}년 ${Number(month)}월`;
    };

    const renderMonth = () => {
        if (!monthPicker.value) return;
        const label = formatYearMonth(monthPicker.value);
        monthDisplay.value = label;
        timetableTitleMonth.textContent = label;
    };

    monthPicker.addEventListener('change', async () => {
        renderMonth();
        // 호호스쿨은 월별 시간표 기준이라 월이 바뀌면 선생님 목록도 다시 거름
        if (subjectSelect.value === 'hoho') await renderTeacherOptions();
        fetchTimetable();
    });
    renderMonth();

    // ────────────────────────────────────────
    // 센터 선택 → 선생님 목록 조회
    // ────────────────────────────────────────
    const sortTeachers = (teachers) => {
        const rank = (name) => {
            const idx = TEACHER_ORDER.indexOf(name);
            return idx === -1 ? TEACHER_ORDER.length : idx;
        };
        return teachers
            .map((t, i) => ({ t, i }))
            .sort((a, b) => rank(a.t.userName) - rank(b.t.userName) || a.i - b.i)
            .map(({ t }) => t);
    };

    // 과목별 선생님 필터
    //  - 한스쿨/북스쿨 : erp_user.is_han / is_book
    //  - 호호스쿨      : 선택한 수강 월에 subject = 'hoho' 시간표가 있는 선생님
    //  - ULS001 은 secondary DB 라 과목 정보가 없어 필터하지 않음
    const filterTeachersBySubject = async (teachers, subject) => {
        const centerCode = centerSelect.value;
        if (!subject || centerCode === 'ULS001') return teachers;
        if (subject === 'han') return teachers.filter((t) => t.isHan);
        if (subject === 'book') return teachers.filter((t) => t.isBook);

        const [year, month] = monthPicker.value.split('-');
        const res = await fetch(`/center/teachers/by-subject?centerCode=${centerCode}&year=${year}&month=${month}&subject=${subject}`);
        const userCodes = new Set((await res.json()) || []);
        return teachers.filter((t) => userCodes.has(t.userCode));
    };

    const updateTeacherTitle = () => {
        timetableTitleTeacher.textContent = teacherSelect.value
            ? `${teacherSelect.selectedOptions[0].textContent} 선생님`
            : '전체';
    };

    const renderTeacherOptions = async () => {
        const prevValue = teacherSelect.value;
        const teachers = await filterTeachersBySubject(allTeachers, subjectSelect.value);

        teacherSelect.innerHTML = '<option value="">전체</option>';
        teachers.forEach((t) => {
            const option = document.createElement('option');
            option.value = t.userCode;
            option.textContent = t.userName;
            teacherSelect.appendChild(option);
        });

        // 기존 선택 선생님이 필터 후에도 남아 있으면 유지
        teacherSelect.value = teachers.some((t) => t.userCode === prevValue) ? prevValue : '';
        updateTeacherTitle();
    };

    const fetchTeachers = async (centerCode) => {
        allTeachers = [];
        if (centerCode) {
            const res = await fetch(`/center/teachers?centerCode=${centerCode}`);
            allTeachers = sortTeachers((await res.json()) || []);
        }
        await renderTeacherOptions();
    };

    centerSelect.addEventListener('change', async () => {
        await fetchTeachers(centerSelect.value);
        fetchTimetable();
    });

    // ────────────────────────────────────────
    // 시간표 영역 : 수업 카드 렌더링
    // ────────────────────────────────────────
    const clearTimetable = () => {
        // 셀만 비움 (조회 데이터 currentTimetables 는 유지)
        timetableBody.querySelectorAll('td[data-day]').forEach((td) => {
            td.innerHTML = '';
            td.classList.remove('has-class');
        });
    };

    // subject 가 비어 있으면 class_type 으로 대체 (1: 한스쿨, 2: 북스쿨)
    const resolveSubject = (tt) => {
        if (tt.subject) return tt.subject;
        return tt.classType === '1' ? 'han' : 'book';
    };

    const createClassCard = (tt) => {
        const students = tt.students || [];
        const title = tt.unitName ? `${tt.className} ${tt.unitName}` : tt.className;
        const card = document.createElement('div');
        card.className = `class-card ${resolveSubject(tt)}`;
        card.innerHTML = `
            <div class="class-card-header">
                <div class="class-card-head">
                    <span class="class-card-time">${escapeHtml(tt.startTime)} ~ ${escapeHtml(tt.endTime)}</span>
                    <span class="class-card-count">${students.length}명</span>
                </div>
                <strong class="class-card-title">${escapeHtml(title)}</strong>
            </div>
            <ul class="class-card-students">
                ${students.map((s) => `<li>${escapeHtml(s.studentName)}${s.gradeName ? `<span class="student-grade">(${escapeHtml(s.gradeName)})</span>` : ''}</li>`).join('')}
            </ul>
        `;
        return card;
    };

    const renderTimetable = () => {
        clearTimetable();
        const subject = subjectSelect.value;
        currentTimetables
            .filter((tt) => !subject || resolveSubject(tt) === subject)
            .forEach((tt) => {
                const td = timetableBody.querySelector(`tr[data-period="${tt.periodNo}"] td[data-day="${tt.dayname}"]`);
                if (!td) return;
                td.appendChild(createClassCard(tt));
                td.classList.add('has-class');
            });
    };

    // 필터 변경 시 자동 조회 : 센터 + 선생님이 모두 선택된 경우에만 조회, 아니면 비움
    // notify = true 면 (조회 버튼) 미선택 항목을 알림으로 안내
    const fetchTimetable = async ({ notify = false } = {}) => {
        const centerCode = centerSelect.value;
        const teacherCode = teacherSelect.value;
        const seq = ++timetableRequestSeq;

        if (!centerCode || !teacherCode) {
            currentTimetables = [];
            clearTimetable();
            renderClassStatus(null);
            renderBookStatus(null);
            if (notify) alert(!centerCode ? '센터를 선택해주세요.' : '선생님을 선택해주세요.');
            return;
        }

        const [year, month] = monthPicker.value.split('-');
        const query = `centerCode=${centerCode}&userCode=${teacherCode}&year=${year}&month=${month}`;
        const [timetableRes, statusRes, bookRes] = await Promise.all([
            fetch(`/center/timetable?${query}`),
            fetch(`/center/student-status?${query}`),
            fetch(`/center/book-orders?${query}`),
        ]);
        const data = (await timetableRes.json()) || [];
        const status = (await statusRes.json()).response;
        const bookOrders = (await bookRes.json()) || [];
        if (seq !== timetableRequestSeq) return;

        currentTimetables = data;
        renderTimetable();
        renderClassStatus(status);
        renderBookStatus(bookOrders);
    };

    // ────────────────────────────────────────
    // 현황 영역 : 수업 현황
    // ────────────────────────────────────────
    // status : /center/student-status 응답 (ULS001 은 null)
    const renderClassStatus = (status) => {
        const statsMap = (status && status.statsMap) || {};

        classStatusRows.forEach((row) => {
            const stats = statsMap[row.dataset.gb] || [];
            row.querySelector('.status-value').textContent = status ? `${stats.length}명` : '-';
        });

        if (status) {
            const totalLong = status.totalStudentsLong;
            const totalDouble = status.totalStudentsDouble;
            statusTotalStudents.textContent = totalLong !== totalDouble
                ? `${totalLong}명 (${totalDouble}강좌)`
                : `${totalLong}명`;
        } else {
            statusTotalStudents.textContent = '-';
        }

        // 총 교재 수량 = 시간표에 배정된 학생 수 합 (주문 수량은 교재 현황의 전체 합계)
        const hasTeacher = !!teacherSelect.value;
        const assignedCount = currentTimetables.reduce((sum, tt) => sum + (tt.students || []).length, 0);
        statusTotalClasses.textContent = hasTeacher ? `${currentTimetables.length}개` : '-';
        statusTotalBooks.textContent = hasTeacher ? `${assignedCount}권` : '-';
    };

    // ────────────────────────────────────────
    // 현황 영역 : 교재 현황 (과목 / 단계 / 교재수량 + 전체 합계)
    // ────────────────────────────────────────
    // class_key 앞의 'N' 은 떼고 비교 (주문 쿼리와 동일 기준)
    const normalizeClassKey = (classKey) => (classKey && classKey.startsWith('N') ? classKey.slice(1) : classKey);
    const bookKey = (classKey, unitKey) => `${normalizeClassKey(classKey)}|${unitKey}`;

    // 시간표 교재별 배정 학생 수 (수업 현황의 총 교재 수량과 같은 기준)
    const countTimetableBooks = () => {
        const counts = new Map();
        currentTimetables.forEach((tt) => {
            if (!tt.classKey) return;
            const key = bookKey(tt.classKey, tt.unitKey);
            const prev = counts.get(key) || { className: tt.className, unitName: tt.unitName, count: 0 };
            prev.count += (tt.students || []).length;
            counts.set(key, prev);
        });
        return counts;
    };

    // books : /center/book-orders 응답 (정기 주문 + 승인된 추가/반품 수량), null 이면 선생님 미선택
    // 주문 수량이 시간표 배정 학생 수와 다르면 mismatch 로 표시 (시간표에만 있는 교재는 주문 0권으로 추가)
    const renderBookStatus = (books) => {
        if (!books) {
            bookStatusBody.innerHTML = '<p class="status-empty">선생님을 선택하면 교재 현황이 표시됩니다.</p>';
            return;
        }

        const timetableCounts = countTimetableBooks();
        const rows = books.map((b) => {
            const key = bookKey(b.classKey, b.unitKey);
            const timetableCount = timetableCounts.get(key)?.count ?? 0;
            timetableCounts.delete(key);
            return { className: b.className, unitName: b.unitName, qty: b.qty || 0, timetableCount };
        });
        timetableCounts.forEach((t) => {
            if (t.count > 0) rows.push({ className: t.className, unitName: t.unitName, qty: 0, timetableCount: t.count });
        });

        if (rows.length === 0) {
            bookStatusBody.innerHTML = '<p class="status-empty">주문 내역이 없습니다.</p>';
            return;
        }

        const totalQty = rows.reduce((sum, r) => sum + r.qty, 0);
        const totalTimetable = rows.reduce((sum, r) => sum + r.timetableCount, 0);
        const mismatchAttr = (qty, timetableCount) => (qty !== timetableCount
            ? ` class="mismatch" title="시간표 배정 ${timetableCount}명"`
            : '');

        bookStatusBody.innerHTML = `
            <table class="book-table">
                <colgroup>
                    <col>
                    <col style="width: 26%">
                    <col style="width: 28%">
                </colgroup>
                <thead>
                    <tr><th>과목</th><th>단계</th><th>교재수량</th></tr>
                </thead>
                <tbody>
                    ${rows.map((r) => `
                        <tr${mismatchAttr(r.qty, r.timetableCount)}>
                            <td>${escapeHtml(r.className) || '-'}</td>
                            <td>${escapeHtml(r.unitName) || '-'}</td>
                            <td>${r.qty}권</td>
                        </tr>`).join('')}
                </tbody>
                <tfoot>
                    <tr${mismatchAttr(totalQty, totalTimetable)}><td>전체 합계</td><td></td><td>${totalQty}권</td></tr>
                </tfoot>
            </table>
        `;
    };

    searchBtn.addEventListener('click', () => fetchTimetable({ notify: true }));
    subjectSelect.addEventListener('change', async () => {
        const prevTeacher = teacherSelect.value;
        await renderTeacherOptions();
        // 선생님이 그대로면 이미 받은 데이터로 과목만 다시 거르고, 바뀌었으면 다시 조회
        if (teacherSelect.value === prevTeacher) renderTimetable();
        else fetchTimetable();
    });

    // ────────────────────────────────────────
    // 선생님 선택
    // ────────────────────────────────────────
    teacherSelect.addEventListener('change', () => {
        updateTeacherTitle();
        fetchTimetable();
    });

    // ────────────────────────────────────────
    // 필터 초기화 : 센터·과목·선생님 해제, 수강 월은 처음 화면의 월(이번 달)로
    // ────────────────────────────────────────
    resetFilterBtn.addEventListener('click', async () => {
        centerSelect.value = '';
        subjectSelect.value = '';
        monthPicker.value = monthPicker.defaultValue;
        renderMonth();
        await fetchTeachers('');
        fetchTimetable();
    });

    // ────────────────────────────────────────
    // 출력 / 내려받기 (미구현)
    // ────────────────────────────────────────
    ['timetablePrintBtn', 'centerPrintBtn', 'excelDownloadBtn'].forEach((id) => {
        document.getElementById(id)?.addEventListener('click', () => alert('미구현 기능입니다.'));
    });
});
