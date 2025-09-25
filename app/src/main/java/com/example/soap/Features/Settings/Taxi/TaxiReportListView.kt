package com.example.soap.Features.Settings.Taxi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Enums.TaxiReportType
import com.example.soap.Domain.Models.Taxi.TaxiReport
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.Settings.Components.SettingsViewNavigationBar
import com.example.soap.Features.Settings.Components.TaxiReportDetailRow
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.Shared.ViewModelMocks.MockTaxiReportListViewModel
import com.example.soap.ui.theme.Theme

@Composable
fun TaxiReportListView(
    viewModel: TaxiReportListViewModelProtocol,
    navController: NavController
) {
    var taxiReportType by remember { mutableStateOf(TaxiReportType.INCOMING) }
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = "Taxi Reports",
                onDismiss = { navController.navigate(Channel.TaxiSettings.name )}
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                TaxiReportType.entries.forEach { type ->
                    Button(
                        onClick = { taxiReportType = type },
                        modifier = Modifier.weight(1f),
                        colors = if (taxiReportType == type) ButtonDefaults.buttonColors()
                        else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(type.name)
                    }
                }
            }

            when (state) {
                is TaxiReportListViewModel.ViewState.Loading -> LoadingView()
                is TaxiReportListViewModel.ViewState.Loaded -> LoadedView(viewModel, taxiReportType)
                is TaxiReportListViewModel.ViewState.Error -> {
                    val message = (viewModel.state.collectAsState().value as TaxiReportListViewModel.ViewState.Error).message
                    ErrorView(
                        Icons.Default.Warning,
                        message,
                        { //Todo- refresh
                             }
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorView(icon: ImageVector, message: String, onRetry: () -> Unit){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Error",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Error",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onRetry() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Try Again")
        }
    }
}

@Composable
private fun LoadingView() {
    Column {
        repeat(2) {

        }
    }
}

@Composable
private fun LoadedView(
    viewModel: TaxiReportListViewModelProtocol,
    taxiReportType: TaxiReportType
) {
    val reports = when (taxiReportType) {
        TaxiReportType.INCOMING -> viewModel.reports.incoming
        TaxiReportType.OUTGOING -> viewModel.reports.outgoing
    }

    ReportViewList(reports = reports)
}

@Composable
private fun ReportViewList(reports: List<TaxiReport>){
    if (reports.isEmpty()) {
        ErrorView(Icons.Default.Clear, "No Reports", {/* Refresh */})
    } else {
        LazyColumn {
            items(reports) { report ->
                TaxiReportDetailRow(report)
            }
        }
    }
}


@Preview
@Composable
private fun LoadingPreview() {
    Theme { TaxiReportListView(MockTaxiReportListViewModel(TaxiReportListViewModel.ViewState.Loading), rememberNavController()) }
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
    val viewModel = MockTaxiReportListViewModel(TaxiReportListViewModel.ViewState.Error("Network error"))
    Theme { TaxiReportListView(viewModel, rememberNavController()) }
}
