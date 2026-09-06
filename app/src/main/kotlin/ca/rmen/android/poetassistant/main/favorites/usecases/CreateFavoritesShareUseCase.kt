package ca.rmen.android.poetassistant.main.favorites.usecases

import android.app.Application
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.common.models.Share
import ca.rmen.android.poetassistant.main.favorites.data.FavoritesRepository
import javax.inject.Inject

/**
 * Use case for creating shareable content from the favorites list.
 * Formats the favorites into a shareable string with proper formatting.
 */
class CreateFavoritesShareUseCase @Inject constructor(
    private val application: Application,
    private val repository: FavoritesRepository,
) {
    /**
     * Creates a Share object containing formatted favorites content.
     *
     * @return Share object with title and formatted content ready for sharing.
     */
    suspend fun invoke(): Share {
        val title = application.getString(R.string.share_favorites_title)
        val builder = StringBuilder(title)
        val entries = repository.getFavorites().sorted()
        entries.forEach { builder.append(application.getString(R.string.share_rt_entry, it)) }
        return Share(
            title = application.getString(R.string.share),
            content = builder.toString(),
        )
    }
}