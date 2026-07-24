package mezz.jei.library.ingredients.subtypes;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public class SubtypeInterpreters {
	private final Map<Object, IIngredientSubtypeInterpreter<?>> map;

	public SubtypeInterpreters() {
		this.map = new IdentityHashMap<>();
	}

	public <B, I> boolean addInterpreter(IIngredientTypeWithSubtypes<B, I> type, B base, IIngredientSubtypeInterpreter<I> interpreter) {
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

	public <B, I> Optional<IIngredientSubtypeInterpreter<I>> get(IIngredientTypeWithSubtypes<B, I> type, I ingredient) {
		B base = type.getBase(ingredient);
		IIngredientSubtypeInterpreter<?> interpreter = map.get(base);
		@SuppressWarnings("unchecked")
		IIngredientSubtypeInterpreter<I> cast = (IIngredientSubtypeInterpreter<I>) interpreter;
		return Optional.ofNullable(cast);
	}

	public <B, T> boolean contains(IIngredientTypeWithSubtypes<B, T> type, T ingredient) {
		B base = type.getBase(ingredient);
		return map.containsKey(base);
	}
}
