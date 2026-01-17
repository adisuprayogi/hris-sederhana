# Functional Requirement Specification (FRS)
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

1. [Introduction](#1-introduction)
2. [Authentication & Authorization](#2-authentication--authorization)
3. [User Management](#3-user-management)
4. [Employee Management](#4-employee-management)
5. [Department & Position Management](#5-department--position-management)
6. [Lecturer Management](#6-lecturer-management)
7. [Attendance Management](#7-attendance-management)
8. [Leave Management](#8-leave-management)
9. [Payroll Management](#9-payroll-management)
10. [Activity Logging](#10-activity-logging)
11. [Dashboard](#11-dashboard)

---

## 1. Introduction

### 1.1 Purpose

Dokumen ini mendefinisikan functional requirements untuk sistem HRIS Sederhana secara detail. Dokumen ini menjadi panduan untuk development team, QA team, dan stakeholders.

### 1.2 Scope

**In Scope:**
- Manajemen data karyawan dan dosen
- Absensi dengan clock in/clock out
- Manajemen cuti dengan approval workflow
- Penggajian karyawan dan dosen
- Activity logging untuk audit trail
- Multi-role access control

**Out of Scope:**
- Recruitment / applicant tracking
- Performance management
- Training management
- Benefits administration
- Time off request selain cuti
- Employee self-service portal mobile app

### 1.3 User Personas

| Persona | Role | Goals |
|---------|------|-------|
| **Admin** | ADMIN | Manage users, view all data, system configuration |
| **HR Staff** | HR | Manage employee/dosen data, process leave, generate payroll |
| **Dosen** | DOSEN | View profile, clock in/out, request leave, view payslip |
| **Karyawan** | EMPLOYEE | View profile, clock in/out, request leave, view payslip |

---

## 2. Authentication & Authorization

### FR-AUTH-001: User Login

**Description:** User dapat login ke sistem menggunakan email dan password.

**Priority:** Mandatory

**User Story:**
```
AS A user
I WANT TO login to the system using my email and password
SO THAT I can access the system based on my assigned roles
```

**Preconditions:**
- User telah terdaftar di sistem
- User memiliki password yang valid
- User email dan status = ACTIVE

**Main Flow:**
1. User membuka halaman login
2. User memasukkan email dan password
3. User klik tombol "Login"
4. Sistem memvalidasi email dan password
5. Jika valid:
   a. Sistem cek jumlah role user
   b. Jika user memiliki 1 role: langsung ke dashboard
   c. Jika user memiliki >1 role: tampilkan halaman pilih role
6. User dipilih role
7. Sistem buat session dengan selected role
8. User diarahkan ke dashboard sesuai role

**Alternative Flows:**

*Alt Flow 1: Email atau password salah*
- Pada langkah 4: Sistem menemukan email/password tidak valid
- Sistem tampilkan pesan error: "Email atau password salah"
- Sistem catat login failed ke activity log
- Kembali ke langkah 2

*Alt Flow 2: User tidak aktif*
- Pada langkah 4: Sistem menemukan status user = INACTIVE/RESIGNED
- Sistem tampilkan pesan error: "Akun Anda tidak aktif. Hubungi HR/Admin."
- Sistem catat login failed ke activity log
- Flow berakhir

*Alt Flow 3: User tidak memiliki role*
- Pada langkah 5b: Sistem menemukan user tidak memiliki role
- Sistem tampilkan pesan error: "Akun Anda belum memiliki role. Hubungi Admin."
- Sistem catat ke activity log
- Flow berakhir

**Postconditions:**
- User berhasil login dan session dibuat
- Selected role disimpan di session
- Activity log tercatat (LOGIN - SUCCESS)

**Business Rules:**
- BR-AUTH-001: Password minimal 8 karakter
- BR-AUTH-002: Password harus mengandung huruf dan angka
- BR-AUTH-003: Session expire setelah 1 jam inactivity
- BR-AUTH-004: Maksimal 3 percobaan login gagal, akun dikunci 15 menit

**Acceptance Criteria:**
- [ ] User dapat login dengan email dan password valid
- [ ] Password ditampilkan sebagai mask (••••••••)
- [ ] Pesan error tampil jelas jika email/password salah
- [ ] User dengan 1 role langsung ke dashboard
- [ ] User dengan >1 role ditampilkan halaman pilih role
- [ ] Session tercatat dengan selected role
- [ ] Login activity tercatat di activity log
- [ ] Login failed juga tercatat di activity log

---

### FR-AUTH-002: Role Selection

**Description:** User dengan lebih dari 1 role harus memilih role sebelum mengakses fitur sistem.

**Priority:** Mandatory

**User Story:**
```
AS A user with multiple roles
I WANT TO select which role to use for the current session
SO THAT I can access appropriate features for that role
```

**Preconditions:**
- User berhasil login
- User memiliki lebih dari 1 role

**Main Flow:**
1. Setelah login sukses, sistem tampilkan halaman "Pilih Role"
2. Sistem tampilkan pesan: "Welcome, [User Name]! Silakan pilih role:"
3. Sistem tampilkan semua role yang dimiliki user dalam card/button:
   - ADMIN (jika ada)
   - HR (jika ada)
   - EMPLOYEE (jika ada)
   - DOSEN (jika ada)
4. User klik salah satu role
5. Sistem set session dengan selected_role = role yang dipilih
6. Sistem catat activity log: ROLE_SELECTION
7. Sistem redirect ke dashboard sesuai role yang dipilih

**Alternative Flows:**

*Alt Flow 1: User memilih role berbeda di session lain*
- User logout
- User login kembali
- Halaman pilih role tampil kembali
- User dapat memilih role berbeda

**Postconditions:**
- Selected role tersimpan di session
- Dashboard sesuai role ditampilkan
- Activity log tercatat

**Business Rules:**
- BR-AUTH-005: Role selection wajib untuk user dengan >1 role
- BR-AUTH-006: Selected role berlaku untuk seluruh session

**Acceptance Criteria:**
- [ ] Halaman pilih role tampil setelah login untuk user dengan >1 role
- [ ] Nama user ditampilkan di halaman pilih role
- [ ] Semua role user ditampilkan sebagai card/button
- [ ] Klik role menyimpan selected_role ke session
- [ ] Redirect ke dashboard sesuai role yang dipilih
- [ ] Role selection tercatat di activity log

---

### FR-AUTH-003: Role Switching

**Description:** User dapat switch role tanpa logout jika memiliki multiple roles.

**Priority:** Mandatory

**User Story:**
```
AS A user with multiple roles
I WANT TO switch my active role without logging out
SO THAT I can quickly change context without losing my session
```

**Preconditions:**
- User sedang login
- User memiliki lebih dari 1 role
- Session masih valid

**Main Flow:**
1. User klik dropdown "Role: [CURRENT_ROLE]" di header
2. Sistem tampilkan daftar role lain yang dimiliki user
3. User klik role yang ingin digunakan
4. Sistem update session dengan new selected_role
5. Sistem catat activity log: ROLE_SWITCHING
6. Sistem refresh halaman dengan role baru
7. Menu dan akses fitur berubah sesuai role baru

**Alternative Flows:**

*Alt Flow 1: User klik role yang sedang aktif*
- Sistem tidak melakukan apa-apa
- Dropdown tertutup

**Postconditions:**
- Session diupdate dengan new selected_role
- Halaman refresh dengan role baru
- Activity log tercatat

**Business Rules:**
- BR-AUTH-007: Role switching hanya available untuk user dengan >1 role
- BR-AUTH-008: Role switching tidak mengubah session expiration

**Acceptance Criteria:**
- [ ] Dropdown role tampil di header untuk user dengan >1 role
- [ ] Dropdown menampilkan semua role user
- [ ] Klik role mengupdate selected_role di session
- [ ] Halaman refresh otomatis setelah switch role
- [ ] Menu berubah sesuai role baru
- [ ] Role switching tercatat di activity log

---

### FR-AUTH-004: Logout

**Description:** User dapat keluar dari sistem (logout).

**Priority:** Mandatory

**User Story:**
```
AS A logged-in user
I WANT TO logout from the system
SO THAT my session is terminated and my account is secured
```

**Preconditions:**
- User sedang login
- Session valid

**Main Flow:**
1. User klik tombol "Logout" di header
2. Sistem catat activity log: LOGOUT
3. Sistem invalidate session
4. Sistem hapus session data
5. Sistem redirect ke halaman login

**Alternative Flows:**

*Alt Flow 1: Session sudah expire*
- User klik menu/fitur
- Sistem deteksi session expire
- Sistem redirect ke halaman login
- Sistem tampilkan pesan: "Session Anda telah expired. Silakan login kembali."

**Postconditions:**
- User di-logout dari sistem
- Session dihapus
- Activity log tercatat

**Business Rules:**
- BR-AUTH-009: Session expire setelah 1 jam inactivity

**Acceptance Criteria:**
- [ ] Tombol logout tersedia di header
- [ ] Klik logout menghapus session
- [ ] Redirect ke halaman login setelah logout
- [ ] Logout activity tercatat di activity log
- [ ] Session expire otomatis setelah 1 jam inactivity

---

## 3. User Management

### FR-USER-001: Create User

**Description:** ADMIN dapat membuat user baru.

**Priority:** Mandatory

**User Story:**
```
AS AN ADMIN
I WANT TO create a new user
SO THAT new employees can access the system
```

**Preconditions:**
- ADMIN login
- ADMIN memiliki role ADMIN aktif

**Main Flow:**
1. ADMIN klik menu "Users"
2. ADMIN klik tombol "Add User"
3. Sistem tampilkan form create user:
   - NIK (required, unique)
   - Nama Lengkap (required)
   - Email (required, unique)
   - Password (required)
   - Confirm Password (required)
   - Department (optional)
   - Position (optional)
   - Roles (required, minimal 1, bisa multiple)
   - Status (required, default: ACTIVE)
4. ADMIN isi form data
5. ADMIN klik "Save"
6. Sistem validasi input:
   - NIK belum terdaftar
   - Email belum terdaftar
   - Password = confirm password
   - Minimal 1 role dipilih
7. Jika valid:
   a. Sistem create employee
   b. Sistem assign role(s) ke user
   c. Sistem catat activity log: CREATE - employee
   d. Sistem tampilkan pesan sukses: "User berhasil dibuat"
   e. Sistem redirect ke list users

**Alternative Flows:**

*Alt Flow 1: NIK sudah terdaftar*
- Pada langkah 6: Sistem menemukan NIK sudah ada
- Sistem tampilkan error: "NIK sudah terdaftar. Gunakan NIK lain."
- Kembali ke langkah 4

*Alt Flow 2: Email sudah terdaftar*
- Pada langkah 6: Sistem menemukan email sudah ada
- Sistem tampilkan error: "Email sudah terdaftar. Gunakan email lain."
- Kembali ke langkah 4

*Alt Flow 3: Password tidak match*
- Pada langkah 6: Password ≠ confirm password
- Sistem tampilkan error: "Password dan Confirm Password tidak cocok."
- Kembali ke langkah 4

**Postconditions:**
- User baru berhasil dibuat
- Role(s) berhasil di-assign
- Employee data tersimpan di database
- Activity log tercatat

**Business Rules:**
- BR-USER-001: NIK harus unique
- BR-USER-002: Email harus unique
- BR-USER-003: Minimal 1 role harus dipilih
- BR-USER-004: Password minimal 8 karakter, mengandung huruf dan angka

**Acceptance Criteria:**
- [ ] Form create user tampilkan semua field required
- [ ] Validasi NIK unique berfungsi
- [ ] Validasi email unique berfungsi
- [ ] Validasi password match berfungsi
- [ ] Role selection mendukung multiple selection
- [ ] User berhasil dibuat jika semua valid
- [ ] Error message jelas untuk tiap validasi
- [ ] Activity log tercatat

---

### FR-USER-002: Assign Roles

**Description:** ADMIN dapat assign role ke user.

**Priority:** Mandatory

**User Story:**
```
AS AN ADMIN
I WANT TO assign one or more roles to a user
SO THAT the user can access appropriate features
```

**Preconditions:**
- User sudah ada
- ADMIN login dengan role ADMIN aktif

**Main Flow:**
1. ADMIN buka form edit user
2. Sistem tampilkan checkbox untuk role selection:
   - ☐ ADMIN
   - ☐ HR
   - ☐ EMPLOYEE
   - ☐ DOSEN
3. ADMIN pilih satu atau lebih role:
4. ADMIN klik "Save"
5. Sistem update role assignment:
   - Role yang dipilih = ditambah
   - Role yang tidak dipilih = dihapus
6. Sistem catat activity log: UPDATE - employee_roles
7. Sistem tampilkan pesan sukses

**Alternative Flows:**

*Alt Flow 1: Tidak ada role yang dipilih*
- Pada langkah 5: Sistem menemukan 0 role dipilih
- Sistem tampilkan error: "Minimal 1 role harus dipilih."
- Kembali ke langkah 2

*Alt Flow 2: Assign role DOSEN*
- Pada langkah 3: ADMIN pilih role DOSEN
- Setelah save, sistem tampilkan prompt: "User memiliki role DOSEN. Isi data dosen sekarang?"
- ADMIN klik "Yes"
- Sistem redirect ke form lecturer profile
- Flow lanjut ke FR-LEC-001

**Postconditions:**
- Role assignment berhasil di-update
- Activity log tercatat

**Business Rules:**
- BR-USER-005: Minimal 1 role per user
- BR-USER-006: User dengan role DOSEN harus memiliki lecturer profile

**Acceptance Criteria:**
- [ ] Checkbox role tampilkan 4 role
- [ ] Support multiple selection
- [ ] Role yang existing terselect saat edit
- [ ] Update role assignment berhasil
- [ ] Error jika 0 role dipilih
- [ ] Prompt isi data dosen jika role DOSEN dipilih
- [ ] Activity log tercatat

---

### FR-USER-003: Reset Password

**Description:** ADMIN dapat reset password user.

**Priority:** Mandatory

**User Story:**
```
AS AN ADMIN
I WANT TO reset a user's password
SO THAT the user can regain access if they forgot their password
```

**Preconditions:**
- User sudah ada
- ADMIN login dengan role ADMIN aktif

**Main Flow:**
1. ADMIN buka list users
2. ADMIN klik icon "Reset Password" pada user yang diinginkan
3. Sistem tampilkan modal: "Reset Password untuk [User Name]?"
4. ADMIN klik "Yes, Reset"
5. Sistem generate password baru (random)
6. Sistem update password user
7. Sistem catat activity log: UPDATE - employee (password reset)
8. Sistem tampilkan pesan sukses: "Password berhasil di-reset. Password baru: [NEW_PASSWORD]"

**Alternative Flows:**

*Alt Flow 1: ADMIN cancel reset*
- Pada langkah 3: ADMIN klik "Cancel"
- Modal tertutup
- Password tidak di-reset

**Postconditions:**
- Password user berhasil di-reset
- User dapat login dengan password baru
- Activity log tercatat

**Business Rules:**
- BR-USER-007: Password baru minimal 8 karakter
- BR-USER-008: Password baru harus mengandung huruf dan angka

**Acceptance Criteria:**
- [ ] Icon reset password tampil di list users
- [ ] Konfirmasi modal tampilkan
- [ ] Password baru di-generate otomatis
- [ ] Password baru ditampilkan ke ADMIN
- [ ] Password user di-update di database (hashed)
- [ ] Activity log tercatat

---

### FR-USER-004: Activate/Deactivate User

**Description:** ADMIN dapat mengaktifkan atau menonaktifkan user.

**Priority:** Mandatory

**User Story:**
```
AS AN ADMIN
I WANT TO activate or deactivate a user
SO THAT I can control who can access the system
```

**Preconditions:**
- User sudah ada
- ADMIN login dengan role ADMIN aktif

**Main Flow (Deactivate):**
1. ADMIN buka list users
2. ADMIN klik toggle status pada user
3. Sistem tampilkan konfirmasi: "Nonaktifkan user [User Name]?"
4. ADMIN klik "Yes"
5. Sistem update user status = INACTIVE
6. Sistem catat activity log: UPDATE - employee (deactivate)
7. Sistem tampilkan pesan sukses

**Main Flow (Activate):**
1. ADMIN buka list users
2. ADMIN klik toggle status pada user yang INACTIVE
3. Sistem tampilkan konfirmasi: "Aktifkan user [User Name]?"
4. ADMIN klik "Yes"
5. Sistem update user status = ACTIVE
6. Sistem catat activity log: UPDATE - employee (activate)
7. Sistem tampilkan pesan sukses

**Alternative Flows:**

*Alt Flow 1: User mencoba login saat INACTIVE*
- Pada FR-AUTH-001: Sistem menemukan status = INACTIVE
- Sistem tampilkan error: "Akun Anda tidak aktif. Hubungi HR/Admin."
- Login ditolak

**Postconditions:**
- User status berhasil diubah
- User INACTIVE tidak dapat login
- Activity log tercatat

**Business Rules:**
- BR-USER-009: User INACTIVE tidak dapat login
- BR-USER-010: Status tidak mempengaruhi data (soft delete)

**Acceptance Criteria:**
- [ ] Toggle status tersedia untuk setiap user
- [ ] Konfirmasi modal tampilkan
- [ ] Status berubah sesuai aksi
- [ ] User INACTIVE tidak dapat login
- [ ] Activity log tercatat

---

## 4. Employee Management

### FR-EMP-001: Create Employee

**Description:** HR dapat membuat data karyawan baru.

**Priority:** Mandatory

**User Story:**
```
AS AN HR staff
I WANT TO create a new employee record
SO THAT new employee data is captured in the system
```

**Preconditions:**
- HR login dengan role HR aktif
- Data tersedia untuk diinput

**Main Flow:**
1. HR klik menu "Employees"
2. HR klik tombol "Add Employee"
3. Sistem tampilkan form dengan tab:
   **Tab 1: Data Identitas**
   - NIK (required, unique)
   - Nama Lengkap (required)
   - Tempat Lahir (required)
   - Tanggal Lahir (required)
   - Gender (required: MALE/FEMALE)
   - Nama Ibu Kandung (required)
   - Alamat (required)
   - Telepon (required)
   - Email (required, unique)

   **Tab 2: Data Kepegawaian**
   - Status Kepegawaian (required: PERMANENT/CONTRACT/PROBATION/DAILY)
   - Tanggal Mulai Bekerja (required)
   - Lokasi Kerja (required)
   - Department (required)
   - Position (required)

   **Tab 3: Data BPJS & Pajak**
   - No. BPJS Ketenagakerjaan (unique)
   - No. BPJS Kesehatan (unique)
   - NPWP (unique)
   - Gaji Pokok (required)

   **Tab 4: Data Keluarga**
   - No. Kartu Keluarga
   - Status Pernikahan (SINGLE/MARRIED/DIVORCED/WIDOWED)
   - Nama Pasangan (jika married)
   - Jumlah Tanggungan (default: 0)

   **Tab 5: Role Assignment**
   - Roles (required, minimal 1: EMPLOYEE/DOSEN)
   - Jika DOSEN dipilih → tampilkan prompt isi data dosen

4. HR isi semua field required
5. HR klik "Save"
6. Sistem validasi semua input
7. Jika valid:
   a. Sistem create employee
   b. Sistem assign role(s)
   c. Jika role DOSEN → create lecturer profile
   d. Sistem catat activity log: CREATE - employee
   e. Sistem tampilkan pesan sukses
   f. Sistem redirect ke list employees

**Alternative Flows:**

*Alt Flow 1: NIK sudah ada*
- Validasi gagal: "NIK sudah terdaftar"
- Kembali ke form

*Alt Flow 2: Email sudah ada*
- Validasi gagal: "Email sudah terdaftar"
- Kembali ke form

*Alt Flow 3: Department belum ada*
- Sistem tampilkan link: "Buat Department baru"
- Buka modal create department
- Setelah department dibuat, field terupdate otomatis

**Postconditions:**
- Employee berhasil dibuat
- Role(s) ter-assign
- Lecturer profile dibuat jika role DOSEN
- Activity log tercatat

**Business Rules:**
- BR-EMP-001: Semua field "Data Identitas" wajib diisi (sesuai regulasi)
- BR-EMP-002: Nama Ibu Kandung wajib untuk BPJS
- BR-EMP-003: Gaji Pokok wajib untuk BPJS (maksimal Rp9.559.600 untuk iuran)
- BR-EMP-004: Minimal 1 role harus dipilih

**Acceptance Criteria:**
- [ ] Form employee tampilkan dengan tab
- [ ] Semua field required ditandai dengan *
- [ ] Validasi unique berfungsi (NIK, email, BPJS, NPWP)
- [ ] Department dropdown hanya menampilkan department aktif
- [ ] Position dropdown hanya menampilkan position aktif
- [ ] Role assignment support multiple selection
- [ ] Prompt isi data dosen jika role DOSEN dipilih
- [ ] Employee berhasil dibuat
- [ ] Activity log tercatat

---

### FR-EMP-002: Update Employee

**Description:** HR dapat mengupdate data karyawan.

**Priority:** Mandatory

**User Story:**
```
AS AN HR staff
I WANT TO update employee data
SO THAT employee information is always up-to-date
```

**Preconditions:**
- Employee sudah ada
- HR login dengan role HR aktif

**Main Flow:**
1. HR buka list employees
2. HR klik employee yang akan diupdate
3. Sistem tampilkan detail employee dengan tombol "Edit"
4. HR klik "Edit"
5. Sistem tampilkan form edit (sama seperti create)
6. Form terisi dengan data employee yang existing
7. HR update field yang diperlukan
8. HR klik "Save"
9. Sistem validasi input
10. Jika valid:
    a. Sistem update employee data
    b. Sistem catat activity log: UPDATE - employee
    c. Sistem tampilkan pesan sukses
    d. Sistem redirect ke detail employee

**Alternative Flows:**

*Alt Flow 1: Update NIK ke NIK yang sudah ada*
- Validasi gagal: "NIK sudah digunakan oleh employee lain"
- Kembali ke form

*Alt Flow 2: Update email ke email yang sudah ada*
- Validasi gagal: "Email sudah digunakan oleh employee lain"
- Kembali ke form

**Postconditions:**
- Employee data berhasil di-update
- Perubahan tercatat di updated_at dan updated_by
- Activity log tercatat

**Business Rules:**
- BR-EMP-005: NIK dan email tetap unique setelah update
- BR-EMP-006: Data kritis (NIK, email) perlu konfirmasi jika diubah

**Acceptance Criteria:**
- [ ] Form edit terisi dengan data existing
- [ ] Semua field dapat diupdate
- [ ] Validasi unique berfungsi
- [ ] Update berhasil
- [ ] Updated_by tercatat
- [ ] Activity log tercatat

---

### FR-EMP-003: View Employee List

**Description:** HR dapat melihat daftar semua karyawan.

**Priority:** Mandatory

**User Story:**
```
AS AN HR staff
I WANT TO view the list of all employees
SO THAT I can find and manage employee data easily
```

**Preconditions:**
- HR login dengan role HR aktif

**Main Flow:**
1. HR klik menu "Employees"
2. Sistem tampilkan halaman list employees dengan:
   - Search bar (cari berdasarkan nama/NIK/email)
   - Filter department
   - Filter position
   - Filter employment status
   - Pagination (20 data per halaman)
   - Tabel dengan kolom:
     * No
     * NIK
     * Nama Lengkap
     * Department
     * Position
     * Employment Status
     * Status
     * Actions (View, Edit, Delete)
3. HR dapat search, filter, dan pagination

**Alternative Flows:**

*Alt Flow 1: Search employee*
- HR ketik keyword di search bar
- Sistem filter employee yang mengandung keyword
- Tabel update hasil filter

*Alt Flow 2: Filter department*
- HR pilih department di dropdown filter
- Sistem filter employee berdasarkan department
- Tabel update hasil filter

**Postconditions:**
- List employee tampil
- Filter dan search berfungsi
- Pagination berfungsi

**Business Rules:**
- BR-EMP-007: Hanya tampilkan employee dengan deleted_at = NULL
- BR-EMP-008: Default sort berdasarkan created_at DESC

**Acceptance Criteria:**
- [ ] List employee tampilkan dengan kolom yang sesuai
- [ ] Search berfungsi (nama/NIK/email)
- [ ] Filter department berfungsi
- [ ] Filter position berfungsi
- [ ] Filter employment status berfungsi
- [ ] Pagination berfungsi
- [ ] Hanya employee aktif yang tampil
- [ ] Klik NIK/Nama untuk lihat detail
- [ ] Tombol View, Edit, Delete tersedia

---

### FR-EMP-004: Delete Employee (Soft Delete)

**Description:** HR dapat menghapus data karyawan (soft delete).

**Priority:** Mandatory

**User Story:**
```
AS AN HR staff
I WANT TO delete an employee record
SO THAT the employee is removed from the active list
```

**Preconditions:**
- Employee ada dan aktif
- HR login dengan role HR aktif

**Main Flow:**
1. HR buka list employees
2. HR klik tombol "Delete" pada employee
3. Sistem tampilkan modal konfirmasi:
   "Hapus data karyawan [Nama] - [NIK]?
    Data tidak akan dihapus permanen dan dapat dipulihkan kembali."
4. HR klik "Yes, Delete"
5. Sistem lakukan soft delete:
   a. Set deleted_at = NOW()
   b. Set deleted_by = current_user_id
   c. Employee tidak tampil di list aktif
6. Sistem catat activity log: DELETE - employee
7. Sistem tampilkan pesan sukses: "Employee berhasil dihapus"

**Alternative Flows:**

*Alt Flow 1: HR cancel delete*
- Pada langkah 3: HR klik "Cancel"
- Modal tertutup
- Employee tidak dihapus

**Postconditions:**
- Employee berhasil dihapus (soft delete)
- Employee tidak tampil di list aktif
- Data masih ada di database untuk audit
- Activity log tercatat

**Business Rules:**
- BR-EMP-009: Tidak ada hard delete untuk employee
- BR-EMP-010: Data yang dihapus masih dapat di-restore
- BR-EMP-011: Employee tidak dapat dihapus jika memiliki data terkait aktif (payroll bulan ini, dll)

**Acceptance Criteria:**
- [ ] Tombol delete tampil di list
- [ ] Konfirmasi modal tampilkan dengan warning
- [ ] Soft delete berfungsi (deleted_at terisi)
- [ ] Employee tidak tampil di list aktif setelah delete
- [ ] Activity log tercatat
- [ ] Data masih ada di database

---

### FR-EMP-005: Employee Self-Service (View Profile)

**Description:** Karyawan dapat melihat data diri sendiri.

**Priority:** Mandatory

**User Story:**
```
AS AN EMPLOYEE
I WANT TO view my own profile
SO THAT I can verify my data is correct
```

**Preconditions:**
- Employee login dengan role EMPLOYEE aktif

**Main Flow:**
1. Employee klik menu "My Profile"
2. Sistem tampilkan profile employee dengan:
   - Semua data identitas (read-only)
   - Data kepegawaian (read-only)
   - Data BPJS & Pajak (masked: ****-****-****-1234)
   - Data keluarga (read-only)
   - Roles yang dimiliki
   - Tombol "Edit" (terbatas)

**Alternative Flows:**

*N/A*

**Postconditions:**
- Profile ditampilkan
- Data sensitif di-mask untuk keamanan

**Business Rules:**
- BR-EMP-012: Employee hanya dapat view data diri sendiri
- BR-EMP-013: Data BPJS dan NPWP ditampilkan sebagian (masked)

**Acceptance Criteria:**
- [ ] Menu "My Profile" tampil untuk EMPLOYEE
- [ ] Semua data employee ditampilkan
- [ ] Data BPJS & NPWP di-mask
- [ ] Hanya data diri sendiri yang tampil
- [ ] Tombol edit tersedia (terbatas)

---

### FR-EMP-006: Employee Self-Service (Edit Profile)

**Description:** Karyawan dapat mengedit data diri sendiri (terbatas).

**Priority:** Mandatory

**User Story:**
```
AS AN EMPLOYEE
I WANT TO update my own profile
SO THAT my personal information is up-to-date
```

**Preconditions:**
- Employee login
- Employee hanya dapat edit data diri sendiri

**Main Flow:**
1. Employee buka "My Profile"
2. Employee klik "Edit"
3. Sistem tampilkan form dengan field yang bisa diedit:
   ✅ Editable:
   - Telepon
   - Alamat
   - Nama Pasangan
   - Jumlah Tanggungan
   - Foto Profil

   ❌ Read-only:
   - NIK
   - Nama Lengkap
   - Tempat/Tanggal Lahir
   - Gender
   - Nama Ibu Kandung
   - Email
   - Data Kepegawaian
   - Data BPJS & Pajak
   - Department
   - Position
4. Employee update field yang diizinkan
5. Employee klik "Save"
6. Sistem update employee data
7. Sistem catat activity log: UPDATE - employee (self-service)
8. Sistem tampilkan pesan sukses

**Alternative Flows:**

*N/A*

**Postconditions:**
- Employee dapat mengupdate data tertentu
- Data penting tetap terlindungi (read-only)
- Activity log tercatat

**Business Rules:**
- BR-EMP-014: Employee hanya dapat edit field tertentu
- BR-EMP-015: Perubahan data kritis (NIK, email, BPJS, dll) harus via HR

**Acceptance Criteria:**
- [ ] Form edit tampilkan untuk employee
- [ ] Hanya field tertentu yang editable
- [ ] Field kunci (NIK, nama, email) read-only
- [ ] Data kepegawaian read-only
- [ ] Data BPJS & pajak read-only
- [ ] Update berhasil
- [ ] Activity log tercatat dengan flag self-service

---

## 5. Department & Position Management

### FR-DEPT-001: Create Department

**Description:** ADMIN/HR dapat membuat department baru.

**Priority:** Mandatory

**User Story:**
```
AS AN HR/ADMIN
I WANT TO create a new department
SO THAT employees can be assigned to departments
```

**Preconditions:**
- ADMIN/HR login dengan role aktif

**Main Flow:**
1. ADMIN/HR klik menu "Departments"
2. ADMIN/HR klik "Add Department"
3. Sistem tampilkan form:
   - Nama Department (required)
   - Deskripsi (optional)
   - Apakah ini Prodi? (checkbox: is_prodi)
   - Kode Prodi (required jika is_prodi = true)
4. ADMIN/HR isi form
5. ADMIN/HR klik "Save"
6. Sistem validasi:
   - Nama department harus unique
   - Kode prodi wajib jika is_prodi = true
   - Kode prodi harus unique jika is_prodi = true
7. Jika valid:
   a. Sistem create department
   b. Sistem catat activity log: CREATE - department
   c. Sistem tampilkan pesan sukses

**Alternative Flows:**

*Alt Flow 1: Nama department sudah ada*
- Validasi gagal
- Error: "Nama department sudah ada"
- Kembali ke form

**Postconditions:**
- Department berhasil dibuat
- Activity log tercatat

**Business Rules:**
- BR-DEPT-001: Nama department harus unique
- BR-DEPT-002: Kode prodi wajib jika is_prodi = true
- BR-DEPT-003: Kode prodi unique untuk department dengan is_prodi = true

**Acceptance Criteria:**
- [ ] Form department tampilkan
- [ ] Checkbox is_prodi berfungsi
- [ ] Field kode_prodi muncul jika is_prodi dicentang
- [ ] Validasi nama unique berfungsi
- [ ] Validasi kode_prodi berfungsi
- [ ] Department berhasil dibuat
- [ ] Activity log tercatat

---

### FR-DEPT-002: Create Position

**Description:** ADMIN/HR dapat membuat position/jabatan baru.

**Priority:** Mandatory

**User Story:**
```
AS AN HR/ADMIN
I WANT TO create a new position
SO THAT employees can be assigned to positions
```

**Preconditions:**
- ADMIN/HR login dengan role aktif

**Main Flow:**
1. ADMIN/HR klik menu "Positions"
2. ADMIN/HR klik "Add Position"
3. Sistem tampilkan form:
   - Nama Position (required)
   - Deskripsi (optional)
   - Base Salary (required, minimal UMR)
4. ADMIN/HR isi form
5. ADMIN/HR klik "Save"
6. Sistem validasi:
   - Nama position harus unique
   - Base salary harus >= UMR
7. Jika valid:
   a. Sistem create position
   b. Sistem catat activity log: CREATE - position
   c. Sistem tampilkan pesan sukses

**Postconditions:**
- Position berhasil dibuat
- Activity log tercatat

**Business Rules:**
- BR-POS-001: Nama position harus unique
- BR-POS-002: Base salary minimal sesuai UMR daerah

**Acceptance Criteria:**
- [ ] Form position tampilkan
- [ ] Validasi nama unique berfungsi
- [ ] Validasi base salary >= UMR
- [ ] Position berhasil dibuat
- [ ] Activity log tercatat

---

## 6. Lecturer Management

### FR-LEC-001: Create Lecturer Profile

**Description:** ADMIN/HR dapat membuat profile dosen baru.

**Priority:** Mandatory

**User Story:**
```
AS AN HR/ADMIN
I WANT TO create a lecturer profile
SO THAT lecturer data is captured according to government regulations
```

**Preconditions:**
- Employee sudah ada
- Employee memiliki role DOSEN
- ADMIN/HR login dengan role aktif

**Main Flow:**
1. ADMIN/HR buka form create/update employee
2. ADMIN/HR assign role DOSEN ke employee
3. Setelah save, sistem prompt: "Isi data dosen sekarang?"
4. ADMIN/HR klik "Yes"
5. Sistem tampilkan form lecturer profile:
   - NIDN (required, unique)
   - Pendidikan Terakhir (required: S2/S3)
   - Bidang Keahlian (required)
   - Jenjang Dosen (required: ASISTEN_AHLI/LEKTOR/LEKTOR_KEPALA/PROFESOR)
   - Status Kepegawaian (required: DOSEN_TETAP/DOSEN_TIDAK_TETAP)
   - Status Kerja (required: ACTIVE/LEAVE/RETIRED)
   - Homebase Prodi (required, dropdown department dengan is_prodi=true)
6. ADMIN/HR isi form
7. ADMIN/HR klik "Save"
8. Sistem validasi:
   - NIDN unique
   - Homebase prodi harus department dengan is_prodi = true
9. Jika valid:
   a. Sistem create lecturer profile
   b. Sistem catat activity log: CREATE - lecturer_profile
   c. Sistem tampilkan pesan sukses

**Alternative Flows:**

*Alt Flow 1: NIDN sudah ada*
- Validasi gagal
- Error: "NIDN sudah digunakan"
- Kembali ke form

*Alt Flow 2: Pilih homebase prodi bukan prodi*
- Validasi gagal
- Error: "Homebase prodi harus prodi (department dengan is_prodi=true)"
- Kembali ke form

**Postconditions:**
- Lecturer profile berhasil dibuat
- Dosen terhubung dengan employee
- Activity log tercatat

**Business Rules:**
- BR-LEC-001: NIDN harus unique
- BR-LEC-002: Homebase prodi harus department dengan is_prodi = true
- BR-LEC-003: Jenjang dosen sesuai Permendiktisaintek

**Acceptance Criteria:**
- [ ] Form lecturer profile tampilkan
- [ ] NIDN wajib dan unique
- [ ] Dropdown jenjang dosen sesuai regulasi
- [ ] Dropdown homebase prodi hanya department is_prodi=true
- [ ] Validasi NIDN berfungsi
- [ ] Validasi homebase prodi berfungsi
- [ ] Lecturer profile berhasil dibuat
- [ ] Activity log tercatat

---

### FR-LEC-002: View Lecturer List

**Description:** ADMIN/HR dapat melihat daftar semua dosen.

**Priority:** Mandatory

**User Story:**
```
AS AN HR/ADMIN
I WANT TO view the list of all lecturers
SO THAT I can manage lecturer data easily
```

**Preconditions:**
- ADMIN/HR login dengan role aktif

**Main Flow:**
1. ADMIN/HR klik menu "Lecturers"
2. Sistem tampilkan halaman list dosen dengan:
   - Search bar (nama/NIK/NIDN)
   - Filter homebase prodi
   - Filter jenjang
   - Filter status kepegawaian
   - Filter status kerja
   - Tabel dengan kolom:
     * No
     * NIK
     * Nama Lengkap
     * NIDN
     * Jenjang
     * Status Kepegawaian
     * Status Kerja
     * Homebase Prodi
     * Actions (View, Edit, Delete)

**Postconditions:**
- List dosen tampil
- Filter berfungsi

**Business Rules:**
- BR-LEC-004: Hanya employee dengan role DOSEN yang tampil

**Acceptance Criteria:**
- [ ] List dosen tampil dengan kolom sesuai
- [ ] Search berfungsi (nama/NIK/NIDN)
- [ ] Filter homebase prodi berfungsi
- [ ] Filter jenjang berfungsi
- [ ] Filter status kepegawaian berfungsi
- [ ] Filter status kerja berfungsi
- [ ] Hanya employee role DOSEN yang tampil

---

## 7. Attendance Management

### FR-ATT-001: Clock In

**Description:** Karyawan/dosen dapat melakukan Clock In.

**Priority:** Mandatory

**User Story:**
```
AS AN EMPLOYEE/DOSEN
I WANT TO clock in when I arrive at work
SO THAT my attendance is recorded
```

**Preconditions:**
- Employee/dosen login
- Employee/dosen belum clock in hari ini

**Main Flow:**
1. Employee/dosen buka dashboard
2. Sistem tampilkan tombol "Clock In" (jika belum clock in)
3. Employee/dosen klik "Clock In"
4. Sistem catat attendance:
   - employee_id = current user
   - date = hari ini
   - clock_in = waktu sekarang
   - status = PRESENT
5. Sistem catat activity log: CLOCK_IN
6. Sistem tampilkan pesan sukses: "Berhasil Clock In pada [HH:MM]"
7. Tombol berubah menjadi "Clock Out"

**Alternative Flows:**

*Alt Flow 1: Sudah clock in hari ini*
- Pada langkah 2: Sistem menemukan attendance hari ini sudah ada
- Tombol "Clock In" tidak tampil
- Tombol "Clock Out" tampil (jika belum clock out)

*Alt Flow 2: Clock in lewat jam 08:00*
- Pada langkah 4: Sistem cek waktu > 08:00
- Status = LATE (bukan PRESENT)
- Catat notes: "Telat [X] menit"

**Postconditions:**
- Attendance clock in tercatat
- Activity log tercatat
- User tidak dapat clock in lagi hari ini

**Business Rules:**
- BR-ATT-001: Satu employee hanya bisa 1x clock in per hari
- BR-ATT-002: Clock in lewat 08:00 dianggap LATE
- BR-ATT-003: Clock in hanya bisa hari kerja (Senin-Jumat, kecuali hari libur)

**Acceptance Criteria:**
- [ ] Tombol Clock In tampil di dashboard
- [ ] Klik Clock In mencatat waktu
- [ ] Status PRESENT jika <= 08:00
- [ ] Status LATE jika > 08:00
- [ ] Pesan sukses tampilkan
- [ ] Tombol berubah jadi Clock Out
- [ ] Clock in hanya 1x per hari
- [ ] Activity log tercatat

---

### FR-ATT-002: Clock Out

**Description:** Karyawan/dosen dapat melakukan Clock Out.

**Priority:** Mandatory

**User Story:**
```
AS AN EMPLOYEE/DOSEN
I WANT TO clock out when I leave work
SO THAT my working hours are recorded
```

**Preconditions:**
- Employee/dosen login
- Employee/dosen sudah clock in hari ini
- Belum clock out hari ini

**Main Flow:**
1. Employee/dosen buka dashboard
2. Sistem tampilkan tombol "Clock Out" (jika sudah clock in)
3. Employee/dosen klik "Clock Out"
4. Sistem update attendance:
   - clock_out = waktu sekarang
5. Sistem catat activity log: CLOCK_OUT
6. Sistem tampilkan pesan sukses: "Berhasil Clock Out pada [HH:MM]"
7. Tombol Clock Out tidak tampil lagi

**Alternative Flows:**

*Alt Flow 1: Belum clock in*
- Tombol Clock Out tidak tampil
- Pesan: "Silakan Clock In terlebih dahulu"

*Alt Flow 2: Clock out sebelum jam kerja selesai*
- Sistem tetap mengizinkan
- Catat notes jika perlu

**Postconditions:**
- Attendance clock out tercatat
- Activity log tercatat

**Business Rules:**
- BR-ATT-004: Clock out hanya setelah clock in
- BR-ATT-005: Clock out bisa dilakukan kapan saja

**Acceptance Criteria:**
- [ ] Tombol Clock Out tampil setelah Clock In
- [ ] Klik Clock Out mencatat waktu
- [ ] Pesan sukses tampilkan
- [ ] Tombol Clock Out hilang setelah clock out
- [ ] Tidak bisa clock out 2x
- [ ] Activity log tercatat

---

### FR-ATT-003: View Attendance History

**Description:** Karyawan/dosen dapat melihat riwayat absensi sendiri.

**Priority:** Mandatory

**User Story:**
```
AS AN EMPLOYEE/DOSEN
I WANT TO view my attendance history
SO THAT I can track my attendance record
```

**Preconditions:**
- Employee/dosen login

**Main Flow:**
1. Employee/dosen klik menu "My Attendance"
2. Sistem tampilkan halaman attendance history dengan:
   - Filter bulan/tahun
   - Kalender dengan indicator:
     * Hijau = Hadir
     * Kuning = Telat
     * Merah = Alpha/Sakit/Izin
   - Tabel riwayat absensi:
     * Tanggal
     * Clock In
     * Clock Out
     * Status
     * Working Hours

**Postconditions:**
- Attendance history ditampilkan
- Filter berfungsi

**Business Rules:**
- BR-ATT-006: Employee hanya bisa view riwayat sendiri
- BR-ATT-007: Data absensi tidak bisa di-edit oleh employee

**Acceptance Criteria:**
- [ ] Menu "My Attendance" tampil
- [ ] Kalender absensi tampilkan dengan indicator warna
- [ ] Tabel riwayat absensi tampilkan
- [ ] Filter bulan/tahun berfungsi
- [ ] Hanya data sendiri yang tampil
- [ ] Data read-only

---

## 8. Leave Management

### FR-LEAVE-001: Submit Leave Request

**Description:** Karyawan/dosen dapat mengajukan cuti.

**Priority:** Mandatory

**User Story:**
```
AS AN EMPLOYEE/DOSEN
I WANT TO submit a leave request
SO THAT I can take time off from work
```

**Preconditions:**
- Karyawan/dosen login
- Saldo cuti tersedia

**Main Flow:**
1. Karyawan/dosen klik menu "My Leave" → "Request Leave"
2. Sistem tampilkan form:
   - Tipe Cuti (required: ANNUAL/SICK/MATERNITY/MARRIAGE/SPECIAL/UNPAID)
   - Tanggal Mulai (required)
   - Tanggal Selesai (required)
   - Alasan (required)
3. Karyawan/dosen isi form
4. Karyawan/dosen klik "Submit"
5. Sistem validasi:
   - Tanggal selesai >= tanggal mulai
   - Durasi cuti <= saldo cuti tersedia
6. Jika valid:
   a. Sistem create leave request dengan status PENDING
   b. Sistem catat activity log: SUBMIT_LEAVE
   c. Sistem tampilkan pesan sukses: "Pengajuan cuti berhasil. Menunggu approval."

**Alternative Flows:**

*Alt Flow 1: Saldo cuti tidak cukup*
- Validasi gagal: "Saldo cuti tidak mencukupi. Sisa: [X] hari"
- Kembali ke form

*Alt Flow 2: Tanggal cuti overlap dengan cuti lain*
- Validasi gagal: "Anda memiliki pengajuan cuti pada tanggal tersebut"
- Kembali ke form

**Postconditions:**
- Leave request berhasil dibuat
- Status = PENDING
- Menunggu approval HR/ADMIN
- Activity log tercatat

**Business Rules:**
- BR-LEAVE-001: Saldo cuti tahunan: 12 hari
- BR-LEAVE-002: Saldo cuti sakit: 14 hari
- BR-LEAVE-003: Cuti tidak boleh overlap dengan pengajuan lain
- BR-LEAVE-004: Cuti melahirkan: 3 bulan (sesuai regulasi)

**Acceptance Criteria:**
- [ ] Form cuti tampilkan
- [ ] Tipe cuti sesuai regulasi
- [ ] Validasi saldo cuti berfungsi
- [ ] Validasi tanggal overlap berfungsi
- [ ] Leave request berhasil dibuat
- [ ] Status PENDING
- [ ] Activity log tercatat

---

### FR-LEAVE-002: Approve Leave Request

**Description:** HR/ADMIN dapat menyetujui pengajuan cuti.

**Priority:** Mandatory

**User Story:**
```
AS AN HR/ADMIN
I WANT TO approve a leave request
SO THAT the employee can take their time off
```

**Preconditions:**
- Leave request ada dengan status PENDING
- HR/ADMIN login dengan role aktif

**Main Flow:**
1. HR/ADMIN klik menu "Leave Requests"
2. Sistem tampilkan list leave request dengan filter status (default: PENDING)
3. HR/ADMIN klik salah satu request
4. Sistem tampilkan detail request
5. HR/ADMIN klik "Approve"
6. Sistem tampilkan modal konfirmasi:
   "Setujui pengajuan cuti [Employee] - [Tipe] ([Tanggal] - [Durasi] hari)?"
7. HR/ADMIN klik "Yes, Approve"
8. Sistem update leave request:
   - status = APPROVED
   - approved_by = current user
   - approved_at = NOW()
9. Sistem kurangi saldo cuti employee
10. Sistem catat activity log: APPROVE_LEAVE
11. Sistem tampilkan pesan sukses

**Alternative Flows:**

*Alt Flow 1: Reject leave request*
- Pada langkah 5: HR/ADMIN klik "Reject"
- Sistem tampilkan modal: "Alasan reject:"
- HR/ADMIN isi alasan
- Sistem update status = REJECTED
- Sistem TIDAK mengurangi saldo cuti
- Catat rejection reason

**Postconditions:**
- Leave request di-approve
- Saldo cuti berkurang
- Employee menerima notifikasi
- Activity log tercatat

**Business Rules:**
- BR-LEAVE-005: Hanya HR/ADMIN yang dapat approve
- BR-LEAVE-006: Approve mengurangi saldo cuti
- BR-LEAVE-007: Reject TIDAK mengurangi saldo cuti

**Acceptance Criteria:**
- [ ] List leave request tampil untuk HR/ADMIN
- [ ] Filter status berfungsi
- [ ] Detail request tampilkan
- [ ] Tombol Approve dan Reject tersedia
- [ ] Approve mengubah status = APPROVED
- [ ] Approve mengurangi saldo cuti
- [ ] Reject mengubah status = REJECTED
- [ ] Reject memerlukan alasan
- [ ] Reject TIDAK mengurangi saldo cuti
- [ ] Activity log tercatat

---

## 9. Payroll Management

### FR-PAY-001: Generate Payroll

**Description:** HR/ADMIN dapat generate payroll karyawan per periode.

**Priority:** Mandatory

**User Story:**
```
AS AN HR/ADMIN
I WANT TO generate payroll for a specific period
SO THAT employees can be paid correctly
```

**Preconditions:**
- HR/ADMIN login dengan role aktif
- Data attendance dan cuti periode tersebut complete

**Main Flow:**
1. HR/ADMIN klik menu "Payroll"
2. HR/ADMIN klik "Generate Payroll"
3. Sistem tampilkan form:
   - Periode (YYYY-MM) (required)
   - Tipe Payroll: Karyawan (required)
4. HR/ADMIN pilih periode
5. HR/ADMIN klik "Generate"
6. Sistem proses generate payroll untuk semua employee aktif:
   a. Ambil basic_salary dari position
   b. Hitung allowances berdasarkan data employee
   c. Hitung overtime berdasarkan attendance overtime
   d. Hitung deductions (BPJS, PPh 21, dll)
   e. Total salary = basic + allowances + overtime - deductions
7. Sistem create payroll record untuk setiap employee
8. Sistem catat activity log: GENERATE_PAYROLL
9. Sistem tampilkan summary:
   - Total employee: X
   - Total gaji: RpX
   - Status: Draft (belum paid)
10. HR/ADMIN klik "Save" untuk menyimpan sebagai draft

**Alternative Flows:**

*Alt Flow 1: Payroll periode tersebut sudah ada*
- Validasi gagal
- Error: "Payroll periode [Bulan-Tahun] sudah ada. Gunakan 'Edit' untuk mengubah."
- Kembali ke langkah 4

**Postconditions:**
- Payroll berhasil digenerate
- Status = DRAFT
- Payroll dapat diedit sebelum di-set PAID
- Activity log tercatat

**Business Rules:**
- BR-PAY-001: Basic salary diambil dari position.base_salary
- BR-PAY-002: Overtime dihitung berdasarkan jam lembur
- BR-PAY-003: PPh 21 dihitung berdasarkan UU Pajak

**Acceptance Criteria:**
- [ ] Form generate payroll tampil
- [ ] Periode bisa dipilih
- [ ] Generate semua employee aktif
- [ ] Perhitungan otomatis (basic + allowances + overtime - deductions)
- [ ] Payroll tersimpan dengan status DRAFT
- [ ] Validasi periode sudah ada berfungsi
- [ ] Summary generate tampilkan
- [ ] Activity log tercatat

---

### FR-PAY-002: Generate Lecturer Payroll

**Description:** HR/ADMIN dapat generate payroll dosen per periode.

**Priority:** Mandatory

**User Story:**
```
AS AN HR/ADMIN
I WANT TO generate lecturer payroll for a specific period
SO THAT lecturers are paid correctly
```

**Preconditions:**
- HR/ADMIN login dengan role aktif
- Data lecturer complete

**Main Flow:**
1. HR/ADMIN klik menu "Lecturer Payroll"
2. HR/ADMIN klik "Generate Payroll"
3. Sistem tampilkan form:
   - Periode (YYYY-MM) (required)
4. HR/ADMIN pilih periode
5. HR/ADMIN klik "Generate"
6. Sistem proses generate payroll untuk semua dosen aktif:
   a. Ambil basic_salary
   b. Hitung certification_allowance (tunjangan sertifikasi)
   c. Hitung functional_allowance (tunjangan fungsional berdasarkan jenjang)
   d. Hitung teaching_honor (honor mengajar)
   e. Hitung other_allowances
   f. Total salary = semua komponen
7. Sistem create lecturer payroll record
8. Sistem catat activity log: GENERATE_LECTURER_PAYROLL
9. Sistem tampilkan summary

**Postconditions:**
- Lecturer payroll berhasil digenerate
- Status = DRAFT
- Activity log tercatat

**Business Rules:**
- BR-PAY-004: Tunjangan fungsional berdasarkan jenjang dosen
- BR-PAY-005: Honor mengajar dihitung per kredit/sks

**Acceptance Criteria:**
- [ ] Form generate lecturer payroll tampil
- [ ] Komponen gaji dosen sesuai (basic, certification, functional, teaching)
- [ ] Payroll dosen tersimpan dengan status DRAFT
- [ ] Activity log tercatat

---

### FR-PAY-003: View Payslip

**Description:** Karyawan/dosen dapat melihat slip gaji sendiri.

**Priority:** Mandatory

**User Story:**
```
AS AN EMPLOYEE/DOSEN
I WANT TO view my payslip
SO THAT I can see my salary details
```

**Preconditions:**
- Karyawan/dosen login
- Payroll untuk periode tersebut sudah ada dengan status PAID

**Main Flow:**
1. Karyawan/dosen klik menu "My Payslip"
2. Sistem tampilkan daftar periode payslip
3. Karyawan/dosen klik salah satu periode
4. Sistem tampilkan payslip dengan detail:
   - Periode
   - Basic Salary
   - Allowances (detail)
   - Overtime (detail)
   - Deductions (detail: BPJS, PPh 21, dll)
   - Total Salary (Net)
   - Take Home Pay
5. Karyawan/dosen dapat download/print PDF

**Alternative Flows:**

*Alt Flow 1: Payroll belum PAID*
- Sistem tampilkan pesan: "Payslip periode [Bulan-Tahun] belum tersedia."
- Daftar periode hanya menunjukkan periode yang sudah PAID

**Postconditions:**
- Payslip ditampilkan
- Download/print tersedia

**Business Rules:**
- BR-PAY-006: Payslip hanya tersedia jika status = PAID
- BR-PAY-007: Employee hanya bisa melihat payslip sendiri

**Acceptance Criteria:**
- [ ] Menu "My Payslip" tampil
- [ ] Daftar periode payslip tampilkan
- [ ] Hanya periode PAID yang tampil
- [ ] Detail payslip lengkap
- [ ] Download PDF tersedia
- [ ] Print PDF tersedia
- [ ] Hanya payslip sendiri yang bisa dilihat

---

## 10. Activity Logging

### FR-LOG-001: Automatic Activity Logging

**Description:** Sistem otomatis mencatat semua aktivitas user.

**Priority:** Mandatory

**User Story:**
```
AS AN ADMIN
I WANT TO have automatic activity logging
SO THAT I can track all user activities for audit purposes
```

**Preconditions:**
- Sistem berjalan
- User melakukan aktivitas

**Main Flow:**
1. User melakukan aktivitas yang memiliki annotation @LogActivity
2. Aspect intercept eksekusi method
3. Sistem extract informasi:
   - User (dari SecurityContext)
   - Activity type (dari annotation)
   - Module name (dari class/controller)
   - Entity info (dari parameter)
   - IP address (dari request)
   - User agent (dari request)
   - Status (SUCCESS/FAILED)
4. Sistem simpan activity log ke database
5. Activity log tersimpan untuk reporting

**Activities yang dicatat:**
- LOGIN, LOGOUT, ROLE_SELECTION
- CREATE, READ, UPDATE, DELETE, RESTORE
- CLOCK_IN, CLOCK_OUT
- SUBMIT_LEAVE, APPROVE_LEAVE, REJECT_LEAVE
- GENERATE_PAYROLL
- VIEW_SENSITIVE_DATA

**Alternative Flows:**

*N/A*

**Postconditions:**
- Semua aktivitas tercatat
- Activity log dapat di-view di dashboard

**Business Rules:**
- BR-LOG-001: Semua aktivitas wajib di-log
- BR-LOG-002: Activity log tidak bisa dihapus (permanent)
- BR-LOG-003: Activity log retensi 1 tahun

**Acceptance Criteria:**
- [ ] Semua aktivitas di atas tercatat
- [ ] Login success dan failed tercatat
- [ ] CRUD operations tercatat
- [ ] Clock in/out tercatat
- [ ] Leave request/approval tercatat
- [ ] Payroll generate tercatat
- [ ] View sensitive data tercatat
- [ ] IP address dan user agent tercatat
- [ ] Status (SUCCESS/FAILED) tercatat
- [ ] Error message tercatat jika FAILED

---

### FR-LOG-002: View Activity Log Dashboard

**Description:** ADMIN dapat melihat dashboard activity log.

**Priority:** Mandatory

**User Story:**
```
AS AN ADMIN
I WANT TO view activity log dashboard
SO THAT I can monitor all user activities
```

**Preconditions:**
- ADMIN login dengan role ADMIN aktif

**Main Flow:**
1. ADMIN klik menu "Activity Logs"
2. Sistem tampilkan halaman activity log dengan:
   - Filter:
     * Employee (dropdown)
     * Modul (dropdown)
     * Activity Type (dropdown)
     * Status (dropdown)
     * Date Range (from - to)
   - Tabel activity log:
     * Waktu
     * User
     * Aktivitas
     * Modul
     * Deskripsi
     * Status
     * IP Address
3. ADMIN dapat filter berdasarkan kriteria
4. ADMIN dapat export ke Excel/PDF

**Alternative Flows:**

*Alt Flow 1: Filter by employee*
- ADMIN pilih employee di dropdown
- Sistem filter activity log untuk employee tersebut

*Alt Flow 2: Filter by date range*
- ADMIN pilih tanggal dari - sampai
- Sistem filter activity log range tanggal tersebut

**Postconditions:**
- Activity log dashboard tampil
- Filter berfungsi
- Export tersedia

**Business Rules:**
- BR-LOG-004: Hanya ADMIN yang dapat view semua activity log
- BR-LOG-005: Export activity log untuk audit

**Acceptance Criteria:**
- [ ] Menu Activity Log hanya tampil untuk ADMIN
- [ ] Tabel activity log tampilkan semua kolom
- [ ] Filter employee berfungsi
- [ ] Filter module berfungsi
- [ ] Filter activity type berfungsi
- [ ] Filter status berfungsi
- [ ] Filter date range berfungsi
- [ ] Export Excel tersedia
- [ ] Export PDF tersedia
- [ ] Pagination berfungsi

---

## 11. Dashboard

### FR-DASH-001: Admin Dashboard

**Description:** ADMIN dapat melihat dashboard overview.

**Priority:** Mandatory

**User Story:**
```
AS AN ADMIN
I WANT TO view a comprehensive dashboard
SO THAT I can monitor the entire system at a glance
```

**Preconditions:**
- ADMIN login dengan role ADMIN aktif

**Main Flow:**
1. ADMIN login
2. Sistem tampilkan dashboard ADMIN dengan:
   **Stat Cards:**
   - Total Employee: [X]
   - Total Dosen: [Y]
   - Total Departments: [Z]
   - Total Prodi: [W]
   - Absensi Hari Ini: [Hadir]/[Total]
   - Pending Leave: [N]

   **Quick Actions:**
   - Add Employee
   - Add Dosen
   - Generate Payroll
   - View Activity Logs

   **Charts:**
   - Employee per Department (pie chart)
   - Attendance Overview (bar chart)
   - Leave Trend (line chart)

   **Recent Activities:**
   - 5 activity terakhir

**Postconditions:**
- Dashboard ADMIN tampil dengan informasi lengkap

**Business Rules:**
- BR-DASH-001: Data dashboard real-time

**Acceptance Criteria:**
- [ ] Stat cards tampilkan dengan data actual
- [ ] Quick actions berfungsi
- [ ] Charts visualisasi data
- [ ] Recent activities tampilkan 5 terakhir
- [ ] Dashboard refresh otomatis (setiap 30 detik)

---

### FR-DASH-002: HR Dashboard

**Description:** HR dapat melihat dashboard overview.

**Priority:** Mandatory

**User Story:**
```
AS AN HR
I WANT TO view a dashboard relevant to HR functions
SO THAT I can quickly access HR features and see important metrics
```

**Preconditions:**
- HR login dengan role HR aktif

**Main Flow:**
1. HR login
2. Sistem tampilkan dashboard HR dengan:
   **Stat Cards:**
   - Total Employee: [X]
   - Total Dosen: [Y]
   - Absensi Hari Ini: [Hadir]/[Total]
   - Pending Leave: [N]
   - Payroll Bulan Ini: [Status]

   **Quick Actions:**
   - Add Employee
   - Add Dosen
   - Process Leave
   - Generate Payroll

   **Charts:**
   - Attendance This Month
   - Leave Requests

   **Recent Activities:**
   - 5 activity terakhir

**Postconditions:**
- Dashboard HR tampil

**Acceptance Criteria:**
- [ ] Stat cards relevan untuk HR
- [ ] Quick actions berfungsi
- [ ] Charts tampilkan
- [ ] Recent activities tampilkan

---

### FR-DASH-003: Employee/DOSEN Dashboard

**Description:** Karyawan/dosen dapat melihat dashboard personal.

**Priority:** Mandatory

**User Story:**
```
AS AN EMPLOYEE/DOSEN
I WANT TO view my personal dashboard
SO THAT I can quickly access my features and see my information
```

**Preconditions:**
- Employee/DOSEN login dengan role aktif

**Main Flow:**
1. Employee/DOSEN login
2. Sistem tampilkan dashboard dengan:
   **Profile Summary:**
   - Nama, Photo, Position, Department
   - Untuk DOSEN: Jenjang, Homebase Prodi

   **Today's Attendance:**
   - Clock In button (jika belum clock in)
   - Clock Out button (jika sudah clock in)
   - Status: Belum absen / Sudah clock in / Sudah clock out

   **Leave Balance:**
   - Annual: [Used]/[Total] hari
   - Sick: [Used]/[Total] hari

   **Quick Actions:**
   - Request Leave
   - View Attendance History
   - View Payslip

   **Recent:**
   - Recent leave requests
   - Recent payslip

**Postconditions:**
- Dashboard personal tampil

**Acceptance Criteria:**
- [ ] Profile summary tampilkan
- [ ] Clock In/Out button berfungsi
- [ ] Leave balance tampilkan
- [ ] Quick actions berfungsi
- [ ] Recent leave tampilkan
- [ ] Recent payslip tampilkan

---

## Appendix A: Requirement Priority Definition

| Priority | Description |
|----------|-------------|
| **Mandatory** | Wajib ada untuk MVP |
| **High** | Sangat penting, prioritaskan setelah Mandatory |
| **Medium** | Penting, dapat ditambahkan di phase selanjutnya |
| **Low** | Nice to have, dapat ditambahkan jika waktu memungkinkan |

---

## Appendix B: Functional Requirement Matrix

| Module | Total Requirements | Mandatory | High | Medium | Low |
|--------|-------------------|-----------|------|--------|-----|
| Authentication & Authorization | 7 | 7 | 0 | 0 | 0 |
| User Management | 4 | 4 | 0 | 0 | 0 |
| Employee Management | 6 | 6 | 0 | 0 | 0 |
| Department & Position | 2 | 2 | 0 | 0 | 0 |
| Lecturer Management | 2 | 2 | 0 | 0 | 0 |
| Attendance Management | 3 | 3 | 0 | 0 | 0 |
| Leave Management | 2 | 2 | 0 | 0 | 0 |
| Payroll Management | 3 | 3 | 0 | 0 | 0 |
| Activity Logging | 2 | 2 | 0 | 0 | 0 |
| Dashboard | 3 | 3 | 0 | 0 | 0 |
| **TOTAL** | **34** | **34** | **0** | **0** | **0** |

---

## Appendix C: Change History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 16/01/2026 | HRIS Team | Initial FRS |

---

## Appendix D: Approval

| Role | Name | Signature | Date |
|------|------|-----------|-------|
| **Product Owner** | | | |
| **Tech Lead** | | | |
| **Business Analyst** | | | |
| **QA Lead** | | | |
