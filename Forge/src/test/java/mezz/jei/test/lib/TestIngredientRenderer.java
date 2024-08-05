package mezz.jei.test.lib;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

class TestIngredientRenderer implements IIngredientRenderer<TestIngredient> {
	@Override
	public void render(PoseStack stack, TestIngredient ingredient) {

	}

	@SuppressWarnings("removal")
	@Override
	public List<Component> getTooltip(TestIngredient ingredient, TooltipFlag tooltipFlag) {
		return List.of(
			new TextComponent("Test Ingredient Tooltip " + ingredient),
			new TextComponent("Test ingredient tooltip " + ingredient + " line 2")
		);
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, TestIngredient ingredient, TooltipFlag tooltipFlag) {
		tooltip.add(new TextComponent("Test Ingredient Tooltip " + ingredient));
		tooltip.add(new TextComponent("Test Ingredient Tooltip " + ingredient + " line 2"));
	}
}
