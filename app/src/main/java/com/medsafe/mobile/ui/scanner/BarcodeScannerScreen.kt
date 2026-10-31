package com.medsafe.mobile.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Tela cheia com a camera para ler o codigo de barras da caixa do remedio (CameraX + ML Kit).
 * Entrega o primeiro codigo lido em [aoLerCodigo]; [aoFechar] cancela sem ler nada.
 */
@Composable
fun BarcodeScannerScreen(
    aoLerCodigo: (String) -> Unit,
    aoFechar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contexto = LocalContext.current
    var temPermissao by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var permissaoNegada by remember { mutableStateOf(false) }
    val pedirPermissao = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
        temPermissao = concedida
        permissaoNegada = !concedida
    }

    LaunchedEffect(Unit) {
        if (!temPermissao) pedirPermissao.launch(Manifest.permission.CAMERA)
    }
    BackHandler(onBack = aoFechar)

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        if (temPermissao) {
            PreviaDaCamera(aoLerCodigo = aoLerCodigo)
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = "Aponte a câmera para o código de barras da caixa",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(3.dp, Color.White),
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                ) {}
                OutlinedButton(
                    onClick = aoFechar,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White,
                    ),
                ) {
                    Text("Cancelar", style = MaterialTheme.typography.titleLarge)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (permissaoNegada) {
                        "Sem acesso à câmera não dá para ler o código de barras. Você pode digitar o código no formulário, ou liberar a câmera nas configurações do celular."
                    } else {
                        "Para ler o código de barras, o app precisa usar a câmera."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                if (!permissaoNegada) {
                    Button(onClick = { pedirPermissao.launch(Manifest.permission.CAMERA) }, modifier = Modifier.height(56.dp)) {
                        Text("Permitir câmera", style = MaterialTheme.typography.titleLarge)
                    }
                }
                OutlinedButton(
                    onClick = aoFechar,
                    modifier = Modifier.height(56.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                ) {
                    Text("Voltar", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
private fun PreviaDaCamera(aoLerCodigo: (String) -> Unit) {
    val donoDoCicloDeVida = LocalLifecycleOwner.current
    val aoLerCodigoAtual by rememberUpdatedState(aoLerCodigo)
    val executor = remember { Executors.newSingleThreadExecutor() }
    val jaLeu = remember { AtomicBoolean(false) }
    val provedor = remember { arrayOfNulls<ProcessCameraProvider>(1) }

    DisposableEffect(Unit) {
        onDispose {
            provedor[0]?.unbindAll()
            executor.shutdown()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { contextoDaView ->
            val previaView = PreviewView(contextoDaView)
            val futuro = ProcessCameraProvider.getInstance(contextoDaView)
            futuro.addListener({
                val cameraProvider = futuro.get()
                provedor[0] = cameraProvider

                val previa = Preview.Builder().build()
                previa.setSurfaceProvider(previaView.surfaceProvider)

                val analise = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analise.setAnalyzer(
                    executor,
                    LeitorDeCodigoDeBarras { codigo ->
                        // So o primeiro codigo importa; as leituras seguintes do mesmo quadro sao ignoradas.
                        if (jaLeu.compareAndSet(false, true)) aoLerCodigoAtual(codigo)
                    },
                )

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(donoDoCicloDeVida, CameraSelector.DEFAULT_BACK_CAMERA, previa, analise)
            }, ContextCompat.getMainExecutor(contextoDaView))
            previaView
        },
    )
}

/** Analisa cada quadro da camera com o ML Kit, procurando codigos de barras lineares (EAN/UPC/Code 128...). */
private class LeitorDeCodigoDeBarras(
    private val aoDetectar: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    private val leitor = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_ITF,
            )
            .build(),
    )

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val imagemDaCamera = imageProxy.image
        if (imagemDaCamera == null) {
            imageProxy.close()
            return
        }
        val imagem = InputImage.fromMediaImage(imagemDaCamera, imageProxy.imageInfo.rotationDegrees)
        leitor.process(imagem)
            .addOnSuccessListener { codigos ->
                codigos.firstNotNullOfOrNull { it.rawValue?.takeIf { valor -> valor.isNotBlank() } }
                    ?.let(aoDetectar)
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
