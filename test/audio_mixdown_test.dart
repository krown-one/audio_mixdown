import 'package:flutter_test/flutter_test.dart';
import 'package:audio_mixdown/audio_mixdown.dart';
import 'package:audio_mixdown/audio_mixdown_platform_interface.dart';
import 'package:audio_mixdown/audio_mixdown_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockAudioMixdownPlatform
    with MockPlatformInterfaceMixin
    implements AudioMixdownPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final AudioMixdownPlatform initialPlatform = AudioMixdownPlatform.instance;

  test('$MethodChannelAudioMixdown is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelAudioMixdown>());
  });

  test('getPlatformVersion', () async {
    AudioMixdown audioMixdownPlugin = AudioMixdown();
    MockAudioMixdownPlatform fakePlatform = MockAudioMixdownPlatform();
    AudioMixdownPlatform.instance = fakePlatform;

    expect(await audioMixdownPlugin.getPlatformVersion(), '42');
  });
}
