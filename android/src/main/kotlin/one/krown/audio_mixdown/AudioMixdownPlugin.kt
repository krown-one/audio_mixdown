package one.krown.audio_mixdown

import io.flutter.embedding.engine.plugins.FlutterPlugin

/** AudioMixdownPlugin: registra la API generada por Pigeon */
class AudioMixdownPlugin : FlutterPlugin {

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    // IMPLEMENTACIÓN nativa de la HostApi
    val apiImpl = AudioMixdownHostApiImpl(binding.applicationContext)

    // Clase generada por Pigeon (Pigeon.kt) que hace el wiring binario
    AudioMixdownHostApi.setUp(binding.binaryMessenger, apiImpl)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    // Desregistra la API si tu versión de Pigeon lo soporta
    AudioMixdownHostApi.setUp(binding.binaryMessenger, null)
  }
}
