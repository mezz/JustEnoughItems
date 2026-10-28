package mezz.jei.fabric.test;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferListener;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.common.Internal;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings({"UnstableApiUsage", "removal"})
public class RecipeTransferCompletionClientTest implements FabricClientGameTest, IModPlugin {
	private static final IRecipeType<Mode> SPECIFIC = IRecipeType.create("jei_test", "transfer_specific", Mode.class);
	private static final IRecipeType<Mode> UNIVERSAL = IRecipeType.create("jei_test", "transfer_universal", Mode.class);
	private static final IRecipeType<Mode> LEGACY = IRecipeType.create("jei_test", "transfer_legacy", Mode.class);
	private static final Map<IRecipeType<?>, Attempt> LAST_ATTEMPTS = new HashMap<>();
	private static final Set<IRecipeTransferContext<?, ?>> STARTED = Collections.newSetFromMap(new IdentityHashMap<>());
	private static final Map<IRecipeTransferContext<?, ?>, RecipeTransferResult> COMPLETED = new IdentityHashMap<>();
	private static @Nullable IRecipeTransferContext<?, ?> lastStarted;
	private static @Nullable IRecipeTransferHandlerHelper transferHelper;
	private static final IRecipeTransferError BLOCKING_ERROR = () -> IRecipeTransferError.Type.INTERNAL;
	private static final IRecipeTransferError COSMETIC_ERROR = () -> IRecipeTransferError.Type.COSMETIC;

	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath("jei_test", "transfer_completion");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new Category(SPECIFIC), new Category(UNIVERSAL), new Category(LEGACY));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		for (var type : List.of(SPECIFIC, UNIVERSAL, LEGACY)) {
			registration.addRecipes(type, List.of(Mode.values()));
		}
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		transferHelper = registration.getTransferHelper();
		registration.addRecipeTransferHandlerWithResults(new Handler(SPECIFIC), SPECIFIC);
		registration.addUniversalRecipeTransferHandlerWithResults(new UniversalHandler());
		registration.addRecipeTransferHandler(new Handler(LEGACY), LEGACY);
		registration.addRecipeTransferListener(new IRecipeTransferListener() {
			@Override
			public void beforeRecipeTransfer(IRecipeTransferContext<?, ?> context) {
				if (context.getRecipe() instanceof Mode) {
					STARTED.add(context);
					lastStarted = context;
				}
			}

			@Override
			public void afterRecipeTransfer(IRecipeTransferContext<?, ?> context, RecipeTransferResult result) {
				if (context.getRecipe() instanceof Mode) {
					check(STARTED.contains(context), "Completion must retain the original transfer context");
					check(COMPLETED.putIfAbsent(context, result) == null, "A transfer must complete only once");
				}
			}
		});
	}

	@Override
	public void onRuntimeUnavailable() {
		transferHelper = null;
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReport("fabric-client-gametest", getClass().getSimpleName(), () -> {
			try (TestSingleplayerContext ignored = context.worldBuilder().create()) {
				JeiFabricClientGameTestAssertions.assertJeiStartedWithSyncedRecipes(context);
				context.runOnClient(client -> {
					STARTED.clear();
					COMPLETED.clear();
					Player player = Objects.requireNonNull(client.player);
					var screen = new InventoryScreen(client.player);
					var service = new RecipeTransferService(Internal.getJeiRuntime().getRecipeTransferManager());
					checkCompletion(service, screen, player, SPECIFIC);
					checkCompletion(service, screen, player, UNIVERSAL);
					Attempt legacy = transfer(service, screen, player, LEGACY, Mode.IMMEDIATE);
					check(COMPLETED.get(legacy.context) == RecipeTransferResult.SUCCESS, "Original registration must retain direct completion");
					transfer(service, screen, player, SPECIFIC, Mode.DEFERRED);
				});
			}
			context.runOnClient(client -> {
				int count = COMPLETED.size();
				LAST_ATTEMPTS.get(SPECIFIC).complete(RecipeTransferResult.SUCCESS);
				check(COMPLETED.size() == count, "Results after runtime shutdown must be ignored");
				LAST_ATTEMPTS.clear();
				STARTED.clear();
				COMPLETED.clear();
				lastStarted = null;
			});
		});
	}

	private static void checkCompletion(RecipeTransferService service, InventoryScreen screen, Player player, IRecipeType<Mode> type) {
		var runtime = Internal.getJeiRuntime();
		var layout = runtime.getRecipeManager().createRecipeLayoutDrawableOrShowError(new Category(type), Mode.DEFERRED, runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup());
		int count = COMPLETED.size();
		check(service.getTransferRecipeError(screen, layout, player).isEmpty(), "The transfer check must succeed");
		LAST_ATTEMPTS.get(type).complete(RecipeTransferResult.SUCCESS);
		check(COMPLETED.size() == count, "Checks must not be registered as pending transfers");

		Attempt immediate = transfer(service, screen, player, type, Mode.IMMEDIATE);
		check(COMPLETED.get(immediate.context) == RecipeTransferResult.SUCCESS, "Completion inside the handler must work");
		Attempt first = transfer(service, screen, player, type, Mode.DEFERRED);
		Attempt second = transfer(service, screen, player, type, Mode.DEFERRED);
		second.complete(RecipeTransferResult.REJECTED);
		check(!COMPLETED.containsKey(first.context), "A context must only complete its own transfer");
		first.complete(RecipeTransferResult.SUCCESS);
		first.complete(RecipeTransferResult.REJECTED);
		check(COMPLETED.size() == count + 3, "Deferred and duplicate results must complete each transfer once");
		check(COMPLETED.get(first.context) == RecipeTransferResult.SUCCESS && COMPLETED.get(second.context) == RecipeTransferResult.REJECTED, "Each context must retain its own transfer");

		for (Mode mode : List.of(Mode.ERROR, Mode.THROW)) {
			Attempt failed = transfer(service, screen, player, type, mode);
			failed.complete(RecipeTransferResult.SUCCESS);
			check(!COMPLETED.containsKey(failed.context), "Failed attempts must discard pending completion");
		}
		Attempt cosmetic = transfer(service, screen, player, type, Mode.COSMETIC);
		cosmetic.complete(RecipeTransferResult.SUCCESS);
		check(COMPLETED.get(cosmetic.context) == RecipeTransferResult.SUCCESS, "Cosmetic errors must still allow completion");
		if (type == SPECIFIC) {
			Attempt delegated = transfer(service, screen, player, type, Mode.DELEGATED);
			delegated.complete(RecipeTransferResult.SUCCESS);
			delegated.complete(RecipeTransferResult.REJECTED);
			check(COMPLETED.get(delegated.context) == RecipeTransferResult.SUCCESS, "Delegated completion must notify the original transfer once");
		}
	}

	private static Attempt transfer(RecipeTransferService service, InventoryScreen screen, Player player, IRecipeType<Mode> type, Mode mode) {
		var runtime = Internal.getJeiRuntime();
		var layout = runtime.getRecipeManager().createRecipeLayoutDrawableOrShowError(new Category(type), mode, runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup());
		boolean accepted = service.transferRecipe(screen, layout, player, false);
		check(accepted == (mode != Mode.ERROR && mode != Mode.THROW), "Unexpected transfer outcome for " + mode);
		return LAST_ATTEMPTS.get(type);
	}

	private static @Nullable IRecipeTransferError handle(IRecipeTransferContext<?, InventoryMenu> context, boolean doTransfer) {
		if (!(context.getRecipe() instanceof Mode mode)) {
			return BLOCKING_ERROR;
		}
		recordAttempt(context, doTransfer);
		if (mode == Mode.DELEGATED && context.getRecipeType() == SPECIFIC) {
			check(context.getRecipeSlots().getSlotViews().isEmpty(), "Delegation must preserve the replacement recipe slots");
		}
		if (!doTransfer) {
			context.completeRecipeTransfer(RecipeTransferResult.SUCCESS);
			return null;
		}
		switch (mode) {
			case IMMEDIATE -> context.completeRecipeTransfer(RecipeTransferResult.SUCCESS);
			case ERROR -> {
				return BLOCKING_ERROR;
			}
			case COSMETIC -> {
				return COSMETIC_ERROR;
			}
			case THROW -> throw new IllegalStateException("Expected transfer test failure");
			case DEFERRED, DELEGATED -> {}
		}
		return null;
	}

	private record Handler(IRecipeType<Mode> type) implements IRecipeTransferHandler<InventoryMenu, Mode> {
		@Override
		public Class<InventoryMenu> getContainerClass() { return InventoryMenu.class; }
		@Override
		public Optional<MenuType<InventoryMenu>> getMenuType() { return Optional.empty(); }
		@Override
		public IRecipeType<Mode> getRecipeType() { return type; }
		@Override
		public @Nullable IRecipeTransferError transferRecipe(IRecipeTransferContext<Mode, InventoryMenu> context, boolean doTransfer) {
			if (type == LEGACY) {
				recordAttempt(context, doTransfer);
				if (doTransfer) {
					context.completeRecipeTransfer(RecipeTransferResult.SUCCESS);
				}
				return null;
			}
			if (context.getRecipe() == Mode.DELEGATED) {
				var manager = Internal.getJeiRuntime().getRecipeTransferManager();
				var delegate = manager.getRecipeTransferHandler(context.getContainer(), new Category(UNIVERSAL)).orElseThrow();
				var helper = Objects.requireNonNull(transferHelper);
				var replacementSlots = helper.createRecipeSlotsView(List.of());
				return helper.transferRecipeWithInputAlternatives(delegate, context, List.of(replacementSlots), doTransfer);
			}
			return handle(context, doTransfer);
		}
		@Override
		public @Nullable IRecipeTransferError transferRecipe(InventoryMenu container, Mode recipe, IRecipeSlotsView slots, Player player, boolean maxTransfer, boolean doTransfer) {
			throw new AssertionError("Expected context-based transfer");
		}
	}

	private static final class UniversalHandler implements IUniversalRecipeTransferHandler<InventoryMenu> {
		@Override
		public Class<InventoryMenu> getContainerClass() { return InventoryMenu.class; }
		@Override
		public Optional<MenuType<InventoryMenu>> getMenuType() { return Optional.empty(); }
		@Override
		public @Nullable IRecipeTransferError transferRecipe(IRecipeTransferContext<?, InventoryMenu> context, boolean doTransfer) {
			return handle(context, doTransfer);
		}
		@Override
		public @Nullable IRecipeTransferError transferRecipe(InventoryMenu container, Object recipe, IRecipeSlotsView slots, Player player, boolean maxTransfer, boolean doTransfer) {
			throw new AssertionError("Expected context-based universal transfer");
		}
	}

	private record Category(IRecipeType<Mode> type) implements IRecipeCategory<Mode> {
		@Override
		public IRecipeType<Mode> getRecipeType() { return type; }
		@Override
		public Component getTitle() { return Component.literal("Transfer completion test"); }
		@Override
		public int getWidth() { return 18; }
		@Override
		public int getHeight() { return 18; }
		@Override
		public @Nullable IDrawable getIcon() { return null; }
		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, Mode recipe, IFocusGroup focuses) {
			builder.addOutputSlot(0, 0).add(Items.DIAMOND);
		}
	}

	private enum Mode { IMMEDIATE, DEFERRED, ERROR, THROW, COSMETIC, DELEGATED }

	private static void recordAttempt(IRecipeTransferContext<?, ?> context, boolean doTransfer) {
		IRecipeTransferContext<?, ?> original = context;
		if (doTransfer) {
			original = Objects.requireNonNull(lastStarted);
		}
		LAST_ATTEMPTS.put(context.getRecipeType(), new Attempt(original, context));
	}

	private record Attempt(IRecipeTransferContext<?, ?> context, IRecipeTransferContext<?, ?> completion) {
		private void complete(RecipeTransferResult result) {
			completion.completeRecipeTransfer(result);
		}
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
