// Toggle sidebar on mobile
document.addEventListener('DOMContentLoaded', function() {
    const menuToggle = document.getElementById('menu-toggle');
    const sidebar = document.getElementById('sidebar');

    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', function() {
            sidebar.classList.toggle('active');
        });
    }

    // Close sidebar when clicking outside on mobile
    document.addEventListener('click', function(event) {
        const isMobile = window.innerWidth <= 768;
        if (isMobile && sidebar && sidebar.classList.contains('active')) {
            // Check if click is outside the sidebar
            if (!sidebar.contains(event.target) && event.target !== menuToggle) {
                sidebar.classList.remove('active');
            }
        }
    });

    // Animate elements on scroll
    const animateOnScroll = () => {
        const elements = document.querySelectorAll('.stat-card, .card');
        elements.forEach(element => {
            const elementPosition = element.getBoundingClientRect().top;
            const windowHeight = window.innerHeight;
            if (elementPosition < windowHeight - 50) {
                element.style.opacity = '1';
                element.style.transform = 'translateY(0)';
            }
        });
    };

    window.addEventListener('scroll', animateOnScroll);
    animateOnScroll(); // Initial call to animate visible elements

    // Initialize dropdowns, tooltips, or other interactive elements
    initializeUI();
});

function initializeUI() {
    // Add any additional UI initializations here
    // This can include tooltips, popovers, dropdowns, etc.
    
    // Example: Initialize tooltips (if using Bootstrap)
    // if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
    //     const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    //     tooltipTriggerList.map(function (tooltipTriggerEl) {
    //         return new bootstrap.Tooltip(tooltipTriggerEl);
    //     });
    // }
}

document.addEventListener('DOMContentLoaded', function() {
    // Toggle sidebar
    const menuToggle = document.getElementById('menu-toggle');
    const sidebar = document.getElementById('sidebar');

    menuToggle?.addEventListener('click', function() {
        sidebar.classList.toggle('active');
    });

    // Form validation
    const userForm = document.getElementById('userForm');
    const passwordField = document.getElementById('password');
    const confirmPasswordField = document.getElementById('confirmPassword');
    const firstNameField = document.getElementById('firstName');
    const lastNameField = document.getElementById('lastName');
    const successToast = document.getElementById('successToast');

    userForm.addEventListener('submit', function(e) {
        e.preventDefault();

        // Reset previous errors
        document.querySelectorAll('.field-error').forEach(el => el.remove());
        document.querySelectorAll('.input-error').forEach(el => el.classList.remove('input-error'));

        let isValid = true;

        // Validate password match
        if (passwordField.value !== confirmPasswordField.value) {
            showFieldError(confirmPasswordField, 'Passwords do not match');
            isValid = false;
        }

        // Validate password strength
        if (passwordField.value.length < 8) {
            showFieldError(passwordField, 'Password must be at least 8 characters long');
            isValid = false;
        }

        // Update avatar preview with initials
        updateAvatarPreview();

        if (isValid) {
            // Simulate successful form submission
            showSuccessToast();

            // In a real app, you would submit the form here
            // This is just a simulation for the UI demonstration
            setTimeout(() => {
                window.location.href = '/admin/annotateurs';
            }, 2000);
        }
    });

    // Avatar preview with initials
    firstNameField.addEventListener('input', updateAvatarPreview);
    lastNameField.addEventListener('input', updateAvatarPreview);

    function updateAvatarPreview() {
        const firstName = firstNameField.value.trim();
        const lastName = lastNameField.value.trim();
        const initials = (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
        document.querySelector('.avatar-text').textContent = initials || 'JD';
    }

    // File upload for avatar
    const avatarUpload = document.getElementById('avatarUpload');
    const avatarPreview = document.getElementById('avatarPreview');

    avatarUpload.addEventListener('change', function() {
        const file = this.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                avatarPreview.innerHTML = '';
                avatarPreview.style.background = 'none';

                const img = document.createElement('img');
                img.src = e.target.result;
                img.style.width = '100%';
                img.style.height = '100%';
                img.style.objectFit = 'cover';

                avatarPreview.appendChild(img);
            }
            reader.readAsDataURL(file);
        }
    });

    // Close toast
    document.querySelector('.toast-close').addEventListener('click', function() {
        successToast.classList.remove('show');
    });

    // Helper functions
    function showFieldError(field, message) {
        field.classList.add('input-error');
        const errorDiv = document.createElement('div');
        errorDiv.className = 'field-error';
        errorDiv.textContent = message;
        field.parentNode.appendChild(errorDiv);
    }

    function showSuccessToast() {
        successToast.classList.add('show');
        setTimeout(() => {
            successToast.classList.remove('show');
        }, 5000);
    }
});