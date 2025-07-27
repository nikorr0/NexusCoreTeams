# NexusCoreTeams
**NexusCoreTeams** is a plugin for the Paper/Purpur/Spigot/Bukkit servers (Minecraft Java Edition 1.17 - 1.21.*).  

[![Available on Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/plugin/nexuscoreteams)

Each scoreboard team protects one End Crystal - their **Nexus**. When the crystal is destroyed, further respawns for that team occur in spectator mode. When all team members are spectators the team is declared eliminated.

Using this plugin, you can create popular team‑based minigames such as "Destroy the Core", "Annihilation", "Mega Walls", "Bed Wars", and more.

![](https://github.com/nikorr0/NexusCoreTeams/blob/main/screenshots/Colorful_end_crystals.png)

---

## Functional overview

* **Configurable health**<br>
You can set the Nexus using `/nexus set <team> <hp>` and change it later via `/nexus hp <team> <hp>`.

* **Weapon damage**<br>
If `weapon-damage-enabled = true` the Nexus loses HP equal to the attack damage of the player’s weapon, else any weapon will deal 1 damage to the Nexus.

* **Arrow toggle**<br>
If `arrow-damage-enabled = false` arrows are ignored, else arrows will damage the Nexus.

* **Hit cooldown**<br>
After a hit the crystal becomes invulnerable for *x* seconds (configured via `hit-cooldown`).

![](https://github.com/nikorr0/NexusCoreTeams/blob/main/screenshots/Configurable_cooldown.png)

* **Explosive immunity**<br>
TNT, creeper and other block/entity explosions do not harm the Nexus.

* **Explosion on destruction**<br>
The Nexus creates a small explosion when a player destroys it.

* **HUD**<br>
The tab-list header shows the current HP of all live Nexuses.

![](https://github.com/nikorr0/NexusCoreTeams/blob/main/screenshots/Nexuses_in_the_tab_list_1.png)

* **Damage alerts** and **Elimination message**<br>
A team receives a chat notice every *y*% (configured via `notify-percent`). When the last player on a team is eliminated, all players receive the elimination message.

![](https://github.com/nikorr0/NexusCoreTeams/blob/main/screenshots/Notifications_and_messages.png)

* **Plugin loading** and **Server restarting**<br>
On load, the plugin clears any End Crystals within a 2x3x2 area around each Nexus to prevent duplicates.

* **Hot reload**<br>
Reload the plugin’s configuration without restarting the server using `/nexus reload`.

---

## Plugin сommands

| Command | Description|
|---------|------------|
| `/nexus set <team> <hp>` | Place a Nexus for the specified team at your location. |
| `/nexus hp <team> <amount>` | Manually adjust the current HP of the team’s Nexus. |
| `/nexus clear <team>` | Remove the Nexus crystal(s) from the world for all teams or a specific team. |
| `/nexus enable/disable` | Enable or disable the NexusCoreTeams plugin without restarting the server. |
| `/nexus info <team>` | Displays the HP of all teams’ Nexuses or a specific team’s Nexus. |
| `/nexus reload` | Reload `config.yml` to apply configuration changes. |

---

## How to use

Create a scoreboard team and assign it a color using vanilla Minecraft commands:
```
/team add Red
/team modify Red color red
```

Join the created team:
```/team join Red <nickname>```

Then place the Nexus using the plugin command:
```/nexus set Red <hp>```

---

## Configuration (`plugins/NexusCoreTeams/config.yml`)

```yml
# Enable/disable the plugin
enabled: true

# The HP that will be assigned to the nexus
# if the user did not specify the value with the </nexus set> command
default-nexus-hp: 30

# The percentage of damage, when each of which is reached,
# the team will receive a notification.
# For example, 20 - notify at 20%, 40%, 60%, 80% damage.
notify-percent: 20

# Time to block the next damage to the Nexus in seconds
hit-cooldown: 0.5

# If true, the Nexus loses HP equal to the player’s weapon attack value
# If false, every hit always deals exactly 1 HP
weapon-damage-enabled: true

# If true, shooting arrows at the Nexus will deal damage
# If false, arrows have no effect on the Nexus
arrow-damage-enabled: true
```
