// Import Alpine.js
import Alpine from 'alpinejs';

// =====================================================
// Utility Functions
// =====================================================

// Format currency to IDR
window.formatCurrency = (amount) => {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0
  }).format(amount);
};

// Format date
window.formatDate = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('id-ID', {
    day: '2-digit',
    month: 'long',
    year: 'numeric'
  }).format(date);
};

// Format date time
window.formatDateTime = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('id-ID', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
};

// Debounce function
window.debounce = (func, wait) => {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
};

// =====================================================
// Alpine.js Components
// =====================================================

// Toast Notification
Alpine.data('toast', (options = {}) => ({
  show: false,
  message: options.message || '',
  type: options.type || 'info', // success, error, warning, info
  duration: options.duration || 3000,

  init() {
    if (this.message) {
      this.show = true;
      setTimeout(() => {
        this.show = false;
      }, this.duration);
    }
  },

  close() {
    this.show = false;
  }
}));

// Modal
Alpine.data('modal', (options = {}) => ({
  open: false,
  title: options.title || '',
  size: options.size || 'md', // sm, md, lg, xl

  init() {
    if (options.open) {
      this.open = true;
    }
  },

  close() {
    this.open = false;
  },

  onBackdropClick(e) {
    if (e.target === e.currentTarget) {
      this.close();
    }
  }
}));

// Confirm Dialog
Alpine.data('confirm', (options = {}) => ({
  show: false,
  title: options.title || 'Konfirmasi',
  message: options.message || 'Apakah Anda yakin?',
  confirmText: options.confirmText || 'Ya',
  cancelText: options.cancelText || 'Batal',
  onConfirm: options.onConfirm || (() => {}),
  onCancel: options.onCancel || (() => {}),

  confirm() {
    this.onConfirm();
    this.show = false;
  },

  cancel() {
    this.onCancel();
    this.show = false;
  }
}));

// Form Validation
Alpine.data('form', (options = {}) => ({
  data: options.data || {},
  errors: {},
  submitting: false,

  validate(rules = {}) {
    this.errors = {};

    for (const [field, rule] of Object.entries(rules)) {
      const value = this.data[field];

      // Required
      if (rule.required && !value) {
        this.errors[field] = rule.message || `${field} wajib diisi`;
        continue;
      }

      // Email
      if (rule.email && value) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(value)) {
          this.errors[field] = rule.message || 'Email tidak valid';
        }
      }

      // Min length
      if (rule.minLength && value && value.length < rule.minLength) {
        this.errors[field] = rule.message || `${field} minimal ${rule.minLength} karakter`;
      }

      // Max length
      if (rule.maxLength && value && value.length > rule.maxLength) {
        this.errors[field] = rule.message || `${field} maksimal ${rule.maxLength} karakter`;
      }

      // Custom validation
      if (rule.validate && value) {
        const result = rule.validate(value);
        if (result !== true) {
          this.errors[field] = result || rule.message || `${field} tidak valid`;
        }
      }
    }

    return Object.keys(this.errors).length === 0;
  },

  clearErrors() {
    this.errors = {};
  },

  reset() {
    this.data = options.data || {};
    this.errors = {};
    this.submitting = false;
  }
}));

// Dropdown
Alpine.data('dropdown', () => ({
  open: false,

  toggle() {
    this.open = !this.open;
  },

  close() {
    this.open = false;
  }
}));

// Tabs
Alpine.data('tabs', (options = {}) => ({
  activeTab: options.active || 0,

  tabs: options.tabs || [],

  setActive(index) {
    this.activeTab = index;
  }
}));

