package mezz.jei.input.mouse.handlers;

import mezz.jei.config.IClientConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.util.CheatUtil;
import mezz.jei.util.CommandUtil;
import mezz.jei.util.GiveAmount;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class CheatInputHandler implements IUserInputHandler {
    private final IRecipeFocusSource showsRecipeFocuses;
    private final IWorldConfig worldConfig;
    private final IClientConfig clientConfig;

    public CheatInputHandler(IRecipeFocusSource showsRecipeFocuses, IWorldConfig worldConfig, IClientConfig clientConfig) {
        this.showsRecipeFocuses = showsRecipeFocuses;
        this.worldConfig = worldConfig;
        this.clientConfig = clientConfig;
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
                        CommandUtil.giveStack(itemStack, giveAmount, clientConfig);
                    }
                }
                return LimitedAreaInputHandler.create(this, clicked.getArea());
            });
    }

}
