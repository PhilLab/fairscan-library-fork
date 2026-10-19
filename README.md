> [!IMPORTANT]
> **This is an unofficial fork of [FairScan](https://github.com/pynicolas/FairScan), made so that other apps can embed its scanner.**
> It is **not** maintained by, endorsed by or affiliated with the FairScan author, and it is
> **not** intended as a general-purpose library or an official FairScan artifact.
>
> - Issues with the **library integration** (packaging, embedding, intents, build) belong **here**.
> - Issues with **document detection or the UI** belong to [upstream](https://github.com/pynicolas/FairScan/issues),
>   but **only if you have reproduced them in the original FairScan app first**.
>
> FairScan is licensed under the **GNU GPL v3**; so is this fork, and apps embedding it must be GPL-compatible.
> The FairScan name, license and attribution remain visible on the About screen.

Artifacts are built by [JitPack](https://jitpack.io) from the git tags of this
repository (`v<FairScan version>-lib-<N>`). The FairScan version is occasionally bumped for bugfixes or new features,
and the `-lib-<N>` suffix is incremented for library-specific changes or fixes.

There are no guarantees about API stability. Always pin an exact tag, as a different tag might
introduce breaking changes.

## Using FairScan as a library

1. Add the dependency.
   ```kotlin
   // settings.gradle.kts
   dependencyResolutionManagement {
       repositories {
           maven {
               url = uri("https://jitpack.io")
               content { includeGroup("com.github.phillab.fairscan-library-fork") }
           }
       }
   }

   // build.gradle.kts
   dependencies {
       implementation("com.github.phillab.fairscan-library-fork:scan-ui:<tag>")
   }
   ```
   JitPack builds the code on its own servers, so:
   - Always pin an exact tag. Never use a branch name or a `-SNAPSHOT`.
   - Enable [Gradle dependency verification](https://docs.gradle.org/current/userguide/dependency_verification.html)
     and commit the result: `./gradlew --write-verification-metadata sha256 help`.

2. Tell FairScan where it can store its files, in your own app's `AndroidManifest.xml`:
   ```xml
   <application>
       ...
       <meta-data
           android:name="org.fairscan.app.STORAGE_SUBDIR"
           android:value="fairscan" />
   </application>
   ```
   This is required - FairScan throws immediately at startup if it's missing. The value names one
   subdirectory FairScan creates and confines itself to under **both** your app's own `filesDir`
   and `cacheDir` (i.e. `<filesDir>/<value>` and `<cacheDir>/<value>`, independently)
   - `filesDir` holds persistent data (in-progress scans, OCR language data, debug logs)
   - `cacheDir` holds transient data (PDF export staging, per-call session folders)
     that FairScan deletes on its own after a while.

   An empty value (`android:value=""`) has FairScan write directly into your `filesDir` and `cacheDir`
   themselves instead of a subdirectory of each. Only do this if nothing else in your app uses
   those directories, since FairScan's file creation and cleanup might interfere with your app.
3. It is advised to hide the "Support" section of the About screen, so your users don't
   directly contact FairScan's developers for support. Add to your manifest:
   ```xml
   <application>
       ...
       <meta-data
           android:name="org.fairscan.app.SHOW_SUPPORT"
           android:value="false" />
   </application>
   ```
   It defaults to `true`.
4. Start a scan and get the result:
   ```kotlin
   import org.fairscan.app.FairScan

   val scanLauncher = registerForActivityResult(
       ActivityResultContracts.StartActivityForResult()
   ) { result ->
       val pdfUri = FairScan.pdfUriFromResult(result.resultCode, result.data)
       if (pdfUri != null) {
           // copy the content of pdfUri immediately, FairScan deletes it later
       }
   }
   scanLauncher.launch(FairScan.scanToPdfIntent(context))
   ```
   Use an **explicit** intent this way rather than firing `FairScan.ACTION_SCAN_TO_PDF` as an
   implicit intent (as described in "Scan to PDF via intent" below, for a separately installed
   FairScan): the library's `MainActivity` has no intent-filter of its own, precisely so that
   embedding it doesn't turn your host app's own "scan" action into a disambiguation dialog against
   a separately installed FairScan on the same device.

---

## Local library development

### Testing uncommitted changes
1. Run `./gradlew publishToMavenLocal` in this repository
2. In your app add
  `mavenLocal { content { includeGroup("com.github.phillab.fairscan-library-fork") } }` to your
  repositories.
3. Locally built artifacts have the version `local-SNAPSHOT`; re-run the command whenever you change this checkout.

### Checking a published tag
Build the same tag yourself with `git checkout <tag> && ./gradlew publishToMavenLocal -Pversion=<tag>`,
then compare the AAR/POM in `~/.m2` with the downloaded ones (the AAR contents, not the zip bytes, as timestamps differ).

### Building entirely from source
If you prefer not to depend on a binary host at all, add this repository as a git submodule
and use `includeBuild("path/to/fork")`; the build then happens entirely from source.

---

## Updating the fork to a newer FairScan version

1. Fetch upstream and bring this branch onto the new release (or merge it, if you prefer):
   ```bash
   git fetch origin --tags
   git rebase v<new FairScan version>
   ```
   Resolve conflicts if there are any.
2. Check what upstream changed that the library setup depends on:
   - Intent filters or other activity/application attributes changed in upstream's
     `app/src/main/AndroidManifest.xml` belong in `sample/src/main/AndroidManifest.xml`; the
     library manifest must stay free of them.
   - New code must not use `filesDir`/`cacheDir` directly, or `R.string.app_name`, as these
     bypass the storage subdirectory or resolve to the host app's values.
3. Verify locally:
   ```bash
   ./gradlew clean check :sample:assembleRelease publishToMavenLocal
   ```
4. Push the branch (`git push --force-with-lease` after a rebase), then tag and push the tag:
   ```bash
   git tag v<new FairScan version>-lib-1
   git push <remote> v<new FairScan version>-lib-1
   ```
   Tags are never moved once published. For another build on the same FairScan version
   (e.g. a fix in the library setup), use the next number: `-lib-2`, `-lib-3`, ...
5. Open `https://jitpack.io/#phillab/fairscan-library-fork` and trigger the build of the new
   tag (click "Get it") and check that it is green.
6. Switch the apps using this library to the new tag.

---

<p align="center">
  <img src="metadata/en-US/images/icon.png" alt="FairScan icon" width="120" />
</p>

<h1 align="center">FairScan</h1>

<p align="center">
  An Android app to scan your documents —
<br/><b>simple</b> and <b>respectful</b>.
</p>

<p align="center">
  <a href="https://github.com/pynicolas/FairScan/releases"><img src="https://img.shields.io/github/v/release/pynicolas/FairScan?logo=github" /></a>
  <a href="LICENSE"><img alt="License" src="https://img.shields.io/github/license/pynicolas/FairScan?color=blue"></a>
</p>
<h3 align="center">
  <b>Get it on:</b>
  <a href="https://f-droid.org/en/packages/org.fairscan.app/">F-Droid</a> ·
  <a href="https://play.google.com/store/apps/details?id=org.fairscan.app">Google Play</a> ·
  <a href="https://github.com/pynicolas/FairScan/releases">GitHub</a>
</h3>

---

FairScan is an Android app to **scan documents quickly, easily and privately**.

It's designed to be **simple**: users get a clean, shareable PDF in seconds, with no manual adjustments.<br>
And **respectful**: open source, minimal permissions, no tracking, no ads.

- Website: https://fairscan.org  
- Blog: https://fairscan.org/blog/

---

## Contributing

Contributions are welcome, but please read the guidelines first: [CONTRIBUTING.md](CONTRIBUTING.md)

---

## Translations

Translations are managed on Weblate, and everyone is welcome to contribute.

[![Translation status](https://hosted.weblate.org/widget/fairscan/android-app/language-badge.svg?threshold=95)](https://hosted.weblate.org/engage/fairscan/)

Start translating:
https://hosted.weblate.org/projects/fairscan/

---

## Screenshots

| Scan | Preview | Save & Share |
|------|---------|--------------|
| ![](metadata/en-US/images/phoneScreenshots/1.jpg) | ![](metadata/en-US/images/phoneScreenshots/2.jpg) | ![](metadata/en-US/images/phoneScreenshots/3.jpg) |

---

## Features

- **Clear, distraction-free interface**
- **Easy flow**: scan, review if needed, save or share
- **Automatic document detection** using a custom segmentation model
- **Automatic perspective correction**
- **Automatic image enhancement**
- **Fast PDF generation** with no manual adjustments
- **All document processing happens locally on your device**, no cloud processing
- **Minimal permissions**
- **Open source**, GPLv3

---

## What FairScan is not

FairScan is **not** intended to:
- provide fine-grained manual control over document processing
- replicate all features found in other scanning apps
- optimize for highly specific use cases at the expense of simplicity
 
---

## Compatibility

FairScan works on any device that:
- runs **Android 8.0+**
- has a camera

---

## Experimental: Scan to PDF via intent

FairScan can be invoked by other Android applications to perform a document scan and return a generated PDF.

This feature is **experimental** and intended for developers who want to rely on FairScan as a
simple, privacy-respecting scanning tool.
The intent contract and behavior may change between versions, and backward compatibility
is not guaranteed at this stage.

Intent action: `org.fairscan.app.action.SCAN_TO_PDF`

This is an **implicit intent** that launches FairScan in a dedicated external mode.

When started via this intent:

- FairScan opens directly in scan mode
- the user scans one or more pages
- FairScan generates a single PDF
- the resulting PDF is returned to the calling application as a URI with a limited lifetime
- the calling application should immediately copy the content of the URI as FairScan deletes it later

See an example app: [fairscan-intent-sample](https://github.com/pynicolas/fairscan-intent-sample)

---

## Technical details

FairScan uses:

- [Jetpack Compose](https://developer.android.com/compose) for the UI
- [CameraX](https://developer.android.com/media/camera/camerax) for image capture
- [LiteRT](https://ai.google.dev/edge/litert) to run the custom segmentation model for automatic document detection
- [OpenCV](https://opencv.org/) for perspective correction and image enhancement
- [Tesseract](https://github.com/tesseract-ocr/tesseract) for text recognition (OCR)
- [PDFBox-Android](https://github.com/TomRoush/PdfBox-Android) for PDF generation

---

## The segmentation model

FairScan uses a custom-trained image segmentation model to detect documents:<br>
https://github.com/pynicolas/fairscan-segmentation-model

It's based on a fully public dataset that is available here:<br>
https://github.com/pynicolas/fairscan-dataset

The build system automatically downloads the model using  
[`download-tflite.gradle.kts`](app/download-tflite.gradle.kts).

Related blog posts:
- [*Making document detection more reliable*](https://fairscan.org/blog/automatic-document-detection/)
- [*Building a public dataset for FairScan*](https://fairscan.org/blog/building_a_public_dataset/)

---

## Build

To build an APK:

```bash
./gradlew clean check assembleRelease
```
To build an Android App Bundle:
```bash
./gradlew clean check :standalone:bundleRelease
```

## License
This project is licensed under the GNU GPLv3. See [LICENSE](LICENSE) for details.
