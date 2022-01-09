package com.androchef.cameraxfacedetection.camerax

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import android.graphics.BitmapFactory
import java.nio.ByteBuffer


abstract class BaseImageAnalyzer<T> : ImageAnalysis.Analyzer {

    abstract val graphicOverlay: GraphicOverlay

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        mediaImage?.let {
            detectInImage(InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees))
                .addOnSuccessListener { results ->
                    val buffer: ByteBuffer = imageProxy.image!!.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)

                    val faceCrop = Bitmap.createBitmap(
                        original,
                        it.cropRect.left,
                        it.cropRect.top,
                        it.cropRect.width(),
                        it.cropRect.height()
                    )
                    //face cropped using rect values
                    onSuccess(
                        results,
                        graphicOverlay,
                        it.cropRect,
                        original
                    )
                    imageProxy.close()
                }
                .addOnFailureListener {
                    onFailure(it)
                    imageProxy.close()
                }
        }
    }

    protected abstract fun detectInImage(image: InputImage): Task<T>

    abstract fun stop()

    protected abstract fun onSuccess(
        results: T,
        graphicOverlay: GraphicOverlay,
        rect: Rect,
        original: Bitmap
    )

    protected abstract fun onFailure(e: Exception)

}