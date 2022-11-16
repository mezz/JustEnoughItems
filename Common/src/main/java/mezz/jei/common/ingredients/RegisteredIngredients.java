package mezz.jei.common.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientInfo;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RegisteredIngredients implements IRegisteredIngredients {
	/** preserves a stable ordering of the types */
	@Unmodifiable
	private final List<IIngredientType<?>> orderedTypes;

	/** for looking up ingredient info for each type */
	@Unmodifiable
	private final Map<IIngredientType<?>, IngredientInfo<?>> typeToInfo;

	/** for looking up types by ingredient class */
	private final Map<Class<?>, IIngredientType<?>> classToType;

	public RegisteredIngredients(List<IngredientInfo<?>> ingredientInfoList) {
		this.orderedTypes = ingredientInfoList.stream()
			.<IIngredientType<?>>map(IngredientInfo::getIngredientType)
			.toList();

		this.typeToInfo = ingredientInfoList.stream()
			.collect(Collectors.toUnmodifiableMap(IngredientInfo::getIngredientType, Function.identity()));

		this.classToType = this.orderedTypes.stream()
			.collect(Collectors.toMap(IIngredientType::getIngredientClass, Function.identity()));
	}

	@Override
	public <V> IngredientInfo<V> getIngredientInfo(IIngredientType<V> ingredientType) {
		@SuppressWarnings("unchecked")
		IngredientInfo<V> ingredientInfo = (IngredientInfo<V>) typeToInfo.get(ingredientType);
		if (ingredientInfo == null) {
			throw new IllegalArgumentException("Unknown ingredient type: " + ingredientType.getIngredientClass());
		}
		return ingredientInfo;
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(IIngredientType<V> ingredientType) {
		IIngredientInfo<V> ingredientInfo = getIngredientInfo(ingredientType);
		return ingredientInfo.getIngredientHelper();
	}

	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> ingredientType) {
		IIngredientInfo<V> ingredientInfo = getIngredientInfo(ingredientType);
		return ingredientInfo.getIngredientRenderer();
	}

	@Override
	@Unmodifiable
	public List<IIngredientType<?>> getIngredientTypes() {
		return this.orderedTypes;
	}

	@Override
	public <V> IIngredientType<V> getIngredientType(V ingredient) {
		@SuppressWarnings("unchecked")
		Class<? extends V> ingredientClass = (Class<? extends V>) ingredient.getClass();
		return getIngredientType(ingredientClass);
	}

	public <V> IIngredientType<V> getIngredientType(Class<? extends V> ingredientClass) {
		@SuppressWarnings("unchecked")
		IIngredientType<V> ingredientType = (IIngredientType<V>) this.classToType.get(ingredientClass);
		if (ingredientType != null) {
			return ingredientType;
		}
		for (IIngredientType<?> type : this.orderedTypes) {
			if (type.getIngredientClass().isAssignableFrom(ingredientClass)) {
				@SuppressWarnings("unchecked")
				IIngredientType<V> castType = (IIngredientType<V>) type;
				this.classToType.put(ingredientClass, castType);
				return castType;
			}
		}
		throw new IllegalArgumentException("Unknown ingredient class: " + ingredientClass);
	}

}
