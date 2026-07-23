package mezz.jei.gui.overlay.elements;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public class GroupElementTooltipComponent implements ClientTooltipComponent, TooltipComponent {
	private static final int MAX_PER_LINE = 10;
	private static final int MAX_LINES = 3;
	private static final int MAX_INGREDIENTS = MAX_PER_LINE * MAX_LINES;
	private static final int INGREDIENT_SIZE = 18;
	private static final int INGREDIENT_PADDING = 1;

	private final List<? extends RenderElement<?>> elements;

	public GroupElementTooltipComponent(List<IElement> elements) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		this.elements = elements.stream()
								.map(e -> RenderElement.create(ingredientManager, e))
								.toList();
	}

	@Override
	public int getHeight(Font font) {
		return getLineCount() * INGREDIENT_SIZE + (2 * INGREDIENT_PADDING);
	}

	@Override
	public int getWidth(Font font) {
		return getMaxPerLine() * INGREDIENT_SIZE + (2 * INGREDIENT_PADDING);
	}

	@Override
	public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
		if (elements.size() <= MAX_INGREDIENTS) {
			drawIngredients(graphics, x, y, elements.size());
		} else {
			final int drawCount = MAX_INGREDIENTS - 1;
			drawIngredients(graphics, x, y, drawCount);
			final int remainingCount = Math.min(elements.size() - drawCount, 99);
			String countString = "+" + remainingCount;
			final int textHeight = font.lineHeight - 1;
			final int textWidth = font.width(countString);
			final int textCenterX = x + (MAX_PER_LINE - 1) * INGREDIENT_SIZE + ((INGREDIENT_SIZE - textWidth) / 2);
			final int textCenterY = y + (MAX_LINES - 1) * INGREDIENT_SIZE + ((INGREDIENT_SIZE - textHeight) / 2);
			graphics.text(font, countString, textCenterX, textCenterY, 0xFFAAAAAA);
		}
	}

	private void drawIngredients(GuiGraphicsExtractor guiGraphics, int x, int y, int maxIngredients) {
		final int maxPerLine = MathUtil.divideCeil(maxIngredients, getLineCount());

		for (int i = 0; i < elements.size() && i < maxIngredients; i++) {
			int column = i % maxPerLine;
			int row = i / maxPerLine;
			var poseStack = guiGraphics.pose();
			poseStack.pushMatrix();
			{
				poseStack.translate(
						x + column * INGREDIENT_SIZE + INGREDIENT_PADDING,
						y + row * INGREDIENT_SIZE + INGREDIENT_PADDING
				);
				drawIngredient(guiGraphics, elements.get(i));
			}
			poseStack.popMatrix();
		}
	}

	private static <T> void drawIngredient(GuiGraphicsExtractor guiGraphics, RenderElement<T> element) {
		element.renderer.render(guiGraphics, element.ingredient.getIngredient(), 0, 0);
	}

	private int getLineCount() {
		int lineCount = MathUtil.divideCeil(elements.size(), MAX_PER_LINE);
		return Math.min(lineCount, MAX_LINES);
	}

	private int getMaxPerLine() {
		int perLine = MathUtil.divideCeil(elements.size(), getLineCount());
		return Math.min(perLine, MAX_PER_LINE);
	}

	private record RenderElement<T>(IIngredientRenderer<T> renderer, ITypedIngredient<T> ingredient) {

		public static RenderElement<?> create(IIngredientManager ingredientManager, IElement element) {
			return doCreate(ingredientManager, element.getTypedIngredient());
		}

		private static <T> RenderElement<T> doCreate(IIngredientManager ingredientManager, ITypedIngredient<T> ingredient) {
			IIngredientRenderer<T> renderer = ingredientManager.getIngredientRenderer(ingredient.getType());
			return new RenderElement<>(renderer, ingredient);
		}
	}
}
