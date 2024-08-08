package mezz.jei.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import net.minecraft.client.Minecraft;
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

public record RecipeLayoutWithButtons<R>(
	IRecipeLayoutDrawable<R> recipeLayout,
	RecipeTransferButton transferButton,
	@Nullable RecipeBookmarkButton bookmarkButton
) implements IRecipeLayoutWithButtons<R> {
	@Override
	public void draw(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		recipeLayout.drawRecipe(poseStack, mouseX, mouseY);
		transferButton.draw(poseStack, mouseX, mouseY, partialTicks);
		if (bookmarkButton != null) {
			bookmarkButton.draw(poseStack, mouseX, mouseY, partialTicks);
		}
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
		Rect2i buttonArea = recipeLayout.getRecipeTransferButtonArea();
		buttonArea.setX(buttonArea.getX() + layoutArea.getX());
		buttonArea.setY(buttonArea.getY() + layoutArea.getY());
		transferButton.updateBounds(buttonArea);

		if (bookmarkButton != null) {
			Rect2i recipeBookmarkButtonArea = recipeLayout.getRecipeBookmarkButtonArea();
			recipeBookmarkButtonArea.setX(recipeBookmarkButtonArea.getX() + layoutArea.getX());
			recipeBookmarkButtonArea.setY(recipeBookmarkButtonArea.getY() + layoutArea.getY());
			bookmarkButton.updateBounds(recipeBookmarkButtonArea);
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
		if (bookmarkButton != null && bookmarkButton.isVisible()) {
			Rect2i buttonArea = recipeLayout.getRecipeBookmarkButtonArea();
			int buttonRight = buttonArea.getX() + buttonArea.getWidth();
			rightAreaWidth = Math.max(buttonRight, rightAreaWidth);
		}

		return leftBorderWidth + rightAreaWidth;
	}

	@Override
	public IUserInputHandler createUserInputHandler() {
		IUserInputHandler recipeLayoutInputHandler = new RecipeLayoutUserInputHandler<>(recipeLayout);
		if (bookmarkButton == null) {
			return new CombinedInputHandler(
				"RecipeLayoutWithButtons",
				transferButton.createInputHandler(),
				recipeLayoutInputHandler
			);
		}
		return new CombinedInputHandler(
			"RecipeLayoutWithButtons",
			bookmarkButton.createInputHandler(),
			transferButton.createInputHandler(),
			recipeLayoutInputHandler
		);
	}

	@Override
	public void tick(@Nullable AbstractContainerMenu parentContainer, @Nullable Player player) {
		recipeLayout.tick();
		updateTransferButton(parentContainer, player);
		if (bookmarkButton != null) {
			bookmarkButton.tick();
		}
	}

	@Override
	public void updateTransferButton(@Nullable AbstractContainerMenu parentContainer, @Nullable Player player) {
		transferButton.update(parentContainer, player);
	}

	@Override
	public IRecipeLayoutDrawable<R> getRecipeLayout() {
		return recipeLayout;
	}

	@Override
	public void drawTooltips(PoseStack poseStack, int mouseX, int mouseY) {
		transferButton.drawTooltips(poseStack, mouseX, mouseY);
		if (bookmarkButton != null) {
			bookmarkButton.drawTooltips(poseStack, mouseX, mouseY);
		}
	}

	@Override
	public boolean isBookmarked() {
		return bookmarkButton != null && bookmarkButton.isBookmarked();
	}

	@Override
	public int getMissingCountHint() {
		return transferButton.getMissingCountHint();
	}
	private record RecipeLayoutUserInputHandler<R>(IRecipeLayoutDrawable<R> recipeLayout) implements IUserInputHandler {

		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
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
		public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
			if (recipeLayout.isMouseOver(mouseX, mouseY) &&
				recipeLayout.getInputHandler().handleMouseScrolled(mouseX, mouseY, scrollDelta)
			) {
				return Optional.of(this);
			}

			return Optional.empty();
		}
	}
}
