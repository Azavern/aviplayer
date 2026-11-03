# PRD — AVP (Android Video Player)

| | |
|---|---|
| Pemilik produk | Veo |
| Tanggal | 2 November 2026 |
| Status | Final — hasil diskusi & keputusan terkunci di thread Ngoding, 2 November 2026 |
| Dokumen teknis pendamping | `AVP_blueprint.md` (versi final yang sudah di-patch, folder yang sama) |
| Sifat produk | Aplikasi pribadi, satu pengguna, **bukan** rilis publik |

---

## 1. Ringkasan Produk

AVP adalah aplikasi pemutar video Android yang **offline-only** untuk memutar file video lokal (`.mp4` dan `.mkv` hardsub) dari penyimpanan HP, menggantikan pemutar bawaan/pihak ketiga yang penuh iklan. Fokus utamanya: bersih, cepat, nyaman dipakai harian oleh pemiliknya sendiri, dan dapat dikustomisasi secara terkendali (tema, gaya judul, posisi tombol).

Produk ini dibangun ulang dari nol berdasarkan blueprint baru. Versi legacy (blueprint 14 Juni 2026) pernah dibangun sekali dan hasilnya dinilai jelek — keluhan paling nyata: video selalu dipaksa landscape saat dibuka. PRD ini memastikan kegagalan itu tidak terulang (lihat FR-ORI).

## 2. Latar Belakang & Masalah

1. Pemutar video bawaan dan banyak pemutar pihak ketiga menampilkan iklan dan fitur yang tidak dibutuhkan untuk penggunaan pribadi.
2. Build legacy AVP memaksa orientasi landscape lewat Manifest — mengabaikan sensor perangkat dan kunci rotasi HP — sehingga pengalaman menonton terasa kaku dan salah, terutama untuk video portrait.
3. Tidak ada satu pemutar yang menggabungkan: resume per file yang andal, gesture standar (brightness/volume/seek), capture frame asli video, dan kustomisasi tombol — dalam paket offline tanpa iklan.

## 3. Prinsip Produk

- **Usable untuk pribadi adalah tolok ukurnya.** Bukan fitur terbanyak; yang menentukan sukses adalah aplikasi ini benar-benar dipakai setiap hari oleh pemiliknya.
- **UX di atas segalanya pada tahap desain.** Keputusan perilaku (terutama orientasi dan gesture) diambil dari sudut pengguna, detail teknis mengikuti.
- **Offline & privat secara desain.** Tidak ada jaringan, tidak ada akun, tidak ada pelacakan; aplikasi tidak meminta izin internet sama sekali.
- **Kustomisasi terkendali, bukan tak terbatas.** Yang dapat diatur: tema & aksen, gaya judul player, posisi tombol player (slot system), dan folder yang ditampilkan (whitelist). Sisanya tetap, sederhana, dan konsisten.

## 4. Pengguna & Konteks Penggunaan

- Pengguna tunggal: Veo. Tidak ada peran/aktor lain, tidak ada multi-user.
- Perangkat utama saat ini: Redmi Note 11, Android 12 (API 31).
- Konteks khas: menonton file video hasil unduhan yang tersimpan lokal di HP (termasuk video `.mkv` hardsub), sering berpindah tontonan dan melanjutkan di lain waktu (resume penting), kadang menonton dengan kunci rotasi HP menyala.

## 5. Tujuan & Kriteria Keberhasilan

Produk dianggap berhasil bila:

1. Semua video `.mp4`/`.mkv` lokal yang biasa diputar pengguna dapat diputar tanpa iklan dan tanpa jaringan.
2. Membuka ulang video apa pun selalu melanjutkan dari posisi terakhir (lihat FR-PLY-03).
3. Perilaku orientasi lulus seluruh matriks penerimaan di FR-ORI pada perangkat Android 12 pengguna.
4. Pengguna memilih AVP sebagai pemutar bawaan hariannya (bukan sekadar terpasang).

## 6. Rentang Dukungan & Distribusi

| Aspek | Keputusan |
|---|---|
| Platform | Android saja |
| Versi yang didukung | **Android 10 (API 29) sampai Android 15 (API 35)** |
| Perangkat di luar rentang | Di bawah Android 10 tidak didukung; perilaku di Android 16+ bukan target versi ini |
| Distribusi | Sideload APK langsung ke perangkat — tanpa Play Store, tanpa akun developer |
| Bahasa aplikasi | Bahasa Indonesia untuk label antarmuka buatan aplikasi (nama file dan metadata mengikuti aslinya) |

## 7. Ruang Lingkup — Fitur

