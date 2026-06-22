package com.codelab.parte_2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.codelab.parte_2.data.local.AppDatabase
import com.codelab.parte_2.data.local.repositories.EstadoRotacionRepository
import com.codelab.parte_2.data.local.repositories.MedidaHistoricaRepository
import com.codelab.parte_2.data.local.repositories.PotreroRepository
import com.codelab.parte_2.databinding.ActivityFormPotreroBinding
import com.codelab.parte_2.ui.form.FormPotreroViewModel
import com.codelab.parte_2.ui.form.FormPotreroViewModelFactory
import com.codelab.parte_2.util.MediaStorageHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla 2 — alta/edición de un potrero: nombre, medida en m²,
 * fecha de creación (automática), foto y video (cámara o galería),
 * con validaciones e insert/update en Room.
 */
class FormPotreroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormPotreroBinding

    private val viewModel: FormPotreroViewModel by viewModels {
        val db = AppDatabase.getInstance(applicationContext)
        FormPotreroViewModelFactory(
            PotreroRepository(db.potreroDao()),
            EstadoRotacionRepository(db.estadoRotacionDao()),
            MedidaHistoricaRepository(db.medidaHistoricaDao())
        )
    }

    private val potreroId: Int by lazy { intent.getIntExtra(EXTRA_POTRERO_ID, -1) }

    private var fechaCreacion: Long = hoyNormalizadoUtc()
    private var fotoUri: Uri? = null
    private var videoUri: Uri? = null

    // Uris "de salida" cuando la cámara va a escribir directo a un archivo nuestro
    private var fotoUriTemporal: Uri? = null
    private var videoUriTemporal: Uri? = null

    private var accionCamaraPendiente: AccionCamara? = null
    private enum class AccionCamara { FOTO, VIDEO }

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // ---------- Launchers ----------

    private val permisoCamaraLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            when (accionCamaraPendiente) {
                AccionCamara.FOTO -> lanzarCamaraFoto()
                AccionCamara.VIDEO -> lanzarCamaraVideo()
                null -> Unit
            }
        } else {
            mostrarMensaje(getString(R.string.error_permiso_camara))
        }
        accionCamaraPendiente = null
    }

    private val tomarFotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito && fotoUriTemporal != null) {
            fotoUri = fotoUriTemporal
            mostrarPreviewFoto(fotoUri!!)
        }
    }

    private val grabarVideoLauncher = registerForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { exito ->
        if (exito && videoUriTemporal != null) {
            videoUri = videoUriTemporal
            actualizarEstadoVideo()
        }
    }

    private val elegirFotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { copiarYMostrarFoto(it) } }

    private val elegirVideoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { copiarYGuardarVideo(it) } }

    // ---------- Ciclo de vida ----------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormPotreroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        if (potreroId != -1) {
            supportActionBar?.title = getString(R.string.title_editar_potrero)
            cargarPotreroExistente()
        } else {
            supportActionBar?.title = getString(R.string.title_nuevo_potrero)
            binding.textFechaCreacion.text =
                getString(R.string.potrero_fecha_format, dateFormat.format(Date(fechaCreacion)))
        }

        binding.btnTomarFoto.setOnClickListener { solicitarCamara(AccionCamara.FOTO) }
        binding.btnElegirFoto.setOnClickListener { elegirFotoLauncher.launch("image/*") }
        binding.btnGrabarVideo.setOnClickListener { solicitarCamara(AccionCamara.VIDEO) }
        binding.btnElegirVideo.setOnClickListener { elegirVideoLauncher.launch("video/*") }
        binding.btnVerVideo.setOnClickListener { reproducirVideo() }

        binding.btnGuardar.setOnClickListener { validarYGuardar() }
    }

    // ---------- Carga de datos existentes (modo edición) ----------

    private fun cargarPotreroExistente() {
        lifecycleScope.launch {
            val potrero = viewModel.cargarPotrero(potreroId)
            if (potrero == null) {
                mostrarMensaje(getString(R.string.error_potrero_no_encontrado))
                finish()
                return@launch
            }

            binding.editNombre.setText(potrero.nombre)
            binding.editMedida.setText(potrero.medidaM2.toString())

            fechaCreacion = potrero.fechaCreacion
            binding.textFechaCreacion.text =
                getString(R.string.potrero_fecha_format, dateFormat.format(Date(fechaCreacion)))

            potrero.fotoUri?.let {
                fotoUri = Uri.parse(it)
                mostrarPreviewFoto(fotoUri!!)
            }
            potrero.videoUri?.let {
                videoUri = Uri.parse(it)
                actualizarEstadoVideo()
            }
        }
    }

    // ---------- Cámara ----------

    private fun solicitarCamara(accion: AccionCamara) {
        val tienePermiso = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            when (accion) {
                AccionCamara.FOTO -> lanzarCamaraFoto()
                AccionCamara.VIDEO -> lanzarCamaraVideo()
            }
        } else {
            accionCamaraPendiente = accion
            permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun lanzarCamaraFoto() {
        val archivo = MediaStorageHelper.crearArchivoTemporal(this, "IMG", "jpg")
        val uri = MediaStorageHelper.uriParaArchivo(this, archivo)
        fotoUriTemporal = uri
        tomarFotoLauncher.launch(uri)
    }

    private fun lanzarCamaraVideo() {
        val archivo = MediaStorageHelper.crearArchivoTemporal(this, "VID", "mp4")
        val uri = MediaStorageHelper.uriParaArchivo(this, archivo)
        videoUriTemporal = uri
        grabarVideoLauncher.launch(uri)
    }

    // ---------- Galería ----------

    private fun copiarYMostrarFoto(origen: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val extension = MediaStorageHelper.obtenerExtension(this@FormPotreroActivity, origen, "jpg")
            val copiada = MediaStorageHelper.copiarAAlmacenamientoApp(
                this@FormPotreroActivity, origen, "IMG", extension
            )
            withContext(Dispatchers.Main) {
                if (copiada != null) {
                    fotoUri = copiada
                    mostrarPreviewFoto(copiada)
                } else {
                    mostrarMensaje(getString(R.string.error_copiar_archivo))
                }
            }
        }
    }

    private fun copiarYGuardarVideo(origen: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val extension = MediaStorageHelper.obtenerExtension(this@FormPotreroActivity, origen, "mp4")
            val copiado = MediaStorageHelper.copiarAAlmacenamientoApp(
                this@FormPotreroActivity, origen, "VID", extension
            )
            withContext(Dispatchers.Main) {
                if (copiado != null) {
                    videoUri = copiado
                    actualizarEstadoVideo()
                } else {
                    mostrarMensaje(getString(R.string.error_copiar_archivo))
                }
            }
        }
    }

    // ---------- Preview ----------

    private fun mostrarPreviewFoto(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = decodificarBitmapEscalado(uri, 800)
            withContext(Dispatchers.Main) {
                if (bitmap != null) {
                    binding.imagePreview.setImageBitmap(bitmap)
                    binding.imagePreview.visibility = View.VISIBLE
                }
            }
        }
    }

    /** Decodifica con downsampling para no comerse la memoria con fotos de cámara a full res. */
    private fun decodificarBitmapEscalado(uri: Uri, anchoObjetivo: Int): Bitmap? {
        return try {
            val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }

            var inSampleSize = 1
            while (opciones.outWidth / inSampleSize > anchoObjetivo) {
                inSampleSize *= 2
            }

            val opcionesFinal = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opcionesFinal) }
        } catch (e: Exception) {
            null
        }
    }

    private fun actualizarEstadoVideo() {
        binding.textVideoEstado.text = getString(R.string.video_seleccionado)
        binding.btnVerVideo.visibility = View.VISIBLE
    }

    private fun reproducirVideo() {
        val uri = videoUri ?: return
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            mostrarMensaje(getString(R.string.error_sin_reproductor))
        }
    }

    // ---------- Validación y guardado ----------

    private fun validarYGuardar() {
        val nombre = binding.editNombre.text?.toString()?.trim().orEmpty()
        val medidaTexto = binding.editMedida.text?.toString()?.trim().orEmpty()

        var esValido = true

        if (nombre.isEmpty()) {
            binding.layoutNombre.error = getString(R.string.error_nombre_vacio)
            esValido = false
        } else {
            binding.layoutNombre.error = null
        }

        // Acepta coma o punto como separador decimal según el teclado del dispositivo
        val medida = medidaTexto.replace(",", ".").toDoubleOrNull()
        if (medida == null || medida <= 0.0) {
            binding.layoutMedida.error = getString(R.string.error_medida_invalida)
            esValido = false
        } else {
            binding.layoutMedida.error = null
        }

        if (!esValido) return

        binding.btnGuardar.isEnabled = false
        viewModel.guardar(
            potreroId = potreroId,
            nombre = nombre,
            medidaM2 = medida!!,
            fechaCreacion = fechaCreacion,
            fotoUri = fotoUri?.toString(),
            videoUri = videoUri?.toString()
        ) { exito ->
            binding.btnGuardar.isEnabled = true
            if (exito) {
                mostrarMensaje(getString(R.string.msg_guardado_ok))
                finish()
            } else {
                mostrarMensaje(getString(R.string.msg_guardado_error))
            }
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_SHORT).show()
    }

    private fun hoyNormalizadoUtc(): Long {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    companion object {
        const val EXTRA_POTRERO_ID = "extra_potrero_id"
    }
}