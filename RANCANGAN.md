# Rancangan Aplikasi HRIS Sederhana

## Tech Stack
- **Backend**: SpringBoot 3.x
- **Build Tool**: Maven
- **Frontend**: HTMX + Alpine.js + Tailwind CSS
- **Database**: MySQL 8.x
- **ORM**: Spring Data JPA (Hibernate)

---

## 1. Arsitektur Aplikasi (Monolitik)

### 1.1 Arsitektur Monolitik

Aplikasi HRIS ini menggunakan **Arsitektur Monolitik** - satu aplikasi tunggal yang menghandle semua fungsionalitas.

```
┌─────────────────────────────────────────────────────────────┐
│                    Browser (Client)                         │
│  HTMX + Alpine.js + Tailwind CSS (Server-Side Rendering)   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              Single SpringBoot Application                  │
│                    (Monolithic JAR)                         │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Controller Layer                        │   │
│  │  AuthController │ EmployeeController │ Lecturer...  │   │
│  └─────────────────────────────────────────────────────┘   │
│                          │                                  │
│                          ▼                                  │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Service Layer                           │   │
│  │  AuthService │ EmployeeService │ LecturerService... │   │
│  └─────────────────────────────────────────────────────┘   │
│                          │                                  │
│                          ▼                                  │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Repository Layer                        │   │
│  │  EmployeeRepository │ LecturerRepository...         │   │
│  └─────────────────────────────────────────────────────┘   │
│                          │                                  │
│                          ▼                                  │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              MySQL Database                         │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 Keuntungan Arsitektur Monolitik untuk HRIS

| Aspek | Keuntungan |
|-------|------------|
| **Simplicity** | Lebih mudah dikembangkan, di-test, dan di-debug |
| **Deployment** | Satu JAR file, deploy lebih sederhana |
| **Performance** | Tidak ada network latency antar service (in-process calls) |
| **Transaction** | ACID transaction lebih mudah di-handle |
| **Development** | Tim lebih kecil, tidak butuh DevOps yang kompleks |
| **Debugging** | Stack trace lebih jelas, logging terpusat |
| **Testing** | Integration test lebih straightforward |
| **Cost** | Lebih hemat resource infrastruktur |

### 1.3 Kapan Monolitik Cocok untuk HRIS?

✅ **Cocok karena:**
- Aplikasi HRIS berukuran menengah (tidak enterprise-scale)
- Tim development kecil hingga menengah
- Tidak memerlukan scaling yang kompleks
- Fokus pada fitur HRIS lengkap, bukan microservices
- Data consistency sangat penting (payroll, attendance)

❌ **Tidak cocok jika:**
- Aplikasi akan memiliki ratusan developer
- Setiap modul butuh scaling independen
- Teknologi berbeda untuk setiap modul

### 1.4 Struktur Paket Java (Monolitik)

```
com.hris
├── HrisApplication.java          # Main class
├── config/                        # Konfigurasi global
│   ├── SecurityConfig.java
│   ├── WebConfig.java
│   └── RoleConfig.java
├── controller/                    # Semua controller satu paket
│   ├── AuthController.java
│   ├── EmployeeController.java
│   ├── LecturerController.java
│   └── ...
├── service/                       # Semua service satu paket
│   ├── AuthService.java
│   ├── EmployeeService.java
│   └── ...
├── repository/                    # Semua repository satu paket
│   ├── EmployeeRepository.java
│   └── ...
├── model/                         # Semua entity satu paket
│   ├── Employee.java
│   └── enums/
├── dto/                           # Semua DTO satu paket
├── security/                      # Security logic
├── exception/                     # Global exception handler
└── util/                          # Utility classes
```

### 1.5 Deployment Monolitik

```bash
# Build single JAR
mvn clean package

# Run single JAR
java -jar hris-sederhana.jar

# Atau dengan profile
java -jar hris-sederhana.jar --spring.profiles.active=production
```

**Single Point of Deployment:**
- Satu application server
- Satu database connection pool
- Satu logging configuration
- Satu monitoring setup

---

## 2. Sistem Role & Access Control

### 2.1 Role Selection Flow

```
┌──────────────┐     ┌──────────────────┐     ┌─────────────────────┐
│   Login      │────▶│ Role Selection   │────▶│ Dashboard per Role  │
│   Page       │     │   Page           │     │                     │
└──────────────┘     └──────────────────┘     └─────────────────────┘
                           │
                           ├─── Pilih ADMIN  ──▶ Dashboard ADMIN
                           ├─── Pilih HR     ──▶ Dashboard HR
                           ├─── Pilih EMPLOYEE──▶ Dashboard EMPLOYEE
                           └─── Pilih DOSEN  ──▶ Dashboard DOSEN
