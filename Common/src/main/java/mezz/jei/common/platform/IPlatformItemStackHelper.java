package mezz.jei.common.platform;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface IPlatformItemStackHelper {
    int getBurnTime(ItemStack itemStack);

    boolean isBookEnchantable(ItemStack stack, ItemStack book);

    Optional<String> getCreatorModId(ItemStack stack);

    List<Component> getTestTooltip(@Nullable Player player, ItemStack itemStack);
}
