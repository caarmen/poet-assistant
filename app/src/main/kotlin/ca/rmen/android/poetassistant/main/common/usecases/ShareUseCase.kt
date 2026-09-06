package ca.rmen.android.poetassistant.main.common.usecases

import android.content.Context
import android.content.Intent
import ca.rmen.android.poetassistant.main.common.models.Share
import javax.inject.Inject

/**
 * Starts an intent chooser to share the given share.
 */
class ShareUseCase @Inject constructor(){
    operator fun invoke(share: Share, context: Context) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, share.content)
        intent.type = "text/plain"
        val chooserIntent = Intent.createChooser(intent, share.title)
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
}