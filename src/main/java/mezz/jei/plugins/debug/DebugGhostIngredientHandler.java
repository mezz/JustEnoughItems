package mezz.jei.plugins.debug;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import mezz.jei.Internal;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.IIngredientHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler.Target;

public class DebugGhostIngredientHandler<T extends AbstractContainerScreen<?>> implements IGhostIngredientHandler<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public <I> List<Target<I>> getTargets(T gui, I ingredient, boolean doStart) {
		List<Target<I>> targets = new ArrayList<>();
		targets.add(new DebugInfoTarget<>("Got an Ingredient", new Rect2i(0, 0, 20, 20)));
		if (doStart) {
			IIngredientHelper<I> ingredientHelper = Internal.getIngredientManager().getIngredientHelper(ingredient);
			LOGGER.info("Ghost Ingredient Handling Starting with {}", ingredientHelper.getErrorInfo(ingredient));
			targets.add(new DebugInfoTarget<>("Got an Ingredient", new Rect2i(20, 20, 20, 20)));
		}
		if (ingredient instanceof ItemStack) {
			boolean even = true;
			for (Slot slot : gui.getMenu().slots) {
				if (even) {
					Rect2i area = new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
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
			IIngredientHelper<I> ingredientHelper = Internal.getIngredientManager().getIngredientHelper(ingredient);
			LOGGER.info("{}: {}", message, ingredientHelper.getErrorInfo(ingredient));
		}
	}
}
