// Sidebar load
// const menu = document.querySelectorAll(".menu-link");

// for (let i = 0; i < menu.length; i++) {
//   menu[i].addEventListener("click", function () {
//     menu.forEach(link => link.classList.remove("active"));
//     this.classList.add("active");
//   });
// }

$(document).ready(function () {
    /* ====== conslut.html ====== */
    // modal
    $('.counsel-button, #student-tbody tr').click(function () {
        $('.student-modal').fadeIn()
    });
    $('.btn-close').click(function () {
        $('.modal').fadeOut();
    });
    $('.modal').click(function (event) {
        if ($(event.target).is('.modal')) {
            $('.modal').fadeOut();
        }
    });

    /* ====== manage-teacher.html ====== */
    // delete modal
    $('.check-delete').click(function () {
        $('.delete-modal').fadeIn();
    });
    // add manager modal
    $('.add-manager').click(function () {
        $('.manager-modal').fadeIn();
    });

    /* ====== book-result.html ====== */
    // book-result modal
    $('.book-result-btn').click(function () {
        $('.book-result-modal').fadeIn();
    });

    /* ====== manage-sms.html ====== */
    // word-modify modal
    $('#word-modify').click(function () {
        $('.sms-modal').fadeIn();
    });
    // point-charge modal
    $('#check-point').click(function () {
        $('.point-modal').fadeIn();
    });

    /* ====== sidebar.html ====== */
    // side bar menu toggle
    // $(document).on('click', '.sidebar-item .menu-toggle', function() {
    //   $(this).next('.submenu').stop().slideToggle(300);
    //   $(this).toggleClass('active');
    // });
    // $('.sidebar-item .submenu').click(function(){
    //   $(this).stop().slideUp(300);
    // });
    $(document).ready(function () {
        $(".menu-toggle").click(function () {
            // 왼쪽 메뉴 상태 초기화
            $(".menu-toggle").removeClass("active");
            $(".sidebar-right .submenu").removeClass("active");

            // 현재 메뉴 활성화
            $(this).addClass("active");
            const target = $(this).data("target");

            // 오른쪽 사이드바 열기 + 해당 submenu 표시
            $(".sidebar-right").removeClass("collapsed");
            $('.sidebar-right .submenu[data-menu="' + target + '"]').addClass("active");
        });

        $(".sidebar-close").click(function () {
            $(".sidebar-right").toggleClass("collapsed");
        });
    });

    // 모든 달력
    document.addEventListener("DOMContentLoaded", () => {
        document.querySelectorAll('.calendar-open').forEach(btn => {
            btn.addEventListener('click', function () {
                const input = this.previousElementSibling;
                if (input && input.showPicker) {
                    input.showPicker();
                }
            });
        });
    });


    /* ====== student-main.html ====== */
    // student-main tab
    const btns = document.querySelectorAll('.info-tab-btn a');
    const materials = document.querySelectorAll('.tab');

    btns.forEach(button => {
        button.addEventListener('click', () => {
            btns.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');
            materials.forEach(material => material.classList.remove('active'));
            const tabs = button.getAttribute('data-tab');
            document.getElementById(tabs).classList.add('active');
        });
    });

    $('.btn-gender').click(function () {
        $(this).closest('td').find('.btn-gender').removeClass('active');
        $(this).addClass('active');
    });

    $('.status-buttons .btn-status').click(function () {
        $('.reason-input').addClass('active');
    })

    // join
    $('.new-regist').click(function () {
        window.open(
            '/student/join',
            'joinPopup',
            'width=auto,height=auto,scrollbars=yes,resizable=yes'
        );
    });
    // 기관 검색
    $('.search-school').click(function () {
        window.open(
            '/school',
            'schoolPopup',
            'width=750px,height=500px,scrollbars=yes,resizable=yes'
        );
    });
    // caleneder
    // 날짜 선택 시 div 안에 반영
    $('#birth-date').on('change', function () {
        const val = this.value; // yyyy-mm-dd
        if (val) {
            const formatted = formatDateKorean(val);
            $('.birth-display').text(formatted);
        }
    });
    const initVal = $('#birth-date').val();
    if (initVal) {
        $('.birth-display').text(formatDateKorean(initVal));
    }


    /* ====== student-inout.html ====== */

    // btn-inout modal
    // $('#btn-inout').click(function () {
    //   $('.modal').fadeIn();
    // });

    /* ====== bfclass.html ====== */
    // remarks modal
    $('.remarks').click(function () {
        $('.remarks-modal').fadeIn();
    });
    // 시간 선택 시 표시되는 텍스트 업데이트
    $('.timepicker').on('input', function () {
        const timeValue = $(this).val(); // ex: "14:30"
        $(this).siblings('.display-time').text(timeValue || '--:--');
    });

    $('.datepicker').on('change', function () {
        const dateValue = $(this).val();
        $(this).closest('div').prev('.display-date').text(formatDateKorean(dateValue) || '');
    });

    const today = new Date().toISOString().split('T')[0];
    $('.datepicker').each(function () {
        $(this).val(today).trigger('change');
    });

    // class-timetable tab
    const tabButtons = document.querySelectorAll('.class-before-after a');
    const tabContents = document.querySelectorAll('.ctab');

    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            tabButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');
            tabContents.forEach(content => content.classList.remove('active'));
            const tabId = button.getAttribute('data-tab');
            document.getElementById(tabId).classList.add('active');
        });
    });
    // 주차 버튼
    $('.week-btn').click(function () {
        $('.week-btn').removeClass('active');
        $(this).addClass('active');
    });
    // 요일 버튼
    $(document).ready(function () {
        var today = new Date().getDay(); // 0=일 ~ 6=토

        // 요일 맵핑
        var weekMap = ["sun", "mon", "tue", "wed", "thu", "fri", "sat"];
        // 일요일이면 월요일로 강제 세팅
        var targetDay = (today === 0) ? "mon" : weekMap[today];

        // 해당 버튼에 active 추가
        $('.day-btn[data-week="' + targetDay + '"]').addClass('active');

        // 클릭 이벤트 처리
        $('.day-btn').click(function () {
            $('.day-btn').removeClass('active');
            $(this).addClass('active');
        });
    });
    // 클래스 버튼
    $('.class-btn').click(function () {
        $('.class-btn').removeClass('active');
        $(this).addClass('active');
    });
    // 컨설트 버튼
    $('.counsel-type button').click(function () {
        $('.counsel-type button').removeClass('active');
        $(this).addClass('active');
    });

    /* ====== class-timetable.html ====== */
    // class-timetable tab
    const buttons = document.querySelectorAll('.time-tab-btn');
    const contents = document.querySelectorAll('.time-tab-content');

    buttons.forEach(button => {
        button.addEventListener('click', () => {
            buttons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');
            contents.forEach(content => content.classList.remove('active'));
            const tab = button.getAttribute('data-tab');
            document.getElementById(tab).classList.add('active');
        });
    });

    // timetable information change -> schedule-btn modal
    $('.schedule-btn').click(function () {
        $('.time-change-modal').fadeIn();
    });

    // notice.html
    $('.notice-nums a').click(function () {
        $('.notice-nums a').removeClass('active');
        $(this).addClass('active');
    });
});


