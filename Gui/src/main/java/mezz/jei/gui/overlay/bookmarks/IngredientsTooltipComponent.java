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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class IngredientsTooltipComponent implements ClientTooltipComponent {
	private final List<RenderElement<?>> ingredients;

	public IngredientsTooltipComponent(IRecipeLayoutDrawable<?> layout) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		IRecipeSlotsView recipeSlotsView = layout.getRecipeSlotsView();
		Map<String, SummaryElement<?>> summary = new HashMap<>();
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

	private static <T> void addToSummary(ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager, Map<String, SummaryElement<?>> summary) {
		IIngredientType<T> type = typedIngredient.getType();
		IIngredientHelper<T> helper = ingredientManager.getIngredientHelper(type);
		T ingredient = typedIngredient.getIngredient();
		long ingredientAmount = helper.getAmount(ingredient);
		if (ingredientAmount == -1) {
			return;
		}
		String uid = getUid(typedIngredient, ingredientManager);
		summary.compute(uid, (k, v) -> {
			if (v == null) {
				return SummaryElement.create(typedIngredient, ingredientAmount);
			}
			long newAmount = v.getAmount() + ingredientAmount;
			v.setAmount(newAmount);
			return v;
		});
	}

	private static <T> String getUid(ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
		return ingredientHelper.getUniqueId(typedIngredient.getIngredient(), UidContext.Recipe);
	}

	@Override
	public int getHeight() {
		return 16;
	}

	@Override
	public int getWidth(Font font) {
		return ingredients.size() * 16;
	}

	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
		for (int i = 0; i < ingredients.size(); i++) {
			RenderElement<?> renderElement = ingredients.get(i);
			PoseStack pose = guiGraphics.pose();
			pose.pushPose();
			{
				pose.translate(x + i * 16, y, 0);
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

	private static class RenderElement<T> {
		public static <T> RenderElement<T> create(SummaryElement<T> summaryElement, IIngredientManager ingredientManager) {
			ITypedIngredient<T> typedIngredient = summaryElement.getIngredient();
			IIngredientType<T> type = typedIngredient.getType();
			IIngredientHelper<T> helper = ingredientManager.getIngredientHelper(type);
			IIngredientRenderer<T> renderer = ingredientManager.getIngredientRenderer(type);
			T ingredient = helper.setAmount(typedIngredient.getIngredient(), summaryElement.getAmount());
			return new RenderElement<>(renderer, ingredient);
		}

		private final IIngredientRenderer<T> renderer;
		private final T ingredient;

		private RenderElement(IIngredientRenderer<T> renderer, T ingredient) {
			this.renderer = renderer;
			this.ingredient = ingredient;
		}

		public void render(GuiGraphics guiGraphics) {
			renderer.render(guiGraphics, ingredient);
		}
	}
}
