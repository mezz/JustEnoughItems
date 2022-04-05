package mezz.jei.common.ingredients.subtypes;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.core.collect.Table;

import java.util.Optional;

public class SubtypeInterpreters {
	private final Table<IIngredientTypeWithSubtypes<?, ?>, Object, IIngredientSubtypeInterpreter<?>> table;

	public SubtypeInterpreters() {
		this.table = Table.identityHashBasedTable();
	}

	public <B, I> void addInterpreter(IIngredientTypeWithSubtypes<B, I> type, B base, IIngredientSubtypeInterpreter<I> interpreter) {
		table.put(type, base, interpreter);
	}

	public <B, I> Optional<IIngredientSubtypeInterpreter<I>> get(IIngredientTypeWithSubtypes<B, I> type, I ingredient) {
		B base = type.getBase(ingredient);
		IIngredientSubtypeInterpreter<?> interpreter = table.get(type, base);
		@SuppressWarnings("unchecked")
		IIngredientSubtypeInterpreter<I> cast = (IIngredientSubtypeInterpreter<I>) interpreter;
		return Optional.ofNullable(cast);
	}

	public <B> boolean contains(IIngredientTypeWithSubtypes<B, ?> type, B base) {
		return table.contains(type, base);
	}
}
