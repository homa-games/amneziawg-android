This project forked from amnezia-vpn/amneziawg-android

## Clone repository and assemble project

```
$ git clone --recurse-submodules git@github.com:homa-games/amneziawg-android.git
$ cd amneziawg-android
$ ./gradlew assembleRelease
```

macOS users may need [flock(1)](https://github.com/discoteq/flock).

## Publish and use

[Documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)

Publish artifacts to demorepo
```
$ ./gradlew publishReleasePublicationToDemoRepository
```

Add repository to project
```
maven("https://github.com/homa-games/amneziawg-android/raw/repository/demorepo")
```

Declare dependency
```
implementation("org.amnezia.awg:awg-tunnel:1.0.20251231")
```

Build tunnel library
```
$ ./gradlew tunnel:assembleRelease
```
