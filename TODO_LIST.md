# TODO LIST - Implementasi HRIS Sederhana

> Dokumen ini berisi checklist tugas untuk implementasi aplikasi HRIS Sederhana
> Berdasarkan RANCANGAN.md, BUSINESS_REQUIREMENT.md, FRS_FUNCTIONAL_REQUIREMENT.md, dan IMPLEMENTATION_PLAN.md

---

## Progress Overview

| Phase | Sprint | Status | Progress |
|-------|--------|--------|----------|
| Phase 1: Foundation | Sprint 1-2 | ✅ Completed | 100% |
| Phase 2: Master Data | Sprint 3-4 | 🔄 In Progress | 40% |
| Phase 3: Lecturer Data | Sprint 5-6 | ⏸️ Not Started | 0% |
| Phase 4: Attendance | Sprint 7 | ⏸️ Not Started | 0% |
| Phase 5: Leave Management | Sprint 8 | ⏸️ Not Started | 0% |
| Phase 6: Payroll Karyawan | Sprint 9 | ⏸️ Not Started | 0% |
| Phase 7: Payroll Dosen | Sprint 10 | ⏸️ Not Started | 0% |
| Phase 8: Activity Logging | Sprint 11 | ⏸️ Not Started | 0% |
| Phase 9: Testing & UAT | Sprint 12 | ⏸️ Not Started | 0% |

---

## Phase 1: Foundation (Sprint 1-2)

### Sprint 1: Foundation & Authentication ✅ COMPLETED

#### Backend Setup
- [x] **S1-01**: Initialize SpringBoot project ✅
  - [x] Create Maven project with SpringBoot 3.x ✅
  - [x] Add dependencies (Web, Data JPA, Thymeleaf, Validation, MySQL, Lombok) ✅
  - [x] Setup folder structure (com.hris.*) ✅
  - [x] Configure application.yml (database, server port, file upload) ✅
  - [x] Create HrisApplication.java ✅

- [x] **S1-02**: Setup database & Flyway ✅
  - [x] Create MySQL database: hris_db ✅
  - [x] Configure datasource in application.yml ✅
  - [x] Add Flyway dependency ✅
  - [x] Create db/migration folder ✅
  - [x] Create V1__init_schema.sql, V2__insert_default_data.sql, V3__insert_all_roles_user.sql, V4__fix_passwords.sql ✅

- [x] **S1-03**: Implement Spring Security ✅
  - [x] Add Spring Security dependency ✅
  - [x] Create SecurityConfig.java ✅
  - [x] Configure password encoder (BCrypt) ✅
  - [x] Create CustomUserDetailsService implementation ✅
  - [x] Create CustomAuthenticationProvider ✅
  - [x] Configure authentication rules ✅

#### Frontend Setup
- [x] **S1-04**: Create base layout template ✅
  - [x] Create templates/layout/main.html (Glassmorphism design) ✅
  - [x] Create templates/fragments/header.html ✅
  - [x] Create templates/fragments/sidebar.html ✅
  - [x] Create templates/fragments/footer.html ✅
  - [x] Setup Tailwind CSS (PostCSS CLI build) ✅
  - [x] Setup Alpine.js (esbuild) ✅

- [x] **S1-05**: Implement Login page ✅
  - [x] Create templates/auth/login.html ✅
  - [x] Create AuthController.java (login, logout) ✅
  - [x] Implement login validation ✅
  - [x] Add error handling ✅

- [x] **S1-06**: Implement Role Selection page ✅
  - [x] Create templates/auth/role-selection.html ✅
  - [x] Create role selection logic in AuthController ✅
  - [x] Store selected_role in session ✅

- [x] **S1-07**: Implement UserSession & session management ✅
  - [x] Configure session timeout (30 minutes) ✅
  - [x] Add session timeout handling ✅

#### Testing
- [x] **S1-08**: Unit testing ✅
  - [x] Test authentication flow (login with admin & allroles users) ✅
  - [x] Test role selection ✅
  - [x] Test session management ✅
  - [x] Test password hashing ✅

- [x] **S1-09**: Code review & refinement ✅

---

### Sprint 2: Dashboard & User Management ✅ COMPLETED

#### Base Entities & Enums
- [x] **S2-01**: Create Base Entities ✅
  - [x] Create BaseEntity.java (created_at, updated_at) ✅
  - [x] Create SoftDeleteEntity.java (deleted_at, deleted_by) ✅
  - [x] Create AuditableEntity.java (extends BaseEntity + created_by, updated_by) ✅

