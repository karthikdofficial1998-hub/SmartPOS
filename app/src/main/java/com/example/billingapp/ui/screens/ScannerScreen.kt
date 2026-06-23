package com.example.billingapp.ui.screens

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.billingapp.ui.viewmodel.BillingViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.AspectRatio
import android.util.Size
import java.util.concurrent.Executors

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size as ComposeSize
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.animation.core.*

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: BillingViewModel,
    onNavigateToCart: () -> Unit,
    onNavigateToDetails: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cartItems by viewModel.cartItems.collectAsState()
    val scannedProduct by viewModel.scannedProduct.collectAsState()
    val lastScannedBarcode by viewModel.lastScannedBarcode.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var showManualInputDialog by remember { mutableStateOf(false) }
    var manualBarcode by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val scannerBarOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scannerBarOffset"
    )

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    LaunchedEffect(Unit) {
        viewModel.scanEvent.collect { event ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasCameraPermission = it }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Scan Barcode", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* Toggle Flash */ }) {
                        Icon(Icons.Default.FlashlightOn, contentDescription = "Flash")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.clearScannedProduct()
                        showManualInputDialog = true 
                    }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Manual")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val resolutionSelector = ResolutionSelector.Builder()
                                .setResolutionStrategy(
                                    ResolutionStrategy(
                                        Size(1280, 720),
                                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                                    )
                                )
                                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                                .build()

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .setResolutionSelector(resolutionSelector)
                                .build()

                            val options = BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                                .build()
                            val scanner = BarcodeScanning.getClient(options)
                            var lastLogTime = 0L
                            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    val currentTime = System.currentTimeMillis()
                                    if (currentTime - lastLogTime > 2000) {
                                        Log.d("ScannerScreen", "Analyzer running... Image size: ${imageProxy.width}x${imageProxy.height}")
                                        lastLogTime = currentTime
                                    }
                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            if (barcodes.isNotEmpty()) {
                                                val barcode = barcodes[0].rawValue
                                                barcode?.let { 
                                                    Log.d("ScannerScreen", "Barcode detected: $it")
                                                    viewModel.scanBarcode(it) 
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("ScannerScreen", "Barcode scanning failed", e)
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Camera permission required")
                }
            }

            // Scanning Overlay with Rectangle and Animated Bar
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val rectSize = canvasWidth * 0.7f
                val left = (canvasWidth - rectSize) / 2
                val top = (canvasHeight - rectSize) / 2
                val rect = Rect(left, top, left + rectSize, top + rectSize)

                // 1. Draw the semi-transparent background with a hole
                val backgroundPath = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
                    addRoundRect(RoundRect(rect, CornerRadius(20f, 20f)))
                }

                drawPath(
                    path = backgroundPath,
                    color = Color.Black.copy(alpha = 0.6f),
                )

                // 2. Draw the rectangle border
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(left, top),
                    size = ComposeSize(rectSize, rectSize),
                    cornerRadius = CornerRadius(20f, 20f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // 3. Draw the animated scanning bar
                val barY = top + (rectSize * scannerBarOffset)
                drawLine(
                    color = Color.Red,
                    start = Offset(left + 10.dp.toPx(), barY),
                    end = Offset(left + rectSize - 10.dp.toPx(), barY),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Overlay for scanned information
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 80.dp) // Move it up significantly to avoid system buttons
            ) {
                if (lastScannedBarcode != null && scannedProduct == null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Unknown Barcode: $lastScannedBarcode",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Not found in database",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onNavigateToDetails(lastScannedBarcode!!) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onError,
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Add to Database", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                if (scannedProduct != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(16.dp),
                        onClick = { onNavigateToDetails(scannedProduct!!.barcode) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (scannedProduct!!.imageUrl != null) {
                                        AsyncImage(
                                            model = scannedProduct!!.imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.ShoppingCart,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        scannedProduct!!.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(
                                        "ID: ${scannedProduct!!.barcode}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "₹${scannedProduct!!.price}",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    viewModel.addToCart(scannedProduct!!)
                                    onNavigateToCart()
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.ShoppingCart, null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Add to Cart & View (${cartItems.size})", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }

        if (showManualInputDialog) {
            AlertDialog(
                onDismissRequest = { showManualInputDialog = false },
                title = { Text("Enter Barcode") },
                text = {
                    TextField(
                        value = manualBarcode,
                        onValueChange = { manualBarcode = it },
                        placeholder = { Text("e.g. 8901030895432") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (manualBarcode.isNotBlank()) {
                            viewModel.scanBarcode(manualBarcode)
                            showManualInputDialog = false
                        }
                    }) {
                        Text("Search")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showManualInputDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
