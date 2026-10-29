package mezz.jei.fabric.test;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.elements.IconButton;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.plugins.vanilla.cooking.FurnaceSmeltingCategory;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmeltingRecipe;

@SuppressWarnings("UnstableApiUsage")
final class ExtractorRenderingClientTest {
	private ExtractorRenderingClientTest() {
	}

	@SuppressWarnings("deprecation")
	static void run(ClientGameTestContext context) {
		context.runOnClient(client -> {
			var runtime = Internal.getJeiRuntime();
			var category = new TrackingCategory(runtime.getJeiHelpers().getGuiHelper());
			var recipe = runtime.getRecipeManager().createRecipeLookup(RecipeTypes.SMELTING).get().findFirst().orElseThrow();
			var layout = runtime.getRecipeManager().createRecipeLayoutDrawable(category, recipe, FocusGroup.EMPTY).orElseThrow();
			var graphics = new GuiGraphicsExtractor(client, new GuiRenderState(), 53, 67);
			layout.setPosition(40, 50);
			layout.drawRecipe(graphics);
			check(category.mouseX == 13 && category.mouseY == 17, "Category coordinates must remain relative to the layout");
			check(category.widgetMouseX == 10 && category.widgetMouseY == 13, "Widget coordinates must remain relative to the widget");

			layout.drawRecipe(graphics, 60, 80);
			check(category.mouseX == 20 && category.mouseY == 30, "The legacy overload must honor its explicit mouse position");

			layout.setPosition(-20, -20);
			graphics.extractWithoutMouse(() -> layout.drawRecipe(graphics));
			check(category.mouseX < 0 && category.widgetMouseX < 0, "Mouse suppression must survive layouts positioned outside the viewport");

			int[] calls = {0};
			var button = new IconButton(new IIconButtonController() {
				@Override
				public boolean onPress(IJeiUserInput input) {
					return false;
				}

				@Override
				@Deprecated(since = "32.1.0")
				public void drawExtras(GuiGraphicsExtractor extractor, Rect2i area, int mouseX, int mouseY, float partialTicks) {
					check(mouseX == extractor.mouseX() && mouseY == extractor.mouseY(), "Legacy callbacks must receive screen coordinates");
					check(partialTicks == extractor.deltaTracker().getGameTimeDeltaPartialTick(false), "Legacy callbacks must retain the interpolation fraction");
					calls[0]++;
				}
			}, new ImmutableRect2i(40, 50, 20, 20));
			button.draw(graphics);
			graphics.extractWithoutMouse(() -> button.draw(graphics));
			check(calls[0] == 2, "Extractor-based button rendering must still invoke legacy addon callbacks");
		});
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static final class TrackingCategory extends FurnaceSmeltingCategory {
		private double mouseX;
		private double mouseY;
		private double widgetMouseX;
		private double widgetMouseY;

		private TrackingCategory(IGuiHelper guiHelper) {
			super(guiHelper);
		}

		@Override
		public void draw(RecipeHolder<SmeltingRecipe> recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
			this.mouseX = mouseX;
			this.mouseY = mouseY;
		}

		@Override
		public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<SmeltingRecipe> recipe, IFocusGroup focuses) {
			super.createRecipeExtras(builder, recipe, focuses);
			builder.addWidget(new IRecipeWidget() {
				@Override
				public ScreenPosition getPosition() {
					return new ScreenPosition(3, 4);
				}

				@Override
				public void drawWidget(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
					widgetMouseX = mouseX;
					widgetMouseY = mouseY;
				}
			});
		}
	}
}
