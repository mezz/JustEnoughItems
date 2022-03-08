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
	public void render(PoseStack poseStack, @Nullable ItemStack ingredient) {
		if (ingredient != null) {
			PoseStack modelViewStack = RenderSystem.getModelViewStack();
			modelViewStack.pushPose();
			{
				modelViewStack.mulPoseMatrix(poseStack.last().pose());

				RenderSystem.enableDepthTest();

				Minecraft minecraft = Minecraft.getInstance();
				Font font = getFontRenderer(minecraft, ingredient);
				ItemRenderer itemRenderer = minecraft.getItemRenderer();
				itemRenderer.renderAndDecorateFakeItem(ingredient, 0, 0);
				itemRenderer.renderGuiItemDecorations(font, ingredient, 0, 0);
				RenderSystem.disableBlend();
			}
			modelViewStack.popPose();
			// Restore model-view matrix now that the item has been rendered
			RenderSystem.applyModelViewMatrix();
		}
	}

	@SuppressWarnings("removal")
	@Override
	public void render(PoseStack stack, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
		stack.pushPose();
		{
			stack.translate(xPosition, yPosition, 0);
			render(stack, ingredient);
		}
		stack.popPose();
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

	@Override
	public int getWidth() {
		return 16;
	}

	@Override
	public int getHeight() {
		return 16;
	}
}
