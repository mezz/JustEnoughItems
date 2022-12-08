package mezz.jei.gui.input.handlers;

import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public class NullDragHandler implements IDragHandler {
    public static final NullDragHandler INSTANCE = new NullDragHandler();

    private NullDragHandler() {

    }

    @Override
    public Optional<IDragHandler> handleDragStart(Screen screen, UserInput input) {
        return Optional.empty();
    }

    @Override
    public boolean handleDragComplete(Screen screen, UserInput input) {
        return false;
    }
}
