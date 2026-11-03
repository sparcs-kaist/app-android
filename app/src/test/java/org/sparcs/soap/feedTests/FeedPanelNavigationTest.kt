package org.sparcs.soap.feedTests

import android.app.Application
import android.view.View
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.sparcs.soap.app.domain.models.feed.FeedComment
import org.sparcs.soap.app.domain.models.feed.FeedPost
import org.sparcs.soap.app.features.feed.ActiveFeedPostViewModel
import org.sparcs.soap.app.features.feed.FeedPostRoute
import org.sparcs.soap.app.features.feed.FeedPostSelection
import org.sparcs.soap.app.features.feed.FeedSplit
import org.sparcs.soap.app.features.feed.FeedView
import org.sparcs.soap.app.features.feed.FeedViewModel
import org.sparcs.soap.app.features.feed.feedSplit
import org.sparcs.soap.app.features.feedPost.FeedPostViewModel
import org.sparcs.soap.app.features.main.LocalAnalytics
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.shared.mocks.feed.mock
import org.sparcs.soap.app.shared.viewModelMocks.feed.MockFeedPostViewModel
import org.sparcs.soap.buddyPreviewSupport.feed.PreviewFeedViewModel
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockFeedCommentUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockFeedPostUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockUserUseCase

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-w1000dp-h800dp")
class FeedPanelNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun panelTransfersToDetailAndBackWithoutLosingDraft() {
        val post = FeedPost.mock().copy(content = "Open this post", images = emptyList())
        val feed = PreviewFeedViewModel(FeedViewModel.ViewState.Loaded(listOf(post)), listOf(post))
        val activePost = ActiveFeedPostViewModel(object : FeedPostViewModel.Factory {
            override fun create(feedId: String) = FeedPostViewModel(
                feedId = feedId,
                feedCommentUseCase = MockFeedCommentUseCase(),
                feedPostUseCase = MockFeedPostUseCase().apply { fetchPostResult = Result.success(post) },
                userUseCase = MockUserUseCase(),
                crashlyticsService = MockCrashlyticsService(),
                analyticsService = MockAnalyticsService(),
                textProcessingDelegate = MockFeedPostViewModel(FeedPostViewModel.ViewState.Loading),
            )
        })
        val selection = mutableStateOf(FeedPostSelection())
        var split by mutableStateOf<FeedSplit?>(feedSplit(1000.dp))
        lateinit var navController: NavHostController
        compose.setContent {
            CompositionLocalProvider(LocalAnalytics provides MockAnalyticsService()) {
                MaterialTheme {
                    navController = rememberNavController()
                    NavHost(navController, startDestination = Channel.Start.name) {
                        composable(Channel.Start.name) {
                            FeedView(feed, navController, activePost[selection.value.postID], split, selection)
                        }
                        composable(Channel.FeedPost.name + "?feedId={feedId}") { entry ->
                            val postID = requireNotNull(entry.arguments?.getString("feedId"))
                            val model = remember(postID) { requireNotNull(activePost[postID]) }
                            FeedPostRoute(
                                postID,
                                null, model, feed, navController, split, selection,
                            )
                        }
                    }
                }
            }
        }

        compose.onNodeWithText("Open this post").performClick()
        compose.onNodeWithText("Comments").assertExists()
        compose.onNodeWithText("Write a comment").performTextInput("Keep this draft")

        compose.runOnIdle { split = null }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(Channel.FeedPost.name, navController.currentDestination?.route?.substringBefore('?'))
            assertEquals("Keep this draft", activePost[post.id]!!.text)
        }

        compose.runOnIdle { split = feedSplit(1000.dp) }
        compose.onNodeWithText("Comments").assertExists()
        compose.onNodeWithText("Keep this draft").assertExists()
        compose.runOnIdle { assertEquals(Channel.Start.name, navController.currentDestination?.route) }

        compose.onNodeWithContentDescription("Close").performClick()
        compose.onNodeWithText("Comments").assertDoesNotExist()
        compose.runOnIdle { assertNull(selection.value.postID) }

        compose.runOnIdle { split = null }
        compose.onNodeWithText("Open this post").performClick()
        compose.runOnIdle { navController.popBackStack() }
        compose.waitForIdle()
        compose.runOnIdle { assertNull(selection.value.postID) }
    }
    @Test
    @Config(qualifiers = "en-w1000dp-h500dp-land")
    fun landscapePanelScrollsAndComposerStaysAboveSystemNavigation() {
        val post = FeedPost.mock().copy(content = "Post", images = emptyList(), commentCount = 40)
        val feed = PreviewFeedViewModel(FeedViewModel.ViewState.Loaded(listOf(post)), listOf(post))
        val activePost = MockFeedPostViewModel(FeedPostViewModel.ViewState.Loaded(post)).apply {
            comments = List(40) { FeedComment.mock().copy(id = "comment-$it", content = "Comment $it", replies = emptyList()) }
        }
        val selection = mutableStateOf(FeedPostSelection(post.id, isPanel = true))
        lateinit var view: View
        var navigationHeight = 0
        var statusHeight = 0
        compose.setContent {
            view = LocalView.current
            navigationHeight = with(LocalDensity.current) { 48.dp.roundToPx() }
            statusHeight = with(LocalDensity.current) { 24.dp.roundToPx() }
            CompositionLocalProvider(LocalAnalytics provides MockAnalyticsService()) {
                MaterialTheme {
                    FeedView(feed, rememberNavController(), activePost, feedSplit(1000.dp), selection)
                }
            }
        }
        // The system navigation area moves between physical edges on opposite rotations.
        for ((leftInset, rightInset) in listOf(statusHeight to 0, 0 to statusHeight)) {
            compose.runOnIdle {
                ViewCompat.dispatchApplyWindowInsets(view, WindowInsetsCompat.Builder()
                    .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(leftInset, 0, rightInset, navigationHeight))
                    .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(0, statusHeight, 0, 0))
                    .setVisible(WindowInsetsCompat.Type.navigationBars(), true)
                    .build())
            }
            compose.waitForIdle()
            val input = compose.onNodeWithText("Write a comment").fetchSemanticsNode().boundsInRoot
            compose.runOnIdle {
                assertTrue("Composer overlaps navigation bar: $input, height=${view.height}",
                    input.bottom <= view.height - navigationHeight)
            }
            val timeline = compose.onNodeWithTag("feed-timeline-content").fetchSemanticsNode().boundsInRoot
            val panel = compose.onNodeWithTag("feed-comments-panel").fetchSemanticsNode().boundsInRoot
            val header = compose.onNodeWithTag("feed-comments-header").fetchSemanticsNode().boundsInRoot
            assertEquals("No internal gap with left=$leftInset right=$rightInset", timeline.right, header.left, 1f)
            assertTrue("Feed avoids the outer left system bar", timeline.left >= leftInset)
            assertTrue("Comments avoid the outer right system bar", header.right <= view.width - rightInset)
            assertEquals("Header paints the status bar area too", panel.top, header.top, 1f)
        }
        val comments = compose.onNodeWithTag("feed-comments")
        comments.performTouchInput { swipeUp() }
        compose.waitForIdle()
        val scroll = comments.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange]
        assertTrue("Dragging the comments must scroll the list", scroll.value() > 0f)
    }

}
