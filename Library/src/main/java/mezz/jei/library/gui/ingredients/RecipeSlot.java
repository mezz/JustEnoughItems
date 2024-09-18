package mezz.jei.library.gui.ingredients;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.SafeIngredientUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipeSlot implements IRecipeSlotView, IRecipeSlotDrawable {
	private static final int MAX_DISPLAYED_INGREDIENTS = 100;

	private final RecipeIngredientRole role;
	private final ICycler cycler;
	private final List<IRecipeSlotTooltipCallback> tooltipCallbacks;
	private final @Nullable RendererOverrides rendererOverrides;
	private final @Nullable IDrawable background;
	private final @Nullable IDrawable overlay;
	private final @Nullable String slotName;
	private final ImmutableRect2i rect;

	/**
	 * All ingredients, ignoring focus and visibility
	 * {@link Optional#empty()} ingredients represent a "blank" drawn ingredient in the rotation.
	 */
	@Unmodifiable
	private final List<Optional<ITypedIngredient<?>>> allIngredients;

	/**
	 * Displayed ingredients, taking focus and visibility into account.
	 * {@link Optional#empty()} ingredients represent a "blank" drawn ingredient in the rotation.
	 */
	@Unmodifiable
	@Nullable
	private List<Optional<ITypedIngredient<?>>> displayIngredients;

	public RecipeSlot(
		RecipeIngredientRole role,
		ImmutableRect2i rect,
		ICycler cycler,
		List<IRecipeSlotTooltipCallback> tooltipCallbacks,
		List<Optional<ITypedIngredient<?>>> allIngredients,
		@Nullable List<Optional<ITypedIngredient<?>>> focusedIngredients,
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
		this.cycler = cycler;
		this.displayIngredients = focusedIngredients;
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
			this.displayIngredients = calculateDisplayIngredients(this.allIngredients);
		}
		return cycler.getCycled(this.displayIngredients);
	}

	private static List<Optional<ITypedIngredient<?>>> calculateDisplayIngredients(List<Optional<ITypedIngredient<?>>> allIngredients) {
		if (allIngredients.isEmpty()) {
			return List.of();
		}

		List<Optional<ITypedIngredient<?>>> visibleIngredients = List.of();
		boolean hasInvisibleIngredients = false;

		// hide invisible ingredients if there are any
		// try scanning through all the ingredients without building the list of visible ingredients.
		// if an invisible ingredient is found, start building the list of visible ingredients
		IIngredientVisibility ingredientVisibility = Internal.getJeiRuntime().getIngredientVisibility();
		for (int i = 0; i < allIngredients.size() && visibleIngredients.size() < MAX_DISPLAYED_INGREDIENTS; i++) {
			Optional<ITypedIngredient<?>> ingredient = allIngredients.get(i);
			boolean visible = ingredient.isEmpty() || ingredientVisibility.isIngredientVisible(ingredient.get());
			if (visible) {
				if (hasInvisibleIngredients) {
					visibleIngredients.add(ingredient);
				}
			} else if (!hasInvisibleIngredients) {
				hasInvisibleIngredients = true;
				// `i` is the first invisible ingredient, start putting visible ingredients into visibleIngredients
				visibleIngredients = new ArrayList<>(allIngredients.subList(0, i));
			}
		}

		if (!visibleIngredients.isEmpty()) {
			// some ingredients have been successfully hidden, and some are still visible
			return visibleIngredients;
		}

		// either everything is visible or everything is invisible.
		// if everything is invisible, we show them all anyway so that the recipe slot isn't blank
		if (allIngredients.size() < MAX_DISPLAYED_INGREDIENTS) {
			// re-use allIngredients to save some memory
			return allIngredients;
		} else {
			return allIngredients.subList(0, MAX_DISPLAYED_INGREDIENTS);
		}
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
		GuiComponent.fill(poseStack, x, y, x + width, y + height , color);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}

	private <T> List<Component> legacyGetTooltip(ITypedIngredient<T> typedIngredient) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();

		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);

		JeiTooltip tooltip = new JeiTooltip();
		SafeIngredientUtil.getTooltip(tooltip, ingredientManager, ingredientRenderer, typedIngredient);
		addTagNameTooltip(tooltip, ingredientManager, typedIngredient);

		List<Component> legacyComponents = tooltip.getLegacyComponents();
		for (IRecipeSlotTooltipCallback tooltipCallback : this.tooltipCallbacks) {
			tooltipCallback.onTooltip(this, legacyComponents);
		}
		return legacyComponents;
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
				Component tagName = Component.literal(tagKeyEquivalent.location().toString());
				tooltip.add(
					tagName.copy().withStyle(ChatFormatting.GRAY)
				);
			});
	}

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
	public void draw(PoseStack poseStack) {
		final int x = this.rect.getX();
		final int y = this.rect.getY();

		if (background != null) {
			background.draw(poseStack, x, y);
		}

		RenderSystem.enableBlend();

		getDisplayedIngredient()
			.ifPresent(ingredient -> drawIngredient(poseStack, ingredient, x, y));

		if (overlay != null) {
			RenderSystem.enableBlend();

			poseStack.pushPose();
			{
				poseStack.translate(0, 0, 200);
				overlay.draw(poseStack, x, y);
			}
			poseStack.popPose();
		}

		RenderSystem.disableBlend();
	}

	private <T> void drawIngredient(PoseStack poseStack, ITypedIngredient<T> typedIngredient, int xPos, int yPos) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		T ingredient = typedIngredient.getIngredient();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);

		try {
			ingredientRenderer.render(poseStack, ingredient, xPos, yPos);
		} catch (RuntimeException | LinkageError e) {
			IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
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
			.map(this::legacyGetTooltip)
			.orElseGet(List::of);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.rect.contains(mouseX, mouseY);
	}

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
