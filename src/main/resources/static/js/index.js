// Custom cursor
const cursor = document.querySelector('.cursor');
const cursorFollower = document.querySelector('.cursor-follower');
const links = document.querySelectorAll('a, button');

document.addEventListener('mousemove', (e) => {
    cursor.style.left = e.clientX + 'px';
    cursor.style.top = e.clientY + 'px';
    
    setTimeout(() => {
        cursorFollower.style.left = e.clientX + 'px';
        cursorFollower.style.top = e.clientY + 'px';
    }, 50);
});

links.forEach(link => {
    link.addEventListener('mouseenter', () => {
        cursor.style.transform = 'translate(-50%, -50%) scale(1.5)';
        cursorFollower.style.width = '60px';
        cursorFollower.style.height = '60px';
        cursorFollower.style.borderColor = 'var(--primary)';
    });
    
    link.addEventListener('mouseleave', () => {
        cursor.style.transform = 'translate(-50%, -50%) scale(1)';
        cursorFollower.style.width = '40px';
        cursorFollower.style.height = '40px';
        cursorFollower.style.borderColor = 'var(--primary)';
    });
});

// Hide cursor when mouse leaves window
document.addEventListener('mouseout', () => {
    cursor.style.opacity = '0';
    cursorFollower.style.opacity = '0';
});

document.addEventListener('mouseover', () => {
    cursor.style.opacity = '1';
    cursorFollower.style.opacity = '1';
});

// Disable custom cursor on touch devices
if ('ontouchstart' in window) {
    cursor.style.display = 'none';
    cursorFollower.style.display = 'none';
}

// Navbar scroll effect
const header = document.querySelector('.main-header');
window.addEventListener('scroll', () => {
    if (window.scrollY > 50) {
        header.classList.add('scrolled');
    } else {
        header.classList.remove('scrolled');
    }
});

// Mobile menu toggle
const mobileMenuToggle = document.querySelector('.mobile-menu-toggle');
const navLinks = document.querySelector('.nav-links');
const navActions = document.querySelector('.nav-actions');

mobileMenuToggle.addEventListener('click', () => {
    mobileMenuToggle.classList.toggle('active');
    
    if (mobileMenuToggle.classList.contains('active')) {
        const mobileMenu = document.createElement('div');
        mobileMenu.classList.add('mobile-menu');
        
        const mobileNavLinks = navLinks.cloneNode(true);
        const mobileNavActions = navActions.cloneNode(true);
        
        mobileMenu.appendChild(mobileNavLinks);
        mobileMenu.appendChild(mobileNavActions);
        
        document.body.appendChild(mobileMenu);
        
        setTimeout(() => {
            mobileMenu.classList.add('active');
        }, 10);
    } else {
        const mobileMenu = document.querySelector('.mobile-menu');
        mobileMenu.classList.remove('active');
        
        setTimeout(() => {
            mobileMenu.remove();
        }, 300);
    }
});

// Theme toggle
const themeToggle = document.getElementById('themeToggle');
const themeIcon = themeToggle.querySelector('i');

// Check for saved theme preference or use device preference
const savedTheme = localStorage.getItem('theme');
const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

if (savedTheme === 'dark' || (!savedTheme && prefersDark)) {
    document.documentElement.setAttribute('data-theme', 'dark');
    themeIcon.classList.remove('fa-moon');
    themeIcon.classList.add('fa-sun');
}

themeToggle.addEventListener('click', () => {
    const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';
    
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    
    if (newTheme === 'dark') {
        themeIcon.classList.remove('fa-moon');
        themeIcon.classList.add('fa-sun');
    } else {
        themeIcon.classList.remove('fa-sun');
        themeIcon.classList.add('fa-moon');
    }
});

// Counter animation
const counterElements = document.querySelectorAll('.stat-number');

const animateCounter = (element) => {
    const target = parseInt(element.getAttribute('data-count'));
    const duration = 2000;
    const step = target / (duration / 16);
    let current = 0;
    
    const updateCounter = () => {
        current += step;
        if (current < target) {
            element.textContent = Math.floor(current);
            requestAnimationFrame(updateCounter);
        } else {
            element.textContent = target;
        }
    };
    
    updateCounter();
};

// Testimonial slider
const testimonialTrack = document.querySelector('.testimonial-track');
const testimonialCards = document.querySelectorAll('.testimonial-card');
const prevButton = document.querySelector('.slider-arrow.prev');
const nextButton = document.querySelector('.slider-arrow.next');
const dots = document.querySelectorAll('.dot');

let currentSlide = 0;
const slideCount = testimonialCards.length;

const updateSlider = () => {
    testimonialTrack.style.transform = `translateX(-${currentSlide * 100}%)`;
    
    dots.forEach((dot, index) => {
        dot.classList.toggle('active', index === currentSlide);
    });
};

prevButton.addEventListener('click', () => {
    currentSlide = (currentSlide - 1 + slideCount) % slideCount;
    updateSlider();
});

