package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.Internal;
import mezz.jei.common.transfer.RecipeTransferUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jspecify.annotations.Nullable;

public class PreviewTooltipComponent<R> implements ClientTooltipComponent, TooltipComponent {
	private static final int UPDATE_INTERVAL_MS = 2000;

	private final IRecipeLayoutDrawable<R> drawable;
	private @Nullable IRecipeTransferError transferError;
	private long lastUpdateTime = 0;
	private boolean interactive;
	private double mouseX = -1;
	private double mouseY = -1;

	public PreviewTooltipComponent(IRecipeLayoutDrawable<R> drawable) {
		this.drawable = drawable;
	}

	public IRecipeLayoutDrawable<R> getRecipeLayout() {
		return drawable;
	}

	public void setInteractive(double mouseX, double mouseY) {
		this.interactive = true;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public void setStatic() {
		this.interactive = false;
	}

	@Override
	public int getHeight(Font font) {
		return drawable.getRect().getHeight() + 10;
	}

	@Override
	public int getWidth(Font font) {
		return drawable.getRect().getWidth() + 4;
	}

	@Override
	public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor guiGraphics) {
		if (interactive) {
			int mouseX = (int) this.mouseX;
			int mouseY = (int) this.mouseY;
			drawable.setPosition(x + 2, y + 5);
			drawable.drawRecipe(guiGraphics, mouseX, mouseY);
			drawTransferError(guiGraphics, mouseX, mouseY);
			return;
		}
		var pose = guiGraphics.pose();
		pose.pushMatrix();
		{
			pose.translate(x + 2, y + 5);
			drawable.setPosition(0, 0);
			drawable.drawRecipe(guiGraphics, 0, 0);
			drawTransferError(guiGraphics, x, y);
		}
		pose.popMatrix();
	}

	private void drawTransferError(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		updateTransferError();
		if (transferError != null) {
			Rect2i recipeRect = drawable.getRect();
			transferError.showError(guiGraphics, mouseX, mouseY, drawable.getRecipeSlotsView(), recipeRect.getX(), recipeRect.getY());
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
		Screen screen = Minecraft.getInstance().screen;
		if (screen instanceof AbstractContainerScreen<?> containerScreen) {
			AbstractContainerMenu container = containerScreen.getMenu();
			IRecipeTransferManager recipeTransferManager = Internal.getJeiRuntime().getRecipeTransferManager();
			transferError = RecipeTransferUtil.getTransferRecipeError(recipeTransferManager, container, drawable, player)
				.orElse(null);
		} else {
			transferError = null;
		}
	}
}
