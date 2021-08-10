package mezz.jei.api.registration;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IRecipeCatalystRegistration {
	/**
	 * Add an association between an {@link ItemStack} and what it can craft.
	 * (i.e. Furnace ItemStack can craft Smelting and Fuel Recipes)
	 * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
	 *
	 * @param catalystIngredient the {@link ItemStack} that can craft recipes (like a furnace or crafting table)
	 * @param recipeCategoryUids the recipe categories handled by the catalyst
	 * @see #addRecipeCatalyst(IIngredientType, Object, ResourceLocation...) to add non-{@link ItemStack} catalysts.
	 */
	default void addRecipeCatalyst(ItemStack catalystIngredient, ResourceLocation... recipeCategoryUids) {
		addRecipeCatalyst(VanillaTypes.ITEM, catalystIngredient, recipeCategoryUids);
	}

	/**
	 * Add an association between an ingredient and what it can craft. (i.e. Furnace ItemStack -> Smelting and Fuel Recipes)
	 * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
	 *
	 * @param catalystIngredient the ingredient that can craft recipes (like a furnace or crafting table)
	 * @param recipeCategoryUids the recipe categories handled by the ingredient
	 */
	<T> void addRecipeCatalyst(IIngredientType<T> ingredientType, T catalystIngredient, ResourceLocation... recipeCategoryUids);
}
