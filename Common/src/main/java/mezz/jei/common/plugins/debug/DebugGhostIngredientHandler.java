package mezz.jei.common.plugins.debug;

import mezz.jei.common.Internal;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class DebugGhostIngredientHandler<T extends AbstractContainerScreen<?>> implements IGhostIngredientHandler<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public <I> List<Target<I>> getTargets(T gui, I ingredient, boolean doStart) {
		List<Target<I>> targets = new ArrayList<>();
		targets.add(new DebugInfoTarget<>("Got an Ingredient", new Rect2i(0, 0, 20, 20)));
		if (doStart) {
			RegisteredIngredients registeredIngredients = Internal.getRegisteredIngredients();
			IIngredientType<I> ingredientType = registeredIngredients.getIngredientType(ingredient);
			IIngredientHelper<I> ingredientHelper = registeredIngredients.getIngredientHelper(ingredientType);
			LOGGER.info("Ghost Ingredient Handling Starting with {}", ingredientHelper.getErrorInfo(ingredient));
			targets.add(new DebugInfoTarget<>("Got an Ingredient", new Rect2i(20, 20, 20, 20)));
		}
		if (ingredient instanceof ItemStack) {
			boolean even = true;
			IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
			for (Slot slot : gui.getMenu().slots) {
				if (even) {
					int guiLeft = screenHelper.getGuiLeft(gui);
					int guiTop = screenHelper.getGuiTop(gui);
					Rect2i area = new Rect2i(guiLeft + slot.x, guiTop + slot.y, 16, 16);
					targets.add(new DebugInfoTarget<>("Got an Ingredient in Gui", area));
				}
				even = !even;
			}
		}
		return targets;
	}

	@Override
	public void onComplete() {
		LOGGER.info("Ghost Ingredient Handling Complete");
	}

	private static class DebugInfoTarget<I> implements IGhostIngredientHandler.Target<I> {
		private final String message;
		private final Rect2i rectangle;

		public DebugInfoTarget(String message, Rect2i rectangle) {
			this.message = message;
			this.rectangle = rectangle;
		}

		@Override
		public Rect2i getArea() {
			return rectangle;
		}

		@Override
		public void accept(I ingredient) {
			RegisteredIngredients registeredIngredients = Internal.getRegisteredIngredients();
			IIngredientType<I> ingredientType = registeredIngredients.getIngredientType(ingredient);
			IIngredientHelper<I> ingredientHelper = registeredIngredients.getIngredientHelper(ingredientType);
			LOGGER.info("{}: {}", message, ingredientHelper.getErrorInfo(ingredient));
		}
	}
}
