package org.sparcs.soap.app.features.lectureDetail.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchNavigationBar
import org.sparcs.soap.app.features.navigationBar.components.BackButton
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.wishlistColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureDetailNavigationBar(
    navController: NavController,
    text: String,
    onAdd: () -> Unit,
    onDelete: () -> Unit,
    isCurrentTimetable: Boolean,
    isEnabled: Boolean,
    isWishlisted: Boolean = false,
    onToggleWishlist: (() -> Unit)? = null,
    isSearchContext: Boolean = false,
    navigationActions: @Composable RowScope.() -> Unit = {},
) {
    if (isSearchContext) {
        LectureSearchNavigationBar(text, { navController.popBackStack() }) {
            navigationActions()
            if (onToggleWishlist != null) WishlistButton(isWishlisted, onToggleWishlist)
            if (isEnabled) IconButton(onClick = onAdd, enabled = !isCurrentTimetable) {
                Icon(Icons.Rounded.Add, stringResource(if (isCurrentTimetable) R.string.lecture_in_timetable else R.string.add_course))
            }
        }
        return
    }

    TopAppBar(
        navigationIcon = {
            BackButton { navController.popBackStack() }
        },
        title = {
            Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        actions = {
            if (onToggleWishlist != null) WishlistButton(isWishlisted, onToggleWishlist)
            if(!isEnabled) return@TopAppBar
            if (isSearchContext && isCurrentTimetable) {
                TextButton(onClick = {}, enabled = false) { Text(stringResource(R.string.lecture_in_timetable)) }
                return@TopAppBar
            }
            if(!isCurrentTimetable) {
                IconButton(onClick = onAdd) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.add_course)
                    )
                }
            } else {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }
        }
    )
}

@Composable
private fun WishlistButton(isWishlisted: Boolean, onToggleWishlist: () -> Unit) {
    IconButton(onClick = onToggleWishlist) {
        Icon(
            if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            stringResource(if (isWishlisted) R.string.wishlist_remove else R.string.wishlist_add),
            tint = if (isWishlisted) wishlistColor else LocalContentColor.current,
        )
    }
}

@Composable
@Preview
private fun Preview(){
    Theme{ LectureDetailNavigationBar(rememberNavController(), "title", {}, {},
        isCurrentTimetable = false,
        isEnabled = true
    ) }
}
