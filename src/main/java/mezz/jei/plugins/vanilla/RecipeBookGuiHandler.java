package mezz.jei.plugins.vanilla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.world.inventory.AbstractContainerMenu;

class RecipeBookGuiHandler<C extends AbstractContainerMenu, T extends AbstractContainerScreen<C> & RecipeUpdateListener> implements IGuiContainerHandler<T> {
	/**
	 * Modeled after {@link RecipeBookComponent#render(com.mojang.blaze3d.vertex.PoseStack, int, int, float)}
	 */
	@Override
	public List<Rect2i> getGuiExtraAreas(T containerScreen) {
		RecipeBookComponent guiRecipeBook = containerScreen.getRecipeBookComponent();
		if (guiRecipeBook.isVisible()) {
			List<Rect2i> areas = new ArrayList<>();
			int i = (guiRecipeBook.width - 147) / 2 - guiRecipeBook.xOffset;
			int j = (guiRecipeBook.height - 166) / 2;
			areas.add(new Rect2i(i, j, 147, 166));
			for (RecipeBookTabButton tab : guiRecipeBook.tabButtons) {
				if (tab.visible) {
					areas.add(new Rect2i(tab.x, tab.y, tab.getWidth(), tab.getHeight()));
				}
			}
			return areas;
		}
		return Collections.emptyList();
	}
}
