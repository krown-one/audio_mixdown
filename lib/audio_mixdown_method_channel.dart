import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'audio_mixdown_platform_interface.dart';

/// An implementation of [AudioMixdownPlatform] that uses method channels.
class MethodChannelAudioMixdown extends AudioMixdownPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('audio_mixdown');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
