package com.lebentech.lebentechtorniquetes.retrofit.request

/**
 * Created by Gerardo Garzon on 16/01/23.
 */
class FaceRecognitionRequest (
    var idDispositivo: String,
    var foto: ByteArray,
    var torniquete: Int = 0
)