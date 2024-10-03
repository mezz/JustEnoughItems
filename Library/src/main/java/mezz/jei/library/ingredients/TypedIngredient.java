package mezz.jei.library.ingredients;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.ingredients.itemStacks.TypedItemStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class TypedIngredient<T> implements ITypedIngredient<T> {
	private static <T> void checkParameters(IIngredientType<T> ingredientType, T ingredient) {
		Preconditions.checkNotNull(ingredientType, "ingredientType");
		Preconditions.checkNotNull(ingredient, "ingredient");

		Class<? extends T> ingredientClass = ingredientType.getIngredientClass();
		if (!ingredientClass.isInstance(ingredient)) {
			throw new IllegalArgumentException("Invalid ingredient found. " +
				" Should be an instance of: " + ingredientClass + " Instead got: " + ingredient.getClass());
		}
	}

	public static <T> ITypedIngredient<T> normalize(ITypedIngredient<T> typedIngredient, IIngredientHelper<T> ingredientHelper) {
		IIngredientType<T> type = typedIngredient.getType();

		if (type == VanillaTypes.ITEM_STACK) {
			@SuppressWarnings("unchecked")
			ITypedIngredient<ItemStack> cast = (ITypedIngredient<ItemStack>) typedIngredient;
			ITypedIngredient<ItemStack> normalized = TypedItemStack.normalize(cast);
			@SuppressWarnings("unchecked")
			ITypedIngredient<T> castNormalized = (ITypedIngredient<T>) normalized;
			return castNormalized;
		}

		T ingredient = typedIngredient.getIngredient();
		T normalized = ingredientHelper.normalizeIngredient(ingredient);
		return createUnvalidated(type, normalized);
	}

	public static <T> ITypedIngredient<T> createUnvalidated(IIngredientType<T> ingredientType, T ingredient) {
		if (ingredientType == VanillaTypes.ITEM_STACK) {
			ITypedIngredient<ItemStack> typedIngredient = TypedItemStack.create((ItemStack) ingredient);
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
		IIngredientType<T> type = ingredientManager.getIngredientType(ingredient);
		if (type == null) {
			return null;
		}
		return createAndFilterInvalid(ingredientManager, type, ingredient, normalize);
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
		} catch (RuntimeException e) {
			String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
			throw new IllegalArgumentException("Crashed when checking if ingredient is valid. Ingredient Info: " + ingredientInfo, e);
		}

		return createUnvalidated(ingredientType, ingredient);
	}

	@Nullable
	public static <T> ITypedIngredient<T> defensivelyCopyTypedIngredientFromApi(IIngredientManager ingredientManager, ITypedIngredient<T> value) {
		if (value instanceof TypedItemStack || value instanceof TypedIngredient) {
			return value;
		}
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		T ingredient = ingredientHelper.copyIngredient(value.getIngredient());
		return TypedIngredient.createAndFilterInvalid(ingredientManager, value.getType(), ingredient, false);
	}

	private final IIngredientType<T> ingredientType;
	private final T ingredient;

	private TypedIngredient(IIngredientType<T> ingredientType, T ingredient) {
		checkParameters(ingredientType, ingredient);
		this.ingredientType = ingredientType;
		this.ingredient = ingredient;
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
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("type", ingredientType.getUid())
			.add("ingredient", ingredient)
			.toString();
	}
}
