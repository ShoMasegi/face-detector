package sho.masegi.facedetector.ui.result

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sho.masegi.facedetector.ui.theme.FaceDetectorTheme

@Composable
internal fun FaceDetectResultBottomSheetContent() {
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .height(120.dp)
      .fillMaxWidth()
  ) {
    Text(text = "仮")
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(device = Devices.PIXEL_7)
@Composable
private fun FaceDetectResultBottomSheetContent_Preview() {
  FaceDetectorTheme {
    BottomSheetScaffold(
      sheetContent = {
        FaceDetectResultBottomSheetContent()
      }
    ) {
      Box {
        Text(text = "Content")
      }
    }
  }
}