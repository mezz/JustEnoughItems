package mezz.jei.library.render;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {
	@Override
	public void render(GuiGraphics guiGraphics, @Nullable ItemStack ingredient) {
		if (ingredient != null) {
			RenderSystem.enableDepthTest();

			Minecraft minecraft = Minecraft.getInstance();
			Font font = getFontRenderer(minecraft, ingredient);
			guiGraphics.renderFakeItem(ingredient, 0, 0);
			guiGraphics.renderItemDecorations(font, ingredient, 0, 0);
			RenderSystem.disableBlend();
		}
	}

	@SuppressWarnings("removal")
	@Override
	public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		return ingredient.getTooltipLines(player, tooltipFlag);
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, ItemStack ingredient, TooltipFlag tooltipFlag) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		List<Component> components = ingredient.getTooltipLines(player, tooltipFlag);
		tooltip.addAll(components);
	}

	@Override
	public Font getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		return renderHelper.getFontRenderer(minecraft, ingredient);
	}

	@Override
	public int getWidth() {
		return 16;
	}

	@Override
	public int getHeight() {
		return 16;
	}
}
