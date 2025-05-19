package mezz.jei.library.recipes;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.ICraftingStationLookup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("removal")
public class CraftingStationLookup implements mezz.jei.api.recipe.IRecipeCatalystLookup, ICraftingStationLookup {
	private final IRecipeType<?> recipeType;
	private final RecipeManagerInternal recipeManager;
	private boolean includeHidden;

	public CraftingStationLookup(IRecipeType<?> recipeType, RecipeManagerInternal recipeManager) {
		this.recipeType = recipeType;
		this.recipeManager = recipeManager;
	}

	@Override
	public CraftingStationLookup includeHidden() {
		this.includeHidden = true;
		return this;
	}

	@Override
	public Stream<ITypedIngredient<?>> get() {
		return recipeManager.getCraftingStations(recipeType, includeHidden);
	}

	@Override
	public <V> Stream<V> get(IIngredientType<V> ingredientType) {
		return get()
			.map(i -> i.getIngredient(ingredientType))
			.flatMap(Optional::stream);
	}

	@Override
	public Stream<ItemStack> getItemStack() {
		return get(VanillaTypes.ITEM_STACK);
	}
}
