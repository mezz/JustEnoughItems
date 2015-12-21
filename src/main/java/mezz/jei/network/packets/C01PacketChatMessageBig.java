package mezz.jei.network.packets;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C01PacketChatMessage;

/** Get around the 100 character limit on chat messages */
public class C01PacketChatMessageBig extends C01PacketChatMessage {
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
