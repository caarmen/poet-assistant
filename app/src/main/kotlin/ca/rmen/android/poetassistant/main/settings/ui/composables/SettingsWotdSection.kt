/*
 * Copyright (c) 2016 - present Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.android.poetassistant.main.settings.ui.composables

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.settings.WotdNotificationPriority

// Test tags: the legacy preference keys, so the migrated tests address
// the rows as they addressed the preferences.
const val SETTINGS_WOTD_ENABLED_ROW_TAG = "PREF_WOTD_ENABLED"
const val SETTINGS_WOTD_NOTIFICATION_PRIORITY_ROW_TAG = "PREF_WOTD_NOTIFICATION_PRIORITY"
const val SETTINGS_WOTD_NOTIFICATION_PRIORITY_ITEM_TAG_PREFIX = "PREF_WOTD_NOTIFICATION_PRIORITY_"

/**
 * The notifications section: the word-of-the-day switch and the
 * notification-priority dropdown.
 *
 * - The priority row is hidden on API 26 and above: those users change
 *   the priority in the system settings (legacy behavior, spec §1.3.2).
 * - The priority row is disabled while the switch is off, like the
 *   legacy android:dependency.
 */
@Composable
fun SettingsWotdSection(
    isWotdEnabled: Boolean,
    wotdNotificationPriority: WotdNotificationPriority,
    onWotdEnabledChanged: (Boolean) -> Unit,
    onWotdNotificationPrioritySelected: (WotdNotificationPriority) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SettingsSwitchRow(
            title = stringResource(R.string.wotd_setting_title),
            description = stringResource(R.string.wotd_setting_description),
            checked = isWotdEnabled,
            onCheckedChange = onWotdEnabledChanged,
            testTag = SETTINGS_WOTD_ENABLED_ROW_TAG,
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_wotd),
                    contentDescription = null,
                )
            },
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            WotdNotificationPriorityRow(
                enabled = isWotdEnabled,
                selectedPriority = wotdNotificationPriority,
                onPrioritySelected = onWotdNotificationPrioritySelected,
            )
        }
    }
}

@Composable
private fun WotdNotificationPriorityRow(
    enabled: Boolean,
    selectedPriority: WotdNotificationPriority,
    onPrioritySelected: (WotdNotificationPriority) -> Unit,
) {
    var openMenu by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text(stringResource(R.string.wotd_setting_system_notification_priority_title)) },
        supportingContent = { Text(stringResource(R.string.wotd_setting_system_notification_priority_summary)) },
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_settings_notification_priority),
                contentDescription = null,
            )
        },
        modifier = Modifier
            .testTag(SETTINGS_WOTD_NOTIFICATION_PRIORITY_ROW_TAG)
            .alpha(if (enabled) 1f else 0.38f)
            .clickable(enabled = enabled) { openMenu = true },
    )
    Box {
        DropdownMenu(
            expanded = openMenu,
            onDismissRequest = { openMenu = false },
        ) {
            WotdNotificationPriority.entries.forEach { priority ->
                DropdownMenuItem(
                    text = { Text(priority.label()) },
                    onClick = {
                        onPrioritySelected(priority)
                        openMenu = false
                    },
                    modifier = Modifier.testTag(SETTINGS_WOTD_NOTIFICATION_PRIORITY_ITEM_TAG_PREFIX + priority.name),
                )
            }
        }
    }
}

@Composable
private fun WotdNotificationPriority.label(): String = when (this) {
    WotdNotificationPriority.MAX -> stringResource(R.string.wotd_setting_system_notification_priority_label_max)
    WotdNotificationPriority.HIGH -> stringResource(R.string.wotd_setting_system_notification_priority_label_high)
    WotdNotificationPriority.DEFAULT -> stringResource(R.string.wotd_setting_system_notification_priority_label_default)
    WotdNotificationPriority.LOW -> stringResource(R.string.wotd_setting_system_notification_priority_label_low)
    WotdNotificationPriority.MIN -> stringResource(R.string.wotd_setting_system_notification_priority_label_min)
}
