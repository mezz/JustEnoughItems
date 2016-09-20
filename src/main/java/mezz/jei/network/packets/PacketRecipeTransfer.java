package mezz.jei.network.packets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdServer;
import mezz.jei.transfer.BasicRecipeTransferHandlerServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public class PacketRecipeTransfer extends PacketJEI {

	private Map<Integer, Integer> recipeMap;
	private List<Integer> craftingSlots;
	private List<Integer> inventorySlots;
	private boolean maxTransfer;

	public PacketRecipeTransfer() {

	}

	public PacketRecipeTransfer(Map<Integer, Integer> recipeMap, List<Integer> craftingSlots, List<Integer> inventorySlots, boolean maxTransfer) {
		this.recipeMap = recipeMap;
		this.craftingSlots = craftingSlots;
		this.inventorySlots = inventorySlots;
		this.maxTransfer = maxTransfer;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.RECIPE_TRANSFER;
	}

	@Override
	public void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
		int recipeMapSize = buf.readVarIntFromBuffer();
		recipeMap = new HashMap<Integer, Integer>(recipeMapSize);
		for (int i = 0; i < recipeMapSize; i++) {
			int slotIndex = buf.readVarIntFromBuffer();
			int recipeItem = buf.readVarIntFromBuffer();
			recipeMap.put(slotIndex, recipeItem);
		}

		int craftingSlotsSize = buf.readVarIntFromBuffer();
		craftingSlots = new ArrayList<Integer>(craftingSlotsSize);
		for (int i = 0; i < craftingSlotsSize; i++) {
			int slotIndex = buf.readVarIntFromBuffer();
			craftingSlots.add(slotIndex);
		}

		int inventorySlotsSize = buf.readVarIntFromBuffer();
		inventorySlots = new ArrayList<Integer>(inventorySlotsSize);
		for (int i = 0; i < inventorySlotsSize; i++) {
			int slotIndex = buf.readVarIntFromBuffer();
			inventorySlots.add(slotIndex);
		}

		maxTransfer = buf.readBoolean();

		BasicRecipeTransferHandlerServer.setItems(player, recipeMap, craftingSlots, inventorySlots, maxTransfer);
	}

	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeVarIntToBuffer(recipeMap.size());
		for (Map.Entry<Integer, Integer> recipeMapEntry : recipeMap.entrySet()) {
			buf.writeVarIntToBuffer(recipeMapEntry.getKey());
			buf.writeVarIntToBuffer(recipeMapEntry.getValue());
		}

		buf.writeVarIntToBuffer(craftingSlots.size());
		for (Integer craftingSlot : craftingSlots) {
			buf.writeVarIntToBuffer(craftingSlot);
		}

		buf.writeVarIntToBuffer(inventorySlots.size());
		for (Integer inventorySlot : inventorySlots) {
			buf.writeVarIntToBuffer(inventorySlot);
		}

		buf.writeBoolean(maxTransfer);
	}
}
