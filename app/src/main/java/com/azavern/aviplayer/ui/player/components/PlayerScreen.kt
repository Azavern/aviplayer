package com.azavern.aviplayer.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.azavern.aviplayer.data.model.ControlSlot
import com.azavern.aviplayer.data.model.PlaybackMode
import com.azavern.aviplayer.player.core.PlayerState
import com.azavern.aviplayer.ui.player.CaptureResult
import com.azavern.aviplayer.ui.player.PlayerUiState
import com.azavern.aviplayer.utils.Constants
import kotlinx.coroutines.delay

/**
 * Layar Player — mengikuti referensi Veo (2026-11-04): permukaan video
 * memenuhi layar, bar atas dan gugus kontrol bawah MELAYANG di atasnya
 * (overlay), semua datar tanpa latar di balik tombol.
 *
 * Kontrol menyembunyikan diri (perintah Veo 2026-11-04): saat video
 * berjalan, sesudah 3 detik tanpa interaksi bar atas meluncur ke
 * atas dan gugus bawah meluncur ke bawah keluar layar. Saat jeda
 * kontrol selalu tampil. Ketuk sekali di area bukan tombol (lewat
 * GestureOverlay) membalik tampil/sembunyi kapan pun.
 *
 * Mode TERKUNCI (fitur LOCK, Veo 2026-11-07): semua kontrol dan
 * gesture mati; yang tersisa hanya tombol melayang di kiri tengah
 * layar (area layout kiri tengah, bawaan LOCK) yang berubah menjadi
 * tombol buka kunci — tampil sebentar sesudah mengunci lalu
 * memudar, dan muncul lagi setiap layar diketuk.
 *
 * Stateless untuk state player: semua state dari [uiState], semua
 * aksi lewat callback. Visibilitas kontrol adalah state lokal layar.
 */
