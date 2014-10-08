package mezz.jei.util;

import mezz.jei.gui.GuiContainerOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.Iterator;
import java.util.List;

/**
 * @author dmillerw
 */
public class Render {

    public static void renderToolTip(GuiContainerOverlay guiContainerOverlay, ItemStack itemStack, int mouseX, int mouseY) {
        if (itemStack == null)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        List list = itemStack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);

        for (int k = 0; k < list.size(); ++k) {
            if (k == 0) {
                list.set(k, itemStack.getRarity().rarityColor + (String) list.get(k));
            } else {
                list.set(k, EnumChatFormatting.GRAY + (String) list.get(k));
            }
        }

        FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
        drawHoveringText(guiContainerOverlay, list, mouseX, mouseY, (font == null ? mc.fontRenderer : font));
    }

    private static void drawHoveringText(GuiContainerOverlay guiContainerOverlay, List p_146283_1_, int p_146283_2_, int p_146283_3_, FontRenderer font) {
        if (!p_146283_1_.isEmpty()) {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int k = 0;
            Iterator iterator = p_146283_1_.iterator();

            while (iterator.hasNext()) {
                String s = (String) iterator.next();
                int l = font.getStringWidth(s);

                if (l > k) {
                    k = l;
                }
            }

            int j2 = p_146283_2_ + 12;
            int k2 = p_146283_3_ - 12;
            int i1 = 8;

            if (p_146283_1_.size() > 1) {
                i1 += 2 + (p_146283_1_.size() - 1) * 10;
            }

            if (j2 + k > guiContainerOverlay.width) {
                j2 -= 28 + k;
            }

            if (k2 + i1 + 6 > guiContainerOverlay.height) {
                k2 = guiContainerOverlay.height - i1 - 6;
            }

            int j1 = -267386864;
            drawGradientRect(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j1, j1);
            drawGradientRect(j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j1, j1);
            drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j1, j1);
            drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j1, j1);
            drawGradientRect(j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j1, j1);
            int k1 = 1347420415;
            int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
            drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k1, l1);
            drawGradientRect(j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k1, l1);
            drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k1, k1);
            drawGradientRect(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l1, l1);

            for (int i2 = 0; i2 < p_146283_1_.size(); ++i2) {
                String s1 = (String) p_146283_1_.get(i2);
                font.drawStringWithShadow(s1, j2, k2, -1);

                if (i2 == 0) {
                    k2 += 2;
                }

                k2 += 10;
            }

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            RenderHelper.enableStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
    }

    private static void drawGradientRect(int p_73733_1_, int p_73733_2_, int p_73733_3_, int p_73733_4_, int p_73733_5_, int p_73733_6_) {
        float f = (float) (p_73733_5_ >> 24 & 255) / 255.0F;
        float f1 = (float) (p_73733_5_ >> 16 & 255) / 255.0F;
        float f2 = (float) (p_73733_5_ >> 8 & 255) / 255.0F;
        float f3 = (float) (p_73733_5_ & 255) / 255.0F;
        float f4 = (float) (p_73733_6_ >> 24 & 255) / 255.0F;
        float f5 = (float) (p_73733_6_ >> 16 & 255) / 255.0F;
        float f6 = (float) (p_73733_6_ >> 8 & 255) / 255.0F;
        float f7 = (float) (p_73733_6_ & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(f1, f2, f3, f);
        tessellator.addVertex((double) p_73733_3_, (double) p_73733_2_, (double) 300);
        tessellator.addVertex((double) p_73733_1_, (double) p_73733_2_, (double) 300);
        tessellator.setColorRGBA_F(f5, f6, f7, f4);
        tessellator.addVertex((double) p_73733_1_, (double) p_73733_4_, (double) 300);
        tessellator.addVertex((double) p_73733_3_, (double) p_73733_4_, (double) 300);
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
