package org.spaceserve.tablist.cloak.mixin

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.network.ServerPlayerInteractionManager
import net.minecraft.world.GameMode
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ServerPlayerInteractionManager::class)
abstract class ServerPlayerInteractionManagerMixin {
    @Shadow @Final
    lateinit var player: ServerPlayerEntity

    @Inject(
        method = ["setGameMode"],
        at = [At("TAIL")],
    )
    private fun updateTablist(gameMode: GameMode, previousGameMode: GameMode?, ci: CallbackInfo) {
        if (previousGameMode == GameMode.SPECTATOR || gameMode == GameMode.SPECTATOR) {
            val spectators = mutableListOf<ServerPlayerEntity>()

            player.server.playerManager.playerList
                .filter { p -> p.isSpectator }
                .forEach { p -> spectators.add(p) }

            player.networkHandler.sendPacket(
                PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_GAME_MODE, spectators)
            )
        }
    }
}