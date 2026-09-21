package mezz.jei.library.transfer;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketRecipeTransferCountedWithResult;
import mezz.jei.common.network.packets.PacketRecipeTransferItemHandlerWithResult;
import mezz.jei.common.network.packets.PacketRecipeTransferResult;
import mezz.jei.common.network.packets.PacketRecipeTransferWithResult;
import mezz.jei.common.network.packets.legacy.PacketRecipeTransfer;
import mezz.jei.common.network.packets.legacy.PacketRecipeTransferCounted;
import mezz.jei.common.platform.Services;
import mezz.jei.common.transfer.RecipeTransferOperationsResult;
import mezz.jei.common.transfer.RecipeTransferRequirement;
import mezz.jei.common.transfer.RecipeTransferSource;
import mezz.jei.common.transfer.RecipeTransferUtil;
import mezz.jei.common.transfer.TransferOperation;
import mezz.jei.common.util.StringUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BasicRecipeTransferHandler<C extends AbstractContainerMenu, R> implements IRecipeTransferHandler<C, R> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int UNTRACKED_TRANSFER_ID = 0;

	private final IConnectionToServer serverConnection;
	private final IStackHelper stackHelper;
	private final IRecipeTransferHandlerHelper handlerHelper;
	private final IRecipeTransferInfo<C, R> transferInfo;

	public BasicRecipeTransferHandler(
		IConnectionToServer serverConnection,
		IStackHelper stackHelper,
		IRecipeTransferHandlerHelper handlerHelper,
		IRecipeTransferInfo<C, R> transferInfo
	) {
		this.serverConnection = serverConnection;
		this.stackHelper = stackHelper;
		this.handlerHelper = handlerHelper;
		this.transferInfo = transferInfo;
	}

	@Override
	public Class<? extends C> getContainerClass() {
		return transferInfo.getContainerClass();
	}

	@Override
	public Optional<MenuType<C>> getMenuType() {
		return transferInfo.getMenuType();
	}

	@Override
	public IRecipeType<R> getRecipeType() {
		return transferInfo.getRecipeType();
	}

	@SuppressWarnings("removal")
	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(C container, R recipe, IRecipeSlotsView recipeSlotsView, Player player, boolean maxTransfer, boolean doTransfer) {
		return transferRecipeInternal(container, recipe, recipeSlotsView, player, maxTransfer, doTransfer, null);
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(IRecipeTransferContext<R, C> context, boolean doTransfer) {
		return transferRecipeInternal(
			context.getContainer(),
			context.getRecipe(),
			context.getRecipeSlots(),
			context.getPlayer(),
			context.isMaxTransfer(),
			doTransfer,
			context
		);
	}

	@Nullable
	private IRecipeTransferError transferRecipeInternal(
		C container,
		R recipe,
		IRecipeSlotsView recipeSlotsView,
		Player player,
		boolean maxTransfer,
		boolean doTransfer,
		@Nullable IRecipeTransferContext<R, C> context
	) {
		if (!serverConnection.isJeiOnServer()) {
			Component tooltipMessage = Component.translatable("jei.tooltip.error.recipe.transfer.no.server");
			return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
		}

		if (!transferInfo.canHandle(container, recipe)) {
			IRecipeTransferError handlingError = transferInfo.getHandlingError(container, recipe);
			if (handlingError != null) {
				return handlingError;
			}
			return handlerHelper.createInternalError();
		}

		List<Slot> craftingSlots = Collections.unmodifiableList(transferInfo.getRecipeSlots(container, recipe));
		List<Slot> inventorySlots = Collections.unmodifiableList(transferInfo.getInventorySlots(container, recipe));
		if (!validateTransferInfo(transferInfo, container, craftingSlots, inventorySlots)) {
			return handlerHelper.createInternalError();
		}

		List<IRecipeSlotView> inputItemSlotViews = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT);
		if (!validateRecipeView(transferInfo, container, craftingSlots, inputItemSlotViews)) {
			return handlerHelper.createInternalError();
		}

		InventoryState inventoryState = getInventoryState(craftingSlots, inventorySlots, player, container, transferInfo);
		if (inventoryState == null) {
			return handlerHelper.createInternalError();
		}

		// check if we have enough inventory space to shuffle items around to their final locations
		int inputCount = (int) inputItemSlotViews.stream()
			.filter(slot -> !slot.isEmpty())
			.count();
		if (!inventoryState.hasRoom(inputCount)) {
			Component message = Component.translatable("jei.tooltip.error.recipe.transfer.inventory.full");
			return handlerHelper.createUserErrorWithTooltip(message);
		}

		RecipeTransferOperationsResult transferOperations = RecipeTransferUtil.getRecipeTransferOperations(
			stackHelper,
			inventoryState.availableItemStacks,
			inputItemSlotViews,
			craftingSlots
		);
		List<RecipeTransferRequirement> itemHandlerRequirements = null;
		if (!transferOperations.missingItems.isEmpty() &&
			serverConnection.canSendPacket(PacketRecipeTransferItemHandlerWithResult.TYPE) &&
			hasItemHandler(inventorySlots, player)
		) {
			itemHandlerRequirements = createItemHandlerRequirements(inputItemSlotViews, craftingSlots);
		}

		boolean useItemHandlerTransferPacket = itemHandlerRequirements != null;
		if (!transferOperations.missingItems.isEmpty() && !useItemHandlerTransferPacket) {
			Component message = Component.translatable("jei.tooltip.error.recipe.transfer.missing");
			return handlerHelper.createUserErrorForMissingSlots(message, transferOperations.missingItems);
		}

		if (!useItemHandlerTransferPacket &&
			!RecipeTransferUtil.validateSlots(player, transferOperations.results, craftingSlots, inventorySlots)
		) {
			return handlerHelper.createInternalError();
		}

		boolean requiresCountedTransferPacket = requiresCountedTransferPacket(transferOperations.results);
		boolean useCountedTransferPacket = requiresCountedTransferPacket && serverConnection.canSendPacket(PacketRecipeTransferCounted.TYPE);

		if (doTransfer) {
			boolean requireCompleteSets = transferInfo.requireCompleteSets(container, recipe);
			boolean supportsTransferResults = supportsServerRecipeTransferResults(useItemHandlerTransferPacket, useCountedTransferPacket);
			if (context != null && supportsTransferResults) {
				sendTransferWithResult(
					transferOperations.results,
					craftingSlots,
					inventorySlots,
					maxTransfer,
					requireCompleteSets,
					itemHandlerRequirements,
					useItemHandlerTransferPacket,
					useCountedTransferPacket,
					context
				);
			} else if (useItemHandlerTransferPacket) {
				PacketRecipeTransferItemHandlerWithResult packet = PacketRecipeTransferItemHandlerWithResult.fromSlots(
					itemHandlerRequirements,
					craftingSlots,
					inventorySlots,
					maxTransfer,
					requireCompleteSets,
					UNTRACKED_TRANSFER_ID
				);
				serverConnection.sendPacketToServer(packet);
			} else if (useCountedTransferPacket) {
				PacketRecipeTransferCounted packet = PacketRecipeTransferCounted.fromSlots(
					transferOperations.results,
					craftingSlots,
					inventorySlots,
					maxTransfer,
					requireCompleteSets
				);
				serverConnection.sendPacketToServer(packet);
			} else {
				PacketRecipeTransfer packet = PacketRecipeTransfer.fromSlots(
					transferOperations.results,
					craftingSlots,
					inventorySlots,
					maxTransfer,
					requireCompleteSets
				);
				serverConnection.sendPacketToServer(packet);
			}
		}

		return null;
	}

	private boolean supportsServerRecipeTransferResults(boolean useItemHandlerTransferPacket, boolean useCountedTransferPacket) {
		if (useItemHandlerTransferPacket) {
			return serverConnection.canSendPacket(PacketRecipeTransferItemHandlerWithResult.TYPE);
		}
		if (useCountedTransferPacket) {
			return serverConnection.canSendPacket(PacketRecipeTransferCountedWithResult.TYPE);
		}
		return serverConnection.canSendPacket(PacketRecipeTransferWithResult.TYPE);
	}

	private void sendTransferWithResult(
		List<TransferOperation> transferOperations,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets,
		@Nullable List<RecipeTransferRequirement> itemHandlerRequirements,
		boolean useItemHandlerTransferPacket,
		boolean useCountedTransferPacket,
		IRecipeTransferContext<R, C> context
	) {
		PacketRecipeTransferResult.registerPendingRecipeTransfer(context);
		int transferId = context.getTransferId();
		if (useItemHandlerTransferPacket) {
			assert itemHandlerRequirements != null;
			PacketRecipeTransferItemHandlerWithResult packet = PacketRecipeTransferItemHandlerWithResult.fromSlots(
				itemHandlerRequirements,
				craftingSlots,
				inventorySlots,
				maxTransfer,
				requireCompleteSets,
				transferId
			);
			serverConnection.sendPacketToServer(packet);
		} else if (useCountedTransferPacket) {
			PacketRecipeTransferCountedWithResult packet = PacketRecipeTransferCountedWithResult.fromSlots(
				transferOperations,
				craftingSlots,
				inventorySlots,
				maxTransfer,
				requireCompleteSets,
				transferId
			);
			serverConnection.sendPacketToServer(packet);
		} else {
			PacketRecipeTransferWithResult packet = PacketRecipeTransferWithResult.fromSlots(
				transferOperations,
				craftingSlots,
				inventorySlots,
				maxTransfer,
				requireCompleteSets,
				transferId
			);
			serverConnection.sendPacketToServer(packet);
		}
	}

	private static boolean requiresCountedTransferPacket(List<TransferOperation> transferOperations) {
		Set<Integer> craftingSlotIds = new IntOpenHashSet();
		for (TransferOperation transferOperation : transferOperations) {
			if (transferOperation.count() > 1 || !craftingSlotIds.add(transferOperation.craftingSlotId())) {
				return true;
			}
		}
		return false;
	}

	public static <C extends AbstractContainerMenu, R> boolean validateTransferInfo(
		IRecipeTransferInfo<C, R> transferInfo,
		C container,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots
	) {
		for (Slot slot : craftingSlots) {
			if (slot.isFake()) {
				LOGGER.error("Recipe Transfer helper {} does not work for container {}. " +
					"The Recipe Transfer Helper references crafting slot index [{}] but it is a fake (output) slot, which is not allowed.",
					transferInfo.getClass(), container.getClass(), slot.index
				);
				return false;
			}
		}
		for (Slot slot : inventorySlots) {
			if (slot.isFake()) {
				LOGGER.error("Recipe Transfer helper {} does not work for container {}. " +
					"The Recipe Transfer Helper references inventory slot index [{}] but it is a fake (output) slot, which is not allowed.",
					transferInfo.getClass(), container.getClass(), slot.index
				);
				return false;
			}
		}
		Collection<Integer> craftingSlotIndexes = slotIndexes(craftingSlots);
		Collection<Integer> inventorySlotIndexes = slotIndexes(inventorySlots);
		Collection<Integer> containerSlotIndexes = slotIndexes(container.slots);

		if (!containerSlotIndexes.containsAll(craftingSlotIndexes)) {
			LOGGER.error("Recipe Transfer helper {} does not work for container {}. " +
				"The Recipes Transfer Helper references crafting slot indexes [{}] that are not found in the inventory container slots [{}]",
				transferInfo.getClass(), container.getClass(), StringUtil.intsToString(craftingSlotIndexes), StringUtil.intsToString(containerSlotIndexes)
			);
			return false;
		}

		if (!containerSlotIndexes.containsAll(inventorySlotIndexes)) {
			LOGGER.error("Recipe Transfer helper {} does not work for container {}. " +
				"The Recipes Transfer Helper references inventory slot indexes [{}] that are not found in the inventory container slots [{}]",
				transferInfo.getClass(), container.getClass(), StringUtil.intsToString(inventorySlotIndexes), StringUtil.intsToString(containerSlotIndexes)
			);
			return false;
		}

		return true;
	}

	public static <C extends AbstractContainerMenu, R> boolean validateRecipeView(
		IRecipeTransferInfo<C, R> transferInfo,
		C container,
		List<Slot> craftingSlots,
		List<IRecipeSlotView> inputSlots
	) {
		if (inputSlots.size() > craftingSlots.size()) {
			LOGGER.error("Recipe View {} does not work for container {}. " +
				"The Recipe View has more input slots ({}) than the number of inventory crafting slots ({})",
				transferInfo.getClass(), container.getClass(), inputSlots.size(), craftingSlots.size()
			);
			return false;
		}

		return true;
	}

	public static Set<Integer> slotIndexes(Collection<Slot> slots) {
		Set<Integer> set = new IntOpenHashSet(slots.size());
		for (Slot s : slots) {
			set.add(s.index);
		}
		return set;
	}

	@Nullable
	public static <C extends AbstractContainerMenu, R> InventoryState getInventoryState(
		Collection<Slot> craftingSlots,
		Collection<Slot> inventorySlots,
		Player player,
		C container,
		IRecipeTransferInfo<C, R> transferInfo
	) {
		Map<RecipeTransferSource, ItemStack> availableItemStacks = new HashMap<>();
		int filledCraftSlotCount = 0;
		int emptySlotCount = 0;

		for (Slot slot : craftingSlots) {
			final ItemStack stack = slot.getItem();
			if (!stack.isEmpty()) {
				if (!slot.allowModification(player)) {
					LOGGER.error(
						"Recipe Transfer helper {} does not work for container {}. The Player is not able to move items out of Crafting Slot number {}",
						transferInfo.getClass(), container.getClass(), slot.index
					);
					return null;
				}
				filledCraftSlotCount++;
				availableItemStacks.put(new RecipeTransferSource(slot), stack.copy());
			}
		}

		for (Slot slot : inventorySlots) {
			final ItemStack stack = slot.getItem();
			if (!stack.isEmpty()) {
				if (slot.allowModification(player)) {
					availableItemStacks.put(new RecipeTransferSource(slot), stack.copy());
				}
			} else if (slot.allowModification(player)) {
				emptySlotCount++;
			}
		}

		return new InventoryState(availableItemStacks, filledCraftSlotCount, emptySlotCount);
	}

	private static boolean hasItemHandler(
		Collection<Slot> inventorySlots,
		Player player
	) {
		for (Slot slot : inventorySlots) {
			ItemStack stack = slot.getItem();
			if (!stack.isEmpty() &&
				slot.allowModification(player) &&
				Services.PLATFORM.getRecipeTransferHelper().hasItemHandler(stack.copy())
			) {
				return true;
			}
		}
		return false;
	}

	@Nullable
	private static List<RecipeTransferRequirement> createItemHandlerRequirements(
		List<IRecipeSlotView> inputItemSlotViews,
		List<Slot> craftingSlots
	) {
		List<RecipeTransferRequirement> requirements = new ArrayList<>();
		int totalAlternatives = 0;
		for (int i = 0; i < inputItemSlotViews.size(); i++) {
			IRecipeSlotView slotView = inputItemSlotViews.get(i);
			if (slotView.isEmpty()) {
				continue;
			}

			List<ItemStack> acceptedStacks = new ArrayList<>();
			for (ITypedIngredient<?> ingredient : slotView.getAllIngredientsList()) {
				if (ingredient == null) {
					continue;
				}
				ITypedIngredient<ItemStack> itemIngredient = ingredient.castToItemStackType();
				if (itemIngredient == null || itemIngredient.getIngredient().isEmpty()) {
					continue;
				}
				addAcceptedStack(acceptedStacks, itemIngredient.getIngredient());
				if (acceptedStacks.size() > RecipeTransferRequirement.MAX_ALTERNATIVES) {
					return null;
				}
			}
			if (acceptedStacks.isEmpty()) {
				return null;
			}
			totalAlternatives += acceptedStacks.size();
			if (totalAlternatives > RecipeTransferRequirement.MAX_TOTAL_ALTERNATIVES) {
				return null;
			}
			if (requirements.size() >= RecipeTransferRequirement.MAX_REQUIREMENTS) {
				return null;
			}
			requirements.add(new RecipeTransferRequirement(craftingSlots.get(i).index, acceptedStacks));
		}
		if (requirements.isEmpty()) {
			return null;
		}
		return List.copyOf(requirements);
	}

	private static void addAcceptedStack(List<ItemStack> acceptedStacks, ItemStack stack) {
		for (int i = 0; i < acceptedStacks.size(); i++) {
			ItemStack acceptedStack = acceptedStacks.get(i);
			if (ItemStack.isSameItemSameComponents(acceptedStack, stack)) {
				if (stack.getCount() > acceptedStack.getCount()) {
					acceptedStacks.set(i, stack.copy());
				}
				return;
			}
		}
		acceptedStacks.add(stack.copyWithCount(Math.max(1, stack.getCount())));
	}

	public record InventoryState(
		Map<RecipeTransferSource, ItemStack> availableItemStacks,
		int filledCraftSlotCount,
		int emptySlotCount
	) {
		/**
		 * check if we have enough inventory space to shuffle items around to their final locations
		 */
		public boolean hasRoom(int inputCount) {
			return filledCraftSlotCount - inputCount <= emptySlotCount;
		}
	}
}
