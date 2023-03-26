package mezz.jei.library.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RecipeUidHelper {

    public static CompoundTag putAll(CompoundTag tag, String key, List<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            tag.put(key + i, stack.save(new CompoundTag()));
        }
        return tag;
    }

}
