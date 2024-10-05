package mezz.jei.library.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class FluidSlotRenderer<T> implements IIngredientRenderer<T> {
    private final IPlatformFluidHelperInternal<T> fluidHelper;
    private final long capacity;

    public FluidSlotRenderer(IPlatformFluidHelperInternal<T> fluidHelper, long capacity) {
        this.fluidHelper = fluidHelper;
        this.capacity = capacity;
    }

    @Override
    public void render(GuiGraphics guiGraphics, T ingredient) {
        render(guiGraphics, ingredient, 0, 0);
    }

    @Override
    public void render(GuiGraphics guiGraphics, T ingredient, int posX, int posY) {
        RenderSystem.enableBlend();

        drawFluid(guiGraphics, ingredient, posX, posY);

        RenderSystem.disableBlend();
    }

    private void drawFluid(GuiGraphics guiGraphics, T fluidStack, int posX, int posY) {
        IIngredientTypeWithSubtypes<Fluid, T> type = fluidHelper.getFluidIngredientType();
        Fluid fluid = type.getBase(fluidStack);
        if (fluid.isSame(Fluids.EMPTY)) {
            return;
        }
        fluidHelper.getStillFluidSprite(fluidStack)
            .ifPresent(fluidStillSprite -> {
                int fluidColor = fluidHelper.getColorTint(fluidStack);
                FluidTankRenderer.drawTiledSprite(guiGraphics, getWidth(), getHeight(), fluidColor, 16, fluidStillSprite, posX, posY);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                renderAmount(guiGraphics, fluidStack, posX, posY);
            });
    }

    private void renderAmount(GuiGraphics guiGraphics, T fluidStack, int posX, int posY) {
        long amount = fluidHelper.getAmount(fluidStack);
        String amountString;
        if (amount < capacity) {
            amountString = amount + fluidHelper.unit();
        } else {
            amountString = String.format("%.1fB", (double) amount / capacity);
        }

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        {
            poseStack.translate(posX + 0.5f, posY + 0.5f, 200f);
            Font font = getFontRenderer(Minecraft.getInstance(), fluidStack);

            guiGraphics.drawString(font, amountString, getWidth() - font.width(amountString), 9, 0xFFFFFF, true);
        }
        poseStack.popPose();
    }

    @SuppressWarnings("removal")
	@Override
    public List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag) {
        return List.of();
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, T ingredient, TooltipFlag tooltipFlag) {

    }
}
