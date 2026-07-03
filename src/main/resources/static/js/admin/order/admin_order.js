document.addEventListener('DOMContentLoaded', () => {
    const printBtn = document.getElementById('print-invoice');

    printBtn.addEventListener("click", () => {
        alert("클릭!");
    })


    document.querySelectorAll('.deadline-save-btn').forEach(btn => {
        btn.addEventListener('click', async function () {
            const select = this.previousElementSibling;
            const centerCode = select.name.replace('orderDeadline_', '');
            const deadline = select.value;
            const original = select.dataset.original || '';
            const centerName = this.closest('tr').querySelectorAll('td')[1].textContent.trim();

            if (!deadline) {
                alert('마감일을 선택해주세요.');
                return;
            }

            if (deadline === original) {
                alert('변경된 내용이 없습니다.');
                return;
            }

            const beforeText = original ? `${original}일` : '없음';
            const confirmed = confirm(
                `${centerName}의 교재 주문 마감일을 변경하시겠습니까?\n\n변경 전: ${beforeText}\n변경 후: ${deadline}일`
            );
            if (!confirmed) {
                return;
            }

            try {
                const res = await fetch('/admin/order/deadline', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({centerCode, deadline})
                });
                const data = await res.json();
                if (data.success) {
                    select.dataset.original = deadline;
                    alert(`${centerName}의 교재 주문 마감일이 ${deadline}일로 등록되었습니다. `);
                } else {
                    alert('저장 실패');
                }
            } catch (err) {
                console.log('마감일 저장 실패: ', err);
            }
        });
    });
});