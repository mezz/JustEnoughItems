package mezz.jei.library.gui.ingredients;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeIngredientsSource;
import mezz.jei.api.gui.ingredient.IRecipeHoverable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeTooltipAppender;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.Unmodifiable;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.SimpleIngredientAcceptor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Adapts an {@link IRecipeIngredientsSource} to a {@link IRecipeSlotDrawable} so that JEI can manage it
 * exactly like one of its own recipe slots (hover detection, tooltips, ingredient lookup, bookmarks, and focus).
 * JEI never draws the wrapped source, and its area is a zero-sized rectangle.
 */
public class IngredientsSourceAdapter implements IRecipeSlotDrawable {
	private final IRecipeIngredientsSource source;
	private final IIngredientManagerInternal ingredientManager;
	private final ContextMap contextMap;

	public IngredientsSourceAdapter(IRecipeIngredientsSource source, IIngredientManagerInternal ingredientManager, ContextMap contextMap) {
		this.source = source;
		this.ingredientManager = ingredientManager;
		this.contextMap = contextMap;
	}

	@Override
	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return source.getAllIngredients();
	}

	@Override
	@Unmodifiable
	public List<@Nullable ITypedIngredient<?>> getAllIngredientsList() {
		return source.getAllIngredients().toList();
	}

	@Override
	public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		return source.getDisplayedIngredient();
	}

	@Override
	public Stream<ITypedIngredient<?>> getDisplayedIngredients() {
		return source.getDisplayedIngredient().stream();
	}

	@Override
	public Optional<TagKey<?>> getTagKey() {
		return source.getTagKey();
	}

	@Override
	public RecipeIngredientRole getRole() {
		return source.getRole();
	}

	@Override
	public Optional<String> getSlotName() {
		return Optional.empty();
	}

	@Override
	public void drawHighlight(GuiGraphicsExtractor guiGraphics, int color) {
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (source instanceof IRecipeHoverable hoverable) {
			return hoverable.isMouseOver(mouseX, mouseY);
		}
		return false;
	}

	@Override
	@SuppressWarnings("removal")
	public void draw(GuiGraphicsExtractor guiGraphics) {
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, boolean hovered) {
	}

	@Override
	@SuppressWarnings("removal")
	public void drawHoverOverlays(GuiGraphicsExtractor guiGraphics) {
	}

	@Override
	@SuppressWarnings("removal")
	public List<Component> getTooltip() {
		JeiTooltip tooltip = new JeiTooltip();
		buildTooltip(tooltip);
		return tooltip.getLegacyComponents();
	}

	@Override
	@SuppressWarnings("removal")
	public void getTooltip(ITooltipBuilder tooltipBuilder) {
		buildTooltip(tooltipBuilder);
	}

	@Override
	public void drawTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		JeiTooltip tooltip = new JeiTooltip();
		buildTooltip(tooltip);
		tooltip.draw(guiGraphics, mouseX, mouseY);
	}

	@Override
	public void setPosition(int x, int y) {
	}

	@Override
	public IIngredientAcceptor<?> createDisplayOverrides() {
		return new SimpleIngredientAcceptor(ingredientManager, contextMap, source.getRole());
	}

	@Override
	public void clearDisplayOverrides() {
	}

	@Override
	public Rect2i getAreaIncludingBackground() {
		return new Rect2i(0, 0, 0, 0);
	}

	private void buildTooltip(ITooltipBuilder tooltip) {
		source.getDisplayedIngredient()
			.ifPresent(typedIngredient -> addRichTooltip(tooltip, ingredientManager, typedIngredient));
		addTagTooltip(tooltip);
		if (source instanceof IRecipeTooltipAppender appender) {
			appender.addTooltip(tooltip);
		}
	}

	private <T> void addRichTooltip(ITooltipBuilder tooltip, IIngredientManager ingredientManager, ITypedIngredient<T> typedIngredient) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		SafeIngredientUtil.getRichTooltip(tooltip, ingredientManager, ingredientRenderer, typedIngredient);
	}

	private void addTagTooltip(ITooltipBuilder tooltip) {
		Optional<TagKey<?>> tagKey = source.getTagKey();
		if (tagKey.isEmpty()) {
			return;
		}
		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		List<ITypedIngredient<?>> allIngredients = source.getAllIngredients().toList();
		if (clientConfig.hideSingleTagContentTooltipEnabled().getValue() && allIngredients.size() == 1) {
			return;
		}
		TagKey<?> key = tagKey.get();
		String registryName = key.registry().identifier().getPath()
			.replace('_', ' ');
		tooltip.add(
			Component.translatable("jei.tooltip.recipe.tag", StringUtils.capitalize(registryName))
				.withStyle(ChatFormatting.GRAY)
		);
		Component tagName = Services.PLATFORM.getRenderHelper().getName(key);
		tooltip.add(tagName.copy().withStyle(ChatFormatting.GRAY));
	}
}
