package mezz.jei.plugins.vanilla.ingredients.item;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.util.ErrorUtil;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.RenderProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void render(PoseStack poseStack, int xPosition, int yPosition, int width, int height, @Nullable ItemStack ingredient) {
		// Scaling ItemStack rendering is a pain because the rendering is weird,
		// so we just ignore width and height for this renderer.
		// For reference, see the hacks in the ItemZoom mod required to make scaling work properly.

		if (ingredient != null) {
			PoseStack modelViewStack = RenderSystem.getModelViewStack();
			modelViewStack.pushPose();
			{
				modelViewStack.mulPoseMatrix(poseStack.last().pose());

				RenderSystem.enableDepthTest();

				Minecraft minecraft = Minecraft.getInstance();
				Font font = getFontRenderer(minecraft, ingredient);
				ItemRenderer itemRenderer = minecraft.getItemRenderer();
				itemRenderer.renderAndDecorateFakeItem(ingredient, xPosition, yPosition);
				itemRenderer.renderGuiItemDecorations(font, ingredient, xPosition, yPosition, null);
				RenderSystem.disableBlend();
			}
			modelViewStack.popPose();
			// Restore model-view matrix now that the item has been rendered
			RenderSystem.applyModelViewMatrix();
		}
	}

	@Override
	public void render(PoseStack stack, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
		render(stack, xPosition, yPosition, 16, 16, ingredient);
	}

	@Override
	public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		try {
			return ingredient.getTooltipLines(player, tooltipFlag);
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
			LOGGER.error("Failed to get tooltip: {}", itemStackInfo, e);
			List<Component> list = new ArrayList<>();
			TranslatableComponent crash = new TranslatableComponent("jei.tooltip.error.crash");
			list.add(crash.withStyle(ChatFormatting.RED));
			return list;
		}
	}

	@Override
	public Font getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
		Font fontRenderer = RenderProperties.get(ingredient).getFont(ingredient);
		if (fontRenderer == null) {
			fontRenderer = minecraft.font;
		}
		return fontRenderer;
	}
}
