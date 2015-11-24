package mezz.jei.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.config.Constants;
import mezz.jei.input.IClickable;
import mezz.jei.input.IShowsItemStacks;
import mezz.jei.util.Log;
import mezz.jei.util.MathUtil;
import mezz.jei.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.config.GuiButtonExt;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;

public class RecipesGui extends GuiScreen implements IShowsItemStacks, IClickable {
	private enum Mode {
		INPUT, OUTPUT
	}

	private static final int borderPadding = 8;
	private static final int textPadding = 5;

	private int titleHeight;
	private int headerHeight;
	private int buttonWidth;

	/* Whether this GUI is dispzzlaying input or output recipes */
	private Mode mode;

	/* The ItemStack that is the focus of this GUI */
	private ItemStack focusStack;
	/* List of Recipe Categories that involve "focusStack" */
	@Nonnull
	private ImmutableList<IRecipeCategory> recipeCategories = ImmutableList.of();

	/* List of RecipeWidget to display */
	@Nonnull
	private final List<RecipeWidget> recipeWidgets = new ArrayList<>();

	/* List of recipes for the currently selected recipeClass */
	@Nonnull
	private ImmutableList<Object> recipes = ImmutableList.of();
	private int recipesPerPage;

	private int recipeCategoryIndex = 0;
	private int pageIndex = 0;
	private String pageString;
	private String title;

	private GuiButton nextRecipeCategory;
	private GuiButton previousRecipeCategory;
	private GuiButton nextPage;
	private GuiButton previousPage;

	private ResourceLocation backgroundTexture;
	private boolean isOpen = false;

	private int guiLeft;
	private int guiTop;
	private int xSize;
	private int ySize;

