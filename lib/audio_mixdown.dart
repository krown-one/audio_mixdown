import 'src/pigeon.g.dart';

class AudioMixdown {
  static final _api = AudioMixdownHostApi();

  static Future<String> mix({
    required String voicePath,
    required String bgPath,
    required String outPath,
    double voiceGain = 1.0,
    double bgGain = 0.35,
    int delayMs = 400,
    int fadeInMs = 600,
    int fadeOutMs = 600,
  }) async {
    final req = MixRequest(
      voicePath: voicePath,
      bgPath: bgPath,
      outPath: outPath,
      voiceGain: voiceGain,
      bgGain: bgGain,
      delayMs: delayMs,
      fadeInMs: fadeInMs,
      fadeOutMs: fadeOutMs,
    );

    final res = await _api.mix(req);
    if (res.error != null && res.error!.isNotEmpty) {
      throw Exception(res.error);
    }
    return res.outPath;
  }
}
