package auth.vault.ui.screens.qscanner

import android.Manifest
import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "QrScannerScreen"

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, ExperimentalGetImage::class)
@Composable
fun QrScannerScreen(
    onNavigateBack: () -> Unit,
    onQrScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var scannedSecret by remember { mutableStateOf<String?>(null) }
    var processingComplete by remember { mutableStateOf(false) }

    LaunchedEffect(cameraPermissionState.status) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(scannedSecret) {
        scannedSecret?.let { secret ->
            if (!processingComplete) {
                processingComplete = true
                onQrScanned(secret)
            }
        }
    }

    when {
        cameraPermissionState.status.isGranted -> {
            QrScannerContent(
                modifier = modifier,
                onNavigateBack = onNavigateBack,
                onSecretDetected = { secret ->
                    scannedSecret = secret
                }
            )
        }
        cameraPermissionState.status is PermissionStatus.Denied -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                PermissionDeniedContent(
                    onNavigateBack = onNavigateBack,
                onRetryPermission = {
                    cameraPermissionState.launchPermissionRequest()
                },
                onOpenAppSettings = {
                    val intent = android.content.Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                }
                )
            }
            }
        else -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showPermissionDeniedDialog || cameraPermissionState.status is PermissionStatus.Denied) {
        showPermissionDeniedDialog = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QrScannerContent(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onSecretDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var detectedSecret by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraExecutor.shutdown()
            } catch (e: Exception) {}
        }
    }

    LaunchedEffect(detectedSecret) {
        detectedSecret?.let { secret ->
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        ScannedSecretDialog(
            secret = detectedSecret ?: "",
            onDismiss = {
                showSuccessDialog = false
                detectedSecret = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scan QR Code",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                context = context,
                lifecycleOwner = lifecycleOwner,
                cameraExecutor = cameraExecutor,
                onBarcodeDetected = { barcode ->
                    if (detectedSecret == null) {
                        detectedSecret = barcode
                    }
                }
            )

            ScannerOverlay(
                modifier = Modifier.align(Alignment.Center)
            )

            ScanningHint(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            )
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraExecutor: ExecutorService,
    onBarcodeDetected: (String) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val barcodeScannerOptions = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE
                    )
                    .build()

                val barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        barcodeScanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    barcode.rawValue?.let { value ->
                                        if (value.isNotBlank()) {
                                            onBarcodeDetected(value)
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Barcode scanning failed", e)
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

@Composable
private fun ScannerOverlay(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFF00C853).copy(alpha = 0.8f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.1f))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cornerLength = with(density) { 40.dp.toPx() }
                val strokeWidth = with(density) { 4.dp.toPx() }
                val color = Color(0xFF00C853)

                drawLine(
                    color = color,
                    start = Offset(strokeWidth / 2, cornerLength),
                    end = Offset(strokeWidth / 2, strokeWidth / 2),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = color,
                    start = Offset(strokeWidth / 2, strokeWidth / 2),
                    end = Offset(cornerLength, strokeWidth / 2),
                    strokeWidth = strokeWidth
                )

                drawLine(
                    color = color,
                    start = Offset(size.width - cornerLength, strokeWidth / 2),
                    end = Offset(size.width - strokeWidth / 2, strokeWidth / 2),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = color,
                    start = Offset(size.width - strokeWidth / 2, strokeWidth / 2),
                    end = Offset(size.width - strokeWidth / 2, cornerLength),
                    strokeWidth = strokeWidth
                )

                drawLine(
                    color = color,
                    start = Offset(size.width - strokeWidth / 2, size.height - cornerLength),
                    end = Offset(size.width - strokeWidth / 2, size.height - strokeWidth / 2),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = color,
                    start = Offset(size.width - strokeWidth / 2, size.height - strokeWidth / 2),
                    end = Offset(size.width - cornerLength, size.height - strokeWidth / 2),
                    strokeWidth = strokeWidth
                )

                drawLine(
                    color = color,
                    start = Offset(cornerLength, size.height - strokeWidth / 2),
                    end = Offset(strokeWidth / 2, size.height - strokeWidth / 2),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = color,
                    start = Offset(strokeWidth / 2, size.height - strokeWidth / 2),
                    end = Offset(strokeWidth / 2, size.height - cornerLength),
                    strokeWidth = strokeWidth
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .offset(y = with(density) { (animatedOffset * (250.dp.toPx() - 4.dp.toPx())).toDp() })
                    .height(2.dp)
                    .background(
                        Color(0xFF00C853).copy(alpha = 0.9f)
                    )
            )
        }
    }
}

@Composable
private fun ScanningHint(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Position the QR code within the frame",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "The code will be scanned automatically",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ScannedSecretDialog(
    secret: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "QR Code Scanned",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Secret Key Detected",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF00C853),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionDeniedContent(
    onNavigateBack: () -> Unit,
    onRetryPermission: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scan QR Code",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Camera Permission Required",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Camera access is required to scan QR codes. Please grant permission in your device settings to continue.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRetryPermission,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(text = "Try Again")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOpenAppSettings,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(text = "Open Settings")
            }
        }
    }
}
