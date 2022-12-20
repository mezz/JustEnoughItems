package mezz.jei.library.gui.ingredients;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.IngredientTooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class RecipeSlot extends GuiComponent implements IRecipeSlotView, IRecipeSlotDrawable {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int MAX_DISPLAYED_INGREDIENTS = 100;

	private final IIngredientManager ingredientManager;
	private final RecipeIngredientRole role;
	private final CycleTimer cycleTimer;
	private final List<IRecipeSlotTooltipCallback> tooltipCallbacks = new ArrayList<>();
	private final RendererOverrides rendererOverrides;

	/**
	 * Displayed ingredients, taking focus into account.
	 * {@link Optional#empty()} ingredients represent a "blank" drawn ingredient in the rotation.
	 */
	@Unmodifiable
	private List<Optional<ITypedIngredient<?>>> displayIngredients = List.of();

	/**
	 * All ingredients, ignoring focus
	 * {@link Optional#empty()} ingredients represent a "blank" drawn ingredient in the rotation.
	 */
	@Unmodifiable
	private List<Optional<ITypedIngredient<?>>> allIngredients = List.of();

	private ImmutableRect2i rect;
	@Nullable
	private IDrawable background;
	@Nullable
	private IDrawable overlay;
	@Nullable
	private String slotName;

	public RecipeSlot(
		IIngredientManager ingredientManager,
		RecipeIngredientRole role,
		int xPos,
		int yPos,
		int ingredientCycleOffset
	) {
		this.ingredientManager = ingredientManager;
		this.rendererOverrides = new RendererOverrides();
		this.role = role;
		this.rect = new ImmutableRect2i(xPos, yPos, 16, 16);
		this.cycleTimer = new CycleTimer(ingredientCycleOffset);
	}

	@Override
	@Unmodifiable
	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return this.allIngredients.stream()
			.flatMap(Optional::stream);
	}

	@Override
	public boolean isEmpty() {
		// note that an all-blanks (all Optional#isEmpty()) slot is considered empty.
		return getAllIngredients().findAny().isEmpty();
	}

	@Override
	public <T> Stream<T> getIngredients(IIngredientType<T> ingredientType) {
		return getAllIngredients()
			.map(i -> i.getIngredient(ingredientType))
			.flatMap(Optional::stream);
	}

	@Override
	public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		return cycleTimer.getCycledItem(displayIngredients);
	}

	@Override
	public <T> Optional<T> getDisplayedIngredient(IIngredientType<T> ingredientType) {
		return getDisplayedIngredient()
			.flatMap(i -> i.getIngredient(ingredientType));
	}

	@Override
	public Optional<String> getSlotName() {
		return Optional.ofNullable(this.slotName);
	}

	@Override
	public RecipeIngredientRole getRole() {
		return role;
	}

	@Override
	public void drawHighlight(PoseStack poseStack, int color) {
		int x = this.rect.getX();
		int y = this.rect.getY();
		int width = this.rect.getWidth();
		int height = this.rect.getHeight();

		RenderSystem.disableDepthTest();
		fill(poseStack, x, y, x + width, y + height , color);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}

	private <T> List<Component> getTooltip(ITypedIngredient<T> typedIngredient) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		T value = typedIngredient.getIngredient();

		try {
			IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);
			return getTooltip(value, ingredientType, ingredientRenderer);
		} catch (RuntimeException e) {
			LOGGER.error("Exception when rendering tooltip on {}.", value, e);
			return List.of();
		}
	}

	private <T> List<Component> getTooltip(T value, IIngredientType<T> ingredientType, IIngredientRenderer<T> ingredientRenderer) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		List<Component> tooltip = IngredientTooltipHelper.getMutableIngredientTooltipSafe(value, ingredientRenderer);
		for (IRecipeSlotTooltipCallback tooltipCallback : this.tooltipCallbacks) {
			tooltipCallback.onTooltip(this, tooltip);
		}

		List<T> ingredients = getIngredients(ingredientType).toList();
		ingredientHelper.getTagEquivalent(ingredients)
			.ifPresent(tagEquivalent -> {
				final MutableComponent acceptsAny = Component.translatable("jei.tooltip.recipe.tag", tagEquivalent);
				tooltip.add(acceptsAny.withStyle(ChatFormatting.GRAY));
			});

		return tooltip;
	}

	public void setBackground(IDrawable background) {
		this.background = background;
	}

	public void setOverlay(IDrawable overlay) {
		this.overlay = overlay;
	}

	public void set(List<Optional<ITypedIngredient<?>>> ingredients, Set<Integer> focusMatches, IIngredientVisibility ingredientVisibility) {
		this.allIngredients = List.copyOf(ingredients);

		if (!focusMatches.isEmpty()) {
			this.displayIngredients = focusMatches.stream()
				.filter(i -> i < this.allIngredients.size())
				.map(i -> this.allIngredients.get(i))
				.toList();
		} else {
			this.displayIngredients = this.allIngredients.stream()
				.filter(i -> i.isEmpty() || ingredientVisibility.isIngredientVisible(i.get()))
				.limit(MAX_DISPLAYED_INGREDIENTS)
				.toList();

			if (this.displayIngredients.isEmpty()) {
				this.displayIngredients = this.allIngredients.stream()
					.limit(MAX_DISPLAYED_INGREDIENTS)
					.toList();
			}
		}
	}

	@Override
	public void addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback) {
		this.tooltipCallbacks.add(tooltipCallback);
	}

	public <T> void addRenderOverride(IIngredientType<T> ingredientType, IIngredientRenderer<T> ingredientRenderer) {
		this.rendererOverrides.addOverride(ingredientType, ingredientRenderer);
		this.rect = new ImmutableRect2i(
			this.rect.getX(),
			this.rect.getY(),
			rendererOverrides.getIngredientWidth(),
			rendererOverrides.getIngredientHeight()
		);
	}

	private <T> IIngredientRenderer<T> getIngredientRenderer(IIngredientType<T> ingredientType) {
		return Optional.of(rendererOverrides)
			.flatMap(r -> r.getIngredientRenderer(ingredientType))
			.orElseGet(() -> ingredientManager.getIngredientRenderer(ingredientType));
	}

	@Override
	public void draw(PoseStack poseStack) {
		cycleTimer.onDraw();

		final int x = this.rect.getX();
		final int y = this.rect.getY();
		poseStack.pushPose();
		{
			poseStack.translate(x, y, 0);

			if (background != null) {
				background.draw(poseStack);
			}

			RenderSystem.enableBlend();

			getDisplayedIngredient()
				.ifPresent(ingredient -> drawIngredient(poseStack, ingredient));

			if (overlay != null) {
				RenderSystem.enableBlend();

				poseStack.pushPose();
				{
					poseStack.translate(0, 0, 200);
					overlay.draw(poseStack);
				}
				poseStack.popPose();
			}

			RenderSystem.disableBlend();
		}
		poseStack.popPose();
	}

	private <T> void drawIngredient(PoseStack poseStack, ITypedIngredient<T> typedIngredient) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		T ingredient = typedIngredient.getIngredient();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);

		try {
			ingredientRenderer.render(poseStack, ingredient);
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, ingredient, ingredientManager);
		}
	}

	@Override
	public void drawHoverOverlays(PoseStack poseStack) {
		drawHighlight(poseStack, 0x80FFFFFF);
	}

	@Override
	public List<Component> getTooltip() {
		return getDisplayedIngredient()
			.map(this::getTooltip)
			.orElseGet(List::of);
	}

	@Override
	public Rect2i getRect() {
		return this.rect.toMutable();
	}

	public void setSlotName(String slotName) {
		this.slotName = slotName;
	}
}
