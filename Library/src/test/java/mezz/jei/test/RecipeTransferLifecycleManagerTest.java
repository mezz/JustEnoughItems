package mezz.jei.test;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferListener;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.common.network.packets.PacketRecipeTransferResult;
import mezz.jei.common.transfer.PendingRecipeTransfers;
import mezz.jei.common.transfer.RecipeTransferLifecycleManager;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;
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
		PendingRecipeTransfers.INSTANCE.clear();
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
		IRecipeTransferContext<?, ?> completion = PendingRecipeTransfers.INSTANCE.registerRecipeTransfer(context, this);
		completion.completeRecipeTransfer(RecipeTransferResult.SUCCESS);

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
		IRecipeTransferContext<?, ?> completion = PendingRecipeTransfers.INSTANCE.registerRecipeTransfer(context, this);
		PendingRecipeTransfers.INSTANCE.clear();
		completion.completeRecipeTransfer(RecipeTransferResult.SUCCESS);

		assertEquals(List.of(), completedTransfers);
	}

	@Test
	public void resultsOnlyCompleteTheMatchingTransferOnce() {
		List<String> completedTransfers = new ArrayList<>();
		IRecipeTransferListener listener = new IRecipeTransferListener() {
			@Override
			public void afterRecipeTransfer(IRecipeTransferContext<?, ?> context, RecipeTransferResult result) {
				completedTransfers.add(context.getTransferId() + ":" + result);
			}
		};
		var lifecycleManager = new RecipeTransferLifecycleManager(List.of(listener));
		var firstContext = new TestRecipeTransferContext(lifecycleManager, TRANSFER_ID);
		var secondContext = new TestRecipeTransferContext(lifecycleManager, TRANSFER_ID + 1);
		int firstId = firstContext.getTransferId();
		int secondId = secondContext.getTransferId();
		IRecipeTransferContext<?, ?> firstCompletion = PendingRecipeTransfers.INSTANCE.registerRecipeTransfer(firstContext, this);
		IRecipeTransferContext<?, ?> secondCompletion = PendingRecipeTransfers.INSTANCE.registerRecipeTransfer(secondContext, this);

		new PacketRecipeTransferResult(TRANSFER_ID + 2, true).completePendingRecipeTransfer();
		secondCompletion.completeRecipeTransfer(RecipeTransferResult.REJECTED);
		new PacketRecipeTransferResult(secondId, true).completePendingRecipeTransfer();
		new PacketRecipeTransferResult(firstId, true).completePendingRecipeTransfer();
		firstCompletion.completeRecipeTransfer(RecipeTransferResult.REJECTED);

		assertEquals(List.of("43:REJECTED", "42:SUCCESS"), completedTransfers);
	}

	@Test
	public void oldContextCannotCompleteANewTransferWithTheSameId() {
		List<RecipeTransferResult> results = new ArrayList<>();
		IRecipeTransferListener listener = new IRecipeTransferListener() {
			@Override
			public void afterRecipeTransfer(IRecipeTransferContext<?, ?> context, RecipeTransferResult result) {
				results.add(result);
			}
		};
		var lifecycleManager = new RecipeTransferLifecycleManager(List.of(listener));
		IRecipeTransferContext<?, ?> oldCompletion = PendingRecipeTransfers.INSTANCE.registerRecipeTransfer(new TestRecipeTransferContext(lifecycleManager), this);
		PendingRecipeTransfers.INSTANCE.clear();
		IRecipeTransferContext<?, ?> newCompletion = PendingRecipeTransfers.INSTANCE.registerRecipeTransfer(new TestRecipeTransferContext(lifecycleManager), this);

		oldCompletion.completeRecipeTransfer(RecipeTransferResult.SUCCESS);
		assertEquals(List.of(), results);
		newCompletion.completeRecipeTransfer(RecipeTransferResult.REJECTED);
		assertEquals(List.of(RecipeTransferResult.REJECTED), results);
	}

	private static class TestRecipeTransferContext implements IRecipeTransferContext<String, AbstractContainerMenu> {
		private static final IRecipeType<String> RECIPE_TYPE = IRecipeType.create("jei", "test_transfer_listener", String.class);
		private final RecipeTransferLifecycleManager lifecycleManager;
		private final int transferId;

		public TestRecipeTransferContext(RecipeTransferLifecycleManager lifecycleManager) {
			this(lifecycleManager, TRANSFER_ID);
		}

		public TestRecipeTransferContext(RecipeTransferLifecycleManager lifecycleManager, int transferId) {
			this.lifecycleManager = lifecycleManager;
			this.transferId = transferId;
		}

		@Override
		public int getTransferId() {
			return transferId;
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
		public IRecipeType<String> getRecipeType() {
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
