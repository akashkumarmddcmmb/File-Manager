# Third-Party Licenses and Notices

Copyright © 2026 Akash Kumar. All Rights Reserved.

This document identifies major third-party software libraries and components
used by the File Manager project.

The original source code, application design, original project materials,
and other original work created specifically for this project remain the
property of Akash Kumar and are governed by the project's proprietary
license in the `LICENSE` file.

Third-party software listed in this document is NOT owned by Akash Kumar.
Each third-party component remains subject to its own copyright, license,
and applicable terms.

Nothing in this document transfers ownership of any third-party software
to the copyright holder of this project.

---

## 1. Important Licensing Notice

This project contains third-party libraries and software components.

The project's proprietary `LICENSE` applies only to the original project
materials that are owned by Akash Kumar.

It does not replace, restrict, or override the license of any third-party
component.

Where a third-party component is distributed with the application, its
applicable license terms, copyright notices, attribution requirements,
patent notices, and other legally required notices remain applicable.

Users and distributors are responsible for complying with the applicable
licenses of third-party components.

---

# 2. Major Third-Party Components

## 2.1 AndroidX

**Copyright:** The Android Open Source Project

**License:** Apache License 2.0

AndroidX components are used throughout the Android application, including
core Android extensions, lifecycle components, activity integration,
Compose integration, Room, testing components, and other AndroidX modules.

Examples used by this project include:

- androidx.core
- androidx.activity
- androidx.lifecycle
- androidx.compose
- androidx.compose.material
- androidx.compose.material3
- androidx.room
- androidx.annotation
- androidx.collection
- androidx.savedstate
- androidx.startup
- androidx.tracing
- androidx.profileinstaller
- androidx.datastore
- other AndroidX transitive components

The AndroidX components remain subject to the Apache License 2.0 and their
respective copyright notices.

---

## 2.2 Jetpack Compose

**Copyright:** The Android Open Source Project

**License:** Apache License 2.0

The project uses Jetpack Compose and related Compose components for the
application user interface.

Components include, where applicable:

- Compose UI
- Compose Runtime
- Compose Foundation
- Compose Material
- Compose Material Icons
- Compose Material 3
- Compose UI Graphics
- Compose UI Tooling
- Compose UI Testing
- related Compose runtime and UI components

The Compose components remain subject to their applicable Apache License 2.0
terms.

---

## 2.3 Kotlin

**Copyright:** JetBrains s.r.o. and Kotlin contributors

**License:** Apache License 2.0

The application is written using Kotlin and uses Kotlin standard library
components.

Resolved Kotlin runtime components in the project include:

- Kotlin Standard Library
- Kotlin Standard Library Common
- Kotlin Standard Library JDK components
- related Kotlin runtime components

The Kotlin project is licensed under the Apache License 2.0, subject to its
own license and third-party notices.

---

## 2.4 Kotlin Coroutines

**Copyright:** JetBrains s.r.o. and Kotlin contributors

**License:** Apache License 2.0

The project uses Kotlin Coroutines for asynchronous programming.

Components may include:

- kotlinx-coroutines-core
- kotlinx-coroutines-android
- kotlinx-coroutines-test
- kotlinx-coroutines-play-services
- related coroutine components

The Kotlin Coroutines project remains subject to the Apache License 2.0.

---

## 2.5 Google Accompanist Permissions

**Copyright:** The Android Open Source Project / Google contributors

**License:** Apache License 2.0

The project uses:

- accompanist-permissions

This component remains subject to its Apache License 2.0 terms.

---

## 2.6 Android Media3

**Copyright:** The Android Open Source Project

**License:** Apache License 2.0

The project uses Android Media3 components for media playback and related
media functionality.

Components include:

- androidx.media3:media3-exoplayer
- androidx.media3:media3-session
- androidx.media3:media3-ui

The resolved project version currently includes Media3 version `1.4.1`.

Media3 remains subject to the applicable Apache License 2.0 terms and
copyright notices.

---

## 2.7 Android Room

**Copyright:** The Android Open Source Project

**License:** Apache License 2.0

The project uses Android Room for local database functionality.

Components include:

- androidx.room:room-runtime
- androidx.room:room-ktx
- androidx.room:room-compiler
- related Room and SQLite components

The Room components remain subject to their applicable Apache License 2.0
terms.

---

## 2.8 Coil

**Copyright:** Coil Contributors

**License:** Apache License 2.0

The project uses Coil for image loading and image-related functionality.

Component:

- io.coil-kt:coil
- related Coil components

The project currently resolves Coil version `2.7.0`.

Coil remains subject to the Apache License 2.0 and its applicable copyright
notices.

---

## 2.9 Retrofit

**Copyright:** Square, Inc. and contributors

**License:** Apache License 2.0

The project uses Retrofit for HTTP API communication.

Components include:

- com.squareup.retrofit2:retrofit
- com.squareup.retrofit2:converter-moshi

The project currently uses Retrofit version `2.12.0`.

