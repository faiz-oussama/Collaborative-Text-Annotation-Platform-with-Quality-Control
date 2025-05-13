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

});