nextButton.addEventListener('click', () => {
    currentSlide = (currentSlide + 1) % slideCount;
    updateSlider();
});

dots.forEach((dot, index) => {
    dot.addEventListener('click', () => {
        currentSlide = index;
        updateSlider();
    });
});

// Auto slide every 5 seconds
let slideInterval = setInterval(() => {
    currentSlide = (currentSlide + 1) % slideCount;
    updateSlider();
}, 5000);

testimonialTrack.addEventListener('mouseenter', () => {
    clearInterval(slideInterval);
});

testimonialTrack.addEventListener('mouseleave', () => {
    slideInterval = setInterval(() => {
        currentSlide = (currentSlide + 1) % slideCount;
        updateSlider();
    }, 5000);
});

// Pricing toggle
const billingToggle = document.getElementById('billingToggle');
const priceElements = document.querySelectorAll('.amount');

billingToggle.addEventListener('change', () => {
    const isYearly = billingToggle.checked;
    
    priceElements.forEach(element => {
        const monthlyPrice = element.getAttribute('data-monthly');
        const yearlyPrice = element.getAttribute('data-yearly');
        
        element.textContent = isYearly ? yearlyPrice : monthlyPrice;
    });
    
    const periodElements = document.querySelectorAll('.period');
    periodElements.forEach(element => {
        element.textContent = isYearly ? '/month (billed yearly)' : '/month';
    });
});

// Back to top button
const backToTopButton = document.getElementById('backToTop');

window.addEventListener('scroll', () => {
    if (window.scrollY > 500) {
        backToTopButton.classList.add('visible');
    } else {
        backToTopButton.classList.remove('visible');
    }
});

backToTopButton.addEventListener('click', () => {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
});

// Intersection Observer for animations
const observeElements = (elements, className) => {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add(className);
                
                // Animate counter if it's a stat number
                if (entry.target.classList.contains('stat-number')) {
                    animateCounter(entry.target);
                }
                
                observer.unobserve(entry.target);
            }
        });
    }, {
        threshold: 0.1
    });
    
    elements.forEach(element => {
        observer.observe(element);
    });
};

// Add animation classes
document.addEventListener('DOMContentLoaded', () => {
    // Hero section animations
    observeElements([document.querySelector('.hero-title')], 'animate-fade-in');
    observeElements([document.querySelector('.hero-subtitle')], 'animate-fade-in-delay-1');
    observeElements([document.querySelector('.hero-actions')], 'animate-fade-in-delay-2');
    observeElements([document.querySelector('.hero-stats')], 'animate-fade-in-delay-3');
    observeElements([document.querySelector('.hero-image')], 'animate-fade-in-up');
    
    // Feature cards animations
    observeElements(document.querySelectorAll('.feature-card'), 'animate-fade-in-up');
    
    // Steps animations
    observeElements(document.querySelectorAll('.step'), 'animate-fade-in-up');
    
    // Testimonial animations
    observeElements([document.querySelector('.testimonial-slider')], 'animate-fade-in');
    
    // Pricing cards animations
    observeElements(document.querySelectorAll('.pricing-card'), 'animate-fade-in-up');
    
    // CTA section animations
    observeElements([document.querySelector('.cta-content')], 'animate-fade-in');
    
    // Counter animations
    observeElements(document.querySelectorAll('.stat-number'), 'ready-to-animate');
});

// Add CSS animations dynamically
const style = document.createElement('style');
style.textContent = `
    .animate-fade-in {
        opacity: 0;
        animation: fadeIn 1s ease forwards;
    }
    
    .animate-fade-in-delay-1 {
        opacity: 0;
        animation: fadeIn 1s ease 0.2s forwards;
    }
    
    .animate-fade-in-delay-2 {
        opacity: 0;
        animation: fadeIn 1s ease 0.4s forwards;
    }
    
    .animate-fade-in-delay-3 {
        opacity: 0;
        animation: fadeIn 1s ease 0.6s forwards;
    }
    
    .animate-fade-in-up {
        opacity: 0;
        transform: translateY(30px);
        animation: fadeInUp 1s ease forwards;
    }
    
    @keyframes fadeIn {
        from {
            opacity: 0;
        }
        to {
            opacity: 1;
        }
    }
    
    @keyframes fadeInUp {
        from {
            opacity: 0;
            transform: translateY(30px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }
    
    /* Mobile menu animation */
    .mobile-menu {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100vh;
        background: var(--light);
        z-index: 99;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 2rem;
        opacity: 0;
        transform: translateY(-20px);
        transition: all 0.3s ease;
        pointer-events: none;
    }
    
    [data-theme="dark"] .mobile-menu {
        background: var(--light);
    }
    
    .mobile-menu.active {
        opacity: 1;
        transform: translateY(0);
        pointer-events: all;
    }
    
    .mobile-menu .nav-links {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 1.5rem;
    }
    
    .mobile-menu .nav-actions {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 1rem;
    }
`;

document.head.appendChild(style);
