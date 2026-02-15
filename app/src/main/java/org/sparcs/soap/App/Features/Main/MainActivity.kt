package org.sparcs.soap.App.Features.Main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import org.sparcs.soap.App.Domain.Helpers.PopupManager
import org.sparcs.soap.App.Domain.Services.AuthenticationCallbackHandler
import org.sparcs.soap.App.Features.NavigationBar.MainTabBar
import org.sparcs.soap.App.Features.Settings.SettingsViewModel
import org.sparcs.soap.App.Features.SignIn.SignInView
import org.sparcs.soap.App.InAppUpdateHelper
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private lateinit var helper: InAppUpdateHelper
    private val snackbarHostState = SnackbarHostState() // 유연한 인앱 업데이트용 스낵 바

    // 인앱 업데이트 결과 처리 런처
    private val appUpdateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult -> helper.onActivityResult(result.resultCode) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        helper = InAppUpdateHelper(this, appUpdateLauncher, 4, snackbarHostState, lifecycleScope)
        helper.check()

        intent?.data?.let { uri ->
            AuthenticationCallbackHandler.handleUri(uri)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setContent {
            val darkMode by settingsViewModel.darkModeSetting.collectAsState(initial = null)
            val useDarkTheme = darkMode ?: isSystemInDarkTheme()

            Theme(darkTheme = useDarkTheme) {
                val mustUpdate by viewModel.mustUpdate.collectAsState()
                val isAuthenticated by viewModel.isAuthenticated.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()

                var showNotice by remember {
                    mutableStateOf(PopupManager.shouldShowPopup(this@MainActivity, 7))
                }

                LaunchedEffect(Unit) {
                    val currentVersion =
                        packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
                    viewModel.checkVersion(currentVersion)
                }

                LaunchedEffect(mustUpdate) {
                    if (mustUpdate) {
                        helper.forceStart()
                    }
                }

                if (isLoading || isAuthenticated == null) {
                    // MARK: THIS PLAYS CRUCIAL ROLE HIDING SIGN IN VIEW ON LOADING
                } else {
                    if (isAuthenticated == true) {
                        /* TODO: 공지사항 팝업 기능 활성화 시 주석 해제
                        if (showNotice) {
                            NoticeDialog(
                                onDismiss = { showNotice = false },
                                onDoNotShowAgain = {
                                    PopupManager.saveIgnoreTimestamp(this@MainActivity)
                                    showNotice = false
                                }
                            )
                        }
                        */
                        Box(modifier = Modifier.fillMaxSize()) {
                            MainTabBar()
                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 20.dp)
                            ) {
                                Snackbar(
                                    snackbarData = it,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    } else {
                        SignInView()
                    }
                }
            }
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { uri ->
            AuthenticationCallbackHandler.handleUri(uri)
        }
    }

    override fun onResume() {
        super.onResume()
        helper.resumeCheck()
        viewModel.checkAuthOnResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        helper.onDestroy()
    }
}

@Composable
fun NoticeDialog(
    onDismiss: () -> Unit,
    onDoNotShowAgain: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(300.dp)
        ) {
            Column {
                Image(
                    imageVector = Icons.Rounded.Search, //TODO: 팝업 임시 이미지
                    contentDescription = "Popup Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onDismiss() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.close),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    VerticalDivider(modifier = Modifier.height(20.dp), thickness = 1.dp)

                    TextButton(
                        onClick = { onDoNotShowAgain() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.never_show_again),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}