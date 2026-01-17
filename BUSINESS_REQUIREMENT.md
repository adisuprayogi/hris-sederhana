# Business Requirement Document (BRD)
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

## 1. Executive Summary

### 1.1 Project Overview

**HRIS Sederhana** adalah sistem informasi manajemen sumber daya manusia berbasis web yang dirancang untuk mengelola data karyawan dan dosen secara terintegrasi. Sistem ini menggunakan arsitektur monolitik dengan teknologi SpringBoot untuk backend dan HTMX + Alpine.js + Tailwind CSS untuk frontend.

### 1.2 Business Problem

Saat ini, pengelolaan data karyawan dan dosen dilakukan secara manual dengan berbagai sistem yang terpisah, menyebabkan:
- Data tidak terintegrasi antara modul employee dan dosen
- Proses absensi dan cuti masih manual
- Penggajian tidak terotomatisasi
- Tidak ada audit trail untuk aktivitas user
- Tidak mematuhi regulasi pemerintah Indonesia terkait kelengkapan data

### 1.3 Proposed Solution

Membangun sistem HRIS terintegrasi yang mencakup:
1. Manajemen data karyawan lengkap sesuai regulasi Indonesia
2. Manajemen data dosen dengan homebase prodi
3. Sistem absensi dengan clock in/clock out
4. Manajemen cuti dengan approval workflow
5. Penggajian karyawan dan dosen
6. Role-based access control dengan multi-role support
7. Activity logging untuk audit trail
8. Soft delete untuk data retention

### 1.4 Key Benefits

| Benefit | Description |
|---------|-------------|
| **Efisiensi** | Otomatisasi proses HR manual |
| **Kepatuhan** | Sesuai regulasi Indonesia (UU Ketenagakerjaan, BPJS, PDDIKTI) |
| **Transparansi** | Audit trail lengkap untuk semua aktivitas |
| **Integrasi** | Data karyawan dan dosen terintegrasi |
| **Akurasi** | Penggajian terotomatisasi dan akurat |

---

## 2. Business Background

### 2.1 Current State Analysis

**Proses saat ini:**
- Data karyawan disimpan dalam spreadsheet terpisah
- Data dosen dikelola dalam sistem berbeda
- Absensi manual dengan formulir kertas
- Cuti diajukan via email/formulir kertas
- Penggajian dihitung manual di spreadsheet
- Tidak ada tracking aktivitas user
- Tidak ada audit trail untuk perubahan data

**Pain Points:**
- Data redundant dan tidak konsisten
- Proses approval cuti lambat
- Penggajian rawan human error
- Sulit melakukan tracking perubahan data
- Tidak memenuhi kelengkapan data sesuai regulasi

### 2.2 Target State

**Setelah implementasi:**
- Single source of truth untuk data SDM
- Proses absensi dan cuti terotomatisasi
- Penggajian terotomatisasi berdasarkan absensi
- Audit trail lengkap untuk semua aktivitas
- Data sesuai regulasi pemerintah Indonesia
- Akses role-based untuk keamanan data

---

## 3. Business Objectives

### 3.1 Primary Objectives

| Objective | Success Metric | Target |
|-----------|----------------|--------|
| **Otomatisasi HR** | Proses manual yang terotomatisasi | 80% proses terotomatis |
| **Kepatuhan Regulasi** | Kelengkapan data sesuai regulasi | 100% data lengkap |
| **Efisiensi Cuti** | Waktu approval cuti | Max 1 hari |
| **Akurasi Penggajian** | Pengurangan error penggajian | 99% akurasi |
| **Audit Trail** | Semua aktivitas tercatat | 100% aktivitas logged |

### 3.2 Secondary Objectives

- Meningkatkan transparansi data SDM
- Memudahkan pelaporan ke pemerintah (WLKP, BPJS, PDDIKTI)
- Mengurangi penggunaan kertas
- Menyediakan dashboard untuk monitoring

---

## 4. Stakeholder Analysis

