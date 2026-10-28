package org.sparcs.soap.screenshots

import android.security.keystore.KeyGenParameterSpec
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.navigation.compose.rememberNavController
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.features.post.PostView
import org.sparcs.soap.app.features.post.PostViewModel
import org.sparcs.soap.app.features.settings.SettingsView
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.viewModelMocks.MockSettingsViewModel
import org.sparcs.soap.app.shared.viewModelMocks.ara.MockPostViewModel
import org.sparcs.soap.app.theme.ui.Theme
import java.security.Key
import java.security.KeyStoreSpi
import java.security.Provider
import java.security.SecureRandom
import java.security.Security
import java.security.cert.Certificate
import java.security.spec.AlgorithmParameterSpec
import java.util.Collections
import java.util.Date
import java.util.Enumeration
import javax.crypto.KeyGenerator
import javax.crypto.KeyGeneratorSpi
import javax.crypto.SecretKey

/**
 * Captures screens whose generated preview tests cannot run under Robolectric:
 * SettingsView needs a FirebaseApp and PostView's TokenStorage needs the AndroidKeyStore.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xhdpi")
class ScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        FakeAndroidKeyStore.install()
        val context = RuntimeEnvironment.getApplication()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("1:1:android:1")
                    .setApiKey("screenshot")
                    .setProjectId("screenshot")
                    .build()
            )
        }
    }

    @Test
    fun settingsView() {
        composeRule.setContent {
            Theme { SettingsView(rememberNavController(), MockSettingsViewModel()) }
        }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/screens/SettingsView.png")
    }

    @Test
    fun postView() {
        composeRule.setContent {
            val viewModel = remember {
                MockPostViewModel(initialState = PostViewModel.ViewState.Loaded, post = AraPost.mock())
            }
            Theme { PostView(viewModel = viewModel, navController = rememberNavController()) }
        }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/screens/PostView.png")
    }
}

private object FakeAndroidKeyStore {
    val keys = mutableMapOf<String, SecretKey>()

    fun install() {
        if (Security.getProvider("AndroidKeyStore") != null) return
        Security.addProvider(object : Provider("AndroidKeyStore", 1.0, "Fake AndroidKeyStore for tests") {
            init {
                put("KeyStore.AndroidKeyStore", FakeKeyStoreSpi::class.java.name)
                put("KeyGenerator.AES", FakeAesKeyGenerator::class.java.name)
            }
        })
    }
}

class FakeKeyStoreSpi : KeyStoreSpi() {
    private val keys get() = FakeAndroidKeyStore.keys
    override fun engineGetKey(alias: String, password: CharArray?): Key? = keys[alias]
    override fun engineGetCertificateChain(alias: String): Array<Certificate>? = null
    override fun engineGetCertificate(alias: String): Certificate? = null
    override fun engineGetCreationDate(alias: String): Date = Date()
    override fun engineSetKeyEntry(alias: String, key: Key, password: CharArray?, chain: Array<out Certificate>?) {
        keys[alias] = key as SecretKey
    }
    override fun engineSetKeyEntry(alias: String, key: ByteArray, chain: Array<out Certificate>?) = Unit
    override fun engineSetCertificateEntry(alias: String, cert: Certificate) = Unit
    override fun engineDeleteEntry(alias: String) { keys.remove(alias) }
    override fun engineAliases(): Enumeration<String> = Collections.enumeration(keys.keys.toList())
    override fun engineContainsAlias(alias: String): Boolean = alias in keys
    override fun engineSize(): Int = keys.size
    override fun engineIsKeyEntry(alias: String): Boolean = alias in keys
    override fun engineIsCertificateEntry(alias: String): Boolean = false
    override fun engineGetCertificateAlias(cert: Certificate): String? = null
    override fun engineStore(stream: java.io.OutputStream?, password: CharArray?) = Unit
    override fun engineLoad(stream: java.io.InputStream?, password: CharArray?) = Unit
}

class FakeAesKeyGenerator : KeyGeneratorSpi() {
    private var alias = "default"
    override fun engineInit(random: SecureRandom?) = Unit
    override fun engineInit(params: AlgorithmParameterSpec?, random: SecureRandom?) {
        (params as? KeyGenParameterSpec)?.let { alias = it.keystoreAlias }
    }
    override fun engineInit(keysize: Int, random: SecureRandom?) = Unit
    override fun engineGenerateKey(): SecretKey {
        val key = KeyGenerator.getInstance("AES", "SunJCE").apply { init(256) }.generateKey()
        FakeAndroidKeyStore.keys[alias] = key
        return key
    }
}
