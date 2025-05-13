// Document ready handler
document.addEventListener('DOMContentLoaded', function() {
    // Initialize Kappa Chart
    const kappaChartCanvas = document.getElementById('kappaChart');
    if (kappaChartCanvas) {
        const kappaCtx = kappaChartCanvas.getContext('2d');
        new Chart(kappaCtx, {
            type: 'bar',
            data: {
                labels: ['Cohen\'s Kappa', 'Fleiss\' Kappa', 'Percent Agreement'],
                datasets: [{
                    label: 'Agreement Score',
                    data: [
                        /*[(${#numbers.formatDecimal(cohenKappa, 1, 'POINT', 2, 'POINT')}]) || 0.82*/,
                        /*[(${#numbers.formatDecimal(fleissKappa, 1, 'POINT', 2, 'POINT')}]) || 0.75*/,
                        /*[(${#numbers.formatDecimal(percentAgreement, 1, 'POINT', 2, 'POINT')}]) || 0.88*/
                    ],
                    backgroundColor: [
                        'rgba(79, 70, 229, 0.7)',
                        'rgba(79, 70, 229, 0.5)',
                        'rgba(79, 70, 229, 0.3)'
                    ],
                    borderColor: [
                        'rgba(79, 70, 229, 1)',
                        'rgba(79, 70, 229, 1)',
                        'rgba(79, 70, 229, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 1
                    }
                }
            }
        });
    }

    // Initialize Performance Trend Chart with ApexCharts
    if (typeof ApexCharts !== 'undefined') {
        const performanceOptions = {
            series: [{
                name: 'Accuracy',
                data: [88, 89, 87, 90, 91, 92, 92.4]
            }, {
                name: 'F1 Score',
                data: [0.82, 0.83, 0.81, 0.85, 0.87, 0.88, 0.89]
            }],
            chart: {
                height: 280,
                type: 'line',
                toolbar: { show: true },
                zoom: { enabled: true }
            },
            colors: ['#4F46E5', '#EC4899'],
            dataLabels: { enabled: false },
            stroke: { curve: 'smooth', width: 3 },
            grid: {
                borderColor: '#e0e0e0',
                row: { colors: ['#f3f3f3', 'transparent'], opacity: 0.5 }
            },
            markers: { size: 5 },
            xaxis: { categories: ['Apr 10', 'Apr 15', 'Apr 20', 'Apr 25', 'May 1', 'May 5', 'May 12'] },
            yaxis: [
                { title: { text: 'Accuracy (%)' }, min: 80, max: 100 },
                { opposite: true, title: { text: 'F1 Score' }, min: 0.7, max: 1 }
            ],
            legend: { position: 'top', horizontalAlign: 'right' }
        };

        const performanceChart = new ApexCharts(document.querySelector("#performanceTrendChart"), performanceOptions);
        performanceChart.render();

        // Initialize Confusion Matrix
        const confusionMatrixOptions = {
            series: [{
                name: 'Confusion Matrix',
                data: [
                    { x: 'True Positive', y: 85 },
                    { x: 'False Positive', y: 7 },
                    { x: 'False Negative', y: 8 },
                    { x: 'True Negative', y: 90 }
                ]
            }],
            chart: { height: 240, type: 'heatmap' },
            dataLabels: { enabled: true, style: { colors: ['#fff'] } },
            colors: ['#4F46E5'],
            title: { text: 'Confusion Matrix', align: 'center' },
            plotOptions: {
                heatmap: {
                    shadeIntensity: 0.5,
                    radius: 0,
                    colorScale: {
                        ranges: [
                            { from: 0, to: 20, color: '#BFDBFE' },
                            { from: 21, to: 50, color: '#93C5FD' },
                            { from: 51, to: 80, color: '#60A5FA' },
                            { from: 81, to: 100, color: '#3B82F6' }
                        ]
                    }
                }
            }
        };

        const confusionMatrix = new ApexCharts(document.querySelector("#confusionMatrixChart"), confusionMatrixOptions);
        confusionMatrix.render();
    }
});