| Stakeholder | Interest | Influence |
|-------------|----------|-----------|
| **Top Management** | Dashboard, laporan, penggajian | High |
| **HR Department** | Manajemen employee & dosen, cuti, penggajian | High |
| **Dosen** | Profile dosen, homebase prodi, penggajian | Medium |
| **Karyawan** | Self-service data, absensi, cuti, slip gaji | Medium |
| **IT Department** | Maintenance, security, support | High |
| **Keuangan** | Data penggajian untuk pembayaran | High |

---

## 5. Functional Requirements

### 5.1 Authentication & Authorization

| ID | Requirement | Priority |
|----|------------|----------|
| **FR-AUTH-001** | Sistem harus mendukung login dengan email dan password | Mandatory |
| **FR-AUTH-002** | Sistem harus mendukung multi-role per user | Mandatory |
| **FR-AUTH-003** | User harus memilih role setelah login jika memiliki lebih dari 1 role | Mandatory |
| **FR-AUTH-004** | Sesi user harus menyimpan role yang sedang aktif | Mandatory |
| **FR-AUTH-005** | User dapat switch role tanpa logout | Mandatory |
| **FR-AUTH-006** | Password harus di-hash menggunakan BCrypt | Mandatory |
| **FR-AUTH-007** | Sesi harus expire setelah 1 jam inactivity | Mandatory |

### 5.2 User Management

| ID | Requirement | Priority |
|----|------------|----------|
| **FR-USER-001** | ADMIN dapat CRUD user | Mandatory |
| **FR-USER-002** | ADMIN dapat assign role ke user (bisa multiple) | Mandatory |
| **FR-USER-003** | ADMIN dapat reset password user | Mandatory |
| **FR-USER-004** | ADMIN dapat aktivasi/deaktivasi user | Mandatory |
| **FR-USER-005** | Role yang tersedia: ADMIN, HR, EMPLOYEE, DOSEN | Mandatory |

### 5.3 Employee Management

| ID | Requirement | Priority |
|----|------------|----------|
| **FR-EMP-001** | HR dapat CRUD data karyawan | Mandatory |
| **FR-EMP-002** | Data karyawan minimal mencakup: NIK, nama lengkap, tempat/tanggal lahir, gender, nama ibu kandung, alamat, telepon, email | Mandatory |
| **FR-EMP-003** | Data pekerjaan: status kepegawaian, tanggal mulai, lokasi kerja, department, position | Mandatory |
| **FR-EMP-004** | Data BPJS & Pajak: nomor BPJS Ketenagakerjaan, nomor BPJS Kesehatan, NPWP, gaji pokok | Mandatory |
| **FR-EMP-005** | Data keluarga: nomor KK, status pernikahan, nama pasangan, jumlah tanggungan | Mandatory |
| **FR-EMP-006** | Upload foto profil | Mandatory |
| **FR-EMP-007** | Assign role EMPLOYEE dan/atau DOSEN | Mandatory |
| **FR-EMP-008** | Karyawan dapat view dan edit data diri sendiri (terbatas) | Mandatory |

### 5.4 Department & Position Management

| ID | Requirement | Priority |
|----|------------|----------|
| **FR-DEPT-001** | CRUD department dengan flag prodi | Mandatory |
| **FR-DEPT-002** | Department memiliki field: is_prodi (BOOLEAN) dan kode_prodi (VARCHAR) | Mandatory |
| **FR-DEPT-003** | CRUD position/jabatan | Mandatory |
| **FR-DEPT-004** | Position memiliki base salary | Mandatory |

### 5.5 Lecturer Management

| ID | Requirement | Priority |
|----|------------|----------|
| **FR-LEC-001** | Dosen adalah Employee dengan role DOSEN | Mandatory |
| **FR-LEC-002** | CRUD profile dosen: NIDN, pendidikan terakhir, bidang keahlian | Mandatory |
| **FR-LEC-003** | Status kepegawaian dosen: Dosen Tetap atau Dosen Tidak Tetap | Mandatory |
| **FR-LEC-004** | Jenjang dosen: Asisten Ahli, Lektor, Lektor Kepala, Profesor | Mandatory |
| **FR-LEC-005** | Status kerja dosen: Aktif, Cuti, Pensiun | Mandatory |
| **FR-LEC-006** | Homebase prodi harus link ke department dengan is_prodi = true | Mandatory |
| **FR-LEC-007** | Dosen dapat view dan edit profile sendiri (terbatas) | Mandatory |

