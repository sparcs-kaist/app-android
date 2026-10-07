package org.sparcs.soap.app.features.timetable.creditCalculation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewCreditCalculationViewModel
import java.io.IOException

@Composable
internal fun GradeEntryRoute(
    semesterId: String,
    viewModel: CreditCalculationViewModelProtocol,
    navController: NavController,
) {
    val onBack: () -> Unit = { navController.popBackStack() }
    val state by viewModel.state.collectAsState()
    val error = state.error
    val semester = state.semesters.find { it.id == semesterId }
    Box(Modifier.analyticsScreen("GradeEntry")) {
        if (semester != null) {
            GradeEntryView(semester, state, onBack, viewModel::setGrade)
        } else {
            CreditScreen(stringResource(R.string.credit_enter_grade), onBack) { padding ->
                Box(Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center) {
                    when {
                        state.isLoading -> CircularProgressIndicator()
                        error != null -> ErrorView(
                            error = error,
                            defaultMessageResId = R.string.credit_load_error,
                            onRetry = { viewModel.load() }
                        )

                        else -> Text(stringResource(R.string.credit_empty))
                    }
                }
            }
        }

        GlobalAlertDialog(
            isPresented = viewModel.isAlertPresented,
            state = viewModel.alertState,
            onDismiss = { viewModel.isAlertPresented = false },
        )
    }
}

@Preview(name = "Grades", showBackground = true)
@Preview(name = "Grades dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GradeEntryRoutePreview() {
    val viewModel = remember { PreviewCreditCalculationViewModel() }
    Theme {
        GradeEntryRoute(viewModel.state.collectAsState().value.semesters.first().id, viewModel, rememberNavController())
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun GradeEntryLoadingPreview() {
    val viewModel = remember { PreviewCreditCalculationViewModel(CreditCalculationViewState()) }
    Theme { GradeEntryRoute("loading", viewModel, rememberNavController()) }
}

@Preview(name = "Load error - retry", showBackground = true)
@Composable
private fun GradeEntryErrorPreview() {
    val semesterId = remember { creditPreviewState().semesters.first().id }
    val viewModel = remember {
        PreviewCreditCalculationViewModel(CreditCalculationViewState(isLoading = false, error = IOException()))
    }
    Theme { GradeEntryRoute(semesterId, viewModel, rememberNavController()) }
}
