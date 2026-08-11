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
import mezz.jei.library.plugins.vanilla.cooking.JeiSmeltingRecipe;
import mezz.jei.library.plugins.vanilla.grindstone.GrindstoneRecipe;
import mezz.jei.library.plugins.vanilla.crafting.JeiShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VanillaRecipeFactory implements IVanillaRecipeFactory {
	private final BrewingRecipeUtil brewingRecipeUtil;

	public VanillaRecipeFactory(IIngredientHelper<ItemStack> ingredientHelper) {
		this.brewingRecipeUtil = new BrewingRecipeUtil(ingredientHelper);
	}

	@Override
	public IJeiAnvilRecipe createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs, @Nullable ResourceLocation uid) {
		ErrorUtil.checkNotEmpty(leftInput, "leftInput");
		ErrorUtil.checkNotNull(rightInputs, "rightInputs");
		ErrorUtil.checkNotEmpty(outputs, "outputs");

		return new AnvilRecipe(List.of(leftInput), List.copyOf(rightInputs), List.copyOf(outputs), uid);
	}

	@Override
	public AnvilRecipe createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		ErrorUtil.checkNotEmpty(leftInput, "leftInput");
		ErrorUtil.checkNotNull(rightInputs, "rightInputs");
		ErrorUtil.checkNotEmpty(outputs, "outputs");

		return new AnvilRecipe(List.of(leftInput), List.copyOf(rightInputs), List.copyOf(outputs), null);
	}

	@Override
	public AnvilRecipe createAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs, ResourceLocation uid) {
		ErrorUtil.checkNotEmpty(leftInputs, "leftInput");
		ErrorUtil.checkNotNull(rightInputs, "rightInputs");
		ErrorUtil.checkNotEmpty(outputs, "outputs");
		ErrorUtil.checkNotNull(uid, "uid");

		return new AnvilRecipe(List.copyOf(leftInputs), List.copyOf(rightInputs), List.copyOf(outputs), uid);
	}

	@Override
	public AnvilRecipe createAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		ErrorUtil.checkNotEmpty(leftInputs, "leftInput");
		ErrorUtil.checkNotNull(rightInputs, "rightInputs");
		ErrorUtil.checkNotEmpty(outputs, "outputs");

		return new AnvilRecipe(List.copyOf(leftInputs), List.copyOf(rightInputs), List.copyOf(outputs), null);
	}

	@Override
	public GrindstoneRecipe createGrindstoneRecipe(List<ItemStack> topInputs, List<ItemStack> bottomInputs, List<ItemStack> outputs, int minXp, int maxXp, @Nullable ResourceLocation uid) {
		ErrorUtil.checkNotEmpty(topInputs, "topInputs");
		ErrorUtil.checkNotNull(bottomInputs, "bottomInputs");
		ErrorUtil.checkNotEmpty(outputs, "outputs");

		return new GrindstoneRecipe(List.copyOf(topInputs), List.copyOf(bottomInputs), List.copyOf(outputs), minXp, maxXp, uid);
	}

	@Override
	public SmeltingRecipe createSmeltingRecipe(
		Ingredient input,
		Ingredient fuel,
		ItemStack fuelOutput,
		ItemStack output,
		int cookingTime,
		float experience,
		ResourceLocation uid
	) {
		ErrorUtil.checkNotNull(input, "input");
		ErrorUtil.checkNotNull(fuel, "fuel");
		ErrorUtil.checkNotNull(fuelOutput, "fuelOutput");
		ErrorUtil.checkNotEmpty(output, "output");
		if (cookingTime <= 0) {
			throw new IllegalArgumentException("cookingTime must be greater than 0");
		}
		if (!Float.isFinite(experience) || experience < 0) {
			throw new IllegalArgumentException("experience must be greater than or equal to 0");
		}
		ErrorUtil.checkNotNull(uid, "uid");

		return new JeiSmeltingRecipe(
			uid,
			input,
			fuel,
			fuelOutput.copy(),
			output.copy(),
			experience,
			cookingTime
		);
	}

	@Override
	public IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput, ResourceLocation uid) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		ErrorUtil.checkNotEmpty(potionInput, "potionInput");
		ErrorUtil.checkNotEmpty(potionOutput, "potionOutput");
		ErrorUtil.checkNotNull(uid, "uid");

		List<ItemStack> potionInputs = List.of(potionInput);
		return new JeiBrewingRecipe(ingredients, potionInputs, potionOutput, uid, brewingRecipeUtil);
	}

	@Override
	public IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		ErrorUtil.checkNotEmpty(potionInput, "potionInput");
		ErrorUtil.checkNotEmpty(potionOutput, "potionOutput");

		List<ItemStack> potionInputs = List.of(potionInput);
		return new JeiBrewingRecipe(ingredients, potionInputs, potionOutput, null, brewingRecipeUtil);
	}

	@Override
	public IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, List<ItemStack> potionInputs, ItemStack potionOutput, ResourceLocation uid) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		ErrorUtil.checkNotEmpty(potionInputs, "potionInputs");
		ErrorUtil.checkNotEmpty(potionOutput, "potionOutput");
		ErrorUtil.checkNotNull(uid, "uid");

		return new JeiBrewingRecipe(ingredients, potionInputs, potionOutput, uid, brewingRecipeUtil);
	}

	@Override
	public IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, List<ItemStack> potionInputs, ItemStack potionOutput) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		ErrorUtil.checkNotEmpty(potionInputs, "potionInputs");
		ErrorUtil.checkNotEmpty(potionOutput, "potionOutput");

		return new JeiBrewingRecipe(ingredients, potionInputs, potionOutput, null, brewingRecipeUtil);
	}

	@Override
	public IJeiShapedRecipeBuilder createShapedRecipeBuilder(CraftingBookCategory category, List<ItemStack> results) {
		ErrorUtil.checkNotNull(category, "category");
		ErrorUtil.checkNotEmpty(results, "results");

		return new JeiShapedRecipeBuilder(category, List.copyOf(results));
	}
}
