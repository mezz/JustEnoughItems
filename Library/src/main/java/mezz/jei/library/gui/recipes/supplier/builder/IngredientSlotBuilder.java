package mezz.jei.library.gui.recipes.supplier.builder;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.gui.recipes.layout.builder.RecipeSlotBuilder;
import mezz.jei.library.gui.recipes.layout.builder.RecipeSlotIngredients;
import mezz.jei.library.ingredients.IngredientAcceptor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Minimal version of {@link RecipeSlotBuilder} that can only return the ingredients,
 * but doesn't bother building anything for drawing on screen.
 */
public class IngredientSlotBuilder implements IRecipeSlotBuilder {
	private final IngredientAcceptor ingredients;
	private final RecipeIngredientRole role;

	public IngredientSlotBuilder(IIngredientManager ingredientManager, RecipeIngredientRole role) {
		this.ingredients = new IngredientAcceptor(ingredientManager);
		this.role = role;
	}

	@Override
	public <I> IRecipeSlotBuilder addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		this.ingredients.addIngredients(ingredientType, ingredients);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		this.ingredients.addIngredient(ingredientType, ingredient);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount) {
		this.ingredients.addFluidStack(fluid, amount);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount, CompoundTag tag) {
		this.ingredients.addFluidStack(fluid, amount, tag);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addIngredientsUnsafe(List<?> ingredients) {
		this.ingredients.addIngredientsUnsafe(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		this.ingredients.addTypedIngredients(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		this.ingredients.addOptionalTypedIngredients(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setBackground(IDrawable background, int xOffset, int yOffset) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setOverlay(IDrawable overlay, int xOffset, int yOffset) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height) {
		return this;
	}

	@Override
	public <T> IRecipeSlotBuilder setCustomRenderer(IIngredientType<T> ingredientType, IIngredientRenderer<T> ingredientRenderer) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setSlotName(String slotName) {
		return this;
	}

	public RecipeSlotIngredients getRecipeSlotIngredients() {
		return new RecipeSlotIngredients(
			this.role,
			this.ingredients.getAllIngredients(),
			this.ingredients.getIngredientTypes()
		);
	}
}
