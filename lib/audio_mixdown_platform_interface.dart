import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'audio_mixdown_method_channel.dart';

abstract class AudioMixdownPlatform extends PlatformInterface {
  /// Constructs a AudioMixdownPlatform.
  AudioMixdownPlatform() : super(token: _token);

  static final Object _token = Object();

  static AudioMixdownPlatform _instance = MethodChannelAudioMixdown();

  /// The default instance of [AudioMixdownPlatform] to use.
  ///
  /// Defaults to [MethodChannelAudioMixdown].
  static AudioMixdownPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [AudioMixdownPlatform] when
  /// they register themselves.
  static set instance(AudioMixdownPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
