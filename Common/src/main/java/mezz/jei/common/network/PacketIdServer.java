package mezz.jei.common.network;

public enum PacketIdServer implements IPacketId {
	RECIPE_TRANSFER,
	DELETE_ITEM,
	GIVE_ITEM,
	SET_HOTBAR_ITEM,
	CHEAT_PERMISSION_REQUEST,
	RECIPE_TRANSFER_COUNTED,
	RECIPE_TRANSFER_WITH_RESULT,
	RECIPE_TRANSFER_COUNTED_WITH_RESULT;

	public static final PacketIdServer[] VALUES = values();
}
