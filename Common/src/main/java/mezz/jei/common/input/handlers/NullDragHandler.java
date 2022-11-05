package mezz.jei.common.input.handlers;

import mezz.jei.common.input.IDragHandler;
import mezz.jei.common.input.UserInput;
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
