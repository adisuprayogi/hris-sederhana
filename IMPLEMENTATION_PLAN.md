# Implementation Plan
## Human Resource Information System (HRIS)

---

## Document Information

| Item | Description |
|------|-------------|
| **Project Name** | HRIS Sederhana |
| **Document Version** | 1.0 |
| **Date** | 16 Januari 2026 |
| **Author** | HRIS Development Team |
| **Status** | Draft |

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Implementation Strategy](#2-implementation-strategy)
3. [Timeline Overview](#3-timeline-overview)
4. [Sprint Breakdown](#4-sprint-breakdown)
5. [Resource Planning](#5-resource-planning)
6. [Risk Management](#6-risk-management)
7. [Quality Assurance](#7-quality-assurance)
8. [Deployment Strategy](#8-deployment-strategy)
9. [Handover & Training](#9-handover--training)
10. [Maintenance & Support](#10-maintenance--support)

---

## 1. Project Overview

### 1.1 Project Objectives

Membangun sistem HRIS (Human Resource Information System) yang mencakupi:
- Manajemen data karyawan dan dosen
- Sistem absensi (Clock In/Out)
- Manajemen cuti dengan approval workflow
- Penggajian karyawan dan dosen
- Activity logging untuk audit trail
- Multi-role access control

### 1.2 Success Criteria

| Criteria | Target |
|----------|--------|
| Functional Requirements | 100% dari 34 requirements terimplementasi |
| Data Completeness | 100% data sesuai regulasi Indonesia |
| System Performance | Response time < 3 detik |
| User Satisfaction | Minimal 4/5 |
| Zero Bug Critical | 0 critical bug saat go-live |

### 1.3 Constraints

| Constraint | Detail |
|------------|--------|
| **Timeline** | 6 bulan (24 minggu) |
| **Budget** | Terbatas, gunakan open source |
| **Team** | 3-5 developers |
| **Technology** | SpringBoot, HTMX, Alpine.js, Tailwind, MySQL |

---

## 2. Implementation Strategy

### 2.1 Development Approach

**Agile Scrum Framework:**
- Sprint duration: 2 minggu
- Total sprint: 12 sprint
- Daily standup: 15 menit
- Sprint review: Akhir sprint
- Sprint retrospective: Akhir sprint

**Development Principles:**
1. **MVP First**: Fokus fitur mandatory dulu
2. **Incremental**: Setiap sprint menghasilkan fitur yang bisa ditest
3. **Test-Driven**: Unit test untuk setiap fitur
4. **Documentation**: Code comment dan user guide

### 2.2 Technical Approach

**Layered Architecture:**
```
Controller → Service → Repository → Database
     ↓
  Thymeleaf Templates (SSR with HTMX)
```

**Database Migration:**
- Flyway untuk version control
- Schema update bertahap setiap sprint

**Continuous Integration:**
- Git untuk version control
- Automated build dengan Maven
- Database migration otomatis

---

## 3. Timeline Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      IMPLEMENTATION TIMELINE (6 BULAN / 24 MINGGU)          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  PHASE 1: FOUNDATION         │████████████████│                           │
│  Sprint 1-2                  │   4 MINGGU        │                           │
│                             │                   │                           │
│  PHASE 2: MASTER DATA        │                   │████████████████│      │
│  Sprint 3-4                  │                   │   4 MINGGU        │      │
│                             │                   │                   │      │
│  PHASE 3: LECTURER DATA      │                   │                   │█████│
│  Sprint 5-6                  │                   │                   │    │      │
│                             │                   │                   │ 2 MG│      │
│  PHASE 4: ATTENDANCE         │                   │                   │    │      │
│  Sprint 7                    │                   │                   │    │      │
│                             │                   │                   │    │      │
│  PHASE 5: LEAVE MANAGEMENT   │                   │                   │    │      │
│  Sprint 8                    │                   │                   │    │      │
│                             │                   │                   │    │      │
│  PHASE 6: PAYROLL KARYAWAN   │                   │                   │    │      │
│  Sprint 9                    │                   │                   │    │      │
│                             │                   │                   │    │      │
│  PHASE 7: PAYROLL DOSEN      │                   │                   │    │      │
│  Sprint 10                   │                   │                   │    │      │
│                             │                   │                   │    │      │
│  PHASE 8: ACTIVITY LOGGING   │                   │                   │    │      │
│  Sprint 11                   │                   │                   │    │      │
│                             │                   │                   │    │      │
│  PHASE 9: TESTING & UAT      │                   │                   │█████│      │
│  Sprint 12                   │                   │                   │    │      │
│                             │                   │                   │    │      │
└─────────────────────────────────────────────────────────────────────────────┘
│  JAN  │  FEB  │  MAR  │  APR  │  MEI  │  JUN  │
└────────┴───────┴───────┴───────┴───────┴───────┘
```

### 3.1 Milestone

| Milestone | Sprint | Target Date | Deliverable |
|-----------|--------|------------|------------|
| **M1: Foundation Ready** | 2 | End of Month 1 | Login, Role Selection, Layout Template |
| **M2: Master Data Ready** | 4 | End of Month 2 | Department, Position, Employee, User Management |
| **M3: Lecturer Data Ready** | 6 | End of Month 3 | Lecturer Profile, Employee-Dosen Relation |
| **M4: Attendance Live** | 7 | Mid of Month 4 | Clock In/Out, Attendance History |
| **M5: Leave Management Live** | 8 | End of Month 4 | Leave Request, Approval Workflow |
| **M6: Payroll Ready** | 10 | End of Month 5 | Generate Payroll, Payslip |
| **M7: System Complete** | 12 | End of Month 6 | All Features Tested & Deployed |

---

## 4. Sprint Breakdown

### Sprint 1: Foundation & Authentication (Minggu 1-2)

**Sprint Goal:** Setup project foundation dan implement authentication

| Task ID | Task | Estimate | Owner | Notes |
|---------|------|----------|-------|-------|
| **S1-01** | Initialize SpringBoot project | 2 hari | Backend | Maven deps, folder structure |
| **S1-02** | Setup database & Flyway | 1 hari | Backend | MySQL connection, migration script |
| **S1-03** | Implement Spring Security | 2 hari | Backend | Authentication, password hashing |
| **S1-04** | Create base layout template | 2 hari | Frontend | Header, sidebar, footer with Thymeleaf |
| **S1-05** | Implement Login page | 1 hari | Frontend | Form login dengan HTMX |
| **S1-06** | Implement Role Selection page | 1 hari | Frontend | Multi-role selector |
| **S1-07** | Implement UserSession & session management | 2 hari | Backend | Session dengan selected_role |
| **S1-08** | Unit testing & integration test | 2 hari | QA | Authentication flow |
| **S1-09** | Code review & refinement | 1 hari | Team | - |

**Deliverables:**
- ✅ Project structure terbentuk
- ✅ Login berfungsi
- ✅ Role selection berfungsi
- ✅ Base layout template
- ✅ Session management
- ✅ Unit test coverage > 80%

**Definition of Done:**
- [ ] Login dengan email/password berfungsi
- [ ] Multi-role user dapat pilih role
- [ ] Session tersimpan dengan selected_role
- [ ] Base layout (header, sidebar, footer) siap digunakan
- [ ] Password di-hash dengan BCrypt
- [ ] Unit test terpasang dan passing
- [ ] Code review selesai

---

### Sprint 2: Dashboard & User Management (Minggu 3-4)

**Sprint Goal:** Implement dashboard per role dan user management

| Task ID | Task | Estimate | Owner | Notes |
|---------|------|----------|-------|-------|
| **S2-01** | Create BaseEntity for audit trail | 1 hari | Backend | created_at, updated_at, created_by, updated_by |
| **S2-02** | Implement Admin Dashboard | 2 hari | Frontend | Stat cards, quick actions, recent activities |
| **S2-03** | Implement HR Dashboard | 2 hari | Frontend | HR-specific metrics |
| **S2-04** | Implement Employee/DOSEN Dashboard | 2 hari | Frontend | Personal dashboard |
| **S2-05** | Implement User Management (ADMIN only) | 2 hari | Backend | CRUD user, assign role |
| **S2-06** | Implement Role Assignment UI | 1 hari | Frontend | Checkbox role, multiple selection |
| **S2-07** | Implement Reset Password | 1 hari | Backend | Generate random password |
| **S2-08** | Implement Activate/Deactivate User | 1 hari | Backend | Toggle status |
| **S2-09** | Unit testing | 2 hari | QA | - |
| **S2-10** | Sprint review & retrospective | 0.5 hari | Team | - |

**Deliverables:**
- ✅ Dashboard per role (Admin, HR, Employee, DOSEN)
- ✅ CRUD User Management
- ✅ Role Assignment (multi-role support)
- ✅ Reset Password
- ✅ Activate/Deactivate User

**Definition of Done:**
- [ ] Dashboard Admin tampilkan dengan stat cards
- [ ] Dashboard HR tampilkan dengan HR metrics
- [ ] Dashboard Employee/DOSEN tampilkan
- [ ] ADMIN dapat create, edit, delete user
- [ ] ADMIN dapat assign role (multiple)
- [ ] ADMIN dapat reset password user
- [ ] ADMIN dapat activate/deactivate user
- [ ] Unit test passing
- [ ] User guide untuk user management

---

### Sprint 3: Department & Position Management (Minggu 5-6)

**Sprint Goal:** Implement master data: department dan position

| Task ID | Task | Estimate | Owner | Notes |
|---------|------|----------|-------|-------|
| **S3-01** | Implement Department CRUD | 2 hari | Backend | Dengan is_prodi dan kode_prodi |
| **S3-02** | Implement Position CRUD | 1 hari | Backend | Dengan base_salary |
| **S3-03** | Create Department form UI | 1 hari | Frontend | Form dengan conditional field |
| **S3-04** | Create Position form UI | 1 hari | Frontend | Form position |
| **S3-05** | Implement Department list page | 1 hari | Frontend | Table dengan actions |
| **S3-06** | Implement Position list page | 1 hari | Frontend | Table dengan actions |
| **S3-07** | Implement soft delete for Department/Position | 2 hari | Backend | deleted_at, deleted_by |
| **S3-08** | Validation: unique name, kode_prodi logic | 1 hari | Backend | - |
| **S3-09** | Unit testing | 2 hari | QA | - |
| **S3-10** | Documentation | 1 hari | Team | User guide |

**Deliverables:**
- ✅ Department CRUD (dengan is_prodi & kode_prodi)
- ✅ Position CRUD (dengan base_salary)
- ✅ Soft delete untuk department & position
- ✅ Validation unique name dan kode_prodi

**Definition of Done:**
- [ ] Department dapat dibuat, diedit, dihapus (soft delete)
- [ ] Department memiliki field is_prodi dan kode_prodi
- [ ] Position dapat dibuat, diedit, dihapus (soft delete)
- [ ] Position memiliki base_salary
- [ ] Nama department unique
- [ ] Nama position unique
- [ ] Kode_prodi unique untuk department dengan is_prodi=true
- [ ] Soft delete berfungsi
- [ ] Unit test passing

---

### Sprint 4: Employee Management (Minggu 7-8)

**Sprint Goal:** Implement employee management lengkap sesuai regulasi Indonesia

| Task ID | Task | Estimate | Owner | Notes |
|---------|------|----------|-------|-------|
| **S4-01** | Create Employee entity (23 fields) | 2 hari | Backend | Lengkap sesuai regulasi |
| **S4-02** | Implement Employee CRUD | 3 hari | Backend | Create, Read, Update, Soft Delete |
| **S4-03** | Create Employee form (5 tabs) | 3 hari | Frontend | Data Identitas, Kepegawaian, BPJS, Keluarga, Role |
| **S4-04** | Implement Employee list page | 2 hari | Frontend | Search, filter, pagination |
| **S4-05** | Implement Employee detail page | 1 hari | Frontend | View profile |
| **S4-06** | Implement validation (NIK, email unique, dll) | 2 hari | Backend | Sesuai regulasi |
| **S4-07** | Implement file upload (photo) | 1 hari | Backend | Multipart config |
| **S4-08** | Employee self-service (view profile) | 1 hari | Frontend | Read-only, masked BPJS |
| **S4-09** | Employee self-service (edit profile) | 1 hari | Frontend | Limited fields |
| **S4-10** | Unit testing | 2 hari | QA | - |
| **S4-11** | Documentation | 1 hari | Team | - |

**Deliverables:**
- ✅ Employee CRUD lengkap (23 field sesuai regulasi)
- ✅ Employee list dengan search/filter/pagination
- ✅ Employee self-service (view & edit terbatas)
- ✅ File upload foto profil
- ✅ Validation sesuai regulasi Indonesia

**Definition of Done:**
- [ ] Employee dapat dibuat dengan data lengkap
- [ ] Data employee: NIK, nama, TTL, gender, nama ibu, alamat, telepon, email
- [ ] Data kepegawaian: status, hire_date, work_location, dept, position
- [ ] Data BPJS: no. BPJS TK, no. BPJS Kesehatan, NPWP
- [ ] Data pajak: gaji pokok
- [ ] Data keluarga: no. KK, status nikah, nama pasangan, tanggungan
- [ ] NIK unique, email unique, BPJS unique, NPWP unique
- [ ] Nama ibu kandung wajib (untuk BPJS)
- [ ] Upload foto profil berfungsi
- [ ] Employee self-service: view profile
- [ ] Employee self-service: edit profil (terbatas: telepon, alamat, foto)
- [ ] Soft delete berfungsi
- [ ] Unit test passing

---

### Sprint 5: Lecturer Management (Minggu 9-10)

**Sprint Goal:** Implement lecturer management dengan relasi ke employee

| Task ID | Task | Estimate | Owner | Notes |
|---------|------|----------|-------|-------|
| **S5-01** | Create LecturerProfile entity | 1 hari | Backend | NIDN, pendidikan, keahlian, jenjang, homebase |
| **S5-02** | Implement Lecturer CRUD | 2 hari | Backend | Create, Read, Update, Soft Delete |
| **S5-03** | Implement 2 status dosen | 1 hari | Backend | Employment status + Work status |
| **S5-04** | Create Lecturer form UI | 2 hari | Frontend | Form lecturer profile |
| **S5-05** | Implement Lecturer list page | 1 hari | Frontend | Filter by prodi, jenjang, status |
| **S5-06** | Implement validation: homebase prodi = is_prodi=true | 1 hari | Backend | - |
| **S5-07** | Auto-create lecturer prompt when role DOSEN assigned | 1 hari | Backend | Conditional flow |
| **S5-08** | Unit testing | 2 hari | QA | - |
| **S5-09** | Documentation | 1 hari | Team | - |

**Deliverables:**
- ✅ Lecturer Profile CRUD
- ✅ Status Dosen Tetap/Tidak Tetap
- ✅ Status Kerja Aktif/Cuti/Pensiun
- ✅ Jenjang dosen (Asisten Ahli - Profesor)
- ✅ Homebase prodi linking

**Definition of Done:**
- [ ] Lecturer profile dapat dibuat untuk employee dengan role DOSEN
- [ ] NIDN wajib dan unique
- [ ] Pendidikan terakhir (S2/S3)
- [ ] Bidang keahlian
- [ ] Jenjang: ASISTEN_AHLI, LEKTOR, LEKTOR_KEPALA, PROFESOR
- [ ] Status kepegawaian: DOSEN_TETAP atau DOSEN_TIDAK_TETAP
- [ ] Status kerja: ACTIVE, LEAVE, RETIRED
- [ ] Homebase prodi harus department dengan is_prodi=true
- [ ] Lecturer list tampilkan dengan filter
- [ ] Soft delete berfungsi
- [ ] Prompt isi data dosen saat role DOSEN di-assign
- [ ] Unit test passing

---

### Sprint 6: Attendance Management (Minggu 11)

**Sprint Goal:** Implement sistem absensi dengan clock in/clock out

| Task ID | Task | Estimate | Owner | Notes |
|---------|------|----------|-------|-------|
| **S6-01** | Create Attendance entity | 1 hari | Backend | Dengan soft delete |
| **S6-02** | Implement Clock In API | 1 hari | Backend | Auto-status (PRESENT/LATE) |
| **S6-03** | Implement Clock Out API | 1 hari | Backend | Update clock_out |
| **S6-04** | Create Clock In/Out UI | 2 hari | Frontend | Button di dashboard |
| **S6-03** | Implement Attendance history page | 2 hari | Frontend | Calendar view, table |
| **S6-06** | Implement Attendance report (HR/ADMIN) | 1 hari | Backend | Rekap bulanan |
| **S6-07** | Implement validation: 1x clock in per day | 1 hari | Backend | - |
| **S6-08** | Implement status logic (LATE jika > 08:00) | 1 hari | Backend | - |
| **S6-09** | Unit testing | 1 hari | QA | - |
| **S6-10** | Documentation | 0.5 hari | Team | - |

**Deliverables:**
- ✅ Clock In/Out feature
- ✅ Attendance history dengan calendar view
- ✅ Attendance report bulanan
- ✅ Status tracking (Hadir, Telat, Sakit, Izin, Alpha)

**Definition of Done:**
- [ ] Clock In berfungsi, catat waktu
- [ ] Clock Out berfungsi, catat waktu
- [ ] Status PRESENT jika clock in ≤ 08:00
- [ ] Status LATE jika clock in > 08:00
- [ ] Satu employee hanya bisa 1x clock in per hari
- [ ] Clock In hanya hari kerja (Senin-Jumat)
- [ ] Clock Out hanya setelah Clock In
- [ ] Attendance history tampil dengan calendar view
- [ ] Attendance history tampilkan dengan table
- [ ] HR/ADMIN dapat view semua attendance
- [ ] Employee hanya view attendance sendiri
- [ ] Activity log tercatat untuk Clock In/Out
- [ ] Unit test passing

---

### Sprint 7: Leave Management (Minggu 12)

**Sprint Goal:** Implement manajemen cuti dengan approval workflow

| Task ID | Task | Estimate | Owner | Notes |
|---------|------|----------|-------|-------|
| **S7-01** | Create LeaveRequest entity | 1 hari | Backend | Dengan soft delete |
| **S7-02** | Create LeaveBalance entity | 1 hari | Backend | Dengan soft delete |
| **S7-03** | Implement Leave Request API | 2 hari | Backend | Submit, validation saldo |
| **S7-04** | Implement Approval API | 2 hari | Backend | Approve/Reject, update saldo |
| **S7-05** | Create Leave Request form UI | 2 hari | Frontend | Form dengan tipe cuti |
| **S7-06** | Create Leave Request list page | 2 hari | Frontend | Dengan filter status |
| **S7-07** | Implement Leave Balance display | 1 hari | Frontend | Di dashboard |
| **S7-08** | Implement validation: saldo cukup, no overlap | 1 hari | Backend | - |
| **S7-09** | Implement email notification (optional) | 1 hari | Backend | Notif approval |
| **S7-10** | Unit testing | 1 hari | QA | - |
| **S7-11** | Documentation | 0.5 hari | Team | - |

**Deliverables:**
- ✅ Leave Request form
- ✅ Leave Request list dengan filter
- ✅ Leave Approval (Approve/Reject)
- ✅ Leave Balance tracking
- ✅ Leave report bulanan

**Definition of Done:**
- [ ] Employee dapat submit leave request
- [ ] Tipe cuti: ANNUAL, SICK, MATERNITY, MARRIAGE, SPECIAL, UNPAID
- [ ] Start date dan end date wajib
- [ ] Alasan wajib
- [ ] Validasi saldo cuti cukup
- [ ] Validasi tidak overlap dengan cuti lain
- [ ] Cuti annual: 12 hari, Sick: 14 hari per tahun
- [ ] HR/ADMIN dapat approve/reject
- [ ] Approve mengurangi saldo cuti
- [ ] Reject tidak mengurangi saldo cuti
- [ ] Reject memerlukan alasan
- [ ] Employee dapat view saldo cuti sendiri
- [ ] Activity log tercatat untuk submit/approve/reject
- [ ] Unit test passing

---

### Sprint 8: Payroll - Karyawan (Minggu 13-14)

**Sprint Goal:** Implement penggajian karyawan

| Task ID | Task | Estimate | Owner | Notes |
|---------|------|----------|-------|-------|
| **S8-01** | Create Payroll entity | 1 hari | Backend | Dengan soft delete |
| **S8-02** | Implement Payroll calculation logic | 3 hari | Backend | Basic + allowances + overtime - deductions |
| **S8-03** | Implement Generate Payroll API | 2 hari | Backend | Generate semua employee per periode |
| **S8-04** | Implement Update Payroll API | 1 hari | Backend | Edit sebelum PAID |
| **S8-05** | Implement Mark as Paid API | 1 hari | Backend | Status DRAFT → PAID |
| **S8-06** | Create Payroll form UI | 1 hari | Frontend | Generate form |
| **S8-07** | Create Payroll list page | 2 hari | Frontend | Filter periode, status |
| **S8-08** | Create Payslip UI (PDF) | 2 hari | Frontend | Download/print |
| **S8-09** | Implement Payslip access control | 1 hari | Backend | Hanya owner jika PAID |
| **S8-10** | Unit testing | 2 hari | QA | Termasuk calculation test |
| **S8-11** | Documentation | 1 hari | Team | - |

**Deliverables:**
- ✅ Generate Payroll per periode
- ✅ Edit Payroll (sebelum PAID)
- ✅ Mark as Paid
- ✅ Payslip PDF
- ✅ Payroll report

**Definition of Done:**
- [ ] HR/ADMIN dapat generate payroll per periode (YYYY-MM)
- [ ] Komponen gaji: basic_salary, allowances, overtime, deductions
- [ ] Basic salary dari position.base_salary
- [ ] Allowances dari data employee
- [ ] Overtime dihitung (jam lembur × rate)
- [ ] Deductions: BPJS, PPh 21, dll
- [ ] Total salary = basic + allowances + overtime - deductions
- [ ] Status payroll: DRAFT → PAID
- [ ] Payroll DRAFT dapat diedit
- [ ] Payroll PAID tidak dapat dihapus
- [ ] Payslip PDF dapat di-download/print
- [ ] Employee hanya bisa lihat payslip sendiri jika PAID
- [ ] Activity log tercatat untuk generate
- [ ] Unit test passing (termasuk calculation)

---

### Sprint 9: Payroll - Dosen (Minggu 15-16)

**Sprint Goal:** Implement penggajian dosen

| Task ID | Task | Estimate | Owner | Notes |
|---------|------|----------|-------|-------|
| **S9-01** | Create LecturerSalary entity | 1 hari | Backend | Dengan soft delete |
| **S9-02** | Implement Lecturer Payroll calculation | 2 hari | Backend | Basic + certification + functional + teaching |
| **S9-03** | Implement Generate Lecturer Payroll API | 2 hari | Backend | Generate semua dosen per periode |
| **S9-04** | Create Lecturer Payroll form UI | 1 hari | Frontend | Generate form |
| **S9-05** | Create Lecturer Payroll list page | 2 hari | Frontend | Filter periode, status |
| **S9-06** | Create Lecturer Payslip UI | 2 hari | Frontend | PDF dengan komponen dosen |
| **S9-07** | Implement tunjangan fungsional logic | 1 hari | Backend | Berdasarkan jenjang |
| **S9-08** | Unit testing | 2 hari | QA | - |
| **S9-09** | Documentation | 1 hari | Team | - |

**Deliverables:**
- ✅ Generate Lecturer Payroll
- ✅ Lecturer Payslip dengan komponen dosen
- ✅ Tunjangan fungsional per jenjang

**Definition of Done:**
- [ ] HR/ADMIN dapat generate lecturer payroll per periode
- [ ] Komponen gaji dosen: basic_salary, certification_allowance, functional_allowance, teaching_honor, other_allowances
- [ ] Certification allowance: tunjangan sertifikasi
- [ ] Functional allowance: berdasarkan jenjang (Asisten Ahli - Profesor)
- [ ] Teaching honor: per kredit/sks
- [ ] Total salary = semua komponen
- [ ] Status: DRAFT → PAID
- [ ] Payslip PDF dosen dapat di-download/print
- [ ] Dosen hanya bisa lihat payslip sendiri jika PAID
- [ ] Activity log tercatat untuk generate
- [ ] Unit test passing

---

### Sprint 10: Activity Logging (Minggu 17)

**Sprint Goal:** Implement activity logging dengan Spring AOP

| Task ID | Task | Estimate | Owner | Notes |
|---------|------|----------|-------|-------|
| **S10-01** | Create UserActivityLog entity | 1 hari | Backend | Dengan index untuk performa |
| **S10-02** | Create ActivityType enum | 0.5 hari | Backend | 14 tipe aktivitas |
| **S10-03** | Create @LogActivity annotation | 1 hari | Backend | Custom annotation |
| **S10-04** | Implement ActivityLogAspect (AOP) | 3 hari | Backend | @AfterReturning, @AfterThrowing |
| **S10-05** | Implement UserActivityLogService | 2 hari | Backend | Async logging |
| **S10-06** | Implement UserActivityLogRepository | 1 hari | Backend | Query methods |
| **S10-07** | Implement Activity Log Dashboard UI | 2 hari | Frontend | Filter & export |
| **S10-08** | Apply @LogActivity to all controllers | 2 hari | Backend | Add annotation |
| **S10-09** | Implement manual logging for Auth | 1 hari | Backend | Login, logout, failed |
| **S10-10** | Unit testing | 1 hari | QA | - |
| **S10-11** | Documentation | 0.5 hari | Team | - |

**Deliverables:**
- ✅ @LogActivity annotation
- ✅ ActivityLogAspect (AOP)
- ✅ UserActivityLogService
- ✅ Activity Log Dashboard
- ✅ Automatic logging untuk semua fitur

**Definition of Done:**
- [ ] @LogActivity annotation berfungsi
- [ ] ActivityLogAspect intercept semua method dengan annotation
- [ ] Login (success/failed) tercatat
- [ ] Logout tercatat
- [ ] Role selection tercatat
- [ ] CRUD operations tercatat
- [ ] Clock In/Out tercatat
- [ ] Leave submit/approve/reject tercatat
- [ ] Payroll generate tercatat
- [ ] View sensitive data tercatat
- [ ] IP address tercatat
- [ ] User agent tercatat
- [ ] Status SUCCESS/FAILED tercatat
- [ ] Error message tercatat jika FAILED
- [ ] Activity log dashboard tampilkan untuk ADMIN
- [ ] Filter: employee, module, activity type, date range, status
- [ ] Export Excel/PDF tersedia
- [ ] Activity log tidak dapat dihapus
- [ ] Logging async (tidak mengganggu performa)
- [ ] Unit test passing

---

### Sprint 11: Testing & Bug Fixing (Minggu 18-20)

**Sprint Goal:** Comprehensive testing dan bug fixing

| Task ID | Task | Estimate | Owner | Notes |
|---------|------|----------|-------|-------|
| **S11-01** | Integration Testing | 3 hari | QA | Test semua flow end-to-end |
| **S11-02** | Security Testing | 2 hari | QA | SQL injection, XSS, CSRF |
| **S11-03** | Performance Testing | 2 hari | QA | Load testing, response time |
| **S11-04** | Usability Testing | 2 hari | QA | User experience |
| **S11-05** | Cross-browser Testing | 1 hari | QA | Chrome, Firefox, Edge, Safari |
| **S11-06** | Responsive Testing | 1 hari | QA | Mobile, tablet, desktop |
| **S11-07** | Data Validation Testing | 2 hari | QA | Test semua validasi |
| **S11-08** | Bug Fixing | 3 hari | Dev | Perbaiki bug dari testing |
| **S11-09** | Regression Testing | 1 hari | QA | Test ulang setelah fix |
| **S11-10** | Documentation update | 1 hari | Team | Update berdasarkan fix |

**Deliverables:**
- ✅ Test report (integration, security, performance)
- ✅ Bug terfix dan ditest ulang
- ✅ Sistem siap untuk UAT

**Definition of Done:**
- [ ] Semua feature melewati integration test
- [ ] Security test pass (SQL injection, XSS, CSRF)
- [ ] Performance test pass (response time < 3 detik)
- [ ] Usability test pass
- [ ] Cross-browser compatible (Chrome, Firefox, Edge, Safari)
- [ ] Responsive design working (mobile, tablet, desktop)
- [ ] Semua validasi berfungsi
- [ ] Critical bug = 0
- [ ] High bug = 0
- [ ] Medium bug < 5
- [ ] Low bug < 20
- [ ] Regression test pass

---

### Sprint 12: UAT, Deployment & Handover (Minggu 21-24)

**Sprint Goal:** User Acceptance Test, Production Deployment, Handover

| Task ID | Task | Estimate | Owner | Notes |
|---------|------|----------|-------|-------|
| **S12-01** | Prepare UAT environment | 1 hari | DevOps | Staging server |
| **S12-02** | Deploy to Staging | 1 hari | DevOps | Deploy ke staging |
| **S12-03** | Import existing data (mock) | 2 hari | Dev | Data seed untuk testing |
| **S12-04** | Conduct UAT dengan stakeholder | 3 hari | All | Demo dan testing |
| **S12-05** | Collect UAT feedback | 2 hari | Team | Document feedback |
| **S12-06** | Fix critical UAT issues | 2 hari | Dev | Perbaikan untuk go-live |
| **S12-07** | Prepare production server | 2 hari | DevOps | Server setup, MySQL, nginx |
| **S12-08** | Deploy to Production | 1 hari | DevOps | Production deployment |
| **S12-09** | Production smoke test | 1 hari | QA | Verify deployment |
| **S12-10** | Create user documentation | 2 hari | Team | User manual, training material |
| **S12-11** | Conduct training session | 2 hari | Team | Training untuk ADMIN, HR, Employee |
| **S12-12** | Handover to operation team | 2 hari | Team | Knowledge transfer |
| **S12-13** | Go-live preparation | 1 hari | Team | Final check |
| **S12-14** | GO-LIVE | - | - | Production live |
| **S12-15** | Post-go-live support (1 minggu) | 5 hari | Team | Monitor dan support |

**Deliverables:**
- ✅ UAT report
- ✅ Production deployment
- ✅ User documentation
- ✅ Training completion
- ✅ Handover documentation
- ✅ GO-LIVE

**Definition of Done:**
- [ ] UAT environment siap
- [ ] Staging deployment sukses
- [ ] Data mock terimport
- [ ] UAT dilakukan dengan semua stakeholder
- [ ] UAT sign-off didapat
- [ ] Critical UAT issues diperbaiki
- [ ] Production server siap
- [ ] Aplikasi deployed ke production
- [ ] Smoke test production pass
- [ ] User documentation lengkap
- [ ] Training selesai (ADMIN, HR, Employee)
- [ ] Handover selesai
- [ ] Operation team siap mengambil alih
- [ ] GO-LIVE berhasil
- [ ] Post-go-live support 1 minggu

---

## 5. Resource Planning

### 5.1 Team Structure

| Role | Person | Allocation | Responsibilities |
|------|--------|------------|----------------|
| **Project Manager** | [PM Name] | 50% | Overall project management, coordination |
| **Tech Lead** | [Lead Name] | 100% | Architecture, code review, mentoring |
| **Backend Developer 1** | [Dev1 Name] | 100% | Controller, Service, Repository |
| **Backend Developer 2** | [Dev2 Name] | 100% | AOP, Security, Activity logging |
| **Frontend Developer** | [Dev3 Name] | 100% | Thymeleaf, HTMX, Alpine.js, Tailwind |
| **QA Engineer** | [QA Name] | 100% | Testing, test automation, bug reporting |
| **DevOps Engineer** | [Ops Name] | 25% | Server, deployment, CI/CD |

### 5.2 Skill Requirements

| Role | Required Skills |
|------|-----------------|
| **Backend Dev** | Java 17+, SpringBoot, Spring Security, JPA, MySQL, Git |
| **Frontend Dev** | Thymeleaf, HTMX, Alpine.js, Tailwind CSS, JavaScript |
| **QA** | Testing methodology, JUnit, integration testing, API testing |
| **DevOps** | Linux, MySQL, Nginx, Maven, Git, bash scripting |

### 5.3 Development Environment

| Environment | Purpose | Spec |
|------------|---------|-----|
| **Local Dev** | Development laptop | RAM 8GB, JDK 17, Maven, MySQL 8.x |
| **Dev Server** | Integration testing | 2 CPU, 4GB RAM |
| **Staging** | UAT | 2 CPU, 4GB RAM |
| **Production** | Live system | 4 CPU, 8GB RAM |

### 5.4 Tools & Software

| Category | Tools |
|----------|-------|
| **IDE** | IntelliJ IDEA / VS Code |
| **Version Control** | Git (GitHub/GitLab) |
| **Build Tool** | Maven |
| **Database** | MySQL 8.x |
| **API Testing** | Postman |
| **Browser Testing** | Chrome DevTools |
| **Design** | Figma (mockup) |
| **Documentation** | Markdown |
| **Communication** | Slack, Microsoft Teams |

---

## 6. Risk Management

### 6.1 Risk Register

| Risk | Impact | Probability | Mitigation Strategy | Contingency Plan |
|------|--------|-------------|------------------|----------------|
| **Requirement tidak clear** | High | Medium | Frequent communication dengan stakeholder, demo setiap sprint | Update requirement sebelum development |
| **Technical debt accumulation** | High | Medium | Code review, refactoring time setiap sprint | Reserve 1 sprint untuk refactoring |
| **Team member tidak available** | High | Low | Knowledge sharing, code documentation | Backup developer atau reallocate task |
| **Performance tidak memenuhi NFR** | Medium | Low | Early performance testing, optimization | Optimize query, add caching |
| **Security breach** | High | Low | Security testing, code review, best practices | Security patch immediately |
| **Data migration gagal** | High | Medium | Thorough testing, backup before migration | Manual data entry jika perlu |
| **User adoption rendah** | Medium | Medium | Early involvement, training, user-friendly UI | Additional training session |
| **Scope creep** | High | Medium | Strict change control, prioritize MVP | Defer non-essential features |

### 6.2 Issue Escalation Matrix

| Issue Type | Level 1 | Level 2 | Level 3 |
|------------|--------|--------|--------|
| **Technical Issue** | Dev Lead → Tech Lead | → PM → Stakeholder | → Management |
| **Requirement Issue** | Dev → PM | → Stakeholder | → Management |
| **Resource Issue** | Dev Lead → PM | → Management | - |
| **Timeline Issue** | Dev Lead → PM | → Stakeholder | → Management |
| **Quality Issue** | QA → Dev Lead | → PM | → Stakeholder |

---

## 7. Quality Assurance

### 7.1 Testing Strategy

**Testing Pyramid:**
```
        /\
       /  \
      / E2E \         ← End-to-End testing (10%)
     /------\
    /        \
   / Integration \    ← Integration testing (30%)
  /______________\
 /   Unit Tests    \   ← Unit testing (60% |
/__________________\
```

### 7.2 Testing Types

| Testing Type | Coverage | Tools | Frequency |
|--------------|----------|-------|-----------|
| **Unit Test** | 80% code coverage | JUnit, Mockito | Setiap selesai coding |
| **Integration Test** | All API endpoints | Postman, RestAssured | Setiap sprint |
| **Security Test** | OWASP Top 10 | Burp Suite, manual check | Sprint 11 |
| **Performance Test** | Response time < 3s | JMeter | Sprint 11 |
| **Usability Test** | User friendly | User feedback | Sprint 11 |
| **Cross-browser Test** | Chrome, Firefox, Edge, Safari | Manual | Sprint 11 |
| **Regression Test** | No bug reappear | Automated + manual | Setiap release |

### 7.3 Definition of Done (DoD)

**Setiap Task/Feature:**
- [ ] Code completed (100%)
- [ ] Unit test written and passing (80% coverage)
- [ ] Code review done
- [ ] Integration test passing
- [ ] Documented (code comment + user guide)
- [ ] No critical/high bug
- [ ] Performance acceptable

**Setiap Sprint:**
- [ ] All planned features completed
- [ ] All DoD per task terpenuhi
- [ ] Sprint review dilakukan
- [ ] Sprint retrospective dilakukan
- [ ] Bug dari sprint sebelumnya < 5

### 7.4 Acceptance Criteria

**Per Release:**
- [ ] All functional requirements met
- [ ] All non-functional requirements met
- [ ] Zero critical bug
- [ ] Zero high bug
- [ ] Medium bug < 5
- [ ] Low bug < 20
- [ ] Performance test pass
- [ ] Security test pass
- [ ] Documentation complete

---

## 8. Deployment Strategy

### 8.1 Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Production Environment                       │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────┐    ┌───────────┐    ┌────────────────────────┐    │
│  │ Nginx   │───▶│ Tomcat    │───▶│  MySQL 8.x             │    │
│  │ (Reverse│    │ (Embedded)│    │  Port 3306             │    │
│  │ Proxy)  │    └───────────┘    └────────────────────────┘    │
│  └─────────┘                                                     │
│       │                                                          │
│       ▼                                                          │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  hris-sederaha.jar (SpringBoot Application)              │  │
│  │  - Context: /hris-app                                      │  │
│  │  - Port: 8080                                               │  │
│  │  - Health Check: /actuator/health                         │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                        Staging Environment                          │
├─────────────────────────────────────────────────────────────────┤
│  Same configuration as production (scaled down)                 │
│  - Used for UAT                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 8.2 Deployment Steps

**Sprint Deployment (ke Staging):**
1. Build JAR: `mvn clean package`
2. Stop application: `systemctl stop hris`
3. Backup database: `mysqldump`
4. Copy JAR ke server
5. Start application: `systemctl start hris`
6. Run migration (jika perlu)
7. Verify health check: `curl http://localhost:8080/actuator/health`

**Production Deployment:**
1. Final build: `mvn clean package -Pprod`
2. Full database backup
3. Deploy to production server
4. Smoke testing
5. Monitor logs
6. GO-LIVE announcement

### 8.3 Rollback Plan

**Kriteria Rollback:**
- Critical bug found post-deployment
- Performance degradation > 50%
- Data corruption
- Security breach

**Rollback Steps:**
1. Stop application
2. Restore database dari backup
3. Deploy previous stable version
4. Verify health check
5. Investigate issue

---

## 9. Handover & Training

### 9.1 Training Plan

| Target Audience | Duration | Topics |
|----------------|----------|--------|
| **ADMIN** | 4 jam | User management, dashboard navigation, activity log, system configuration |
| **HR** | 6 jam | Employee CRUD, Dosen CRUD, Leave approval, Payroll generation, Reports |
| **Employee/DOSEN** | 2 jam | Clock in/out, Request cuti, View profile, View payslip |

### 9.2 Documentation

| Document | Audience | Format |
|----------|----------|--------|
| **User Manual** | End User | PDF/Web |
| **Admin Guide** | ADMIN | PDF/Web |
| **Developer Guide** | Tech Team | Markdown/Git Wiki |
| **API Documentation** | Developers | Swagger/OpenAPI |
| **Troubleshooting Guide** | Operation Team | PDF/Web |

### 9.3 Handover Checklist

| Item | Handover To | Status |
|------|-------------|--------|
| Source Code | Operation Team | [ ] |
| Database Credentials | Operation Team | [ ] |
| Server Access | Operation Team | [ ] |
| Deployment Script | Operation Team | [ ] |
| Monitoring Setup | Operation Team | [ ] |
| Backup Procedure | Operation Team | [ ] |
| Troubleshooting Guide | Operation Team | [ ] |
| User Documentation | HR & Users | [ ] |
| Training Material | HR & Users | [ ] |
| Known Issues | Operation Team | [ ] |
| Future Roadmap | Stakeholder | [ ] |

---

## 10. Maintenance & Support

### 10.1 Support Period

Post-go-live support: **1 bulan** (Sprint 12 extended)

| Type | SLA | Coverage |
|------|-----|----------|
| **Critical** | 2 jam | Pengerjaan terhenti |
| **High** | 4 jam | Fitur utama tidak berfungsi |
| **Medium** | 1 hari | Fitur penting ada bug |
| **Low** | 3 hari | Minor issues |

### 10.2 Maintenance Activities

**Daily:**
- Monitor application health
- Check error logs
- Backup database otomatis (jam 2 pagi)

**Weekly:**
- Review activity log
- Check disk space
- Performance monitoring
- Security scan

**Monthly:**
- Review and update dependencies
- Backup verification
- User feedback review

**Quarterly:**
- Security audit
- Performance review
- Capacity planning

### 10.3 Future Enhancements

**Phase 2 (Post-Launch):**
- Notification system (email/notifikasi)
- Export report ke Excel/PDF
- Overtime calculator
- PPh 21 calculator otomatis
- Advanced filtering & search

**Phase 3 (Future):**
- Mobile app (Android/iOS)
- Integration dengan sistem lain (akuntansi, dll)
- Advanced analytics & reporting
- AI-powered recommendation

---

## 11. Budget Estimate

### 11.1 Resource Cost

| Resource | Rate/Person/Month | Duration | Total (Person-Month) | Cost |
|----------|------------------|----------|---------------------|------|
| **Project Manager** | Rp15.000.000 | 6 bulan | 3 PM | Rp45.000.000 |
| **Tech Lead** | Rp20.000.000 | 6 bulan | 6 TL | Rp120.000.000 |
| **Backend Dev 1** | Rp15.000.000 | 6 bulan | 6 BD | Rp90.000.000 |
| **Backend Dev 2** | Rp15.000.000 | 6 bulan | 6 BD | Rp90.000.000 |
| **Frontend Dev** | Rp15.000.000 | 6 bulan | 6 FD | Rp90.000.000 |
| **QA Engineer** | Rp12.000.000 | 6 bulan | 6 QA | Rp72.000.000 |
| **DevOps Engineer** | Rp15.000.000 | 6 bulan (25%) | 1.5 DO | Rp22.500.000 |
| **SUBTOTAL** | | | | **Rp529.500.000** |

### 11.2 Infrastructure Cost

| Item | Cost | Period |
|------|------|--------|
| **VPS/Server** | Rp1.500.000/bulan | 6 bulan = Rp9.000.000 |
| **Domain** | Rp150.000/tahun | 1 tahun |
| **SSL Certificate** | Rp500.000/tahun | 1 tahun |
| **Backup Storage** | Rp200.000/bulan | 6 bulan = Rp1.200.000 |
| **SUBTOTAL** | | | **Rp10.850.000** |

### 11.3 Contingency

**Contingency:** 15% dari total resource cost

| Item | Calculation | Amount |
|------|------------|--------|
| **Resource Contingency** | 15% × Rp529.500.000 | Rp79.425.000 |
| **TOTAL PROJECT COST** | Rp529.500.000 + Rp10.850.000 + Rp79.425.000 | **Rp619.775.000** |

---

## 12. Success Metrics & KPIs

### 12.1 Project Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| **On-Time Delivery** | 6 bulan | Selesai sesuai timeline |
| **Budget** | ±10% | Tidak melebihi Rp619.775.000 ±10% |
| **Scope** | 100% | Semua 34 requirements terimplementasi |
| **Quality** | < 20 bugs low | Bug count saat go-live |

### 12.2 Post-Launch KPIs

| KPI | Target | Measurement Period |
|-----|--------|------------------|
| **User Adoption** | 90% | 3 bulan post-launch |
| **Data Completeness** | 100% sesuai regulasi | 1 bulan post-launch |
| **System Uptime** | 99% | Monthly |
| **Response Time** | < 3 detik | Monthly average |
| **User Satisfaction** | ≥ 4/5 | Quarterly survey |
| **Zero Downtime** | 0 unplanned outage | Monthly |

---

## 13. Communication Plan

### 13.1 Meeting Schedule

| Meeting | Frequency | Duration | Participants | Purpose |
|---------|-----------|----------|------------|---------|
| **Daily Standup** | Daily | 15 menit | Dev Team | Progress, blockers, plan |
| **Sprint Planning** | Sprint start | 2 jam | Dev Team | Plan sprint backlog |
| **Sprint Review** | Sprint end | 1 jam | All Stakeholders | Demo sprint outcome |
| **Sprint Retrospective** | Sprint end | 1 jam | Dev Team | Lessons learned |
| **Weekly Review** | Weekly | 1 jam | PM + Stakeholders | Project status |
| | | | | | |

### 13.2 Reporting

| Report | Frequency | Audience | Format |
|--------|-----------|----------|--------|
| **Daily Standup Report** | Daily | Dev Team | Slack/Teams |
| **Sprint Report** | Per Sprint | Stakeholder | Email/PDF |
| **Monthly Status Report** | Monthly | Stakeholder | Email/PDF |
| **UAT Report** | Setelah UAT | Stakeholder | PDF |

---

## 14. Appendix

### 14.1 Sprint Schedule Detail

| Sprint | Period | Focus | Deliverable |
|--------|--------|-------|------------|
| **Sprint 1** | Wk 1-2 | Foundation | Login, Role Selection, Base Layout |
| **Sprint 2** | Wk 3-4 | Dashboard & User | Dashboard, User Management |
| **Sprint 3** | Wk 5-6 | Master Data | Department, Position |
| **Sprint 4** | Wk 7-8 | Employee | Employee Management |
| **Sprint 5** | Wk 9-10 | Lecturer | Lecturer Management |
| **Sprint 6** | Wk 11 | Attendance | Clock In/Out, History |
| **Sprint 7** | Wk 12 | Leave | Leave Request, Approval |
| **Sprint 8** | Wk 13-14 | Payroll Karyawan | Generate Payroll, Payslip |
| **Sprint 9** | Wk 15-16 | Payroll Dosen | Lecturer Payroll |
| **Sprint 10** | Wk 17 | Activity Log | Logging System |
| **Sprint 11** | Wk 18-20 | Testing | Integration, Security, Performance |
| **Sprint 12** | Wk 21-24 | UAT & Deploy | UAT, Deployment, Go-Live |

### 14.2 Glossary

| Term | Definition |
|------|------------|
| **Sprint** | Iterasi development 2 minggu |
| **DoD** | Definition of Done - kriteria penyelesaian task |
| **UAT** | User Acceptance Testing - testing oleh user |
| **AOP** | Aspect-Oriented Programming - pemisahan cross-cutting concern |
| **SSR** | Server-Side Rendering - rendering di server |
| **Soft Delete** | Hapus logis (data tetap ada di database) |

### 14.3 References

1. Business Requirement Document (BRD)
2. Functional Requirement Specification (FRS)
3. Technical Design Document (RANCANGAN.md)
4. Agile/Scrum Guide
5. SpringBoot Documentation
6. HTMX Documentation

---

## 15. Approval

| Role | Name | Signature | Date |
|------|------|-----------|-------|
| **Project Sponsor** | | | |
| **Product Owner** | | | |
| **Tech Lead** | | | |
| **Business Analyst** | | | |
| **QA Lead** | | | |

---

**END OF IMPLEMENTATION PLAN**
