package mezz.jei.common.startup;

import mezz.jei.common.gui.GuiEventHandler;
import mezz.jei.common.input.ClientInputHandler;

public record JeiEventHandlers(
    GuiEventHandler guiEventHandler,
    ClientInputHandler clientInputHandler
) {
}
