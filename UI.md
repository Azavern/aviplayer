# UI.md — Spesifikasi Visual AVP (Android Video Player)

> Spesifikasi visual dan tampilan **khusus AVP**. Tidak memuat arsitektur, stack, atau langkah pengerjaan. Sumber, berurutan: PRD.md (§7, §9) → AVP_blueprint.md (§5 dan nilai konfigurasi §8) → referensi tambahan **Neumorphism.md** dan **Motion.md** (keduanya versi web, diadaptasi ke Android; lihat §0). Bila sumber bertentangan, yang urutannya lebih tinggi diikuti dan pertentangannya dicatat di §11.
> Tanda **⚠PT-xx** menunjuk Pertanyaan Terbuka di §11 (celah atau pertentangan; tidak ditebak).

## Daftar Isi

0. Cara Membaca & Pembacaan yang Diambil
1. Identitas Visual AVP
2. Token Visual (warna, kontras, bentuk, jarak, bayangan, tipografi, ikon, gerak)
3. Beranda: Kerangka, Navigasi & Pencarian
4. Tab Video
5. Tab Folder & Daftar Video dalam Folder
6. Tab Daftar Putar & Detail Playlist
7. Layar Player
8. Layar Kustomisasi (4 Editor)
9. Tabel Nilai Konkret Proyek
10. Keadaan Visual per Layar
11. Catatan & Pertanyaan Terbuka

---

## 0. Cara Membaca & Pembacaan yang Diambil

Dua dokumen tambahan (Neumorphism.md dan Motion.md) ditulis untuk web (CSS, Tailwind, Alpine, hover, scroll). Pembacaan yang diambil untuk AVP:

1. **Yang dipakai**: palet warna, sistem bayangan, skala radius, jarak, tipografi, panduan ikon, token durasi dan easing, serta pola gerak yang punya padanan di layar sentuh. Satuan `px` pada referensi dibaca sebagai **dp** (teks sebagai **sp**).
2. **Yang tidak dipakai**: kode CSS/Tailwind/Alpine, hover, scroll reveal, stagger, transisi halaman penuh, tooltip hover, scroll progress, sticky header, dan tabel data bergaya web. Alasan per butir ada di §2.9.
3. **Struktur dan perilaku** tetap milik PRD/blueprint: Material Design 3, dark-first, empat mode tema, AMOLED `#000000`, 3 tab, slot system, dan nilai konkret blueprint.
4. **Lapisan "Soft UI" (Neumorphism)** diterapkan sebagai karakter permukaan pada tema **Terang dan Gelap** untuk layar Beranda, Daftar Putar, dan Kustomisasi — pasangan bayangan per tema mengikuti Neumorphism.md versi Android (§2–§3 dokumen itu). **AMOLED** memakai varian datar (tanpa bayangan ganda; pemisahan oleh garis tipis & tonal). **Layar Player tidak memakai** Soft UI: kontrolnya overlay transparan di atas video (PRD).
5. **Keputusan besar yang sudah diambil (3 November 2026):** aksen bawaan = lavender Primary referensi (PT-25, opsi a) dan nilai netral + bayangan tema Gelap diisi dari Neumorphism.md versi Android (PT-26), dan font dibundel Manrope + Inter (PT-27). Ketiga keputusan visual besar di daftar ini sudah tuntas.

---

## 1. Identitas Visual AVP

| Aspek | Ketetapan |
|---|---|
| Fondasi | Material Design 3; sudut membulat, elevasi halus, peran warna; **dark-first** (gelap dirancang lebih dulu) |
| Karakter permukaan | Soft UI pada tema Terang & Gelap (§0 butir 4); varian datar pada AMOLED |
| Mode tema bawaan | **Sistem** (mengikuti terang/gelap perangkat) |
| Mode tersedia | Sistem · Terang · Gelap · AMOLED |
| Warna aksen bawaan | **Lavender `#6655F5`** (`0xFF6655F5`) di tema Terang; **`#8B7FFF`** (`0xFF8B7FFF`) di tema Gelap/AMOLED (diterangkan agar kontras cukup — Neumorphism.md versi Android §2.1). Keputusan 3 Nov 2026, lihat PT-25 |
| Dynamic color | Bawaan **mati**. Toggle "Gunakan Dynamic Color" hanya muncul pada **Android 12 ke atas**; di Android 10–11 toggle tidak ditampilkan |
| AMOLED | Seperti tema Gelap, kecuali **latar (`background`) dan permukaan (`surface`) = `#000000`** |
| Warna judul player bawaan | Putih `#FFFFFF` |
| Pemakaian aksen | Ikon folder, ikon tab terpilih, progres dan thumb seek bar, tombol aktif, FAB, elemen primer lainnya |
| Huruf | **Dibundel di dalam aplikasi**: Manrope (judul layar, label navigasi, tombol) dan Inter (isi, metadata, judul player, semua bacaan angka). Lihat PT-27 |
| Ikon | Material Icons keluarga **Rounded** |
| Gaya umum | Minimalis tapi informatif; tidak ada elemen dekoratif yang mengganggu keterbacaan |
| Gerak | Fade (kontrol), crossfade (pergantian konten), animasi ukuran (elemen memuai/menyusut), ripple pada semua yang dapat diketuk |
| Bahasa label | Bahasa Indonesia untuk semua label buatan aplikasi; nama file/metadata mengikuti aslinya |

Perilaku dasar: tema diterapkan instan di seluruh layar; semua warna berasal dari peran warna tema (tidak ada warna keras di komponen).

---

## 2. Token Visual

### 2.1 Warna: tema Terang (palet Soft UI)

| Token | Hex | Pemakaian di AVP |
|---|---|---|
| Background | `#E9EBF8` | Latar layar |
| Background Soft | `#EFF0FA` | Area sekunder (pengelompokan di Kustomisasi) |
| Surface | `#EAECF8` | Kartu (folder, playlist), panel, dialog, kontainer bar navigasi |
| Surface Light | `#F4F5FC` | Area terangkat |
| Surface Dark | `#DDE1F1` | Area inset: bar pencarian, input HEX, trek slider, trek switch mati |
| Surface Pressed | `#E4E7F6` | Keadaan ditekan pada permukaan (padanan *hover* referensi) |
| Text Primary | `#171A36` | Judul dan teks utama |
| Text Secondary | `#515A8C` | Deskripsi, metadata (nama folder pada chip, jumlah video, ukuran file), placeholder |
| Text Muted | `#858CAD` | **Tidak dipakai untuk teks informatif** (kontras 2,79:1; lihat §2.3) |
| Text Disabled | `#AEB3CA` | Teks nonaktif saja, dengan penanda tambahan (ikon/redup) |
| Border Subtle | `#DCE0F0` | Pemisah elemen |
| Divider | `#D7DBED` | Garis pemisah |
| Info (Blue) / latar | `#438BFA` / `#DCEBFF` | Informasi (ikon/isian) |
| Sukses (Green) / latar | `#25B993` / `#D9F5EC` | Sukses (ikon/isian) |
| Peringatan (Orange) / latar | `#F5A443` / `#FFF0DA` | Peringatan (ikon/isian) |
| Error (Red) / latar | `#E76B82` / `#FCE4E9` | Error (ikon/isian) |

