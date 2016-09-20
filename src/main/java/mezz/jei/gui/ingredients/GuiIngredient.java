package mezz.jei.gui.ingredients;

import javax.annotation.Nullable;
import java.awt.Color;
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
import net.minecraft.item.ItemStack;

public class GuiIngredient<T> extends Gui implements IGuiIngredient<T> {
	private final int slotIndex;
	private final boolean input;

	private final int xPosition;
	private final int yPosition;
	private final int width;
	private final int height;
	private final int padding;

	private final CycleTimer cycleTimer;
	private final List<T> displayIngredients = new ArrayList<T>(); // ingredients, taking focus into account
	private final List<T> allIngredients = new ArrayList<T>(); // all ingredients, ignoring focus
	private final IIngredientRenderer<T> ingredientRenderer;
	private final IIngredientHelper<T> ingredientHelper;
	@Nullable
	private ITooltipCallback<T> tooltipCallback;

	private boolean enabled;

	public GuiIngredient(IIngredientRenderer<T> ingredientRenderer, IIngredientHelper<T> ingredientHelper, int slotIndex, boolean input, int xPosition, int yPosition, int width, int height, int padding, int itemCycleOffset) {
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
	@Nullable
	public Focus<T> getCurrentlyDisplayed() {
		T ingredient = getIngredient();
		if (ingredient == null) {
			return null;
		}
		return ingredientHelper.createFocus(ingredient);
	}

	@Override
	public List<T> getAllIngredients() {
		return allIngredients;
	}

	public void set(T ingredient, IFocus<T> focus) {
		set(Collections.singleton(ingredient), focus);
	}

	public void set(Collection<T> ingredients, IFocus<T> focus) {
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

	public void draw(Minecraft minecraft, int xOffset, int yOffset) {
		cycleTimer.onDraw();

		T value = getIngredient();
		ingredientRenderer.draw(minecraft, xOffset + xPosition + padding, yOffset + yPosition + padding, value);
	}

	public void drawHovered(Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY) {
		T value = getIngredient();
		if (value == null) {
			return;
		}
		draw(minecraft, xOffset, yOffset);
		drawTooltip(minecraft, xOffset, yOffset, mouseX, mouseY, value);
	}

	@Override
	public void drawHighlight(Minecraft minecraft, Color color, int xOffset, int yOffset) {
		int x = xPosition + xOffset + padding;
		int y = yPosition + yOffset + padding;
		GlStateManager.disableLighting();
		drawRect(x, y, x + width - padding * 2, y + height - padding * 2, color.getRGB());
		GlStateManager.color(1f, 1f, 1f, 1f);
	}

	private void drawTooltip(Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY, T value) {
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
			if (value instanceof ItemStack) {
				TooltipRenderer.drawHoveringText((ItemStack) value, minecraft, tooltip, xOffset + mouseX, yOffset + mouseY, fontRenderer);
			} else {
				TooltipRenderer.drawHoveringText(minecraft, tooltip, xOffset + mouseX, yOffset + mouseY, fontRenderer);
			}

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
