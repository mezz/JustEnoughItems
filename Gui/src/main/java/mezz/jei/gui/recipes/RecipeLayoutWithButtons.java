package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.advanced.IRecipeButtonControllerFactory;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.handlers.InputGroup;
import mezz.jei.common.input.interaction.ApiInputAdapter;
import mezz.jei.common.input.interaction.ConsumedInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.InputAction;
import mezz.jei.common.input.interaction.ReleaseInsideBounds;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.elements.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RecipeLayoutWithButtons<R> implements IRecipeLayoutWithButtons<R> {

	public static <T> IRecipeLayoutWithButtons<T> create(
		IRecipeLayoutDrawable<T> recipeLayoutDrawable,
		@Nullable RecipeBookmark<?, ?> recipeBookmark,
		BookmarkList bookmarks,
		RecipesGui recipesGui,
		RecipeTransferService recipeTransferService,
		List<IRecipeButtonControllerFactory> extraButtonControllerFactories
	) {
		RecipeTransferButtonController transferButton = new RecipeTransferButtonController(recipeLayoutDrawable, recipesGui, recipeTransferService);
		RecipeBookmarkButtonController bookmarkButton = new RecipeBookmarkButtonController(bookmarks, recipeBookmark);

		List<IconButton> buttons = new ArrayList<>();
		buttons.add(new IconButton(transferButton));
		buttons.add(new IconButton(bookmarkButton));
		for (IRecipeButtonControllerFactory buttonControllerFactory : extraButtonControllerFactories) {
			IIconButtonController buttonController = buttonControllerFactory.createButtonController(recipeLayoutDrawable);
			if (buttonController != null) {
				buttons.add(new IconButton(buttonController));
			}
		}

		return new RecipeLayoutWithButtons<>(recipeLayoutDrawable, transferButton, recipeBookmark, buttons);
	}

	private final IRecipeLayoutDrawable<R> recipeLayout;
	private final RecipeTransferButtonController transferButton;
	private final @Nullable RecipeBookmark<?, ?> recipeBookmark;
	private final List<IconButton> buttons;

	private RecipeLayoutWithButtons(
		IRecipeLayoutDrawable<R> recipeLayout,
		RecipeTransferButtonController transferButton,
		@Nullable RecipeBookmark<?, ?> recipeBookmark,
		List<IconButton> buttons
	) {
		this.recipeLayout = recipeLayout;
		this.transferButton = transferButton;
		this.recipeBookmark = recipeBookmark;
		this.buttons = buttons;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		recipeLayout.drawRecipe(guiGraphics, mouseX, mouseY);

		if (isMouseOverNonSlotLayout(mouseX, mouseY)) {
			drawRecipeTransferError(guiGraphics, mouseX, mouseY);
		}

		for (IconButton button : buttons) {
			if (button.isVisible()) {
				button.draw(guiGraphics, mouseX, mouseY, partialTicks);
			}
		}
	}

	private void drawRecipeTransferError(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		IRecipeTransferError recipeTransferError = transferButton.getRecipeTransferError();
		if (recipeTransferError == null || !recipeTransferError.getType().allowsTransfer) {
			return;
		}

		Rect2i recipeRect = recipeLayout.getRect();
		IRecipeSlotsView recipeSlotsView = recipeLayout.getRecipeSlotsView();
		RecipeTransferButtonController.runWithRestoredPose(
			guiGraphics.pose(), () -> recipeTransferError.showError(guiGraphics, mouseX, mouseY, recipeSlotsView, recipeRect.getX(), recipeRect.getY())
		);
	}

	private boolean isMouseOverNonSlotLayout(int mouseX, int mouseY) {
		return recipeLayout.isMouseOver(mouseX, mouseY) &&
			recipeLayout.getSlotUnderMouse(mouseX, mouseY).isEmpty();
	}

	private ImmutableRect2i getAbsoluteButtonArea(int buttonIndex) {
		Rect2i recipeLayoutRect = recipeLayout.getRect();
		Rect2i buttonArea = recipeLayout.getSideButtonArea(buttonIndex);
		return new ImmutableRect2i(
			buttonArea.getX() + recipeLayoutRect.getX(),
			buttonArea.getY() + recipeLayoutRect.getY(),
			buttonArea.getWidth(),
			buttonArea.getHeight()
		);
	}

	@Override
	public void updateBounds(int recipeXOffset, int recipeYOffset) {
		Rect2i rectWithBorder = recipeLayout.getRectWithBorder();
		Rect2i rect = recipeLayout.getRect();
		recipeLayout.setPosition(
			recipeXOffset - rectWithBorder.getX() + rect.getX(),
			recipeYOffset - rectWithBorder.getY() + rect.getY()
		);

		int i = 0;
		for (IconButton button : buttons) {
			if (button.isVisible()) {
				ImmutableRect2i buttonArea = getAbsoluteButtonArea(i);
				if (buttonArea.getWidth() * buttonArea.getHeight() > 0) {
					button.updateBounds(buttonArea);
				}
				i++;
			}
		}
	}

	@Override
	public int totalWidth() {
		Rect2i area = recipeLayout.getRect();
		Rect2i areaWithBorder = recipeLayout.getRectWithBorder();
		int leftBorderWidth = area.getX() - areaWithBorder.getX();
		int rightAreaWidth = areaWithBorder.getWidth() - leftBorderWidth;

		int i = 0;
		for (IconButton button : buttons) {
			if (button.isVisible()) {
				Rect2i buttonArea = recipeLayout.getSideButtonArea(i);
				int buttonRight = buttonArea.getX() + buttonArea.getWidth();
				rightAreaWidth = Math.max(buttonRight, rightAreaWidth);
				i++;
			}
		}

		return leftBorderWidth + rightAreaWidth;
	}

	@Override
	public IInputTarget createUserInputHandler() {
		List<IInputTarget> inputHandlers = new ArrayList<>();
		for (IconButton button : buttons) {
			inputHandlers.add(button.createInputHandler());
		}
		inputHandlers.add(new RecipeLayoutInputTarget<>(transferButton, recipeLayout));

		return new InputGroup("RecipeLayoutWithButtons", inputHandlers);
	}

	@Override
	public void tick() {
		recipeLayout.tick();
		for (IconButton button : buttons) {
			button.tick();
		}
	}

	@Override
	public Optional<IInputInteraction> beginLegacyCategoryInput(UserInput input) {
		if (!recipeLayout.isMouseOver(input.getMouseX(), input.getMouseY())) {
			return Optional.empty();
		}
		if (input.isMouseInput()) {
			return Optional.of(new InputAction(this::invokeLegacyCategoryInput).within(recipeLayout::isMouseOver));
		}
		if (invokeLegacyCategoryInput(input)) {
			return Optional.of(ConsumedInput.INSTANCE);
		}
		return Optional.empty();
	}

	@SuppressWarnings("removal")
	private boolean invokeLegacyCategoryInput(UserInput input) {
		Rect2i area = recipeLayout.getRect();
		return recipeLayout.getRecipeCategory().handleInput(recipeLayout.getRecipe(),
			input.getMouseX() - area.getX(), input.getMouseY() - area.getY(), input.getKey());
	}

	@Override
	public IRecipeLayoutDrawable<R> getRecipeLayout() {
		return recipeLayout;
	}

	@Override
	public @Nullable RecipeBookmark<?, ?> getRecipeBookmark() {
		return recipeBookmark;
	}

	@Override
	public void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		for (IconButton button : buttons) {
			if (button.isVisible() && button.isMouseOver(mouseX, mouseY)) {
				button.drawTooltips(guiGraphics, mouseX, mouseY);
				return;
			}
		}

		if (isMouseOverNonSlotLayout(mouseX, mouseY)) {
			drawRecipeTransferTooltip(guiGraphics, mouseX, mouseY);
		}
	}

	private void drawRecipeTransferTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		IInternalKeyMappings keyMappings = Internal.getKeyMappings();
		IJeiKeyMapping transferRecipeGui = keyMappings.getTransferRecipeGui();
		IJeiKeyMapping maxTransferRecipeGui = keyMappings.getMaxTransferRecipeGui();
		if (transferRecipeGui.isUnbound() && maxTransferRecipeGui.isUnbound()) {
			return;
		}

		IRecipeTransferError recipeTransferError = transferButton.getRecipeTransferError();
		if (recipeTransferError != null && !recipeTransferError.getType().allowsTransfer) {
			return;
		}

		JeiTooltip tooltip = new JeiTooltip();
		if (!transferRecipeGui.isUnbound()) {
			tooltip.addKeyUsageComponent("jei.tooltip.bookmarks.tooltips.transfer.usage", transferRecipeGui);
		}
		if (!maxTransferRecipeGui.isUnbound()) {
			tooltip.addKeyUsageComponent("jei.tooltip.bookmarks.tooltips.transfer.max.usage", maxTransferRecipeGui);
		}
		tooltip.draw(guiGraphics, mouseX, mouseY);
	}

	@Override
	public int getMissingCountHint() {
		return transferButton.getMissingCountHint();
	}

	/**
	 * Offers input to the recipe handler provided by a mod before trying JEI's recipe shortcuts.
	 *
	 * @param transferButton handles recipe transfer shortcuts outside ingredient slots
	 * @param recipeLayout the recipe whose handler receives input and whose area is checked on release
	 * @param <R> recipe type displayed by the layout
	 */
	private record RecipeLayoutInputTarget<R>(RecipeTransferButtonController transferButton, IRecipeLayoutDrawable<R> recipeLayout) implements IInputTarget {

		@Override
		public Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
			if (!recipeLayout.isMouseOver(input.getMouseX(), input.getMouseY())) {
				return Optional.empty();
			}
			Optional<IInputInteraction> extension = ApiInputAdapter.beginInput(screen, input, keyBindings, recipeLayout.getInputHandler());
			if (extension.isPresent()) {
				return extension.map(interaction -> new ReleaseInsideBounds(interaction, recipeLayout::isMouseOver));
			}
			if (input.is(keyBindings.getCopyRecipeId())) {
				return Optional.of(InputAction.run(this::copyRecipeId).within(recipeLayout::isMouseOver));
			}
			if (recipeLayout.getSlotUnderMouse(input.getMouseX(), input.getMouseY()).isEmpty()) {
				boolean transferOnce = input.is(keyBindings.getTransferRecipeGui());
				boolean transferMax = input.is(keyBindings.getMaxTransferRecipeGui());
				if ((transferOnce || transferMax) && transferButton.transferRecipe(transferMax, true)) {
					return Optional.of(InputAction.run(() -> transferButton.transferRecipe(transferMax, false))
						.within((x, y) -> recipeLayout.isMouseOver(x, y) && recipeLayout.getSlotUnderMouse(x, y).isEmpty()));
				}
			}
			return Optional.empty();
		}

		private void copyRecipeId() {
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			IRecipeCategory<R> recipeCategory = recipeLayout.getRecipeCategory();
			R recipe = recipeLayout.getRecipe();
			List<ResourceLocation> sourceRecipeIds = recipeCategory.getSourceRecipeIds(recipe);
			if (sourceRecipeIds.isEmpty()) {
				MutableComponent message = Component.translatable("jei.message.copy.recipe.id.failure");
				if (player != null) {
					player.displayClientMessage(message, false);
				}
				return;
			}
			String recipeId;
			if (sourceRecipeIds.size() == 1) {
				recipeId = sourceRecipeIds.getFirst().toString();
			} else {
				recipeId = sourceRecipeIds.toString();
			}
			minecraft.keyboardHandler.setClipboard(recipeId);
			MutableComponent message = Component.translatable("jei.message.copy.recipe.id.success", Component.literal(recipeId));
			if (player != null) {
				player.displayClientMessage(message, false);
			}
		}

		@Override
		public boolean scroll(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
			return recipeLayout.isMouseOver(mouseX, mouseY) &&
				recipeLayout.getInputHandler().handleMouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
		}
	}
}
