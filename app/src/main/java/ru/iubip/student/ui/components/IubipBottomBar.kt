package ru.iubip.student.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.iubip.student.navigation.BottomDestination
import ru.iubip.student.ui.theme.IubipShapes
import ru.iubip.student.ui.theme.IubipTheme

/**
 * Нижняя панель навигации в стиле приложения.
 *
 * Без цветной заливки: фон — фон приложения, сверху тонкая линия-разделитель.
 * Активный пункт — акцентная «таблетка» вокруг иконки (primary с прозрачностью ~12%),
 * иконка и подпись акцентного цвета; неактивный — серый.
 *
 * @param currentRoute маршрут активного раздела
 * @param onNavigate обработчик перехода
 */
@Composable
fun IubipBottomBar(
    currentRoute: String?,
    onNavigate: (BottomDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        HorizontalDivider(thickness = 1.dp, color = IubipTheme.colors.divider)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .selectableGroup()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomDestination.entries.forEach { destination ->
                BottomBarItem(
                    destination = destination,
                    selected = destination.route == currentRoute,
                    onClick = { onNavigate(destination) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    destination: BottomDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val contentColor = if (selected) colors.primary else colors.onSurfaceVariant

    // Кликается вся ячейка, но без «квадратной» ряби Material: её перекрывает
    // собственное выделение в виде таблетки вокруг иконки и подписи.
    Box(
        modifier = modifier
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = if (selected) IubipTheme.colors.accentPill else Color.Transparent,
                    shape = IubipShapes.Pill,
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = if (selected) destination.iconSelected else destination.icon,
                contentDescription = stringResource(destination.contentDescriptionRes),
                tint = contentColor,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = stringResource(destination.labelRes),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
            )
        }
    }
}
