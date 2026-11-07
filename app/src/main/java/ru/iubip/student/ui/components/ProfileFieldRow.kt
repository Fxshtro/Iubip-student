package ru.iubip.student.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.iubip.student.ui.theme.IubipShapes

/**
 * Строка поля профиля: подпись второстепенным цветом, значение — основным.
 *
 * Подпись и значение переносятся на несколько строк, поэтому длинные email
 * и ФИО не обрезаются. Цвета берутся только из [MaterialTheme].
 *
 * @param label подпись поля, например «Адрес электронной почты»
 * @param value значение поля, например «12k1i192024@mail.ru»
 */
@Composable
fun ProfileFieldRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 14.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
    }
}

/**
 * Карточка-контейнер для полей профиля.
 *
 * В теме пока нет собственных форм (`Shape.kt` и `surfaceContainer*` не заданы),
 * поэтому радиус 20 dp и `surface` + тонкая обводка взяты локально: на светлой теме
 * `surface == background`, и без обводки карточка была бы невидимой. Когда в теме
 * появится `Shape.kt`/`surfaceContainer`, заменить локальные радиус и обводку на токены.
 */
@Composable
fun ProfileFieldsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = IubipShapes.Panel,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = modifier.fillMaxWidth(),
        content = content,
    )
}
