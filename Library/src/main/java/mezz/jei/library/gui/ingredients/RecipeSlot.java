package mezz.jei.library.gui.ingredients;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.builder.ITooltipBuilder;
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
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.SafeIngredientUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipeSlot implements IRecipeSlotView, IRecipeSlotDrawable {
	private static final int MAX_DISPLAYED_INGREDIENTS = 100;

	private final RecipeIngredientRole role;
	private final CycleTimer cycleTimer;
	private final List<IRecipeSlotTooltipCallback> tooltipCallbacks;
	private final @Nullable RendererOverrides rendererOverrides;
	private final @Nullable IDrawable background;
	private final @Nullable IDrawable overlay;
	private final @Nullable String slotName;
	private ImmutableRect2i rect;

	/**
	 * All ingredients, ignoring focus
	 * {@link Optional#empty()} ingredients represent a "blank" drawn ingredient in the rotation.
	 */
	@Unmodifiable
	private final List<Optional<ITypedIngredient<?>>> allIngredients;

	/**
	 * Displayed ingredients, taking focus into account.
	 * {@link Optional#empty()} ingredients represent a "blank" drawn ingredient in the rotation.
	 */
	@Unmodifiable
	@Nullable
	private List<Optional<ITypedIngredient<?>>> displayIngredients;

	public RecipeSlot(
		RecipeIngredientRole role,
		ImmutableRect2i rect,
		int ingredientCycleOffset,
		List<IRecipeSlotTooltipCallback> tooltipCallbacks,
		List<Optional<ITypedIngredient<?>>> allIngredients,
		@Nullable List<Optional<ITypedIngredient<?>>> displayIngredients,
		@Nullable IDrawable background,
		@Nullable IDrawable overlay,
		@Nullable String slotName,
		@Nullable RendererOverrides rendererOverrides
	) {
		this.allIngredients = List.copyOf(allIngredients);
		this.background = background;
		this.overlay = overlay;
		this.slotName = slotName;
		this.rendererOverrides = rendererOverrides;
		this.role = role;
		this.rect = rect;
		this.cycleTimer = new CycleTimer(ingredientCycleOffset);
		this.displayIngredients = displayIngredients;
		this.tooltipCallbacks = tooltipCallbacks;
	}

	@Override
	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return this.allIngredients.stream()
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

	private <T> void getTooltip(ITooltipBuilder tooltip, ITypedIngredient<T> typedIngredient) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();

		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);
		SafeIngredientUtil.getTooltip(tooltip, ingredientManager, ingredientRenderer, typedIngredient);
		for (IRecipeSlotTooltipCallback tooltipCallback : this.tooltipCallbacks) {
			tooltipCallback.onRichTooltip(this, tooltip);
		}

		addTagNameTooltip(tooltip, ingredientManager, typedIngredient);
		addIngredientsToTooltip(tooltip, typedIngredient);
	}

	private <T> List<Component> legacyGetTooltip(ITypedIngredient<T> typedIngredient) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();

		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);

		JeiTooltip tooltip = new JeiTooltip();
		SafeIngredientUtil.getTooltip(tooltip, ingredientManager, ingredientRenderer, typedIngredient);
		addTagNameTooltip(tooltip, ingredientManager, typedIngredient);

		List<Component> legacyTooltip = tooltip.toLegacyToComponents();
		for (IRecipeSlotTooltipCallback tooltipCallback : this.tooltipCallbacks) {
			//noinspection removal
			tooltipCallback.onTooltip(this, legacyTooltip);
		}
		return legacyTooltip;
	}

	private <T> void addTagNameTooltip(ITooltipBuilder tooltip, IIngredientManager ingredientManager, ITypedIngredient<T> ingredient) {
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
		ingredientHelper.getTagKeyEquivalent(ingredients)
			.ifPresent(tagKeyEquivalent -> {
				tooltip.add(
					Component.translatable("jei.tooltip.recipe.tag", "")
						.withStyle(ChatFormatting.GRAY)
				);
				IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
				Component tagName = renderHelper.getName(tagKeyEquivalent);
				tooltip.add(
					tagName.copy().withStyle(ChatFormatting.GRAY)
				);
			});
	}

	private <T> void addIngredientsToTooltip(ITooltipBuilder tooltip, ITypedIngredient<T> displayed) {
		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		if (clientConfig.isTagContentTooltipEnabled()) {
			IIngredientType<T> type = displayed.getType();

			IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
			IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();
			IIngredientRenderer<T> renderer = ingredientManager.getIngredientRenderer(type);

			List<T> ingredients = getIngredients(type).toList();

			if (ingredients.size() > 1) {
				tooltip.add(new TagContentTooltipComponent<>(renderer, ingredients));
			}
		}
	}

	@SuppressWarnings("removal")
	@Override
	public void addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback) {
		this.tooltipCallbacks.add(tooltipCallback);
	}

	private <T> IIngredientRenderer<T> getIngredientRenderer(IIngredientType<T> ingredientType) {
		return Optional.ofNullable(rendererOverrides)
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

		if (background != null) {
			background.draw(guiGraphics, x, y);
		}

		RenderSystem.enableBlend();

		getDisplayedIngredient()
			.ifPresent(ingredient -> drawIngredient(guiGraphics, ingredient, x, y));

		if (overlay != null) {
			RenderSystem.enableBlend();

			var poseStack = guiGraphics.pose();
			poseStack.pushPose();
			{
				poseStack.translate(0, 0, 200);
				overlay.draw(guiGraphics, x, y);
			}
			poseStack.popPose();
		}

		RenderSystem.disableBlend();
	}

	private <T> void drawIngredient(GuiGraphics guiGraphics, ITypedIngredient<T> typedIngredient, int xPos, int yPos) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);

		SafeIngredientUtil.render(guiGraphics, ingredientRenderer, typedIngredient, xPos, yPos);
	}

	@Override
	public void drawHoverOverlays(GuiGraphics guiGraphics) {
		drawHighlight(guiGraphics, 0x80FFFFFF);
	}

	@Override
	public List<Component> getTooltip() {
		return getDisplayedIngredient()
			.map(this::legacyGetTooltip)
			.orElseGet(List::of);
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltipBuilder) {
		getDisplayedIngredient()
			.ifPresent(ingredient -> {
				getTooltip(tooltipBuilder, ingredient);
			});
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.rect.contains(mouseX, mouseY);
	}

	@Override
	public void setPosition(int x, int y) {
		this.rect = this.rect.setPosition(x, y);
	}

	@SuppressWarnings("removal")
	@Override
	public Rect2i getRect() {
		return rect.toMutable();
	}

	@Override
	public String toString() {
		return "RecipeSlot{" +
			"rect=" + rect +
			'}';
	}
}
