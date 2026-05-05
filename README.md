# 🏠 HestiaBag

> A pocket-dimension housing system for Hytale, inspired by Dofus' *Havre-Sac*.

Craft a Hestia Bag, equip it, place it anywhere in the world, and turn the
spot into a portal to your private dimension. Loot upgrade modules from chests
to expand your home. Invite friends, knock to enter someone else's place,
unlock cosmetic skins that re-theme both your bag and your interior.

---

## ✨ Features

### Crafting & equipping
- Hestia Bags must be **crafted** — no `/give`. The recipe is
  intentionally meaningful so the bag feels earned.
- Each crafted bag is **bound to its crafter's UUID**. Other players cannot
  use a bag they didn't craft.
- Equipped in a **dedicated virtual slot** (custom UI overlay).

### One player, one dimension
- Crafting a second bag does **not** create a second dimension —
  both bags portal to the same home.
- Resetting a home **drops every chest, plant, and upgrade** at the
  placed-bag's location, so you keep the loot but get a clean slate.

### Multiplayer permissions
Four permission modes plus a transverse blacklist:
| Mode | Who can enter |
|---|---|
| `PUBLIC`    | Anyone |
| `WHITELIST` | Only invited players |
| `KNOCK`     | Anyone may knock; owner accepts/denies |
| `PRIVATE`   | Owner only |
| (Blacklist) | Always denied — overrides everything else |

When the owner is **offline**, the bag entity stays placed but every guest
interaction is rejected.

### Skins as full themes
A skin re-styles **both** the placed bag entity and the dimension's spawn pad
decoration — themes like *Verdant Sanctuary*, *Ember Forge*, *Celestial
Chamber*, *Void Walker*. Skins drop from world chests or unlock through
achievements.

### Dimension safety
- Spawn pad is **16×16**, expandable +16×16 per area-granting upgrade.
- Falling below **Y=0** teleports you back to the spawn pad of the home you're
  currently inside (works for both owners and guests).

---

## 🎮 Usage

| Action | How |
|---|---|
| Get a bag | Craft a Hestia Bag at a workbench |
| Place the bag | Right-click a block surface with the bag equipped |
| Pick the bag back up | Shift-right-click the placed bag (owner only) |
| Enter your home | Right-click your own placed bag, or `/hestia` |
| Visit a friend | Right-click their placed bag (per their permission level) |
| Exit | Right-click any Hestia Bag, or `/hestia exit` |
| Manage upgrades & skins | Right-click the **Hestia Socket** block inside the home |
| Set permissions | Socket UI, or `/hestia perm <mode>` |
| Invite a friend | `/hestia invite <player>` |
| Reset (DESTRUCTIVE) | Socket UI → Reset tab (confirmation required) |