### 7.1 Beranda & Penjelajahan

- **FR-HOME-01** — Beranda memiliki 3 tab dengan bottom navigation: **Video**, **Folder**, **Daftar Putar**.
- **FR-HOME-02** — Tab Video menampilkan seluruh video lokal sebagai daftar: thumbnail 16:9, nama file, lencana durasi, chip folder asal, ukuran file, dan menu `⋮` per item (Ganti Nama, Properti, Tambah ke Playlist, Hapus dari Daftar).
- **FR-HOME-03** — Tab Folder menampilkan folder berisi video sebagai grid 2 kolom: ikon folder beraksen, lencana aplikasi asal bila terdeteksi dari path (mis. WhatsApp), nama folder, dan jumlah video. Mengetuk folder menampilkan video di dalamnya.
- **FR-HOME-04** — Pencarian video berdasarkan nama file.
- **FR-HOME-05** — Keadaan kosong, keadaan memuat (shimmer), dan keadaan tanpa izin harus tampil sebagai layar yang jelas — aplikasi tidak boleh crash atau tampil kosong tanpa penjelasan. Tanpa izin, tampilkan layar khusus dengan tombol "Izinkan Akses".

### 7.2 Pemutaran

- **FR-PLY-01** — Memutar file `.mp4` dan `.mkv` (hardsub) dari penyimpanan lokal.
- **FR-PLY-02** — Kontrol dasar: putar/jeda, sebelumnya/berikutnya, seek bar kustom dengan penanda waktu berjalan & total, kecepatan putar (speed picker), kunci kontrol (lock — membekukan gesture & tombol), dan ganti rasio layar (fit/fill).
- **FR-PLY-03 — Resume:** posisi terakhir setiap video disimpan dan dilanjutkan otomatis saat video yang sama dibuka lagi. Posisi tersimpan saat aplikasi/player ditutup atau diminimalkan, dan tidak hilang setelah aplikasi dimulai ulang.
- **FR-PLY-04** — Kontrol menghilang otomatis setelah ±3 detik tanpa interaksi saat video berjalan; saat jeda, kontrol tetap tampil. Ketuk layar untuk menampilkan/menyembunyikan.
- **FR-PLY-05** — Layar tetap menyala selama player aktif; tampilan player bersifat immersive (bilah sistem tersembunyi).
- **FR-PLY-06** — Video berhenti sepenuhnya saat player ditinggalkan (tidak ada pemutaran di latar belakang pada versi ini).

### 7.3 Gesture

- **FR-GST-01** — Seret vertikal di sepertiga kiri layar: kecerahan. Di sepertiga kanan: volume. Keduanya menampilkan indikator visual (ikon + persentase).
- **FR-GST-02** — Seret horizontal di tengah layar: seek dengan pratinjau posisi terlebih dahulu; seek sungguhan terjadi saat jari diangkat.
- **FR-GST-03** — Ketuk dua kali kiri/kanan: mundur/maju sebesar langkah seek yang dapat diatur (bawaan 10 detik), dengan animasi penanda di zona yang diketuk.
- **FR-GST-04** — Saat kunci kontrol aktif, semua gesture diabaikan kecuali membuka kunci.

### 7.4 Orientasi (Keputusan Terkunci — bagian terpenting PRD ini)

Orientasi player **tidak boleh dikunci landscape**. Orientasi adalah keadaan yang dapat berubah saat aplikasi berjalan, dengan tiga mode:

- **FR-ORI-01 — Mode AUTO (bawaan):** player mengikuti perangkat.
  - Kunci rotasi HP **mati** → memiringkan HP memutar layar; menegakkan HP mengembalikan layar.
  - Kunci rotasi HP **nyala** → layar diam mengikuti kunci perangkat (tidak berputar sendiri).
- **FR-ORI-02 — Tombol rotasi:** satu tombol khusus di kontrol player (bawaan: kanan atas) mengganti mode dengan siklus **AUTO → LANDSCAPE → PORTRAIT → AUTO**. Mode LANDSCAPE/PORTRAIT **memaksa** orientasi dan mengalahkan kunci rotasi HP — inilah jalan keluar saat rotasi HP terkunci. Kembali ke AUTO berarti membuka kunci orientasi buatan aplikasi.
- **FR-ORI-03** — Ikon/label tombol selalu menunjukkan mode yang aktif; pergantian mode menampilkan label sesaat ("Otomatis" / "Landscape" / "Portrait").
- **FR-ORI-04** — Mengubah orientasi (dengan cara apa pun) **tidak boleh** memulai ulang video: posisi, status putar, dan kecepatan tidak berubah; hanya tata letak yang menyesuaikan.
- **FR-ORI-05** — Dalam portrait, video tampil dengan rasio aslinya (letterbox) dan kontrol tetap terjangkau; dalam landscape, video memenuhi layar.
- **FR-ORI-06** — Mode orientasi terakhir diingat aplikasi dan dipakai saat player dibuka lagi.
- **FR-ORI-07** — Membuka player tidak mengubah orientasi secara paksa: tampilan awal mengikuti mode tersimpan dan keadaan perangkat saat itu.

