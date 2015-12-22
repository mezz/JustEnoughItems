package mezz.jei.debug;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class DebugItem extends Item {
	private static final RandomStringMaker RANDOM_STRING_MAKER = new RandomStringMaker(12);

	public DebugItem(String name) {
		setUnlocalizedName(name);
		setCreativeTab(CreativeTabs.tabAllSearch);
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List subItems) {
		for (int i = 0; i < 1000000; i++) {
			String name = RANDOM_STRING_MAKER.nextString();
			ItemStack itemStack = new ItemStack(itemIn);
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("name", name);
			itemStack.setTagCompound(nbt);
			//noinspection unchecked
			subItems.add(itemStack);
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return stack.getTagCompound().getString("name");
	}
}
