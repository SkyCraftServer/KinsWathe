package org.BsXinQin.kinswathe;

import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KinsWatheAnnouncementText extends RoleAnnouncementTexts.RoleAnnouncementText {

    public KinsWatheAnnouncementText() {super("neutral", 0xCC6600);}

    public @Nullable Text getEndText(@NotNull GameFunctions.@NotNull WinStatus status, @NotNull Text winner) {
        return switch (status) {
            case NONE -> null;
            case PASSENGERS, TIME -> RoleAnnouncementTexts.CIVILIAN.winText;
            case KILLERS -> RoleAnnouncementTexts.KILLER.winText;
            case LOOSE_END -> super.getEndText(status, winner);
        };
    }
}
