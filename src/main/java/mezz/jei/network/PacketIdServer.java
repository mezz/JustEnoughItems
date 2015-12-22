package mezz.jei.network;

public enum PacketIdServer implements IPacketId {
	RECIPE_TRANSFER,
	DELETE_ITEM;

	public static final PacketIdServer[] VALUES = values();
}
