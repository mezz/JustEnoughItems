package mezz.jei.gui.ingredients;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.Internal;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeSlotId;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.Focus;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.TypedIngredient;
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
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

public class RecipeSlot extends GuiComponent implements IRecipeSlotView {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int MAX_DISPLAYED_INGREDIENTS = 100;

	private final IIngredientManager ingredientManager;
	private final RecipeIngredientRole role;
	private int slotIndex = -1;
	@Nullable
	private IRecipeSlotId slotId;
	private int legacyIngredientIndex = -1;

	private final Rect2i rect;
	private final int xInset;
	private final int yInset;

	private final CycleTimer cycleTimer;

	/**  ingredients, taking focus into account */
	@Unmodifiable
	private List<Optional<ITypedIngredient<?>>> displayIngredients = List.of();
	/** all ingredients, ignoring focus */
	@UnmodifiableView
	private List<Optional<ITypedIngredient<?>>> allIngredients = List.of();

	private final List<IRecipeSlotTooltipCallback> tooltipCallbacks = new ArrayList<>();
	@Nullable
	private RendererOverrides rendererOverrides;
	@Nullable
	private IDrawable background;
	@Nullable
	private IDrawable overlay;

	private boolean enabled;

	public RecipeSlot(
		IIngredientManager ingredientManager,
		RecipeIngredientRole role,
		Rect2i rect,
		int xInset,
		int yInset,
		int cycleOffset
	) {
		this.ingredientManager = ingredientManager;
		this.role = role;
		this.rect = rect;
		this.xInset = xInset;
		this.yInset = yInset;
		this.cycleTimer = new CycleTimer(cycleOffset);
	}

	public void setSlotIndex(int slotIndex) {
		this.slotIndex = slotIndex;
	}

	public void setSlotId(@Nullable IRecipeSlotId slotId) {
		this.slotId = slotId;
	}

	public void setLegacyIngredientIndex(int legacyIngredientIndex) {
		this.legacyIngredientIndex = legacyIngredientIndex;
	}

	public int getLegacyIngredientIndex() {
		return legacyIngredientIndex;
	}

	public void setRendererOverrides(@Nullable RendererOverrides rendererOverrides) {
		this.rendererOverrides = rendererOverrides;
	}

