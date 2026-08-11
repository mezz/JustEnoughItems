package mezz.jei.common.ingredients;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.ingredients.itemStacks.TypedItemStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class TypedIngredient<T> implements ITypedIngredient<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	private static <T> void checkParameters(IIngredientType<T> ingredientType, T ingredient) {
		Preconditions.checkNotNull(ingredientType, "ingredientType");
		Preconditions.checkNotNull(ingredient, "ingredient");

		Class<? extends T> ingredientClass = ingredientType.getIngredientClass();
		if (!ingredientClass.isInstance(ingredient)) {
			throw new IllegalArgumentException("Invalid ingredient found. " +
				" Should be an instance of: " + ingredientClass + " Instead got: " + ingredient.getClass());
		}
	}

	public static <T> ITypedIngredient<T> createUnvalidated(IIngredientType<T> ingredientType, T ingredient) {
		ItemStack itemStack = VanillaTypes.ITEM_STACK.getCastIngredient(ingredient);
		if (itemStack != null) {
			ITypedIngredient<ItemStack> typedIngredient = TypedItemStack.create(itemStack);
			@SuppressWarnings("unchecked")
			ITypedIngredient<T> castIngredient = (ITypedIngredient<T>) typedIngredient;
			return castIngredient;
		}

		return new TypedIngredient<>(ingredientType, ingredient);
	}

	@Nullable
	public static <T> ITypedIngredient<?> createAndFilterInvalid(
		IIngredientManager ingredientManager,
		@Nullable T ingredient,
		boolean normalize
	) {
		if (ingredient == null) {
			return null;
		}
		IIngredientType<T> ingredientType = ingredientManager.getIngredientTypeOrNull(ingredient);
		if (ingredientType == null) {
			return null;
		}
		return createAndFilterInvalid(ingredientManager, ingredientType, ingredient, normalize);
	}

	@Nullable
	public static <T> ITypedIngredient<?> createAndFilterInvalidForDisplay(
		IIngredientManager ingredientManager,
		@Nullable T ingredient,
		boolean normalize
	) {
		if (ingredient == null) {
			return null;
		}
		IIngredientType<T> ingredientType = ingredientManager.getIngredientTypeOrNull(ingredient);
		if (ingredientType == null) {
			return null;
		}
		return createAndFilterInvalidForDisplay(ingredientManager, ingredientType, ingredient, normalize);
	}

	@Nullable
	public static <T> ITypedIngredient<T> createAndFilterInvalid(
		IIngredientManager ingredientManager,
		IIngredientType<T> ingredientType,
		@Nullable T ingredient,
		boolean normalize
	) {
		if (ingredient == null) {
			return null;
		}

		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		return createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, normalize);
	}

	@Nullable
	public static <T> ITypedIngredient<T> createAndFilterInvalidForDisplay(
		IIngredientManager ingredientManager,
		IIngredientType<T> ingredientType,
		@Nullable T ingredient,
		boolean normalize
	) {
		if (ingredient == null) {
			return null;
		}

		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		return createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, normalize, false);
	}

	public static <T> List<ITypedIngredient<T>> createAndFilterInvalidNonnullList(
		IIngredientManager ingredientManager,
		IIngredientType<T> ingredientType,
		Collection<T> ingredients,
		boolean normalize
	) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		List<ITypedIngredient<T>> results = new ArrayList<>(ingredients.size());
		for (T ingredient : ingredients) {
			@Nullable ITypedIngredient<T> result = createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, normalize);
			if (result != null) {
				results.add(result);
			}
		}
		return results;
	}

	public static <T> List<@Nullable ITypedIngredient<T>> createAndFilterInvalidListForDisplay(
		IIngredientManager ingredientManager,
		IIngredientType<T> ingredientType,
		List<@Nullable T> ingredients,
		boolean normalize
	) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		List<@Nullable ITypedIngredient<T>> results = new ArrayList<>(ingredients.size());
		for (@Nullable T ingredient : ingredients) {
			@Nullable ITypedIngredient<T> result = createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, normalize, false);
			results.add(result);
		}
		return results;
	}

	public static List<@Nullable ITypedIngredient<ItemStack>> createAndFilterInvalidListForDisplay(
		IIngredientManager ingredientManager,
		Ingredient ingredient,
		boolean normalize
	) {
		ItemStack[] itemStacks = ingredient.getItems();
		IIngredientHelper<ItemStack> ingredientHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);

		List<@Nullable ITypedIngredient<ItemStack>> results = new ArrayList<>(itemStacks.length);
		for (ItemStack itemStack : itemStacks) {
			@Nullable ITypedIngredient<ItemStack> result = createAndFilterInvalid(ingredientHelper, VanillaTypes.ITEM_STACK, itemStack, normalize, false);
			results.add(result);
		}
		return results;
	}

	public static <T> List<@Nullable ITypedIngredient<T>> createAndFilterInvalidList(
		IIngredientManager ingredientManager,
		IIngredientType<T> ingredientType,
		List<@Nullable T> ingredients,
		boolean normalize
	) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		List<@Nullable ITypedIngredient<T>> results = new ArrayList<>(ingredients.size());
		for (@Nullable T ingredient : ingredients) {
			@Nullable ITypedIngredient<T> result = createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, normalize);
			results.add(result);
		}
		return results;
	}

	public static List<@Nullable ITypedIngredient<ItemStack>> createAndFilterInvalidList(IIngredientManager ingredientManager, Ingredient ingredient, boolean normalize) {
		ItemStack[] itemStacks = ingredient.getItems();
		IIngredientHelper<ItemStack> ingredientHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
		List<@Nullable ITypedIngredient<ItemStack>> results = new ArrayList<>(itemStacks.length);
		for (ItemStack itemStack : itemStacks) {
			ITypedIngredient<ItemStack> result = createAndFilterInvalid(ingredientHelper, VanillaTypes.ITEM_STACK, itemStack, normalize);
			results.add(result);
		}
		return results;
	}
	@Nullable
	public static <T> ITypedIngredient<T> createAndFilterInvalid(
		IIngredientHelper<T> ingredientHelper,
		IIngredientType<T> ingredientType,
		@Nullable T ingredient,
		boolean normalize
	) {
		return createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, normalize, true);
	}

	@Nullable
	private static <T> ITypedIngredient<T> createAndFilterInvalid(
		IIngredientHelper<T> ingredientHelper,
		IIngredientType<T> ingredientType,
		@Nullable T ingredient,
		boolean normalize,
		boolean checkServer
	) {
		if (ingredient == null) {
			return null;
		}
		try {
			if (normalize) {
				ingredient = ingredientHelper.normalizeIngredient(ingredient);
			}
			if (!ingredientHelper.isValidIngredient(ingredient)) {
				return null;
			}
			if (checkServer && !ingredientHelper.isIngredientOnServer(ingredient)) {
				String errorInfo = ingredientHelper.getErrorInfo(ingredient);
				LOGGER.warn("Ignoring ingredient that isn't on the server: {}", errorInfo);
				return null;
			}
		} catch (RuntimeException e) {
			String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
			throw new IllegalArgumentException("Crashed when checking if ingredient is valid. Ingredient Info: " + ingredientInfo, e);
		}

		return createUnvalidated(ingredientType, ingredient);
	}

	private final IIngredientType<T> ingredientType;
	private final T ingredient;

	private TypedIngredient(IIngredientType<T> ingredientType, T ingredient) {
		checkParameters(ingredientType, ingredient);
		this.ingredientType = ingredientType;
		this.ingredient = ingredient;
	}

	@Override
	public ITypedIngredient<T> normalize(IIngredientHelper<T> ingredientHelper) {
		T normalized = ingredientHelper.normalizeIngredient(ingredient);
		return createUnvalidated(ingredientType, normalized);
	}

	@Override
	public T getIngredient() {
		return this.ingredient;
	}

	@Override
	public IIngredientType<T> getType() {
		return this.ingredientType;
	}

	@Override
	@Nullable
	public <V> ITypedIngredient<V> cast(IIngredientType<V> ingredientType) {
		if (getType().equals(ingredientType)) {
			@SuppressWarnings("unchecked")
			ITypedIngredient<V> cast = (ITypedIngredient<V>) this;
			return cast;
		}
		return null;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("type", ingredientType)
			.add("ingredient", ingredient)
			.toString();
	}
}
