package com.medsafe.mobile.ui.scanner

import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Cliente do ML Kit usado pelo scanner, em um unico lugar: o app e o teste instrumentado leem
 * codigos com exatamente os mesmos formatos (os lineares das caixas de remedio).
 */
internal fun criarLeitorDeCodigos(): BarcodeScanner = BarcodeScanning.getClient(
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

/** O primeiro valor nao vazio entre os codigos encontrados no quadro, ou null se nao ha nenhum. */
internal fun primeiroValor(codigos: List<Barcode>): String? =
    codigos.firstNotNullOfOrNull { it.rawValue?.takeIf { valor -> valor.isNotBlank() } }
