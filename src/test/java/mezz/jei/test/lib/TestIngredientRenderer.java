package mezz.jei.test.lib;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;

import java.util.Arrays;
import java.util.List;

class TestIngredientRenderer implements IIngredientRenderer<TestIngredient> {
	@Override
	public List<Component> getTooltip(TestIngredient ingredient, TooltipFlag tooltipFlag) {
		return Arrays.asList(
			new TextComponent("Test Ingredient Tooltip " + ingredient),
			new TextComponent("Test ingredient tooltip " + ingredient + " line 2")
		);
	}
}
