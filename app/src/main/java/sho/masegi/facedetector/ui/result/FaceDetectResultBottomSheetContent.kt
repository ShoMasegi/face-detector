package sho.masegi.facedetector.ui.result

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.internal.mlkit_vision_face.zzf
import com.google.mlkit.vision.face.Face
import sho.masegi.facedetector.ui.imageprocessor.FaceDetectionResult
import sho.masegi.facedetector.ui.theme.FaceDetectorTheme

@Composable
internal fun FaceDetectResultBottomSheetContent(
  faceDetectionResults: List<FaceDetectionResult>,
  modifier: Modifier = Modifier,
) {
  Surface(modifier = modifier.fillMaxHeight(0.9f)) {
    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = 128.dp),
      contentPadding = PaddingValues(16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      items(faceDetectionResults) { result ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Image(
            bitmap = result.bitmap.asImageBitmap(),
            contentDescription = "顔検出結果",
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(1f)
              .background(color = MaterialTheme.colorScheme.surfaceContainerHighest)
          )
          Text(
            text = result.brightness.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(device = Devices.PIXEL_7)
@Composable
private fun FaceDetectResultBottomSheetContent_Preview() {
  FaceDetectorTheme {
    BottomSheetScaffold(
      sheetContent = {
        FaceDetectResultBottomSheetContent(
          faceDetectionResults = List(10) {
            FaceDetectionResult(
              Face(
                zzf(0, 0, 0f,0f, 0f, 0f, 0f, 0f, 0f, emptyArray(), 0f, 0f, 0f, emptyArray(), 0f),
                Matrix()
              ),
              Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888),
              brightness = 0.0,
            )
          }
        )
      }
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "Content")
      }
    }
  }
}