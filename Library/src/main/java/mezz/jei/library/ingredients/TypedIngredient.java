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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

	public static <T> Optional<ITypedIngredient<?>> createAndFilterInvalid(
		IIngredientManager ingredientManager,
		@Nullable T ingredient,
		boolean normalize
	) {
		if (ingredient == null) {
			return Optional.empty();
		}
		return ingredientManager.getIngredientTypeChecked(ingredient)
			.flatMap(ingredientType -> createAndFilterInvalid(ingredientManager, ingredientType, ingredient, normalize));
	}

	public static <T> Optional<ITypedIngredient<T>> createAndFilterInvalid(
		IIngredientManager ingredientManager,
		IIngredientType<T> ingredientType,
		@Nullable T ingredient,
		boolean normalize
	) {
		if (ingredient == null) {
			return Optional.empty();
		}

		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		return createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, normalize);
	}

	public static <T> List<Optional<ITypedIngredient<T>>> createAndFilterInvalidList(
		IIngredientManager ingredientManager,
		IIngredientType<T> ingredientType,
		List<@Nullable T> ingredients,
		boolean normalize
	) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		List<Optional<ITypedIngredient<T>>> results = new ArrayList<>(ingredients.size());
		for (T ingredient : ingredients) {
			if (ingredient == null) {
				results.add(Optional.empty());
			} else {
				Optional<ITypedIngredient<T>> result = createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, normalize);
				results.add(result);
			}
		}
		return results;
	}

	public static <T> Optional<ITypedIngredient<T>> createAndFilterInvalid(
		IIngredientHelper<T> ingredientHelper,
		IIngredientType<T> ingredientType,
		T ingredient,
		boolean normalize
	) {
		try {
			if (normalize) {
				ingredient = ingredientHelper.normalizeIngredient(ingredient);
			}
			if (!ingredientHelper.isValidIngredient(ingredient)) {
				return Optional.empty();
			}
			if (!ingredientHelper.isIngredientOnServer(ingredient)) {
				String errorInfo = ingredientHelper.getErrorInfo(ingredient);
				LOGGER.warn("Ignoring ingredient that isn't on the server: {}", errorInfo);
				return Optional.empty();
			}
		} catch (RuntimeException e) {
			String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
			throw new IllegalArgumentException("Crashed when checking if ingredient is valid. Ingredient Info: " + ingredientInfo, e);
		}

		ITypedIngredient<T> typedIngredient = createUnvalidated(ingredientType, ingredient);
		return Optional.of(typedIngredient);
	}

	public static <T> Optional<ITypedIngredient<T>> deepCopy(IIngredientManager ingredientManager, ITypedIngredient<T> value) {
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
			.add("type", ingredientType)
			.add("ingredient", ingredient)
			.toString();
	}
}