	@Override
	@Unmodifiable
	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return this.allIngredients.stream()
			.flatMap(Optional::stream);
	}

	@Override
	public <T> Stream<T> getIngredients(IIngredientType<T> ingredientType) {
		return getAllIngredients()
			.map(i -> TypedIngredient.optionalCast(i, ingredientType))
			.flatMap(Optional::stream)
			.map(ITypedIngredient::getIngredient);
	}

	@Override
	public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		return cycleTimer.getCycledItem(displayIngredients);
	}

	@Override
	public <T> Optional<T> getDisplayedIngredient(IIngredientType<T> ingredientType) {
		return getDisplayedIngredient()
			.flatMap(i -> TypedIngredient.optionalCast(i, ingredientType))
			.map(ITypedIngredient::getIngredient);
	}

	@Override
	public OptionalInt getContainerSlotIndex() {
		if (slotIndex < 0) {
			return OptionalInt.empty();
		}
		return OptionalInt.of(slotIndex);
	}

	@Override
	public Optional<IRecipeSlotId> getSlotId() {
		return Optional.ofNullable(this.slotId);
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

	private <T> void drawTooltip(PoseStack poseStack, int xOffset, int yOffset, int mouseX, int mouseY, ITypedIngredient<T> typedIngredient) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		T value = typedIngredient.getIngredient();

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
			IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);
			IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
			List<Component> tooltip = IngredientRenderHelper.getIngredientTooltipSafe(value, ingredientRenderer, ingredientHelper, modIdHelper);
			for (IRecipeSlotTooltipCallback tooltipCallback : this.tooltipCallbacks) {
				tooltipCallback.onTooltip(this, tooltip);
			}

			if (value instanceof ItemStack) {
				Collection<ItemStack> itemStacks = getIngredients(VanillaTypes.ITEM).toList();
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

	public void setBackground(@Nullable IDrawable background) {
		this.background = background;
	}

	public void setOverlay(@Nullable IDrawable overlay) {
		this.overlay = overlay;
	}

	public void set(List<Optional<ITypedIngredient<?>>> ingredients, @Nullable Focus<?> focus) {
		this.allIngredients = Collections.unmodifiableList(ingredients);
		setFocus(focus);
	}

	public <T> void setFocus(@Nullable Focus<T> focus) {
		Optional<ITypedIngredient<?>> match = getMatch(focus);
		if (match.isPresent()) {
			this.displayIngredients = List.of(match);
		} else {
			IngredientFilter ingredientFilter = Internal.getIngredientFilter();
			this.displayIngredients = this.allIngredients.stream()
				.filter(i -> i.isEmpty() || ingredientFilter.isIngredientVisible(i.get()))
				.limit(MAX_DISPLAYED_INGREDIENTS)
				.toList();
		}

		this.enabled = !this.displayIngredients.isEmpty();
	}

	private <T> Optional<ITypedIngredient<?>> getMatch(@Nullable Focus<T> focus) {
		if (focus == null || this.getRole() != focus.getRole()) {
			return Optional.empty();
		}
		ITypedIngredient<T> focusValue = focus.getTypedValue();
		IIngredientType<T> ingredientType = focusValue.getType();
		List<T> typedIngredients = getIngredients(ingredientType).toList();
		if (typedIngredients.isEmpty()) {
			return Optional.empty();
		}
		IIngredientHelper<T> ingredientHelper = this.ingredientManager.getIngredientHelper(ingredientType);
		T match = ingredientHelper.getMatch(typedIngredients, focusValue.getIngredient(), UidContext.Ingredient);
		return TypedIngredient.create(this.ingredientManager, ingredientType, match);
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return enabled && MathUtil.contains(rect, mouseX, mouseY);
	}

	public void addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback) {
		this.tooltipCallbacks.add(tooltipCallback);
	}

	private <T> IIngredientRenderer<T> getIngredientRenderer(IIngredientType<T> ingredientType) {
		return Optional.ofNullable(rendererOverrides)
			.flatMap(r -> r.getIngredientRenderer(ingredientType))
			.orElseGet(() -> ingredientManager.getIngredientRenderer(ingredientType));
	}

	public void draw(PoseStack poseStack, int xOffset, int yOffset) {
		cycleTimer.onDraw();

		final int x = xOffset + rect.getX();
		final int y = yOffset + rect.getY();

		if (background != null) {
			background.draw(poseStack, x, y);
		}

		RenderSystem.enableBlend();

		getDisplayedIngredient()
			.ifPresent(ingredient -> drawIngredient(poseStack, x, y, ingredient));

		if (overlay != null) {
			poseStack.pushPose();
			poseStack.translate(0, 0, 200);
			overlay.draw(poseStack, x, y);
			poseStack.popPose();
		}

		RenderSystem.disableBlend();
	}

	private <T> void drawIngredient(PoseStack poseStack, int x, int y, ITypedIngredient<T> typedIngredient) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		T ingredient = typedIngredient.getIngredient();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);

		try {
			int xPosition = x + xInset;
			int yPosition = y + yInset;
			int width = rect.getWidth() - (2 * xInset);
			int height = rect.getHeight() - (2 * yInset);
			ingredientRenderer.render(poseStack, xPosition, yPosition, width, height, ingredient);
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, ingredient);
		}
	}

	public void drawOverlays(PoseStack poseStack, int xOffset, int yOffset, int mouseX, int mouseY) {
		getDisplayedIngredient()
			.ifPresent(typedIngredient -> drawTooltip(poseStack, xOffset, yOffset, mouseX, mouseY, typedIngredient));
	}

	public Rect2i getRect() {
		return this.rect;
	}
}
