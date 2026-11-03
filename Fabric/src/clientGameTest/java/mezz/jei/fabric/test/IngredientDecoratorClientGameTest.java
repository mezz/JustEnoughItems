package mezz.jei.fabric.test;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.rendering.BatchRenderElement;
import mezz.jei.api.ingredients.rendering.IIngredientDecorator;
import mezz.jei.api.ingredients.rendering.IIngredientRenderContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.IngredientTooltipComponent;
import mezz.jei.common.gui.elements.DrawableCombined;
import mezz.jei.common.gui.elements.DrawableRecipeWidget;
import mezz.jei.common.gui.elements.HighResolutionDrawable;
import mezz.jei.common.gui.elements.OffsetDrawable;
import mezz.jei.common.gui.elements.RecipeButtonWidget;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.config.sorting.SortingOrderConfigValues;
import mezz.jei.gui.ghost.GhostIngredientDrag;
import mezz.jei.gui.ghost.GhostIngredientReturning;
import mezz.jei.gui.overlay.elements.IngredientElement;
import mezz.jei.gui.overlay.ingredients.IngredientListRenderer;
import mezz.jei.gui.overlay.ingredients.IngredientListSlot;
import mezz.jei.library.gui.ingredients.TagContentTooltipComponent;
import mezz.jei.library.render.ItemStackRenderer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Exercises registered decorators through real ingredient rendering and GUI draws. */
@SuppressWarnings("UnstableApiUsage")
public class IngredientDecoratorClientGameTest implements IModPlugin {
	private static final List<IIngredientRenderContext> CONTEXTS = List.of(
		IIngredientRenderContext.INGREDIENT,
		IIngredientRenderContext.ICON
	);
	private static final List<Decoration> DECORATIONS = new ArrayList<>();
	private static boolean recording;

	@Override
	public ResourceLocation getPluginUid() {
		return ResourceLocation.fromNamespaceAndPath("jei_test", "ingredient_decorators");
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addIngredientDecorator(VanillaTypes.ITEM_STACK, decorator(0));
		registration.addIngredientDecorator(VanillaTypes.ITEM_STACK, decorator(1));
	}

	private static IIngredientDecorator<ItemStack> decorator(int id) {
		return (graphics, stack, x, y, context) -> {
			if (recording) {
				DECORATIONS.add(new Decoration(id, new BatchRenderElement<>(stack, x, y), context));
			}
		};
	}

