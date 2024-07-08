package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PreviewTooltipComponent implements ClientTooltipComponent, IBookmarkTooltip {

    private final IRecipeTransferManager recipeTransferManager;
    private final IRecipeLayoutDrawable<?> drawable;
    private IRecipeTransferHandler recipeTransferHandler;
    private Class<?> lastContainerClass;
    private IRecipeTransferError transferError;
    private long lastUpdateTime = 0;

    public <T> PreviewTooltipComponent(IRecipeLayoutDrawable<T> layout, IRecipeTransferManager recipeTransferManager) {
        this.drawable = layout;
        this.recipeTransferManager = recipeTransferManager;
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
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(x + 2, y + 5, 0);
        drawable.drawRecipe(guiGraphics, 0, 0);
        updateTransferError();
        if (transferError != null) {
            Rect2i recipeRect = drawable.getRect();
            transferError.drawHighlight(guiGraphics, x, y, drawable.getRecipeSlotsView(), recipeRect.getX(), recipeRect.getY());
        }
        pose.popPose();
    }

    private void updateTransferError() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >= 2000 || recipeTransferHandler == null) {
            lastUpdateTime = currentTime;
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null) return;
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                AbstractContainerMenu container = containerScreen.getMenu();
                if (lastContainerClass != container.getClass() || recipeTransferHandler == null) {
                    recipeTransferManager.getRecipeTransferHandler(container, drawable.getRecipeCategory()).ifPresent(handler -> {
                        lastContainerClass = container.getClass();
                        recipeTransferHandler = handler;
                    });
                }
                if (recipeTransferHandler == null) return;
                transferError = recipeTransferHandler.transferRecipe(container, drawable.getRecipe(), drawable.getRecipeSlotsView(), player, false, false);
            }
        }
    }

    @Override
    public boolean longTerm() {
        return true;
    }
}
