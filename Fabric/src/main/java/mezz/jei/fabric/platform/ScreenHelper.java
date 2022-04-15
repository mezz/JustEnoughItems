package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.fabric.mixin.AbstractContainerScreenAccess;
import mezz.jei.fabric.mixin.RecipeBookComponentAccess;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScreenHelper implements IPlatformScreenHelper {
    @Override
    public @Nullable Slot getSlotUnderMouse(AbstractContainerScreen<?> containerScreen) {
        var access = (AbstractContainerScreenAccess) containerScreen;
        return access.getHoveredSlot();
    }

    @Override
    public int getGuiLeft(AbstractContainerScreen<?> containerScreen) {
        var access = (AbstractContainerScreenAccess) containerScreen;
        return access.getLeftPos();
    }

    @Override
    public int getGuiTop(AbstractContainerScreen<?> containerScreen) {
        var access = (AbstractContainerScreenAccess) containerScreen;
        return access.getTopPos();
    }

    @Override
    public int getXSize(AbstractContainerScreen<?> containerScreen) {
        var access = (AbstractContainerScreenAccess) containerScreen;
        return access.getImageWidth();
    }

    @Override
    public int getYSize(AbstractContainerScreen<?> containerScreen) {
        var access = (AbstractContainerScreenAccess) containerScreen;
        return access.getImageHeight();
    }

    @Override
    public ImmutableRect2i getBookArea(RecipeUpdateListener containerScreen) {
        RecipeBookComponent guiRecipeBook = containerScreen.getRecipeBookComponent();
        var access = (RecipeBookComponentAccess) guiRecipeBook;
        if (guiRecipeBook.isVisible()) {
            int i = (access.getWidth() - 147) / 2 - access.getXOffset();
            int j = (access.getHeight() - 166) / 2;
            return new ImmutableRect2i(i, j, 147, 166);
        }
        return ImmutableRect2i.EMPTY;
    }

    @Override
    public List<RecipeBookTabButton> getTabButtons(RecipeBookComponent recipeBookComponent) {
        var access = (RecipeBookComponentAccess) recipeBookComponent;
        return access.getTabButtons();
    }
}
