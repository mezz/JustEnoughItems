package mezz.jei.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.ingredients.RecipeSlot;
import mezz.jei.gui.ingredients.RecipeSlots;
import mezz.jei.gui.recipes.builder.RecipeLayoutBuilder;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.input.UserInput;
import mezz.jei.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class RecipeLayout<R> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int HIGHLIGHT_COLOR = 0x80FFFFFF;
	private static final int RECIPE_BORDER_PADDING = 4;

	private final int ingredientCycleOffset = (int) ((Math.random() * 10000) % Integer.MAX_VALUE);
	private final IRecipeCategory<R> recipeCategory;
	private final RegisteredIngredients registeredIngredients;
	private final RecipeSlots recipeSlots;
	private final RecipeLayoutLegacyAdapter<R> legacyAdapter;
	private final R recipe;
	private final DrawableNineSliceTexture recipeBorder;
	@Nullable
	private final RecipeTransferButton recipeTransferButton;
	@Nullable
	private ShapelessIcon shapelessIcon;

	private int posX;
	private int posY;

	@Nullable
	public static <T> RecipeLayout<T> create(int index, IRecipeCategory<T> recipeCategory, T recipe, IFocusGroup focuses, RegisteredIngredients registeredIngredients, IModIdHelper modIdHelper, int posX, int posY) {
		RecipeLayout<T> recipeLayout = new RecipeLayout<>(index, recipeCategory, recipe, focuses, registeredIngredients, posX, posY);
		if (
			recipeLayout.setRecipeLayout(recipeCategory, recipe, registeredIngredients, focuses) ||
			recipeLayout.getLegacyAdapter().setRecipeLayout(recipeCategory, recipe)
		) {
			ResourceLocation recipeName = recipeCategory.getRegistryName(recipe);
			if (recipeName != null) {
				addOutputSlotTooltip(recipeLayout, recipeName, modIdHelper);
			}
			return recipeLayout;
		}
		return null;
	}

	private boolean setRecipeLayout(
		IRecipeCategory<R> recipeCategory,
		R recipe,
		RegisteredIngredients registeredIngredients,
		IFocusGroup focuses
	) {
		RecipeLayoutBuilder builder = new RecipeLayoutBuilder(registeredIngredients, this.ingredientCycleOffset);
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

	private static void addOutputSlotTooltip(RecipeLayout<?> recipeLayout, ResourceLocation recipeName, IModIdHelper modIdHelper) {
		RecipeSlots recipeSlots = recipeLayout.recipeSlots;
		List<RecipeSlot> outputSlots = recipeSlots.getSlots().stream()
			.filter(r -> r.getRole() == RecipeIngredientRole.OUTPUT)
			.toList();

		if (!outputSlots.isEmpty()) {
			OutputSlotTooltipCallback callback = new OutputSlotTooltipCallback(recipeName, modIdHelper, recipeLayout.registeredIngredients);
			for (RecipeSlot outputSlot : outputSlots) {
				outputSlot.addTooltipCallback(callback);
			}
		}
	}

	public RecipeLayout(
		int index,
		IRecipeCategory<R> recipeCategory,
		R recipe,
		IFocusGroup focuses,
		RegisteredIngredients registeredIngredients,
		int posX,
		int posY
	) {
		this.recipeCategory = recipeCategory;
		this.registeredIngredients = registeredIngredients;
		this.recipeSlots = new RecipeSlots();

		if (index >= 0) {
			IDrawable icon = Internal.getTextures().getRecipeTransfer();
			IDrawable background = recipeCategory.getBackground();
			int width = background.getWidth();
			int height = background.getHeight();
			int buttonX = width + RECIPE_BORDER_PADDING + 2;
			int buttonY = height - RecipeTransferButton.RECIPE_BUTTON_SIZE;
			this.recipeTransferButton = new RecipeTransferButton(buttonX, buttonY, icon, this);
		} else {
			this.recipeTransferButton = null;
		}

		setPosition(posX, posY);

		this.recipe = recipe;
		this.recipeBorder = Internal.getTextures().getRecipeBackground();
		this.legacyAdapter = new RecipeLayoutLegacyAdapter<>(this, registeredIngredients, focuses, ingredientCycleOffset);
	}

	public void setPosition(int posX, int posY) {
		if (this.recipeTransferButton != null) {
			int xDiff = posX - this.posX;
			int yDiff = posY - this.posY;
			this.recipeTransferButton.x += xDiff;
			this.recipeTransferButton.y += yDiff;
		}

		this.posX = posX;
		this.posY = posY;
	}

	public void drawRecipe(PoseStack poseStack, int mouseX, int mouseY) {
		IDrawable background = recipeCategory.getBackground();

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		final int recipeMouseX = mouseX - posX;
		final int recipeMouseY = mouseY - posY;

		poseStack.pushPose();
		{
			poseStack.translate(posX, posY, 0);

			IDrawable categoryBackground = recipeCategory.getBackground();
			int width = categoryBackground.getWidth() + (2 * RECIPE_BORDER_PADDING);
			int height = categoryBackground.getHeight() + (2 * RECIPE_BORDER_PADDING);
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

			recipeSlots.draw(poseStack, HIGHLIGHT_COLOR, mouseX, mouseY);
		}
		poseStack.popPose();

		if (recipeTransferButton != null) {
			Minecraft minecraft = Minecraft.getInstance();
			float partialTicks = minecraft.getFrameTime();
			recipeTransferButton.render(poseStack, mouseX, mouseY, partialTicks);
		}
		RenderSystem.disableBlend();
	}

	public void drawOverlays(PoseStack poseStack, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		final int recipeMouseX = mouseX - posX;
		final int recipeMouseY = mouseY - posY;

		RecipeSlot hoveredSlot = this.recipeSlots.getHoveredSlot(recipeMouseX, recipeMouseY)
			.orElse(null);

		if (recipeTransferButton != null) {
			recipeTransferButton.drawToolTip(poseStack, mouseX, mouseY);
		}
		RenderSystem.disableBlend();

		if (hoveredSlot != null) {
			hoveredSlot.drawOverlays(poseStack, posX, posY, recipeMouseX, recipeMouseY);
		} else if (isMouseOver(mouseX, mouseY)) {
			List<Component> tooltipStrings = recipeCategory.getTooltipStrings(recipe, recipeSlots.getView(), recipeMouseX, recipeMouseY);
			if (tooltipStrings.isEmpty() && shapelessIcon != null) {
				tooltipStrings = shapelessIcon.getTooltipStrings(recipeMouseX, recipeMouseY);
			}
			if (tooltipStrings != null && !tooltipStrings.isEmpty()) {
				TooltipRenderer.drawHoveringText(poseStack, tooltipStrings, mouseX, mouseY);
			}
		}
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		final IDrawable background = recipeCategory.getBackground();
		final ImmutableRect2i backgroundRect = new ImmutableRect2i(posX, posY, background.getWidth(), background.getHeight());
		return backgroundRect.contains(mouseX, mouseY) ||
			(recipeTransferButton != null && recipeTransferButton.isMouseOver(mouseX, mouseY));
	}

	public Optional<RecipeSlot> getRecipeSlotUnderMouse(double mouseX, double mouseY) {
		final double recipeMouseX = mouseX - posX;
		final double recipeMouseY = mouseY - posY;
		return this.recipeSlots.getHoveredSlot(recipeMouseX, recipeMouseY);
	}

	public boolean handleInput(UserInput input) {
		if (!isMouseOver(input.getMouseX(), input.getMouseY())) {
			return false;
		}

		double recipeMouseX = input.getMouseX() - posX;
		double recipeMouseY = input.getMouseY() - posY;
		if (recipeCategory.handleInput(recipe, recipeMouseX, recipeMouseY, input.getKey())) {
			return true;
		}

		if (input.is(KeyBindings.copyRecipeId)) {
			return handleCopyRecipeId();
		}
		return false;
	}

	private boolean handleCopyRecipeId() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		ResourceLocation registryName = recipeCategory.getRegistryName(recipe);
		if (registryName == null) {
			TranslatableComponent message = new TranslatableComponent("jei.message.copy.recipe.id.failure");
			if (player != null) {
				player.displayClientMessage(message, false);
			}
			return false;
		}
		String recipeId = registryName.toString();
		minecraft.keyboardHandler.setClipboard(recipeId);
		TranslatableComponent message = new TranslatableComponent("jei.message.copy.recipe.id.success", recipeId);
		if (player != null) {
			player.displayClientMessage(message, false);
		}
		return true;
	}

	public void moveRecipeTransferButton(int posX, int posY) {
		if (recipeTransferButton != null) {
			recipeTransferButton.x = posX + this.posX;
			recipeTransferButton.y = posY + this.posY;
		}
	}

	public void setShapeless() {
		this.shapelessIcon = new ShapelessIcon();
		int categoryWidth = this.recipeCategory.getBackground().getWidth();

		// align to top-right
		int x = categoryWidth - shapelessIcon.getIcon().getWidth();
		int y = 0;
		this.shapelessIcon.setPosition(x, y);
	}

	public void setShapeless(int shapelessX, int shapelessY) {
		this.shapelessIcon = new ShapelessIcon();
		this.shapelessIcon.setPosition(shapelessX, shapelessY);
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

	public RecipeSlots getRecipeSlots() {
		return this.recipeSlots;
	}

	public RecipeLayoutLegacyAdapter<R> getLegacyAdapter() {
		return this.legacyAdapter;
	}
}
