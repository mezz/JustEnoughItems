package mezz.jei.library.plugins.vanilla.crafting;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.platform.IPlatformRecipeHelper.FireworkRocketRecipeData;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.crafting.FireworkRocketRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FireworkRocketRecipeCategoryExtension implements ICraftingCategoryExtension<FireworkRocketRecipe> {
	private static final int CRAFTING_GRID_SLOT_COUNT = 9;

	private final IPlatformRecipeHelper recipeHelper;

	public FireworkRocketRecipeCategoryExtension(IPlatformRecipeHelper recipeHelper) {
		this.recipeHelper = recipeHelper;
	}

	@Override
	public void setRecipe(
		RecipeHolder<FireworkRocketRecipe> recipeHolder,
		IRecipeLayoutBuilder builder,
		ICraftingGridHelper craftingGridHelper,
		IFocusGroup focuses
	) {
		FireworkRocketRecipeData recipeData = recipeHelper.getFireworkRocketRecipeData(recipeHolder.value());
		Fireworks fireworks = getFocusedFireworks(focuses, recipeData)
			.orElseGet(() -> getDefaultFireworks(recipeData));

		ItemStack output = createOutput(recipeData.result(), fireworks);
		craftingGridHelper.createAndSetOutputs(builder, List.of(output));

		List<List<ItemStack>> ingredients = createIngredientDisplays(recipeData, fireworks);
		craftingGridHelper.createAndSetInputs(builder, ingredients, 0, 0);

	}

	private static Optional<Fireworks> getFocusedFireworks(IFocusGroup focuses, FireworkRocketRecipeData recipeData) {
		ItemStack defaultOutput = recipeData.result().copy();
		Optional<Fireworks> focusedRocket = focuses.getItemStackFocuses(RecipeIngredientRole.OUTPUT)
			.map(focus -> focus.getTypedValue().getIngredient())
			.filter(stack -> ItemStack.isSameItem(stack, defaultOutput))
			.map(stack -> Optional.ofNullable(stack.get(DataComponents.FIREWORKS)))
			.flatMap(Optional::stream)
			.filter(FireworkRocketRecipeCategoryExtension::canFitCraftingGrid)
			.findFirst();
		if (focusedRocket.isPresent()) {
			return focusedRocket;
		}

		return focuses.getItemStackFocuses(RecipeIngredientRole.INPUT)
			.map(focus -> focus.getTypedValue().getIngredient())
			.filter(recipeData.star())
			.map(stack -> Optional.ofNullable(stack.get(DataComponents.FIREWORK_EXPLOSION)))
			.flatMap(Optional::stream)
			.findFirst()
			.map(explosion -> {
				Fireworks defaultFireworks = getDefaultFireworks(recipeData);
				return new Fireworks(defaultFireworks.flightDuration(), List.of(explosion));
			});
	}

	private static Fireworks getDefaultFireworks(FireworkRocketRecipeData recipeData) {
		ItemStack defaultOutput = recipeData.result().copy();
		return Optional.ofNullable(defaultOutput.get(DataComponents.FIREWORKS))
			.orElseGet(() -> {
				if (FireworkRocketItem.CRAFTABLE_DURATIONS.length == 0) {
					throw new IllegalStateException("Minecraft defines no craftable firework rocket durations");
				}
				return new Fireworks(FireworkRocketItem.CRAFTABLE_DURATIONS[0], List.of());
			});
	}

	private static boolean canFitCraftingGrid(Fireworks fireworks) {
		boolean craftableDuration = false;
		for (byte duration : FireworkRocketItem.CRAFTABLE_DURATIONS) {
			if (duration == fireworks.flightDuration()) {
				craftableDuration = true;
				break;
			}
		}
		return craftableDuration &&
			1 + fireworks.flightDuration() + fireworks.explosions().size() <= CRAFTING_GRID_SLOT_COUNT;
	}

	private static List<List<ItemStack>> createIngredientDisplays(FireworkRocketRecipeData recipeData, Fireworks fireworks) {
		List<List<ItemStack>> ingredients = new ArrayList<>(1 + fireworks.flightDuration() + fireworks.explosions().size());
		ingredients.add(List.of(recipeData.shell().getItems()));
		for (int i = 0; i < fireworks.flightDuration(); i++) {
			ingredients.add(List.of(recipeData.fuel().getItems()));
		}
		for (FireworkExplosion explosion : fireworks.explosions()) {
			ingredients.add(java.util.Arrays.stream(recipeData.star().getItems())
				.map(stack -> {
					ItemStack copy = stack.copy();
					copy.set(DataComponents.FIREWORK_EXPLOSION, explosion);
					return copy;
				})
				.toList());
		}
		return ingredients;
	}

	private static ItemStack createOutput(ItemStack result, Fireworks fireworks) {
		DataComponentPatch components = DataComponentPatch.builder()
			.set(DataComponents.FIREWORKS, fireworks)
			.build();
		ItemStack output = result.copy();
		output.applyComponents(components);
		return output;
	}

}
