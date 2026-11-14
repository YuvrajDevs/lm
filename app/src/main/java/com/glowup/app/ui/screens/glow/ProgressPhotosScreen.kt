package com.glowup.app.ui.screens.glow

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.ConsentSheet
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.components.hasPermission
import com.glowup.app.ui.data.Consent
import com.glowup.app.ui.data.ConsentStore
import com.glowup.app.ui.data.PhotoVault
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.screens.fitness.ChoicePill
import com.glowup.app.ui.theme.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private enum class Pose(val tag: String, val label: String, val tip: String) {
    FRONT("front", "Face", "Face the camera, neutral expression, same light each time."),
    SIDE("side", "Side", "Turn side-on, relaxed shoulders — great for posture and jawline."),
    BODY("body", "Body", "Full body or torso, same spot and distance each week.")
}

@Composable
fun ProgressPhotosScreen(navController: NavHostController) {
    var consented by remember { mutableStateOf(ConsentStore.has(Consent.PROGRESS_PHOTOS)) }
    var pose by remember { mutableStateOf(Pose.FRONT) }
    var capturing by remember { mutableStateOf(false) }
    var version by remember { mutableIntStateOf(0) }
    var viewing by remember { mutableStateOf<File?>(null) }
    val photos = remember(pose, version) { PhotoVault.list(PhotoVault.Album.PROGRESS, pose.tag) }
    val reminded = RoutineStore.items.any { it.title.equals("Progress photo", ignoreCase = true) }

    if (capturing) {
        CaptureView(ghost = photos.lastOrNull(), pose = pose, onDone = {
            capturing = false
            version++
        })
        return
    }

    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader(stringResource(R.string.progress_photos_progress_photos)) { navController.goBack() }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(Space.Gutter, Space.L, Space.Gutter, Space.Gutter),
                horizontalArrangement = Arrangement.spacedBy(Space.S),
                verticalArrangement = Arrangement.spacedBy(Space.S)
            ) {
                item(span = { GridItemSpan(3) }) {
                    Column {
                        Row(
                            Modifier.fillMaxWidth().clip(Shapes.Medium).background(GreenTint).padding(Space.M, Space.S),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = Green, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(Space.S))
                            Text(
                                stringResource(R.string.progress_photos_stored_inside_the_app_only),
                                color = GreenDeep,
                                fontSize = TextSize.Caption,
                                lineHeight = LineHeight.Tight
                            )
                        }
                        Spacer(Modifier.height(Space.M))
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                            Pose.entries.forEach { p -> ChoicePill(p.label, pose == p, Modifier.weight(1f)) { pose = p } }
                        }
                        Spacer(Modifier.height(Space.M))
                        if (photos.size >= 2) {
                            Text(stringResource(R.string.progress_photos_first_vs_latest), fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(Space.S))
                            Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                                Compare(photos.first(), Modifier.weight(1f))
                                Compare(photos.last(), Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(Space.L))
                        }
                        Text(
                            if (photos.isEmpty()) {
                                stringResource(
                                    R.string.progress_photos_no_1_s_photos_yet,
                                    pose.label.lowercase()
                                )
                            } else {
                                stringResource(R.string.progress_photos_all_1_s_photos, pose.label.lowercase())
                            },
                            fontSize = TextSize.Title,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            pose.tip,
                            color = TextSecondary,
                            fontSize = TextSize.BodySmall,
                            lineHeight = LineHeight.Compact,
                            modifier = Modifier.padding(top = 2.dp, bottom = Space.Xs)
                        )
                    }
                }
                items(photos.reversed(), key = { it.name }) { f ->
                    Thumb(f, Modifier.aspectRatio(0.75f).clip(Shapes.Small).clickable { viewing = f })
                }
                item(span = { GridItemSpan(3) }) {
                    if (!reminded) {
                        Text(
                            stringResource(R.string.progress_photos_remind_me_weekly),
                            color = Purple,
                            fontSize = TextSize.BodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = Space.S).clickable { RoutineStore.add("Looks", "Progress photo", "WEEKLY") }
                        )
                    }
                }
            }
            Box(Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.Gutter)) {
                AppButton(
                    stringResource(R.string.progress_photos_take_a_1_s_photo, pose.label.lowercase()),
                    variant = ButtonVariant.Primary,
                    icon = Icons.Filled.PhotoCamera
                ) {
                    capturing = true
                }
            }
        }
    }

    if (!consented) {
        ConsentSheet(Consent.PROGRESS_PHOTOS, Icons.Filled.PhotoCamera, onAllow = { consented = true }, onDecline = { navController.goBack() })
    }
    viewing?.let { f ->
        var confirm by remember(f) { mutableStateOf(false) }
        Box(Modifier.fillMaxSize().background(Color.Black).clickable { viewing = null }, contentAlignment = Alignment.Center) {
            Thumb(f, Modifier.fillMaxWidth().aspectRatio(0.75f), full = true)
            Row(Modifier.align(Alignment.TopCenter).fillMaxWidth().systemBarsPadding().padding(Space.L), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)).clickable {
                        viewing = null
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.common_close), tint = Color.White)
                }
                Text(
                    PhotoVault.dateOf(f)?.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())) ?: "",
                    color = Color.White,
                    fontSize = TextSize.BodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)).clickable {
                        confirm = true
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.progress_photos_delete_photo), tint = Color.White)
                }
            }
        }
        if (confirm) {
            ReMakeUDialog(
                onDismissRequest = { confirm = false },
                title = "Delete this photo?",
                message = "It's removed from this phone for good.",
                primaryText = "Delete",
                primaryDestructive = true,
                onPrimary = {
                    PhotoVault.delete(f)
                    viewing = null
                    version++
                }
            )
        }
    }
}

