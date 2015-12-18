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

import mezz.jei.gui.Focus;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.util.CycleTimer;
import mezz.jei.util.Log;

public class GuiIngredient<T> extends Gui implements IGuiIngredient<T> {
	private final boolean input;

	private final int xPosition;
	private final int yPosition;
	private final int width;
	private final int height;
	private final int padding;

	@Nonnull
	private final CycleTimer cycleTimer = new CycleTimer();
	@Nonnull
	private final List<T> contained = new ArrayList<>();
	@Nonnull
	private final IIngredientRenderer<T> ingredientRenderer;
	@Nonnull
	private final IIngredientHelper<T> ingredientHelper;

	private boolean enabled;
	private boolean visible;

	public GuiIngredient(@Nonnull IIngredientRenderer<T> ingredientRenderer, @Nonnull IIngredientHelper<T> ingredientHelper, boolean input, int xPosition, int yPosition, int width, int height, int padding) {
		this.ingredientRenderer = ingredientRenderer;
		this.ingredientHelper = ingredientHelper;

		this.input = input;

		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.width = width;
		this.height = height;
		this.padding = padding;
	}

	@Override
	public void clear() {
		visible = enabled = false;
		contained.clear();
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return enabled && visible && (mouseX >= xPosition) && (mouseY >= yPosition) && (mouseX < xPosition + width) && (mouseY < yPosition + height);
	}

	@Override
	@Nullable
	public T get() {
		return cycleTimer.getCycledItem(contained);
	}

	@Nonnull
	@Override
	public List<T> getAll() {
		return contained;
	}

	@Override
	public void set(@Nonnull T contained, @Nonnull Focus focus) {
		set(Collections.singleton(contained), focus);
	}

	@Override
	public void set(@Nonnull Collection<T> contained, @Nonnull Focus focus) {
		this.contained.clear();
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
		visible = enabled = !this.contained.isEmpty();
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft) {
		draw(minecraft, true);
	}

	@Override
	public void drawHovered(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		T value = get();
		if (value == null) {
			return;
		}
		draw(minecraft, false);
		drawTooltip(minecraft, mouseX, mouseY, value);
	}

	@Override
	public void drawHighlight(@Nonnull Minecraft minecraft, Color color, int xOffset, int yOffset) {
		int x = xPosition + xOffset;
		int y = yPosition + yOffset;
		GuiScreen.drawRect(x, y, x + width, y + height, color.getRGB());
	}

	private void draw(Minecraft minecraft, boolean cycleIcons) {
		if (!visible) {
			return;
		}

		cycleTimer.onDraw(cycleIcons);

		T value = get();
		if (value == null) {
			return;
		}

		ingredientRenderer.draw(minecraft, xPosition + padding, yPosition + padding, value);
	}

	private void drawTooltip(@Nonnull Minecraft minecraft, int mouseX, int mouseY, @Nonnull T value) {
		try {
			GlStateManager.disableDepth();

			RenderHelper.disableStandardItemLighting();
			GlStateManager.enableBlend();
			drawRect(xPosition, yPosition, xPosition + width, yPosition + width, 0x7FFFFFFF);

			List<String> tooltip = ingredientRenderer.getTooltip(minecraft, value);
			FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(minecraft, value);
			TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, fontRenderer);

			GlStateManager.enableDepth();
		} catch (RuntimeException e) {
			Log.error("Exception when rendering tooltip on {}.", value, e);
		}
	}

	public boolean isInput() {
		return input;
	}
}
