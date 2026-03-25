/* ===== RevWorkForce App - JavaScript with jQuery ===== */

// Global state
let currentUser = null;

// ===== Utility Functions =====
function showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    const icon = type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle';
    toast.innerHTML = `<i class="fas fa-${icon}"></i> ${message}`;
    container.appendChild(toast);
    setTimeout(() => { toast.remove(); }, 4000);
}

function apiCall(url, method, data, successCb, errorCb) {
    const headers = {};
    if (method !== 'GET') {
        const csrfToken = $('meta[name="_csrf"]').attr('content');
        const csrfHeader = $('meta[name="_csrf_header"]').attr('content');
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }
    }
    $.ajax({
        url: url,
        type: method,
        contentType: 'application/json',
        headers: headers,
        data: data ? JSON.stringify(data) : null,
        success: function (resp) {
            if (successCb) successCb(resp);
        },
        error: function (xhr) {
            const msg = xhr.responseJSON ? xhr.responseJSON.message : 'An error occurred';
            if (errorCb) errorCb(msg);
            else showToast(msg, 'error');
        }
    });
}

function getStatusBadge(status) {
    return `<span class="badge-custom badge-${status.toLowerCase()}">${status}</span>`;
}

function getPriorityBadge(priority) {
    return `<span class="badge-custom badge-${priority.toLowerCase()}">${priority}</span>`;
}

function getRoleBadge(role) {
    return `<span class="badge-custom badge-${role.toLowerCase()}">${role}</span>`;
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function togglePasswordVisibility(inputId, btnElement) {
    const input = document.getElementById(inputId);
    const icon = btnElement.querySelector('i');
    if (input.type === 'password') {
        input.type = 'text';
        icon.classList.remove('fa-eye');
        icon.classList.add('fa-eye-slash');
    } else {
        input.type = 'password';
        icon.classList.remove('fa-eye-slash');
        icon.classList.add('fa-eye');
    }
}

// ===== Auth Functions =====
function checkSession() {
    $.get('/api/auth/session', function (resp) {
        if (resp.authenticated) {
            currentUser = resp.user;
            const path = window.location.pathname;
            if (path === '/login' || path === '/register' || path === '/') {
                window.location.href = '/dashboard';
            } else {
                initSidebar();
                loadNotificationCount();
                const page = path.substring(1);
                if (typeof window['load' + page.charAt(0).toUpperCase() + page.slice(1)] === 'function') {
                    window['load' + page.charAt(0).toUpperCase() + page.slice(1)]();
                }
            }
        } else {
            const path = window.location.pathname;
            if (path !== '/login' && path !== '/register') {
                window.location.href = '/login';
            }
        }
    }).fail(function () {
        const path = window.location.pathname;
        if (path !== '/login' && path !== '/register') {
            window.location.href = '/login';
        }
    });
}

function showLoginError(msg) {
    $('#loginErrorMsg').text(msg);
    $('#loginError').slideDown(200);
}

function hideLoginError() {
    $('#loginError').slideUp(200);
}

function handleLogin(e) {
    e.preventDefault();
    hideLoginError();
    const email = $('#loginEmail').val();
    const password = $('#loginPassword').val();
    const $btn = $('#loginBtn');
    $btn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Logging in...');

    // Safety timeout - reset button after 10 seconds if no response
    const safetyTimer = setTimeout(function() {
        $btn.prop('disabled', false).html('<i class="fas fa-sign-in-alt"></i> Login');
        showLoginError('Login request timed out. Please try again.');
    }, 10000);

    $.ajax({
        url: '/api/auth/login',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ email, password }),
        success: function (resp) {
            clearTimeout(safetyTimer);
            if (resp.success) {
                showToast('Login successful!', 'success');
                setTimeout(() => window.location.href = '/dashboard', 500);
            } else {
                showLoginError(resp.message || 'Login failed. Please check your credentials.');
                $btn.prop('disabled', false).html('<i class="fas fa-sign-in-alt"></i> Login');
            }
        },
        error: function (xhr) {
            clearTimeout(safetyTimer);
            let msg = 'An error occurred. Please try again.';
            try {
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    msg = xhr.responseJSON.message;
                } else if (xhr.responseText) {
                    const parsed = JSON.parse(xhr.responseText);
                    if (parsed.message) msg = parsed.message;
                }
            } catch (e) { /* use default msg */ }
            showLoginError(msg);
            $btn.prop('disabled', false).html('<i class="fas fa-sign-in-alt"></i> Login');
        }
    });
}

function handleRegister(e) {
    e.preventDefault();
    const user = {
        name: $('#regName').val(),
        email: $('#regEmail').val(),
        password: $('#regPassword').val(),
        department: $('#regDepartment').val(),
        designation: $('#regDesignation').val(),
        phone: $('#regPhone').val()
    };
    if (user.password.length < 6) {
        showToast('Password must be at least 6 characters', 'error');
        return;
    }
    $('#registerBtn').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Registering...');
    apiCall('/api/auth/register', 'POST', user,
        function (resp) {
            if (resp.success) {
                showToast('Registration successful! Please login.', 'success');
                setTimeout(() => window.location.href = '/login', 1500);
            } else {
                showToast(resp.message, 'error');
                $('#registerBtn').prop('disabled', false).html('<i class="fas fa-user-plus"></i> Register');
            }
        },
        function (msg) {
            showToast(msg, 'error');
            $('#registerBtn').prop('disabled', false).html('<i class="fas fa-user-plus"></i> Register');
        }
    );
}

function handleLogout() {
    apiCall('/api/auth/logout', 'POST', null, function () {
        window.location.href = '/login';
    });
}

