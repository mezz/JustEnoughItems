package mezz.jei.gui.ingredients;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.gui.Focus;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.util.CycleTimer;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class GuiIngredient<T> extends Gui implements IGuiIngredient<T> {
	private static final String oreDictionaryIngredient = Translator.translateToLocal("jei.tooltip.recipe.ore.dict");

	private final int slotIndex;
	private final boolean input;

	private final int xPosition;
	private final int yPosition;
	private final int width;
	private final int height;
	private final int xPadding;
	private final int yPadding;

	private final CycleTimer cycleTimer;
	private final List<T> displayIngredients = new ArrayList<T>(); // ingredients, taking focus into account
	private final List<T> allIngredients = new ArrayList<T>(); // all ingredients, ignoring focus
	private final IIngredientRenderer<T> ingredientRenderer;
	private final IIngredientHelper<T> ingredientHelper;
	@Nullable
	private ITooltipCallback<T> tooltipCallback;

	private boolean enabled;

	public GuiIngredient(
			int slotIndex,
			boolean input,
			IIngredientRenderer<T> ingredientRenderer,
			IIngredientHelper<T> ingredientHelper,
			int xPosition, int yPosition,
			int width, int height,
			int xPadding, int yPadding,
			int itemCycleOffset
	) {
		this.ingredientRenderer = ingredientRenderer;
		this.ingredientHelper = ingredientHelper;

		this.slotIndex = slotIndex;
		this.input = input;

		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.width = width;
		this.height = height;
		this.xPadding = xPadding;
		this.yPadding = yPadding;

		this.cycleTimer = new CycleTimer(itemCycleOffset);
	}

	public boolean isMouseOver(int xOffset, int yOffset, int mouseX, int mouseY) {
		return enabled && (mouseX >= xOffset + xPosition) && (mouseY >= yOffset + yPosition) && (mouseX < xOffset + xPosition + width) && (mouseY < yOffset + yPosition + height);
	}

	@Override
	@Nullable
	public Focus<T> getCurrentlyDisplayed() {
		T ingredient = getDisplayedIngredient();
		if (ingredient == null) {
			return null;
		}
		return new Focus<T>(ingredient);
	}

	@Nullable
	@Override
	public T getDisplayedIngredient() {
		return cycleTimer.getCycledItem(displayIngredients);
	}

	@Override
	public List<T> getAllIngredients() {
		return allIngredients;
	}

	public void set(T ingredient, IFocus<T> focus) {
		set(Collections.singletonList(ingredient), focus);
	}

	public void set(List<T> ingredients, IFocus<T> focus) {
		this.displayIngredients.clear();
		this.allIngredients.clear();
		ingredients = this.ingredientHelper.expandSubtypes(ingredients);

		T match = getMatch(ingredients, focus);
		if (match != null) {
			this.displayIngredients.add(match);
		} else {
			this.displayIngredients.addAll(ingredients);
		}

		this.allIngredients.addAll(ingredients);
		enabled = !this.displayIngredients.isEmpty();
	}

	@Nullable
	private T getMatch(Collection<T> ingredients, IFocus<T> focus) {
		if ((isInput() && focus.getMode() == IFocus.Mode.INPUT) ||
				(!isInput() && focus.getMode() == IFocus.Mode.OUTPUT)) {
			T focusValue = focus.getValue();
			if (focusValue != null) {
				return ingredientHelper.getMatch(ingredients, focusValue);
			}
		}
		return null;
	}

	public void setTooltipCallback(@Nullable ITooltipCallback<T> tooltipCallback) {
		this.tooltipCallback = tooltipCallback;
	}

	public void draw(Minecraft minecraft, int xOffset, int yOffset) {
		cycleTimer.onDraw();

		T value = getDisplayedIngredient();
		ingredientRenderer.render(minecraft, xOffset + xPosition + xPadding, yOffset + yPosition + yPadding, value);
	}

	public void drawHovered(Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY) {
		draw(minecraft, xOffset, yOffset);

		T value = getDisplayedIngredient();
		if (value != null) {
			drawTooltip(minecraft, xOffset, yOffset, mouseX, mouseY, value);
		}
	}

	@Override
	public void drawHighlight(Minecraft minecraft, Color color, int xOffset, int yOffset) {
		int x = xPosition + xOffset + xPadding;
		int y = yPosition + yOffset + yPadding;
		GlStateManager.disableLighting();
		drawRect(x, y, x + width - xPadding * 2, y + height - yPadding * 2, color.getRGB());
		GlStateManager.color(1f, 1f, 1f, 1f);
	}

	private void drawTooltip(Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY, T value) {
		try {
			GlStateManager.disableDepth();

			RenderHelper.disableStandardItemLighting();
			drawRect(xOffset + xPosition + xPadding,
					yOffset + yPosition + yPadding,
					xOffset + xPosition + width - xPadding,
					yOffset + yPosition + height - yPadding,
					0x7FFFFFFF);
			GlStateManager.color(1f, 1f, 1f, 1f);

			List<String> tooltip = ingredientRenderer.getTooltip(minecraft, value);
			Internal.getHelpers().getModIdUtil().addModNameToIngredientTooltip(tooltip, value);

			if (tooltipCallback != null) {
				tooltipCallback.onTooltip(slotIndex, input, value, tooltip);
			}

			FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(minecraft, value);
			if (value instanceof ItemStack) {
				//noinspection unchecked
				Collection<ItemStack> itemStacks = (Collection<ItemStack>) this.allIngredients;
				String oreDictEquivalent = Internal.getStackHelper().getOreDictEquivalent(itemStacks);
				if (oreDictEquivalent != null) {
					final String acceptsAny = String.format(oreDictionaryIngredient, oreDictEquivalent);
					tooltip.add(TextFormatting.GRAY + acceptsAny);
				}
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
