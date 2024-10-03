package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class IngredientsTooltipComponent implements ClientTooltipComponent, TooltipComponent {
	private static final int MAX_INGREDIENTS_PER_ROW = 16;
	private static final int INGREDIENT_SIZE = 18;
	private static final int INGREDIENT_PADDING = 1;
	private final List<RenderElement<?>> ingredients;

	public IngredientsTooltipComponent(IRecipeLayoutDrawable<?> layout) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		IRecipeSlotsView recipeSlotsView = layout.getRecipeSlotsView();
		Map<Object, SummaryElement<?>> summary = new HashMap<>();
		recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT)
			.stream()
			.map(IRecipeSlotView::getDisplayedIngredient)
			.filter(Optional::isPresent)
			.<ITypedIngredient<?>>map(Optional::get)
			.forEach(ingredient -> {
				addToSummary(ingredient, ingredientManager, summary);
			});

		Comparator<SummaryElement<?>> comparator = Comparator.comparingLong(SummaryElement::getAmount);
		this.ingredients = summary.values().stream()
			.sorted(comparator.reversed())
			.<RenderElement<?>>map(e -> RenderElement.create(e, ingredientManager))
			.toList();
	}

	private static <T> void addToSummary(ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager, Map<Object, SummaryElement<?>> summary) {
		IIngredientType<T> type = typedIngredient.getType();
		IIngredientHelper<T> helper = ingredientManager.getIngredientHelper(type);
		T ingredient = typedIngredient.getIngredient();
		long ingredientAmount = helper.getAmount(ingredient);
		if (ingredientAmount == -1) {
			return;
		}
		Object uid = getUid(typedIngredient, ingredientManager);
		summary.compute(uid, (k, v) -> {
			if (v == null) {
				return SummaryElement.create(typedIngredient, ingredientAmount);
			}
			long newAmount = v.getAmount() + ingredientAmount;
			v.setAmount(newAmount);
			return v;
		});
	}

	private static <T> Object getUid(ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager) {
		IIngredientType<T> type = typedIngredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(type);
		return ingredientHelper.getUid(typedIngredient, UidContext.Recipe);
	}

	@Override
	public int getHeight() {
		// Add 4 extra height so that there is some extra room below the rendered items.
		// They look too cramped if there is text right below them without some extra room.
		return 4 + INGREDIENT_SIZE * MathUtil.divideCeil(ingredients.size(), MAX_INGREDIENTS_PER_ROW);
	}

	@Override
	public int getWidth(Font font) {
		return INGREDIENT_SIZE * Math.min(ingredients.size(), MAX_INGREDIENTS_PER_ROW);
	}

	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
		for (int i = 0; i < ingredients.size(); i++) {
			int elementX = INGREDIENT_PADDING + x + ((i % MAX_INGREDIENTS_PER_ROW) * INGREDIENT_SIZE);
			int elementY = INGREDIENT_PADDING + y + ((i / MAX_INGREDIENTS_PER_ROW) * INGREDIENT_SIZE);
			RenderElement<?> renderElement = ingredients.get(i);
			PoseStack pose = guiGraphics.pose();
			pose.pushPose();
			{
				pose.translate(elementX, elementY, 0);
				renderElement.render(guiGraphics);
			}
			pose.popPose();
		}
	}

	private static class SummaryElement<T> {
		public static <T> SummaryElement<T> create(ITypedIngredient<T> ingredient, long amount) {
			return new SummaryElement<>(ingredient, amount);
		}

		private final ITypedIngredient<T> ingredient;
		private long amount;

		private SummaryElement(ITypedIngredient<T> ingredient, long amount) {
			this.ingredient = ingredient;
			this.amount = amount;
		}

		public ITypedIngredient<T> getIngredient() {
			return ingredient;
		}

		public long getAmount() {
			return amount;
		}

		public void setAmount(long amount) {
			this.amount = amount;
		}
	}

	private record RenderElement<T>(
		IIngredientRenderer<T> renderer,
		T ingredient
	) {
		public static <T> RenderElement<T> create(SummaryElement<T> summaryElement, IIngredientManager ingredientManager) {
			ITypedIngredient<T> typedIngredient = summaryElement.getIngredient();
			IIngredientType<T> type = typedIngredient.getType();
			IIngredientHelper<T> helper = ingredientManager.getIngredientHelper(type);
			IIngredientRenderer<T> renderer = ingredientManager.getIngredientRenderer(type);
			T ingredient = helper.copyWithAmount(typedIngredient.getIngredient(), summaryElement.getAmount());
			return new RenderElement<>(renderer, ingredient);
		}

		public void render(GuiGraphics guiGraphics) {
			renderer.render(guiGraphics, ingredient);
		}
	}
}