@Composable
fun PlayerScreen(
    uiState: PlayerUiState,
    player: ExoPlayer,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onRotateClick: () -> Unit,
    onBackClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSeekPreview: (Long) -> Unit,
    onSeekCommit: () -> Unit,
    onSeekClear: () -> Unit,
    onGestureSeekForward: () -> Unit,
    onGestureSeekBackward: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onCaptureClick: () -> Unit,
    onCaptureResultConsumed: () -> Unit,
    onMuteClick: () -> Unit,
    onLockClick: () -> Unit,
    onPlaybackModeSelected: (PlaybackMode) -> Unit,
    modifier: Modifier = Modifier
) {
    // Lencana hasil capture tampil ±1,5 detik, lalu state dibersihkan
    // lewat onCaptureResultConsumed supaya tidak muncul dua kali.
    LaunchedEffect(uiState.captureResult) {
        if (uiState.captureResult != null) {
            delay(1500)
            onCaptureResultConsumed()
        }
    }

    // ---- Sembunyi otomatis kontrol ----
    var controlsVisible by remember { mutableStateOf(true) }
    var lastInteractionAt by remember { mutableLongStateOf(System.currentTimeMillis()) }
    fun noteInteraction() {
        lastInteractionAt = System.currentTimeMillis()
    }

    // Video berhenti/jeda → kontrol selalu tampil.
    LaunchedEffect(uiState.isPlaying) {
        if (!uiState.isPlaying) controlsVisible = true
    }
    // Sedang berjalan + kontrol tampil → sembunyikan sesudah jeda
    // tanpa interaksi; setiap interaksi mengulang hitungan dari awal.
    LaunchedEffect(
        uiState.isPlaying, controlsVisible, lastInteractionAt, uiState.seekPreviewMs
    ) {
        if (uiState.isPlaying && controlsVisible) {
            delay(Constants.AUTO_HIDE_CONTROLS_DELAY_MS)
            controlsVisible = false
        }
    }

    // ---- Mode terkunci (LOCK) ----
    // Tombol buka kunci tampil sesudah mengunci, memudar sesudah
    // 3 detik, dan muncul lagi setiap layar diketuk (lapisan
    // penangkap ketuk di bawah).
    var lockButtonVisible by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLocked) {
        if (uiState.isLocked) lockButtonVisible = true
    }
    LaunchedEffect(uiState.isLocked, lockButtonVisible) {
        if (uiState.isLocked && lockButtonVisible) {
            delay(3_000)
            lockButtonVisible = false
        }
    }

    val controlActions = PlayerControlActions(
        onPlayPause = { noteInteraction(); onPlayPauseClick() },
        onPrevious = { noteInteraction(); onPreviousClick() },
        onNext = { noteInteraction(); onNextClick() },
        onRotate = { noteInteraction(); onRotateClick() },
        onCapture = { noteInteraction(); onCaptureClick() },
        onSpeed = { noteInteraction(); onSpeedClick() },
        onMute = { noteInteraction(); onMuteClick() },
        onLock = { noteInteraction(); onLockClick() }
    )
    val layout = uiState.playerUiConfig.controlLayoutConfig

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VideoSurface(
            player = player,
            modifier = Modifier.fillMaxSize()
        )

        // Lapisan gesture Milestone 5: di atas video, di bawah kontrol,
        // sehingga tombol tetap menerima sentuhannya sendiri. Saat
        // TERKUNCI lapisan ini diganti penangkap ketuk polos yang
        // satu-satunya fungsinya memunculkan lagi tombol buka kunci.
        if (uiState.isLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { lockButtonVisible = true }
                    }
            )
        } else {
            GestureOverlay(
                uiState = uiState,
                onBrightnessChange = { noteInteraction(); onBrightnessChange(it) },
                onVolumeChange = { noteInteraction(); onVolumeChange(it) },
                onSeekPreview = { noteInteraction(); onSeekPreview(it) },
                onSeekCommit = { noteInteraction(); onSeekCommit() },
                onSeekClear = onSeekClear,
                onSeekForward = { noteInteraction(); onGestureSeekForward() },
                onSeekBackward = { noteInteraction(); onGestureSeekBackward() },
                onTogglePlayPause = { noteInteraction(); onTogglePlayPause() },
                onSingleTap = {
                    controlsVisible = !controlsVisible
                    noteInteraction()
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        when (val state = uiState.playerState) {
            PlayerState.Loading -> CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
            is PlayerState.Error -> Text(
                text = state.message,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
            )
            else -> Unit
        }

        // Bar atas: meluncur ke atas keluar layar saat kontrol sembunyi.
        AnimatedVisibility(
            visible = controlsVisible && !uiState.isLocked,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TitleDisplay(
                title = uiState.videoTitle,
                titleConfig = uiState.playerUiConfig.titleConfig,
                topRightSlots = layout.topRightSlots,
                uiState = uiState,
                actions = controlActions,
                onBackClick = onBackClick,
                playbackMode = uiState.playbackMode,
                onPlaybackModeSelected = onPlaybackModeSelected
            )
        }

        // Notifikasi hasil capture (perintah Veo 2026-11-04, mengganti
        // Snackbar kotak teks): lingkaran berisi centang (atau silang
        // bila gagal) yang menempel di tepi kanan layar, nyaris keluar
        // — hanya disisakan margin beberapa piksel. Meluncur masuk
        // dari kanan dan menghilang sendiri.
        AnimatedVisibility(
            visible = uiState.captureResult != null,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.captureResult is CaptureResult.Success) {
                        Icons.Filled.Check
                    } else {
                        Icons.Filled.Close
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Tombol melayang kiri tengah (area layout kiri tengah;
        // bawaan LOCK). Tidak terkunci: isinya slot layout dan ia
        // ikut tampil/sembunyi bersama kontrol lain. Terkunci: ia
        // satu-satunya yang tersisa dan selalu tombol LOCK (buka
        // kunci) di posisi ini, apa pun isi slot layout-nya.
        AnimatedVisibility(
            visible = if (uiState.isLocked) lockButtonVisible else controlsVisible,
            enter = slideInHorizontally { -it } + fadeIn(),
            exit = slideOutHorizontally { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
        ) {
            ControlSlotButton(
                slot = if (uiState.isLocked) ControlSlot.LOCK else layout.middleLeftSlot,
                uiState = uiState,
                actions = controlActions
            )
        }

        // Gugus bawah (seekbar + tombol): meluncur ke bawah keluar
        // layar saat kontrol sembunyi.
        AnimatedVisibility(
            visible = controlsVisible && !uiState.isLocked,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Seekbar digeser turun mendekati baris tombol (perintah
                // Veo 2026-11-07: ±50% dari jarak semula). Offset hanya
                // memindah gambar + area sentuhnya, tinggi gugus tidak
                // berubah; besarnya di Constants.PLAYER_SEEK_BAR_DROP_DP.
                SeekBarComponent(
                    positionMs = uiState.currentPositionMs,
                    durationMs = uiState.durationMs,
                    onSeekTo = { noteInteraction(); onSeekTo(it) },
                    previewPositionMs = uiState.seekPreviewMs,
                    modifier = Modifier.offset(
                        y = Constants.PLAYER_SEEK_BAR_DROP_DP.dp
                    )
                )
                PlayerControls(
                    layout = layout,
                    uiState = uiState,
                    actions = controlActions
                )
            }
        }
    }
}
