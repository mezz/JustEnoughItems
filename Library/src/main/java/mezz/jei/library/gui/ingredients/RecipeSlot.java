package mezz.jei.library.gui.ingredients;

import com.mojang.datafixers.util.Either;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.elements.OffsetDrawable;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.library.ingredients.SlotDisplayData;
import mezz.jei.library.ingredients.SlotDisplayInfo;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.SlotIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipeSlot implements IRecipeSlotView, IRecipeSlotDrawable {
	private final RecipeIngredientRole role;
	private final RecipeSlotIngredients ingredients;
	private final ICycler cycler;
	private final List<IRecipeSlotRichTooltipCallback> tooltipCallbacks;
	private final @Nullable RendererOverrides rendererOverrides;
	private final @Nullable OffsetDrawable background;
	private final @Nullable IDrawable overlay;
	private final @Nullable String slotName;
	private ImmutableRect2i rect;

	public RecipeSlot(
		IIngredientManagerInternal ingredientManager,
		RecipeIngredientRole role,
		ImmutableRect2i rect,
		ICycler cycler,
		List<IRecipeSlotRichTooltipCallback> tooltipCallbacks,
		List<@Nullable SlotIngredient<?>> allIngredients,
		@Nullable List<@Nullable SlotIngredient<?>> focusedIngredients,
		IFocusGroup focusGroup,
		@Nullable OffsetDrawable background,
		@Nullable IDrawable overlay,
		@Nullable String slotName,
		@Nullable RendererOverrides rendererOverrides,
		ContextMap contextMap
	) {
		this.ingredients = new RecipeSlotIngredients(
			ingredientManager,
			contextMap,
			role,
			allIngredients,
			focusedIngredients,
			focusGroup
		);
		this.background = background;
		this.overlay = overlay;
		this.slotName = slotName;
		this.rendererOverrides = rendererOverrides;
		this.role = role;
		this.rect = rect;
		this.cycler = cycler;
		this.tooltipCallbacks = tooltipCallbacks;
	}

	@Override
	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return ingredients.getAllIngredients();
	}

	@Override
	@Unmodifiable
	public List<@Nullable ITypedIngredient<?>> getAllIngredientsList() {
		return ingredients.getAllIngredientsList();
	}

	@Override
	public boolean isEmpty() {
		return ingredients.isEmpty();
	}

	@Override
	public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		return getDisplayedSlotIngredient()
			.map(SlotIngredient::typedIngredient);
	}

	private Optional<SlotIngredient<?>> getDisplayedSlotIngredient() {
		return ingredients.getDisplayedIngredient(cycler);
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
	public void drawHighlight(GuiGraphicsExtractor guiGraphics, int color) {
		int x = this.rect.getX();
		int y = this.rect.getY();
		int width = this.rect.getWidth();
		int height = this.rect.getHeight();

		guiGraphics.fillGradient(
			x,
			y,
			x + width,
			y + height,
			color,
			color
		);
	}

	private <T> void getTooltip(ITooltipBuilder tooltip, SlotIngredient<T> slotIngredient) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		ITypedIngredient<T> typedIngredient = slotIngredient.typedIngredient();

		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);
		SafeIngredientUtil.getRichTooltip(tooltip, ingredientManager, ingredientRenderer, typedIngredient);
		addSlotDisplayTooltip(tooltip, slotIngredient);
		addTagNameTooltip(tooltip, ingredientManager, slotIngredient);
		addIngredientsToTooltip(tooltip, slotIngredient);
		for (IRecipeSlotRichTooltipCallback tooltipCallback : this.tooltipCallbacks) {
			tooltipCallback.onRichTooltip(this, tooltip);
		}
	}

	@Deprecated
	private <T> List<Component> getLegacyTooltip(SlotIngredient<T> slotIngredient) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		ITypedIngredient<T> typedIngredient = slotIngredient.typedIngredient();

		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);

		JeiTooltip tooltip = new JeiTooltip();
		SafeIngredientUtil.getRichTooltip(tooltip, ingredientManager, ingredientRenderer, typedIngredient);
		addSlotDisplayTooltip(tooltip, slotIngredient);
		addTagNameTooltip(tooltip, ingredientManager, slotIngredient);

		for (IRecipeSlotRichTooltipCallback tooltipCallback : this.tooltipCallbacks) {
			tooltipCallback.onRichTooltip(this, tooltip);
		}
		return tooltip.getLegacyComponents();
	}

	private static void addSlotDisplayTooltip(
		ITooltipBuilder tooltip,
		SlotIngredient<?> slotIngredient
	) {
		Optional.ofNullable(slotIngredient.slotDisplayData())
			.map(SlotDisplayData::info)
			.flatMap(SlotDisplayInfo::tooltipHeader)
			.ifPresent(tooltipHeader -> tooltip.getLines().addFirst(Either.left(tooltipHeader)));
	}

	private <T> void addTagNameTooltip(ITooltipBuilder tooltip, IIngredientManager ingredientManager, SlotIngredient<T> slotIngredient) {
		ITypedIngredient<T> ingredient = slotIngredient.typedIngredient();
		IIngredientType<T> ingredientType = ingredient.getType();
		List<T> ingredients = getVisibleIngredientsInDisplayGroup(slotIngredient);
		if (ingredients.isEmpty()) {
			return;
		}

		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		if (clientConfig.getHideSingleTagContentTooltipEnabled() && ingredients.size() == 1) {
			return;
		}

		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		getTagKeyEquivalent(ingredientHelper, ingredients, slotIngredient)
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

	private static <T> Optional<TagKey<?>> getTagKeyEquivalent(
		IIngredientHelper<T> ingredientHelper,
		List<T> ingredients,
		SlotIngredient<T> ingredient
	) {
		SlotDisplayData<T> slotDisplayData = ingredient.slotDisplayData();
		if (slotDisplayData == null) {
			return ingredientHelper.getTagKeyEquivalent(ingredients);
		}
		return slotDisplayData.info()
			.tagKeyOrElse(() -> ingredientHelper.getTagKeyEquivalent(ingredients));
	}

	private <T> void addIngredientsToTooltip(ITooltipBuilder tooltip, SlotIngredient<T> displayed) {
		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		if (clientConfig.isTagContentTooltipEnabled()) {
			IIngredientType<T> type = displayed.typedIngredient().getType();

			IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
			IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();
			IIngredientRenderer<T> renderer = ingredientManager.getIngredientRenderer(type);

			List<T> ingredients = getVisibleIngredientsInDisplayGroup(displayed);

			if (ingredients.size() > 1) {
				tooltip.add(new TagContentTooltipComponent<>(renderer, ingredients));
			}
		}
	}

	private <T> List<T> getVisibleIngredientsInDisplayGroup(SlotIngredient<T> displayed) {
		return ingredients.getVisibleIngredientsInDisplayGroup(displayed);
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
	@SuppressWarnings("removal")
	public void draw(GuiGraphicsExtractor guiGraphics) {
		draw(guiGraphics, false);
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, boolean hovered) {
		final int x = this.rect.getX();
		final int y = this.rect.getY();

		if (background != null) {
			background.draw(guiGraphics, x, y);
		}

		if (hovered) {
			IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
			drawHighlight(guiGraphics, screenHelper.getSlotHighlightBackSprite());
		}

		getDisplayedSlotIngredient()
			.map(SlotIngredient::typedIngredient)
			.ifPresent(ingredient -> drawIngredient(guiGraphics, ingredient, x, y));

		if (overlay != null) {
			overlay.draw(guiGraphics, x, y);
		}

		if (hovered) {
			IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
			drawHighlight(guiGraphics, screenHelper.getSlotHighlightFrontSprite());
		}
	}

	@Override
	@SuppressWarnings("removal")
	public void drawHoverOverlays(GuiGraphicsExtractor guiGraphics) {
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		drawHighlight(guiGraphics, screenHelper.getSlotHighlightFrontSprite());
	}

	private void drawHighlight(GuiGraphicsExtractor guiGraphics, Identifier sprite) {
		int x = this.rect.getX();
		int y = this.rect.getY();
		int width = this.rect.getWidth();
		int height = this.rect.getHeight();

		guiGraphics.blitSprite(
			RenderPipelines.GUI_TEXTURED,
			sprite,
			x - 4,
			y - 4,
			width + 8,
			height + 8
		);
	}

	private <T> void drawIngredient(GuiGraphicsExtractor guiGraphics, ITypedIngredient<T> typedIngredient, int xPos, int yPos) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);

		SafeIngredientUtil.render(guiGraphics, ingredientRenderer, typedIngredient, xPos, yPos);
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated
	public List<Component> getTooltip() {
		return getDisplayedSlotIngredient()
			.map(this::getLegacyTooltip)
			.orElseGet(List::of);
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated
	public void getTooltip(ITooltipBuilder tooltipBuilder) {
		getDisplayedSlotIngredient()
			.ifPresent(ingredient -> getTooltip(tooltipBuilder, ingredient));
	}

	@Override
	public void drawTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		getDisplayedSlotIngredient()
			.ifPresent(ingredient -> {
				JeiTooltip tooltip = new JeiTooltip();
				getTooltip(tooltip, ingredient);
				tooltip.draw(guiGraphics, mouseX, mouseY);
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

	@Override
	public void clearDisplayOverrides() {
		ingredients.clearDisplayOverrides();
	}

	@Override
	public IIngredientAcceptor<?> createDisplayOverrides() {
		return ingredients.createDisplayOverrides();
	}

	@Override
	public Rect2i getAreaIncludingBackground() {
		if (background == null) {
			return rect.toMutable();
		}
		return MathUtil.union(rect, background.getArea()).toMutable();
	}

	@Override
	public String toString() {
		return "RecipeSlot{" +
			"rect=" + rect +
			'}';
	}
}
