package ca.rmen.android.poetassistant.main.common.usecases

import android.annotation.TargetApi
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import ca.rmen.android.poetassistant.di.IODispatcher
import ca.rmen.android.poetassistant.main.common.models.ExternalAppMenuItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for querying the system for apps that support the ACTION_PROCESS_TEXT intent.
 * Returns a list of [ExternalAppMenuItem] objects representing available text processing apps.
 *
 * @param application The Android application context.
 * @param ioDispatcher The coroutine dispatcher for IO operations.
 */
class GetProcessTextMenuItemsUseCase @Inject constructor(
    private val application: Application,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Queries for apps that can process text and returns them as menu items.
     *
     * @param text The text to be processed by the external apps.
     * @return List of [ExternalAppMenuItem] objects for available apps.
     */
    suspend operator fun invoke(text: String): List<ExternalAppMenuItem> =
        withContext(ioDispatcher) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                application.packageManager.queryIntentActivities(
                    createProcessTextIntent(text),
                    PackageManager.ResolveInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                application.packageManager.queryIntentActivities(createProcessTextIntent(text), 0)
            }.filter { resolveInfo -> application.applicationInfo.packageName != resolveInfo.activityInfo.packageName }
                .map { resolveInfo ->
                    ExternalAppMenuItem(
                        label = resolveInfo.loadLabel(application.packageManager).toString(),
                        icon = resolveInfo.loadIcon(application.packageManager),
                        packageName = resolveInfo.activityInfo.packageName,
                        className = resolveInfo.activityInfo.name
                    )
                }
        }

    /**
     * Creates an ACTION_PROCESS_TEXT intent with the given text.
     *
     * @param text The text to be processed.
     * @return Intent configured for text processing.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun createProcessTextIntent(text: String): Intent {
        // See: https://android-developers.googleblog.com/2015/10/in-app-translations-in-android.html
        return Intent()
            .setAction(Intent.ACTION_PROCESS_TEXT)
            .putExtra(Intent.EXTRA_PROCESS_TEXT, text)
            .setType("text/plain")
    }
}