package mezz.jei.util;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;

public class FakeClientPlayer extends EntityPlayer {
	public static FakeClientPlayer INSTANCE;

	public static FakeClientPlayer getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new FakeClientPlayer();
		}
		return INSTANCE;
	}

	private FakeClientPlayer() {
		super(FakeClientWorld.getInstance(), new GameProfile(new UUID(0, 0), "JEI_Fake"));
	}

	@Override
	public boolean isSpectator() {
		return false;
	}

	@Override
	public boolean isCreative() {
		return false;
	}
}
