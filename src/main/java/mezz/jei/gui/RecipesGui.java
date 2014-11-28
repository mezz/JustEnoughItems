package mezz.jei.gui;

import cpw.mods.fml.client.FMLClientHandler;
import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeGui;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeType;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.config.Constants;
import mezz.jei.input.IClickable;
import mezz.jei.input.IKeyable;
import mezz.jei.input.IShowsItemStacks;
import mezz.jei.util.Log;
import mezz.jei.util.MathUtil;
import mezz.jei.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class RecipesGui extends GuiScreen implements IShowsItemStacks, IClickable, IKeyable {
	private enum Mode {
		INPUT, OUTPUT
	}

	private static final int borderPadding = 6;
	private static final int textPadding = 2;

	private int titleHeight;
	private int headerHeight;

	/* Whether this GUI is displaying input or output recipes */
	private Mode mode;

	/* The ItemStack that is the focus of this GUI */
	private ItemStack focusStack;
	/* List of Recipe Types that involve "focusStack" */
	@Nonnull
	private List<IRecipeType> recipeTypes = new ArrayList<IRecipeType>();

	/* List of RecipeGui to display */
	@Nonnull
	private final List<IRecipeGui> recipeGuis = new ArrayList<IRecipeGui>();

	/* List of recipes for the currently selected recipeClass */
	@Nonnull
	private List<Object> recipes = new ArrayList<Object>();
	private int recipesPerPage;

	private int recipeTypeIndex = 0;
	private int pageIndex = 0;
	private String pageString;
	private String title;

	private GuiButton nextRecipeType;
	private GuiButton previousRecipeType;
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

		int buttonWidth = 13;
		int buttonHeight = fontRendererObj.FONT_HEIGHT + textPadding;

		int rightButtonX = guiLeft + xSize - borderPadding - buttonWidth;
		int leftButtonX = guiLeft + borderPadding;

		int recipeClassButtonTop = guiTop + borderPadding - 3;
		nextRecipeType = new GuiButton(2, rightButtonX, recipeClassButtonTop, buttonWidth, buttonHeight, ">");
		previousRecipeType = new GuiButton(3, leftButtonX, recipeClassButtonTop, buttonWidth, buttonHeight, "<");

		int pageButtonTop =  guiTop + titleHeight;
		nextPage = new GuiButton(4, rightButtonX, pageButtonTop, buttonWidth, buttonHeight, ">");
		previousPage = new GuiButton(5, leftButtonX, pageButtonTop, buttonWidth, buttonHeight, "<");

		addButtons();

		// on screen resize
		if (recipeGuis.size() > 0) {
			resetLayout();
		}
	}

	private void resetLayout() {
		recipeGuis.clear();
		pageIndex = 0;
		updateLayout();
	}

	@SuppressWarnings("unchecked")
	private void addButtons() {
		this.buttonList.add(nextRecipeType);
		this.buttonList.add(previousRecipeType);
		this.buttonList.add(nextPage);
		this.buttonList.add(previousPage);
	}

	@Nullable
	@Override
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		for (IRecipeGui recipeGui : recipeGuis) {
			ItemStack stack = recipeGui.getStackUnderMouse(mouseX, mouseY);
			if (stack != null) {
				return stack;
			}
		}
		return null;
	}

	@Override
	public void handleMouseClicked(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
		handleMouseInput();
	}

	@Override
	public boolean hasKeyboardFocus() {
		return false;
	}

	@Override
	public void setKeyboardFocus(boolean keyboardFocus) {

	}

	@Override
	public boolean onKeyPressed(int keyCode) {
		return false;
	}

	@Override
	public void open() {
		setOpen(true);
	}

	@Override
	public void close() {
		setOpen(false);
	}

	@Override
	public boolean isOpen() {
		return isOpen && recipes.size() > 0;
	}

	public void showRecipes(@Nonnull ItemStack stack) {
		if (setStack(stack, Mode.OUTPUT))
			setOpen(true);
	}

	public void showUses(@Nonnull ItemStack stack) {
		if(setStack(stack, Mode.INPUT))
			setOpen(true);
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton guibutton) {
		if (guibutton.id == nextPage.id)
			nextPage();
		else if (guibutton.id == previousPage.id)
			previousPage();
		else if (guibutton.id == nextRecipeType.id)
			nextRecipeType();
		else if (guibutton.id == previousRecipeType.id)
			previousRecipeType();
	}

	public void setOpen(boolean open) {
		this.isOpen = open;
	}

	private boolean setStack(@Nullable ItemStack stack, @Nonnull Mode mode) {
		if (stack == null)
			return false;
		if (this.focusStack != null && this.focusStack.equals(stack) && this.mode == mode)
			return true;

		List<IRecipeType> types = null;
		switch (mode) {
			case INPUT:
				types = JEIManager.recipeRegistry.getRecipeTypesForInput(stack);
				break;
			case OUTPUT:
				types = JEIManager.recipeRegistry.getRecipeTypesForOutput(stack);
				break;
		}
		if (types.isEmpty()) {
			return false;
		}

		this.recipeTypes = types;
		this.focusStack = stack;
		this.mode = mode;
		this.recipeTypeIndex = 0;
		this.pageIndex = 0;

		updateLayout();
		return true;
	}

	private void nextRecipeType() {
		int recipesTypesCount = recipeTypes.size();
		recipeTypeIndex = (recipeTypeIndex + 1) % recipesTypesCount;
		pageIndex = 0;
		updateLayout();
	}

	private void previousRecipeType() {
		int recipesTypesCount = recipeTypes.size();
		recipeTypeIndex = (recipesTypesCount + recipeTypeIndex - 1) % recipesTypesCount;
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
		if (recipes.size() <= 1)
			return 1;

		return MathUtil.divideCeil(recipes.size(), recipesPerPage);
	}

	private void updateLayout() {
		if (recipeTypes.isEmpty())
			return;

		IRecipeType recipeType = recipeTypes.get(recipeTypeIndex);

		title = recipeType.getLocalizedName();

		switch (mode) {
			case INPUT:
				recipes = JEIManager.recipeRegistry.getInputRecipes(recipeType, focusStack);
				break;
			case OUTPUT:
				recipes = JEIManager.recipeRegistry.getOutputRecipes(recipeType, focusStack);
				break;
		}

		IDrawable recipeBackground = recipeType.getBackground();

		recipesPerPage = (ySize - headerHeight) / (recipeBackground.getHeight() + borderPadding);
		int recipeXOffset = (xSize - recipeBackground.getWidth()) / 2;
		int recipeSpacing = (ySize - headerHeight - (recipesPerPage * recipeBackground.getHeight())) / (recipesPerPage + 1);

		int posX = guiLeft + recipeXOffset;
		int posY = guiTop + headerHeight + recipeSpacing;

		recipeGuis.clear();
		for (int i = 0; i < recipesPerPage; i++) {
			int recipeIndex = (pageIndex * recipesPerPage) + i;
			if (recipeIndex >= recipes.size())
				break;

			Object recipe = recipes.get(recipeIndex);
			IRecipeHandler recipeHandler = JEIManager.recipeRegistry.getRecipeHandler(recipe.getClass());
			if (recipeHandler == null) {
				Log.error("Couldn't find recipe helper for recipe: " + recipe);
				continue;
			}

			PositionedRecipeGui recipeGui = new PositionedRecipeGui(recipeType.createGui());
			recipeGui.setPosition(posX, posY);
			posY += recipeBackground.getHeight() + recipeSpacing;

			IRecipeWrapper recipeWrapper = recipeHandler.getRecipeWrapper(recipe);
			recipeGui.setRecipe(recipeWrapper, focusStack);
			recipeGuis.add(recipeGui);
		}

		nextPage.enabled = previousPage.enabled = (pageCount() > 1);
		nextRecipeType.enabled = previousRecipeType.enabled = (recipeTypes.size() > 1);

		this.pageString = (pageIndex + 1) + "/" + pageCount();
	}

	public void draw(int mouseX, int mouseY) {
		Minecraft minecraft = Minecraft.getMinecraft();

		nextRecipeType.drawButton(minecraft, mouseX, mouseY);
		previousRecipeType.drawButton(minecraft, mouseX, mouseY);

		nextPage.drawButton(minecraft, mouseX, mouseY);
		previousPage.drawButton(minecraft, mouseX, mouseY);

		GL11.glPushMatrix();
		{
			GL11.glTranslatef(guiLeft, guiTop, 0.0F);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			StringUtil.drawCenteredString(fontRendererObj, title, xSize, borderPadding, Color.black.getRGB());
			StringUtil.drawCenteredString(fontRendererObj, pageString, xSize, titleHeight + textPadding, Color.black.getRGB());
		}
		GL11.glPopMatrix();

		for (IRecipeGui recipeGui : recipeGuis) {
			recipeGui.draw(minecraft, mouseX, mouseY);
		}

		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
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
