/*
 * build.gradle.kts — HestiaBag plugin build configuration.
 *
 * We use Kotlin DSL because:
 *   - It is the recommended default for new Hytale plugin projects.
 *   - Type-safety helps when tweaking shadowJar / compileOnly setups.
 *   - The community automation scripts (Koboo's manifest plugin, Britakee's
 *     run-server task) are written in Kotlin and integrate more cleanly here.
 *
 * Plugins applied:
 *   - `java`             : Java toolchain support.
 *   - `shadow`           : Bundles all runtime dependencies inside a single fat JAR.
 *                          The Hytale server does NOT merge classpaths automatically,
 *                          so anything we ship that the server doesn't already provide
 *                          must be embedded here.
 */

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

// Project coordinates pulled from gradle.properties for easy override in CI.
group   = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

java {
    toolchain {
        // Hytale targets Java 25 (uses virtual threads from Project Loom internally).
        // Mismatched JDK = "Unsupported class file major version" at load time.
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()

    // Official Hytale Maven repository (release builds of the server API).
    maven {
        name = "hytaleRelease"
        url  = uri("https://maven.hytale.com/releases")
    }

    // Pre-release repository for testing against upcoming server builds.
    // Disable for production builds to avoid pulling unstable APIs.
    maven {
        name = "hytalePrerelease"
        url  = uri("https://maven.hytale.com/prereleases")
    }
}

dependencies {
    /*
     * compileOnly = available at compile time, NOT bundled into the final JAR.
     * The server already provides the API jar at runtime, and bundling our own copy
     * would shadow the runtime classes and trigger LinkageError at load time.
     *
     * ⚠️ TODO — verify the exact Maven coordinate against the official template:
     *           https://github.com/Build-9/Hytale-Example-Project (branch `plugin`)
     *           Several names circulate in community docs:
     *             - com.hypixel.hytale:server-api:<version>
     *             - com.hytale:server:<version>
     *           Whichever the upstream template uses, copy it verbatim here.
     */
    compileOnly("com.hypixel.hytale:server-api:+")

    // JetBrains nullability annotations. Compile-only is enough — they're not
    // present at runtime, the JVM doesn't enforce them.
    compileOnly("org.jetbrains:annotations:24.1.0")
}

tasks {
    shadowJar {
        // We want HestiaBag-0.1.0.jar, not HestiaBag-0.1.0-all.jar.
        archiveClassifier.set("")

        // No relocation needed yet — we don't bundle any third-party libraries
        // that could clash with the server. Add minimize() once we do.
    }

    // `./gradlew build` should produce the deployable JAR by default.
    build {
        dependsOn(shadowJar)
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        // -parameters keeps method parameter names available via reflection.
        // Useful for command framework parameter binding and cleaner stack traces.
        options.compilerArgs.add("-parameters")
    }
}
