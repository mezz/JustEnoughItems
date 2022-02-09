package mezz.jei.gui.recipes.builder;

import com.google.common.base.Preconditions;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.Focus;
import mezz.jei.gui.elements.OffsetDrawable;
import mezz.jei.gui.ingredients.RecipeSlot;
import mezz.jei.gui.ingredients.RecipeSlots;
import mezz.jei.gui.ingredients.RendererOverrides;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.ingredients.IngredientAcceptor;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import mezz.jei.util.ErrorUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipeSlotBuilder implements IRecipeSlotBuilder, IRecipeLayoutSlotSource {
	private final IIngredientManager ingredientManager;
	private final RecipeIngredientRole role;
	private final IngredientAcceptor ingredients;
	private final List<IRecipeSlotTooltipCallback> tooltipCallbacks = new ArrayList<>(0);
	private final RendererOverrides rendererOverrides;
	private final int xPos;
	private final int yPos;
	@Nullable
	private IDrawable background;
	@Nullable
	private IDrawable overlay;
	@Nullable
	private String slotName;

	public RecipeSlotBuilder(
		IIngredientManager ingredientManager,
		RecipeIngredientRole role,
		int x,
		int y
	) {
		this.ingredientManager = ingredientManager;
		this.rendererOverrides = new RendererOverrides();
		this.ingredients = new IngredientAcceptor(ingredientManager);
		this.role = role;
		this.xPos = x;
		this.yPos = y;
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
		this.background = OffsetDrawable.create(background, xOffset, yOffset);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setOverlay(IDrawable overlay, int xOffset, int yOffset) {
		ErrorUtil.checkNotNull(overlay, "overlay");
		this.overlay = OffsetDrawable.create(overlay, xOffset, yOffset);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setFluidRenderer(int capacityMb, boolean showCapacity, int width, int height) {
		Preconditions.checkArgument(capacityMb > 0, "capacityMb must be > 0");

		FluidStackRenderer fluidStackRenderer = new FluidStackRenderer(capacityMb, showCapacity, width, height);
		return setCustomRenderer(VanillaTypes.FLUID, fluidStackRenderer);
	}

	@Override
	public <T> IRecipeSlotBuilder setCustomRenderer(
		IIngredientType<T> ingredientType,
		IIngredientRenderer<T> ingredientRenderer
	) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");

		this.rendererOverrides.addOverride(ingredientType, ingredientRenderer);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback) {
		ErrorUtil.checkNotNull(tooltipCallback, "tooltipCallback");

		this.tooltipCallbacks.add(tooltipCallback);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setSlotName(String slotName) {
		this.slotName = slotName;
		return this;
	}

	@Override
	public <R> void setRecipeLayout(RecipeLayout<R> recipeLayout, List<Focus<?>> focuses) {
		RecipeSlots recipeSlots = recipeLayout.getRecipeSlots();

		RecipeSlot recipeSlot = new RecipeSlot(
			this.ingredientManager,
			this.role,
			this.xPos,
			this.yPos,
			recipeLayout.getIngredientCycleOffset()
		);

		List<Optional<ITypedIngredient<?>>> allIngredients = this.ingredients.getAllIngredients();

		recipeSlot.set(allIngredients, focuses);
		if (this.background != null) {
			recipeSlot.setBackground(this.background);
		}
		if (this.overlay != null) {
			recipeSlot.setOverlay(this.overlay);
		}
		if (this.slotName != null) {
			recipeSlot.setSlotName(this.slotName);
		}
		this.tooltipCallbacks.forEach(recipeSlot::addTooltipCallback);
		recipeSlot.setRendererOverrides(this.rendererOverrides);

		recipeSlots.addSlot(recipeSlot);
	}

	@Override
	public RecipeIngredientRole getRole() {
		return role;
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
