# MyCampus — Firebase & Google Sign-In Setup Guide

### Application Identity
- **App Name:** MyCampus
- **Tagline:** One Campus. One Platform.
- **Developer Credit:** Crafted with ❤️ by AkaX
- **Package Name / Application ID:** Defined in `app/build.gradle.kts`

---

## 1. Obtaining SHA-1 and SHA-256 Fingerprints

To enable Google Sign-In with Firebase Authentication, you must register the SHA fingerprints from the signing keystore into your Firebase Console.

### Local Debug Build (Debug Keystore)

#### Windows (Command Prompt / PowerShell)
```bash
gradlew signingReport
```
*Or using Gradle directly:*
```bash
gradle :app:signingReport
```

#### macOS / Linux
```bash
./gradlew signingReport
```

#### Locating the Fingerprints in the Output:
Look for the `Variant: debug` section:
```text
Variant: debug
Config: debug
Store: ~/.android/debug.keystore
Alias: AndroidDebugKey
MD5:  XX:XX:XX:...
SHA1: 1A:2B:3C:4D:5E:6F:7A:8B:9C:...
SHA-256: AA:BB:CC:DD:EE:FF:00:11:22:33:...
```

### Release Build (Production Signing Keystore)
Use Java `keytool` on your release keystore:
```bash
keytool -list -v -keystore path/to/your-release-key.jks -alias your-key-alias
```

---

## 2. Firebase Console Configuration Checklist

1. **Open Firebase Project Settings**:
   - Go to [Firebase Console](https://console.firebase.google.com/).
   - Select your project -> Project Settings -> **Your apps**.
2. **Add SHA Fingerprints**:
   - Under your Android App (`com.aistudio...`), click **Add fingerprint**.
   - Add both the **SHA-1** and **SHA-256** fingerprints obtained above.
3. **Enable Google Provider in Authentication**:
   - Go to **Build -> Authentication -> Sign-in method**.
   - Enable **Google** provider and **Email/Password** provider.
   - Set the support email for the project.
4. **Download `google-services.json`**:
   - Download the updated `google-services.json` from Project Settings.
   - Replace `/app/google-services.json` with your downloaded file.
5. **Rebuild the App**:
   ```bash
   gradle assembleDebug
   ```

---

## 3. Role-Based Architecture & Security

- **Student (`student`)**: Default role for new Google accounts. Access to attendance statistics, timetable, notes, exam papers, StudyMate AI tutor, and CampusConnect.
- **Faculty / Teacher (`teacher`)**: Assigned by Principal. Access to take attendance, upload unit notes & question papers, and create assignments for their assigned classes.
- **Principal (`principal`)**: Provisioned administrative access. Full governance over students, teachers, subject allocations, college timetable, and announcements.
