package mezz.jei.gui.config.screen;

import net.minecraft.network.chat.Component;

import java.util.List;

record ConfigInfo(Component title, List<Component> lines) {
	public ConfigInfo(Component title, Component line) {
		this(title, List.of(line));
	}
}
