package mezz.jei.gui.input.handlers;

import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.util.CheatUtil;
import mezz.jei.gui.util.CommandUtil;
import mezz.jei.gui.util.GiveAmount;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class CheatInputHandler implements IUserInputHandler {
    private final IRecipeFocusSource showsRecipeFocuses;
    private final IClientToggleState toggleState;
    private final CommandUtil commandUtil;
    private final CheatUtil cheatUtil;

    public CheatInputHandler(
        IRecipeFocusSource showsRecipeFocuses,
        IClientToggleState toggleState,
        IClientConfig clientConfig,
        IConnectionToServer serverConnection,
        CheatUtil cheatUtil
    ) {
        this.showsRecipeFocuses = showsRecipeFocuses;
        this.toggleState = toggleState;
        this.cheatUtil = cheatUtil;
        this.commandUtil = new CommandUtil(clientConfig, serverConnection);
    }

    @Override
    public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
        if (!toggleState.isCheatItemsEnabled() ||
            screen instanceof IRecipesGui
        ) {
            return Optional.empty();
        }

        if (input.is(keyBindings.getCheatItemStack())) {
            return handleGive(input, GiveAmount.MAX);
        }

        if (input.is(keyBindings.getCheatOneItem())) {
            return handleGive(input, GiveAmount.ONE);
        }

        return Optional.empty();
    }

    private Optional<IUserInputHandler> handleGive(UserInput input, GiveAmount giveAmount) {
        return showsRecipeFocuses.getIngredientUnderMouse(input.getMouseX(), input.getMouseY())
            .findFirst()
            .map(clicked -> {
                if (!input.isSimulate()) {
                    ItemStack itemStack = cheatUtil.getCheatItemStack(clicked);
                    if (!itemStack.isEmpty()) {
                        commandUtil.giveStack(itemStack, giveAmount);
                    }
                }
                ImmutableRect2i area = clicked.getArea();
                return LimitedAreaInputHandler.create(this, area);
            });
    }

}
