package mezz.jei.gui.recipes;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.config.ClientConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mezz.jei.gui.HoverChecker;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.elements.GuiIconButtonSmall;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.MouseUtil;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.transfer.RecipeTransferUtil;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.StringUtil;
import net.minecraft.util.text.TranslationTextComponent;

public class RecipesGui extends Screen implements IRecipesGui, IShowsRecipeFocuses, IRecipeLogicStateListener {
	private static final int borderPadding = 6;
	private static final int innerPadding = 14;
	private static final int buttonWidth = 13;
	private static final int buttonHeight = 13;
	private final RecipeTransferManager recipeTransferManager;

	private int headerHeight;

	/* Internal logic for the gui, handles finding recipes */
	private final IRecipeGuiLogic logic;

	/* List of RecipeLayout to display */
	private final List<RecipeLayout<?>> recipeLayouts = new ArrayList<>();

	private String pageString = "1/1";
	private String title = "";
	private final DrawableNineSliceTexture background;

	private final RecipeCatalysts recipeCatalysts;
	private final RecipeGuiTabs recipeGuiTabs;

	private final HoverChecker titleHoverChecker = new HoverChecker();

	private final GuiIconButtonSmall nextRecipeCategory;
	private final GuiIconButtonSmall previousRecipeCategory;
	private final GuiIconButtonSmall nextPage;
	private final GuiIconButtonSmall previousPage;

	@Nullable
	private Screen parentScreen;
	private int xSize;
	private int ySize;
	private int guiLeft;
	private int guiTop;

	private boolean init = false;

