package mezz.jei.gui;

import cpw.mods.fml.client.FMLClientHandler;
import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IRecipeGui;
import mezz.jei.api.recipes.IRecipeHelper;
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

	/* The list of Recipe Classes that involve "stack" */
	private List<Class> recipeClasses;

	/* List of IRecipeGuis to display */
	private List<IRecipeGui> recipeGuis = new ArrayList<IRecipeGui>();

	/* List of recipes for the currently selected recipeClass */
	private List<Object> recipes = new ArrayList<Object>();;

	private int recipeClassNum = 0;
	private int pageNum = 0;
	private String pageString;
	private String title;
	private IRecipeHelper recipeHelper;

	private GuiButton nextRecipeClass;
	private GuiButton previousRecipeClass;
	private GuiButton nextPage;
	private GuiButton previousPage;

	private ResourceLocation backgroundTexture;
	private boolean visible = false;

	protected int guiLeft;
	protected int guiTop;
	protected int xSize;
	protected int ySize;

	public RecipesGui() {
		this.backgroundTexture = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "recipeBackground.png");
	}

	public void initGui(Minecraft minecraft) {
		setWorldAndResolution(minecraft, minecraft.currentScreen.width, minecraft.currentScreen.height);

		this.xSize = 176;
		this.ySize = 166;

		this.guiLeft = (minecraft.currentScreen.width - this.xSize) / 2;
		this.guiTop = (minecraft.currentScreen.height - this.ySize) / 2;

		this.titleHeight = fontRendererObj.FONT_HEIGHT + borderPadding;
		this.headerHeight = titleHeight + fontRendererObj.FONT_HEIGHT + textPadding;


		int buttonWidth = 13;
		int buttonHeight = fontRendererObj.FONT_HEIGHT + textPadding;

		int rightButtonX = guiLeft + xSize - borderPadding - buttonWidth;
		int leftButtonX = guiLeft + borderPadding;

		int recipeClassButtonTop = guiTop + borderPadding - 3;
		nextRecipeClass = new GuiButton(2, rightButtonX, recipeClassButtonTop, buttonWidth, buttonHeight, ">");
		previousRecipeClass = new GuiButton(3, leftButtonX, recipeClassButtonTop, buttonWidth, buttonHeight, "<");

		int pageButtonTop =  guiTop + titleHeight;
		nextPage = new GuiButton(4, rightButtonX, pageButtonTop, buttonWidth, buttonHeight, ">");
		previousPage = new GuiButton(5, leftButtonX, pageButtonTop, buttonWidth, buttonHeight, "<");

		addButtons();

		// on screen resize, create new recipeGuis so they reposition
		if (recipeGuis.size() > 0) {
			recipeGuis.clear();
			updateLayout();
		}
	}

	@SuppressWarnings("unchecked")
	private void addButtons() {
		this.buttonList.add(nextRecipeClass);
		this.buttonList.add(previousRecipeClass);
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
		else if (guibutton.id == nextRecipeClass.id)
			nextRecipeClass();
		else if (guibutton.id == previousRecipeClass.id)
			previousRecipeClass();
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

		List<Class> recipeClasses = null;
		switch (mode) {
			case INPUT:
				recipeClasses = JEIManager.recipeRegistry.getInputRecipeClasses(stack);
				break;
			case OUTPUT:
				recipeClasses = JEIManager.recipeRegistry.getOutputRecipeClasses(stack);
				break;
		}
		if (recipeClasses == null)
			return false;

		this.recipeClasses = recipeClasses;
		this.recipeClassNum = 0;
		this.stack = stack;
		this.mode = mode;
		this.pageNum = 0;

		updateLayout();
		return true;
	}

	public void nextRecipeClass() {
		int recipesClassesCount = recipeClasses.size();
		recipeClassNum = (recipeClassNum + 1) % recipesClassesCount;
		pageNum = 0;
		updateLayout();
	}

	public void previousRecipeClass() {
		int recipesClassesCount = recipeClasses.size();
		recipeClassNum = (recipesClassesCount + recipeClassNum - 1) % recipesClassesCount;
		pageNum = 0;
		updateLayout();
	}

	public void nextPage() {
		int pageCount = pageCount();
		pageNum = (pageNum + 1) % pageCount;
		updateLayout();
	}

	public void previousPage() {
		int pageCount = pageCount();
		pageNum = (pageCount + pageNum - 1) % pageCount;
		updateLayout();
	}

	private int pageCount() {
		if (recipes.size() <= 1)
			return 1;
		return (int)Math.ceil(recipes.size() / (float)recipeGuis.size());
	}

	private void updateLayout() {
		Class recipeClass = recipeClasses.get(recipeClassNum);
		if (recipeHelper == null || recipeHelper.getRecipeClass() != recipeClass) {
			recipeGuis.clear();

			recipeHelper = JEIManager.recipeRegistry.getRecipeHelper(recipeClass);
			title = recipeHelper.getTitle();
		}

		if (recipeGuis.isEmpty()) {
			IRecipeGui recipeGui = recipeHelper.createGui();

			int recipesPerPage = (ySize - headerHeight) / (recipeGui.getHeight() + borderPadding);
			int recipeXOffset = (xSize - recipeGui.getWidth()) / 2;
			int recipeSpacing = (ySize - headerHeight - (recipesPerPage * recipeGui.getHeight())) / (recipesPerPage + 1);

			int posX = guiLeft + recipeXOffset;
			int posY = guiTop + headerHeight + recipeSpacing;

			recipeGui.setPosition(posX, posY);
			recipeGuis.add(recipeGui);

			// add more recipeGuis if they fit on a page
			for (int i = 0; i < recipesPerPage - 1; i++) {
				recipeGui = recipeHelper.createGui();
				posY += recipeGui.getHeight() + recipeSpacing;
				recipeGui.setPosition(posX, posY);
				recipeGuis.add(recipeGui);
			}
		}

		switch (mode) {
			case INPUT:
				recipes = JEIManager.recipeRegistry.getInputRecipes(recipeClass, stack);
				break;
			case OUTPUT:
				recipes = JEIManager.recipeRegistry.getOutputRecipes(recipeClass, stack);
				break;
		}
		if (recipes == null) {
			recipes = new ArrayList<Object>();
		}

		for (int i = 0; i < recipeGuis.size(); i++) {
			IRecipeGui recipeGui = recipeGuis.get(i);

			int recipeIndex = (pageNum * recipeGuis.size()) + i;
			if (recipeIndex >= recipes.size()) {
				recipeGui.setRecipe(null, null);
				break;
			}
			Object recipe = recipes.get(recipeIndex);

			recipeGui.setRecipe(recipe, stack);
		}

		nextPage.enabled = previousPage.enabled = (pageCount() > 1);
		nextRecipeClass.enabled = previousRecipeClass.enabled = (recipeClasses.size() > 1);

		this.pageString = (pageNum + 1) + "/" + pageCount();
	}

	public void draw(int mouseX, int mouseY) {
		Minecraft minecraft = Minecraft.getMinecraft();

		nextRecipeClass.drawButton(minecraft, mouseX, mouseY);
		previousRecipeClass.drawButton(minecraft, mouseX, mouseY);

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

		if (recipeGuis.size() == 0)
			return;

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
