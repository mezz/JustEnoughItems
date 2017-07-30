package mezz.jei.gui.recipes;

import mezz.jei.Internal;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.Focus;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.ingredients.GuiFluidStackGroup;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.ingredients.GuiIngredientGroup;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.LegacyUtil;
import mezz.jei.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class RecipeLayout implements IRecipeLayoutDrawable {
	private static final int RECIPE_BUTTON_SIZE = 13;
	public static final int recipeTransferButtonIndex = 100;

	private final int ingredientCycleOffset = (int) (Math.random() * 10000);
	private final IRecipeCategory recipeCategory;
	private final GuiItemStackGroup guiItemStackGroup;
	private final GuiFluidStackGroup guiFluidStackGroup;
	private final Map<Class, GuiIngredientGroup> guiIngredientGroups;
	@Nullable
	private final RecipeTransferButton recipeTransferButton;
	private final IRecipeWrapper recipeWrapper;
	@Nullable
	private final IFocus<?> focus;
	private final Color highlightColor = new Color(0x7FFFFFFF, true);
	@Nullable
	private ShapelessIcon shapelessIcon;

	private int posX;
	private int posY;

	@Nullable
	public static <T extends IRecipeWrapper> RecipeLayout create(int index, IRecipeCategory<T> recipeCategory, T recipeWrapper, @Nullable IFocus focus, int posX, int posY) {
		RecipeLayout recipeLayout = new RecipeLayout(index, recipeCategory, recipeWrapper, focus, posX, posY);
		try {
			IIngredients ingredients = new Ingredients();
			recipeWrapper.getIngredients(ingredients);
			recipeCategory.setRecipe(recipeLayout, recipeWrapper, ingredients);
			return recipeLayout;
		} catch (RuntimeException e) {
			Log.error("Error caught from Recipe Category: {}", recipeCategory.getClass().getCanonicalName(), e);
		} catch (LinkageError e) {
			Log.error("Error caught from Recipe Category: {}", recipeCategory.getClass().getCanonicalName(), e);
		}
		return null;
	}

	private  <T extends IRecipeWrapper> RecipeLayout(int index, IRecipeCategory<T> recipeCategory, T recipeWrapper, @Nullable IFocus focus, int posX, int posY) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ErrorUtil.checkNotNull(recipeWrapper, "recipeWrapper");
		if (focus != null) {
			focus = Focus.check(focus);
		}
		this.recipeCategory = recipeCategory;
		this.focus = focus;

		IFocus<ItemStack> itemStackFocus = null;
		IFocus<FluidStack> fluidStackFocus = null;
		if (focus != null) {
			Object focusValue = focus.getValue();
			if (focusValue instanceof ItemStack) {
				//noinspection unchecked
				itemStackFocus = (IFocus<ItemStack>) focus;
			} else if (focusValue instanceof FluidStack) {
				//noinspection unchecked
				fluidStackFocus = (IFocus<FluidStack>) focus;
			}
		}
		this.guiItemStackGroup = new GuiItemStackGroup(itemStackFocus, ingredientCycleOffset);
		this.guiFluidStackGroup = new GuiFluidStackGroup(fluidStackFocus, ingredientCycleOffset);

		this.guiIngredientGroups = new IdentityHashMap<Class, GuiIngredientGroup>();
		this.guiIngredientGroups.put(ItemStack.class, this.guiItemStackGroup);
		this.guiIngredientGroups.put(FluidStack.class, this.guiFluidStackGroup);

		if (index >= 0) {
			IDrawable plusIcon = Internal.getHelpers().getGuiHelper().getPlusSign();
			this.recipeTransferButton = new RecipeTransferButton(recipeTransferButtonIndex + index, 0, 0, RECIPE_BUTTON_SIZE, RECIPE_BUTTON_SIZE, plusIcon, this);
		} else {
			this.recipeTransferButton = null;
		}

		setPosition(posX, posY);

		this.recipeWrapper = recipeWrapper;
	}

	@Override
	public void setPosition(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;

		if (this.recipeTransferButton != null) {
			int width = recipeCategory.getBackground().getWidth();
			int height = recipeCategory.getBackground().getHeight();
			this.recipeTransferButton.xPosition = posX + width + 2;
			this.recipeTransferButton.yPosition = posY + height - RECIPE_BUTTON_SIZE;
		}
	}

	@Override
	public void draw(Minecraft minecraft, final int mouseX, final int mouseY) {
		drawRecipe(minecraft, mouseX, mouseY);
		drawOverlays(minecraft, mouseX, mouseY);
	}

	@Override
	public void drawRecipe(Minecraft minecraft, int mouseX, int mouseY) {
		IDrawable background = recipeCategory.getBackground();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();

		final int recipeMouseX = mouseX - posX;
		final int recipeMouseY = mouseY - posY;

		GlStateManager.pushMatrix();
		GlStateManager.translate(posX, posY, 0.0F);
		{
			background.draw(minecraft);
			recipeCategory.drawExtras(minecraft);
			recipeWrapper.drawInfo(minecraft, background.getWidth(), background.getHeight(), recipeMouseX, recipeMouseY);
			// drawExtras and drawInfo often render text which messes with the color, this clears it
			GlStateManager.color(1, 1, 1, 1);
			if (shapelessIcon != null) {
				shapelessIcon.draw(minecraft, background.getWidth());
			}
		}
		GlStateManager.popMatrix();

		for (GuiIngredientGroup guiIngredientGroup : guiIngredientGroups.values()) {
			guiIngredientGroup.draw(minecraft, posX, posY, highlightColor, mouseX, mouseY);
		}
		if (recipeTransferButton != null) {
			recipeTransferButton.drawButton(minecraft, mouseX, mouseY);
		}
		GlStateManager.disableBlend();
		GlStateManager.disableLighting();
		GlStateManager.disableAlpha();
	}

	@Override
	public void drawOverlays(Minecraft minecraft, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();

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
			recipeTransferButton.drawToolTip(minecraft, mouseX, mouseY);
		}
		GlStateManager.disableBlend();
		GlStateManager.disableLighting();

		if (hoveredIngredient != null) {
			hoveredIngredient.drawOverlays(minecraft, posX, posY, recipeMouseX, recipeMouseY);
		} else if (isMouseOver(mouseX, mouseY)) {
			List<String> tooltipStrings = new ArrayList<String>();
			List<String> categoryTooltipStrings = LegacyUtil.getTooltipStrings(recipeCategory, recipeMouseX, recipeMouseY);
			tooltipStrings.addAll(categoryTooltipStrings);
			List<String> wrapperTooltips = recipeWrapper.getTooltipStrings(recipeMouseX, recipeMouseY);
			//noinspection ConstantConditions
			if (wrapperTooltips != null) {
				tooltipStrings.addAll(wrapperTooltips);
			}
			if (tooltipStrings.isEmpty() && shapelessIcon != null) {
				tooltipStrings = shapelessIcon.getTooltipStrings(recipeMouseX, recipeMouseY);
			}
			if (tooltipStrings != null && !tooltipStrings.isEmpty()) {
				TooltipRenderer.drawHoveringText(minecraft, tooltipStrings, mouseX, mouseY);
			}
		}

		GlStateManager.disableAlpha();
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		final IDrawable background = recipeCategory.getBackground();
		final Rectangle backgroundRect = new Rectangle(posX, posY, background.getWidth(), background.getHeight());
		return backgroundRect.contains(mouseX, mouseY) ||
				(recipeTransferButton != null && recipeTransferButton.isMouseOver());
	}

	@Override
	@Nullable
	public Object getIngredientUnderMouse(int mouseX, int mouseY) {
		for (GuiIngredientGroup<?> guiIngredientGroup : guiIngredientGroups.values()) {
			Object clicked = guiIngredientGroup.getIngredientUnderMouse(posX, posY, mouseX, mouseY);
			if (clicked != null) {
				return clicked;
			}
		}

		return null;
	}

	public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
		return recipeWrapper.handleClick(minecraft, mouseX - posX, mouseY - posY, mouseButton);
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
	public <T> IGuiIngredientGroup<T> getIngredientsGroup(Class<T> ingredientClass) {
		//noinspection unchecked
		GuiIngredientGroup<T> guiIngredientGroup = guiIngredientGroups.get(ingredientClass);
		if (guiIngredientGroup == null) {
			IFocus<T> focus = null;
			if (this.focus != null) {
				Object focusValue = this.focus.getValue();
				if (ingredientClass.isInstance(focusValue)) {
					//noinspection unchecked
					focus = (IFocus<T>) this.focus;
				}
			}
			guiIngredientGroup = new GuiIngredientGroup<T>(ingredientClass, focus, ingredientCycleOffset);
			guiIngredientGroups.put(ingredientClass, guiIngredientGroup);
		}
		return guiIngredientGroup;
	}

	@Override
	public void setRecipeTransferButton(int posX, int posY) {
		if (recipeTransferButton != null) {
			recipeTransferButton.xPosition = posX + this.posX;
			recipeTransferButton.yPosition = posY + this.posY;
		}
	}

	@Override
	public void setShapeless() {
		this.shapelessIcon = new ShapelessIcon();
	}

	@Override
	@Nullable
	public IFocus<?> getFocus() {
		return focus;
	}

	@Nullable
	public RecipeTransferButton getRecipeTransferButton() {
		return recipeTransferButton;
	}

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
