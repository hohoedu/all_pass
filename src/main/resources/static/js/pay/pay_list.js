document.addEventListener("DOMContentLoaded", () => {

    const btnAddPayment = document.querySelector('#btn-add-payment');
    if (btnAddPayment) {
        btnAddPayment.addEventListener('click', () => {
            console.log('버튼 클릭!!')
            openModal();
        });
    }
    function openModal() {
        document.querySelectorAll('.modal').forEach(modal => {
            modal.style.display = 'none';
        });

        const targetModal = document.querySelector(`.add-payment-modal`);
        console.log("targetModal" + targetModal);
        if (targetModal) {
            targetModal.style.display = 'block';
        }
    }




});