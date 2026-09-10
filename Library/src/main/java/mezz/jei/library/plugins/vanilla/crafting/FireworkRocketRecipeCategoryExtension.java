package mezz.jei.library.plugins.vanilla.crafting;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.platform.IPlatformRecipeHelper.FireworkRocketRecipeData;
import mezz.jei.library.plugins.vanilla.ingredients.FireworkRocketIngredientFactory;
import mezz.jei.library.plugins.vanilla.ingredients.FireworkRocketIngredientFactory.Rocket;
import mezz.jei.library.plugins.vanilla.ingredients.FireworkStarIngredientFactory;
import mezz.jei.library.plugins.vanilla.ingredients.FireworkStarIngredientFactory.Explosion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.FireworkRocketRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class FireworkRocketRecipeCategoryExtension implements ICraftingCategoryExtension {
	private final FireworkRocketRecipe recipe;
	private final IPlatformRecipeHelper recipeHelper;

	public FireworkRocketRecipeCategoryExtension(FireworkRocketRecipe recipe, IPlatformRecipeHelper recipeHelper) {
		this.recipe = recipe;
		this.recipeHelper = recipeHelper;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		FireworkRocketRecipeData recipeData = recipeHelper.getFireworkRocketRecipeData(recipe);
		Rocket rocket = getFocusedRocket(focuses, recipeData).orElseGet(() -> new Rocket(1, List.of()));
		ItemStack output = FireworkRocketIngredientFactory.createRocket(rocket.duration(), rocket.explosions());
		output.setCount(recipeData.result().getCount());
		craftingGridHelper.createAndSetOutputs(builder, List.of(output));

		List<List<ItemStack>> ingredients = new ArrayList<>();
		ingredients.add(Arrays.asList(recipeData.shell().getItems()));
		for (int i = 0; i < rocket.duration(); i++) {
			ingredients.add(Arrays.asList(recipeData.fuel().getItems()));
		}
		for (Explosion explosion : rocket.explosions()) {
			List<ItemStack> stars = Arrays.stream(recipeData.star().getItems())
				.map(ItemStack::copy)
				.peek(stack -> FireworkStarIngredientFactory.setExplosion(stack, explosion))
				.toList();
			ingredients.add(stars);
		}
		craftingGridHelper.createAndSetInputs(builder, ingredients, 0, 0);
	}

	private static Optional<Rocket> getFocusedRocket(IFocusGroup focuses, FireworkRocketRecipeData recipeData) {
		Optional<Rocket> focusedRocket = focuses.getItemStackFocuses(RecipeIngredientRole.OUTPUT)
			.map(focus -> focus.getTypedValue().getIngredient())
			.filter(stack -> stack.sameItem(recipeData.result()))
			.map(FireworkRocketIngredientFactory::getRocket)
			.flatMap(Optional::stream)
			.filter(FireworkRocketIngredientFactory::isCraftable)
			.findFirst();
		if (focusedRocket.isPresent()) {
			return focusedRocket;
		}
		return focuses.getItemStackFocuses(RecipeIngredientRole.INPUT)
			.map(focus -> focus.getTypedValue().getIngredient())
			.filter(recipeData.star())
			.map(FireworkStarIngredientFactory::getExplosion)
			.flatMap(Optional::stream)
			.findFirst()
			.map(explosion -> new Rocket(1, List.of(explosion)));
	}

	@Nullable
	@Override
	public ResourceLocation getRegistryName() {
		return recipe.getId();
	}
}
