package com.glowup.app.ui.screens.glow

import android.Manifest
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.glowup.app.ui.components.hasPermission
import com.glowup.app.ui.data.glow.FaceAnalysis
import com.glowup.app.ui.data.glow.FaceGeometry
import com.glowup.app.ui.data.glow.FaceMetrics
import com.glowup.app.ui.data.glow.PostureAnalysis
import com.glowup.app.ui.data.glow.Pt
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.Executors
import kotlin.math.abs

enum class ScanMode { FACE, POSTURE }

private const val FACE_FRAMES = 8
private const val POSTURE_FRAMES = 12

/**
 * Live camera that measures and never stores. Frames are analysed in memory on the phone and
 * dropped immediately; only the final numbers leave this composable via [onFace]/[onPosture].
 */
@Composable
fun CameraScanner(
    mode: ScanMode,
    onFace: (FaceMetrics) -> Unit = {},
    onPosture: (Int) -> Unit = {},
    onPermissionDenied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(hasPermission(context, Manifest.permission.CAMERA)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        granted = ok
        if (!ok) onPermissionDenied()
    }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.CAMERA) }

    var hint by remember { mutableStateOf("Starting camera…") }
    var progress by remember { mutableStateOf(0f) }
    var done by remember { mutableStateOf(false) }
    val faceCallback by rememberUpdatedState(onFace)
    val postureCallback by rememberUpdatedState(onPosture)

    Box(modifier.clip(RoundedCornerShape(28.dp)).background(Color.Black)) {
        if (granted) {
            val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
            AndroidView({ previewView }, Modifier.fillMaxSize())
            DisposableEffect(mode) {
                val executor = Executors.newSingleThreadExecutor()
                val faceDetector = FaceDetection.getClient(
                    FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .build(),
                )
                val poseDetector = PoseDetection.getClient(PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.STREAM_MODE).build())
                val faceFrames = mutableListOf<FaceMetrics>()
                val angles = mutableListOf<Int>()
                val beep = runCatching { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70) }.getOrNull()
                val providerFuture = ProcessCameraProvider.getInstance(context)
                var provider: ProcessCameraProvider? = null

                @OptIn(ExperimentalGetImage::class)
                fun analyze(proxy: androidx.camera.core.ImageProxy) {
                    val media = proxy.image
                    if (media == null || done) { proxy.close(); return }
                    val rotation = proxy.imageInfo.rotationDegrees
                    val image = InputImage.fromMediaImage(media, rotation)
                    val width = if (rotation % 180 == 0) proxy.width else proxy.height
                    if (mode == ScanMode.FACE) {
                        faceDetector.process(image).addOnSuccessListener { faces ->
                            val face = faces.maxByOrNull { it.boundingBox.width() }
                            when {
                                face == null -> { hint = "Look straight at the camera"; faceFrames.clear() }
                                face.boundingBox.width() < width * 0.32 -> hint = "Move a little closer"
                                face.boundingBox.width() > width * 0.85 -> hint = "Move back a little"
                                abs(face.headEulerAngleY) > 7 -> { hint = "Turn to face the camera straight on"; faceFrames.clear() }
                                abs(face.headEulerAngleX) > 10 -> { hint = "Keep your chin level"; faceFrames.clear() }
                                abs(face.headEulerAngleZ) > 8 -> { hint = "Straighten your head"; faceFrames.clear() }
                                else -> {
                                    fun c(type: Int) = face.getContour(type)?.points.orEmpty().map { Pt(it.x, it.y) }
                                    FaceAnalysis.measure(
                                        FaceGeometry(
                                            c(FaceContour.FACE), c(FaceContour.LEFT_EYE), c(FaceContour.RIGHT_EYE),
                                            c(FaceContour.LEFT_EYEBROW_TOP), c(FaceContour.RIGHT_EYEBROW_TOP),
                                            c(FaceContour.NOSE_BRIDGE), c(FaceContour.NOSE_BOTTOM), c(FaceContour.UPPER_LIP_TOP),
                                        ),
                                    )?.let { faceFrames += it }
                                    hint = "Hold still…"
                                    progress = faceFrames.size / FACE_FRAMES.toFloat()
                                    if (faceFrames.size >= FACE_FRAMES && !done) {
                                        done = true
                                        beep?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
                                        FaceAnalysis.average(faceFrames)?.let { m -> ContextCompat.getMainExecutor(context).execute { faceCallback(m) } }
                                    }
                                }
                            }
                        }.addOnCompleteListener { proxy.close() }
                    } else {
                        poseDetector.process(image).addOnSuccessListener { pose ->
                            fun lm(type: Int) = pose.getPoseLandmark(type)?.takeIf { it.inFrameLikelihood > 0.7f }
                            val leftPair = lm(PoseLandmark.LEFT_EAR) to lm(PoseLandmark.LEFT_SHOULDER)
                            val rightPair = lm(PoseLandmark.RIGHT_EAR) to lm(PoseLandmark.RIGHT_SHOULDER)
                            val pair = listOf(leftPair, rightPair).filter { it.first != null && it.second != null }
                                .maxByOrNull { it.first!!.inFrameLikelihood + it.second!!.inFrameLikelihood }
                            val ls = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                            val rs = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
                            val shoulderSpread = if (ls != null && rs != null) abs(ls.position.x - rs.position.x) else 0f
                            when {
                                pair == null -> { hint = "Step back until your head and shoulders are in view"; angles.clear() }
                                shoulderSpread > width * 0.12f -> { hint = "Turn side-on to the camera"; angles.clear() }
                                else -> {
                                    val ear = pair.first!!.position
                                    val shoulder = pair.second!!.position
                                    PostureAnalysis.neckAngle(Pt(ear.x, ear.y), Pt(shoulder.x, shoulder.y))?.let { angles += it }
                                    hint = "Stand naturally and hold still…"
                                    progress = angles.size / POSTURE_FRAMES.toFloat()
                                    if (angles.size >= POSTURE_FRAMES && !done) {
                                        done = true
                                        beep?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
                                        val median = angles.sorted()[angles.size / 2]
                                        ContextCompat.getMainExecutor(context).execute { postureCallback(median) }
                                    }
                                }
                            }
                        }.addOnCompleteListener { proxy.close() }
                    }
                }

                providerFuture.addListener({
                    val p = providerFuture.get()
                    provider = p
                    val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { it.setAnalyzer(executor, ::analyze) }
                    val selector = if (p.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                    runCatching {
                        p.unbindAll()
                        p.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
                        hint = if (mode == ScanMode.FACE) "Fit your face in the oval" else "Prop your phone up and stand side-on, 2 m away"
                    }.onFailure { hint = "Couldn't open the camera" }
                }, ContextCompat.getMainExecutor(context))

                onDispose {
                    provider?.unbindAll()
                    executor.shutdown()
                    faceDetector.close()
                    poseDetector.close()
                    beep?.release()
                }
            }

            // Guides
            Canvas(Modifier.fillMaxSize()) {
                val stroke = Stroke(3.dp.toPx(), pathEffect = if (progress == 0f) PathEffect.dashPathEffect(floatArrayOf(18f, 14f)) else null)
                val color = if (progress > 0f) Color(0xFF7FD9A8) else Color.White.copy(alpha = 0.85f)
                if (mode == ScanMode.FACE) {
                    val w = size.width * 0.62f
                    val h = w * 1.32f
                    val topLeft = Offset((size.width - w) / 2, (size.height - h) / 2 - size.height * 0.04f)
                    drawOval(color, topLeft, Size(w, h), style = stroke)
                    if (progress > 0f) drawArc(Color(0xFF7FD9A8), -90f, 360f * progress.coerceAtMost(1f), false, topLeft, Size(w, h), style = Stroke(7.dp.toPx()))
                } else {
                    val x = size.width / 2
                    drawLine(color, Offset(x, size.height * 0.12f), Offset(x, size.height * 0.88f), 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f)))
                    if (progress > 0f) drawArc(Color(0xFF7FD9A8), -90f, 360f * progress.coerceAtMost(1f), false, Offset(x - 40.dp.toPx(), 24.dp.toPx()), Size(80.dp.toPx(), 80.dp.toPx()), style = Stroke(6.dp.toPx()))
                }
            }
        }
        Text(
            if (!granted) "Camera permission is needed for the scan" else if (done) "Got it" else hint,
            color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).clip(RoundedCornerShape(999.dp)).background(Color.Black.copy(alpha = 0.55f)).padding(16.dp, 10.dp),
        )
    }
}