Warna semantik dipakai sebagai **isian atau ikon yang selalu disertai label teks** dalam Text Primary/Secondary. Tidak dipakai sebagai warna teks pada latar terang (kontrasnya 1,7–2,8:1; §2.3).

**Aksen bawaan & preset swatches** (⚠PT-07): Primary (aksen bawaan sejak 3 Nov 2026, lihat PT-25) `#6655F5` (turunan: Hover `#5745E8`, Active `#4938D4`, Light `#E8E5FF`, Soft `#F0EEFF`, Dark `#39318F`), Blue `#438BFA`, Purple `#8875FA`, Cyan `#55C9D5`, Green `#25B993`, Orange `#F5A443`, Red `#E76B82`. Gradien Primary `#8172FF → #5946EF` (lihat ⚠PT-34).

Aksen yang dipilih pengguna menurunkan padanan *Primary Light/Soft/Dark* secara tonal; aturan turunan untuk aksen kustom belum ditetapkan (⚠PT-34).

### 2.2 Warna: tema Gelap dan AMOLED

| Elemen | Gelap | AMOLED |
|---|---|---|
| `background` | `#191B2B` (Background — Neumorphism Android §2.2) | `#000000` |
| `surface` | `#1C1E30` (Surface) | `#000000` |
| Tingkat permukaan lain | Surface Light `#262842` (terangkat), Surface Dark `#12131F` (inset), Surface Pressed `#23253E` | Elevasi tonal sangat halus + garis tipis (`outlineVariant` `#1E1E28`, 1dp) agar kartu terbaca |
| Teks | Text Primary `#F1F2FA` / Text Secondary `#B4B9D4` (`onSurface` / `onSurfaceVariant`) | sama |
| Border / Divider | Border Subtle `#2C2F4D` / Divider `#272A44` | `#1E1E28` |
| Aksen | Bawaan `#8B7FFF` (Primary tema Gelap) atau pilihan pengguna | sama |

Tema Gelap memakai bayangan ganda versi gelap (§2.6b). Khusus AMOLED tidak ada bayangan ganda: pemisahan kartu dilakukan dengan garis tipis dan tonal, bukan bayangan (varian datar, Neumorphism Android §3.4).

### 2.3 Kontras terang: hasil hitungan (WCAG 2.x)

| Pasangan | Rasio | Hasil | Akibat di AVP |
|---|---|---|---|
| Text Primary di Background | 14,31:1 | Lulus | Judul dan teks utama |
| Text Secondary `#515A8C` di Background | 5,55:1 | Lulus 4,5:1 | Metadata dan placeholder |
| Secondary alt `#737B9F` di Background | 3,50:1 | Gagal teks biasa | Tidak dipakai |
| Placeholder alt `#9299B6` di Background | 2,37:1 | Gagal | Tidak dipakai |
| Text Muted `#858CAD` di Background | 2,79:1 | Gagal | Tidak untuk teks informatif |
| Text Disabled `#AEB3CA` di Background | 1,75:1 | Dikecualikan (nonaktif) | Wajib disertai penanda lain |
| Primary `#6655F5` di Background | 4,23:1 | Lulus 3:1 (ikon/indikator), gagal 4,5:1 | Bukan untuk teks kecil |
| Putih di Primary `#6655F5` | 5,02:1 | Lulus | Teks tombol |
| Putih di awal gradien `#8172FF` | 3,63:1 | Gagal teks biasa | Teks kecil di atas ujung terang gradien tidak aman |
| Putih di akhir gradien `#5946EF` | 5,92:1 | Lulus | — |
| Biru Material `#2196F3` (aksen lama, bukan bawaan lagi) di Background | 2,63:1 | **Gagal 3:1** | Alasan aksen bawaan diganti (PT-25) |
| Putih di `#2196F3` | 3,12:1 | Lulus hanya teks besar/ikon | Teks tombol kecil di atas biru lama gagal (PT-25) |
| `#2196F3` di `#000000` | 6,72:1 | Lulus | AMOLED aman |
| Surface vs Background | 1,008:1 | Sengaja (pemisahan oleh bayangan) | Bukan penanda interaksi (§2.6) |

Aturan turunannya: teks informatif memakai Text Primary/Secondary; kontrol interaktif punya penanda selain bayangan (label, ikon, isian aksen) dengan kontras ≥ 3:1; keadaan terpilih memakai isian aksen plus ikon terisi.

### 2.4 Bentuk (radius)

| Token | Nilai | Pemakaian di AVP |
|---|---|---|
| XS | 6dp | Lencana durasi (usulan) |
| Thumbnail | **8dp** | Thumbnail video 16:9 (blueprint; di luar skala referensi, ⚠PT-29) |
| SM | 10dp | Chip folder |
| MD | 14dp | Tombol, input, bar pencarian (14–16), segmented button |
| LG | 20dp | Kartu folder, kartu playlist, FAB (usulan) |
| XL | 24dp | Dialog, panel besar |
| 2XL | 30dp | Tidak dipakai |
| Full | 9999dp | Trek dan knob switch, swatches warna |

Radius bersarang: radius dalam = radius luar − padding.

### 2.5 Jarak

Basis kelipatan 4dp: 4 · 8 · 12 · 16 · 20 · 24 · 32 · 40 · 48 · 64.

| Pemakaian | Nilai |
|---|---|
| Margin layar | 16dp |
| Jarak antar item grid folder | **8dp** (blueprint) |
| Padding kartu folder (grid ±160dp lebar) | 16dp |
| Padding kartu playlist | 20dp |
| Jarak antar kartu playlist | 16dp |
| Jarak antar item daftar video | 12dp |
| Dalam komponen | 8–16dp |
| Antar bagian di Kustomisasi | 32dp |

### 2.6 Bayangan (Soft UI, tema Terang)

Format: `offset x, y / blur`: bayangan gelap di (+x, +y) dan sorotan putih di (−x, −y).

| Token | Gelap | Sorotan | Pemakaian |
|---|---|---|---|
| XS | 3/3/6 `#D2D6E7` | −3/−3/6 `#FFFFFF` | Elemen kecil |
| SM | 5/5/10 `#D1D5E7` | −5/−5/10 `#FFFFFF` | Tombol, kartu folder (grid rapat), kontainer bar bawah |
| MD | 8/8/16 `#CDD2E5` | −8/−8/16 `#FFFFFF` | Kartu playlist |
| LG | 12/12/24 `#CBD0E3` | −12/−12/24 `#FFFFFF` | Dialog, panel besar |
| Inset | inset 4/4/8 `#D1D5E7` | inset −4/−4/8 `#FFFFFF` | Bar pencarian, input HEX, trek slider, trek switch, kontrol terpilih |
| Ditekan | inset 3/3/6 `#D1D5E7` | inset −3/−3/6 `#FFFFFF` | Tombol saat ditekan |

