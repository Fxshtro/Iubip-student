package ru.iubip.student.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.iubip.student.navigation.BottomDestination
import ru.iubip.student.ui.theme.IubipShapes
import ru.iubip.student.ui.theme.IubipTheme

/**
 * Один пункт навигации — и для нижней панели, и для боковой.
 *
 * Раскладка вертикальная: иконка 22 dp сверху, подпись `labelSmall` под ней.
 * Активный пункт — акцентная «таблетка» (`accentPill`) вокруг всей колонки, иконка
 * и подпись `primary`; неактивный — `onSurfaceVariant`, контурная иконка.
 *
 * Компонент один на обе панели: [IubipBottomBar] и [IubipSideBar] различаются только
 * контейнером (разделитель сверху/слева) и расположением пунктов (в ряд/в столбец).
 *
 * @param destination пункт меню (иконки thin/fill и подписи)
 * @param selected активен ли пункт
 * @param onClick переход в раздел
 * @param modifier модификатор ячейки (в панелях передаётся `weight`)
 * @param compact компактный пункт для боковой панели: только иконка в «таблетке»,
 *   без подписи — так подпись не переносится и не обрезается в узкой колонке
 *   (название раздела остаётся в `contentDescription` для диктора)
 */
@Composable
fun IubipNavigationItem(
    destination: BottomDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val contentColor = if (selected) colors.primary else colors.onSurfaceVariant

    // Кликается вся ячейка, но без «квадратной» ряби Material: её перекрывает
    // собственное выделение вокруг иконки.
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
        // Подсветка — ОТДЕЛЬНЫЙ слой фиксированного размера, а не фон контента:
        // иначе Column растягивается на высоту ячейки (флекс-стиль) и вместо круга
        // получается вытянутый блок с отступами сверху и снизу.
        Box(
            modifier = Modifier
                .size(if (compact) COMPACT_HIGHLIGHT_SIZE else BOTTOM_HIGHLIGHT_WIDTH)
                .background(
                    color = if (selected) IubipTheme.colors.accentPill else Color.Transparent,
                    shape = if (compact) CircleShape else IubipShapes.Pill,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    imageVector = if (selected) destination.iconSelected else destination.icon,
                    contentDescription = stringResource(destination.contentDescriptionRes),
                    tint = contentColor,
                    modifier = Modifier.size(if (compact) 24.dp else 22.dp),
                )
                if (!compact) {
                    Text(
                        text = stringResource(destination.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Диаметр круглой подсветки в боковой панели (круг вокруг иконки 24 dp). */
private val COMPACT_HIGHLIGHT_SIZE = 48.dp

/** Ширина «таблетки» подсветки в нижней панели (иконка + подпись). */
private val BOTTOM_HIGHLIGHT_WIDTH = 96.dp

/** Высота «таблетки» подсветки в нижней панели. */
private val BOTTOM_HIGHLIGHT_HEIGHT = 56.dp
