package mezz.jei.neoforge.tests.client;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.common.Internal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@JeiPlugin
public final class RecipeIngredientNavigationTest implements IModPlugin {
	private static final RecipeType<Integer> TYPE = RecipeType.create("jeiclienttests", "ingredient_navigation", Integer.class);
	private static int listenerPresses;
	private static int listenerReleases;
	private static int listenerScrolls;
	private static int buttonPresses;

	@Override
	public ResourceLocation getPluginUid() {
		return TYPE.getUid();
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new Category());
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(TYPE, List.of(0, 1));
	}

	static void run() {
		ClientTestUtil.runOnClient(client -> client.setScreen(new InventoryScreen(client.player)));
		ClientTestUtil.runOnClient(client -> {
			for (int recipe : List.of(0, 1)) {
				verifyIngredientLookup(client, recipe, GLFW.GLFW_MOUSE_BUTTON_LEFT, List.of(RecipeIngredientRole.OUTPUT));
				verifyIngredientLookup(client, recipe, GLFW.GLFW_MOUSE_BUTTON_RIGHT, List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CATALYST));
				verifyListenerClick(client, recipe, 100, 35, GLFW.GLFW_MOUSE_BUTTON_LEFT);
				verifyListenerClick(client, recipe, 92, 12, GLFW.GLFW_MOUSE_BUTTON_LEFT);
				verifyListenerClick(client, recipe, 12, 28, GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
				IRecipeLayoutDrawable<?> layout = openRecipe(recipe);
				click(client, layout, 100, 44, GLFW.GLFW_MOUSE_BUTTON_LEFT);
				check(buttonPresses == 1 && listenerPresses == 0 && listenerReleases == 0,
					"JEI button controls must run before plugin recipe callbacks");
				layout = openRecipe(recipe);
				var scrolled = new ScreenEvent.MouseScrolled.Pre(client.screen,
					layout.getRect().getX() + 100, layout.getRect().getY() + 35, 0, 1);
				NeoForge.EVENT_BUS.post(scrolled);
				check(scrolled.isCanceled() && listenerScrolls == 1, "Plugin recipe callbacks must receive scrolling after JEI controls");
			}
			client.screen.onClose();
		});
	}

	private static void verifyIngredientLookup(Minecraft client, int recipe, int button, List<RecipeIngredientRole> roles) {
		IRecipeLayoutDrawable<?> layout = openRecipe(recipe);
		click(client, layout, 12, 28, button);
		var focuses = Internal.getJeiRuntime().getGuiRuntime().getRecipesGui().getFocuses();
		check(!focuses.isEmpty(), "Clicking a fluid gauge must open an ingredient lookup despite its consuming listener");
		check(focuses.stream().map(focus -> focus.getRole()).toList().equals(roles) &&
			focuses.stream().allMatch(focus -> focus.getTypedValue().getIngredient(NeoForgeTypes.FLUID_STACK).orElseThrow().is(Fluids.WATER)),
			"The mouse button must select recipes or uses of the clicked fluid");
		check(listenerPresses == 0 && listenerReleases == 0, "Ingredient lookup must not trigger the overlapping recipe control");
	}

	private static void verifyListenerClick(Minecraft client, int recipe, int x, int y, int button) {
		IRecipeLayoutDrawable<?> layout = openRecipe(recipe);
		click(client, layout, x, y, button);
		check(listenerPresses == 1 && listenerReleases == 1, "Recipe controls must still receive clicks without an ingredient lookup");
		check(Internal.getJeiRuntime().getGuiRuntime().getRecipesGui().getFocuses().isEmpty(),
			"Recipe control clicks must not start ingredient lookups");
	}

	private static IRecipeLayoutDrawable<?> openRecipe(int recipe) {
		var runtime = Internal.getJeiRuntime();
		var gui = runtime.getGuiRuntime().getRecipesGui();
		gui.showRecipes(runtime.getRecipeManager().getRecipeCategory(TYPE), List.of(recipe), List.of());
		IRecipeLayoutDrawable<?> layout = gui.getVisibleRecipeLayouts().getFirst();
		layout.getWidgets();
		listenerPresses = 0;
		listenerReleases = 0;
		listenerScrolls = 0;
		buttonPresses = 0;
		return layout;
	}

	private static void click(Minecraft client, IRecipeLayoutDrawable<?> layout, int x, int y, int button) {
		double mouseX = layout.getRect().getX() + x;
		double mouseY = layout.getRect().getY() + y;
		var screen = client.screen;
		var pressed = new ScreenEvent.MouseButtonPressed.Pre(screen, mouseX, mouseY, button);
		NeoForge.EVENT_BUS.post(pressed);
		if (!pressed.isCanceled()) {
			screen.mouseClicked(mouseX, mouseY, button);
		}
		var released = new ScreenEvent.MouseButtonReleased.Pre(screen, mouseX, mouseY, button);
		NeoForge.EVENT_BUS.post(released);
		if (!released.isCanceled()) {
			screen.mouseReleased(mouseX, mouseY, button);
		}
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static final class Category implements IRecipeCategory<Integer> {
		@Override
		public RecipeType<Integer> getRecipeType() { return TYPE; }
		@Override
		public Component getTitle() { return Component.literal("Ingredient navigation test"); }
		@Override
		public @Nullable IDrawable getIcon() { return null; }
		@Override
		public int getWidth() { return 120; }
		@Override
		public int getHeight() { return 60; }

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, Integer recipe, IFocusGroup focuses) {
			builder.addSlot(RecipeIngredientRole.INPUT, 4, 4)
				.setFluidRenderer(1000, false, 16, 48).addFluidStack(Fluids.WATER, 1000);
			builder.addSlot(RecipeIngredientRole.OUTPUT, 60, 4)
				.setFluidRenderer(1000, false, 16, 48).addFluidStack(Fluids.WATER, 1000);
			builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 84, 4);
		}

		@Override
		public void createRecipeExtras(IRecipeExtrasBuilder builder, Integer recipe, IFocusGroup focuses) {
			builder.addButton(18, 16, () -> buttonPresses++).setPosition(90, 36);
			if (recipe == 1) {
				builder.addInputHandler(new IJeiInputHandler() {
					@Override
					public ScreenRectangle getArea() { return new ScreenRectangle(0, 0, getWidth(), getHeight()); }

					@Override
					public boolean handleInput(double mouseX, double mouseY, IJeiUserInput input) {
						if (input.isSimulate()) {
							listenerPresses++;
						} else {
							listenerReleases++;
						}
						return true;
					}

					@Override
					public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
						listenerScrolls++;
						return true;
					}
				});
				return;
			}
			builder.addGuiEventListener(new IJeiGuiEventListener() {
				@Override
				public ScreenRectangle getArea() { return new ScreenRectangle(0, 0, getWidth(), getHeight()); }

				@Override
				public boolean mouseClicked(double mouseX, double mouseY, int button) {
					listenerPresses++;
					return true;
				}

				@Override
				public boolean mouseReleased(double mouseX, double mouseY, int button) {
					listenerReleases++;
					return true;
				}

				@Override
				public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
					listenerScrolls++;
					return true;
				}
			});
		}
	}
}
