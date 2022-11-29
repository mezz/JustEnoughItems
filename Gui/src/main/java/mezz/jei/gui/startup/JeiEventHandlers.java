package mezz.jei.gui.startup;

import mezz.jei.gui.events.GuiEventHandler;
import mezz.jei.common.input.ClientInputHandler;

public record JeiEventHandlers(
    GuiEventHandler guiEventHandler,
    ClientInputHandler clientInputHandler
) {
}
