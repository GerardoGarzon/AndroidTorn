/**
 * Created by Gerardo Garzon on 23/12/22.
 */

package com.lebentech.lebentechtorniquetes.modules.cameraqr

import android.content.Context
import android.util.Log
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.lebentech.lebentechtorniquetes.interfaces.CaptureQRListener

class CameraQRManager(val context: Context, private val scannerView: CodeScannerView, private val scannerListener: CaptureQRListener) {
    private lateinit var codeScanner: CodeScanner
    private var isFrontCamera: Boolean = true

    /**
     * Setup the camera qr to be used with the back camera, this method should be called before
     * the start camera
     */
    fun setupCamera() {
        codeScanner = CodeScanner(context, scannerView)

        codeScanner.camera = CodeScanner.CAMERA_FRONT
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.scanMode = ScanMode.SINGLE

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            /* This method comes from the ui thread where is declared the usage of the scanned
             * string and it is passed through the initializer
             */
            scannerListener.capturedQR(it.text)
        }
        codeScanner.errorCallback = ErrorCallback {
            it.message?.let { it1 -> Log.d("Error", it1) }
        }

        // It could be restarted if we click over the scanner view
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    /**
     * After setup the camera it would be able to start the detection of QR codes
     */
    fun startCamera() {
        codeScanner.startPreview()
    }

    /**
     * When the owner activity is stopped or it go to the background, the camera should release
     * resources
     */
    fun stopCamera() {
        codeScanner.releaseResources()
    }

    fun switchCamera() {
        if (isFrontCamera) {
            isFrontCamera = false
            codeScanner.camera = CodeScanner.CAMERA_BACK
        } else {
            isFrontCamera = true
            codeScanner.camera = CodeScanner.CAMERA_FRONT
        }
    }
}