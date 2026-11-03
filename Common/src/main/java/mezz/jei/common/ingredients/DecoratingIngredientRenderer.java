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
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

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
	@Deprecated(since = "11.81.0", forRemoval = true)
	public void render(PoseStack poseStack, T ingredient) {
		render(poseStack, ingredient, 0, 0, IIngredientRenderContext.INGREDIENT);
	}

	@Override
	@Deprecated(since = "11.81.0", forRemoval = true)
	public void render(PoseStack poseStack, T ingredient, int posX, int posY) {
		render(poseStack, ingredient, posX, posY, IIngredientRenderContext.INGREDIENT);
	}

	@Override
	public void render(PoseStack poseStack, T ingredient, int posX, int posY, IIngredientRenderContext context) {
		delegate.render(poseStack, ingredient, posX, posY, context);
		if (!context.renderDecorations()) {
			return;
		}
		for (var decorator : getDecorators()) {
			renderDecoration(poseStack, decorator, ingredient, posX, posY, context);
		}
	}

	@Override
	@Deprecated(since = "11.81.0", forRemoval = true)
	public void renderBatch(PoseStack poseStack, List<BatchRenderElement<T>> elements) {
		renderBatch(poseStack, elements, IIngredientRenderContext.INGREDIENT);
	}

	@Override
	public void renderBatch(PoseStack poseStack, List<BatchRenderElement<T>> elements, IIngredientRenderContext context) {
		delegate.renderBatch(poseStack, elements, context);
		if (!context.renderDecorations()) {
			return;
		}
		for (var decorator : getDecorators()) {
			for (var element : elements) {
				renderDecoration(poseStack, decorator, element.ingredient(), element.x(), element.y(), context);
			}
		}
	}

	private List<IIngredientDecorator<T>> getDecorators() {
		return Internal.getOptionalJeiRuntime()
			.map(IJeiRuntime::getIngredientManager)
			.map(i -> i.getIngredientDecorators(ingredientType))
			.orElseGet(List::of);
	}

	private void renderDecoration(PoseStack poseStack, IIngredientDecorator<T> decorator, T ingredient, int x, int y, IIngredientRenderContext context) {
		if (CRASHING_DECORATORS.contains(decorator)) {
			return;
		}
		var pose = poseStack;
		pose.pushPose();
		try {
			// Draw above item models so depth testing keeps decoration overlays visible.
			pose.translate(0, 0, 200);
			decorator.render(poseStack, ingredient, x, y, context);
		} catch (RuntimeException | LinkageError e) {
			CRASHING_DECORATORS.add(decorator);
			LOGGER.error("Caught an error rendering ingredient decorations with decorator: {}", decorator.getClass(), e);
		} finally {
			pose.popPose();
		}
	}

	@Override
	@Deprecated(since = "11.59.0", forRemoval = true)
	public List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag) {
		return delegate.getTooltip(ingredient, tooltipFlag);
	}

	@Override
	public List<Component> getTooltip(T ingredient, @Nullable Player player, TooltipFlag tooltipFlag) {
		return delegate.getTooltip(ingredient, player, tooltipFlag);
	}

	@Override
	@Deprecated(since = "11.59.0", forRemoval = true)
	public void getTooltip(ITooltipBuilder tooltip, T ingredient, TooltipFlag tooltipFlag) {
		delegate.getTooltip(tooltip, ingredient, tooltipFlag);
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, T ingredient, @Nullable Player player, TooltipFlag tooltipFlag) {
		delegate.getTooltip(tooltip, ingredient, player, tooltipFlag);
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
