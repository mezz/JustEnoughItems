package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.gui.input.IUserInputHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public interface IRecipeLayoutWithButtons<R> {
	void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks);

	void updateBounds(int recipeXOffset, int recipeYOffset);

	int totalWidth();

	IUserInputHandler createUserInputHandler();

	void tick(@Nullable AbstractContainerMenu parentContainer, @Nullable Player player);

	IRecipeLayoutDrawable<R> getRecipeLayout();

	void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY);

	int getMissingCountHint();
}
