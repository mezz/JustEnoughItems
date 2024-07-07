package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class PreviewTooltipComponent implements ClientTooltipComponent, IBookmarkTooltip {

    private final IRecipeLayoutDrawable<?> drawable;

    public <T, R> PreviewTooltipComponent(IRecipeLayoutDrawable<T> layout) {
        this.drawable = layout;
    }

    @Override
    public int getHeight() {
        return drawable == null ? 0 : drawable.getRect().getHeight() + 10;
    }

    @Override
    public int getWidth(Font font) {
        return drawable == null ? 0 : drawable.getRect().getWidth() + 4;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(x + 2, y + 5, 0);
        if (drawable != null) {
            drawable.drawRecipe(guiGraphics, 0, 0);
        }
        pose.popPose();
    }

    @Override
    public boolean longTerm() {
        return true;
    }
}