// Data Table with Search & Pagination
Alpine.data('datatable', (options = {}) => ({
  data: options.data || [],
  columns: options.columns || [],
  search: '',
  page: 1,
  perPage: options.perPage || 10,
  sort: {
    field: options.sortField || 'id',
    direction: options.sortDirection || 'asc'
  },

  get filteredData() {
    let filtered = this.data;

    // Search
    if (this.search) {
      const searchLower = this.search.toLowerCase();
      filtered = filtered.filter(row => {
        return this.columns.some(col => {
          const value = row[col.field];
          return value && value.toString().toLowerCase().includes(searchLower);
        });
      });
    }

    // Sort
    if (this.sort.field) {
      filtered.sort((a, b) => {
        const aVal = a[this.sort.field];
        const bVal = b[this.sort.field];

        if (aVal === bVal) return 0;

        const comparison = aVal < bVal ? -1 : 1;
        return this.sort.direction === 'asc' ? comparison : -comparison;
      });
    }

    return filtered;
  },

  get paginatedData() {
    const start = (this.page - 1) * this.perPage;
    const end = start + this.perPage;
    return this.filteredData.slice(start, end);
  },

  get totalPages() {
    return Math.ceil(this.filteredData.length / this.perPage);
  },

  sortBy(field) {
    if (this.sort.field === field) {
      this.sort.direction = this.sort.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sort.field = field;
      this.sort.direction = 'asc';
    }
  },

  nextPage() {
    if (this.page < this.totalPages) {
      this.page++;
    }
  },

  prevPage() {
    if (this.page > 1) {
      this.page--;
    }
  },

  goToPage(page) {
    this.page = page;
  }
}));

// Clock In/Out Widget
Alpine.data('clockWidget', () => ({
  currentTime: '',
  currentDate: '',
  clockedIn: false,

  init() {
    this.updateTime();
    setInterval(() => this.updateTime(), 1000);

    // Check if already clocked in today
    this.checkClockStatus();
  },

  updateTime() {
    const now = new Date();
    this.currentTime = now.toLocaleTimeString('id-ID', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
    this.currentDate = now.toLocaleDateString('id-ID', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  },

  async checkClockStatus() {
    // This would be implemented with actual API call
    // For now, using localStorage
    const today = new Date().toDateString();
    const clockedIn = localStorage.getItem(`clocked_in_${today}`);
    this.clockedIn = clockedIn === 'true';
  },

  async clockIn() {
    // API call to clock in
    const today = new Date().toDateString();
    localStorage.setItem(`clocked_in_${today}`, 'true');
    this.clockedIn = true;
  },

  async clockOut() {
    // API call to clock out
    const today = new Date().toDateString();
    localStorage.setItem(`clocked_in_${today}`, 'false');
    this.clockedIn = false;
  }
}));

// File Upload
Alpine.data('fileUpload', (options = {}) => ({
  files: [],
  dragging: false,
  maxSize: options.maxSize || 2 * 1024 * 1024, // 2MB default
  accept: options.accept || '*',

  onDragOver() {
    this.dragging = true;
  },

  onDragLeave() {
    this.dragging = false;
  },

  onDrop(e) {
    this.dragging = false;
    this.addFiles(e.dataTransfer.files);
  },

  onFileSelect(e) {
    this.addFiles(e.target.files);
  },

  addFiles(fileList) {
    for (const file of fileList) {
      if (file.size > this.maxSize) {
        alert(`File ${file.name} terlalu besar. Maksimal ${this.formatSize(this.maxSize)}`);
        continue;
      }

      this.files.push({
        name: file.name,
        size: file.size,
        type: file.type,
        file: file
      });
    }
  },

  removeFile(index) {
    this.files.splice(index, 1);
  },

  formatSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  },

  clear() {
    this.files = [];
  }
}));

// =====================================================
// Global Stores
// =====================================================

// Auth Store
Alpine.store('auth', {
  user: null,
  roles: [],
  selectedRole: null,

  init() {
    // Load from localStorage or session
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      this.user = JSON.parse(storedUser);
    }

    const storedRoles = localStorage.getItem('roles');
    if (storedRoles) {
      this.roles = JSON.parse(storedRoles);
    }

    const storedRole = localStorage.getItem('selectedRole');
    if (storedRole) {
      this.selectedRole = storedRole;
    }
  },

  setUser(user) {
    this.user = user;
    localStorage.setItem('user', JSON.stringify(user));
  },

  setRoles(roles) {
    this.roles = roles;
    localStorage.setItem('roles', JSON.stringify(roles));
  },

  setSelectedRole(role) {
    this.selectedRole = role;
    localStorage.setItem('selectedRole', role);
  },

  logout() {
    this.user = null;
    this.roles = [];
    this.selectedRole = null;
    localStorage.removeItem('user');
    localStorage.removeItem('roles');
    localStorage.removeItem('selectedRole');
  }
});

// =====================================================
// Initialize Alpine.js
// =====================================================
window.Alpine = Alpine;
Alpine.start();
