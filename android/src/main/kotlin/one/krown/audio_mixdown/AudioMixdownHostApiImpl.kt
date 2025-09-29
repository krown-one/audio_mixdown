package one.krown.audio_mixdown

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.common.MimeTypes
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

private const val TAG = "AudioMixdown"

class AudioMixdownHostApiImpl(private val context: Context) : AudioMixdownHostApi {

  override fun mix(req: MixRequest, callback: (Result<MixResult>) -> Unit) {
    // Pigeon mapea int? -> Long? en Kotlin
    val delayMsInt = ((req.delayMs ?: 0L)
      .coerceAtLeast(0L)
      .coerceAtMost(Int.MAX_VALUE.toLong()))
      .toInt()

    try {
      val outPath = req.outPath
      File(outPath).parentFile?.mkdirs()

      val voiceDurMs = getDurationMs(req.voicePath).coerceAtLeast(1L)

      val voiceMedia = MediaItem.Builder()
        .setUri(Uri.fromFile(File(req.voicePath)))
        .setClippingConfiguration(
          ClippingConfiguration.Builder()
            .setStartPositionMs(0)
            .setEndPositionMs(voiceDurMs)
            .build()
        ).build()

      val bgMedia = MediaItem.Builder()
        .setUri(Uri.fromFile(File(req.bgPath)))
        .setClippingConfiguration(
          ClippingConfiguration.Builder()
            .setStartPositionMs(0)
            .setEndPositionMs(voiceDurMs)
            .build()
        ).build()

      val voiceItem = EditedMediaItem.Builder(voiceMedia).build()

      // delay: prepend silencio al BG si hace falta
      val bgSeqItems = mutableListOf<EditedMediaItem>()
      var silenceTmp: File? = null
      if (delayMsInt > 0) {
        silenceTmp = createSilenceWav(delayMsInt, 44100, 2)
        bgSeqItems += EditedMediaItem.Builder(
          MediaItem.fromUri(Uri.fromFile(silenceTmp))
        ).build()
      }
      bgSeqItems += EditedMediaItem.Builder(bgMedia).build()

      val seqVoice = EditedMediaItemSequence(listOf(voiceItem))
      val seqBg    = EditedMediaItemSequence(bgSeqItems)

      val composition = Composition.Builder(listOf(seqVoice, seqBg)).build()

      var replied = false
      val transformer = Transformer.Builder(context)
        .setAudioMimeType(MimeTypes.AUDIO_AAC) // salida AAC (.m4a)
        .addListener(object : Transformer.Listener {
          override fun onCompleted(composition: Composition, result: ExportResult) {
            if (!replied) {
              replied = true
              try { silenceTmp?.delete() } catch (_: Exception) {}
              Log.d(TAG, "COMPLETED -> $outPath (${File(outPath).length()} bytes)")
              callback(Result.success(MixResult(outPath)))
            }
          }
          override fun onError(composition: Composition, result: ExportResult, exception: ExportException) {
            // Media3 a veces emite un “error after export ended” (watchdog).
            if (!replied) {
              replied = true
              try { silenceTmp?.delete() } catch (_: Exception) {}
              Log.w(TAG, "ERROR -> ${exception.message}")
              callback(Result.failure(exception))
            } else {
              Log.w(TAG, "Ignored late error after completion: ${exception.message}")
            }
          }
        })
        .build()

      Log.d(TAG, "Starting export -> $outPath")
      transformer.start(composition, outPath)

      // ← NO esperamos aquí; respondemos en onCompleted/onError
    } catch (e: Exception) {
      callback(Result.failure(e))
    }
  }

  private fun createSilenceWav(durationMs: Int, sampleRate: Int, channels: Int): File {
    val frames = (durationMs.toLong() * sampleRate / 1000L).toInt().coerceAtLeast(1)
    val bytesPerSample = 2 // 16-bit PCM
    val dataSize = frames * channels * bytesPerSample
    val byteRate = sampleRate * channels * bytesPerSample
    val blockAlign = channels * bytesPerSample
    val riffSize = 36 + dataSize

    val out = File.createTempFile("silence_${durationMs}ms_", ".wav", context.cacheDir)
    FileOutputStream(out).use { fos ->
      val hdr = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
      hdr.put("RIFF".toByteArray(Charsets.US_ASCII))
      hdr.putInt(riffSize)
      hdr.put("WAVE".toByteArray(Charsets.US_ASCII))
      hdr.put("fmt ".toByteArray(Charsets.US_ASCII))
      hdr.putInt(16) // PCM
      hdr.putShort(1) // formato PCM
      hdr.putShort(channels.toShort())
      hdr.putInt(sampleRate)
      hdr.putInt(byteRate)
      hdr.putShort(blockAlign.toShort())
      hdr.putShort(16.toShort()) // bitsPerSample
      hdr.put("data".toByteArray(Charsets.US_ASCII))
      hdr.putInt(dataSize)
      fos.write(hdr.array())

      val buf = ByteArray(4096)
      var remaining = dataSize
      while (remaining > 0) {
        val n = min(remaining, buf.size)
        fos.write(buf, 0, n)
        remaining -= n
      }
      fos.flush()
    }
    return out
  }

  private fun getDurationMs(path: String): Long {
    val mmr = MediaMetadataRetriever()
    mmr.setDataSource(path)
    val d = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
    mmr.release()
    return d
  }
}
