package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.gui.Focus;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.util.CycleTimer;
import mezz.jei.util.Log;

public class GuiIngredient<T> extends Gui implements IGuiIngredient<T> {
	private final int slotIndex;
	private final boolean input;

	private final int xPosition;
	private final int yPosition;
	private final int width;
	private final int height;
	private final int padding;

	@Nonnull
	private final CycleTimer cycleTimer;
	@Nonnull
	private final List<T> contained = new ArrayList<>(); // contained, taking focus into account
	@Nonnull
	private final List<T> allContained = new ArrayList<>(); // contained, ignoring focus
	@Nonnull
	private final IIngredientRenderer<T> ingredientRenderer;
	@Nonnull
	private final IIngredientHelper<T> ingredientHelper;
	@Nullable
	private ITooltipCallback<T> tooltipCallback;

	private boolean enabled;

	public GuiIngredient(@Nonnull IIngredientRenderer<T> ingredientRenderer, @Nonnull IIngredientHelper<T> ingredientHelper, int slotIndex, boolean input, int xPosition, int yPosition, int width, int height, int padding, int itemCycleOffset) {
		this.ingredientRenderer = ingredientRenderer;
		this.ingredientHelper = ingredientHelper;

		this.slotIndex = slotIndex;
		this.input = input;

		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.width = width;
		this.height = height;
		this.padding = padding;

		this.cycleTimer = new CycleTimer(itemCycleOffset);
	}

	@Override
	public void clear() {
		enabled = false;
		contained.clear();
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return enabled && (mouseX >= xPosition) && (mouseY >= yPosition) && (mouseX < xPosition + width) && (mouseY < yPosition + height);
	}

	@Override
	@Nullable
	public T get() {
		return cycleTimer.getCycledItem(contained);
	}

	@Nonnull
	@Override
	public List<T> getAll() {
		return allContained;
	}

	@Override
	public void set(@Nonnull T contained, @Nonnull Focus focus) {
		set(Collections.singleton(contained), focus);
	}

	@Override
	public void set(@Nonnull Collection<T> contained, @Nonnull Focus focus) {
		this.contained.clear();
		this.allContained.clear();
		contained = ingredientHelper.expandSubtypes(contained);
		T match = null;
		if ((isInput() && focus.getMode() == Focus.Mode.INPUT) || (!isInput() && focus.getMode() == Focus.Mode.OUTPUT)) {
			match = ingredientHelper.getMatch(contained, focus);
		}
		if (match != null) {
			this.contained.add(match);
		} else {
			this.contained.addAll(contained);
		}
		this.allContained.addAll(contained);
		enabled = !this.contained.isEmpty();
	}

	public void setTooltipCallback(@Nullable ITooltipCallback<T> tooltipCallback) {
		this.tooltipCallback = tooltipCallback;
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft) {
		cycleTimer.onDraw();

		T value = get();
		ingredientRenderer.draw(minecraft, xPosition + padding, yPosition + padding, value);
	}

	@Override
	public void drawHovered(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		T value = get();
		if (value == null) {
			return;
		}
		draw(minecraft);
		drawTooltip(minecraft, mouseX, mouseY, value);
	}

	@Override
	public void drawHighlight(@Nonnull Minecraft minecraft, Color color, int xOffset, int yOffset) {
		int x = xPosition + xOffset + padding;
		int y = yPosition + yOffset + padding;
		GlStateManager.disableLighting();
		GuiScreen.drawRect(x, y, x + width - padding * 2, y + height - padding * 2, color.getRGB());
	}

	private void drawTooltip(@Nonnull Minecraft minecraft, int mouseX, int mouseY, @Nonnull T value) {
		try {
			GlStateManager.disableDepth();

			RenderHelper.disableStandardItemLighting();
			drawRect(xPosition + padding, yPosition + padding, xPosition + width - padding, yPosition + height - padding, 0x7FFFFFFF);

			List<String> tooltip = ingredientRenderer.getTooltip(minecraft, value);

			if (tooltipCallback != null) {
				tooltipCallback.onTooltip(slotIndex, input, value, tooltip);
			}

			FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(minecraft, value);
			TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, fontRenderer);

			GlStateManager.enableDepth();
		} catch (RuntimeException e) {
			Log.error("Exception when rendering tooltip on {}.", value, e);
		}
	}

	@Override
	public boolean isInput() {
		return input;
	}
}