Aturan:
- Kartu dalam grid folder memakai **SM**, bukan MD, karena jarak 8dp antar item tidak cukup untuk bayangan MD (⚠PT-31).
- Daftar video **datar** (tanpa bayangan): konten padat berulang; referensi sendiri menyarankan tabel/daftar data tidak diberi efek neumorphism berlebihan (⚠PT-31).
- Bayangan bukan satu-satunya penanda interaksi atau keadaan terpilih (§2.3).

### 2.6b Bayangan (Soft UI, tema Gelap)

Format sama dengan §2.6: bayangan gelap di (+x, +y), sorotan di (−x, −y) — di tema Gelap sorotan bukan putih, melainkan biru-abu terang (pasangan bayangan gelap dari Neumorphism.md versi Android §3).

| Token | Gelap | Sorotan | Pemakaian |
|---|---|---|---|
| XS | 3/3/6 `#101222` | −3/−3/6 `#2A2E4C` | Elemen kecil |
| SM | 5/5/10 `#101222` | −5/−5/10 `#2A2E4C` | Tombol, kartu folder (grid rapat), kontainer bar bawah |
| MD | 8/8/16 `#0E1020` | −8/−8/16 `#2C3050` | Kartu playlist |
| LG | 12/12/24 `#0E1020` | −12/−12/24 `#2C3050` | Dialog, panel besar |
| Inset | inset 4/4/8 `#101222` | inset −4/−4/8 `#2A2E4C` | Bar pencarian, input HEX, trek slider, trek switch, kontrol terpilih |
| Ditekan | inset 3/3/6 `#101222` | inset −3/−3/6 `#2A2E4C` | Tombol saat ditekan |

Aturan pemakaian sama dengan §2.6 (daftar video tetap datar; bayangan bukan satu-satunya penanda interaksi atau keadaan terpilih). AMOLED: tanpa bayangan sama sekali — lihat §2.2.

### 2.7 Tipografi

Manrope untuk judul, navigasi, tombol; Inter untuk isi dan metadata. Bobot 750 pada referensi dipakai hanya bila font variabel mendukungnya; selain itu 700 atau 800.

**Pembundelan (PT-27, diputuskan 3 November 2026):** kedua font **dibundel di dalam aplikasi** sebagai font variabel (lisensi OFL) — aplikasi offline-only, jadi tidak ada unduhan font saat runtime. Alternatif yang ditimbang dan **tidak dipilih**: font sistem (kehilangan karakter Soft UI) dan Montserrat (huruf paling lebar, paling lemah di ukuran kecil, karakternya font branding — kalah dari Manrope untuk judul dan dari Inter untuk teks).

**Dua penajaman pemakaian:** (1) judul di dalam player memakai Inter, bukan Manrope — ia adalah nama file di atas video pada ukuran 10–22 sp dan sering berjalan (marquee), jadi keterbacaan didahulukan (lihat §7.6). (2) **Semua bacaan angka memakai Inter dengan angka tabular** (`fontFeatureSettings = "tnum"`): teks durasi posisi/total, label kecepatan (mis. `1.25×`), persentase indikator gestur, selisih seek (`+00:30`), dan angka pada lencana — lebar digit yang tetap mencegah teks bergoyang saat nilainya berubah tiap detik. Aturan angka ini mengalahkan penugasan font per elemen khusus untuk bagian angkanya.

| Skala | Font | Ukuran | Bobot | Tinggi baris | Pemakaian di AVP (usulan, ⚠PT-35) |
|---|---|---|---|---|---|
| Display | Manrope | 40sp | 800 | 1,15 | Tidak dipakai |
| H1 | Manrope | 32sp | 800 | 1,2 | Tidak dipakai |
| H2 | Manrope | 26sp | 750 | 1,25 | Tidak dipakai |
| H3 | Manrope | 21sp | 700 | 1,3 | Judul layar (Kustomisasi, Detail playlist) |
| H4 | Manrope | 18sp | 700 | 1,35 | Nama playlist, nama folder, judul keadaan kosong |
| Body Large | Inter | 16sp | 400 | 1,6 | Nama video di daftar |
| Body | Inter | 14sp | 400 | 1,6 | Teks isi, label kontrol |
| Body Small | Inter | 13sp | 400 | 1,5 | Metadata (ukuran file, jumlah video) |
| Caption | Inter | 12sp | 400 | 1,4 | Lencana durasi, label kecil |
| Button | Manrope | 14sp | 700 | 1,2 | Label tombol, label navigasi bawah |
| Statistic | Manrope | 28sp | 800 | 1,2 | Tidak dipakai |

Judul di player mengikuti **TitleConfig** (§7.6), bukan tabel ini. Teks panjang: ellipsis di akhir; marquee hanya pada judul player.

### 2.8 Ikon

Keluarga **Rounded** (blueprint). Ukuran: kecil **16dp** (chip, lencana), standar **20dp** (item menu), besar **24dp** (bar navigasi, kontrol player, FAB). Semua kontrol interaktif punya area sentuh 48×48dp. Warna ikon: aksen atau Text Secondary; jangan mencampur gaya ikon dalam satu layar (⚠PT-28).

### 2.9 Gerak

**Durasi**

| Token | Durasi | Pemakaian di AVP |
|---|---|---|
| Instant | 100ms | Umpan balik tekan |
| Fast | 150ms | Keluar menu, thumb seek membesar, label orientasi masuk/keluar |
| Normal | 200ms | Fade kontrol player, switch, animasi ukuran kecil, menu masuk, crossfade tab |
| Moderate | 300ms | Dialog, perubahan layar kecil |
| Relaxed | 450ms | Tidak dipakai |

**Easing**: Standard `cubic-bezier(0.2, 0.8, 0.2, 1)` (umum), Enter `cubic-bezier(0, 0, 0.2, 1)`, Exit `cubic-bezier(0.4, 0, 1, 1)`, Linear (spinner, pemuat).

**Pola per komponen**

| Komponen | Gerak | Durasi |
|---|---|---|
| Tekan tombol/kartu (Terang) | Ripple + skala 0,97 + bayangan menjadi inset | 100–150ms |
| Overlay kontrol player | Fade masuk/keluar | 200ms (usulan, ⚠PT-36) |
| Pergantian tab | Crossfade konten | 200ms (usulan, ⚠PT-36) |
| Menu ⋮ | Fade + geser −8dp + skala 0,95→1; keluar kebalikannya | 200ms masuk / 150ms keluar |
| Dialog | Scrim fade 200ms; panel fade + skala 0,95→1 + geser 12dp→0, 250ms; keluar 150ms | 250ms |
| Switch | Knob geser + warna trek berubah bersamaan | 150–200ms |
| Thumb seek bar | Membesar saat diseret (animasi skala) | 150ms |
| Elemen memuai/menyusut | Animasi ukuran | 200ms |
| Shimmer | Gradien bergeser tanpa henti sampai data siap | 1500ms, ease-in-out |
| Spinner (player memuat) | Berputar | 700ms, linear |

