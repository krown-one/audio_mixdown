# audio_mixdown

**Flutter plugin (Android/iOS) para mezclar audio**: combina una grabación de voz con una pista de fondo (tu “fantasma”), aplica *delay* y *fades* (iOS hoy; Android v1 overlay básico) y exporta **.m4a (AAC)** listo para compartir (p. ej. WhatsApp).

* **Org / reverse‑DNS:** `one.krown`
* **Canal (Pigeon):** generado (sin strings mágicos)
* **Salida:** `.m4a` (AAC 128 kbps, 44.1 kHz)
* **Repo:** `krown-one/audio_mixdown`

> **Estado:** iOS ✅ (gains + delay + fades). Android v1 ✅ (mezcla superpuesta). Android v2 ➜ (delay + fades + gain) — ver Roadmap.

---

## Características

* Mezcla **voz + fondo** y exporta **.m4a (AAC)**.
* Parámetros: `voiceGain`, `bgGain`, `delayMs`, `fadeInMs`, `fadeOutMs`.
* **iOS (AVAudioEngine)**: render offline con fades y delay.
* **Android (Media3 Transformer)**: overlay básico (paridad completa en Roadmap).
* API **tipada con Pigeon** (Dart ⇆ Swift/Kotlin) — sin `MethodChannel` manual.

---

## Requisitos

* Flutter 3.19+
* Android: **minSdk 21**, **compileSdk 34**, Java/Kotlin **17**.
* iOS: **12.0+**, Swift 5.

---

## Instalación (en tu app)

En `pubspec.yaml` de tu app:

```yaml
dependencies:
  audio_mixdown:
    git:
      url: https://github.com/krown-one/audio_mixdown.git
      ref: main
```

> O localmente, si estás desarrollando el plugin: `path: ../audio_mixdown`.

---

## Uso rápido (Dart)

```dart
import 'package:audio_mixdown/audio_mixdown.dart';

final outPath = await AudioMixdown.mix(
  voicePath: "/abs/path/voice.m4a",
  bgPath:    "/abs/path/ghost_fx.m4a",
  outPath:   "/abs/path/final_123.m4a",
  voiceGain: 1.0,
  bgGain:    0.35,
  delayMs:   400,
  fadeInMs:  600,
  fadeOutMs: 600,
);
```

**Compartir a WhatsApp (opcional):**

```dart
import 'package:share_plus/share_plus.dart';
await Share.shareXFiles([
  XFile(outPath, mimeType: 'audio/mp4'), // .m4a = audio/mp4
], text: '👻 Mira esto');
```

---

## Parámetros de `mix()`

| Parámetro   | Tipo     | Default | Descripción                                            |
| ----------- | -------- | ------: | ------------------------------------------------------ |
| `voicePath` | `String` |       — | Ruta absoluta al archivo de voz (grabación).           |
| `bgPath`    | `String` |       — | Ruta absoluta al archivo de fondo (efecto/“fantasma”). |
| `outPath`   | `String` |       — | Ruta de salida `.m4a`. La carpeta debe existir.        |
| `voiceGain` | `double` |   `1.0` | Volumen lineal de voz (≈ +6 dB a 2.0).                 |
| `bgGain`    | `double` |  `0.35` | Volumen lineal del fondo.                              |
| `delayMs`   | `int`    |   `400` | Retraso del fondo vs. voz (iOS ✅ / Android v2).        |
| `fadeInMs`  | `int`    |   `600` | Fade‑in del fondo (iOS ✅ / Android v2).                |
| `fadeOutMs` | `int`    |   `600` | Fade‑out al final (iOS ✅ / Android v2).                |

**Notas**

* Si `fade*` excede la duración, se recorta de forma segura.
* Re-muestreo automático a 44.1 kHz/estéreo para la exportación.

---

## Formatos

* **Entrada**: `.m4a/.aac/.wav/.mp3` (mono o estéreo).
* **Salida**: `.m4a (AAC 128 kbps, 44.1 kHz, estéreo)` — compatible con reproducción directa en WhatsApp.

---

## Android (nativo)

