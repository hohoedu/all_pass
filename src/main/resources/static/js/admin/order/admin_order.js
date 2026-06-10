document.addEventListener('DOMContentLoaded', () => {
    const printBtn = document.getElementById('print-invoice');

    printBtn.addEventListener("click", () => {
        alert("클릭!");
    })


    document.querySelectorAll('select[name^="orderDeadline_"]').forEach(select => {
        select.addEventListener('change', async function () {
            const centerCode = this.name.replace('orderDeadline_', '');
            const deadline = this.value;
            const centerName = this.closest('tr').querySelectorAll('td')[1].textContent.trim();

            try {
                const res = await fetch('/admin/order/deadline', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({centerCode, deadline})
                });
                const data = await res.json();
                if (data.success) {
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