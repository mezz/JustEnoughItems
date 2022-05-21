package mezz.jei.test.lib;

import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

class TestIngredientRenderer implements IIngredientRenderer<TestIngredient> {
    @Override
    public void render(MatrixStack matrixStack, int xPosition, int yPosition, @Nullable TestIngredient ingredient) {
        // test ingredient is never rendered
    }

    @Override
    public List<ITextComponent> getTooltip(TestIngredient ingredient, ITooltipFlag tooltipFlag) {
        return Arrays.asList(
                new StringTextComponent("Test Ingredient Tooltip " + ingredient),
                new StringTextComponent("Test ingredient tooltip " + ingredient + " line 2")
        );
    }
}
