package org.BsXinQin.kinswathe.client.mixin.host;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.client.gui.RoundTextRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.MutableText;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;

@Mixin(value = RoundTextRenderer.class, priority = 500)
public class EndTextRendererMixin {

    @Unique private static final ThreadLocal<Integer> CIVILIAN_TOTAL = new ThreadLocal<>();
    @Unique private static final ThreadLocal<Integer> NEUTRAL_TOTAL = new ThreadLocal<>();
    @Unique private static boolean STUPID_EXPRESS_RENDERER;
    @Unique private static final ThreadLocal<Boolean> SKIP_RENDERER = ThreadLocal.withInitial(() -> false);

    static {
        if (FabricLoader.getInstance().isModLoaded("stupid_express")) {
            try {
                Class.forName("pro.fazeclan.river.stupid_express.cca.CustomWinnerComponent");
                STUPID_EXPRESS_RENDERER = true;
            } catch (Exception exception) {
                STUPID_EXPRESS_RENDERER = false;
            }
        }
    }

    @Inject(method = "renderHud", at = @At("HEAD"))
    private static void onRenderStart(@NotNull TextRenderer renderer, @NotNull ClientPlayerEntity player, @NotNull DrawContext context, CallbackInfo ci) {
        if (!ConfigWorldComponent.KEY.get(player.getWorld()).EnableNeutralAnnouncement || !STUPID_EXPRESS_RENDERER) {
            SKIP_RENDERER.set(false);
            return;
        }
        boolean stupidexpressCustomWin = isStupidExpressCustomWin(player);
        if (stupidexpressCustomWin) {
            SKIP_RENDERER.set(true);
            return;
        }
        SKIP_RENDERER.set(false);
        CIVILIAN_TOTAL.remove();
        NEUTRAL_TOTAL.remove();
    }

    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/GameRoundEndComponent;getPlayers()Ljava/util/List;", ordinal = 2))
    private static void renderNeutralTitle(@NotNull TextRenderer renderer, @NotNull ClientPlayerEntity player, @NotNull DrawContext context, CallbackInfo ci, @NotNull @Local(name = "roundEnd") GameRoundEndComponent roundEnd) {
        if (!ConfigWorldComponent.KEY.get(player.getWorld()).EnableNeutralAnnouncement || SKIP_RENDERER.get()) return;
        int civilianTotal = 0;
        int neutralTotal = 0;
        for (var entry : roundEnd.getPlayers()) {
            if (entry.role() == RoleAnnouncementTexts.CIVILIAN) civilianTotal += 1;
            if (entry.role() == KinsWatheRoles.NEUTRAL_TEXT) neutralTotal += 1;
        }
        CIVILIAN_TOTAL.set(civilianTotal);
        NEUTRAL_TOTAL.set(0);
        if (neutralTotal > 0) {
            MutableText titleText = KinsWatheRoles.NEUTRAL_TEXT.titleText.copy();
            context.drawTextWithShadow(renderer, titleText, -renderer.getWidth(titleText) / 2 - 60, 14 + 16 + 24 * ((civilianTotal + 3) / 4), 0xFFFFFF);
        }
    }

    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/GameRoundEndComponent$RoundEndData;role()Ldev/doctor4t/wathe/client/gui/RoleAnnouncementTexts$RoleAnnouncementText;", ordinal = 1))
    private static void renderNeutralPlayers(@NotNull TextRenderer renderer, @NotNull ClientPlayerEntity player, @NotNull DrawContext context, CallbackInfo ci, @NotNull @Local(name = "entry") GameRoundEndComponent.RoundEndData entry) {
        if (!ConfigWorldComponent.KEY.get(player.getWorld()).EnableNeutralAnnouncement || SKIP_RENDERER.get()) return;
        if (entry.role() == KinsWatheRoles.NEUTRAL_TEXT) {
            int civilianTotal = CIVILIAN_TOTAL.get();
            int neutralTotal = NEUTRAL_TOTAL.get();
            context.getMatrices().translate(0, 8 + ((civilianTotal + 3) / 4) * 12, 0);
            context.getMatrices().translate(-60 + (neutralTotal % 4) * 12, 14 + (neutralTotal / 4) * 12, 0);
            NEUTRAL_TOTAL.set(++neutralTotal);
        }
    }

    @Inject(method = "renderHud", at = @At("RETURN"))
    private static void onRenderEnd(@NotNull TextRenderer renderer, @NotNull ClientPlayerEntity player, @NotNull DrawContext context, CallbackInfo ci) {
        if (!ConfigWorldComponent.KEY.get(player.getWorld()).EnableNeutralAnnouncement) return;
        SKIP_RENDERER.remove();
        CIVILIAN_TOTAL.remove();
        NEUTRAL_TOTAL.remove();
    }

    @Unique
    private static boolean isStupidExpressCustomWin(@NotNull ClientPlayerEntity player) {
        if (ConfigWorldComponent.KEY.get(player.getWorld()).EnableNeutralAnnouncement) {
            if (FabricLoader.getInstance().isModLoaded("stupid_express")) {
                try {
                    Class<?> componentClass = Class.forName("pro.fazeclan.river.stupid_express.cca.CustomWinnerComponent");
                    var keyField = componentClass.getField("KEY");
                    var key = keyField.get(null);
                    var getMethod = key.getClass().getMethod("get", Object.class);
                    var component = getMethod.invoke(key, player.getWorld());
                    var hasCustomWinnerMethod = componentClass.getMethod("hasCustomWinner");
                    return (boolean) hasCustomWinnerMethod.invoke(component);
                } catch (NoSuchFieldException | ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException ignored) {}
            }
        }
        return false;
    }
}