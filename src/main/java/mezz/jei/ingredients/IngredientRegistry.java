package mezz.jei.ingredients;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.gui.overlay.ItemListOverlay;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.tileentity.TileEntityFurnace;

public class IngredientRegistry implements IIngredientRegistry {
	private final Map<Class, List> ingredientsMap;
	private final ImmutableMap<Class, IIngredientHelper> ingredientHelperMap;
	private final ImmutableMap<Class, IIngredientRenderer> ingredientRendererMap;
	private final List<ItemStack> fuels = new ArrayList<ItemStack>();
	private final List<ItemStack> potionIngredients = new ArrayList<ItemStack>();

	public IngredientRegistry(
			Map<Class, List> ingredientsMap,
			ImmutableMap<Class, IIngredientHelper> ingredientHelperMap,
			ImmutableMap<Class, IIngredientRenderer> ingredientRendererMap
	) {
		this.ingredientsMap = ingredientsMap;
		this.ingredientHelperMap = ingredientHelperMap;
		this.ingredientRendererMap = ingredientRendererMap;

		for (ItemStack itemStack : getIngredients(ItemStack.class)) {
			getStackProperties(itemStack);
		}
	}

	private void getStackProperties(ItemStack itemStack) {
		try {
			if (TileEntityFurnace.isItemFuel(itemStack)) {
				fuels.add(itemStack);
			}
		} catch (RuntimeException e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			Log.error("Failed to check if item is fuel {}.", itemStackInfo, e);
		} catch (LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			Log.error("Failed to check if item is fuel {}.", itemStackInfo, e);
		}

		try {
			if (PotionHelper.isReagent(itemStack)) {
				potionIngredients.add(itemStack);
			}
		} catch (RuntimeException e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			Log.error("Failed to check if item is a potion ingredient {}.", itemStackInfo, e);
		} catch (LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			Log.error("Failed to check if item is a potion ingredient {}.", itemStackInfo, e);
		}
	}

	@Override
	public <V> List<V> getIngredients(Class<V> ingredientClass) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");

		//noinspection unchecked
		List<V> ingredients = ingredientsMap.get(ingredientClass);
		if (ingredients == null) {
			return ImmutableList.of();
		} else {
			return Collections.unmodifiableList(ingredients);
		}
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		//noinspection unchecked
		return (IIngredientHelper<V>) getIngredientHelper(ingredient.getClass());
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(Class<V> ingredientClass) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");

		//noinspection unchecked
		IIngredientHelper<V> ingredientHelper = ingredientHelperMap.get(ingredientClass);
		if (ingredientHelper == null) {
			throw new IllegalArgumentException("Unknown ingredient type: " + ingredientClass);
		}
		return ingredientHelper;
	}

	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		//noinspection unchecked
		Class<V> ingredientClass = (Class<V>) ingredient.getClass();
		return getIngredientRenderer(ingredientClass);
	}

	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(Class<V> ingredientClass) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");

		//noinspection unchecked
		IIngredientRenderer<V> ingredientRenderer = ingredientRendererMap.get(ingredientClass);
		if (ingredientRenderer == null) {
			throw new IllegalArgumentException("Could not find ingredient renderer for " + ingredientClass);
		}
		return ingredientRenderer;
	}

	@Override
	public Collection<Class> getRegisteredIngredientClasses() {
		return Collections.unmodifiableCollection(ingredientsMap.keySet());
	}

	@Override
	public List<ItemStack> getFuels() {
		return Collections.unmodifiableList(fuels);
	}

	@Override
	public List<ItemStack> getPotionIngredients() {
		return Collections.unmodifiableList(potionIngredients);
	}

	@Override
	public <V> void addIngredientsAtRuntime(Class<V> ingredientClass, List<V> ingredients) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");

		//noinspection unchecked
		List<V> list = ingredientsMap.get(ingredientClass);
		if (list == null) {
			list = new ArrayList<V>();
			ingredientsMap.put(ingredientClass, list);
		}
		list.addAll(ingredients);

		JeiRuntime runtime = Internal.getRuntime();
		if (runtime != null) {
			ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
			itemListOverlay.rebuildItemFilter();
		}
	}

	@Override
	public <V> void removeIngredientsAtRuntime(Class<V> ingredientClass, List<V> ingredients) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");

		//noinspection unchecked
		List<V> list = ingredientsMap.get(ingredientClass);
		if (list != null) {
			list.removeAll(ingredients);
		}

		JeiRuntime runtime = Internal.getRuntime();
		if (runtime != null) {
			ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
			itemListOverlay.rebuildItemFilter();
		}
	}
}
