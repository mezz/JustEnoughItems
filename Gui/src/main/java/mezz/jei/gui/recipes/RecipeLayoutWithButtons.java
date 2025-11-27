package mezz.jei.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class RecipeLayoutWithButtons<R> implements IRecipeLayoutWithButtons<R> {

	public static <T> IRecipeLayoutWithButtons<T> create(
		IRecipeLayoutDrawable<T> recipeLayoutDrawable,
		@Nullable RecipeBookmark<?, ?> recipeBookmark,
		BookmarkList bookmarks,
		RecipesGui recipesGui,
		@Nullable AbstractContainerMenu container
	) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		RecipeTransferButton transferButton = RecipeTransferButton.create(recipeLayoutDrawable, recipesGui::onClose, container, player);
		RecipeBookmarkButton bookmarkButton = RecipeBookmarkButton.create(recipeLayoutDrawable, bookmarks, recipeBookmark);
		return new RecipeLayoutWithButtons<>(recipeLayoutDrawable, transferButton, bookmarkButton);
	}

	private final IRecipeLayoutDrawable<R> recipeLayout;
	private final RecipeTransferButton transferButton;
	private final RecipeBookmarkButton bookmarkButton;

	private RecipeLayoutWithButtons(
		IRecipeLayoutDrawable<R> recipeLayout,
		RecipeTransferButton transferButton,
		RecipeBookmarkButton bookmarkButton
	) {
		this.recipeLayout = recipeLayout;
		this.transferButton = transferButton;
		this.bookmarkButton = bookmarkButton;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		recipeLayout.drawRecipe(guiGraphics, mouseX, mouseY);
		transferButton.draw(guiGraphics, mouseX, mouseY, partialTicks);
		bookmarkButton.draw(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void updateBounds(int recipeXOffset, int recipeYOffset) {
		Rect2i rectWithBorder = recipeLayout.getRectWithBorder();
		Rect2i rect = recipeLayout.getRect();
		recipeLayout.setPosition(
			recipeXOffset - rectWithBorder.getX() + rect.getX(),
			recipeYOffset - rectWithBorder.getY() + rect.getY()
		);

		Rect2i layoutArea = recipeLayout.getRect();
		{
			Rect2i buttonArea = recipeLayout.getRecipeTransferButtonArea();
			buttonArea.setX(buttonArea.getX() + layoutArea.getX());
			buttonArea.setY(buttonArea.getY() + layoutArea.getY());
			transferButton.updateBounds(buttonArea);
		}
		{
			Rect2i buttonArea = recipeLayout.getRecipeBookmarkButtonArea();
			buttonArea.setX(buttonArea.getX() + layoutArea.getX());
			buttonArea.setY(buttonArea.getY() + layoutArea.getY());
			bookmarkButton.updateBounds(buttonArea);
		}
	}

	@Override
	public int totalWidth() {
		Rect2i area = recipeLayout.getRect();
		Rect2i areaWithBorder = recipeLayout.getRectWithBorder();
		int leftBorderWidth = area.getX() - areaWithBorder.getX();
		int rightAreaWidth = areaWithBorder.getWidth() - leftBorderWidth;

		if (transferButton.isVisible()) {
			Rect2i buttonArea = recipeLayout.getRecipeTransferButtonArea();
			int buttonRight = buttonArea.getX() + buttonArea.getWidth();
			rightAreaWidth = Math.max(buttonRight, rightAreaWidth);
		}

		if (bookmarkButton.isVisible()) {
			Rect2i buttonArea = recipeLayout.getRecipeBookmarkButtonArea();
			int buttonRight = buttonArea.getX() + buttonArea.getWidth();
			rightAreaWidth = Math.max(buttonRight, rightAreaWidth);
		}

		return leftBorderWidth + rightAreaWidth;
	}

	@Override
	public IUserInputHandler createUserInputHandler() {
		return new CombinedInputHandler(
			"RecipeLayoutWithButtons",
			bookmarkButton.createInputHandler(),
			transferButton.createInputHandler(),
			new RecipeLayoutUserInputHandler<>(recipeLayout)
		);
	}

	@Override
	public void tick(@Nullable AbstractContainerMenu parentContainer, @Nullable Player player) {
		recipeLayout.tick();
		transferButton.update(parentContainer, player);
		bookmarkButton.tick();
	}

	@Override
	public IRecipeLayoutDrawable<R> getRecipeLayout() {
		return recipeLayout;
	}

	@Override
	public void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		transferButton.drawTooltips(guiGraphics, mouseX, mouseY);
		bookmarkButton.drawTooltips(guiGraphics, mouseX, mouseY);
	}

	@Override
	public int getMissingCountHint() {
		return transferButton.getMissingCountHint();
	}

	private record RecipeLayoutUserInputHandler<R>(IRecipeLayoutDrawable<R> recipeLayout) implements IUserInputHandler {

		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
			final double mouseX = input.getMouseX();
			final double mouseY = input.getMouseY();
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
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
			MutableComponent message = Component.translatable("jei.message.copy.recipe.id.success", Component.literal(recipeId));
			if (player != null) {
				player.displayClientMessage(message, false);
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
