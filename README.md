# tw9ine

**tw9ine** is a utility mod for **Minecraft 26.3.x** built with Fabric. It is a fork of
[tw9ine](https://github.com/coltonk9043/Tw9ine-Client), with a new interface, a
reduced combat module set and many bug fixes.

---

## Requirements

- Minecraft **26.3.x**
- Fabric Loader **0.19.5+** and Fabric API
- Java **25**

## Build

```bash
./gradlew build
```

The jar is written to `build/libs/tw9ine-<version>.jar`.

---

## Interface

- Dark, neutral ClickGUI with rounded panels over a blurred background.
- Disabled modules are grey; enabled modules are white with a light-blue accent bar.
- **Left-click** a module to toggle it, **right-click** to expand its settings inline.
- The accent colour and the rest of the palette can be changed in the *Hud* tab.

Default key: `RSHIFT` opens/closes the ClickGUI.

---

## Combat modules

| Module | What it does |
| --- | --- |
| **Aimbot** | Aims at a target. Priority by crosshair, distance, health or damage; sticky targeting; can aim only while attacking. |
| **BowAimbot** | Calculates the arrow arc (bows and crossbows) and leads moving targets. |
| **TriggerBot** | Attacks the entity under your crosshair when the cooldown is ready. Optional weapon-only and criticals-only modes. |
| **MaceAura** | Mace smash on nearby targets, switching to the mace automatically and back afterwards. |
| **AutoTotem** | Keeps a Totem of Undying in the off-hand (always, or only when in danger). |
| **AutoCrystal** | Places and breaks End Crystals around enemies with separate place/break ranges and self-damage protection. |
| **AutoAnchor** | Places, charges and detonates Respawn Anchors (does nothing in the Nether, where anchors do not explode). |
| **HitCrystal** | Hold right-click while looking at a block: places obsidian, places a crystal on it and breaks it. |

---

## Credits and license

tw9ine is based on **tw9ine** by coltonk9043 and its contributors
(cvs0, Tewxx, OsakiTsukiko, Logging4J, TangyKiwi, Xatsec, BatchDebug, Cocolots).

This project is licensed under the **GNU General Public License v3 (GPLv3)**, the same
licence as tw9ine. See [LICENSE](LICENSE).
