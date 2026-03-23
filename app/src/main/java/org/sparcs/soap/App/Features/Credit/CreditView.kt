package org.sparcs.soap.App.Features.Credit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.App.Features.NavigationBar.Components.DismissButton
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.isDark
import org.sparcs.soap.R

data class CreditMember(
    val name: String,
    val nickname: String,
    val roles: List<String>
)

val MEMBER_LIST = listOf(
    CreditMember("권순규", "soongyu", listOf("PM", "iOS")),
    CreditMember("장승혁", "hyuk", listOf("PM", "Backend")),
    CreditMember("윤인하", "amet", listOf("Android")),
    CreditMember("하정우", "thread", listOf("PM", "iOS")),
    CreditMember("박현우", "namu", listOf("Android")),
    CreditMember("이준엽", "orca", listOf("iOS")),
    CreditMember("김우현", "dreamer", listOf("Android")),
    CreditMember("김민찬", "static", listOf("Backend", "iOS")),
    CreditMember("이종현", "teddybear", listOf("Backend")),
    CreditMember("김민재", "neymar", listOf("iOS")),
    CreditMember("박찬혁", "bonk", listOf("Backend")),
    CreditMember("서인성", "cheese", listOf("Design")),
    CreditMember("김희진", "gimme", listOf("Design")),
    CreditMember("양채빈", "yatcha", listOf("Design")),
    CreditMember("임가은", "casio", listOf("Frontend")),
)

val CATEGORIES = listOf("PM", "iOS", "Android", "Backend", "Frontend", "Design")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditView(navController: NavController) {
    val logoColor = Color(0xFFD9A242)
    val nameColor = MaterialTheme.colorScheme.onSurface

    val mobisLogoRes = if (MaterialTheme.colorScheme.isDark()) {
        R.drawable.ic_mobis_logo_night
    } else {
        R.drawable.ic_mobis_logo
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = { DismissButton { navController.popBackStack() } },
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
        modifier = Modifier.analyticsScreen("Credit"),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            CATEGORIES.forEach { category ->
                val membersInRole = MEMBER_LIST.filter { it.roles.contains(category) }

                if (membersInRole.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier.width(IntrinsicSize.Min)
                                .padding(bottom = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                thickness = 1.dp,
                                color = logoColor
                            )
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            membersInRole.forEach { member ->
                                MemberIconCard(member, logoColor, nameColor)
                            }
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
            ) {
                Text(
                    text = stringResource(R.string.sponsored_by),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )
                Image(
                    painter = painterResource(mobisLogoRes),
                    contentDescription = "Hyundai Mobis",
                    modifier = Modifier.height(24.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun MemberIconCard(
    member: CreditMember,
    logoColor: Color,
    nameColor: Color
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .padding(6.dp)
            .shadow(elevation = 3.dp, shape = MaterialTheme.shapes.medium)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.sparcs_logo),
                contentDescription = null,
                modifier = Modifier.height(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = member.nickname,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = logoColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = member.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                color = nameColor
            )
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        CreditView(navController = rememberNavController())
    }
}