document.addEventListener('DOMContentLoaded', function () {
    const rawData = [
        {month: "2024-07", 재원: 65, 전입: 5, 전출: 2, 입회: 3, 탈퇴: 1},
        {month: "2024-08", 재원: 66, 전입: 7, 전출: 3, 입회: 6, 탈퇴: 2},
        {month: "2024-09", 재원: 62, 전입: 4, 전출: 5, 입회: 9, 탈퇴: 3},
        {month: "2024-10", 재원: 59, 전입: 3, 전출: 2, 입회: 2, 탈퇴: 4},
        {month: "2024-11", 재원: 54, 전입: 2, 전출: 3, 입회: 0, 탈퇴: 1},
        {month: "2024-12", 재원: 55, 전입: 1, 전출: 0, 입회: 0, 탈퇴: 0},
        {month: "2025-01", 재원: 60, 전입: 4, 전출: 1, 입회: 15, 탈퇴: 7},
        {month: "2025-02", 재원: 70, 전입: 5, 전출: 2, 입회: 17, 탈퇴: 5},
        {month: "2025-03", 재원: 80, 전입: 6, 전출: 3, 입회: 20, 탈퇴: 6},
        {month: "2025-04", 재원: 90, 전입: 4, 전출: 1, 입회: 13, 탈퇴: 9},
        {month: "2025-05", 재원: 85, 전입: 5, 전출: 2, 입회: 6, 탈퇴: 11},
        {month: "2025-06", 재원: 83, 전입: 3, 전출: 1, 입회: 2, 탈퇴: 6},
    ];
    const labels = rawData.map(item => item.month);
    const datasets = [
        {
            label: "재원",
            data: rawData.map(item => item.재원),
            borderColor: "#06a645",
            backgroundColor: "#06a645",
            tension: 0,
            pointRadius: 3,
        },
        {
            label: "입회",
            data: rawData.map(item => item.입회),
            borderColor: "#35c3e7",
            backgroundColor: "#35c3e7",
            tension: 0,
            pointRadius: 3,
        },
        {
            label: "탈퇴",
            data: rawData.map(item => item.탈퇴),
            borderColor: "#ee4d79",
            backgroundColor: "#ee4d79",
            tension: 0,
            pointRadius: 3,
        },
    ];
    const ctx = document.getElementById("studentChart").getContext("2d");
    new Chart(ctx, {
        type: "line",
        data: {
            labels: labels,
            datasets: datasets,
        },
        options: {
            responsive: true,
            plugins: {
                legend: {display: false},
                tooltip: {
                    backgroundColor: 'rgba(0,0,0,0.5)',
                    titleColor: '#fff',
                    bodyColor: '#fff',
                    borderRadius: 8,
                    padding: 10,
                    displayColors: true,
                    callbacks: {
                        label: function (context) {
                            return `${context.dataset.label}: ${context.parsed.y}`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {stepSize: 10}
                }
            }
        }
    });
});