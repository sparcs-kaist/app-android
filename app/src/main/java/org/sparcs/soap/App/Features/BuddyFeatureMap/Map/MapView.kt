package org.sparcs.soap.App.Features.BuddyFeatureMap.Map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import org.sparcs.soap.App.Domain.Models.Map.CampusLocation

@Composable
fun MapView(
    locations: List<CampusLocation>,
    selectedLocation: CampusLocation?,
    onLocationSelected: (CampusLocation?) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialCenter = LatLng.from(36.3725, 127.3624)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                start(object : MapLifeCycleCallback() {
                    override fun onMapDestroy() {
                        // 지도 파괴 시 처리
                    }

                    override fun onMapError(error: Exception?) {
                        // 에러 처리
                    }
                }, object : KakaoMapReadyCallback() {
                    override fun onMapReady(kakaoMap: KakaoMap) {
                        val cameraUpdate = CameraUpdateFactory.newCenterPosition(initialCenter, 15)
                        kakaoMap.moveCamera(cameraUpdate)

                        renderMarkers(kakaoMap, locations, onLocationSelected)

//                        kakaoMap.setOnCameraMoveListener { map, cameraPosition, gestureType ->
//                            println("Zoom Level: ${cameraPosition.zoomLevel}")
//                        }
                    }
                })
            }
        },
        update = { mapView ->
        }
    )
}

private fun renderMarkers(
    kakaoMap: KakaoMap,
    locations: List<CampusLocation>,
    onLocationSelected: (CampusLocation?) -> Unit
) {
    val layer = kakaoMap.labelManager?.layer

    locations.forEach { location ->
        val styles = kakaoMap.labelManager?.addLabelStyles(
            LabelStyles.from(LabelStyle.from(android.R.drawable.ic_dialog_map)) // 임시 아이콘
        )

        val options = LabelOptions.from(location.coordinate)
            .setStyles(styles)
//            .setTexts(location.name)
            .setTag(location)
            .setClickable(true)

        layer?.addLabel(options)
    }

    // 마커 클릭 리스너
    kakaoMap.setOnLabelClickListener { _, _, label ->
        val clickedLocation = label.tag as? CampusLocation
        onLocationSelected(clickedLocation)
        true
    }
}