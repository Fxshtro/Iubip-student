package ru.iubip.student.ui.screens.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.iubip.student.R
import ru.iubip.student.ui.icons.PhosphorIcons
import ru.iubip.student.ui.theme.IubipTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Компактный индикатор актуальности в слоте `action` шапки (рядом с кнопкой календаря).
 * 20 dp иконка + 28 dp зона нажатия; по тапу — диалог с человеческим объяснением.
 */
@Composable
fun FreshnessIndicator(
    freshness: DataFreshness,
    details: FreshnessDetails,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(28.dp)
            .clickable(interactionSource = null, indication = null, role = Role.Button) {
                showDialog = true
            },
        contentAlignment = Alignment.Center,
    ) {
        val (icon, tint, description) = when (freshness) {
            DataFreshness.Fresh -> Triple(
                PhosphorIcons.Regular.Check,
                IubipTheme.colors.statusOk,
                stringResource(R.string.cd_status_fresh),
            )
            DataFreshness.Stale -> Triple(
                PhosphorIcons.Regular.WarningCircle,
                IubipTheme.colors.statusWarning,
                stringResource(R.string.cd_status_stale),
            )
            DataFreshness.Waiting -> Triple(
                PhosphorIcons.Regular.CircleDashed,
                MaterialTheme.colorScheme.onSurfaceVariant,
                stringResource(R.string.cd_status_waiting),
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.pult_status_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = statusMessage(details),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.pult_status_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.pult_status_ok))
                }
            },
        )
    }
}

@Composable
private fun statusMessage(details: FreshnessDetails): String {
    val formattedTime = details.updatedAt?.let { timestamp ->
        val formatter = DateTimeFormatter.ofPattern("d MMMM, HH:mm", Locale.forLanguageTag("ru"))
        Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(formatter)
    }
    return when (details.freshness) {
        DataFreshness.Fresh -> stringResource(
            R.string.pult_status_fresh,
            details.group,
            formattedTime ?: stringResource(R.string.pult_status_time_unknown),
        )
        DataFreshness.Stale -> stringResource(
            R.string.pult_status_stale,
            details.group,
            formattedTime ?: stringResource(R.string.pult_status_time_unknown),
        )
        DataFreshness.Waiting -> stringResource(R.string.pult_status_waiting, details.group)
    }
}
