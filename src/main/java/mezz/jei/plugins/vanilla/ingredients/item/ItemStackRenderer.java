package mezz.jei.plugins.vanilla.ingredients.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.util.ErrorUtil;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	@SuppressWarnings("deprecation")
	public void render(MatrixStack matrixStack, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
		if (ingredient != null) {
			RenderSystem.pushMatrix();
			RenderSystem.multMatrix(matrixStack.getLast().getMatrix());
			RenderSystem.enableDepthTest();
			RenderHelper.enableStandardItemLighting();
			Minecraft minecraft = Minecraft.getInstance();
			FontRenderer font = getFontRenderer(minecraft, ingredient);
			ItemRenderer itemRenderer = minecraft.getItemRenderer();
			itemRenderer.renderItemAndEffectIntoGUI(null, ingredient, xPosition, yPosition);
			itemRenderer.renderItemOverlayIntoGUI(font, ingredient, xPosition, yPosition, null);
			RenderSystem.disableBlend();
			RenderHelper.disableStandardItemLighting();
			RenderSystem.popMatrix();
		}
	}

	@Override
	public List<ITextComponent> getTooltip(ItemStack ingredient, ITooltipFlag tooltipFlag) {
		Minecraft minecraft = Minecraft.getInstance();
		PlayerEntity player = minecraft.player;
		try {
			return ingredient.getTooltip(player, tooltipFlag);
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
			LOGGER.error("Failed to get tooltip: {}", itemStackInfo, e);
			List<ITextComponent> list = new ArrayList<>();
			TranslationTextComponent crash = new TranslationTextComponent("jei.tooltip.error.crash");
			list.add(crash.mergeStyle(TextFormatting.RED));
			return list;
		}
	}

	@Override
	public FontRenderer getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
		FontRenderer fontRenderer = ingredient.getItem().getFontRenderer(ingredient);
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRenderer;
		}
		return fontRenderer;
	}
}
