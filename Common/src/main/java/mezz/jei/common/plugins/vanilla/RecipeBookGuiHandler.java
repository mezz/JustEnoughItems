package mezz.jei.common.plugins.vanilla;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeBookGuiHandler<C extends AbstractContainerMenu, T extends AbstractContainerScreen<C> & RecipeUpdateListener> implements IGuiContainerHandler<T> {
	/**
	 * Modeled after {@link RecipeBookComponent#render(com.mojang.blaze3d.vertex.PoseStack, int, int, float)}
	 */
	@Override
	public List<Rect2i> getGuiExtraAreas(T containerScreen) {
		RecipeBookComponent guiRecipeBook = containerScreen.getRecipeBookComponent();
		if (guiRecipeBook.isVisible()) {
			IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
			List<Rect2i> tabAreas = new ArrayList<>();
			for (RecipeBookTabButton tab : screenHelper.getTabButtons(guiRecipeBook)) {
				if (tab.visible) {
					tabAreas.add(new Rect2i(tab.x, tab.y, tab.getWidth(), tab.getHeight()));
				}
			}
			return tabAreas;
		}
		return Collections.emptyList();
	}
}
