package mezz.jei.test;

import com.google.common.collect.ImmutableTable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.common.Constants;
import mezz.jei.library.recipes.RecipeTransferManager;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class RecipeTransferManagerTest {
	private static final IRecipeType<String> RECIPE_TYPE = IRecipeType.create("jei", "transfer_manager_test", String.class);
	private static final IRecipeCategory<?> CATEGORY = (IRecipeCategory<?>) Proxy.newProxyInstance(
		IRecipeCategory.class.getClassLoader(),
		new Class<?>[]{IRecipeCategory.class},
		(proxy, method, args) -> {
			if (method.getName().equals("getRecipeType")) {
				return RECIPE_TYPE;
			}
			throw new AssertionError("Unexpected category method: " + method.getName());
		}
	);

	@BeforeAll
	static void setup() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		Bootstrap.bootStrap();
	}

	@Test
	void menuTypeRestrictionsAreCheckedPerInstance() {
		MenuType<TestMenu> expectedType = menuType(MenuType.GENERIC_9x3);
		TestHandler handler = new TestHandler(Optional.of(expectedType));
		RecipeTransferManager manager = manager(handler, null);

		assertTrue(manager.getRecipeTransferHandler(new TestMenu(null), CATEGORY).isEmpty());
		assertSame(handler, manager.getRecipeTransferHandler(new TestMenu(expectedType), CATEGORY).orElseThrow());
		assertTrue(manager.getRecipeTransferHandler(new TestMenu(MenuType.GENERIC_9x6), CATEGORY).isEmpty());
	}

	@Test
	void unrestrictedHandlerAcceptsUntypedMenus() {
		TestHandler handler = new TestHandler(Optional.empty());
		RecipeTransferManager manager = manager(handler, null);

		assertSame(handler, manager.getRecipeTransferHandler(new TestMenu(null), CATEGORY).orElseThrow());
	}

	@Test
	void universalHandlerIsUsedWhenSpecificHandlerRejectsMenuType() {
		MenuType<TestMenu> expectedType = menuType(MenuType.GENERIC_9x3);
		TestHandler specific = new TestHandler(Optional.of(expectedType));
		TestHandler universal = new TestHandler(Optional.empty());
		RecipeTransferManager manager = manager(specific, universal);

		assertSame(specific, manager.getRecipeTransferHandler(new TestMenu(expectedType), CATEGORY).orElseThrow());
		assertSame(universal, manager.getRecipeTransferHandler(new TestMenu(MenuType.GENERIC_9x6), CATEGORY).orElseThrow());
		assertSame(universal, manager.getRecipeTransferHandler(new TestMenu(null), CATEGORY).orElseThrow());
	}

	@Test
	void transferLookupDoesNotRetainUntypedMenus() throws InterruptedException {
		List<RecipeTransferManager> managers = List.of(
			new RecipeTransferManager(ImmutableTable.of(), List.of()),
			manager(new TestHandler(Optional.of(menuType(MenuType.GENERIC_9x3))), null)
		);
		List<WeakReference<TestMenu>> checkedMenus = new ArrayList<>();
		List<WeakReference<TestMenu>> controlMenus = new ArrayList<>();
		try {
			for (RecipeTransferManager manager : managers) {
				for (int i = 0; i < 5; i++) {
					checkedMenus.add(lookupMenu(manager));
					controlMenus.add(new WeakReference<>(new TestMenu(null)));
				}
			}
			for (int i = 0; i < 20 && (hasLiveMenu(checkedMenus) || hasLiveMenu(controlMenus)); i++) {
				System.gc();
				Thread.sleep(25);
			}
			assumeTrue(!hasLiveMenu(controlMenus), "This JVM did not collect the unreferenced control menus");
			assertTrue(!hasLiveMenu(checkedMenus), "Transfer lookup retained menus after their callers released them");
		} finally {
			Reference.reachabilityFence(managers);
		}
	}

	private static boolean hasLiveMenu(List<WeakReference<TestMenu>> menus) {
		return menus.stream().anyMatch(ref -> !ref.refersTo(null));
	}

	private static WeakReference<TestMenu> lookupMenu(RecipeTransferManager manager) {
		TestMenu menu = new TestMenu(null);
		manager.getRecipeTransferHandler(menu, CATEGORY);
		return new WeakReference<>(menu);
	}

	@SuppressWarnings("unchecked")
	private static MenuType<TestMenu> menuType(MenuType<?> menuType) {
		// TestMenu uses the type identity without calling its menu factory.
		return (MenuType<TestMenu>) menuType;
	}

	private static RecipeTransferManager manager(TestHandler specific, @Nullable TestHandler universal) {
		var handlers = ImmutableTable.<Class<? extends AbstractContainerMenu>, IRecipeType<?>, IRecipeTransferHandler<?, ?>>builder()
			.put(TestMenu.class, RECIPE_TYPE, specific);
		if (universal != null) {
			handlers.put(TestMenu.class, Constants.UNIVERSAL_RECIPE_TRANSFER_TYPE, universal);
		}
		return new RecipeTransferManager(handlers.build(), List.of());
	}

	private record TestHandler(Optional<MenuType<TestMenu>> menuType) implements IRecipeTransferHandler<TestMenu, String> {
		@Override
		public Class<TestMenu> getContainerClass() {
			return TestMenu.class;
		}

		@Override
		public Optional<MenuType<TestMenu>> getMenuType() {
			return menuType;
		}

		@Override
		public IRecipeType<String> getRecipeType() {
			return RECIPE_TYPE;
		}

		@Override
		@SuppressWarnings("removal")
		public IRecipeTransferError transferRecipe(TestMenu container, String recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
			throw new AssertionError("Looking up a handler must not transfer a recipe");
		}
	}

	private static class TestMenu extends AbstractContainerMenu {
		TestMenu(@Nullable MenuType<?> menuType) {
			super(menuType, 0);
		}

		@Override
		public ItemStack quickMoveStack(Player player, int slotIndex) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean stillValid(Player player) {
			return true;
		}
	}
}
