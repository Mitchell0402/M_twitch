# Twitch Android Client M0-M1 Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create the design-handoff workflow and a native Android Compose foundation for a complete third-party Twitch client, with VOD as the first usable path and live/chat/plugin seams present from day one.

**Architecture:** This first plan covers M0-M1 only. It keeps production code in a single Android `:app` module with strict package boundaries for design system, features, playback, chat, plugin contracts, data contracts, and local state; split Gradle modules can be introduced after VOD, live, and chat boundaries prove their shape. UI starts with real Compose screens, fake VOD/chat state, a default local chat plugin, and placeholder live/chat surfaces so the app can launch before Twitch API, EventSub, IRC, and playback-resolution work begins.

**Tech Stack:** Kotlin, Android Gradle Plugin, Gradle Wrapper, Android SDK, Jetpack Compose BOM, Media3, Navigation Compose, Lifecycle Compose, DataStore, Kotlin coroutines, and JUnit. Task 2 begins with an explicit version verification step; use the latest stable compatible set verified from official sources, with the current preferred baseline being Kotlin 2.3.20, AGP 9.1.0, Gradle Wrapper 9.3.1, Android SDK 36, Compose BOM 2026.04.01, Media3 1.10.0, Navigation Compose 2.9.x stable, Lifecycle Compose 2.10.0, DataStore 1.2.1, coroutines 1.10.x stable, and JUnit 4.13.2. Chat, emote, and plugin foundation tasks use app-owned Kotlin contracts only; production network transports are planned after M1.

---

## Scope Boundary

This plan implements only the first client foundation:

- M0 design workflow artifacts.
- M1 Android foundation.
- App shell, theme, navigation, screen scaffolds, local watch-history contract, account-state model, playback-source contract, chat-event contract, emote-provider contract, chat-plugin contract, and default built-in local chat plugin.

This plan does not implement Twitch OAuth, Twitch API calls, real VOD list loading, real HLS URL resolution, real live playback, real chat transport, real emote-provider network calls, VOD chat replay fetching, downloads, external plugin loading, or release signing. Those are not cut from the product; they each get their own plan after this foundation compiles and launches.

## References Checked

- Android Gradle Plugin release notes: https://developer.android.com/build/releases/gradle-plugin
- Compose BOM metadata: https://dl.google.com/dl/android/maven2/androidx/compose/compose-bom/maven-metadata.xml
- Media3 metadata: https://dl.google.com/dl/android/maven2/androidx/media3/media3-exoplayer/maven-metadata.xml
- Twitch Get Videos API: https://dev.twitch.tv/docs/api/reference#get-videos
- Twitch Videos guide: https://dev.twitch.tv/docs/api/videos
- Twitch Chat & Chatbots guide: https://dev.twitch.tv/docs/chat
- Twitch EventSub guide: https://dev.twitch.tv/docs/eventsub
- Twitch EventSub reference for channel chat message events: https://dev.twitch.tv/docs/eventsub/eventsub-reference/
- Twitch Authentication guide: https://dev.twitch.tv/docs/authentication

## Dependency Approval Note

Executing this plan adds the Android production dependencies listed in `gradle/libs.versions.toml`: Compose, Navigation Compose, Lifecycle Compose, Media3, DataStore, Kotlin serialization for type-safe Navigation routes, and coroutines. Because the repository instructions require confirmation before adding production dependencies, pause before Task 2 if the user has not approved this dependency set. This M0-M1 revision does not add a chat networking, WebSocket, IRC, image-loading, database, or plugin-runtime dependency; those require separate approval when their implementation plans are written.

## Version Verification Note

The version catalog in this plan must not be treated as guessed truth. Before writing `gradle/libs.versions.toml`, verify the local toolchain and official release metadata:

- `gradle --version`, if Gradle is installed locally.
- Gradle current release endpoint: https://services.gradle.org/versions/current.
- AGP release notes: https://developer.android.com/build/releases/gradle-plugin.
- Kotlin release notes: https://kotlinlang.org/docs/releases.html.
- Compose BOM release notes or metadata.
- Media3, Navigation, Lifecycle, and DataStore AndroidX release notes.
- Kotlinx serialization release metadata for type-safe Navigation route support.

If AGP 9.2.x is still preview-only when Task 2 runs, use the latest stable AGP 9.1.x-compatible stack instead. As of this review, AGP 9.1.0 + Gradle 9.3.1 is the safer stable baseline, while Gradle 9.4.1 is current but should not be paired with AGP unless the AGP compatibility table says so.

## File Structure

Create or modify these files:

- `DESIGN.md`: human-readable product and UI rules produced from Stitch and Claude Design handoff.
- `docs/design/stitch-client-foundation-prompt.md`: prompt for Google Stitch exploration.
- `docs/design/claude-design-client-translation-prompt.md`: prompt for Claude Design translation.
- `docs/design/artifact-checklist.md`: required design outputs before engineering signoff.
- `.gitignore`: Android and Gradle ignored files.
- `settings.gradle.kts`: Gradle project and repositories.
- `build.gradle.kts`: root plugin declarations.
- `gradle.properties`: Android/Kotlin build settings.
- `gradle/libs.versions.toml`: dependency and plugin version catalog.
- `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`: generated Gradle wrapper files.
- `app/build.gradle.kts`: Android application module.
- `app/src/main/AndroidManifest.xml`: app manifest.
- `app/src/main/java/dev/mitchell/mtwitch/MainActivity.kt`: Android entry point.
- `app/src/main/java/dev/mitchell/mtwitch/MtwitchApp.kt`: root Compose app.
- `app/src/main/java/dev/mitchell/mtwitch/core/model/VodModels.kt`: shared VOD and progress models.
- `app/src/main/java/dev/mitchell/mtwitch/core/local/WatchHistoryRepository.kt`: watch-history boundary.
- `app/src/main/java/dev/mitchell/mtwitch/core/local/InMemoryWatchHistoryRepository.kt`: testable in-memory implementation for foundation.
- `app/src/main/java/dev/mitchell/mtwitch/data/playback/PlaybackSourceModels.kt`: playback-source contract models.
- `app/src/main/java/dev/mitchell/mtwitch/data/playback/FakePlaybackSourceResolver.kt`: fake resolver for UI wiring.
- `app/src/main/java/dev/mitchell/mtwitch/data/chat/ChatModels.kt`: normalized chat events, messages, connection state, and emote references.
- `app/src/main/java/dev/mitchell/mtwitch/plugin/chat/ChatPlugin.kt`: typed local chat plugin API and registry.
- `app/src/main/java/dev/mitchell/mtwitch/plugin/chat/DefaultChatPlugin.kt`: default built-in local chat plugin.
- `app/src/main/java/dev/mitchell/mtwitch/navigation/AppRoute.kt`: route definitions.
- `app/src/main/java/dev/mitchell/mtwitch/ui/theme/MtwitchTheme.kt`: Compose theme.
- `app/src/main/java/dev/mitchell/mtwitch/feature/home/HomeScreen.kt`: Home / Resume scaffold.
- `app/src/main/java/dev/mitchell/mtwitch/feature/channel/ChannelVodListScreen.kt`: Channel VOD list scaffold.
- `app/src/main/java/dev/mitchell/mtwitch/feature/player/PlayerScreen.kt`: VOD player scaffold.
- `app/src/main/java/dev/mitchell/mtwitch/feature/live/LivePlayerScreen.kt`: live player placeholder scaffold.
- `app/src/main/java/dev/mitchell/mtwitch/feature/plugins/ChatPluginSettingsScreen.kt`: chat plugin settings scaffold.
- `app/src/main/java/dev/mitchell/mtwitch/feature/settings/SettingsScreen.kt`: Settings scaffold.
- `app/src/test/java/dev/mitchell/mtwitch/core/model/VodModelsTest.kt`: model tests.
- `app/src/test/java/dev/mitchell/mtwitch/core/local/InMemoryWatchHistoryRepositoryTest.kt`: repository tests.
- `app/src/test/java/dev/mitchell/mtwitch/data/playback/FakePlaybackSourceResolverTest.kt`: playback contract tests.
- `app/src/test/java/dev/mitchell/mtwitch/plugin/chat/ChatPluginRegistryTest.kt`: chat plugin contract tests.
- `app/src/test/java/dev/mitchell/mtwitch/navigation/AppRouteTest.kt`: navigation route tests.

