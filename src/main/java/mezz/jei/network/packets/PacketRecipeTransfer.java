package mezz.jei.network.packets;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdServer;
import mezz.jei.transfer.BasicRecipeTransferHandlerServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PacketRecipeTransfer extends PacketJei {
	private static final Logger LOGGER = LogManager.getLogger();

	public final Map<Integer, Integer> recipeMap;
	public final List<Integer> craftingSlots;
	public final List<Integer> inventorySlots;
	private final boolean maxTransfer;
	private final boolean requireCompleteSets;

	public PacketRecipeTransfer(Map<Integer, Integer> recipeMap, List<Integer> craftingSlots, List<Integer> inventorySlots, boolean maxTransfer, boolean requireCompleteSets) {
		this.recipeMap = recipeMap;
		this.craftingSlots = craftingSlots;
		this.inventorySlots = inventorySlots;
		this.maxTransfer = maxTransfer;
		this.requireCompleteSets = requireCompleteSets;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.RECIPE_TRANSFER;
	}

	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeVarInt(recipeMap.size());
		for (Map.Entry<Integer, Integer> recipeMapEntry : recipeMap.entrySet()) {
			buf.writeVarInt(recipeMapEntry.getKey());
			buf.writeVarInt(recipeMapEntry.getValue());
		}

		buf.writeVarInt(craftingSlots.size());
		for (Integer craftingSlot : craftingSlots) {
			buf.writeVarInt(craftingSlot);
		}

		buf.writeVarInt(inventorySlots.size());
		for (Integer inventorySlot : inventorySlots) {
			buf.writeVarInt(inventorySlot);
		}

		buf.writeBoolean(maxTransfer);
		buf.writeBoolean(requireCompleteSets);
	}

	public static void readPacketData(PacketBuffer buf, PlayerEntity player) {
		Container container = player.containerMenu;

		int recipeMapSize = buf.readVarInt();
		Map<Integer, Integer> recipeMap = readRecipeMap(buf, container, recipeMapSize);
		if (recipeMap == null) {
			return;
		}

		int craftingSlotsSize = buf.readVarInt();
		List<Integer> craftingSlots = readSlotIndexes(buf, container, craftingSlotsSize);
		if (craftingSlots == null) {
			return;
		}

		if (!validateRecipeMapCraftingSlots(recipeMap, craftingSlots)) {
			return;
		}

		int inventorySlotsSize = buf.readVarInt();
		List<Integer> inventorySlots = readSlotIndexes(buf, container, inventorySlotsSize);
		if (inventorySlots == null) {
			return;
		}
		boolean maxTransfer = buf.readBoolean();
		boolean requireCompleteSets = buf.readBoolean();

		BasicRecipeTransferHandlerServer.setItems(player, recipeMap, craftingSlots, inventorySlots, maxTransfer, requireCompleteSets);
	}

	@Nullable
	private static Map<Integer, Integer> readRecipeMap(PacketBuffer buf, Container container, int recipeMapSize) {
		if (!isValidCollectionSize(container, recipeMapSize, "recipe map")) {
			return null;
		}

		Map<Integer, Integer> recipeMap = new HashMap<>();
		for (int i = 0; i < recipeMapSize; i++) {
			int slotIndex = buf.readVarInt();
			int recipeItem = buf.readVarInt();
			if (!isValidSlotIndex(container, recipeItem, "recipe item")) {
				return null;
			}
			recipeMap.put(slotIndex, recipeItem);
		}
		return recipeMap;
	}

	@Nullable
	private static List<Integer> readSlotIndexes(PacketBuffer buf, Container container, int slotCount) {
		if (!isValidCollectionSize(container, slotCount, "slot ids")) {
			return null;
		}

		List<Integer> slots = new ArrayList<>(slotCount);
		for (int i = 0; i < slotCount; i++) {
			int slotIndex = buf.readVarInt();
			if (!isValidSlotIndex(container, slotIndex, "slot")) {
				return null;
			}
			slots.add(slotIndex);
		}
		return slots;
	}

	private static boolean isValidCollectionSize(Container container, int slotCount, String collectionName) {
		if (slotCount < 0 || slotCount > container.slots.size()) {
			LOGGER.error(
				"Recipe transfer packet has invalid {} count {} for container {} with {} slots",
				collectionName,
				slotCount,
				container.getClass(),
				container.slots.size()
			);
			return false;
		}
		return true;
	}

	private static boolean isValidSlotIndex(Container container, int slotIndex, String slotName) {
		if (slotIndex < 0 || slotIndex >= container.slots.size()) {
			LOGGER.error(
				"Recipe transfer packet has invalid {} id {} for container {}",
				slotName,
				slotIndex,
				container.getClass()
			);
			return false;
		}
		return true;
	}

	private static boolean validateRecipeMapCraftingSlots(Map<Integer, Integer> recipeMap, List<Integer> craftingSlots) {
		int craftingSlotCount = craftingSlots.size();
		for (Integer craftingSlotNumber : recipeMap.keySet()) {
			if (craftingSlotNumber < 0 || craftingSlotNumber >= craftingSlotCount) {
				LOGGER.error(
					"Recipe transfer packet has invalid crafting slot number {} for {} crafting slots",
					craftingSlotNumber,
					craftingSlotCount
				);
				return false;
			}
		}
		return true;
	}

}