Retrofit remains subject to its applicable Apache License 2.0 terms.

---

## 2.10 OkHttp

**Copyright:** Square, Inc. and contributors

**License:** Apache License 2.0

The project uses OkHttp for HTTP networking.

Components include:

- com.squareup.okhttp3:okhttp
- com.squareup.okhttp3:logging-interceptor
- related OkHttp components

The declared OkHttp version may be `4.10.0`, but the resolved dependency
graph currently resolves OkHttp to version `4.12.0` where applicable.

OkHttp remains subject to the Apache License 2.0.

---

## 2.11 Okio

**Copyright:** Square, Inc. and contributors

**License:** Apache License 2.0

Okio is used directly or transitively by components such as OkHttp, Moshi,
Retrofit, and other libraries.

The resolved dependency graph includes Okio components.

Okio remains subject to its applicable Apache License 2.0 terms.

---

## 2.12 Moshi

**Copyright:** Square, Inc. and contributors

**License:** Apache License 2.0

The project uses Moshi for JSON serialization and deserialization.

Components include:

- com.squareup.moshi:moshi
- com.squareup.moshi:moshi-kotlin
- com.squareup.moshi:moshi-kotlin-codegen

The project currently uses Moshi version `1.15.2`.

Moshi remains subject to the Apache License 2.0.

---

## 2.13 Firebase Android SDK

**Copyright:** Google LLC

**License:** Apache License 2.0

The project uses Firebase Android components.

Components currently present in the resolved dependency graph include
Firebase components such as:

- Firebase AI
- Firebase App Check
- Firebase App Check reCAPTCHA
- Firebase App Check Debug
- Firebase Common
- Firebase Components
- Firebase Annotations
- related Firebase Android components

The project currently uses Firebase BOM version `34.17.0`.

Firebase components remain subject to their applicable licenses and notices.

---

## 2.14 Google Play Services

**Copyright:** Google LLC

**License:** See the applicable license terms for the individual Google
Play Services component.

Google Play Services components may be included transitively through
Firebase and other dependencies.

Examples include:

- play-services-basement
- play-services-tasks
- play-services-base
- other required Google Play Services components

These components remain subject to their own applicable license terms.

---

## 2.15 Ktor

**Copyright:** JetBrains s.r.o. and contributors

**License:** Apache License 2.0

Ktor components may be included transitively through the Firebase AI and
related dependency graph.

Components present in the resolved dependency graph include Ktor client
components such as:

- ktor-client-core
- ktor-client-okhttp
- ktor-client-websockets
- ktor-client-content-negotiation
- ktor-serialization-kotlinx-json
- ktor-http
- ktor-utils
- ktor-io
- related Ktor components

The resolved dependency graph includes Ktor version `3.0.3`.

Ktor remains subject to the Apache License 2.0.

---

## 2.16 SLF4J

**Copyright:** QOS.ch and contributors

**License:** MIT License

SLF4J components may be included transitively by networking and other
libraries.

Components may include:

- slf4j-api
- slf4j-nop

The resolved dependency graph currently includes SLF4J components.

SLF4J remains subject to its applicable MIT License terms.

---

# 3. Testing and Development Dependencies

The following third-party components are used for testing, development,
verification, or build-related purposes. They are not necessarily packaged
into the final production APK.

---

## 3.1 JUnit 4

**Copyright:** JUnit contributors

**License:** Eclipse Public License 1.0

The project uses JUnit for unit testing.

Component:

- junit:junit

JUnit remains subject to the Eclipse Public License 1.0.

---

## 3.2 AndroidX Test

**Copyright:** The Android Open Source Project

**License:** Apache License 2.0

The project uses AndroidX testing components.

Examples include:

- androidx.test.ext:junit
- androidx.test:core
- androidx.test:runner
- related AndroidX Test components

These components remain subject to the Apache License 2.0.

---

## 3.3 Espresso

**Copyright:** The Android Open Source Project

**License:** Apache License 2.0

The project uses Android Espresso testing components.

Component:

- androidx.test.espresso:espresso-core

Espresso remains subject to the Apache License 2.0.

---

## 3.4 Robolectric

**Copyright:** Robolectric contributors

**License:** MIT License

Robolectric may be used for JVM-based Android testing.

Component:

- org.robolectric:robolectric

Robolectric remains subject to its applicable license and copyright notices.

---

## 3.5 Roborazzi

**Copyright:** Roborazzi contributors

**License:** Apache License 2.0

The project uses Roborazzi-related components for screenshot/UI testing.

Components include:

- roborazzi
- roborazzi-compose
- roborazzi-junit-rule

Roborazzi remains subject to its applicable Apache License 2.0 terms.

---

# 4. Other Transitive Dependencies

The Gradle dependency graph contains additional transitive dependencies
required by the libraries listed above.

These may include components from:

- AndroidX
- Kotlin
- Kotlinx
- Google
- Firebase
- Google Play Services
- Square
- JetBrains
- Ktor
- OkHttp
- Okio
- SLF4J
- Guava
- Error Prone
- JSpecify
- javax.inject
- SQLite
- other supporting libraries