- [x] **S2-02**: Create Enums ✅
  - [x] RoleType.java (ADMIN, HR, EMPLOYEE, DOSEN) ✅
  - [x] Gender.java (MALE, FEMALE) ✅
  - [x] EmployeeStatus.java (ACTIVE, INACTIVE, RESIGNED) ✅
  - [x] EmploymentStatus.java (PERMANENT, CONTRACT, PROBATION, DAILY) ✅
  - [x] MaritalStatus.java (SINGLE, MARRIED, DIVORCED, WIDOWED) ✅
  - [x] CompanyType.java (COMPANY, UNIVERSITY, SCHOOL, OTHER) ✅
  - [x] LecturerEmploymentStatus.java (DOSEN_TETAP, DOSEN_TIDAK_TETAP) ✅
  - [x] LecturerWorkStatus.java (ACTIVE, LEAVE, RETIRED) ✅
  - [x] LecturerRank.java (ASISTEN_AHLI, LEKTOR, LEKTOR_KEPALA, PROFESOR) ✅
  - [x] AttendanceStatus.java (PRESENT, LATE, LEAVE, SICK, ABSENT) ✅
  - [x] LeaveType.java (ANNUAL, SICK, MATERNITY, MARRIAGE, SPECIAL, UNPAID) ✅
  - [x] LeaveRequestStatus.java (PENDING, APPROVED, REJECTED) ✅
  - [x] PayrollStatus.java (DRAFT, PAID) ✅
  - [x] ActivityType.java (14 activity types) ✅

#### Dashboard
- [x] **S2-03**: Create DashboardController ✅
  - [x] Implement getDashboardPage() based on role ✅
  - [x] Create DashboardService ✅

- [x] **S2-04**: Implement Admin Dashboard ✅
  - [x] Create templates/dashboard/admin.html ✅
  - [x] Stat cards: total employees, total lecturers, pending leaves ✅
  - [x] Quick actions menu ✅
  - [x] Recent activities table ✅

- [x] **S2-05**: Implement HR Dashboard ✅
  - [x] Create templates/dashboard/hr.html ✅
  - [x] Stat cards specific for HR ✅
  - [x] Pending approvals section ✅

- [x] **S2-06**: Implement Employee Dashboard ✅
  - [x] Create templates/dashboard/employee.html ✅
  - [x] Clock in/out buttons ✅
  - [x] My attendance summary ✅
  - [x] My leave balance ✅

- [x] **S2-07**: Implement Dosen Dashboard ✅
  - [x] Create templates/dashboard/dosen.html ✅
  - [x] Similar to employee + lecturer specific info ✅

#### User Management
- [x] **S2-08**: Create User models ✅
  - [x] Create Employee.java (with all fields from RANCANGAN.md) ✅
  - [x] Create EmployeeRole.java ✅
  - [x] Create repositories: EmployeeRepository, EmployeeRoleRepository ✅

- [x] **S2-09**: Implement User Management (Admin only) ✅
  - [x] Create UserService ✅
  - [x] Create UserController ✅
  - [x] Templates: user list, user form, assign role modal ✅
  - [x] Create user, edit user, assign roles, reset password, activate/deactivate ✅

- [x] **S2-10**: Unit testing & integration test ✅

---

## Phase 2: Master Data (Sprint 3-4) 🔄 IN PROGRESS

### Sprint 3: Company & Department Management 🔄 IN PROGRESS

#### Company Management
- [ ] **S3-01**: Create Company model (TODO)
  - [ ] Create Company.java (singleton, all fields from RANCANGAN.md)
  - [ ] Create CompanyRepository
  - [ ] Create CompanyService
  - [ ] Create CompanyController

- [ ] **S3-02**: Implement Company Profile page (TODO)
  - [ ] Create templates/company/profile.html (view only)
  - [ ] Display company information
  - [ ] Show working hours configuration

- [ ] **S3-03**: Implement Company Edit page (TODO)
  - [ ] Create templates/company/edit.html
  - [ ] Form to edit company data
  - [ ] Logo upload functionality
  - [ ] Stamp upload functionality

- [ ] **S3-04**: Initialize default company data (TODO)
  - [ ] Create Flyway migration to insert default company
  - [ ] Add validation: only 1 company record allowed

