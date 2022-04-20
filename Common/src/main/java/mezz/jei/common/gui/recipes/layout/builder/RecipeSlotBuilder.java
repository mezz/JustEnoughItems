package mezz.jei.common.gui.recipes.layout.builder;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.gui.elements.OffsetDrawable;
import mezz.jei.common.gui.ingredients.RecipeSlot;
import mezz.jei.common.gui.ingredients.RecipeSlots;
import mezz.jei.common.ingredients.IngredientAcceptor;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipeSlotBuilder implements IRecipeSlotBuilder, IRecipeLayoutSlotSource {
	private final IngredientAcceptor ingredients;
	private final RecipeSlot recipeSlot;
	private final IIngredientVisibility ingredientVisibility;

	public RecipeSlotBuilder(RegisteredIngredients registeredIngredients, RecipeIngredientRole role, IIngredientVisibility ingredientVisibility, int x, int y, int ingredientCycleOffset, int legacyIngredientIndex) {
		this.ingredients = new IngredientAcceptor(registeredIngredients);
		this.ingredientVisibility = ingredientVisibility;
		this.recipeSlot = new RecipeSlot(registeredIngredients, role, x, y, ingredientCycleOffset, legacyIngredientIndex);
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

	@SuppressWarnings("removal")
	@Override
	public IRecipeSlotBuilder setFluidRenderer(int capacity, boolean showCapacity, int width, int height) {
		Preconditions.checkArgument(capacity > 0, "capacity must be > 0");

		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		return setFluidRenderer(fluidHelper, capacity, showCapacity, width, height);
	}

	@Override
	public IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height) {
		Preconditions.checkArgument(capacity > 0, "capacity must be > 0");

		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		return setFluidRenderer(fluidHelper, capacity, showCapacity, width, height);
	}

	private <T> IRecipeSlotBuilder setFluidRenderer(IPlatformFluidHelperInternal<T> fluidHelper, long capacity, boolean showCapacity, int width, int height) {
		IIngredientRenderer<T> renderer = fluidHelper.createRenderer(capacity, showCapacity, width, height);
		IIngredientTypeWithSubtypes<Fluid, T> type = fluidHelper.getFluidIngredientType();
		this.recipeSlot.addRenderOverride(type, renderer);
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
	public void setRecipeSlots(RecipeSlots recipeSlots, IntSet focusMatches) {
		List<Optional<ITypedIngredient<?>>> allIngredients = this.ingredients.getAllIngredients();
		recipeSlot.set(allIngredients, focusMatches, ingredientVisibility);

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
	public int getIngredientCount() {
		return this.ingredients.getAllIngredients().size();
	}

	@Override
	public IntSet getMatches(IFocusGroup focuses) {
		RecipeIngredientRole role = recipeSlot.getRole();
		return this.ingredients.getMatches(focuses, role);
	}

	@Override
	public Stream<IIngredientType<?>> getIngredientTypes() {
		return this.ingredients.getIngredientTypes();
	}
}
