# Vouchers
Create items that run commands for players

### Features
- Create unique items that players right-click to run commands(s)
- An admin menu to view and obtain all loaded vouchers

### Requirements
- There is one required dependency: [ServerUtils](https://www.spigotmc.org/resources/serverutils.106515/) 1.0.10+
- There is one soft dependency RewardsInventory, which is currently private

### Admin Commands
- The base command is `voucher` with the permission `vouchers.admin`
- All commands require permission to use which follows the format `vouchers.admin.command` where command is the name of the command
- Note: arguments with <> are required and [] are optional
- `/voucher give <player> <id> [amount]` Give the player a voucher item
- `/voucher help` Opens the help menu
- `/voucher list` Opens the voucher file system menu
- `/voucher reload [arg]` Reload the plugin
- `/voucher reward <player> <id> <menuID> [amount]` Add a voucher to the player's RewardsInventory menu

### General Configuration
```yaml
vouchers:
  # The global default voucher material
  default_material: NAME_TAG
  # The global default voucher rarity
  default_rarity: common
  # If the plugin should stop players from claiming a voucher they have the permission for
  checkForPermission: true
  # How long the plugin should wait for a second click by the player
  confirmationDelaySeconds: 5
  # Commands to run when any voucher is claimed. Placeholders are {player} and {permission}
  onClaimCommands:
  - lp user {player} permission set {permission}
  # Global default lore prefix and suffix for all items. Placeholders are {rarity} {rarity_color}
  lore:
    prefix:
    - '&7Rarity: {rarity}'
    - ''
    - '&8Obtained by:'
    suffix:
    - ''
    - '&7Right-click this item to claim'
messages:
  confirmation: <SOLID:10A5F5>[Vouchers] &eClick again to claim
  hasPermission: <SOLID:d2b48c>[Vouchers] &cYou already have access to this claimable!
# Define rarities to fill item name/lore placeholders
rarity:
  '1':
    id: common
    colorCode: '&7'
    name: Common
  '2':
    id: rare
    colorCode: '&9'
    name: Rare
```

### Creating Vouchers
```yaml
overrides:
  # All fields here are optional. If set, they will override the global setting for this file ONLY
  material: ''
  rarity: ''
  lore: # Placeholders are {rarity} {rarity_color}.
    prefix: []
    suffix: []
vouchers:
  # The id of the voucher. Must be unique in the global space
  chatcolor-black:
    # Used in {placeholder}. Cannot be blank
    permission: chatcolor.color.0
    # Sent to the player when claimed. Set to '' to disable
    onClaimMessage: '&a&l(+) &7You''ve claimed the &e/chatcolor &8Black&7'
    # Extra commands to send to the player when claimed. Placeholders are {player}
    extraCommands: []
    # Uses ServerUtils item generation which optionally takes more parameters
    item:
      # Setting the material here will override any global or local materials
      # material: DIAMOND
      name: '&f&lChatcolor: &8[&8&lBlack&8]'
      lore:
      - '&7This item is obtained from ???'
  # Define more vouchers in this file ...
```

### Notes
- You can create files and folders inside the `/vouchers` folder to organize your vouchers
- Vouchers must be right-clicked when not looking at a block to be claimed
- Vouchers can only be claimed from the main hand