**Matriks penerimaan FR-ORI** (pengujian di perangkat pengguna):

| Keadaan awal | Aksi | Hasil yang wajib terjadi |
|---|---|---|
| Mode AUTO, kunci rotasi HP mati, HP portrait | HP dimiringkan | Layar menjadi landscape, video lanjut tanpa jeda/restart |
| Mode AUTO, kunci rotasi HP nyala | HP dimiringkan | Layar tidak berubah; video lanjut |
| Kunci rotasi HP nyala, layar portrait | Ketuk tombol rotasi 1× | Mode LANDSCAPE, layar berputar ke landscape |
| Mode LANDSCAPE | Ketuk tombol rotasi 1× | Mode PORTRAIT, layar berputar ke portrait |
| Mode PORTRAIT | Ketuk tombol rotasi 1× | Kembali AUTO, layar mengikuti perangkat lagi |

### 7.5 Capture Frame

- **FR-CAP-01** — Tombol capture di player mengambil frame video pada posisi saat itu **dari video itu sendiri** (bukan tangkapan layar): hasil berupa gambar resolusi asli video, tanpa overlay UI dan tanpa letterbox.
- **FR-CAP-02** — Hasil capture tersimpan ke galeri (folder `Pictures/AVP`) dan pengguna menerima konfirmasi visual (snackbar).

### 7.6 Kustomisasi, Tema & Playlist

- **FR-THM-01** — Tema mengikuti pilihan pengguna: Sistem / Terang / Gelap / AMOLED (hitam murni). Warna aksen dapat diganti (swatches preset + HEX kustom) atau mengikuti dynamic color sistem pada perangkat yang mendukung (Android 12+).
- **FR-CST-01 — Editor Judul:** ukuran font judul player (10–22sp), jumlah baris (1/2), tebal, marquee (teks berjalan), dan warna — dengan pratinjau langsung.
- **FR-CST-02 — Editor Tombol (slot system):** posisi tombol player dapat ditukar dengan mengetuk dua slot; ada tombol reset ke susunan bawaan. Susunan bawaan: kunci (kiri atas); rotasi, kecepatan, rasio (kanan atas); sebelumnya/putar-jeda/berikutnya (tengah); capture (kanan bawah).
- **FR-CST-03 — Whitelist Folder:** mode "semua folder" atau "hanya folder pilihan"; pada mode kedua, hanya folder tercentang yang tampil di tab Folder.
- **FR-CST-04** — Seluruh pengaturan kustomisasi dan tema tersimpan dan tetap berlaku setelah aplikasi dimulai ulang. Layar kustomisasi memiliki 4 tab: Judul, Tombol, Tema, Whitelist.
- **FR-LST-01 — Playlist manual:** pengguna dapat membuat/menghapus/mengganti nama playlist, menambah video dari daftar, menyusun ulang urutan dengan seret, dan memutar berurutan (Play All) — video berikutnya diputar otomatis hingga playlist habis.
- **FR-LST-02** — Kartu playlist menampilkan strip thumbnail (2–3 video pertama), nama, jumlah video, dan total durasi.

## 8. Di Luar Ruang Lingkup (versi ini)

Berikut **sengaja tidak** termasuk, dan tidak boleh ditambahkan tanpa keputusan baru pemilik produk:

- Streaming / putar dari URL, jaringan, atau cloud (Google Drive dsb.)
- Subtitle eksternal (`.srt`/`.ass` terpisah) dan penggantian trek audio — target format produk ini adalah hardsub
- Pemutaran di latar belakang, Picture-in-Picture, floating player
- Unduhan di dalam aplikasi, akun/sinkronisasi, berbagi, analitik
- Platform selain Android; penerbitan ke toko aplikasi
- Pengeditan berkas video (trim, convert, dsb.)

## 9. Persyaratan Non-Fungsional