## Task 1: Design Platform Handoff Prompts

**Files:**

- Create: `docs/design/stitch-client-foundation-prompt.md`
- Create: `docs/design/claude-design-client-translation-prompt.md`
- Create: `docs/design/artifact-checklist.md`
- Create: `DESIGN.md`

- [ ] **Step 1: Create the Google Stitch prompt**

Create `docs/design/stitch-client-foundation-prompt.md` with this content:

```markdown
# Google Stitch Prompt: M Twitch Full Client Foundation

Design the first prototype for a native Android Twitch client for personal use. The long-term app is a complete third-party client with live playback, VOD playback, live chat, VOD chat replay, third-party emotes, and local chat plugins. The first version should be VOD-first for the main usable path, but the shell must visibly account for live, chat, replay, plugin settings, and diagnostics.

Primary user goal:
- Find a Twitch channel.
- Browse recent archived broadcasts.
- Open a VOD.
- Watch, pause, scrub, fullscreen, and resume later.
- See where live playback and chat will live in the same app.
- Configure the default local chat plugin even while real chat transport is not implemented.

Design constraints:
- Android phone first.
- Native Jetpack Compose implementation.
- Performance-sensitive video player screen.
- Avoid heavy decorative animation around video playback.
- The first screen should be useful immediately, not a marketing page.
- Chat, VOD chat replay, BTTV, FFZ, 7TV emotes, and chat plugins are later full implementations of the same app, not separate products.
- The default chat plugin is built in locally and enabled by default.

Create 2-3 visual directions and include these screens:
- Home / Resume
- Channel VOD List
- VOD Player portrait
- VOD Player landscape/fullscreen
- Live Player placeholder
- Chat panel / replay panel placeholder
- Chat Plugin Settings
- Settings

For each screen include:
- Loading state
- Empty state
- Recoverable error state
- Offline or auth-needed state where relevant
- Rate-limited or reconnecting state where chat is present

Pay special attention to:
- Player overlay layout
- Scrubber ergonomics
- Recent VOD progress indicators
- Chat panel density and readability
- Emote-heavy message rows without overwhelming playback
- Plugin settings and diagnostics
- One-handed phone use
- Smooth but restrained transitions
- A design-system summary with colors, type scale, spacing, controls, and motion rules

Output a DESIGN.md-style summary after the visual exploration.
```

- [ ] **Step 2: Create the Claude Design translation prompt**

Create `docs/design/claude-design-client-translation-prompt.md` with this content:

```markdown
# Claude Design Prompt: Translate Stitch Client Direction To Compose Handoff

You are translating the selected Google Stitch design direction for a native Android app named M Twitch.

The app is a complete third-party Twitch client for personal use. The first implementation is a full-client foundation with a VOD-first usable path. It includes:
- Home / Resume
- Channel VOD List
- VOD Player
- Live Player placeholder
- Chat panel and VOD replay panel placeholders
- Chat Plugin Settings
- Settings
- Saved watch progress
- Default local chat plugin settings and diagnostics

The MVP includes contracts and UI placeholders for:
- Live playback
- Live chat
- VOD chat replay
- Third-party emotes
- Local chat plugins

The MVP excludes production implementations of:
- Real live playback
- Real Twitch chat transport
- VOD chat replay fetching
- Third-party emote network integrations
- Downloads
- External plugin loading
- Public store release polish

Use the provided Stitch output as the visual source of truth. Produce an implementation handoff for Kotlin + Jetpack Compose.

The handoff must include:
- Design tokens: colors, typography, spacing, elevation, shape radius
- Component list: app shell, top bars, list rows, VOD cards, player controls, scrubber, buttons, settings rows, error states
- Screen-by-screen layout rules
- State table for loading, empty, auth-needed, offline, playback unavailable, and retrying
- State table for chat disconnected, chat reconnecting, chat rate-limited, plugin failed, and emote provider unavailable
- Motion rules with durations and easing
- Player overlay behavior in portrait and landscape
- Chat panel behavior over or beside video on phone screens
- Plugin settings and diagnostics behavior
- Accessibility notes for touch targets, contrast, and content descriptions
- A concise DESIGN.md that engineering can commit to the repository

Avoid web-specific implementation details. Do not generate a WebView app. Optimize the handoff for native Compose.
```

- [ ] **Step 3: Create the artifact checklist**

Create `docs/design/artifact-checklist.md` with this content:

```markdown
# Design Artifact Checklist

Final UI polish starts only after these artifacts exist. M1 engineering can run
from the baseline `DESIGN.md` while Stitch and Claude Design work continues in
parallel.

- `DESIGN.md` with selected visual direction and design tokens.
- Stitch export or screenshots for Home / Resume.
- Stitch export or screenshots for Channel VOD List.
- Stitch export or screenshots for VOD Player portrait.
- Stitch export or screenshots for VOD Player landscape/fullscreen.
- Stitch export or screenshots for Live Player placeholder.
- Stitch export or screenshots for chat panel and VOD replay panel placeholder.
- Stitch export or screenshots for Chat Plugin Settings.
- Stitch export or screenshots for Settings.
- Claude Design handoff notes for Compose components.
- Claude Design state table for loading, empty, error, offline, and auth-needed states.
- Claude Design state table for chat disconnected, chat reconnecting, rate-limited, plugin failed, and emote provider unavailable states.
- Claude Design motion rules for navigation and player overlays.

The first implementation can use the baseline `DESIGN.md` in this repository while external artifacts are being prepared. Replace it with the Claude Design handoff before final UI polish.
```

- [ ] **Step 4: Create the baseline DESIGN.md**

Create `DESIGN.md` with this content:

