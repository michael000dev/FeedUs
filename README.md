FeedUs is a cross-platform, and open-source RSS client.

It is in the early stages of development and anyone is welcome to give suggestions and PR.

FeedUs contains code that is open sourced from [FeedMe](https://github.com/seazon/FeedMe).

## Screenshots
### Android
<img width="250" alt="ui-Android-login" src="./docs/imgs/android-login.png" /><img width="250" alt="ui-Android-feeds" src="./docs/imgs/android-feeds.png" /><img width="250" alt="ui-Android-articles" src="./docs/imgs/android-articles.png" />

### iOS
<img width="250" alt="ui-iOS-login" src="./docs/imgs/ios-login.png" /><img width="250" alt="ui-iOS-feeds" src="./docs/imgs/ios-feeds.png" /><img width="250" alt="ui-iOS-articles" src="./docs/imgs/ios-articles.png" />

### macOS / Windows / Linux
```
./gradlew clean
./gradlew build
./gradlew run
```
<img width="250" alt="ui-macOS-login" src="./docs/imgs/macos-login.png" /><img width="250" alt="ui-macOS-feeds" src="./docs/imgs/macos-feeds.png" />

<img width="250" alt="ui-Windows-login" src="./docs/imgs/windows-login.jpg" /><img width="250" alt="ui-Windows-feeds" src="./docs/imgs/windows-feeds.jpg" /><img width="250" alt="ui-Windows-articles" src="./docs/imgs/windows-articles.jpg" />

<img width="250" alt="ui-Linux-login" src="./docs/imgs/linux-login.png" /><img width="250" alt="ui-Linux-feeds" src="./docs/imgs/linux-feeds.png" />

## Release

The GitHub Actions workflow (`.github/workflows/build-release.yml`) automatically builds, signs, and publishes multi-ABI APKs to GitHub Releases when a version tag is pushed.

### Required GitHub Secrets

Configure the following secrets in your repository (**Settings → Secrets and variables → Actions**):

| Secret | Description |
|--------|-------------|
| `KEYSTORE` | Base64-encoded keystore file. Generate with: `base64 -w 0 your-keystore.jks` |
| `SIGNING_STORE_PASSWORD` | Keystore password |
| `SIGNING_KEY_ALIAS` | Key alias inside the keystore |
| `SIGNING_KEY_PASSWORD` | Key password |

### Creating a Release

Push a version tag to trigger the workflow:

```bash
git tag v0.2.0
git push origin v0.2.0
```

The workflow will build APKs for `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`, then create a GitHub Release with all four APKs attached.

## Progress
| function     | Android | iOS | macOS | Windows | Linux |
|--------------|---------|-----|-------|---------|------|
| normal login | ✅      | ✅  | ✅     | ✅      | ✅    |
| auth login   | ✅      | ❌  | ❌     | ❌      | ❌    |
| sync         | ✅      | ✅  | ✅     | ✅      | ✅    |
| subscribe    | ✅      | ✅  | ✅     | ✅      | ❓    |
| mark read    | ✅      | ✅  | ✅     | ✅      | ❓    |

## API Support
|                                                                                     | Support Unread Count API | Feed ID Star with ”Feed” | Support Subscribe API | Support Tag API | Support Star API | Support Fetch by Feed / Category | Support Fetch IDs and then Stream | Support pagination | Support podcast |
|-------------------------------------------------------------------------------------| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [Folo](https://api.folo.is/reference)                                               | ✅ | N | ✅ | ❌ | ✅ | ✅ | ❌ | ✅ | ✅ |
| Fever API [link 1](https://web.archive.org/web/20230616124016/https://feedafever.com/api),[link 2](https://blog.badguy.top/index.php/archives/294/), [link 3](https://github.com/bazqux/bazqux-api/blob/master/FeverAPI.md) (Miniflux / CommaFeed) | ❌ | N | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ |
| Tiny Tiny RSS                                                                       | ✅ | N | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Feedly                                                                              | ✅ | Y | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Feedbin                                                                             | ❌ | N | ✅ | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ |
| Google Reader API ( Inoreader / The Old Reader / FreshRSS / BazQux)                 | ✅ | Y | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
