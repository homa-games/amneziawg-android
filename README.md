# Android GUI for [AmneziaWG](https://amnezia.org/learn-more/31_amneziawg)

**[Download from the Play Store](https://play.google.com/store/apps/details?id=org.amnezia.awg)**

This is an Android GUI for [AmneziaWG](https://amnezia.org/learn-more/31_amneziawg).

## Building

```
$ git clone --recurse-submodules https://github.com/amnezia-vpn/amneziawg-android
$ cd amneziawg-android
$ ./gradlew assembleRelease
```

macOS users may need [flock(1)](https://github.com/discoteq/flock).

## Publish and use

[Documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)

Publish artifacts
```
$ ./gradlew publishReleaseToDemoRepository
```

Use repo
```
maven("https://github.com/homa-games/amneziawg-android/raw/repository/demorepo")
```