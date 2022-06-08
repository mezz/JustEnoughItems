package mezz.jei.common.ingredients;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.util.ErrorUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IngredientAcceptor implements IIngredientAcceptor<IngredientAcceptor> {
	private final RegisteredIngredients registeredIngredients;
	/**
	 * A list of ingredients, including "blank" ingredients represented by {@link Optional#empty()}.
	 * Blank ingredients are drawn as "nothing" in a rotation of ingredients, but aren't considered in lookups.
	 */
	private final List<Optional<ITypedIngredient<?>>> ingredients = new ArrayList<>();
	private final Set<IIngredientType<?>> types = new HashSet<>();

	public IngredientAcceptor(RegisteredIngredients registeredIngredients) {
		this.registeredIngredients = registeredIngredients;
	}

	@Override
	public IngredientAcceptor addIngredientsUnsafe(List<?> ingredients) {
		Preconditions.checkNotNull(ingredients, "ingredients");

		for (Object ingredient : ingredients) {
			Optional<ITypedIngredient<?>> typedIngredient = TypedIngredient.create(this.registeredIngredients, ingredient);
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
		Optional<ITypedIngredient<?>> typedIngredient = TypedIngredient.create(this.registeredIngredients, ingredientType, ingredient);
		typedIngredient.ifPresent(i -> this.types.add(i.getType()));
		this.ingredients.add(typedIngredient);
	}

	public <T> Stream<T> getIngredients(IIngredientType<T> ingredientType) {
		return this.ingredients.stream()
			.flatMap(Optional::stream)
			.map(i -> i.getIngredient(ingredientType))
			.flatMap(Optional::stream);
	}

	public Stream<IIngredientType<?>> getIngredientTypes() {
		return this.types.stream();
	}

	@UnmodifiableView
	public List<Optional<ITypedIngredient<?>>> getAllIngredients() {
		return Collections.unmodifiableList(this.ingredients);
	}

	public IntSet getMatches(IFocusGroup focusGroup, RecipeIngredientRole role) {
		int[] matches = focusGroup.getFocuses(role)
			.flatMapToInt(this::getMatches)
			.distinct()
			.toArray();
		return new IntArraySet(matches);
	}

	private <T> IntStream getMatches(IFocus<T> focus) {
		List<Optional<ITypedIngredient<?>>> ingredients = getAllIngredients();
		if (ingredients.isEmpty()) {
			return IntStream.empty();
		}

		ITypedIngredient<T> focusValue = focus.getTypedValue();
		IIngredientType<T> ingredientType = focusValue.getType();
		T focusIngredient = focusValue.getIngredient();
		IIngredientHelper<T> ingredientHelper = this.registeredIngredients.getIngredientHelper(ingredientType);
		String focusUid = ingredientHelper.getUniqueId(focusIngredient, UidContext.Ingredient);

		return IntStream.range(0, ingredients.size())
			.filter(i ->
				ingredients.get(i)
					.flatMap(typedIngredient -> typedIngredient.getIngredient(ingredientType))
					.map(ingredient -> {
						String uniqueId = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
						return focusUid.equals(uniqueId);
					})
					.orElse(false)
			);
	}
}
