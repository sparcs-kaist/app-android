package org.sparcs.soap.App.Shared.Extensions

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import com.kakao.vectormap.label.LabelTextStyle
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import org.sparcs.soap.R

fun KakaoMap.drawTaxiRoute(
    context: Context,
    startPos: LatLng,
    endPos: LatLng,
    pathPoints: List<LatLng>,
    pathColor: Int,
    startLabel: String,
    endLabel: String,
    startIconColor: Int,
    endIconColor: Int
) {
    val labelManager = this.labelManager ?: return
    val layer = labelManager.layer ?: return
    val routeLayer = this.routeLineManager?.layer ?: return

    layer.removeAll()
    routeLayer.removeAll()

    val startIconBitmap = context.vectorToBitmap(R.drawable.round_location_on, startIconColor)
    val endIconBitmap = context.vectorToBitmap(R.drawable.round_location_on, endIconColor)

    if (startIconBitmap != null && endIconBitmap != null) {
        val startIconStyle = LabelStyles.from(LabelStyle.from(startIconBitmap).setAnchorPoint(0.5f, 1.0f))
        val endIconStyle = LabelStyles.from(LabelStyle.from(endIconBitmap).setAnchorPoint(0.5f, 1.0f))

        val textStyle = LabelStyles.from(
            LabelStyle.from(LabelTextStyle.from(20, Color.WHITE).setFont(Typeface.BOLD))
        )

        layer.addLabel(LabelOptions.from("start_icon", startPos).setStyles(startIconStyle).setRank(100))
        layer.addLabel(LabelOptions.from("end_icon", endPos).setStyles(endIconStyle).setRank(100))

        val startTextLabel = layer.addLabel(
            LabelOptions.from("start_text", startPos).setStyles(textStyle).setTexts(LabelTextBuilder().setTexts(startLabel)).setRank(200)
        )
        val endTextLabel = layer.addLabel(
            LabelOptions.from("end_text", endPos).setStyles(textStyle).setTexts(LabelTextBuilder().setTexts(endLabel)).setRank(200)
        )

        val offsetY = -50f
        startTextLabel?.changePixelOffset(0f, offsetY)
        endTextLabel?.changePixelOffset(0f, offsetY)
    }

    if (pathPoints.isNotEmpty()) {
        val segment = RouteLineSegment.from(pathPoints)
            .setStyles(RouteLineStyle.from(12f, pathColor))
        routeLayer.addRouteLine(RouteLineOptions.from(segment))

        this.moveCamera(
            CameraUpdateFactory.fitMapPoints(pathPoints.toTypedArray(), 100),
            CameraAnimation.from(1000)
        )
    } else {
        this.moveCamera(
            CameraUpdateFactory.fitMapPoints(arrayOf(startPos, endPos), 100),
            CameraAnimation.from(800)
        )
    }
}