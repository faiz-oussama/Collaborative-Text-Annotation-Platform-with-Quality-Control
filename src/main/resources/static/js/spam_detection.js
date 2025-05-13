/**
 * Spam Detection System JavaScript
 * Handles the interaction with the spam detection backend
 */
/**
 * Spam Detection System JavaScript - Enhanced for Modern UI
 * Handles the interaction with the spam detection backend with added animations
 */
document.addEventListener('DOMContentLoaded', function() {
    // Elements
    const pythonServiceStatus = document.getElementById('pythonServiceStatus');
    const serviceControls = document.getElementById('serviceControls');
    const startServiceBtn = document.getElementById('startServiceBtn');
    const serviceStartStatus = document.getElementById('serviceStartStatus');
    const refreshSpamResults = document.getElementById('refreshSpamResults');
    const spamDetectionForm = document.getElementById('spamDetectionForm');
    const runDetectionBtn = document.getElementById('runDetectionBtn');
    const detectionStatus = document.getElementById('detectionStatus');
    const modelTypeSelect = document.getElementById('modelType');

    
    // Add animation classes to cards and sections
    animateUIElements();
    
    // Check Python service status on page load
    checkServiceStatus();
    
    // Set up event listeners
    if (startServiceBtn) {
        startServiceBtn.addEventListener('click', startPythonService);
    }
    
    if (refreshSpamResults) {
        refreshSpamResults.addEventListener('click', function(e) {
            e.preventDefault();
            checkServiceStatus();
            window.location.reload();
        });
    }
    
    if (spamDetectionForm) {
        spamDetectionForm.addEventListener('submit', function(e) {
            // Check if Python service is running before submitting
            if (pythonServiceStatus && pythonServiceStatus.classList.contains('status-unhealthy')) {
                e.preventDefault();
                showToast('Python service is not running. Please start the service before running detection.', 'danger');
                return false;
            }
            
            // Show loading state
            if (runDetectionBtn) {
                runDetectionBtn.disabled = true;
                runDetectionBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Running...';
            }
            
            if (detectionStatus) {
                detectionStatus.innerHTML = '<div class="alert alert-info">Detection in progress. This may take a few minutes...</div>';
            }
        });
    }
    
    /**
     * Check the status of the Python service
     */
    function checkServiceStatus() {
        if (!pythonServiceStatus) return;
        
        fetch('/admin/spam/service-status')
            .then(response => response.json())
            .then(data => {
                if (data.healthy) {
                    pythonServiceStatus.innerHTML = '<i class="fas fa-check-circle text-success"></i> <span class="text-success">Service is running</span>';
                    pythonServiceStatus.classList.add('status-healthy');
                    pythonServiceStatus.classList.remove('status-unhealthy');
                    
                    if (serviceControls) {
                        serviceControls.style.display = 'none';
                    }
                } else {
                    pythonServiceStatus.innerHTML = '<i class="fas fa-exclamation-circle text-danger"></i> <span class="text-danger">Service is not running</span>';
                    pythonServiceStatus.classList.add('status-unhealthy');
                    pythonServiceStatus.classList.remove('status-healthy');
                    
                    if (serviceControls) {
                        serviceControls.style.display = 'block';
                    }
                }
            })
            .catch(error => {
                console.error('Error checking service status:', error);
                pythonServiceStatus.innerHTML = '<i class="fas fa-exclamation-triangle text-warning"></i> <span class="text-warning">Unable to check service status</span>';
                
                if (serviceControls) {
                    serviceControls.style.display = 'block';
                }
            });
    }
    
    /**
     * Start the Python service
     */
    function startPythonService() {
        if (!startServiceBtn || !serviceStartStatus) return;
        
        startServiceBtn.disabled = true;
        startServiceBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Starting...';
        serviceStartStatus.innerHTML = '';
        
        fetch('/admin/spam/start-service', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                serviceStartStatus.innerHTML = '<span class="text-success">Service started successfully!</span>';
                setTimeout(() => {
                    checkServiceStatus();
                }, 2000);
            } else {
                serviceStartStatus.innerHTML = '<span class="text-danger">Failed to start service: ' + data.message + '</span>';
                startServiceBtn.disabled = false;
                startServiceBtn.innerHTML = '<i class="fas fa-play"></i> Retry Starting Service';
            }
        })
        .catch(error => {
            console.error('Error starting service:', error);
            serviceStartStatus.innerHTML = '<span class="text-danger">Error communicating with server</span>';
            startServiceBtn.disabled = false;
            startServiceBtn.innerHTML = '<i class="fas fa-play"></i> Retry Starting Service';
        });
    }
    
    // Configuration function removed as settings are now part of the main form
    
    /**
     * Add animations and interactive elements to the UI
     */
    function animateUIElements() {
        // Animate card on load
        const spamCard = document.querySelector('.spam-detection .card');
        if (spamCard) {
            setTimeout(() => {
                spamCard.style.opacity = '0';
                spamCard.style.transform = 'translateY(20px)';
                spamCard.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                
                setTimeout(() => {
                    spamCard.style.opacity = '1';
                    spamCard.style.transform = 'translateY(0)';
                }, 100);
            }, 300);
        }
        
        // Add animation to table rows when results are available
        const tableRows = document.querySelectorAll('.spam-detection table tbody tr');
        tableRows.forEach((row, index) => {
            row.style.opacity = '0';
            row.style.transform = 'translateY(10px)';
            row.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
            
            setTimeout(() => {
                row.style.opacity = '1';
                row.style.transform = 'translateY(0)';
            }, 500 + (index * 100));
        });
        
        // Add hover effects to buttons dynamically
        const buttons = document.querySelectorAll('.spam-detection .btn');
        buttons.forEach(button => {
            button.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-2px)';
                this.style.boxShadow = '0 6px 15px rgba(0, 0, 0, 0.1)';
            });
            button.addEventListener('mouseleave', function() {
                this.style.transform = '';
                this.style.boxShadow = '';
            });
        });
        
        // Add animation to progress bars
        const progressBars = document.querySelectorAll('.progress-bar');
        progressBars.forEach(bar => {
            // Store the target width
            const targetWidth = bar.style.width;
            // Start at width 0
            bar.style.width = '0%';
            // Animate to target width
            setTimeout(() => {
                bar.style.transition = 'width 1.2s ease-in-out';
                bar.style.width = targetWidth;
            }, 800);
        });
        
        // Add numbers counter animation to summary values
        const summaryValues = document.querySelectorAll('.summary-value');
        summaryValues.forEach(value => {
            const targetValue = parseInt(value.textContent, 10);
            if (!isNaN(targetValue)) {
                animateCounter(value, targetValue);
            }
        });
    }
    
    /**
     * Animate a counter from 0 to target value
     */
    function animateCounter(element, targetValue) {
        let currentValue = 0;
        const duration = 1500; // ms
        const interval = 50; // ms
        const steps = duration / interval;
        const increment = targetValue / steps;
        
        const counter = setInterval(() => {
            currentValue += increment;
            if (currentValue >= targetValue) {
                clearInterval(counter);
                element.textContent = targetValue;
            } else {
                element.textContent = Math.floor(currentValue);
            }
        }, interval);
    }
    
    /**
     * Show a toast notification
     */
    function showToast(message, type = 'info') {
        // Create toast container if it doesn't exist
        let toastContainer = document.querySelector('.toast-container');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
            document.body.appendChild(toastContainer);
        }
        
        // Create toast element
        const toastId = 'toast-' + Date.now();
        const toast = document.createElement('div');
        toast.className = `toast bg-${type} text-white`;
        toast.id = toastId;
        toast.setAttribute('role', 'alert');
        toast.setAttribute('aria-live', 'assertive');
        toast.setAttribute('aria-atomic', 'true');
        
        // Set toast content
        toast.innerHTML = `
            <div class="toast-header bg-${type} text-white">
                <strong class="me-auto">Notification</strong>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
            <div class="toast-body">
                ${message}
            </div>
        `;
        
        // Add toast to container
        toastContainer.appendChild(toast);
        
        // Initialize and show toast with fallback if Bootstrap is not available
        if (typeof bootstrap !== 'undefined') {
            const bsToast = new bootstrap.Toast(toast);
            bsToast.show();
        } else {
            // Simple fallback if Bootstrap is not available
            toast.style.display = 'block';
            setTimeout(() => {
                toast.remove();
            }, 5000);
        }
    }
});
