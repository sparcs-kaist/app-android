package org.sparcs.soap.imageUploadTests

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.sparcs.soap.app.shared.extensions.compressForUpload
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompressForUploadTest {

    private fun noise(width: Int, height: Int): Bitmap {
        val random = Random(42)
        val pixels = IntArray(width * height) { 0xFF000000.toInt() or random.nextInt(0xFFFFFF) }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun decodedSize(bytes: ByteArray): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        return options.outWidth to options.outHeight
    }

    @Test
    fun `large photo keeps far more than the old 500px cap`() {
        val bytes = noise(1600, 1200).compressForUpload(maxSizeMB = 10.0, maxDimension = 2048)
        assertEquals(1600 to 1200, decodedSize(bytes))
    }

    @Test
    fun `longest side is capped at maxDimension and aspect ratio is kept`() {
        val (width, height) = decodedSize(noise(3000, 1500).compressForUpload(maxSizeMB = 10.0, maxDimension = 2048))
        assertEquals(2048, width)
        assertEquals(1024, height)
    }

    @Test
    fun `small images are never upscaled`() {
        assertEquals(300 to 200, decodedSize(noise(300, 200).compressForUpload(maxSizeMB = 10.0, maxDimension = 2048)))
    }

    @Test
    fun `output fits the size budget by shrinking when quality alone is not enough`() {
        val bytes = noise(2000, 2000).compressForUpload(maxSizeMB = 0.3, maxDimension = 2048)
        assertTrue("was ${bytes.size} bytes", bytes.size <= 0.3 * 1024 * 1024)
        assertTrue(decodedSize(bytes).first < 2000)
    }

    @Test
    fun `an impossible budget still terminates with a valid image at the minimum size`() {
        val bytes = noise(2000, 2000).compressForUpload(maxSizeMB = 0.0001, maxDimension = 2048)
        assertEquals(640 to 640, decodedSize(bytes))
    }
}