* **Dependencias** (módulo del plugin):

  * `androidx.media3:media3-transformer:1.8.0`
  * `androidx.media3:media3-effect:1.8.0`
  * `androidx.media3:media3-extractor:1.8.0`
  * `androidx.media3:media3-common:1.8.0`
* **Manifest del plugin**: `package="one.krown.audio_mixdown"`.
* **ProGuard (opcional)**: mantener clases del plugin.
* **Permisos**: el plugin **no** graba; si tu app graba, declara `RECORD_AUDIO` y solicita runtime permission en la app.

### Limitaciones v1 (Android)

* Mezcla **superpuesta** (sin delay/fades). Paridad completa en Roadmap (v2).

---

## iOS (nativo)

* **Frameworks del sistema**: `AVFoundation`, `AudioToolbox` (via Podspec).
* **Render offline** con `AVAudioEngine` (fades + delay). Salida `.m4a (AAC)`.
* **Permisos**: el plugin no graba; si tu app graba, añade en tu `Info.plist`:

```xml
<key>NSMicrophoneUsageDescription</key>
<string>Necesitamos el micrófono para grabar tu mensaje.</string>
```

---

## Pigeon (contrato tipado)

### Configuración recomendada (Pigeon 26.x+) con `@ConfigurePigeon`

A partir de Pigeon 26.x es necesario indicar el **nombre del paquete Dart**. Te recomendamos configurar todo en el propio archivo de contrato.

1. **Declara la configuración** en `pigeons/audio_mixdown.dart`:

```dart
import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(PigeonOptions(
  dartPackageName: 'audio_mixdown',
  dartOut: 'lib/src/pigeon.g.dart',
  kotlinOut: 'android/src/main/kotlin/one/krown/audio_mixdown/Pigeon.kt',
  kotlinOptions: KotlinOptions(package: 'one.krown.audio_mixdown'),
  swiftOut: 'ios/Classes/Pigeon.swift',
))

// ↓ Aquí van tus clases y APIs Pigeon (MixRequest, MixResult, @HostApi ...)
```

> `dartPackageName` es **obligatorio** en Pigeon 26.x para que pueda deducir el paquete de salida.

2. **Genera el código** (desde la raíz del plugin):

```bash
dart run pigeon --input pigeons/audio_mixdown.dart
```

Se generarán:

* Dart: `lib/src/pigeon.g.dart`
* Android: `android/src/main/kotlin/one/krown/audio_mixdown/Pigeon.kt`
* iOS: `ios/Classes/Pigeon.swift`

> Vuelve a ejecutar el comando siempre que cambies el contrato Pigeon.

**Sugerencia de dev_dependency en el plugin:**

```yaml
# pubspec.yaml (del plugin)
dev_dependencies:
  pigeon: ^26.0.1
```

---

## Ejemplo (example/) (example/)

El template del plugin incluye un proyecto `example/`:

```bash
# iOS
cd example/ios && pod install && cd ..
flutter run -d ios

# Android
a dbus-launch flutter run -d android # (si lo requieres en Linux)
```

Pasa rutas reales a archivos `voicePath` y `bgPath` en el ejemplo.

---

## Troubleshooting

* **iOS: `pod install`** — si faltan frameworks, corre `pod install` en `example/ios`.
* **Android: `NoClassDefFoundError` Media3** — verifica `compileSdk 34`, Java/Kotlin 17 y dependencias Media3.
* **Ruta inexistente** — crea la carpeta de `outPath` antes de llamar a `mix()`.
* **Clipping/volumen** — baja `bgGain` (p. ej. 0.25–0.4) o `voiceGain` si distorsiona.

---

## Roadmap

* **Android v2**: `delayMs`, `fadeInMs`, `fadeOutMs`, `voiceGain/bgGain` vía `AudioProcessor` y/o items de silencio.
* **Opcionales**: `panBg`, segmentado (`bgStartMs/bgEndMs`), `normalize`, tasa de bits configurable.

---

## Licencia

MIT © krown.one — ajusta si tu proyecto requiere una licencia distinta.

---

## Créditos

Hecho con ❤️ por **krown.one**. Soporte y contacto: [https://krown.one](https://krown.one)
