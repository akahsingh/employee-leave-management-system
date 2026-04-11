// ELMS - Main JavaScript

// Toggle Sidebar
function toggleSidebar() {
    document.body.classList.toggle('sidebar-collapsed');
    const sidebar = document.getElementById('sidebar');
    sidebar.classList.toggle('open');
}

// Auto-dismiss alerts after 4 seconds
document.addEventListener('DOMContentLoaded', function () {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            bsAlert.close();
        }, 4000);
    });

    // Date validation for leave application
    const startDate = document.querySelector('input[name="startDate"]');
    const endDate = document.querySelector('input[name="endDate"]');

    if (startDate && endDate) {
        startDate.addEventListener('change', function () {
            endDate.min = this.value;
            if (endDate.value && endDate.value < this.value) {
                endDate.value = this.value;
            }
        });
    }

    // Highlight active nav items based on URL
    const currentPath = window.location.pathname;
    document.querySelectorAll('.sidebar-nav .nav-item').forEach(function (item) {
        if (item.getAttribute('href') === currentPath) {
            item.classList.add('active');
        }
    });

    // Animate stat numbers
    document.querySelectorAll('.stat-number').forEach(function (el) {
        const target = parseInt(el.textContent, 10);
        if (!isNaN(target) && target > 0) {
            let current = 0;
            const step = Math.ceil(target / 20);
            const timer = setInterval(function () {
                current += step;
                if (current >= target) {
                    el.textContent = target;
                    clearInterval(timer);
                } else {
                    el.textContent = current;
                }
            }, 40);
        }
    });
});
