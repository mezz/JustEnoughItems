package mezz.jei.plugins.debug;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import mezz.jei.Internal;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.IIngredientHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugGhostIngredientHandler<T extends ContainerScreen> implements IGhostIngredientHandler<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public <I> List<Target<I>> getTargets(T gui, I ingredient, boolean doStart) {
		List<Target<I>> targets = new ArrayList<>();
		targets.add(new DebugInfoTarget<>("Got an Ingredient", new Rectangle2d(0, 0, 20, 20)));
		if (doStart) {
			IIngredientHelper<I> ingredientHelper = Internal.getIngredientManager().getIngredientHelper(ingredient);
			LOGGER.info("Ghost Ingredient Handling Starting with {}", ingredientHelper.getErrorInfo(ingredient));
			targets.add(new DebugInfoTarget<>("Got an Ingredient", new Rectangle2d(20, 20, 20, 20)));
		}
		if (ingredient instanceof ItemStack) {
			boolean even = true;
			for (Slot slot : gui.getContainer().inventorySlots) {
				if (even) {
					Rectangle2d area = new Rectangle2d(gui.getGuiLeft() + slot.xPos, gui.getGuiTop() + slot.yPos, 16, 16);
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
		private final Rectangle2d rectangle;

		public DebugInfoTarget(String message, Rectangle2d rectangle) {
			this.message = message;
			this.rectangle = rectangle;
		}

		@Override
		public Rectangle2d getArea() {
			return rectangle;
		}

		@Override
		public void accept(I ingredient) {
			IIngredientHelper<I> ingredientHelper = Internal.getIngredientManager().getIngredientHelper(ingredient);
			LOGGER.info("{}: {}", message, ingredientHelper.getErrorInfo(ingredient));
		}
	}
}