### 5.6 Attendance Management

| ID | Requirement | Priority |
|----|------------|----------|
| **FR-ATT-001** | Karyawan/dosen dapat Clock In | Mandatory |
| **FR-ATT-002** | Karyawan/dosen dapat Clock Out | Mandatory |
| **FR-ATT-003** | Sistem mencatat waktu dan lokasi clock in/out | Mandatory |
| **FR-ATT-004** | Status absensi: Hadir, Telat, Izin, Sakit, Alpha | Mandatory |
| **FR-ATT-005** | HR dapat view semua data absensi | Mandatory |
| **FR-ATT-006** | Karyawan/dosen dapat view riwayat absensi sendiri | Mandatory |
| **FR-ATT-007** | Rekap absensi bulanan | Mandatory |

### 5.7 Leave Management

| ID | Requirement | Priority |
|----|------------|----------|
| **FR-LEAVE-001** | Karyawan/dosen dapat mengajukan cuti | Mandatory |
| **FR-LEAVE-002** | Tipe cuti: Tahunan, Sakit, Melahirkan, Menikah, Khusus, Tanpa Gaji | Mandatory |
| **FR-LEAVE-003** | Setiap pengajuan cuti harus ada alasan | Mandatory |
| **FR-LEAVE-004** | HR/ADMIN dapat approve atau reject cuti | Mandatory |
| **FR-LEAVE-005** | Cuti disetujui akan mengurangi saldo cuti | Mandatory |
| **FR-LEAVE-006** | Saldo cuti: 12 hari tahunan + 14 hari sakit per tahun | Mandatory |
| **FR-LEAVE-007** | User dapat view saldo cuti sendiri | Mandatory |
| **FR-LEAVE-008** | HR dapat view semua pengajuan cuti | Mandatory |

### 5.8 Payroll Management

| ID | Requirement | Priority |
|----|------------|----------|
| **FR-PAY-001** | Komponen gaji karyawan: gaji pokok, tunjangan, lembur, potongan | Mandatory |
| **FR-PAY-002** | Komponen gaji dosen: gaji pokok, tunjangan sertifikasi, tunjangan fungsional, honor mengajar | Mandatory |
| **FR-PAY-003** | Generate payroll berdasarkan periode (YYYY-MM) | Mandatory |
| **FR-PAY-004** | Status payroll: Draft, Paid | Mandatory |
| **FR-PAY-005** | Slip gaji dapat dicetak/di-download | Mandatory |
| **FR-PAY-006** | Karyawan/dosen dapat view slip gaji sendiri | Mandatory |
| **FR-PAY-007** | HR/ADMIN dapat generate dan view semua payroll | Mandatory |
| **FR-PAY-008** | Rekap gaji bulanan | Mandatory |

### 5.9 Activity Logging

| ID | Requirement | Priority |
|----|------------|----------|
| **FR-LOG-001** | Semua aktivitas user wajib dicatat | Mandatory |
| **FR-LOG-002** | Aktivitas yang dicatat: Login, Logout, Role Selection, CRUD, Clock In/Out, Submit/Approve Cuti, Generate Payroll, View Sensitive Data | Mandatory |
| **FR-LOG-003** | Informasi yang dicatat: user, aktivitas, modul, entity, timestamp, IP address, user agent, status, error message | Mandatory |
| **FR-LOG-004** | ADMIN dapat view semua activity log | Mandatory |
| **FR-LOG-005** | Filter activity log berdasarkan: user, modul, tipe aktivitas, rentang waktu, status | Mandatory |
| **FR-LOG-006** | Activity log tidak dapat dihapus (permanent) | Mandatory |

### 5.10 Dashboard

| ID | Requirement | Priority |
|----|------------|----------|
| **FR-DASH-001** | Dashboard ADMIN: total employee, total dosen, total departments, absensi hari ini, pending leave request, recent activities | Mandatory |
| **FR-DASH-002** | Dashboard HR: total employee, total dosen, absensi hari ini, pending leave, payroll bulan ini | Mandatory |
| **FR-DASH-003** | Dashboard EMPLOYEE/DOSEN: profile summary, clock in/out button, leave balance, recent leave, recent payslip | Mandatory |
| **FR-DASH-004** | Quick actions di dashboard sesuai role | Mandatory |

