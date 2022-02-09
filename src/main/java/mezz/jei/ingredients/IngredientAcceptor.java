package mezz.jei.ingredients;

import com.google.common.base.Preconditions;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.util.ErrorUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class IngredientAcceptor implements IIngredientAcceptor<IngredientAcceptor> {
	private final IIngredientManager ingredientManager;
	/**
	 * A list of ingredients, including "blank" ingredients represented by {@link Optional#empty()}.
	 * Blank ingredients are drawn as "nothing" in a rotation of ingredients, but aren't considered in lookups.
	 */
	private final List<Optional<ITypedIngredient<?>>> ingredients = new ArrayList<>();
	private final Set<IIngredientType<?>> types = new HashSet<>();

	public IngredientAcceptor(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@Override
	public IngredientAcceptor addIngredientsUnsafe(List<?> ingredients) {
		Preconditions.checkNotNull(ingredients, "ingredients");

		for (Object ingredient : ingredients) {
			Optional<ITypedIngredient<?>> typedIngredient = TypedIngredient.create(this.ingredientManager, ingredient);
			typedIngredient.ifPresent(i -> this.types.add(i.getType()));

			this.ingredients.add(typedIngredient);
		}

		return this;
	}

	@Override
	public <T> IngredientAcceptor addIngredients(IIngredientType<T> ingredientType, List<@Nullable T> ingredients) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		Preconditions.checkNotNull(ingredients, "ingredients");

		for (T ingredient : ingredients) {
			addIngredientInternal(ingredientType, ingredient);
		}

		return this;
	}

	@Override
	public <T> IngredientAcceptor addIngredient(IIngredientType<T> ingredientType, T ingredient) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		addIngredientInternal(ingredientType, ingredient);
		return this;
	}

	private <T> void addIngredientInternal(IIngredientType<T> ingredientType, @Nullable T ingredient) {
		Optional<ITypedIngredient<?>> typedIngredient = TypedIngredient.create(this.ingredientManager, ingredientType, ingredient);
		typedIngredient.ifPresent(i -> this.types.add(i.getType()));
		this.ingredients.add(typedIngredient);
	}

	public <T> Stream<T> getIngredients(IIngredientType<T> ingredientType) {
		return this.ingredients.stream()
			.flatMap(Optional::stream)
			.map(i -> TypedIngredient.optionalCast(i, ingredientType))
			.flatMap(Optional::stream)
			.map(ITypedIngredient::getIngredient);
	}

	public Stream<IIngredientType<?>> getIngredientTypes() {
		return this.types.stream();
	}

	@UnmodifiableView
	public List<Optional<ITypedIngredient<?>>> getAllIngredients() {
		return Collections.unmodifiableList(this.ingredients);
	}
}
