package com.lebentech.lebentechtorniquetes.interfaces

/**
 * Created by Gerardo Garzon on 13/01/23.
 */
interface FaceRecognitionListener {
    fun faceDetected()
    fun faceTooFar()
    fun faceTooClose()
    fun noFaceDetection()
}