// ===== Sidebar & Navigation =====
function initSidebar() {
    if (!currentUser) return;
    const role = currentUser.role;
    let nav = `
        <li><a href="/dashboard" class="${location.pathname === '/dashboard' ? 'active' : ''}"><i class="fas fa-th-large"></i> Dashboard</a></li>
        <li><a href="/profile" class="${location.pathname === '/profile' ? 'active' : ''}"><i class="fas fa-user"></i> My Profile</a></li>
    `;
    nav += '<div class="sidebar-section">Modules</div>';
    nav += `<li><a href="/leaves" class="${location.pathname === '/leaves' ? 'active' : ''}"><i class="fas fa-calendar-alt"></i> Leave Management</a></li>`;
    nav += `<li><a href="/goals" class="${location.pathname === '/goals' ? 'active' : ''}"><i class="fas fa-bullseye"></i> Goals</a></li>`;
    nav += `<li><a href="/performance" class="${location.pathname === '/performance' ? 'active' : ''}"><i class="fas fa-chart-line"></i> Performance</a></li>`;
    nav += `<li><a href="/employees" class="${location.pathname === '/employees' ? 'active' : ''}"><i class="fas fa-users"></i> Employees</a></li>`;
    nav += `<li><a href="/announcements" class="${location.pathname === '/announcements' ? 'active' : ''}"><i class="fas fa-bullhorn"></i> Announcements</a></li>`;
    nav += `<li><a href="/holidays" class="${location.pathname === '/holidays' ? 'active' : ''}"><i class="fas fa-umbrella-beach"></i> Holidays</a></li>`;
    nav += '<div class="sidebar-section">Account</div>';
    nav += '<li><a href="#" onclick="handleLogout()"><i class="fas fa-sign-out-alt"></i> Logout</a></li>';
    $('#sidebarNav').html(nav);
    $('#sidebarUserName').text(currentUser.name);
    $('#sidebarUserRole').text(currentUser.role);
    const initials = currentUser.name.split(' ').map(n => n[0]).join('').substring(0, 2);
    $('#userAvatar').text(initials);
    $('#topBarUser').text(currentUser.name);
}

function loadNotificationCount() {
    $.get('/api/employees/notifications/count', function (resp) {
        if (resp.count > 0) {
            $('#notifBadge').text(resp.count).show();
        } else {
            $('#notifBadge').hide();
        }
    });
}

// ===== Dashboard =====
function loadDashboard() {
    // Show welcome banner
    if (currentUser && currentUser.name) {
        const firstName = currentUser.name.split(' ')[0];
        $('#welcomeText').text('Welcome back, ' + firstName + '! 👋');
        $('#welcomeBanner').show();
    }
    apiCall('/api/dashboard', 'GET', null, function (data) {
        let statsHtml = '';
        if (data.role === 'ADMIN') {
            statsHtml = `
                <div class="stat-card" onclick="window.location.href='/employees'" style="cursor:pointer"><div class="stat-icon primary"><i class="fas fa-users"></i></div><div class="stat-value">${data.totalEmployees || 0}</div><div class="stat-label">Total Employees</div></div>
                <div class="stat-card success" onclick="window.location.href='/employees'" style="cursor:pointer"><div class="stat-icon success"><i class="fas fa-user-tie"></i></div><div class="stat-value">${data.totalManagers || 0}</div><div class="stat-label">Managers</div></div>
                <div class="stat-card warning" onclick="window.location.href='/leaves'" style="cursor:pointer"><div class="stat-icon warning"><i class="fas fa-clock"></i></div><div class="stat-value">${data.pendingLeaves || 0}</div><div class="stat-label">Pending Leaves</div></div>
                <div class="stat-card secondary" onclick="window.location.href='/employees'" style="cursor:pointer"><div class="stat-icon secondary"><i class="fas fa-building"></i></div><div class="stat-value">${data.departments || 0}</div><div class="stat-label">Departments</div></div>`;
        } else if (data.role === 'MANAGER') {
            statsHtml = `
                <div class="stat-card" onclick="window.location.href='/employees'" style="cursor:pointer"><div class="stat-icon primary"><i class="fas fa-users"></i></div><div class="stat-value">${data.teamSize || 0}</div><div class="stat-label">Team Members</div></div>
                <div class="stat-card warning" onclick="window.location.href='/leaves'" style="cursor:pointer"><div class="stat-icon warning"><i class="fas fa-clock"></i></div><div class="stat-value">${data.pendingTeamLeaves || 0}</div><div class="stat-label">Pending Team Leaves</div></div>
                <div class="stat-card secondary" style="cursor:default"><div class="stat-icon secondary"><i class="fas fa-bell"></i></div><div class="stat-value">${data.unreadNotifications || 0}</div><div class="stat-label">Unread Notifications</div></div>`;
        } else {
            statsHtml = `
                <div class="stat-card secondary" style="cursor:default"><div class="stat-icon secondary"><i class="fas fa-bell"></i></div><div class="stat-value">${data.unreadNotifications || 0}</div><div class="stat-label">Unread Notifications</div></div>`;
        }
        $('#dashboardStats').html(statsHtml);

        // Leave balances
        if (data.leaveBalances && data.leaveBalances.length > 0) {
            let balHtml = '';
            data.leaveBalances.forEach(b => {
                const remaining = b.totalDays - b.usedDays;
                balHtml += `<div class="balance-card"><div class="balance-type">${b.leaveType}</div><div class="balance-value">${remaining}</div><div class="balance-detail">Used: ${b.usedDays} / Total: ${b.totalDays}</div></div>`;
            });
            $('#leaveBalances').html(balHtml);
        }

        // Announcements
        if (data.announcements && data.announcements.length > 0) {
            let annHtml = '';
            data.announcements.forEach(a => {
                annHtml += `<div style="padding:0.75rem 0;border-bottom:1px solid var(--border)"><strong style="color:var(--text-primary)">${escapeHtml(a.title)}</strong><p style="color:var(--text-muted);font-size:0.82rem;margin-top:0.3rem">${escapeHtml(a.description || '')}</p><small style="color:var(--text-muted)">${formatDate(a.createdDate || a.date)}</small></div>`;
            });
            $('#dashboardAnnouncements').html(annHtml);
        }
    });
}

// ===== Leave Management =====
function loadLeaves() {
    const role = currentUser.role;
    let tabs = '<button class="tab-btn active" onclick="switchTab(\'myLeaves\', this)">My Leaves</button>';
    tabs += '<button class="tab-btn" onclick="switchTab(\'applyLeave\', this)">Apply Leave</button>';
    tabs += '<button class="tab-btn" onclick="switchTab(\'leaveBalance\', this)">Balance</button>';
    if (role === 'MANAGER' || role === 'ADMIN') {
        tabs += '<button class="tab-btn" onclick="switchTab(\'teamLeaves\', this)">Team Leaves</button>';
    }
    if (role === 'ADMIN') {
        tabs += '<button class="tab-btn" onclick="switchTab(\'allLeaves\', this)">All Leaves</button>';
        tabs += '<button class="tab-btn" onclick="switchTab(\'leaveTypes\', this)">Leave Types</button>';
    }
    $('#leaveTabs').html(tabs);
    loadMyLeaves();
    loadLeaveBalance();
    loadLeaveTypeOptions();
}

