package org.sparcs.App

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class InAppUpdateHelper(
    private val activity: Activity,
    private val launcher: ActivityResultLauncher<IntentSenderRequest>,
    private val priorityThreshold: Int = 4, //중요도. 이거 이상이면 강제 업데이트 창 띄움
    private val exitIfDeclined: Boolean = true
) {
    private val manager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    fun check() {
        manager.appUpdateInfo.addOnSuccessListener { info ->
            val available = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val immediateAllowed = info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            val priority = info.updatePriority()

            if (available && immediateAllowed && priority >= priorityThreshold) {
                prompt(info)
            }

            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                start(info)
            }
        }.addOnFailureListener {
            Log.w("InAppUpdateHelper", it.message ?: "")
        }
    }

    private fun prompt(info: AppUpdateInfo) {
        AlertDialog.Builder(activity)
            .setTitle("UPDATE")
            .setMessage("REQUIRED")
            .setCancelable(!exitIfDeclined)
            .setPositiveButton("OK") { _, _ -> start(info) }
            .apply {
                if (exitIfDeclined) {
                    setNegativeButton("EXIT") { _, _ -> activity.finishAffinity() }
                }
            }
            .show()
    }

    private fun start(info: AppUpdateInfo) {
        manager.startUpdateFlowForResult(
            info,
            launcher,
            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
        )
    }

    fun resumeCheck() {
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                manager.completeUpdate()
            }
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                start(info)
            }
        }
    }
}