> **Note on the `H` shortcut key**: Hytale does not yet expose custom client
> keybinds to plugins (server-first architecture, see
> [hytalemodding.dev Player Input Guide](https://hytalemodding.dev/en/docs/guides/plugin/player-input-guide)).
> Right-click and `/hestia` are the supported entry methods. Once the API
> exposes keybinds, an `H` shortcut will be added.

---

## 🛠️ Build

### Prerequisites
- **Java 25** ([Temurin](https://adoptium.net/) recommended)
- **Git**
- **VS Code** with the recommended extensions
  (auto-suggested on first open via `.vscode/extensions.json`)

### Steps
```bash
git clone https://github.com/TODO_YOUR_USERNAME/hestia-bag.git
cd hestia-bag
./gradlew shadowJar
```

The deployable JAR lands in `build/libs/HestiaBag-<version>.jar`.

### Install on a Hytale server
Copy the JAR into:
- **Windows (local dev client)**: `%AppData%\Roaming\Hytale\UserData\earlyplugins\`
- **Linux server**: `/opt/hytale/Server/mods/`

---

## 📦 Architecture

```
src/main/java/com/hestia/bag/
├── HestiaBagPlugin.java                  Plugin entry point, DI root
├── api/                                  Public API for other plugins (placeholder)
├── commands/
│   ├── HestiaCommand.java                /hestia
│   └── HestiaAdminCommand.java           /hestia-admin
├── config/
│   └── HestiaConfig.java                 Tunable values (loot rate, TTLs, caps)
├── crafting/
│   └── HestiaBagRecipe.java              Bag recipe registration
├── dimension/
│   ├── HestiaDimensionManager.java       Slot allocation, teleport, expand
│   ├── DimensionResetService.java        Destructive reset + drop pipeline
│   └── DimensionContentSnapshot.java     Captured contents during reset
├── events/
│   ├── PlayerJoinListener.java           Restore equipped slot on join
│   ├── BagPlacementListener.java         Equipped → placed entity
│   ├── BagPickupListener.java            Owner picks bag back up
│   ├── BagInteractListener.java          Anyone interacts with placed bag
│   └── FallVoidListener.java             Bounce on Y < 0
├── inventory/
│   ├── BagSlotManager.java               Virtual custom equipment slot
│   └── BagSocketUI.java                  Tabbed UI (upgrades, skins, perms)
├── items/
│   └── HestiaItems.java                  Item / block registration
├── permissions/
│   ├── BagPermissionLevel.java           PUBLIC / WHITELIST / KNOCK / PRIVATE
│   ├── PermissionManager.java            Decision logic, knock state
│   ├── KnockRequest.java                 Pending request with TTL
│   └── AccessDecision.java               Decision result type
├── skins/
│   ├── BagSkin.java                      Theme descriptor
│   ├── SkinRegistry.java                 Catalog
│   └── SkinUnlockService.java            Unlock + equip operations
├── storage/
│   ├── PlayerHome.java                   Persistent state per player
│   ├── PlayerHomeStorage.java            JSON-per-file persistence
│   ├── HomeJsonCodec.java                (placeholder) JSON serializer
│   ├── PlacedBag.java                    World-bag value type
│   └── PlacedBagRegistry.java            Track all placed bags
└── upgrades/
    ├── Upgrade.java                      Immutable descriptor
    ├── UpgradeRegistry.java              Catalog
    └── UpgradeType.java                  Categories
```

---

## 🚧 Project status

**Version 0.1.0 — structural skeleton.**

The architecture, data model, and permission flow are fully in place. What's
NOT yet wired:

- The Hytale **plugin API surface** (event types, command framework, item
  registry) is referenced as `// TODO` blocks throughout. Each TODO has
  pseudo-code showing the intended integration.
- **JSON deserialization** of saved homes throws — homes don't yet survive
  a server restart. Replace with Hytale's Codec system or bundle a JSON lib.
- **UI rendering** (socket tabs, knock popup) waits on Hytale's UI API,
  which isn't fully published yet.
- **Models and textures** are referenced by id (e.g. `hestia:skin/ember.bag`)
  but the actual `.bbmodel` and texture assets need to be created in the
  Hytale Asset Editor or Blockbench.

The quickest path to a functional v0.2 is:
1. Clone the [official template](https://github.com/Build-9/Hytale-Example-Project)
2. Copy its build dependencies into our `build.gradle.kts`
3. Resolve the TODOs in roughly this order:
   `HestiaItems` → `HestiaCommand` → `BagPlacementListener` →
   `BagInteractListener` → `HestiaDimensionManager` (world creation, teleport)

See [SETUP.md](SETUP.md) for the detailed step-by-step.

---

## ⚠️ Known limitations

1. **No client-side `H` keybind** — Hytale's modding model is server-first.
   See [Player Input Guide](https://hytalemodding.dev/en/docs/guides/plugin/player-input-guide).
2. **Single-server scope** — homes don't follow players across servers.
3. **In-memory permission state** — knock requests don't survive server
   restarts (intentional: stale requests would be confusing).

---

## 🗺️ Roadmap

- [x] **v0.1** — full architecture, all systems present as stubs
- [ ] **v0.2** — Hytale API wiring: items, recipes, commands, listeners
- [ ] **v0.3** — Dimension creation, teleport, void-fall functional
- [ ] **v0.4** — Loot tables (upgrades drop from world chests)
- [ ] **v0.5** — Socket UI: install upgrades, equip skins, manage permissions
- [ ] **v0.6** — Reset pipeline: snapshot, drop, wipe
- [ ] **v0.7** — Bag entity model + first 3 skins (Wanderer, Verdant, Ember)
- [ ] **v1.0** — CurseForge release

---

## 📝 License

[MIT](LICENSE) — fork, modify, redistribute freely.

## 🙏 Credits

- **Hypixel Studios** for Hytale and its modding-first design
- The Hytale modding community: Britakee Studios, HytaleModding.dev, Koboo
- **Ankama** for the original Havre-Sac concept in Dofus
