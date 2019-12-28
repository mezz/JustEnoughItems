package mezz.jei.gui.recipes;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.item.ItemStack;

import mezz.jei.Internal;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipeLayout implements IRecipeLayoutDrawable {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int HIGHLIGHT_COLOR = 0x7FFFFFFF;
	private static final int RECIPE_BUTTON_SIZE = 13;
	private static final int RECIPE_BORDER_PADDING = 4;

	private final int ingredientCycleOffset = (int) ((Math.random() * 10000) % Integer.MAX_VALUE);
	private final IRecipeCategory recipeCategory;
	private final GuiItemStackGroup guiItemStackGroup;
	private final GuiFluidStackGroup guiFluidStackGroup;
	private final Map<IIngredientType, GuiIngredientGroup> guiIngredientGroups;
	@Nullable
	private final RecipeTransferButton recipeTransferButton;
	private final Object recipe;
	@Nullable
	private final Focus<?> focus;
	@Nullable
	private ShapelessIcon shapelessIcon;
	private final DrawableNineSliceTexture recipeBorder;

	private int posX;
	private int posY;

	@Nullable
	public static <T> RecipeLayout create(int index, IRecipeCategory<T> recipeCategory, T recipe, @Nullable Focus focus, int posX, int posY) {
		RecipeLayout recipeLayout = new RecipeLayout(index, recipeCategory, recipe, focus, posX, posY);
		try {
			IIngredients ingredients = new Ingredients();
			recipeCategory.setIngredients(recipe, ingredients);
			recipeCategory.setRecipe(recipeLayout, recipe, ingredients);
			return recipeLayout;
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Error caught from Recipe Category: {}", recipeCategory.getClass().getCanonicalName(), e);
		}
		return null;
	}

	private <T> RecipeLayout(int index, IRecipeCategory<T> recipeCategory, T recipe, @Nullable Focus<?> focus, int posX, int posY) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ErrorUtil.checkNotNull(recipe, "recipe");
		this.recipeCategory = recipeCategory;
		this.focus = focus;

		Focus<ItemStack> itemStackFocus = null;
		Focus<FluidStack> fluidStackFocus = null;
		if (focus != null) {
			Object focusValue = focus.getValue();
			if (focusValue instanceof ItemStack) {
				//noinspection unchecked
				itemStackFocus = (Focus<ItemStack>) focus;
			} else if (focusValue instanceof FluidStack) {
				//noinspection unchecked
				fluidStackFocus = (Focus<FluidStack>) focus;
			}
		}
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
	public void drawRecipe(int mouseX, int mouseY) {
		IDrawable background = recipeCategory.getBackground();

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableLighting();
		RenderSystem.enableAlphaTest();

		final int recipeMouseX = mouseX - posX;
		final int recipeMouseY = mouseY - posY;

		RenderSystem.pushMatrix();
		RenderSystem.translatef(posX, posY, 0.0F);
		{
			IDrawable categoryBackground = recipeCategory.getBackground();
			int width = categoryBackground.getWidth() + (2 * RECIPE_BORDER_PADDING);
			int height = categoryBackground.getHeight() + (2 * RECIPE_BORDER_PADDING);
			recipeBorder.draw(-RECIPE_BORDER_PADDING, -RECIPE_BORDER_PADDING, width, height);
			background.draw();
			//noinspection unchecked
			recipeCategory.draw(recipe, recipeMouseX, recipeMouseY);
			// drawExtras and drawInfo often render text which messes with the color, this clears it
			RenderSystem.color4f(1, 1, 1, 1);
			if (shapelessIcon != null) {
				shapelessIcon.draw(background.getWidth());
			}
		}
		RenderSystem.popMatrix();

		for (GuiIngredientGroup guiIngredientGroup : guiIngredientGroups.values()) {
			guiIngredientGroup.draw(posX, posY, HIGHLIGHT_COLOR, mouseX, mouseY);
		}
		if (recipeTransferButton != null) {
			Minecraft minecraft = Minecraft.getInstance();
			float partialTicks = minecraft.getRenderPartialTicks();
			recipeTransferButton.render(mouseX, mouseY, partialTicks);
		}
		RenderSystem.disableBlend();
		RenderSystem.disableLighting();
		RenderSystem.disableAlphaTest();
	}

	@Override
	public void drawOverlays(int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableLighting();
		RenderSystem.enableAlphaTest();

		final int recipeMouseX = mouseX - posX;
		final int recipeMouseY = mouseY - posY;

		GuiIngredient hoveredIngredient = null;
		for (GuiIngredientGroup guiIngredientGroup : guiIngredientGroups.values()) {
			hoveredIngredient = guiIngredientGroup.getHoveredIngredient(posX, posY, mouseX, mouseY);
			if (hoveredIngredient != null) {
				break;
			}
		}
		if (recipeTransferButton != null) {
			recipeTransferButton.drawToolTip(mouseX, mouseY);
		}
		RenderSystem.disableBlend();
		RenderSystem.disableLighting();

		if (hoveredIngredient != null) {
			hoveredIngredient.drawOverlays(posX, posY, recipeMouseX, recipeMouseY);
		} else if (isMouseOver(mouseX, mouseY)) {
			@SuppressWarnings("unchecked")
			List<String> tooltipStrings = recipeCategory.getTooltipStrings(recipe, recipeMouseX, recipeMouseY);
			if (tooltipStrings.isEmpty() && shapelessIcon != null) {
				tooltipStrings = shapelessIcon.getTooltipStrings(recipeMouseX, recipeMouseY);
			}
			if (tooltipStrings != null && !tooltipStrings.isEmpty()) {
				TooltipRenderer.drawHoveringText(tooltipStrings, mouseX, mouseY);
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
		//noinspection unchecked
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
	public <T> IGuiIngredientGroup<T> getIngredientsGroup(IIngredientType<T> ingredientType) {
		@SuppressWarnings("unchecked")
		GuiIngredientGroup<T> guiIngredientGroup = guiIngredientGroups.get(ingredientType);
		if (guiIngredientGroup == null) {
			Focus<T> focus = null;
			if (this.focus != null) {
				Object focusValue = this.focus.getValue();
				if (ingredientType.getIngredientClass().isInstance(focusValue)) {
					//noinspection unchecked
					focus = (Focus<T>) this.focus;
				}
			}
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
	public RecipeTransferButton getRecipeTransferButton() {
		return recipeTransferButton;
	}

	@Override
	public IRecipeCategory getRecipeCategory() {
		return recipeCategory;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}
}
