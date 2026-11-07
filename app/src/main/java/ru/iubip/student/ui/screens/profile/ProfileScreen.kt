package ru.iubip.student.ui.screens.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.iubip.student.R
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
 * @param profile данные профиля, по умолчанию [SampleProfile] (тестовые данные)
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    profile: Profile = SampleProfile,
) {
    val contentColor = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            // На планшете/в горизонтали карточки не растягиваются на всю ширину.
            modifier = Modifier.widthIn(max = 640.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileAvatarPlaceholder()

            Spacer(Modifier.height(16.dp))

            Text(
                text = profile.fullName,
                style = MaterialTheme.typography.headlineMedium,
                color = contentColor,
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

            ProfileFieldsCard {
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
        }
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

@Preview(name = "Профиль — светлая", showBackground = true, widthDp = 393, heightDp = 800)
@Composable
private fun ProfileLightPreview() {
    IubipAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(0.dp)) {
            ProfileScreen(profile = SampleProfile)
        }
    }
}

@Preview(
    name = "Профиль — тёмная",
    showBackground = true,
    widthDp = 393,
    heightDp = 800,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ProfileDarkPreview() {
    IubipAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(0.dp)) {
            ProfileScreen(profile = SampleProfile)
        }
    }
}

@Preview(name = "Профиль — длинное ФИО", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ProfileLongNamePreview() {
    IubipAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(0.dp)) {
            ProfileScreen(profile = SampleProfileLongName)
        }
    }
}