@Composable
private fun Compare(file: File, modifier: Modifier) {
    Column(modifier) {
        Thumb(file, Modifier.fillMaxWidth().aspectRatio(0.75f).clip(Shapes.Medium))
        Text(
            PhotoVault.dateOf(file)?.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())) ?: "",
            color = TextSecondary,
            fontSize = TextSize.Caption,
            modifier = Modifier.padding(top = Space.Xs)
        )
    }
}

@Composable
private fun Thumb(file: File, modifier: Modifier, full: Boolean = false) {
    val bitmap by produceState<Bitmap?>(null, file) {
        value = withContext(Dispatchers.IO) {
            val opts = BitmapFactory.Options().apply { inSampleSize = if (full) 1 else 4 }
            runCatching { BitmapFactory.decodeFile(file.absolutePath, opts) }.getOrNull()
        }
    }
    Box(modifier.background(CardNeutral)) {
        bitmap?.let {
            Image(
                it.asImageBitmap(),
                contentDescription = null,
                contentScale = if (full) ContentScale.Fit else ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun CaptureView(ghost: File?, pose: Pose, onDone: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(hasPermission(context, Manifest.permission.CAMERA)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        granted = ok
        if (!ok) onDone()
    }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.CAMERA) }
    var front by remember { mutableStateOf(pose != Pose.BODY) }
    var countdown by remember { mutableStateOf<Int?>(null) }
    var saving by remember { mutableStateOf(false) }
    val capture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    val ghostBitmap by produceState<Bitmap?>(null, ghost) {
        value =
            ghost?.let {
                withContext(Dispatchers.IO) {
                    runCatching { BitmapFactory.decodeFile(it.absolutePath, BitmapFactory.Options().apply { inSampleSize = 2 }) }.getOrNull()
                }
            }
    }

    fun shoot() {
        saving = true
        capture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val rotation = image.imageInfo.rotationDegrees
                    val raw = image.toBitmap()
                    image.close()
                    Thread {
                        // Upright, and mirrored for the front camera so it matches what you saw.
                        val m = Matrix().apply {
                            postRotate(rotation.toFloat())
                            if (front) postScale(-1f, 1f)
                        }
                        val upright = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, m, true)
                        val out = ByteArrayOutputStream()
                        upright.compress(Bitmap.CompressFormat.JPEG, 88, out)
                        PhotoVault.save(PhotoVault.Album.PROGRESS, out.toByteArray(), tag = pose.tag)
                        ContextCompat.getMainExecutor(context).execute { onDone() }
                    }.start()
                }

                override fun onError(exception: ImageCaptureException) {
                    saving = false
                }
            }
        )
    }

    LaunchedEffect(countdown) {
        val c = countdown ?: return@LaunchedEffect
        if (c == 0) {
            countdown = null
            shoot()
        } else {
            delay(1000)
            countdown = c - 1
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (granted) {
            AndroidView({ previewView }, Modifier.fillMaxSize())
            DisposableEffect(front) {
                val future = ProcessCameraProvider.getInstance(context)
                var provider: ProcessCameraProvider? = null
                future.addListener({
                    val p = future.get()
                    provider = p
                    val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                    val selector = if (front &&
                        p.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                    ) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
                    runCatching {
                        p.unbindAll()
                        p.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                    }
                }, ContextCompat.getMainExecutor(context))
                onDispose { provider?.unbindAll() }
            }
            ghostBitmap?.let {
                // Last photo as a faint guide, so the angle and distance match.
                Image(
                    it.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(0.32f)
                )
            }
        }
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(Space.L), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)).clickable(onClick = onDone),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.common_cancel), tint = Color.White)
                }
                Text(
                    if (ghost != null) stringResource(R.string.progress_photos_line_up_with_your_last) else pose.tip,
                    color = Color.White,
                    fontSize = TextSize.BodySmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(
                        1f
                    ).padding(horizontal = Space.S).clip(Shapes.Pill).background(Color.Black.copy(alpha = 0.45f)).padding(Space.M, Space.S)
                )
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)).clickable {
                        front = !front
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Cameraswitch, contentDescription = stringResource(R.string.progress_photos_switch_camera), tint = Color.White)
                }
            }
            Spacer(Modifier.weight(1f))
            countdown?.let {
                Text(
                    "$it",
                    color = Color.White,
                    fontSize = TextSize.Countdown,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth().padding(Space.Xxl), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(52.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)).clickable(enabled = !saving) {
                        countdown = 5
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Timer, contentDescription = stringResource(R.string.progress_photos_5_second_timer), tint = Color.White)
                }
                Box(
                    Modifier.size(78.dp).clip(CircleShape).border(4.dp, Color.White, CircleShape).padding(Space.S).clip(CircleShape)
                        .background(if (saving) Color.White.copy(alpha = 0.4f) else Color.White).clickable(enabled = !saving) { shoot() }
                )
                Spacer(Modifier.size(52.dp))
            }
        }
    }
}
