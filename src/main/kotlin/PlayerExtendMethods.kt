@file:JvmName("PlayerUtils")

package me.vaan.playerutils

import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

val OfflinePlayer.head : ItemStack get() {
    val skull = ItemStack(Material.PLAYER_HEAD)
    val skullMeta = skull.itemMeta as SkullMeta
    skullMeta.setOwningPlayer(player)
    skull.itemMeta = skullMeta
    return skull
}