**Tidak diadopsi dari Motion.md** (⚠PT-33): hover (tidak ada di layar sentuh), scroll reveal dan stagger (tidak ada di PRD/blueprint), transisi halaman penuh, tooltip, accordion, scroll progress, sticky header, Alpine/IntersectionObserver.

**Pengurangan animasi**: bila pengguna memilih mengurangi/menghapus animasi di sistem, transisi di atas menjadi perubahan instan atau fade minimal; shimmer menjadi blok statis; teks berjalan (marquee) pada judul player berhenti dan memakai ellipsis (⚠PT-37). Animasi tidak boleh menghalangi interaksi.

---

## 3. Beranda: Kerangka, Navigasi & Pencarian

### 3.1 Kerangka layar (portrait)

```
┌──────────────────────────────────┐
│ [Bilah atas: pencarian + ⚙ menu] │  ← ⚠PT-21
│                                  │
│   Konten tab aktif               │
│   (Video / Folder / Daftar Putar)│
│                         [ + ]    │  ← FAB, hanya di tab Daftar Putar
├──────────────────────────────────┤
│ [Video]  [Folder]  [Daftar Putar]│  ← Navigation bar M3 (bawah)
└──────────────────────────────────┘
```

Beranda tampil **portrait** (⚠PT-22). Latar layar = Background.

### 3.2 Navigation bar bawah (3 tab)

| Tab | Label | Ikon terpilih | Ikon tidak terpilih |
|---|---|---|---|
| Video | "Video" | `PlayCircle` terisi | `PlayCircle` outline |
| Folder | "Folder" | `Folder` terisi | `FolderOpen` |
| Daftar Putar | "Daftar Putar" (⚠PT-01) | `QueueMusic` terisi (varian auto-mirrored) | `QueueMusic` outline |

- Kontainer: Surface dengan bayangan SM; label Manrope 12sp (Button/Caption), ikon 24dp.
- Terpilih: **ikon terisi + warna aksen + kontainer indikator aksen lembut** (padanan Primary Light). Tidak terpilih: ikon outline + Text Secondary. Status terpilih tidak hanya dibedakan oleh bayangan.
- Pergantian tab: crossfade pada konten; posisi gulir tiap tab dipertahankan.

