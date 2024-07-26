package mezz.jei.library.ingredients;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
			Optional<ITypedIngredient<?>> typedIngredient = TypedIngredient.createAndFilterInvalid(ingredientManager, ingredient, false);
			typedIngredient.ifPresent(i -> this.types.add(i.getType()));

			this.ingredients.add(typedIngredient);
		}

		return this;
	}

	@Override
	public <T> IngredientAcceptor addIngredients(IIngredientType<T> ingredientType, List<@Nullable T> ingredients) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		Preconditions.checkNotNull(ingredients, "ingredients");

		List<Optional<ITypedIngredient<T>>> typedIngredients = TypedIngredient.createAndFilterInvalidList(this.ingredientManager, ingredientType, ingredients, false);

		if (!typedIngredients.isEmpty()) {
			boolean anyPresent = false;
			for (Optional<ITypedIngredient<T>> typedIngredientOptional : typedIngredients) {
				this.ingredients.add(typedIngredientOptional.map(Function.identity()));
				if (!anyPresent && typedIngredientOptional.isPresent()) {
					anyPresent = true;
				}
			}
			if (anyPresent) {
				this.types.add(ingredientType);
			}
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

	@SuppressWarnings("deprecation")
	@Override
	public IngredientAcceptor addFluidStack(Fluid fluid, long amount) {
		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		return addFluidInternal(fluidHelper, fluid.builtInRegistryHolder(), amount, DataComponentPatch.EMPTY);
	}

	@SuppressWarnings("deprecation")
	@Override
	public IngredientAcceptor addFluidStack(Fluid fluid, long amount, DataComponentPatch componentPatch) {
		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		return addFluidInternal(fluidHelper, fluid.builtInRegistryHolder(), amount, componentPatch);
	}

	private <T> IngredientAcceptor addFluidInternal(IPlatformFluidHelperInternal<T> fluidHelper, Holder<Fluid> fluid, long amount, DataComponentPatch tag) {
		T fluidStack = fluidHelper.create(fluid, amount, tag);
		IIngredientTypeWithSubtypes<Fluid, T> fluidIngredientType = fluidHelper.getFluidIngredientType();
		addIngredientInternal(fluidIngredientType, fluidStack);
		return this;
	}

	private <T> void addIngredientInternal(IIngredientType<T> ingredientType, @Nullable T ingredient) {
		Optional<ITypedIngredient<T>> typedIngredient = TypedIngredient.createAndFilterInvalid(this.ingredientManager, ingredientType, ingredient, false);
		typedIngredient.ifPresent(i -> this.types.add(i.getType()));
		this.ingredients.add(typedIngredient.map(Function.identity()));
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
		List<IFocus<?>> focuses = focusGroup.getFocuses(role).toList();
		IntSet results = new IntOpenHashSet();
		for (IFocus<?> focus : focuses) {
			getMatches(focus, results);
		}
		return results;
	}

	private <T> void getMatches(IFocus<T> focus, IntSet results) {
		List<Optional<ITypedIngredient<?>>> ingredients = getAllIngredients();
		if (ingredients.isEmpty()) {
			return;
		}

		ITypedIngredient<T> focusValue = focus.getTypedValue();
		IIngredientType<T> ingredientType = focusValue.getType();
		T focusIngredient = focusValue.getIngredient();
		IIngredientHelper<T> ingredientHelper = this.ingredientManager.getIngredientHelper(ingredientType);
		String focusUid = ingredientHelper.getUniqueId(focusIngredient, UidContext.Ingredient);

		for (int i = 0; i < ingredients.size(); i++) {
			Optional<ITypedIngredient<?>> typedIngredientOptional = ingredients.get(i);
			if (typedIngredientOptional.isEmpty()) {
				continue;
			}
			ITypedIngredient<?> typedIngredient = typedIngredientOptional.get();
			Optional<T> ingredientOptional = typedIngredient.getIngredient(ingredientType);
			if (ingredientOptional.isEmpty()) {
				continue;
			}
			T ingredient = ingredientOptional.get();
			String uniqueId = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
			if (focusUid.equals(uniqueId)) {
				results.add(i);
			}
		}
	}
}