/* ====== order.html ====== */
// custom-spinner
document.addEventListener('DOMContentLoaded', () => {
    // All spinner
    document.querySelectorAll('.custom-spinner').forEach((spinner) => {
        const inputField = spinner.querySelector('input[type="number"]');
        const incrementButton = spinner.querySelector('.spinner-buttons button:first-child');
        const decrementButton = spinner.querySelector('.spinner-buttons button:last-child');

        // Ensure that all elements exist
        if (inputField && incrementButton && decrementButton) {
            // Increase button event
            incrementButton.addEventListener('click', () => {
                inputField.stepUp();
            });
            // Decease button event
            decrementButton.addEventListener('click', () => {
                inputField.stepDown();
            });
        } else {
            console.error('Spinner 구성 요소를 찾을 수 없습니다.', spinner);
        }
    });
});


function formatDateKorean(iso) {
    if (!iso) return '';
    const d = new Date(iso);
    return `${d.getFullYear()}년 ${d.getMonth() + 1}월 ${d.getDate()}일`;
}

function formatDateDot(iso) {
    if (!iso) return '';
    const d = new Date(iso);
    return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`;
}

function updateStatusButton(statusCode) {
    const statusStr = String(statusCode);

    document.querySelectorAll('.status-buttons').forEach(group => {
        const mode = group.getAttribute('data-visibility');

        group.querySelectorAll('.s_status').forEach(btn => {
            const btnStatus = btn.getAttribute('data-status');

            if (mode === 'only-current') {

                btn.style.display = (btnStatus === statusStr) ? 'inline-block' : 'none';
            } else if (mode === 'except-current') {

                btn.style.display = (btnStatus === statusStr) ? 'none' : 'inline-block';
            }
        });
    });
}

document.addEventListener("click", function (e) {
    const btn = e.target.closest(".select-btn");
    if (!btn) return;

    document.querySelectorAll(".select-btn").forEach(b => b.classList.remove("selected"));
    btn.classList.add("selected");
});

function openJusoPopup() {
    const width = 570;
    const height = 640;
    const left = (screen.width - width) / 2;
    const top = (screen.height - height) / 2;
    console.log("[팝업열기] 주소 검색 팝업을 엽니다.");
    window.open('/juso', 'jusoPopup', `width=${width}, height=${height}, top=${top}, left=${left}`);
}


function jusoCallBack(roadFullAddr, roadAddrPart1, addrDetail) {
    console.log("[CallBack] 주소 검색 완료. 값 전달 받음");
    console.log(" - roadFullAddr:", roadFullAddr);
    console.log(" - roadAddrPart1:", roadAddrPart1);
    console.log(" - addrDetail:", addrDetail);
    const addrInput = document.getElementById('address-input');
    if (addrInput) {
        addrInput.value = roadAddrPart1;
    }

    const detailInput = document.getElementById('address-detail-input');
    if (detailInput) {
        detailInput.value = addrDetail || '';
        detailInput.focus();
    }
}


function showAlert(options) {
    return Swal.fire({
        icon: options.icon || 'info',
        title: options.title || '',
        text: options.text || '',
        showCancelButton: options.showCancelButton || false,
        confirmButtonText: options.confirmButtonText || '확인',
        cancelButtonText: options.cancelButtonText || '취소',
        allowOutsideClick: options.allowOutsideClick ?? true,
        allowEscapeKey: options.allowEscapeKey ?? true,
        customClass: {
            popup: 'rounded-alert',
            confirmButton: 'rounded-alert-button',
            cancelButton: 'rounded-alert-button'
        }
    });
}


// 정렬 함수
function addHeadSort(headId, tbodyId, opts = {}) {
    const head = document.getElementById(headId);
    const tbody = document.getElementById(tbodyId);
    if (!head || !tbody) return;

    const icons = head.querySelectorAll('.svg-sort');

    const setIconByDirection = (imgEl, direction) => {
        const toFile =
            direction === 'asc' ? 'sort_checked_up.svg' :
                direction === 'desc' ? 'sort_checked_down.svg' : 'sort.svg';

        const current = imgEl.getAttribute('src') || '';
        imgEl.setAttribute(
            'src',
            current.replace(/(sort_checked_up\.svg|sort_checked_down\.svg|sort_checked\.svg|sort\.svg)$/i, toFile)
        );
    };

    const updateIcons = (clickedIcon, direction) => {
        icons.forEach(i => setIconByDirection(i, i === clickedIcon ? direction : 'default'));
    };

    // 셀 값 파싱(날짜/숫자/문자)
    const parseCell = (text) => {
        const v = (text || '').trim();


        if (/^\d{4}-\d{2}-\d{2}$/.test(v)) {
            const d = new Date(v);
            if (!isNaN(d)) return {type: 'date', value: d.getTime()};
        }

        const n = Number(v.replace(/[^0-9.-]/g, ''));
        if (!Number.isNaN(n) && v.match(/[0-9]/)) return {type: 'number', value: n};

        return {type: 'string', value: v};
    };

    const compareBy = (aRow, bRow, colIndex, asc) => {
        const A = aRow.children[colIndex] ? aRow.children[colIndex].innerText : '';
        const B = bRow.children[colIndex] ? bRow.children[colIndex].innerText : '';
        const a = parseCell(A);
        const b = parseCell(B);

        if (a.type === 'empty' && b.type !== 'empty') return 1;
        if (b.type === 'empty' && a.type !== 'empty') return -1;
        if (a.type === 'empty' && b.type === 'empty') return 0;

        if (a.type === b.type) {
            if (a.value < b.value) return asc ? -1 : 1;
            if (a.value > b.value) return asc ? 1 : -1;
            return 0;
        }

        const rank = {date: 3, number: 2, string: 1, empty: 0};
        if (rank[a.type] !== rank[b.type]) {
            return asc ? (rank[a.type] - rank[b.type]) : (rank[b.type] - rank[a.type]);
        }
        return 0;
    };

    const sortByColumn = (icon, colIndex, direction) => {

        const rows = Array.from(tbody.querySelectorAll('tr'));
        rows.sort((r1, r2) => compareBy(r1, r2, colIndex, direction === 'asc'));
        rows.forEach(r => tbody.appendChild(r));

        icons.forEach(i => i.classList.remove('asc', 'desc'));
        icon.classList.add(direction);
        updateIcons(icon, direction);
    };

    icons.forEach((icon) => {
        icon.style.cursor = 'pointer';
        icon.addEventListener('click', (e) => {
            const th = e.target.closest('th');
            if (!th) return;

            const colIndex = th.cellIndex;

            const nextDirection = icon.classList.contains('desc') ? 'asc' : 'desc';
            sortByColumn(icon, colIndex, nextDirection);
        });
    });

    if (typeof opts.initialIndex === 'number') {
        const ths = head.querySelectorAll('th');
        const targetTh = ths[opts.initialIndex];
        if (targetTh) {
            const icon = targetTh.querySelector('.svg-sort');
            if (icon) {
                const dir = (opts.initialDir === 'asc' || opts.initialDir === 'desc') ? opts.initialDir : 'desc';
                sortByColumn(icon, opts.initialIndex, dir);
            }
        }
    }
}