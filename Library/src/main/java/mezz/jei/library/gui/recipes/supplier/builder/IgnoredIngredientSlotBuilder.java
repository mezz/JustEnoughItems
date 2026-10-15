package mezz.jei.library.gui.recipes.supplier.builder;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.TilingDirection;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

class IgnoredIngredientSlotBuilder implements IRecipeSlotBuilder {
	private final ContextMap contextMap;

	IgnoredIngredientSlotBuilder(ContextMap contextMap) {
		this.contextMap = contextMap;
	}

	@Override
	public ContextMap getContextMap() {
		return contextMap;
	}

	@Override
	public IRecipeSlotBuilder add(SlotDisplay slotDisplay) {
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, SlotDisplay slotDisplay) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(ItemStack itemStack) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(ItemLike itemLike) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Fluid fluid) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Fluid fluid, long amount) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Fluid fluid, long amount, DataComponentPatch component) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Ingredient ingredient) {
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, Ingredient ingredient) {
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(ITypedIngredient<I> typedIngredient) {
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, I ingredient) {
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		return this;
	}

	@SuppressWarnings("removal")
	@Override
	public <I> IRecipeSlotBuilder addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder addIngredientsUnsafe(List<?> ingredients) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder addItemStacks(List<ItemStack> itemStacks) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setStandardSlotBackground() {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setOutputSlotBackground() {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setBackground(IDrawable background, int xOffset, int yOffset) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setHoverPadding(int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
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
	public IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height, TilingDirection tilingDirection) {
		return this;
	}

	@Override
	public <T> IRecipeSlotBuilder setCustomRenderer(IIngredientType<T> ingredientType, IIngredientRenderer<T> ingredientRenderer) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder addRichTooltipCallback(IRecipeSlotRichTooltipCallback tooltipCallback) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setSlotName(String slotName) {
		return this;
	}

	@Override
	public int getWidth() {
		return 16;
	}

	@Override
	public int getHeight() {
		return 16;
	}

	@Override
	public IRecipeSlotBuilder setPosition(int xPos, int yPos) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setPosition(int areaX, int areaY, int areaWidth, int areaHeight, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		return this;
	}
}
