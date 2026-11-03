# UI Animation & Motion Design System — Versi Android

Design Direction: Smooth, Subtle, Responsive Motion
Primary Stack: **Android — Jetpack Compose (Compose Animation / Motion)**
Visual Style: Neumorphism (terang & gelap) + Modern Minimalism
Status: Diadaptasi dari versi web (CSS/Tailwind/JavaScript) per 3 November 2026. Seluruh contoh & mekanisme web (class CSS, hover, IntersectionObserver, template Blade) telah diganti padanan Android-nya. Token durasi & easing dipertahankan agar karakternya sama.

---

## 1. Animation Philosophy

Animasi merupakan bagian dari pengalaman pengguna (User Experience), bukan sekadar dekorasi visual.

Setiap animasi harus memiliki tujuan, seperti:

- Memberikan feedback terhadap interaksi pengguna.
- Membantu pengguna memahami perubahan keadaan komponen.
- Menunjukkan hubungan antara elemen sebelum dan sesudah perubahan.
- Membuat perpindahan antarkomponen terasa natural.
- Memberikan kesan aplikasi yang responsif dan hidup.

Prinsip utama:

1. **Smooth:** Gerakan tidak terasa patah atau mendadak.
2. **Subtle:** Animasi tidak berlebihan dan tidak mengganggu fokus.
3. **Consistent:** Durasi dan karakter animasi memiliki standar yang seragam.
4. **Responsive:** Animasi tetap nyaman di perangkat kelas bawah sekalipun — bila ragu, persingkat, jangan perpanjang.
5. **Purposeful:** Setiap animasi memiliki alasan untuk digunakan.
6. **Accessible:** Pengguna yang mematikan animasi sistem tetap dapat menggunakan aplikasi dengan nyaman (lihat Bagian 8).

Design principle:

> «Motion should explain, respond, and guide — never distract.»

Perbedaan mendasar dari versi web: Android tidak memiliki *hover* dan tidak memiliki *scroll reveal* berbasis viewport observer. Padanannya diatur di Bagian 4 (feedback sentuh) dan Bagian 3 (animasi masuk daftar).

---

## 2. Animation Tokens

Seluruh animasi harus menggunakan standar durasi dan easing yang konsisten dari bagian ini. Jangan menulis angka durasi mentah di dalam Composable.

### 2.1 Duration Scale

| Token | Duration | Penggunaan di Android |
|---|---|---|
| Instant | 100 ms | Tekan tombol, micro-interaction, ripple awal |
| Fast | 150 ms | Pergantian ikon, toggle, chip terpilih |
| Normal | 200 ms | Menu dropdown, panel kecil, snackbar masuk |
| Moderate | 300 ms | Dialog, bottom sheet, accordion/expand |
| Relaxed | 450 ms | Transisi layar, panel besar, kartu masuk |
| Slow | 600 ms | Animasi masuk kelompok konten (hanya sesekali) |
| Cinematic | 800 ms | Khusus momen dekoratif langka (splash/empty state) — **hindari di aplikasi utilitas** |

Rekomendasi penggunaan:

- Interaksi yang sering (tekan, toggle, tab) tidak boleh lebih dari 200 ms.
- Dialog & bottom sheet idealnya 200–350 ms.
- Transisi antarlayar idealnya 250–450 ms.
- Hindari durasi panjang untuk elemen yang muncul berulang kali (daftar yang di-scroll, misalnya).

Di Compose, simpan skala ini sebagai konstanta/objek token (mis. `MotionTokens.durationFast`) dan pakai lewat `tween(durationMillis = ...)` atau `spring()` yang setara karakternya.

### 2.2 Easing Functions

Easing menentukan bagaimana kecepatan animasi berubah selama transisi. Kurva cubic-bezier dari versi web berlaku **persis sama** di Android karena Compose mendukung kurva yang sama lewat `CubicBezierEasing`:

