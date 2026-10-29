package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.common.transfer.RecipeTransferService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jspecify.annotations.Nullable;

public class PreviewTooltipComponent<R> implements ClientTooltipComponent, TooltipComponent {
	private static final int UPDATE_INTERVAL_MS = 2000;

	private final IRecipeLayoutDrawable<R> drawable;
	private final RecipeTransferService recipeTransferService;
	private @Nullable IRecipeTransferError transferError;
	private long lastUpdateTime = 0;
	private boolean interactive;
	private int interactiveWidth;

	public PreviewTooltipComponent(
		IRecipeLayoutDrawable<R> drawable,
		RecipeTransferService recipeTransferService
	) {
		this.drawable = drawable;
		this.recipeTransferService = recipeTransferService;
	}

	public IRecipeLayoutDrawable<R> getRecipeLayout() {
		return drawable;
	}

	public void setInteractive(int interactiveWidth) {
		this.interactive = true;
		this.interactiveWidth = interactiveWidth;
	}

	public void setStatic() {
		this.interactive = false;
		this.interactiveWidth = 0;
	}

	@Override
	public int getHeight(Font font) {
		return drawable.getRect().getHeight() + 10;
	}

	@Override
	public int getWidth(Font font) {
		int width = drawable.getRect().getWidth() + 4;
		return Math.max(width, interactiveWidth);
	}

	@Override
	public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor guiGraphics) {
		drawable.setPosition(x + 2, y + 5);
		if (interactive) {
			drawable.drawRecipe(guiGraphics);
		} else {
			guiGraphics.extractWithoutMouse(() -> {
				drawable.drawRecipe(guiGraphics);
				drawTransferError(guiGraphics);
			});
		}
	}

	private void drawTransferError(GuiGraphicsExtractor guiGraphics) {
		updateTransferError();
		if (transferError != null) {
			Rect2i recipeRect = drawable.getRect();
			transferError.showError(guiGraphics, drawable.getRecipeSlotsView(), recipeRect.getX(), recipeRect.getY());
		}
	}

	public void tick() {
		drawable.tick();
	}

	private void updateTransferError() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastUpdateTime < UPDATE_INTERVAL_MS) {
			return;
		}
		lastUpdateTime = currentTime;

		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			transferError = null;
			return;
		}
		Screen screen = Minecraft.getInstance().gui.screen();
		if (screen instanceof AbstractContainerScreen<?> containerScreen) {
			transferError = recipeTransferService.getTransferRecipeError(containerScreen, drawable, player)
				.orElse(null);
		} else {
			transferError = null;
		}
	}
}