#### Department Management ✅ PARTIALLY COMPLETED
- [x] **S3-05**: Create Department model ✅
  - [x] Create Department.java (with is_prodi, kode_prodi) ✅
  - [x] Add parent_id for hierarchical structure ✅
  - [x] Add head_id for department head ✅
  - [x] Add helper methods (getParentChain, getLevel, etc.) ✅
  - [x] Create DepartmentRepository ✅
  - [ ] Create DepartmentService (TODO)
  - [ ] Create DepartmentController (TODO)

- [ ] **S3-06**: Implement Department CRUD (TODO)
  - [ ] Create templates/department/list.html (tree view)
  - [ ] Create templates/department/form.html
  - [ ] List departments with employee count
  - [ ] Create, edit, delete (soft delete) department
  - [ ] Mark department as Prodi (is_prodi flag)
  - [ ] Set parent department
  - [ ] Set department head

- [ ] **S3-07**: Unit testing (TODO)

---

### Sprint 4: Position & Employee Management 🔄 PARTIALLY COMPLETED

#### Position Management ✅ PARTIALLY COMPLETED
- [x] **S4-01**: Create Position model ✅
  - [x] Create Position.java (with base_salary) ✅
  - [x] Add level field (1-6 hierarchy) ✅
  - [x] Add helper methods (isAtLeast, isAtMost, getLevelName) ✅
  - [x] Add Level enum ✅
  - [ ] Create PositionRepository (TODO)
  - [ ] Create PositionService (TODO)
  - [ ] Create PositionController (TODO)

- [ ] **S4-02**: Implement Position CRUD (TODO)
  - [ ] Create templates/position/list.html
  - [ ] Create templates/position/form.html
  - [ ] List positions with salary range and level
  - [ ] Create, edit, delete (soft delete) position

#### Employee Management ✅ PARTIALLY COMPLETED
- [x] **S4-03**: Complete Employee model ✅
  - [x] Add all fields from RANCANGAN.md section 5.2 ✅
  - [x] Data identitas: nik, full_name, place_of_birth, date_of_birth, gender, mothers_name ✅
  - [x] Data alamat & kontak: address, phone, email ✅
  - [x] Data kepegawaian: employment_status, hire_date, work_location ✅
  - [x] Data BPJS: bpjs_ketenagakerjaan_no, bpjs_kesehatan_no, npwp ✅
  - [x] Data gaji: basic_salary ✅
  - [x] Data keluarga: kk_number, marital_status, spouse_name, number_of_dependents ✅
  - [x] Status fields: status, resignation_date, resignation_reason ✅
  - [x] Soft delete support ✅
  - [x] Add approver_id for approval workflow ✅
  - [x] Add helper methods (isDepartmentHead, isManagerOrAbove, getApprovalChain) ✅

- [x] **S4-04**: Create EmployeeRepository with custom queries ✅
  - [x] findByEmailAndDeletedAtIsNull() ✅
  - [x] findByNikAndDeletedAtIsNull() ✅
  - [x] findByDepartmentIdAndDeletedAtIsNull() ✅
  - [x] findByStatusAndDeletedAtIsNull() ✅
  - [x] findAllActiveEmployees() ✅
  - [x] hasRole(employeeId, role) ✅
  - [x] findByRole(role) ✅

- [ ] **S4-05**: Create EmployeeService (TODO)
  - [ ] CRUD operations
  - [ ] Soft delete implementation
  - [ ] Validation: unique email, unique NIK, unique BPJS numbers
  - [ ] Photo upload handling
  - [ ] Set approver for employee

- [ ] **S4-06**: Implement Employee pages (TODO)
  - [ ] Create templates/employee/list.html (pagination, search, filter)
  - [ ] Create templates/employee/form.html (multi-section form)
  - [ ] Create templates/employee/detail.html (view employee details)
  - [ ] Photo upload with preview

- [ ] **S4-07**: Implement Employee Role Assignment (TODO)
  - [ ] Create role assignment modal
  - [ ] Assign/remove roles (EMPLOYEE, DOSEN, etc.)
  - [ ] Validate role assignments

- [ ] **S4-08**: Unit testing & integration test (TODO)

---

### 🆕 Sprint 2 Bonus: Approval System Implementation ✅ COMPLETED

