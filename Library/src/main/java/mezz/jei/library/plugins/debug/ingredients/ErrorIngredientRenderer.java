package mezz.jei.library.plugins.debug.ingredients;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.common.gui.JeiTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ErrorIngredientRenderer implements IIngredientRenderer<ErrorIngredient> {
	private static final List<RenderType> RENDER_TYPES = List.of(
		RenderType.gui(),
		RenderType.glint(),
		RenderType.debugFilledBox(),
		RenderType.guiOverlay(),
		RenderType.guiGhostRecipeOverlay(),
		RenderType.guiTextHighlight()
	);

	private final IIngredientHelper<ErrorIngredient> ingredientHelper;

	public ErrorIngredientRenderer(IIngredientHelper<ErrorIngredient> ingredientHelper) {
		this.ingredientHelper = ingredientHelper;
	}

	@Override
	public void render(GuiGraphics guiGraphics, ErrorIngredient ingredient) {
		Minecraft minecraft = Minecraft.getInstance();
		switch (ingredient.crashType()) {
			case RenderBreakVertexBufferCrash -> {
				MultiBufferSource.BufferSource bufferSource = guiGraphics.bufferSource();
				for (RenderType renderType : RENDER_TYPES) {
					VertexConsumer buffer = bufferSource.getBuffer(renderType);
					buffer.addVertex(0, 0, 0)
						.setColor(100);
				}
				throw new RuntimeException("intentional render crash for testing");
			}
			case TooltipCrash -> {
				Font font = getFontRenderer(minecraft, ingredient);
				guiGraphics.drawString(font, "JEI", 0, 0, 0xFFFF0000, false);
				guiGraphics.drawString(font, "TEST", 0, 8, 0xFFFF0000, false);
				RenderSystem.setShaderColor(1, 1, 1, 1);
			}
		}
	}

	@SuppressWarnings("removal")
	@Override
	public List<Component> getTooltip(ErrorIngredient ingredient, TooltipFlag tooltipFlag) {
		JeiTooltip tooltip = new JeiTooltip();
		getTooltip(tooltip, ingredient, tooltipFlag);
		return tooltip.toLegacyToComponents();
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, ErrorIngredient ingredient, TooltipFlag tooltipFlag) {
		if (ingredient.crashType() == ErrorIngredient.CrashType.TooltipCrash) {
			throw new RuntimeException("intentional tooltip crash for testing");
		}
		String displayName = ingredientHelper.getDisplayName(ingredient);
		tooltip.add(Component.literal(displayName));
		MutableComponent debugIngredient = Component.literal("debug ingredient");
		tooltip.add(debugIngredient.withStyle(ChatFormatting.GRAY));
	}
}
