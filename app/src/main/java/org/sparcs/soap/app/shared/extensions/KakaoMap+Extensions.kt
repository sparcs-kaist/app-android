package org.sparcs.soap.app.shared.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapGravity
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import org.sparcs.soap.R

/**
 * Keeps the camera's fits inside the part of the map left clear by whatever is drawn over its
 * edges, and room for the markers around their points; and Kakao's logo in the clear part's
 * bottom-right corner ([logoRight], [logoBottom] from the map's edges), all in pixels.
 */
fun KakaoMap.setSafeArea(left: Int, top: Int, right: Int, bottom: Int, logoRight: Int, logoBottom: Int) {
    // The padding carries the logo in with it, so its offset is from the padded corner.
    setPadding(left, top, right, bottom)
    logo?.setPosition(MapGravity.BOTTOM or MapGravity.RIGHT, (logoRight - right).toFloat(), (logoBottom - bottom).toFloat())
}

/** How a taxi route is drawn on the map: colours from the app's theme, shapes from Material 3 Expressive. */
class TaxiRouteStyle(
    val routeColor: Int,
    val routeOutlineColor: Int,
    val start: Stop,
    val end: Stop,
    val labelContainerColor: Int,
    val labelContentColor: Int,
) {
    class Stop(
        val title: String,
        val shape: RoundedPolygon,
        val containerColor: Int,
        val contentColor: Int,
        @param:DrawableRes val glyph: Int,
    )
}

fun KakaoMap.drawTaxiRoute(
    context: Context,
    startPos: LatLng,
    endPos: LatLng,
    pathPoints: List<LatLng>,
    style: TaxiRouteStyle,
    // Kept around the route inside the map's safe area, in pixels.
    fitPadding: Int = 100,
) {
    val labelManager = this.labelManager ?: return
    val layer = labelManager.layer ?: return
    val routeLayer = this.routeLineManager?.layer ?: return

    layer.removeAll()
    routeLayer.removeAll()

    val density = context.resources.displayMetrics.density

    // Under the markers, which sit on its ends.
    if (pathPoints.isNotEmpty()) {
        val segment = RouteLineSegment.from(pathPoints)
            .setStyles(RouteLineStyle.from(3.5f * density, style.routeColor, 0.5f * density, style.routeOutlineColor))
        routeLayer.addRouteLine(RouteLineOptions.from(segment))
    }

    listOf(startPos to style.start, endPos to style.end).forEachIndexed { index, (position, stop) ->
        val marker = context.stopMarker(stop, style) ?: return@forEachIndexed
        val labelStyle = LabelStyle.from(marker.bitmap).setAnchorPoint(0.5f, marker.anchorY)
        layer.addLabel(
            LabelOptions.from("stop_$index", position)
                .setStyles(LabelStyles.from(labelStyle))
                .setRank(100L + index)
        )
    }

    this.moveCamera(
        CameraUpdateFactory.fitMapPoints(pathPoints.ifEmpty { listOf(startPos, endPos) }.toTypedArray(), fitPadding),
        CameraAnimation.from(if (pathPoints.isNotEmpty()) 1000 else 800)
    )
}

private class StopMarker(val bitmap: Bitmap, val anchorY: Float)

/**
 * The stop's shape, centred on it, with its glyph, and its title in a pill beneath: a raised
 * badge, ringed in the label's colour so it stands off the map and the route under it.
 */
private fun Context.stopMarker(stop: TaxiRouteStyle.Stop, style: TaxiRouteStyle): StopMarker? {
    val icon = ContextCompat.getDrawable(this, stop.glyph)?.mutate() ?: return null
    val density = resources.displayMetrics.density
    fun dp(value: Float) = value * density

    val badge = dp(26f)
    val ring = dp(1.5f)
    val margin = dp(4f) // Room for the shadows.
    val gap = dp(3f)
    val pillPaddingX = dp(8f)
    val pillHeight = dp(20f)

    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(11f)
        typeface = Typeface.create(Typeface.DEFAULT, 500, false)
        textAlign = Paint.Align.CENTER
        color = style.labelContentColor
    }
    val text = TextUtils.ellipsize(stop.title, textPaint, dp(140f), TextUtils.TruncateAt.END).toString()
    val pillWidth = textPaint.measureText(text) + pillPaddingX * 2

    val width = (maxOf(badge + ring * 2, pillWidth) + margin * 2).toInt()
    val centerX = width / 2f
    // Centred on the stop, so the bitmap is as tall below the badge's centre as above it.
    val centerY = margin + ring + badge / 2
    val pillTop = centerY + badge / 2 + ring + gap
    val contentBottom = pillTop + pillHeight + margin
    val height = (centerY + maxOf(contentBottom - centerY, centerY)).toInt()

    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    val shadow = 0x26000000

    // The shape, scaled from its unit square onto the badge, and again a little larger for its ring.
    fun shapePath(size: Float) = stop.shape.toPath().apply {
        val bounds = RectF().also { computeBounds(it, true) }
        transform(Matrix().apply {
            setRectToRect(
                bounds,
                RectF(centerX - size / 2, centerY - size / 2, centerX + size / 2, centerY + size / 2),
                Matrix.ScaleToFit.CENTER
            )
        })
    }
    canvas.drawPath(shapePath(badge + ring * 2), Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = style.labelContainerColor
        setShadowLayer(dp(2f), 0f, dp(0.5f), shadow)
    })
    canvas.drawPath(shapePath(badge), Paint(Paint.ANTI_ALIAS_FLAG).apply { color = stop.containerColor })

    val glyphSize = dp(14f).toInt()
    icon.setTint(stop.contentColor)
    // Centred on its ink rather than its box, so an arrow pointing into a corner looks centred.
    val (inkX, inkY) = icon.inkCentre(glyphSize)
    val glyphLeft = centerX - inkX
    val glyphTop = centerY - inkY
    icon.setBounds(
        glyphLeft.toInt(),
        glyphTop.toInt(),
        (glyphLeft + glyphSize).toInt(),
        (glyphTop + glyphSize).toInt(),
    )
    icon.draw(canvas)

    val pill = RectF(centerX - pillWidth / 2, pillTop, centerX + pillWidth / 2, pillTop + pillHeight)
    canvas.drawRoundRect(pill, pillHeight / 2, pillHeight / 2, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = style.labelContainerColor
        setShadowLayer(dp(1.5f), 0f, dp(0.5f), shadow)
    })
    val baseline = pill.centerY() - (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2
    canvas.drawText(text, centerX, baseline, textPaint)

    return StopMarker(bitmap, centerY / height)
}

/** Where the drawable's ink balances, drawn [size] pixels square: the centre of its opaque pixels. */
private fun android.graphics.drawable.Drawable.inkCentre(size: Int): Pair<Float, Float> {
    val sample = createBitmap(size, size)
    setBounds(0, 0, size, size)
    draw(Canvas(sample))
    val pixels = IntArray(size * size).also { sample.getPixels(it, 0, size, 0, 0, size, size) }
    sample.recycle()
    var mass = 0.0
    var sumX = 0.0
    var sumY = 0.0
    pixels.forEachIndexed { index, pixel ->
        val alpha = (pixel ushr 24).toDouble()
        if (alpha > 0) {
            mass += alpha
            sumX += alpha * (index % size + 0.5)
            sumY += alpha * (index / size + 0.5)
        }
    }
    return if (mass == 0.0) size / 2f to size / 2f else (sumX / mass).toFloat() to (sumY / mass).toFloat()
}
