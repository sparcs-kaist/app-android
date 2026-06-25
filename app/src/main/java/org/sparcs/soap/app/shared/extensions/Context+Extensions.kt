package org.sparcs.soap.app.shared.extensions

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import timber.log.Timber

fun Context.openUri(uri: Uri, packageName: String? = null) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        this.startActivity(intent)
    } catch (_: Exception) {
        if (packageName != null) {
            try {
                val marketIntent = Intent(Intent.ACTION_VIEW,
                    "market://details?id=$packageName".toUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                this.startActivity(marketIntent)
            } catch (_: Exception) {
                val webIntent =
                    Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                this.startActivity(webIntent)
            }
        } else {
            Toast.makeText(this, "앱을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}

fun Context.vectorToBitmap(drawableId: Int, tintColor: Int? = null): Bitmap? {
    val drawable = ContextCompat.getDrawable(this, drawableId) ?: return null

    tintColor?.let {
        drawable.setTint(it)
    }
    val sizeInPx = (32 * resources.displayMetrics.density).toInt()

    val bitmap = createBitmap(sizeInPx, sizeInPx)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun Context.createKakaoMap(
    room: TaxiRoom,
    onMapReady: (KakaoMap) -> Unit,
    onError: () -> Unit,
): View {
    return try {
        MapView(this).apply {
            start(object : MapLifeCycleCallback() {
                override fun onMapDestroy() {}
                override fun onMapError(e: Exception?) { Timber.e(e); onError() }
            }, object : KakaoMapReadyCallback() {
                override fun onMapReady(map: KakaoMap) {
                    onMapReady(map)
                    val points = arrayOf(
                        LatLng.from(room.source.latitude, room.source.longitude),
                        LatLng.from(room.destination.latitude, room.destination.longitude)
                    )
                    map.moveCamera(CameraUpdateFactory.fitMapPoints(points, 100))
                }
            })
        }
    } catch (e: Throwable) {
        Timber.e(e, "Kakao Map load failed")
        onError()
        View(this)
    }
}