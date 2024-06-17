# hMachines [1.2.0]
Developed by: Markineo

First of all, thank you for your stay here!

## Requirements
The plugin has some dependencies. Here is the list:
- `PlaceholderAPI`
- `Vault`
- `LuckPerms`
- `HeadDatabase`
- `WorldGuard`

## About
### Fuel
- Each fuel is defined in the '`combustiveis.yml`' file, where you can set its information. It is important to note that the plugin considers one liter equivalent to one drop.
- There are two types of fuel: crude and refined. To refine them, you need to use a Refinery.
- You can set the percentage chance of each crude fuel breaking a machine.

### Refinery
- The refinery is purchased from the store and is basically used for refining. `(programmer laughs)`

### Machines
- Machine drops can be defined in two main ways: by file and by the command `/madmin adddrop <machine id> <percentage%>`. For the command to work, you must be holding the item you want to add.
- To remove a drop, you can use `/madmin removedrop <machine id> <drop id>`.

### Menus
The menus have interesting functionalities; initially, there are only two: combustiveis.yml and maquinas.yml. You can add more, but they will need different IDs and TITLES. You can configure different actions for the clicks.
- `left_action` and `right_action` support the following functions:
```yml
- "compra <produto:id>" -> Os produtos aceitos são: 'maquina/refinaria/combustivel/fix'
- "playermessage <mensagem>"
- "playercmd <comando>"
- "consolecmd <comando>"
- "openmenu <id do menu>"
```

Example:
```yml
items:
    1:
        type: "icon"
        id: '397:3'
        skull_owner: '{heads-9356}'
        name: "&3Cancel"
        left_action: "playermessage &7You canceled the action."
        description:
        - "&7Click to cancel."
        pos_x: 0
        pos_y: 0
```

### HeadsDatabase
- The plugin is compatible with HeadsDatabase, so you can add custom heads by inserting {heads-ID} into skull_owner.

## Permissions
- hmachines.admin
- hmachines.stack
- hmachines.use
