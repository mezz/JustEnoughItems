package mezz.jei.library.gui.ingredients;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.List;

public class TagContentTooltipComponent<T> implements ClientTooltipComponent {
    private static final int MAX_PER_LINE = 10;
    private static final int MAX_LINES = 3;
    private static final int MAX_INGREDIENTS = MAX_PER_LINE * MAX_LINES;
    private static final int MAX_HEIGHT = 16 * MAX_LINES;

    private final IIngredientRenderer<T> renderer;
    private final List<T> ingredients;

    public TagContentTooltipComponent(IIngredientRenderer<T> renderer, List<T> ingredients) {
        this.renderer = renderer;
        this.ingredients = ingredients;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        if (ingredients.size() <= MAX_INGREDIENTS) {
            drawIngredients(guiGraphics, x, y, ingredients.size());
        } else {
            drawIngredients(guiGraphics, x, y, MAX_INGREDIENTS - 1);
            if (ingredients.size() > MAX_INGREDIENTS) {
                PoseStack poseStack = guiGraphics.pose();
                poseStack.pushPose();
                poseStack.translate(x + 9 * 16, y + 2 * 18, 0.0D);
                guiGraphics.drawString(font, "+" + (ingredients.size() - MAX_INGREDIENTS + 1), 0, 0, 0xAAAAAA);
                poseStack.popPose();
            }
        }
    }

    private void drawIngredients(GuiGraphics guiGraphics, int x, int y, int maxIngredients) {
        for (int i = 0; i < ingredients.size() && i < maxIngredients; i++) {
            int xOffset = i % MAX_PER_LINE;
            int yOffset = i / MAX_PER_LINE;
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(x + xOffset * 16, y + yOffset * 16, 0.0D);
            renderer.render(guiGraphics, ingredients.get(i));
            poseStack.popPose();
        }
    }

    @Override
    public int getHeight() {
        return Math.min((ingredients.size() + MAX_PER_LINE - 1) / MAX_PER_LINE * 16, MAX_HEIGHT);
    }

    @Override
    public int getWidth(Font font) {
        return Math.min(ingredients.size(), MAX_PER_LINE) * 16;
    }
}

