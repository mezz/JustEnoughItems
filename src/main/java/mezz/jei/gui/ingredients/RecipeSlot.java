package mezz.jei.gui.ingredients;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.Internal;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.Focus;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.render.IngredientRenderHelper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.MathUtil;
import mezz.jei.util.TagUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class RecipeSlot extends GuiComponent implements IRecipeSlotView {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IIngredientManager ingredientManager;
	private final RecipeIngredientRole role;
	private final int slotIndex;

	private final Rect2i rect;
	private final int xInset;
	private final int yInset;

	private final CycleTimer cycleTimer;
	private final List<Object> displayIngredients = new ArrayList<>(); // ingredients, taking focus into account
	private final List<Object> allIngredients = new ArrayList<>(); // all ingredients, ignoring focus
	private List<IRecipeSlotTooltipCallback> tooltipCallbacks = List.of();

	@Nullable
	private IDrawable background;

	private boolean enabled;

	public RecipeSlot(
		IIngredientManager ingredientManager,
		RecipeIngredientRole role,
		int slotIndex,
		Rect2i rect,
		int xInset,
		int yInset,
		int cycleOffset
	) {
		this.ingredientManager = ingredientManager;
		this.role = role;
		this.slotIndex = slotIndex;
		this.rect = rect;
		this.xInset = xInset;
		this.yInset = yInset;
		this.cycleTimer = new CycleTimer(cycleOffset);
	}

	@Override
	public List<?> getAllIngredients() {
		return Collections.unmodifiableList(this.allIngredients);
	}

	@Override
	public <T> Stream<T> getAllIngredients(IIngredientType<T> ingredientType) {
		Class<? extends T> ingredientClass = ingredientType.getIngredientClass();
		return this.allIngredients.stream()
			.filter(ingredientClass::isInstance)
			.map(ingredientClass::cast);
	}

	@Nullable
	@Override
	public Object getDisplayedIngredient() {
		return cycleTimer.getCycledItem(displayIngredients);
	}

	@Nullable
	@Override
	public <T> T getDisplayedIngredient(IIngredientType<T> ingredientType) {
		Object displayedIngredient = getDisplayedIngredient();
		Class<? extends T> ingredientClass = ingredientType.getIngredientClass();
		if (ingredientClass.isInstance(displayedIngredient)) {
			return ingredientClass.cast(displayedIngredient);
		}
		return null;
	}

	@Override
	public int getSlotIndex() {
		return slotIndex;
	}

	@Override
	public RecipeIngredientRole getRole() {
		return role;
	}

	@Override
	public void drawHighlight(PoseStack poseStack, int color, int xOffset, int yOffset) {
		int x = rect.getX() + xOffset + xInset;
		int y = rect.getY() + yOffset + yInset;
		RenderSystem.disableDepthTest();
		fill(poseStack, x, y, x + rect.getWidth() - xInset * 2, y + rect.getHeight() - yInset * 2, color);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}

	private <T> void drawTooltip(PoseStack poseStack, int xOffset, int yOffset, int mouseX, int mouseY, T value) {
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
			IIngredientType<T> ingredientType = ingredientManager.getIngredientType(value);
			IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
			IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
			List<Component> tooltip = IngredientRenderHelper.getIngredientTooltipSafe(value, ingredientRenderer, ingredientHelper, modIdHelper);
			for (IRecipeSlotTooltipCallback tooltipCallback : this.tooltipCallbacks) {
				tooltipCallback.onTooltip(this, tooltip);
			}

			if (value instanceof ItemStack) {
				Collection<ItemStack> itemStacks = getAllIngredients(VanillaTypes.ITEM).toList();
				ResourceLocation tagEquivalent = TagUtil.getTagEquivalent(itemStacks);
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


	@Override
	public String toString() {
		return "RecipeSlotView{" +
			"role=" + role +
			", slotIndex=" + slotIndex +
			", ingredients=" + allIngredients +
			'}';
	}

	public void setBackground(IDrawable background) {
		this.background = background;
	}

	public <T, V> void set(@Nullable List<V> ingredients, @Nullable Focus<T> focus) {
		this.displayIngredients.clear();
		this.allIngredients.clear();

		List<V> allIngredients = Objects.requireNonNullElse(ingredients, Collections.emptyList());

		T match = getMatch(allIngredients, focus);
		if (match != null) {
			this.displayIngredients.add(match);
		} else {
			this.displayIngredients.addAll(allIngredients);
		}

		allIngredients = filterOutHidden(allIngredients);
		this.allIngredients.addAll(allIngredients);

		this.enabled = !this.displayIngredients.isEmpty();
	}

	@Nullable
	private <T> T getMatch(Collection<?> ingredients, @Nullable Focus<T> focus) {
		if (focus != null && this.getRole() == focus.getRole()) {
			T focusValue = focus.getValue();
			@SuppressWarnings("unchecked")
			Class<T> focusValueClass = (Class<T>) focusValue.getClass();
			List<T> typedIngredients = ingredients.stream()
				.filter(focusValueClass::isInstance)
				.map(focusValueClass::cast)
				.toList();
			if (typedIngredients.isEmpty()) {
				return null;
			}
			IIngredientHelper<T> ingredientHelper = this.ingredientManager.getIngredientHelper(focusValue);
			return ingredientHelper.getMatch(typedIngredients, focusValue, UidContext.Ingredient);
		}
		return null;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return enabled && MathUtil.contains(rect, mouseX, mouseY);
	}

	public void setTooltipCallbacks(List<IRecipeSlotTooltipCallback> tooltipCallbacks) {
		this.tooltipCallbacks = tooltipCallbacks;
	}

	public void draw(PoseStack poseStack, int xOffset, int yOffset) {
		cycleTimer.onDraw();

		if (background != null) {
			background.draw(poseStack, xOffset + rect.getX(), yOffset + rect.getY());
		}

		Object value = getDisplayedIngredient();
		if (value == null) {
			return;
		}
		IIngredientRenderer<Object> ingredientRenderer = ingredientManager.getIngredientRenderer(value);
		try {
			int xPosition = xOffset + rect.getX() + xInset;
			int yPosition = yOffset + rect.getY() + yInset;
			int width = rect.getWidth() - (2 * xInset);
			int height = rect.getHeight() - (2 * yInset);
			ingredientRenderer.render(poseStack, xPosition, yPosition, width, height, value);
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, value);
		}
	}

	public void drawOverlays(PoseStack poseStack, int xOffset, int yOffset, int mouseX, int mouseY) {
		Object value = getDisplayedIngredient();
		if (value != null) {
			drawTooltip(poseStack, xOffset, yOffset, mouseX, mouseY, value);
		}
	}

	public Rect2i getRect() {
		return this.rect;
	}

	private static <T> List<T> filterOutHidden(List<T> ingredients) {
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
}