function loadMyLeaves() {
    apiCall('/api/leaves/my', 'GET', null, function (data) {
        if (!data || data.length === 0) {
            $('#myLeavesTable').html('<div class="empty-state"><i class="fas fa-calendar-check"></i><p>No leave applications yet</p></div>');
            return;
        }
        let html = '<table class="table-custom"><thead><tr><th>Type</th><th>From</th><th>To</th><th>Reason</th><th>Status</th><th>Comment</th><th>Actions</th></tr></thead><tbody>';
        data.forEach(l => {
            html += `<tr><td>${l.leaveType}</td><td>${formatDate(l.startDate)}</td><td>${formatDate(l.endDate)}</td><td>${escapeHtml(l.reason || '-')}</td><td>${getStatusBadge(l.status)}</td><td>${escapeHtml(l.managerComment || '-')}</td>`;
            html += `<td>${l.status === 'PENDING' ? `<button class="btn-danger-custom" onclick="cancelLeave(${l.id})"><i class="fas fa-times"></i> Cancel</button>` : '-'}</td></tr>`;
        });
        html += '</tbody></table>';
        $('#myLeavesTable').html(html);
    });
}

function loadLeaveBalance() {
    apiCall('/api/leaves/balance', 'GET', null, function (data) {
        if (!data || data.length === 0) {
            $('#leaveBalanceGrid').html('<div class="empty-state"><i class="fas fa-chart-bar"></i><p>No leave balances configured</p></div>');
            return;
        }
        let html = '';
        const deduped = new Map();
        data.forEach(b => {
            const key = b.leaveType ? b.leaveType.trim().toLowerCase() : '';
            if (!key) return;
            if (!deduped.has(key)) {
                deduped.set(key, { ...b });
            }
        });

        deduped.forEach(b => {
            const remaining = b.totalDays - b.usedDays;
            html += `<div class="balance-card"><div class="balance-type">${b.leaveType}</div><div class="balance-value">${remaining}</div><div class="balance-detail">Used: ${b.usedDays} / Total: ${b.totalDays}</div><div class="progress-custom mt-1"><div class="progress-bar-custom" style="width:${b.totalDays > 0 ? ((b.usedDays / b.totalDays) * 100) : 0}%"></div></div></div>`;
        });
        $('#leaveBalanceGrid').html(html);
    });
}

function loadLeaveTypeOptions() {
    apiCall('/api/leaves/types', 'GET', null, function (data) {
        let opts = '<option value="">Select Leave Type</option>';
        data.forEach(t => { opts += `<option value="${t.name}">${t.name}</option>`; });
        $('#leaveTypeSelect').html(opts);
    });
}

function loadTeamLeaves() {
    apiCall('/api/leaves/team', 'GET', null, function (data) {
        if (!data || data.length === 0) {
            $('#teamLeavesTable').html('<div class="empty-state"><i class="fas fa-users"></i><p>No team leave requests</p></div>');
            return;
        }
        let html = '<table class="table-custom"><thead><tr><th>Employee</th><th>Type</th><th>From</th><th>To</th><th>Reason</th><th>Status</th><th>Actions</th></tr></thead><tbody>';
        data.forEach(l => {
            html += `<tr><td>${l.employeeId}</td><td>${l.leaveType}</td><td>${formatDate(l.startDate)}</td><td>${formatDate(l.endDate)}</td><td>${escapeHtml(l.reason || '-')}</td><td>${getStatusBadge(l.status)}</td>`;
            if (l.status === 'PENDING') {
                html += `<td><button class="btn-success-custom" onclick="approveLeave(${l.id})"><i class="fas fa-check"></i></button> <button class="btn-danger-custom" onclick="rejectLeave(${l.id})"><i class="fas fa-times"></i></button></td>`;
            } else {
                html += '<td>-</td>';
            }
            html += '</tr>';
        });
        html += '</tbody></table>';
        $('#teamLeavesTable').html(html);
    });
}

function loadAllLeaves() {
    apiCall('/api/admin/leaves', 'GET', null, function (data) {
        if (!data || data.length === 0) {
            $('#allLeavesTable').html('<div class="empty-state"><i class="fas fa-calendar"></i><p>No leave data</p></div>');
            return;
        }
        let html = '<table class="table-custom"><thead><tr><th>ID</th><th>Employee</th><th>Type</th><th>From</th><th>To</th><th>Status</th></tr></thead><tbody>';
        data.forEach(l => {
            html += `<tr><td>${l.id}</td><td>${l.employeeId}</td><td>${l.leaveType}</td><td>${formatDate(l.startDate)}</td><td>${formatDate(l.endDate)}</td><td>${getStatusBadge(l.status)}</td></tr>`;
        });
        html += '</tbody></table>';
        $('#allLeavesTable').html(html);
    });
}

function loadLeaveTypes() {
    apiCall('/api/leaves/types', 'GET', null, function (data) {
        let html = '<table class="table-custom"><thead><tr><th>Name</th><th>Default Days</th><th>Actions</th></tr></thead><tbody>';
        data.forEach(t => {
            html += `<tr><td>${t.name}</td><td>${t.defaultDays}</td><td><button class="btn-danger-custom" onclick="deleteLeaveType(${t.id})"><i class="fas fa-trash"></i></button></td></tr>`;
        });
        html += '</tbody></table>';
        $('#leaveTypesTable').html(html);
    });
}

function handleApplyLeave(e) {
    e.preventDefault();
    const request = {
        leaveType: $('#leaveTypeSelect').val(),
        startDate: $('#leaveStartDate').val(),
        endDate: $('#leaveEndDate').val(),
        reason: $('#leaveReason').val()
    };
    if (!request.leaveType || !request.startDate || !request.endDate) {
        showToast('Please fill all required fields', 'error');
        return;
    }
    apiCall('/api/leaves', 'POST', request,
        function (resp) {
            showToast('Leave applied successfully!', 'success');
            switchTab('myLeaves', document.querySelector('.tab-btn'));
            loadMyLeaves();
            loadLeaveBalance();
            $('#applyLeaveForm')[0].reset();
        },
        function (msg) { showToast(msg, 'error'); }
    );
}

