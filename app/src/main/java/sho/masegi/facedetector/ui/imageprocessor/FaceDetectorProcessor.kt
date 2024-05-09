package sho.masegi.facedetector.ui.imageprocessor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlin.math.max
import kotlin.math.min

class FaceDetectorProcessor(private val context: Context) {
  private val faceDetector: FaceDetector

  init {
    val options = FaceDetectorOptions.Builder()
      .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
      .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
      .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
      .build()
    faceDetector = FaceDetection.getClient(options)
  }

  suspend fun detect(uri: Uri): List<FaceDetectionResult> = coroutineScope {
    val stream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(stream)
    val faces = faceDetector.process(InputImage.fromBitmap(bitmap, 0)).await()
    return@coroutineScope faces.map { face ->
      async {
        /**
         * 顔を切り取る際に[Face.getBoundingBox]が`bitmap`外にはみ出ている場合があるので、計算して範囲内に収める。
         */
        val cropRect = Rect(
          max(face.boundingBox.left, 0),
          max(face.boundingBox.top, 0),
          min(face.boundingBox.right, bitmap.width),
          min(face.boundingBox.bottom, bitmap.height)
        )
        val faceBitmap = Bitmap.createBitmap(bitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height())
        FaceDetectionResult(
          face = face,
          bitmap = faceBitmap,
          brightness = measureBrightness(faceBitmap)
        )
      }
    }.awaitAll()
  }

  private suspend fun measureBrightness(bitmap: Bitmap): Double = coroutineScope {
    async {
      var totalBrightness = 0.0
      val pixels = IntArray(bitmap.width * bitmap.height)
      bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
      pixels.forEach { pixel ->
        val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3.0 / 255.0
        totalBrightness += brightness
      }
      Log.i("[FaceDetectorProcessor#measureBrightness]", "brightness: ${totalBrightness}, pixels size: ${pixels.size}")
      return@async totalBrightness / pixels.size.toDouble()
    }.await()
  }
}

data class FaceDetectionResult(
  val face: Face,
  val bitmap: Bitmap,
  val brightness: Double,
)