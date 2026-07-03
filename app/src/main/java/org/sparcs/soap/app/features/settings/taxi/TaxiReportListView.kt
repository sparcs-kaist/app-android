package org.sparcs.soap.app.features.settings.taxi

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.taxi.TaxiReportType
import org.sparcs.soap.app.domain.enums.taxi.TaxiReports
import org.sparcs.soap.app.domain.models.taxi.TaxiReport
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.features.settings.components.TaxiReportDetailRow
import org.sparcs.soap.app.features.settings.components.TaxiReportDetailSkeletonRow
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.viewModelMocks.taxi.MockTaxiReportListViewModel
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.UnavailableView
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun TaxiReportListView(
    viewModel: TaxiReportListViewModelProtocol,
    navController: NavController,
) {
    var taxiReportType by remember { mutableStateOf(TaxiReportType.INCOMING) }
    val state by viewModel.state.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchReports()
    }

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.taxi_reports),
                onDismiss = { navController.popBackStack() }
            )
        },
        modifier = Modifier.analyticsScreen("Taxi Report List")
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            AnimatedSegmentedPicker(
                options = TaxiReportType.entries.map { stringResource(it.value) },
                selectedIndex = TaxiReportType.entries.indexOf(taxiReportType),
                onSelected = { index ->
                    taxiReportType = TaxiReportType.entries[index]
                }
            )

            Spacer(Modifier.height(16.dp))

            when (state) {
                is TaxiReportListViewModel.ViewState.Loading -> LoadingView()
                is TaxiReportListViewModel.ViewState.Loaded -> LoadedView(viewModel, taxiReportType)
                is TaxiReportListViewModel.ViewState.Error -> {
                    val error =
                        (viewModel.state.collectAsState().value as TaxiReportListViewModel.ViewState.Error).error
                    ErrorView(
                        error = error
                    ) { coroutineScope.launch { viewModel.fetchReports() } }
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Column {
        repeat(2) {
            TaxiReportDetailSkeletonRow()
        }
    }
}

@Composable
private fun LoadedView(
    viewModel: TaxiReportListViewModelProtocol,
    taxiReportType: TaxiReportType,
) {
    val reports = when (taxiReportType) {
        TaxiReportType.INCOMING -> viewModel.reports.incoming
        TaxiReportType.OUTGOING -> viewModel.reports.outgoing
    }

    ReportViewList(
        reports = reports,
        reportType = taxiReportType
    )
}

@Composable
private fun ReportViewList(
    reports: List<TaxiReport>,
    reportType: TaxiReportType,
) {
    if (reports.isEmpty()) {
        UnavailableView(
            Icons.Rounded.SearchOff, stringResource(R.string.no_reports), ""
        )
    } else {
        LazyColumn {
            items(reports) { report ->
                TaxiReportDetailRow(report, reportType)
            }
        }
    }
}

@Composable
fun AnimatedSegmentedPicker(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    val animProgress by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val segmentWidth = maxWidth / options.size

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(segmentWidth)
                .offset(x = segmentWidth * animProgress)
                .background(MaterialTheme.colorScheme.primary)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, option ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (index == selectedIndex) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}


@Preview
@Composable
private fun LoadingPreview() {
    Theme {
        TaxiReportListView(
            MockTaxiReportListViewModel(TaxiReportListViewModel.ViewState.Loading),
            rememberNavController()
        )
    }
}

@Preview
@Composable
private fun LoadedPreview() {
    val viewModel = MockTaxiReportListViewModel(TaxiReportListViewModel.ViewState.Loaded).apply {
        reports = TaxiReports(
            incoming = listOf(
                TaxiReport.mock()
            ),
            outgoing = TaxiReport.mockList()
        )
    }
    Theme { TaxiReportListView(viewModel, rememberNavController()) }

}

@Preview
@Composable
private fun ErrorPreview() {
    val viewModel =
        MockTaxiReportListViewModel(TaxiReportListViewModel.ViewState.Error(Exception()))
    Theme { TaxiReportListView(viewModel, rememberNavController()) }
}
