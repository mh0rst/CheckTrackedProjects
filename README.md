# CheckTrackedProjects
Java tool to check tracked AOSP projects from Android manifests for updates between tags.

It takes the name of a new and old AOSP tag, and checks the tracked projects in the android manifest for updates introduced by the new tag.

## Requirements
- JDK 8
- Maven

## Build
1. Clone this repo
2. Run ```mvn package```
3. Find the executable in the ```target``` dir

## Usage

```java -jar CheckTrackedProjects-1.0-SNAPSHOT-jar-with-dependencies.jar <NEW_TAG> <OLD_TAG> [<ANDROID_MANIFEST>]```

e.g.: ```java -jar CheckTrackedProjects-1.0-SNAPSHOT-jar-with-dependencies.jar android-6.0.1_r78 android-6.0.1_r74 CM13_0```

Supported android manifests are:
- LOS14_1 - LineageOS 14.1
- CM13_0 - LineageOS 13