#### Approval Service & Related
- [x] **S2-BONUS-01**: Create ApprovalService ✅
  - [x] getApprover(Employee requester, LeaveRequest request) ✅
  - [x] getNextApprover(Employee currentApprover, LeaveRequest request) ✅
  - [x] canApprove(Employee approver, Employee requester) ✅
  - [x] isDepartmentHead(Employee employee) ✅
  - [x] isDepartmentHeadOf(Employee employee, Department department) ✅
  - [x] isParentDepartmentHeadOf(Employee employee, Department department) ✅
  - [x] setCurrentApprover(LeaveRequest request) ✅
  - [x] isApprovalComplete(LeaveRequest request) ✅

- [x] **S2-BONUS-02**: Create LeaveRequest model ✅
  - [x] Create LeaveRequest.java with approval fields ✅
  - [x] Add current_approver_id field ✅
  - [x] Add helper methods (approve, reject, isShortLeave) ✅

- [x] **S2-BONUS-03**: Database Migration V5 ✅
  - [x] Add parent_id, head_id to departments ✅
  - [x] Add level to positions ✅
  - [x] Add approver_id to employees ✅
  - [x] Add current_approver_id to leave_requests ✅
  - [x] Fix audit columns in leave_requests ✅
  - [x] Fix Flyway migration history ✅

---

## Phase 3: Lecturer Data (Sprint 5-6)

### Sprint 5: Lecturer Profile Management

#### Lecturer Profile Model
- [ ] **S5-01**: Create LecturerProfile model
  - [ ] Create LecturerProfile.java
  - [ ] Fields: employee_id (FK), nidn, last_education, expertise
  - [ ] Lecturer rank (Asisten Ahli, Lektor, Lektor Kepala, Profesor)
  - [ ] Employment status (DOSEN_TETAP, DOSEN_TIDAK_TETAP)
  - [ ] Work status (ACTIVE, LEAVE, RETIRED)
  - [ ] Homebase prodi (department_id FK where is_prodi=true)
  - [ ] Soft delete support

- [ ] **S5-02**: Create LecturerProfileRepository
  - [ ] findByEmployeeIdAndDeletedAtIsNull()
  - [ ] findByNidnAndDeletedAtIsNull()
  - [ ] findByHomebaseProdiIdAndDeletedAtIsNull()
  - [ ] findByEmploymentStatusAndWorkStatus()

- [ ] **S5-03**: Create LecturerService
  - [ ] CRUD lecturer profile
  - [ ] Validation: NIDN unique
  - [ ] Validate homebase is a prodi department

- [ ] **S5-04**: Create LecturerController
  - [ ] List all lecturers
  - [ ] Create/edit lecturer profile
  - [ ] View lecturer detail

- [ ] **S5-05**: Implement Lecturer pages
  - [ ] Create templates/lecturer/list.html
  - [ ] Create templates/lecturer/profile-form.html
  - [ ] Create templates/lecturer/detail.html
  - [ ] Filter by employment status, work status, homebase

---

### Sprint 6: Lecturer Salary Setup

#### Lecturer Salary Model
- [ ] **S6-01**: Create LecturerSalary model
  - [ ] Create LecturerSalary.java
  - [ ] Fields: lecturer_profile_id (FK), period (YYYY-MM)
  - [ ] Salary components: basic_salary, certification, functional_allowance, teaching_honor, other_allowances
  - [ ] Total salary calculation
  - [ ] Status (DRAFT, PAID)

- [ ] **S6-02**: Create LecturerSalaryRepository
  - [ ] findByLecturerProfileIdAndPeriod()
  - [ ] findByPeriodAndStatus()

- [ ] **S6-03**: Create LecturerSalaryService (preparation only)
  - [ ] Basic CRUD structure
  - [ ] Calculation logic placeholder

- [ ] **S6-04**: Unit testing

---

## Phase 4: Attendance (Sprint 7)

### Attendance Management
- [ ] **S7-01**: Create Attendance model
  - [ ] Create Attendance.java
  - [ ] Fields: employee_id (FK), date, clock_in, clock_out
  - [ ] Status (PRESENT, LATE, LEAVE, SICK, ABSENT)
  - [ ] Notes field
  - [ ] Soft delete support

- [ ] **S7-02**: Create AttendanceRepository
  - [ ] findByEmployeeIdAndDate()
  - [ ] findByEmployeeIdAndDateBetween()
  - [ ] findByDateAndStatus()
  - [ ] findTodayAttendanceByEmployee()

