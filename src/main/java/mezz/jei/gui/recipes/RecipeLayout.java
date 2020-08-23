package mezz.jei.gui.recipes;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.Internal;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.Focus;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.ingredients.GuiFluidStackGroup;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.ingredients.GuiIngredientGroup;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class RecipeLayout<T> implements IRecipeLayoutDrawable {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int HIGHLIGHT_COLOR = 0x7FFFFFFF;
	private static final int RECIPE_BUTTON_SIZE = 13;
	private static final int RECIPE_BORDER_PADDING = 4;

	private final int ingredientCycleOffset = (int) ((Math.random() * 10000) % Integer.MAX_VALUE);
	private final IRecipeCategory<T> recipeCategory;
	private final GuiItemStackGroup guiItemStackGroup;
	private final GuiFluidStackGroup guiFluidStackGroup;
	private final Map<IIngredientType<?>, GuiIngredientGroup<?>> guiIngredientGroups;
	@Nullable
	private final RecipeTransferButton recipeTransferButton;
	private final T recipe;
	@Nullable
	private final Focus<?> focus;
	@Nullable
	private ShapelessIcon shapelessIcon;
	private final DrawableNineSliceTexture recipeBorder;

	private int posX;
	private int posY;

	@Nullable
	public static <T> RecipeLayout<T> create(int index, IRecipeCategory<T> recipeCategory, T recipe, @Nullable Focus<?> focus, IModIdHelper modIdHelper, int posX, int posY) {
		RecipeLayout<T> recipeLayout = new RecipeLayout<>(index, recipeCategory, recipe, focus, posX, posY);
		try {
			IIngredients ingredients = new Ingredients();
			recipeCategory.setIngredients(recipe, ingredients);
			recipeCategory.setRecipe(recipeLayout, recipe, ingredients);
			if (recipe instanceof IRecipe) {
				addOutputSlotTooltip(recipeLayout, (IRecipe<?>) recipe, modIdHelper);
			}
			return recipeLayout;
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Error caught from Recipe Category: {}", recipeCategory.getClass().getCanonicalName(), e);
		}
		return null;
	}

	private static void addOutputSlotTooltip(RecipeLayout<?> recipeLayout, IRecipe<?> recipe, IModIdHelper modIdHelper) {
		GuiItemStackGroup guiItemStackGroup = recipeLayout.guiItemStackGroup;

		ResourceLocation registryName = recipe.getId();
		guiItemStackGroup.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
			if (guiItemStackGroup.getOutputSlots().contains(slotIndex)) {
				if (modIdHelper.isDisplayingModNameEnabled()) {
					String recipeModId = registryName.getNamespace();
					boolean modIdDifferent = false;
					ResourceLocation itemRegistryName = ingredient.getItem().getRegistryName();
					if (itemRegistryName != null) {
						String itemModId = itemRegistryName.getNamespace();
						modIdDifferent = !recipeModId.equals(itemModId);
					}

					if (modIdDifferent) {
						String modName = modIdHelper.getFormattedModNameForModId(recipeModId);
						TranslationTextComponent recipeBy = new TranslationTextComponent("jei.tooltip.recipe.by", modName);
						tooltip.add(recipeBy.mergeStyle(TextFormatting.GRAY));
					}
				}

				boolean showAdvanced = Minecraft.getInstance().gameSettings.advancedItemTooltips || Screen.hasShiftDown();
				if (showAdvanced) {
					TranslationTextComponent recipeId = new TranslationTextComponent("jei.tooltip.recipe.id", registryName.toString());
					tooltip.add(recipeId.mergeStyle(TextFormatting.DARK_GRAY));
				}
			}
		});
	}

	private RecipeLayout(int index, IRecipeCategory<T> recipeCategory, T recipe, @Nullable Focus<?> focus, int posX, int posY) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ErrorUtil.checkNotNull(recipe, "recipe");
		this.recipeCategory = recipeCategory;
		this.focus = focus;

		Focus<ItemStack> itemStackFocus = Focus.cast(focus, VanillaTypes.ITEM);
		Focus<FluidStack> fluidStackFocus = Focus.cast(focus, VanillaTypes.FLUID);
		this.guiItemStackGroup = new GuiItemStackGroup(itemStackFocus, ingredientCycleOffset);
		this.guiFluidStackGroup = new GuiFluidStackGroup(fluidStackFocus, ingredientCycleOffset);

		this.guiIngredientGroups = new IdentityHashMap<>();
		this.guiIngredientGroups.put(VanillaTypes.ITEM, this.guiItemStackGroup);
		this.guiIngredientGroups.put(VanillaTypes.FLUID, this.guiFluidStackGroup);

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
	@SuppressWarnings("deprecation")
	public void drawRecipe(MatrixStack matrixStack, int mouseX, int mouseY) {
		IDrawable background = recipeCategory.getBackground();

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableLighting();
		RenderSystem.enableAlphaTest();

		final int recipeMouseX = mouseX - posX;
		final int recipeMouseY = mouseY - posY;

		matrixStack.push();
		matrixStack.translate(posX, posY, 0);
		{
			IDrawable categoryBackground = recipeCategory.getBackground();
			int width = categoryBackground.getWidth() + (2 * RECIPE_BORDER_PADDING);
			int height = categoryBackground.getHeight() + (2 * RECIPE_BORDER_PADDING);
			recipeBorder.draw(matrixStack, -RECIPE_BORDER_PADDING, -RECIPE_BORDER_PADDING, width, height);
			background.draw(matrixStack);
			recipeCategory.draw(recipe, matrixStack, recipeMouseX, recipeMouseY);
			// drawExtras and drawInfo often render text which messes with the color, this clears it
			RenderSystem.color4f(1, 1, 1, 1);
			if (shapelessIcon != null) {
				shapelessIcon.draw(matrixStack, background.getWidth());
			}
		}
		matrixStack.pop();

		for (GuiIngredientGroup<?> guiIngredientGroup : guiIngredientGroups.values()) {
			guiIngredientGroup.draw(matrixStack, posX, posY, HIGHLIGHT_COLOR, mouseX, mouseY);
		}
		if (recipeTransferButton != null) {
			Minecraft minecraft = Minecraft.getInstance();
			float partialTicks = minecraft.getRenderPartialTicks();
			recipeTransferButton.render(matrixStack, mouseX, mouseY, partialTicks);
		}
		RenderSystem.disableBlend();
		RenderSystem.disableLighting();
		RenderSystem.disableAlphaTest();
	}

	@Override
	@SuppressWarnings("deprecation")
	public void drawOverlays(MatrixStack matrixStack, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableLighting();
		RenderSystem.enableAlphaTest();

		final int recipeMouseX = mouseX - posX;
		final int recipeMouseY = mouseY - posY;

		GuiIngredient<?> hoveredIngredient = null;
		for (GuiIngredientGroup<?> guiIngredientGroup : guiIngredientGroups.values()) {
			hoveredIngredient = guiIngredientGroup.getHoveredIngredient(posX, posY, mouseX, mouseY);
			if (hoveredIngredient != null) {
				break;
			}
		}
		if (recipeTransferButton != null) {
			recipeTransferButton.drawToolTip(matrixStack, mouseX, mouseY);
		}
		RenderSystem.disableBlend();
		RenderSystem.disableLighting();

		if (hoveredIngredient != null) {
			hoveredIngredient.drawOverlays(matrixStack, posX, posY, recipeMouseX, recipeMouseY);
		} else if (isMouseOver(mouseX, mouseY)) {
			List<ITextComponent> tooltipStrings = recipeCategory.getTooltipStrings(recipe, recipeMouseX, recipeMouseY);
			if (tooltipStrings.isEmpty() && shapelessIcon != null) {
				tooltipStrings = shapelessIcon.getTooltipStrings(recipeMouseX, recipeMouseY);
			}
			if (tooltipStrings != null && !tooltipStrings.isEmpty()) {
				TooltipRenderer.drawHoveringText(tooltipStrings, mouseX, mouseY, matrixStack);
			}
		}

		RenderSystem.disableAlphaTest();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		final IDrawable background = recipeCategory.getBackground();
		final Rectangle2d backgroundRect = new Rectangle2d(posX, posY, background.getWidth(), background.getHeight());
		return MathUtil.contains(backgroundRect, mouseX, mouseY) ||
			(recipeTransferButton != null && recipeTransferButton.isMouseOver(mouseX, mouseY));
	}

	@Override
	@Nullable
	public Object getIngredientUnderMouse(int mouseX, int mouseY) {
		GuiIngredient<?> guiIngredient = getGuiIngredientUnderMouse(mouseX, mouseY);
		if (guiIngredient != null) {
			return guiIngredient.getDisplayedIngredient();
		}

		return null;
	}

	@Nullable
	public GuiIngredient<?> getGuiIngredientUnderMouse(double mouseX, double mouseY) {
		for (GuiIngredientGroup<?> guiIngredientGroup : guiIngredientGroups.values()) {
			GuiIngredient<?> clicked = guiIngredientGroup.getHoveredIngredient(posX, posY, mouseX, mouseY);
			if (clicked != null) {
				return clicked;
			}
		}
		return null;
	}

	public boolean handleClick(double mouseX, double mouseY, int mouseButton) {
		return recipeCategory.handleClick(recipe, mouseX - posX, mouseY - posY, mouseButton);
	}

	@Override
	public GuiItemStackGroup getItemStacks() {
		return guiItemStackGroup;
	}

	@Override
	public IGuiFluidStackGroup getFluidStacks() {
		return guiFluidStackGroup;
	}

	@Override
	public <V> IGuiIngredientGroup<V> getIngredientsGroup(IIngredientType<V> ingredientType) {
		@SuppressWarnings("unchecked")
		GuiIngredientGroup<V> guiIngredientGroup = (GuiIngredientGroup<V>) guiIngredientGroups.get(ingredientType);
		if (guiIngredientGroup == null) {
			Focus<V> focus = getFocus(ingredientType);
			guiIngredientGroup = new GuiIngredientGroup<>(ingredientType, focus, ingredientCycleOffset);
			guiIngredientGroups.put(ingredientType, guiIngredientGroup);
		}
		return guiIngredientGroup;
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

	@Override
	@Nullable
	public Focus<?> getFocus() {
		return focus;
	}

	@Nullable
	@Override
	public <V> Focus<V> getFocus(IIngredientType<V> ingredientType) {
		return Focus.cast(this.focus, ingredientType);
	}

	@Nullable
	public RecipeTransferButton getRecipeTransferButton() {
		return recipeTransferButton;
	}

	@Override
	public IRecipeCategory<?> getRecipeCategory() {
		return recipeCategory;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public T getRecipe() {
		return recipe;
	}
}
