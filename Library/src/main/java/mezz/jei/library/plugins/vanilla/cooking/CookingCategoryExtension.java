package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.cooking.ICookingCategoryExtension;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public class CookingCategoryExtension implements ICookingCategoryExtension<AbstractCookingRecipe> {
	@Override
	public void setRecipe(AbstractCookingRecipe recipe, IRecipeLayoutBuilder builder, IRecipeSlotBuilder inputSlot, IRecipeSlotBuilder outputSlot, IFocusGroup focuses) {
		inputSlot.addIngredients(recipe.getIngredients().get(0));
		ItemStack result;
		if (recipe instanceof JeiSmeltingRecipe jeiRecipe) {
			result = jeiRecipe.getResult();
		} else {
			result = recipe.getResultItem(RegistryAccess.EMPTY);
		}
		outputSlot.addItemStack(result);
	}

	@Override
	public boolean isHandled(AbstractCookingRecipe recipe) {
		return !recipe.isSpecial();
	}

	public static Ingredient getDefaultFuel(AbstractCookingRecipe recipe) {
		if (recipe instanceof JeiSmeltingRecipe jeiRecipe) {
			return jeiRecipe.getFuel();
		}
		return Ingredient.EMPTY;
	}

	public static ItemStack getDefaultFuelOutput(AbstractCookingRecipe recipe) {
		if (recipe instanceof JeiSmeltingRecipe jeiRecipe) {
			return jeiRecipe.getFuelOutput();
		}
		return ItemStack.EMPTY;
	}

	public static boolean isSpecificFuel(@Nullable Ingredient fuel) {
		return fuel != null && !fuel.isEmpty();
	}
}
