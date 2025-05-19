package mezz.jei.library.plugins.vanilla.gui;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.RecipeBookMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeBookGuiHandler<C extends RecipeBookMenu, T extends AbstractRecipeBookScreen<C>> implements IGuiContainerHandler<T> {
	/**
	 * Modeled after {@link RecipeBookComponent#render(GuiGraphics, int, int, float)}
	 */
	@Override
	public List<Rect2i> getGuiExtraAreas(T containerScreen) {
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();

		RecipeBookComponent<?> guiRecipeBook = screenHelper.getRecipeBookComponent(containerScreen);
		if (guiRecipeBook.isVisible()) {
			List<Rect2i> tabAreas = new ArrayList<>();
			for (RecipeBookTabButton tab : screenHelper.getTabButtons(guiRecipeBook)) {
				if (tab.visible) {
					tabAreas.add(new Rect2i(tab.getX(), tab.getY(), tab.getWidth(), tab.getHeight()));
				}
			}
			return tabAreas;
		}
		return Collections.emptyList();
	}
}
