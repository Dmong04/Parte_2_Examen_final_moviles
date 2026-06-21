package com.codelab.parte_2.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

/**
 * Maneja dónde y cómo se guardan las fotos/videos que captura o elige el
 * usuario. Siempre termina en un archivo dentro del almacenamiento propio
 * de la app (vía FileProvider), así Room solo guarda el URI/path (String),
 * nunca el binario, y ese URI sigue siendo válido aunque se reinicie el
 * dispositivo o se actualice la app.
 */
object MediaStorageHelper {

    private const val SUFIJO_AUTORIDAD = ".fileprovider"

    fun crearArchivoTemporal(context: Context, prefijo: String, extension: String): File {
        val dir = context.getExternalFilesDir("media") ?: File(context.filesDir, "media")
        if (!dir.exists()) dir.mkdirs()
        val timestamp = System.currentTimeMillis()
        return File(dir, "${prefijo}_$timestamp.$extension")
    }

    fun uriParaArchivo(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            context.packageName + SUFIJO_AUTORIDAD,
            file
        )
    }

    /** Copia el contenido de un URI externo (elegido en galería) a un archivo propio. */
    fun copiarAAlmacenamientoApp(
        context: Context,
        origen: Uri,
        prefijo: String,
        extension: String
    ): Uri? {
        return try {
            val destino = crearArchivoTemporal(context, prefijo, extension)
            val copiado = context.contentResolver.openInputStream(origen)?.use { input ->
                destino.outputStream().use { output -> input.copyTo(output) }
                true
            } ?: false

            if (copiado) uriParaArchivo(context, destino) else null
        } catch (e: IOException) {
            null
        }
    }

    fun obtenerExtension(context: Context, uri: Uri, porDefecto: String): String {
        val mime = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: porDefecto
    }
}
