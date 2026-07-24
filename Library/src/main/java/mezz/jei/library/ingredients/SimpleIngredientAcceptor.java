package mezz.jei.library.ingredients;

import com.google.common.base.Preconditions;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
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

/**
 * Minimal version of {@link DisplayIngredientAcceptor} that can only return the ingredients,
 * but doesn't bother building anything for drawing on screen.
 */
@SuppressWarnings("OptionalIsPresent")
public class SimpleIngredientAcceptor implements IIngredientAcceptor<SimpleIngredientAcceptor> {
	private final IIngredientManager ingredientManager;
	private final List<ITypedIngredient<?>> ingredients = new ArrayList<>();

	public SimpleIngredientAcceptor(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@Override
	public SimpleIngredientAcceptor addIngredientsUnsafe(List<?> ingredients) {
		Preconditions.checkNotNull(ingredients, "ingredients");

		for (Object ingredient : ingredients) {
			Optional<ITypedIngredient<?>> typedIngredient = TypedIngredient.createAndFilterInvalid(ingredientManager, ingredient, false);
			if (typedIngredient.isPresent()) {
				this.ingredients.add(typedIngredient.get());
			}
		}

		return this;
	}

	@Override
	public <T> SimpleIngredientAcceptor addIngredients(IIngredientType<T> ingredientType, List<@Nullable T> ingredients) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		Preconditions.checkNotNull(ingredients, "ingredients");

		List<Optional<ITypedIngredient<T>>> typedIngredients = TypedIngredient.createAndFilterInvalidList(this.ingredientManager, ingredientType, ingredients, false);

		for (Optional<ITypedIngredient<T>> typedIngredientOptional : typedIngredients) {
			if (typedIngredientOptional.isPresent()) {
				this.ingredients.add(typedIngredientOptional.get());
			}
		}

		return this;
	}

	@Override
	public <T> SimpleIngredientAcceptor addIngredient(IIngredientType<T> ingredientType, T ingredient) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		addIngredientInternal(ingredientType, ingredient);
		return this;
	}

	@Override
	public <I> SimpleIngredientAcceptor addTypedIngredient(ITypedIngredient<I> typedIngredient) {
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");

		Optional<ITypedIngredient<I>> copy = TypedIngredient.deepCopy(ingredientManager, typedIngredient);
		if (copy.isPresent()) {
			this.ingredients.add(copy.get());
		}

		return this;
	}

	@Override
	public SimpleIngredientAcceptor addFluidStack(Fluid fluid, long amount) {
		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		return addFluidInternal(fluidHelper, fluid, amount, null);
	}

	@Override
	public SimpleIngredientAcceptor addFluidStack(Fluid fluid, long amount, CompoundTag tag) {
		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		return addFluidInternal(fluidHelper, fluid, amount, tag);
	}

	private <T> SimpleIngredientAcceptor addFluidInternal(IPlatformFluidHelperInternal<T> fluidHelper, Fluid fluid, long amount, @Nullable CompoundTag tag) {
		T fluidStack = fluidHelper.create(fluid, amount, tag);
		IIngredientTypeWithSubtypes<Fluid, T> fluidIngredientType = fluidHelper.getFluidIngredientType();
		addIngredientInternal(fluidIngredientType, fluidStack);
		return this;
	}

	@Override
	public SimpleIngredientAcceptor addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		ErrorUtil.checkNotNull(ingredients, "ingredients");

		for (ITypedIngredient<?> typedIngredient : ingredients) {
			this.addTypedIngredient(typedIngredient);
		}
		return this;
	}

	@Override
	public SimpleIngredientAcceptor addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		ErrorUtil.checkNotNull(ingredients, "ingredients");

		for (Optional<ITypedIngredient<?>> optionalTypedIngredient : ingredients) {
			if (optionalTypedIngredient.isPresent()) {
				this.ingredients.add(optionalTypedIngredient.get());
			}
		}
		return this;
	}

	private <T> void addIngredientInternal(IIngredientType<T> ingredientType, @Nullable T ingredient) {
		if (ingredient == null) {
			return;
		}
		Optional<ITypedIngredient<T>> typedIngredient = TypedIngredient.createAndFilterInvalid(this.ingredientManager, ingredientType, ingredient, false);
		if (typedIngredient.isPresent()) {
			this.ingredients.add(typedIngredient.get());
		}
	}

	@UnmodifiableView
	public List<ITypedIngredient<?>> getAllIngredients() {
		return Collections.unmodifiableList(this.ingredients);
	}
}
