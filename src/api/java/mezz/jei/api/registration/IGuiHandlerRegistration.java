package mezz.jei.api.registration;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.handlers.IScreenHandler;

public interface IGuiHandlerRegistration {
	/**
	 * Add a handler to give JEI extra information about how to layout the item list next to a specific type of {@link ContainerScreen}.
	 * Multiple handlers can be registered for one {@link ContainerScreen}.
	 */
	<T extends ContainerScreen<?>> void addGuiContainerHandler(Class<? extends T> guiClass, IGuiContainerHandler<T> guiHandler);

	/**
	 * Add a handler to let JEI draw next to a specific class (or subclass) of {@link Screen}.
	 * By default, JEI can only draw next to {@link ContainerScreen}.
	 */
	<T extends Screen> void addGuiScreenHandler(Class<T> guiClass, IScreenHandler<T> handler);

	/**
	 * Add a handler to give JEI extra information about how to layout the ingredient list.
	 * Used for guis that display next to GUIs and would normally intersect with JEI.
	 */
	void addGlobalGuiHandler(IGlobalGuiHandler globalGuiHandler);

	/**
	 * Add a clickable area on a gui to jump to specific categories of recipes in JEI.
	 *
	 * @param guiContainerClass  the gui class for JEI to detect.
	 * @param xPos               left x position of the clickable area, relative to the left edge of the gui.
	 * @param yPos               top y position of the clickable area, relative to the top edge of the gui.
	 * @param width              the width of the clickable area.
	 * @param height             the height of the clickable area.
	 * @param recipeCategoryUids the recipe categories that JEI should display.
	 */
	default <T extends ContainerScreen<?>> void addRecipeClickArea(Class<? extends T> guiContainerClass, int xPos, int yPos, int width, int height, ResourceLocation... recipeCategoryUids) {
		this.addGuiContainerHandler(guiContainerClass, new IGuiContainerHandler<T>() {
			@Override
			public Collection<IGuiClickableArea> getGuiClickableAreas(T containerScreen, double mouseX, double mouseY) {
				IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(xPos, yPos, width, height, recipeCategoryUids);
				return Collections.singleton(clickableArea);
			}
		});
	}

	/**
	 * Lets mods accept ghost ingredients from JEI.
	 * These ingredients are dragged from the ingredient list on to your gui, and are useful
	 * for setting recipes or anything else that does not need the real ingredient to exist.
	 */
	<T extends Screen> void addGhostIngredientHandler(Class<T> guiClass, IGhostIngredientHandler<T> handler);

}
