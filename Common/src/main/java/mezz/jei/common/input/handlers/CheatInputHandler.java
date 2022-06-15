package mezz.jei.common.input.handlers;

import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.input.IRecipeFocusSource;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.CheatUtil;
import mezz.jei.common.util.CommandUtil;
import mezz.jei.common.util.GiveAmount;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IWorldConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class CheatInputHandler implements IUserInputHandler {
    private final IRecipeFocusSource showsRecipeFocuses;
    private final IWorldConfig worldConfig;
    private final CommandUtil commandUtil;

    public CheatInputHandler(
        IRecipeFocusSource showsRecipeFocuses,
        IWorldConfig worldConfig,
        IClientConfig clientConfig,
        IConnectionToServer serverConnection
    ) {
        this.showsRecipeFocuses = showsRecipeFocuses;
        this.worldConfig = worldConfig;
        this.commandUtil = new CommandUtil(clientConfig, serverConnection);
    }

    @Override
    public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IKeyBindings keyBindings) {
        if (!worldConfig.isCheatItemsEnabled() ||
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
                    ItemStack itemStack = CheatUtil.getCheatItemStack(clicked);
                    if (!itemStack.isEmpty()) {
                        commandUtil.giveStack(itemStack, giveAmount);
                    }
                }
                return LimitedAreaInputHandler.create(this, clicked.getArea());
            });
    }

}
