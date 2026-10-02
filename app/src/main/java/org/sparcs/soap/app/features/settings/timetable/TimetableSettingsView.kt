package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.LectureSearchStyleStore
import org.sparcs.soap.app.domain.helpers.rememberLectureSearchStyle
import org.sparcs.soap.app.domain.models.otl.LectureSearchStyle
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.features.settings.taxi.NavigationLinkWithIcon
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.landscapeHideOnScrollBehavior
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableSettingsView(
    navController: NavController,
    viewModel: TimetableSettingsViewModelProtocol = hiltViewModel<TimetableSettingsViewModel>(),
) {
    val state by viewModel.state.collectAsState()
    val topBarScrollBehavior = landscapeHideOnScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.fetchDepartments()
    }

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                stringResource(R.string.otl_settings),
                { navController.popBackStack() },
                scrollBehavior = topBarScrollBehavior
            )
        },
        modifier = Modifier
            .hideTopBarOnScroll(topBarScrollBehavior)
            .analyticsScreen("Timetable Settings")
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                when (val s = state) {
                    is TimetableSettingsViewModel.ViewState.Loading -> {
                        item { LoadingView() }
                    }
                    is TimetableSettingsViewModel.ViewState.Error -> {
                        item {
                            ErrorView(
                                error = s.error,
                                defaultMessageResId = s.resId ?: R.string.error,
                                onRetry = { viewModel.fetchDepartments() }
                            )
                        }
                    }
                    is TimetableSettingsViewModel.ViewState.Loaded -> {
                        item {
                            LoadedView(navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Column {
        Text(
            text = stringResource(R.string.adding_lectures),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            RadioButton(selected = true, onClick = null, enabled = false)
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.lecture_search_full_screen),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.interested_departments),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun LoadedView(
    navController: NavController,
) {
    val context = LocalContext.current
    val style by rememberLectureSearchStyle()
    val store = remember(context) { LectureSearchStyleStore(context) }

    Column {
        Text(
            text = stringResource(R.string.adding_lectures),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(12.dp))

        LectureSearchStyle.entries.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = style == option,
                        role = Role.RadioButton,
                        onClick = { store.lectureSearchStyle = option }
                    )
                    .padding(vertical = 10.dp, horizontal = 4.dp)
            ) {
                RadioButton(
                    selected = style == option,
                    onClick = null
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(option.titleRes),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.interested_departments),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(12.dp))

        NavigationLinkWithIcon(
            onClick = {
                navController.navigate(Channel.InterestedDepartments.name)
            },
            text = stringResource(R.string.interested_departments),
            icon = Icons.Outlined.School
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimetableSettingsViewPreview() {
    Theme { TimetableSettingsView(rememberNavController()) }
}
