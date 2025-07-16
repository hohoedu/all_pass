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
      'join',
      'joinPopup',
      'width=auto,height=auto,scrollbars=yes,resizable=yes'
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
  $('.icon-btn').on('click', function () {
    const input = $(this).siblings('input[type="date"], input[type="month"]')[0];
    if (!input || input.type === 'month') return;

    if (typeof input.showPicker === 'function') {
      input.showPicker();
    } else {
      input.click();
    }
  });
  // btn-inout modal
  // $('#btn-inout').click(function () {
  //   $('.modal').fadeIn();
  // });

  /* ====== bfclass.html ====== */
  // remarks modal
  $('.remarks').click(function () {
    $('.remarks-modal').fadeIn();
  });
  $('.class-guide').click(function () {
    $('.class-guide-modal').fadeIn();
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

  // class-guide modal
  $('.class-guide').click(function () {
    $('.calss-guide-modal').fadeIn();
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

function openModal(row) {
  const studentId = row.getAttribute("data-id");
  document.querySelector('.modal').style.display = 'block';

  fetch(`/student/${studentId}`)
    .then(res => {
      if (!res.ok) throw new Error("서버 오류");
      return res.json();
    })
    .then(data => {
      const s = data.response;
      console.log(s);
      const setElementValue = (selector, value) => {
        document.querySelectorAll(selector).forEach(el => {
          if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
            el.value = value;
          } else {
            el.innerText = value;
          }
        });
      };

      setElementValue(".s_student_no", s.studentNo || '');
      setElementValue(".s_name", s.studentName || '');
      setElementValue(".s_subjects", [s.hanClass, s.bookClass].filter(Boolean).join(', '));
      setElementValue(".s_phone", s.parentTel || '');

      const latestDate = new Date(s.entryHanDate) > new Date(s.entryBookDate)
        ? s.entryHanDate : s.entryBookDate;
      setElementValue(".s_entry", formatDateKorean(latestDate));
      setElementValue(".s_school", s.school || '');
      setElementValue(".s_grade", s.grade || '');
      setElementValue(".s_birth", formatDateKorean(s.birth));
      setElementValue(".s_address", s.address || '');
      setElementValue(".s_address_detail", s.addressDetail || '');

      updateStatusButton(s.status);
      const genderButtons = document.querySelectorAll(".s_gender");
      genderButtons.forEach(btn => btn.classList.remove("active"));
      if (s.gender === 'TRUE') {
        genderButtons[0].classList.add("active");
      } else if (s.gender === 'FALSE') {
        genderButtons[1].classList.add("active");
      } else {
        console.log(s.gender);
      }

      fetch("/student/gradeCodes")
        .then(res => res.json())
        .then(codeData => {
          const gradeSelect = document.querySelector('.grade-select');
          gradeSelect.innerHTML = "";

          if (codeData.success && Array.isArray(codeData.response)) {
            codeData.response.forEach(grade => {
              const option = document.createElement("option");
              option.value = grade.gradeNo;
              option.textContent = grade.grade;

              if (grade.grade === s.grade) {
                option.selected = true;
              }

              gradeSelect.appendChild(option);
            });

          } else {
            console.error("학년 데이터 오류");
          }
        })
    })
    .catch(err => {
      console.error(err);
    });
}

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


document.addEventListener('click', function (e) {
  const target = e.target.closest('#status-change');
  if (!target) return;

  e.preventDefault();

  const studentNo = document.querySelector('.s_student_no').innerText;
  const resonInput = document.getElementById('reason');
  const reason = resonInput?.value;

  const selectedBtn = document.querySelector('.select-btn.selected');
  const statusNo = selectedBtn?.getAttribute('data-status');

  if (!selectedBtn) {
    alert("상태를 선택해주세요.");
    return;
  }
  if (!reason) {
    alert("사유를 입력해주세요.");
    return;
  }

  const formData = new URLSearchParams();
  formData.append("studentNo", studentNo);
  formData.append("statusNo", statusNo);
  formData.append("reason", reason);

  fetch("/student/status", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded"
    },
    body: formData.toString()
  })
    .then(res => {
      if (!res.ok) throw new Error("요청 실패!");
      return res.text();
    })
    .then(data => {
      alert("상태가 성공적으로 변경되었습니다.");
      document.querySelector('.reason-input')?.classList.remove('active');
      resonInput.value = '';
      document.querySelectorAll('.select-btn').forEach(btn => btn.classList.remove('selected'));

      fetch(`/student/${studentNo}`)
        .then(res => res.json())
        .then(updated => {
          const s = updated.response;
          const row = document.querySelector(`[data-id='${studentNo}']`);
          if (!row) return;
          openModal(row);
          const tds = row.querySelectorAll("td");
          if (tds.length < 8) return;
          tds[1].textContent = s.studentName;
          tds[2].textContent = formatDateDot(s.createdAt);
          tds[3].innerHTML = getStatusText(s.status);
          tds[4].textContent = [s.hanClass, s.bookClass].filter(Boolean).join(", ");
          tds[5].textContent = s.grade;
          tds[6].textContent = s.school;
          tds[7].textHTML = s.reason == null
            ? `<img src="image/link.png" class="link" alt="링크">`
            : '';
        });
    }).catch(err => {
      console.error("요청 오류:", err);
      alert("오류가 발생했습니다.");
    });

  function getStatusText(status) {
    switch (String(status)) {
      case "1":
      case 1:
        return "재원중";
      case "2":
      case 2:
        return "입학취소";
      case "3":
      case 3:
        return "휴원";
      case "4":
      case 4:
        return "탈퇴";
      default:
        return "-";
    }
  }
});

function openJusoPopup() {
  const width = 570;
  const height = 640;
  const left = (screen.width - width) / 2;
  const top = (screen.height - height) / 2;

  window.open('/juso', 'jusoPopup', `width=${width}, height=${height}, top=${top}, left=${left}`);
}

function jusoCallBack(roadFullAddr, roadAddrPart1, addrDetail) {
  document.getElementById("roadAddrPart1").value = roadAddrPart1;
  document.getElementById("addrDetail").focus();
}

function clickInOutModal(row) {
  const studentId = row.getAttribute("data-id") || "00";
  console.log(studentId);
  const studentName = row.getAttribute("data-name");
  console.log(studentName);
  document.querySelector('.modal').style.display = 'block';
  const titleEl = document.querySelector('.inout-modal-title');
  titleEl.innerHTML = studentId === '00' ? '전체 전입/전출 내역' : titleEl.innerHTML = studentName + ' 학생 전입/전출 내역'

  fetch(`/student/inout/${studentId}`)
    .then(res => {
      if (!res.ok) throw new Error("서버 오류");
      return res.json();
    })
    .then(data => {
      const histories = data.response;
      console.log(histories);

      const tbody = document.querySelector(".inout-modal-body");
      tbody.innerHTML = '';
      if (!histories || histories.length === 0) {
        const tr = document.createElement("tr");
        tr.innerHTML = `<td colspan="7" style="text-align:center;">전입/전출 내역이 없습니다.</td>`;
        tbody.appendChild(tr);
        return;
      }

      titleEl.innerHTML = studentId === '00'
        ? '전체 전입/전출 내역'
        : `${studentName} 학생 전입/전출 내역`;

      histories.forEach((item, index) => {
        const tr = document.createElement("tr");
        tr.innerHTML = tr.innerHTML = `
          <td>${index + 1}</td>
          <td>${formatDateKorean(item.moveAt)}</td>
          <td>${item.studentName}</td>
          <td>${item.className}</td>
          <td>${item.fromTeacher}</td>
          <td>${item.toTeacher}</td>
          <td>${item.transferReason}</td>
        `;
        tbody.appendChild(tr);
      });
    })
    .catch(err => {
      console.error(err);
    });
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