function cancelLeave(id) {
    if (!confirm('Cancel this leave request?')) return;
    apiCall('/api/leaves/' + id + '/cancel', 'PUT', {},
        function () { showToast('Leave cancelled', 'success'); loadMyLeaves(); loadLeaveBalance(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

function approveLeave(id) {
    const comment = prompt('Add a comment (optional):');
    apiCall('/api/leaves/' + id + '/approve', 'PUT', { comment: comment || '' },
        function () { showToast('Leave approved', 'success'); loadTeamLeaves(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

function rejectLeave(id) {
    const comment = prompt('Reason for rejection (required):');
    if (!comment || !comment.trim()) { showToast('Comment is required', 'error'); return; }
    apiCall('/api/leaves/' + id + '/reject', 'PUT', { comment: comment },
        function () { showToast('Leave rejected', 'success'); loadTeamLeaves(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

function deleteLeaveType(id) {
    if (!confirm('Delete this leave type?')) return;
    apiCall('/api/admin/leave-types/' + id, 'DELETE', null,
        function () { showToast('Leave type deleted', 'success'); loadLeaveTypes(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

// ===== Goal Management =====
function loadGoals() {
    const role = currentUser.role;
    let tabs = '<button class="tab-btn active" onclick="switchTab(\'myGoals\', this)">My Goals</button>';
    tabs += '<button class="tab-btn" onclick="switchTab(\'addGoal\', this)">Add Goal</button>';
    if (role === 'MANAGER' || role === 'ADMIN') {
        tabs += '<button class="tab-btn" onclick="switchTab(\'teamGoals\', this)">Team Goals</button>';
    }
    $('#goalTabs').html(tabs);
    loadMyGoals();
}

function loadMyGoals() {
    apiCall('/api/goals/my', 'GET', null, function (data) {
        if (!data || data.length === 0) {
            $('#myGoalsContent').html('<div class="empty-state"><i class="fas fa-bullseye"></i><p>No goals set yet</p></div>');
            return;
        }
        let html = '';
        data.forEach(g => {
            html += `<div class="card-custom mb-2">
                <div class="flex-between"><strong>${escapeHtml(g.description)}</strong>${getPriorityBadge(g.priority)}</div>
                <div class="mt-1" style="font-size:0.82rem;color:var(--text-muted)">Deadline: ${formatDate(g.deadline)}</div>
                <div class="mt-1"><span style="font-size:0.78rem;color:var(--text-muted)">Progress: ${g.progress}%</span><div class="progress-custom mt-1"><div class="progress-bar-custom" style="width:${g.progress}%"></div></div></div>
                ${g.managerComment ? `<div class="mt-1" style="font-size:0.82rem;color:var(--info)"><i class="fas fa-comment"></i> ${escapeHtml(g.managerComment)}</div>` : ''}
                <div class="mt-2 flex gap-1">
                    <button class="btn-outline-custom" onclick="editGoal(${g.id}, ${g.progress})"><i class="fas fa-edit"></i> Update</button>
                    <button class="btn-danger-custom" onclick="deleteGoal(${g.id})"><i class="fas fa-trash"></i></button>
                </div>
            </div>`;
        });
        $('#myGoalsContent').html(html);
    });
}

function loadTeamGoals() {
    apiCall('/api/goals/team', 'GET', null, function (data) {
        if (!data || data.length === 0) {
            $('#teamGoalsContent').html('<div class="empty-state"><i class="fas fa-users"></i><p>No team goals</p></div>');
            return;
        }
        let html = '';
        data.forEach(g => {
            html += `<div class="card-custom mb-2">
                <div class="flex-between"><strong>${escapeHtml(g.description)}</strong>${getPriorityBadge(g.priority)}</div>
                <div style="font-size:0.82rem;color:var(--text-muted)">Employee: ${escapeHtml(g.employeeName || '')} | Deadline: ${formatDate(g.deadline)}</div>
                <div class="mt-1"><span style="font-size:0.78rem">Progress: ${g.progress}%</span><div class="progress-custom mt-1"><div class="progress-bar-custom" style="width:${g.progress}%"></div></div></div>
                <div class="mt-2"><button class="btn-outline-custom" onclick="commentGoal(${g.id})"><i class="fas fa-comment"></i> Comment</button></div>
            </div>`;
        });
        $('#teamGoalsContent').html(html);
    });
}

function handleCreateGoal(e) {
    e.preventDefault();
    const goal = {
        description: $('#goalDescription').val(),
        deadline: $('#goalDeadline').val(),
        priority: $('#goalPriority').val()
    };
    if (!goal.description) {
        showToast('Description is required', 'error');
        return;
    }
    apiCall('/api/goals', 'POST', goal,
        function (resp) {
            showToast('Goal created!', 'success');
            switchTab('myGoals', document.querySelector('.tab-btn'));
            loadMyGoals();
            $('#addGoalForm')[0].reset();
        },
        function (msg) { showToast(msg, 'error'); }
    );
}

function editGoal(id, currentProgress) {
    const progress = prompt('Enter new progress (0-100):', currentProgress);
    if (progress === null) return;
    const val = parseInt(progress);
    if (isNaN(val) || val < 0 || val > 100) { showToast('Invalid progress value', 'error'); return; }
    apiCall('/api/goals/' + id + '/progress', 'PUT', { progress: val },
        function () { showToast('Progress updated', 'success'); loadMyGoals(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

function deleteGoal(id) {
    if (!confirm('Delete this goal?')) return;
    apiCall('/api/goals/' + id, 'DELETE', null,
        function () { showToast('Goal deleted', 'success'); loadMyGoals(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

function commentGoal(id) {
    const comment = prompt('Enter your comment:');
    if (!comment || !comment.trim()) return;
    apiCall('/api/goals/' + id + '/comment', 'PUT', { comment: comment },
        function () { showToast('Comment added', 'success'); loadTeamGoals(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

// ===== Performance Review =====
function loadPerformance() {
    const role = currentUser.role;
    let tabs = '<button class="tab-btn active" onclick="switchTab(\'myReviews\', this)">My Reviews</button>';
    tabs += '<button class="tab-btn" onclick="switchTab(\'createReview\', this)">Create Review</button>';
    if (role === 'MANAGER' || role === 'ADMIN') {
        tabs += '<button class="tab-btn" onclick="switchTab(\'teamReviews\', this)">Team Reviews</button>';
    }
    $('#performanceTabs').html(tabs);
    loadMyReviews();
}

function loadMyReviews() {
    apiCall('/api/performance-reviews/my', 'GET', null, function (data) {
        if (!data || data.length === 0) {
            $('#myReviewsContent').html('<div class="empty-state"><i class="fas fa-chart-line"></i><p>No performance reviews</p></div>');
            return;
        }
        let html = '';
        data.forEach(r => {
            html += `<div class="card-custom mb-2">
                <div class="flex-between"><strong>Review ${r.reviewYear || ''}</strong>${getStatusBadge(r.status)}</div>
                <div class="grid-2 mt-1" style="font-size:0.82rem;color:var(--text-muted)">
                    <div><strong>Deliverables:</strong> ${escapeHtml(r.deliverables || '-')}</div>
                    <div><strong>Accomplishments:</strong> ${escapeHtml(r.accomplishments || '-')}</div>
                    <div><strong>Improvements:</strong> ${escapeHtml(r.improvements || '-')}</div>
                    <div><strong>Self Rating:</strong> ${'⭐'.repeat(r.selfRating || 0)}</div>
                </div>
                ${r.managerFeedback ? `<div class="mt-1" style="font-size:0.82rem;color:var(--info)"><strong>Manager Feedback:</strong> ${escapeHtml(r.managerFeedback)} | Rating: ${'⭐'.repeat(r.managerRating || 0)}</div>` : ''}
                ${r.status === 'DRAFT' ? `<div class="mt-2"><button class="btn-primary-custom" onclick="submitReview(${r.id})"><i class="fas fa-paper-plane"></i> Submit</button></div>` : ''}
            </div>`;
        });
        $('#myReviewsContent').html(html);
    });
}

function loadTeamReviews() {
    apiCall('/api/performance-reviews/team', 'GET', null, function (data) {
        if (!data || data.length === 0) {
            $('#teamReviewsContent').html('<div class="empty-state"><i class="fas fa-users"></i><p>No team reviews</p></div>');
            return;
        }
        let html = '';
        data.forEach(r => {
            html += `<div class="card-custom mb-2">
                <div class="flex-between"><strong>${escapeHtml(r.employeeName || '')} - Review ${r.reviewYear || ''}</strong>${getStatusBadge(r.status)}</div>
                <div class="grid-2 mt-1" style="font-size:0.82rem;color:var(--text-muted)">
                    <div><strong>Deliverables:</strong> ${escapeHtml(r.deliverables || '-')}</div>
                    <div><strong>Accomplishments:</strong> ${escapeHtml(r.accomplishments || '-')}</div>
                    <div><strong>Self Rating:</strong> ${'⭐'.repeat(r.selfRating || 0)}</div>
                </div>
                ${r.status === 'SUBMITTED' ? `<div class="mt-2"><button class="btn-primary-custom" onclick="provideFeedback(${r.id})"><i class="fas fa-comment"></i> Provide Feedback</button></div>` : ''}
                ${r.managerFeedback ? `<div class="mt-1" style="color:var(--success);font-size:0.82rem"><strong>Your Feedback:</strong> ${escapeHtml(r.managerFeedback)} | Rating: ${'⭐'.repeat(r.managerRating || 0)}</div>` : ''}
            </div>`;
        });
        $('#teamReviewsContent').html(html);
    });
}

function handleCreateReview(e) {
    e.preventDefault();
    const review = {
        deliverables: $('#reviewDeliverables').val(),
        accomplishments: $('#reviewAccomplishments').val(),
        improvements: $('#reviewImprovements').val(),
        selfRating: parseInt($('#reviewSelfRating').val()),
        reviewYear: parseInt($('#reviewYear').val()) || new Date().getFullYear()
    };
    if (!review.deliverables || !review.selfRating) {
        showToast('Please fill required fields', 'error');
        return;
    }
    apiCall('/api/performance-reviews', 'POST', review,
        function (resp) {
            showToast('Review created!', 'success');
            switchTab('myReviews', document.querySelector('.tab-btn'));
            loadMyReviews();
            $('#createReviewForm')[0].reset();
        },
        function (msg) { showToast(msg, 'error'); }
    );
}

function submitReview(id) {
    if (!confirm('Submit this review? You cannot edit it after submission.')) return;
    apiCall('/api/performance-reviews/' + id + '/submit', 'PUT', {},
        function () { showToast('Review submitted!', 'success'); loadMyReviews(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

function provideFeedback(id) {
    const rating = prompt('Enter rating (1-5):');
    if (!rating) return;
    const r = parseInt(rating);
    if (isNaN(r) || r < 1 || r > 5) { showToast('Rating must be 1-5', 'error'); return; }
    const feedback = prompt('Enter your feedback:');
    if (!feedback || !feedback.trim()) { showToast('Feedback is required', 'error'); return; }
    apiCall('/api/performance-reviews/' + id + '/feedback', 'PUT', { managerRating: r, managerFeedback: feedback },
        function () { showToast('Feedback submitted!', 'success'); loadTeamReviews(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

// ===== Employee Management =====
function loadEmployees() {
    const role = currentUser.role;
    let tabs = '';
    if (role === 'ADMIN') {
        tabs += '<button class="tab-btn active" onclick="switchTab(\'employeesOnly\', this)"><i class="fas fa-user"></i> Employees</button>';
        tabs += '<button class="tab-btn" onclick="switchTab(\'managersOnly\', this)"><i class="fas fa-user-tie"></i> Managers</button>';
        tabs += '<button class="tab-btn" onclick="switchTab(\'directory\', this)"><i class="fas fa-users"></i> All Users</button>';
        tabs += '<button class="tab-btn" onclick="switchTab(\'addEmployee\', this)"><i class="fas fa-user-plus"></i> Add Employee</button>';
    } else if (role === 'MANAGER') {
        tabs += '<button class="tab-btn active" onclick="switchTab(\'myTeam\', this)"><i class="fas fa-people-group"></i> My Team</button>';
        tabs += '<button class="tab-btn" onclick="switchTab(\'directory\', this)"><i class="fas fa-users"></i> Directory</button>';
    } else {
        tabs += '<button class="tab-btn active" onclick="switchTab(\'directory\', this)">Directory</button>';
    }
    $('#employeeTabs').html(tabs);

    // Switch to the correct tab content div
    $('.tab-content').removeClass('active');
    if (role === 'ADMIN') {
        $('#employeesOnly').addClass('active');
        loadEmployeesOnly();
    } else if (role === 'MANAGER') {
        $('#myTeam').addClass('active');
        loadMyTeam();
    } else {
        $('#directory').addClass('active');
        loadEmployeeDirectory();
    }
}

function loadEmployeesOnly(search) {
    const url = '/api/employees/directory' + (search ? '?search=' + encodeURIComponent(search) : '');
    apiCall(url, 'GET', null, function (data) {
        // Filter to show only EMPLOYEE role
        const employees = data.filter(e => e.role === 'EMPLOYEE');
        if (!employees || employees.length === 0) {
            $('#employeesOnlyTable').html('<div class="empty-state"><i class="fas fa-user"></i><p>No employees found</p></div>');
            return;
        }
        let html = '<table class="table-custom"><thead><tr><th>Emp ID</th><th>Name</th><th>Email</th><th>Department</th><th>Designation</th><th>Phone</th><th>Status</th><th>Actions</th></tr></thead><tbody>';
        employees.forEach(e => {
            html += `<tr><td>${e.employeeId}</td><td>${escapeHtml(e.name)}</td><td>${e.email}</td><td>${e.department}</td><td>${e.designation}</td><td>${e.phone || '-'}</td>`;
            html += `<td>${e.active ? '<span class="badge-custom badge-active">Active</span>' : '<span class="badge-custom badge-inactive">Inactive</span>'}</td>`;
            html += `<td>
                <button class="btn-outline-custom" onclick="editEmployee(${e.id})" title="Edit"><i class="fas fa-edit"></i></button>
                ${e.active
                    ? `<button class="btn-danger-custom" onclick="deactivateEmployee(${e.id})" title="Deactivate"><i class="fas fa-ban"></i></button>`
                    : `<button class="btn-success-custom" onclick="reactivateEmployee(${e.id})" title="Reactivate"><i class="fas fa-check"></i></button>`}
            </td>`;
            html += '</tr>';
        });
        html += '</tbody></table>';
        $('#employeesOnlyTable').html(html);
    });
}

function loadManagersOnly(search) {
    const url = '/api/employees/directory' + (search ? '?search=' + encodeURIComponent(search) : '');
    apiCall(url, 'GET', null, function (data) {
        // Filter to show only MANAGER role
        const managers = data.filter(e => e.role === 'MANAGER');
        if (!managers || managers.length === 0) {
            $('#managersOnlyTable').html('<div class="empty-state"><i class="fas fa-user-tie"></i><p>No managers found</p></div>');
            return;
        }
        let html = '<table class="table-custom"><thead><tr><th>Emp ID</th><th>Name</th><th>Email</th><th>Department</th><th>Designation</th><th>Phone</th><th>Status</th><th>Actions</th></tr></thead><tbody>';
        managers.forEach(e => {
            html += `<tr><td>${e.employeeId}</td><td>${escapeHtml(e.name)}</td><td>${e.email}</td><td>${e.department}</td><td>${e.designation}</td><td>${e.phone || '-'}</td>`;
            html += `<td>${e.active ? '<span class="badge-custom badge-active">Active</span>' : '<span class="badge-custom badge-inactive">Inactive</span>'}</td>`;
            html += `<td>
                <button class="btn-outline-custom" onclick="editEmployee(${e.id})" title="Edit"><i class="fas fa-edit"></i></button>
                ${e.active
                    ? `<button class="btn-danger-custom" onclick="deactivateEmployee(${e.id})" title="Deactivate"><i class="fas fa-ban"></i></button>`
                    : `<button class="btn-success-custom" onclick="reactivateEmployee(${e.id})" title="Reactivate"><i class="fas fa-check"></i></button>`}
            </td>`;
            html += '</tr>';
        });
        html += '</tbody></table>';
        $('#managersOnlyTable').html(html);
    });
}

function loadMyTeam() {
    apiCall('/api/employees/team', 'GET', null, function (data) {
        if (!data || data.length === 0) {
            $('#myTeamTable').html('<div class="empty-state"><i class="fas fa-people-group"></i><p>No team members assigned to you</p></div>');
            return;
        }
        let html = '<table class="table-custom"><thead><tr><th>Emp ID</th><th>Name</th><th>Email</th><th>Department</th><th>Designation</th><th>Phone</th></tr></thead><tbody>';
        data.forEach(e => {
            html += `<tr><td>${e.employeeId}</td><td>${escapeHtml(e.name)}</td><td>${e.email}</td><td>${e.department}</td><td>${e.designation}</td><td>${e.phone || '-'}</td></tr>`;
        });
        html += '</tbody></table>';
        $('#myTeamTable').html(html);
    });
}

function loadEmployeeDirectory(search) {
    const url = '/api/employees/directory' + (search ? '?search=' + encodeURIComponent(search) : '');
    apiCall(url, 'GET', null, function (data) {
        if (!data || data.length === 0) {
            $('#employeeTable').html('<div class="empty-state"><i class="fas fa-users"></i><p>No employees found</p></div>');
            return;
        }
        let html = '<table class="table-custom"><thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Department</th><th>Designation</th><th>Role</th>';
        if (currentUser.role === 'ADMIN') html += '<th>Status</th><th>Actions</th>';
        html += '</tr></thead><tbody>';
        data.forEach(e => {
            html += `<tr><td>${e.employeeId}</td><td>${escapeHtml(e.name)}</td><td>${e.email}</td><td>${e.department}</td><td>${e.designation}</td><td>${getRoleBadge(e.role)}</td>`;
            if (currentUser.role === 'ADMIN') {
                html += `<td>${e.active ? '<span class="badge-custom badge-active">Active</span>' : '<span class="badge-custom badge-inactive">Inactive</span>'}</td>`;
                html += `<td>
                    <button class="btn-outline-custom" onclick="editEmployee(${e.id})" title="Edit"><i class="fas fa-edit"></i></button>
                    ${e.active
                        ? `<button class="btn-danger-custom" onclick="deactivateEmployee(${e.id})" title="Deactivate"><i class="fas fa-ban"></i></button>`
                        : `<button class="btn-success-custom" onclick="reactivateEmployee(${e.id})" title="Reactivate"><i class="fas fa-check"></i></button>`}
                </td>`;
            }
            html += '</tr>';
        });
        html += '</tbody></table>';
        $('#employeeTable').html(html);
    });
}

function handleAddEmployee(e) {
    e.preventDefault();
    const phone = $('#empPhone').val().trim();
    if (!phone || !/^[0-9]{10}$/.test(phone)) {
        showToast('Phone number is mandatory and must be exactly 10 digits', 'error');
        $('#empPhone').focus();
        return;
    }
    const user = {
        name: $('#empName').val(),
        email: $('#empEmail').val(),
        password: $('#empPassword').val(),
        employeeId: $('#empId').val(),
        role: $('#empRole').val(),
        department: $('#empDepartment').val(),
        designation: $('#empDesignation').val(),
        phone: phone,
        salary: parseFloat($('#empSalary').val()) || null,
        managerId: parseInt($('#empManager').val()) || null
    };
    apiCall('/api/admin/employees', 'POST', user,
        function (resp) {
            showToast('Employee added!', 'success');
            switchTab('directory', document.querySelector('.tab-btn'));
            loadEmployeeDirectory();
            $('#addEmployeeForm')[0].reset();
        },
        function (msg) { showToast(msg, 'error'); }
    );
}

function editEmployee(id) {
    apiCall('/api/employees/' + id, 'GET', null, function (emp) {
        const html = `<div class="modal-overlay" id="editModal" onclick="if(event.target===this)this.remove()">
            <div class="modal-content">
                <h3><i class="fas fa-edit"></i> Edit Employee</h3>
                <form id="editEmployeeForm" onsubmit="handleEditEmployee(event, ${id})">
                    <div class="form-floating-custom"><label class="form-label-custom">Name</label><input class="form-control-custom" id="editName" value="${escapeHtml(emp.name)}"></div>
                    <div class="form-floating-custom"><label class="form-label-custom">Department</label><input class="form-control-custom" id="editDept" value="${escapeHtml(emp.department)}"></div>
                    <div class="form-floating-custom"><label class="form-label-custom">Designation</label><input class="form-control-custom" id="editDesig" value="${escapeHtml(emp.designation)}"></div>
                    <div class="form-floating-custom"><label class="form-label-custom">Phone</label><input class="form-control-custom" id="editPhone" value="${escapeHtml(emp.phone)}"></div>
                    <div class="form-floating-custom"><label class="form-label-custom">Salary</label><input class="form-control-custom" id="editSalary" type="number" value="${emp.salary || ''}"></div>
                    <div class="form-floating-custom"><label class="form-label-custom">Role</label>
                        <select class="form-control-custom" id="editRole">
                            <option value="EMPLOYEE" ${emp.role === 'EMPLOYEE' ? 'selected' : ''}>Employee</option>
                            <option value="MANAGER" ${emp.role === 'MANAGER' ? 'selected' : ''}>Manager</option>
                            <option value="ADMIN" ${emp.role === 'ADMIN' ? 'selected' : ''}>Admin</option>
                        </select>
                    </div>
                    <div class="form-floating-custom"><label class="form-label-custom">Assign Manager</label>
                        <select class="form-control-custom" id="editManager">
                            <option value="">No Manager</option>
                        </select>
                    </div>
                    <div class="flex gap-1 mt-2">
                        <button type="submit" class="btn-primary-custom"><i class="fas fa-save"></i> Save</button>
                        <button type="button" class="btn-outline-custom" onclick="document.getElementById('editModal').remove()">Cancel</button>
                    </div>
                </form>
            </div>
        </div>`;
        $('body').append(html);
        // Load manager options and pre-select current manager
        apiCall('/api/employees/managers', 'GET', null, function (managers) {
            let opts = '<option value="">No Manager</option>';
            managers.forEach(m => {
                const selected = (emp.managerId && emp.managerId == m.id) ? 'selected' : '';
                opts += `<option value="${m.id}" ${selected}>${escapeHtml(m.name)} (${escapeHtml(m.email)})</option>`;
            });
            $('#editManager').html(opts);
        });
    });
}

function handleEditEmployee(e, id) {
    e.preventDefault();
    const phone = $('#editPhone').val() ? $('#editPhone').val().trim() : '';
    if (!phone || !/^[0-9]{10}$/.test(phone)) {
        showToast('Phone number is mandatory and must be exactly 10 digits', 'error');
        $('#editPhone').focus();
        return;
    }
    const data = {
        name: $('#editName').val(),
        department: $('#editDept').val(),
        designation: $('#editDesig').val(),
        phone: phone,
        salary: parseFloat($('#editSalary').val()) || null,
        role: $('#editRole').val(),
        managerId: $('#editManager').val() ? parseInt($('#editManager').val()) : null
    };
    apiCall('/api/admin/employees/' + id, 'PUT', data,
        function () {
            showToast('Employee updated!', 'success');
            $('#editModal').remove();
            refreshEmployeeTabs();
        },
        function (msg) { showToast(msg, 'error'); }
    );
}

function deactivateEmployee(id) {
    if (!confirm('Deactivate this employee?')) return;
    apiCall('/api/admin/employees/' + id + '/deactivate', 'PUT', {},
        function () { showToast('Employee deactivated', 'success'); refreshEmployeeTabs(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

function reactivateEmployee(id) {
    if (!confirm('Reactivate this employee?')) return;
    apiCall('/api/admin/employees/' + id + '/reactivate', 'PUT', {},
        function () { showToast('Employee reactivated', 'success'); refreshEmployeeTabs(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

function refreshEmployeeTabs() {
    // Refresh whichever tab is currently active
    if ($('#employeesOnly').hasClass('active')) loadEmployeesOnly();
    else if ($('#managersOnly').hasClass('active')) loadManagersOnly();
    else if ($('#myTeam').hasClass('active')) loadMyTeam();
    else if ($('#directory').hasClass('active')) loadEmployeeDirectory();
}

// ===== Profile =====
function loadProfile() {
    apiCall('/api/employees/me', 'GET', null, function (data) {
        $('#profileName').text(data.name);
        $('#profileEmail').text(data.email);
        $('#profileEmpId').text(data.employeeId);
        $('#profileRole').html(getRoleBadge(data.role));
        $('#profileDept').text(data.department || '-');
        $('#profileDesig').text(data.designation || '-');
        $('#profileJoining').text(formatDate(data.joiningDate));
        $('#profilePhone').val(data.phone || '');
        $('#profileAddress').val(data.address || '');
        $('#profileEmergency').val(data.emergencyContact || '');
        if (data.managerName) {
            $('#profileManager').text(data.managerName + ' (' + data.managerEmail + ')');
        } else {
            $('#profileManager').text('-');
        }
    });
}

function handleUpdateProfile(e) {
    e.preventDefault();
    const data = {
        phone: $('#profilePhone').val(),
        address: $('#profileAddress').val(),
        emergencyContact: $('#profileEmergency').val()
    };
    apiCall('/api/employees/me', 'PUT', data,
        function () { showToast('Profile updated!', 'success'); loadProfile(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

// ===== Announcements =====
function loadAnnouncements() {
    apiCall('/api/announcements', 'GET', null, function (data) {
        if (!data || data.length === 0) {
            $('#announcementsContent').html('<div class="empty-state"><i class="fas fa-bullhorn"></i><p>No announcements</p></div>');
            return;
        }
        let html = '';
        data.forEach(a => {
            html += `<div class="card-custom mb-2">
                <div class="flex-between"><strong>${escapeHtml(a.title)}</strong><small style="color:var(--text-muted)">${formatDate(a.createdDate || a.date)}</small></div>
                <p style="color:var(--text-muted);font-size:0.85rem;margin-top:0.5rem">${escapeHtml(a.description || '')}</p>
                ${currentUser.role === 'ADMIN' ? `<div class="mt-1"><button class="btn-danger-custom" onclick="deleteAnnouncement(${a.id})"><i class="fas fa-trash"></i> Delete</button></div>` : ''}
            </div>`;
        });
        $('#announcementsContent').html(html);
    });
    // Show add form for admin
    if (currentUser.role === 'ADMIN') {
        $('#addAnnouncementSection').show();
    }
}

function handleAddAnnouncement(e) {
    e.preventDefault();
    const data = { title: $('#annTitle').val(), description: $('#annDescription').val() };
    apiCall('/api/announcements', 'POST', data,
        function () { showToast('Announcement posted!', 'success'); loadAnnouncements(); $('#addAnnouncementForm')[0].reset(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

function deleteAnnouncement(id) {
    if (!confirm('Delete this announcement?')) return;
    apiCall('/api/announcements/' + id, 'DELETE', null,
        function () { showToast('Announcement deleted', 'success'); loadAnnouncements(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

// ===== Holidays =====
function loadHolidays() {
    const url = currentUser.role === 'ADMIN' ? '/api/admin/holidays' : '/api/holidays';
    apiCall(url, 'GET', null, function (data) {
        if (!data || data.length === 0) {
            $('#holidaysContent').html('<div class="empty-state"><i class="fas fa-umbrella-beach"></i><p>No holidays</p></div>');
            return;
        }
        let html = '<table class="table-custom"><thead><tr><th>Holiday</th><th>Date</th>';
        if (currentUser.role === 'ADMIN') html += '<th>Actions</th>';
        html += '</tr></thead><tbody>';
        data.forEach(h => {
            html += `<tr><td>${escapeHtml(h.name)}</td><td>${formatDate(h.holidayDate || h.date)}</td>`;
            if (currentUser.role === 'ADMIN') {
                html += `<td><button class="btn-danger-custom" onclick="deleteHoliday(${h.id})"><i class="fas fa-trash"></i></button></td>`;
            }
            html += '</tr>';
        });
        html += '</tbody></table>';
        $('#holidaysContent').html(html);
    });
    if (currentUser.role === 'ADMIN') {
        $('#addHolidaySection').show();
    }
}

function handleAddHoliday(e) {
    e.preventDefault();
    const data = { name: $('#holidayName').val(), holidayDate: $('#holidayDate').val() };
    apiCall('/api/admin/holidays', 'POST', data,
        function () { showToast('Holiday added!', 'success'); loadHolidays(); $('#addHolidayForm')[0].reset(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

function deleteHoliday(id) {
    if (!confirm('Delete this holiday?')) return;
    apiCall('/api/admin/holidays/' + id, 'DELETE', null,
        function () { showToast('Holiday deleted', 'success'); loadHolidays(); },
        function (msg) { showToast(msg, 'error'); }
    );
}

// ===== Tab Switching =====
function switchTab(tabId, btn) {
    $('.tab-content').removeClass('active');
    $('.tab-btn').removeClass('active');
    $('#' + tabId).addClass('active');
    $(btn).addClass('active');
    // Auto-load data for certain tabs
    if (tabId === 'teamLeaves') loadTeamLeaves();
    if (tabId === 'allLeaves') loadAllLeaves();
    if (tabId === 'leaveTypes') loadLeaveTypes();
    if (tabId === 'teamGoals') loadTeamGoals();
    if (tabId === 'teamReviews') loadTeamReviews();
    if (tabId === 'leaveBalance') loadLeaveBalance();
    if (tabId === 'employeesOnly') loadEmployeesOnly();
    if (tabId === 'managersOnly') loadManagersOnly();
    if (tabId === 'directory') loadEmployeeDirectory();
    if (tabId === 'myTeam') loadMyTeam();
}

// ===== Notification Toggle =====
function toggleNotifications() {
    const dropdown = document.getElementById('notifDropdown');
    if (dropdown.classList.contains('show')) {
        dropdown.classList.remove('show');
        return;
    }
    dropdown.classList.add('show');
    apiCall('/api/employees/notifications', 'GET', null, function (data) {
        if (!data || data.length === 0) {
            dropdown.innerHTML = '<div class="notification-item">No notifications</div>';
            return;
        }
        let html = `<div style="padding:0.5rem 1rem;border-bottom:1px solid var(--border);display:flex;justify-content:space-between"><strong style="font-size:0.8rem">Notifications</strong><button class="btn-outline-custom" style="padding:2px 8px;font-size:0.7rem" onclick="markAllRead()">Read All</button></div>`;
        data.slice(0, 20).forEach(n => {
            html += `<div class="notification-item ${!n.read ? 'unread' : ''}" onclick="markNotifRead(${n.id})">
                <div>${escapeHtml(n.message)}</div>
                <div class="notification-time">${n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}</div>
            </div>`;
        });
        dropdown.innerHTML = html;
    });
}

function markNotifRead(id) {
    apiCall('/api/employees/notifications/' + id + '/read', 'PUT', {}, function () { loadNotificationCount(); });
}

function markAllRead() {
    apiCall('/api/employees/notifications/read-all', 'PUT', {}, function () {
        loadNotificationCount();
        $('#notifDropdown').removeClass('show');
        showToast('All notifications marked as read', 'success');
    });
}

// Load managers for admin add employee form
function loadManagerOptions() {
    apiCall('/api/employees/managers', 'GET', null, function (data) {
        let opts = '<option value="">No Manager</option>';
        data.forEach(m => { opts += `<option value="${m.id}">${m.name}</option>`; });
        $('#empManager').html(opts);
    });
}

// Load departments for dropdowns
function loadDepartmentOptions() {
    apiCall('/api/admin/departments', 'GET', null, function (data) {
        let opts = '<option value="">Select Department</option>';
        data.forEach(d => { opts += `<option value="${d.name}">${d.name}</option>`; });
        $('.dept-select').each(function () { $(this).html(opts); });
    });
}

// ===== Initialize =====
$(document).ready(function () {
    checkSession();

    // Search functionality
    $(document).on('input', '#employeeSearch', function () {
        loadEmployeeDirectory($(this).val());
    });
    $(document).on('input', '#employeeSearchEmp', function () {
        loadEmployeesOnly($(this).val());
    });
    $(document).on('input', '#employeeSearchMgr', function () {
        loadManagersOnly($(this).val());
    });

    // Close notification dropdown on outside click
    $(document).on('click', function (e) {
        if (!$(e.target).closest('.notification-badge, #notifDropdown').length) {
            $('#notifDropdown').removeClass('show');
        }
    });
});
