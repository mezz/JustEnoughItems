package mezz.jei.gui.ingredients;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.Internal;
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
import mezz.jei.ingredients.IngredientVisibility;
import mezz.jei.ingredients.TypedIngredient;
import mezz.jei.render.IngredientRenderHelper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipeSlot extends GuiComponent implements IRecipeSlotView {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int MAX_DISPLAYED_INGREDIENTS = 100;

	private final IIngredientManager ingredientManager;
	private final RecipeIngredientRole role;
	private final CycleTimer cycleTimer;

	private int legacyIngredientIndex = -1;
	private Rect2i rect;

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

	private final List<IRecipeSlotTooltipCallback> tooltipCallbacks = new ArrayList<>();
	private RendererOverrides rendererOverrides;
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
		int cycleOffset
	) {
		this.ingredientManager = ingredientManager;
		this.rendererOverrides = new RendererOverrides();
		this.role = role;
		this.rect = new Rect2i(xPos, yPos, 16, 16);
		this.cycleTimer = new CycleTimer(cycleOffset);
	}

	public void setLegacyIngredientIndex(int legacyIngredientIndex) {
		this.legacyIngredientIndex = legacyIngredientIndex;
	}

	public int getLegacyIngredientIndex() {
		return legacyIngredientIndex;
	}

	public void setRendererOverrides(RendererOverrides rendererOverrides) {
		this.rendererOverrides = rendererOverrides;
		this.rect = new Rect2i(
			this.rect.getX(),
			this.rect.getY(),
			rendererOverrides.getIngredientWidth(),
			rendererOverrides.getIngredientHeight()
		);
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

	private <T> void drawTooltip(PoseStack poseStack, int xOffset, int yOffset, int mouseX, int mouseY, ITypedIngredient<T> typedIngredient) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		T value = typedIngredient.getIngredient();

		try {
			poseStack.pushPose();
			{
				poseStack.translate(xOffset, yOffset, 0);
				drawHighlight(poseStack, 0x7FFFFFFF);
			}
			poseStack.popPose();

			IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);
			List<Component> tooltip = getTooltip(value, ingredientType, ingredientRenderer);
			TooltipRenderer.drawHoveringText(poseStack, tooltip, xOffset + mouseX, yOffset + mouseY, value, ingredientRenderer);

			RenderSystem.enableDepthTest();
		} catch (RuntimeException e) {
			LOGGER.error("Exception when rendering tooltip on {}.", value, e);
		}
	}

	private <T> List<Component> getTooltip(T value, IIngredientType<T> ingredientType, IIngredientRenderer<T> ingredientRenderer) {
		IModIdHelper modIdHelper = Internal.getHelpers().getModIdHelper();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		List<Component> tooltip = IngredientRenderHelper.getIngredientTooltipSafe(value, ingredientRenderer, ingredientHelper, modIdHelper);
		for (IRecipeSlotTooltipCallback tooltipCallback : this.tooltipCallbacks) {
			tooltipCallback.onTooltip(this, tooltip);
		}

		List<T> ingredients = getIngredients(ingredientType).toList();
		ingredientHelper.getTagEquivalent(ingredients)
			.ifPresent(tagEquivalent -> {
				final TranslatableComponent acceptsAny = new TranslatableComponent("jei.tooltip.recipe.tag", tagEquivalent);
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

	public void set(List<Optional<ITypedIngredient<?>>> ingredients, List<Focus<?>> focuses) {
		this.allIngredients = List.copyOf(ingredients);

		List<ITypedIngredient<?>> matches = getMatches(focuses);
		if (!matches.isEmpty()) {
			this.displayIngredients = matches.stream()
				.<Optional<ITypedIngredient<?>>>map(Optional::of)
				.toList();
		} else {
			IngredientVisibility ingredientVisibility = Internal.getIngredientVisibility();
			this.displayIngredients = this.allIngredients.stream()
				.filter(i -> i.isEmpty() || ingredientVisibility.isIngredientVisible(i.get()))
				.limit(MAX_DISPLAYED_INGREDIENTS)
				.toList();
		}
	}

	private List<ITypedIngredient<?>> getMatches(List<Focus<?>> focuses) {
		return focuses.stream()
			.map(this::getMatch)
			.flatMap(Optional::stream)
			.toList();
	}

	private <T> Optional<ITypedIngredient<?>> getMatch(Focus<T> focus) {
		if (this.getRole() != focus.getRole()) {
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

	public boolean isMouseOver(double recipeMouseX, double recipeMouseY) {
		return MathUtil.contains(this.rect, recipeMouseX, recipeMouseY);
	}

	public void addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback) {
		this.tooltipCallbacks.add(tooltipCallback);
	}

	private <T> IIngredientRenderer<T> getIngredientRenderer(IIngredientType<T> ingredientType) {
		return Optional.of(rendererOverrides)
			.flatMap(r -> r.getIngredientRenderer(ingredientType))
			.orElseGet(() -> ingredientManager.getIngredientRenderer(ingredientType));
	}

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

	public void setSlotName(String slotName) {
		this.slotName = slotName;
	}
}