```

**Contoh Scenario:**
- **User A** punya role: ADMIN, EMPLOYEE, DOSEN
  - Setelah login, muncul 3 pilihan role
  - Jika pilih ADMIN → masuk mode ADMIN (akses penuh ke semua data)
  - Jika pilih EMPLOYEE → masuk mode EMPLOYEE (hanya data diri sendiri)
  - Jika pilih DOSEN → masuk mode DOSEN (data dosen & self-service)

- **User B** (Staff Biro Akademik) hanya punya role: EMPLOYEE
  - Setelah login, langsung masuk dashboard EMPLOYEE (tanpa pilihan)

### 2.2 Role Definitions

| Role | Deskripsi | Akses Data |
|------|-----------|------------|
| **ADMIN** | Administrator | Akses SEMUA data (employee, dosen, payroll, user management) |
| **HR** | HR Staff | Akses data Employee + Dosen (CRUD, attendance, leave, payroll) |
| **EMPLOYEE** | Karyawan/Staff | Self-service data diri sendiri (clock in/out, pengajuan cuti, slip gaji) |
| **DOSEN** | Dosen | Self-service data diri + data dosen (clock in/out, pengajuan cuti, slip gaji dosen) |

### 2.3 Fitur per Role

**ADMIN** - Full Access:
- Manajemen User (create, edit, delete user & assign role)
- Manajemen Department & Position
- Manajemen Employee (CRUD semua employee)
- Manajemen Dosen (CRUD semua dosen)
- Attendance (view semua data)
- Leave Request (view semua, approve/reject)
- Payroll (generate & view semua payroll)
- Laporan & Export
- Settings aplikasi

**HR** - HR Management:
- View Department & Position
- Manajemen Employee (CRUD semua employee)
- Manajemen Dosen (CRUD semua dosen)
- Attendance (view semua data)
- Leave Request (view semua, approve/reject)
- Payroll (generate & view semua payroll)
- Laporan & Export

**EMPLOYEE** - Self Service:
- View profil sendiri
- Edit profil sendiri (terbatas)
- Clock In / Clock Out
- View riwayat absensi sendiri
- Pengajuan cuti
- View saldo cuti sendiri
- View slip gaji sendiri

**DOSEN** - Lecturer Service:
- View profil dosen sendiri
- Edit profil dosen sendiri (terbatas)
- Clock In / Clock Out
- View riwayat absensi sendiri
- Pengajuan cuti
- View saldo cuti sendiri
- View slip gaji dosen
- (Future) Jadwal mengajar
- (Future) Input nilai

---

## 3. Konsep Data Employee vs Dosen

### 3.1 Employee dengan Role EMPLOYEE
- Punya data karyawan
- Digaji sebagai karyawan (tabel `payrolls`)
- Punya leave balance

### 3.2 Employee dengan Role DOSEN
- Punya data karyawan
- Punya **lecturer profile** (NIDN, pendidikan, keahlian, jenjang, homebase prodi)
- Digaji sebagai dosen (tabel `lecturer_salaries`)
- Punya leave balance

### 3.3 Employee dengan Role EMPLOYEE + DOSEN
- Punya data karyawan
- Punya lecturer profile
- Bisa digaji sebagai karyawan DAN dosen

### Contoh Assignment

| User | Role | Punya Data |
|------|------|------------|
| Staff Biro Akademik | EMPLOYEE | Data karyawan |
| Dosen Biasa | DOSEN | Data karyawan + Lecturer profile |
| Dosen yang juga HR | HR, DOSEN | Data karyawan + Lecturer profile |
| Admin (bukan dosen) | ADMIN | Data karyawan |
| Admin (sekaligus dosen) | ADMIN, DOSEN | Data karyawan + Lecturer profile |

---

## 4. Fitur-Fitur Utama

### 4.1 Authentication & Authorization
- Login dengan email/username & password
- Role selection setelah login (jika user punya lebih dari 1 role)
- Session management dengan active role
- Logout

### 4.2 User Activity Log (Audit Trail)
- **Logging otomatis semua aktivitas user**
- Aktivitas yang dicatat:
  - **Authentication**: Login (success/failed), Logout, Role Selection
  - **CRUD Operations**: Create, Read, Update, Delete (soft delete), Restore
  - **Business Operations**: Clock In/Out, Submit Leave Request, Approve/Reject Leave, Generate Payroll
  - **Data Access**: View sensitive data (payslip, personal info)
- Informasi yang dicapat:
  - User yang melakukan aktivitas
  - Jenis aktivitas
  - Modul/entity yang diakses
  - Timestamp aktivitas
  - IP Address
  - User Agent (browser/device)
  - Detail aktivitas (JSON format)
  - Status (SUCCESS/FAILED)
  - Error message (jika failed)

### 4.3 Manajemen Institusi/Perusahaan (Company Management)
- **Singleton data - hanya 1 institusi**
- Manage profil institusi (ADMIN only)
  - Data dasar: nama, kode, tipe (UNIVERSITY/COMPANY/SCHOOL/OTHER)
  - Alamat lengkap: jalan, kota, provinsi, kode pos
  - Kontak: telepon, email, website
  - Data legal: NPWP perusahaan, SIUP, tanggal pendirian
  - Data BPJS perusahaan: nomor BPJS Ketenagakerjaan, BPJS Kesehatan
  - Data keuangan: bank untuk payroll, nomor rekening perusahaan
  - Branding: logo, stempel/tanda tangan digital (untuk slip gaji)
- Konfigurasi jam kerja:
  - Working days (hari kerja)
  - Clock in start/end (jam masuk - untuk deteksi telat)
  - Clock out start/end (jam pulang)

### 4.4 Manajemen User (ADMIN only)
- CRUD user
- Assign role ke user (bisa multiple)
- Reset password
- Aktivasi/deaktivasi user

### 4.5 Manajemen Karyawan (Employee Management)
- CRUD data karyawan (ADMIN, HR)
- Data pribadi (nama, NIK, email, telepon, alamat)
- Data pekerjaan (department, jabatan, tanggal bergabung, status)
- Upload foto profil
- **Role Assignment**: Assign EMPLOYEE dan/atau DOSEN role
- Riwayat pekerjaan/promosi

### 4.6 Manajemen Department & Jabatan
- CRUD department dengan flag Prodi
  - Field `is_prodi`: menandai apakah department adalah Prodi
  - Field `kode_prodi`: kode program studi (jika is_prodi = true)
- CRUD jabatan/position
- Struktur organisasi sederhana

### 4.7 Manajemen Dosen (Lecturer Management)
- **Dosen adalah Employee dengan role DOSEN**
- CRUD profile dosen (ADMIN, HR)
- Data akademik (pendidikan terakhir, bidang keahlian)
- Homebase Prodi (link ke department dengan is_prodi = true)
- Jenjang dosen (Asisten Ahli, Lektor, Lektor Kepala, Profesor)
- Status kepegawaian dosen: **Dosen Tetap** atau **Dosen Tidak Tetap**
- Status kerja dosen (Aktif, Cuti, Pensiun)
- NIDN (Nomor Induk Dosen Nasional)

### 4.8 Absensi (Attendance)
- Clock In / Clock Out (EMPLOYEE, DOSEN)
- Lihat riwayat absensi
- Rekap absensi bulanan (ADMIN, HR)
- Status: Hadir, Telat, Izin, Sakit, Alpha

### 4.9 Pengajuan Cuti (Leave Request)
- Pengajuan cuti oleh karyawan/dosen
- Approval oleh HR atau ADMIN
- Saldo cuti
- Kategori cuti: Tahunan, Sakit, Melahirkan, Menikah, dll

### 4.10 Payroll Karyawan
- Komponen gaji: Gaji pokok, tunjangan, lembur, potongan
- Slip gaji
- Rekap gaji bulanan (ADMIN, HR)

### 4.11 Payroll Dosen
- Komponen gaji dosen: Gaji pokok, tunjangan sertifikasi, tunjangan fungsional, honor mengajar
- Slip gaji dosen
- Rekap gaji dosen per periode (ADMIN, HR)

---

## 5. Kelengkapan Data Employee (Sesuai Regulasi Indonesia)

### 5.1 Dasar Hukum

Aplikasi ini dirancang untuk memenuhi kewajiban pengusaha dalam menyimpan data tenaga kerja sesuai dengan:

1. **Pasal 185 UU No. 13 Tahun 2003 tentang Ketenagakerjaan**
   - Pengusaha wajib menyimpan "Daftar Tenaga Kerja" dan "Daftar Upah"
   - Data wajib disimpan minimal 10 tahun setelah hubungan kerja berakhir

2. **Peraturan Menteri Ketenagakerjaan**
   - WLKP (Wajib Lapor Ketenagakerjaan Perusahaan)
   - Kewajiban lapor periodik ke Kemnaker

3. **Peraturan BPJS Ketenagakerjaan**
   - Kewajiban mendaftarkan seluruh pekerja
   - Maksimal upah yang dijaminkan: Rp9.559.600/bulan (2024)

4. **Peraturan BPJS Kesehatan**
   - Kewajiban mendaftarkan seluruh pekerja dan keluarga

### 5.2 Kategori Data Employee

#### 5.2.1 Data Identitas Pribadi (Wajib)
| Field | Keterangan | Dasar Hukum |
|-------|------------|-------------|
| `nik` | Nomor Induk Kependudukan (KTP) | UU Kependudukan |
| `full_name` | Nama lengkap sesuai KTP | UU Kependudukan |
| `place_of_birth` | Tempat lahir | BPJS Ketenagakerjaan |
| `date_of_birth` | Tanggal lahir | BPJS Ketenagakerjaan |
| `gender` | Jenis kelamin (MALE/FEMALE) | BPJS Ketenagakerjaan |
| `mothers_name` | Nama ibu kandung | **Wajib untuk BPJS** |
| `address` | Alamat lengkap | UU Ketenagakerjaan |
| `phone` | Nomor telepon | UU Ketenagakerjaan |
| `email` | Email (untuk login) | - |

#### 5.2.2 Data Keluarga (Untuk Pajak & BPJS)
| Field | Keterangan | Dasar Hukum |
|-------|------------|-------------|
| `kk_number` | Nomor Kartu Keluarga | BPJS Kesehatan |
| `marital_status` | Status pernikahan (SINGLE, MARRIED, DIVORCED, WIDOWED) | PPh 21 |
| `spouse_name` | Nama pasangan (jika menikah) | BPJS Kesehatan |
| `number_of_dependents` | Jumlah tanggungan (untuk PPh 21) | UU Pajak |

#### 5.2.3 Data Kepegawaian (Pasal 185 UU 13/2003)
| Field | Keterangan | Dasar Hukum |
|-------|------------|-------------|
| `employment_status` | Status kepegawaian (PERMANENT, CONTRACT, PROBATION, DAILY) | UU Ketenagakerjaan |
| `hire_date` | Tanggal mulai bekerja | **Wajib Pasal 185** |
| `work_location` | Lokasi kerja | WLKP |
| `status` | Status employee (ACTIVE, INACTIVE, RESIGNED) | UU Ketenagakerjaan |
| `resignation_date` | Tanggal resign | UU Ketenagakerjaan |
| `resignation_reason` | Alasan resign | UU Ketenagakerjaan |

#### 5.2.4 Data BPJS (Wajib Peraturan Pemerintah)
| Field | Keterangan | Dasar Hukum |
|-------|------------|-------------|
| `bpjs_ketenagakerjaan_no` | Nomor BPJS Ketenagakerjaan | **Wajib** |
| `bpjs_kesehatan_no` | Nomor BPJS Kesehatan | **Wajib** |
| `basic_salary` | Gaji pokok (untuk BPJS) | **Wajib (maksimal Rp9.559.600 untuk iuran BPJS TK)** |

#### 5.2.5 Data Pajak
| Field | Keterangan | Dasar Hukum |
|-------|------------|-------------|
| `npwp` | Nomor Pokok Wajib Pajak | UU PPh 21 |

#### 5.2.6 Data Organisasi
| Field | Keterangan | Dasar Hukum |
|-------|------------|-------------|
| `department_id` | Department/Unit kerja | Organisasi |
| `position_id` | Jabatan/Position | Organisasi |
| `photo_path` | Path foto profil | - |

### 5.3 Mapping ke Tabel employees

```sql
-- Urutan field dalam tabel employees (sudah di-update):
CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- === DATA IDENTITAS (Per UU Ketenagakerjaan & BPJS) ===
    nik VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    place_of_birth VARCHAR(50),
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE') NOT NULL,
    mothers_name VARCHAR(100),           -- Wajib untuk BPJS

    -- === ALAMAT & KONTAK ===
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(100) UNIQUE NOT NULL,

    -- === DATA KEPEGAWAIAN (Per Pasal 185 UU 13/2003) ===
    employment_status ENUM('PERMANENT', 'CONTRACT', 'PROBATION', 'DAILY'),
    hire_date DATE NOT NULL,
    work_location VARCHAR(100),

    -- === DATA BPJS (Wajib per Peraturan Pemerintah) ===
    bpjs_ketenagakerjaan_no VARCHAR(20) UNIQUE,
    bpjs_kesehatan_no VARCHAR(20) UNIQUE,
    npwp VARCHAR(20) UNIQUE,

    -- === DATA GAJI (Untuk BPJS & Payroll) ===
    basic_salary DECIMAL(15, 2),

    -- === DATA ORGANISASI ===
    department_id BIGINT,
    position_id BIGINT,

    -- === DATA TAMBAHAN ===
    kk_number VARCHAR(20),
    photo_path VARCHAR(255),
    marital_status ENUM('SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED'),
    spouse_name VARCHAR(100),
    number_of_dependents INT DEFAULT 0,

    -- === STATUS ===
    status ENUM('ACTIVE', 'INACTIVE', 'RESIGNED'),
    resignation_date DATE,
    resignation_reason TEXT,

    -- === AUDIT ===
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
);
```

### 5.4 Kewajiban Pelaporan (WLKP)

Perusahaan wajib melaporkan data ketenagakerjaan melalui sistem **WLKP (Wajib Lapor Ketenagakerjaan Perusahaan)** di [kemnaker.go.id](https://www.kemnaker.go.id/):

**Data yang Dilaporkan:**
1. Nama Perusahaan
2. Jumlah tenaga kerja
3. Daftar tenaga kerja aktif dengan identitas lengkap
4. Kondisi ketenagakerjaan

### 5.5 Dokumen Karyawan yang Wajib Disimpan

Selain data di database, perusahaan wajib menyimpan dokumen fisik/digital berikut:

| Dokumen | Keterangan | Retensi |
|---------|------------|---------|
| Fotokopi KTP | Identitas resmi | Selama masa kerja + 10 tahun |
| Kartu Keluarga | Untuk BPJS Kesehatan | Selama masa kerja + 10 tahun |
| NPWP | Untuk PPh 21 | Selama masa kerja + 10 tahun |
| Kontrak Kerja / Surat Perjanjian Kerja | Bukti hubungan kerja | Selama masa kerja + 10 tahun |
| Slip Gaji | Bukti pembayaran upah | Selama masa kerja + 10 tahun |
| Data Absensi | Bukit kehadiran | Selama masa kerja + 10 tahun |

### 5.6 Catatan Implementasi

1. **Field `mothers_name` wajib diisi** untuk pendaftaran BPJS Ketenagakerjaan
2. **Field `basic_salary`** digunakan untuk:
   - Perhitungan iuran BPJS Ketenagakerjaan (maksimal Rp9.559.600)
   - Perhitungan iuran BPJS Kesehatan
   - Perhitungan PPh 21
3. **Field `employment_status`** menentukan jenis kontrak dan hak-hak employee
4. **Data retensi**: Semua data dan dokumen employee wajib disimpan minimal 10 tahun setelah hubungan kerja berakhir (UU No. 8 Tahun 1997)

### 5.7 Sumber Referensi

- [Undang-Undang No. 13 Tahun 2003 tentang Ketenagakerjaan](https://peraturan.bpk.go.id/Download/31128/UU%20Nomor%2013%20Tahun%202003.pdf)
- [Panduan WLKP - Kementerian Ketenagakerjaan](https://box.kemnaker.go.id/docs/wlkp/Panduan%20WLKP.pdf)
- [BPJS Ketenagakerjaan - Penerima Upah](https://www.bpjsketenagakerjaan.go.id/penerima-upah.html)
- [Wajib Lapor Ketenagakerjaan Perusahaan - Kemnaker](https://bantuan.kemnaker.go.id/support/solutions/folders/43000232593)

---

## 6. Desain Database (Skema)

### 6.1 Soft Delete Policy

**PENTING**: Aplikasi ini menggunakan **Soft Delete** untuk semua data. Tidak ada hard delete.

**Aturan Soft Delete:**
- Semua tabel memiliki field `deleted_at TIMESTAMP NULL`
- Data tidak dihapus secara fisik dari database
- Saat dihapus, field `deleted_at` diisi dengan timestamp saat ini
- Semua query WAJIB menambahkan filter `WHERE deleted_at IS NULL`
- Data yang sudah dihapus (`deleted_at IS NOT NULL`) tidak ditampilkan di aplikasi
- Sebagian besar tabel juga memiliki field `deleted_by BIGINT` untuk tracking siapa yang menghapus

**Keuntungan Soft Delete:**
1. Audit trail lengkap - semua data history tersimpan
2. Data bisa di-restore jika diperlukan
3. Memenuhi retensi data 10 tahun sesuai UU No. 8 Tahun 1997
4. Analisis historical data possible

### 6.2 ERD Diagram

```
┌──────────────────────────────────────┐
│            companies                 │  ◄─── Singleton (1 data only)
├──────────────────────────────────────┤
│ id (PK)                             │
│ name                                │
│ code                                │
│ type (UNIVERSITY, COMPANY, ...)     │
│ address                             │
│ city, province, postal_code         │
│ phone, email, website               │
│ npwp_company                        │
│ bpjs_ketenagakerjaan_no             │
│ bpjs_kesehatan_no                   │
│ bank_name, bank_account_number      │
│ logo_path, stamp_path               │
│ working_days                        │
│ clock_in_start, clock_in_end        │
│ clock_out_start, clock_out_end      │
│ created_at, updated_at              │
│ created_by, updated_by              │
│ deleted_at, deleted_by              │
└──────────────────────────────────────┘
                    │
                    │ Referensi (tidak ada FK, hanya lookup)
                    │
        ┌───────────┴──────────────────────────────┐
        │                                          │
        ▼                                          ▼
