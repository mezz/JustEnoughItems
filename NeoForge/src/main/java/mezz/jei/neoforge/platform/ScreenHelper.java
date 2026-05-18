package mezz.jei.neoforge.platform;

import com.mojang.blaze3d.platform.Window;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.Optional;

public class ScreenHelper implements IPlatformScreenHelper {
	@Override
	public Optional<Slot> getHoveredSlot(AbstractContainerScreen<?> containerScreen) {
		Slot slot = containerScreen.getHoveredSlot();
		return Optional.ofNullable(slot);
	}

	@Override
	public int getLeftPos(AbstractContainerScreen<?> containerScreen) {
		return containerScreen.getLeftPos();
	}

	@Override
	public int getTopPos(AbstractContainerScreen<?> containerScreen) {
		return containerScreen.getTopPos();
	}

	@Override
	public int getImageWidth(AbstractContainerScreen<?> containerScreen) {
		return containerScreen.getImageWidth();
	}

	@Override
	public int getImageHeight(AbstractContainerScreen<?> containerScreen) {
		return containerScreen.getImageHeight();
	}

	@Override
	public ImmutableRect2i getBookArea(RecipeBookComponent<?> guiRecipeBook) {
		if (guiRecipeBook.isVisible()) {
			return new ImmutableRect2i(
				guiRecipeBook.getXOrigin(),
				guiRecipeBook.getYOrigin(),
				RecipeBookComponent.IMAGE_WIDTH,
				RecipeBookComponent.IMAGE_HEIGHT
			);
		}
		return ImmutableRect2i.EMPTY;
	}

	@Override
	public ImmutableRect2i getToastsArea() {
		Minecraft minecraft = Minecraft.getInstance();
		ToastManager toastManager = minecraft.getToastManager();
		List<ToastManager.ToastInstance<?>> visible = toastManager.visibleToasts;
		if (visible.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}
		int height = 0;
		int width = 0;
		for (ToastManager.ToastInstance<?> instance : visible) {
			Toast toast = instance.getToast();
			height += toast.height();
			width = Math.max(toast.width(), width);
		}
		Window window = minecraft.getWindow();
		int screenWidth = window.getGuiScaledWidth();
		return new ImmutableRect2i(screenWidth - width, 0, width, height);
	}

	@Override
	public List<RecipeBookTabButton> getTabButtons(RecipeBookComponent<?> recipeBookComponent) {
		return recipeBookComponent.tabButtons;
	}

	@Override
	public <T extends RecipeBookMenu> RecipeBookComponent<?> getRecipeBookComponent(AbstractRecipeBookScreen<T> screen) {
		return screen.recipeBookComponent;
	}

	@Override
	public boolean canLoseFocus(EditBox editBox) {
		return editBox.canLoseFocus;
	}
}
