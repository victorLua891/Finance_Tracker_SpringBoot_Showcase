document.addEventListener('DOMContentLoaded', function() {
        // Expenditure Chart
        var expenditureCtx = document.getElementById('expenditureChart').getContext('2d');
        var thisMonthExpenditure = /*[[${thisMonthExpenditure}]]*/ 0; // Get data from Thymeleaf
        var thisMonthBudgetPlanForExpenditure = /*[[${thisMonthBudgetPlanForExpenditure}]]*/ 100; // Get budget relevant to expenditure
        var expenditureChart = new Chart(expenditureCtx, {
            type: 'doughnut',
            data: {
                labels: ['Spent', 'Remaining'],
                datasets: [{
                    data: [thisMonthExpenditure, Math.max(0, thisMonthBudgetPlanForExpenditure - thisMonthExpenditure)],
                    backgroundColor: ['#FF6384', '#36A2EB'],
                    hoverBackgroundColor: ['#FF6384', '#36A2EB'],
                    borderWidth: 1,
                    borderColor: '#ccc'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutoutPercentage: 70, // Adjust for donut hole size
                legend: {
                    display: false
                },
                tooltips: {
                    callbacks: {
                        label: function(tooltipItem, data) {
                            var dataset = data.datasets[tooltipItem.datasetIndex];
                            var total = dataset.data.reduce(function(previousValue, currentValue, currentIndex, array) {
                                return previousValue + currentValue;
                            });
                            var currentValue = dataset.data[tooltipItem.index];
                            var percentage = Math.round((currentValue / total) * 100);
                            return data.labels[tooltipItem.index] + ': ' + currentValue + ' (' + percentage + '%)';
                        }
                    }
                }
            }
        });

    });