┌──────────────────────────────────────┐       ┌──────────────────┐
│           departments               │       │    positions     │
├──────────────────────────────────────┤       ├──────────────────┤
│ id (PK)                             │       │ id (PK)          │
│ name                                │       │ name             │
│ description                         │       │ description      │
│ is_prodi (BOOLEAN)                  │       │ base_salary      │
│ kode_prodi (VARCHAR)                │       │ created_at       │
│ created_at                          │       │ updated_at       │
│ updated_at                          │       └──────────────────┘
└──────────────────────────────────────┘
              │
              │──────────────────────────────────┐
              │                                  │
              ▼                                  ▼
┌─────────────────────────────────────────────────────────┐
│                      employees                          │
├─────────────────────────────────────────────────────────┤
│ id (PK)                                                 │
│ nik (UNIQUE)                                            │
│ full_name                                               │
│ email (UNIQUE)                                          │
│ password                                                │
│ phone                                                  │
│ address                                                │
│ date_of_birth                                          │
│ gender                                                 │
│ photo_path                                             │
│ department_id (FK)                                      │
│ position_id (FK)                                        │
│ hire_date                                              │
│ status (ACTIVE, INACTIVE, RESIGNED)                     │
│ created_at                                              │
│ updated_at                                              │
└─────────────────────────────────────────────────────────┘
         │
         ├──┐
         │  │
         │  └──────────────────────────────────────┐
         │                                         │
         │                                         ▼
         │                              ┌───────────────────┐
         │                              │  employee_roles   │
         │                              ├───────────────────┤
         │                              │ id (PK)           │
         │                              │ employee_id (FK)  │◄────────┐
         │                              │ role (ENUM)       │         │
         │                              │   (ADMIN, HR,     │         │
         │                              │    EMPLOYEE, DOSEN)│         │
         │                              │ created_at        │         │
         │                              └───────────────────┘         │
         │                                                            │
         ▼                                                            │
┌──────────────────┐                                              │
│ lecturer_profiles│                                              │
├──────────────────┤                                              │
│ id (PK)          │                                              │
│ employee_id (FK) │◄─────────────────────────────────────────────┘
│ nidn (UNIQUE)    │
│ last_education   │
│ expertise        │
│ rank             │
│   (ASISTEN_AHLI, │
│    LEKTOR,       │
│    LEKTOR_KEPALA,│
│    PROFESOR)     │
│ employment_status│
│   (DOSEN_TETAP,  │
│    DOSEN_TIDAK_  │
│     TETAP)       │
│ work_status      │
│   (ACTIVE,       │
│    LEAVE,        │
│    RETIRED)      │
│ homebase_prodi_id│
│   (FK to dept)   │
│ created_at       │
│ updated_at       │
└──────────────────┘
         │
         ▼
┌──────────────────┐
│ lecturer_salaries│
├──────────────────┤
│ id (PK)          │
│ lecturer_profile_id(FK)│
│ period (YYYY-MM) │
│ basic_salary     │
│ certification    │
│ functional_allow │
│ teaching_honor  │
│ other_allowances │
│ total_salary     │
│ status           │
│ created_at       │
└──────────────────┘

┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
│  attendances     │ │   leave_requests │ │    payrolls      │
├──────────────────┤ ├──────────────────┤ ├──────────────────┤
│ id (PK)          │ │ id (PK)          │ │ id (PK)          │
│ employee_id (FK) │ │ employee_id (FK) │ │ employee_id (FK) │
│ date             │ │ leave_type       │ │ period           │
│ clock_in         │ │ start_date       │ │ basic_salary     │
│ clock_out        │ │ end_date         │ │ allowances       │
│ status           │ │ reason           │ │ overtime         │
│ notes            │ │ status           │ │ deductions       │
│ created_at       │ │ (PENDING, APPROVED│ │ total_salary     │
│ updated_at       │ │  REJECTED)       │ │ status           │
└──────────────────┘ │ approved_by      │ │ created_at       │
                    │ approved_at      │ └──────────────────┘
                    │ created_at       │
                    └──────────────────┘

┌──────────────────┐
│ leave_balances   │
├──────────────────┤
│ id (PK)          │
│ employee_id (FK) │
│ year             │
│ annual_total     │
│ annual_used      │
│ sick_total       │
│ sick_used        │
│ created_at       │
│ updated_at       │
└──────────────────┘

┌──────────────────────────────────────┐
│        user_sessions                 │
├──────────────────────────────────────┤
│ id (PK)                             │
│ employee_id (FK)                    │
│ selected_role (ENUM)                │
│   (ADMIN, HR, EMPLOYEE, DOSEN)      │
│ session_token                       │
│ created_at                          │
│ expires_at                          │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│      user_activity_logs              │
├──────────────────────────────────────┤
│ id (PK)                             │
│ employee_id (FK)                    │
│ activity_type (ENUM)                │
│   (LOGIN, LOGOUT, ROLE_SELECTION,    │
│    CREATE, READ, UPDATE, DELETE,     │
│    RESTORE, CLOCK_IN, CLOCK_OUT,     │
│    SUBMIT_LEAVE, APPROVE_LEAVE,      │
│    REJECT_LEAVE, GENERATE_PAYROLL,   │
│    VIEW_SENSITIVE_DATA)              │
│ module_name                         │
│ entity_type                         │
│ entity_id                           │
│ description                         │
│ activity_details (JSON)             │
│ status (ENUM)                       │
│   (SUCCESS, FAILED)                 │
│ error_message                       │
│ ip_address                          │
│ user_agent                          │
│ created_at                          │
└──────────────────────────────────────┘
```

### Detail Tabel

#### companies
```sql
-- Tabel ini menyimpan data institusi/perusahaan (singleton - hanya 1 data)
CREATE TABLE companies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- === DATA PERUSAHAAN/INSTITUSI ===
    name VARCHAR(200) NOT NULL COMMENT 'Nama perusahaan/institusi',
    code VARCHAR(50) UNIQUE COMMENT 'Kode perusahaan (untuk reports)',
    type ENUM('COMPANY', 'UNIVERSITY', 'SCHOOL', 'OTHER') DEFAULT 'UNIVERSITY' COMMENT 'Tipe institusi',

    -- === ALAMAT & KONTAK ===
    address TEXT COMMENT 'Alamat lengkap perusahaan',
    city VARCHAR(100) COMMENT 'Kota',
    province VARCHAR(100) COMMENT 'Provinsi',
    postal_code VARCHAR(10) COMMENT 'Kode pos',
    phone VARCHAR(20) COMMENT 'Nomor telepon',
    email VARCHAR(100) COMMENT 'Email perusahaan',
    website VARCHAR(255) COMMENT 'Website',

    -- === DATA LEGAL ===
    npwp_company VARCHAR(25) UNIQUE COMMENT 'NPWP Perusahaan',
    siup_number VARCHAR(50) COMMENT 'Nomor SIUP',
    siup_expired_date DATE COMMENT 'Tanggal expired SIUP',
    establishment_date DATE COMMENT 'Tanggal pendirian perusahaan',

    -- === DATA BPJS PERUSAHAAN ===
    bpjs_ketenagakerjaan_no VARCHAR(30) COMMENT 'Nomor BPJS Ketenagakerjaan Perusahaan',
    bpjs_kesehatan_no VARCHAR(30) COMMENT 'Nomor BPJS Kesehatan Perusahaan',

    -- === DATA KEUANGAN ===
    tax_address TEXT COMMENT 'Alamat untuk pajak',
    bank_name VARCHAR(100) COMMENT 'Bank untuk payroll',
    bank_account_number VARCHAR(50) COMMENT 'Nomor rekening perusahaan',
    bank_account_name VARCHAR(100) COMMENT 'Nama pemilik rekening',

    -- === BRANDING ===
    logo_path VARCHAR(255) COMMENT 'Path logo perusahaan',
    stamp_path VARCHAR(255) COMMENT 'Path stempel/tanda tangan digital (untuk slip gaji)',

    -- === KONFIGURASI ===
    working_days VARCHAR(20) DEFAULT 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY' COMMENT 'Hari kerja',
    clock_in_start TIME DEFAULT '08:00:00' COMMENT 'Jam masuk kerja (mulai)',
    clock_in_end TIME DEFAULT '09:00:00' COMMENT 'Jam masuk kerja (akhir - batang terlambat)',
    clock_out_start TIME DEFAULT '17:00:00' COMMENT 'Jam pulang kerja (mulai)',
    clock_out_end TIME DEFAULT '18:00:00' COMMENT 'Jam pulang kerja (akhir)',

    -- === AUDIT ===
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat data',
    updated_by BIGINT COMMENT 'User yang terakhir mengupdate',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    FOREIGN KEY (created_by) REFERENCES employees(id),
    FOREIGN KEY (updated_by) REFERENCES employees(id),
    FOREIGN KEY (deleted_by) REFERENCES employees(id)
);

