document.addEventListener("DOMContentLoaded", () => {
    const phoneInput = document.getElementById("new-member-phone");
    const sendBtn = document.getElementById("send-btn");

    /* =================== *
     *   전화번호 정규화     *
     * =================== */
    function normalizePhone(input) {
        let phone = input.replace(/-/g, "").trim();

        if (!/^[0-9]+$/.test(phone)) {
            alert("전화번호는 숫자만 입력해 주세요.");
            return null;
        }

        if (phone.startsWith("010") && phone.length === 11) {
            return phone;
        }

        if (phone.length === 8) {
            return "010" + phone;
        }

        alert("올바른 전화번호를 입력해 주세요. (010으로 시작하거나 8자리 숫자)");
        return null;
    }

    /* =================== *
     *   학생 정보 모달      *
     * =================== */

    // 모달 HTML 동적 생성
    function createStudentInfoModal() {
        const existing = document.getElementById('student-info-modal');
        if (existing) return;

        const modal = document.createElement('div');
        modal.id = 'student-info-modal';
        modal.style.cssText = `
            display:none; position:fixed; inset:0;
            background:rgba(0,0,0,0.5); z-index:9999;
            justify-content:center; align-items:center;
        `;

        modal.innerHTML = `
            <div style="background:#fff; border-radius:12px; padding:32px; width:360px; box-shadow:0 8px 32px rgba(0,0,0,0.15);">
                <h3 style="margin:0 0 20px; font-size:16px; font-weight:600;">학생 정보 입력</h3>

                <div style="margin-bottom:14px;">
                    <label style="display:block; font-size:13px; font-weight:500; margin-bottom:6px;">이름 <span style="color:red">*</span></label>
                    <input id="modal-name" type="text" placeholder="이름을 입력하세요"
                        style="width:100%; padding:9px 12px; border:1px solid #ddd; border-radius:8px; font-size:14px; box-sizing:border-box;">
                </div>

                <div style="margin-bottom:14px;">
                    <label style="display:block; font-size:13px; font-weight:500; margin-bottom:6px;">학년 <span style="color:red">*</span></label>
                    <select id="modal-grade" style="width:100%; padding:9px 12px; border:1px solid #ddd; border-radius:8px; font-size:14px; box-sizing:border-box;">
                        <option value="">학년 선택</option>
                        
                        <option value="05">5세</option>
                        <option value="06">6세</option>
                        <option value="07">7세</option>
                        <option value="11">초1</option>
                        <option value="12">초2</option>
                        <option value="13">초3</option>
                        <option value="14">초4</option>
                        <option value="15">초5</option>
                        <option value="16">초6</option>
                        <option value="21">중1</option>
                        <option value="22">중2</option>
                        <option value="23">중3</option>
              
                    </select>
                </div>

                <div style="margin-bottom:20px;">
                    <label style="display:block; font-size:13px; font-weight:500; margin-bottom:10px;">수강 과목 <span style="color:red">*</span></label>
                    <div style="display:flex; gap:16px; flex-wrap:wrap;">
                        <label style="display:flex; align-items:center; gap:6px; cursor:pointer; font-size:14px;">
                            <input type="checkbox" name="modal-subject" value="han"> 한스쿨
                        </label>
                        <label style="display:flex; align-items:center; gap:6px; cursor:pointer; font-size:14px;">
                            <input type="checkbox" name="modal-subject" value="book"> 북스쿨
                        </label> 
                        <label style="display:flex; align-items:center; gap:6px; cursor:pointer; font-size:14px;">
                            <input type="checkbox" name="modal-subject" value="hoho"> 호호스쿨
                        </label>
                    </div>
                </div>

                <div style="display:flex; gap:10px; justify-content:flex-end;">
                    <button id="modal-cancel" style="padding:9px 20px; border:1px solid #ddd; background:#fff; border-radius:8px; cursor:pointer; font-size:14px;">취소</button>
                    <button id="modal-confirm" style="padding:9px 20px; background:#4F7EF7; color:#fff; border:none; border-radius:8px; cursor:pointer; font-size:14px; font-weight:500;">발송</button>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        document.getElementById('modal-cancel').addEventListener('click', closeStudentInfoModal);
        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeStudentInfoModal();
        });
    }

    function openStudentInfoModal() {
        const modal = document.getElementById('student-info-modal');
        // 초기화
        document.getElementById('modal-name').value = '';
        document.getElementById('modal-grade').value = '';
        document.querySelectorAll('input[name="modal-subject"]').forEach(r => r.checked = false);
        modal.style.display = 'flex';
    }

    function closeStudentInfoModal() {
        document.getElementById('student-info-modal').style.display = 'none';
    }

    function getModalValues() {
        const name    = document.getElementById('modal-name').value.trim();
        const grade   = document.getElementById('modal-grade').value;
        const subject = document.querySelector('input[name="modal-subject"]:checked')?.value;

        return {
            name,
            gradeKey: grade,
            subHoho: subject === 'hoho',
            subHan:  subject === 'han',
            subBook: subject === 'book',
            subject
        };
    }

    /* =================== *
     *   발송 로직         *
     * =================== */
    async function sendJoinLink(phone, modalValues) {
        const response = await fetch("/popbill/send-join", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                source:   'MAIN',
                phone,
                name:     modalValues.name,
                gradeKey: modalValues.gradeKey,
                subHoho:  modalValues.subHoho,
                subHan:   modalValues.subHan,
                subBook:  modalValues.subBook
            })
        });

        return await response.json();
    }

    /* =================== *
     *   이벤트 바인딩       *
     * =================== */
    createStudentInfoModal();

    sendBtn.addEventListener("click", () => {
        const rawPhone = phoneInput.value;
        const phone = normalizePhone(rawPhone);
        if (!phone) return;

        if (!/^[0-9]{10,11}$/.test(phone)) {
            alert("올바른 전화번호를 입력해 주세요. ('-' 없이 10~11자리)");
            return;
        }

        openStudentInfoModal();

        // 기존 confirm 이벤트 중복 방지
        const confirmBtn = document.getElementById('modal-confirm');
        const newConfirmBtn = confirmBtn.cloneNode(true);
        confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);

        newConfirmBtn.addEventListener('click', async () => {
            const values = getModalValues();

            if (!values.name) {
                alert('이름을 입력해주세요.');
                return;
            }
            if (!values.gradeKey) {
                alert('학년을 선택해주세요.');
                return;
            }
            if (!values.subject) {
                alert('수강 과목을 선택해주세요.');
                return;
            }

            closeStudentInfoModal();

            try {
                const result = await sendJoinLink(phone, values);

                if (result.success) {
                    alert("알림톡이 발송되었습니다.");
                    phoneInput.value = '';
                } else {
                    alert("발송 실패: " + (result.message || ""));
                }
            } catch (e) {
                console.error(e);
                alert("알림톡 발송 중 오류가 발생했습니다.");
            }
        });
    });
});