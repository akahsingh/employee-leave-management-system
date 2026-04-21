// ELMS - Main JavaScript

// Toggle Sidebar
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');
    sidebar.classList.toggle('collapsed');
    if (mainContent) mainContent.classList.toggle('expanded');
}

document.addEventListener('DOMContentLoaded', function () {
    // Auto-dismiss alerts after 4 seconds
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

    // Animate stat values
    document.querySelectorAll('.stat-val').forEach(function (el) {
        const target = parseInt(el.textContent, 10);
        if (!isNaN(target) && target > 0) {
            let current = 0;
            const step = Math.ceil(target / 20);
            el.textContent = '0';
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