---

## 6. Non-Functional Requirements

### 6.1 Performance

| ID | Requirement | Target |
|----|------------|--------|
| **NFR-PERF-001** | Response time untuk login | < 2 detik |
| **NFR-PERF-002** | Response time untuk load data list | < 3 detik |
| **NFR-PERF-003** | Response time untuk CRUD operation | < 2 detik |
| **NFR-PERF-004** | Response time untuk generate payroll | < 10 detik |
| **NFR-PERF-005** | Concurrent users | Minimal 50 user simultan |

### 6.2 Security

| ID | Requirement | Target |
|----|------------|--------|
| **NFR-SEC-001** | Password hashing | BCrypt |
| **NFR-SEC-002** | Session timeout | 1 jam inactivity |
| **NFR-SEC-003** | HTTPS untuk production | Wajib |
| **NFR-SEC-004** | CSRF protection | Wajib |
| **NFR-SEC-005** | Role-based access control | Wajib |
| **NFR-SEC-006** | Input validation | Wajib |
| **NFR-SEC-007** | SQL injection prevention | Wajib |
| **NFR-SEC-008** | XSS prevention | Wajib |

### 6.3 Availability

| ID | Requirement | Target |
|----|------------|--------|
| **NFR-AVAIL-001** | Uptime | 99% |
| **NFR-AVAIL-002** | Backup database | Harian |
| **NFR-AVAIL-003** | Recovery time objective (RTO) | 4 jam |
| **NFR-AVAIL-004** | Recovery point objective (RPO) | 24 jam |

### 6.4 Scalability

| ID | Requirement | Target |
|----|------------|--------|
| **NFR-SCALE-001** | Total employee | Hingga 500 employee |
| **NFR-SCALE-002** | Total dosen | Hingga 200 dosen |
| **NFR-SCALE-003** | Activity log records | Hingga 1 juta records |
| **NFR-SCALE-004** | Storage | Minimal 10 GB |

### 6.5 Usability

| ID | Requirement | Target |
|----|------------|--------|
| **NFR-USE-001** | UI responsive | Mobile & desktop |
| **NFR-USE-002** | Accessibility | WCAG 2.1 Level A |
| **NFR-USE-003** | Browser support | Chrome, Firefox, Edge, Safari (versi terbaru) |
| **NFR-USE-004** | Learning curve | Minimal training required |

### 6.6 Maintainability

| ID | Requirement | Target |
|----|------------|--------|
| **NFR-MAINT-001** | Code structure | Modular, monolitik |
| **NFR-MAINT-002** | Documentation | Code komentar + user manual |
| **NFR-MAINT-003** | Logging | Comprehensive logging |
| **NFR-MAINT-004** | Error handling | Graceful error handling |

---

## 7. Data Requirements

### 7.1 Data Entities

**Primary Entities:**
1. Employees
2. Employee Roles
3. Departments
4. Positions
5. Lecturer Profiles
6. Lecturer Salaries
7. Attendances
8. Leave Requests
9. Leave Balances
10. Payrolls
11. User Sessions
12. User Activity Logs

### 7.2 Data Completeness (Regulatory)

**Data Karyawan - Wajib (sesuai UU Ketenagakerjaan & BPJS):**

| Kategori | Field |
|----------|-------|
| **Identitas** | NIK, Nama Lengkap, Tempat Lahir, Tanggal Lahir, Gender, Nama Ibu Kandung |
| **Kontak** | Alamat, Telepon, Email |
| **Kepegawaian** | Status Kepegawaian, Tanggal Mulai, Lokasi Kerja |
| **BPJS** | No. BPJS Ketenagakerjaan, No. BPJS Kesehatan |
| **Pajak** | NPWP |
| **Gaji** | Gaji Pokok |
| **Keluarga** | No. KK, Status Pernikahan, Nama Pasangan, Jumlah Tanggungan |

**Data Dosen - Wajib (sesuai PDDIKTI):**

