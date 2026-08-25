package mezz.jei.debug.ingredients;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DebugIngredientRenderer implements IIngredientRenderer<DebugIngredient> {
	private final IIngredientHelper<DebugIngredient> ingredientHelper;

	public DebugIngredientRenderer(IIngredientHelper<DebugIngredient> ingredientHelper) {
		this.ingredientHelper = ingredientHelper;
	}

	@Override
	public void render(GuiGraphics guiGraphics, DebugIngredient ingredient) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = getFontRenderer(minecraft, ingredient);
		guiGraphics.drawString(font, "JEI", 0, 0, 0xFFFF0000, false);
		guiGraphics.drawString(font, "#" + ingredient.number(), 0, 8, 0xFFFF0000, false);
	}

	@Override
	@Deprecated(since = "27.32.0", forRemoval = true)
	@SuppressWarnings("removal")
	public List<Component> getTooltip(DebugIngredient ingredient, TooltipFlag tooltipFlag) {
		return getTooltip(ingredient, Item.TooltipContext.EMPTY, null, tooltipFlag);
	}

	@Override
	@Deprecated(since = "27.32.0", forRemoval = true)
	@SuppressWarnings("removal")
	public void getTooltip(ITooltipBuilder tooltip, DebugIngredient ingredient, TooltipFlag tooltipFlag) {
		getTooltip(tooltip, ingredient, Item.TooltipContext.EMPTY, null, tooltipFlag);
	}

	@Override
	public List<Component> getTooltip(DebugIngredient ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
		List<Component> tooltip = new ArrayList<>();
		String displayName = ingredientHelper.getDisplayName(ingredient);
		tooltip.add(Component.literal(displayName));
		MutableComponent debugIngredient = Component.literal("debug ingredient");
		tooltip.add(debugIngredient.withStyle(ChatFormatting.GRAY));
		return tooltip;
	}
}
