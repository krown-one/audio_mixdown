import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

import 'package:audio_mixdown/audio_mixdown.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});
  @override
  Widget build(BuildContext context) => const MaterialApp(home: MixDemoPage());
}

class MixDemoPage extends StatefulWidget {
  const MixDemoPage({super.key});
  @override
  State<MixDemoPage> createState() => _MixDemoPageState();
}

class _MixDemoPageState extends State<MixDemoPage> {
  String? _voicePath;
  String? _bgPath;
  String? _outPath;
  String? _err;
  bool _busy = false;

  @override
  void initState() {
    super.initState();
    _prepareSampleFiles(); // copia assets -> archivos reales
  }

  Future<void> _prepareSampleFiles() async {
    try {
      final docs = await getApplicationDocumentsDirectory();
      final sampleDir = Directory(p.join(docs.path, 'samples'));
      if (!await sampleDir.exists()) await sampleDir.create(recursive: true);

      final voice = await _writeAsset('assets/audio/voice_sample.m4a', p.join(sampleDir.path, 'voice_sample.m4a'));
      final bg = await _writeAsset('assets/audio/ghost_sample.m4a', p.join(sampleDir.path, 'ghost_sample.m4a'));

      setState(() {
        _voicePath = voice.path;
        _bgPath = bg.path;
      });
    } catch (e) {
      setState(() => _err = 'Error preparando assets: $e');
    }
  }

  Future<File> _writeAsset(String asset, String outPath) async {
    final bytes = await rootBundle.load(asset);
    final f = File(outPath);
    await f.writeAsBytes(bytes.buffer.asUint8List(bytes.offsetInBytes, bytes.lengthInBytes), flush: true);
    return f;
  }

  Future<void> _mix() async {
    if (_voicePath == null || _bgPath == null) return;
    setState(() {
      _busy = true;
      _err = null;
      _outPath = null;
    });

    try {
      final docs = await getApplicationDocumentsDirectory();
      final outDir = Directory(p.join(docs.path, 'audios'));
      if (!await outDir.exists()) await outDir.create(recursive: true);

      final out = p.join(outDir.path, 'final_${DateTime.now().millisecondsSinceEpoch}.m4a');

      final result = await AudioMixdown.mix(voicePath: _voicePath!, bgPath: _bgPath!, outPath: out, voiceGain: 1.0, bgGain: 0.35, delayMs: 400, fadeInMs: 600, fadeOutMs: 600);

      setState(() => _outPath = result);
    } catch (e) {
      setState(() => _err = '$e');
    } finally {
      setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final ready = _voicePath != null && _bgPath != null;
    return Scaffold(
      appBar: AppBar(title: const Text('audio_mixdown example (assets)')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Archivos:'),
            Text('voz:   ${_voicePath ?? "..."}'),
            Text('fondo: ${_bgPath ?? "..."}'),
            const SizedBox(height: 12),
            ElevatedButton.icon(onPressed: ready && !_busy ? _mix : null, icon: const Icon(Icons.play_arrow), label: Text(_busy ? 'Procesando…' : 'Mezclar')),
            const SizedBox(height: 12),
            if (_outPath != null) SelectableText('✅ Salida: $_outPath'),
            if (_err != null) Text('❌ $_err', style: const TextStyle(color: Colors.red)),
          ],
        ),
      ),
    );
  }
}
