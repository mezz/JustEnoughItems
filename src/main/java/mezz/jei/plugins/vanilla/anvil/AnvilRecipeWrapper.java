package mezz.jei.plugins.vanilla.anvil;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.Arrays;

public class AnvilRecipeWrapper extends BlankRecipeWrapper {
    private final ItemStack leftInput;
    private final ItemStack rightInput;
    private final ItemStack output;
    private final int cost;

    public AnvilRecipeWrapper(ItemStack leftInput, ItemStack rightInput, ItemStack output) {
        this(leftInput, rightInput, output, -1);
    }

    public AnvilRecipeWrapper(ItemStack leftInput, ItemStack rightInput, ItemStack output, int experienceCost) {
        this.leftInput = leftInput;
        this.rightInput = rightInput;
        this.output = output;
        this.cost = experienceCost;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {

        if (cost >= 0) {
            // GuiRepair'text special shadow
            int mainColor = 0xFF80FF20;
            String text = I18n.format("container.repair.cost", cost);

            if (cost >= 40) {
                mainColor = 0xFFFF6060;
            }
            else if (cost > minecraft.player.experienceLevel && !minecraft.player.capabilities.isCreativeMode) {
                mainColor = 0xFFFF6060;
                //mainColor = 0xFFFFFF40;
            }

            int shadowColor = 0xFF000000 | (mainColor & 0xFCFCFC) >> 2;
            int width = minecraft.fontRendererObj.getStringWidth(text);
            int x = 145 - 2 - width;
            int y = 27;

            if (minecraft.fontRendererObj.getUnicodeFlag()) {
                Gui.drawRect(x-2, y-2, x+width+2, y+10,0xFF000000);
                Gui.drawRect(x-1, y-1, x+width+1, y+9,0xFF3B3B3B);
            }
            else {
                minecraft.fontRendererObj.drawString(text, x+1, y, shadowColor);
                minecraft.fontRendererObj.drawString(text, x, y+1, shadowColor);
                minecraft.fontRendererObj.drawString(text, x+1, y+1, shadowColor);
            }

            minecraft.fontRendererObj.drawString(text, x, y, mainColor);
        }
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(ItemStack.class, Arrays.asList(leftInput, rightInput));
        ingredients.setOutput(ItemStack.class, output);
    }
}