- [ ] **S7-03**: Create AttendanceService
  - [ ] clockIn(employeeId, clockInTime) - validate working hours from Company config
  - [ ] clockOut(employeeId, clockOutTime)
  - [ ] checkLateStatus(clockInTime) - compare with company clock_in_end
  - [ ] getAttendanceHistory(employeeId, month, year)
  - [ ] getMonthlyAttendanceReport(month, year)

- [ ] **S7-04**: Create AttendanceController
  - [ ] Clock in endpoint
  - [ ] Clock out endpoint
  - [ ] Attendance history page

- [ ] **S7-05**: Implement Attendance pages
  - [ ] Create templates/attendance/list.html (calendar view)
  - [ ] Clock in/out buttons on dashboard
  - [ ] Attendance history table
  - [ ] Monthly report (HR/Admin only)

- [ ] **S7-06**: Implement auto status calculation
  - [ ] On clock in: determine PRESENT or LATE based on company config
  - [ ] On clock out: calculate total hours

- [ ] **S7-07**: Unit testing & integration test

---

## Phase 5: Leave Management (Sprint 8)

### Leave Request Management
- [ ] **S8-01**: Create LeaveRequest model
  - [ ] Create LeaveRequest.java
  - [ ] Fields: employee_id (FK), leave_type, start_date, end_date, reason
  - [ ] Status (PENDING, APPROVED, REJECTED)
  - [ ] approved_by (employee_id FK), approved_at
  - [ ] Soft delete support

- [ ] **S8-02**: Create LeaveBalance model
  - [ ] Create LeaveBalance.java
  - [ ] Fields: employee_id (FK), year, annual_total, annual_used, sick_total, sick_used
  - [ ] Unique: employee_id + year
  - [ ] Soft delete support

- [ ] **S8-03**: Create LeaveRequestRepository & LeaveBalanceRepository
  - [ ] findByEmployeeIdAndStatus()
  - [ ] findByStatusOrderByCreatedAtAsc()
  - [ ] findPendingRequests()
  - [ ] findByEmployeeIdAndYear()

- [ ] **S8-04**: Create LeaveRequestService
  - [ ] submitLeaveRequest() - validate balance
  - [ ] approveLeaveRequest() - deduct balance
  - [ ] rejectLeaveRequest()
  - [ ] getLeaveBalance(employeeId, year)
  - [ ] initializeLeaveBalance(employeeId, year)
  - [ ] Calculate leave days

- [ ] **S8-05**: Create LeaveRequestController
  - [ ] My leave requests page
  - [ ] Submit leave request form
  - [ ] Approval page (HR/Admin)
  - [ ] Leave balance display

- [ ] **S8-06**: Implement Leave pages
  - [ ] Create templates/leave/list.html (my requests)
  - [ ] Create templates/leave/form.html (submit request)
  - [ ] Create templates/leave/approval.html (pending approvals for HR/Admin)
  - [ ] Leave balance widget on dashboard

- [ ] **S8-07**: Unit testing & integration test

---

## Phase 6: Payroll Karyawan (Sprint 9)

### Payroll Management
- [ ] **S9-01**: Create Payroll model
  - [ ] Create Payroll.java
  - [ ] Fields: employee_id (FK), period (YYYY-MM)
  - [ ] Components: basic_salary, allowances, overtime, deductions, total_salary
  - [ ] Status (DRAFT, PAID)
  - [ ] Soft delete support

- [ ] **S9-02**: Create PayrollRepository
  - [ ] findByEmployeeIdAndPeriod()
  - [ ] findByPeriodAndStatus()
  - [ ] findByPeriod()

- [ ] **S9-03**: Create PayrollService
  - [ ] generatePayroll(employeeId, period) - generate from employee basic_salary
  - [ ] calculateOvertime(employeeId, period) - from attendance data
  - [ ] calculateDeductions(employeeId, period) - BPJS, PPh21 placeholder
  - [ ] calculateTotalSalary()
  - [ ] generateBatchPayroll(period) - for all employees
  - [ ] markAsPaid()

- [ ] **S9-04**: Create PayrollController
  - [ ] Payroll list page (by period)
  - [ ] Generate payroll form
  - [ ] Payslip view
  - [ ] Batch generate for period

- [ ] **S9-05**: Implement Payroll pages
  - [ ] Create templates/payroll/list.html (filter by period, status)
  - [ ] Create templates/payroll/form.html (single employee)
  - [ ] Create templates/payroll/slip.html (payslip with company stamp)
  - [ ] Add company logo and stamp to payslip

