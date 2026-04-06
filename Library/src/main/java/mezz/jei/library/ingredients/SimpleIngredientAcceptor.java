package mezz.jei.library.ingredients;

import com.google.common.base.Preconditions;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.ingredients.itemStacks.TypedItemStack;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

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
	public SimpleIngredientAcceptor add(ItemLike itemLike) {
		Preconditions.checkNotNull(itemLike, "itemLike");

		ITypedIngredient<ItemStack> ingredient = TypedItemStack.create(itemLike);
		this.ingredients.add(ingredient);

		return this;
	}

	@Override
	public SimpleIngredientAcceptor add(ItemStackTemplate itemStackTemplate) {
		Preconditions.checkNotNull(itemStackTemplate, "itemStackTemplate");

		ITypedIngredient<ItemStack> ingredient = TypedItemStack.create(itemStackTemplate);
		this.ingredients.add(ingredient);

		return this;
	}

	@Override
	public SimpleIngredientAcceptor addIngredientsUnsafe(List<?> ingredients) {
		Preconditions.checkNotNull(ingredients, "ingredients");

		for (Object ingredient : ingredients) {
			ITypedIngredient<?> typedIngredient = TypedIngredient.createAndFilterInvalid(ingredientManager, ingredient, false);
			if (typedIngredient != null) {
				this.ingredients.add(typedIngredient);
			}
		}

		return this;
	}

	@Override
	public <T> SimpleIngredientAcceptor addIngredients(IIngredientType<T> ingredientType, List<@Nullable T> ingredients) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		Preconditions.checkNotNull(ingredients, "ingredients");

		List<@Nullable ITypedIngredient<T>> typedIngredients = TypedIngredient.createAndFilterInvalidList(this.ingredientManager, ingredientType, ingredients, false);
		for (@Nullable ITypedIngredient<T> typedIngredientOptional : typedIngredients) {
			if (typedIngredientOptional != null) {
				this.ingredients.add(typedIngredientOptional);
			}
		}

		return this;
	}

	@Override
	public SimpleIngredientAcceptor add(SlotDisplay slotDisplay) {
		ErrorUtil.checkNotNull(slotDisplay, "slotDisplay");

		TypedIngredient.createAndFilterInvalidList(ingredientManager, slotDisplay, false)
			.forEach(typedIngredient -> {
				if (typedIngredient != null) {
					this.add(typedIngredient);
				}
			});

		return this;
	}

	@Override
	public <I> SimpleIngredientAcceptor add(IIngredientType<I> ingredientType, SlotDisplay slotDisplay) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(slotDisplay, "slotDisplay");

		TypedIngredient.createAndFilterInvalidList(ingredientManager, ingredientType, slotDisplay, false)
			.forEach(typedIngredient -> {
				if (typedIngredient != null) {
					this.add(typedIngredient);
				}
			});

		return this;
	}

	@Override
	public SimpleIngredientAcceptor add(ItemStack itemStack) {
		ErrorUtil.checkNotNull(itemStack, "itemStack");

		addIngredientInternal(VanillaTypes.ITEM_STACK, itemStack);
		return this;
	}

	@Override
	public <T> SimpleIngredientAcceptor add(IIngredientType<T> ingredientType, T ingredient) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		addIngredientInternal(ingredientType, ingredient);
		return this;
	}

	@Override
	public <I> SimpleIngredientAcceptor add(ITypedIngredient<I> typedIngredient) {
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");

		ITypedIngredient<I> copy = TypedIngredient.defensivelyCopyTypedIngredientFromApi(ingredientManager, typedIngredient);
		if (copy != null) {
			this.ingredients.add(copy);
		}

		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public SimpleIngredientAcceptor add(Fluid fluid) {
		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		return addFluidInternal(fluidHelper, fluid.builtInRegistryHolder(), fluidHelper.bucketVolume(), DataComponentPatch.EMPTY);
	}

	@SuppressWarnings("deprecation")
	@Override
	public SimpleIngredientAcceptor add(Fluid fluid, long amount) {
		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		return addFluidInternal(fluidHelper, fluid.builtInRegistryHolder(), amount, DataComponentPatch.EMPTY);
	}

	@SuppressWarnings("deprecation")
	@Override
	public SimpleIngredientAcceptor add(Fluid fluid, long amount, DataComponentPatch component) {
		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		return addFluidInternal(fluidHelper, fluid.builtInRegistryHolder(), amount, component);
	}

	@Override
	public SimpleIngredientAcceptor add(Ingredient ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		return this.add(ingredient.display());
	}

	@Override
	public <I> SimpleIngredientAcceptor add(IIngredientType<I> ingredientType, Ingredient ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		return this.add(ingredientType, ingredient.display());
	}

	private <T> SimpleIngredientAcceptor addFluidInternal(IPlatformFluidHelperInternal<T> fluidHelper, Holder<Fluid> fluidHolder, long amount, DataComponentPatch component) {
		T fluidStack = fluidHelper.create(fluidHolder, amount, component);
		IIngredientTypeWithSubtypes<Fluid, T> fluidIngredientType = fluidHelper.getFluidIngredientType();
		addIngredientInternal(fluidIngredientType, fluidStack);
		return this;
	}

	@Override
	public SimpleIngredientAcceptor addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		ErrorUtil.checkNotNull(ingredients, "ingredients");

		for (ITypedIngredient<?> typedIngredient : ingredients) {
			this.add(typedIngredient);
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

	@Override
	public SimpleIngredientAcceptor addItemStacks(List<ItemStack> itemStacks) {
		return addIngredients(VanillaTypes.ITEM_STACK, itemStacks);
	}

	private <T> void addIngredientInternal(IIngredientType<T> ingredientType, @Nullable T ingredient) {
		if (ingredient == null) {
			return;
		}
		ITypedIngredient<T> typedIngredient = TypedIngredient.createAndFilterInvalid(this.ingredientManager, ingredientType, ingredient, false);
		if (typedIngredient != null) {
			this.ingredients.add(typedIngredient);
		}
	}

	@UnmodifiableView
	public List<ITypedIngredient<?>> getAllIngredients() {
		return Collections.unmodifiableList(this.ingredients);
	}
}
