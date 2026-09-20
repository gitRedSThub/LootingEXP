# LootingEXP

Looting gets you more drops. It doesn't get you more XP. This fixes that.

Every level of Looting on the weapon that actually got the kill adds a percentage on top of the mob's XP. Default's 30% per level, so a zombie worth 5 XP hands you 10 with Looting III.

Paper 1.21.11. Java 21 or newer.

## Setup

Drop the jar in `plugins/` and start the server. `config.yml` writes itself on first boot and the defaults are already sensible. There's nothing you have to set up before it works.

## How the number gets worked out

```
final XP = base XP × (1 + Looting level × percent / 100)
```

At the default 30%:

| Looting | Multiplier |
| --- | --- |
| none | 1.00× |
| I | 1.30× |
| II | 1.60× |
| III | 1.90× |
| IV | 2.20× |
| V | 2.50× |

20 XP with Looting III is 38. 25 XP with Looting III is 47.5, which rounds to 48.

There's no floating point anywhere in that. Percentages are stored as whole hundredths and the whole calculation runs on integers, because `25 * 1.9` as a double is really 47.499999999999996 and would quietly round *down* to 47. Nobody would ever catch that in game. Which is exactly why it's worth doing properly.

It works off the XP that's about to drop, not the vanilla number. If some other plugin already pushed a mob up to 100 XP, Looting III makes it 190.

## Which weapon counts

Melee: whatever's in the main hand at the moment of the hit.

Arrows and tridents: the item they were fired from. Minecraft records that when the shot leaves the bow, so swapping weapons mid-flight changes nothing. Fire with a Looting bow, switch to a stone shovel, the bow still counts.

Everything else is Looting 0. Splash potions, TNT you lit, a wolf you own, fall damage, lava, a creeper that wandered into someone. The plugin won't guess if it can't point at a specific weapon, you get plain XP.

Custom levels work fine. A datapack that hands out Looting X gives you ten times your percentage. Nothing is hardcoded to stop at III.

## config.yml

```yaml
more-exp:

  # Turn this off and mobs drop their normal vanilla XP again.
  # Nothing else in this file matters while it's false.
  enabled: true

  # What one level of Looting is worth, as a percentage of the XP
  # the mob was already going to drop.
  # At 30, a zombie worth 5 XP gives 9.5 with Looting III, which rounds up to 10.
  # Set it to 0 if you want the bonus gone but the plugin still loaded.
  # Range: 0.00 - 1000.00, two decimal places at most.
  exp-per-looting-level-percent: 30.00

  # Hard ceiling for a single mob. It's here so that a slip of the hand on the
  # line above can't dump thousands of XP out of one kill.
  # It only ever trims the bonus. XP a mob was already dropping is never reduced.
  # Range: 1 - 100000.
  max-xp-per-kill: 10000

  rounding:

    # 25 XP with Looting III lands on exactly 47.5. This decides where it goes.
    # NEAREST - 47.5 becomes 48
    # DOWN    - 47.5 becomes 47
    # UP      - 47.1 becomes 48
    mode: NEAREST
```

Two decimal places, never more. `30`, `30.0`, `30.00` and `99.25` all work. `30.123` doesn't.

A value that's missing, the wrong type, or out of range goes back to its **default** — not clamped to the nearest limit — and the console tells you exactly what happened. Put `-50` in for the percent and you get 30.00 back, not 0. The file gets rewritten to match, comments and all, so what's on disk is always what's actually running.

Break the YAML itself and the plugin falls back to defaults and leaves your file completely alone, so you can still find the typo.

## Commands

`/lootingexp`, or `/lexp` if you're in a hurry. Needs `lootingexp.admin`, which ops have already.

| Command | What it does |
| --- | --- |
| `/lootingexp` | Current settings and status |
| `/lootingexp get [system] [setting]` | The same, or just one value |
| `/lootingexp set more-exp <setting> <value>` | Change a value |
| `/lootingexp toggle more-exp` | Switch the system on or off |
| `/lootingexp reload` | Re-read config.yml |
| `/lootingexp help` | The list above |

Everything tab-completes the whole way down, including a few safe numbers to pick from.

```
/lootingexp set more-exp exp-per-looting-level-percent 50
/lootingexp set more-exp max-xp-per-kill 5000
/lootingexp set more-exp rounding mode DOWN
/lootingexp toggle more-exp
```

`/set` won't touch `enabled` on purpose — that's what `/toggle` is for. Anything you change saves to `config.yml` right away.

`/lootingexp get` prints this:

```
LootingEXP v1.0.0
│
├─ MoreEXP: ON
│  ├─ XP Per Looting Level: 30.00%
│  ├─ Maximum XP Per Kill: 10,000
│  └─ Rounding: Nearest
│
└─ Status
   └─ XP bonus: Active
```