package mezz.jei.gui.recipes;

import mezz.jei.Internal;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.Config;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.startup.ForgeModIdHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.HoverChecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeInfoIcon {
    private static final IDrawable ICON = Internal.getHelpers().getGuiHelper().getInfoIcon();
    private final HoverChecker hoverChecker;

    public RecipeInfoIcon() {
        int iconTop = 0;
        int iconBottom = getHeight();
        int iconLeft = CraftingRecipeCategory.width - (getWidth() * 2) - 1;
        int iconRight = iconLeft + getWidth();
        this.hoverChecker = new HoverChecker(iconTop, iconBottom, iconLeft, iconRight, 0);
    }

    public static int getWidth() {
        return ICON.getWidth() / 2;
    }

    public static int getHeight() {
        return ICON.getHeight() / 2;
    }

    public void draw(Minecraft minecraft) {
        int iconX = CraftingRecipeCategory.width - (getWidth() * 2) - 1;

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1.0);
        ICON.draw(minecraft, iconX * 2, 0);
        GlStateManager.popMatrix();
    }

    public List<String> getTooltipStrings(ResourceLocation recipeRegistryName, int mouseX, int mouseY) {
        if (hoverChecker.checkHover(mouseX, mouseY)) {
            List<String> tooltipStrings = new ArrayList<>();
            if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips || GuiScreen.isShiftKeyDown()) {
                tooltipStrings.add(TextFormatting.GRAY + recipeRegistryName.getResourcePath());
            }
            String modNameFormat = Config.getModNameFormat();
            String modName = ForgeModIdHelper.getInstance().getModNameForModId(recipeRegistryName.getResourceDomain());
            tooltipStrings.add(modNameFormat + modName);
            return tooltipStrings;
        }
        return Collections.emptyList();
    }
}
