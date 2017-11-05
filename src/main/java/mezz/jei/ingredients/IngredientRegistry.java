package mezz.jei.ingredients;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.startup.IModIdHelper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.IngredientSet;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IngredientRegistry implements IIngredientRegistry {
	private final IModIdHelper modIdHelper;
	private final Map<Class, IngredientSet> ingredientsMap;
	private final ImmutableMap<Class, IIngredientHelper> ingredientHelperMap;
	private final ImmutableMap<Class, IIngredientRenderer> ingredientRendererMap;
	private final List<ItemStack> fuels = new ArrayList<>();
	private final List<ItemStack> potionIngredients = new ArrayList<>();

	public IngredientRegistry(
			IModIdHelper modIdHelper,
			Map<Class, IngredientSet> ingredientsMap,
			ImmutableMap<Class, IIngredientHelper> ingredientHelperMap,
			ImmutableMap<Class, IIngredientRenderer> ingredientRendererMap
	) {
		this.modIdHelper = modIdHelper;
		this.ingredientsMap = ingredientsMap;
		this.ingredientHelperMap = ingredientHelperMap;
		this.ingredientRendererMap = ingredientRendererMap;

		for (ItemStack itemStack : getAllIngredients(ItemStack.class)) {
			getStackProperties(itemStack);
		}
	}

	private void getStackProperties(ItemStack itemStack) {
		try {
			if (TileEntityFurnace.isItemFuel(itemStack)) {
				fuels.add(itemStack);
			}
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			Log.get().error("Failed to check if item is fuel {}.", itemStackInfo, e);
		}

		try {
			if (PotionHelper.isReagent(itemStack)) {
				potionIngredients.add(itemStack);
			}
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			Log.get().error("Failed to check if item is a potion ingredient {}.", itemStackInfo, e);
		}
	}

	@Override
	public <V> List<V> getIngredients(Class<V> ingredientClass) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");

		//noinspection unchecked
		IngredientSet<V> ingredients = ingredientsMap.get(ingredientClass);
		if (ingredients == null) {
			return ImmutableList.of();
		} else {
			return ImmutableList.copyOf(ingredients);
		}
	}

	@Override
	public <V> Collection<V> getAllIngredients(Class<V> ingredientClass) {
		//noinspection unchecked
		IngredientSet<V> ingredients = ingredientsMap.get(ingredientClass);
		if (ingredients == null) {
			return Collections.emptySet();
		} else {
			return Collections.unmodifiableCollection(ingredients);
		}
	}

	public <V> boolean isValidIngredient(V ingredient) {
		//noinspection unchecked
		IIngredientHelper<V> ingredientHelper = ingredientHelperMap.get(ingredient.getClass());
		return ingredientHelper != null && ingredientHelper.isValidIngredient(ingredient);
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		//noinspection unchecked
		return (IIngredientHelper<V>) getIngredientHelper(ingredient.getClass());
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(Class<? extends V> ingredientClass) {
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
	public <V> IIngredientRenderer<V> getIngredientRenderer(Class<? extends V> ingredientClass) {
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
		ErrorUtil.assertMainThread();
		addIngredientsAtRuntime(ingredientClass, ingredients, Internal.getIngredientFilter());
	}

	public <V> void addIngredientsAtRuntime(Class<V> ingredientClass, List<V> ingredients, IngredientFilter ingredientFilter) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");

		IIngredientHelper<V> ingredientHelper = getIngredientHelper(ingredientClass);
		//noinspection unchecked
		Set<V> set = ingredientsMap.computeIfAbsent(ingredientClass, k -> IngredientSet.create(ingredientClass, ingredientHelper));
		List<V> newIngredients = new ArrayList<>(ingredients.size());
		for (V ingredient : ingredients) {
			if (set.add(ingredient)) {
				newIngredients.add(ingredient);
			}
			if (ingredient instanceof ItemStack) {
				getStackProperties((ItemStack) ingredient);
			}
		}

		if (!newIngredients.isEmpty()) {
			NonNullList<IIngredientListElement> ingredientListElements = IngredientListElementFactory.createList(this, ingredientClass, newIngredients, modIdHelper);
			ingredientFilter.addIngredients(ingredientListElements);
		}
	}

	@Override
	public <V> void removeIngredientsAtRuntime(Class<V> ingredientClass, List<V> ingredients) {
		ErrorUtil.assertMainThread();
		removeIngredientsAtRuntime(ingredientClass, ingredients, Internal.getIngredientFilter());
	}

	public <V> void removeIngredientsAtRuntime(Class<V> ingredientClass, List<V> ingredients, IngredientFilter ingredientFilter) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");

		//noinspection unchecked
		IngredientSet<V> set = ingredientsMap.get(ingredientClass);
		if (set != null) {
			set.removeAll(ingredients);
		}

		NonNullList<IIngredientListElement> ingredientListElements = IngredientListElementFactory.createList(this, ingredientClass, ingredients, modIdHelper);
		ingredientFilter.removeIngredients(ingredientListElements);
	}
}
