package mezz.jei.test.lib;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class TestIngredientRenderer implements IIngredientRenderer<TestIngredient> {
	@Override
	public void render(PoseStack stack, TestIngredient ingredient) {

	}

	@Override
	@Deprecated(since = "11.59.0", forRemoval = true)
	@SuppressWarnings("removal")
	public List<Component> getTooltip(TestIngredient ingredient, TooltipFlag tooltipFlag) {
		return getTooltip(ingredient, null, tooltipFlag);
	}

	@Override
	@Deprecated(since = "11.59.0", forRemoval = true)
	@SuppressWarnings("removal")
	public void getTooltip(ITooltipBuilder tooltip, TestIngredient ingredient, TooltipFlag tooltipFlag) {
		getTooltip(tooltip, ingredient, null, tooltipFlag);
	}

	@Override
	public List<Component> getTooltip(TestIngredient ingredient, @Nullable Player player, TooltipFlag tooltipFlag) {
		return List.of(
			Component.literal("Test Ingredient Tooltip " + ingredient),
			Component.literal("Test ingredient tooltip " + ingredient + " line 2")
		);
	}
}
