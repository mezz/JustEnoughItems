package mezz.jei.gui.overlay.bookmarks.history;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.ingredients.GuiIngredientProperties;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.elements.IElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class HistoryOverlay implements IRecipeFocusSource {

    private static final int INGREDIENT_PADDING = 1;
    public static final int ROWS = 2;
    public static final int SLOT_WIDTH = GuiIngredientProperties.getWidth(INGREDIENT_PADDING);
    public static final int SLOT_HEIGHT = GuiIngredientProperties.getHeight(INGREDIENT_PADDING);

    // display elements
    private final IngredientGrid contents;

    // data
    private final HistoryList historyList;
    private final IClientConfig clientConfig;
    private int rows;

    public HistoryOverlay(
            IIngredientManager ingredientManager,
            HistoryList historyList,
            IInternalKeyMappings keyMappings,
            IIngredientGridConfig historyListConfig,
            IIngredientFilterConfig ingredientFilterConfig,
            IClientConfig clientConfig,
            IClientToggleState toggleState,
            IConnectionToServer serverConnection,
            IColorHelper colorHelper
    ) {
        this.clientConfig = clientConfig;
        this.historyList = historyList;
        this.contents = new IngredientGrid(
                ingredientManager,
                historyListConfig,
                ingredientFilterConfig,
                clientConfig,
                toggleState,
                serverConnection,
                keyMappings,
                colorHelper,
                false
        );
        historyList.addSourceListChangedListener(this::updateLayout);
    }

    public boolean isListDisplayed() {
        return clientConfig.isHistoryEnabled() &&
                contents.hasRoom();
    }

    public HistoryList getHistoryList() {
        return historyList;
    }

    public void updateBounds(final ImmutableRect2i availableArea, Set<ImmutableRect2i> guiExclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint) {
        this.contents.updateBounds(availableArea, guiExclusionAreas, mouseExclusionPoint);
        int rows = this.contents.getArea().getHeight() / SLOT_HEIGHT;
        this.rows = Math.min(rows, clientConfig.getMaxHistoryRows());
        int maxSize = this.contents.getArea().getWidth() / SLOT_WIDTH * this.rows;
        this.historyList.setMaxSize(maxSize);
    }

    public void updateLayout() {
        List<IElement<?>> ingredientList = historyList.getElements();
        this.contents.set(0, ingredientList);
    }

    private void drawLine(PoseStack poseStack, int x1, int x2, int y, int color) {
        float offset = (System.currentTimeMillis() % 600) / 100.0F;
        offset = 6 - offset;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        Matrix4f pose = poseStack.last().pose();

        for (float x = x1 - offset; x < x2; x += 7) {
            builder.addVertex(pose, Mth.clamp(x + 4, x1, x2), y, 0).setColor(r, g, b, a);
            builder.addVertex(pose, Mth.clamp(x, x1, x2), y, 0).setColor(r, g, b, a);
            builder.addVertex(pose, Mth.clamp(x, x1, x2), y + 1, 0).setColor(r, g, b, a);
            builder.addVertex(pose, Mth.clamp(x + 4, x1, x2), y + 1, 0).setColor(r, g, b, a);
        }

        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public void draw(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (isListDisplayed()) {
            this.contents.draw(minecraft, guiGraphics, mouseX, mouseY);
            ImmutableRect2i area = this.contents.getArea();
            int endX = area.getX() + area.getWidth();
            int startY = area.getY() + area.getHeight() - rows * SLOT_HEIGHT - 3;
            int colour = 0xFFFFFFFF;
            drawLine(guiGraphics.pose(), area.getX(), endX, startY, colour);
        }
    }

    public void drawTooltips(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isListDisplayed()) {
            this.contents.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
        }
    }

    public ImmutableRect2i getArea() {
        return this.contents.getArea();
    }

    @Override
    public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
        if (isListDisplayed()) {
            return contents.getIngredientUnderMouse(mouseX, mouseY);
        }
        return Stream.empty();
    }

    @Override
    public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
        if (isListDisplayed()) {
            return contents.getDraggableIngredientUnderMouse(mouseX, mouseY);
        }
        return Stream.empty();
    }
}
