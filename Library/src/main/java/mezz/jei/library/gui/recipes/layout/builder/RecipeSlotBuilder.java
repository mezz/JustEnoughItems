package mezz.jei.library.gui.recipes.layout.builder;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.OffsetDrawable;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.core.util.Pair;
import mezz.jei.library.gui.ingredients.ICycler;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import mezz.jei.library.gui.ingredients.RendererOverrides;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RecipeSlotBuilder implements IRecipeSlotBuilder {
	private final DisplayIngredientAcceptor ingredients;
	private final RecipeIngredientRole role;
	private final List<IRecipeSlotRichTooltipCallback> tooltipCallbacks = new ArrayList<>();
	private final int slotIndex;
	private ImmutableRect2i rect;
	private @Nullable RendererOverrides rendererOverrides;
	private @Nullable OffsetDrawable background;
	private @Nullable IDrawable overlay;
	private @Nullable String slotName;

	public RecipeSlotBuilder(IIngredientManager ingredientManager, int slotIndex, RecipeIngredientRole role) {
		this.ingredients = new DisplayIngredientAcceptor(ingredientManager);
		this.rect = new ImmutableRect2i(0, 0, 16, 16);
		this.role = role;
		this.slotIndex = slotIndex;
	}

	@Override
	public IRecipeSlotBuilder add(SlotDisplay slotDisplay) {
		this.ingredients.add(slotDisplay);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, SlotDisplay slotDisplay) {
		this.ingredients.add(ingredientType, slotDisplay);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(ItemStack itemStack) {
		this.ingredients.add(itemStack);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(ItemLike itemLike) {
		this.ingredients.add(itemLike);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(ItemStackTemplate itemStackTemplate) {
		this.ingredients.add(itemStackTemplate);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Fluid fluid) {
		this.ingredients.add(fluid);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Fluid fluid, long amount) {
		this.ingredients.add(fluid, amount);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Fluid fluid, long amount, DataComponentPatch component) {
		this.ingredients.add(fluid, amount, component);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Ingredient ingredient) {
		this.ingredients.add(ingredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, Ingredient ingredient) {
		this.ingredients.add(ingredientType, ingredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(ITypedIngredient<I> typedIngredient) {
		this.ingredients.add(typedIngredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, I ingredient) {
		this.ingredients.add(ingredientType, ingredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		this.ingredients.addIngredients(ingredientType, ingredients);
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
	public IRecipeSlotBuilder addItemStacks(List<ItemStack> itemStacks) {
		this.ingredients.addItemStacks(itemStacks);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setStandardSlotBackground() {
		IGuiHelper guiHelper = Internal.getJeiRuntime().getJeiHelpers().getGuiHelper();
		IDrawableStatic background = guiHelper.getSlotDrawable();
		this.background = new OffsetDrawable(background, -1, -1);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setOutputSlotBackground() {
		IGuiHelper guiHelper = Internal.getJeiRuntime().getJeiHelpers().getGuiHelper();
		IDrawableStatic background = guiHelper.getOutputSlot();
		this.background = new OffsetDrawable(background, -5, -5);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setBackground(IDrawable background, int xOffset, int yOffset) {
		ErrorUtil.checkNotNull(background, "background");

		this.background = new OffsetDrawable(background, xOffset, yOffset);
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
	public IRecipeSlotBuilder addRichTooltipCallback(IRecipeSlotRichTooltipCallback tooltipCallback) {
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

	@Override
	public int getWidth() {
		return this.rect.width();
	}

	@Override
	public int getHeight() {
		return this.rect.height();
	}

	@Override
	public IRecipeSlotBuilder setPosition(int xPos, int yPos) {
		this.rect = this.rect.setPosition(xPos, yPos);
		return this;
	}

	public Pair<Integer, IRecipeSlotDrawable> build(IFocusGroup focusGroup, ICycler cycler) {
		Set<Integer> focusMatches = getMatches(focusGroup);
		return build(focusMatches, cycler);
	}

	public Pair<Integer, IRecipeSlotDrawable> build(Set<Integer> focusMatches, ICycler cycler) {
		List<@Nullable ITypedIngredient<?>> allIngredients = this.ingredients.getAllIngredients();

		List<@Nullable ITypedIngredient<?>> focusedIngredients = null;

		if (!focusMatches.isEmpty()) {
			focusedIngredients = new ArrayList<>();
			for (Integer i : focusMatches) {
				if (i < allIngredients.size()) {
					ITypedIngredient<?> ingredient = allIngredients.get(i);
					focusedIngredients.add(ingredient);
				}
			}
		}

		RecipeSlot recipeSlot = new RecipeSlot(
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
		return new Pair<>(slotIndex, recipeSlot);
	}

	public IntSet getMatches(IFocusGroup focuses) {
		return this.ingredients.getMatches(focuses, role);
	}

	public DisplayIngredientAcceptor getIngredientAcceptor() {
		return ingredients;
	}

	public RecipeIngredientRole getRole() {
		return role;
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
