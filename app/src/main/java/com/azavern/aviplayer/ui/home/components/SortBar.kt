package com.azavern.aviplayer.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azavern.aviplayer.R
import com.azavern.aviplayer.data.model.SortConfig
import com.azavern.aviplayer.data.model.SortField

/**
 * Bar urutan daftar video (sorting global): satu tombol menu untuk
 * memilih bidang (Nama / Waktu / Ukuran / Durasi) dan satu tombol
 * ikon di sampingnya untuk membalik arah (naik/turun). Pilihan
 * tersimpan permanen lewat SettingsRepository; pengurutan sendiri
 * terjadi di VideoRepository — komponen ini hanya menampilkan
 * state dan meneruskan event.
 */
@Composable
fun SortBar(
    sortConfig: SortConfig,
    onFieldSelected: (SortField) -> Unit,
    onToggleDirection: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            TextButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Rounded.Sort, contentDescription = null)
                Text(
                    text = stringResource(R.string.sort_label, sortFieldLabel(sortConfig.field)),
                    modifier = Modifier.padding(start = 6.dp)
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                SortField.entries.forEach { field ->
                    DropdownMenuItem(
                        text = { Text(sortFieldLabel(field)) },
                        trailingIcon = if (field == sortConfig.field) {
                            { Icon(Icons.Filled.Check, contentDescription = null) }
                        } else {
                            null
                        },
                        onClick = {
                            menuExpanded = false
                            onFieldSelected(field)
                        }
                    )
                }
            }
        }
        IconButton(onClick = onToggleDirection) {
            Icon(
                imageVector = if (sortConfig.ascending) {
                    Icons.Filled.ArrowUpward
                } else {
                    Icons.Filled.ArrowDownward
                },
                contentDescription = stringResource(
                    if (sortConfig.ascending) {
                        R.string.sort_ascending
                    } else {
                        R.string.sort_descending
                    }
                )
            )
        }
    }
}

@Composable
private fun sortFieldLabel(field: SortField): String = stringResource(
    when (field) {
        SortField.NAME -> R.string.sort_field_name
        SortField.TIME -> R.string.sort_field_time
        SortField.SIZE -> R.string.sort_field_size
        SortField.DURATION -> R.string.sort_field_duration
    }
)
