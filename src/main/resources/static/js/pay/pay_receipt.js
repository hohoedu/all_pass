document.addEventListener('DOMContentLoaded', function () {

    /* ── 월 표시 ── */
    var now = new Date();
    var yr = now.getFullYear();
    var mo = String(now.getMonth() + 1).padStart(2, '0');
    document.getElementById('currentMonth').textContent = yr + '년 ' + mo + '월';
    document.getElementById('monthPickerInput').value = yr + '-' + mo;

    /* ── 월 피커 ── */
    document.getElementById('openMonthPicker').addEventListener('click', function () {
        var picker = document.getElementById('monthPickerInput');
        picker.showPicker ? picker.showPicker() : picker.click();
    });

    document.getElementById('monthPickerInput').addEventListener('change', function () {
        var parts = this.value.split('-');
        document.getElementById('currentMonth').textContent = parts[0] + '년 ' + parts[1] + '월';
        this.style.pointerEvents = 'none';
        this.style.opacity = '0';
    });

    /* ── 전체 체크박스 ── */
    document.getElementById('check-all').addEventListener('change', function () {
        document.querySelectorAll('#receipt-list-body input[type="checkbox"]')
            .forEach(function (cb) {
                cb.checked = this.checked;
            }, this);
    });

    /* ── 검색 ── */
    document.getElementById('btn-search').addEventListener('click', function () {
        var keyword = document.getElementById('search-name').value.trim().toLowerCase();
        document.querySelectorAll('.receipt-row').forEach(function (row) {
            var name = row.querySelectorAll('td')[2].textContent.toLowerCase();
            row.style.display = (!keyword || name.includes(keyword)) ? '' : 'none';
        });
    });

    document.getElementById('search-name').addEventListener('keydown', function (e) {
        if (e.key === 'Enter') document.getElementById('btn-search').click();
    });

    /* ── 미리보기 버튼 → fetch POST ── */
    document.getElementById('btn-preview').addEventListener('click', function () {
        var selected = Array.from(
            document.querySelectorAll('#receipt-list-body input[type="checkbox"]:checked')
        )
            .map(function (cb) {
                return cb.value;
            })
            .filter(function (v) {
                return !!v;
            });

        if (selected.length === 0) {
            alert('선택된 항목이 없습니다.');
            return;
        }

        fetch('/pay/receipt/print-data', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({rowKeys: selected})
        })
            .then(function (res) {
                if (!res.ok) throw new Error('서버 오류: ' + res.status);
                return res.json();
            })
            .then(function (data) {
                console.log('수신 완료', data);  // 2단계에서 렌더링으로 교체
            })
            .catch(function (err) {
                console.error(err);
                alert('조회 중 오류가 발생했습니다.');
            });
    });

});