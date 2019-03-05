package mezz.jei.plugins.vanilla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.recipebook.GuiButtonRecipeTab;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.renderer.Rectangle2d;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;

class RecipeBookGuiHandler<T extends GuiContainer & IRecipeShownListener> implements IGuiContainerHandler<T> {
	/**
	 * Modeled after {@link GuiRecipeBook#render(int, int, float)}
	 */
	@Override
	public List<Rectangle2d> getGuiExtraAreas(T guiContainer) {
		GuiRecipeBook guiRecipeBook = guiContainer.func_194310_f();
		if (guiRecipeBook.isVisible()) {
			List<Rectangle2d> areas = new ArrayList<>();
			int i = (guiRecipeBook.width - 147) / 2 - guiRecipeBook.xOffset;
			int j = (guiRecipeBook.height - 166) / 2;
			areas.add(new Rectangle2d(i, j, 147, 166));
			for (GuiButtonRecipeTab tab : guiRecipeBook.recipeTabs) {
				if (tab.visible) {
					areas.add(new Rectangle2d(tab.x, tab.y, tab.width, tab.height));
				}
			}
			return areas;
		}
		return Collections.emptyList();
	}
}
