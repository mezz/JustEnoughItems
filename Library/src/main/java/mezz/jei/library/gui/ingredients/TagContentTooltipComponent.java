package mezz.jei.library.gui.ingredients;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.List;

public class TagContentTooltipComponent<T> implements ClientTooltipComponent {
    private static final int MAX_PER_LINE = 10;
    private static final int MAX_LINES = 3;
    private static final int MAX_INGREDIENTS = MAX_PER_LINE * MAX_LINES;
    private static final int INGREDIENT_SIZE = 18;
    private static final int INGREDIENT_PADDING = 1;

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
            final int drawCount = MAX_INGREDIENTS - 1;
            drawIngredients(guiGraphics, x, y, drawCount);
            if (ingredients.size() > MAX_INGREDIENTS) {
                final int remainingCount = Math.min(ingredients.size() - drawCount, 99);
                String countString = "+" + remainingCount;
                final int textHeight = font.lineHeight - 1;
                final int textWidth = font.width(countString);
                final int textCenterX = x + (MAX_PER_LINE - 1) * INGREDIENT_SIZE  + ((INGREDIENT_SIZE - textWidth) / 2);
                final int textCenterY = y + (MAX_LINES - 1) * INGREDIENT_SIZE + ((INGREDIENT_SIZE - textHeight) / 2);
                guiGraphics.drawString(font, countString, textCenterX, textCenterY, 0xAAAAAA);
            }
        }
    }

    private void drawIngredients(GuiGraphics guiGraphics, int x, int y, int maxIngredients) {
        final int maxPerLine = MathUtil.divideCeil(maxIngredients, getLineCount());

        for (int i = 0; i < ingredients.size() && i < maxIngredients; i++) {
            int column = i % maxPerLine;
            int row = i / maxPerLine;
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            {
                poseStack.translate(
                    x + column * INGREDIENT_SIZE + INGREDIENT_PADDING,
                    y + row * INGREDIENT_SIZE + INGREDIENT_PADDING,
                    0.0D
                );
                renderer.render(guiGraphics, ingredients.get(i));
            }
            poseStack.popPose();
        }
    }

    private int getLineCount() {
        int lineCount = MathUtil.divideCeil(ingredients.size(), MAX_PER_LINE);
        return Math.min(lineCount, MAX_LINES);
    }

    private int getMaxPerLine() {
        int perLine = MathUtil.divideCeil(ingredients.size(), getLineCount());
        return Math.min(perLine, MAX_PER_LINE);
    }

    @Override
    public int getHeight() {
        return getLineCount() * INGREDIENT_SIZE + (2 * INGREDIENT_PADDING);
    }

    @Override
    public int getWidth(Font font) {
        return getMaxPerLine() * INGREDIENT_SIZE + (2 * INGREDIENT_PADDING);
    }
}

