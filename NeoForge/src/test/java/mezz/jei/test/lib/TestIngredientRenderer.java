package mezz.jei.test.lib;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class TestIngredientRenderer implements IIngredientRenderer<TestIngredient> {
	@Override
	public void render(GuiGraphics guiGraphics, TestIngredient ingredient) {

	}

	@Override
	@Deprecated(since = "19.49.0", forRemoval = true)
	@SuppressWarnings("removal")
	public List<Component> getTooltip(TestIngredient ingredient, TooltipFlag tooltipFlag) {
		return getTooltip(ingredient, Item.TooltipContext.EMPTY, null, tooltipFlag);
	}

	@Override
	@Deprecated(since = "19.49.0", forRemoval = true)
	@SuppressWarnings("removal")
	public void getTooltip(ITooltipBuilder tooltip, TestIngredient ingredient, TooltipFlag tooltipFlag) {
		getTooltip(tooltip, ingredient, Item.TooltipContext.EMPTY, null, tooltipFlag);
	}

	@Override
	public List<Component> getTooltip(TestIngredient ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
		return List.of(
			Component.literal("Test Ingredient Tooltip " + ingredient),
			Component.literal("Test ingredient tooltip " + ingredient + " line 2")
		);
	}
}
