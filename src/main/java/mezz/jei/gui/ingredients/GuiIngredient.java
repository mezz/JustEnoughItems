package mezz.jei.gui.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.gui.Focus;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.render.IngredientRenderHelper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Translator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiIngredient<T> extends AbstractGui implements IGuiIngredient<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final int slotIndex;
	private final boolean input;

	private final Rectangle2d rect;
	private final int xPadding;
	private final int yPadding;

	private final CycleTimer cycleTimer;
	private final List<T> displayIngredients = new ArrayList<>(); // ingredients, taking focus into account
	private final List<T> allIngredients = new ArrayList<>(); // all ingredients, ignoring focus
	private final IIngredientRenderer<T> ingredientRenderer;
	private final IIngredientHelper<T> ingredientHelper;
	@Nullable
	private ITooltipCallback<T> tooltipCallback;
	@Nullable
	private IDrawable background;

	private boolean enabled;

	public GuiIngredient(
		int slotIndex,
		boolean input,
		IIngredientRenderer<T> ingredientRenderer,
		IIngredientHelper<T> ingredientHelper,
		Rectangle2d rect,
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

	public Rectangle2d getRect() {
		return rect;
	}

	public boolean isMouseOver(double xOffset, double yOffset, double mouseX, double mouseY) {
		return enabled && (mouseX >= xOffset + rect.getX()) && (mouseY >= yOffset + rect.getY()) && (mouseX < xOffset + rect.getX() + rect.getWidth()) && (mouseY < yOffset + rect.getY() + rect.getHeight());
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

	public void set(@Nullable List<T> ingredients, @Nullable Focus<T> focus) {
		this.displayIngredients.clear();
		this.allIngredients.clear();
		List<T> displayIngredients;
		if (ingredients == null) {
			displayIngredients = Collections.emptyList();
		} else {
			displayIngredients = ingredients;
		}

		T match = getMatch(displayIngredients, focus);
		if (match != null) {
			this.displayIngredients.add(match);
		} else {
			displayIngredients = filterOutHidden(displayIngredients);
			this.displayIngredients.addAll(displayIngredients);
		}

		if (ingredients != null) {
			this.allIngredients.addAll(ingredients);
		}
		enabled = !this.displayIngredients.isEmpty();
	}

	private List<T> filterOutHidden(List<T> ingredients) {
		if (ingredients.isEmpty()) {
			return ingredients;
		}
		IngredientManager ingredientManager = Internal.getIngredientManager();
		IngredientFilter ingredientFilter = Internal.getIngredientFilter();
		List<T> visible = new ArrayList<>();
		for (T ingredient : ingredients) {
			if (ingredient == null || ingredientManager.isIngredientVisible(ingredient, ingredientFilter)) {
				visible.add(ingredient);
			}
			if (visible.size() > 100) {
				return visible;
			}
		}
		if (visible.size() > 0) {
			return visible;
		}
		return ingredients;
	}

	public void setBackground(IDrawable background) {
		this.background = background;
	}

	@Nullable
	private T getMatch(Collection<T> ingredients, @Nullable Focus<T> focus) {
		if (focus != null && isMode(focus.getMode())) {
			T focusValue = focus.getValue();
			return ingredientHelper.getMatch(ingredients, focusValue);
		}
		return null;
	}

	public void setTooltipCallback(@Nullable ITooltipCallback<T> tooltipCallback) {
		this.tooltipCallback = tooltipCallback;
	}

	public void draw(int xOffset, int yOffset) {
		cycleTimer.onDraw();

		if (background != null) {
			background.draw(xOffset + rect.getX(), yOffset + rect.getY());
		}

		T value = getDisplayedIngredient();
		try {
			ingredientRenderer.render(xOffset + rect.getX() + xPadding, yOffset + rect.getY() + yPadding, value);
		} catch (RuntimeException | LinkageError e) {
			if (value != null) {
				throw ErrorUtil.createRenderIngredientException(e, value);
			}
			throw e;
		}
	}

	@Override
	public void drawHighlight(int color, int xOffset, int yOffset) {
		int x = rect.getX() + xOffset + xPadding;
		int y = rect.getY() + yOffset + yPadding;
		RenderSystem.disableLighting();
		RenderSystem.disableDepthTest();
		fill(x, y, x + rect.getWidth() - xPadding * 2, y + rect.getHeight() - yPadding * 2, color);
		RenderSystem.color4f(1f, 1f, 1f, 1f);
	}

	public void drawOverlays(int xOffset, int yOffset, int mouseX, int mouseY) {
		T value = getDisplayedIngredient();
		if (value != null) {
			drawTooltip(xOffset, yOffset, mouseX, mouseY, value);
		}
	}

	private void drawTooltip(int xOffset, int yOffset, int mouseX, int mouseY, T value) {
		try {
			RenderSystem.disableDepthTest();

			RenderHelper.disableStandardItemLighting();
			fill(xOffset + rect.getX() + xPadding,
				yOffset + rect.getY() + yPadding,
				xOffset + rect.getX() + rect.getWidth() - xPadding,
				yOffset + rect.getY() + rect.getHeight() - yPadding,
				0x7FFFFFFF);
			RenderSystem.color4f(1f, 1f, 1f, 1f);

			IModIdHelper modIdHelper = Internal.getHelpers().getModIdHelper();
			List<String> tooltip = IngredientRenderHelper.getIngredientTooltipSafe(value, ingredientRenderer, ingredientHelper, modIdHelper);
			if (tooltipCallback != null) {
				tooltipCallback.onTooltip(slotIndex, input, value, tooltip);
			}

			Minecraft minecraft = Minecraft.getInstance();
			FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(minecraft, value);
			if (value instanceof ItemStack) {
				//noinspection unchecked
				Collection<ItemStack> itemStacks = (Collection<ItemStack>) this.allIngredients;
				ResourceLocation tagEquivalent = getTagEquivalent(itemStacks);
				if (tagEquivalent != null) {
					final String acceptsAny = Translator.translateToLocalFormatted("jei.tooltip.recipe.tag", tagEquivalent);
					tooltip.add(TextFormatting.GRAY + acceptsAny);
				}
			}
			TooltipRenderer.drawHoveringText(value, tooltip, xOffset + mouseX, yOffset + mouseY, fontRenderer);

			RenderSystem.enableDepthTest();
		} catch (RuntimeException e) {
			LOGGER.error("Exception when rendering tooltip on {}.", value, e);
		}
	}

	@Nullable
	private static ResourceLocation getTagEquivalent(Collection<ItemStack> itemStacks) {
		if (itemStacks.size() < 2) {
			return null;
		}

		Set<Item> items = itemStacks.stream()
			.filter(Objects::nonNull)
			.map(ItemStack::getItem)
			.collect(Collectors.toSet());

		TagCollection<Item> collection = ItemTags.getCollection();
		Collection<Tag<Item>> tags = collection.getTagMap().values();
		for (Tag<Item> tag : tags) {
			if (tag.getAllElements().equals(items)) {
				return tag.getId();
			}
		}
		return null;
	}

	@Override
	public boolean isInput() {
		return input;
	}

	public boolean isMode(IFocus.Mode mode) {
		return (input && mode == IFocus.Mode.INPUT) || (!input && mode == IFocus.Mode.OUTPUT);
	}
}