- [ ] **S9-06**: Unit testing & integration test

---

## Phase 7: Payroll Dosen (Sprint 10)

### Lecturer Payroll Management
- [ ] **S10-01**: Complete LecturerSalaryService
  - [ ] generateLecturerSalary(lecturerProfileId, period)
  - [ ] calculateCertificationAllowance()
  - [ ] calculateFunctionalAllowance(rank)
  - [ ] calculateTeachingHonor(credit_hours)
  - [ ] calculateTotalLecturerSalary()
  - [ ] generateBatchLecturerSalary(period)

- [ ] **S10-02**: Create LecturerPayrollController
  - [ ] Lecturer salary list page
  - [ ] Generate lecturer salary form
  - [ ] Lecturer payslip view

- [ ] **S10-03**: Implement Lecturer Payroll pages
  - [ ] Create templates/lecturer/salary-list.html
  - [ ] Create templates/lecturer/salary-form.html
  - [ ] Create templates/lecturer/salary-slip.html
  - [ ] Different format from regular payslip (lecturer components)

- [ ] **S10-04**: Unit testing & integration test

---

## Phase 8: Activity Logging (Sprint 11)

### Activity Logging Implementation
- [ ] **S11-01**: Create UserActivityLog model
  - [ ] Create UserActivityLog.java
  - [ ] Fields: employee_id (FK), activity_type, module_name, entity_type, entity_id
  - [ ] Description, activity_details (JSON), status, error_message
  - [ ] IP address, user agent, timestamp

- [ ] **S11-02**: Create UserActivityLogRepository
  - [ ] findByEmployeeIdOrderByCreatedAtDesc()
  - [ ] findByActivityTypeAndCreatedAtBetween()
  - [ ] findByModuleNameAndEntityId()

- [ ] **S11-03**: Create @LogActivity annotation
  - [ ] Create annotation with: activityType, moduleName, entityName, description
  - [ ] Add AspectJ dependency

- [ ] **S11-04**: Create ActivityLogAspect
  - [ ] @Around advice for @LogActivity
  - [ ] Extract user from session
  - [ ] Capture request metadata (IP, user agent)
  - [ ] Log successful operations (@AfterReturning)
  - [ ] Log failed operations (@AfterThrowing)
  - [ ] Async logging with @Async

- [ ] **S11-05**: Apply @LogActivity to all controllers
  - [ ] AuthController (LOGIN, LOGOUT, ROLE_SELECTION)
  - [ ] EmployeeController (CREATE, READ, UPDATE, DELETE)
  - [ ] DepartmentController, PositionController, CompanyController
  - [ ] LecturerController
  - [ ] AttendanceController (CLOCK_IN, CLOCK_OUT)
  - [ ] LeaveRequestController (SUBMIT_LEAVE, APPROVE_LEAVE, REJECT_LEAVE)
  - [ ] PayrollController, LecturerPayrollController (GENERATE_PAYROLL)
  - [ ] Payroll view (VIEW_SENSITIVE_DATA)

- [ ] **S11-06**: Create ActivityLogController
  - [ ] Activity log viewer (Admin only)
  - [ ] Filter by user, activity type, date range
  - [ ] Export log functionality

- [ ] **S11-07**: Implement Activity Log pages
  - [ ] Create templates/activity-log/list.html
  - [ ] Filterable table
  - [ ] Activity detail modal

- [ ] **S11-08**: Unit testing & integration test

---

## Phase 9: Testing & UAT (Sprint 12)

### Testing & Finalization
- [ ] **S12-01**: End-to-end testing
  - [ ] Test complete user flows
  - [ ] Admin flow: login → manage employees → view reports
  - [ ] HR flow: login → approve leaves → manage attendance
  - [ ] Employee flow: login → clock in/out → request leave → view payslip
  - [ ] Dosen flow: login → clock in/out → view lecturer payslip

- [ ] **S12-02**: Security testing
  - [ ] Test authentication bypass
  - [ ] Test authorization (role-based access)
  - [ ] Test session management
  - [ ] Test SQL injection prevention
  - [ ] Test XSS prevention

- [ ] **S12-03**: Performance testing
  - [ ] Test with 1000+ employees
  - [ ] Test payroll generation performance
  - [ ] Test report generation performance
  - [ ] Optimize slow queries

