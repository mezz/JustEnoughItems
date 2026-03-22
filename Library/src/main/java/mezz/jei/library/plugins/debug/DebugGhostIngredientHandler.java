package mezz.jei.library.plugins.debug;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.Internal;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class DebugGhostIngredientHandler<T extends AbstractContainerScreen<?>> implements IGhostIngredientHandler<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IIngredientManager ingredientManager;

	public DebugGhostIngredientHandler(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@Override
	public <I> List<Target<I>> getTargetsTyped(T gui, ITypedIngredient<I> typedIngredient, boolean doStart) {
		List<Target<I>> targets = new ArrayList<>();
		targets.add(new DebugInfoTarget<>("Got an Ingredient", new Rect2i(0, 0, 20, 20), ingredientManager));
		if (doStart) {
			IIngredientType<I> ingredientType = typedIngredient.getType();
			IIngredientHelper<I> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
			LOGGER.info("1: Ghost Ingredient Handling Starting with {}", ingredientHelper.getErrorInfo(typedIngredient.getIngredient()));
			targets.add(new DebugInfoTarget<>("Got an Ingredient", new Rect2i(20, 20, 20, 20), ingredientManager));
		}
		IScreenHelper screenHelper = Internal.getJeiRuntime().getScreenHelper();
		screenHelper.getGuiProperties(gui)
			.ifPresent(guiProperties -> {
				typedIngredient.getItemStack()
					.ifPresent(itemStack -> {
						final int guiLeft = guiProperties.guiLeft();
						final int guiTop = guiProperties.guiTop();
						boolean even = true;
						int count = 0;
						for (Slot slot : gui.getMenu().slots) {
							if (even) {
								Rect2i area = new Rect2i(guiLeft + slot.x, guiTop + slot.y, 16, 16);
								targets.add(new DebugInfoTarget<>("Got an Ingredient in Gui", area, ingredientManager));
							}
							count++;
							if (count > 10) {
								break;
							}
							even = !even;
						}
					});
			});

		return targets;
	}

	@Override
	public void onComplete() {
		LOGGER.info("1: Ghost Ingredient Handling Complete");
	}

	@Override
	public <I> boolean quickMove(T gui, ITypedIngredient<I> typedIngredient) {
		IIngredientType<I> ingredientType = typedIngredient.getType();
		IIngredientHelper<I> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		LOGGER.info("Ghost Ingredient Quick Moved with {}", ingredientHelper.getErrorInfo(typedIngredient.getIngredient()));
		return true;
	}

	private record DebugInfoTarget<I>(
		String message,
		Rect2i rectangle,
		IIngredientManager ingredientManager
	) implements Target<I> {

		@Override
		public Rect2i getArea() {
			return rectangle;
		}

		@Override
		public void accept(I ingredient) {
			IIngredientType<I> ingredientType = ingredientManager.getIngredientTypeChecked(ingredient)
				.orElseThrow();
			IIngredientHelper<I> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
			LOGGER.info("1: {}: {}", message, ingredientHelper.getErrorInfo(ingredient));
		}
	}
}
