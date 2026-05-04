package org.BsXinQin.kinswathe.mixin.host;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRoundEndComponent.class)
public class GameRoundEndMixin {

    @Shadow @Final @NotNull private World world;

    @ModifyArg(method = "setRoundEndData", at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/GameRoundEndComponent$RoundEndData;<init>(Lcom/mojang/authlib/GameProfile;Ldev/doctor4t/wathe/client/gui/RoleAnnouncementTexts$RoleAnnouncementText;Z)V"))
    private static RoleAnnouncementTexts.RoleAnnouncementText setNeutralRoundEndData(@NotNull RoleAnnouncementTexts.RoleAnnouncementText original, @NotNull @Local(name = "game") GameWorldComponent gameWorld, @NotNull @Local(name = "player") ServerPlayerEntity player) {
        if (KinsWatheConfig.HANDLER.instance().EnableNeutralAnnouncement) {
            if (gameWorld.getRole(player) != null && !gameWorld.isInnocent(player) && !gameWorld.canUseKillerFeatures(player)) {
                return KinsWatheRoles.NEUTRAL_TEXT;
            }
        }
        return original;
    }
}