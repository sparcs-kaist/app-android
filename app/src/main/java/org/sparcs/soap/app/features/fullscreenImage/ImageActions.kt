package org.sparcs.soap.app.features.fullscreenImage

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import org.sparcs.soap.R
import org.sparcs.soap.app.shared.sharing.ShareContent
import org.sparcs.soap.app.shared.sharing.ShareImageStore
import org.sparcs.soap.app.shared.sharing.ShareLauncher
import org.sparcs.soap.app.shared.sharing.ShareRequest
import org.sparcs.soap.app.shared.sharing.ShareTarget
import java.io.IOException

internal enum class ImageAction(
    @param:StringRes val label: Int,
    val icon: ImageVector,
    @param:StringRes val failureTitle: Int,
    val target: ShareTarget,
) {
    Share(R.string.share, Icons.Outlined.Share, R.string.viewer_share_failed, ShareTarget.More),
    Save(R.string.share_save_image, Icons.Outlined.Download, R.string.viewer_save_failed, ShareTarget.Save),
    Copy(R.string.share_copy_image, Icons.Outlined.ContentCopy, R.string.viewer_copy_failed, ShareTarget.Copy),
}

internal class ImageActionPerformer(private val context: Context) {
    private val store = ShareImageStore(context.applicationContext)
    private val launcher = ShareLauncher(context)

    suspend fun perform(action: ImageAction, url: String) {
        val bitmap = loadBitmap(url)
        val uri = if (action == ImageAction.Save) store.saveToGallery(bitmap) else store.save(bitmap)
        val title = context.getString(R.string.viewer_photo)
        val content = ShareContent(title = title, text = "", topColor = "", bottomColor = "")
        if (!launcher.launch(ShareRequest(action.target, content, uri), onFeed = null)) {
            throw IOException(context.getString(R.string.share_failed))
        }
    }

    private suspend fun loadBitmap(url: String): Bitmap {
        val request = ImageRequest.Builder(context).data(url).allowHardware(false).build()
        return when (val result = context.imageLoader.execute(request)) {
            is SuccessResult -> result.drawable.toBitmap()
            is ErrorResult -> throw result.throwable
        }
    }
}
