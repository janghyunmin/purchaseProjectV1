fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android firebaseDev

```sh
[bundle exec] fastlane android firebaseDev
```

파이어베이스 개발서버

### android firebaseStage

```sh
[bundle exec] fastlane android firebaseStage
```

파이어베이스 스테이지서버

### android firebaseReal

```sh
[bundle exec] fastlane android firebaseReal
```

파이어베이스 운영서버

### android realApk

```sh
[bundle exec] fastlane android realApk
```

운영 APK

### android realAab

```sh
[bundle exec] fastlane android realAab
```

운영 번들

### android app_version_update

```sh
[bundle exec] fastlane android app_version_update
```

버전 업데이트

### android send_slack_message

```sh
[bundle exec] fastlane android send_slack_message
```



----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
