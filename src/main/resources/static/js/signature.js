document.addEventListener('DOMContentLoaded', () => {
  const canvas = document.getElementById('signature-pad');
  const ctx = canvas.getContext('2d');
  const clearButton = document.getElementById('clear-button');

  // 기본 설정
  ctx.strokeStyle = '#000';
  ctx.lineWidth = 2;
  let isDrawing = false;

  // 그리기 시작
  canvas.addEventListener('mousedown', (e) => {
    e.preventDefault();
    isDrawing = true;
    ctx.beginPath();
    ctx.moveTo(e.offsetX, e.offsetY);
  });

  // 그리기 중
  canvas.addEventListener('mousemove', (e) => {
    e.preventDefault();
    if (isDrawing) {
      ctx.lineTo(e.offsetX, e.offsetY);
      ctx.stroke();
    }
  });

  // 그리기 종료
  canvas.addEventListener('mouseup', () => {
    isDrawing = false;
  });

  canvas.addEventListener('touchstart', (e) => {
    e.preventDefault(); // 스크롤 방지
    isDrawing = true;
    const touch = e.touches[0];
    const rect = canvas.getBoundingClientRect();
    ctx.beginPath();
    ctx.lineTo(touch.clientX - rect.left, touch.clientY - rect.top);
  });

  canvas.addEventListener('touchmove', (e) => {
    e.preventDefault(); // 스크롤 방지
    if (isDrawing) {
      const touch = e.touches[0];
      const rect = canvas.getBoundingClientRect();
      ctx.lineTo(touch.clientX - rect.left, touch.clientY - rect.top);
      ctx.stroke();
    }
  });

  canvas.addEventListener('touchend', () => {
    isDrawing = false;
  });

  // 서명창 지우기
  clearButton.addEventListener('click', () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
  });
});
