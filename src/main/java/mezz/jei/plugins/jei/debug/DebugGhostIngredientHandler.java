package mezz.jei.plugins.jei.debug;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import mezz.jei.Internal;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.util.Log;

public class DebugGhostIngredientHandler<T extends GuiContainer> implements IGhostIngredientHandler<T> {
	@Override
	public <I> List<Target<I>> getTargets(T gui, I ingredient, boolean doStart) {
		List<Target<I>> targets = new ArrayList<>();
		targets.add(new DebugInfoTarget<>("Got an Ingredient", new Rectangle(0, 0, 20, 20)));
		if (doStart) {
			IIngredientHelper<I> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(ingredient);
			Log.get().info("Ghost Ingredient Handling Starting with {}", ingredientHelper.getErrorInfo(ingredient));
			targets.add(new DebugInfoTarget<>("Got an Ingredient", new Rectangle(20, 20, 20, 20)));
		}
		if (ingredient instanceof ItemStack) {
			boolean even = true;
			for (Slot slot : gui.inventorySlots.inventorySlots) {
				if (even) {
					Rectangle area = new Rectangle(gui.getGuiLeft() + slot.xPos, gui.getGuiTop() + slot.yPos, 16, 16);
					targets.add(new DebugInfoTarget<>("Got an Ingredient in Gui", area));
				}
				even = !even;
			}
		}
		return targets;
	}

	@Override
	public void onComplete() {
		Log.get().info("Ghost Ingredient Handling Complete");
	}

	private static class DebugInfoTarget<I> implements IGhostIngredientHandler.Target<I> {
		private final String message;
		private final Rectangle rectangle;

		public DebugInfoTarget(String message, Rectangle rectangle) {
			this.message = message;
			this.rectangle = rectangle;
		}

		@Override
		public Rectangle getArea() {
			return rectangle;
		}

		@Override
		public void accept(I ingredient) {
			IIngredientHelper<I> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(ingredient);
			Log.get().info("{}: {}", message, ingredientHelper.getErrorInfo(ingredient));
		}
	}
}