| Token | Cubic Bézier | Padanan Compose | Karakter | Penggunaan |
|---|---|---|---|---|
| EaseStandard | (0.2, 0.8, 0.2, 1) | `CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)` | Umum, seimbang | Transisi bawaan hampir semua komponen |
| EaseSmooth | (0.25, 0.1, 0.25, 1) | `CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)` | Sangat lembut | Pergantian konten, crossfade |
| EaseEnter | (0, 0, 0.2, 1) | `CubicBezierEasing(0f, 0f, 0.2f, 1f)` ≈ `FastOutSlowInEasing` | Cepat masuk, melambat | Entrance: elemen datang ke layar |
| EaseExit | (0.4, 0, 1, 1) | `CubicBezierEasing(0.4f, 0f, 1f, 1f)` | Melambat lalu pergi | Exit: elemen meninggalkan layar |
| Linear | — | `LinearEasing` | Kecepatan konstan | Loading & progress saja |

Aturan: elemen **masuk** memakai EaseEnter/EaseStandard, elemen **keluar** memakai EaseExit dan durasi keluar selalu ≤ durasi masuk.

---

## 3. Entrance Animation (Pengganti Scroll Reveal)

Di web, elemen muncul saat masuk viewport. Di Android, padanannya adalah animasi masuk item pada daftar/kisi yang dapat di-scroll (`LazyColumn`/`LazyGrid`) dan animasi masuk layar.

### 3.1 Fade Up (bawaan)

- Keadaan awal: opacity 0, posisi Y bergeser **+24 dp** dari posisi akhir.
- Keadaan akhir: opacity 1, posisi Y 0.
- Durasi: 450–600 ms (token Relaxed–Slow), easing EaseStandard.
- Terapkan lewat state visibilitas per item yang diingat (`remember`) atau API animasi item bawaan daftar; item yang sudah pernah tampil **tidak** dianimasikan ulang saat di-scroll kembali.

### 3.2 Variasi

| Animation | Keadaan Awal | Keadaan Akhir |
|---|---|---|
| Fade Up | Y: +24 dp, opacity 0 | Y: 0, opacity 1 |
| Fade Down | Y: −24 dp, opacity 0 | Y: 0, opacity 1 |
| Fade Left | X: +24 dp, opacity 0 | X: 0, opacity 1 |
| Fade Right | X: −24 dp, opacity 0 | X: 0, opacity 1 |
| Fade In | Opacity 0 | Opacity 1 |
| Zoom In | Scale 0.95 | Scale 1 |
| Zoom Out | Scale 1.05 | Scale 1 |

Gunakan Fade Up sebagai bawaan untuk konten umum. Di layar dengan daftar panjang (daftar berkas/video), **matikan animasi masuk per item** atau batasi hanya untuk item yang pertama kali tampil — animasi yang berulang saat scroll cepat terasa mengganggu dan membebani perangkat.

### 3.3 Stagger Animation

Beberapa elemen yang berhubungan (mis. sekelompok kartu) muncul berurutan:

- Jeda antar-elemen: **40–80 ms** di Android (lebih rapat dari web karena layar lebih kecil & interaksi lebih cepat).
- Maksimum total jeda dalam satu kelompok: ±300 ms.
- Hanya untuk kelompok kecil yang punya hubungan visual (maks ±6 elemen); jangan pernah men-stagger seluruh isi layar.

---

## 4. Interaction Feedback (Pengganti Hover)

Android tidak punya pointer melayang untuk sentuhan; yang setara dengan *hover* adalah **keadaan ditekan (pressed)**, **fokus** (untuk keyboard/D-pad/aksesibilitas), dan **ripple**. Aturan berikut menggantikan seluruh bagian hover versi web.

### 4.1 Tombol

