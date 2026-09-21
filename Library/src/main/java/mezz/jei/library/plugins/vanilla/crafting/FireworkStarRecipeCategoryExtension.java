package mezz.jei.library.plugins.vanilla.crafting;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.library.plugins.vanilla.ingredients.FireworkStarIngredientFactory;
import mezz.jei.library.plugins.vanilla.ingredients.FireworkStarIngredientFactory.Explosion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.FireworkStarFadeRecipe;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class FireworkStarRecipeCategoryExtension implements ICraftingCategoryExtension {
	private final CraftingRecipe recipe;
	private final IPlatformRecipeHelper recipeHelper;

	public FireworkStarRecipeCategoryExtension(CraftingRecipe recipe, IPlatformRecipeHelper recipeHelper) {
		this.recipe = recipe;
		this.recipeHelper = recipeHelper;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		ItemStack focusedInput = getFocus(focuses, RecipeIngredientRole.INPUT);
		ItemStack focusedOutput = getFocus(focuses, RecipeIngredientRole.OUTPUT);
		if (recipe instanceof FireworkStarRecipe starRecipe) {
			setStarRecipe(starRecipe, builder, craftingGridHelper, focusedInput, focusedOutput);
		} else if (recipe instanceof FireworkStarFadeRecipe fadeRecipe) {
			setFadeRecipe(fadeRecipe, builder, craftingGridHelper, focusedInput, focusedOutput);
		}
	}

	private void setStarRecipe(
		FireworkStarRecipe starRecipe,
		IRecipeLayoutBuilder builder,
		ICraftingGridHelper craftingGridHelper,
		ItemStack focusedInput,
		ItemStack focusedOutput
	) {
		var data = recipeHelper.getFireworkStarRecipeData(starRecipe);
		Explosion explosion = FireworkStarIngredientFactory.getExplosion(focusedOutput)
			.filter(value -> value.fadeColors().isEmpty())
			.orElseGet(() -> createExplosion(data.shapes(), data.dye(), data.trail(), data.twinkle(), focusedInput));
		List<List<ItemStack>> inputs = new ArrayList<>();
		inputs.add(stacks(data.fuel(), focusedInput));
		inputs.add(stacks(data.dye(), focusedInput));
		if (explosion.shape() != FireworkRocketItem.Shape.SMALL_BALL) {
			Ingredient shape = data.shapes().get(explosion.shape());
			if (shape != null) {
				inputs.add(stacks(shape, focusedInput));
			}
		}
		if (explosion.trail()) {
			inputs.add(stacks(data.trail(), focusedInput));
		}
		if (explosion.twinkle()) {
			inputs.add(stacks(data.twinkle(), focusedInput));
		}
		craftingGridHelper.createAndSetInputs(builder, inputs, 0, 0);
		craftingGridHelper.createAndSetOutputs(builder, List.of(FireworkStarIngredientFactory.createStar(explosion)));
	}

	private void setFadeRecipe(
		FireworkStarFadeRecipe fadeRecipe,
		IRecipeLayoutBuilder builder,
		ICraftingGridHelper craftingGridHelper,
		ItemStack focusedInput,
		ItemStack focusedOutput
	) {
		var data = recipeHelper.getFireworkStarFadeRecipeData(fadeRecipe);
		Explosion outputExplosion = FireworkStarIngredientFactory.getExplosion(focusedOutput)
			.filter(value -> !value.fadeColors().isEmpty())
			.orElse(null);
		Explosion baseExplosion = FireworkStarIngredientFactory.getExplosion(focusedInput)
			.orElseGet(() -> FireworkStarIngredientFactory.createExplosions().get(0));
		if (outputExplosion == null) {
			outputExplosion = baseExplosion.withFadeColors(List.of(getDyeColor(data.dye(), focusedInput)));
		}
		Explosion inputExplosion = new Explosion(outputExplosion.shape(), outputExplosion.colors(), List.of(), outputExplosion.trail(), outputExplosion.twinkle());
		craftingGridHelper.createAndSetInputs(builder, List.of(
			List.of(FireworkStarIngredientFactory.createStar(inputExplosion)),
			stacks(data.dye(), focusedInput)
		), 0, 0);
		craftingGridHelper.createAndSetOutputs(builder, List.of(FireworkStarIngredientFactory.createStar(outputExplosion)));
	}

	private static Explosion createExplosion(
		Map<FireworkRocketItem.Shape, Ingredient> shapes,
		Ingredient dyes,
		Ingredient trail,
		Ingredient twinkle,
		ItemStack focusedInput
	) {
		FireworkRocketItem.Shape shape = shapes.entrySet().stream()
			.filter(entry -> entry.getValue().test(focusedInput))
			.map(Map.Entry::getKey)
			.findFirst()
			.orElse(FireworkRocketItem.Shape.SMALL_BALL);
		return new Explosion(
			shape,
			List.of(getDyeColor(dyes, focusedInput)),
			List.of(),
			trail.test(focusedInput),
			twinkle.test(focusedInput)
		);
	}

	private static int getDyeColor(Ingredient dyes, ItemStack focusedInput) {
		if (dyes.test(focusedInput) && focusedInput.getItem() instanceof DyeItem dyeItem) {
			return dyeItem.getDyeColor().getFireworkColor();
		}
		return Arrays.stream(dyes.getItems())
			.map(ItemStack::getItem)
			.filter(DyeItem.class::isInstance)
			.map(DyeItem.class::cast)
			.map(DyeItem::getDyeColor)
			.mapToInt(DyeColor::getFireworkColor)
			.findFirst()
			.orElse(DyeColor.RED.getFireworkColor());
	}

	private static List<ItemStack> stacks(Ingredient ingredient, ItemStack focusedInput) {
		if (ingredient.test(focusedInput)) {
			return List.of(focusedInput);
		}
		return Arrays.asList(ingredient.getItems());
	}

	private static ItemStack getFocus(IFocusGroup focuses, RecipeIngredientRole role) {
		return focuses.getItemStackFocuses(role)
			.map(focus -> focus.getTypedValue().getIngredient())
			.findFirst()
			.orElse(ItemStack.EMPTY);
	}

	@Nullable
	@Override
	public ResourceLocation getRegistryName() {
		return recipe.getId();
	}
}
