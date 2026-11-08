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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.ConsentSheet
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.components.hasPermission
import com.glowup.app.ui.data.Consent
import com.glowup.app.ui.data.ConsentStore
import com.glowup.app.ui.data.PhotoVault
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.screens.fitness.ChoicePill
import com.glowup.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class Pose(val tag: String, val label: String, val tip: String) {
    FRONT("front", "Face", "Face the camera, neutral expression, same light each time."),
    SIDE("side", "Side", "Turn side-on, relaxed shoulders — great for posture and jawline."),
    BODY("body", "Body", "Full body or torso, same spot and distance each week."),
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
        CaptureView(ghost = photos.lastOrNull(), pose = pose, onDone = { capturing = false; version++ })
        return
    }

    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader("Progress photos") { navController.goBack() }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(span = { GridItemSpan(3) }) {
                    Column {
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(GreenTint).padding(14.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = Green, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Stored inside the app only — never in your gallery, never backed up or uploaded.", color = Color(0xFF1E7A4F), fontSize = 12.sp, lineHeight = 16.sp)
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Pose.entries.forEach { p -> ChoicePill(p.label, pose == p, Modifier.weight(1f)) { pose = p } }
                        }
                        Spacer(Modifier.height(14.dp))
                        if (photos.size >= 2) {
                            Text("First vs latest", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Compare(photos.first(), Modifier.weight(1f))
                                Compare(photos.last(), Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                        Text(if (photos.isEmpty()) "No ${pose.label.lowercase()} photos yet" else "All ${pose.label.lowercase()} photos", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(pose.tip, color = TextSecondary, fontSize = 12.5.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 2.dp, bottom = 4.dp))
                    }
                }
                items(photos.reversed(), key = { it.name }) { f ->
                    Thumb(f, Modifier.aspectRatio(0.75f).clip(RoundedCornerShape(14.dp)).clickable { viewing = f })
                }
                item(span = { GridItemSpan(3) }) {
                    if (!reminded) {
                        Text(
                            "Remind me weekly", color = Purple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp).clickable { RoutineStore.add("Looks", "Progress photo", "WEEKLY") },
                        )
                    }
                }
            }
            Box(Modifier.padding(24.dp, 8.dp, 24.dp, 24.dp)) {
                Row(
                    Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(999.dp)).background(Ink).clickable {
                        capturing = true
                    },
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Take a ${pose.label.lowercase()} photo", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
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
            Row(Modifier.align(Alignment.TopCenter).fillMaxWidth().systemBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)).clickable { viewing = null }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
                Text(
                    PhotoVault.dateOf(f)?.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())) ?: "",
                    color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
                )
                Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)).clickable { confirm = true }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete photo", tint = Color.White)
                }
            }
        }
        if (confirm) {
            GlowUpDialog(
                onDismissRequest = { confirm = false }, title = "Delete this photo?", message = "It's removed from this phone for good.",
                primaryText = "Delete", primaryDestructive = true,
                onPrimary = { PhotoVault.delete(f); viewing = null; version++ },
            )
        }
    }
}

@Composable
private fun Compare(file: File, modifier: Modifier) {
    Column(modifier) {
        Thumb(file, Modifier.fillMaxWidth().aspectRatio(0.75f).clip(RoundedCornerShape(16.dp)))
        Text(PhotoVault.dateOf(file)?.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())) ?: "", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
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
        bitmap?.let { Image(it.asImageBitmap(), contentDescription = null, contentScale = if (full) ContentScale.Fit else ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
    }
}

@Composable
private fun CaptureView(ghost: File?, pose: Pose, onDone: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(hasPermission(context, Manifest.permission.CAMERA)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> granted = ok; if (!ok) onDone() }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.CAMERA) }
    var front by remember { mutableStateOf(pose != Pose.BODY) }
    var countdown by remember { mutableStateOf<Int?>(null) }
    var saving by remember { mutableStateOf(false) }
    val capture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    val ghostBitmap by produceState<Bitmap?>(null, ghost) {
        value = ghost?.let { withContext(Dispatchers.IO) { runCatching { BitmapFactory.decodeFile(it.absolutePath, BitmapFactory.Options().apply { inSampleSize = 2 }) }.getOrNull() } }
    }

    fun shoot() {
        saving = true
        capture.takePicture(ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val rotation = image.imageInfo.rotationDegrees
                val raw = image.toBitmap()
                image.close()
                Thread {
                    // Upright, and mirrored for the front camera so it matches what you saw.
                    val m = Matrix().apply { postRotate(rotation.toFloat()); if (front) postScale(-1f, 1f) }
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
        })
    }

    LaunchedEffect(countdown) {
        val c = countdown ?: return@LaunchedEffect
        if (c == 0) { countdown = null; shoot() } else { delay(1000); countdown = c - 1 }
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
                    val selector = if (front && p.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                    runCatching { p.unbindAll(); p.bindToLifecycle(lifecycleOwner, selector, preview, capture) }
                }, ContextCompat.getMainExecutor(context))
                onDispose { provider?.unbindAll() }
            }
            ghostBitmap?.let {
                // Last photo as a faint guide, so the angle and distance match.
                Image(
                    it.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(0.32f),
                )
            }
        }
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)).clickable(onClick = onDone), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = Color.White)
                }
                Text(
                    if (ghost != null) "Line up with your last photo" else pose.tip,
                    color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(horizontal = 10.dp).clip(RoundedCornerShape(999.dp)).background(Color.Black.copy(alpha = 0.45f)).padding(12.dp, 8.dp),
                )
                Box(Modifier.size(44.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)).clickable { front = !front }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Cameraswitch, contentDescription = "Switch camera", tint = Color.White)
                }
            }
            Spacer(Modifier.weight(1f))
            countdown?.let { Text("$it", color = Color.White, fontSize = 96.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally)) }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth().padding(32.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(52.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)).clickable(enabled = !saving) { countdown = 5 }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Timer, contentDescription = "5 second timer", tint = Color.White)
                }
                Box(
                    Modifier.size(78.dp).clip(CircleShape).border(4.dp, Color.White, CircleShape).padding(6.dp).clip(CircleShape)
                        .background(if (saving) Color.White.copy(alpha = 0.4f) else Color.White).clickable(enabled = !saving) { shoot() },
                )
                Spacer(Modifier.size(52.dp))
            }
        }
    }
}
