package mezz.jei.network.packets;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdServer;
import mezz.jei.transfer.BasicRecipeTransferHandlerServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PacketRecipeTransfer extends PacketJei {
	public final Map<IRecipeSlotView, Slot> recipeSlotToInventorySlotMap;
	public final List<Slot> craftingSlots;
	public final List<Slot> inventorySlots;
	private final boolean maxTransfer;
	private final boolean requireCompleteSets;

	public PacketRecipeTransfer(
		Map<IRecipeSlotView, Slot> recipeSlotToInventorySlotMap,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets
	) {
		this.recipeSlotToInventorySlotMap = recipeSlotToInventorySlotMap;
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
	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeVarInt(recipeSlotToInventorySlotMap.size());
		for (Map.Entry<IRecipeSlotView, Slot> recipeMapEntry : recipeSlotToInventorySlotMap.entrySet()) {
			IRecipeSlotView slotView = recipeMapEntry.getKey();
			int slotIndex = slotView.getContainerSlotIndex().orElseThrow();
			buf.writeVarInt(slotIndex);

			Slot inventorySlot = recipeMapEntry.getValue();
			buf.writeVarInt(inventorySlot.index);
		}

		buf.writeVarInt(craftingSlots.size());
		for (Slot craftingSlot : craftingSlots) {
			buf.writeVarInt(craftingSlot.index);
		}

		buf.writeVarInt(inventorySlots.size());
		for (Slot inventorySlot : inventorySlots) {
			buf.writeVarInt(inventorySlot.index);
		}

		buf.writeBoolean(maxTransfer);
		buf.writeBoolean(requireCompleteSets);
	}

	public static void readPacketData(FriendlyByteBuf buf, Player player) {
		AbstractContainerMenu container = player.containerMenu;

		int recipeSlotsSize = buf.readVarInt();
		List<Tuple<Slot, Slot>> recipeSlotToSourceSlots = new ArrayList<>(recipeSlotsSize);
		for (int i = 0; i < recipeSlotsSize; i++) {
			int slotIndex = buf.readVarInt();
			Slot recipeSlot = container.getSlot(slotIndex);
			int inventoryIndex = buf.readVarInt();
			Slot inventorySlot = container.getSlot(inventoryIndex);
			recipeSlotToSourceSlots.add(new Tuple<>(recipeSlot, inventorySlot));
		}

		int craftingSlotsSize = buf.readVarInt();
		List<Slot> craftingSlots = new ArrayList<>(craftingSlotsSize);
		for (int i = 0; i < craftingSlotsSize; i++) {
			int slotIndex = buf.readVarInt();
			Slot slot = container.getSlot(slotIndex);
			craftingSlots.add(slot);
		}

		int inventorySlotsSize = buf.readVarInt();
		List<Slot> inventorySlots = new ArrayList<>();
		for (int i = 0; i < inventorySlotsSize; i++) {
			int slotIndex = buf.readVarInt();
			Slot slot = container.getSlot(slotIndex);
			inventorySlots.add(slot);
		}
		boolean maxTransfer = buf.readBoolean();
		boolean requireCompleteSets = buf.readBoolean();

		BasicRecipeTransferHandlerServer.setItems(
			player,
			recipeSlotToSourceSlots,
			craftingSlots,
			inventorySlots,
			maxTransfer,
			requireCompleteSets
		);
	}

}
