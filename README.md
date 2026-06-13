# WeQueue — Aplikasi Manajemen Antrian Pemesanan Makanan

Aplikasi Android (Java) untuk manajemen antrian pemesanan makanan, terintegrasi
dengan REST API (PHP native) dan database MySQL. Mendukung tiga peran pengguna:
**Customer**, **Staff**, dan **Admin**, dengan data tersinkronisasi ke server.

> Proyek UAS Mata Kuliah **Pemrograman Mobile** (Ganjil 2025/2026).
> Stack: Android Studio + Java, Retrofit2, GSON, RecyclerView, MySQL, arsitektur MVVM.

---

## 👥 Anggota Kelompok

| Nama | NIM | Peran |
|------|-----|-------|
| Devin Ng | 2432012 | Project Lead, REST API (PHP) & MySQL |
| Lam Jeen Sin Anthony | 2432070 | UI/UX, Layout, RecyclerView |
| Nicholas Syahputra | 2432050 | Autentikasi, Session, arsitektur MVVM |
| Xlhynz | 2432059 | Fitur Admin & Staff (CRUD, kelola pengguna) |
| Muhammad Anugrah Wahyu Saputra | 2432003 | Sistem antrian & notifikasi |

---

## ✨ Fitur

### Fitur Wajib (sesuai soal — terpenuhi semua)
- **Login & Registrasi via API** (token-based authentication).
- **CRUD data dari MySQL via API** (modul Foods: Create, Read, Update, Delete).
- **Menampilkan data dalam RecyclerView** (makanan, antrian, pengguna, riwayat).
- **Retrofit** untuk seluruh koneksi API.
- **Splash Screen + BottomNavigationView**.
- **Fitur pencarian data** (pencarian makanan & filter antrian/pengguna).
- **Fitur logout** (menghapus token di server + sesi lokal).

### Fitur Tambahan
- **Customer:** status antrian (now serving, sisa antrian, estimasi waktu), ambil & batal
  antrian (auto-refresh), riwayat antrian, makanan populer, favorit.
- **Staff:** dashboard operasional, call next, complete, skip, cancel, monitor & cari antrian.
- **Admin:** CRUD makanan + upload gambar (multipart), toggle ketersediaan
  (Available/Out of Stock), kelola pengguna (ubah role, hapus user).
- **Profil:** edit profil, ganti password, upload foto profil, dark mode, pengaturan notifikasi.
- **Keamanan:** sesi login terenkripsi & auto-logout saat token kedaluwarsa (HTTP 401).
- **Notifikasi antrian** (NotificationChannel) saat giliran dipanggil.

---

## 🧰 Teknologi & Library

| Komponen | Teknologi |
|----------|-----------|
| IDE | Android Studio |
| Bahasa | Java |
| API Server | REST API — PHP native |
| Database | MySQL |
| Networking | Retrofit2 + OkHttp (logging & interceptor) |
| Parsing JSON | GSON |
| UI | Material Components, RecyclerView, CardView, ConstraintLayout |
| Gambar | Glide |
| Keamanan sesi | androidx.security `EncryptedSharedPreferences` |
| Arsitektur | MVVM (Lifecycle ViewModel + LiveData) |
| Version Control | GitHub |

---

## 🏗️ Arsitektur (MVVM)

```
View (Activity/Fragment)
   └─ mengamati LiveData ───────────────┐
ViewModel (AndroidX ViewModel)          │  state: Resource<T> (LOADING/SUCCESS/ERROR)
   └─ memanggil ─────────────┐          │
Repository                   │          │
   └─ Retrofit ApiService ───┴── JSON ──┘
       └─ REST API (PHP) ── MySQL
```

- **Model:** `api/model/*` + endpoint REST + tabel MySQL.
- **View:** Activity/Fragment — hanya merender state & meneruskan aksi user (tidak memanggil API langsung).
- **ViewModel:** `viewmodel/*` — menyimpan state via `LiveData`, bertahan saat rotasi layar.
- **Repository:** `data/repository/*` — satu-satunya yang memanggil Retrofit; mengembalikan `Resource<T>`.
- **Lifecycle-aware:** Fragment mengamati LiveData dengan `getViewLifecycleOwner()` → mencegah memory leak.

---

## 📁 Struktur Proyek

