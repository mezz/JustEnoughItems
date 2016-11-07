package mezz.jei.network;

public enum PacketIdServer implements IPacketId {
	RECIPE_TRANSFER,
	DELETE_ITEM,
	GIVE_BIG,
	CHEAT_PERMISSION_REQUEST;

	public static final PacketIdServer[] VALUES = values();
}