	static FabricClientTestRunner.ClientTestCase getTestCase() {
		return new FabricClientTestRunner.ClientTestCase("fabric-client-rendering", "ingredientDecorators", () -> {
			try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
				var context = new FabricClientTestContext();
				context.waitFor(client -> Internal.getOptionalJeiRuntime().isPresent());
				boolean[] rendered = {false};
				context.runOnClient(client -> {
					var screen = new InventoryScreen(Objects.requireNonNull(client.player));
					client.setScreen(screen);
					ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, delta) -> {
						if (!rendered[0]) {
							checkRendering(graphics);
							rendered[0] = true;
						}
					});
				});
				context.waitFor(client -> rendered[0]);
			}
		});
	}

	@SuppressWarnings("removal")
	private static void checkRendering(GuiGraphics graphics) {
		var runtime = Internal.getJeiRuntime();
		var manager = runtime.getIngredientManager();
		var ingredient = manager.createTypedIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.STICK), false).orElseThrow();
		var secondIngredient = manager.createTypedIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.STONE), false).orElseThrow();
		DECORATIONS.clear();
		recording = true;
		try {
			var renderer = new IngredientListRenderer(manager, false);
			renderer.add(new IngredientListSlot(20, 40, 18, 18, 1));
			var blocked = new IngredientListSlot(40, 40, 18, 18, 1);
			blocked.setBlocked(true);
			renderer.add(blocked);
			renderer.add(new IngredientListSlot(60, 40, 18, 18, 1));
			renderer.add(new IngredientListSlot(100, 40, 18, 18, 1));
			renderer.set(0, List.of(new IngredientElement<>(ingredient), new IngredientElement<>(secondIngredient)));
			renderer.render(graphics);
			var expectedElement = new BatchRenderElement<>(ingredient.getIngredient(), 21, 41);
			var secondExpectedElement = new BatchRenderElement<>(secondIngredient.getIngredient(), 61, 41);
			assertDecorations(List.of(expectedElement, secondExpectedElement), IIngredientRenderContext.INGREDIENT);
			var guiHelper = runtime.getJeiHelpers().getGuiHelper();
			var drawable = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, ingredient.getIngredient());
			drawable.draw(graphics, 80, 40);
			assertDecorations(ingredient.getIngredient(), IIngredientRenderContext.INGREDIENT, 80, 40);
			var offsetDrawable = new OffsetDrawable(new DrawableCombined(drawable), 2, 3);
			for (var context : CONTEXTS) {
				offsetDrawable.draw(graphics, 80, 40, context);
				assertDecorations(ingredient.getIngredient(), context, 82, 43);
				new HighResolutionDrawable(offsetDrawable, 2).draw(graphics, 80, 40, context);
				assertDecorations(ingredient.getIngredient(), context, 2, 3);
			}
			drawable.draw(graphics, 80, 40);
			assertDecorations(ingredient.getIngredient(), IIngredientRenderContext.INGREDIENT, 80, 40);

			new DrawableRecipeWidget(drawable).drawWidget(graphics, 0, 0);
			assertDecorations(ingredient.getIngredient(), IIngredientRenderContext.ICON, 0, 0);
			new RecipeButtonWidget(20, 20, () -> {}).setIcon(drawable).drawWidget(graphics, 0, 0);
			assertDecorations(ingredient.getIngredient(), IIngredientRenderContext.ICON, 0, 0);

			var sortingValues = new SortingOrderConfigValues(runtime);
			sortingValues.getIngredientTypeSortOrderValueIcon(ItemStack.class.getName()).orElseThrow()
				.draw(graphics, new Rect2i(80, 40, 16, 16));
			assertDecorations(new ItemStack(Items.DIAMOND_PICKAXE), IIngredientRenderContext.ICON, 80, 40);

			for (boolean overlay : List.of(false, true)) {
				var decorationSlot = guiHelper.createRecipeSlotDrawable(RecipeIngredientRole.INPUT, acceptor -> {
					var builder = (IRecipeSlotBuilder) acceptor;
					if (overlay) {
						builder.setOverlay(drawable, 2, 3);
					} else {
						builder.setBackground(drawable, 2, 3);
					}
				}, Set.of(), 0);
				decorationSlot.setPosition(80, 40);
				decorationSlot.draw(graphics, false);
				assertDecorations(ingredient.getIngredient(), IIngredientRenderContext.ICON, 82, 43);
			}

			var ingredientRenderer = manager.getIngredientRenderer(VanillaTypes.ITEM_STACK);
			guiHelper.createDrawableIngredient(ingredientRenderer, ingredient.getIngredient()).draw(graphics, 100, 40);
			assertDecorations(ingredient.getIngredient(), IIngredientRenderContext.INGREDIENT, 100, 40);
			guiHelper.createDrawableIngredient(ingredientRenderer, ingredient.getIngredient())
				.draw(graphics, 100, 40, IIngredientRenderContext.ICON);
			assertDecorations(ingredient.getIngredient(), IIngredientRenderContext.ICON, 100, 40);

			guiHelper.createDrawableIngredient(new ItemStackRenderer(), ingredient.getIngredient()).draw(graphics, 120, 40);
			assertDecorations(ingredient.getIngredient(), IIngredientRenderContext.INGREDIENT, 120, 40);

			ingredientRenderer.render(graphics, ingredient.getIngredient(), 140, 40, IIngredientRenderContext.INGREDIENT);
			assertDecorations(ingredient.getIngredient(), IIngredientRenderContext.INGREDIENT, 140, 40);
			var batch = List.of(
				new BatchRenderElement<>(ingredient.getIngredient(), 160, 40),
				new BatchRenderElement<>(secondIngredient.getIngredient(), 180, 40)
			);
			ingredientRenderer.renderBatch(graphics, batch, IIngredientRenderContext.INGREDIENT);
			assertDecorations(batch, IIngredientRenderContext.INGREDIENT);

			for (var context : CONTEXTS) {
				ingredientRenderer.render(graphics, ingredient.getIngredient(), 220, 40, context);
				assertDecorations(ingredient.getIngredient(), context, 220, 40);
				ingredientRenderer.renderBatch(graphics, batch, context);
				assertDecorations(batch, context);
			}
			checkLegacyRendering(graphics, ingredientRenderer, false);
			checkLegacyRendering(graphics, ingredientRenderer, true);
			checkCustomRenderer(graphics);

			var slot = guiHelper.createRecipeSlotDrawable(
				RecipeIngredientRole.INPUT,
				acceptor -> {
					acceptor.addTypedIngredient(ingredient);
					((IRecipeSlotBuilder) acceptor).setCustomRenderer(VanillaTypes.ITEM_STACK, new ItemStackRenderer());
				},
				Set.of(),
				0
			);
			slot.setPosition(20, 70);
			var pose = graphics.pose();
			pose.pushPose();
			try {
				pose.translate(3, 5, 0);
				slot.draw(graphics, false);
				assertDecorations(ingredient.getIngredient(), IIngredientRenderContext.INGREDIENT, 20, 70);
			} finally {
				pose.popPose();
			}

			var font = Minecraft.getInstance().font;
			var tooltip = new IngredientTooltipComponent<>(ingredient, ingredientRenderer);
			tooltip.renderImage(font, 40, 70, graphics);
			assertDecorations(ingredient.getIngredient(), IIngredientRenderContext.INGREDIENT, 41, 71);

			var gridTooltip = new TagContentTooltipComponent(manager, List.of(ingredient));
			gridTooltip.renderImage(font, 60, 70, graphics);
			assertDecorations(ingredient.getIngredient(), IIngredientRenderContext.INGREDIENT, 65, 75);

			var drag = new GhostIngredientDrag<>(List.of(), ingredientRenderer, ingredient, 20, 40, new ImmutableRect2i(20, 40, 16, 16));
			var returning = GhostIngredientReturning.create(drag, 160, 80).orElseThrow();
			returning.drawItem(graphics);
			assertDecorations(ingredient.getIngredient(), IIngredientRenderContext.INGREDIENT, 0, 0);
		} finally {
			recording = false;
			DECORATIONS.clear();
		}
	}

	private static void assertDecorations(ItemStack ingredient, IIngredientRenderContext context, int x, int y) {
		var element = new BatchRenderElement<>(ingredient, x, y);
		assertDecorations(List.of(element), context);
	}

	private static void assertDecorations(List<BatchRenderElement<ItemStack>> elements, IIngredientRenderContext context) {
		List<Decoration> expected = new ArrayList<>();
		if (context.renderDecorations()) {
			for (int id = 0; id < 2; id++) {
				for (var element : elements) {
					expected.add(new Decoration(id, element, context));
				}
			}
		}
		boolean matches = DECORATIONS.size() == expected.size();
		for (int i = 0; matches && i < expected.size(); i++) {
			var actual = DECORATIONS.get(i);
			var wanted = expected.get(i);
			matches = actual.id() == wanted.id() && actual.context() == wanted.context() &&
				actual.element().x() == wanted.element().x() && actual.element().y() == wanted.element().y() &&
				ItemStack.matches(actual.element().ingredient(), wanted.element().ingredient());
		}
		if (!matches) {
			throw new AssertionError("Unexpected ingredient decorations: expected " + expected + ", got " + DECORATIONS);
		}
		DECORATIONS.clear();
	}

	@SuppressWarnings("removal")
	private static void checkLegacyRendering(GuiGraphics graphics, IIngredientRenderer<ItemStack> renderer, boolean batch) {
		var counted = new ItemStack(Items.STICK, 32);
		var damaged = new ItemStack(Items.DIAMOND_PICKAXE);
		damaged.setDamageValue(100);
		var elements = List.of(new BatchRenderElement<>(counted, 20, 40), new BatchRenderElement<>(damaged, 60, 40));
		if (batch) {
			renderer.renderBatch(graphics, elements);
			assertDecorations(elements, IIngredientRenderContext.INGREDIENT);
		} else {
			renderer.render(graphics, counted);
			assertDecorations(counted, IIngredientRenderContext.INGREDIENT, 0, 0);
			renderer.render(graphics, damaged, 60, 40);
			assertDecorations(damaged, IIngredientRenderContext.INGREDIENT, 60, 40);
		}
	}

	@SuppressWarnings("removal")
	private static void checkCustomRenderer(GuiGraphics graphics) {
		var itemRenderer = new ItemStackRenderer();
		IIngredientRenderer<ItemStack> customRenderer = new IIngredientRenderer<>() {
			@Override
			public void render(GuiGraphics graphics, ItemStack ingredient) {
				graphics.renderFakeItem(ingredient, 0, 0);
			}

			@Override
			public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
				return itemRenderer.getTooltip(ingredient, tooltipFlag);
			}
		};
		var counted = new ItemStack(Items.STICK, 32);
		var damaged = new ItemStack(Items.DIAMOND_PICKAXE);
		damaged.setDamageValue(100);
		var guiHelper = Internal.getJeiRuntime().getJeiHelpers().getGuiHelper();
		guiHelper.createDrawableIngredient(customRenderer, counted).draw(graphics, 20, 40);
		assertDecorations(counted, IIngredientRenderContext.INGREDIENT, 20, 40);
		guiHelper.createDrawableIngredient(customRenderer, damaged).draw(graphics, 60, 40);
		assertDecorations(damaged, IIngredientRenderContext.INGREDIENT, 60, 40);
	}

	private record Decoration(int id, BatchRenderElement<ItemStack> element, IIngredientRenderContext context) {}
}
