package org.BsXinQin.kinswathe.mixin.roles.cook;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.BsXinQin.kinswathe.KinsWatheShops;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerShopComponent.class)
public abstract class CookShopMixin {

    @Unique public int price;
    @Shadow public int balance;
    @Unique @NotNull public Item item;
    @Shadow public abstract void sync();
    @Shadow @Final @NotNull private PlayerEntity player;

    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
    void tryBuy(int index, @NotNull CallbackInfo ci) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.getWorld());
        if (gameWorld.isRole(this.player, KinsWatheRoles.COOK)) {
            boolean enablePan = ConfigWorldComponent.KEY.get(this.player.getWorld()).EnableCookPanInShop;
            Item selectedItem;
            int selectedPrice;
            switch (index) {
                case 0:
                    if (!enablePan) return;
                    selectedItem = KinsWatheItems.PAN;
                    selectedPrice = 250;
                    break;
                case 1:
                    selectedItem = Items.COOKED_BEEF;
                    selectedPrice = 75;
                    break;
                case 2:
                    selectedItem = Items.COOKED_CHICKEN;
                    selectedPrice = 75;
                    break;
                case 3:
                    selectedItem = Items.COOKED_PORKCHOP;
                    selectedPrice = 75;
                    break;
                default:
                    return;
            }
            if (index < 0 || index > 3) return;
            this.item = selectedItem;
            this.price = selectedPrice;
            if (KinsWatheShops.handlePurchase(this.player, this.balance, this.item, this.price)) {
                this.balance -= this.price;
                this.sync();
            }
            ci.cancel();
        }
    }
}