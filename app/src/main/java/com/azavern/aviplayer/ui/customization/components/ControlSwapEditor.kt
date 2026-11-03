package com.azavern.aviplayer.ui.customization.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azavern.aviplayer.R
import com.azavern.aviplayer.data.model.ControlLayoutConfig
import com.azavern.aviplayer.data.model.ControlSlot
import com.azavern.aviplayer.ui.customization.CustomizationViewModel.SlotPosition

/**
 * Editor posisi tombol player (Milestone 7): kelima area layout
 * digambar sebagai baris chip slot. Ketuk satu slot untuk memilih,
 * ketuk slot lain untuk menukar — perubahan langsung tersimpan dan
 * player menerapkannya seketika.
 *
 * Area yang daftarnya kosong tetap menampilkan satu sel "Kosong"
 * sebagai target tukar, supaya tombol bisa dipindah ke sana
 * (sel kosong itu menjadi entri EMPTY sungguhan di config).
 */
@Composable
fun ControlSwapEditor(
    layout: ControlLayoutConfig,
    selectedSlotA: SlotPosition?,
    onSlotTapped: (SlotPosition) -> Unit,
    onResetLayout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = stringResource(R.string.controls_editor_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SlotAreaRow(
            label = stringResource(R.string.area_top_left),
            slots = listOf(layout.topLeftSlot),
            positionOf = { SlotPosition.TopLeft },
            selectedSlotA = selectedSlotA,
            onSlotTapped = onSlotTapped
        )
        SlotAreaRow(
            label = stringResource(R.string.area_top_right),
            slots = layout.topRightSlots.ifEmpty { listOf(ControlSlot.EMPTY) },
            positionOf = { index -> SlotPosition.TopRight(index) },
            selectedSlotA = selectedSlotA,
            onSlotTapped = onSlotTapped
        )
        SlotAreaRow(
            label = stringResource(R.string.area_center),
            slots = layout.centerSlots.ifEmpty { listOf(ControlSlot.EMPTY) },
            positionOf = { index -> SlotPosition.Center(index) },
            selectedSlotA = selectedSlotA,
            onSlotTapped = onSlotTapped
        )
        SlotAreaRow(
            label = stringResource(R.string.area_bottom_left),
            slots = layout.bottomLeftSlots.ifEmpty { listOf(ControlSlot.EMPTY) },
            positionOf = { index -> SlotPosition.BottomLeft(index) },
            selectedSlotA = selectedSlotA,
            onSlotTapped = onSlotTapped
        )
        SlotAreaRow(
            label = stringResource(R.string.area_bottom_right),
            slots = layout.bottomRightSlots.ifEmpty { listOf(ControlSlot.EMPTY) },
            positionOf = { index -> SlotPosition.BottomRight(index) },
            selectedSlotA = selectedSlotA,
            onSlotTapped = onSlotTapped
        )

        OutlinedButton(
            onClick = onResetLayout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.controls_editor_reset))
        }
    }
}

@Composable
private fun SlotAreaRow(
    label: String,
    slots: List<ControlSlot>,
    positionOf: (Int) -> SlotPosition,
    selectedSlotA: SlotPosition?,
    onSlotTapped: (SlotPosition) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            slots.forEachIndexed { index, slot ->
                val position = positionOf(index)
                SlotChip(
                    slot = slot,
                    selected = position == selectedSlotA,
                    onClick = { onSlotTapped(position) }
                )
            }
        }
    }
}

@Composable
private fun SlotChip(
    slot: ControlSlot,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            slotIcon(slot)?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = stringResource(slotLabelRes(slot)),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

private fun slotIcon(slot: ControlSlot): ImageVector? = when (slot) {
    ControlSlot.PLAY_PAUSE -> Icons.Rounded.PlayArrow
    ControlSlot.PREVIOUS -> Icons.Rounded.SkipPrevious
    ControlSlot.NEXT -> Icons.Rounded.SkipNext
    ControlSlot.LOCK -> Icons.Rounded.Lock
    ControlSlot.ORIENTATION -> Icons.Rounded.ScreenRotation
    ControlSlot.CAPTURE -> Icons.Rounded.PhotoCamera
    ControlSlot.SPEED -> Icons.Rounded.Speed
    ControlSlot.SCREEN_RATIO -> Icons.Rounded.AspectRatio
    ControlSlot.EMPTY -> null
}

private fun slotLabelRes(slot: ControlSlot): Int = when (slot) {
    ControlSlot.PLAY_PAUSE -> R.string.slot_play_pause
    ControlSlot.PREVIOUS -> R.string.slot_previous
    ControlSlot.NEXT -> R.string.slot_next
    ControlSlot.LOCK -> R.string.slot_lock
    ControlSlot.ORIENTATION -> R.string.slot_orientation
    ControlSlot.CAPTURE -> R.string.slot_capture
    ControlSlot.SPEED -> R.string.slot_speed
    ControlSlot.SCREEN_RATIO -> R.string.slot_screen_ratio
    ControlSlot.EMPTY -> R.string.slot_empty
}
