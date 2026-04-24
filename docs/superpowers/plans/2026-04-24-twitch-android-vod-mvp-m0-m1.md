# Twitch Android VOD MVP M0-M1 Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create the design-handoff workflow and a native Android Compose foundation for the VOD-first Twitch client.

**Architecture:** This first plan covers M0-M1 only. It keeps production code in a single Android `:app` module with strict package boundaries for design system, features, data contracts, and local state; split Gradle modules can be introduced after the VOD discovery and playback flows prove their shape. UI starts with real Compose screens and fake state so the app can launch before Twitch API work begins.

**Tech Stack:** Kotlin 2.3.21, Android Gradle Plugin 9.2.0, Gradle Wrapper 9.4.1, Android SDK 36, Jetpack Compose BOM 2026.04.01, Media3 1.10.0, Navigation Compose 2.9.8, Lifecycle Compose 2.10.0, DataStore 1.2.1, Kotlin coroutines 1.10.2, JUnit 4.13.2.

---

## Scope Boundary

This plan implements only:

- M0 design workflow artifacts.
- M1 Android foundation.
- App shell, theme, navigation, screen scaffolds, local watch-history contract, account-state model, and playback-source contract.

This plan does not implement Twitch OAuth, Twitch API calls, real VOD list loading, real HLS URL resolution, Media3 playback wiring, chat, emotes, downloads, or release signing. Those each get their own plan after this foundation compiles and launches.

## References Checked

- Android Gradle Plugin release notes: https://developer.android.com/build/releases/gradle-plugin
- Compose BOM metadata: https://dl.google.com/dl/android/maven2/androidx/compose/compose-bom/maven-metadata.xml
- Media3 metadata: https://dl.google.com/dl/android/maven2/androidx/media3/media3-exoplayer/maven-metadata.xml
- Twitch Get Videos API: https://dev.twitch.tv/docs/api/reference#get-videos

## Dependency Approval Note

Executing this plan adds the Android production dependencies listed in `gradle/libs.versions.toml`: Compose, Navigation Compose, Lifecycle Compose, Media3, DataStore, and coroutines. Because the repository instructions require confirmation before adding production dependencies, pause before Task 2 if the user has not approved this dependency set.

## File Structure

Create or modify these files:

- `DESIGN.md`: human-readable product and UI rules produced from Stitch and Claude Design handoff.
- `docs/design/stitch-mvp-a-prompt.md`: prompt for Google Stitch exploration.
- `docs/design/claude-design-translation-prompt.md`: prompt for Claude Design translation.
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
- `app/src/main/java/dev/mitchell/mtwitch/navigation/AppRoute.kt`: route definitions.
- `app/src/main/java/dev/mitchell/mtwitch/ui/theme/MtwitchTheme.kt`: Compose theme.
- `app/src/main/java/dev/mitchell/mtwitch/feature/home/HomeScreen.kt`: Home / Resume scaffold.
- `app/src/main/java/dev/mitchell/mtwitch/feature/channel/ChannelVodListScreen.kt`: Channel VOD list scaffold.
- `app/src/main/java/dev/mitchell/mtwitch/feature/player/PlayerScreen.kt`: VOD player scaffold.
- `app/src/main/java/dev/mitchell/mtwitch/feature/settings/SettingsScreen.kt`: Settings scaffold.
- `app/src/test/java/dev/mitchell/mtwitch/core/model/VodModelsTest.kt`: model tests.
- `app/src/test/java/dev/mitchell/mtwitch/core/local/InMemoryWatchHistoryRepositoryTest.kt`: repository tests.
- `app/src/test/java/dev/mitchell/mtwitch/data/playback/FakePlaybackSourceResolverTest.kt`: playback contract tests.
- `app/src/test/java/dev/mitchell/mtwitch/navigation/AppRouteTest.kt`: navigation route tests.

## Task 1: Design Platform Handoff Prompts

**Files:**

- Create: `docs/design/stitch-mvp-a-prompt.md`
- Create: `docs/design/claude-design-translation-prompt.md`
- Create: `docs/design/artifact-checklist.md`
- Create: `DESIGN.md`

- [ ] **Step 1: Create the Google Stitch prompt**

Create `docs/design/stitch-mvp-a-prompt.md` with this content:

