package ru.iubip.student.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.iubip.student.R
import ru.iubip.student.ui.components.ContentColumn
import ru.iubip.student.ui.components.toIubipWindowInfo
import ru.iubip.student.ui.components.ProfileAvatarPlaceholder
import ru.iubip.student.ui.components.ProfileFieldRow
import ru.iubip.student.ui.components.ProfileFieldsCard
import ru.iubip.student.ui.theme.IubipAppTheme

/**
 * Экран «О себе»: аватарка, ФИО, группа и список полей профиля
 * (email, страна, город, часовой пояс) — в порядке убывания важности.
 *
 * Все цвета и шрифты берутся из `MaterialTheme` (токены темы), поэтому экран
 * автоматически подхватывает смену дизайна и тёмную тему. Кнопок редактирования
 * и прочих действий нет — это только просмотр.
 *
 * Раскладка зависит от ширины окна:
 * - < 600 dp (телефон в портрете) — одна центрированная колонка, как раньше;
 * - ≥ 600 dp (горизонталь, планшет) — две колонки: слева (40%) аватарка, ФИО и группа,
 *   справа — карточка полей. ФИО переносится на несколько строк.
 *
 * Ширина контента ограничена и центрируется ([ContentColumn]).
 *
 * @param profile данные профиля, по умолчанию [SampleProfile] (тестовые данные)
 * @param windowWidthSizeClass класс ширины окна
 */
@Composable
fun ProfileScreen(
    windowWidthSizeClass: WindowWidthSizeClass,
    modifier: Modifier = Modifier,
    profile: Profile = SampleProfile,
) {
    val window = windowWidthSizeClass.toIubipWindowInfo()
    ContentColumn(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (window.isWide) {
                ProfileTwoColumns(profile = profile)
            } else {
                ProfileSingleColumn(profile = profile)
            }
        }
    }
}

/** Одна центрированная колонка: аватарка, ФИО, группа, карточка полей. */
@Composable
private fun ProfileSingleColumn(profile: Profile) {
    Column(
        // На очень узком экране карточки не растягиваются на всю ширину.
        modifier = Modifier.widthIn(max = 640.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileAvatarPlaceholder()

        Spacer(Modifier.height(16.dp))

        Text(
            text = profile.fullName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            // Длинное ФИО переносится на несколько строк, а не обрезается многоточием.
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.profile_group_format, profile.group),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        ProfileFieldsCard { ProfileFields(profile) }
    }
}

/**
 * Две колонки для горизонтали/планшета: слева 40% ширины под аватарку, ФИО и группу,
 * справа 60% — карточка полей. Колонки разделены зазором 24 dp, выровнены по верху.
 */
@Composable
private fun ProfileTwoColumns(profile: Profile) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(0.4f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileAvatarPlaceholder()

            Spacer(Modifier.height(16.dp))

            Text(
                text = profile.fullName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                // Широкий экран — но ФИО по-прежнему переносится, а не обрезается.
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.profile_group_format, profile.group),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Box(
            modifier = Modifier
                .weight(0.6f)
                .align(Alignment.CenterVertically),
        ) {
            ProfileFieldsCard { ProfileFields(profile) }
        }
    }
}

/** Поля профиля внутри карточки — одни и те же в обеих раскладках. */
@Composable
private fun ProfileFields(profile: Profile) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        ProfileFieldRow(
            label = stringResource(R.string.profile_field_email),
            value = profile.email,
        )
        FieldDivider()
        ProfileFieldRow(
            label = stringResource(R.string.profile_field_country),
            value = profile.country,
        )
        FieldDivider()
        ProfileFieldRow(
            label = stringResource(R.string.profile_field_city),
            value = profile.city,
        )
        FieldDivider()
        ProfileFieldRow(
            label = stringResource(R.string.profile_field_time_zone),
            value = profile.timeZone,
        )
    }
}

/**
 * Разделитель между полями профиля, не доходящий до краёв карточки.
 */
@Composable
private fun FieldDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Нужен только превью: класс ширины, соответствующий ширине превью. */
private fun previewWidthClass(widthDp: Int): WindowWidthSizeClass = when {
    widthDp < 600 -> WindowWidthSizeClass.Compact
    widthDp < 840 -> WindowWidthSizeClass.Medium
    else -> WindowWidthSizeClass.Expanded
}

@Preview(name = "Профиль — портрет, светлая", showBackground = true, widthDp = 393, heightDp = 800)
@Composable
private fun ProfileLightPreview() {
    IubipAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(0.dp)) {
            ProfileScreen(windowWidthSizeClass = previewWidthClass(393), profile = SampleProfile)
        }
    }
}

@Preview(
    name = "Профиль — портрет, тёмная",
    showBackground = true,
    widthDp = 393,
    heightDp = 800,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ProfileDarkPreview() {
    IubipAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(0.dp)) {
            ProfileScreen(windowWidthSizeClass = previewWidthClass(393), profile = SampleProfile)
        }
    }
}

@Preview(name = "Профиль — длинное ФИО", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ProfileLongNamePreview() {
    IubipAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(0.dp)) {
            ProfileScreen(
                windowWidthSizeClass = previewWidthClass(360),
                profile = SampleProfileLongName,
            )
        }
    }
}

@Preview(name = "Профиль — горизонталь, две колонки", showBackground = true, widthDp = 914, heightDp = 411)
@Composable
private fun ProfileLandscapePreview() {
    IubipAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(0.dp)) {
            ProfileScreen(windowWidthSizeClass = previewWidthClass(914), profile = SampleProfile)
        }
    }
}
