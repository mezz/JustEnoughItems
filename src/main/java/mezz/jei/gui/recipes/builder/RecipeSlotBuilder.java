package mezz.jei.gui.recipes.builder;

import com.google.common.base.Preconditions;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.elements.OffsetDrawable;
import mezz.jei.gui.ingredients.RecipeSlot;
import mezz.jei.gui.ingredients.RecipeSlots;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.ingredients.IngredientAcceptor;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import mezz.jei.util.ErrorUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipeSlotBuilder implements IRecipeSlotBuilder, IRecipeLayoutSlotSource {
	private final IngredientAcceptor ingredients;
	private final RecipeSlot recipeSlot;

	public RecipeSlotBuilder(IIngredientManager ingredientManager, RecipeIngredientRole role, int x, int y, int ingredientCycleOffset) {
		this.ingredients = new IngredientAcceptor(ingredientManager);
		this.recipeSlot = new RecipeSlot(ingredientManager, role, x, y, ingredientCycleOffset);
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
	public IRecipeSlotBuilder addIngredientsUnsafe(List<?> ingredients) {
		this.ingredients.addIngredientsUnsafe(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setBackground(IDrawable background, int xOffset, int yOffset) {
		ErrorUtil.checkNotNull(background, "background");

		IDrawable offsetBackground = OffsetDrawable.create(background, xOffset, yOffset);
		this.recipeSlot.setBackground(offsetBackground);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setOverlay(IDrawable overlay, int xOffset, int yOffset) {
		ErrorUtil.checkNotNull(overlay, "overlay");

		IDrawable offsetOverlay = OffsetDrawable.create(overlay, xOffset, yOffset);
		this.recipeSlot.setOverlay(offsetOverlay);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setFluidRenderer(int capacityMb, boolean showCapacity, int width, int height) {
		Preconditions.checkArgument(capacityMb > 0, "capacityMb must be > 0");

		FluidStackRenderer fluidStackRenderer = new FluidStackRenderer(capacityMb, showCapacity, width, height);
		this.recipeSlot.addRenderOverride(VanillaTypes.FLUID, fluidStackRenderer);
		return this;
	}

	@Override
	public <T> IRecipeSlotBuilder setCustomRenderer(
		IIngredientType<T> ingredientType,
		IIngredientRenderer<T> ingredientRenderer
	) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");

		this.recipeSlot.addRenderOverride(ingredientType, ingredientRenderer);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback) {
		ErrorUtil.checkNotNull(tooltipCallback, "tooltipCallback");

		this.recipeSlot.addTooltipCallback(tooltipCallback);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setSlotName(String slotName) {
		ErrorUtil.checkNotNull(slotName, "slotName");

		this.recipeSlot.setSlotName(slotName);
		return this;
	}

	@Override
	public <R> void setRecipeLayout(RecipeLayout<R> recipeLayout, IFocusGroup focuses) {
		RecipeSlots recipeSlots = recipeLayout.getRecipeSlots();

		List<Optional<ITypedIngredient<?>>> allIngredients = this.ingredients.getAllIngredients();
		RecipeIngredientRole role = recipeSlot.getRole();
		List<ITypedIngredient<?>> focusMatches = this.ingredients.getMatches(focuses, role);
		recipeSlot.set(allIngredients, focusMatches);

		recipeSlots.addSlot(recipeSlot);
	}

	@Override
	public RecipeIngredientRole getRole() {
		return recipeSlot.getRole();
	}

	@Override
	public <T> Stream<T> getIngredients(IIngredientType<T> ingredientType) {
		return this.ingredients.getIngredients(ingredientType);
	}

	@Override
	public Stream<IIngredientType<?>> getIngredientTypes() {
		return this.ingredients.getIngredientTypes();
	}
}
