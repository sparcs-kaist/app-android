package com.example.soap.Features.PostCompose

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.soap.Domain.Models.Ara.AraBoard
import com.example.soap.Domain.Models.Ara.AraBoardTopic

interface PostComposeViewModelProtocol {
    val board: AraBoard
    var selectedTopic: AraBoardTopic?
    var title: String
    var content: String
    var selectedItems: List<Uri>
    var selectedImages: List<Bitmap>

    var writeAsAnonymous: Boolean
    var isNSFW: Boolean
    var isPolitical: Boolean

    suspend fun writePost()
    suspend fun updateSelectedImages(context: Context)

}