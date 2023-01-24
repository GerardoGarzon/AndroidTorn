package com.lebentech.lebentechtorniquetes.modules.camerax.listeners

import android.graphics.Bitmap
import android.media.Image

/**
 * Created by Gerardo Garzon on 02/01/23.
 */
interface ImageCapturedListener {
    fun imageCaptured(image: Bitmap)
}