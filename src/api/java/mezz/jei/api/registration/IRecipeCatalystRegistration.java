package mezz.jei.api.registration;

import net.minecraft.util.ResourceLocation;

public interface IRecipeCatalystRegistration {
	/**
	 * Add an association between an ingredient and what it can craft. (i.e. Furnace ItemStack -> Smelting and Fuel Recipes)
	 * Allows players to see what ingredient they need to craft in order to make recipes from a recipe category.
	 *
	 * @param catalystIngredient the ingredient that can craft recipes (like a furnace or crafting table)
	 * @param recipeCategoryUids the recipe categories handled by the ingredient
	 */
	void addRecipeCatalyst(Object catalystIngredient, ResourceLocation... recipeCategoryUids);
}
