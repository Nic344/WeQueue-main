# WeQueue — Food Queue Management App

Aplikasi Android (Java) untuk manajemen antrian pemesanan makanan, terintegrasi
dengan REST API (PHP native) dan database MySQL. Mendukung tiga peran pengguna:
**Customer**, **Staff**, dan **Admin**.

> Proyek UAS Pemrograman Mobile. Dibangun dengan Android Studio + Java, koneksi API
> menggunakan Retrofit, dan backend PHP native + MySQL.

---

## ✨ Fitur Utama

### Umum
- **Login & Registrasi** via API (token-based auth).
- **Logout** (menghapus token di server + sesi lokal).
- **Splash Screen** dengan animasi + auto-routing sesuai status login & peran.
- **BottomNavigationView** untuk navigasi antar halaman.
- **Sesi login terenkripsi** (`EncryptedSharedPreferences`).
- **Penanganan token kedaluwarsa (HTTP 401) global** — otomatis kembali ke login.
- **Dark Mode** (tersimpan, berlaku di seluruh aplikasi).
- **Notifikasi antrian** (NotificationChannel + preferensi yang dapat diatur).

### Customer
- Lihat status antrian & ambil nomor antrian.
- Lihat antrian aktif, posisi, dan estimasi waktu (auto-refresh).
- Riwayat antrian, daftar makanan populer, favorit.
- **Pencarian makanan** (live search via API).
- Profil: edit profil, **ganti password**, **upload foto profil**, dark mode, notifikasi.

### Staff
- Dashboard operasional (now serving, jumlah menunggu, dsb.).
- Kelola antrian: call next, complete, skip, cancel.
- Monitor & cari antrian.

### Admin
- **CRUD Makanan** (Create, Read, Update, Delete) lengkap.
- **Upload gambar makanan** (multipart) + preview, hapus gambar ikut terhapus dari server.
- Toggle ketersediaan makanan (Available / Out of Stock).
- Kelola pengguna: ubah peran, hapus pengguna, pencarian.

---

## 🧰 Teknologi & Library

| Komponen        | Teknologi                                              |
|-----------------|--------------------------------------------------------|
| IDE             | Android Studio                                         |
| Bahasa          | Java                                                   |
| API Server      | REST API — PHP native                                  |
| Database        | MySQL                                                  |
| Networking      | Retrofit 2, OkHttp (+ logging interceptor)             |
| JSON Parsing    | GSON                                                   |
| UI              | Material Components, RecyclerView, CardView, ConstraintLayout |
| Gambar          | Glide                                                  |
| Keamanan sesi   | androidx.security `EncryptedSharedPreferences`         |

---

## 🏗️ Struktur Proyek

```
WeQueue-main/
├─ app/                      # Aplikasi Android (Java)
│  └─ src/main/java/com/example/queueapp/
│     ├─ api/                # Retrofit: ApiService, ApiConfig, interceptors, model/
│     ├─ data/               # SessionManager (terenkripsi), AppSession
│     ├─ fragment/           # Layar customer (Home, Queue, History, Favorites, Profile)
│     ├─ staff/              # Activity + fragment + adapter staff
│     ├─ admin/              # Activity + fragment + adapter admin
│     ├─ util/               # Helper (notifikasi, upload gambar, tema, dll.)
│     ├─ auth/               # Role navigation & proteksi
│     ├─ LoginActivity / RegisterActivity / SplashActivity / MainActivity ...
│     └─ ...
└─ wequeue-api/              # Backend REST API (PHP native)
   ├─ auth/                  # login, register, logout
   ├─ queue/                 # status, take, my, cancel, history
   ├─ foods/                 # list, popular, search, create, update, delete
   ├─ favorites/             # list, add, remove
   ├─ user/                  # profile, update, change-password
   ├─ upload/                # image.php (multipart upload)
   ├─ staff/ , admin/        # endpoint staff & admin
   ├─ helpers/ , config/     # auth, response, db, uploads
   ├─ migrations/            # skrip migrasi tambahan
   └─ database.sql           # skema + data contoh
```

---

## 🚀 Cara Menjalankan

### 1. Backend (Laragon / XAMPP)
1. Salin folder `wequeue-api/` ke document root web server sehingga dapat diakses di
   `http://localhost/webabiq/` (sesuaikan dengan `BASE_URL`).
2. Buat database & import skema:
   ```sql
   -- via HeidiSQL / phpMyAdmin
   SOURCE database.sql;   -- atau import file wequeue-api/database.sql
   ```
3. Jalankan migrasi (jika DB dibuat sebelum kolom `is_available` ada):
   ```sql
   ALTER TABLE foods ADD COLUMN is_available TINYINT(1) NOT NULL DEFAULT 1 AFTER category;
   ```