- [ ] **S12-04**: Bug fixes & refinement
  - [ ] Fix all reported bugs
  - [ ] UI/UX improvements
  - [ ] Code cleanup
  - [ ] Final code review

- [ ] **S12-05**: Documentation
  - [ ] User guide (how to use the system)
  - [ ] Admin guide (how to configure)
  - [ ] Developer documentation
  - [ ] API documentation

- [ ] **S12-06**: Deployment preparation
  - [ ] Create production build
  - [ ] Database migration for production
  - [ ] Environment configuration
  - [ ] Backup strategy

- [ ] **S12-07**: UAT with users
  - [ ] Deploy to staging
  - [ ] User acceptance testing
  - [ ] Collect feedback
  - [ ] Final adjustments

---

## Database Migration Checklist

### Flyway Migrations
- [ ] **V1__init_schema.sql**: Create all tables
  - [ ] companies
  - [ ] employees
  - [ ] employee_roles
  - [ ] user_sessions
  - [ ] departments
  - [ ] positions
  - [ ] lecturer_profiles
  - [ ] lecturer_salaries
  - [ ] attendances
  - [ ] leave_requests
  - [ ] leave_balances
  - [ ] payrolls
  - [ ] user_activity_logs

- [ ] **V2__insert_default_data.sql**: Insert default data
  - [ ] Default company record
  - [ ] Default admin user

---

## Configuration Checklist

### application.yml
- [ ] Database configuration (MySQL)
- [ ] JPA/Hibernate configuration
- [ ] Server port (8080)
- [ ] File upload configuration (max 2MB)
- [ ] Upload directory configuration
- [ ] Session timeout configuration
- [ ] Logging configuration

### Security Configuration
- [ ] Password encoder (BCrypt)
- [ ] Authentication rules
- [ ] Authorization rules (role-based)
- [ ] CSRF configuration
- [ ] CORS configuration

### Web Configuration
- [ ] Static resources handling
- [ ] View resolver (Thymeleaf)
- [ ] Message converter
- [ ] Exception handler

---

## File Structure Verification

### Backend (Java)
- [ ] com.hris.HrisApplication
- [ ] com.hris.config.* (SecurityConfig, WebConfig, RoleConfig)
- [ ] com.hris.controller.* (All controllers)
- [ ] com.hris.service.* (All services)
- [ ] com.hris.repository.* (All repositories)
- [ ] com.hris.model.* (All entities + enums)
- [ ] com.hris.dto.* (All DTOs)
- [ ] com.hris.exception.* (Global exception handler)
- [ ] com.hris.aspect.* (ActivityLogAspect)

### Frontend (Templates)
- [ ] templates/layout/main.html
- [ ] templates/fragments/* (header, sidebar, footer)
- [ ] templates/auth/* (login, role-selection)
- [ ] templates/dashboard/* (admin, hr, employee, dosen)
- [ ] templates/company/* (profile, edit)
- [ ] templates/employee/* (list, form, detail)
- [ ] templates/department/* (list, form)
- [ ] templates/position/* (list, form)
- [ ] templates/lecturer/* (list, profile-form, detail, salary-list, salary-form, salary-slip)
- [ ] templates/attendance/* (list)
- [ ] templates/leave/* (list, form, approval)
- [ ] templates/payroll/* (list, form, slip)
- [ ] templates/activity-log/* (list)

### Static Resources
- [ ] static/css/style.css (custom styles)
- [ ] static/js/app.js (common JavaScript)
- [ ] static/images/* (placeholders)
- [ ] uploads/* (photo storage)

---

## Notes

### Important Implementation Notes
1. **Soft Delete**: Semua query WAJIB filter `WHERE deleted_at IS NULL`
2. **Audit Trail**: Semua operasi CRUD harus log activity
3. **Singleton Company**: Hanya boleh ada 1 record di tabel companies
4. **Multi-Role**: User bisa punya multiple roles, pilih role setelah login
5. **Working Hours**: Gunakan config dari tabel companies untuk hitung terlambat
6. **Leave Balance**: Auto-initialize saat employee baru dibuat
7. **Payslip**: Tampilkan logo dan stamp dari company config

### Testing Priority
- **Critical Path**: Login → Dashboard → Employee Management → Attendance → Leave → Payroll
- **Security**: Authentication, authorization, session management
- **Data Integrity**: Soft delete, audit trail, validations

---

**Last Updated**: 17 Januari 2026
