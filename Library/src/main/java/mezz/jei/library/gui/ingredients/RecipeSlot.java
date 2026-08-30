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
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.RecipeSlotOptionsTooltipComponent;
import mezz.jei.common.gui.elements.OffsetDrawable;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.common.util.function.LazySupplier;
import mezz.jei.library.ingredients.SlotDisplayData;
import mezz.jei.library.ingredients.SlotDisplayInfo;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.SlotIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

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
	private final LazySupplier<Optional<TagKey<?>>> tagKey;
	private Runnable displayOverridesChangedListener = () -> {};
	private ImmutableRect2i rect;

	public RecipeSlot(
		IIngredientManagerInternal ingredientManager,
		RecipeIngredientRole role,
		ImmutableRect2i rect,
		ICycler cycler,
		List<IRecipeSlotRichTooltipCallback> tooltipCallbacks,
		List<? extends @Nullable SlotIngredient<?>> allIngredients,
		@Nullable List<? extends @Nullable SlotIngredient<?>> focusedIngredients,
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
			focusGroup,
			this::onDisplayOverridesChanged
		);
		this.background = background;
		this.overlay = overlay;
		this.slotName = slotName;
		this.rendererOverrides = rendererOverrides;
		this.role = role;
		this.rect = rect;
		this.cycler = cycler;
		this.tooltipCallbacks = tooltipCallbacks;
		this.tagKey = new LazySupplier<>(this::calculateTagKey);
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

	@Override
	public Stream<ITypedIngredient<?>> getDisplayedIngredients() {
		return getDisplayedSlotIngredient()
			.stream()
			.flatMap(ingredients::getVisibleTypedIngredientsInDisplayGroup);
	}

	private Optional<SlotIngredient<?>> getDisplayedSlotIngredient() {
		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		if (!clientConfig.recipeSlotCyclingEnabled().getValue()) {
			return ingredients.getFirstDisplayedIngredient();
		}
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
	public void drawHighlight(GuiGraphics guiGraphics, int color) {
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

	private <T> void addIngredientTooltip(ITooltipBuilder tooltip, SlotIngredient<T> slotIngredient) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		ITypedIngredient<T> typedIngredient = slotIngredient.typedIngredient();
		List<ITypedIngredient<?>> visibleCandidates = getVisibleIngredients();
		List<ITypedIngredient<?>> visibleDisplayGroup = getVisibleIngredientsInDisplayGroup(slotIngredient);

		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);
		SafeIngredientUtil.getRichTooltip(tooltip, ingredientManager, ingredientRenderer, typedIngredient);
		addSlotDisplayTooltip(tooltip, slotIngredient);
		addTagNameTooltip(tooltip, ingredientManager, slotIngredient, visibleDisplayGroup);
		addIngredientGridToTooltip(tooltip, ingredientManager, visibleCandidates);
		if (visibleCandidates.size() > 1) {
			var pauseRecipeCycling = Internal.getKeyMappings().getPauseRecipeCycling();
			tooltip.add(new RecipeSlotOptionsTooltipComponent(pauseRecipeCycling));
		}
	}

	private void addTooltip(ITooltipBuilder tooltip) {
		getDisplayedSlotIngredient()
			.ifPresent(ingredient -> addIngredientTooltip(tooltip, ingredient));
		for (IRecipeSlotRichTooltipCallback tooltipCallback : tooltipCallbacks) {
			tooltipCallback.onRichTooltip(this, tooltip);
		}
	}

	@Deprecated
	private <T> List<Component> getLegacyTooltip(SlotIngredient<T> slotIngredient) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		ITypedIngredient<T> typedIngredient = slotIngredient.typedIngredient();

		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);
		List<ITypedIngredient<?>> visibleCandidates = getVisibleIngredientsInDisplayGroup(slotIngredient);

		JeiTooltip tooltip = new JeiTooltip();
		SafeIngredientUtil.getRichTooltip(tooltip, ingredientManager, ingredientRenderer, typedIngredient);
		addSlotDisplayTooltip(tooltip, slotIngredient);
		addTagNameTooltip(tooltip, ingredientManager, slotIngredient, visibleCandidates);

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

	private static <T> void addTagNameTooltip(
		ITooltipBuilder tooltip,
		IIngredientManager ingredientManager,
		SlotIngredient<T> slotIngredient,
		List<ITypedIngredient<?>> visibleCandidates
	) {
		if (visibleCandidates.isEmpty()) {
			return;
		}

		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		if (clientConfig.hideSingleTagContentTooltipEnabled().getValue() && visibleCandidates.size() == 1) {
			return;
		}

		getTagKeyEquivalent(ingredientManager, visibleCandidates, slotIngredient)
			.ifPresent(tagKeyEquivalent -> {
				String registryName = tagKeyEquivalent.registry().identifier().getPath()
					.replace('_', ' ');
				tooltip.add(
					Component.translatable("jei.tooltip.recipe.tag", StringUtils.capitalize(registryName))
						.withStyle(ChatFormatting.GRAY)
				);
				IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
				Component tagName = renderHelper.getName(tagKeyEquivalent);
				tooltip.add(
					tagName.copy().withStyle(ChatFormatting.GRAY)
				);
			});
	}

	@Override
	public Optional<TagKey<?>> getTagKey() {
		return this.tagKey.get();
	}

	private Optional<TagKey<?>> calculateTagKey() {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		if (!ingredients.hasDisplayOverrides()) {
			List<ITypedIngredient<?>> allIngredients = ingredients.getAllIngredients().toList();
			return ingredients.getSingleDisplayGroupTagKey(() -> getTagKeyEquivalent(ingredientManager, allIngredients));
		}
		return getDisplayedSlotIngredient()
			.flatMap(displayed -> {
				List<ITypedIngredient<?>> displayGroup = ingredients.getCandidateIngredientsInDisplayGroup(displayed).toList();
				return ingredients.getDisplayGroupTagKey(
					displayed,
					() -> getTagKeyEquivalent(ingredientManager, displayGroup)
				);
			});
	}

	private static Optional<TagKey<?>> getTagKeyEquivalent(
		IIngredientManager ingredientManager,
		List<ITypedIngredient<?>> ingredients
	) {
		return ingredients.stream()
			.findFirst()
			.flatMap(first -> getTagKeyEquivalent(ingredientManager, ingredients, first));
	}

	private static <T> Optional<TagKey<?>> getTagKeyEquivalent(
		IIngredientManager ingredientManager,
		List<ITypedIngredient<?>> allIngredients,
		ITypedIngredient<T> first
	) {
		IIngredientType<T> ingredientType = first.getType();
		List<T> ingredients = allIngredients.stream()
			.map(ingredient -> ingredient.getIngredient(ingredientType))
			.flatMap(Optional::stream)
			.toList();
		if (ingredients.size() != allIngredients.size()) {
			return Optional.empty();
		}
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		return ingredientHelper.getTagKeyEquivalent(ingredients);
	}

	private static <T> Optional<TagKey<?>> getTagKeyEquivalent(
		IIngredientManager ingredientManager,
		List<ITypedIngredient<?>> allIngredients,
		SlotIngredient<T> ingredient
	) {
		if (allIngredients.isEmpty()) {
			return Optional.empty();
		}

		ITypedIngredient<T> typedIngredient = ingredient.typedIngredient();
		IIngredientType<T> ingredientType = typedIngredient.getType();
		List<T> ingredients = allIngredients.stream()
			.map(candidate -> candidate.getIngredient(ingredientType))
			.flatMap(Optional::stream)
			.toList();
		if (ingredients.size() != allIngredients.size()) {
			return Optional.empty();
		}
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		SlotDisplayData<T> slotDisplayData = ingredient.slotDisplayData();
		if (slotDisplayData == null) {
			return ingredientHelper.getTagKeyEquivalent(ingredients);
		}
		return slotDisplayData.info()
			.tagKeyOrElse(() -> ingredientHelper.getTagKeyEquivalent(ingredients));
	}

	private static void addIngredientGridToTooltip(
		ITooltipBuilder tooltip,
		IIngredientManager ingredientManager,
		List<ITypedIngredient<?>> visibleCandidates
	) {
		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		if (clientConfig.tagContentTooltipEnabled().getValue() && visibleCandidates.size() > 1) {
			List<ITypedIngredient<?>> normalizedCandidates = visibleCandidates.stream()
				.<ITypedIngredient<?>>map(ingredient -> normalizeTypedIngredient(ingredientManager, ingredient))
				.toList();
			tooltip.add(new TagContentTooltipComponent(ingredientManager, normalizedCandidates));
		}
	}

	private static ITypedIngredient<?> normalizeTypedIngredient(
		IIngredientManager ingredientManager,
		ITypedIngredient<?> ingredient
	) {
		return ingredientManager.normalizeTypedIngredient(ingredient);
	}

	private List<ITypedIngredient<?>> getVisibleIngredientsInDisplayGroup(SlotIngredient<?> displayed) {
		return ingredients.getVisibleTypedIngredientsInDisplayGroup(displayed)
			.toList();
	}

	private List<ITypedIngredient<?>> getVisibleIngredients() {
		if (ingredients.hasDisplayOverrides()) {
			return getDisplayedIngredients().toList();
		}
		return ingredients.getVisibleTypedIngredients()
			.toList();
	}

	private boolean hasCandidates() {
		return getVisibleIngredients()
			.stream()
			.limit(2)
			.count() > 1;
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
	public void draw(GuiGraphics guiGraphics) {
		draw(guiGraphics, false);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, boolean hovered) {
		final int x = this.rect.getX();
		final int y = this.rect.getY();

		if (background != null) {
			background.draw(guiGraphics, x, y);
		}

		if (hovered) {
			IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
			drawHighlight(guiGraphics, screenHelper.getSlotHighlightBackSprite());
		}

		Optional<SlotIngredient<?>> displayedIngredient = getDisplayedSlotIngredient();
		displayedIngredient
			.map(SlotIngredient::typedIngredient)
			.ifPresent(ingredient -> drawIngredient(guiGraphics, ingredient, x, y));

		if (overlay != null) {
			overlay.draw(guiGraphics, x, y);
		}

		displayedIngredient.ifPresent(ignored -> drawCandidatesBadge(guiGraphics));

		if (hovered) {
			IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
			drawHighlight(guiGraphics, screenHelper.getSlotHighlightFrontSprite());
		}
	}

	private void drawCandidatesBadge(GuiGraphics guiGraphics) {
		if (!hasCandidates()) {
			return;
		}
		Textures textures = Internal.getTextures();
		IDrawable badgeIcon = getTagKey()
			.map(tagKey -> textures.getTagBadgeIcon())
			.orElseGet(textures::getListBadgeIcon);
		int badgeX = this.rect.getX() + this.rect.getWidth() - badgeIcon.getWidth() + 1;
		int badgeY = this.rect.getY() - 1;
		badgeIcon.draw(guiGraphics, badgeX, badgeY);
	}

	@Override
	@SuppressWarnings("removal")
	public void drawHoverOverlays(GuiGraphics guiGraphics) {
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		drawHighlight(guiGraphics, screenHelper.getSlotHighlightFrontSprite());
	}

	private void drawHighlight(GuiGraphics guiGraphics, Identifier sprite) {
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

	private <T> void drawIngredient(GuiGraphics guiGraphics, ITypedIngredient<T> typedIngredient, int xPos, int yPos) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);

		SafeIngredientUtil.render(guiGraphics, ingredientRenderer, typedIngredient, xPos, yPos);
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated
	public List<Component> getTooltip() {
		JeiTooltip tooltip = new JeiTooltip();
		getDisplayedSlotIngredient()
			.ifPresent(ingredient -> tooltip.addAll(getLegacyTooltip(ingredient)));
		for (IRecipeSlotRichTooltipCallback tooltipCallback : tooltipCallbacks) {
			tooltipCallback.onRichTooltip(this, tooltip);
		}
		return tooltip.getLegacyComponents();
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated
	public void getTooltip(ITooltipBuilder tooltipBuilder) {
		addTooltip(tooltipBuilder);
	}

	@Override
	public void drawTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		JeiTooltip tooltip = new JeiTooltip();
		addTooltip(tooltip);
		tooltip.draw(guiGraphics, mouseX, mouseY);
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

	public void setDisplayOverridesChangedListener(Runnable listener) {
		this.displayOverridesChangedListener = listener;
	}

	private void onDisplayOverridesChanged() {
		invalidateTagKey();
		displayOverridesChangedListener.run();
	}

	private void invalidateTagKey() {
		this.tagKey.invalidate();
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