CREATE INDEX idx_companies_deleted_at ON companies(deleted_at);
```

#### employees
```sql
CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- === DATA IDENTITAS (Per UU Ketenagakerjaan & BPJS) ===
    nik VARCHAR(20) UNIQUE NOT NULL COMMENT 'Nomor Induk Kependudukan',
    full_name VARCHAR(100) NOT NULL COMMENT 'Nama lengkap sesuai KTP',
    place_of_birth VARCHAR(50) COMMENT 'Tempat lahir',
    date_of_birth DATE NOT NULL COMMENT 'Tanggal lahir',
    gender ENUM('MALE', 'FEMALE') NOT NULL COMMENT 'Jenis kelamin',
    mothers_name VARCHAR(100) COMMENT 'Nama ibu kandung (wajib untuk BPJS)',

    -- === ALAMAT & KONTAK ===
    address TEXT COMMENT 'Alamat lengkap',
    phone VARCHAR(20) COMMENT 'Nomor telepon',
    email VARCHAR(100) UNIQUE NOT NULL,

    -- === DATA KEPEGAWAIAN (Per Pasal 185 UU 13/2003) ===
    employment_status ENUM('PERMANENT', 'CONTRACT', 'PROBATION', 'DAILY') DEFAULT 'PERMANENT' COMMENT 'Status kepegawaian',
    hire_date DATE NOT NULL COMMENT 'Tanggal mulai bekerja',
    work_location VARCHAR(100) COMMENT 'Lokasi kerja',

    -- === DATA BPJS (Wajib per Peraturan Pemerintah) ===
    bpjs_ketenagakerjaan_no VARCHAR(20) UNIQUE COMMENT 'Nomor BPJS Ketenagakerjaan',
    bpjs_kesehatan_no VARCHAR(20) UNIQUE COMMENT 'Nomor BPJS Kesehatan',
    npwp VARCHAR(20) UNIQUE COMMENT 'Nomor Pokok Wajib Pajak',

    -- === DATA GAJI (Untuk BPJS & Payroll) ===
    basic_salary DECIMAL(15, 2) COMMENT 'Gaji pokok (untuk BPJS & payroll)',

    -- === DATA ORGANISASI ===
    department_id BIGINT COMMENT 'Department/Unit kerja',
    position_id BIGINT COMMENT 'Jabatan/Position',

    -- === DATA TAMBAHAN ===
    kk_number VARCHAR(20) COMMENT 'Nomor Kartu Keluarga',
    photo_path VARCHAR(255) COMMENT 'Path foto profil',
    marital_status ENUM('SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED') COMMENT 'Status pernikahan',
    spouse_name VARCHAR(100) COMMENT 'Nama pasangan (jika menikah)',
    number_of_dependents INT DEFAULT 0 COMMENT 'Jumlah tanggungan (untuk pajak)',

    -- === STATUS ===
    status ENUM('ACTIVE', 'INACTIVE', 'RESIGNED') DEFAULT 'ACTIVE' COMMENT 'Status employee',
    resignation_date DATE COMMENT 'Tanggal resign (jika status=RESIGNED)',
    resignation_reason TEXT COMMENT 'Alasan resign',

    -- === AUDIT ===
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat data',
    updated_by BIGINT COMMENT 'User yang terakhir mengupdate',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (position_id) REFERENCES positions(id),
    FOREIGN KEY (created_by) REFERENCES employees(id),
    FOREIGN KEY (updated_by) REFERENCES employees(id),
    FOREIGN KEY (deleted_by) REFERENCES employees(id)
);

-- INDEX untuk soft delete
CREATE INDEX idx_employees_deleted_at ON employees(deleted_at);
```

#### employee_roles
```sql
CREATE TABLE employee_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    role ENUM('ADMIN', 'HR', 'EMPLOYEE', 'DOSEN') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang assign role',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus role',

    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (created_by) REFERENCES employees(id),
    FOREIGN KEY (deleted_by) REFERENCES employees(id),
    UNIQUE KEY unique_employee_role (employee_id, role)
);

CREATE INDEX idx_employee_roles_deleted_at ON employee_roles(deleted_at);
```

#### departments
```sql
CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_prodi BOOLEAN DEFAULT FALSE,
    kode_prodi VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat data',
    updated_by BIGINT COMMENT 'User yang terakhir mengupdate',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    FOREIGN KEY (created_by) REFERENCES employees(id),
    FOREIGN KEY (updated_by) REFERENCES employees(id),
    FOREIGN KEY (deleted_by) REFERENCES employees(id)
);

CREATE INDEX idx_departments_deleted_at ON departments(deleted_at);
```

#### positions
```sql
CREATE TABLE positions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    base_salary DECIMAL(15, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat data',
    updated_by BIGINT COMMENT 'User yang terakhir mengupdate',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    FOREIGN KEY (created_by) REFERENCES employees(id),
    FOREIGN KEY (updated_by) REFERENCES employees(id),
    FOREIGN KEY (deleted_by) REFERENCES employees(id)
);

CREATE INDEX idx_positions_deleted_at ON positions(deleted_at);
```

#### lecturer_profiles
```sql
CREATE TABLE lecturer_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT UNIQUE NOT NULL,
    nidn VARCHAR(15) UNIQUE NOT NULL COMMENT 'Nomor Induk Dosen Nasional',
    last_education VARCHAR(50) COMMENT 'Pendidikan terakhir (S2, S3, dll)',
    expertise VARCHAR(100) COMMENT 'Bidang keahlian',
    rank ENUM('ASISTEN_AHLI', 'LEKTOR', 'LEKTOR_KEPALA', 'PROFESOR') COMMENT 'Jenjang akademik',
    employment_status ENUM('DOSEN_TETAP', 'DOSEN_TIDAK_TETAP') DEFAULT 'DOSEN_TETAP' COMMENT 'Status kepegawaian dosen',
    work_status ENUM('ACTIVE', 'LEAVE', 'RETIRED') DEFAULT 'ACTIVE' COMMENT 'Status kerja',
    homebase_prodi_id BIGINT COMMENT 'Homebase prodi (dept dengan is_prodi=true)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat data',
    updated_by BIGINT COMMENT 'User yang terakhir mengupdate',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (homebase_prodi_id) REFERENCES departments(id),
    FOREIGN KEY (created_by) REFERENCES employees(id),
    FOREIGN KEY (updated_by) REFERENCES employees(id),
    FOREIGN KEY (deleted_by) REFERENCES employees(id)
);

CREATE INDEX idx_lecturer_profiles_deleted_at ON lecturer_profiles(deleted_at);
```

#### lecturer_salaries
```sql
CREATE TABLE lecturer_salaries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lecturer_profile_id BIGINT NOT NULL,
    period VARCHAR(7) NOT NULL,
    basic_salary DECIMAL(15, 2) NOT NULL,
    certification_allowance DECIMAL(15, 2) DEFAULT 0,
    functional_allowance DECIMAL(15, 2) DEFAULT 0,
    teaching_honor DECIMAL(15, 2) DEFAULT 0,
    other_allowances DECIMAL(15, 2) DEFAULT 0,
    total_salary DECIMAL(15, 2) NOT NULL,
    status ENUM('DRAFT', 'PAID') DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat data',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    FOREIGN KEY (lecturer_profile_id) REFERENCES lecturer_profiles(id),
    FOREIGN KEY (created_by) REFERENCES employees(id),
    FOREIGN KEY (deleted_by) REFERENCES employees(id),
    UNIQUE KEY unique_lecturer_period (lecturer_profile_id, period)
);

CREATE INDEX idx_lecturer_salaries_deleted_at ON lecturer_salaries(deleted_at);
```

#### attendances
```sql
CREATE TABLE attendances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    date DATE NOT NULL,
    clock_in TIME,
    clock_out TIME,
    status ENUM('PRESENT', 'LATE', 'LEAVE', 'SICK', 'ABSENT') DEFAULT 'PRESENT',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang mencatat absensi',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (created_by) REFERENCES employees(id),
    FOREIGN KEY (deleted_by) REFERENCES employees(id),
    UNIQUE KEY unique_employee_date (employee_id, date)
);

CREATE INDEX idx_attendances_deleted_at ON attendances(deleted_at);
```

#### leave_requests
```sql
CREATE TABLE leave_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    leave_type ENUM('ANNUAL', 'SICK', 'MATERNITY', 'MARRIAGE', 'SPECIAL', 'UNPAID') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    approved_by BIGINT,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang mengajukan cuti',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (approved_by) REFERENCES employees(id),
    FOREIGN KEY (created_by) REFERENCES employees(id),
    FOREIGN KEY (deleted_by) REFERENCES employees(id)
);

CREATE INDEX idx_leave_requests_deleted_at ON leave_requests(deleted_at);
```

#### leave_balances
```sql
CREATE TABLE leave_balances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    year INT NOT NULL,
    annual_total INT DEFAULT 12,
    annual_used INT DEFAULT 0,
    sick_total INT DEFAULT 14,
    sick_used INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat data',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (created_by) REFERENCES employees(id),
    FOREIGN KEY (deleted_by) REFERENCES employees(id),
    UNIQUE KEY unique_employee_year (employee_id, year)
);

CREATE INDEX idx_leave_balances_deleted_at ON leave_balances(deleted_at);
```

#### payrolls
```sql
CREATE TABLE payrolls (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    period VARCHAR(7) NOT NULL,
    basic_salary DECIMAL(15, 2) NOT NULL,
    allowances DECIMAL(15, 2) DEFAULT 0,
    overtime DECIMAL(15, 2) DEFAULT 0,
    deductions DECIMAL(15, 2) DEFAULT 0,
    total_salary DECIMAL(15, 2) NOT NULL,
    status ENUM('DRAFT', 'PAID') DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat payroll',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (created_by) REFERENCES employees(id),
    FOREIGN KEY (deleted_by) REFERENCES employees(id),
    UNIQUE KEY unique_employee_period (employee_id, period)
);

CREATE INDEX idx_payrolls_deleted_at ON payrolls(deleted_at);
```

#### user_sessions
```sql
CREATE TABLE user_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    selected_role ENUM('ADMIN', 'HR', 'EMPLOYEE', 'DOSEN') NOT NULL,
    session_token VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',

    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE INDEX idx_user_sessions_deleted_at ON user_sessions(deleted_at);
