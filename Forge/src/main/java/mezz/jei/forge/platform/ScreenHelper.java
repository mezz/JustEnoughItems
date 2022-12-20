package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.Optional;

public class ScreenHelper implements IPlatformScreenHelper {
    @Override
    public Optional<Slot> getSlotUnderMouse(AbstractContainerScreen<?> containerScreen) {
        Slot slot = containerScreen.getSlotUnderMouse();
        return Optional.ofNullable(slot);
    }

    @Override
    public int getGuiLeft(AbstractContainerScreen<?> containerScreen) {
        return containerScreen.getGuiLeft();
    }

    @Override
    public int getGuiTop(AbstractContainerScreen<?> containerScreen) {
        return containerScreen.getGuiTop();
    }

    @Override
    public int getXSize(AbstractContainerScreen<?> containerScreen) {
        return containerScreen.getXSize();
    }

    @Override
    public int getYSize(AbstractContainerScreen<?> containerScreen) {
        return containerScreen.getYSize();
    }

    @Override
    public ImmutableRect2i getBookArea(RecipeUpdateListener containerScreen) {
        RecipeBookComponent guiRecipeBook = containerScreen.getRecipeBookComponent();
        if (guiRecipeBook.isVisible()) {
            int i = (guiRecipeBook.width - 147) / 2 - guiRecipeBook.xOffset;
            int j = (guiRecipeBook.height - 166) / 2;
            return new ImmutableRect2i(i, j, 147, 166);
        }
        return ImmutableRect2i.EMPTY;
    }

    @Override
    public List<RecipeBookTabButton> getTabButtons(RecipeBookComponent recipeBookComponent) {
        return recipeBookComponent.tabButtons;
    }

    @Override
    public void setFocused(AbstractWidget widget, boolean value) {
        widget.setFocused(value);
    }
}