	public RecipesGui(IRecipeManager recipeManager, RecipeTransferManager recipeTransferManager, IngredientManager ingredientManager) {
		super(new StringTextComponent("Recipes"));
		this.recipeTransferManager = recipeTransferManager;
		this.logic = new RecipeGuiLogic(recipeManager, recipeTransferManager, this, ingredientManager);
		this.recipeCatalysts = new RecipeCatalysts();
		this.recipeGuiTabs = new RecipeGuiTabs(this.logic);
		this.field_230706_i_ = Minecraft.getInstance();

		Textures textures = Internal.getTextures();
		IDrawableStatic arrowNext = textures.getArrowNext();
		IDrawableStatic arrowPrevious = textures.getArrowPrevious();

		nextRecipeCategory = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowNext, b -> logic.nextRecipeCategory());
		previousRecipeCategory = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowPrevious, b -> logic.previousRecipeCategory());
		nextPage = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowNext, b -> logic.nextPage());
		previousPage = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowPrevious, b -> logic.previousPage());

		background = textures.getGuiBackground();
	}

	private static void drawCenteredStringWithShadow(MatrixStack matrixStack, FontRenderer font, String string, int guiWidth, int xOffset, int yPos, int color) {
		font.func_238405_a_(matrixStack, string, (guiWidth - font.getStringWidth(string)) / 2.0f + xOffset, yPos, color);
	}

	public int getGuiLeft() {
		return guiLeft;
	}

	public int getGuiTop() {
		return guiTop;
	}

	public int getXSize() {
		return xSize;
	}

	public int getYSize() {
		return ySize;
	}

	public int getRecipeCatalystExtraWidth() {
		if (recipeCatalysts.isEmpty()) {
			return 0;
		}
		return recipeCatalysts.getWidth();
	}

	@Override
	public boolean func_231177_au__() {
		return false;
	}

	@Override
	public void func_231158_b_(Minecraft minecraft, int width, int height) {
		super.func_231158_b_(minecraft, width, height);

		this.xSize = 198;
		this.ySize = this.field_230709_l_ - 68;
		int extraSpace = 0;
		final int maxHeight = ClientConfig.getInstance().getMaxRecipeGuiHeight();
		if (this.ySize > maxHeight) {
			extraSpace = this.ySize - maxHeight;
			this.ySize = maxHeight;
		}

		this.guiLeft = (width - this.xSize) / 2;
		this.guiTop = RecipeGuiTab.TAB_HEIGHT + 21 + (extraSpace / 2);

		final int rightButtonX = guiLeft + xSize - borderPadding - buttonWidth;
		final int leftButtonX = guiLeft + borderPadding;

		int titleHeight = field_230712_o_.FONT_HEIGHT + borderPadding;
		int recipeClassButtonTop = guiTop + titleHeight - buttonHeight + 2;
		nextRecipeCategory.field_230690_l_ = rightButtonX;
		nextRecipeCategory.field_230691_m_ = recipeClassButtonTop;
		previousRecipeCategory.field_230690_l_ = leftButtonX;
		previousRecipeCategory.field_230691_m_ = recipeClassButtonTop;

		int pageButtonTop = recipeClassButtonTop + buttonHeight + 2;
		nextPage.field_230690_l_ = rightButtonX;
		nextPage.field_230691_m_ = pageButtonTop;
		previousPage.field_230690_l_ = leftButtonX;
		previousPage.field_230691_m_ = pageButtonTop;

		this.headerHeight = (pageButtonTop + buttonHeight) - guiTop;

		addButtons();

		this.init = true;
		updateLayout();
	}

	private void addButtons() {
		this.func_230480_a_(nextRecipeCategory);
		this.func_230480_a_(nextRecipeCategory);
		this.func_230480_a_(previousRecipeCategory);
		this.func_230480_a_(nextPage);
		this.func_230480_a_(previousPage);
	}

	@Override
	public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (field_230706_i_ == null) {
			return;
		}
		func_230446_a_(matrixStack);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.background.draw(matrixStack, guiLeft, guiTop, xSize, ySize);

		RenderSystem.disableBlend();

		func_238467_a_(matrixStack,
			guiLeft + borderPadding + buttonWidth,
			nextRecipeCategory.field_230691_m_,
			guiLeft + xSize - borderPadding - buttonWidth,
			nextRecipeCategory.field_230691_m_ + buttonHeight,
			0x30000000);
		func_238467_a_(matrixStack,
			guiLeft + borderPadding + buttonWidth,
			nextPage.field_230691_m_,
			guiLeft + xSize - borderPadding - buttonWidth,
			nextPage.field_230691_m_ + buttonHeight,
			0x30000000);

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		int textPadding = (buttonHeight - field_230712_o_.FONT_HEIGHT) / 2;
		drawCenteredStringWithShadow(matrixStack, field_230712_o_, title, xSize, guiLeft, nextRecipeCategory.field_230691_m_ + textPadding, 0xFFFFFFFF);
		drawCenteredStringWithShadow(matrixStack, field_230712_o_, pageString, xSize, guiLeft, nextPage.field_230691_m_ + textPadding, 0xFFFFFFFF);

		nextRecipeCategory.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
		previousRecipeCategory.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
		nextPage.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
		previousPage.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);

		RecipeLayout<?> hoveredLayout = null;
		for (RecipeLayout<?> recipeLayout : recipeLayouts) {
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
				hoveredLayout = recipeLayout;
			}
			recipeLayout.drawRecipe(matrixStack, mouseX, mouseY);
		}

		GuiIngredient<?> hoveredRecipeCatalyst = recipeCatalysts.draw(matrixStack, mouseX, mouseY);

		recipeGuiTabs.draw(field_230706_i_, matrixStack, mouseX, mouseY);

		if (hoveredLayout != null) {
			hoveredLayout.drawOverlays(matrixStack, mouseX, mouseY);
		}
		if (hoveredRecipeCatalyst != null) {
			hoveredRecipeCatalyst.drawOverlays(matrixStack, 0, 0, mouseX, mouseY);
		}

		if (titleHoverChecker.checkHover(mouseX, mouseY) && !logic.hasAllCategories()) {
			TranslationTextComponent showAllRecipesString = new TranslationTextComponent("jei.tooltip.show.all.recipes");
			TooltipRenderer.drawHoveringText(showAllRecipesString, mouseX, mouseY, matrixStack);
		}
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		if (field_230706_i_ != null && field_230706_i_.currentScreen == this) {
			if ((mouseX >= guiLeft) && (mouseY >= guiTop) && (mouseX < guiLeft + xSize) && (mouseY < guiTop + ySize)) {
				return true;
			}
			for (RecipeLayout<?> recipeLayout : this.recipeLayouts) {
				if (recipeLayout.isMouseOver(mouseX, mouseY)) {
					return true;
				}
			}
		}
		return false;
	}

	@Nullable
	@Override
	public IClickedIngredient<?> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (isOpen()) {
			{
				IClickedIngredient<?> clicked = recipeCatalysts.getIngredientUnderMouse(mouseX, mouseY);
				if (clicked != null) {
					return clicked;
				}
			}

			if (isMouseOver(mouseX, mouseY)) {
				for (RecipeLayout<?> recipeLayouts : this.recipeLayouts) {
					GuiIngredient<?> clicked = recipeLayouts.getGuiIngredientUnderMouse(mouseX, mouseY);
					if (clicked != null) {
						Object displayedIngredient = clicked.getDisplayedIngredient();
						if (displayedIngredient != null) {
							return ClickedIngredient.create(displayedIngredient, clicked.getRect());
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return true;
	}

	@Override
	public boolean func_231043_a_(double scrollX, double scrollY, double scrollDelta) {
		final double x = MouseUtil.getX();
		final double y = MouseUtil.getY();
		if (isMouseOver(x, y)) {
			if (scrollDelta < 0) {
				logic.nextPage();
				return true;
			} else if (scrollDelta > 0) {
				logic.previousPage();
				return true;
			}
		}
		return super.func_231043_a_(scrollX, scrollY, scrollDelta);
	}

	@Override
	public boolean func_231044_a_(double mouseX, double mouseY, int mouseButton) {
		if (isMouseOver(mouseX, mouseY)) {
			if (titleHoverChecker.checkHover(mouseX, mouseY)) {
				if (logic.setCategoryFocus()) {
					return true;
				}
			} else {
				for (RecipeLayout<?> recipeLayout : recipeLayouts) {
					if (recipeLayout.handleClick(mouseX, mouseY, mouseButton)) {
						return true;
					}
				}
			}
		}

		if (recipeGuiTabs.isMouseOver(mouseX, mouseY)) {
			if (recipeGuiTabs.handleMouseClicked(mouseX, mouseY, mouseButton)) {
				return true;
			}
		}

		InputMappings.Input input = InputMappings.Type.MOUSE.getOrMakeInput(mouseButton);
		if (handleKeybindings(input)) {
			return true;
		}

		return super.func_231044_a_(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean func_231046_a_(int keyCode, int scanCode, int modifiers) {
		InputMappings.Input input = InputMappings.getInputByCode(keyCode, scanCode);
		return handleKeybindings(input);
	}

	private boolean handleKeybindings(InputMappings.Input input) {
		if (KeyBindings.isInventoryCloseKey(input) || KeyBindings.isInventoryToggleKey(input)) {
			func_231175_as__();
			return true;
		} else if (KeyBindings.recipeBack.isActiveAndMatches(input)) {
			back();
			return true;
		} else {
			JeiRuntime runtime = Internal.getRuntime();
			if (runtime != null) {
				IngredientListOverlay itemListOverlay = runtime.getIngredientListOverlay();
				if (!itemListOverlay.isMouseOver(MouseUtil.getX(), MouseUtil.getY())) {
					if (KeyBindings.nextPage.isActiveAndMatches(input)) {
						logic.nextPage();
						return true;
					} else if (KeyBindings.previousPage.isActiveAndMatches(input)) {
						logic.previousPage();
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isOpen() {
		return field_230706_i_ != null && field_230706_i_.currentScreen == this;
	}

	private void open() {
		if (field_230706_i_ != null) {
			if (!isOpen()) {
				parentScreen = field_230706_i_.currentScreen;
			}
			field_230706_i_.displayGuiScreen(this);
		}
	}

	@Override
	public void func_231175_as__() {
		if (isOpen() && field_230706_i_ != null) {
			if (parentScreen != null) {
				field_230706_i_.displayGuiScreen(parentScreen);
				parentScreen = null;
			} else {
				ClientPlayerEntity player = field_230706_i_.player;
				if (player != null) {
					player.closeScreen();
				}
			}
			logic.clearHistory();
		}
	}

	@Override
	public <V> void show(IFocus<V> focus) {
		Focus<V> checkedFocus = Focus.check(focus);
		if (logic.setFocus(checkedFocus)) {
			open();
		}
	}

	@Override
	public void showCategories(List<ResourceLocation> recipeCategoryUids) {
		ErrorUtil.checkNotEmpty(recipeCategoryUids, "recipeCategoryUids");

		if (logic.setCategoryFocus(recipeCategoryUids)) {
			open();
		}
	}

	@Nullable
	@Override
	public Object getIngredientUnderMouse() {
		IClickedIngredient<?> ingredient = getIngredientUnderMouse(MouseUtil.getX(), MouseUtil.getY());
		if (ingredient != null) {
			return ingredient.getValue();
		}
		return null;
	}

	public void back() {
		logic.back();
	}

	private void updateLayout() {
		if (!init) {
			return;
		}
		IRecipeCategory<?> recipeCategory = logic.getSelectedRecipeCategory();
		IDrawable recipeBackground = recipeCategory.getBackground();

		int availableHeight = ySize - headerHeight;
		final int heightPerRecipe = recipeBackground.getHeight() + innerPadding;
		int recipesPerPage = availableHeight / heightPerRecipe;

		if (recipesPerPage == 0) {
			availableHeight = heightPerRecipe;
			recipesPerPage = 1;
		}

		final int recipeXOffset = guiLeft + (xSize - recipeBackground.getWidth()) / 2;
		final int recipeSpacing = (availableHeight - (recipesPerPage * recipeBackground.getHeight())) / (recipesPerPage + 1);

		logic.setRecipesPerPage(recipesPerPage);

		title = recipeCategory.getTitle();
		int titleWidth = field_230712_o_.getStringWidth(title);
		final int availableTitleWidth = (nextPage.field_230690_l_ - (previousPage.field_230690_l_ + previousPage.func_230998_h_())) - (2 * innerPadding);
		if (titleWidth > availableTitleWidth) {
			title = StringUtil.truncateStringToWidth(title, availableTitleWidth, field_230712_o_);
			titleWidth = field_230712_o_.getStringWidth(title);
		}
		final int titleX = guiLeft + (xSize - titleWidth) / 2;
		final int titleY = guiTop + borderPadding;
		titleHoverChecker.updateBounds(titleY, titleY + field_230712_o_.FONT_HEIGHT, titleX, titleX + titleWidth);

		int spacingY = recipeBackground.getHeight() + recipeSpacing;

		recipeLayouts.clear();
		recipeLayouts.addAll(logic.getRecipeLayouts(recipeXOffset, guiTop + headerHeight + recipeSpacing, spacingY));
		addRecipeTransferButtons(recipeLayouts);

		nextPage.field_230693_o_ = previousPage.field_230693_o_ = logic.hasMultiplePages();
		nextRecipeCategory.field_230693_o_ = previousRecipeCategory.field_230693_o_ = logic.hasMultipleCategories();

		pageString = logic.getPageString();

		List<Object> recipeCatalysts = logic.getRecipeCatalysts();
		this.recipeCatalysts.updateLayout(recipeCatalysts, this);
		recipeGuiTabs.initLayout(this);
	}

	private void addRecipeTransferButtons(List<RecipeLayout<?>> recipeLayouts) {
		field_230705_e_.removeAll(field_230710_m_);
		field_230710_m_.clear();
		addButtons();

		if (field_230706_i_ == null) {
			return;
		}
		PlayerEntity player = field_230706_i_.player;
		if (player == null) {
			return;
		}
		Container container = getParentContainer();

		for (RecipeLayout<?> recipeLayout : recipeLayouts) {
			RecipeTransferButton button = recipeLayout.getRecipeTransferButton();
			if (button != null) {
				button.init(recipeTransferManager, container, player);
				button.setOnClickHandler((mouseX, mouseY) -> {
					boolean maxTransfer = Screen.func_231173_s_();
					if (container != null && RecipeTransferUtil.transferRecipe(recipeTransferManager, container, recipeLayout, player, maxTransfer)) {
						func_231175_as__();
					}
				});
				func_230480_a_(button);
			}
		}
	}

	@Nullable
	public Screen getParentScreen() {
		return parentScreen;
	}

	@Nullable
	private Container getParentContainer() {
		if (parentScreen instanceof ContainerScreen) {
			return ((ContainerScreen<?>) parentScreen).getContainer();
		}
		return null;
	}

	@Override
	public void onStateChange() {
		updateLayout();
	}
}
