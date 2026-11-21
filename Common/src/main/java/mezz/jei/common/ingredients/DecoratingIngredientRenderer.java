package mezz.jei.common.ingredients;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.rendering.BatchRenderElement;
import mezz.jei.api.ingredients.rendering.IIngredientDecorator;
import mezz.jei.api.ingredients.rendering.IIngredientRenderContext;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("removal")
public final class DecoratingIngredientRenderer<T> implements IIngredientRenderer<T> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Set<IIngredientDecorator<?>> CRASHING_DECORATORS = new HashSet<>();
	private final IIngredientType<T> ingredientType;
	private final IIngredientRenderer<T> delegate;

	public static <T> IIngredientRenderer<T> create(IIngredientType<T> ingredientType, IIngredientRenderer<T> renderer) {
		// Keep a single wrapper so renderers passed back through GUI helpers do not draw decorators twice.
		if (renderer instanceof DecoratingIngredientRenderer<T> decoratingRenderer) {
			if (decoratingRenderer.ingredientType == ingredientType) {
				return renderer;
			}
			renderer = decoratingRenderer.delegate;
		}
		return new DecoratingIngredientRenderer<>(ingredientType, renderer);
	}

	private DecoratingIngredientRenderer(IIngredientType<T> ingredientType, IIngredientRenderer<T> delegate) {
		this.ingredientType = ingredientType;
		this.delegate = delegate;
	}

	@Override
	@Deprecated(since = "31.18.0", forRemoval = true)
	public void render(GuiGraphicsExtractor guiGraphics, T ingredient) {
		render(guiGraphics, ingredient, 0, 0, IIngredientRenderContext.INGREDIENT);
	}

	@Override
	@Deprecated(since = "31.18.0", forRemoval = true)
	public void render(GuiGraphicsExtractor guiGraphics, T ingredient, int posX, int posY) {
		render(guiGraphics, ingredient, posX, posY, IIngredientRenderContext.INGREDIENT);
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, T ingredient, int posX, int posY, IIngredientRenderContext context) {
		delegate.render(guiGraphics, ingredient, posX, posY, context);
		if (!context.renderDecorations()) {
			return;
		}
		for (var decorator : getDecorators()) {
			renderDecoration(guiGraphics, decorator, ingredient, posX, posY, context);
		}
	}

	@Override
	public void renderBatch(GuiGraphicsExtractor guiGraphics, List<BatchRenderElement<T>> elements, IIngredientRenderContext context) {
		delegate.renderBatch(guiGraphics, elements, context);
		if (!context.renderDecorations()) {
			return;
		}
		for (var decorator : getDecorators()) {
			for (var element : elements) {
				renderDecoration(guiGraphics, decorator, element.ingredient(), element.x(), element.y(), context);
			}
		}
	}

	private List<IIngredientDecorator<T>> getDecorators() {
		return Internal.getOptionalJeiRuntime()
			.map(IJeiRuntime::getIngredientManager)
			.map(i -> i.getIngredientDecorators(ingredientType))
			.orElseGet(List::of);
	}

	private void renderDecoration(GuiGraphicsExtractor guiGraphics, IIngredientDecorator<T> decorator, T ingredient, int x, int y, IIngredientRenderContext context) {
		if (CRASHING_DECORATORS.contains(decorator)) {
			return;
		}
		var pose = guiGraphics.pose();
		pose.pushMatrix();
		try {
			decorator.render(guiGraphics, ingredient, x, y, context);
		} catch (RuntimeException | LinkageError e) {
			CRASHING_DECORATORS.add(decorator);
			LOGGER.error("Caught an error rendering ingredient decorations with decorator: {}", decorator.getClass(), e);
		} finally {
			pose.popMatrix();
		}
	}

	@Override
	@Deprecated(since = "30.26.0", forRemoval = true)
	public List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag) {
		return delegate.getTooltip(ingredient, tooltipFlag);
	}

	@Override
	public List<Component> getTooltip(T ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
		return delegate.getTooltip(ingredient, tooltipContext, player, tooltipFlag);
	}

	@Override
	@Deprecated(since = "30.26.0", forRemoval = true)
	public void getTooltip(ITooltipBuilder tooltip, T ingredient, TooltipFlag tooltipFlag) {
		delegate.getTooltip(tooltip, ingredient, tooltipFlag);
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, T ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
		delegate.getTooltip(tooltip, ingredient, tooltipContext, player, tooltipFlag);
	}

	@Override
	public Font getFontRenderer(Minecraft minecraft, T ingredient) {
		return delegate.getFontRenderer(minecraft, ingredient);
	}

	@Override
	public int getWidth() {
		return delegate.getWidth();
	}

	@Override
	public int getHeight() {
		return delegate.getHeight();
	}
}