| Kategori | Field |
|----------|-------|
| **Identitas** | NIDN, Pendidikan Terakhir, Bidang Keahlian |
| **Akademik** | Jenjang (Asisten Ahli/Lektor/Lektor Kepala/Profesor) |
| **Kepegawaian** | Status (Dosen Tetap/Tidak Tetap), Status Kerja (Aktif/Cuti/Pensiun) |
| **Homebase** | Prodi (link ke department dengan is_prodi=true) |

### 7.3 Data Retention

| Data Tipe | Retensi |
|-----------|---------|
| Data Employee/HR | 10 tahun setelah hubungan kerja berakhir (UU No. 8/1997) |
| Activity Log | 1 tahun (configurable) |
| Payroll Data | 10 tahun |
| Attendance Data | 10 tahun |

### 7.4 Soft Delete Policy

- Semua data TIDAK BOLEH dihapus secara fisik (hard delete)
- Gunakan soft delete dengan field `deleted_at`
- Data yang dihapus masih dapat di-restore
- Permanent delete hanya setelah retensi period berakhir

---

## 8. Regulatory Compliance

### 8.1 Indonesian Regulations

| Regulation | Compliance Requirement |
|------------|----------------------|
| **UU No. 13 Tahun 2003** | Pasal 185: Wajib simpan "Daftar Tenaga Kerja" dan "Daftar Upah" |
| **UU No. 8 Tahun 1997** | Retensi dokumen minimal 10 tahun |
| **Permenaker** | Wajib Lapor Ketenagakerjaan Perusahaan (WLKP) |
| **BPJS Ketenagakerjaan** | Wajib daftarkan seluruh pekerja, max upah Rp9.559.600 |
| **BPJS Kesehatan** | Wajib daftarkan seluruh pekerja dan keluarga |
| **PDDIKTI** | Kelengkapan data dosen sesuai standar Kemendikbud |
| **Permendikbudristek No. 44/2024** | Standar data dosen Indonesia |

### 8.2 Data Privacy

- Tidak mencatat password dalam log
- Tidak menampilkan data sensitif di log kecuali diperlukan
- Data pribadi dilindungi sesuai UU Perlindungan Data Pribadi

---

## 9. User Roles & Access Control

### 9.1 Role Definitions

| Role | Deskripsi | Akses |
|------|-----------|-------|
| **ADMIN** | Administrator | Full access termasuk user management |
| **HR** | HR Staff | Employee & dosen data, attendance, leave, payroll |
| **EMPLOYEE** | Karyawan | Self-service data diri, absensi, cuti, slip gaji |
| **DOSEN** | Dosen | Self-service + data dosen, absensi, cuti, slip gaji dosen |

### 9.2 Access Matrix

| Fitur | ADMIN | HR | EMPLOYEE | DOSEN |
|-------|-------|-------|----------|-------|
| Login | ✅ | ✅ | ✅ | ✅ |
| User Management | ✅ | ❌ | ❌ | ❌ |
| Employee CRUD | ✅ | ✅ | ❌ | ❌ |
| Dosen CRUD | ✅ | ✅ | ❌ | ❌ |
| Department/Position | ✅ | ✅ | ❌ | ❌ |
| Attendance (all) | ✅ | ✅ | ❌ | ❌ |
| Attendance (mine) | ✅ | ✅ | ✅ | ✅ |
| Leave Request (all) | ✅ | ✅ | ❌ | ❌ |
| Leave Request (mine) | ✅ | ✅ | ✅ | ✅ |
| Leave Approval | ✅ | ✅ | ❌ | ❌ |
| Payroll (all) | ✅ | ✅ | ❌ | ❌ |
| Payroll (mine) | ❌ | ❌ | ✅ | ✅ |
| Activity Logs | ✅ | ❌ | ❌ | ❌ |

### 9.3 Multi-Role Support

- User dapat memiliki lebih dari 1 role
- Setelah login, user memilih role yang akan digunakan
- User dapat switch role tanpa logout
- Akses fitur ditentukan oleh role yang sedang aktif

---

## 10. Technical Requirements

### 10.1 Technology Stack

