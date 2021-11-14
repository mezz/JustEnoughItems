package mezz.jei.gui.overlay;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.click.MouseClickState;
import mezz.jei.util.CommandUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class CheatMouseHandler implements IMouseHandler {
    private final IShowsRecipeFocuses showsRecipeFocuses;
    private final IWorldConfig worldConfig;
    private final IClientConfig clientConfig;

    public CheatMouseHandler(IShowsRecipeFocuses showsRecipeFocuses, IWorldConfig worldConfig, IClientConfig clientConfig) {
        this.showsRecipeFocuses = showsRecipeFocuses;
        this.worldConfig = worldConfig;
        this.clientConfig = clientConfig;
    }

    @Nullable
    @Override
    public IMouseHandler handleClick(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
        if (!worldConfig.isCheatItemsEnabled()) {
            return null;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return null;
        }
        Screen currentScreen = minecraft.screen;
        if (currentScreen == null || currentScreen instanceof RecipesGui) {
            return null;
        }

        InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
        if (mouseButton == 0 || mouseButton == 1 || minecraft.options.keyPickItem.isActiveAndMatches(input)) {
            IClickedIngredient<?> clicked = showsRecipeFocuses.getIngredientUnderMouse(mouseX, mouseY);
            if (clicked != null) {
                if (!clickState.isSimulate()) {
                    ItemStack itemStack = clicked.getCheatItemStack();
                    if (!itemStack.isEmpty()) {
                        CommandUtil.giveStack(itemStack, input, clientConfig);
                    }
                }
                return this;
            }
        }
        return null;
    }

}
