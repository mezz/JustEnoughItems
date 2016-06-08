package mezz.jei.gui;

import javax.annotation.Nonnull;
import java.util.List;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.ingredients.GuiFluidStackGroup;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

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

	public <T extends IRecipeWrapper> RecipeLayout(int index, int posX, int posY, @Nonnull IRecipeCategory<T> recipeCategory, @Nonnull T recipeWrapper, @Nonnull MasterFocus focus) {
		this.recipeCategory = recipeCategory;
		this.guiItemStackGroup = new GuiItemStackGroup(new Focus<>(focus.getMode(), focus.getItemStack()));
		this.guiFluidStackGroup = new GuiFluidStackGroup(new Focus<>(focus.getMode(), focus.getFluidStack()));
		int width = recipeCategory.getBackground().getWidth();
		int height = recipeCategory.getBackground().getHeight();
		this.recipeTransferButton = new RecipeTransferButton(recipeTransferButtonIndex + index, posX + width + 2, posY + height - RECIPE_BUTTON_SIZE, RECIPE_BUTTON_SIZE, RECIPE_BUTTON_SIZE, "+");
		this.posX = posX;
		this.posY = posY;

		this.recipeWrapper = recipeWrapper;
		recipeCategory.setRecipe(this, recipeWrapper);
	}

	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		IDrawable background = recipeCategory.getBackground();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();

		GlStateManager.pushMatrix();
		GlStateManager.translate(posX, posY, 0.0F);
		{
			background.draw(minecraft);
			recipeCategory.drawExtras(minecraft);
			recipeCategory.drawAnimations(minecraft);
			recipeWrapper.drawAnimations(minecraft, background.getWidth(), background.getHeight());
		}
		GlStateManager.popMatrix();

		recipeTransferButton.drawButton(minecraft, mouseX, mouseY);
		GlStateManager.disableBlend();
		GlStateManager.disableLighting();

		final int recipeMouseX = mouseX - posX;
		final int recipeMouseY = mouseY - posY;

		GlStateManager.pushMatrix();
		GlStateManager.translate(posX, posY, 0.0F);
		{
			recipeWrapper.drawInfo(minecraft, background.getWidth(), background.getHeight(), recipeMouseX, recipeMouseY);
		}
		GlStateManager.popMatrix();

		RenderHelper.enableGUIStandardItemLighting();
		GuiIngredient hoveredItemStack = guiItemStackGroup.draw(minecraft, posX, posY, mouseX, mouseY);
		RenderHelper.disableStandardItemLighting();
		GuiIngredient hoveredFluidStack = guiFluidStackGroup.draw(minecraft, posX, posY, mouseX, mouseY);

		if (hoveredItemStack != null) {
			RenderHelper.enableGUIStandardItemLighting();
			hoveredItemStack.drawHovered(minecraft, posX, posY, recipeMouseX, recipeMouseY);
			RenderHelper.disableStandardItemLighting();
		} else if (hoveredFluidStack != null) {
			hoveredFluidStack.drawHovered(minecraft, posX, posY, recipeMouseX, recipeMouseY);
		} else if (isMouseOver(mouseX, mouseY)) {
			List<String> tooltipStrings = recipeWrapper.getTooltipStrings(recipeMouseX, recipeMouseY);
			if (tooltipStrings != null && !tooltipStrings.isEmpty()) {
				TooltipRenderer.drawHoveringText(minecraft, tooltipStrings, mouseX, mouseY);
			}
		}

		GlStateManager.disableAlpha();
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		final int recipeMouseX = mouseX - posX;
		final int recipeMouseY = mouseY - posY;
		final IDrawable background = recipeCategory.getBackground();
		return recipeMouseX >= 0 && recipeMouseX < background.getWidth() && recipeMouseY >= 0 && recipeMouseY < background.getHeight();
	}

	public Focus<?> getFocusUnderMouse(int mouseX, int mouseY) {
		Focus<?> focus = guiItemStackGroup.getFocusUnderMouse(posX, posY, mouseX, mouseY);
		if (focus == null) {
			focus = guiFluidStackGroup.getFocusUnderMouse(posX, posY, mouseX, mouseY);
		}
		return focus;
	}

	public boolean handleClick(@Nonnull Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
		return recipeWrapper.handleClick(minecraft, mouseX - posX, mouseY - posY, mouseButton);
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
