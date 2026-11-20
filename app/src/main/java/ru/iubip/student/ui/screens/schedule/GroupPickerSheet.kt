package ru.iubip.student.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.iubip.student.R
import ru.iubip.student.data.AcademyGroups
import ru.iubip.student.data.GroupsRepository
import ru.iubip.student.data.ServiceLocator
import ru.iubip.student.ui.components.IconCircleButton
import ru.iubip.student.ui.icons.PhosphorIcons
import ru.iubip.student.ui.theme.IubipAppTheme
import ru.iubip.student.ui.theme.IubipShapes

/**
 * Временный «пульт»: плавающая кнопка и лист выбора группы поверх экрана расписания.
 *
 * Кнопка НЕ является частью вёрстки контента — она живёт в отдельном слое `Box(Modifier.fillMaxSize())`
 * в [ScheduleScreen]. Отступ снизу/справа учитывает нижнюю или боковую панель навигации.
 *
 * Выбор НЕ сохраняется: он живёт только в состоянии ViewModel и флаге [ServiceLocator.useRemote].
 */
@Composable
fun GroupPickerOverlay(
    onGroupSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    /** true — панель навигации справа (горизонталь/планшет): кнопка уходит в левый угол. */
    navigationOnSide: Boolean = false,
) {
    var sheetVisible by remember { mutableStateOf(false) }
    val repository: GroupsRepository = remember { ServiceLocator.groupsRepository() }

    Box(modifier = modifier.fillMaxSize()) {
        // Портрет: нижний правый угол над нижней панелью. Горизонталь: нижний ЛЕВЫЙ
        // угол — справа находятся календарь-колонка и боковая панель навигации,
        // там кнопка перекрывала бы дни календаря.
        PultButton(
            onClick = { sheetVisible = true },
            modifier = Modifier
                .align(if (navigationOnSide) Alignment.BottomStart else Alignment.BottomEnd)
                .padding(
                    start = if (navigationOnSide) 20.dp else 0.dp,
                    end = if (navigationOnSide) 0.dp else 20.dp,
                    bottom = if (navigationOnSide) 24.dp else 96.dp,
                ),
        )
    }

    if (sheetVisible) {
        GroupPickerSheet(
            repository = repository,
            onDismiss = { sheetVisible = false },
            onGroupSelected = { name ->
                sheetVisible = false
                onGroupSelected(name)
            },
        )
    }
}

/** Компактная круглая кнопка 56 dp с иконкой «ползунков» Phosphor. */
@Composable
private fun PultButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(interactionSource = null, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = PhosphorIcons.Regular.SlidersHorizontal,
            contentDescription = stringResource(R.string.cd_open_group_picker),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp),
        )
    }
}

/** Лист «Выбрать группу»: поиск, академии, ошибка/повтор. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupPickerSheet(
    repository: GroupsRepository,
    onDismiss: () -> Unit,
    onGroupSelected: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var academies by remember { mutableStateOf<List<AcademyGroups>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }

    fun close() = scope.launch {
        if (sheetState.isVisible) sheetState.hide()
        onDismiss()
    }

    LaunchedEffect(reloadKey) {
        isLoading = true
        error = null
        repository.academies()
            .onSuccess { academies = it }
            .onFailure { error = it.message }
        isLoading = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = IubipShapes.Panel,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.pult_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconCircleButton(
                    icon = PhosphorIcons.Regular.X,
                    contentDescription = stringResource(R.string.cd_group_picker_close),
                    onClick = { close() },
                )
            }
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.pult_search_hint)) },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconCircleButton(
                            icon = PhosphorIcons.Regular.X,
                            contentDescription = stringResource(R.string.pult_search_clear),
                            onClick = { query = "" },
                            iconSize = 16.dp,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                } else null,
            )
            when {
                isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                error != null -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.pult_groups_error),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Button(onClick = { reloadKey++ }) {
                        Text(stringResource(R.string.schedule_retry))
                    }
                }

                else -> {
                    val filter = query.trim()
                    val filtered = academies.mapNotNull { academy ->
                        val groups = if (filter.isEmpty()) {
                            academy.groups
                        } else {
                            academy.groups.filter { it.contains(filter, ignoreCase = true) }
                        }
                        if (groups.isEmpty()) null else academy.copy(groups = groups)
                    }
                    if (filtered.isEmpty()) {
                        Text(
                            text = stringResource(R.string.pult_groups_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            filtered.forEach { academy ->
                                item(key = "header-${academy.name}") {
                                    Text(
                                        text = academy.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                    )
                                }
                                items(academy.groups, key = { "${academy.name}-$it" }) { group ->
                                    Text(
                                        text = group,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                interactionSource = null,
                                                indication = null,
                                                role = Role.Button,
                                                onClick = { onGroupSelected(group) },
                                            )
                                            .padding(vertical = 4.dp, horizontal = 8.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Пульт — кнопка", showBackground = true, widthDp = 393, heightDp = 120)
@Composable
private fun PultButtonPreview() {
    IubipAppTheme {
        Box(Modifier.fillMaxSize()) {
            PultButton(
                onClick = {},
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp),
            )
        }
    }
}