A dependency may appear in the Gradle dependency tree because another
library requires it. Its presence does not mean that the dependency is
owned by this project.

Each transitive dependency remains governed by its own applicable license.

---

# 5. Dependency Resolution

The dependency versions listed in this document are based on the project's
Gradle dependency configuration and the resolved dependency graph.

Gradle may resolve a different version than the version initially declared
when dependency constraints, BOMs, or transitive dependencies require a
different compatible version.

For this reason, the resolved Gradle dependency report should be treated as
the reference for the exact dependency versions used by a particular build.

The project dependency report was generated for:

`releaseRuntimeClasspath`

---

# 6. Third-Party License Compliance

When distributing an APK, application bundle, source distribution, or other
form of the software, applicable third-party license requirements must be
respected.

Depending on the component and distribution method, these requirements may
include:

- Retaining copyright notices.
- Retaining license notices.
- Providing a copy of the applicable license.
- Providing required attribution notices.
- Retaining applicable NOTICE information.
- Complying with patent-related provisions where applicable.
- Complying with trademark restrictions where applicable.
- Following any additional requirements specified by the individual
  third-party license.

The Apache License 2.0, for example, contains requirements concerning
copyright notices, license copies, modified-file notices, attribution
notices, and NOTICE files where applicable.

---

# 7. No Transfer of Ownership

Listing a third-party library in this document does not transfer ownership
of that library to Akash Kumar.

Likewise, the presence of third-party software in this project does not
transfer ownership of the original File Manager source code to any
third-party library author.

The rights and ownership of the original project and third-party software
remain separate.

---

# 8. Proprietary Project License

The original project source code and original materials created specifically
for this project are proprietary.

The project is distributed under the terms stated in:

`LICENSE`

Unless expressly permitted by the copyright holder, the public availability
of this repository does not grant permission to copy, modify, redistribute,
republish, sublicense, commercially exploit, or create derivative
applications from the original proprietary project materials.

This restriction applies only to materials that are actually owned by the
project copyright holder and does not override the licenses of third-party
components.

---

# 9. Third-Party Components Are Separate

Third-party libraries are not covered by the project's proprietary license
where their own licenses grant different rights.

Where a third-party license permits use, modification, or redistribution,
those rights arise from the third-party license and not from the proprietary
license of this project.

Where a third-party license imposes obligations, those obligations remain
applicable.

---

# 10. Source and License Verification

The following sources should be consulted when verifying the current
license of a dependency:

- The official source repository of the dependency.
- The license file distributed with the dependency.
- The dependency's published Maven/POM metadata.
- Any NOTICE file distributed with the dependency.
- The exact version of the dependency used by the build.

License information can change between versions. Therefore, the license
information applicable to a particular release should be verified against
the exact dependency version being distributed.

---

# 11. Changes to Dependencies

If a dependency is:

- Added,
- Removed,
- Upgraded,
- Downgraded,
- Replaced,
- Introduced transitively,

this document should be reviewed and updated when necessary.

A new dependency must not be assumed to have the same license as another
dependency merely because it provides similar functionality.

---

# 12. Application Assets and Other Materials

Third-party software licenses are separate from the licensing of:

- Images
- Icons
- Fonts
- Music
- Videos
- Sounds
- Logos
- Trademarks
- Screenshots
- Documentation
- Sample files
- Other externally sourced materials

Such materials may have separate copyright and license requirements.

Before distributing any third-party asset with the application, its
individual license and permission requirements should be verified.

---

# 13. Trademarks

Third-party names, product names, logos, trademarks, service marks, and
brand names belong to their respective owners.

Mention of a third-party product or library in this document does not imply
endorsement, sponsorship, or affiliation unless expressly stated.

---

# 14. No Warranty Regarding Third-Party Software

Third-party software is provided by its respective authors and licensors
under their respective licenses.

Akash Kumar does not claim ownership of third-party software and does not
provide a separate warranty for third-party components beyond any rights or
obligations expressly applicable under their respective licenses.

---

# 15. Important Notice for Distributors

If you redistribute this application, APK, AAB, source code, or any other
distribution containing third-party components, you are responsible for
reviewing and complying with the licenses applicable to those components.

The fact that this repository contains this document does not by itself
guarantee that every possible distribution format or future dependency
version has identical licensing requirements.

The exact contents of a particular release should be reviewed before
distribution.

---

# 16. Project Copyright

Copyright © 2026 Akash Kumar.

All rights reserved for the original project materials, subject to the
rights and licenses of third-party components identified in this document.

---

# 17. Final Licensing Statement

This document is provided to clearly identify major third-party software
used by the project.

It does not replace the individual licenses of those third-party
components.

It does not grant additional rights to the original proprietary source code
of this project.

The project's original materials remain governed by the project's
proprietary `LICENSE` file.

Third-party materials remain governed by their respective licenses.

Copyright © 2026 Akash Kumar. All Rights Reserved.
