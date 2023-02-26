package mezz.jei.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.gui.TooltipRenderer;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.ClickableIngredientInternal;
import mezz.jei.common.input.IClickableIngredientInternal;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.common.util.StringUtil;
import mezz.jei.gui.GuiProperties;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.gui.elements.GuiIconButtonSmall;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.InputType;
import mezz.jei.gui.input.MouseUtil;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipesGui extends Screen implements IRecipesGui, IRecipeFocusSource, IRecipeLogicStateListener {
	private static final int borderPadding = 6;
	private static final int innerPadding = 14;
	private static final int buttonWidth = 13;
	private static final int buttonHeight = 13;

	private final IRecipeTransferManager recipeTransferManager;
	private final IModIdHelper modIdHelper;
	private final IClientConfig clientConfig;
	private final IInternalKeyMappings keyBindings;
	private final Textures textures;
	private final IFocusFactory focusFactory;
	private final IIngredientManager ingredientManager;

	private int headerHeight;

	/* Internal logic for the gui, handles finding recipes */
	private final IRecipeGuiLogic logic;

	/* List of RecipeLayout to display */
	private final List<IRecipeLayoutDrawable<?>> recipeLayouts = new ArrayList<>();

	private String pageString = "1/1";
	private Component title = CommonComponents.EMPTY;
	private final DrawableNineSliceTexture background;

	private final RecipeCatalysts recipeCatalysts;
	private final RecipeGuiTabs recipeGuiTabs;

	private final List<RecipeTransferButton> recipeTransferButtons;

	private final GuiIconButtonSmall nextRecipeCategory;
	private final GuiIconButtonSmall previousRecipeCategory;
	private final GuiIconButtonSmall nextPage;
	private final GuiIconButtonSmall previousPage;

	@Nullable
	private Screen parentScreen;
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;
	private ImmutableRect2i titleArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i titleStringArea = ImmutableRect2i.EMPTY;

	private boolean init = false;

	public RecipesGui(
		IRecipeManager recipeManager,
		IRecipeTransferManager recipeTransferManager,
		IIngredientManager ingredientManager,
		IModIdHelper modIdHelper,
		IClientConfig clientConfig,
		Textures textures,
		IInternalKeyMappings keyBindings,
		IFocusFactory focusFactory
	) {
		super(Component.literal("Recipes"));
		this.textures = textures;
		this.recipeTransferButtons = new ArrayList<>();
		this.recipeTransferManager = recipeTransferManager;
		this.ingredientManager = ingredientManager;
		this.modIdHelper = modIdHelper;
		this.clientConfig = clientConfig;
		this.keyBindings = keyBindings;
		this.logic = new RecipeGuiLogic(recipeManager, recipeTransferManager, this, focusFactory);
		this.recipeCatalysts = new RecipeCatalysts(textures, recipeManager);
		this.recipeGuiTabs = new RecipeGuiTabs(this.logic, textures, ingredientManager);
		this.focusFactory = focusFactory;
		this.minecraft = Minecraft.getInstance();

		IDrawableStatic arrowNext = textures.getArrowNext();
		IDrawableStatic arrowPrevious = textures.getArrowPrevious();

		nextRecipeCategory = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowNext, b -> logic.nextRecipeCategory(), textures);
		previousRecipeCategory = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowPrevious, b -> logic.previousRecipeCategory(), textures);
		nextPage = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowNext, b -> logic.nextPage(), textures);
		previousPage = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowPrevious, b -> logic.previousPage(), textures);

		background = textures.getRecipeGuiBackground();
	}

	private static void drawCenteredStringWithShadow(PoseStack poseStack, Font font, String string, ImmutableRect2i area) {
		ImmutableRect2i textArea = MathUtil.centerTextArea(area, font, string);
		font.drawShadow(poseStack, string, textArea.getX(), textArea.getY(), 0xFFFFFFFF);
	}

	private static void drawCenteredStringWithShadow(PoseStack poseStack, Font font, Component text, ImmutableRect2i area) {
		ImmutableRect2i textArea = MathUtil.centerTextArea(area, font, text);
		font.drawShadow(poseStack, text, textArea.getX(), textArea.getY(), 0xFFFFFFFF);
	}

	public ImmutableRect2i getArea() {
		return this.area;
	}

	public int getRecipeCatalystExtraWidth() {
		if (recipeCatalysts.isEmpty()) {
			return 0;
		}
		return recipeCatalysts.getWidth();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void init() {
		super.init();

		final int xSize = 198;
		int ySize;
		if (this.clientConfig.isCenterSearchBarEnabled()) {
			ySize = this.height - 76;
		} else {
			ySize = this.height - 68;
		}
		int extraSpace = 0;
		final int maxHeight = this.clientConfig.getMaxRecipeGuiHeight();
		if (ySize > maxHeight) {
			extraSpace = ySize - maxHeight;
			ySize = maxHeight;
		}

		final int guiLeft = (this.width - xSize) / 2;
		final int guiTop = RecipeGuiTab.TAB_HEIGHT + 21 + (extraSpace / 2);

		this.area = new ImmutableRect2i(guiLeft, guiTop, xSize, ySize);

		final int rightButtonX = guiLeft + xSize - borderPadding - buttonWidth;
		final int leftButtonX = guiLeft + borderPadding;

		int titleHeight = font.lineHeight + borderPadding;
		int recipeClassButtonTop = guiTop + titleHeight - buttonHeight + 2;
		nextRecipeCategory.setX(rightButtonX);
		nextRecipeCategory.setY(recipeClassButtonTop);
		previousRecipeCategory.setX(leftButtonX);
		previousRecipeCategory.setY(recipeClassButtonTop);

		int pageButtonTop = recipeClassButtonTop + buttonHeight + 2;
		nextPage.setX(rightButtonX);
		nextPage.setY(pageButtonTop);
		previousPage.setX(leftButtonX);
		previousPage.setY(pageButtonTop);

		this.headerHeight = (pageButtonTop + buttonHeight) - guiTop;
		this.titleArea = MathUtil.union(previousRecipeCategory.getArea(), nextRecipeCategory.getArea())
			.cropLeft(previousRecipeCategory.getWidth() + innerPadding)
			.cropRight(nextRecipeCategory.getWidth() + innerPadding);

		this.addRenderableWidget(nextRecipeCategory);
		this.addRenderableWidget(previousRecipeCategory);
		this.addRenderableWidget(nextPage);
		this.addRenderableWidget(previousPage);

		this.init = true;
		updateLayout();
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (minecraft == null) {
			return;
		}
		renderBackground(poseStack);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		final int x = area.getX();
		final int y = area.getY();
		final int width = area.getWidth();
		final int height = area.getHeight();
		this.background.draw(poseStack, x, y, width, height);

		RenderSystem.disableBlend();

		fill(poseStack,
			x + borderPadding + buttonWidth,
			nextRecipeCategory.getY(),
			x + width - borderPadding - buttonWidth,
			nextRecipeCategory.getY() + buttonHeight,
			0x30000000);
		fill(poseStack,
			x + borderPadding + buttonWidth,
			nextPage.getY(),
			x + width - borderPadding - buttonWidth,
			nextPage.getY() + buttonHeight,
			0x30000000);

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		drawCenteredStringWithShadow(poseStack, font, title, titleArea);

		ImmutableRect2i pageArea = MathUtil.union(previousPage.getArea(), nextPage.getArea());
		drawCenteredStringWithShadow(poseStack, font, pageString, pageArea);

		nextRecipeCategory.render(poseStack, mouseX, mouseY, partialTicks);
		previousRecipeCategory.render(poseStack, mouseX, mouseY, partialTicks);
		nextPage.render(poseStack, mouseX, mouseY, partialTicks);
		previousPage.render(poseStack, mouseX, mouseY, partialTicks);

		Optional<IRecipeLayoutDrawable<?>> hoveredRecipeLayout = drawLayouts(poseStack, mouseX, mouseY);
		Optional<IRecipeSlotDrawable> hoveredRecipeCatalyst = recipeCatalysts.draw(poseStack, mouseX, mouseY);

		recipeGuiTabs.draw(minecraft, poseStack, mouseX, mouseY, modIdHelper);

		for (RecipeTransferButton button : recipeTransferButtons) {
			button.drawToolTip(poseStack, mouseX, mouseY);
		}
		RenderSystem.disableBlend();

		hoveredRecipeLayout.ifPresent(l -> l.drawOverlays(poseStack, mouseX, mouseY));
		hoveredRecipeCatalyst.ifPresent(h -> h.drawHoverOverlays(poseStack));

		hoveredRecipeCatalyst.ifPresent(h ->
			h.getDisplayedIngredient()
				.ifPresent(i -> {
					List<Component> tooltip = h.getTooltip();
					tooltip = modIdHelper.addModNameToIngredientTooltip(tooltip, i);
					TooltipRenderer.drawHoveringText(poseStack, tooltip, mouseX, mouseY, i, ingredientManager);
				})
		);
		RenderSystem.enableDepthTest();

		if (titleStringArea.contains(mouseX, mouseY) && !logic.hasAllCategories()) {
			MutableComponent showAllRecipesString = Component.translatable("jei.tooltip.show.all.recipes");
			TooltipRenderer.drawHoveringText(poseStack, List.of(showAllRecipesString), mouseX, mouseY);
		}
	}

	private Optional<IRecipeLayoutDrawable<?>> drawLayouts(PoseStack poseStack, int mouseX, int mouseY) {
		IRecipeLayoutDrawable<?> hoveredLayout = null;
		for (IRecipeLayoutDrawable<?> recipeLayout : recipeLayouts) {
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
				hoveredLayout = recipeLayout;
			}
			recipeLayout.drawRecipe(poseStack, mouseX, mouseY);
		}

		Minecraft minecraft = Minecraft.getInstance();
		float partialTicks = minecraft.getFrameTime();
		for (RecipeTransferButton button : recipeTransferButtons) {
			button.render(poseStack, mouseX, mouseY, partialTicks);
		}
		RenderSystem.disableBlend();
		return Optional.ofNullable(hoveredLayout);
	}

	@Override
	public void tick() {
		super.tick();

		Optional.ofNullable(minecraft)
			.map(minecraft -> minecraft.player)
			.ifPresent(localPlayer -> {
				AbstractContainerMenu container = getParentContainer().orElse(null);
				List<RecipeTransferButton> transferButtons = this.recipeTransferButtons;
				for (int i = 0; i < transferButtons.size(); i++) {
					IRecipeLayoutDrawable<?> recipeLayout = recipeLayouts.get(i);
					RecipeTransferButton button = transferButtons.get(i);
					Rect2i buttonArea = recipeLayout.getRecipeTransferButtonArea();
					button.update(buttonArea, recipeTransferManager, container, localPlayer);
				}
			});
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (minecraft != null && minecraft.screen == this) {
			if (this.area.contains(mouseX, mouseY)) {
				return true;
			}
			for (IRecipeLayoutDrawable<?> recipeLayout : this.recipeLayouts) {
				if (recipeLayout.isMouseOver(mouseX, mouseY)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (isOpen()) {
			return Stream.concat(
				recipeCatalysts.getIngredientUnderMouse(mouseX, mouseY),
				getRecipeLayoutsIngredientUnderMouse(mouseX, mouseY)
			);
		}
		return Stream.empty();
	}

	private Stream<IClickableIngredientInternal<?>> getRecipeLayoutsIngredientUnderMouse(double mouseX, double mouseY) {
		return this.recipeLayouts.stream()
			.map(recipeLayout -> getRecipeLayoutIngredientUnderMouse(recipeLayout, mouseX, mouseY))
			.flatMap(Optional::stream);
	}

	private static Optional<IClickableIngredientInternal<?>> getRecipeLayoutIngredientUnderMouse(IRecipeLayoutDrawable<?> recipeLayout, double mouseX, double mouseY) {
		return recipeLayout.getRecipeSlotUnderMouse(mouseX, mouseY)
			.flatMap(recipeSlot -> getClickedIngredient(recipeLayout, recipeSlot));
	}

	private static Optional<IClickableIngredientInternal<?>> getClickedIngredient(IRecipeLayoutDrawable<?> recipeLayout, IRecipeSlotDrawable recipeSlot) {
		return recipeSlot.getDisplayedIngredient()
			.map(displayedIngredient -> {
				ImmutableRect2i area = absoluteClickedArea(recipeLayout, recipeSlot.getRect());
				return new ClickableIngredientInternal<>(displayedIngredient, area, false, true);
			});
	}

	/**
	 * Converts from relative recipeLayout coordinates to absolute screen coordinates
	 */
	private static ImmutableRect2i absoluteClickedArea(IRecipeLayoutDrawable<?> recipeLayout, Rect2i area) {
		Rect2i layoutArea = recipeLayout.getRect();

		return new ImmutableRect2i(
			area.getX() + layoutArea.getX(),
			area.getY() + layoutArea.getY(),
			area.getWidth(),
			area.hashCode()
		);
	}

	@Override
	public boolean mouseScrolled(double scrollX, double scrollY, double scrollDelta) {
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
		return super.mouseScrolled(scrollX, scrollY, scrollDelta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		boolean handled = UserInput.fromVanilla(mouseX, mouseY, mouseButton, InputType.IMMEDIATE)
			.map(this::handleInput)
			.orElse(false);

		if (handled) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		UserInput input = UserInput.fromVanilla(keyCode, scanCode, modifiers, InputType.IMMEDIATE);
		return handleInput(input);
	}

	private boolean handleInput(UserInput input) {
		double mouseX = input.getMouseX();
		double mouseY = input.getMouseY();
		if (isMouseOver(mouseX, mouseY)) {
			if (titleStringArea.contains(mouseX, mouseY)) {
				if (input.is(keyBindings.getLeftClick()) && logic.setCategoryFocus()) {
					return true;
				}
			} else {
				for (IRecipeLayoutDrawable<?> recipeLayout : recipeLayouts) {
					if (handleRecipeLayoutInput(recipeLayout, input)) {
						return true;
					}
				}
			}
		}

		IUserInputHandler handler = recipeGuiTabs.getInputHandler();
		if (handler.handleUserInput(this, input, keyBindings).isPresent()) {
			return true;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (input.is(keyBindings.getCloseRecipeGui()) || input.is(minecraft.options.keyInventory)) {
			onClose();
			return true;
		} else if (input.is(keyBindings.getRecipeBack())) {
			back();
			return true;
		} else if (input.is(keyBindings.getNextCategory())) {
			logic.nextRecipeCategory();
			return true;
		} else if (input.is(keyBindings.getPreviousCategory())) {
			logic.previousRecipeCategory();
			return true;
		} else if (input.is(keyBindings.getNextRecipePage())) {
			logic.nextPage();
			return true;
		} else if (input.is(keyBindings.getPreviousRecipePage())) {
			logic.previousPage();
			return true;
		}
		return false;
	}

	private <R> boolean handleRecipeLayoutInput(IRecipeLayoutDrawable<R> recipeLayout, UserInput input) {
		if (!isMouseOver(input.getMouseX(), input.getMouseY())) {
			return false;
		}

		Rect2i recipeArea = recipeLayout.getRect();
		double recipeMouseX = input.getMouseX() - recipeArea.getX();
		double recipeMouseY = input.getMouseY() - recipeArea.getY();
		R recipe = recipeLayout.getRecipe();
		IRecipeCategory<R> recipeCategory = recipeLayout.getRecipeCategory();
		if (recipeCategory.handleInput(recipe, recipeMouseX, recipeMouseY, input.getKey())) {
			return true;
		}

		if (input.is(keyBindings.getCopyRecipeId())) {
			return handleCopyRecipeId(recipeLayout);
		}
		return false;
	}

	private <R> boolean handleCopyRecipeId(IRecipeLayoutDrawable<R> recipeLayout) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		IRecipeCategory<R> recipeCategory = recipeLayout.getRecipeCategory();
		R recipe = recipeLayout.getRecipe();
		ResourceLocation registryName = recipeCategory.getRegistryName(recipe);
		if (registryName == null) {
			MutableComponent message = Component.translatable("jei.message.copy.recipe.id.failure");
			if (player != null) {
				player.displayClientMessage(message, false);
			}
			return false;
		}
		String recipeId = registryName.toString();
		minecraft.keyboardHandler.setClipboard(recipeId);
		MutableComponent message = Component.translatable("jei.message.copy.recipe.id.success", recipeId);
		if (player != null) {
			player.displayClientMessage(message, false);
		}
		return true;
	}

	public boolean isOpen() {
		return minecraft != null && minecraft.screen == this;
	}

	private void open() {
		if (minecraft != null) {
			if (!isOpen()) {
				parentScreen = minecraft.screen;
			}
			minecraft.setScreen(this);
		}
	}

	@Override
	public void onClose() {
		if (isOpen() && minecraft != null) {
			minecraft.setScreen(parentScreen);
			parentScreen = null;
			logic.clearHistory();
			return;
		}
		super.onClose();
	}

	@Override
	public void show(List<IFocus<?>> focuses) {
		IFocusGroup checkedFocuses = focusFactory.createFocusGroup(focuses);
		if (logic.setFocus(checkedFocuses)) {
			open();
		}
	}

	@Override
	public void showTypes(List<RecipeType<?>> recipeTypes) {
		ErrorUtil.checkNotEmpty(recipeTypes, "recipeTypes");

		if (logic.setCategoryFocus(recipeTypes)) {
			open();
		}
	}

	@Override
	public <T> Optional<T> getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		double x = MouseUtil.getX();
		double y = MouseUtil.getY();

		return getIngredientUnderMouse(x, y)
			.map(IClickableIngredientInternal::getTypedIngredient)
			.flatMap(i -> i.getIngredient(ingredientType).stream())
			.findFirst();
	}

	public void back() {
		logic.back();
	}

	private void updateLayout() {
		if (!init) {
			return;
		}
		IRecipeCategory<?> recipeCategory = logic.getSelectedRecipeCategory();

		final int x = area.getX();
		final int y = area.getY();
		final int width = area.getWidth();
		final int height = area.getHeight();

		int availableHeight = height - headerHeight;
		final int heightPerRecipe = recipeCategory.getHeight() + innerPadding;
		int recipesPerPage = availableHeight / heightPerRecipe;

		if (recipesPerPage == 0) {
			availableHeight = heightPerRecipe;
			recipesPerPage = 1;
		}

		logic.setRecipesPerPage(recipesPerPage);

		title = StringUtil.stripStyling(recipeCategory.getTitle());
		final int availableTitleWidth = titleArea.getWidth();
		if (font.width(title) > availableTitleWidth) {
			title = StringUtil.truncateStringToWidth(title, availableTitleWidth, font);
		}
		this.titleStringArea = MathUtil.centerTextArea(this.titleArea, font, title);

		recipeLayouts.clear();

		final int recipeXOffset = x + (width - recipeCategory.getWidth()) / 2;
		final int recipeSpacing = (availableHeight - (recipesPerPage * recipeCategory.getHeight())) / (recipesPerPage + 1);
		int spacingY = recipeCategory.getHeight() + recipeSpacing;
		int recipeYOffset = y + headerHeight + recipeSpacing;
		for (IRecipeLayoutDrawable<?> recipeLayout : logic.getRecipeLayouts()) {
			recipeLayout.setPosition(recipeXOffset, recipeYOffset);
			recipeYOffset += spacingY;
			recipeLayouts.add(recipeLayout);
		}

		addRecipeTransferButtons(recipeLayouts);

		nextPage.active = previousPage.active = logic.hasMultiplePages();
		nextRecipeCategory.active = previousRecipeCategory.active = logic.hasMultipleCategories();

		pageString = logic.getPageString();

		List<ITypedIngredient<?>> recipeCatalystIngredients = logic.getRecipeCatalysts().toList();
		recipeCatalysts.updateLayout(recipeCatalystIngredients, this.area);
		recipeGuiTabs.initLayout(this.area);
	}

	private void addRecipeTransferButtons(List<IRecipeLayoutDrawable<?>> recipeLayouts) {
		if (minecraft == null) {
			return;
		}
		Player player = minecraft.player;
		if (player == null) {
			return;
		}

		for (GuiEventListener button : this.recipeTransferButtons) {
			removeWidget(button);
		}
		this.recipeTransferButtons.clear();

		AbstractContainerMenu container = getParentContainer().orElse(null);

		recipeLayouts.forEach(recipeLayout ->
			{
				Rect2i buttonArea = recipeLayout.getRecipeTransferButtonArea();
				IDrawable icon = textures.getRecipeTransfer();
				RecipeTransferButton button = new RecipeTransferButton(icon, recipeLayout, textures, this::onClose);
				button.update(buttonArea, recipeTransferManager, container, player);
				addRenderableWidget(button);
				this.recipeTransferButtons.add(button);
			}
		);
	}

	private Optional<AbstractContainerMenu> getParentContainer() {
		if (parentScreen instanceof AbstractContainerScreen<?> screen) {
			AbstractContainerMenu menu = screen.getMenu();
			return Optional.of(menu);
		}
		return Optional.empty();
	}

	@Override
	public void onStateChange() {
		updateLayout();
	}

	@Nullable
	public IGuiProperties getProperties() {
		if (width <= 0 || height <= 0) {
			return null;
		}
		int extraWidth = getRecipeCatalystExtraWidth();
		ImmutableRect2i recipeArea = getArea();
		int guiXSize = recipeArea.getWidth() + extraWidth;
		int guiYSize = recipeArea.getHeight();
		if (guiXSize <= 0 || guiYSize <= 0) {
			return null;
		}
		return new GuiProperties(
			getClass(),
			recipeArea.getX() - extraWidth,
			recipeArea.getY(),
			guiXSize,
			guiYSize,
			width,
			height
		);
	}
}
