# AdvancedVanish
![license](https://img.shields.io/github/license/quantiom/EventHandler?color=%23b59e28&style=for-the-badge) ![made-with-kotlin](https://img.shields.io/badge/MADE%20WITH-KOTLIN-%23b59e28?style=for-the-badge&logo=java)  ![last-commit](https://img.shields.io/github/last-commit/quantiom/AdvancedVanish?color=%23b59e28&style=for-the-badge)  
AdvancedVanish is a fully customizable and advanced vanish plugin made in Kotlin.

## Features
- Fully customizable through the [config](src/main/resources/config.yml). (70+ options)
  - Messages
  - Permissions
  - Actions
  - Hooks
  - Placeholders
  - Much more...
- Vanished players are **completely** invisible, as if they are not even online.
- Vanish priorities/levels ([more info](https://github.com/quantiom/AdvancedVanish/blob/main/src/main/resources/config.yml#L93-L117))
  - Supports many different permissions plugins. (LuckPerms, PermissionsEx, bPermissions, GroupManager)
- Many configurable hooks which provide support to other plugins.
   - Essentials
   - PlaceholderAPI
   - DiscordSRV
   - Dynmap
   - Much more...
- Togglable Actions when vanished (15+)
- Commands:
  - `/vanish` *- Toggle vanish.*
  - `/vanish reload` *- Reloads the config and hooks*
  - `/vanish priority` *- Displays your vanish priority.*
  - `/vanish list` *- Dispalys a list of vanished players.*
  - `/vanish status <player>` *- Check if a player is in vanish*
- For the rest of the features, check out the [config](src/main/resources/config.yml).

## API
Before utilizing the API, make sure that the `AdvancedVanish` plugin is
enabled, or add `depend: [AdvancedVanish]` to your plugin's `plugin.yml`.

### Methods
```kotlin
AdvancedVanishAPI.vanishPlayer(player: Player): Unit
AdvancedVanishAPI.unVanishPlayer(player: Player): Unit
AdvancedVanishAPI.isPlayerVanished(player: Player): Boolean
AdvancedVanishAPI.canSee(player: Player, target: Player): Boolean
```
### Extensions
```kotlin
Player.isVanished(): Boolean
```
### Events
- `PrePlayerVanishEvent` - Gets called before vanishing a player, implements `Canellable`.
- `PlayerVanishEvent` - Gets called after a player vanishes.
- `PrePlayerUnVanishEvent` - Gets called before a player unvanishes, imeplements `Cancellable`.
- `PlayerUnVanishEvent` - Gets called after a player unvanishes.
