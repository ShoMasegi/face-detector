package sho.masegi.facedetector.ui.main

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import sho.masegi.facedetector.R
import sho.masegi.facedetector.ui.imageprocessor.FaceDetectionResult
import sho.masegi.facedetector.ui.imageprocessor.FaceDetectorProcessor
import sho.masegi.facedetector.ui.result.FaceDetectResultBottomSheetContent
import sho.masegi.facedetector.ui.theme.FaceDetectorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen() {
  val bottomSheetState = rememberModalBottomSheetState()
  val snackbarHostState = remember { SnackbarHostState() }
  var showBottomSheet by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current
  val faceDetector = remember { FaceDetectorProcessor(context) }
  var faceDetectionResults: List<FaceDetectionResult>? by remember { mutableStateOf(null) }
  var isLoading by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(text = stringResource(id = R.string.app_name))
        },
      )
    },
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    modifier = Modifier.fillMaxSize(),
  ) { contentPadding ->
    Box(modifier = Modifier.padding(contentPadding)) {
      MainContent(
        isLoading = isLoading,
        onFaceDetectionClick = { imageUri ->
          coroutineScope.launch {
            faceDetectionResults = null
            showBottomSheet = false

            if (imageUri != Uri.EMPTY) {
              try {
                isLoading = true
                faceDetectionResults = faceDetector.detect(imageUri)
                showBottomSheet = true
                isLoading = false
              } catch (e: Exception) {
                isLoading = false
                snackbarHostState.showSnackbar("${context.getString(R.string.error_face_detection)} ${e.localizedMessage}")
              }
            } else {
              snackbarHostState.showSnackbar(context.getString(R.string.error_image_not_set))
            }
          }
        }
      )

      if (showBottomSheet && faceDetectionResults != null) {
        ModalBottomSheet(
          onDismissRequest = { showBottomSheet = false },
          sheetState = bottomSheetState,
        ) {
          FaceDetectResultBottomSheetContent(
            faceDetectionResults = faceDetectionResults!!,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainContent(
  isLoading: Boolean,
  onFaceDetectionClick: (Uri) -> Unit,
  modifier: Modifier = Modifier,
) {
  val selectableLogics = FaceDetectLogic.entries
  var selectedLogic by remember { mutableStateOf(FaceDetectLogic.FIREBASE_ML) }

  var pickedImageUri by remember { mutableStateOf(Uri.EMPTY) }
  val pickVisualMediaLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) pickedImageUri = uri
  }

  BoxWithConstraints(
    contentAlignment = Alignment.Center,
    modifier = modifier.fillMaxSize(),
  ) {
    val imageSize = maxWidth * 2 / 3
    Column(
      verticalArrangement = Arrangement.spacedBy(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      AsyncImage(
        model = pickedImageUri,
        contentDescription = "選択中の画像",
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .clip(shape = RoundedCornerShape(32.dp))
          .clickable(enabled = !isLoading) {
            pickVisualMediaLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          }
          .background(
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
          )
          .size(imageSize)
      )

      SingleChoiceSegmentedButtonRow {
        selectableLogics.forEachIndexed { index, logic ->
          SegmentedButton(
            selected = selectedLogic == logic,
            onClick = { if (!isLoading) selectedLogic = logic },
            shape = SegmentedButtonDefaults.itemShape(
              index = index,
              count = selectableLogics.size,
            ),
            enabled = false,
          ) {
            Text(text = logic.title)
          }
        }
      }

      Button(
        onClick = { onFaceDetectionClick(pickedImageUri) },
        modifier = Modifier.width(156.dp),
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(20.dp),
          )
        } else {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
          ) {
            Icon(
              painter = painterResource(id = R.drawable.ic_familiar_face_and_zone),
              contentDescription = "顔検出",
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(20.dp)
            )

            Text(
              text = "顔検出",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onPrimary,
            )
          }
        }
      }
    }
  }
}

enum class FaceDetectLogic(val title: String) {
  FACE_DETECT("FaceDetect"),
  FIREBASE_ML("Firebase ML");
}

@Preview(
  device = Devices.PIXEL_7,
  uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
  device = Devices.PIXEL_7,
  uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun MainScreen_Preview() {
  FaceDetectorTheme {
    MainScreen()
  }
}