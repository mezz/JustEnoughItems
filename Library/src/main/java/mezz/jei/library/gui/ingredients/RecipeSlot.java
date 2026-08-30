package mezz.jei.library.gui.ingredients;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IIngredientConsumer;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.RecipeSlotOptionsTooltipComponent;
import mezz.jei.common.gui.elements.OffsetDrawable;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.common.util.function.LazySupplier;
import mezz.jei.library.gui.recipes.layout.builder.LegacyTooltipCallbackAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipeSlot implements IRecipeSlotView, IRecipeSlotDrawable {
	private static final int SLOT_FOREGROUND_Z = 200;

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
		RecipeIngredientRole role,
		ImmutableRect2i rect,
		ICycler cycler,
		List<IRecipeSlotRichTooltipCallback> tooltipCallbacks,
		List<@Nullable ITypedIngredient<?>> allIngredients,
		@Nullable List<@Nullable ITypedIngredient<?>> focusedIngredients,
		@Nullable OffsetDrawable background,
		@Nullable IDrawable overlay,
		@Nullable String slotName,
		@Nullable RendererOverrides rendererOverrides
	) {
		this.ingredients = new RecipeSlotIngredients(
			allIngredients,
			focusedIngredients,
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
		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		if (!clientConfig.recipeSlotCyclingEnabled().getValue()) {
			return ingredients.getFirstDisplayedIngredient();
		}
		return ingredients.getDisplayedIngredient(cycler);
	}

	@Override
	public Stream<ITypedIngredient<?>> getDisplayedIngredients() {
		return ingredients.getDisplayedIngredients();
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
		GuiComponent.fill(poseStack, x, y, x + width, y + height, color);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}

	private <T> void addIngredientTooltip(ITooltipBuilder tooltip, ITypedIngredient<T> typedIngredient) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);
		List<ITypedIngredient<?>> visibleCandidates = getVisibleCandidates();

		SafeIngredientUtil.getRichTooltip(tooltip, ingredientManager, ingredientRenderer, typedIngredient);
		addTagNameTooltip(tooltip, ingredientManager, typedIngredient, visibleCandidates);
		addIngredientGridToTooltip(tooltip, ingredientManager, visibleCandidates);
		if (visibleCandidates.size() > 1) {
			var pauseRecipeCycling = Internal.getKeyMappings().getPauseRecipeCycling();
			tooltip.add(new RecipeSlotOptionsTooltipComponent(pauseRecipeCycling));
		}
	}

	private void addTooltip(ITooltipBuilder tooltip) {
		getDisplayedIngredient()
			.ifPresent(ingredient -> addIngredientTooltip(tooltip, ingredient));
		for (IRecipeSlotRichTooltipCallback tooltipCallback : tooltipCallbacks) {
			tooltipCallback.onRichTooltip(this, tooltip);
		}
	}

	@Deprecated
	private <T> List<Component> getLegacyTooltip(ITypedIngredient<T> typedIngredient) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);
		List<ITypedIngredient<?>> visibleCandidates = getVisibleCandidates();

		JeiTooltip tooltip = new JeiTooltip();
		SafeIngredientUtil.getRichTooltip(tooltip, ingredientManager, ingredientRenderer, typedIngredient);
		addTagNameTooltip(tooltip, ingredientManager, typedIngredient, visibleCandidates);

		return tooltip.getLegacyComponents();
	}

	private static <T> void addTagNameTooltip(
		ITooltipBuilder tooltip,
		IIngredientManager ingredientManager,
		ITypedIngredient<T> displayedIngredient,
		List<ITypedIngredient<?>> visibleCandidates
	) {
		if (visibleCandidates.isEmpty()) {
			return;
		}

		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		if (clientConfig.getHideSingleTagContentTooltipEnabled() && visibleCandidates.size() == 1) {
			return;
		}

		getTagKeyEquivalent(ingredientManager, visibleCandidates, displayedIngredient)
			.ifPresent(tagKeyEquivalent -> {
				String registryName = tagKeyEquivalent.registry().location().getPath()
					.replace('_', ' ');
				tooltip.add(
					Component.translatable("jei.tooltip.recipe.tag", StringUtils.capitalize(registryName))
						.withStyle(ChatFormatting.GRAY)
				);
				IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
				Component tagName = renderHelper.getName(tagKeyEquivalent);
				tooltip.add(tagName.copy().withStyle(ChatFormatting.GRAY));
			});
	}

	@Override
	public Optional<TagKey<?>> getTagKey() {
		return this.tagKey.get();
	}

	private Optional<TagKey<?>> calculateTagKey() {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		List<ITypedIngredient<?>> candidates = ingredients.hasDisplayOverrides() ?
			getDisplayedIngredients().toList() :
			ingredients.getAllIngredients().toList();
		return getTagKeyEquivalent(ingredientManager, candidates);
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
		List<T> values = allIngredients.stream()
			.map(ingredient -> ingredient.getIngredient(ingredientType))
			.flatMap(Optional::stream)
			.toList();
		if (values.size() != allIngredients.size()) {
			return Optional.empty();
		}
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		return ingredientHelper.getTagKeyEquivalent(values);
	}

	private static void addIngredientGridToTooltip(
		ITooltipBuilder tooltip,
		IIngredientManager ingredientManager,
		List<ITypedIngredient<?>> visibleCandidates
	) {
		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		if (clientConfig.isTagContentTooltipEnabled() && visibleCandidates.size() > 1) {
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

	private List<ITypedIngredient<?>> getVisibleCandidates() {
		return ingredients.getDisplayedIngredients()
			.toList();
	}

	private boolean hasCandidates() {
		return ingredients.getDisplayedIngredients()
			.limit(2)
			.count() > 1;
	}

	@SuppressWarnings("removal")
	@Override
	public void addTooltipCallback(mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback tooltipCallback) {
		this.tooltipCallbacks.add(new LegacyTooltipCallbackAdapter(tooltipCallback));
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
	@Deprecated(since = "11.20.0", forRemoval = true)
	public void draw(PoseStack poseStack) {
		draw(poseStack, false);
	}

	@Override
	public void draw(PoseStack poseStack, boolean hovered) {
		final int x = this.rect.getX();
		final int y = this.rect.getY();

		if (background != null) {
			background.draw(poseStack, x, y);
		}

		RenderSystem.enableBlend();

		Optional<ITypedIngredient<?>> displayedIngredient = getDisplayedIngredient();
		displayedIngredient.ifPresent(ingredient -> drawIngredient(poseStack, ingredient, x, y));

		if (overlay != null) {
			RenderSystem.enableBlend();

			poseStack.pushPose();
			{
				poseStack.translate(0, 0, SLOT_FOREGROUND_Z);
				overlay.draw(poseStack, x, y);
			}
			poseStack.popPose();
		}

		drawCandidatesBadge(poseStack);

		if (hovered) {
			drawHighlight(poseStack, 0x80FFFFFF);
		}

		RenderSystem.disableBlend();
	}

	private void drawCandidatesBadge(PoseStack poseStack) {
		if (!hasCandidates()) {
			return;
		}
		Textures textures = Internal.getTextures();
		IDrawable badgeIcon = getTagKey()
			.map(tagKey -> textures.getTagBadgeIcon())
			.orElseGet(textures::getListBadgeIcon);
		int badgeX = this.rect.getX() + this.rect.getWidth() - badgeIcon.getWidth() + 1;
		int badgeY = this.rect.getY() - 1;

		poseStack.pushPose();
		{
			poseStack.translate(0, 0, SLOT_FOREGROUND_Z);
			badgeIcon.draw(poseStack, badgeX, badgeY);
		}
		poseStack.popPose();
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
	@SuppressWarnings("removal")
	@Deprecated(since = "11.20.0", forRemoval = true)
	public void drawHoverOverlays(PoseStack poseStack) {
		drawHighlight(poseStack, 0x80FFFFFF);
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated
	public List<Component> getTooltip() {
		JeiTooltip tooltip = new JeiTooltip();
		getDisplayedIngredient()
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
	public void drawTooltip(PoseStack poseStack, int mouseX, int mouseY) {
		JeiTooltip tooltip = new JeiTooltip();
		addTooltip(tooltip);
		tooltip.draw(poseStack, mouseX, mouseY);
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
	public IIngredientConsumer createDisplayOverrides() {
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

	@SuppressWarnings("removal")
	@Override
	public Rect2i getRect() {
		return rect.toMutable();
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
