# KillStreaks Plugin

Simple Bukkit plugin for Minecraft 1.21.x providing killstreak abilities, bounties, and server events.

Build
```
mvn package
```

Install
- Put the produced JAR from `target/` into your server's `plugins/` folder and restart.

Quick build (helper):
```
./build.sh
```

Commands
- `/ks bounties` - list current bounties
- `/ks config toggleDrop` - toggle using drop (Q) to activate abilities
- `/ks admin giveks <player> [amount]` - admin: give killstreak credits
- `/ks admin setbounty <player> <amount>` - admin: set bounty

Notes
- New players get a 30-minute grace period (no killstreaks counted against them).
- Dash ability is triggered by dropping an item (Q) when unlocked.

Notes about bounties:
- When you kill a player who has a bounty, you receive a random reward: netherite ingots, gold blocks, an enchanted golden apple, and/or a custom "Bounty Blade".
