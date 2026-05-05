# 🚀 Setup guide

This guide takes you from a fresh clone to a built plugin JAR running on a
local Hytale dev server. If something here is out of date, please open an issue.

---

## 1. Install prerequisites

### Java 25
Hytale targets **Java 25** (Project Loom virtual threads). Older JDKs will fail
at plugin load time with `Unsupported class file major version`.

- Download [Eclipse Temurin 25](https://adoptium.net/temurin/releases/?version=25)
- Verify: `java -version` should print `openjdk version "25..."`
- On Windows, set `JAVA_HOME` to the JDK install path and add `%JAVA_HOME%\bin`
  to `PATH`.

### Git
- Windows: [Git for Windows](https://git-scm.com/download/win)
- macOS: `brew install git`
- Linux: your package manager

### VS Code
- [Download VS Code](https://code.visualstudio.com/)
- On first opening this project, VS Code will offer to install the
  **recommended extensions** from `.vscode/extensions.json` — accept.

---

## 2. Clone and open

```bash
git clone https://github.com/TODO_YOUR_USERNAME/hestia-bag.git
cd hestia-bag
code .
```

VS Code will detect the Gradle project and start importing. Wait for the
"Java: Loading..." indicator in the status bar to disappear (1–2 minutes
the first time — Gradle is downloading dependencies).

---

## 3. Generate the Gradle wrapper

The repo doesn't ship `gradlew` / `gradlew.bat` (they're build artifacts and
shouldn't live in version control as binaries). Generate them once:

```bash
# Linux / macOS
gradle wrapper --gradle-version 9.2.0

# Windows (Powershell, with Gradle installed via Scoop or Chocolatey)
gradle wrapper --gradle-version 9.2.0
```

If you don't have a system Gradle:
- macOS: `brew install gradle`
- Windows: `choco install gradle` or `scoop install gradle`
- Linux: [SDKMAN!](https://sdkman.io/) — `sdk install gradle 9.2.0`

After this step you'll see new files: `gradlew`, `gradlew.bat`,
`gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`.
Commit them — from this point on, everyone uses the wrapper, not their system
Gradle.

---

## 4. First build

```bash
./gradlew shadowJar
```

> **Expected on the very first run**: this build will likely **fail** with
> `Could not resolve com.hypixel.hytale:server-api:+`. That's because the
> exact Maven coordinate is unverified — this is the first integration point
> with the real Hytale API.

To fix:
1. Clone the official template:
   `git clone --branch plugin https://github.com/Build-9/Hytale-Example-Project`
2. Open its `build.gradle.kts` (or `build.gradle`)
3. Find the `compileOnly("...")` line that pulls in the Hytale server API
4. Copy that exact coordinate into our `build.gradle.kts`, replacing the
   `compileOnly("com.hypixel.hytale:server-api:+")` line
5. Re-run `./gradlew shadowJar`

The build should now succeed and produce
`build/libs/HestiaBag-0.1.0.jar`.

---

## 5. Resolve the `// TODO` blocks

Search for `⚠️ TODO` across the codebase — every Hytale API integration point
is marked. Recommended order to bring the plugin to a working state:

| Order | File | Why first |
|---|---|---|
| 1 | `items/HestiaItems.java`            | All other systems reference items |
| 2 | `crafting/HestiaBagRecipe.java`     | Without it, no bag exists in-game |
| 3 | `commands/HestiaCommand.java`       | Lets you test entering home via `/hestia` |
| 4 | `events/BagPlacementListener.java`  | Lets you place the bag |
| 5 | `events/BagInteractListener.java`   | Lets you (and guests) enter via right-click |
| 6 | `dimension/HestiaDimensionManager.java` | World creation, teleport, platform |
| 7 | `events/FallVoidListener.java`      | Void fall safety net |
| 8 | `inventory/BagSocketUI.java`        | Last because UI API may still shift |

Each TODO block has pseudo-code showing the *intended* shape. Adapt names to
match the real Hytale API as you encounter it.

---

## 6. Test on a local Hytale server

Hytale plugins are loaded from:
- **Windows (local dev client)**:
  `%AppData%\Roaming\Hytale\UserData\earlyplugins\`
- **Linux (dedicated server)**:
  `/opt/hytale/Server/mods/`

Copy `build/libs/HestiaBag-0.1.0.jar` into that directory and start (or
restart) the server. Watch the server log — you should see:

```
[INFO] === HestiaBag: starting ===
[INFO] HestiaBag ready: 13 upgrades, 5 skins registered.
```

If you see `[TODO] ...` lines, that's expected at this stage — they trace
exactly which integration points still need wiring.

### Setting up an automated test server (optional)

Once the API is wired, add a `runServer` Gradle task that downloads a Hytale
dev server, drops the freshly built JAR into its plugins folder, and launches
it. The Britakee template has a working example you can lift directly.

---

## 7. Going public

### GitHub
```bash
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/TODO_YOUR_USERNAME/hestia-bag.git
git push -u origin main
```

The CI workflow (`.github/workflows/build.yml`) will run on every push.
A green check on `main` means the plugin builds reproducibly from a clean
checkout — the bare minimum quality bar before sharing.

### Tagging a release
```bash
git tag v0.1.0
git push --tags
```

The `release` job in the workflow detects the `v*` tag and creates a GitHub
Release with the JAR attached.

### CurseForge
1. Go to <https://www.curseforge.com/> and create a project under the Hytale
   category (once Hytale is officially supported there)
2. Generate an API token at <https://authors.curseforge.com/account/api-tokens>
3. Add it as a GitHub secret named `CURSEFORGE_TOKEN`
4. Uncomment the CurseForge upload step in `.github/workflows/build.yml`,
   filling in your `project_id`
5. The next tagged release will auto-upload

Before publishing publicly:
- Replace every `TODO_YOUR_NAME` and `TODO_YOUR_USERNAME` in the repo
- Replace `"ServerVersion": "*"` in `manifest.json` with the actual Hytale
  version you tested against (Update 3 introduced strict version equality —
  `"*"` may not be valid)
- Bump the version in `gradle.properties`

---

## 8. Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `Unsupported class file major version 69` | Server running on Java < 25 | Install Java 25 |
| `Could not resolve com.hypixel.hytale:server-api` | Coordinate is wrong/placeholder | See step 4 |
| Plugin loads but no items appear | `HestiaItems.registerAll` is still all `TODO` | See step 5 |
| `ServerVersion mismatch` on plugin load | `manifest.json` uses `"*"` and Hytale rejects it | Set the exact server version |
| Homes vanish after restart | `HomeJsonCodec.deserialize` throws by design | Wire JSON parsing (priority TODO) |

---

## 9. Reference docs

- [Hytale modding hub](https://hytalemodding.dev) — community-maintained
- [Britakee Studios GitBook](https://britakee-studios.gitbook.io/hytale-plugin-development/) — recipes, items, events
- [Build-9 example project](https://github.com/Build-9/Hytale-Example-Project) — minimal working plugin
- [Koboo's manifest plugin](https://github.com/Koboo/hytale-pluginmanifest) — auto-generates `manifest.json` from Gradle

Happy hacking.
