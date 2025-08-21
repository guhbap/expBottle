package ru.guhbap.expbottle

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.pow
import kotlin.math.roundToInt

class ExpBottle : JavaPlugin(), Listener {

    private var expCost: Int = 10
    private var removeBottle: Boolean = true
    override fun onEnable() {
        saveDefaultConfig()
        expCost = config.getInt("exp-cost", 10)
        removeBottle = config.getBoolean("exp-cost", true)
        server.pluginManager.registerEvents(this, this)
        logger.info("ExpBottlePlugin enabled!")
    }

    override fun onDisable() {
        logger.info("ExpBottlePlugin disabled!")
    }
    @EventHandler
    fun onPlayerUse(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return

        if (item.type == Material.GLASS_BOTTLE && player.isSneaking && (event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.RIGHT_CLICK_AIR)  ) {

            val playerExp = getPlayerExp(player)
            if (playerExp < expCost) {
                player.sendMessage("§cНедостаточно опыта!")
                return
            }

            // Отнимаем 10 опыта
            setPlayerExp(player, playerExp-expCost)
//            changePlayerExp(player, -expCost)

            // Убираем 1 пустую бутылку
            item.amount -= 1

            // Даём пузырёк опыта
            if (removeBottle){
                player.inventory.addItem(ItemStack(Material.EXPERIENCE_BOTTLE, 1))
            }
        }
    }

    // thx DOGC_Kyle for this calculate methods (https://www.spigotmc.org/threads/how-to-get-players-exp-points.239171/)

    // Calculate amount of EXP needed to level up
    fun getExpToLevelUp(level: Int): Int {
        return if (level <= 15) {
            2 * level + 7
        } else if (level <= 30) {
            5 * level - 38
        } else {
            9 * level - 158
        }
    }

    // Calculate total experience up to a level
    fun getExpAtLevel(level: Int): Int {
        return if (level <= 16) {
            (level.toDouble().pow(2.0) + 6 * level).toInt()
        } else if (level <= 31) {
            (2.5 * level.toDouble().pow(2.0) - 40.5 * level + 360.0).toInt()
        } else {
            (4.5 * level.toDouble().pow(2.0) - 162.5 * level + 2220.0).toInt()
        }
    }

    // Calculate player's current EXP amount
    fun getPlayerExp(player: Player): Int {
        var exp = 0
        val level = player.level


        // Get the amount of XP in past levels
        exp += getExpAtLevel(level)


        // Get amount of XP towards next level
        exp += (getExpToLevelUp(level) * player.exp).roundToInt()

        return exp
    }

    fun setPlayerExp(player: Player, exp:Int){
        player.exp = 0f
        player.level = 0
        player.giveExp(exp)
    }
    // Give or take EXP
    fun changePlayerExp(player: Player, exp: Int): Int {
        // Get player's current exp
        val currentExp = getPlayerExp(player)


        // Reset player's current exp to 0
        player.exp = 0f
        player.level = 0


        // Give the player their exp back, with the difference
        val newExp = currentExp + exp
        player.giveExp(newExp)


        // Return the player's new exp amount
        return newExp
    }
}