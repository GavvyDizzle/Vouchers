# Vouchers
Create items that run commands for players

### Features
- Create unique items that players right-click to run commands(s)
- An admin menu to view and obtain all loaded vouchers

### Requirements
- There is ons required dependency: [ServerUtils](https://www.spigotmc.org/resources/serverutils.106515/)
- There is one soft dependency RewardsInventory, which is currently private

### Admin Commands
- The base command is `voucher` with the permission `vouchers.admin`
- All commands require permission to use which follows the format `vouchers.admin.command` where command is the name of the command
- Note: arguments with <> are required and [] are optional
- `/voucher give <player> <id> [amount]` Give the player a voucher item
- `/voucher help` Opens the help menu
- `/voucher list` Opens the voucher list menu
- `/voucher reload [arg]` Reload the plugin
- `/voucher reward <player> <id> <menuID> [amount]` Add a voucher to the player's RewardsInventory menu

### Creating Vouchers
- Shared voucher commands
```yaml
# config.yml
# This command will be run for all players and fill in the permission from the claimed voucher
onClaimCommands:
  - lp user {player} permission set {permission}
```
- Individual vouchers
```yaml
# any .yml file in the /vouchers folder
vouchers:
  example:
    permission: permission.example # Fills in {permission} in config.yml
    customModelData: 0 # Supports custom model data for resource packs
    material: NAME_TAG
    displayName: '&eExample Voucher'
    lore:
    - 'Explain what your voucher does'
    - 'or how it was obtained'
    extraCommands: [] # Run extra commands here. Supports {player} placeholder
    onClaimMessage: '&a(!) You claimed the example voucher'
    glow: true # If this voucher should be enchanted
  example2:
   # Continue making more vouchers here
```

### Notes
- You can create files and folders inside the `/vouchers` folder to organize your vouchers
- You can sort the vouchers by id in the menu with `sortVouchersAlphabetically` in config.yml
- Vouchers must be right-clicked when not looking at a block to be claimed
- By default, the plugin will stop a player from claiming a voucher if they already have permission. This can be turned off with `checkForPermission` in config.yml