# Setup Build aviplayer di Laptop (Android Studio)

Model kerja proyek ini: **Ryn menulis source code dan push ke GitHub; build & run dilakukan di laptop ini** lewat Android Studio. Setiap ada perubahan: `git pull`, lalu build ulang.

## 1. Instal Android Studio

1. Unduh Android Studio dari https://developer.android.com/studio dan instal seperti biasa (sudah termasuk JDK dan Android SDK Manager).
2. Buka Android Studio sekali, selesaikan wizard awal (akan mengunduh SDK Platform dan Build-Tools bawaan).

## 2. Ambil source code

Pertama kali (clone):

```powershell
git clone https://github.com/Azavern/aviplayer.git
```

Setelah itu, tiap ada update dari Ryn cukup:

```powershell
cd aviplayer
git pull
```

## 3. Buka proyek

1. Android Studio → **Open** → pilih folder `aviplayer`.
2. Studio akan menjalankan **Gradle Sync** otomatis: mengunduh Gradle 9.6.1 dan semua dependency sesuai `gradle/libs.versions.toml`. Sync pertama bisa beberapa menit.
3. Bila Studio menawarkan menginstal **SDK Platform 37** / **Build-Tools 37** yang belum ada, setujui (compileSdk proyek ini 37; targetSdk 35, minSdk 29).

Catatan:
- File `local.properties` (lokasi SDK di laptop ini) dibuat otomatis oleh Studio dan **tidak** ikut Git — memang begitu seharusnya.
- Proyek ini tidak menyertakan `gradlew`/`gradle-wrapper.jar`. Untuk build dari Android Studio itu tidak masalah. Bila suatu saat ingin build dari terminal, jalankan sekali dari terminal di dalam Android Studio (**View → Tool Windows → Terminal**): `gradle wrapper --gradle-version 9.6.1` bila perintah `gradle` tersedia, atau abaikan — build dari Studio saja cukup.

## 4. Build & jalankan

- **Build APK debug**: menu **Build → Build App Bundle(s) / APK(s) → Build APK(s)**. Hasilnya di `app/build/outputs/apk/debug/app-debug.apk`.
- **Jalankan di HP (Redmi Note 11)**:
  1. Di HP: aktifkan **Opsi pengembang** (ketuk Nomor build 7× di Pengaturan → Tentang ponsel), lalu nyalakan **USB debugging**.
  2. Colok USB, izinkan prompt debugging di HP.
  3. Di Studio, pilih perangkat di toolbar atas, klik **Run ▶**. Aplikasi terpasang dan terbuka di HP.
- Alternatif tanpa kabel: kirim file APK ke HP dan pasang manual (izinkan "instal dari sumber tidak dikenal").

## 5. Melapor balik ke Ryn

Bila sync/build gagal, kirim ke Ryn:

- Teks error dari panel **Build** / **Sync** di Android Studio (salin seluruhnya, jangan diringkas), atau screenshot-nya.
- Bila aplikasi crash saat dijalankan: teks dari tab **Logcat** beberapa baris di sekitar "FATAL EXCEPTION".

Ryn akan memperbaiki source-nya dan push; kamu tinggal `git pull` lagi.