```markdown
# M Twitch Design System

## Product Direction

M Twitch is a complete Android Twitch client being built through a VOD-first prototype. The prototype interface should feel calm, fast, and playback-centered. It should prioritize resuming content, scanning VOD lists, and controlling playback without visual noise while leaving room for later live chat, chat replay, and emote surfaces.

The first foundation also includes visible homes for live playback, chat, replay, plugin settings, and diagnostics so future work extends the app instead of redesigning it.

## Visual Style

- Dark-first interface tuned for video viewing.
- High-contrast text on dark surfaces.
- One accent color for active playback and progress.
- No decorative gradients, floating blobs, or oversized marketing sections.
- Rounded corners stay at 8dp or below unless the final Claude Design handoff changes that rule.

## Layout

- Phone-first portrait layout.
- Bottom navigation is avoided in MVP-A; Home and Settings provide explicit
  entry points until the primary live/VOD/chat destinations are proven.
- Home links to channel lookup, recent VODs, settings, and player resume.
- Player controls appear as an overlay on top of the video surface.
- Landscape player prioritizes video and hides nonessential UI.
- Chat surfaces collapse behind a clear affordance on phone-sized screens.
- Plugin settings are grouped under Settings until chat becomes a primary tab.

## Motion

- Navigation transitions should be quick and quiet.
- Player controls fade in and out.
- Scrubber feedback should be immediate.
- Loading states use simple progress indicators.

## Accessibility

- Touch targets should be at least 48dp.
- Icon-only controls need content descriptions.
- Text contrast should remain readable on dark video-adjacent surfaces.
- Scrubber elapsed and remaining time should be visible when controls are shown.
```

- [ ] **Step 5: Verify design files exist**

Run:

```powershell
Test-Path DESIGN.md
Test-Path docs/design/stitch-client-foundation-prompt.md
Test-Path docs/design/claude-design-client-translation-prompt.md
Test-Path docs/design/artifact-checklist.md
```

Expected:

```text
True
True
True
True
```

- [ ] **Step 6: Commit design handoff prompts**

Run:

```powershell
git add DESIGN.md docs/design
git commit -m "docs: add design handoff prompts"
```

Expected: commit succeeds.

## Task 2: Android Gradle Foundation

**Files:**

- Modify: `.gitignore`
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml`
- Generate: `gradlew`
- Generate: `gradlew.bat`
- Generate: `gradle/wrapper/gradle-wrapper.jar`
- Generate: `gradle/wrapper/gradle-wrapper.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/dev/mitchell/mtwitch/MainActivity.kt`

- [ ] **Step 1: Verify Android build versions**

Run:

```powershell
gradle --version
```

Expected if Gradle is installed: prints a Gradle version. If `gradle` is not installed, continue with the wrapper version selected from official metadata.

Open and verify these official sources:

```text
https://services.gradle.org/versions/current
https://developer.android.com/build/releases/gradle-plugin
https://kotlinlang.org/docs/releases.html
https://developer.android.com/jetpack/androidx/releases/media3
https://developer.android.com/jetpack/androidx/releases/navigation
https://developer.android.com/jetpack/androidx/releases/datastore
https://github.com/Kotlin/kotlinx.serialization/releases
```

Expected: choose the latest stable compatible set before writing `gradle/libs.versions.toml`. Prefer AGP 9.1.0 + Gradle 9.3.1 unless AGP 9.2.x is stable and explicitly lists a compatible Gradle version.

- [ ] **Step 2: Extend `.gitignore`**

Replace `.gitignore` with:

```gitignore
.superpowers/
.gradle/
build/
captures/
.externalNativeBuild/
.cxx/
local.properties
*.iml
.idea/
app/build/
```

- [ ] **Step 3: Create root Gradle settings**

Create `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "M Twitch"
include(":app")
```

- [ ] **Step 4: Create root build file**

Create `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
```

- [ ] **Step 5: Create Gradle properties**

Create `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
org.gradle.configuration-cache=true
org.gradle.parallel=true
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
```

- [ ] **Step 6: Create version catalog**

Create `gradle/libs.versions.toml`:

```toml
[versions]
agp = "9.1.0"
kotlin = "2.3.20"
composeBom = "2026.04.01"
activityCompose = "1.13.0"
lifecycle = "2.10.0"
navigation = "2.9.7"
media3 = "1.10.0"
datastore = "1.2.1"
coroutines = "1.10.2"
serialization = "1.11.0"
junit = "4.13.2"

