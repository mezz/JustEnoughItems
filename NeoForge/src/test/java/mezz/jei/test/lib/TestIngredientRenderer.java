package mezz.jei.test.lib;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import net.minecraft.client.gui.GuiGraphics;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

class TestIngredientRenderer implements IIngredientRenderer<TestIngredient> {
	@Override
	public void render(GuiGraphics guiGraphics, TestIngredient ingredient) {

	}

	@SuppressWarnings("removal")
	@Override
	public List<Component> getTooltip(TestIngredient ingredient, TooltipFlag tooltipFlag) {
		return List.of(
			Component.literal("Test Ingredient Tooltip " + ingredient),
			Component.literal("Test ingredient tooltip " + ingredient + " line 2")
		);
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, TestIngredient ingredient, TooltipFlag tooltipFlag) {
		tooltip.add(Component.literal("Test Ingredient Tooltip " + ingredient));
		tooltip.add(Component.literal("Test Ingredient Tooltip " + ingredient + " line 2"));
	}
}
