
import 'audio_mixdown_platform_interface.dart';

class AudioMixdown {
  Future<String?> getPlatformVersion() {
    return AudioMixdownPlatform.instance.getPlatformVersion();
  }
}
