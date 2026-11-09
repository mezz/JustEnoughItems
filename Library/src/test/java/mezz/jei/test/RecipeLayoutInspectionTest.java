package mezz.jei.test;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.gui.ingredients.CycleTicker;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import mezz.jei.library.gui.recipes.RecipeLayout;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeLayoutInspectionTest {
	@Test
	void inspectionCreatesExtrasOnceAndCopiesTheWidgetList() {
		Category category = new Category();
		RecipeLayout<String> layout = layout(category, List.of(), List.of());
		List<IRecipeWidget> widgets = layout.getWidgets();
		assertEquals(1, category.extrasCreated);
		assertEquals(1, widgets.size());
		assertThrows(UnsupportedOperationException.class, widgets::clear);
		layout.getWidgets();
		layout.getTooltip(new JeiTooltip(), 1, 1);
		assertEquals(1, category.extrasCreated);
		layout.addWidget(() -> new ScreenPosition(0, 0));
		assertEquals(1, widgets.size());
		assertEquals(2, layout.getWidgets().size());
	}

	@Test
	void tooltipQueryUsesScreenCoordinatesAndPreservesRichContent() {
		TooltipComponent image = new TooltipComponent() {};
		IRecipeCategoryDecorator<String> decorator = new IRecipeCategoryDecorator<>() {
			@Override
			public void decorateTooltips(ITooltipBuilder tooltip, String recipe, IRecipeCategory<String> category,
				IRecipeSlotsView slots, double mouseX, double mouseY) {
				tooltip.add(image);
			}
		};
		RecipeLayout<String> layout = layout(new Category(), List.of(decorator), List.of());
		layout.setPosition(100, 200);
		JeiTooltip tooltip = new JeiTooltip();
		layout.getTooltip(tooltip, 112.5, 215.5);
		assertEquals("category 12.5,15.5", tooltip.getLines().get(0).left().orElseThrow().getString());
		assertEquals(image, tooltip.getLines().get(1).right().orElseThrow());
		assertEquals("widget 2.5,5.5", tooltip.getLines().get(2).left().orElseThrow().getString());

		tooltip.clear();
		layout.getTooltip(tooltip, 99.99, 215);
		assertTrue(tooltip.getLines().isEmpty());
		layout.getTooltip(tooltip, 140, 215);
		assertTrue(tooltip.getLines().isEmpty());
	}

	private static RecipeLayout<String> layout(Category category, List<IRecipeCategoryDecorator<String>> decorators, List<RecipeSlot> slots) {
		return new RecipeLayout<>(category, decorators, "recipe", List.of(),
			(graphics, x, y, width, height) -> { throw new AssertionError("Inspection must not draw"); },
			0, null, new ImmutablePoint2i(0, 0), slots, CycleTicker.createWithRandomOffset(), FocusGroup.EMPTY);
	}

	private static class Category implements IRecipeCategory<String> {
		private int extrasCreated;

		@Override
		public IRecipeType<String> getRecipeType() {
			return IRecipeType.create("test", "inspection", String.class);
		}

		@Override
		public Component getTitle() {
			return Component.literal("Inspection");
		}

		@Override
		public @Nullable IDrawable getIcon() {
			return null;
		}

		@Override
		public int getWidth() {
			return 40;
		}

		@Override
		public int getHeight() {
			return 40;
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, String recipe, IFocusGroup focuses) {
		}

		@Override
		public void createRecipeExtras(IRecipeExtrasBuilder builder, String recipe, IFocusGroup focuses) {
			extrasCreated++;
			builder.addWidget(new IRecipeWidget() {
				@Override
				public ScreenPosition getPosition() {
					return new ScreenPosition(10, 10);
				}

				@Override
				public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
					if (mouseX >= 0 && mouseY >= 0) {
						tooltip.add(FormattedText.of("widget " + mouseX + "," + mouseY));
					}
				}
			});
		}

		@Override
		public void getTooltip(ITooltipBuilder tooltip, String recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
			tooltip.add(FormattedText.of("category " + mouseX + "," + mouseY));
		}
	}
}
