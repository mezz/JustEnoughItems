package mezz.jei.library.plugins.debug.ingredients;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.common.gui.JeiTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.TooltipFlag;

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
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

	@SuppressWarnings("removal")
	@Override
	public List<Component> getTooltip(DebugIngredient ingredient, TooltipFlag tooltipFlag) {
		JeiTooltip tooltip = new JeiTooltip();
		getTooltip(tooltip, ingredient, tooltipFlag);
		return tooltip.toLegacyToComponents();
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, DebugIngredient ingredient, TooltipFlag tooltipFlag) {
		String displayName = ingredientHelper.getDisplayName(ingredient);
		tooltip.add(Component.literal(displayName));
		MutableComponent debugIngredient = Component.literal("debug ingredient");
		tooltip.add(debugIngredient.withStyle(ChatFormatting.GRAY));
	}
}