```markdown
# Google Stitch Prompt: M Twitch VOD-first Android Client

Design a native Android Twitch VOD client for personal use. The first version is VOD-first, not a full social Twitch replacement.

Primary user goal:
- Find a Twitch channel.
- Browse recent archived broadcasts.
- Open a VOD.
- Watch, pause, scrub, fullscreen, and resume later.

Design constraints:
- Android phone first.
- Native Jetpack Compose implementation.
- Performance-sensitive video player screen.
- Avoid heavy decorative animation around video playback.
- The first screen should be useful immediately, not a marketing page.
- Chat, VOD chat replay, BTTV, FFZ, and 7TV emotes are post-MVP.

Create 2-3 visual directions and include these screens:
- Home / Resume
- Channel VOD List
- VOD Player portrait
- VOD Player landscape/fullscreen
- Settings

For each screen include:
- Loading state
- Empty state
- Recoverable error state
- Offline or auth-needed state where relevant

Pay special attention to:
- Player overlay layout
- Scrubber ergonomics
- Recent VOD progress indicators
- One-handed phone use
- Smooth but restrained transitions
- A design-system summary with colors, type scale, spacing, controls, and motion rules

Output a DESIGN.md-style summary after the visual exploration.
```

- [ ] **Step 2: Create the Claude Design translation prompt**

Create `docs/design/claude-design-translation-prompt.md` with this content:

```markdown
# Claude Design Prompt: Translate Stitch Direction To Compose Handoff

You are translating the selected Google Stitch design direction for a native Android app named M Twitch.

The app is a VOD-first Twitch client for personal use. The MVP includes:
- Home / Resume
- Channel VOD List
- VOD Player
- Settings
- Saved watch progress

The MVP excludes:
- Live chat
- VOD chat replay
- Third-party emotes
- Downloads
- Public store release polish

Use the provided Stitch output as the visual source of truth. Produce an implementation handoff for Kotlin + Jetpack Compose.

The handoff must include:
- Design tokens: colors, typography, spacing, elevation, shape radius
- Component list: app shell, top bars, list rows, VOD cards, player controls, scrubber, buttons, settings rows, error states
- Screen-by-screen layout rules
- State table for loading, empty, auth-needed, offline, playback unavailable, and retrying
- Motion rules with durations and easing
- Player overlay behavior in portrait and landscape
- Accessibility notes for touch targets, contrast, and content descriptions
- A concise DESIGN.md that engineering can commit to the repository

Avoid web-specific implementation details. Do not generate a WebView app. Optimize the handoff for native Compose.
```

- [ ] **Step 3: Create the artifact checklist**

Create `docs/design/artifact-checklist.md` with this content:

```markdown
# Design Artifact Checklist

Engineering starts only after these artifacts exist:

- `DESIGN.md` with selected visual direction and design tokens.
- Stitch export or screenshots for Home / Resume.
- Stitch export or screenshots for Channel VOD List.
- Stitch export or screenshots for VOD Player portrait.
- Stitch export or screenshots for VOD Player landscape/fullscreen.
- Stitch export or screenshots for Settings.
- Claude Design handoff notes for Compose components.
- Claude Design state table for loading, empty, error, offline, and auth-needed states.
- Claude Design motion rules for navigation and player overlays.

The first implementation can use the baseline `DESIGN.md` in this repository while external artifacts are being prepared. Replace it with the Claude Design handoff before final UI polish.
```

- [ ] **Step 4: Create the baseline DESIGN.md**

Create `DESIGN.md` with this content:

