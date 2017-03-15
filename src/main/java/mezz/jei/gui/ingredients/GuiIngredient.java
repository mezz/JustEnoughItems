package mezz.jei.gui.ingredients;

import javax.annotation.Nullable;
import java.awt.Color;
import java.awt.Rectangle;
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
import mezz.jei.gui.TooltipRenderer;
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

	private final Rectangle rect;
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
			Rectangle rect,
			int xPadding, int yPadding,
			int cycleOffset
	) {
		this.ingredientRenderer = ingredientRenderer;
		this.ingredientHelper = ingredientHelper;

		this.slotIndex = slotIndex;
		this.input = input;

		this.rect = rect;
		this.xPadding = xPadding;
		this.yPadding = yPadding;

		this.cycleTimer = new CycleTimer(cycleOffset);
	}

	public boolean isMouseOver(int xOffset, int yOffset, int mouseX, int mouseY) {
		return enabled && (mouseX >= xOffset + rect.x) && (mouseY >= yOffset + rect.y) && (mouseX < xOffset + rect.x + rect.width) && (mouseY < yOffset + rect.y + rect.height);
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

	public void set(@Nullable List<T> ingredients, @Nullable IFocus<T> focus) {
		this.displayIngredients.clear();
		this.allIngredients.clear();
		List<T> displayIngredients;
		if (ingredients == null) {
			displayIngredients = Collections.emptyList();
		} else {
			displayIngredients = this.ingredientHelper.expandSubtypes(ingredients);
		}

		T match = getMatch(displayIngredients, focus);
		if (match != null) {
			this.displayIngredients.add(match);
		} else {
			this.displayIngredients.addAll(displayIngredients);
		}

		if (ingredients != null) {
			this.allIngredients.addAll(ingredients);
		}
		enabled = !this.displayIngredients.isEmpty();
	}

	@Nullable
	private T getMatch(Collection<T> ingredients, @Nullable IFocus<T> focus) {
		if (focus != null && isMode(focus.getMode())) {
			T focusValue = focus.getValue();
			return ingredientHelper.getMatch(ingredients, focusValue);
		}
		return null;
	}

	public void setTooltipCallback(@Nullable ITooltipCallback<T> tooltipCallback) {
		this.tooltipCallback = tooltipCallback;
	}

	public void draw(Minecraft minecraft, int xOffset, int yOffset) {
		cycleTimer.onDraw();

		T value = getDisplayedIngredient();
		ingredientRenderer.render(minecraft, xOffset + rect.x + xPadding, yOffset + rect.y + yPadding, value);
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
		int x = rect.x + xOffset + xPadding;
		int y = rect.y + yOffset + yPadding;
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		drawRect(x, y, x + rect.width - xPadding * 2, y + rect.height - yPadding * 2, color.getRGB());
		GlStateManager.color(1f, 1f, 1f, 1f);
	}

	private void drawTooltip(Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY, T value) {
		try {
			GlStateManager.disableDepth();

			RenderHelper.disableStandardItemLighting();
			drawRect(xOffset + rect.x + xPadding,
					yOffset + rect.y + yPadding,
					xOffset + rect.x + rect.width - xPadding,
					yOffset + rect.y + rect.height - yPadding,
					0x7FFFFFFF);
			GlStateManager.color(1f, 1f, 1f, 1f);

			List<String> tooltip = ingredientRenderer.getTooltip(minecraft, value);
			tooltip = Internal.getModIdHelper().addModNameToIngredientTooltip(tooltip, value, ingredientHelper);

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

	public boolean isMode(IFocus.Mode mode) {
		return (input && mode == IFocus.Mode.INPUT) || (!input && mode == IFocus.Mode.OUTPUT);
	}
}