[libraries]
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
androidx-compose-foundation = { module = "androidx.compose.foundation:foundation" }
androidx-compose-material3 = { module = "androidx.compose.material3:material3" }
androidx-compose-ui = { module = "androidx.compose.ui:ui" }
androidx-compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
androidx-compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
androidx-lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
androidx-media3-exoplayer-hls = { module = "androidx.media3:media3-exoplayer-hls", version.ref = "media3" }
androidx-media3-ui-compose = { module = "androidx.media3:media3-ui-compose", version.ref = "media3" }
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
kotlinx-serialization-core = { module = "org.jetbrains.kotlinx:kotlinx-serialization-core", version.ref = "serialization" }
junit = { module = "junit:junit", version.ref = "junit" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

- [ ] **Step 7: Create app build file**

Create `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.mitchell.mtwitch"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.mitchell.mtwitch"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.core)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

- [ ] **Step 8: Create manifest**

Create `app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="M Twitch"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MTwitch">
        <activity
            android:name=".MainActivity"
            android:configChanges="keyboardHidden|orientation|screenSize"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 9: Create Android theme resource**

Create `app/src/main/res/values/styles.xml`:

```xml
<resources>
    <style name="Theme.MTwitch" parent="android:style/Theme.Material.NoActionBar" />
</resources>
```

- [ ] **Step 10: Create launcher foreground resources**

Create `app/src/main/res/drawable/ic_launcher_foreground.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#7C5CFF"
        android:pathData="M22,22h64v64h-64z" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M38,34h10v40h-10zM58,34h12l-10,20l10,20h-12l-10,-20z" />
</vector>
```

Create `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`:

```xml
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

Create `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`:

```xml
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

Create `app/src/main/res/values/colors.xml`:

```xml
<resources>
    <color name="ic_launcher_background">#101014</color>
</resources>
```

- [ ] **Step 11: Create temporary MainActivity**

Create `app/src/main/java/dev/mitchell/mtwitch/MainActivity.kt`:

```kotlin
package dev.mitchell.mtwitch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface {
                    Text(text = "M Twitch")
                }
            }
        }
    }
}
```

- [ ] **Step 12: Generate Gradle wrapper**

If `gradle` is not installed, run these commands from the repository root:

```powershell
$gradleZip = Join-Path $env:TEMP "gradle-9.3.1-bin.zip"
$gradleHome = Join-Path $env:TEMP "gradle-9.3.1"
Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-9.3.1-bin.zip" -OutFile $gradleZip
Expand-Archive -LiteralPath $gradleZip -DestinationPath $env:TEMP -Force
& "$gradleHome\bin\gradle.bat" wrapper --gradle-version 9.3.1 --distribution-type bin
```

Expected: wrapper files are generated in the repository. If the version verification step selected a different stable AGP-compatible Gradle version, use that verified version in the download URL, `$gradleHome`, and wrapper command.

- [ ] **Step 13: Verify Gradle wrapper version**

Run:

```powershell
.\gradlew.bat --version
```

Expected: output includes the verified stable Gradle version, initially `Gradle 9.3.1`.

- [ ] **Step 14: Run initial Android test task**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: build succeeds with no unit tests found or all tests passing.

- [ ] **Step 15: Commit Android foundation scaffold**

Run:

```powershell
git add .gitignore settings.gradle.kts build.gradle.kts gradle.properties gradle app
git commit -m "chore: scaffold Android Compose project"
```

Expected: commit succeeds.

## Task 3: Core VOD Models

**Files:**

- Create: `app/src/test/java/dev/mitchell/mtwitch/core/model/VodModelsTest.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/core/model/VodModels.kt`

- [ ] **Step 1: Write failing model tests**

Create `app/src/test/java/dev/mitchell/mtwitch/core/model/VodModelsTest.kt`:

```kotlin
package dev.mitchell.mtwitch.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class VodModelsTest {
    @Test
    fun watchProgressFractionClampsIntoValidRange() {
        val progress = WatchProgress(
            videoId = VideoId("123"),
            positionMs = 75_000L,
            durationMs = 150.minutes.inWholeMilliseconds,
            updatedAtEpochMs = 1_000L,
        )

        assertEquals(0.5f, progress.fraction, 0.0001f)
        assertEquals(1f, progress.copy(positionMs = 10_000_000L).fraction, 0.0001f)
        assertEquals(0f, progress.copy(positionMs = -100L).fraction, 0.0001f)
        assertEquals(0f, progress.copy(durationMs = null).fraction, 0.0001f)
    }

    @Test
    fun vodKnowsWhetherItHasProgress() {
        val vod = Vod(
            id = VideoId("456"),
            channelId = ChannelId("channel-1"),
            channelLogin = "example",
            title = "Long broadcast",
            thumbnailUrl = "https://example.com/thumb.jpg",
            duration = 90.minutes,
            publishedAtEpochMs = 2_000L,
            progress = WatchProgress(
                videoId = VideoId("456"),
                positionMs = 30_000L,
                durationMs = 90.minutes.inWholeMilliseconds,
                updatedAtEpochMs = 3_000L,
            ),
        )

        assertTrue(vod.hasProgress)
        assertFalse(vod.copy(progress = null).hasProgress)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "dev.mitchell.mtwitch.core.model.VodModelsTest"
```

Expected: FAIL with unresolved references to `WatchProgress`, `VideoId`, `Vod`, and `ChannelId`.

- [ ] **Step 3: Add core models**

Create `app/src/main/java/dev/mitchell/mtwitch/core/model/VodModels.kt`:

```kotlin
package dev.mitchell.mtwitch.core.model

import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration

@JvmInline
value class VideoId(val value: String)

@JvmInline
value class ChannelId(val value: String)

data class WatchProgress(
    val videoId: VideoId,
    val positionMs: Long,
    val durationMs: Long?,
    val updatedAtEpochMs: Long,
) {
    val fraction: Float
        get() {
            val knownDurationMs = durationMs ?: return 0f
            if (knownDurationMs <= 0L) return 0f
            val clampedPosition = min(max(positionMs, 0L), knownDurationMs)
            return clampedPosition.toFloat() / knownDurationMs.toFloat()
        }
}

data class Vod(
    val id: VideoId,
    val channelId: ChannelId,
    val channelLogin: String,
    val title: String,
    val thumbnailUrl: String,
    val duration: Duration,
    val publishedAtEpochMs: Long,
    val progress: WatchProgress?,
) {
    val hasProgress: Boolean = progress != null && progress.positionMs > 0L
}
```

- [ ] **Step 4: Run model tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "dev.mitchell.mtwitch.core.model.VodModelsTest"
```

Expected: PASS.

- [ ] **Step 5: Commit core models**

Run:

```powershell
git add app/src/main/java/dev/mitchell/mtwitch/core/model app/src/test/java/dev/mitchell/mtwitch/core/model
git commit -m "feat: add core VOD models"
```

Expected: commit succeeds.

## Task 4: Watch History Boundary

**Files:**

- Create: `app/src/test/java/dev/mitchell/mtwitch/core/local/InMemoryWatchHistoryRepositoryTest.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/core/local/WatchHistoryRepository.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/core/local/InMemoryWatchHistoryRepository.kt`

- [ ] **Step 1: Write failing repository tests**

Create `app/src/test/java/dev/mitchell/mtwitch/core/local/InMemoryWatchHistoryRepositoryTest.kt`:

```kotlin
package dev.mitchell.mtwitch.core.local

import dev.mitchell.mtwitch.core.model.VideoId
import dev.mitchell.mtwitch.core.model.WatchProgress
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemoryWatchHistoryRepositoryTest {
    @Test
    fun saveProgressStoresLatestProgressForVideo() = runTest {
        val repository = InMemoryWatchHistoryRepository()
        val first = WatchProgress(VideoId("1"), positionMs = 5_000L, durationMs = 60_000L, updatedAtEpochMs = 10L)
        val second = WatchProgress(VideoId("1"), positionMs = 8_000L, durationMs = 60_000L, updatedAtEpochMs = 20L)

        repository.saveProgress(first)
        repository.saveProgress(second)

        assertEquals(second, repository.progressFor(VideoId("1")))
        assertEquals(listOf(second), repository.recent.value)
    }

    @Test
    fun clearRemovesAllProgress() = runTest {
        val repository = InMemoryWatchHistoryRepository()
        repository.saveProgress(WatchProgress(VideoId("1"), positionMs = 5_000L, durationMs = 60_000L, updatedAtEpochMs = 10L))

        repository.clear()

        assertNull(repository.progressFor(VideoId("1")))
        assertEquals(emptyList<WatchProgress>(), repository.recent.value)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "dev.mitchell.mtwitch.core.local.InMemoryWatchHistoryRepositoryTest"
```

Expected: FAIL with unresolved reference `InMemoryWatchHistoryRepository`.

- [ ] **Step 3: Add repository interface**

Create `app/src/main/java/dev/mitchell/mtwitch/core/local/WatchHistoryRepository.kt`:

```kotlin
package dev.mitchell.mtwitch.core.local

import dev.mitchell.mtwitch.core.model.VideoId
import dev.mitchell.mtwitch.core.model.WatchProgress
import kotlinx.coroutines.flow.StateFlow

interface WatchHistoryRepository {
    val recent: StateFlow<List<WatchProgress>>

    suspend fun saveProgress(progress: WatchProgress)

    suspend fun progressFor(videoId: VideoId): WatchProgress?

    suspend fun clear()
}
```

- [ ] **Step 4: Add in-memory implementation**

Create `app/src/main/java/dev/mitchell/mtwitch/core/local/InMemoryWatchHistoryRepository.kt`:

```kotlin
package dev.mitchell.mtwitch.core.local

import dev.mitchell.mtwitch.core.model.VideoId
import dev.mitchell.mtwitch.core.model.WatchProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class InMemoryWatchHistoryRepository : WatchHistoryRepository {
    private val progressByVideoId = linkedMapOf<VideoId, WatchProgress>()
    private val _recent = MutableStateFlow<List<WatchProgress>>(emptyList())

    override val recent: StateFlow<List<WatchProgress>> = _recent

    override suspend fun saveProgress(progress: WatchProgress) {
        progressByVideoId[progress.videoId] = progress
        _recent.update {
            progressByVideoId.values
                .sortedByDescending { item -> item.updatedAtEpochMs }
        }
    }

    override suspend fun progressFor(videoId: VideoId): WatchProgress? {
        return progressByVideoId[videoId]
    }

    override suspend fun clear() {
        progressByVideoId.clear()
        _recent.value = emptyList()
    }
}
```

This in-memory implementation is intentionally simple for M1. Do not copy the
`sortedByDescending` write-path pattern into DataStore or Room; persisted
history should maintain query-friendly ordering instead of sorting all rows on
every save.

- [ ] **Step 5: Run repository tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "dev.mitchell.mtwitch.core.local.InMemoryWatchHistoryRepositoryTest"
```

Expected: PASS.

- [ ] **Step 6: Commit watch-history boundary**

Run:

```powershell
git add app/src/main/java/dev/mitchell/mtwitch/core/local app/src/test/java/dev/mitchell/mtwitch/core/local
git commit -m "feat: add watch history boundary"
```

Expected: commit succeeds.

## Task 5: Playback Source Contract

**Files:**

- Create: `app/src/test/java/dev/mitchell/mtwitch/data/playback/FakePlaybackSourceResolverTest.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/data/playback/PlaybackSourceModels.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/data/playback/FakePlaybackSourceResolver.kt`

- [ ] **Step 1: Write failing playback resolver tests**

Create `app/src/test/java/dev/mitchell/mtwitch/data/playback/FakePlaybackSourceResolverTest.kt`:

```kotlin
package dev.mitchell.mtwitch.data.playback

import dev.mitchell.mtwitch.core.model.ChannelId
import dev.mitchell.mtwitch.core.model.VideoId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakePlaybackSourceResolverTest {
    @Test
    fun resolverReturnsConfiguredSource() = runTest {
        val resolver = FakePlaybackSourceResolver(
            result = PlaybackSourceResult.Ready(
                source = PlaybackSource(
                    request = PlaybackRequest.Vod(VideoId("123")),
                    hlsUrl = "https://example.com/vod.m3u8",
                    isLive = false,
                    timelineIssues = listOf(
                        PlaybackTimelineIssue(
                            startMs = 30_000L,
                            endMs = 45_000L,
                            reason = PlaybackTimelineIssueReason.MutedSegment,
                        ),
                    ),
                ),
            ),
        )

        val result = resolver.resolve(PlaybackRequest.Vod(VideoId("123")))

        assertEquals(
            PlaybackSourceResult.Ready(
                PlaybackSource(
                    request = PlaybackRequest.Vod(VideoId("123")),
                    hlsUrl = "https://example.com/vod.m3u8",
                    isLive = false,
                    timelineIssues = listOf(
                        PlaybackTimelineIssue(
                            startMs = 30_000L,
                            endMs = 45_000L,
                            reason = PlaybackTimelineIssueReason.MutedSegment,
                        ),
                    ),
                ),
            ),
            result,
        )
    }

    @Test
    fun resolverCanReturnUnavailable() = runTest {
        val resolver = FakePlaybackSourceResolver(
            result = PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.SourceMissing),
        )

        val result = resolver.resolve(PlaybackRequest.Vod(VideoId("missing")))

        assertTrue(result is PlaybackSourceResult.Unavailable)
    }

    @Test
    fun resolverAcceptsLivePlaybackRequests() = runTest {
        val request = PlaybackRequest.Live(
            channelId = ChannelId("channel-1"),
            channelLogin = "cohhcarnage",
        )
        val resolver = FakePlaybackSourceResolver(
            result = PlaybackSourceResult.Ready(
                source = PlaybackSource(
                    request = request,
                    hlsUrl = "https://example.com/live.m3u8",
                    isLive = true,
                    timelineIssues = emptyList(),
                ),
            ),
        )

        val result = resolver.resolve(request)

        assertEquals(
            PlaybackSourceResult.Ready(
                PlaybackSource(
                    request = request,
                    hlsUrl = "https://example.com/live.m3u8",
                    isLive = true,
                    timelineIssues = emptyList(),
                ),
            ),
            result,
        )
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "dev.mitchell.mtwitch.data.playback.FakePlaybackSourceResolverTest"
```

Expected: FAIL with unresolved reference `FakePlaybackSourceResolver`.

- [ ] **Step 3: Add playback source models**

Create `app/src/main/java/dev/mitchell/mtwitch/data/playback/PlaybackSourceModels.kt`:

```kotlin
package dev.mitchell.mtwitch.data.playback

import dev.mitchell.mtwitch.core.model.ChannelId
import dev.mitchell.mtwitch.core.model.VideoId

sealed interface PlaybackRequest {
    data class Vod(val videoId: VideoId) : PlaybackRequest
    data class Live(
        val channelId: ChannelId,
        val channelLogin: String,
    ) : PlaybackRequest
}

data class PlaybackSource(
    val request: PlaybackRequest,
    val hlsUrl: String,
    val isLive: Boolean,
    val timelineIssues: List<PlaybackTimelineIssue>,
)

data class PlaybackTimelineIssue(
    val startMs: Long,
    val endMs: Long,
    val reason: PlaybackTimelineIssueReason,
)

enum class PlaybackTimelineIssueReason {
    MutedSegment,
    Discontinuity,
    Unknown,
)

sealed interface PlaybackSourceResult {
    data class Ready(val source: PlaybackSource) : PlaybackSourceResult
    data class Unavailable(val reason: PlaybackUnavailableReason) : PlaybackSourceResult
}

enum class PlaybackUnavailableReason {
    SourceMissing,
    TokenRejected,
    NetworkTimeout,
    StreamOffline,
    AuthRequired,
    RateLimited,
}

interface PlaybackSourceResolver {
    suspend fun resolve(request: PlaybackRequest): PlaybackSourceResult
}
```

- [ ] **Step 4: Add fake resolver**

Create `app/src/main/java/dev/mitchell/mtwitch/data/playback/FakePlaybackSourceResolver.kt`:

```kotlin
package dev.mitchell.mtwitch.data.playback

class FakePlaybackSourceResolver(
    private val result: PlaybackSourceResult,
) : PlaybackSourceResolver {
    override suspend fun resolve(request: PlaybackRequest): PlaybackSourceResult {
        return when (result) {
            is PlaybackSourceResult.Ready -> result.copy(
                source = result.source.copy(request = request),
            )
            is PlaybackSourceResult.Unavailable -> result
        }
    }
}
```

- [ ] **Step 5: Run playback resolver tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "dev.mitchell.mtwitch.data.playback.FakePlaybackSourceResolverTest"
```

Expected: PASS.

- [ ] **Step 6: Commit playback-source contract**

Run:

```powershell
git add app/src/main/java/dev/mitchell/mtwitch/data/playback app/src/test/java/dev/mitchell/mtwitch/data/playback
git commit -m "feat: add playback source contract"
```

Expected: commit succeeds.

## Task 6: Chat Plugin Foundation Contract

**Files:**

- Create: `app/src/test/java/dev/mitchell/mtwitch/plugin/chat/ChatPluginRegistryTest.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/data/chat/ChatModels.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/plugin/chat/ChatPlugin.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/plugin/chat/DefaultChatPlugin.kt`

- [ ] **Step 1: Write failing chat plugin tests**

Create `app/src/test/java/dev/mitchell/mtwitch/plugin/chat/ChatPluginRegistryTest.kt`:

```kotlin
package dev.mitchell.mtwitch.plugin.chat

import dev.mitchell.mtwitch.core.model.ChannelId
import dev.mitchell.mtwitch.data.chat.ChatEvent
import dev.mitchell.mtwitch.data.chat.ChatMessage
import dev.mitchell.mtwitch.data.chat.ChatMessageFragment
import dev.mitchell.mtwitch.data.chat.ChatMessageId
import dev.mitchell.mtwitch.data.chat.ChatUserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatPluginRegistryTest {
    private val sampleEvent = ChatEvent.MessageReceived(
        channelId = ChannelId("channel-1"),
        message = ChatMessage(
            id = ChatMessageId("message-1"),
            userId = ChatUserId("user-1"),
            userLogin = "viewer",
            userDisplayName = "Viewer",
            fragments = listOf(ChatMessageFragment.Text("hello")),
            timestampEpochMs = 1_000L,
        ),
    )

    @Test
    fun registryDispatchesToEnabledDefaultPlugin() {
        val registry = ChatPluginRegistry(
            plugins = listOf(DefaultChatPlugin),
            enabledPluginIds = setOf(DefaultChatPlugin.id),
        )

        val actions = registry.dispatch(sampleEvent)

        assertEquals(
            listOf(
                ChatPluginAction.AddLocalNotice(
                    pluginId = "default-chat",
                    message = "Default chat plugin observed: hello",
                ),
            ),
            actions,
        )
    }

    @Test
    fun registrySkipsDisabledPlugins() {
        val registry = ChatPluginRegistry(
            plugins = listOf(DefaultChatPlugin),
            enabledPluginIds = emptySet(),
        )

        assertTrue(registry.dispatch(sampleEvent).isEmpty())
    }

    @Test
    fun registryTurnsPluginFailuresIntoDiagnostics() {
        val brokenPlugin = object : ChatPlugin {
            override val id: String = "broken"
            override val displayName: String = "Broken"
            override val enabledByDefault: Boolean = true

            override fun handle(event: ChatEvent): List<ChatPluginAction> {
                error("boom")
            }
        }
        val registry = ChatPluginRegistry(
            plugins = listOf(brokenPlugin),
            enabledPluginIds = setOf("broken"),
        )

        val actions = registry.dispatch(sampleEvent)

        assertEquals(
            listOf(ChatPluginAction.PluginFailed(pluginId = "broken", reason = "boom")),
            actions,
        )
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "dev.mitchell.mtwitch.plugin.chat.ChatPluginRegistryTest"
```

Expected: FAIL with unresolved references to `ChatPluginRegistry`, `DefaultChatPlugin`, `ChatEvent`, and `ChatMessage`.

- [ ] **Step 3: Add normalized chat models**

Create `app/src/main/java/dev/mitchell/mtwitch/data/chat/ChatModels.kt`:

```kotlin
package dev.mitchell.mtwitch.data.chat

import dev.mitchell.mtwitch.core.model.ChannelId

@JvmInline
value class ChatMessageId(val value: String)

@JvmInline
value class ChatUserId(val value: String)

@JvmInline
value class EmoteId(val value: String)

enum class ChatConnectionState {
    Disconnected,
    Connecting,
    Connected,
    Reconnecting,
    RateLimited,
}

sealed interface ChatMessageFragment {
    val text: String

    data class Text(override val text: String) : ChatMessageFragment

    data class Emote(
        override val text: String,
        val emoteId: EmoteId,
        val imageUrl: String?,
    ) : ChatMessageFragment
}

data class ChatMessage(
    val id: ChatMessageId,
    val userId: ChatUserId,
    val userLogin: String,
    val userDisplayName: String,
    val fragments: List<ChatMessageFragment>,
    val timestampEpochMs: Long,
) {
    val plainText: String = fragments.joinToString(separator = "") { it.text }
}

sealed interface ChatEvent {
    data class MessageReceived(
        val channelId: ChannelId,
        val message: ChatMessage,
    ) : ChatEvent

    data class MessageDeleted(
        val channelId: ChannelId,
        val messageId: ChatMessageId,
    ) : ChatEvent

    data class ConnectionStateChanged(
        val channelId: ChannelId,
        val state: ChatConnectionState,
    ) : ChatEvent
}

data class EmoteReference(
    val providerId: String,
    val emoteId: EmoteId,
    val code: String,
    val imageUrl: String,
)

interface EmoteProvider {
    val id: String

    suspend fun resolve(
        code: String,
        channelId: ChannelId,
    ): EmoteReference?
}
```

- [ ] **Step 4: Add chat plugin API and registry**

Create `app/src/main/java/dev/mitchell/mtwitch/plugin/chat/ChatPlugin.kt`:

```kotlin
package dev.mitchell.mtwitch.plugin.chat

import dev.mitchell.mtwitch.data.chat.ChatEvent

interface ChatPlugin {
    val id: String
    val displayName: String
    val enabledByDefault: Boolean

    fun handle(event: ChatEvent): List<ChatPluginAction>
}

sealed interface ChatPluginAction {
    val pluginId: String

    data class AddLocalNotice(
        override val pluginId: String,
        val message: String,
    ) : ChatPluginAction

    data class PluginFailed(
        override val pluginId: String,
        val reason: String,
    ) : ChatPluginAction
}

class ChatPluginRegistry(
    private val plugins: List<ChatPlugin>,
    private val enabledPluginIds: Set<String> = plugins
        .filter { it.enabledByDefault }
        .map { it.id }
        .toSet(),
) {
    fun dispatch(event: ChatEvent): List<ChatPluginAction> {
        return plugins
            .filter { plugin -> plugin.id in enabledPluginIds }
            .flatMap { plugin ->
                try {
                    plugin.handle(event)
                } catch (error: Throwable) {
                    listOf(
                        ChatPluginAction.PluginFailed(
                            pluginId = plugin.id,
                            reason = error.message ?: error.javaClass.simpleName,
                        ),
                    )
                }
            }
    }
}
```

- [ ] **Step 5: Add default local chat plugin**

Create `app/src/main/java/dev/mitchell/mtwitch/plugin/chat/DefaultChatPlugin.kt`:

```kotlin
package dev.mitchell.mtwitch.plugin.chat

import dev.mitchell.mtwitch.data.chat.ChatEvent

data object DefaultChatPlugin : ChatPlugin {
    override val id: String = "default-chat"
    override val displayName: String = "Default Chat"
    override val enabledByDefault: Boolean = true

    override fun handle(event: ChatEvent): List<ChatPluginAction> {
        return when (event) {
            is ChatEvent.MessageReceived -> listOf(
                ChatPluginAction.AddLocalNotice(
                    pluginId = id,
                    message = "Default chat plugin observed: ${event.message.plainText}",
                ),
            )
            is ChatEvent.MessageDeleted,
            is ChatEvent.ConnectionStateChanged -> emptyList()
        }
    }
}
```

- [ ] **Step 6: Run chat plugin tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "dev.mitchell.mtwitch.plugin.chat.ChatPluginRegistryTest"
```

Expected: PASS.

- [ ] **Step 7: Commit chat plugin foundation**

Run:

```powershell
git add app/src/main/java/dev/mitchell/mtwitch/data/chat app/src/main/java/dev/mitchell/mtwitch/plugin/chat app/src/test/java/dev/mitchell/mtwitch/plugin/chat
git commit -m "feat: add chat plugin foundation"
```

Expected: commit succeeds.

## Task 7: Navigation And Compose Screen Shell

**Files:**

- Create: `app/src/test/java/dev/mitchell/mtwitch/navigation/AppRouteTest.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/navigation/AppRoute.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/ui/theme/MtwitchTheme.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/feature/home/HomeScreen.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/feature/channel/ChannelVodListScreen.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/feature/player/PlayerScreen.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/feature/live/LivePlayerScreen.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/feature/plugins/ChatPluginSettingsScreen.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/feature/settings/SettingsScreen.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/MtwitchApp.kt`
- Modify: `app/src/main/java/dev/mitchell/mtwitch/MainActivity.kt`

- [ ] **Step 1: Write failing route tests**

Create `app/src/test/java/dev/mitchell/mtwitch/navigation/AppRouteTest.kt`:

```kotlin
package dev.mitchell.mtwitch.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppRouteTest {
    @Test
    fun routesUseTypedValuesInsteadOfStringBuilders() {
        assertEquals(AppRoute.Home, AppRoute.Home)
        assertEquals(AppRoute.Settings, AppRoute.Settings)
        assertEquals(AppRoute.ChatPlugins, AppRoute.ChatPlugins)
    }

    @Test
    fun dynamicRoutesCarryTypedArguments() {
        assertEquals("cohhcarnage", AppRoute.Channel(channelLogin = "cohhcarnage").channelLogin)
        assertEquals("123456789", AppRoute.Player(videoId = "123456789").videoId)
        assertEquals("cohhcarnage", AppRoute.Live(channelLogin = "cohhcarnage").channelLogin)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "dev.mitchell.mtwitch.navigation.AppRouteTest"
```

Expected: FAIL with unresolved reference `AppRoute`.

- [ ] **Step 3: Add navigation routes**

Create `app/src/main/java/dev/mitchell/mtwitch/navigation/AppRoute.kt`:

```kotlin
package dev.mitchell.mtwitch.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable
    data object Home : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object ChatPlugins : AppRoute

    @Serializable
    data class Channel(val channelLogin: String) : AppRoute

    @Serializable
    data class Player(val videoId: String) : AppRoute

    @Serializable
    data class Live(val channelLogin: String) : AppRoute
}
```

- [ ] **Step 4: Run route tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "dev.mitchell.mtwitch.navigation.AppRouteTest"
```

Expected: PASS.

- [ ] **Step 5: Add Compose theme**

Create `app/src/main/java/dev/mitchell/mtwitch/ui/theme/MtwitchTheme.kt`:

```kotlin
package dev.mitchell.mtwitch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF8F7CFF),
    secondary = Color(0xFF4BD4B8),
    background = Color(0xFF101014),
    surface = Color(0xFF181820),
    surfaceVariant = Color(0xFF242432),
    onPrimary = Color.White,
    onSecondary = Color(0xFF07130F),
    onBackground = Color(0xFFF5F3FF),
    onSurface = Color(0xFFF5F3FF),
    onSurfaceVariant = Color(0xFFC9C5D8),
)

private val LightScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF5E4AE3),
    secondary = Color(0xFF007C69),
    background = Color(0xFFFAF9FF),
    surface = Color.White,
    surfaceVariant = Color(0xFFE7E3F5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF17151F),
    onSurface = Color(0xFF17151F),
    onSurfaceVariant = Color(0xFF4C485C),
)

@Composable
fun MtwitchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content,
    )
}
```

- [ ] **Step 6: Add Home screen**

Create `app/src/main/java/dev/mitchell/mtwitch/feature/home/HomeScreen.kt`:

```kotlin
package dev.mitchell.mtwitch.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onOpenChannel: (String) -> Unit,
    onOpenLive: (String) -> Unit,
    onOpenChatPlugins: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var channelLogin by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "M Twitch",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Resume or open a channel VOD list",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = onOpenSettings) {
                Text(text = "Settings")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = channelLogin,
            onValueChange = { channelLogin = it.trim() },
            label = { Text(text = "Channel login") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = { onOpenChannel(channelLogin) },
            enabled = channelLogin.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Open VODs")
        }

        Button(
            onClick = { onOpenLive(channelLogin) },
            enabled = channelLogin.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Open Live Placeholder")
        }

        Button(
            onClick = onOpenChatPlugins,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Chat Plugins")
        }

        Text(
            text = "Recent VODs, live channels, and chat diagnostics will appear here as the client grows.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
```

- [ ] **Step 7: Add Channel VOD list screen**

Create `app/src/main/java/dev/mitchell/mtwitch/feature/channel/ChannelVodListScreen.kt`:

```kotlin
package dev.mitchell.mtwitch.feature.channel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mitchell.mtwitch.core.model.ChannelId
import dev.mitchell.mtwitch.core.model.VideoId
import dev.mitchell.mtwitch.core.model.Vod
import kotlin.time.Duration.Companion.hours

@Composable
fun ChannelVodListScreen(
    channelLogin: String,
    onOpenVod: (VideoId) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sampleVods = listOf(
        Vod(
            id = VideoId("sample-1"),
            channelId = ChannelId("sample-channel"),
            channelLogin = channelLogin,
            title = "Sample archived broadcast",
            thumbnailUrl = "",
            duration = 3.hours,
            publishedAtEpochMs = 0L,
            progress = null,
        ),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(onClick = onBack) {
            Text(text = "Back")
        }
        Text(
            text = "$channelLogin VODs",
            style = MaterialTheme.typography.headlineSmall,
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sampleVods) { vod ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenVod(vod.id) },
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = vod.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Duration: ${vod.duration.inWholeHours}h",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 8: Add Player screen**

Create `app/src/main/java/dev/mitchell/mtwitch/feature/player/PlayerScreen.kt`:

```kotlin
package dev.mitchell.mtwitch.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PlayerScreen(
    videoId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Video surface: $videoId",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LinearProgressIndicator(
                progress = { 0.2f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = onBack) {
                    Text(text = "Back")
                }
                Text(text = "00:12 / 01:00")
                Button(onClick = {}) {
                    Text(text = "Pause")
                }
            }
        }
    }
}
```

- [ ] **Step 9: Add Live player placeholder screen**

Create `app/src/main/java/dev/mitchell/mtwitch/feature/live/LivePlayerScreen.kt`:

```kotlin
package dev.mitchell.mtwitch.feature.live

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LivePlayerScreen(
    channelLogin: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(onClick = onBack) {
            Text(text = "Back")
        }
        Text(
            text = "$channelLogin Live",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Live playback will use the shared Media3 player contract after stream resolution is implemented.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Chat panel placeholder: default local plugin is enabled, real chat transport arrives in the chat milestone.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
```

- [ ] **Step 10: Add Chat Plugin Settings screen**

Create `app/src/main/java/dev/mitchell/mtwitch/feature/plugins/ChatPluginSettingsScreen.kt`:

```kotlin
package dev.mitchell.mtwitch.feature.plugins

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mitchell.mtwitch.plugin.chat.DefaultChatPlugin

@Composable
fun ChatPluginSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(onClick = onBack) {
            Text(text = "Back")
        }
        Text(
            text = "Chat Plugins",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(text = DefaultChatPlugin.displayName)
        Switch(
            checked = DefaultChatPlugin.enabledByDefault,
            onCheckedChange = {},
        )
        Text(
            text = "The default local plugin is compiled into the app. Future plugins must use the same typed contract.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
```

- [ ] **Step 11: Add Settings screen**

Create `app/src/main/java/dev/mitchell/mtwitch/feature/settings/SettingsScreen.kt`:

```kotlin
package dev.mitchell.mtwitch.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(onClick = onBack) {
            Text(text = "Back")
        }
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Twitch account state, playback defaults, local history, and diagnostics will live here.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
```

- [ ] **Step 12: Add root app navigation**

Create `app/src/main/java/dev/mitchell/mtwitch/MtwitchApp.kt`:

```kotlin
package dev.mitchell.mtwitch

import androidx.compose.runtime.Composable
import androidx.navigation.toRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.mitchell.mtwitch.feature.channel.ChannelVodListScreen
import dev.mitchell.mtwitch.feature.home.HomeScreen
import dev.mitchell.mtwitch.feature.live.LivePlayerScreen
import dev.mitchell.mtwitch.feature.player.PlayerScreen
import dev.mitchell.mtwitch.feature.plugins.ChatPluginSettingsScreen
import dev.mitchell.mtwitch.feature.settings.SettingsScreen
import dev.mitchell.mtwitch.navigation.AppRoute

@Composable
fun MtwitchApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.Home,
    ) {
        composable<AppRoute.Home> {
            HomeScreen(
                onOpenChannel = { channelLogin ->
                    navController.navigate(AppRoute.Channel(channelLogin))
                },
                onOpenLive = { channelLogin ->
                    navController.navigate(AppRoute.Live(channelLogin))
                },
                onOpenChatPlugins = {
                    navController.navigate(AppRoute.ChatPlugins)
                },
                onOpenSettings = {
                    navController.navigate(AppRoute.Settings)
                },
            )
        }
        composable<AppRoute.Channel> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Channel>()
            ChannelVodListScreen(
                channelLogin = route.channelLogin,
                onOpenVod = { videoId ->
                    navController.navigate(AppRoute.Player(videoId.value))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable<AppRoute.Player> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Player>()
            PlayerScreen(
                videoId = route.videoId,
                onBack = { navController.popBackStack() },
            )
        }
        composable<AppRoute.Live> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Live>()
            LivePlayerScreen(
                channelLogin = route.channelLogin,
                onBack = { navController.popBackStack() },
            )
        }
        composable<AppRoute.ChatPlugins> {
            ChatPluginSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable<AppRoute.Settings> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
```

- [ ] **Step 13: Update MainActivity**

Replace `app/src/main/java/dev/mitchell/mtwitch/MainActivity.kt` with:

```kotlin
package dev.mitchell.mtwitch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.mitchell.mtwitch.ui.theme.MtwitchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MtwitchTheme {
                MtwitchApp()
            }
        }
    }
}
```

- [ ] **Step 14: Run all unit tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: PASS.

- [ ] **Step 15: Build debug APK**

Run:

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected: PASS and APK exists at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 16: Commit app shell**

Run:

```powershell
git add app/src/main/java/dev/mitchell/mtwitch app/src/test/java/dev/mitchell/mtwitch
git commit -m "feat: add Compose app shell"
```

Expected: commit succeeds.

## Task 8: M0-M1 Verification

**Files:**

- Modify only if needed after verification: files touched in Tasks 1-7.

- [ ] **Step 1: Run unit tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: PASS.

- [ ] **Step 2: Build debug APK**

Run:

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 3: Install on connected device if available**

Run:

```powershell
adb devices
```

If at least one device is listed as `device`, run:

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Expected: `Success`.

- [ ] **Step 4: Manual smoke test**

Open the app on the device or emulator and verify:

- Home screen renders.
- Entering a channel login enables the Open VODs button.
- Open VODs navigates to the VOD list screen.
- Tapping the sample VOD opens the player screen.
- Open Live Placeholder navigates to the live placeholder screen.
- Chat Plugins opens the default local plugin settings screen.
- Back buttons return to previous screens.
- Settings opens from Home.

- [ ] **Step 5: Check Git state**

Run:

```powershell
git status --short --branch
```

Expected: clean working tree on the active implementation branch.

- [ ] **Step 6: Create verification commit if fixes were needed**

If verification caused file edits, run:

```powershell
git add app DESIGN.md docs/design .gitignore settings.gradle.kts build.gradle.kts gradle.properties gradle
git commit -m "fix: stabilize Android foundation"
```

Expected: commit succeeds only if files changed.

## Immediate Post-M1 Playback Spike

Run this spike before starting VOD discovery UI beyond fake data. It is not part
of the M1 compile gate, but it is the next blocking technical risk.

**Goal:** prove `channel login -> VOD ID -> PlaybackAccessToken -> playable HLS
manifest` outside the UI.

**Output:** create `docs/spikes/playback-feasibility.md` with:

- Tool used: `curl`, a small Kotlin/JVM script, or an OkHttp console runner.
- Tested channel login.
- Tested normal VOD ID.
- Tested muted or partially muted VOD ID.
- Token request shape and required headers, with secrets redacted.
- Manifest URL shape, variant playlist observations, muted/discontinuous segment
  observations, and Media3 assumptions.
- Failure modes and whether the next playback plan can proceed.

**Gate:** do not begin the real M3 playback plan until this spike records a
working playable manifest or chooses a replacement playback strategy.

## Self-Review Checklist

- Spec coverage:
  - Design workflow artifacts are covered by Task 1.
  - Android project structure is covered by Task 2.
  - Compose navigation shell is covered by Task 7.
  - Theme based on `DESIGN.md` is covered by Tasks 1 and 7.
  - Account/auth is represented at the design/settings surface level in Task 7; real OAuth belongs to a later OAuth milestone so it does not block public VOD playback.
  - Local settings/history storage boundary is covered by Task 4.
  - Playback resolution boundary and muted/discontinuous timeline issue metadata are covered by Task 5; real HLS resolution belongs to the playback feasibility spike and playback plan.
  - Chat event, emote provider, chat plugin, default local plugin, and plugin failure isolation contracts are covered by Task 6.
  - Live playback and chat plugin settings are represented in navigation and UI placeholders in Task 7.
- Completeness scan:
  - No incomplete file paths.
  - No empty tasks.
  - External design outputs are handled through concrete prompt and checklist files.
- Type consistency:
  - `VideoId`, `ChannelId`, `WatchProgress`, and `Vod` are defined before use.
  - `AppRoute` typed route classes match tests and navigation.
  - `PlaybackSourceResolver` result types match tests and fake implementation.
  - `ChatEvent`, `ChatMessage`, `ChatPluginAction`, and `DefaultChatPlugin` names match tests and implementation snippets.

## Next Plans After This One

- M2 Playback Feasibility Spike: standalone channel to VOD ID to playback token to playable HLS manifest validation, including one muted VOD sample.
- M3 VOD Discovery And Playback: VOD list states, playback token/source resolution, Media3 player, scrubber, muted segment metadata, fullscreen.
- M4 OAuth And Account: mobile OAuth, token refresh, account state, followed/private surfaces, chat-send prerequisites.
- M5 Live Playback: live channel playback, stream metadata, reconnect/offline states, low-latency player options.
- M6 Live Chat And Default Plugin: selected Twitch chat transport, normalized events, send-message scopes/rate limits, default plugin dispatch.
- M7 Emotes And Plugin Expansion: Twitch emotes, BTTV, FFZ, 7TV provider contracts/caches, plugin decorations and local commands.
- M8 VOD Chat Replay: replay fetching, time sync, seek behavior, and emote-aware rendering.
- M9 Personal Usability And Beta: persisted DataStore or Room history, recent VODs, diagnostics, device performance pass, personal beta APK.
