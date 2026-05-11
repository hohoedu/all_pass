document.addEventListener('DOMContentLoaded', () => {

    let selectedUserCode = null;
    const sessionUserCode = document.getElementById('sessionUserCode').value;

    // ── 선생님 등록 모달  ──────────────────────────────────────────────────────
    // 모달 열기
    document.getElementById('addTeacherBtn').addEventListener('click', () => {
        document.querySelector('.manager-modal').style.display = 'flex';
    });

    document.querySelector('.manager-modal .btn-close').addEventListener('click', (e) => {
        e.preventDefault();
        document.querySelector('.manager-modal').style.display = 'none';
    });

    document.querySelector('.manager-modal').addEventListener('click', (e) => {
        e.stopPropagation();
    });

    // ── 선생님 등록 저장 ──────────────────────────────────────────────────────
    document.getElementById('addTeacherSaveBtn').addEventListener('click', async () => {

        const userId = document.getElementById('add-id').value.trim();
        const password = document.getElementById('add-password').value;
        const passwordChk = document.getElementById('add-password-chk').value;
        const userName = document.getElementById('add-name').value.trim();
        const phone = document.getElementById('add-phone').value.trim();
        const roleKey = document.querySelector('input[name="addRoleKey"]:checked').value;
        const useYn = document.querySelector('input[name="addUseYn"]:checked').value === 'true';
        const han = document.getElementById('add-han').checked;
        const book = document.getElementById('add-book').checked;
        const clinic = document.getElementById('add-clinic').checked;

        // 유효성 검사
        if (!userId) {
            alert('아이디를 입력해주세요.');
            return;
        }
        if (!password) {
            alert('비밀번호를 입력해주세요.');
            return;
        }
        if (password !== passwordChk) {
            alert('비밀번호가 일치하지 않습니다.');
            return;
        }
        if (!userName) {
            alert('이름을 입력해주세요.');
            return;
        }

        try {
            const response = await fetch('/user/register', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({userId, password, userName, phone, roleKey, useYn, han, book, clinic})
            });
            const result = await response.json();

            if (response.ok) {
                alert('등록되었습니다.');
                document.querySelector('.manager-modal').style.display = 'none';
                location.reload();
            } else {
                alert(result.msg || '등록에 실패했습니다.');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('서버 오류가 발생했습니다.');
        }
    });

    // ── 선생님 정보 조회 (권함포함) ──────────────────────────────────────────────────────
    document.querySelectorAll('.teacher-row').forEach(row => {
        row.addEventListener('click', async () => {
            document.querySelectorAll('.teacher-row').forEach(r => r.classList.remove('is-selected'));
            row.classList.add('is-selected');

            selectedUserCode = row.dataset.userCode;

            try {
                const response = await fetch(`/manage/teacher/${selectedUserCode}`);
                const result = await response.json();
                renderTeacherDetail(result.response);
            } catch (error) {
                console.error('선생님 정보 로드 실패:', error);
            }
        });
    });

    function renderTeacherDetail(data) {
        document.getElementById('display-name').textContent = data.userName;
        document.getElementById('display-id').textContent = data.userId;

        document.getElementById('chk-manager').checked = data.roleKey === 'ADMIN';
        document.getElementById('chk-teacher').checked = data.roleKey !== 'ADMIN';

        document.getElementById('chk-use-yn').checked = data.useYn;
        document.getElementById('use-yn-label').textContent = data.useYn ? '사용' : '미사용';

        document.getElementById('hanja').checked = data.han ?? false;
        document.getElementById('read').checked = data.book ?? false;
        document.getElementById('readClinic').checked = data.clinic ?? false;

        document.getElementById('newPassword').value = '';
        document.getElementById('confirmPassword').value = '';

        renderMenuPermissions(data.menuPermissions);
    }

    function renderMenuPermissions(permissions) {
        document.querySelectorAll('.menu-permission-table tbody tr').forEach(row => {
            const menuId = row.dataset.menuId;
            const perm = permissions?.find(p => String(p.menuId) === String(menuId));

            if (!perm) {
                row.querySelectorAll('input[type="checkbox"]').forEach(chk => chk.checked = false);
                return;
            }

            row.querySelector('.chk-read').checked = perm.canRead ?? false;
            row.querySelector('.chk-write').checked = perm.canWrite ?? false;
            row.querySelector('.chk-delete').checked = perm.canDelete ?? false;

            row.querySelector('.chk-all').checked = perm.canRead && perm.canWrite && perm.canDelete;
        });
    }

    // ── 비밀번호 변경 ─────────────────────────────────────────────────────────
    document.getElementById('changePasswordBtn').addEventListener('click', async function () {

        if (!selectedUserCode) {
            alert('선생님을 선택해주세요.');
            return;
        }

        if (selectedUserCode !== sessionUserCode) {
            alert('본인의 비밀번호만 변경할 수 있습니다.');
            return;
        }

        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        if (!newPassword || !confirmPassword) {
            alert('비밀번호를 입력해주세요.');
            return;
        }
        if (newPassword !== confirmPassword) {
            alert('비밀번호가 일치하지 않습니다.');
            return;
        }

        try {
            const response = await fetch('/user/password', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({newPassword})
            });
            const data = await response.json();

            if (response.ok) {
                alert('비밀번호가 변경되었습니다.');
                document.getElementById('newPassword').value = '';
                document.getElementById('confirmPassword').value = '';
            } else {
                alert(data.message || '비밀번호 변경에 실패했습니다.');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('서버 오류가 발생했습니다.');
        }
    });

    document.getElementById('chk-use-yn').addEventListener('change', function () {
        document.getElementById('use-yn-label').textContent = this.checked ? '사용' : '미사용';
    });

    // ── 메뉴 권한 테이블 ──────────────────────────────────────────────────────
    const table = document.querySelector('.menu-permission-table');
    const PERM_CLASSES = ['chk-read', 'chk-write', 'chk-delete'];

    table.addEventListener('change', (e) => {
        const target = e.target;
        const row = target.closest('tr');

        if (target.classList.contains('chk-all')) {
            PERM_CLASSES.forEach(cls => {
                row.querySelector(`.${cls}`).checked = target.checked;
            });

            if (!target.checked && row.classList.contains('parent-menu')) {
                getChildren(row).forEach(child => {
                    child.querySelectorAll('input[type="checkbox"]').forEach(chk => chk.checked = false);
                });
            }

            if (row.classList.contains('child-menu')) {
                syncParentChkAll(row);
            }
            return;
        }

        if (PERM_CLASSES.some(cls => target.classList.contains(cls))) {
            const allChecked = PERM_CLASSES.every(cls => row.querySelector(`.${cls}`).checked);
            row.querySelector('.chk-all').checked = allChecked;

            if (row.classList.contains('parent-menu') && !target.checked) {
                const changedCls = PERM_CLASSES.find(cls => target.classList.contains(cls));
                getChildren(row).forEach(child => {
                    child.querySelector(`.${changedCls}`).checked = false;

                    const allChecked = PERM_CLASSES.every(cls => child.querySelector(`.${cls}`).checked);
                    child.querySelector('.chk-all').checked = allChecked;
                });
            }

            if (row.classList.contains('child-menu')) {
                syncParentPerms(row);
            }
        }

        if (row.classList.contains('child-menu')) {
            if (target.classList.contains('chk-all')) {
                setChecks(row, PERM_CLASSES, target.checked);
                syncParent(row);
            }
            if (PERM_CLASSES.some(cls => target.classList.contains(cls))) {
                syncChkAll(row, PERM_CLASSES);
                syncParent(row);
            }
        }
    });

    table.querySelectorAll('tr.parent-menu').forEach(parentRow => {
        parentRow.addEventListener('click', (e) => {
            if (e.target.closest('label') || e.target.type === 'checkbox') return;
            const menuId = parentRow.dataset.menuId;
            const isOpen = parentRow.classList.toggle('is-open');
            getChildren(parentRow).forEach(child => child.classList.toggle('is-open', isOpen));
        });
    });


    // ── 헬퍼 함수 ─────────────────────────────────────────────────────────────
    function setChecks(row, classes, checked) {
        classes.forEach(cls => {
            const chk = row.querySelector(`.${cls}`);
            if (chk) chk.checked = checked;
        });
    }

    function syncChkAll(row, classes) {
        const allChecked = classes.every(cls => {
            const chk = row.querySelector(`.${cls}`);
            return chk ? chk.checked : true;
        });
        const chkAll = row.querySelector('.chk-all');
        if (chkAll) chkAll.checked = allChecked;
    }

    function getChildren(parentRow) {
        const menuId = parentRow.dataset.menuId;
        return [...table.querySelectorAll(`.child-menu[data-parent-id="${menuId}"]`)];
    }

    function syncParentChkAll(childRow) {
        const parentId = childRow.dataset.parentId;
        const parentRow = table.querySelector(`.parent-menu[data-menu-id="${parentId}"]`);
        if (!parentRow) return;

        const hasAnyChecked = getChildren(parentRow).some(child =>
            child.querySelector('.chk-all').checked
        );

        ['chk-all', ...PERM_CLASSES].forEach(cls => {
            parentRow.querySelector(`.${cls}`).checked = hasAnyChecked;
        });
    }

    function syncParentPerms(childRow) {
        const parentId = childRow.dataset.parentId;
        const parentRow = table.querySelector(`.parent-menu[data-menu-id="${parentId}"]`);
        if (!parentRow) return;

        const children = getChildren(parentRow);

        PERM_CLASSES.forEach(cls => {
            const hasAny = children.some(child => child.querySelector(`.${cls}`).checked);
            parentRow.querySelector(`.${cls}`).checked = hasAny;
        });

        const allPermsOn = PERM_CLASSES.every(cls => parentRow.querySelector(`.${cls}`).checked);
        parentRow.querySelector('.chk-all').checked = allPermsOn;
    }

    function syncParent(childRow) {
        const parentId = childRow.dataset.parentId;
        const parentRow = table.querySelector(`.parent-menu[data-menu-id="${parentId}"]`);
        if (!parentRow) return;

        const hasUnchecked = getChildren(parentRow).some(child =>
            ALL_CLASSES.some(cls => {
                const chk = child.querySelector(`.${cls}`);
                return chk && !chk.checked;
            })
        );

        if (hasUnchecked) setChecks(parentRow, ALL_CLASSES, false);
    }

// ── 권한 저장 ─────────────────────────────────────────────────────────────
    document.getElementById('savePermissionBtn').addEventListener('click', async () => {

        if (!selectedUserCode) {
            alert('선생님을 선택해주세요.');
            return;
        }

        const permissions = collectPermissions();
        console.log('저장할 권한:', permissions); // 확인용

        try {
            const response = await fetch(`/manage/teacher/${selectedUserCode}/permission`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({permissions})
            });
            const result = await response.json();

            if (response.ok) {
                alert('권한이 저장되었습니다.');
                location.reload();

            } else {
                alert(result.message || '권한 저장에 실패했습니다.');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('서버 오류가 발생했습니다.');
        }
    });

// ── 권한 복사 ─────────────────────────────────────────────────────────────
    const copyModal = document.querySelector('.copy-permission-modal');
    let selectedTargetCode = null;

    document.getElementById('copyPermissionBtn').addEventListener('click', () => {
        if (!selectedUserCode) {
            alert('선생님을 선택해주세요.');
            return;
        }

        selectedTargetCode = null;

        // sourceUser 이름 표시
        const sourceName = document.getElementById('display-name').textContent;
        document.getElementById('copySourceName').textContent = sourceName;

        // 선생님 목록 생성 (현재 선택된 유저 제외)
        const listEl = document.getElementById('copyTargetList');
        listEl.innerHTML = '';

        document.querySelectorAll('.teacher-row').forEach(row => {
            const userCode = row.dataset.userCode;
            if (userCode === selectedUserCode) return;

            const name = row.querySelector('.manage-icon span').textContent.trim();

            const item = document.createElement('div');
            item.style.cssText = 'padding:10px 14px; cursor:pointer; border-bottom:1px solid #f0f0f0;';
            item.textContent = name;
            item.dataset.userCode = userCode;

            item.addEventListener('click', () => {
                listEl.querySelectorAll('div').forEach(d => d.style.background = '');
                item.style.background = '#e8f0fe';
                selectedTargetCode = userCode;
            });

            listEl.appendChild(item);
        });

        copyModal.classList.add('is-open');
    });
    document.getElementById('copyPermissionCancelBtn').addEventListener('click', () => {
        copyModal.classList.remove('is-open');  // ← 변경
    });

// 복사하기
    document.getElementById('copyPermissionConfirmBtn').addEventListener('click', async () => {
        if (!selectedTargetCode) {
            alert('복사할 대상을 선택해주세요.');
            return;
        }

        try {
            const response = await fetch('/manage/teacher/copy-permission', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    sourceUserCode: selectedUserCode,
                    targetUserCode: selectedTargetCode
                })
            });
            const result = await response.json();

            if (response.ok) {
                alert('권한이 복사되었습니다.');
                copyModal.style.display = 'none';
                location.reload();
            } else {
                alert(result.message || '권한 복사에 실패했습니다.');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('서버 오류가 발생했습니다.');
        }
    });

// ── 권한 수집 ─────────────────────────────────────────────────────────────
    function collectPermissions() {
        const permissions = [];

        table.querySelectorAll('tbody tr').forEach(row => {
            const menuId = row.dataset.menuId;
            if (!menuId) return;

            permissions.push({
                menuId,
                canRead: row.querySelector('.chk-read').checked,
                canWrite: row.querySelector('.chk-write').checked,
                canDelete: row.querySelector('.chk-delete').checked
            });
        });

        return permissions;
    }

});