4. Sesuaikan kredensial DB di `wequeue-api/config/database.php` bila perlu.

### 2. Aplikasi Android
1. Buka folder proyek di Android Studio, biarkan Gradle sync.
2. Atur `BASE_URL` di `app/.../api/ApiConfig.java`:
   - Emulator → `http://10.0.2.2/webabiq/`
   - HP fisik → `http://<IP-LAN-PC>/webabiq/`
3. Run di emulator/perangkat (`Run > app`), atau build APK:
   ```bash
   gradlew assembleDebug
   ```

---

## 🔌 Ringkasan Endpoint API

| Method | Endpoint                          | Fungsi                       |
|--------|-----------------------------------|------------------------------|
| POST   | `/auth/register.php`              | Registrasi                   |
| POST   | `/auth/login.php`                 | Login                        |
| POST   | `/auth/logout.php`                | Logout                       |
| GET    | `/queue/status.php` / `my.php`    | Status & antrian saya        |
| POST   | `/queue/take.php`                 | Ambil antrian                |
| PUT    | `/queue/cancel.php`               | Batalkan antrian             |
| GET    | `/foods/list.php` / `search.php`  | Daftar & pencarian makanan   |
| POST   | `/foods/create.php`               | Tambah makanan               |
| PUT    | `/foods/update.php`               | Ubah makanan                 |
| DELETE | `/foods/delete.php`               | Hapus makanan (+ gambar)     |
| GET    | `/user/profile.php`               | Profil                       |
| PUT    | `/user/update.php`                | Ubah profil                  |
| PUT    | `/user/change-password.php`       | Ganti password               |
| POST   | `/upload/image.php`               | Upload gambar (multipart)    |
| GET/PUT/DELETE | `/staff/*`, `/admin/*`    | Operasi staff & admin        |

Semua endpoint terproteksi memerlukan header `Authorization: Bearer <token>`.

---

## 👤 Akun Demo (dari `database.sql`)

| Peran    | Email                  | Password    |
|----------|------------------------|-------------|
| Customer | customer@wequeue.com   | customer123 |
| Staff    | staff@foodqueue.com    | staff123    |
| Admin    | admin@wequeue.com      | admin123    |

---

## 🔒 Catatan Keamanan
- Token disimpan terenkripsi di perangkat (`EncryptedSharedPreferences`).
- Password di-hash di server dengan `password_hash()` (bcrypt).
- Request bermuatan file (upload) divalidasi tipe & ukuran di server.
- Ganti password mencabut sesi lain demi keamanan.

---

## 🧱 Arsitektur (MVVM)

Aplikasi menerapkan pola **MVVM (Model–View–ViewModel)** dengan lapisan
Repository sebagai sumber data tunggal:

```
View (Activity/Fragment)
   └─ mengamati LiveData ─────────────┐
ViewModel (AndroidX ViewModel)        │  state: Resource<T> (LOADING/SUCCESS/ERROR)
   └─ memanggil ───────────┐          │
Repository                 │          │
   └─ Retrofit ApiService ─┴─ JSON ───┘
       └─ REST API (PHP) ── MySQL
```

- **Model**: `api/model/*` (data) + endpoint REST + MySQL.
- **View**: `Activity`/`Fragment` — hanya merender state & meneruskan aksi user.
  Tidak memanggil API langsung.
- **ViewModel**: `viewmodel/*` (mis. `AdminFoodsViewModel`, `AddEditFoodViewModel`,
  `QueueViewModel`) — menyimpan state lewat `LiveData`, bertahan saat rotasi layar.
- **Repository**: `data/repository/*` (`FoodRepository`, `QueueRepository`) —
  membungkus Retrofit dan mengembalikan state lewat `Resource<T>`.
- **Resource<T>**: `data/Resource.java` — pembungkus status LOADING/SUCCESS/ERROR.

**Lifecycle-awareness**: Fragment mengamati LiveData dengan `getViewLifecycleOwner()`,
sehingga observer otomatis berhenti saat view dihancurkan (mencegah memory leak),
dan ViewModel mempertahankan data ketika konfigurasi berubah (mis. rotasi).

**Komponen lain**:
- **Background/async**: panggilan jaringan berjalan asinkron (Retrofit `enqueue`)
  sehingga tidak memblokir main thread.
- **Intent eksplisit**: navigasi antar layar (Login → Main, Foods → AddEditFood, dll.)
  dan PendingIntent pada notifikasi.

> Fitur inti (CRUD Foods & alur Queue) sudah dimigrasikan ke MVVM sebagai acuan;
> layar lain mengikuti pola yang sama.