### 3.3 Bilah pencarian
- Komponen pencarian M3 (*docked*/*search bar*), tinggi 48dp, radius 14–16dp, **inset** (Surface Dark + bayangan inset); placeholder Text Secondary; ikon pencarian 24dp.
- Muncul di atas konten saat diaktifkan dan menyaring daftar berdasarkan **nama file** seiring pengetikan.
- Hasil kosong memakai keadaan kosong pencarian (§10.1).

---

## 4. Tab Video (Daftar Video)

Daftar vertikal; satu item per baris; item **datar** (tanpa bayangan), jarak antar item 12dp.

### 4.1 Anatomi item video

```
┌─────────────────────────────────────────────┐
│ ┌───────────────┐  Nama file video maks      │
│ │  thumbnail    │  dua baris, ellipsis…   ⋮  │
│ │  16:9, r=8dp  │  [📁 NamaFolder]           │
│ │        [12:34]│                  215,94 MB │
│ └───────────────┘                            │
└─────────────────────────────────────────────┘
```

| Elemen | Spesifikasi |
|---|---|
| Thumbnail | Rasio **16:9**, radius **8dp**, placeholder **shimmer** saat memuat |
| Lencana durasi | Pojok **kanan bawah** thumbnail; latar gelap semi-transparan (±60–80% opak), teks putih Caption 12sp, radius XS 6dp (⚠PT-08, ⚠PT-09) |
| Nama file | Body Large 16sp, maks **2 baris**, ellipsis di akhir, Text Primary |
| Chip folder | Di bawah nama; ikon folder 16dp + nama folder; gaya chip *tonal*; teks Text Secondary 13sp; radius SM 10dp |
| Ukuran file | Kanan bawah item; Body Small 13sp, Text Secondary; format lokal Indonesia, mis. "215,94 MB", "1,2 GB" (⚠PT-03) |
| Menu `⋮` | Di kanan item; ikon 24dp; area sentuh 48×48dp |
| Interaksi | Ketuk item → putar; tekan lama atau ketuk `⋮` → menu; **ripple** saat ditekan |

### 4.2 Menu `⋮` per item
Butir menu: **Ganti Nama**, **Properti**, **Tambah ke Playlist**, **Hapus dari Daftar**. Menu: Surface, radius MD 14dp, bayangan LG, gerak §2.9. Bentuk dialog/panel tiap butir belum ditentukan sumber (⚠PT-20).

### 4.3 Placeholder shimmer
Beberapa baris shimmer yang meniru item (thumbnail 16:9 + dua baris teks). Warna shimmer Terang: `#E0E3F2` → `#F2F3FA` → `#E0E3F2`, radius 12dp, 1500ms; padanan tonal pada Gelap/AMOLED.

---

## 5. Tab Folder & Daftar Video dalam Folder

### 5.1 Grid folder
**2 kolom**, jarak antar item **8dp**.

```
┌───────────────┐
│          [app]│  ← lencana ikon aplikasi asal (kanan atas ikon), bila terdeteksi
│     📁        │  ← ikon folder besar, warna aksen
│ Nama folder   │  ← tebal, maks 2 baris
│ 12 video      │  ← warna sekunder
└───────────────┘
```

| Elemen | Spesifikasi |
|---|---|
| Kartu | Surface, radius LG 20dp, bayangan SM, padding 16dp |
| Ikon folder | Besar, warna aksen (`primary`); bukan ikon folder generik OS (⚠PT-39) |
| Lencana aplikasi asal | Ikon aplikasi kecil di pojok kanan atas ikon folder, **hanya bila** path mengandung nama aplikasi yang dikenali (mis. WhatsApp) (⚠PT-19) |
| Nama folder | H4 18sp tebal, maks 2 baris |
| Jumlah video | Format "12 video", Body Small 13sp, Text Secondary |
| Interaksi | Ketuk kartu → tampilkan video di dalam folder; ripple + efek tekan |

### 5.2 Daftar video dalam folder
Memakai anatomi item §4, dibatasi pada folder yang dipilih; ada penunjuk folder aktif dan jalan kembali ke grid.

---

## 6. Tab Daftar Putar & Detail Playlist

### 6.1 Kartu playlist
Daftar vertikal kartu: Surface, radius LG 20dp, bayangan MD, padding 20dp, jarak antar kartu 16dp.

```
┌────────────────────────────────────────┐
│ ┌──┐┌──┐┌──┐   Nama Playlist (tebal)    │
│ │t1││t2││t3│   12 video · 1j 23m         │
│ └──┘└──┘└──┘                             │
└────────────────────────────────────────┘
```

- Strip thumbnail: **2–3** thumbnail pertama, **bertumpuk dengan offset tipis** (ukuran dan offset: ⚠PT-39).
- Nama playlist H4 18sp tebal; "jumlah video · total durasi" di bawahnya (Body Small, Text Secondary).
- **FAB `+`** pojok kanan bawah: isian aksen, radius LG, bayangan SM, ikon 24dp, area ≥ 48dp.
- Kosong: keadaan kosong dengan ilustrasi (§10.1).

### 6.2 Detail playlist
- Layar di dalam Beranda (bukan layar terpisah di luar Beranda).
- **Bilah atas**: tombol kembali, nama playlist (H3), tombol edit nama.
- **Daftar entri** datar dengan **handle seret** untuk mengubah urutan.
- **Bawah**: tombol **Play All** (utama: isian aksen) dan **Shuffle** (sekunder: Surface + bayangan SM) (⚠PT-24).
- Bentuk aksi hapus entri/playlist belum ditentukan (⚠PT-20).

---

## 7. Layar Player

Layar ini **tidak memakai Soft UI**: kontrol adalah overlay transparan di atas video (PRD), sehingga tidak ada bayangan ganda atau kartu.

### 7.1 Prinsip tampilan
- Immersive: bilah sistem tersembunyi; layar tetap menyala.
- Orientasi **tidak dikunci landscape**; tampil benar di portrait dan landscape (mode: Otomatis / Landscape / Portrait).
- Latar area video hitam. Kontrol berupa overlay transparan yang muncul/hilang dengan **fade** (200ms, ⚠PT-36).
- Ikon kontrol putih dengan kontras terjaga di atas video (⚠PT-08 untuk scrim).

### 7.2 Diagram layout overlay (posisi bawaan)

**Landscape**

```
┌──────────────────────────────────────────────────────────────┐
│ [←] [🔒]  Judul video…                [⟳] [1.0x] [▭rasio] [⋮]│  ← Bar atas (⚠PT-04, ⚠PT-05)
│                                                              │
│                 Area video (memenuhi layar)                  │
│                                                              │
│ ══════════════●══════════════════════   01:23 / 23:51        │  ← Seek bar + waktu
│                  [⏮]   [▶/⏸]   [⏭]                    [📷]  │  ← Kontrol bawah
└──────────────────────────────────────────────────────────────┘
```

**Portrait (letterbox)**

```
┌──────────────────────────┐
│ [←][🔒] Judul… [⟳][1.0x][▭][⋮]│  ← Bar atas
│                          │
│ ┌──────────────────────┐ │
│ │ video, rasio asli    │ │  ← lebar penuh, pita hitam atas/bawah
│ └──────────────────────┘ │
│                          │
│ ═════●════════ 01:23/23:51│
│    [⏮] [▶/⏸] [⏭]    [📷]│
└──────────────────────────┘
```

Aturan layout: tombol di tepi atas dan bawah layar agar terjangkau di kedua orientasi; di portrait kontrol berada di tepi layar (di atas pita hitam), bukan menempel pada video; di landscape video memenuhi layar. Posisi vertikal video di portrait: ⚠PT-16.

### 7.3 Posisi bawaan tombol (slot system)

Mengikuti PRD (FR-CST-02) dan nilai bawaan model konfigurasi:

| Slot | Isi bawaan |
|---|---|
| Kiri atas | **LOCK** (kunci) |
| Kanan atas (kiri→kanan) | **ORIENTATION** (rotasi), **SPEED** (kecepatan), **SCREEN_RATIO** (rasio) |
| Tengah (kiri→kanan) | **PREVIOUS**, **PLAY_PAUSE**, **NEXT** |
| Kiri bawah | kosong |
| Kanan bawah | **CAPTURE** |
| Slot kosong | **EMPTY** (placeholder) |

### 7.4 Tampilan tiap tombol

| Slot | Tampilan |
|---|---|
| PLAY_PAUSE | Ikon Play saat jeda; ikon Pause saat berjalan |
| PREVIOUS / NEXT | Ikon SkipPrevious / SkipNext |
| LOCK | Ikon Lock bila terkunci; LockOpen bila tidak |
| CAPTURE | Ikon CameraAlt |
| SPEED | Tombol teks "`1.0x`" (kecepatan aktif); ketuk membuka pemilih kecepatan (⚠PT-06) |
| SCREEN_RATIO | Ikon AspectRatio; mengganti fit ↔ fill |
| **ORIENTATION** | Ikon sesuai mode + **label mode kecil**: `ScreenRotation` (Otomatis), `StayCurrentLandscape` (Landscape), `StayCurrentPortrait` (Portrait) |

**Tombol rotasi (ORIENTATION)** di kanan atas, di samping kecepatan dan rasio:
- Ikon + label selalu menunjukkan **mode aktif**.
- Setiap ketukan berpindah mode dengan siklus **Otomatis → Landscape → Portrait → Otomatis**; setiap pergantian menampilkan **label sesaat** di layar: "Otomatis" / "Landscape" / "Portrait" (⚠PT-17).
- Petunjuk kecil opsional saat rotasi perangkat terkunci pada mode Otomatis: "Rotasi HP terkunci — ketuk tombol rotasi" (opsional di blueprint; ⚠PT-17).
- Logika matriks orientasi ada di Blueprint-Workflow.md (§A7).

### 7.5 Seek bar kustom
- **Bukan** slider M3 bawaan; dibuat kustom agar warna dapat diatur penuh. Datar (tanpa bayangan) karena berada di atas video.
- *Track* latar: warna sekunder redup. *Track* progres: **warna aksen** tema. *Thumb* dapat diseret.
- Saat diseret, thumb **membesar** (animasi skala, 150ms).
- Penanda waktu `posisi / total`. Format `mm:ss` bila < 1 jam, `HH:mm:ss` bila ≥ 1 jam (mis. "01:23:45 / 23:51:00").
- Saat gestur seek horizontal aktif, bar menampilkan **posisi pratinjau**, bukan posisi sebenarnya.

### 7.6 Judul video (mengikuti TitleConfig)

| Properti | Rentang | Bawaan |
|---|---|---|
| Ukuran font | **10–22 sp** | 14 sp |
| Jumlah baris | **1 atau 2** | 1 |
| Tebal | ya/tidak | tidak |
| Marquee (teks berjalan) | ya/tidak | **ya** |
| Warna | bebas (swatches + HEX) | putih `#FFFFFF` |

Teks terlalu panjang: ellipsis di akhir; dengan marquee aktif teks berjalan (⚠PT-12). Keluarga huruf judul: **Inter** (bobot mengikuti toggle Tebal di atas) — dipilih untuk keterbacaan nama file di atas video pada ukuran kecil dan saat berjalan; lihat PT-27.

### 7.7 Indikator gestur

| Gestur | Zona | Indikator |
|---|---|---|
| Seret vertikal | Sepertiga **kiri** | Ikon kecerahan + persentase |
| Seret vertikal | Sepertiga **kanan** | Ikon volume + persentase |
| Seret horizontal | **Tengah** | Teks selisih "**+00:30**" / "**-00:30**" + seek bar menampilkan posisi pratinjau; seek sungguhan saat jari diangkat |
| Ketuk dua kali | Kiri / kanan | Animasi ripple di zona yang diketuk: "**⏪ 10s**" (kiri), "**10s ⏩**" (kanan); angka mengikuti langkah seek |
| Ketuk satu kali | Di mana saja | Tampil/sembunyikan kontrol |

Posisi dan lama tampil indikator: ⚠PT-11, ⚠PT-36. Saat kunci aktif, semua gestur diabaikan kecuali membuka kunci (⚠PT-15).

### 7.8 Perilaku tampil/sembunyi kontrol
- Kontrol hilang otomatis setelah **±3 detik** tanpa interaksi saat video **berjalan**; timer di-reset setiap interaksi.
- Saat **jeda**, kontrol **tetap tampil**.
- Ketuk layar menampilkan/menyembunyikan; transisi **fade**.

### 7.9 Konfirmasi capture
Pesan singkat (snackbar) setelah berhasil: "**Frame berhasil disimpan ke Galeri**". Teks kegagalan: ⚠PT-10.

### 7.10 Portrait vs landscape: ringkasan visual

| Aspek | Portrait | Landscape |
|---|---|---|
| Video | Rasio asli (letterbox), lebar penuh | Memenuhi layar |
| Kontrol | Tetap terjangkau, di tepi layar | Di tepi layar |
| Perubahan | Hanya tata letak yang menyesuaikan; video tidak mulai ulang | sama |

---

## 8. Layar Kustomisasi (4 Editor)

Kerangka: **baris tab dengan 4 tab**: **Judul · Tombol · Tema · Whitelist**. Layar tampil portrait (⚠PT-22). Setiap perubahan **langsung tersimpan dan langsung terlihat** di pratinjau. Permukaan memakai Soft UI (Terang) / tonal (Gelap, AMOLED): panel = Surface radius XL 24dp, input dan trek = inset, tombol = SM + efek tekan.

Kontrol yang dipakai di empat editor:

| Kontrol | Spesifikasi visual |
|---|---|
| Slider | Trek inset (Surface Dark), bagian terisi aksen, thumb terangkat; nilai terbaca di samping |
| Segmented button | Radius MD; segmen terpilih = isian aksen lembut + ikon centang (bukan hanya bayangan) |
| Switch | Trek 48×28dp, knob 20dp putih; mati = Surface Dark, hidup = aksen; geser 150–200ms; area sentuh 48dp |
| Swatches | Lingkaran 40dp, area sentuh 48dp; terpilih = cincin + ikon centang |
| Input HEX | Tinggi 48dp, radius 14–16dp, inset, label terlihat |
| Tombol sekunder | Surface, bayangan SM, label Manrope 14sp |

### 8.1 Tab Judul
- **Slider** ukuran font 10–22 sp, langkah 0,5.
- **Segmented button** "1 Baris" / "2 Baris".
- **Toggle** "Teks Tebal". **Toggle** "Marquee" (teks berjalan).
- **Pemilih warna**: swatches + input HEX kustom.
- **Pratinjau langsung** di bawah kontrol: kartu berlatar gelap datar dengan judul bergaya sesuai pengaturan saat ini.

### 8.2 Tab Tombol
- **Pratinjau mini** tata letak player (referensi, tidak interaktif).
- Di bawahnya **grid slot** dengan tombol yang menempati tiap slot.
- Mekanisme **tap-tukar**: ketuk slot pertama → ter-*highlight* (isian aksen lembut + garis aksen); ketuk slot kedua → posisi kedua tombol **ditukar**; seleksi di-*reset*; pratinjau diperbarui seketika.
- Tombol **"Reset ke Default"** di bagian bawah (memulihkan susunan §7.3).
- Slot kosong di editor: ⚠PT-13.

### 8.3 Tab Tema
- **Segmented button 4 mode**: Sistem · Terang · Gelap · AMOLED.
- **Toggle** "Gunakan Dynamic Color" (hanya Android 12 ke atas) (⚠PT-14).
- **Swatches** aksen preset (**8–10**; kandidat warna di §2.1, ⚠PT-07).
- Tombol **"Custom"** → pemilih warna dengan input **HEX**.
- **Pratinjau**: kartu mini yang menunjukkan warna `background`, `surface`, dan `primary`.

### 8.4 Tab Whitelist
- **Switch** di atas: "**Mode Whitelist Saja**" ON/OFF.
- Daftar semua folder dengan **checkbox** di kiri (batas checkbox kontras ≥ 3:1).
- Mode **OFF**: daftar tampak abu-abu (nonaktif) tetapi tetap terlihat, dengan penanda selain warna redup.
- Mode **ON**: hanya folder tercentang yang tampil di tab Folder.
- Teks info: "**Folder yang dicentang akan tampil di halaman Folder**".

---

## 9. Tabel Nilai Konkret Proyek

| Parameter | Nilai | Bawaan |
|---|---|---|
| Rasio thumbnail | 16:9 | — |
| Radius thumbnail | 8dp | — |
| Kolom grid folder | 2 | — |
| Jarak antar item grid | 8dp | — |
| Baris nama video / nama folder | maks 2 / maks 2 | — |
| Strip thumbnail playlist | 2–3 thumbnail, bertumpuk offset tipis | — |
| Tab navigasi | 3 (Video, Folder, Daftar Putar) | Video |
| Tema bawaan | Sistem | Sistem |
| Aksen bawaan | `0xFF6655F5` (Terang) / `0xFF8B7FFF` (Gelap) | `0xFF6655F5` |
| Dynamic color | opsional, Android 12+ | mati |
| AMOLED background/surface | `0xFF000000` | — |
| Ukuran font judul | 10–22 sp, langkah 0,5 | 14 sp |
| Baris judul | 1 atau 2 | 1 |
| Judul tebal | ya/tidak | tidak |
| Marquee judul | ya/tidak | ya |
| Warna judul | `0xFFFFFFFF` | putih |
| Swatches aksen preset | 8–10 | — |
| Auto-hide kontrol | ±3 detik (3.000 ms) | 3 detik |
| Langkah seek ketuk dua kali | 10 detik (10.000 ms) | 10 detik |
| Zona gestur | kiri < 33%, tengah 33–67%, kanan > 67% lebar layar | — |
| Kecerahan (rentang efektif) | 1% – 100% | ikut sistem |
| Sensitivitas seek horizontal | 1,5 (nilai internal; ⚠PT-18) | 1,5 |
| Mode orientasi | Otomatis / Landscape / Portrait | Otomatis |
| Siklus tombol rotasi | Otomatis → Landscape → Portrait → Otomatis | — |
| Label mode sesaat | "Otomatis" / "Landscape" / "Portrait" | — |
| Susunan tombol bawaan | §7.3 | — |
| Format waktu seek bar | `mm:ss` (<1 jam) / `HH:mm:ss` (≥1 jam) | — |
| Format durasi total playlist | "1j 23m" / "23 mnt" | — |
| Format ukuran file | "215,94 MB" / "1,2 GB" | — |
| Teks konfirmasi capture | "Frame berhasil disimpan ke Galeri" | — |
| Filter folder | Semua / Whitelist saja | Semua |
| Radius kartu / dialog | 20dp / 24dp | — |
| Bayangan kartu playlist / kartu folder | MD / SM | — |
| Tinggi bar pencarian, input | 48dp | — |
| Switch (trek × knob) | 48×28dp × 20dp | — |
| Swatches | 40dp (sentuh 48dp) | — |
| Target sentuh minimum | 48×48dp | — |
| Font | Manrope (judul) + Inter (isi) | — |
| Ukuran ikon | 16 / 20 / 24dp | — |
| Durasi gerak | 100 / 150 / 200 / 300ms | — |
| Shimmer / spinner | 1500ms / 700ms | — |

---

## 10. Keadaan Visual per Layar

> Struktur mengikuti PRD (FR-HOME-05). Teks di dalam tanda kutip pada keadaan kosong/error/tanpa izin adalah **usulan** karena sumber tidak menetapkan redaksinya (⚠PT-10). Ikon besar pada keadaan kosong ditempatkan dalam lingkaran kontainer aksen lembut (padanan Primary Soft).

### 10.1 Beranda (semua tab)

| Keadaan | Tampilan |
|---|---|
| **Memuat** | Tab Video: beberapa baris shimmer (thumbnail 16:9 + 2 baris teks). Tab Folder: kartu shimmer dalam grid 2 kolom. Navigation bar tetap tampil |
| **Berisi** | Daftar/grid sesuai §4–§6 |
| **Kosong — Video** | Ikon video besar redup + judul + satu kalimat penjelasan; tanpa aksi |
| **Kosong — Folder** | Ikon folder besar redup + judul + penjelasan. Termasuk bila filter whitelist tidak meloloskan folder apa pun |
| **Kosong — Daftar Putar** | **Ilustrasi** + judul + ajakan menekan FAB `+` |
| **Kosong — Pencarian** | Ikon pencarian + "tidak ada hasil" + teks kueri |
| **Tanpa izin** | **Layar penuh khusus**, bukan daftar kosong: ikon kunci/video, judul, penjelasan mengapa akses video dibutuhkan, tombol utama bergaya aksen "**Izinkan Akses**". Navigation bar tidak menampilkan konten |
| **Error** | Ikon peringatan (isian warna Peringatan/Error + label teks) + pesan singkat + tombol "Coba Lagi" |

### 10.2 Detail playlist

| Keadaan | Tampilan |
|---|---|
| Berisi | Daftar entri + handle seret; Play All & Shuffle aktif |
| Kosong | Penjelasan bahwa playlist belum berisi video; Play All & Shuffle **nonaktif** (redup + tetap terbaca) |

### 10.3 Player

| Keadaan | Tampilan |
|---|---|
| **Memuat** | Latar hitam + indikator memuat di tengah; kontrol tampil |
| **Berjalan** | Video + overlay kontrol (hilang otomatis ±3 detik); ikon Pause |
| **Jeda** | Video diam; kontrol **tetap tampil**; ikon Play |
| **Terkunci** | Gestur dan tombol dibekukan; hanya ikon kunci yang berfungsi (⚠PT-15) |
| **Selesai** | Kontrol tampil; ikon Play (perilaku lanjutan: lihat Blueprint-Workflow.md) |
| **Error** | Latar hitam + ikon peringatan + pesan "video tidak dapat diputar" + aksi kembali |

### 10.4 Kustomisasi

| Keadaan | Tampilan |
|---|---|
| Memuat | Shimmer ringan pada tab aktif |
| Berisi | Tab 4 editor + pratinjau |
| Whitelist tanpa folder | Teks kosong di dalam daftar |

---

## 11. Catatan & Pertanyaan Terbuka

### 11.1 Keputusan yang perlu Anda ambil (dampak visual besar)

- **PT-25 — Aksen bawaan dan kontras. [SELESAI — diputuskan 3 November 2026: opsi (a)]** Aksen bawaan diganti dari biru Material blueprint `#2196F3` menjadi lavender Primary referensi: `#6655F5` untuk tema Terang dan `#8B7FFF` untuk tema Gelap/AMOLED (varian diterangkan dari Neumorphism.md versi Android §2.1). Dasar keputusan adalah hitungan §2.3: `#2196F3` di atas Background terang hanya 2,63:1 (gagal batas 3:1 untuk ikon folder, indikator tab, dan progres) dan teks putih di atasnya 3,12:1 (gagal 4,5:1 untuk teks tombol kecil), sedangkan `#6655F5` memberi 4,23:1 pada Background dan 5,02:1 dengan teks putih. Biru lama tetap terdokumentasi di §2.3 sebagai pembanding; sebagai pilihan pengguna, peran biru pada swatches diwakili Blue `#438BFA`. Nilai bawaan aksen di blueprint (ThemeConfig & PREF_ACCENT_COLOR) diselaraskan pada revisi yang sama.
- **PT-26 — Netral tema Gelap. [SELESAI — diputuskan 3 November 2026]** Nilai netral tema Gelap diisi dari Neumorphism.md versi Android (§2.2–§2.3 dokumen itu): background `#191B2B`, surface `#1C1E30`, Surface Light `#262842`, Surface Dark `#12131F`, Surface Pressed `#23253E`, teks `#F1F2FA` / `#B4B9D4`, border `#2C2F4D`, divider `#272A44` — lihat §2.2 dokumen ini. Karakter permukaan Gelap: Soft UI ikut berlaku memakai pasangan bayangan versi gelap (§2.6b), bukan tonal polos. AMOLED tetap varian datar tanpa bayangan (garis `#1E1E28`).
- **PT-27 — Font. [SELESAI — diputuskan 3 November 2026]** Font **dibundel di dalam aplikasi**: **Manrope** (judul layar, label navigasi, tombol) + **Inter** (isi, metadata, judul player, dan semua bacaan angka dengan `tnum`), keduanya font variabel berlisensi OFL. Aplikasi offline-only sehingga tidak ada unduhan font saat runtime. Pembagian peran menurut kekuatan masing-masing: Inter sebagai font kerja utama (x-height tinggi, terbaca di 12–14 sp, angka tabular untuk durasi/kecepatan/persentase), Manrope untuk karakter judul yang membulat dan selaras Soft UI. Judul player sengaja memakai Inter (nama file di atas video, 10–22 sp, marquee). Yang ditimbang dan ditolak: font sistem (karakter hilang) dan Montserrat (paling lebar, paling lemah di ukuran kecil, karakter branding). Rincian di §2.7.

### 11.2 Pertentangan antar sumber (diselesaikan)

| # | Pertentangan | Keputusan di file ini |
|---|---|---|
| PT-01 | Label tab ketiga: PRD FR-HOME-01 "**Daftar Putar**"; blueprint §5 "**Putar**" | Pakai **"Daftar Putar"** (PRD). Konfirmasi bila terlalu panjang untuk bilah bawah |
| PT-02 | Posisi tombol: diagram blueprint §5 menaruh kecepatan di bawah, kunci di kiri bawah, tanpa rasio; PRD FR-CST-02 dan nilai bawaan model konfigurasi menaruh kunci di kiri atas dan rotasi/kecepatan/rasio di kanan atas | Pakai **PRD** (§7.3). Diagram §5 dianggap ilustrasi lama |
| PT-03 | Ukuran file: blueprint §5 "kanan bawah item"; blueprint §7 menaruhnya di kolom teks | Pakai **§5** (kanan bawah item) |
| PT-28 | Ikon: referensi menyarankan Lucide (outline); blueprint memakai Material Icons Rounded dan menetapkan ikon terisi/outline per tab | Pakai **Material Rounded** (blueprint). Ukuran 16/20/24dp diambil dari referensi |
| PT-29 | Radius thumbnail 8dp (blueprint) tidak ada di skala referensi (6/10/14/20/24) | Pakai **8dp** (nilai blueprint eksplisit) |
| PT-30 | PRD/blueprint: ripple pada semua yang dapat diketuk; Motion.md: tekan = skala 0,97 + bayangan inset | Dipakai **keduanya** pada permukaan Soft UI (Terang); hanya ripple pada konten datar dan player. Konfirmasi bila terasa berlebihan |
| PT-31 | Lingkup Soft UI: referensi mengajarkan efek pada card, tombol, input; PRD meminta overlay transparan di player, blueprint meminta daftar video padat; jarak grid 8dp (blueprint) terlalu rapat untuk bayangan MD | Soft UI **tidak** dipakai di player dan daftar video (datar); kartu grid memakai bayangan **SM**; jarak 8dp dipertahankan |
| PT-32 | Referensi memakai Text Muted `#858CAD` untuk placeholder/metadata dan beberapa warna semantik untuk teks; kontrasnya 1,7–2,8:1 (§2.3), padahal referensi sendiri (§10) mensyaratkan AA | Metadata dan placeholder memakai **Text Secondary**; warna semantik hanya sebagai isian/ikon dengan label teks |
| PT-33 | Motion.md memuat hover, scroll reveal, stagger, transisi halaman, tooltip, accordion, scroll progress, sticky header | **Tidak diadopsi** (§2.9): tidak ada padanan di layar sentuh atau tidak ada di PRD/blueprint |

### 11.3 Celah (tidak diputuskan; butuh keputusan pemilik)

- **PT-04** — Tata letak bar atas player: posisi tombol Kembali, Judul, dan menu `⋮` relatif terhadap slot kiri atas (LOCK) dan kanan atas tidak ditetapkan; diagram §7.2 adalah usulan.
- **PT-05** — Isi menu `⋮` di bar atas player tidak ditetapkan (hanya ada di diagram blueprint). Blueprint menyebut Kustomisasi dapat dibuka dari player; PRD tidak.
- **PT-06** — Daftar nilai kecepatan dan bentuk pemilih kecepatan.
- **PT-07** — Warna swatches preset (8–10) tidak disebutkan sumber utama; §2.1 menawarkan kandidat dari referensi.
- **PT-08** — Opasitas lencana durasi dan ada/tidaknya scrim gradien pada overlay kontrol (DESIGN.md hanya memberi kisaran umum).
- **PT-09** — Format teks lencana durasi (`mm:ss` vs "23 mnt"); "1j 23m"/"23 mnt" hanya jelas untuk total durasi playlist.
- **PT-10** — Redaksi dan ikon keadaan kosong/error/tanpa izin (selain "Izinkan Akses" dan teks konfirmasi capture), termasuk teks saat capture gagal.
- **PT-11** — Posisi indikator gestur (kecerahan/volume/seek) di layar.
- **PT-12** — Interaksi marquee dengan "2 baris": blueprint tidak membedakan; tampilan bila keduanya aktif belum jelas.
- **PT-13** — Editor tombol: kiri bawah kosong secara bawaan dan tidak ada slot EMPTY dalam susunan bawaan, sehingga belum jelas bagaimana tombol dipindah ke kiri bawah.
- **PT-14** — Bila Dynamic Color aktif, apakah swatches/HEX dinonaktifkan atau disembunyikan.
- **PT-15** — Saat kunci aktif: bagaimana ikon kunci tetap terlihat/terjangkau ketika kontrol tersembunyi, apakah kontrol tampil lagi setelah dibuka, dan perilaku tombol rotasi saat terkunci (PRD: tombol dibekukan).
- **PT-16** — Posisi vertikal video di portrait: blueprint §10F menyebut "video letterbox di atas"; PRD hanya "letterbox". Spek ini memakai posisi tengah sampai ada keputusan.
- **PT-17** — Lama tampil label mode orientasi sesaat; petunjuk "Rotasi HP terkunci" opsional di blueprint dan tidak ada di PRD.
- **PT-18** — PRD FR-GST-03 menyebut langkah seek "dapat diatur" (10 detik) dan blueprint menyebut sensitivitas seek "bisa dikustomisasi", tetapi Kustomisasi hanya punya 4 tab tanpa editor untuk keduanya.
- **PT-19** — Daftar aplikasi asal yang dikenali (selain contoh WhatsApp) dan sumber ikon lencananya.
- **PT-20** — Bentuk dialog "Ganti Nama", "Properti", "Tambah ke Playlist", serta pemicu visual hapus entri/hapus playlist/ganti nama playlist.
- **PT-21** — Ikon dan posisi pintu masuk ke layar Kustomisasi di Beranda.
- **PT-22** — Orientasi Beranda dan Kustomisasi: PRD tidak menyebutnya; blueprint mengunci keduanya portrait. Diikuti blueprint (tidak bertentangan dengan keputusan terkunci, yang melarang kunci landscape pada player).
- **PT-23** — Tampilan "end screen" akhir playlist (blueprint: "end screen atau kembali ke Beranda").
- **PT-24** — Tombol **Shuffle** ada di blueprint tetapi tidak di PRD; dicantumkan karena tidak bertentangan, perlu konfirmasi; perilaku visualnya belum ditetapkan.
- **PT-34** — Gradien tombol utama: referensi memakai Primary Gradient `#8172FF → #5946EF`; aturan turunan gradien dan tone "Light/Soft/Dark" untuk aksen kustom belum ada. Sampai diputuskan, tombol utama memakai isian aksen solid. Ujung terang gradien (`#8172FF`) di bawah 4,5:1 dengan teks putih.
- **PT-35** — Pemetaan elemen → skala tipografi (§2.7) adalah usulan; juga keluarga huruf judul player.
- **PT-36** — Durasi gerak elemen yang tidak disebut sumber: fade kontrol, crossfade tab, lama tampil indikator gestur dan penanda ketuk dua kali (angka §2.9 adalah usulan dari token referensi).
- **PT-37** — Pengurangan animasi: perilaku marquee dan shimmer saat pengguna mengurangi animasi di sistem (usulan di §2.9) tidak ada di PRD/blueprint.
- **PT-38** — Konversi satuan: referensi memakai px (web); dokumen ini membacanya sebagai dp (teks sebagai sp) tanpa penskalaan. Konfirmasi bila ingin skala berbeda.
- **PT-39** — Dimensi yang tidak ditetapkan sumber: tinggi baris item video, ukuran ikon folder, ukuran dan offset strip thumbnail playlist, ukuran lencana aplikasi asal.
