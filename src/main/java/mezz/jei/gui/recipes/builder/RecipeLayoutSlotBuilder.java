package mezz.jei.gui.recipes.builder;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import mezz.jei.Internal;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientTooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.gui.ingredients.GuiIngredientGroup;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.IngredientsForTypeMap;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import mezz.jei.util.ErrorUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeLayoutSlotBuilder implements IRecipeLayoutSlotBuilder, IRecipeLayoutSlotSource {
	private static final int DEFAULT_SIZE = 16;

	private final int slotIndex;
	private final RecipeIngredientRole role;
	private final int xPos;
	private final int yPos;
	private int width = DEFAULT_SIZE;
	private int height = DEFAULT_SIZE;
	private int xPadding;
	private int yPadding;
	@Nullable
	private IDrawable background;
	private final IngredientsForTypeMap ingredients = new IngredientsForTypeMap();
	private final Map<IIngredientType<?>, IIngredientRenderer<?>> renderOverrides = new Object2ObjectArrayMap<>(0);
	private final List<IGuiIngredientTooltipCallback> tooltipCallbacks = new ArrayList<>(0);

	public RecipeLayoutSlotBuilder(int slotIndex, RecipeIngredientRole role, int x, int y) {
		this.slotIndex = slotIndex;
		this.role = role;
		this.xPos = x;
		this.yPos = y;
	}

	@Override
	public <I> IRecipeLayoutSlotBuilder addIngredients(IIngredientType<I> ingredientType, List<I> ingredients) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		Preconditions.checkNotNull(ingredients, "ingredients");
		this.ingredients.addIngredients(ingredientType, ingredients);
		return this;
	}

	@Override
	public <I> IRecipeLayoutSlotBuilder addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredients, "ingredient");
		this.ingredients.addIngredient(ingredientType, ingredient);
		return this;
	}

	@Override
	public IRecipeLayoutSlotBuilder setBackground(IDrawable background) {
		ErrorUtil.checkNotNull(background, "background");
		this.background = background;
		return this;
	}

	@Override
	public IRecipeLayoutSlotBuilder setFluidRenderer(int capacityMb, boolean showCapacity, @Nullable IDrawable overlay) {
		FluidStackRenderer fluidStackRenderer = new FluidStackRenderer(capacityMb, showCapacity, overlay);
		return setCustomRenderer(VanillaTypes.FLUID, fluidStackRenderer);
	}

	@Override
	public <T> IRecipeLayoutSlotBuilder setCustomRenderer(
		IIngredientType<T> ingredientType,
		IIngredientRenderer<T> ingredientRenderer
	) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");

		this.renderOverrides.put(ingredientType, ingredientRenderer);
		return this;
	}

	@Override
	public IRecipeLayoutSlotBuilder setSize(int width, int height) {
		this.width = width;
		this.height = height;
		return this;
	}

	@Override
	public IRecipeLayoutSlotBuilder setInnerPadding(int xPadding, int yPadding) {
		this.xPadding = xPadding;
		this.yPadding = yPadding;
		return this;
	}

	private <T> IIngredientRenderer<T> getIngredientRenderer(IIngredientType<T> ingredientType) {
		@SuppressWarnings("unchecked")
		IIngredientRenderer<T> ingredientRenderer = (IIngredientRenderer<T>) this.renderOverrides.get(ingredientType);
		if (ingredientRenderer == null) {
			IngredientManager ingredientManager = Internal.getIngredientManager();
			ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		}
		return ingredientRenderer;
	}

	@Override
	public IRecipeLayoutSlotBuilder addTooltipCallback(IGuiIngredientTooltipCallback tooltipCallback) {
		ErrorUtil.checkNotNull(tooltipCallback, "tooltipCallback");

		this.tooltipCallbacks.add(tooltipCallback);
		return this;
	}

	@Override
	public <R> void setRecipeLayout(RecipeLayout<R> recipeLayout) {
		IngredientsForTypeMap ingredients = this.ingredients;
		for (IIngredientType<?> type : ingredients.getIngredientTypes()) {
			setRecipeLayout(type, recipeLayout);
		}
	}

	private  <T, R> void setRecipeLayout(IIngredientType<T> ingredientType, RecipeLayout<R> recipeLayout) {
		GuiIngredientGroup<T> ingredientsGroup = recipeLayout.getIngredientsGroup(ingredientType);

		IIngredientRenderer<T> ingredientRenderer = this.getIngredientRenderer(ingredientType);
		ingredientsGroup.init(
			this.slotIndex,
			this.role,
			ingredientRenderer,
			this.xPos,
			this.yPos,
			this.width,
			this.height,
			this.xPadding,
			this.yPadding
		);

		List<T> ingredients = this.ingredients.getIngredients(ingredientType);
		ingredientsGroup.set(this.slotIndex, ingredients);

		if (this.background != null) {
			ingredientsGroup.setBackground(this.slotIndex, this.background);
		}

		for (IGuiIngredientTooltipCallback iGuiIngredientTooltipCallback : tooltipCallbacks) {
			ingredientsGroup.addTooltipCallback(iGuiIngredientTooltipCallback);
		}
	}

	@Override
	public RecipeIngredientRole getRole() {
		return role;
	}

	@Override
	public IngredientsForTypeMap getIngredientsForTypeMap() {
		return ingredients;
	}
}
