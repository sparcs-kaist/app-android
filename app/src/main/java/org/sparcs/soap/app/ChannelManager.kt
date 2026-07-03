package org.sparcs.soap.app

import android.app.Activity
import android.app.Application
import com.zoyi.channel.plugin.android.ChannelIO
import com.zoyi.channel.plugin.android.open.config.BootConfig
import com.zoyi.channel.plugin.android.open.enumerate.BootStatus
import com.zoyi.channel.plugin.android.open.model.Profile
import io.channel.plugin.android.open.model.Appearance
import timber.log.Timber

object ChannelManager {
    private const val TAG = "ChannelManager"
    private const val DEFAULT_CONSULTATION_TAG = "buddy/app-android"

    private var initialized = false
    private var lastPluginKey: String? = null

    fun initialize(
        application: Application,
        pluginKey: String?,
        name: String?,
        email: String?,
        mobile: String?,
        memberId: String?,
        consultationTag: String? = DEFAULT_CONSULTATION_TAG,
        extraProperties: Map<String, Any?> = emptyMap(),
    ) {
        try {
            lastPluginKey = pluginKey
            if (pluginKey.isNullOrBlank()) {
                Timber.w("%s: plugin key is blank; ChannelTalk may not boot", TAG)
            }

            ChannelIO.initialize(application)
            Timber.i("%s: ChannelIO.initialize() called", TAG)

            val profile = Profile.create()
            try { profile.setName(name ?: "Unknown") } catch (_: Exception) {}
            try { profile.setEmail(email ?: "") } catch (_: Exception) {}
            try { profile.setProperty("mobileNumber", mobile ?: "") } catch (_: Exception) {}
            try { if (!consultationTag.isNullOrBlank()) profile.setProperty("consultationTag", consultationTag) } catch (_: Exception) {}
            extraProperties.forEach { (key, value) ->
                if (value != null) {
                    try { profile.setProperty(key, value) } catch (_: Exception) {}
                }
            }

            val bootConfig = BootConfig.create(pluginKey ?: "")
            try {
                bootConfig.memberId = memberId
            } catch (_: Exception) {}
            try { bootConfig.setProfile(profile) } catch (_: Exception) {}

            ChannelIO.boot(bootConfig) { status, _ ->
                if (status != BootStatus.SUCCESS) {
                    Timber.w("%s: boot failed on initialize with status=%s", TAG, status)
                }
            }
            Timber.i("%s: ChannelIO.boot() called", TAG)

            try { ChannelIO.hideChannelButton() } catch (e: Exception) { Timber.w(e, "%s: hideChannelButton failed", TAG) }

            initialized = true
            Timber.i("%s: initialized", TAG)
        } catch (e: Exception) {
            Timber.e(e, "%s: ChannelManager initialization failed", TAG)
        }
    }

    fun updateProfile(
        name: String?,
        email: String?,
        mobile: String?,
        memberId: String?,
        consultationTag: String? = DEFAULT_CONSULTATION_TAG,
        extraProperties: Map<String, Any?> = emptyMap(),
    ) {
        try {
            val pluginKey = lastPluginKey
            if (pluginKey.isNullOrBlank()) {
                Timber.w("%s: updateProfile skipped because plugin key is blank", TAG)
                return
            }

            val profile = Profile.create()
            try { profile.setName(name ?: "Unknown") } catch (_: Exception) {}
            try { profile.setEmail(email ?: "") } catch (_: Exception) {}
            try { profile.setProperty("mobileNumber", mobile ?: "") } catch (_: Exception) {}
            try { if (!consultationTag.isNullOrBlank()) profile.setProperty("consultationTag", consultationTag) } catch (_: Exception) {}
            extraProperties.forEach { (key, value) ->
                if (value != null) {
                    try { profile.setProperty(key, value) } catch (_: Exception) {}
                }
            }

            val bootConfig = BootConfig.create(pluginKey)
            try {
                bootConfig.memberId = memberId
            } catch (_: Exception) {}
            try { bootConfig.setProfile(profile) } catch (_: Exception) {}

            ChannelIO.boot(bootConfig) { status, _ ->
                if (status != BootStatus.SUCCESS) {
                    Timber.w("%s: boot failed on updateProfile with status=%s", TAG, status)
                }
            }
            Timber.i("%s: profile updated", TAG)
        } catch (e: Exception) {
            Timber.e(e, "%s: updateProfile failed", TAG)
        }
    }

    fun clearIdentity() {
        try {
            ChannelIO.shutdown()
            Timber.i("%s: ChannelIO.shutdown() called", TAG)
            initialized = false
            lastPluginKey = null
        } catch (e: Exception) {
            Timber.w(e, "%s: clearIdentity failed", TAG)
        }
    }

    fun showMessenger(activity: Activity, chatTag: String? = DEFAULT_CONSULTATION_TAG) {
        try {
            ChannelIO.showMessenger(activity)
            Timber.i("%s: ChannelIO.showMessenger() called", TAG)
            if (!chatTag.isNullOrBlank()) {
                try {
                    ChannelIO.addTags(chatTag)
                    Timber.i("%s: chat tag added: %s", TAG, chatTag)
                } catch (e: Exception) {
                    Timber.w(e, "%s: failed to add chat tag", TAG)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "%s: showMessenger failed", TAG)
        }
    }

    fun syncAppearance(isDark: Boolean) {
        try {
            val appearance = if (isDark) Appearance.DARK else Appearance.LIGHT
            ChannelIO.setAppearance(appearance)
            Timber.i("%s: ChannelTalk appearance set to %s", TAG, appearance)
        } catch (e: Exception) {
            Timber.w(e, "%s: syncAppearance failed", TAG)
        }
    }

}
