package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;

public class PreviewTooltipComponent<R> implements ClientTooltipComponent, TooltipComponent {
	private static final int UPDATE_INTERVAL_MS = 2000;

	private final IRecipeLayoutDrawable<R> drawable;
	private final RecipeTransferService recipeTransferService;
	private @Nullable IRecipeTransferError transferError;
	private long lastUpdateTime = 0;
	private boolean interactive;
	private double mouseX = -1;
	private double mouseY = -1;
	private ImmutableRect2i tooltipArea = ImmutableRect2i.EMPTY;

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

	public void setInteractive(double mouseX, double mouseY) {
		this.interactive = true;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public void setStatic() {
		this.interactive = false;
		this.tooltipArea = ImmutableRect2i.EMPTY;
	}

	public ImmutableRect2i getTooltipArea() {
		return this.tooltipArea;
	}

	@Override
	public int getHeight() {
		return drawable.getRect().getHeight() + 10;
	}

	@Override
	public int getWidth(Font font) {
		return drawable.getRect().getWidth() + 4;
	}

	@Override
	public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int z) {
		if (interactive) {
			int mouseX = (int) this.mouseX;
			int mouseY = (int) this.mouseY;
			this.tooltipArea = new ImmutableRect2i(x - 3, y - 4, getWidth(font) + 6, getHeight() + 8);
			drawable.setPosition(x + 2, y + 5);
			drawable.drawRecipe(poseStack, mouseX, mouseY);
			drawTransferError(poseStack, mouseX, mouseY);
			return;
		}
		poseStack.pushPose();
		{
			poseStack.translate(x + 2, y + 5, 0);
			drawable.setPosition(0, 0);
			drawable.drawRecipe(poseStack, 0, 0);
			drawTransferError(poseStack, x, y);
		}
		poseStack.popPose();
	}

	private void drawTransferError(PoseStack poseStack, int mouseX, int mouseY) {
		updateTransferError();
		if (transferError != null) {
			Rect2i recipeRect = drawable.getRect();
			transferError.showError(poseStack, mouseX, mouseY, drawable.getRecipeSlotsView(), recipeRect.getX(), recipeRect.getY());
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
			transferError = recipeTransferService.getTransferRecipeError(containerScreen, drawable, player)
				.orElse(null);
		} else {
			transferError = null;
		}
	}
}
