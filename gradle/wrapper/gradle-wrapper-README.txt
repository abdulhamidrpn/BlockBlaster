GRADLE WRAPPER JAR SETUP
========================
The gradle-wrapper.jar file is not included due to binary file size.

To set it up:
Option A (Recommended): Open the project in Android Studio.
  Android Studio will automatically detect the missing wrapper jar and offer to download it.

Option B: Run from terminal (requires Gradle installed):
  cd BlockBlaster
  gradle wrapper --gradle-version 8.10.2

Option C: Download manually:
  Download from: https://services.gradle.org/distributions/gradle-8.10.2-bin.zip
  Extract and copy: gradle/wrapper/gradle-wrapper.jar
