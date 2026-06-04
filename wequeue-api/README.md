# WeQueue API (PHP + MySQL)

Semua file backend API ada di **folder ini saja**. Upload folder `wequeue-api` ke hosting (document root atau subfolder).

## Struktur folder

```
wequeue-api/
├── config/
│   └── database.php      ← koneksi MySQL
├── helpers/
│   ├── auth.php          ← validasi token
│   ├── response.php      ← JSON + CORS
│   └── queue.php         ← logika antrian
├── database.sql          ← schema + data contoh
├── auth/                 ← register, login, logout
├── queue/                ← status, take, my, cancel, history
├── foods/                ← popular, search, CRUD
├── favorites/            ← list, add, remove
└── user/                 ← profile, update
```

## Setup

1. Import database:

```bash
mysql -u root -p < database.sql
```

2. Edit `config/database.php` (host, user, password).

3. Jalankan server (contoh):

```bash
cd wequeue-api
php -S localhost:8080
```

Base URL: `http://localhost:8080/auth/login.php`

## Akun contoh

| Email | Password |
|-------|----------|
| admin@wequeue.com | admin123 |

## Auth header

```
Authorization: Bearer {token}
```

## Response

```json
{ "success": true, "message": "OK", "data": {} }
```

## Endpoint

| Method | URL |
|--------|-----|
| POST | `/auth/register.php` |
| POST | `/auth/login.php` |
| POST | `/auth/logout.php` |
| GET | `/queue/status.php` |
| POST | `/queue/take.php` |
| GET | `/queue/my.php` |
| PUT | `/queue/cancel.php` |
| GET | `/queue/history.php` |
| GET | `/foods/popular.php` |
| GET | `/foods/search.php?q=...` |
| GET | `/foods/list.php` |
| POST | `/foods/create.php` |
| PUT | `/foods/update.php` |
| DELETE | `/foods/delete.php` |
| GET | `/favorites/list.php` |
| POST | `/favorites/add.php` |
| DELETE | `/favorites/remove.php` |
| GET | `/user/profile.php` |
| PUT | `/user/update.php` |
