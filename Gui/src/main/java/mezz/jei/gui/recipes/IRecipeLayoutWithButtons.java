package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.gui.input.IUserInputHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface IRecipeLayoutWithButtons<R> {
	void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks);

	void updateBounds(int recipeXOffset, int recipeYOffset);

	int totalWidth();

	IUserInputHandler createUserInputHandler();

	void tick();

	IRecipeLayoutDrawable<R> getRecipeLayout();

	void drawTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);

	int getMissingCountHint();
}
