package mezz.jei.gui.ingredients;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientTooltipCallback;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.gui.Focus;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.render.IngredientRenderHelper;
import mezz.jei.util.ErrorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GuiIngredient<T> extends GuiComponent implements IGuiIngredient<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final int slotIndex;
	private final RecipeIngredientRole role;

	private final Rect2i rect;
	private final int xInset;
	private final int yInset;

	private final CycleTimer cycleTimer;
	private final List<T> displayIngredients = new ArrayList<>(); // ingredients, taking focus into account
	private final List<T> allIngredients = new ArrayList<>(); // all ingredients, ignoring focus
	private final IIngredientRenderer<T> ingredientRenderer;
	private final IIngredientHelper<T> ingredientHelper;
	private List<IGuiIngredientTooltipCallback> tooltipCallbacks = Collections.emptyList();

	@Nullable
	private IDrawable background;

	private boolean enabled;

	public GuiIngredient(
		int slotIndex,
		RecipeIngredientRole role,
		IIngredientRenderer<T> ingredientRenderer,
		IIngredientHelper<T> ingredientHelper,
		Rect2i rect,
		int xInset,
		int yInset,
		int cycleOffset
	) {
		this.ingredientRenderer = ingredientRenderer;
		this.ingredientHelper = ingredientHelper;

		this.slotIndex = slotIndex;
		this.role = role;

		this.rect = rect;
		this.xInset = xInset;
		this.yInset = yInset;

		this.cycleTimer = new CycleTimer(cycleOffset);
	}

	@Override
	public IIngredientType<T> getIngredientType() {
		return ingredientHelper.getIngredientType();
	}

	public Rect2i getRect() {
		return rect;
	}

	public boolean isMouseOver(double xOffset, double yOffset, double mouseX, double mouseY) {
		return enabled &&
			(mouseX >= xOffset + rect.getX()) &&
			(mouseY >= yOffset + rect.getY()) &&
			(mouseX < xOffset + rect.getX() + rect.getWidth()) &&
			(mouseY < yOffset + rect.getY() + rect.getHeight());
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

	@Override
	public int getSlotIndex() {
		return slotIndex;
	}

	@Override
	public RecipeIngredientRole getRole() {
		return this.role;
	}

	public void set(@Nullable List<T> ingredients, @Nullable Focus<T> focus) {
		this.displayIngredients.clear();
		this.allIngredients.clear();
		List<T> displayIngredients = Objects.requireNonNullElse(ingredients, Collections.emptyList());

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
		IngredientFilter ingredientFilter = Internal.getIngredientFilter();
		List<T> visible = new ArrayList<>();
		for (T ingredient : ingredients) {
			if (ingredient == null || ingredientFilter.isIngredientVisible(ingredient)) {
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
		if (focus != null && isFocusType(focus.getRole())) {
			T focusValue = focus.getValue();
			return ingredientHelper.getMatch(ingredients, focusValue, UidContext.Ingredient);
		}
		return null;
	}

	public void setTooltipCallbacks(List<IGuiIngredientTooltipCallback> tooltipCallbacks) {
		this.tooltipCallbacks = tooltipCallbacks;
	}

	public void draw(PoseStack poseStack, int xOffset, int yOffset) {
		cycleTimer.onDraw();

		if (background != null) {
			background.draw(poseStack, xOffset + rect.getX(), yOffset + rect.getY());
		}

		T value = getDisplayedIngredient();
		try {
			int xPosition = xOffset + rect.getX() + xInset;
			int yPosition = yOffset + rect.getY() + yInset;
			int width = rect.getWidth() - (2 * xInset);
			int height = rect.getHeight() - (2 * yInset);
			ingredientRenderer.render(poseStack, xPosition, yPosition, width, height, value);
		} catch (RuntimeException | LinkageError e) {
			if (value != null) {
				throw ErrorUtil.createRenderIngredientException(e, value);
			}
			throw e;
		}
	}

	@Override
	public void drawHighlight(PoseStack poseStack, int color, int xOffset, int yOffset) {
		int x = rect.getX() + xOffset + xInset;
		int y = rect.getY() + yOffset + yInset;
		RenderSystem.disableDepthTest();
		fill(poseStack, x, y, x + rect.getWidth() - xInset * 2, y + rect.getHeight() - yInset * 2, color);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}

	public void drawOverlays(PoseStack poseStack, int xOffset, int yOffset, int mouseX, int mouseY) {
		T value = getDisplayedIngredient();
		if (value != null) {
			drawTooltip(poseStack, xOffset, yOffset, mouseX, mouseY, value);
		}
	}

	private void drawTooltip(PoseStack poseStack, int xOffset, int yOffset, int mouseX, int mouseY, T value) {
		try {
			RenderSystem.disableDepthTest();

			fill(poseStack,
				xOffset + rect.getX() + xInset,
				yOffset + rect.getY() + yInset,
				xOffset + rect.getX() + rect.getWidth() - xInset,
				yOffset + rect.getY() + rect.getHeight() - yInset,
				0x7FFFFFFF);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

			IModIdHelper modIdHelper = Internal.getHelpers().getModIdHelper();
			List<Component> tooltip = IngredientRenderHelper.getIngredientTooltipSafe(value, ingredientRenderer, ingredientHelper, modIdHelper);
			for (IGuiIngredientTooltipCallback tooltipCallback : this.tooltipCallbacks) {
				tooltipCallback.onTooltip(this, tooltip);
			}

			if (value instanceof ItemStack) {
				//noinspection unchecked
				Collection<ItemStack> itemStacks = (Collection<ItemStack>) this.allIngredients;
				ResourceLocation tagEquivalent = getTagEquivalent(itemStacks);
				if (tagEquivalent != null) {
					final TranslatableComponent acceptsAny = new TranslatableComponent("jei.tooltip.recipe.tag", tagEquivalent);
					tooltip.add(acceptsAny.withStyle(ChatFormatting.GRAY));
				}
			}
			TooltipRenderer.drawHoveringText(poseStack, tooltip, xOffset + mouseX, yOffset + mouseY, value, ingredientRenderer);

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

		List<Item> items = itemStacks.stream()
			.filter(Objects::nonNull)
			.map(ItemStack::getItem)
			.toList();

		TagCollection<Item> collection = ItemTags.getAllTags();
		Collection<Tag<Item>> tags = collection.getAllTags().values();
		return tags.stream()
			.filter(tag -> tag.getValues().equals(items))
			.findFirst()
			.map(collection::getId)
			.orElse(null);
	}

	@Override
	public boolean isInput() {
		return switch (role) {
			case INPUT, CATALYST -> true;
			case OUTPUT -> false;
		};
	}

	public boolean isFocusType(RecipeIngredientRole type) {
		return this.role == type;
	}
}
