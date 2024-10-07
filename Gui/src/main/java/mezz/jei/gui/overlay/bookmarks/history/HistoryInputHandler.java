package mezz.jei.gui.overlay.bookmarks.history;

import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.FocusInputHandler;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public class HistoryInputHandler implements IUserInputHandler {

    private final HistoryList historyList;
    private final CombinedRecipeFocusSource focusSource;
    private final FocusInputHandler focusInputHandler;

    public HistoryInputHandler(HistoryList historyList, CombinedRecipeFocusSource focusSource, FocusInputHandler focusInputHandler) {
        this.historyList = historyList;
        this.focusSource = focusSource;
        this.focusInputHandler = focusInputHandler;
    }

    @Override
    public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
        Optional<IUserInputHandler> result = focusInputHandler.handleUserInput(screen, input, keyBindings);
        if ((input.is(keyBindings.getShowRecipe()) || input.is(keyBindings.getShowUses())) && result.isPresent()) {
            focusSource.getIngredientUnderMouse(input, keyBindings)
                    .filter(clicked -> clicked.getElement().isVisible())
                    .findFirst()
                    .ifPresent(clicked -> historyList.add(clicked.getElement()));
        }
        return result;
    }
}
