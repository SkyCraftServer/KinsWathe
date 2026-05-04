package org.BsXinQin.kinswathe.mixin.host;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import dev.doctor4t.wathe.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(MurderGameMode.class)
public class NeutralAnnouncementMixin {

    @Redirect(method = "initializeGame", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking;send(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/packet/CustomPayload;)V"))
    private static void neutralTitle(@NotNull ServerPlayerEntity player, CustomPayload payload, @NotNull @Local(name = "gameWorldComponent") GameWorldComponent gameWorld, @NotNull @Local(name = "players") List<ServerPlayerEntity> players) {
        if (!KinsWatheConfig.HANDLER.instance().EnableNeutralAnnouncement) return;
        int killerCount = gameWorld.getAllWithRole(WatheRoles.KILLER).size();
        if (gameWorld.getRole(player) != null) {
            ServerPlayNetworking.send(player, new AnnounceWelcomePayload(RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(gameWorld.isRole(player, WatheRoles.KILLER) ? RoleAnnouncementTexts.KILLER : gameWorld.isRole(player, WatheRoles.VIGILANTE) ? RoleAnnouncementTexts.VIGILANTE : !gameWorld.isInnocent(player) && !gameWorld.canUseKillerFeatures(player) ? KinsWatheRoles.NEUTRAL_TEXT : RoleAnnouncementTexts.CIVILIAN), killerCount, players.size() - killerCount));
        }
    }
}