	public void initGui(@Nonnull Minecraft minecraft) {
		setWorldAndResolution(minecraft, minecraft.currentScreen.width, minecraft.currentScreen.height);

		this.xSize = 176;

		if (this.height > 300) {
			this.ySize = 256;
			this.backgroundTexture = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "recipeBackgroundTall.png");
		} else {
			this.ySize = 166;
			this.backgroundTexture = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "recipeBackground.png");
		}	

		this.guiLeft = (minecraft.currentScreen.width - this.xSize) / 2;
		this.guiTop = (minecraft.currentScreen.height - this.ySize) / 2;

		this.titleHeight = fontRendererObj.FONT_HEIGHT + borderPadding;
		this.headerHeight = titleHeight + fontRendererObj.FONT_HEIGHT + textPadding;

		buttonWidth = 13;
		
		int buttonHeight = fontRendererObj.FONT_HEIGHT + 3;

		int rightButtonX = guiLeft + xSize - borderPadding - buttonWidth;
		int leftButtonX = guiLeft + borderPadding;

		int recipeClassButtonTop = guiTop + borderPadding - 2;
		nextRecipeCategory = new GuiButtonExt(2, rightButtonX, recipeClassButtonTop, buttonWidth, buttonHeight, ">");
		previousRecipeCategory = new GuiButtonExt(3, leftButtonX, recipeClassButtonTop, buttonWidth, buttonHeight, "<");

		int pageButtonTop = guiTop + titleHeight + 3;
		nextPage = new GuiButtonExt(4, rightButtonX, pageButtonTop, buttonWidth, buttonHeight, ">");
		previousPage = new GuiButtonExt(5, leftButtonX, pageButtonTop, buttonWidth, buttonHeight, "<");

		addButtons();

		// on screen resize
		if (recipeWidgets.size() > 0) {
			resetLayout();
		}
	}

	// don't post GUI events or we end up in an infinite loop handling them
	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		this.mc = mc;
		this.itemRender = mc.getRenderItem();
		this.fontRendererObj = mc.fontRendererObj;
		this.width = width;
		this.height = height;
		this.buttonList.clear();
		this.initGui();
	}

	private void resetLayout() {
		recipeWidgets.clear();
		pageIndex = 0;
		updateLayout();
	}

	@SuppressWarnings("unchecked")
	private void addButtons() {
		this.buttonList.add(nextRecipeCategory);
		this.buttonList.add(previousRecipeCategory);
		this.buttonList.add(nextPage);
		this.buttonList.add(previousPage);
	}

	@Nullable
	@Override
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		if (!isOpen) {
			return null;
		}
		for (RecipeWidget recipeWidget : recipeWidgets) {
			ItemStack stack = recipeWidget.getStackUnderMouse(mouseX, mouseY);
			if (stack != null) {
				return stack;
			}
		}
		return null;
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!isOpen) {
			return false;
		}

		try {
			handleMouseInput();
		} catch (IOException e) {
			Log.error("IOException on mouse click.", e);
		}
		return false;
	}

	@Override
	public void open() {
		this.isOpen = true;
	}

	@Override
	public void close() {
		this.isOpen = false;
	}

	@Override
	public boolean isOpen() {
		return isOpen && recipes.size() > 0;
	}

	public void showRecipes(@Nonnull ItemStack stack) {
		if (setStack(stack, Mode.OUTPUT)) {
			open();
		}
	}

	public void showUses(@Nonnull ItemStack stack) {
		if (setStack(stack, Mode.INPUT)) {
			open();
		}
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton guibutton) {
		if (guibutton.id == nextPage.id) {
			nextPage();
		} else if (guibutton.id == previousPage.id) {
			previousPage();
		} else if (guibutton.id == nextRecipeCategory.id) {
			nextRecipeCategory();
		} else if (guibutton.id == previousRecipeCategory.id) {
			previousRecipeCategory();
		}
	}

	private boolean setStack(@Nullable ItemStack stack, @Nonnull Mode mode) {
		if (stack == null) {
			return false;
		}
		if (this.focusStack != null && this.focusStack.equals(stack) && this.mode == mode) {
			return true;
		}

		ImmutableList<IRecipeCategory> types = null;
		switch (mode) {
			case INPUT:
				types = JEIManager.recipeRegistry.getRecipeCategoriesWithInput(stack);
				break;
			case OUTPUT:
				types = JEIManager.recipeRegistry.getRecipeCategoriesWithOutput(stack);
				break;
		}
		if (types.isEmpty()) {
			return false;
		}

		this.recipeCategories = types;
		this.focusStack = stack;
		this.mode = mode;
		this.recipeCategoryIndex = 0;
		this.pageIndex = 0;

		updateLayout();
		return true;
	}

	private void nextRecipeCategory() {
		int recipesTypesCount = recipeCategories.size();
		recipeCategoryIndex = (recipeCategoryIndex + 1) % recipesTypesCount;
		pageIndex = 0;
		updateLayout();
	}

	private void previousRecipeCategory() {
		int recipesTypesCount = recipeCategories.size();
		recipeCategoryIndex = (recipesTypesCount + recipeCategoryIndex - 1) % recipesTypesCount;
		pageIndex = 0;
		updateLayout();
	}

	private void nextPage() {
		int pageCount = pageCount();
		pageIndex = (pageIndex + 1) % pageCount;
		updateLayout();
	}

	private void previousPage() {
		int pageCount = pageCount();
		pageIndex = (pageCount + pageIndex - 1) % pageCount;
		updateLayout();
	}

	private int pageCount() {
		if (recipes.size() <= 1) {
			return 1;
		}

		return MathUtil.divideCeil(recipes.size(), recipesPerPage);
	}

	private void updateLayout() {
		if (recipeCategories.isEmpty()) {
			return;
		}

		IRecipeCategory recipeCategory = recipeCategories.get(recipeCategoryIndex);

		title = recipeCategory.getCategoryTitle();

		switch (mode) {
			case INPUT:
				recipes = JEIManager.recipeRegistry.getRecipesWithInput(recipeCategory, focusStack);
				break;
			case OUTPUT:
				recipes = JEIManager.recipeRegistry.getRecipesWithOutput(recipeCategory, focusStack);
				break;
		}

		IDrawable recipeBackground = recipeCategory.getBackground();

		recipesPerPage = (ySize - headerHeight) / (recipeBackground.getHeight() + borderPadding);
		final int recipeXOffset = (xSize - recipeBackground.getWidth()) / 2;
		final int recipeSpacing = (ySize - headerHeight - (recipesPerPage * recipeBackground.getHeight())) / (recipesPerPage + 1);

		final int posX = guiLeft + recipeXOffset;
		int posY = guiTop + headerHeight + recipeSpacing;

		recipeWidgets.clear();
		for (int recipeIndex = pageIndex * recipesPerPage; recipeIndex < recipes.size() && recipeWidgets.size() < recipesPerPage; recipeIndex++) {
			Object recipe = recipes.get(recipeIndex);
			IRecipeHandler recipeHandler = JEIManager.recipeRegistry.getRecipeHandler(recipe.getClass());
			if (recipeHandler == null) {
				Log.error("Couldn't find recipe handler for recipe: %s", recipe);
				continue;
			}

			RecipeWidget recipeWidget = new RecipeWidget(recipeCategory);
			recipeWidget.setPosition(posX, posY);
			posY += recipeBackground.getHeight() + recipeSpacing;

			@SuppressWarnings("unchecked")
			IRecipeWrapper recipeWrapper = recipeHandler.getRecipeWrapper(recipe);
			recipeWidget.setRecipe(recipeWrapper, focusStack);
			recipeWidgets.add(recipeWidget);
		}

		nextPage.enabled = previousPage.enabled = (pageCount() > 1);
		nextRecipeCategory.enabled = previousRecipeCategory.enabled = (recipeCategories.size() > 1);

		this.pageString = (pageIndex + 1) + "/" + pageCount();
	}

	public void draw(int mouseX, int mouseY) {
		Minecraft minecraft = Minecraft.getMinecraft();

		nextRecipeCategory.drawButton(minecraft, mouseX, mouseY);
		previousRecipeCategory.drawButton(minecraft, mouseX, mouseY);

		nextPage.drawButton(minecraft, mouseX, mouseY);
		previousPage.drawButton(minecraft, mouseX, mouseY);

		GL11.glPushMatrix();
		{
			GL11.glTranslatef(guiLeft, guiTop, 0.0F);
			
			drawRect(borderPadding + buttonWidth, borderPadding - 2, xSize - borderPadding - buttonWidth, borderPadding + 10, 0x30000000);
			drawRect(borderPadding + buttonWidth, titleHeight + textPadding - 2, xSize - borderPadding - buttonWidth, titleHeight + textPadding + 10, 0x30000000);

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			StringUtil.drawCenteredString(fontRendererObj, title, xSize, borderPadding, Color.WHITE.getRGB(), true);
			StringUtil.drawCenteredString(fontRendererObj, pageString, xSize, titleHeight + textPadding, Color.WHITE.getRGB(), true);
		}
		GL11.glPopMatrix();

		RecipeWidget hovered = null;
		for (RecipeWidget recipeWidget : recipeWidgets) {
			if (recipeWidget.getStackUnderMouse(mouseX, mouseY) != null) {
				hovered = recipeWidget;
			} else {
				recipeWidget.draw(minecraft, mouseX, mouseY);
			}
		}
		if (hovered != null) {
			hovered.draw(minecraft, mouseX, mouseY);
		}
	}

	public void drawBackground() {
		this.zLevel = -100;
		this.drawDefaultBackground();

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		bindTexture(backgroundTexture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.zLevel = 0;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}

	private void bindTexture(ResourceLocation texturePath) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		FMLClientHandler.instance().getClient().getTextureManager().bindTexture(texturePath);
	}

}