- **NFR-01 Privasi:** nol transmisi data; tidak ada izin internet; semua data (posisi putar, playlist, pengaturan) hanya tersimpan di perangkat.
- **NFR-02 Keandalan data:** pembaruan aplikasi tidak boleh menghapus posisi putar, playlist, atau pengaturan pengguna (tanpa migrasi destruktif).
- **NFR-03 Kinerja:** daftar video tetap mulus digulir dengan thumbnail ter-cache; perpindahan membuka player terasa seketika untuk file lokal.
- **NFR-04 Konsistensi perilaku antar versi Android:** perbedaan izin dan perilaku antar Android 10–15 ditangani di dalam aplikasi; dari sisi pengguna perilakunya sama (terutama izin baca video dan penyimpanan hasil capture).
- **NFR-05 Kesederhanaan visual:** Material Design 3, dark-first, animasi keadaan konsisten (fade/crossfade), tidak ada elemen dekoratif yang mengganggu keterbacaan.

## 10. Alur Pengguna Inti (ringkas)

1. Buka aplikasi → (pertama kali: beri izin akses video) → beranda tampil.
2. Pilih video dari tab Video/Folder/pencarian, atau putar playlist.
3. Player terbuka mengikuti orientasi perangkat; tonton dengan gesture & kontrol.
4. Sesuaikan orientasi lewat sensor perangkat atau tombol rotasi bila diperlukan.
5. Keluar kapan pun; posisi tersimpan; buka lagi untuk melanjutkan.
6. Kustomisasi tema/judul/tombol/whitelist dari layar Kustomisasi; perubahan langsung terasa di player.

Alur lengkap (aktor, daftar layar, state) akan didokumentasikan terpisah di `Blueprint-Workflow.md`.

## 11. Prioritas & Urutan Pengerjaan

Mengikuti milestone pada blueprint (Bagian 11), dengan penekanan produk:

- **Inti yang membuat produk usable (harus selesai lebih dulu):** fondasi proyek → pemutaran dasar **termasuk orientasi FR-ORI** → resume → beranda & penjelajahan → tema dasar → gesture.
- **Penyempurna (setelah inti terasa enak dipakai):** capture frame → kustomisasi judul & tombol → kustomisasi tema → whitelist folder → playlist manual.
- Kriteria berpindah tahap: Definition of Done tiap milestone blueprint terpenuhi, dan untuk tahap inti, teruji langsung di perangkat Android 12 pengguna.

## 12. Risiko & Asumsi yang Diketahui

| Risiko/Asumsi | Penanganan |
|---|---|
| Stack blueprint versi baru tergolong ujung (bleeding edge) | Milestone fondasi diperlakukan sebagai uji stack; bila komponen tertentu mentok, turunkan komponen itu saja ke versi stabil terakhir (jalur mundur per komponen sudah dicatat di blueprint), bukan mengganti seluruh rancangan |
| Mesin pemutar bawaan Android punya keterbatasan format di luar target (mis. sebagian codec audio eksotik, subtitle lunak) | Diterima secara sadar: target produk adalah `.mp4`/`.mkv` hardsub; di luar itu di luar ruang lingkup §8 |
| Klaim versi stack berasal dari dokumen (perlu verifikasi saat build) | Verifikasi ke sumber resmi (Google Maven / Maven Central) wajib dilakukan pada tahap fondasi |
| Perilaku Android 16+ di luar rentang dukungan | Bukan target; dievaluasi ulang hanya bila rentang dukungan diperluas |

## 13. Keputusan Terkunci (Decision Log)

| Tanggal | Keputusan |
|---|---|
| 2026-11-02 | Produk: pemutar video **Android-only**, **offline**, **file lokal saja**, untuk **penggunaan pribadi** |
| 2026-11-02 | Blueprint yang dipakai: versi **baru** (dari Claude) sebagai basis tunggal; versi legacy menjadi arsip acuan saja |
| 2026-11-02 | Rentang dukungan **Android 10–15**; orientasi player **bebas** dengan tiga mode runtime (FR-ORI) menggantikan kunci landscape |
| 2026-11-02 | Dokumen turunan yang akan dibuat dari PRD + blueprint ini: `AGENTS.md` (aturan agent, general), `DESIGN.md` (aturan desain, general), `UI.md` (spesifikasi visual khusus AVP), `Blueprint-Workflow.md` (alur kerja saja) |

## 14. Pertanyaan Terbuka (di luar versi ini, menunggu keputusan pemilik)

- Subtitle eksternal & penggantian trek audio: dievaluasi setelah versi ini terbukti dipakai harian.
- Picture-in-Picture & pemutaran latar: kandidat versi berikutnya, belum diputuskan.
- Pemecahan blueprint menjadi dokumen general/spesifik lain di luar empat dokumen pada §13: belum diputuskan.
