package mezz.jei.library.plugins.vanilla;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiShapedRecipeBuilder;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.plugins.vanilla.anvil.AnvilRecipe;
import mezz.jei.library.plugins.vanilla.brewing.BrewingRecipeUtil;
import mezz.jei.library.plugins.vanilla.brewing.JeiBrewingRecipe;
import mezz.jei.library.plugins.vanilla.crafting.JeiShapedRecipeBuilder;
import mezz.jei.library.plugins.vanilla.cooking.JeiSmeltingRecipe;
import mezz.jei.library.plugins.vanilla.grindstone.GrindstoneRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class VanillaRecipeFactory implements IVanillaRecipeFactory {
	private final BrewingRecipeUtil brewingRecipeUtil;
	private final ContextMap contextMap;

	public VanillaRecipeFactory(IIngredientHelper<ItemStack> ingredientHelper, ContextMap contextMap) {
		this.brewingRecipeUtil = new BrewingRecipeUtil(ingredientHelper);
		this.contextMap = contextMap;
	}

	@Override
	public IJeiAnvilRecipe createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs, @Nullable Identifier uid) {
		ErrorUtil.checkNotEmpty(leftInput, "leftInput");
		ErrorUtil.checkNotNull(rightInputs, "rightInputs");
		ErrorUtil.checkNotEmpty(outputs, "outputs");

		return new AnvilRecipe(List.of(leftInput), List.copyOf(rightInputs), List.copyOf(outputs), uid);
	}

	@Override
	public AnvilRecipe createAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs, Identifier uid) {
		ErrorUtil.checkNotEmpty(leftInputs, "leftInput");
		ErrorUtil.checkNotNull(rightInputs, "rightInputs");
		ErrorUtil.checkNotEmpty(outputs, "outputs");
		ErrorUtil.checkNotNull(uid, "uid");

		return new AnvilRecipe(List.copyOf(leftInputs), List.copyOf(rightInputs), List.copyOf(outputs), uid);
	}

	@Override
	public GrindstoneRecipe createGrindstoneRecipe(List<ItemStack> topInputs, List<ItemStack> bottomInputs, List<ItemStack> outputs, int minXp, int maxXp, @Nullable Identifier uid) {
		ErrorUtil.checkNotEmpty(topInputs, "topInputs");
		ErrorUtil.checkNotNull(bottomInputs, "bottomInputs");
		ErrorUtil.checkNotEmpty(outputs, "outputs");

		return new GrindstoneRecipe(List.copyOf(topInputs), List.copyOf(bottomInputs), List.copyOf(outputs), minXp, maxXp, uid);
	}

	@Override
	public RecipeHolder<SmeltingRecipe> createSmeltingRecipe(
		Ingredient input,
		SlotDisplay fuel,
		ItemStack output,
		int cookingTime,
		float experience,
		Identifier uid
	) {
		ErrorUtil.checkNotNull(input, "input");
		ErrorUtil.checkNotNull(fuel, "fuel");
		ErrorUtil.checkNotEmpty(output, "output");
		if (cookingTime <= 0) {
			throw new IllegalArgumentException("cookingTime must be greater than 0");
		}
		if (!Float.isFinite(experience) || experience < 0) {
			throw new IllegalArgumentException("experience must be greater than or equal to 0");
		}
		ErrorUtil.checkNotNull(uid, "uid");

		ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, uid);
		SmeltingRecipe recipe = new JeiSmeltingRecipe(
			input,
			fuel,
			output.copy(),
			experience,
			cookingTime
		);
		return new RecipeHolder<>(recipeKey, recipe);
	}

	@Override
	public IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput, Identifier uid) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		ErrorUtil.checkNotEmpty(potionInput, "potionInput");
		ErrorUtil.checkNotEmpty(potionOutput, "potionOutput");
		ErrorUtil.checkNotNull(uid, "uid");

		List<ItemStack> potionInputs = List.of(potionInput);
		return new JeiBrewingRecipe(ingredients, potionInputs, potionOutput, uid, brewingRecipeUtil);
	}

	@Override
	public IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, List<ItemStack> potionInputs, ItemStack potionOutput, Identifier uid) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		ErrorUtil.checkNotEmpty(potionInputs, "potionInputs");
		ErrorUtil.checkNotEmpty(potionOutput, "potionOutput");
		ErrorUtil.checkNotNull(uid, "uid");

		return new JeiBrewingRecipe(ingredients, potionInputs, potionOutput, uid, brewingRecipeUtil);
	}

	@Override
	public IJeiShapedRecipeBuilder createShapedRecipeBuilder(CraftingBookCategory category, SlotDisplay resultDisplay) {
		return new JeiShapedRecipeBuilder(category, resultDisplay, contextMap);
	}
}
