package mezz.jei.common.network.packets;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.network.ClientPacketContext;
import mezz.jei.common.network.packets.handlers.ClientCheatPermissionHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PacketCheatPermission extends PlayToClientPacket<PacketCheatPermission> {
	public static final Type<PacketCheatPermission> TYPE = new Type<>(new ResourceLocation(ModIds.JEI_ID, "cheat_permission"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketCheatPermission> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL,
		p -> p.hasPermission,
		ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
		p -> p.allowedCheatingMethods,
		PacketCheatPermission::new
	);

	private final boolean hasPermission;
	private final List<String> allowedCheatingMethods;

	public PacketCheatPermission(boolean hasPermission, IServerConfig serverConfig) {
		this(hasPermission, getAllowedCheatingMethods(serverConfig));
	}

	public PacketCheatPermission(boolean hasPermission, List<String> allowedCheatingMethods) {
		this.hasPermission = hasPermission;
		this.allowedCheatingMethods = allowedCheatingMethods;
	}

	@Override
	public Type<PacketCheatPermission> type() {
		return TYPE;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, PacketCheatPermission> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public void process(ClientPacketContext context) {
		ClientCheatPermissionHandler.handleHasCheatPermission(context, hasPermission, allowedCheatingMethods);
	}

	@NotNull
	private static List<String> getAllowedCheatingMethods(IServerConfig serverConfig) {
		List<String> allowedCheatingMethods = new ArrayList<>();
		if (serverConfig.isCheatModeEnabledForOp()) {
			allowedCheatingMethods.add("jei.chat.error.no.cheat.permission.op");
		}
		if (serverConfig.isCheatModeEnabledForCreative()) {
			allowedCheatingMethods.add("jei.chat.error.no.cheat.permission.creative");
		}
		if (serverConfig.isCheatModeEnabledForGive()) {
			allowedCheatingMethods.add("jei.chat.error.no.cheat.permission.give");
		}
		return allowedCheatingMethods;
	}
}