```
WeQueue-main/
├─ app/                          # Aplikasi Android (Java)
│  └─ src/main/java/com/example/queueapp/
│     ├─ api/                    # Retrofit: ApiService, ApiConfig, interceptors, model/
│     ├─ data/                   # Resource, SessionManager (terenkripsi), AppSession
│     │  └─ repository/          # FoodRepository, QueueRepository, AuthRepository, ...
│     ├─ viewmodel/              # ViewModel per layar (MVVM)
│     ├─ fragment/               # Layar customer (Home, Queue, History, Favorites, Profile)
│     ├─ staff/  admin/          # Activity, fragment, adapter untuk staff & admin
│     ├─ adapter/                # RecyclerView adapters
│     ├─ auth/  util/            # Role navigation/protection & helper
│     └─ *Activity.java          # Login, Register, Splash, Main, Search, dll.
└─ wequeue-api/                  # Backend REST API (PHP native)
   ├─ auth/                      # login, register, logout
   ├─ queue/                     # status, take, my, cancel, history
   ├─ foods/                     # list, popular, search, create, update, delete
   ├─ favorites/                 # list, add, remove
   ├─ user/                      # profile, update, change-password
   ├─ upload/                    # image.php (multipart upload)
   ├─ staff/  admin/             # endpoint staff & admin
   ├─ helpers/  config/          # auth, response, db, uploads
   ├─ migrations/                # skrip migrasi tambahan
   └─ database.sql               # skema + data contoh
```

---

## 🚀 Cara Menjalankan

### 1. Backend (Laragon / XAMPP)
1. Salin folder `wequeue-api/` ke document root web server sehingga dapat diakses di
   `http://localhost/webabiq/` (sesuaikan dengan `BASE_URL`).
2. Import skema database (file `wequeue-api/database.sql`) via HeidiSQL/phpMyAdmin.
3. Jika DB lama (sebelum kolom availability ada), jalankan migrasi:
   ```sql
   ALTER TABLE foods ADD COLUMN is_available TINYINT(1) NOT NULL DEFAULT 1 AFTER category;
   ```
4. Sesuaikan kredensial DB di `wequeue-api/config/database.php` bila perlu.

### 2. Aplikasi Android
1. Buka folder proyek di Android Studio, tunggu Gradle sync.
2. Atur `BASE_URL` di `app/.../api/ApiConfig.java`:
   - Emulator → `http://10.0.2.2/webabiq/`
   - HP fisik → `http://<IP-LAN-PC>/webabiq/`
3. Jalankan di emulator/perangkat, atau build APK:
   ```bash
   gradlew assembleDebug
   ```

---

## 🔌 Ringkasan Endpoint API

| Method | Endpoint | Fungsi |
|--------|----------|--------|
| POST | `/auth/register.php` | Registrasi |
| POST | `/auth/login.php` | Login (mengembalikan token) |
| POST | `/auth/logout.php` | Logout |
| GET | `/queue/status.php` / `my.php` | Status & antrian saya |
| POST | `/queue/take.php` | Ambil antrian |
| PUT | `/queue/cancel.php` | Batalkan antrian |
| GET | `/queue/history.php` | Riwayat antrian |
| GET | `/foods/list.php` / `search.php` | Daftar & pencarian makanan |
| POST | `/foods/create.php` | Tambah makanan |
| PUT | `/foods/update.php` | Ubah makanan |
| DELETE | `/foods/delete.php` | Hapus makanan (+ gambar) |
| GET | `/user/profile.php` | Profil |
| PUT | `/user/update.php` | Ubah profil |
| PUT | `/user/change-password.php` | Ganti password |
| POST | `/upload/image.php` | Upload gambar (multipart) |
| GET/PUT/DELETE | `/admin/*`, `/staff/*` | Operasi admin & staff |

Semua endpoint terproteksi memerlukan header `Authorization: Bearer <token>`.

---

## 👤 Akun Demo (dari `database.sql`)

| Peran | Email | Password |
|-------|-------|----------|
| Customer | customer@wequeue.com | customer123 |
| Staff | staff@foodqueue.com | staff123 |
| Admin | admin@wequeue.com | admin123 |

---

## 🔒 Catatan Keamanan
- Token & data user disimpan terenkripsi (`EncryptedSharedPreferences`).
- Password di-hash di server dengan `password_hash()` (bcrypt).
- Auto-logout otomatis saat token ditolak server (HTTP 401).
- Upload file divalidasi tipe & ukuran di server; ganti password mencabut sesi lain.

---

## 🎯 Pemetaan ke CPMK (Rubrik Penilaian)
- **CPMK-1 (Arsitektur):** pola MVVM (View → ViewModel → Repository → API), lifecycle-aware, Intent eksplisit.
- **CPMK-2 (UI/UX):** ConstraintLayout, RecyclerView + ViewHolder, BottomNavigation, Material Design, dark mode.
- **CPMK-3 (Data & API):** Retrofit GET/POST/PUT/DELETE, CRUD MySQL, GSON, session terenkripsi, error handling 401/timeout.
- **CPMK-4 (Testing & Performa):** operasi jaringan asinkron (tidak blok main thread), penanganan error + retry, minim force close.
- **CPMK-5 (Dokumentasi):** source code di GitHub, README ini, APK, dan video presentasi.
