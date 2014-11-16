package mezz.jei.gui;

import cpw.mods.fml.client.FMLClientHandler;
import mezz.jei.api.JEIManager;
import mezz.jei.api.recipes.IRecipeGui;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.config.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class RecipesGui extends GuiScreen {
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
	private ItemStack stack;
	/* List of Recipe Types that involve "stack" */
	private List<IRecipeType> recipeTypes = new ArrayList<IRecipeType>();

	/* List of IRecipeGuis to display */
	private final List<IRecipeGui> recipeGuis = new ArrayList<IRecipeGui>();

	/* List of recipes for the currently selected recipeClass */
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
	private boolean visible = false;

	protected int guiLeft;
	protected int guiTop;
	protected int xSize;
	protected int ySize;

	public RecipesGui() {
	}

	public void initGui(Minecraft minecraft) {
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

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		ItemStack stack;
		for (IRecipeGui recipeGui : recipeGuis) {
			stack = recipeGui.getStackUnderMouse(mouseX, mouseY);
			if (stack != null) {
				mouseClickedStack(mouseButton, stack);
				return;
			}
		}
	}

	public boolean mouseClickedStack(int mouseButton, ItemStack stack) {
		if (mouseButton == 0) {
			return setOutputStack(stack);
		} else if (mouseButton == 1) {
			return setInputStack(stack);
		}
		return false;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);

		if (guibutton.id == nextPage.id)
			nextPage();
		else if (guibutton.id == previousPage.id)
			previousPage();
		else if (guibutton.id == nextRecipeType.id)
			nextRecipeType();
		else if (guibutton.id == previousRecipeType.id)
			previousRecipeType();
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible && recipes.size() > 0;
	}

	public boolean setInputStack(ItemStack stack) {
		return setStack(stack, Mode.INPUT);
	}

	public boolean setOutputStack(ItemStack stack) {
		return setStack(stack, Mode.OUTPUT);
	}

	private boolean setStack(ItemStack stack, Mode mode) {
		if (stack == null)
			return false;
		if (this.stack != null && this.stack.equals(stack) && this.mode == mode)
			return true;

		switch (mode) {
			case INPUT:
				recipeTypes = JEIManager.recipeRegistry.getRecipeTypesForInput(stack);
				break;
			case OUTPUT:
				recipeTypes = JEIManager.recipeRegistry.getRecipeTypesForOutput(stack);
				break;
		}
		if (recipeTypes == null) {
			recipeTypes = new ArrayList<IRecipeType>();
			return false;
		}

		this.stack = stack;
		this.mode = mode;
		this.recipeTypeIndex = 0;
		this.pageIndex = 0;

		updateLayout();
		return true;
	}

	public void nextRecipeType() {
		int recipesTypesCount = recipeTypes.size();
		recipeTypeIndex = (recipeTypeIndex + 1) % recipesTypesCount;
		pageIndex = 0;
		updateLayout();
	}

	public void previousRecipeType() {
		int recipesTypesCount = recipeTypes.size();
		recipeTypeIndex = (recipesTypesCount + recipeTypeIndex - 1) % recipesTypesCount;
		pageIndex = 0;
		updateLayout();
	}

	public void nextPage() {
		int pageCount = pageCount();
		pageIndex = (pageIndex + 1) % pageCount;
		updateLayout();
	}

	public void previousPage() {
		int pageCount = pageCount();
		pageIndex = (pageCount + pageIndex - 1) % pageCount;
		updateLayout();
	}

	private int pageCount() {
		if (recipes.size() <= 1)
			return 1;
		return (int)Math.ceil(recipes.size() / (float)recipesPerPage);
	}

	private void updateLayout() {
		if (recipeTypes.isEmpty())
			return;

		IRecipeType recipeType = recipeTypes.get(recipeTypeIndex);

		title = recipeType.getLocalizedName();

		switch (mode) {
			case INPUT:
				recipes = JEIManager.recipeRegistry.getInputRecipes(recipeType, stack);
				break;
			case OUTPUT:
				recipes = JEIManager.recipeRegistry.getOutputRecipes(recipeType, stack);
				break;
		}
		if (recipes == null) {
			recipes = new ArrayList<Object>();
		}

		recipesPerPage = (ySize - headerHeight) / (recipeType.displayHeight() + borderPadding);
		int recipeXOffset = (xSize - recipeType.displayWidth()) / 2;
		int recipeSpacing = (ySize - headerHeight - (recipesPerPage * recipeType.displayHeight())) / (recipesPerPage + 1);

		int posX = guiLeft + recipeXOffset;
		int posY = guiTop + headerHeight + recipeSpacing;

		recipeGuis.clear();
		for (int i = 0; i < recipesPerPage; i++) {
			int recipeIndex = (pageIndex * recipesPerPage) + i;
			if (recipeIndex >= recipes.size())
				break;

			Object recipe = recipes.get(recipeIndex);
			IRecipeHelper recipeHelper = JEIManager.recipeRegistry.getRecipeHelper(recipe.getClass());
			IRecipeGui recipeGui = recipeHelper.createGui();
			recipeGui.setPosition(posX, posY);
			posY += recipeType.displayHeight() + recipeSpacing;

			recipeGui.setRecipe(recipe, stack);
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

			fontRendererObj.drawString(title, (xSize / 2) - fontRendererObj.getStringWidth(title) / 2, borderPadding, Color.black.getRGB());
			fontRendererObj.drawString(pageString, (xSize / 2) - fontRendererObj.getStringWidth(pageString) / 2, titleHeight + textPadding, Color.black.getRGB());

		}
		GL11.glPopMatrix();

		for (IRecipeGui recipeGui : recipeGuis) {
			if (recipeGui.hasRecipe())
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

	protected void bindTexture(ResourceLocation texturePath) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		FMLClientHandler.instance().getClient().getTextureManager().bindTexture(texturePath);
	}

}
