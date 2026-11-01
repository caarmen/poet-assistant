package ca.rmen.android.poetassistant.settings

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import ca.rmen.android.poetassistant.Environment
import ca.rmen.android.poetassistant.settings.ui.composables.SETTINGS_SYSTEM_TTS_SETTINGS_ROW_TAG
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {
    @Rule(order = 1)
    @JvmField
    val hiltTestRule = HiltAndroidRule(this)

    @Rule(order = 2)
    @JvmField
    val composeTestRule = createEmptyComposeRule()

    companion object {
        private const val SYSTEM_TTS_SETTINGS_INTENT = "com.android.settings.TTS_SETTINGS"
    }

    @Before
    fun setup() {
        mockSystemSettingsApp()
    }

    @Test
    fun testSystemSettings() {
        ActivityScenario.launch(NewSettingsActivity::class.java).use {
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(SETTINGS_SYSTEM_TTS_SETTINGS_ROW_TAG)
                .performScrollTo()
                .assertIsDisplayed()
                .performClick()
            val nextIntent = Shadows.shadowOf(Environment.getApplication()).nextStartedActivity
            Assert.assertNotNull(nextIntent)
            Assert.assertEquals(SYSTEM_TTS_SETTINGS_INTENT, nextIntent.action)
        }
    }

    // https://stackoverflow.com/questions/21638455/add-resolve-info-to-robolectric-package-manager
    private fun mockSystemSettingsApp() {
        val packageManager = Shadows.shadowOf(Environment.getApplication().packageManager)
        val intent = Intent(SYSTEM_TTS_SETTINGS_INTENT)

        val info = ResolveInfo()
        info.isDefault = true

        val applicationInfo = ApplicationInfo()
        applicationInfo.packageName = "com.example"
        info.activityInfo = ActivityInfo()
        info.activityInfo.applicationInfo = applicationInfo
        info.activityInfo.name = "Example"

        packageManager.addResolveInfoForIntent(intent, info)
    }
}