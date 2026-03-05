# WatScripts

A collection of automation scripts for Old School RuneScape, built on the [DreamBot](https://dreambot.org/) API (v3.0.0). Each script targets a specific in-game activity — from account building and skilling to combat and moneymaking.

## Scripts

### WatAB — P2P Account Builder
All-in-one members account builder. Automatically selects and trains skills based on configured targets, completes quests (Witch's Potion, Vampire Slayer, Romeo & Juliet, and more), handles Tutorial Island, and manages combat training. Tracks experience gains and level-ups across 20+ skills.

### WatAIO — F2P Account Builder
Free-to-play account builder with quest automation and skill training. Handles F2P-specific progression paths including quests (Sheep Shearer, Goblin Diplomacy, Ernest the Chicken, Imp Catcher, etc.), all F2P skills, Grand Exchange interaction, and net worth tracking.

### WatBlastMiner
Mines ore at the Blast Mine for 300-400k GP/hour. Handles dynamite placement, ore collection, death recovery via Death's Coffer, and membership bonding. Sends Discord notifications on major milestones (99 Mining/Firemaking).

### WatBonder
Utility script that converts F2P accounts to P2P by purchasing and redeeming bonds. Stops automatically once membership is confirmed. Designed as a preparatory step before running other scripts.

### WatCooker
Private cooking script with forum-based user authentication. Supports multiple authorized users with per-user Discord webhook notifications. Includes GPT-powered chat responses for player interactions during cooking activities.

### WatLiquidator
Account liquidation script for converting in-game assets. Handles asset conversion tasks with world hopping and bonding support.

### WatMarketAlcher
Reads live OSRS market data and performs High Level Alchemy at the Grand Exchange for profit. Automatically purchases profitable items and tracks alch margins. Fetches market prices from a custom API endpoint for real-time arbitrage calculations.

### WatScriptStarter
Utility script that validates account prerequisites (membership status, wealth, skill levels) and then launches a follow-up script. Acts as a conditional entry point for chaining scripts together in automated progressions.

### WatSelfAlcher
Self-sustaining alchemy loop. Trains Fletching to 70 to craft bows, then alchs them for profit. Creates a closed-loop gold generation system with no external item sourcing needed.

### WatShamans
Combat moneymaker that kills Lizardmen Shamans for ~1M GP/hour. Tracks kills, Dragon Warhammer drops, deaths, and ranged levels. Handles death recovery through Death's Coffer rather than stopping. Switches between regular and combat-optimized mouse algorithms based on context. Includes GPT chat responses for player interactions.

### WatShayzienCollector
Account preparation script for Shayzien-related content. Sets up prerequisites before handing off to main scripts.

### WatWcGuild
Woodcutting Guild script focused on plank making. Manages log processing, stamina potion usage, and plank creation tracking. Includes GPT chat responses for player interactions.

## Architecture

All scripts share a common structure:

```
WatXxx/
├── pom.xml
└── src/main/java/org/lolwat/
    ├── WatScript.java          # Entry point (@ScriptManifest)
    ├── managers/
    │   ├── ConfigManager.java  # Profile-based configuration
    │   ├── TaskManager.java    # Task selection and execution
    │   └── TeleportManager.java
    ├── tasks/                  # Modular task implementations
    │   ├── HopperTask.java     # World hopping
    │   ├── BondingTask.java    # F2P → P2P
    │   ├── BankingTask.java
    │   ├── GrandExchangeTask.java
    │   └── ...
    ├── misc/utils/             # Shared utilities
    └── paint/                  # Overlay UI
```

**Task system**: Scripts are driven by a `TaskManager` that selects and executes modular `Task` implementations. Tasks handle specific activities (mining, combat, banking, etc.) and can be chained, queued, or swapped dynamically.

**Mouse algorithms**: Scripts use different mouse movement strategies depending on the activity — `HumanMouse`, `SmartMouseRegular`, `SmartMouseCombat`, and `SmartMouseMultiDir`.

**Integrations**:
- Discord webhooks for notifications (deaths, milestones, drops)
- GPT chat responses via proxy API for player interactions
- Live OSRS market data for alchemy profit calculations

## Building

Each script uses Maven. The shade plugin outputs compiled JARs to `~/DreamBot/Scripts/`.

```bash
cd WatXxx
mvn package
```

## License

[MIT](LICENSE)
