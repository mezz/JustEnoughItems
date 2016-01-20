package mezz.jei.gui;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.GuiFluidStackGroup;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.ingredients.GuiItemStackGroup;

public class RecipeLayout implements IRecipeLayout {
	private static final int RECIPE_BUTTON_SIZE = 12;
	public static final int recipeTransferButtonIndex = 100;

	@Nonnull
	private final IRecipeCategory recipeCategory;
	@Nonnull
	private final GuiItemStackGroup guiItemStackGroup;
	@Nonnull
	private final GuiFluidStackGroup guiFluidStackGroup;
	@Nonnull
	private final RecipeTransferButton recipeTransferButton;
	@Nonnull
	private final IRecipeWrapper recipeWrapper;

	private final int posX;
	private final int posY;

	public RecipeLayout(int index, int posX, int posY, @Nonnull IRecipeCategory recipeCategory, @Nonnull IRecipeWrapper recipeWrapper, @Nonnull Focus focus) {
		this.recipeCategory = recipeCategory;
		this.guiItemStackGroup = new GuiItemStackGroup();
		this.guiFluidStackGroup = new GuiFluidStackGroup();
		int width = recipeCategory.getBackground().getWidth();
		int height = recipeCategory.getBackground().getHeight();
		this.recipeTransferButton = new RecipeTransferButton(recipeTransferButtonIndex + index, posX + width + 2, posY + height - RECIPE_BUTTON_SIZE, RECIPE_BUTTON_SIZE, RECIPE_BUTTON_SIZE, "+");
		this.posX = posX;
		this.posY = posY;

		this.recipeWrapper = recipeWrapper;
		this.guiItemStackGroup.setFocus(focus);
		this.guiFluidStackGroup.setFocus(focus);
		this.recipeCategory.setRecipe(this, recipeWrapper);
	}

	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(posX, posY, 0.0F);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();

		IDrawable background = recipeCategory.getBackground();
		background.draw(minecraft);
		recipeCategory.drawExtras(minecraft);

		if (Config.isRecipeAnimationsEnabled()) {
			recipeCategory.drawAnimations(minecraft);
			recipeWrapper.drawAnimations(minecraft, background.getWidth(), background.getHeight());
		}

		GlStateManager.translate(-posX, -posY, 0.0F);
		recipeTransferButton.drawButton(minecraft, mouseX, mouseY);
		GlStateManager.disableBlend();
		GlStateManager.translate(posX, posY, 0.0F);
		GlStateManager.disableLighting();

		final int recipeMouseX = mouseX - posX;
		final int recipeMouseY = mouseY - posY;

		try {
			recipeWrapper.drawInfo(minecraft, background.getWidth(), background.getHeight(), recipeMouseX, recipeMouseY);
		} catch (AbstractMethodError ignored) {
			// older wrappers don't have this method
		}
		recipeWrapper.drawInfo(minecraft, background.getWidth(), background.getHeight());

		RenderHelper.enableGUIStandardItemLighting();
		GuiIngredient hoveredItemStack = guiItemStackGroup.draw(minecraft, recipeMouseX, recipeMouseY);
		RenderHelper.disableStandardItemLighting();
		GuiIngredient hoveredFluidStack = guiFluidStackGroup.draw(minecraft, recipeMouseX, recipeMouseY);

		if (hoveredItemStack != null) {
			RenderHelper.enableGUIStandardItemLighting();
			hoveredItemStack.drawHovered(minecraft, recipeMouseX, recipeMouseY);
			RenderHelper.disableStandardItemLighting();
		} else if (hoveredFluidStack != null) {
			hoveredFluidStack.drawHovered(minecraft, recipeMouseX, recipeMouseY);
		} else if (recipeMouseX >= 0 && recipeMouseX < background.getWidth() && recipeMouseY >= 0 && recipeMouseY < background.getHeight()) {
			List<String> tooltipStrings = null;
			try {
				tooltipStrings = recipeWrapper.getTooltipStrings(recipeMouseX, recipeMouseY);
			} catch (AbstractMethodError ignored) {
				// older wrappers don't have this method
			}
			if (tooltipStrings != null && !tooltipStrings.isEmpty()) {
				TooltipRenderer.drawHoveringText(minecraft, tooltipStrings, recipeMouseX, recipeMouseY);
			}
		}

		GlStateManager.disableAlpha();
		GlStateManager.popMatrix();
	}

	public Focus getFocusUnderMouse(int mouseX, int mouseY) {
		Focus focus = guiItemStackGroup.getFocusUnderMouse(mouseX - posX, mouseY - posY);
		if (focus == null) {
			focus = guiFluidStackGroup.getFocusUnderMouse(mouseX - posX, mouseY - posY);
		}
		return focus;
	}

	public boolean handleClick(@Nonnull Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
		try {
			return recipeWrapper.handleClick(minecraft, mouseX - posX, mouseY - posY, mouseButton);
		} catch (AbstractMethodError ignored) { // older wrappers don't have this method
			return false;
		}
	}

	@Override
	@Nonnull
	public GuiItemStackGroup getItemStacks() {
		return guiItemStackGroup;
	}

	@Override
	@Nonnull
	public IGuiFluidStackGroup getFluidStacks() {
		return guiFluidStackGroup;
	}

	@Override
	public void setRecipeTransferButton(int posX, int posY) {
		recipeTransferButton.xPosition = posX + this.posX;
		recipeTransferButton.yPosition = posY + this.posY;
	}

	@Nonnull
	public RecipeTransferButton getRecipeTransferButton() {
		return recipeTransferButton;
	}

	@Nonnull
	public IRecipeWrapper getRecipeWrapper() {
		return recipeWrapper;
	}

	@Nonnull
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
