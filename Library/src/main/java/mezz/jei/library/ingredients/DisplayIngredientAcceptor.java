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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class DisplayIngredientAcceptor implements IIngredientAcceptor<DisplayIngredientAcceptor> {
	private final IIngredientManager ingredientManager;
	/**
	 * A list of ingredients, including "blank" ingredients represented by {@link Optional#empty()}.
	 * Blank ingredients are drawn as "nothing" in a rotation of ingredients, but aren't considered in lookups.
	 */
	private final List<Optional<ITypedIngredient<?>>> ingredients = new ArrayList<>();

	public DisplayIngredientAcceptor(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@Override
	public DisplayIngredientAcceptor addIngredientsUnsafe(List<?> ingredients) {
		Preconditions.checkNotNull(ingredients, "ingredients");

		for (Object ingredient : ingredients) {
			Optional<ITypedIngredient<?>> typedIngredient = TypedIngredient.createAndFilterInvalid(ingredientManager, ingredient, false);
			this.ingredients.add(typedIngredient);
		}

		return this;
	}

	@Override
	public <T> DisplayIngredientAcceptor addIngredients(IIngredientType<T> ingredientType, List<@Nullable T> ingredients) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		Preconditions.checkNotNull(ingredients, "ingredients");

		List<Optional<ITypedIngredient<T>>> typedIngredients = TypedIngredient.createAndFilterInvalidList(this.ingredientManager, ingredientType, ingredients, false);

		if (!typedIngredients.isEmpty()) {
			for (Optional<ITypedIngredient<T>> typedIngredientOptional : typedIngredients) {
				this.ingredients.add(typedIngredientOptional.map(Function.identity()));
			}
		}

		return this;
	}

	@Override
	public <T> DisplayIngredientAcceptor addIngredient(IIngredientType<T> ingredientType, T ingredient) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		addIngredientInternal(ingredientType, ingredient);
		return this;
	}

	@Override
	public <I> DisplayIngredientAcceptor addTypedIngredient(ITypedIngredient<I> typedIngredient) {
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");

		Optional<ITypedIngredient<I>> copy = TypedIngredient.deepCopy(ingredientManager, typedIngredient);
		this.ingredients.add(copy.map(Function.identity()));

		return this;
	}

	@Override
	public DisplayIngredientAcceptor addFluidStack(Fluid fluid, long amount) {
		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		return addFluidInternal(fluidHelper, fluid, amount, null);
	}

	@Override
	public DisplayIngredientAcceptor addFluidStack(Fluid fluid, long amount, CompoundTag tag) {
		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		return addFluidInternal(fluidHelper, fluid, amount, tag);
	}

	private <T> DisplayIngredientAcceptor addFluidInternal(IPlatformFluidHelperInternal<T> fluidHelper, Fluid fluid, long amount, @Nullable CompoundTag tag) {
		T fluidStack = fluidHelper.create(fluid, amount, tag);
		IIngredientTypeWithSubtypes<Fluid, T> fluidIngredientType = fluidHelper.getFluidIngredientType();
		addIngredientInternal(fluidIngredientType, fluidStack);
		return this;
	}

	@Override
	public DisplayIngredientAcceptor addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		ErrorUtil.checkNotNull(ingredients, "ingredients");

		for (ITypedIngredient<?> typedIngredient : ingredients) {
			this.addTypedIngredient(typedIngredient);
		}
		return this;
	}

	@Override
	public DisplayIngredientAcceptor addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		ErrorUtil.checkNotNull(ingredients, "ingredients");

		for (Optional<ITypedIngredient<?>> o : ingredients) {
			if (o.isPresent()) {
				this.addTypedIngredient(o.get());
			} else {
				this.ingredients.add(o);
			}
		}

		return this;
	}

	private <T> void addIngredientInternal(IIngredientType<T> ingredientType, @Nullable T ingredient) {
		Optional<ITypedIngredient<T>> typedIngredient = TypedIngredient.createAndFilterInvalid(this.ingredientManager, ingredientType, ingredient, false);
		this.ingredients.add(typedIngredient.map(Function.identity()));
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
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
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
