// 상담 기록 추가 모달 띄우기
document.addEventListener('DOMContentLoaded', () => {
    const consultAddBtn = document.querySelector('.consult-add');
    const modal = document.querySelector('.consult-modal');
    const closeBtn = modal.querySelector('.btn-close');

    consultAddBtn.addEventListener('click', () => {
        modal.style.display = 'block';
    });

    closeBtn.addEventListener('click', (e) => {
        e.preventDefault();
        modal.style.display = 'none';
    });

    window.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.style.display = 'none';
        }
    });
});

// 상담 기록 추가
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.consult-modal .save-btn').forEach(button => {
        button.addEventListener('click', () => {
            const modal = button.closest('.consult-modal');
            const data = {
                studentName: modal.querySelector('[name="studentName"]')?.value.trim(),
                consultDate: modal.querySelector('[name="consultDate"]')?.value,
                school: modal.querySelector('[name="school"]')?.value.trim(),
                gradeNo: modal.querySelector('[name="gradeNo"]')?.value,
                phone: modal.querySelector('[name="parentPhone"]').value.replace(/-/g, ''),
                inflowRouteNo: modal.querySelector('[name="inflowRouteNo"]')?.value,
                content: modal.querySelector('[name="content"]')?.value.trim()
            };
            console.log(data);

            fetch('/consult/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
                .then(res => {
                    if (res.ok) {
                        alert('저장 완료');
                        location.reload();
                    } else {
                        alert('저장 실패');
                    }
                })
                .catch(err => {
                    console.error(err);
                    alert('에러 발생');
                });
        });
    });
});

// 상담 기록 날짜 선택
document.addEventListener('DOMContentLoaded', () => {
    const modal = document.querySelector('.consult-modal');
    const dateInput = modal.querySelector('input[name="consultDate"]');
    const display = modal.querySelector('.day-display');
    const calendarBtn = modal.querySelector('.birth-btn');

    calendarBtn.addEventListener('click', () => {
        dateInput.showPicker?.();
        dateInput.click();
    });

    dateInput.addEventListener('change', () => {
        const selected = dateInput.value;
        if (selected) {
            const [year, month, day] = selected.split('-');
            display.textContent = `${year}년 ${month}월 ${day}일`;
        } else {
            display.textContent = '';
        }
    });
});

// 상담기록 전화번호 포맷 변경
document.addEventListener('DOMContentLoaded', () => {
    const modal = document.querySelector('.consult-modal');
    const phoneInput = modal.querySelector('input[name="parentPhone"]');

    phoneInput.addEventListener('input', () => {
        let raw = phoneInput.value.replace(/\D/g, '');

        if (raw.length > 11) raw = raw.slice(0, 11);

        let formatted = raw;

        if (raw.startsWith('010') && raw.length === 11) {
            formatted = raw.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
        }

        phoneInput.value = formatted;
    });
});

// 상담기록 삭제
document.addEventListener('DOMContentLoaded', () => {
    console.log('null');
});