- **Ditekan:** skala turun ringan ke 0.97–0.98 dan/atau bayangan menyusut (pada permukaan neumorphic: berubah dari *raised* ke *pressed/inset*, lihat Neumorphism.md), durasi Instant–Fast, easing EaseStandard.
- **Dilepas:** kembali ke keadaan normal dengan durasi yang sama; jangan ada "lompatan" warna mendadak selain perubahan state yang memang dimaksud (mis. toggle aktif).
- **Ripple:** gunakan ripple Material bawaan Compose pada permukaan datar; pada permukaan neumorphic, feedback bayangan adalah feedback utama dan ripple dibuat sangat tipis atau dimatikan agar tidak merusak ilusi cahaya.
- **Fokus (keyboard/D-pad/switch access):** tampilkan cincin/outline aksen 2 dp — keadaan fokus tidak boleh hanya mengandalkan bayangan.

### 4.2 Kartu & Item Daftar

- Kartu interaktif: saat ditekan, skala 0.98 + bayangan menguat sesaat (angkat semu), durasi Fast.
- Item daftar panjang: cukup ripple/penyorotan latar Fast — tanpa skala, agar scroll tetap terasa stabil.
- Keadaan terpilih (selected) bersifat menetap (warna aksen/inset), dibedakan jelas dari keadaan ditekan sesaat.

### 4.3 Toggle, Switch, Chip

- Perubahan keadaan: Fast (150 ms) dengan EaseStandard; kenop switch meluncur, bukan berpindah kasar.
- Chip terpilih: transisi warna + inset bayangan Fast; chip tidak terpilih kembali raised/flat.

---

## 5. Component & Screen Transitions

| Komponen / Peristiwa | Animasi | Durasi | Easing |
|---|---|---|---|
| Pergantian tab (konten) | Crossfade konten; indikator tab meluncur | 200–300 ms | EaseSmooth |
| Transisi antarlayar (navigasi) | Masuk: fade + geser 24 dp dari kanan/bawah; Keluar: fade lebih cepat | 250–450 ms masuk, ≤200 ms keluar | EaseEnter / EaseExit |
| Dialog | Masuk: fade + Zoom In dari 0.95; Keluar: fade | 200–300 ms / 150 ms | EaseEnter / EaseExit |
| Bottom sheet | Meluncur dari bawah; latar meredup bersamaan | 300 ms | EaseStandard |
| Menu dropdown/popup | Fade + Zoom In ringan dari titik jangkar | 150–200 ms | EaseEnter |
| Snackbar | Masuk dari bawah (fade + geser), keluar fade | 200 ms / 150 ms | EaseStandard |
| Expand/collapse (accordion, panel) | Animasi ukuran konten + crossfade isi | 200–300 ms | EaseSmooth |
| Bilah navigasi atas/bawah | Muncul/hilang dengan geser + fade mengikuti konteks layar | 200 ms | EaseStandard |

Prinsip: transisi **bolak-balik harus simetris** — jalan kembali memakai animasi kebalikannya, bukan animasi lain. Gunakan mekanisme transisi bawaan kerangka navigasi yang dipakai (shared element hanya untuk pasangan elemen yang benar-benar sama, mis. thumbnail → header detail).

---

## 6. Loading & Progress

- **Shimmer:** untuk placeholder daftar/kartu saat memuat. Gerakan sapuan cahaya Linear 1.200–1.600 ms per siklus, warna sapuan mengikuti permukaan tema (terang: permukaan lebih terang; gelap: permukaan lebih terang dari latar gelap — lihat token Neumorphism.md). Shimmer berhenti begitu konten tampil (crossfade Normal).
- **Progress tak tentu (indeterminate):** indikator Material standar, Linear; jangan membuat animasi loading kustom yang lebih ramai dari standarnya.
- **Progress tentu (determinate):** nilai berubah dengan tween Fast agar tidak tersentak; teks persentase mengikuti tanpa animasi angka.
- Layar kosong (empty state) boleh memakai satu animasi dekoratif Cinematic yang berulang pelan; layar berisi data tidak.

---

## 7. Motion Khusus Konten Media (Player)

Bagian ini tidak ada di versi web; ditambahkan karena konteks Android mencakup pemutar media.