```

#### user_activity_logs
```sql
CREATE TABLE user_activity_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL COMMENT 'User yang melakukan aktivitas',
    activity_type ENUM(
        'LOGIN', 'LOGOUT', 'ROLE_SELECTION',
        'CREATE', 'READ', 'UPDATE', 'DELETE', 'RESTORE',
        'CLOCK_IN', 'CLOCK_OUT',
        'SUBMIT_LEAVE', 'APPROVE_LEAVE', 'REJECT_LEAVE',
        'GENERATE_PAYROLL',
        'VIEW_SENSITIVE_DATA'
    ) NOT NULL COMMENT 'Tipe aktivitas',
    module_name VARCHAR(50) COMMENT 'Nama modul (employee, lecturer, payroll, dll)',
    entity_type VARCHAR(50) COMMENT 'Tipe entity yang diakses',
    entity_id BIGINT COMMENT 'ID entity yang diakses',
    description TEXT COMMENT 'Deskripsi aktivitas',
    activity_details JSON COMMENT 'Detail aktivitas dalam format JSON',
    status ENUM('SUCCESS', 'FAILED') DEFAULT 'SUCCESS' COMMENT 'Status aktivitas',
    error_message TEXT COMMENT 'Pesan error jika gagal',
    ip_address VARCHAR(45) COMMENT 'IP address user',
    user_agent VARCHAR(500) COMMENT 'User agent (browser/device)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Waktu aktivitas',

    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- INDEX untuk performa query
CREATE INDEX idx_activity_logs_employee_id ON user_activity_logs(employee_id);
CREATE INDEX idx_activity_logs_activity_type ON user_activity_logs(activity_type);
CREATE INDEX idx_activity_logs_created_at ON user_activity_logs(created_at);
CREATE INDEX idx_activity_logs_module ON user_activity_logs(module_name);
CREATE INDEX idx_activity_logs_entity ON user_activity_logs(entity_type, entity_id);
```

---

## 6. Struktur Project SpringBoot

```
hris-sederhana/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── hris/
│   │   │           ├── HrisApplication.java
│   │   │           ├── config/
│   │   │           │   ├── SecurityConfig.java
│   │   │           │   ├── WebConfig.java
│   │   │           │   └── RoleConfig.java
│   │   │           ├── controller/
│   │   │           │   ├── AuthController.java
│   │   │           │   ├── DashboardController.java
│   │   │           │   ├── CompanyController.java
│   │   │           │   ├── EmployeeController.java
│   │   │           │   ├── DepartmentController.java
│   │   │           │   ├── PositionController.java
│   │   │           │   ├── LecturerController.java
│   │   │           │   ├── AttendanceController.java
│   │   │           │   ├── LeaveRequestController.java
│   │   │           │   ├── PayrollController.java
│   │   │           │   ├── LecturerPayrollController.java
│   │   │           │   └── ActivityLogController.java
│   │   │           ├── service/
│   │   │           │   ├── AuthService.java
│   │   │           │   ├── DashboardService.java
│   │   │           │   ├── CompanyService.java
│   │   │           │   ├── EmployeeService.java
│   │   │           │   ├── DepartmentService.java
│   │   │           │   ├── PositionService.java
│   │   │           │   ├── LecturerService.java
│   │   │           │   ├── AttendanceService.java
│   │   │           │   ├── LeaveRequestService.java
│   │   │           │   ├── PayrollService.java
│   │   │           │   ├── LecturerPayrollService.java
│   │   │           │   └── UserActivityLogService.java
│   │   │           ├── repository/
│   │   │           │   ├── CompanyRepository.java
│   │   │           │   ├── EmployeeRepository.java
│   │   │           │   ├── EmployeeRoleRepository.java
│   │   │           │   ├── UserSessionRepository.java
│   │   │           │   ├── UserActivityLogRepository.java
│   │   │           │   ├── DepartmentRepository.java
│   │   │           │   ├── PositionRepository.java
│   │   │           │   ├── LecturerProfileRepository.java
│   │   │           │   ├── AttendanceRepository.java
│   │   │           │   ├── LeaveRequestRepository.java
│   │   │           │   ├── LeaveBalanceRepository.java
│   │   │           │   ├── PayrollRepository.java
│   │   │           │   └── LecturerSalaryRepository.java
│   │   │           ├── model/
│   │   │           │   ├── Company.java
│   │   │           │   ├── Employee.java
│   │   │           │   ├── EmployeeRole.java
│   │   │           │   ├── UserSession.java
│   │   │           │   ├── Department.java
│   │   │           │   ├── Position.java
│   │   │           │   ├── LecturerProfile.java
│   │   │           │   ├── Attendance.java
│   │   │           │   ├── LeaveRequest.java
│   │   │           │   ├── LeaveBalance.java
│   │   │           │   ├── Payroll.java
│   │   │           │   ├── LecturerSalary.java
│   │   │           │   ├── UserActivityLog.java
│   │   │           │   └── enums/
│   │   │           │       ├── RoleType.java
│   │   │           │       ├── CompanyType.java
│   │   │           │       ├── Gender.java
│   │   │           │       ├── EmployeeStatus.java
│   │   │           │       ├── EmploymentStatus.java
│   │   │           │       ├── MaritalStatus.java
│   │   │           │       ├── LecturerEmploymentStatus.java
│   │   │           │       ├── LecturerWorkStatus.java
│   │   │           │       ├── LecturerRank.java
│   │   │           │       ├── AttendanceStatus.java
│   │   │           │       ├── LeaveType.java
│   │   │           │       ├── LeaveRequestStatus.java
│   │   │           │       ├── PayrollStatus.java
│   │   │           │       └── ActivityType.java
│   │   │           ├── dto/
│   │   │           │   ├── LoginRequestDto.java
│   │   │           │   ├── RoleSelectionDto.java
│   │   │           │   ├── EmployeeDto.java
│   │   │           │   ├── LecturerProfileDto.java
│   │   │           │   ├── AttendanceDto.java
│   │   │           │   ├── LeaveRequestDto.java
│   │   │           │   ├── PayrollDto.java
│   │   │           │   ├── LecturerSalaryDto.java
│   │   │           │   └── ActivityLogDto.java
│   │   │           ├── security/
│   │   │           │   ├── UserDetailsServiceImpl.java
│   │   │           │   ├── UserRoleFilter.java
│   │   │           │   └── ActivityLogAspect.java
│   │   │           └── exception/
│   │   │               └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── templates/
│   │       │   ├── layout/
│   │       │   │   └── main.html
│   │       │   ├── fragments/
│   │       │   │   ├── header.html
│   │       │   │   ├── sidebar.html
│   │       │   │   └── footer.html
│   │       │   ├── auth/
│   │       │   │   ├── login.html
│   │       │   │   └── role-selection.html
│   │       │   ├── dashboard/
│   │       │   │   ├── admin.html
│   │       │   │   ├── hr.html
│   │       │   │   ├── employee.html
│   │       │   │   └── dosen.html
│   │       │   ├── company/
│   │       │   │   ├── profile.html
│   │       │   │   └── edit.html
│   │       │   ├── employee/
│   │       │   │   ├── list.html
│   │       │   │   ├── form.html
│   │       │   │   └── detail.html
│   │       │   ├── department/
│   │       │   │   ├── list.html
│   │       │   │   └── form.html
│   │       │   ├── position/
│   │       │   │   ├── list.html
│   │       │   │   └── form.html
│   │       │   ├── lecturer/
│   │       │   │   ├── list.html
│   │       │   │   ├── profile-form.html
│   │       │   │   └── detail.html
│   │       │   ├── attendance/
│   │       │   │   ├── list.html
│   │       │   │   ├── clock-in.html
│   │       │   │   └── clock-out.html
│   │       │   ├── leave/
│   │       │   │   ├── list.html
│   │       │   │   ├── form.html
│   │       │   │   └── approval.html
│   │       │   ├── payroll/
│   │       │   │   ├── list.html
│   │       │   │   ├── form.html
│   │       │   │   └── slip.html
│   │       │   ├── lecturer-payroll/
│   │       │   │   ├── list.html
│   │       │   │   ├── form.html
│   │       │   │   └── slip.html
│   │       │   └── index.html
│   │       └── static/
│   │           ├── css/
│   │           │   └── style.css
│   │           └── js/
│   │               └── app.js
│   └── test/
│       └── java/
└── README.md
```

---

## 7. Desain UI/UX

### 7.1 Login Flow

```
┌─────────────────────────────────────────┐
│             LOGIN PAGE                   │
├─────────────────────────────────────────┤
│ Email:    [user@example.com]           │
│ Password: [••••••••]                    │
│                                         │
│           [Login]                       │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│         ROLE SELECTION PAGE             │
├─────────────────────────────────────────┤
│ Welcome, [User Name]!                   │
│                                         │
│ Please select your role:                │
│                                         │
│  ┌──────────────┐  ┌──────────────┐    │
│  │    ADMIN     │  │      HR      │    │
│  └──────────────┘  └──────────────┘    │
│  ┌──────────────┐  ┌──────────────┐    │
│  │  EMPLOYEE    │  │    DOSEN     │    │
│  └──────────────┘  └──────────────┘    │
└─────────────────────────────────────────┘
```

### 7.2 Layout Utama

```
┌─────────────────────────────────────────────────────────────┐
│  Logo HRIS  | User: Ahmad | Role: ADMIN | Switch Role | Logout│
└─────────────────────────────────────────────────────────────┘
├──────────┬──────────────────────────────────────────────────┤
│ Sidebar  │                                                  │
│          │                                                  │
│ Dashboard│              Content Area                        │
│          │                                                  │
│ Menu by Role:                                               │
│ - Users (ADMIN only)                                       │
│ - Employees (ADMIN, HR)                                    │
│ - Dosen (ADMIN, HR)                                        │
│ - Departments (ADMIN, HR)                                  │
│ - Positions (ADMIN, HR)                                    │
│ - Attendance (all, view scope by role)                     │
│ - Leave Request (all, view scope by role)                  │
│ - Payroll (ADMIN, HR)                                      │
│ - My Profile (EMPLOYEE, DOSEN)                             │
│ - My Attendance (EMPLOYEE, DOSEN)                          │
│ - My Leave (EMPLOYEE, DOSEN)                               │
│ - My Payslip (EMPLOYEE, DOSEN)                             │
│                                                          │
└──────────┴──────────────────────────────────────────────────┘
```

### 7.3 Dashboard per Role

**ADMIN Dashboard:**
- Total Employees
- Total Dosen
- Total Departments (Prodi & Non-Prodi)
- Attendance Today Summary
- Pending Leave Requests
- Recent Activities
- Quick Actions: Add Employee, Add Dosen, Generate Payroll

**HR Dashboard:**
- Total Employees
- Total Dosen
- Attendance Today Summary
- Pending Leave Requests
- This Month Payroll Status
- Quick Actions: Add Employee, Process Leave, Generate Payroll

**EMPLOYEE Dashboard:**
- Profile Summary
- Today's Attendance (Clock In/Out button)
- Leave Balance Summary
- Recent Leave Requests
- Recent Payslips
- Quick Actions: Request Leave, View Attendance History

**DOSEN Dashboard:**
- Profile Dosen Summary
- Homebase Prodi
- Today's Attendance (Clock In/Out button)
- Leave Balance Summary
- Recent Leave Requests
- Recent Payslips
- (Future) Teaching Schedule
- Quick Actions: Request Leave, View Attendance History

### 7.4 Komponen HTMX + Alpine.js

- **Role Switcher**: Dropdown di header untuk switch role tanpa logout
- **Modal**: Form add/edit dengan Alpine.js
- **Toast Notification**: Untuk feedback success/error
- **Confirm Dialog**: Untuk delete confirmation
- **Dynamic Table**: Pagination dengan HTMX
- **Auto-refresh**: Untuk clock in/out status
- **Dependent Dropdown**: Pilih homebase prodi hanya department dengan is_prodi = true
- **Conditional Form**: Form lecturer profile muncul jika role DOSEN dipilih
- **Dynamic Sidebar**: Menu berubah berdasarkan role yang dipilih

---

## 8. Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Spring Boot Starter Thymeleaf -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <!-- Spring Boot Starter Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- MySQL Connector -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## 9. Configuration (application.yml)

```yaml
spring:
  application:
    name: hris-sederhana

  datasource:
    url: jdbc:mysql://localhost:3306/hris_db?createDatabaseIfNotExist=true
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true

  servlet:
    multipart:
      max-file-size: 2MB
      max-request-size: 2MB

