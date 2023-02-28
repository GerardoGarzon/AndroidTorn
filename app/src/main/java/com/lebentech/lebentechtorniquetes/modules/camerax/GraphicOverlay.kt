/**
 * Created by Gerardo Garzon on 22/12/22.
 */

package com.lebentech.lebentechtorniquetes.modules.camerax

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Size
import android.view.View
import com.google.mlkit.vision.face.Face
import com.lebentech.lebentechtorniquetes.interfaces.FaceRecognitionListener
import com.lebentech.lebentechtorniquetes.models.FaceSize
import com.lebentech.lebentechtorniquetes.utils.LogUtils

class GraphicOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultAttributeSet: Int = 0
): View(context, attrs, defaultAttributeSet) {

    private lateinit var listener: FaceRecognitionListener

    private var startDetection: Boolean = false
    private var maxFaceSize: FaceSize = FaceSize(0.0f,0.0f,0.0f,0.0f)
    private var minFaceSize: FaceSize = FaceSize(0.0f,0.0f,0.0f,0.0f)
    private var previewWidth: Int = 0
    private var scaleFactorWidth: Float = 1.0f
    private var previewHeight: Int = 0
    private var scaleFactorHeight: Float = 1.0f

    private var facesDetected = emptyArray<Face>()

    /**
     * Paint object to draw a box that indicate the face detected
     */
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.YELLOW
        this.style = Paint.Style.STROKE
        this.strokeWidth = 5.0f
    }

    private val paintMax = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.GREEN
        this.style = Paint.Style.STROKE
        this.strokeWidth = 5.0f
    }

    private val paintMin = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.GREEN
        this.style = Paint.Style.STROKE
        this.strokeWidth = 5.0f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if ( startDetection ) {
            drawOverlay(canvas)
        }
    }

    /**
     * Set the preview width and height with the screen size
     */
    fun setPreviewSize(size: Size) {
        previewWidth = size.height
        previewHeight = size.width
    }

    /**
     * Fill the faces array with the faces detected on the analyzer object
     */
    fun setFaces(faces: List<Face>) {
        this.facesDetected = faces.toTypedArray()
        postInvalidate()
    }

    /**
     * After the post invalidate method is called, it will draw each of the face detected with a
     * border box
     */
    private fun drawOverlay(canvas: Canvas) {
        scaleFactorWidth = width.toFloat() / previewWidth
        scaleFactorHeight = height.toFloat() / previewHeight

        for (face in facesDetected) {
            // Position for the head
            val y = face.headEulerAngleY
            val x = face.headEulerAngleX

            if ( y < 20 && y > -20
                && x < 20 && x > -20 ) {
                // drawFaceBorder(face, canvas)
                verifyFaceSize(face)
            } else {
                listener.noFaceDetection()
            }
            break
        }
        if (this.facesDetected.isEmpty()) {
            listener.noFaceDetection()

        }
        // canvas.drawRect( minFaceSize.left, minFaceSize.top, minFaceSize.right, minFaceSize.bottom, this.paintMin )
        // canvas.drawRect( maxFaceSize.left, maxFaceSize.top, maxFaceSize.right, maxFaceSize.bottom, this.paintMax )
    }

    /**
     * Draw a border box with the size of the detected face
     */
    private fun drawFaceBorder(face: Face, canvas: Canvas) {
        val bounds = face.boundingBox
        LogUtils.printLog(bounds.left.toString())
        val left = translateX(bounds.left.toFloat())
        val top = translateY(bounds.top.toFloat())
        val right = translateX(bounds.right.toFloat())
        val bottom = translateY(bounds.bottom.toFloat())

        canvas.drawRect(left, top, right, bottom, paint)
    }

    private fun translateX(x: Float): Float = x * scaleFactorWidth

    private fun translateY(y: Float): Float = y * scaleFactorHeight

    fun setFaceSizes(max: FaceSize, min: FaceSize) {
        this.maxFaceSize = max
        this.minFaceSize = min

        startDetection = true
    }

    fun setFaceRecognitionListener(listener: FaceRecognitionListener) {
        this.listener = listener
    }

    private fun verifyFaceSize(face: Face) {
        val bounds = face.boundingBox
        LogUtils.printLog(bounds.left.toString())
        val left = translateX(bounds.left.toFloat())
        val top = translateY(bounds.top.toFloat())
        val right = translateX(bounds.right.toFloat())
        val bottom = translateY(bounds.bottom.toFloat())

        val faceWidth = right - left
        val faceHeight = bottom - top

        val faceMinWidth = minFaceSize.right - minFaceSize.left
        val faceMinHeight = minFaceSize.bottom - minFaceSize.top

        if (top > maxFaceSize.top && bottom < maxFaceSize.bottom
            && left > maxFaceSize.left && right < maxFaceSize.right) {
            // Face inside the box

            if (faceWidth < faceMinWidth && faceHeight < faceMinHeight) {
                // Face height and width is not the recommended size for lebentech
                listener.faceTooFar()
            } else {
                // Optimal face size and it is inside the box
                listener.faceDetected()
            }
        } else {
            // Face is in the box
            listener.faceTooClose()
        }
    }
}
