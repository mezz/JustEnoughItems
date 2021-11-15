package mezz.jei.gui.overlay;

import mezz.jei.config.IClientConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.util.CommandUtil;
import mezz.jei.util.GiveAmount;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class CheatUserInputHandler implements IUserInputHandler {
    private final IRecipeFocusSource showsRecipeFocuses;
    private final IWorldConfig worldConfig;
    private final IClientConfig clientConfig;

    public CheatUserInputHandler(IRecipeFocusSource showsRecipeFocuses, IWorldConfig worldConfig, IClientConfig clientConfig) {
        this.showsRecipeFocuses = showsRecipeFocuses;
        this.worldConfig = worldConfig;
        this.clientConfig = clientConfig;
    }

    @Nullable
    @Override
    public IUserInputHandler handleUserInput(Screen screen, UserInput input) {
        if (!worldConfig.isCheatItemsEnabled() ||
            screen instanceof RecipesGui
        ) {
            return null;
        }

        GiveAmount giveAmount = GiveAmount.getGiveAmount(input);
        if (giveAmount == null) {
            return null;
        }

        IClickedIngredient<?> clicked = showsRecipeFocuses.getIngredientUnderMouse(input.getMouseX(), input.getMouseY());
        if (clicked == null) {
            return null;
        }

        if (!input.isSimulate()) {
            ItemStack itemStack = clicked.getCheatItemStack();
            if (!itemStack.isEmpty()) {
                CommandUtil.giveStack(itemStack, giveAmount, clientConfig);
            }
        }
        return this;
    }

}