| Layer | Technology | Versi |
|-------|-----------|-------|
| **Backend** | SpringBoot | 3.x |
| **Build Tool** | Maven | Latest |
| **Frontend** | HTMX + Alpine.js + Tailwind CSS | Latest |
| **Database** | MySQL | 8.x |
| **ORM** | Spring Data JPA (Hibernate) | - |
| **Security** | Spring Security | - |
| **Template Engine** | Thymeleaf | - |
| **Java** | JDK | 17+ |

### 10.2 Architecture

- **Monolithic Architecture**: Single application, single database
- **Server-Side Rendering**: Thymeleaf templates with HTMX for dynamic interactions
- **Layered Architecture**: Controller → Service → Repository

### 10.3 Infrastructure

| Komponen | Requirement |
|----------|-------------|
| **Server** | VPS atau dedicated server minimal 2 CPU, 4 GB RAM |
| **OS** | Linux (Ubuntu/Debian/CentOS) |
| **Web Server** | Nginx (reverse proxy) |
| **Application Server** | Embedded Tomcat (SpringBoot) |
| **Database** | MySQL 8.x |
| **Backup** | Automated daily backup |

### 10.4 Deployment

- Single JAR deployment
- Environment configuration via Spring profiles
- Database migration via Flyway/Liquibase (optional)

---

## 11. Implementation Phases

### Phase 1: Foundation & Authentication (Sprint 1-2)

| Task | Deliverable |
|------|-------------|
| Setup SpringBoot project | Project structure |
| Setup database connection | MySQL connected |
| Implement Spring Security | Login working |
| Implement role selection | Role selection page |
| Create layout template | Base layout with header/sidebar |

### Phase 2: Master Data (Sprint 3-4)

| Task | Deliverable |
|------|-------------|
| Department CRUD (dengan is_prodi & kode_prodi) | Department management |
| Position CRUD | Position management |
| Employee CRUD | Employee management |
| User management (ADMIN only) | User management |
| Role assignment | Role assignment working |

### Phase 3: Lecturer Data (Sprint 5-6)

| Task | Deliverable |
|------|-------------|
| LecturerProfile CRUD | Dosen management |
| Employee-Lecturer relation | Dosen = employee dengan role DOSEN |
| Homebase prodi linking | Link ke department is_prodi=true |
| Jenjang & status dosen | Jenjang dan status working |

### Phase 4: Attendance (Sprint 7)

| Task | Deliverable |
|------|-------------|
| Clock In/Out feature | Absensi working |
| Attendance history | Riwayat absensi |
| Attendance report (ADMIN/HR) | Laporan absensi |
| Calendar view | Kalender absensi |

### Phase 5: Leave Management (Sprint 8)

| Task | Deliverable |
|------|-------------|
| Leave request form | Form cuti working |
| Leave approval (HR/ADMIN) | Approval workflow |
| Leave balance tracking | Saldo cuti terupdate |
| Leave report | Laporan cuti |

### Phase 6: Payroll - Karyawan (Sprint 9)

| Task | Deliverable |
|------|-------------|
| Payroll calculation | Perhitungan gaji |
| Generate payroll | Generate working |
| Payslip generation | Slip gaji dapat di-print |
| Payroll report | Laporan gaji |

### Phase 7: Payroll - Dosen (Sprint 10)

| Task | Deliverable |
|------|-------------|
| Lecturer payroll calculation | Perhitungan gaji dosen |
| Generate lecturer payroll | Generate working |
| Dosen payslip | Slip gaji dosen |
| Lecturer payroll report | Laporan gaji dosen |

### Phase 8: Activity Logging (Sprint 11)

| Task | Deliverable |
|------|-------------|
| @LogActivity annotation | Annotation working |
| ActivityLogAspect | AOP logging working |
| Activity log service | Log service working |
| Activity log dashboard | Dashboard & filter working |

### Phase 9: Testing & Deployment (Sprint 12)

| Task | Deliverable |
|------|-------------|
| Integration testing | All features tested |
| Security testing | Security tested |
| Performance testing | Performance tested |
| UAT | User acceptance test |
| Production deployment | Live deployment |

---

## 12. Reporting Requirements

### 12.1 Mandatory Reports

