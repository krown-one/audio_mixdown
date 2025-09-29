import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(PigeonOptions(
  dartPackageName: 'audio_mixdown',
  dartOut: 'lib/src/pigeon.g.dart',
  kotlinOut: 'android/src/main/kotlin/one/krown/audio_mixdown/Pigeon.kt',
  kotlinOptions: KotlinOptions(package: 'one.krown.audio_mixdown'),
  swiftOut: 'ios/Classes/Pigeon.swift',
))

class MixRequest {
  // Rutas absolutas
  late String voicePath;
  late String bgPath;
  late String outPath;

  // Parámetros opcionales (Dart les pondrá default desde el wrapper)
  double? voiceGain; // 1.0
  double? bgGain; // 0.35
  int? delayMs; // 400
  int? fadeInMs; // 600
  int? fadeOutMs; // 600
}

class MixResult {
  late String outPath; // ruta final exportada
  String? error; // mensaje opcional si algo falla
}

@HostApi() // Implementación nativa (Android/iOS), llamada desde Dart
abstract class AudioMixdownHostApi {
  @async
  MixResult mix(MixRequest req);
}
