package org.sparcs.soap.app.features.timetable.sharing

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.app.shared.sharing.ShareContent
import org.sparcs.soap.app.shared.sharing.ShareImagePreview
import org.sparcs.soap.app.shared.sharing.ShareSheet
import org.sparcs.soap.app.shared.sharing.ShareViewModel

data class TimetableShareSnapshot(val semester: Semester, val timetable: Timetable, val theme: TimetableTheme)

@Composable
fun TimetableShareSheet(
    snapshot: TimetableShareSnapshot,
    onDismiss: () -> Unit,
    onFeed: (Uri, String) -> Unit,
    viewModel: ShareViewModel = hiltViewModel(),
) {
    val layer = rememberGraphicsLayer()
    val content = ShareContent(
        title = stringResource(R.string.timetable_share),
        text = stringResource(R.string.timetable_share_text, snapshot.semester.description, snapshot.timetable.credits),
        topColor = "#${snapshot.theme.hexColors.first()}",
        bottomColor = "#${snapshot.theme.hexColors.getOrElse(1) { snapshot.theme.hexColors.first() }}",
    )
    ShareSheet(
        viewModel = viewModel,
        content = content,
        capture = { layer.toImageBitmap().asAndroidBitmap() },
        onDismiss = onDismiss,
        onFeed = onFeed,
        preview = {
            ShareImagePreview {
                Box(Modifier.drawWithContent { layer.record { this@drawWithContent.drawContent() }; drawContent() }) {
                    TimetableShareRenderingView(snapshot.semester, snapshot.timetable, snapshot.theme)
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun TimetableSharePreviewPreview() {
    ShareImagePreview { TimetableShareRenderingView(Semester.mockList().first(), Timetable.mock(), TimetableTheme.Default) }
}
