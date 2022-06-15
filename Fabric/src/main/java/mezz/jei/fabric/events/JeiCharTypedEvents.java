package mezz.jei.fabric.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.components.events.GuiEventListener;

public class JeiCharTypedEvents {
    public static final Event<BeforeCharTyped> BEFORE_CHAR_TYPED =
        EventFactory.createArrayBacked(BeforeCharTyped.class, callbacks -> (guiEventListener, codepoint, modifiers) -> {
            for (BeforeCharTyped callback : callbacks) {
                if (callback.beforeCharTyped(guiEventListener, codepoint, modifiers)) {
                    return true;
                }
            }
            return false;
        });

    public static final Event<AfterCharTyped> AFTER_CHAR_TYPED =
        EventFactory.createArrayBacked(AfterCharTyped.class, callbacks -> (guiEventListener, codepoint, modifiers) -> {
            for (AfterCharTyped callback : callbacks) {
                if (callback.afterCharTyped(guiEventListener, codepoint, modifiers)) {
                    return true;
                }
            }
            return false;
        });

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface BeforeCharTyped {
        boolean beforeCharTyped(GuiEventListener guiEventListener, char codepoint, int modifiers);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface AfterCharTyped {
        boolean afterCharTyped(GuiEventListener guiEventListener, char codepoint, int modifiers);
    }
}