- **Kontrol tampil/sembunyi:** fade (bukan geser) Normal–Moderate dengan EaseSmooth; kontrol menghilang otomatis tetap menampilkan kursor/penanda singkat sebelum hilang bila platform mendukungnya. Saat jeda (paused), kontrol **tidak** ikut animasi keluar.
- **Ketuk dua kali (double-tap seek):** ripple meluas dari titik ketuk + label jumlah detik memudar dalam 600 ms; sisi kiri/kanan memakai arah animasi yang cermin.
- **Indikator gesture (brightness/volume/seek preview):** muncul Instant saat gesture mulai, nilai berubah tanpa animasi tambahan (mengikuti jari 1:1), hilang dengan fade Fast 300 ms setelah gesture berakhir. Animasi tidak boleh mengejar/menginterpolasi nilai gesture — respons harus seketika.
- **Perubahan orientasi:** tidak ada animasi buatan; rotasi ditangani sistem. Yang dianimasikan hanya penyesuaian tata letak sesudahnya (crossfade/ukuran, Normal, EaseSmooth) bila diperlukan.
- **Perubahan kecepatan/putar-jeda ikon:** ikon play/pause bertukar dengan crossfade + Zoom ringan Fast.

---

## 8. Reduced Motion & Aksesibilitas

Android tidak memiliki saklar tunggal bernama "reduced motion" seperti web (`prefers-reduced-motion`); padanannya adalah **pengaturan sistem yang mematikan/mengurangi animasi** (skala durasi animator = 0 di opsi pengembang, dan pengaturan aksesibilitas "Hapus animasi" pada sebagian perangkat).

- Hormati pengaturan itu: bila animasi sistem dimatikan, semua transisi menjadi potong langsung (durasi 0) kecuali animasi yang menyampaikan informasi penting (progress).
- Jangan pernah menyampaikan informasi **hanya** lewat gerakan (mis. "elemen yang benar ada di sisi yang bergeser") — selalu ada penanda statis (ikon/teks/warna).
- Animasi berulang tanpa henti hanya untuk indikator loading; tidak ada animasi latar dekoratif yang berjalan terus di layar berisi data.
- Kedipan/flash di atas 3 kali per detik dilarang.

---

## 9. Implementation Rules (Compose)

- Semua durasi & easing diambil dari token Bagian 2, didefinisikan satu kali (mis. objek `MotionTokens` / `AppMotion`) — dilarang angka ajaib tersebar di Composable.
- Animasikan properti murah: `alpha`, `scale`, `translation`, dan warna lewat `graphicsLayer`/API animasi Compose; hindari menganimasikan ukuran layout berantai yang memicu pengukuran ulang seluruh layar, kecuali expand/collapse yang memang memerlukannya.
- Untuk permukaan neumorphic, animasikan **parameter bayangan** (offset/blur/alpha) seperlunya saja; bayangan statis jangan dihitung ulang setiap frame — cache/hitung sekali per keadaan.
- Gunakan `spring()` hanya untuk interaksi yang digerakkan jari (sheet, drag) agar bisa mengikuti kecepatan lepas; transisi terjadwal memakai `tween` token di atas.
- Uji di perangkat kelas bawah: bila animasi menjatuhkan frame, persingkat durasinya atau sederhanakan propertinya — jangan menghapus feedback-nya.

---

## 10. Do & Don't

**Do**

- Satu karakter gerak untuk seluruh aplikasi: token yang sama di semua layar, terang maupun gelap.
- Feedback seketika untuk setiap sentuhan (≤100 ms mulai terasa).
- Animasi keluar selalu lebih singkat atau sama dengan animasi masuk.
- Matikan/ringankan animasi daftar panjang dan animasi saat pengguna mematikan animasi sistem.

**Don't**

- Mengimpor pola web apa adanya: hover, scroll reveal viewport, animasi hero/cinematic di aplikasi utilitas.
- Durasi >300 ms untuk interaksi yang dilakukan berulang kali.
- Animasi yang menghalangi input (pengguna harus bisa mengetuk sebelum animasi selesai).
- Gerakan yang berbeda-beda antarlayar untuk peristiwa yang sama.
