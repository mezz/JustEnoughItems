package mezz.jei.test;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferListener;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import mezz.jei.common.network.packets.PacketRecipeTransferResult;
import mezz.jei.common.transfer.RecipeTransferLifecycleManager;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class RecipeTransferLifecycleManagerTest {
	private static final int TRANSFER_ID = 42;

	@AfterEach
	public void clearPendingRecipeTransfers() {
		PacketRecipeTransferResult.clearPendingRecipeTransfers();
	}

	@Test
	public void listenerFailuresAreIsolated() {
		List<String> events = new ArrayList<>();
		AtomicReference<@Nullable IRecipeTransferContext<?, ?>> contextReference = new AtomicReference<>();
		IRecipeTransferListener brokenListener = new IRecipeTransferListener() {
			@Override
			public void beforeRecipeTransfer(IRecipeTransferContext<?, ?> context) {
				throw new IllegalStateException("before failed");
			}

			@Override
			public void afterRecipeTransfer(IRecipeTransferContext<?, ?> context, RecipeTransferResult result) {
				assertSame(contextReference.get(), context);
				throw new IllegalStateException("after failed");
			}
		};
		IRecipeTransferListener recordingListener = new IRecipeTransferListener() {
			@Override
			public void beforeRecipeTransfer(IRecipeTransferContext<?, ?> context) {
				assertSame(contextReference.get(), context);
				events.add("before");
			}

			@Override
			public void afterRecipeTransfer(IRecipeTransferContext<?, ?> context, RecipeTransferResult result) {
				assertSame(contextReference.get(), context);
				events.add("after:" + result);
			}
		};
		var lifecycleManager = new RecipeTransferLifecycleManager(List.of(brokenListener, recordingListener));
		var context = new TestRecipeTransferContext(lifecycleManager);
		contextReference.set(context);

		lifecycleManager.beforeRecipeTransfer(context);
		PacketRecipeTransferResult.registerPendingRecipeTransfer(context);
		new PacketRecipeTransferResult(TRANSFER_ID, true).completePendingRecipeTransfer();

		assertEquals(List.of("before", "after:SUCCESS"), events);
	}

	@Test
	public void resultsAreIgnoredAfterPendingRecipeTransfersAreCleared() {
		List<Integer> completedTransfers = new ArrayList<>();
		IRecipeTransferListener listener = new IRecipeTransferListener() {
			@Override
			public void afterRecipeTransfer(IRecipeTransferContext<?, ?> context, RecipeTransferResult result) {
				completedTransfers.add(context.getTransferId());
			}
		};
		var lifecycleManager = new RecipeTransferLifecycleManager(List.of(listener));
		var context = new TestRecipeTransferContext(lifecycleManager);

		lifecycleManager.beforeRecipeTransfer(context);
		PacketRecipeTransferResult.registerPendingRecipeTransfer(context);
		PacketRecipeTransferResult.clearPendingRecipeTransfers();
		new PacketRecipeTransferResult(TRANSFER_ID, true).completePendingRecipeTransfer();

		assertEquals(List.of(), completedTransfers);
	}

	private static class TestRecipeTransferContext implements IRecipeTransferContext<String, AbstractContainerMenu> {
		private static final RecipeType<String> RECIPE_TYPE = RecipeType.create("jei", "test_transfer_listener", String.class);
		private final RecipeTransferLifecycleManager lifecycleManager;

		public TestRecipeTransferContext(RecipeTransferLifecycleManager lifecycleManager) {
			this.lifecycleManager = lifecycleManager;
		}

		@Override
		public int getTransferId() {
			return TRANSFER_ID;
		}

		@Override
		public void completeRecipeTransfer(RecipeTransferResult result) {
			lifecycleManager.completeRecipeTransfer(this, result);
		}

		@Override
		public String getRecipe() {
			return "recipe";
		}

		@Override
		public RecipeType<String> getRecipeType() {
			return RECIPE_TYPE;
		}

		@Override
		public AbstractContainerMenu getContainer() {
			throw new UnsupportedOperationException();
		}

		@Override
		public AbstractContainerScreen<AbstractContainerMenu> getScreen() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IRecipeSlotsView getRecipeSlots() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Player getPlayer() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isMaxTransfer() {
			return false;
		}
	}
}
