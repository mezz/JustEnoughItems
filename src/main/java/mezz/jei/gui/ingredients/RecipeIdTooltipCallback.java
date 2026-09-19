package mezz.jei.gui.ingredients;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.startup.ForgeModIdHelper;
import mezz.jei.util.Translator;

public class RecipeIdTooltipCallback<T> implements ITooltipCallback<T>
{
    @Nullable
    private final ResourceLocation recipeId;
    private final IIngredientHelper<T> ingredientHelper;

    public RecipeIdTooltipCallback(@Nullable ResourceLocation recipeId, IIngredientHelper<T> ingredientHelper) {
        this.recipeId = recipeId;
        this.ingredientHelper = ingredientHelper;
    }

    @Override
    public void onTooltip(int slotIndex, boolean input, T ingredient, List<String> tooltip) {
        if (recipeId != null && !input) {
            String recipeModId = recipeId.getNamespace();
            String ingredientModId = ingredientHelper.getDisplayModId(ingredient);

            if (!ingredientModId.isEmpty() && !recipeModId.equals(ingredientModId)) {
                String modName = ForgeModIdHelper.getInstance().getFormattedModNameForModId(recipeModId);
                if (modName != null) {
                    tooltip.add(TextFormatting.GRAY + Translator.translateToLocalFormatted("jei.tooltip.recipe.by", modName));
                }
            }

            boolean showAdvanced = Minecraft.getMinecraft().gameSettings.advancedItemTooltips || GuiScreen.isShiftKeyDown();
            if (showAdvanced) {
                tooltip.add(TextFormatting.DARK_GRAY + Translator.translateToLocalFormatted("jei.tooltip.recipe.id", recipeId.toString()));
            }
        }
    }
}
