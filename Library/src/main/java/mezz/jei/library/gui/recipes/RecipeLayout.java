package mezz.jei.library.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import mezz.jei.library.gui.ingredients.RecipeSlots;
import mezz.jei.library.gui.ingredients.TagContentTooltipComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class RecipeLayout<R> implements IRecipeLayoutDrawable<R> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int DEFAULT_RECIPE_BORDER_PADDING = 4;
	public static final int RECIPE_BUTTON_SIZE = 13;
	public static final int RECIPE_BUTTON_SPACING = 2;

	private final int ingredientCycleOffset = (int) ((Math.random() * 10000) % Integer.MAX_VALUE);
	private final IRecipeCategory<R> recipeCategory;
	private final Collection<IRecipeCategoryDecorator<R>> recipeCategoryDecorators;
	private final RecipeSlots recipeSlots;
	private final R recipe;
	private final IScalableDrawable recipeBackground;
	private final int recipeBorderPadding;
	private ImmutableRect2i recipeTransferButtonArea;
	@Nullable
	private ShapelessIcon shapelessIcon;

	private ImmutableRect2i area;

	public static <T> Optional<IRecipeLayoutDrawable<T>> create(
		IRecipeCategory<T> recipeCategory,
		Collection<IRecipeCategoryDecorator<T>> decorators,
		T recipe,
		IFocusGroup focuses,
		IIngredientManager ingredientManager
	) {
		DrawableNineSliceTexture recipeBackground = Internal.getTextures().getRecipeBackground();
		return create(
			recipeCategory,
			decorators,
			recipe,
			focuses,
			ingredientManager,
			recipeBackground,
			DEFAULT_RECIPE_BORDER_PADDING
		);
	}

	public static <T> Optional<IRecipeLayoutDrawable<T>> create(
		IRecipeCategory<T> recipeCategory,
		Collection<IRecipeCategoryDecorator<T>> decorators,
		T recipe,
		IFocusGroup focuses,
		IIngredientManager ingredientManager,
		IScalableDrawable recipeBackground,
		int recipeBorderPadding
	) {
		RecipeLayout<T> recipeLayout = new RecipeLayout<>(recipeCategory, decorators, recipe, recipeBackground, recipeBorderPadding);
		if (recipeLayout.setRecipeLayout(recipeCategory, recipe, focuses, ingredientManager)) {
			ResourceLocation recipeName = recipeCategory.getRegistryName(recipe);
			if (recipeName != null) {
				addOutputSlotTooltip(recipeLayout, recipeName);
			}
			return Optional.of(recipeLayout);
		}
		return Optional.empty();
	}

	private boolean setRecipeLayout(
		IRecipeCategory<R> recipeCategory,
		R recipe,
		IFocusGroup focuses,
		IIngredientManager ingredientManager
	) {
		RecipeLayoutBuilder builder = new RecipeLayoutBuilder(ingredientManager, this.ingredientCycleOffset);
		try {
			recipeCategory.setRecipe(builder, recipe, focuses);
			if (builder.isUsed()) {
				builder.setRecipeLayout(this, focuses);
				return true;
			}
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Error caught from Recipe Category: {}", recipeCategory.getRecipeType().getUid(), e);
		}
		return false;
	}

	private static void addOutputSlotTooltip(RecipeLayout<?> recipeLayout, ResourceLocation recipeName) {
		RecipeSlots recipeSlots = recipeLayout.recipeSlots;
		List<RecipeSlot> outputSlots = recipeSlots.getSlots().stream()
			.filter(r -> r.getRole() == RecipeIngredientRole.OUTPUT)
			.toList();

		if (!outputSlots.isEmpty()) {
			OutputSlotTooltipCallback callback = new OutputSlotTooltipCallback(recipeName);
			for (RecipeSlot outputSlot : outputSlots) {
				outputSlot.addTooltipCallback(callback);
			}
		}
	}

	public RecipeLayout(
		IRecipeCategory<R> recipeCategory,
		Collection<IRecipeCategoryDecorator<R>> recipeCategoryDecorators,
		R recipe,
		IScalableDrawable recipeBackground,
		int recipeBorderPadding
	) {
		this.recipeCategory = recipeCategory;
		this.recipeCategoryDecorators = recipeCategoryDecorators;
		this.recipeSlots = new RecipeSlots();
		this.recipeBorderPadding = recipeBorderPadding;
		this.area = new ImmutableRect2i(
			0,
			0,
			recipeCategory.getWidth(),
			recipeCategory.getHeight()
		);

		this.recipeTransferButtonArea = new ImmutableRect2i(
			area.getWidth() + recipeBorderPadding + RECIPE_BUTTON_SPACING,
			area.getHeight() + recipeBorderPadding - RECIPE_BUTTON_SIZE,
			RECIPE_BUTTON_SIZE,
			RECIPE_BUTTON_SIZE
		);

		this.recipe = recipe;
		this.recipeBackground = recipeBackground;
	}

	@Override
	public void setPosition(int posX, int posY) {
		this.area = new ImmutableRect2i(
			posX,
			posY,
			recipeCategory.getWidth(),
			recipeCategory.getHeight()
		);
	}

	@Override
	public void drawRecipe(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		IDrawable background = recipeCategory.getBackground();

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		recipeBackground.draw(guiGraphics, getRectWithBorder());

		final int recipeMouseX = mouseX - area.getX();
		final int recipeMouseY = mouseY - area.getY();

		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(area.getX(), area.getY(), 0);
			background.draw(guiGraphics);

			// defensive push/pop to protect against recipe categories changing the last pose
			poseStack.pushPose();
			{
				recipeCategory.draw(recipe, recipeSlots.getView(), guiGraphics, recipeMouseX, recipeMouseY);

				// drawExtras and drawInfo often render text which messes with the color, this clears it
				RenderSystem.setShaderColor(1, 1, 1, 1);
			}
			poseStack.popPose();

			for (IRecipeCategoryDecorator<R> decorator : recipeCategoryDecorators) {
				// defensive push/pop to protect against recipe category decorators changing the last pose
				poseStack.pushPose();
				{
					decorator.draw(recipe, recipeCategory, recipeSlots.getView(), guiGraphics, recipeMouseX, recipeMouseY);

					// drawExtras and drawInfo often render text which messes with the color, this clears it
					RenderSystem.setShaderColor(1, 1, 1, 1);
				}
				poseStack.popPose();
			}

			if (shapelessIcon != null) {
				shapelessIcon.draw(guiGraphics);
			}

			recipeSlots.draw(guiGraphics);
		}
		poseStack.popPose();

		RenderSystem.disableBlend();
	}

	@Override
	public void drawOverlays(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		final int recipeMouseX = mouseX - area.getX();
		final int recipeMouseY = mouseY - area.getY();

		IRecipeSlotDrawable hoveredSlot = this.recipeSlots.getHoveredSlot(recipeMouseX, recipeMouseY)
			.orElse(null);

		RenderSystem.disableBlend();

		var poseStack = guiGraphics.pose();
		if (hoveredSlot != null) {
			poseStack.pushPose();
			{
				poseStack.translate(area.getX(), area.getY(), 0);
				hoveredSlot.drawHoverOverlays(guiGraphics);
			}
			poseStack.popPose();

			IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
			IModIdHelper modIdHelper = jeiRuntime.getJeiHelpers().getModIdHelper();
			hoveredSlot.getDisplayedIngredient()
				.ifPresent(i -> {
					JeiTooltip tooltip = new JeiTooltip();
					tooltip.addAll(hoveredSlot.getTooltip());
					addTagContentTooltip(tooltip, i, hoveredSlot);
					List<Component> modIdTooltip = new ArrayList<>();
					modIdTooltip = modIdHelper.addModNameToIngredientTooltip(modIdTooltip, i);
					tooltip.addAll(modIdTooltip);
					tooltip.draw(guiGraphics, mouseX, mouseY, i);
				});
		} else if (isMouseOver(mouseX, mouseY)) {
			List<Component> tooltipStrings = recipeCategory.getTooltipStrings(recipe, recipeSlots.getView(), recipeMouseX, recipeMouseY);
			for (IRecipeCategoryDecorator<R> decorator : recipeCategoryDecorators) {
				tooltipStrings = decorator.decorateExistingTooltips(tooltipStrings, recipe, recipeCategory, recipeSlots.getView(), recipeMouseX, recipeMouseY);
			}

			JeiTooltip tooltip = new JeiTooltip();
			tooltip.addAll(tooltipStrings);

			if (tooltip.isEmpty() && shapelessIcon != null) {
				shapelessIcon.addTooltipStrings(tooltip, recipeMouseX, recipeMouseY);
			}
			tooltip.draw(guiGraphics, mouseX, mouseY);
		}
	}

	private <T> void addTagContentTooltip(JeiTooltip tooltip, ITypedIngredient<T> displayed, IRecipeSlotDrawable slotDrawable) {
		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		if (clientConfig.isTagContentTooltipEnabled()) {
			IIngredientType<T> type = displayed.getType();

			IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
			IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();
			IIngredientRenderer<T> renderer = ingredientManager.getIngredientRenderer(type);

			List<T> ingredients = slotDrawable.getIngredients(type).toList();
			if (ingredients.size() > 1) {
				tooltip.add(new TagContentTooltipComponent<>(renderer, ingredients));
			}
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return MathUtil.contains(area, mouseX, mouseY);
	}

	@Override
	public Rect2i getRect() {
		return area.toMutable();
	}

	@Override
	public Rect2i getRectWithBorder() {
		return area.expandBy(recipeBorderPadding).toMutable();
	}

	@Override
	public <T> Optional<T> getIngredientUnderMouse(int mouseX, int mouseY, IIngredientType<T> ingredientType) {
		return getRecipeSlotUnderMouse(mouseX, mouseY)
			.flatMap(slot -> slot.getDisplayedIngredient(ingredientType));
	}

	@Override
	public Optional<IRecipeSlotDrawable> getRecipeSlotUnderMouse(double mouseX, double mouseY) {
		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();
		return this.recipeSlots.getHoveredSlot(recipeMouseX, recipeMouseY)
			.map(r -> r);
	}

	public void moveRecipeTransferButton(int posX, int posY) {
		recipeTransferButtonArea = new ImmutableRect2i(
			posX,
			posY,
			RECIPE_BUTTON_SIZE,
			RECIPE_BUTTON_SIZE
		);
	}

	public void setShapeless() {
		this.shapelessIcon = new ShapelessIcon();

		// align to top-right
		int x = area.getWidth() - shapelessIcon.getIcon().getWidth();
		this.shapelessIcon.setPosition(x, 0);
	}

	public void setShapeless(int shapelessX, int shapelessY) {
		this.shapelessIcon = new ShapelessIcon();
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
	public Rect2i getRecipeBookmarkButtonArea() {
		Rect2i area = getRecipeTransferButtonArea();
		area.setPosition(area.getX(), area.getY() - area.getHeight() - RECIPE_BUTTON_SPACING);
		return area;
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
