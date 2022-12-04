package mezz.jei.test.lib;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

class TestIngredientRenderer implements IIngredientRenderer<TestIngredient> {
	@Override
	public void render(PoseStack stack, TestIngredient ingredient) {

	}

	@Override
	public List<Component> getTooltip(TestIngredient ingredient, TooltipFlag tooltipFlag) {
		return List.of(
			Component.literal("Test Ingredient Tooltip " + ingredient),
			Component.literal("Test ingredient tooltip " + ingredient + " line 2")
		);
	}
}
