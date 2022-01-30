package mezz.jei.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.Internal;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.IRecipeLayoutView;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.Focus;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.ingredients.RecipeSlot;
import mezz.jei.gui.ingredients.RecipeSlots;
import mezz.jei.gui.ingredients.adapters.RecipeSlotsGuiFluidStackGroupAdapter;
import mezz.jei.gui.ingredients.adapters.RecipeSlotsGuiIngredientGroupAdapter;
import mezz.jei.gui.ingredients.adapters.RecipeSlotsGuiItemStackGroupAdapter;
import mezz.jei.gui.recipes.builder.RecipeLayoutBuilder;
import mezz.jei.ingredients.IngredientTypeHelper;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.input.UserInput;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class RecipeLayout<R> implements IRecipeLayoutDrawable, IRecipeLayoutView {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int HIGHLIGHT_COLOR = 0x7FFFFFFF;
	private static final int RECIPE_BUTTON_SIZE = 13;
	private static final int RECIPE_BORDER_PADDING = 4;

	private final int ingredientCycleOffset = (int) ((Math.random() * 10000) % Integer.MAX_VALUE);
	private final IRecipeCategory<R> recipeCategory;
	private final IIngredientManager ingredientManager;
	private final IGuiItemStackGroup guiItemStackGroup;
	private final IGuiFluidStackGroup guiFluidStackGroup;
	private final RecipeSlots recipeSlots;
	@Nullable
	private final RecipeTransferButton recipeTransferButton;
	private final R recipe;
	private final List<Focus<?>> focuses;
	@Nullable
	private ShapelessIcon shapelessIcon;
	private final DrawableNineSliceTexture recipeBorder;

	private int posX;
	private int posY;

	@Nullable
	public static <T> RecipeLayout<T> create(int index, IRecipeCategory<T> recipeCategory, T recipe, List<Focus<?>> focuses, IIngredientManager ingredientManager, IModIdHelper modIdHelper, int posX, int posY) {
		RecipeLayout<T> recipeLayout = new RecipeLayout<>(index, recipeCategory, recipe, focuses, ingredientManager, posX, posY);
		if (
			setRecipeLayout(recipeLayout, recipeCategory, recipe, focuses) ||
			setRecipeLayoutLegacy(recipeLayout, recipeCategory, recipe)
		) {
			if (recipe instanceof Recipe) {
				addOutputSlotTooltip(recipeLayout, (Recipe<?>) recipe, modIdHelper);
			}
			return recipeLayout;
		}
		return null;
	}

	private static <T> boolean setRecipeLayout(
		RecipeLayout<T> recipeLayout,
		IRecipeCategory<T> recipeCategory,
		T recipe,
		List<Focus<?>> focuses
	) {
		RecipeLayoutBuilder builder = new RecipeLayoutBuilder();
		try {
			recipeCategory.setRecipe(builder, recipe, focuses);
			if (builder.isUsed()) {
				builder.setRecipeLayout(recipeLayout);
				return true;
			}
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Error caught from Recipe Category: {}", recipeCategory.getUid(), e);
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private static <T> boolean setRecipeLayoutLegacy(RecipeLayout<T> recipeLayout, IRecipeCategory<T> recipeCategory, T recipe) {
		try {
			IIngredients ingredients = new Ingredients();
			recipeCategory.setIngredients(recipe, ingredients);
			recipeCategory.setRecipe(recipeLayout, recipe, ingredients);
			return true;
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Error caught from Recipe Category: {}", recipeCategory.getUid(), e);
		}
		return false;
	}

	private static void addOutputSlotTooltip(RecipeLayout<?> recipeLayout, Recipe<?> recipe, IModIdHelper modIdHelper) {
		ResourceLocation recipeName = recipe.getId();
		RecipeSlots recipeSlots = recipeLayout.recipeSlots;
		OutputSlotTooltipCallback callback = new OutputSlotTooltipCallback(recipeName, modIdHelper, recipeLayout.ingredientManager);
		recipeSlots.addTooltipCallback(callback);
	}

	public RecipeLayout(
		int index,
		IRecipeCategory<R> recipeCategory,
		R recipe,
		List<Focus<?>> focuses,
		IIngredientManager ingredientManager,
		int posX,
		int posY
	) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(ingredientManager, "ingredientManager");
		ErrorUtil.checkNotNull(focuses, "focuses");
		this.recipeCategory = recipeCategory;
		this.ingredientManager = ingredientManager;
		this.focuses = focuses;
		this.recipeSlots = new RecipeSlots();

		Focus<ItemStack> itemStackFocus = IngredientTypeHelper.findAndCheckedCast(focuses, VanillaTypes.ITEM);
		this.guiItemStackGroup = new RecipeSlotsGuiItemStackGroupAdapter(this.recipeSlots, ingredientManager, ingredientCycleOffset);
		this.guiItemStackGroup.setOverrideDisplayFocus(itemStackFocus);

		Focus<FluidStack> fluidStackFocus = IngredientTypeHelper.findAndCheckedCast(focuses, VanillaTypes.FLUID);
		this.guiFluidStackGroup = new RecipeSlotsGuiFluidStackGroupAdapter(this.recipeSlots, ingredientManager, ingredientCycleOffset);
		this.guiFluidStackGroup.setOverrideDisplayFocus(fluidStackFocus);

		if (index >= 0) {
			IDrawable icon = Internal.getTextures().getRecipeTransfer();
			this.recipeTransferButton = new RecipeTransferButton(0, 0, RECIPE_BUTTON_SIZE, RECIPE_BUTTON_SIZE, icon, this);
		} else {
			this.recipeTransferButton = null;
		}

		setPosition(posX, posY);

		this.recipe = recipe;
		this.recipeBorder = Internal.getTextures().getRecipeBackground();
	}

	@Override
	public void setPosition(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;

		if (this.recipeTransferButton != null) {
			int width = recipeCategory.getBackground().getWidth();
			int height = recipeCategory.getBackground().getHeight();
			this.recipeTransferButton.x = posX + width + RECIPE_BORDER_PADDING + 2;
			this.recipeTransferButton.y = posY + height - RECIPE_BUTTON_SIZE;
		}
	}

	@Override
	public void drawRecipe(PoseStack poseStack, int mouseX, int mouseY) {
		IDrawable background = recipeCategory.getBackground();

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		final int recipeMouseX = mouseX - posX;
		final int recipeMouseY = mouseY - posY;

		poseStack.pushPose();
		poseStack.translate(posX, posY, 0);
		{
			IDrawable categoryBackground = recipeCategory.getBackground();
			int width = categoryBackground.getWidth() + (2 * RECIPE_BORDER_PADDING);
			int height = categoryBackground.getHeight() + (2 * RECIPE_BORDER_PADDING);
			recipeBorder.draw(poseStack, -RECIPE_BORDER_PADDING, -RECIPE_BORDER_PADDING, width, height);
			background.draw(poseStack);
			recipeCategory.draw(recipe, this, poseStack, recipeMouseX, recipeMouseY);
			// drawExtras and drawInfo often render text which messes with the color, this clears it
			RenderSystem.setShaderColor(1, 1, 1, 1);
			if (shapelessIcon != null) {
				shapelessIcon.draw(poseStack, background.getWidth());
			}
		}
		poseStack.popPose();

		recipeSlots.draw(poseStack, posX, posY, HIGHLIGHT_COLOR, mouseX, mouseY);

		if (recipeTransferButton != null) {
			Minecraft minecraft = Minecraft.getInstance();
			float partialTicks = minecraft.getFrameTime();
			recipeTransferButton.render(poseStack, mouseX, mouseY, partialTicks);
		}
		RenderSystem.disableBlend();
	}

	@Override
	public void drawOverlays(PoseStack poseStack, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		final int recipeMouseX = mouseX - posX;
		final int recipeMouseY = mouseY - posY;

		RecipeSlot hoveredSlot = this.recipeSlots.getHoveredSlot(posX, posY, mouseX, mouseY)
			.orElse(null);

		if (recipeTransferButton != null) {
			recipeTransferButton.drawToolTip(poseStack, mouseX, mouseY);
		}
		RenderSystem.disableBlend();

		if (hoveredSlot != null) {
			hoveredSlot.drawOverlays(poseStack, posX, posY, recipeMouseX, recipeMouseY);
		} else if (isMouseOver(mouseX, mouseY)) {
			List<Component> tooltipStrings = recipeCategory.getTooltipStrings(recipe, recipeMouseX, recipeMouseY);
			if (tooltipStrings.isEmpty() && shapelessIcon != null) {
				tooltipStrings = shapelessIcon.getTooltipStrings(recipeMouseX, recipeMouseY);
			}
			if (tooltipStrings != null && !tooltipStrings.isEmpty()) {
				TooltipRenderer.drawHoveringText(poseStack, tooltipStrings, mouseX, mouseY);
			}
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		final IDrawable background = recipeCategory.getBackground();
		final Rect2i backgroundRect = new Rect2i(posX, posY, background.getWidth(), background.getHeight());
		return MathUtil.contains(backgroundRect, mouseX, mouseY) ||
			(recipeTransferButton != null && recipeTransferButton.isMouseOver(mouseX, mouseY));
	}

	@Override
	@Nullable
	public <I> I getIngredientUnderMouse(int mouseX, int mouseY, IIngredientType<I> ingredientType) {
		return getRecipeSlotUnderMouse(mouseX, mouseY)
			.map(slot -> slot.getDisplayedIngredient(ingredientType))
			.orElse(null);
	}

	public Optional<RecipeSlot> getRecipeSlotUnderMouse(double mouseX, double mouseY) {
		return this.recipeSlots.getHoveredSlot(posX, posY, mouseX, mouseY);
	}

	public boolean handleInput(UserInput input) {
		return recipeCategory.handleInput(recipe, input.getMouseX() - posX, input.getMouseY() - posY, input.getKey());
	}

	@Override
	public IGuiItemStackGroup getItemStacks() {
		return guiItemStackGroup;
	}

	@Override
	public IGuiFluidStackGroup getFluidStacks() {
		return guiFluidStackGroup;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> IGuiIngredientGroup<V> getIngredientsGroup(IIngredientType<V> ingredientType) {
		if (ingredientType == VanillaTypes.ITEM) {
			return (IGuiIngredientGroup<V>) this.guiItemStackGroup;
		}
		if (ingredientType == VanillaTypes.FLUID) {
			return (IGuiIngredientGroup<V>) this.guiFluidStackGroup;
		}
		RecipeSlotsGuiIngredientGroupAdapter<V> adapter = new RecipeSlotsGuiIngredientGroupAdapter<V>(this.recipeSlots, this.ingredientManager, ingredientType, this.ingredientCycleOffset);
		Focus<V> focus = getFocus(ingredientType);
		adapter.setOverrideDisplayFocus(focus);
		return adapter;
	}

	@Override
	public void moveRecipeTransferButton(int posX, int posY) {
		if (recipeTransferButton != null) {
			recipeTransferButton.x = posX + this.posX;
			recipeTransferButton.y = posY + this.posY;
		}
	}

	@Override
	public void setShapeless() {
		this.shapelessIcon = new ShapelessIcon();
	}

	@Nullable
	@Override
	public <V> Focus<V> getFocus(IIngredientType<V> ingredientType) {
		return IngredientTypeHelper.findAndCheckedCast(this.focuses, ingredientType);
	}

	@Nullable
	public RecipeTransferButton getRecipeTransferButton() {
		return recipeTransferButton;
	}

	public IRecipeCategory<R> getRecipeCategory() {
		return recipeCategory;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public R getRecipe() {
		return recipe;
	}

	@Nullable
	@Override
	public <T> T getDisplayedIngredient(IIngredientType<T> ingredientType, int slot) {
		List<RecipeSlot> recipeSlots = this.recipeSlots.getSlots();
		RecipeSlot recipeSlot = recipeSlots.get(slot);
		if (recipeSlot == null) {
			return null;
		}
		return recipeSlot.getDisplayedIngredient(ingredientType);
	}

	public RecipeSlots createRecipeSlotsView() {
		return this.recipeSlots;
//		Collection<GuiIngredientGroup<?>> guiIngredientGroups = this.guiIngredientGroups.values();
//
//		List<IRecipeSlotView> slotViews = guiIngredientGroups.stream()
//			.flatMap(g -> g.getSlots().entrySet().stream())
//			.flatMap(entry -> {
//				RecipeIngredientRole role = entry.getKey();
//				Set<Integer> slotIndexes = entry.getValue();
//				return slotIndexes.stream()
//					.map(slotIndex -> createRecipeSlotView(role, slotIndex, guiIngredientGroups))
//					.flatMap(Optional::stream);
//			})
//			.collect(Collectors.toList());
//
//		return new RecipeSlots(slotViews);
	}

//	private static Optional<IRecipeSlotView> createRecipeSlotView(RecipeIngredientRole role, int slotIndex, Collection<GuiIngredientGroup<?>> guiIngredientGroups) {
//		List<? extends GuiIngredient<?>> ingredients = getIngredients(guiIngredientGroups, slotIndex);
//		if (ingredients.isEmpty()) {
//			return Optional.empty();
//		}
//		return Optional.of(new RecipeSlot(role, slotIndex, ingredients));
//	}

//	private static List<? extends GuiIngredient<?>> getIngredients(Collection<GuiIngredientGroup<?>> guiIngredientGroups, int slotIndex) {
//		return guiIngredientGroups.stream()
//			.map(group -> group.getGuiIngredients().get(slotIndex))
//			.filter(Objects::nonNull)
//			.toList();
//	}
}
