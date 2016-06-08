package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.gui.Focus;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.util.CycleTimer;
import mezz.jei.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

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
	private final List<T> displayIngredients = new ArrayList<>(); // ingredients, taking focus into account
	@Nonnull
	private final List<T> allIngredients = new ArrayList<>(); // all ingredients, ignoring focus
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

	public boolean isMouseOver(int xOffset, int yOffset, int mouseX, int mouseY) {
		return enabled && (mouseX >= xOffset + xPosition) && (mouseY >= yOffset + yPosition) && (mouseX < xOffset + xPosition + width) && (mouseY < yOffset + yPosition + height);
	}

	@Nullable
	public T getIngredient() {
		return cycleTimer.getCycledItem(displayIngredients);
	}

	@Override
	public Focus<T> getCurrentlyDisplayed() {
		T ingredient = getIngredient();
		if (ingredient == null) {
			return null;
		}
		return ingredientHelper.createFocus(ingredient);
	}

	@Nonnull
	@Override
	public List<T> getAllIngredients() {
		return allIngredients;
	}

	public void set(@Nonnull T ingredient, @Nonnull IFocus<T> focus) {
		set(Collections.singleton(ingredient), focus);
	}

	public void set(@Nonnull Collection<T> ingredients, @Nonnull IFocus<T> focus) {
		this.displayIngredients.clear();
		this.allIngredients.clear();
		ingredients = ingredientHelper.expandSubtypes(ingredients);
		T match = null;
		if ((isInput() && focus.getMode() == IFocus.Mode.INPUT) || (!isInput() && focus.getMode() == IFocus.Mode.OUTPUT)) {
			match = ingredientHelper.getMatch(ingredients, focus);
		}
		if (match != null) {
			this.displayIngredients.add(match);
		} else {
			this.displayIngredients.addAll(ingredients);
		}
		this.ingredientRenderer.setIngredients(ingredients);
		this.allIngredients.addAll(ingredients);
		enabled = !this.displayIngredients.isEmpty();
	}

	public void setTooltipCallback(@Nullable ITooltipCallback<T> tooltipCallback) {
		this.tooltipCallback = tooltipCallback;
	}

	public void draw(@Nonnull Minecraft minecraft, int xOffset, int yOffset) {
		cycleTimer.onDraw();

		T value = getIngredient();
		ingredientRenderer.draw(minecraft, xOffset + xPosition + padding, yOffset + yPosition + padding, value);
	}

	public void drawHovered(@Nonnull Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY) {
		T value = getIngredient();
		if (value == null) {
			return;
		}
		draw(minecraft, xOffset, yOffset);
		drawTooltip(minecraft, xOffset, yOffset, mouseX, mouseY, value);
	}

	@Override
	public void drawHighlight(@Nonnull Minecraft minecraft, Color color, int xOffset, int yOffset) {
		int x = xPosition + xOffset + padding;
		int y = yPosition + yOffset + padding;
		GlStateManager.disableLighting();
		drawRect(x, y, x + width - padding * 2, y + height - padding * 2, color.getRGB());
		GlStateManager.color(1f, 1f, 1f, 1f);
	}

	private void drawTooltip(@Nonnull Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY, @Nonnull T value) {
		try {
			GlStateManager.disableDepth();

			RenderHelper.disableStandardItemLighting();
			drawRect(xOffset + xPosition + padding,
					yOffset + yPosition + padding,
					xOffset + xPosition + width - padding,
					yOffset + yPosition + height - padding,
					0x7FFFFFFF);
			GlStateManager.color(1f, 1f, 1f, 1f);

			List<String> tooltip = ingredientRenderer.getTooltip(minecraft, value);

			if (tooltipCallback != null) {
				tooltipCallback.onTooltip(slotIndex, input, value, tooltip);
			}

			FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(minecraft, value);
			TooltipRenderer.drawHoveringText(minecraft, tooltip, xOffset + mouseX, yOffset + mouseY, fontRenderer);

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
