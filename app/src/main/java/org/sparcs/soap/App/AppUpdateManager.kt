package org.sparcs.soap.App

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sparcs.soap.R

class InAppUpdateHelper(
    private val activity: Activity,
    private val launcher: ActivityResultLauncher<IntentSenderRequest>,
    private val priorityThreshold: Int = 4, //중요도. 이거 이상이면 강제 업데이트 창 띄움
    private val snackbarHostState: SnackbarHostState,
    private val scope: CoroutineScope
) {
    private val manager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var currentUpdateType: Int? = null

    // 유연한 업데이트에서 다운로드 후 재시작 스낵바를 띄우는 리스너
    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showUpdateSnackbar()
        }
    }

    fun check() {
        // 리스너 중복 등록 방지
        manager.unregisterListener(installStateUpdatedListener)
        manager.registerListener(installStateUpdatedListener)
        
        manager.appUpdateInfo.addOnSuccessListener { info ->
            val available = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val flexibleAllowed = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            val immediateAllowed = info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            val priority = info.updatePriority()
            
            if (available && immediateAllowed && priority >= priorityThreshold) {
                start(info, AppUpdateType.IMMEDIATE)
            } else if (available && flexibleAllowed) {
                start(info, AppUpdateType.FLEXIBLE)
            }

            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                start(info, AppUpdateType.IMMEDIATE)
            }
        }.addOnFailureListener {
            Log.w("InAppUpdateHelper", it.message ?: "")
        }
    }

    private fun start(info: AppUpdateInfo, type: Int) {
        currentUpdateType = type
        manager.startUpdateFlowForResult(
            info,
            launcher,
            AppUpdateOptions.newBuilder(type).build()
        )
    }
    
    /**
     * 사용자가 업데이트 UI에서 취소하거나 실패했을 때의 처리를 담당. (특히 강제 업데이트인 경우 앱 종료)
     */
    fun onActivityResult(resultCode: Int) {
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.d("InAppUpdateHelper", "CANCELED")
        }
        if (resultCode != Activity.RESULT_OK) {
            Log.d("InAppUpdateHelper", "Update flow failed! Result code: $resultCode")
            // 강제 업데이트(IMMEDIATE)였는데 취소/실패했다면 앱 종료
            if (currentUpdateType == AppUpdateType.IMMEDIATE) {
                activity.finishAffinity()
            }
        }
    }

    // 유연한 업데이트용
    private fun showUpdateSnackbar() {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = activity.getString(R.string.inapp_snackbar_explanation),
                actionLabel = activity.getString(R.string.restart),
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                manager.completeUpdate()
            }
        }
    }

    fun resumeCheck() {
        manager.appUpdateInfo.addOnSuccessListener { info ->
            // 유연한 업데이트용
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                showUpdateSnackbar()
            }
            // 즉시 업데이트용
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                start(info, AppUpdateType.IMMEDIATE)
            }
        }
    }

    fun forceStart() {
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                start(info, AppUpdateType.IMMEDIATE)
            } else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${activity.packageName}"))
                activity.startActivity(intent)
            }
        }
    }

    fun onDestroy() {
        manager.unregisterListener(installStateUpdatedListener)
    }
}