server:
  port: 8080

# File Upload
app:
  upload:
    dir: ./uploads

# Session
session:
  timeout: 3600 # 1 hour
```

---

## 10. Enum Definitions

### RoleType
```java
public enum RoleType {
    ADMIN("Administrator"),
    HR("HR Staff"),
    EMPLOYEE("Employee"),
    DOSEN("Lecturer");

    private final String displayName;
}
```

### CompanyType (Tipe Institusi)
```java
public enum CompanyType {
    COMPANY("Perusahaan"),
    UNIVERSITY("Universitas"),
    SCHOOL("Sekolah"),
    OTHER("Lainnya");

    private final String displayName;
}
```

### EmploymentStatus (Status Kepegawaian - Sesuai UU Ketenagakerjaan)
```java
public enum EmploymentStatus {
    PERMANENT("Karyawan Tetap"),
    CONTRACT("Karyawan Kontrak"),
    PROBATION("Masa Percobaan"),
    DAILY("Harian Lepas");

    private final String displayName;
}
```

### MaritalStatus (Status Pernikahan - Untuk Pajak & BPJS)
```java
public enum MaritalStatus {
    SINGLE("Belum Menikah"),
    MARRIED("Menikah"),
    DIVORCED("Cerai Hidup"),
    WIDOWED("Cerai Mati");

    private final String displayName;
}
```

### LecturerRank (Jenjang Dosen)
```java
public enum LecturerRank {
    ASISTEN_AHLI("Asisten Ahli"),
    LEKTOR("Lektor"),
    LEKTOR_KEPALA("Lektor Kepala"),
    PROFESOR("Profesor");

    private final String displayName;
}
```

### LecturerEmploymentStatus (Status Kepegawaian Dosen)
```java
public enum LecturerEmploymentStatus {
    DOSEN_TETAP("Dosen Tetap"),
    DOSEN_TIDAK_TETAP("Dosen Tidak Tetap");

    private final String displayName;
}
```

### LecturerWorkStatus (Status Kerja Dosen)
```java
public enum LecturerWorkStatus {
    ACTIVE("Aktif Mengajar"),
    LEAVE("Cuti"),
    RETIRED("Pensiun");

    private final String displayName;
}
```

### ActivityType (Tipe Aktivitas User)
```java
public enum ActivityType {
    // Authentication
    LOGIN("Login"),
    LOGOUT("Logout"),
    ROLE_SELECTION("Pilih Role"),

    // CRUD Operations
    CREATE("Buat Data"),
    READ("Lihat Data"),
    UPDATE("Update Data"),
    DELETE("Hapus Data"),
    RESTORE("Pulihkan Data"),

    // Business Operations
    CLOCK_IN("Absen Masuk"),
    CLOCK_OUT("Absen Keluar"),
    SUBMIT_LEAVE("Ajukan Cuti"),
    APPROVE_LEAVE("Setujui Cuti"),
    REJECT_LEAVE("Tolak Cuti"),
    GENERATE_PAYROLL("Generate Gaji"),

    // Sensitive Data Access
    VIEW_SENSITIVE_DATA("Lihat Data Sensitif");

    private final String displayName;
}
```

---

## 17. Implementasi Activity Logging dengan Spring AOP

### 17.1 Konsep Activity Logging

Activity logging dilakukan secara **otomatis** menggunakan Spring AOP (Aspect-Oriented Programming) untuk mencatat semua aktivitas user tanpa mengubah business logic.

### 17.2 Custom Annotation @LogActivity

```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogActivity {
    ActivityType activityType();
    String moduleName() default "";
    String entityName() default "";
    String description() default "";
}
```

### 17.3 Activity Log Aspect

```java
@Aspect
@Component
@Slf4j
public class ActivityLogAspect {

    @Autowired
    private UserActivityLogService activityLogService;

    @Autowired
    private HttpServletRequest request;

    @AfterReturning(pointcut = "@annotation(logActivity)", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, LogActivity logActivity, Object result) {
        logActivity(joinPoint, logActivity, "SUCCESS", null);
    }

    @AfterThrowing(pointcut = "@annotation(logActivity)", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, LogActivity logActivity, Exception exception) {
        logActivity(joinPoint, logActivity, "FAILED", exception.getMessage());
    }

