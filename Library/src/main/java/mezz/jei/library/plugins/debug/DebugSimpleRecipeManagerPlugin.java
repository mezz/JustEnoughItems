package mezz.jei.library.plugins.debug;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.List;

public class DebugSimpleRecipeManagerPlugin implements ISimpleRecipeManagerPlugin<CraftingRecipe> {
	@Override
	public boolean isHandledInput(ITypedIngredient<?> input) {
		ItemStack itemStack = input.getItemStack().orElse(ItemStack.EMPTY);
		return itemStack.is(Items.LIGHT);
	}

	@Override
	public boolean isHandledOutput(ITypedIngredient<?> output) {
		ItemStack itemStack = output.getItemStack().orElse(ItemStack.EMPTY);
		return itemStack.is(Items.DARK_PRISMARINE_SLAB);
	}

	@Override
	public List<CraftingRecipe> getRecipesForInput(ITypedIngredient<?> input) {
		return List.of(generateRecipe());
	}

	@Override
	public List<CraftingRecipe> getRecipesForOutput(ITypedIngredient<?> output) {
		return List.of(generateRecipe());
	}

	@Override
	public List<CraftingRecipe> getAllRecipes() {
		return List.of(generateRecipe());
	}

	private CraftingRecipe generateRecipe() {
		ResourceLocation id = new ResourceLocation(ModIds.JEI_ID, "debug_simple_recipe");

		NonNullList<Ingredient> inputs = NonNullList.of(
			Ingredient.EMPTY,
			Ingredient.of(Items.LIGHT),
			Ingredient.of(Items.LIGHT),
			Ingredient.of(Items.LIGHT),
			Ingredient.of(Items.LIGHT),
			Ingredient.of(Items.LIGHT),
			Ingredient.of(Items.LIGHT),
			Ingredient.of(Items.LIGHT),
			Ingredient.of(Items.LIGHT),
			Ingredient.of(Items.LIGHT)
		);

		return new ShapedRecipe(id, "", CraftingBookCategory.MISC, 3, 3, inputs, new ItemStack(Items.DARK_PRISMARINE_SLAB));
	}
}
