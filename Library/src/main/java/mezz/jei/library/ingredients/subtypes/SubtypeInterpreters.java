package mezz.jei.library.ingredients.subtypes;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public class SubtypeInterpreters {
	private final Map<Object, ISubtypeInterpreter<?>> map;

	public SubtypeInterpreters() {
		this.map = new IdentityHashMap<>();
	}

	public <B, I> boolean addInterpreter(IIngredientTypeWithSubtypes<B, I> type, B base, ISubtypeInterpreter<I> interpreter) {
		if (!type.getIngredientBaseClass().isInstance(base)) {
			throw new IllegalArgumentException(
				String.format("base must be instance of %s but got %s instead", type.getIngredientBaseClass(), base.getClass())
			);
		}
		if (map.containsKey(base)) {
			return false;
		}
		map.put(base, interpreter);
		return true;
	}

	@SuppressWarnings("removal")
	public <B, I> boolean addInterpreter(IIngredientTypeWithSubtypes<B, I> type, B base, mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter<I> interpreter) {
		if (!type.getIngredientBaseClass().isInstance(base)) {
			throw new IllegalArgumentException(
				String.format("base must be instance of %s but got %s instead", type.getIngredientBaseClass(), base.getClass())
			);
		}
		if (map.containsKey(base)) {
			return false;
		}
		map.put(base, new LegacyInterpreterAdapter<>(interpreter));
		return true;
	}

	@Nullable
	public <B, I> ISubtypeInterpreter<I> get(IIngredientTypeWithSubtypes<B, I> type, I ingredient) {
		B base = type.getBase(ingredient);
		return getFromBase(type, base);
	}

	@Nullable
	public <B, I> ISubtypeInterpreter<I> getFromBase(@SuppressWarnings("unused") IIngredientTypeWithSubtypes<B, I> type, B ingredientBase) {
		ISubtypeInterpreter<?> interpreter = map.get(ingredientBase);
		@SuppressWarnings("unchecked")
		ISubtypeInterpreter<I> cast = (ISubtypeInterpreter<I>) interpreter;
		return cast;
	}

	public <B, T> boolean contains(IIngredientTypeWithSubtypes<B, T> type, T ingredient) {
		B base = type.getBase(ingredient);
		return map.containsKey(base);
	}
}
