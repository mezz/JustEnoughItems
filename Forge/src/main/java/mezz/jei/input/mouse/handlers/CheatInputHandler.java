package mezz.jei.input.mouse.handlers;

import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.util.CheatUtil;
import mezz.jei.util.CommandUtil;
import mezz.jei.common.util.GiveAmount;
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
    public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input) {
        if (!worldConfig.isCheatItemsEnabled() ||
            screen instanceof RecipesGui
        ) {
            return Optional.empty();
        }

        if (input.is(KeyBindings.cheatItemStack)) {
            return handleGive(input, GiveAmount.MAX);
        }

        if (input.is(KeyBindings.cheatOneItem)) {
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
