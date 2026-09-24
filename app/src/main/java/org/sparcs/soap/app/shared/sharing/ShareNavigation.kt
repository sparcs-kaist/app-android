package org.sparcs.soap.app.shared.sharing

import android.net.Uri
import androidx.navigation.NavController
import org.sparcs.soap.app.features.navigationBar.Channel

fun NavController.navigateToShareFeed(imageUri: Uri, text: String) {
    navigate("${Channel.FeedPostCompose.name}?initial_text=${Uri.encode(text)}&initial_image_uri=${Uri.encode(imageUri.toString())}")
}
