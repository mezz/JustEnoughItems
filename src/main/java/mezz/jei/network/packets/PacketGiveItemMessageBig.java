package mezz.jei.network.packets;

import javax.annotation.Nonnull;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C01PacketChatMessage;

import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdServer;

public class PacketGiveItemMessageBig extends PacketJEI {
	private String message;

	public PacketGiveItemMessageBig() {

	}

	public PacketGiveItemMessageBig(@Nonnull String message) {
		this.message = message;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.GIVE_BIG;
	}

	@Override
	public void writePacketData(PacketBuffer buf) throws IOException {
		buf.writeString(message);
	}

	@Override
	public void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
		if (player instanceof EntityPlayerMP) {
			String message = buf.readStringFromBuffer(32767);
			C01PacketChatMessageBig packet = new C01PacketChatMessageBig(message);
			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			playerMP.playerNetServerHandler.processChatMessage(packet);
		}
	}

	/** Get around the 100 character limit on chat messages */
	private static class C01PacketChatMessageBig extends C01PacketChatMessage {
		private String message;

		@SuppressWarnings("unused")
		public C01PacketChatMessageBig() {
		}

		public C01PacketChatMessageBig(String messageIn) {
			this.message = messageIn;
		}

		@Override
		public void readPacketData(PacketBuffer buf) throws IOException {
			this.message = buf.readStringFromBuffer(1000);
		}

		@Override
		public void writePacketData(PacketBuffer buf) throws IOException {
			buf.writeString(this.message);
		}

		@Override
		public void processPacket(INetHandlerPlayServer handler) {
			handler.processChatMessage(this);
		}

		@Override
		public String getMessage() {
			return this.message;
		}
	}
}
