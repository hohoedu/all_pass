// 비밀번호 변경 버튼 이벤트
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('changePasswordBtn').addEventListener('click', async function () {
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        // 유효성 검사
        if (!newPassword || !confirmPassword) {
            alert('비밀번호를 입력해주세요.');
            return;
        }

        if (newPassword !== confirmPassword) {
            alert('비밀번호가 일치하지 않습니다.');
            return;
        }

        // 서버로 전송
        try {
            const response = await fetch('/user/password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    newPassword: newPassword
                })
            });

            const data = await response.json();

            if (response.ok) {
                alert('비밀번호가 변경되었습니다.');
                // 입력 필드 초기화
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
});