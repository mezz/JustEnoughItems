package mezz.jei.library.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.gui.TooltipRenderer;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import mezz.jei.library.gui.ingredients.RecipeSlots;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class RecipeLayout<R> implements IRecipeLayoutDrawable<R> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int RECIPE_BORDER_PADDING = 4;
	public static final int RECIPE_TRANSFER_BUTTON_SIZE = 13;

	private final int ingredientCycleOffset = (int) ((Math.random() * 10000) % Integer.MAX_VALUE);
	private final IRecipeCategory<R> recipeCategory;
	private final IIngredientManager ingredientManager;
	private final IModIdHelper modIdHelper;
	private final Textures textures;
	private final RecipeSlots recipeSlots;
	private final R recipe;
	private final DrawableNineSliceTexture recipeBorder;
	private ImmutableRect2i recipeTransferButtonArea;
	@Nullable
	private ShapelessIcon shapelessIcon;

	private int posX;
	private int posY;

	public static <T> Optional<IRecipeLayoutDrawable<T>> create(IRecipeCategory<T> recipeCategory, T recipe, IFocusGroup focuses, IIngredientManager ingredientManager, IIngredientVisibility ingredientVisibility, IModIdHelper modIdHelper, Textures textures) {
		RecipeLayout<T> recipeLayout = new RecipeLayout<>(recipeCategory, recipe, ingredientManager, modIdHelper, textures);
		if (recipeLayout.setRecipeLayout(recipeCategory, recipe, focuses, ingredientVisibility)) {
			ResourceLocation recipeName = recipeCategory.getRegistryName(recipe);
			if (recipeName != null) {
				addOutputSlotTooltip(recipeLayout, recipeName, modIdHelper);
			}
			return Optional.of(recipeLayout);
		}
		return Optional.empty();
	}

	private boolean setRecipeLayout(
		IRecipeCategory<R> recipeCategory,
		R recipe,
		IFocusGroup focuses,
		IIngredientVisibility ingredientVisibility
	) {
		RecipeLayoutBuilder builder = new RecipeLayoutBuilder(ingredientManager, this.ingredientCycleOffset);
		try {
			recipeCategory.setRecipe(builder, recipe, focuses);
			if (builder.isUsed()) {
				builder.setRecipeLayout(this, focuses, ingredientVisibility);
				return true;
			}
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Error caught from Recipe Category: {}", recipeCategory.getRecipeType().getUid(), e);
		}
		return false;
	}

	private static void addOutputSlotTooltip(RecipeLayout<?> recipeLayout, ResourceLocation recipeName, IModIdHelper modIdHelper) {
		RecipeSlots recipeSlots = recipeLayout.recipeSlots;
		List<RecipeSlot> outputSlots = recipeSlots.getSlots().stream()
			.filter(r -> r.getRole() == RecipeIngredientRole.OUTPUT)
			.toList();

		if (!outputSlots.isEmpty()) {
			OutputSlotTooltipCallback callback = new OutputSlotTooltipCallback(recipeName, modIdHelper, recipeLayout.ingredientManager);
			for (RecipeSlot outputSlot : outputSlots) {
				outputSlot.addTooltipCallback(callback);
			}
		}
	}

	public RecipeLayout(
		IRecipeCategory<R> recipeCategory,
		R recipe,
		IIngredientManager ingredientManager,
		IModIdHelper modIdHelper,
		Textures textures
	) {
		this.recipeCategory = recipeCategory;
		this.ingredientManager = ingredientManager;
		this.modIdHelper = modIdHelper;
		this.textures = textures;
		this.recipeSlots = new RecipeSlots();

		int width = recipeCategory.getWidth();
		int height = recipeCategory.getHeight();
		int buttonX = width + RECIPE_BORDER_PADDING + 2;
		int buttonY = height - RECIPE_TRANSFER_BUTTON_SIZE;
		this.recipeTransferButtonArea = new ImmutableRect2i(
			buttonX,
			buttonY,
			RECIPE_TRANSFER_BUTTON_SIZE,
			RECIPE_TRANSFER_BUTTON_SIZE
		);

		this.recipe = recipe;
		this.recipeBorder = textures.getRecipeBackground();
	}

	@Override
	public void setPosition(int posX, int posY) {
		int xDiff = posX - this.posX;
		int yDiff = posY - this.posY;
		this.recipeTransferButtonArea = new ImmutableRect2i(
			recipeTransferButtonArea.getX() + xDiff,
			recipeTransferButtonArea.getY() + yDiff,
			recipeTransferButtonArea.getWidth(),
			recipeTransferButtonArea.getHeight()
		);

		this.posX = posX;
		this.posY = posY;
	}

	@Override
	public void drawRecipe(PoseStack poseStack, int mouseX, int mouseY) {
		IDrawable background = recipeCategory.getBackground();

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		final int recipeMouseX = mouseX - posX;
		final int recipeMouseY = mouseY - posY;

		poseStack.pushPose();
		{
			poseStack.translate(posX, posY, 0);

			int width = recipeCategory.getWidth() + (2 * RECIPE_BORDER_PADDING);
			int height = recipeCategory.getHeight() + (2 * RECIPE_BORDER_PADDING);
			recipeBorder.draw(poseStack, -RECIPE_BORDER_PADDING, -RECIPE_BORDER_PADDING, width, height);
			background.draw(poseStack);

			// defensive push/pop to protect against recipe categories changing the last pose
			poseStack.pushPose();
			{
				recipeCategory.draw(recipe, recipeSlots.getView(), poseStack, recipeMouseX, recipeMouseY);

				// drawExtras and drawInfo often render text which messes with the color, this clears it
				RenderSystem.setShaderColor(1, 1, 1, 1);
			}
			poseStack.popPose();

			if (shapelessIcon != null) {
				shapelessIcon.draw(poseStack);
			}

			recipeSlots.draw(poseStack);
		}
		poseStack.popPose();

		RenderSystem.disableBlend();
	}

	@Override
	public void drawOverlays(PoseStack poseStack, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		final int recipeMouseX = mouseX - posX;
		final int recipeMouseY = mouseY - posY;

		IRecipeSlotDrawable hoveredSlot = this.recipeSlots.getHoveredSlot(recipeMouseX, recipeMouseY)
			.orElse(null);

		RenderSystem.disableBlend();

		if (hoveredSlot != null) {
			poseStack.pushPose();
			{
				poseStack.translate(posX, posY, 0);
				hoveredSlot.drawHoverOverlays(poseStack);
			}
			poseStack.popPose();

			hoveredSlot.getDisplayedIngredient()
				.ifPresent(i -> {
					List<Component> tooltip = hoveredSlot.getTooltip();
					tooltip = modIdHelper.addModNameToIngredientTooltip(tooltip, i);
					TooltipRenderer.drawHoveringText(poseStack, tooltip, mouseX, mouseY, i, ingredientManager);
				});
		} else if (isMouseOver(mouseX, mouseY)) {
			List<Component> tooltipStrings = recipeCategory.getTooltipStrings(recipe, recipeSlots.getView(), recipeMouseX, recipeMouseY);
			if (tooltipStrings.isEmpty() && shapelessIcon != null) {
				tooltipStrings = shapelessIcon.getTooltipStrings(recipeMouseX, recipeMouseY);
			}
			if (!tooltipStrings.isEmpty()) {
				TooltipRenderer.drawHoveringText(poseStack, tooltipStrings, mouseX, mouseY);
			}
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return MathUtil.contains(getRect(), mouseX, mouseY);
	}

	@Override
	public Rect2i getRect() {
		return new Rect2i(posX, posY, recipeCategory.getWidth(), recipeCategory.getHeight());
	}

	@Override
	public <T> Optional<T> getIngredientUnderMouse(int mouseX, int mouseY, IIngredientType<T> ingredientType) {
		return getRecipeSlotUnderMouse(mouseX, mouseY)
			.flatMap(slot -> slot.getDisplayedIngredient(ingredientType));
	}

	@Override
	public Optional<IRecipeSlotDrawable> getRecipeSlotUnderMouse(double mouseX, double mouseY) {
		final double recipeMouseX = mouseX - posX;
		final double recipeMouseY = mouseY - posY;
		return this.recipeSlots.getHoveredSlot(recipeMouseX, recipeMouseY)
			.map(r -> r);
	}

	public void moveRecipeTransferButton(int posX, int posY) {
		recipeTransferButtonArea = new ImmutableRect2i(
			posX + this.posX,
			posY + this.posY,
			recipeTransferButtonArea.getWidth(),
			recipeTransferButtonArea.getHeight()
		);
	}

	public void setShapeless() {
		this.shapelessIcon = new ShapelessIcon(textures);
		int categoryWidth = this.recipeCategory.getWidth();

		// align to top-right
		int x = categoryWidth - shapelessIcon.getIcon().getWidth();
		int y = 0;
		this.shapelessIcon.setPosition(x, y);
	}

	public void setShapeless(int shapelessX, int shapelessY) {
		this.shapelessIcon = new ShapelessIcon(textures);
		this.shapelessIcon.setPosition(shapelessX, shapelessY);
	}

	@Override
	public IRecipeCategory<R> getRecipeCategory() {
		return recipeCategory;
	}

	@Override
	public Rect2i getRecipeTransferButtonArea() {
		return recipeTransferButtonArea.toMutable();
	}

	@Override
	public IRecipeSlotsView getRecipeSlotsView() {
		return recipeSlots.getView();
	}

	@Override
	public R getRecipe() {
		return recipe;
	}

	public RecipeSlots getRecipeSlots() {
		return this.recipeSlots;
	}

}
