package mezz.jei.library.plugins.debug;

import mezz.jei.api.constants.ModIds;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class DebugRecipe {
	private static int NEXT_ID = 0;

	private final Button button;
	private final ResourceLocation registryName;

	public DebugRecipe() {
		this.button = Button.builder(Component.literal("test"), b -> {})
			.bounds(0, 0, 40, 20)
			.build();
		this.registryName = new ResourceLocation(ModIds.JEI_ID, "debug_recipe_" + NEXT_ID);
		NEXT_ID++;
	}

	public Button getButton() {
		return button;
	}

	public boolean checkHover(double mouseX, double mouseY) {
		return this.button.isMouseOver(mouseX, mouseY);
	}

	public ResourceLocation getRegistryName() {
		return registryName;
	}
}
