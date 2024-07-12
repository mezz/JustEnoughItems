package mezz.jei.library.gui.ingredients;

import com.mojang.blaze3d.systems.RenderSystem;
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
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.common.util.Translator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class RecipeSlot implements IRecipeSlotView, IRecipeSlotDrawable {
	private static final int MAX_DISPLAYED_INGREDIENTS = 100;

	private final RecipeIngredientRole role;
	private final CycleTimer cycleTimer;
	private final List<IRecipeSlotTooltipCallback> tooltipCallbacks = new ArrayList<>();
	private final RendererOverrides rendererOverrides;

	/**
	 * Displayed ingredients, taking focus into account.
	 * {@link Optional#empty()} ingredients represent a "blank" drawn ingredient in the rotation.
	 */
	@Unmodifiable
	@Nullable
	private List<Optional<ITypedIngredient<?>>> displayIngredients = null;

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
		RecipeIngredientRole role,
		int xPos,
		int yPos,
		int ingredientCycleOffset
	) {
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
		if (this.displayIngredients == null) {
			IIngredientVisibility ingredientVisibility = Internal.getJeiRuntime().getIngredientVisibility();
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
		return cycleTimer.getCycledItem(this.displayIngredients);
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
	public void drawHighlight(GuiGraphics guiGraphics, int color) {
		int x = this.rect.getX();
		int y = this.rect.getY();
		int width = this.rect.getWidth();
		int height = this.rect.getHeight();

		guiGraphics.fillGradient(
			RenderType.guiOverlay(),
			x,
			y,
			x + width,
			y + height,
			color,
			color,
			0
		);
	}

	private <T> List<Component> getTooltip(ITypedIngredient<T> typedIngredient) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();

		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);
		List<Component> tooltip = SafeIngredientUtil.getTooltip(ingredientManager, ingredientRenderer, typedIngredient);
		for (IRecipeSlotTooltipCallback tooltipCallback : this.tooltipCallbacks) {
			tooltipCallback.onTooltip(this, tooltip);
		}

		addTagNameTooltip(ingredientManager, typedIngredient, tooltip);

		return tooltip;
	}

	private <T> void addTagNameTooltip(IIngredientManager ingredientManager, ITypedIngredient<T> ingredient, List<Component> tooltip) {
		IIngredientType<T> ingredientType = ingredient.getType();
		List<T> ingredients = getIngredients(ingredientType).toList();
		if (ingredients.isEmpty()) {
			return;
		}

		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		if (clientConfig.isHideSingleIngredientTagsEnabled() && ingredients.size() == 1) {
			return;
		}

		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		ingredientHelper.getTagEquivalent(ingredients)
			.ifPresent(tagEquivalent -> {
				tooltip.add(
					Component.translatable("jei.tooltip.recipe.tag", "")
						.withStyle(ChatFormatting.GRAY)
				);
				String tagName = Translator.translateToLocal(tagEquivalent.toString());
				tooltip.add(
					Component.literal(tagName)
					.withStyle(ChatFormatting.GRAY)
				);
			});
	}

	public void setBackground(IDrawable background) {
		this.background = background;
	}

	public void setOverlay(IDrawable overlay) {
		this.overlay = overlay;
	}

	public void set(List<Optional<ITypedIngredient<?>>> ingredients, Set<Integer> focusMatches) {
		this.allIngredients = List.copyOf(ingredients);

		if (!focusMatches.isEmpty()) {
			this.displayIngredients = focusMatches.stream()
				.filter(i -> i < this.allIngredients.size())
				.map(i -> this.allIngredients.get(i))
				.toList();
		} else {
			this.displayIngredients = null;
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
			.orElseGet(() -> {
				IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
				return ingredientManager.getIngredientRenderer(ingredientType);
			});
	}

	@Override
	public void draw(GuiGraphics guiGraphics) {
		cycleTimer.onDraw();

		final int x = this.rect.getX();
		final int y = this.rect.getY();
		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(x, y, 0);

			if (background != null) {
				background.draw(guiGraphics);
			}

			RenderSystem.enableBlend();

			getDisplayedIngredient()
				.ifPresent(ingredient -> drawIngredient(guiGraphics, ingredient));

			if (overlay != null) {
				RenderSystem.enableBlend();

				poseStack.pushPose();
				{
					poseStack.translate(0, 0, 200);
					overlay.draw(guiGraphics);
				}
				poseStack.popPose();
			}

			RenderSystem.disableBlend();
		}
		poseStack.popPose();
	}

	private <T> void drawIngredient(GuiGraphics guiGraphics, ITypedIngredient<T> typedIngredient) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);

		SafeIngredientUtil.render(guiGraphics, ingredientRenderer, typedIngredient);
	}

	@Override
	public void drawHoverOverlays(GuiGraphics guiGraphics) {
		drawHighlight(guiGraphics, 0x80FFFFFF);
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

	@Override
	public String toString() {
		return "RecipeSlot{" +
			"rect=" + rect +
			'}';
	}
}
