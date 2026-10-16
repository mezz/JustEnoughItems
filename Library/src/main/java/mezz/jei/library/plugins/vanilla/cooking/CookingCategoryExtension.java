package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.cooking.ICookingCategoryExtension;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

public class CookingCategoryExtension implements ICookingCategoryExtension<AbstractCookingRecipe> {
	@Override
	public void setRecipe(AbstractCookingRecipe recipe, IRecipeLayoutBuilder builder, IRecipeSlotBuilder inputSlot, IRecipeSlotBuilder outputSlot, IFocusGroup focuses) {
		FurnaceRecipeDisplay display = getDisplay(recipe);
		if (display != null) {
			inputSlot.add(display.ingredient());
			outputSlot.add(display.result());
		}
	}

	@Override
	public boolean isHandled(AbstractCookingRecipe recipe) {
		return getDisplay(recipe) != null;
	}

	public static @Nullable FurnaceRecipeDisplay getDisplay(AbstractCookingRecipe recipe) {
		var displays = recipe.display();
		if (!displays.isEmpty() && displays.getFirst() instanceof FurnaceRecipeDisplay display) {
			return display;
		}
		return null;
	}

	public static SlotDisplay getDefaultFuel(AbstractCookingRecipe recipe) {
		FurnaceRecipeDisplay display = getDisplay(recipe);
		if (display != null) {
			return display.fuel();
		}
		return SlotDisplay.AnyFuel.INSTANCE;
	}

	public static boolean isSpecificFuel(SlotDisplay fuel) {
		if (fuel instanceof SlotDisplay.WithRemainder withRemainder) {
			fuel = withRemainder.input();
		}
		return fuel != SlotDisplay.AnyFuel.INSTANCE && fuel != SlotDisplay.Empty.INSTANCE;
	}
}
