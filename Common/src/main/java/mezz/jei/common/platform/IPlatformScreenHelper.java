package mezz.jei.common.platform;

import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.Optional;

public interface IPlatformScreenHelper {
	Optional<Slot> getHoveredSlot(AbstractContainerScreen<?> containerScreen);

	int getLeftPos(AbstractContainerScreen<?> containerScreen);

	int getTopPos(AbstractContainerScreen<?> containerScreen);

	int getImageWidth(AbstractContainerScreen<?> containerScreen);

	int getImageHeight(AbstractContainerScreen<?> containerScreen);

	ImmutableRect2i getToastsArea();

	List<RecipeBookTabButton> getTabButtons(RecipeBookComponent<?> recipeBookComponent);

	<T extends RecipeBookMenu> RecipeBookComponent<?> getRecipeBookComponent(AbstractRecipeBookScreen<T> screen);

	ImmutableRect2i getBookArea(RecipeBookComponent<?> guiRecipeBook);

	boolean canLoseFocus(EditBox editBox);
}
