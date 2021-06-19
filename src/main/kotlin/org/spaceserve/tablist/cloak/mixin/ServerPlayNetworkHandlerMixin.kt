package org.spaceserve.tablist.cloak.mixin

import net.minecraft.network.Packet
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode
import org.spaceserve.tablist.cloak.Common
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ServerPlayNetworkHandler::class)
abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    lateinit var player: ServerPlayerEntity

    @Inject(
        method = ["sendPacket"],
        at = [At("HEAD")],
        cancellable = true,
    )
    private fun cloakSpectators(packet: Packet<*>, ci: CallbackInfo) {
        if (
            !Common.CONFIG.enableCloaking ||                             // Skip if cloaking is disabled
            packet !is PlayerListS2CPacket ||                            // Skip if not PlayerList packet
            packet.action != PlayerListS2CPacket.Action.UPDATE_GAME_MODE // Skip if not gamemode update packet
        ) { return }

        // Cloak if hide from all, or player is not op/in spec mode
        if (Common.CONFIG.hideFromOperatorsAndSpectators || !player.isSpectator || !player.hasPermissionLevel(2)) {
            val newEntries = mutableListOf<PlayerListS2CPacket.Entry>()

            packet.entries.forEach {
                // If self or in survival, dont change it
                if (it.profile!!.id == player.uuid || it.gameMode == GameMode.SURVIVAL) {
                    newEntries.add(it)
                } else {
                    newEntries.add(
                        PlayerListS2CPacket.Entry(
                            it.profile,
                            it.latency,
                            GameMode.SURVIVAL,
                            it.displayName,
                        )
                    )
                }
            }

            val newPacket = PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_GAME_MODE, player)
            newPacket.entries.removeFirst() // An entry will be created for [player], unwanted, provided by [newEntries]
            newPacket.entries.addAll(newEntries)
            player.networkHandler.sendPacket(newPacket, null)

            ci.cancel()
        }
    }
}