    private void logActivity(JoinPoint joinPoint, LogActivity logActivity, String status, String errorMessage) {
        try {
            // Get current user from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return;
            }

            Employee currentUser = (Employee) authentication.getPrincipal();
            Long employeeId = currentUser.getId();

            // Extract entity information from method arguments
            String entityId = extractEntityId(joinPoint.getArgs());
            String moduleName = logActivity.moduleName().isEmpty()
                ? extractModuleName(joinPoint)
                : logActivity.moduleName();

            // Build activity details JSON
            Map<String, Object> details = new HashMap<>();
            details.put("method", joinPoint.getSignature().getName());
            details.put("parameters", extractParameters(joinPoint.getArgs()));

            // Create activity log
            ActivityLogDto logDto = ActivityLogDto.builder()
                .employeeId(employeeId)
                .activityType(logActivity.activityType())
                .moduleName(moduleName)
                .entityType(logActivity.entityName())
                .entityId(entityId)
                .description(logActivity.description())
                .activityDetails(details)
                .status("SUCCESS".equals(status) ? ActivityStatus.SUCCESS : ActivityStatus.FAILED)
                .errorMessage(errorMessage)
                .ipAddress(getClientIP(request))
                .userAgent(request.getHeader("User-Agent"))
                .build();

            activityLogService.logActivity(logDto);

        } catch (Exception e) {
            log.error("Error logging activity: {}", e.getMessage());
        }
    }

    private String extractEntityId(Object[] args) {
        // Extract ID from method arguments
        for (Object arg : args) {
            if (arg instanceof Long) {
                return arg.toString();
            }
            if (arg instanceof BaseEntity) {
                return String.valueOf(((BaseEntity) arg).getId());
            }
            // Extract ID from DTOs
            try {
                Method getIdMethod = arg.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(arg);
                if (id != null) {
                    return id.toString();
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String extractModuleName(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        return className.replace("Controller", "").replace("Service", "").toLowerCase();
    }

    private Map<String, Object> extractParameters(Object[] args) {
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof HttpServletRequest
                || args[i] instanceof HttpServletResponse
                || args[i] instanceof BindingResult) {
                continue; // Skip web-related objects
            }
            params.put("param" + i, args[i]);
        }
        return params;
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

### 17.4 Contoh Penggunaan di Controller

```java
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @LogActivity(
        activityType = ActivityType.CREATE,
        moduleName = "employee",
        entityName = "Employee",
        description = "Membuat data karyawan baru"
    )
    public ResponseEntity<Employee> create(@RequestBody EmployeeDto dto) {
        Employee employee = employeeService.create(dto, getCurrentUserId());
        return ResponseEntity.ok(employee);
    }

    @PutMapping("/{id}")
    @LogActivity(
        activityType = ActivityType.UPDATE,
        moduleName = "employee",
        entityName = "Employee",
        description = "Update data karyawan"
    )
    public ResponseEntity<Employee> update(@PathVariable Long id, @RequestBody EmployeeDto dto) {
        Employee employee = employeeService.update(id, dto, getCurrentUserId());
        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{id}")
    @LogActivity(
        activityType = ActivityType.DELETE,
        moduleName = "employee",
        entityName = "Employee",
        description = "Hapus data karyawan (soft delete)"
    )
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id, getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @LogActivity(
        activityType = ActivityType.READ,
        moduleName = "employee",
        entityName = "Employee",
        description = "Lihat detail karyawan"
    )
    public ResponseEntity<Employee> getById(@PathVariable Long id) {
        Employee employee = employeeService.findById(id);
        return ResponseEntity.ok(employee);
    }
}
```

### 17.5 Service Layer

```java
@Service
@RequiredArgsConstructor
public class UserActivityLogService {

    private final UserActivityLogRepository repository;
    private final ObjectMapper objectMapper;

    @Async
    public void logActivity(ActivityLogDto dto) {
        try {
            UserActivityLog log = new UserActivityLog();
            log.setEmployeeId(dto.getEmployeeId());
            log.setActivityType(dto.getActivityType());
            log.setModuleName(dto.getModuleName());
            log.setEntityType(dto.getEntityType());
            log.setEntityId(dto.getEntityId());
            log.setDescription(dto.getDescription());
            log.setActivityDetails(objectMapper.writeValueAsString(dto.getActivityDetails()));
            log.setStatus(dto.getStatus());
            log.setErrorMessage(dto.getErrorMessage());
            log.setIpAddress(dto.getIpAddress());
            log.setUserAgent(dto.getUserAgent());

            repository.save(log);

        } catch (Exception e) {
            // Log error tapi jangan throw exception untuk tidak mengganggu flow utama
            log.error("Failed to save activity log: {}", e.getMessage());
        }
    }

    public List<UserActivityLog> getEmployeeLogs(Long employeeId, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findByEmployeeIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            employeeId, startDate, endDate
        );
    }

    public List<UserActivityLog> getModuleLogs(String moduleName, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findByModuleNameAndCreatedAtBetweenOrderByCreatedAtDesc(
            moduleName, startDate, endDate
        );
    }

    public List<UserActivityLog> getEntityLogs(String entityType, Long entityId) {
        return repository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            entityType, entityId
        );
    }

    public Page<UserActivityLog> getAllLogs(Pageable pageable) {
        return repository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
```

### 17.6 Repository

```java
@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    List<UserActivityLog> findByEmployeeIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long employeeId, LocalDateTime startDate, LocalDateTime endDate
    );

    List<UserActivityLog> findByModuleNameAndCreatedAtBetweenOrderByCreatedAtDesc(
        String moduleName, LocalDateTime startDate, LocalDateTime endDate
    );

    List<UserActivityLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
        String entityType, Long entityId
    );

    Page<UserActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Query untuk statistik
    @Query("SELECT log.activityType, COUNT(log) FROM UserActivityLog log " +
           "WHERE log.createdAt BETWEEN :start AND :end GROUP BY log.activityType")
    List<Object[]> getActivityTypeStats(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Query("SELECT log.employeeId, COUNT(log) FROM UserActivityLog log " +
           "WHERE log.createdAt BETWEEN :start AND :end GROUP BY log.employeeId")
    List<Object[]> getMostActiveUsers(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);
}
```

### 17.7 Manual Logging (untuk kasus khusus)

```java
// Untuk aktivitas yang tidak bisa pakai annotation
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserActivityLogService activityLogService;

    public LoginResponse login(LoginRequestDto request, String ipAddress, String userAgent) {
        try {
            // ... login logic
            Employee employee = authenticateUser(request);

            // Log login success
            activityLogService.logActivity(ActivityLogDto.builder()
                .employeeId(employee.getId())
                .activityType(ActivityType.LOGIN)
                .moduleName("auth")
                .description("Login berhasil")
                .status(ActivityStatus.SUCCESS)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build());

            return LoginResponse.builder()
                .employeeId(employee.getId())
                .availableRoles(getUserRoles(employee))
                .build();

        } catch (BadCredentialsException e) {
            // Log login failed
            activityLogService.logActivity(ActivityLogDto.builder()
                .employeeId(null) // Unknown user
                .activityType(ActivityType.LOGIN)
                .moduleName("auth")
                .description("Login gagal: " + request.getEmail())
                .status(ActivityStatus.FAILED)
                .errorMessage(e.getMessage())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build());

            throw e;
        }
    }
}
```

### 17.8 Dashboard Activity Log View

```java
@Controller
@RequiredArgsConstructor
public class ActivityLogController {

    private final UserActivityLogService activityLogService;

    @GetMapping("/activity-logs")
    public String activityLogsPage(
        @RequestParam(required = false) Long employeeId,
        @RequestParam(required = false) String moduleName,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(defaultValue = "0") int page,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());

        Page<UserActivityLog> logs;
        if (employeeId != null) {
            logs = activityLogService.getEmployeeLogsPaged(employeeId,
                startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(30),
                endDate != null ? endDate.atTime(23, 59) : LocalDateTime.now(),
                pageable);
        } else if (moduleName != null) {
            logs = activityLogService.getModuleLogsPaged(moduleName,
                startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(30),
                endDate != null ? endDate.atTime(23, 59) : LocalDateTime.now(),
                pageable);
        } else {
            logs = activityLogService.getAllLogs(pageable);
        }

        model.addAttribute("logs", logs);
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("moduleName", moduleName);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "activity-logs/list";
    }
}
```

### 17.9 Contoh Tampilan Activity Log

```
+------------+---------------+------------------+---------------+---------------------+----------+
| Waktu      | User          | Aktivitas        | Modul         | Detail              | Status   |
+------------+---------------+------------------+---------------+---------------------+----------+
| 16/01 13:30| Budi Santoso  | CREATE           | employee      | Buat karyawan baru  | SUCCESS  |
| 16/01 13:25| Ahmad         | LOGIN            | auth          | Login berhasil      | SUCCESS  |
| 16/01 13:20| Rina          | UPDATE           | employee      | Update data karyawan| SUCCESS  |
| 16/01 13:15| Rina          | DELETE           | employee      | Hapus karyawan #123 | SUCCESS  |
| 16/01 13:10| Unknown       | LOGIN            | auth          | Login gagal         | FAILED   |
| 16/01 12:45| Dosen Ahmad   | APPROVE_LEAVE    | leave         | Setujui cuti Sari   | SUCCESS  |
| 16/01 12:30| Sari          | SUBMIT_LEAVE     | leave         | Ajukan cuti tahunan | SUCCESS  |
| 16/01 12:00| Admin         | VIEW_SENSITIVE   | payroll       | Lihat slip gaji     | SUCCESS  |
|            |               | _DATA            |               |                     |          |
+------------+---------------+------------------+---------------+---------------------+----------+

Filter: [Employee ▼] [Modul ▼] [Tipe Aktivitas ▼] [Dari: ____] [Sampai: ____] [Filter]
```

### 17.10 Retensi & Cleanup Activity Log

```java
// Cleanup activity log yang sudah > 1 tahun (opsional)
@Component
public class ActivityLogCleanupJob {

    @Autowired
    private UserActivityLogRepository repository;

    // Jalankan setiap bulan
    @Scheduled(cron = "0 0 0 1 * ?")
    public void cleanupOldLogs() {
        LocalDateTime retentionDate = LocalDateTime.now().minusYears(1);

        // Soft delete atau permanent delete untuk log yang sudah > 1 tahun
        // Sesuaikan dengan kebutuhan audit trail perusahaan
        repository.deleteByCreatedAtBefore(retentionDate);

        log.info("Activity logs older than {} deleted", retentionDate);
    }
}
```

### 17.11 Catatan Penting Activity Logging

1. **Async Logging**: Gunakan `@Async` untuk logging tidak mengganggu performa aplikasi utama
2. **Error Handling**: Jangan throw exception dari activity logging untuk tidak mengganggu flow utama
3. **PII Protection**: Jangan log password, token, atau data sensitif lainnya
4. **IP Address**: Handle proxy/load balancer untuk mendapatkan IP asli (X-Forwarded-For)
5. **Performance**: Gunakan index yang tepat untuk query activity log
6. **Storage**: Pertimbangkan archive atau partisi untuk data activity log yang besar
7. **Privacy**: Pastikan activity logging mematuhi regulasi privacy (GDPR, dll)

---

## 11. Alur Kerja Role Management

### 11.1 Login & Role Selection Flow

**Step 1: Login**
```
POST /auth/login
Request: { email, password }
Response: { success, employeeId, availableRoles: [ADMIN, EMPLOYEE, DOSEN] }
```

**Step 2: Role Selection (jika user punya lebih dari 1 role)**
```
POST /auth/select-role
Request: { employeeId, selectedRole }
Response: { success, sessionToken, redirectUrl }
```

**Step 3: Access Dashboard dengan Selected Role**
- Session disimpan dengan active role
- Dashboard di-render berdasarkan active role
- Menu dan akses fitur ditentukan oleh active role

### 11.2 Create Employee dengan Role Assignment

**Flow:**
1. Isi form employee (nama, NIK, email, password, dll)
2. Pilih **Role**: ADMIN, HR, EMPLOYEE, DOSEN (bisa multiple)
3. Jika role DOSEN dipilih:
   - Form lecturer profile muncul (conditional dengan Alpine.js)
   - Isi NIDN, pendidikan terakhir, bidang keahlian
   - Pilih jenjang dosen
   - Pilih homebase prodi (hanya department dengan is_prodi=true)
4. Simpan data

**Contoh Assignment:**
- Staff Biro Akademik: Role = EMPLOYEE
- Dosen Biasa: Role = DOSEN
- Dosen yang juga HR: Role = HR, DOSEN
- Admin (bukan dosen): Role = ADMIN
- Admin (sekaligus dosen): Role = ADMIN, DOSEN

### 11.3 Query Logic

**Get User Available Roles:**
```java
SELECT role FROM employee_roles
WHERE employee_id = ?
```

**Get All Employees (for ADMIN/HR):**
```java
SELECT e.*, GROUP_CONCAT(er.role) as roles
FROM employees e
LEFT JOIN employee_roles er ON e.id = er.employee_id
WHERE e.status = 'ACTIVE'
GROUP BY e.id
```

**Get All Dosen:**
```java
SELECT e.*, lp.* FROM employees e
JOIN employee_roles er ON e.id = er.employee_id
JOIN lecturer_profiles lp ON e.id = lp.employee_id
WHERE er.role = 'DOSEN'
```

### 11.4 Display Logic

**List User dengan Roles:**
```
+----+------------+----------------+-------------------------+
| ID | NIK        | Nama           | Roles                  |
+----+------------+----------------+-------------------------+
| 1  | 12345      | Budi Santoso   | EMPLOYEE                |
| 2  | 12346      | Dr. Ahmad     | DOSEN                   |
| 3  | 12347      | Prof. Sari    | ADMIN, EMPLOYEE, DOSEN  |
| 4  | 12348      | Rina HR       | HR, EMPLOYEE            |
+----+------------+----------------+-------------------------+
```

**List Dosen:**
```
+----+------------+----------------+-----------+----------------+
| ID | NIK        | Nama           | NIDN      | Homebase       |
+----+------------+----------------+-----------+----------------+
| 2  | 12346      | Dr. Ahmad     | 00123456  | Informatika    |
| 3  | 12347      | Prof. Sari    | 00123457  | Sipil          |
+----+------------+----------------+-----------+----------------+
```

---

## 12. Security & Access Control

### 12.1 Role-Based Access Control (RBAC)

Setiap endpoint memiliki role-based access:

| Endpoint | ADMIN | HR | EMPLOYEE | DOSEN |
|----------|-------|-------|----------|-------|
| /login | ✅ | ✅ | ✅ | ✅ |
| /dashboard/admin | ✅ | ❌ | ❌ | ❌ |
| /dashboard/hr | ✅ | ✅ | ❌ | ❌ |
| /dashboard/employee | ✅ | ✅ | ✅ (own) | ✅ (own) |
| /dashboard/dosen | ✅ | ✅ | ❌ | ✅ (own) |
| /employees/** (CRUD) | ✅ | ✅ | ❌ | ❌ |
| /employees/me | ✅ | ✅ | ✅ | ✅ |
| /lecturers/** (CRUD) | ✅ | ✅ | ❌ | ❌ |
| /attendance (all) | ✅ | ✅ | ❌ | ❌ |
| /attendance (mine) | ✅ | ✅ | ✅ | ✅ |
| /leave/** (all) | ✅ | ✅ | ❌ | ❌ |
| /leave/** (mine) | ✅ | ✅ | ✅ | ✅ |
| /payroll/** (all) | ✅ | ✅ | ❌ | ❌ |
| /payroll/** (mine) | ❌ | ❌ | ✅ | ✅ |
| /users/** | ✅ | ❌ | ❌ | ❌ |

### 12.2 Session Management

- Session disimpan di tabel `user_sessions`
- Setiap session memiliki: employee_id, selected_role, session_token, expires_at
- Role switch mengupdate session tanpa logout
- Auto logout setelah session expires

### 12.3 Password Hashing

- Password di-hash menggunakan BCrypt
- Tidak boleh menyimpan password dalam plain text

---

## 13. Fitur Tambahan yang Bisa Ditambahkan (Future)

1. **Notifikasi Email**
   - Email approval leave request
   - Email slip gaji karyawan
   - Email slip gaji dosen

2. **Laporan**
   - Export to Excel/PDF
   - Laporan absensi bulanan
   - Laporan cuti
   - Laporan rekap dosen per prodi

3. **Overtime Calculator**
   - Input lembur
   - Perhitungan otomatis

4. **Tax Calculator**
   - PPh 21 karyawan
   - PPh 21 dosen

5. **Manajemen Jadwal Mengajar Dosen**
   - Input jadwal kuliah
   - Rekap jam mengajar per periode

6. **Audit Log**
   - Log semua aktivitas user
   - Track siapa mengubah apa dan kapan

7. **Organization Chart**
   - Visualisasi struktur organisasi
   - Hubungan antar department

---

## 14. Prioritas Implementasi

### Phase 1: Foundation & Auth
1. Setup SpringBoot project dengan Maven
2. Setup MySQL database connection
3. Setup Spring Security & Authentication
4. Implement Login & Role Selection
5. Create layout template dengan dynamic sidebar

### Phase 2: Master Data
6. Implement Department CRUD (dengan is_prodi & kode_prodi)
7. Implement Position CRUD
8. Implement Employee CRUD dengan role management
9. Implement User management (ADMIN only)

### Phase 3: Data Dosen
10. Implement LecturerProfile CRUD
11. Implement relasi Employee - LecturerProfile
12. Implement homebase prodi linking (hanya dept dengan is_prodi=true)

### Phase 4: Attendance
13. Implement Clock In/Out
14. Attendance history & calendar view
15. Attendance report (ADMIN/HR)

### Phase 5: Leave Management
16. Implement Leave Request
17. Implement Leave Approval (ADMIN/HR)
18. Leave Balance tracking

### Phase 6: Payroll Karyawan
19. Implement Payroll calculation karyawan
20. Generate payslip karyawan
21. Payroll report karyawan

### Phase 7: Payroll Dosen
22. Implement Lecturer Payroll calculation
23. Generate payslip dosen
24. Lecturer Payroll report

---

## 15. Catatan Penting

1. **Arsitektur Monolitik**: Aplikasi ini adalah monolitik, bukan microservices. Semua modul dalam satu aplikasi SpringBoot, satu database, satu deployment unit
2. **HTMX Pattern**: Gunakan HTMX untuk interaksi dinamis tanpa page reload
3. **Alpine.js**: Gunakan untuk state management di sisi client (modal, form validation, dependent dropdown, conditional form)
4. **Tailwind CSS**: Untuk styling yang cepat dan konsisten
5. **Thymeleaf Fragments**: Reusable components untuk header, sidebar, modal
6. **Server-side Rendering**: Semua rendering di server, HTMX menangani partial update
7. **Prodi Validation**: Saat create/update lecturer profile, homebase_prodi_id harus mengacu ke department dengan is_prodi = true
8. **Role Validation**: Employee dengan role DOSEN harus memiliki lecturer_profile yang terisi
9. **Soft Delete (WAJIB)**: Tidak ada hard delete di aplikasi ini. Semua operasi hapus menggunakan soft delete dengan mengisi field `deleted_at`. Semua query WAJIB menambahkan filter `WHERE deleted_at IS NULL`
10. **Audit Trail**: Setiap operasi create/update/delete wajib mencatat user yang melakukan operasi tersebut melalui field `created_by`, `updated_by`, `deleted_by`
11. **Session Security**: Gunakan HTTPS di production, implement CSRF protection
12. **Role Switching**: Switch role tanpa login ulang, hanya update session
13. **Access Control**: Validasi role di setiap endpoint untuk security
14. **Password Policy**: Implement password policy (min length, complexity, dll)
15. **Data Retention**: Data yang sudah dihapus (soft delete) tetap disimpan di database untuk audit trail dan memenuhi retensi 10 tahun sesuai UU No. 8 Tahun 1997

---

## 16. Implementasi Soft Delete di SpringBoot

### 16.1 Base Entity Pattern

Semua entity harus extend dari `BaseEntity` yang berisi field soft delete dan audit:

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    // Helper methods
    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete(Long deletedByUserId) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedByUserId;
    }

    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }
}
```

### 16.2 Contoh Entity dengan Soft Delete

```java
@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_employees_deleted_at", columnList = "deleted_at")
})
@SQLDelete(sql = "UPDATE employees SET deleted_at = NOW(), deleted_by = :deletedBy WHERE id = :id")
@Where(clause = "deleted_at IS NULL")
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nik;

    private String fullName;

    // ... other fields

    // Relationship - otomatis filter soft delete
    @OneToMany(mappedBy = "employee")
    private List<EmployeeRole> roles = new ArrayList<>();
}
```

### 16.3 Repository Pattern

Semua repository harus extend `SoftDeleteRepository`:

```java
@NoRepositoryBean
public interface SoftDeleteRepository<T, ID> extends JpaRepository<T, ID> {

    // Override delete untuk soft delete
    @Override
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deletedAt = CURRENT_TIMESTAMP, e.deletedBy = :deletedBy WHERE e.id = :id")
    void deleteById(@Param("id") ID id, @Param("deletedBy") Long deletedBy);

    // Method untuk mendapatkan data termasuk yang dihapus
    @Query("SELECT e FROM #{#entityName} e WHERE e.deletedAt IS NOT NULL")
    List<T> findDeleted();

    // Method untuk restore data
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deletedAt = NULL, e.deletedBy = NULL WHERE e.id = :id")
    void restoreById(@Param("id") ID id);

    // Method untuk permanent delete (HANYA untuk cleanup setelah retensi period)
    @Modifying
    @Query("DELETE FROM #{#entityName} e WHERE e.id = :id AND e.deletedAt < :retentionDate")
    void permanentDeleteIfRetentionExpired(@Param("id") ID id, @Param("retentionDate") LocalDateTime retentionDate);
}
```

### 16.4 Contoh Repository Implementation

```java
@Repository
public interface EmployeeRepository extends SoftDeleteRepository<Employee, Long> {

    // Semua query otomatis filter WHERE deleted_at IS NULL
    // berkat @Where clause di entity

    Optional<Employee> findByEmail(String email);

    List<Employee> findByDepartmentId(Long departmentId);

    // Query explicit jika butuh include deleted data
    @Query("SELECT e FROM Employee e WHERE e.email = :email")
    Optional<Employee> findByEmailIncludeDeleted(@Param("email") String email);

    // Query untuk restore
    @Modifying
    @Query("UPDATE Employee e SET e.deletedAt = NULL, e.deletedBy = NULL WHERE e.id = :id")
    void restoreById(@Param("id") Long id);
}
```

### 16.5 Service Layer Pattern

```java
@Service
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public Employee create(EmployeeDto dto, Long currentUserId) {
        Employee employee = new Employee();
        // ... set fields from dto
        employee.setCreatedBy(currentUserId);
        return employeeRepository.save(employee);
    }

    public Employee update(Long id, EmployeeDto dto, Long currentUserId) {
        Employee employee = findById(id); // otomatis filter deleted_at IS NULL
        // ... update fields
        employee.setUpdatedBy(currentUserId);
        return employeeRepository.save(employee);
    }

    public void delete(Long id, Long currentUserId) {
        Employee employee = findById(id);
        employee.softDelete(currentUserId); // Set deleted_at & deleted_by
        employeeRepository.save(employee);
    }

    public void restore(Long id) {
        employeeRepository.restoreById(id);
    }

    public List<Employee> findDeleted() {
        return employeeRepository.findDeleted();
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Employee not found"));
    }
}
```

### 16.6 Hibernate Filter Alternatif

Jika tidak menggunakan Hibernate @Where, bisa menggunakan @Filter:

```java
@Entity
@Table(name = "employees")
@FilterDef(name = "softDeleteFilter", parameters = {
    @ParamDef(name = "isDeleted", type = "boolean")
})
@Filter(name = "softDeleteFilter", condition = "deleted_at IS NULL")
public class Employee extends BaseEntity {

    // ... fields
}
```

### 16.7 Index untuk Soft Delete

Semua tabel WAJIB memiliki index pada `deleted_at` untuk performa query:

```sql
CREATE INDEX idx_employees_deleted_at ON employees(deleted_at);
CREATE INDEX idx_employee_roles_deleted_at ON employee_roles(deleted_at);
CREATE INDEX idx_departments_deleted_at ON departments(deleted_at);
-- ... dan seterusnya untuk semua tabel
```

### 16.8 Cleanup Job (Scheduled Task)

Untuk permanent delete data yang sudah melewati masa retensi (10 tahun):

```java
@Component
public class DataRetentionCleanupJob {

    @Autowired
    private EmployeeRepository employeeRepository;

    // Jalankan setiap hari jam 2 pagi
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredData() {
        LocalDateTime retentionDate = LocalDateTime.now().minusYears(10);

        // Permanent delete employees yang dihapus > 10 tahun lalu
        employeeRepository.permanentDeleteIfRetentionExpired(null, retentionDate);

        // Lakukan hal yang sama untuk semua repository...
    }
}
```

### 16.9 Testing Soft Delete

```java
@Test
public void testSoftDelete() {
    // Create
    Employee employee = employeeService.create(dto, 1L);
    Long employeeId = employee.getId();

    // Delete
    employeeService.delete(employeeId, 1L);

    // Verify not found in regular query
    assertThrows(NotFoundException.class, () -> employeeService.findById(employeeId));

    // Verify still exists in DB (soft delete)
    Employee deleted = employeeRepository.findByEmailIncludeDeleted(employee.getEmail());
    assertNotNull(deleted);
    assertNotNull(deleted.getDeletedAt());

    // Restore
    employeeService.restore(employeeId);
    Employee restored = employeeService.findById(employeeId);
    assertNotNull(restored);
    assertNull(restored.getDeletedAt());
}
```
