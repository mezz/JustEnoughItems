package mezz.jei.config;

import cpw.mods.fml.client.IModGuiFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class JEIModGuiFactory implements IModGuiFactory {
	@Override
	public void initialize(Minecraft minecraftInstance) {

	}

	@Nonnull
	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return JEIModConfigGui.class;
	}

	@Nullable
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	@Nullable
	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}
}
