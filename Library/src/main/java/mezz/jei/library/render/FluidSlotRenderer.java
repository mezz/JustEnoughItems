package mezz.jei.library.render;

import com.mojang.blaze3d.systems.RenderSystem;
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
    private static final int SLOT_SIZE = 16;
    private final IPlatformFluidHelperInternal<T> fluidHelper;
    private final long capacity;

    public FluidSlotRenderer(IPlatformFluidHelperInternal<T> fluidHelper, long capacity) {
        this.fluidHelper = fluidHelper;
        this.capacity = capacity;
    }

    public FluidSlotRenderer(IPlatformFluidHelperInternal<T> fluidHelper) {
        this.fluidHelper = fluidHelper;
        this.capacity = fluidHelper.bucketVolume();
    }

    @Override
    public void render(GuiGraphics guiGraphics, T ingredient) {
        RenderSystem.enableBlend();

        drawFluid(guiGraphics, ingredient);

        RenderSystem.setShaderColor(1, 1, 1, 1);

        RenderSystem.disableBlend();
    }

    private void drawFluid(GuiGraphics guiGraphics, T fluidStack) {
        IIngredientTypeWithSubtypes<Fluid, T> type = fluidHelper.getFluidIngredientType();
        Fluid fluid = type.getBase(fluidStack);
        if (fluid.isSame(Fluids.EMPTY)) {
            return;
        }
        fluidHelper.getStillFluidSprite(fluidStack)
                .ifPresent(fluidStillSprite -> {
                    int fluidColor = fluidHelper.getColorTint(fluidStack);
                    long amount = fluidHelper.getAmount(fluidStack);
                    String amountString;
                    if (amount < capacity) {
                        amountString = amount + fluidHelper.unit();
                    } else {
                        amountString = String.format("%.1fB", (double) amount / capacity);
                    }

                    FluidTankRenderer.drawTiledSprite(guiGraphics, SLOT_SIZE, SLOT_SIZE, fluidColor, 16, fluidStillSprite);
                    guiGraphics.pose().pushPose();
                    Font font = getFontRenderer(Minecraft.getInstance(), fluidStack);
                    guiGraphics.pose().translate(0.5f, 0.5f, 200f);
                    guiGraphics.drawString(font, amountString, 16 - font.width(amountString), 9, 0xFFFFFF, true);
                    guiGraphics.pose().popPose();
                });
    }

    @Override
    public List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag) {
        return List.of();
    }
}
