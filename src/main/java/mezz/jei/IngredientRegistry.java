package mezz.jei;

import javax.annotation.Nullable;
import java.util.Locale;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.tileentity.TileEntityFurnace;

public class IngredientRegistry implements IIngredientRegistry {
	private final ImmutableMap<Class, ImmutableList> ingredientsMap;
	private final ImmutableMap<Class, IIngredientHelper> ingredientHelperMap;
	private final ImmutableMap<Class, IIngredientRenderer> ingredientRendererMap;
	private final ImmutableList<ItemStack> fuels;
	private final ImmutableList<ItemStack> potionIngredients;

	public IngredientRegistry(
			ImmutableMap<Class, ImmutableList> ingredientsMap,
			ImmutableMap<Class, IIngredientHelper> ingredientHelperMap,
			ImmutableMap<Class, IIngredientRenderer> ingredientRendererMap
	) {
		this.ingredientsMap = ingredientsMap;
		this.ingredientHelperMap = ingredientHelperMap;
		this.ingredientRendererMap = ingredientRendererMap;

		IIngredientHelper<ItemStack> itemStackHelper = getIngredientHelper(ItemStack.class);

		ImmutableList.Builder<ItemStack> fuelsBuilder = ImmutableList.builder();
		ImmutableList.Builder<ItemStack> potionIngredientsBuilder = ImmutableList.builder();
		ImmutableListMultimap.Builder<String, ItemStack> itemsByModIdBuilder = ImmutableListMultimap.builder();

		for (ItemStack itemStack : getIngredients(ItemStack.class)) {
			String modId = itemStackHelper.getModId(itemStack).toLowerCase(Locale.ENGLISH);
			itemsByModIdBuilder.put(modId, itemStack);

			if (TileEntityFurnace.isItemFuel(itemStack)) {
				fuelsBuilder.add(itemStack);
			}

			if (PotionHelper.isReagent(itemStack)) {
				potionIngredientsBuilder.add(itemStack);
			}
		}

		this.fuels = fuelsBuilder.build();
		this.potionIngredients = potionIngredientsBuilder.build();
	}

	@Override
	public <V> ImmutableList<V> getIngredients(@Nullable Class<V> ingredientClass) {
		if (ingredientClass == null) {
			Log.error("Null ingredientClass", new NullPointerException());
			return ImmutableList.of();
		}

		//noinspection unchecked
		ImmutableList<V> ingredients = ingredientsMap.get(ingredientClass);
		if (ingredients == null) {
			return ImmutableList.of();
		} else {
			return ingredients;
		}
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(V ingredient) {
		//noinspection unchecked
		return (IIngredientHelper<V>) getIngredientHelper(ingredient.getClass());
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(Class<V> ingredientClass) {
		//noinspection unchecked
		IIngredientHelper<V> ingredientHelper = ingredientHelperMap.get(ingredientClass);
		if (ingredientHelper == null) {
			throw new IllegalArgumentException("Unknown ingredient type: " + ingredientClass);
		}
		return ingredientHelper;
	}

	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(V ingredient) {
		//noinspection unchecked
		Class<V> ingredientClass = (Class<V>) ingredient.getClass();
		return getIngredientRenderer(ingredientClass);
	}

	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(Class<V> ingredientClass) {
		//noinspection unchecked
		IIngredientRenderer<V> ingredientRenderer = ingredientRendererMap.get(ingredientClass);
		if (ingredientRenderer == null) {
			throw new IllegalArgumentException("Could not find ingredient renderer for " + ingredientClass);
		}
		return ingredientRenderer;
	}

	@Override
	public ImmutableCollection<Class> getRegisteredIngredientClasses() {
		return ingredientsMap.keySet();
	}

	@Override
	public ImmutableList<ItemStack> getFuels() {
		return fuels;
	}

	@Override
	public ImmutableList<ItemStack> getPotionIngredients() {
		return potionIngredients;
	}
}
