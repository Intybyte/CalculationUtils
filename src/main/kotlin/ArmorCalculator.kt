package me.vaan.playerutils

import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import java.util.*
import kotlin.math.floor

// http://www.minecraftwiki.net/wiki/Armor#Armor_enchantment_effect_calculation

/**
 * Assist calculations for armor
 * @author Vaan1310/Intybyte
 */
class ArmorCalculator(private var magicValue: Double = 0.04) {

    private val armorReductionMap: EnumMap<Material, Double> = EnumMap(org.bukkit.Material::class.java)
    private val random: Random = Random()

    init {
        createMap()
    }

    private fun createMap() {
        armorReductionMap[TURTLE_HELMET] = 2 * magicValue

        armorReductionMap[LEATHER_HELMET] = 1 * magicValue
        armorReductionMap[GOLDEN_HELMET] = 2 * magicValue
        armorReductionMap[CHAINMAIL_HELMET] = 2 * magicValue
        armorReductionMap[IRON_HELMET] = 2 * magicValue
        armorReductionMap[DIAMOND_HELMET] = 3 * magicValue
        armorReductionMap[NETHERITE_HELMET] = 3 * magicValue

        armorReductionMap[LEATHER_BOOTS] = 1 * magicValue
        armorReductionMap[GOLDEN_BOOTS] = 1 * magicValue
        armorReductionMap[CHAINMAIL_BOOTS] = 1 * magicValue
        armorReductionMap[IRON_BOOTS] = 2 * magicValue
        armorReductionMap[DIAMOND_BOOTS] = 3 * magicValue
        armorReductionMap[NETHERITE_BOOTS] = 3 * magicValue

        armorReductionMap[LEATHER_LEGGINGS] = 2 * magicValue
        armorReductionMap[GOLDEN_LEGGINGS] = 3 * magicValue
        armorReductionMap[CHAINMAIL_LEGGINGS] = 4 * magicValue
        armorReductionMap[IRON_LEGGINGS] = 5 * magicValue
        armorReductionMap[DIAMOND_LEGGINGS] = 6 * magicValue
        armorReductionMap[NETHERITE_LEGGINGS] = 6 * magicValue

        armorReductionMap[LEATHER_CHESTPLATE] = 3 * magicValue
        armorReductionMap[GOLDEN_CHESTPLATE] = 5 * magicValue
        armorReductionMap[CHAINMAIL_CHESTPLATE] = 5 * magicValue
        armorReductionMap[IRON_CHESTPLATE] = 6 * magicValue
        armorReductionMap[DIAMOND_CHESTPLATE] = 8 * magicValue
        armorReductionMap[NETHERITE_CHESTPLATE] = 8 * magicValue
    }

    fun getMagicValue(): Double {
        return magicValue
    }

    fun setMagicValue(magick: Double) {
        magicValue = magick
    }

    fun getArmorDamageReduced(entity: HumanEntity?): Double {
        entity ?: return 0.0

        val inv = entity.inventory
        if (inv.isEmpty) return 0.0

        var totalReduction = getArmorDamageReduced(inv.helmet, inv.chestplate, inv.leggings, inv.boots)

        // 100% protection, this would make you immune
        if(totalReduction > 1)
            totalReduction = 1.0

        return totalReduction
    }

    private fun getArmorDamageReduced(vararg equip: ItemStack?) : Double {
        return equip.sumOf { getArmorPieceReduction(it) }
    }

    fun getArmorPieceReduction(armorPiece: ItemStack?): Double {
        armorPiece ?: return 0.0

        val material = armorPiece.type
        return armorReductionMap.getOrDefault(material, 0.0)
    }

    fun getPlayerEnchantProtection(entity: HumanEntity?, enchantment: Enchantment) : Double {
        //http://www.minecraftwiki.net/wiki/Armor#Armor_enchantment_effect_calculation

        entity ?: return 0.0

        val inv = entity.inventory

        val reduction = getItemEnchantProtection(enchantment, inv.helmet, inv.chestplate, inv.leggings, inv.boots)

        return capItemEnchantProtection(reduction)
    }

    private fun capItemEnchantProtection(startReduction : Double) : Double {
        var reduction = startReduction
        //cap it to 25
        if (reduction > 25) reduction = 25.0

        //give it some randomness
        reduction *= (random.nextDouble() / 2 + 0.5)

        //cap it to 20
        if (reduction > 20) reduction = 20.0

        //1 point is 4%
        return reduction*4/100
    }

    private fun getItemEnchantProtection(enchantment: Enchantment, vararg equip: ItemStack?) : Double {
        return equip.sumOf { getItemEnchantProtection(it, enchantment) }
    }

    fun getItemEnchantProtection(item: ItemStack?, special: Enchantment): Double {
        var reduction = 0.0

        item ?: return reduction

        var lvl = item.getEnchantmentLevel(special)
        if (lvl > 0)
            reduction += floor((6 + lvl * lvl) * 1.5 / 3)

        lvl = item.getEnchantmentLevel(Enchantment.PROTECTION)
        if (lvl > 0)
            reduction += floor((6 + lvl * lvl) * 0.75 / 3)

        return reduction
    }

    fun getDirectHitReduction(human: HumanEntity, armorPiercing: Double) : Double {
        val overallPiercing = armorPiercing + 1
        return (1 - getArmorDamageReduced(human) / overallPiercing) * (1 - getPlayerEnchantProtection(human, Enchantment.PROJECTILE_PROTECTION) / overallPiercing)
    }

    fun getExplosionHitReduction(human: HumanEntity, armorPiercing: Double) : Double {
        val overallPiercing = armorPiercing + 1
        return (1 - getArmorDamageReduced(human) / overallPiercing) * (1 - getPlayerEnchantProtection(human, Enchantment.BLAST_PROTECTION))
    }

    /**
     * reduces the durability of the player's armor
     * @param entity - the affected human player
     */
    fun reduceArmorDurability(entity: HumanEntity) {
        val inv = entity.inventory

        for(item in inv.armorContents) {
            if (item == null) {
                continue
            }

            val lvl = item.getEnchantmentLevel(Enchantment.UNBREAKING)
            //chance of breaking in 0-1
            val breakingChance = 0.6+0.4/(lvl+1)

            if (random.nextDouble() < breakingChance)
            {
                val damageableMeta = item.itemMeta as Damageable
                damageableMeta.damage += 1
            }
        }
    }
}
