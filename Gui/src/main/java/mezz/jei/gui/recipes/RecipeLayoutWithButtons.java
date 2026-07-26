package mezz.jei.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.advanced.IRecipeButtonControllerFactory;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.elements.IconButton;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RecipeLayoutWithButtons<R> implements IRecipeLayoutWithButtons<R> {

	public static <T> IRecipeLayoutWithButtons<T> create(
		IRecipeLayoutDrawable<T> recipeLayoutDrawable,
		@Nullable RecipeBookmark<?, ?> recipeBookmark,
		BookmarkList bookmarks,
		RecipesGui recipesGui,
		List<IRecipeButtonControllerFactory> extraButtonControllerFactories
	) {
		RecipeTransferButtonController transferButton = new RecipeTransferButtonController(recipeLayoutDrawable, recipesGui);
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
	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
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

	private void drawRecipeTransferError(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		IRecipeTransferError recipeTransferError = transferButton.updateRecipeTransferError();
		if (recipeTransferError == null || !recipeTransferError.getType().allowsTransfer) {
			return;
		}

		Rect2i recipeRect = recipeLayout.getRect();
		IRecipeSlotsView recipeSlotsView = recipeLayout.getRecipeSlotsView();
		RecipeTransferButtonController.runWithRestoredPose(guiGraphics.pose(), () ->
			recipeTransferError.showError(guiGraphics, mouseX, mouseY, recipeSlotsView, recipeRect.getX(), recipeRect.getY())
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
	public IUserInputHandler createUserInputHandler() {
		List<IUserInputHandler> inputHandlers = new ArrayList<>();
		for (IconButton button : buttons) {
			inputHandlers.add(button.createInputHandler());
		}
		inputHandlers.add(new RecipeLayoutUserInputHandler<>(transferButton, recipeLayout));

		return new CombinedInputHandler("RecipeLayoutWithButtons", inputHandlers);
	}

	@Override
	public void tick() {
		recipeLayout.tick();
		for (IconButton button : buttons) {
			button.tick();
		}
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
	public void drawTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
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

	private void drawRecipeTransferTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		IInternalKeyMappings keyMappings = Internal.getKeyMappings();
		IJeiKeyMapping transferRecipeGui = keyMappings.getTransferRecipeGui();
		IJeiKeyMapping maxTransferRecipeGui = keyMappings.getMaxTransferRecipeGui();
		if (transferRecipeGui.isUnbound() && maxTransferRecipeGui.isUnbound()) {
			return;
		}

		IRecipeTransferError recipeTransferError = transferButton.updateRecipeTransferError();
		if (recipeTransferError != null && !recipeTransferError.getType().allowsTransfer) {
			return;
		}

		JeiTooltip tooltip = new JeiTooltip();
		if (!transferRecipeGui.isUnbound()) {
			tooltip.addKeyUsageComponent("jei.tooltip.recipesGui.tooltips.transfer.usage", transferRecipeGui);
		}
		if (!maxTransferRecipeGui.isUnbound()) {
			tooltip.addKeyUsageComponent("jei.tooltip.recipesGui.tooltips.transfer.max.usage", maxTransferRecipeGui);
		}
		tooltip.draw(guiGraphics, mouseX, mouseY);
	}

	@Override
	public int getMissingCountHint() {
		return transferButton.getMissingCountHint();
	}

	private record RecipeLayoutUserInputHandler<R>(RecipeTransferButtonController transferButton, IRecipeLayoutDrawable<R> recipeLayout) implements IUserInputHandler {

		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
			final double mouseX = input.getMouseX();
			final double mouseY = input.getMouseY();
			if (recipeLayout.isMouseOver(mouseX, mouseY) && recipeLayout.getSlotUnderMouse(mouseX, mouseY).isEmpty()) {
				InputConstants.Key key = input.getKey();
				boolean simulate = input.isSimulate();

				if (recipeLayout.getInputHandler().handleInput(mouseX, mouseY, input)) {
					return Optional.of(this);
				}

				IInternalKeyMappings keyMappings = Internal.getKeyMappings();
				if (keyMappings.getCopyRecipeId().isActiveAndMatches(key)) {
					if (handleCopyRecipeId(recipeLayout, simulate)) {
						return Optional.of(this);
					}
				}

				boolean transferOnce = input.is(keyMappings.getTransferRecipeGui());
				boolean transferMax = input.is(keyMappings.getMaxTransferRecipeGui());
				if (transferOnce || transferMax) {
					transferButton.transferRecipe(transferMax, simulate);
					return Optional.of(this);
				}
			}
			return Optional.empty();
		}

		private boolean handleCopyRecipeId(IRecipeLayoutDrawable<R> recipeLayout, boolean simulate) {
			if (simulate) {
				return true;
			}
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			IRecipeCategory<R> recipeCategory = recipeLayout.getRecipeCategory();
			R recipe = recipeLayout.getRecipe();
			Identifier registryId = recipeCategory.getIdentifier(recipe);
			if (registryId == null) {
				MutableComponent message = Component.translatable("jei.message.copy.recipe.id.failure");
				if (player != null) {
					player.sendSystemMessage(message);
				}
				return false;
			}

			String recipeId = registryId.toString();
			minecraft.keyboardHandler.setClipboard(recipeId);
			MutableComponent message = Component.translatable("jei.message.copy.recipe.id.success", Component.literal(recipeId));
			if (player != null) {
				player.sendSystemMessage(message);
			}
			return true;
		}

		@Override
		public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
			if (recipeLayout.isMouseOver(mouseX, mouseY) &&
				recipeLayout.getInputHandler().handleMouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY)
			) {
				return Optional.of(this);
			}

			return Optional.empty();
		}
	}
}
