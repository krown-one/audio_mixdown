package one.krown.audio_mixdown

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Implementación Android de la interfaz generada por Pigeon */
class AudioMixdownHostApiImpl(private val context: Context) : AudioMixdownHostApi {

  // Ajusta la firma EXACTA según tu Pigeon.kt (Reply/Result). Este es el patrón de Pigeon 26.x:
  override fun mix(req: MixRequest, callback: (Result<MixResult>) -> Unit) {
    try {
      val outPath = mixOverlay(
        voicePath = req.voicePath,
        bgPath    = req.bgPath,
        outPath   = req.outPath
      )
      callback(Result.success(MixResult(outPath)))
    } catch (e: Exception) {
      callback(Result.failure(e))
    }
  }

  /** Mezcla simple: voice + bg en paralelo y exporta a AAC/M4A */
  private fun mixOverlay(voicePath: String, bgPath: String, outPath: String): String {
    val voiceItem = EditedMediaItem.Builder(
      MediaItem.fromUri(Uri.fromFile(File(voicePath)))
    )
      // .setEffects(...)  // no usamos Effects para evitar incompatibilidades
      .build()

    val bgItem = EditedMediaItem.Builder(
      MediaItem.fromUri(Uri.fromFile(File(bgPath)))
    )
      // .setEffects(...)
      .build()

    // Dos secuencias paralelas => overlay en la Composition
    val seqVoice = EditedMediaItemSequence(listOf(voiceItem))
    val seqBg    = EditedMediaItemSequence(listOf(bgItem))

    val composition = Composition.Builder(listOf(seqVoice, seqBg))
      // .setEffects(...)  // sin Effects
      .build()

    val latch = CountDownLatch(1)
    var error: Exception? = null

    val transformer = Transformer.Builder(context)
      // Por compatibilidad: salida AAC en contenedor M4A (mime se infiere)
      .addListener(object : Transformer.Listener {
        override fun onCompleted(composition: Composition, result: ExportResult) {
          latch.countDown()
        }
        override fun onError(composition: Composition, result: ExportResult, exception: ExportException) {
          error = exception
          latch.countDown()
        }
      })
      .build()

    // ⬇️ En tu versión la firma es (composition, outputPath: String)
    transformer.start(composition, outPath)

    latch.await(5, TimeUnit.MINUTES)
    error?.let { throw it }
    return outPath
  }
}
