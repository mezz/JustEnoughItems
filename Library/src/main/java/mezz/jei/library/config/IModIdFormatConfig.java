package mezz.jei.library.config;

import net.minecraft.network.chat.Component;

public interface IModIdFormatConfig {
	Component getModNameFormat();

	boolean isModNameFormatOverrideActive();
}