```markdown
# M Twitch Design System

## Product Direction

M Twitch is a focused Android Twitch VOD client. The interface should feel calm, fast, and playback-centered. It should prioritize resuming content, scanning VOD lists, and controlling playback without visual noise.

## Visual Style

- Dark-first interface tuned for video viewing.
- High-contrast text on dark surfaces.
- One accent color for active playback and progress.
- No decorative gradients, floating blobs, or oversized marketing sections.
- Rounded corners stay at 8dp or below unless the final Claude Design handoff changes that rule.

## Layout

- Phone-first portrait layout.
- Bottom navigation is avoided in MVP because there are only four destinations.
- Home links to channel lookup, recent VODs, settings, and player resume.
- Player controls appear as an overlay on top of the video surface.
- Landscape player prioritizes video and hides nonessential UI.

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
Test-Path docs/design/stitch-mvp-a-prompt.md
Test-Path docs/design/claude-design-translation-prompt.md
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

- [ ] **Step 1: Extend `.gitignore`**

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

- [ ] **Step 2: Create root Gradle settings**

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

- [ ] **Step 3: Create root build file**

Create `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
```

- [ ] **Step 4: Create Gradle properties**

Create `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
```

- [ ] **Step 5: Create version catalog**

Create `gradle/libs.versions.toml`:

```toml
[versions]
agp = "9.2.0"
kotlin = "2.3.21"
composeBom = "2026.04.01"
activityCompose = "1.13.0"
lifecycle = "2.10.0"
navigation = "2.9.8"
media3 = "1.10.0"
datastore = "1.2.1"
coroutines = "1.10.2"
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
junit = { module = "junit:junit", version.ref = "junit" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

- [ ] **Step 6: Create app build file**

Create `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
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

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

- [ ] **Step 7: Create manifest**

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

- [ ] **Step 8: Create Android theme resource**

Create `app/src/main/res/values/styles.xml`:

```xml
<resources>
    <style name="Theme.MTwitch" parent="android:style/Theme.Material.NoActionBar" />
</resources>
```

- [ ] **Step 9: Create launcher foreground resources**

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

- [ ] **Step 10: Create temporary MainActivity**

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

- [ ] **Step 11: Generate Gradle wrapper**

If `gradle` is not installed, run these commands from the repository root:

```powershell
$gradleZip = Join-Path $env:TEMP "gradle-9.4.1-bin.zip"
$gradleHome = Join-Path $env:TEMP "gradle-9.4.1"
Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-9.4.1-bin.zip" -OutFile $gradleZip
Expand-Archive -LiteralPath $gradleZip -DestinationPath $env:TEMP -Force
& "$gradleHome\bin\gradle.bat" wrapper --gradle-version 9.4.1 --distribution-type bin
```

Expected: wrapper files are generated in the repository.

- [ ] **Step 12: Verify Gradle wrapper version**

Run:

```powershell
.\gradlew.bat --version
```

Expected: output includes `Gradle 9.4.1`.

- [ ] **Step 13: Run initial Android test task**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: build succeeds with no unit tests found or all tests passing.

- [ ] **Step 14: Commit Android foundation scaffold**

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
            updatedAtEpochMs = 1_000L,
        )

        assertEquals(0.5f, progress.fractionOf(150.minutes), 0.0001f)
        assertEquals(1f, progress.copy(positionMs = 10_000_000L).fractionOf(10.minutes), 0.0001f)
        assertEquals(0f, progress.copy(positionMs = -100L).fractionOf(10.minutes), 0.0001f)
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
    val updatedAtEpochMs: Long,
) {
    fun fractionOf(duration: Duration): Float {
        val durationMs = duration.inWholeMilliseconds
        if (durationMs <= 0L) return 0f
        val clampedPosition = min(max(positionMs, 0L), durationMs)
        return clampedPosition.toFloat() / durationMs.toFloat()
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
        val first = WatchProgress(VideoId("1"), positionMs = 5_000L, updatedAtEpochMs = 10L)
        val second = WatchProgress(VideoId("1"), positionMs = 8_000L, updatedAtEpochMs = 20L)

        repository.saveProgress(first)
        repository.saveProgress(second)

        assertEquals(second, repository.progressFor(VideoId("1")))
        assertEquals(listOf(second), repository.recent.value)
    }

    @Test
    fun clearRemovesAllProgress() = runTest {
        val repository = InMemoryWatchHistoryRepository()
        repository.saveProgress(WatchProgress(VideoId("1"), positionMs = 5_000L, updatedAtEpochMs = 10L))

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
                    videoId = VideoId("123"),
                    hlsUrl = "https://example.com/vod.m3u8",
                ),
            ),
        )

        val result = resolver.resolve(VideoId("123"))

        assertEquals(
            PlaybackSourceResult.Ready(
                PlaybackSource(VideoId("123"), "https://example.com/vod.m3u8"),
            ),
            result,
        )
    }

    @Test
    fun resolverCanReturnUnavailable() = runTest {
        val resolver = FakePlaybackSourceResolver(
            result = PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.SourceMissing),
        )

        val result = resolver.resolve(VideoId("missing"))

        assertTrue(result is PlaybackSourceResult.Unavailable)
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

import dev.mitchell.mtwitch.core.model.VideoId

data class PlaybackSource(
    val videoId: VideoId,
    val hlsUrl: String,
)

sealed interface PlaybackSourceResult {
    data class Ready(val source: PlaybackSource) : PlaybackSourceResult
    data class Unavailable(val reason: PlaybackUnavailableReason) : PlaybackSourceResult
}

enum class PlaybackUnavailableReason {
    SourceMissing,
    TokenRejected,
    NetworkTimeout,
}

interface PlaybackSourceResolver {
    suspend fun resolve(videoId: VideoId): PlaybackSourceResult
}
```

- [ ] **Step 4: Add fake resolver**

Create `app/src/main/java/dev/mitchell/mtwitch/data/playback/FakePlaybackSourceResolver.kt`:

```kotlin
package dev.mitchell.mtwitch.data.playback

import dev.mitchell.mtwitch.core.model.VideoId

class FakePlaybackSourceResolver(
    private val result: PlaybackSourceResult,
) : PlaybackSourceResolver {
    override suspend fun resolve(videoId: VideoId): PlaybackSourceResult {
        return when (result) {
            is PlaybackSourceResult.Ready -> result.copy(
                source = result.source.copy(videoId = videoId),
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

## Task 6: Navigation And Compose Screen Shell

**Files:**

- Create: `app/src/test/java/dev/mitchell/mtwitch/navigation/AppRouteTest.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/navigation/AppRoute.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/ui/theme/MtwitchTheme.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/feature/home/HomeScreen.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/feature/channel/ChannelVodListScreen.kt`
- Create: `app/src/main/java/dev/mitchell/mtwitch/feature/player/PlayerScreen.kt`
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
    fun routesUseStablePathSegments() {
        assertEquals("home", AppRoute.Home.path)
        assertEquals("channel/{channelLogin}", AppRoute.Channel.path)
        assertEquals("player/{videoId}", AppRoute.Player.path)
        assertEquals("settings", AppRoute.Settings.path)
    }

    @Test
    fun routeBuildersEncodeDynamicValues() {
        assertEquals("channel/cohhcarnage", AppRoute.Channel.create("cohhcarnage"))
        assertEquals("player/123456789", AppRoute.Player.create("123456789"))
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

sealed class AppRoute(val path: String) {
    data object Home : AppRoute("home")
    data object Settings : AppRoute("settings")

    data object Channel : AppRoute("channel/{channelLogin}") {
        fun create(channelLogin: String): String = "channel/$channelLogin"
    }

    data object Player : AppRoute("player/{videoId}") {
        fun create(videoId: String): String = "player/$videoId"
    }
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

        Text(
            text = "Recent VODs will appear here after playback progress is saved.",
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

- [ ] **Step 9: Add Settings screen**

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

- [ ] **Step 10: Add root app navigation**

Create `app/src/main/java/dev/mitchell/mtwitch/MtwitchApp.kt`:

```kotlin
package dev.mitchell.mtwitch

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.mitchell.mtwitch.feature.channel.ChannelVodListScreen
import dev.mitchell.mtwitch.feature.home.HomeScreen
import dev.mitchell.mtwitch.feature.player.PlayerScreen
import dev.mitchell.mtwitch.feature.settings.SettingsScreen
import dev.mitchell.mtwitch.navigation.AppRoute

@Composable
fun MtwitchApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.Home.path,
    ) {
        composable(AppRoute.Home.path) {
            HomeScreen(
                onOpenChannel = { channelLogin ->
                    navController.navigate(AppRoute.Channel.create(channelLogin))
                },
                onOpenSettings = {
                    navController.navigate(AppRoute.Settings.path)
                },
            )
        }
        composable(
            route = AppRoute.Channel.path,
            arguments = listOf(navArgument("channelLogin") { type = NavType.StringType }),
        ) { backStackEntry ->
            val channelLogin = requireNotNull(backStackEntry.arguments?.getString("channelLogin"))
            ChannelVodListScreen(
                channelLogin = channelLogin,
                onOpenVod = { videoId ->
                    navController.navigate(AppRoute.Player.create(videoId.value))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = AppRoute.Player.path,
            arguments = listOf(navArgument("videoId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val videoId = requireNotNull(backStackEntry.arguments?.getString("videoId"))
            PlayerScreen(
                videoId = videoId,
                onBack = { navController.popBackStack() },
            )
        }
        composable(AppRoute.Settings.path) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
```

- [ ] **Step 11: Update MainActivity**

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

- [ ] **Step 12: Run all unit tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: PASS.

- [ ] **Step 13: Build debug APK**

Run:

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected: PASS and APK exists at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 14: Commit app shell**

Run:

```powershell
git add app/src/main/java/dev/mitchell/mtwitch app/src/test/java/dev/mitchell/mtwitch
git commit -m "feat: add Compose app shell"
```

Expected: commit succeeds.

## Task 7: M0-M1 Verification

**Files:**

- Modify only if needed after verification: files touched in Tasks 1-6.

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

## Self-Review Checklist

- Spec coverage:
  - Design workflow artifacts are covered by Task 1.
  - Android project structure is covered by Task 2.
  - Compose navigation shell is covered by Task 6.
  - Theme based on `DESIGN.md` is covered by Tasks 1 and 6.
  - Account/auth is represented at the design/settings surface level in Task 6; real OAuth belongs to the next plan.
  - Local settings/history storage boundary is covered by Task 4.
  - Playback resolution boundary is covered by Task 5; real HLS resolution belongs to the playback plan.
- Completeness scan:
  - No incomplete file paths.
  - No empty tasks.
  - External design outputs are handled through concrete prompt and checklist files.
- Type consistency:
  - `VideoId`, `ChannelId`, `WatchProgress`, and `Vod` are defined before use.
  - `AppRoute` route strings match tests and navigation.
  - `PlaybackSourceResolver` result types match tests and fake implementation.

## Next Plans After This One

- M2 VOD Discovery: Twitch OAuth, channel lookup, Get Videos API, VOD list states.
- M3 VOD Playback: playback token/source resolution, Media3 player, scrubber, lifecycle, fullscreen.
- M4 Personal Usability: persisted DataStore or Room history, recent VODs, diagnostics, device performance pass.
