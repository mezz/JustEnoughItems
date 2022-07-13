package mezz.jei.forge.platform;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.platform.IPlatformConfigHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;

import java.util.Optional;

public class ConfigHelper implements IPlatformConfigHelper {
    @Override
    public Optional<Screen> getConfigScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        return ModList.get()
            .getModContainerById(ModIds.JEI_ID)
            .map(ModContainer::getModInfo)
            .flatMap(ConfigScreenHandler::getScreenFactoryFor)
            .map(f -> f.apply(minecraft, minecraft.screen));
    }

    @Override
    public Component getMissingConfigScreenMessage() {
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/configured");
        Style style = Style.EMPTY
            .withUnderlined(true)
            .withClickEvent(clickEvent);
        MutableComponent message = Component.translatable("jei.message.configured");
        return message.setStyle(style);
    }
}
