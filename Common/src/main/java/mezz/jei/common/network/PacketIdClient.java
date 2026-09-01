package mezz.jei.common.network;

public enum PacketIdClient implements IPacketId {
	CHEAT_PERMISSION,
	RECIPE_TRANSFER_RESULT;

	public static final PacketIdClient[] VALUES = values();
}
