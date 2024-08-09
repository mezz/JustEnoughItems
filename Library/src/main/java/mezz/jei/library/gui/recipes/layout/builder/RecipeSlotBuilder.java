package mezz.jei.library.gui.recipes.layout.builder;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.widgets.ISlottedWidgetFactory;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.gui.elements.OffsetDrawable;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.library.gui.ingredients.CycleTimer;
import mezz.jei.library.gui.ingredients.ICycler;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import mezz.jei.library.gui.ingredients.RendererOverrides;
import mezz.jei.library.ingredients.IngredientAcceptor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// TODO: breaking change: give IRecipeSlotBuilder a parameter for ISlottedWidgetFactory
public class RecipeSlotBuilder implements IRecipeSlotBuilder {
	private final IngredientAcceptor ingredients;
	private final RecipeIngredientRole role;
	private final List<IRecipeSlotTooltipCallback> tooltipCallbacks = new ArrayList<>();
	private ImmutableRect2i rect;
	private @Nullable RendererOverrides rendererOverrides;
	private @Nullable IDrawable background;
	private @Nullable IDrawable overlay;
	private @Nullable String slotName;
	private @Nullable ISlottedWidgetFactory<?> assignedWidgetFactory;

	public RecipeSlotBuilder(IIngredientManager ingredientManager, RecipeIngredientRole role, int x, int y) {
		this.ingredients = new IngredientAcceptor(ingredientManager);
		this.rect = new ImmutableRect2i(x, y, 16, 16);
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
	public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount, DataComponentPatch componentPatch) {
		this.ingredients.addFluidStack(fluid, amount, componentPatch);
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
	public IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height) {
		Preconditions.checkArgument(capacity > 0, "capacity must be > 0");

		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		return setFluidRenderer(fluidHelper, capacity, showCapacity, width, height);
	}

	private <T> IRecipeSlotBuilder setFluidRenderer(IPlatformFluidHelperInternal<T> fluidHelper, long capacity, boolean showCapacity, int width, int height) {
		IIngredientRenderer<T> renderer = fluidHelper.createRenderer(capacity, showCapacity, width, height);
		IIngredientTypeWithSubtypes<Fluid, T> type = fluidHelper.getFluidIngredientType();
		addRenderOverride(type, renderer);
		return this;
	}

	@Override
	public <T> IRecipeSlotBuilder setCustomRenderer(
		IIngredientType<T> ingredientType,
		IIngredientRenderer<T> ingredientRenderer
	) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");

		addRenderOverride(ingredientType, ingredientRenderer);
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
		ErrorUtil.checkNotNull(slotName, "slotName");

		this.slotName = slotName;
		return this;
	}

	public RecipeSlotBuilder assignToWidgetFactory(ISlottedWidgetFactory<?> widgetFactory) {
		ErrorUtil.checkNotNull(widgetFactory, "widgetFactory");

		this.assignedWidgetFactory = widgetFactory;
		return this;
	}

	@Nullable
	public ISlottedWidgetFactory<?> getAssignedWidget() {
		return assignedWidgetFactory;
	}

	public IRecipeSlotDrawable build(IFocusGroup focusGroup, ICycler cycler) {
		Set<Integer> focusMatches = getMatches(focusGroup);
		return build(focusMatches, cycler);
	}

	public IRecipeSlotDrawable build(Set<Integer> focusMatches, ICycler cycler) {
		List<Optional<ITypedIngredient<?>>> allIngredients = this.ingredients.getAllIngredients();

		List<Optional<ITypedIngredient<?>>> focusedIngredients = null;

		if (!focusMatches.isEmpty()) {
			focusedIngredients = new ArrayList<>();
			for (Integer i : focusMatches) {
				if (i < allIngredients.size()) {
					Optional<ITypedIngredient<?>> ingredient = allIngredients.get(i);
					focusedIngredients.add(ingredient);
				}
			}
		}

		return new RecipeSlot(
			role,
			rect,
			cycler,
			tooltipCallbacks,
			allIngredients,
			focusedIngredients,
			background,
			overlay,
			slotName,
			rendererOverrides
		);
	}

	public IntSet getMatches(IFocusGroup focuses) {
		return this.ingredients.getMatches(focuses, role);
	}

	public IngredientAcceptor getIngredientAcceptor() {
		return ingredients;
	}

	private <T> void addRenderOverride(
		IIngredientType<T> ingredientType,
		IIngredientRenderer<T> ingredientRenderer
	) {
		if (this.rendererOverrides == null) {
			this.rendererOverrides = new RendererOverrides();
		}
		this.rendererOverrides.addOverride(ingredientType, ingredientRenderer);
		this.rect = new ImmutableRect2i(
			this.rect.getX(),
			this.rect.getY(),
			rendererOverrides.getIngredientWidth(),
			rendererOverrides.getIngredientHeight()
		);
	}
}
