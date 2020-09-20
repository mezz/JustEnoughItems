package mezz.jei.vote;

import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.List;

public class GoVoteIngredientRenderer implements IIngredientRenderer<GoVoteIngredient> {
	@Override
	public void render(MatrixStack matrixStack, int xPosition, int yPosition, @Nullable GoVoteIngredient ingredient) {
		if (ingredient == null) {
			return;
		}
		IDrawable icon = ingredient.getIcon();
		icon.draw(matrixStack, xPosition, yPosition);
	}

	@Override
	public List<ITextComponent> getTooltip(GoVoteIngredient ingredient, ITooltipFlag tooltipFlag) {
		return ingredient.getTooltip();
	}
}
