package org.sparcs.soap.App.Features.Credit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.App.Features.NavigationBar.Components.DismissButton
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.isDark
import org.sparcs.soap.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditView(navController: NavController) {
    val credits: String = """
    권순규(soongyu)
    장승혁(hyuk)
    윤인하(amet)
    하정우(thread)
    박현우(namu)
    이준엽(orca)
    김우현(dreamer)
    박찬혁(bonk)
    김민찬(static)
    이종현(teddybear)
    김민재(neymar)
    박현우(namu)
    김희진(gimme)
    양채빈(yatcha)
    서인성(cheese)
    임가은(casio)
""".trimIndent()

    val logoColor = Color(0xFFD9A242)
    val regex = Regex("""(.+)\((.+)\)""")
    val memberLines = credits.lines().filter { it.isNotBlank() }

    val mobisLogoRes = if (MaterialTheme.colorScheme.isDark()) {
        R.drawable.ic_mobis_logo_night
    } else {
        R.drawable.ic_mobis_logo
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    DismissButton { navController.popBackStack() }
                },
                title = {
                    Text(
                        text = stringResource(R.string.acknowledgements),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = Modifier.analyticsScreen("Credit")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            memberLines.forEach { line ->
                val matchResult = regex.find(line)
                if (matchResult != null) {
                    val (name, nickname) = matchResult.destructured

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Icon(
                            painter = painterResource(R.drawable.sparcs_logo),
                            contentDescription = null,
                            modifier = Modifier.height(20.dp),
                            tint = Color.Unspecified
                        )

                        Text(
                            text = nickname,
                            style = MaterialTheme.typography.bodyMedium,
                            color = logoColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(text = line)
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.sponsored_by),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(8.dp)
                )

                Image(
                    painter = painterResource(mobisLogoRes),
                    contentDescription = "Hyundai Mobis",
                    modifier = Modifier
                        .height(30.dp)
                        .padding(horizontal = 40.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(1.dp))
        }
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        CreditView(rememberNavController())
    }
}