| Report | Frequency | Recipient |
|--------|-----------|-----------|
| **Daftar Tenaga Kerja** | Real-time | HR, Management |
| **Daftar Upah** | Bulanan | HR, Keuangan |
| **Rekap Absensi Bulanan** | Bulanan | HR, Management |
| **Rekap Cuti Bulanan** | Bulanan | HR, Management |
| **Laporan Gaji Bulanan** | Bulanan | HR, Keuangan |
| **Activity Log** | Real-time | Admin, HR |
| **Laporan Dosen per Prodi** | Bulanan | HR, Management |

### 12.2 Export Format

- PDF untuk laporan resmi
- Excel untuk data analysis

---

## 13. Constraints & Assumptions

### 13.1 Constraints

| Constraint | Description |
|------------|-------------|
| **Budget** | Terbatas, gunakan open source technology |
| **Timeline** | Target 6 bulan untuk MVP |
| **Resources** | Team development 3-5 orang |
| **Infrastructure** | Single server deployment |
| **Legacy** | Integrasi dengan sistem existing belum diprioritaskan |

### 13.2 Assumptions

| Assumption | Description |
|------------|-------------|
| **Users** | Semua user melek teknologi, dapat menggunakan web application |
| **Internet** | Koneksi internet tersedia di lokasi kerja |
| **Data** | Data existing dapat di-import dari spreadsheet |
| **Training** | User memerlukan training minimal |
| **Mobile** | Akses mobile via responsive web (tanpa native app) |

---

## 14. Risks & Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **User resistance** | High | Medium | Early user involvement, training |
| **Data migration** | High | Medium | Thorough testing, backup |
| **Performance issue** | Medium | Low | Proper indexing, caching |
| **Security breach** | High | Low | Security best practices, audit |
| **Regulatory change** | Medium | Low | Flexible design, easy update |
| **Resource constraint** | High | Medium | Prioritize features, phased approach |

---

## 15. Success Criteria

### 15.1 Go-Live Criteria

Sistem dianggap sukses dan dapat go-live jika:

1. ✅ Semua fitur Phase 1-8 berfung sesuai requirement
2. ✅ Semua employee dan dosen dapat di-import
3. ✅ 100% data karyawan dan dosen lengkap sesuai regulasi
4. ✅ Sistem lulus UAT dari semua stakeholder
5. ✅ Performance memenuhi NFR
6. ✅ Security lulus security testing
7. ✅ Activity logging berfungsi 100%
8. ✅ Documentation lengkap

### 15.2 Post-Live Success Metrics

| Metric | Target | Timeframe |
|--------|--------|-----------|
| User adoption | 90% employee menggunakan sistem | 3 bulan |
| Data accuracy | 99% data akurat | 1 bulan |
| Process efficiency | 80% proses terotomatis | 3 bulan |
| User satisfaction | Minimal 4/5 | 3 bulan |

---

## 16. Appendix

### 16.1 Glossary

| Term | Definition |
|------|------------|
| **HRIS** | Human Resource Information System |
| **BPJS** | Badan Penyelenggara Jaminan Sosial |
| **WLKP** | Wajib Lapor Ketenagakerjaan Perusahaan |
| **PDDIKTI** | Pangkalan Data Pendidikan Tinggi |
| **CRUD** | Create, Read, Update, Delete |
| **SSR** | Server-Side Rendering |
| **AOP** | Aspect-Oriented Programming |

### 16.2 References

1. Undang-Undang No. 13 Tahun 2003 tentang Ketenagakerjaan
2. Undang-Undang No. 8 Tahun 1997 tentang Dokumen Perusahaan
3. Peraturan Menteri Ketenagakerjaan tentang WLKP
4. Peraturan BPJS Ketenagakerjaan
5. Permendikbudristek No. 44 Tahun 2024
6. SpringBoot Documentation
7. HTMX Documentation

### 16.3 Approval

| Role | Name | Signature | Date |
|------|------|-----------|-------|
| **Project Sponsor** | | | |
| **Product Owner** | | | |
| **Tech Lead** | | | |
| **HR Manager** | | | |

---

**Document Control**

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 16/01/2026 | HRIS Team | Initial BRD |
| | | | |
| | | | |
