package mezz.jei.gui.config;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.StackHelper;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.bookmarks.IngredientBookmark;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.load.registration.SubtypeRegistration;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import mezz.jei.library.plugins.vanilla.ingredients.ItemStackHelper;
import mezz.jei.test.lib.TestColorHelper;

import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BookmarkConfigTest {
	@BeforeAll
	public static void setup() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		Bootstrap.bootStrap();
	}

	@Test
	public void itemStackBookmarkSerializationPreservesCustomNbtNumberTypes() {
		CompoundTag customData = new CompoundTag();
		customData.putByte("byte", (byte) 1);
		customData.putShort("short", (short) 2);
		customData.putInt("int", 3);
		customData.putLong("long", 4L);
		customData.putFloat("float", 5.5F);
		customData.putDouble("double", 6.5D);

		ItemStack stack = new ItemStack(Items.CHEST);
		stack.getOrCreateTag()
			.put("custom", customData);

		IColorHelper colorHelper = new TestColorHelper();
		SubtypeRegistration subtypeRegistration = new SubtypeRegistration();
		new VanillaPlugin().registerItemSubtypes(subtypeRegistration);
		SubtypeManager subtypeManager = new SubtypeManager(subtypeRegistration.getInterpreters());
		StackHelper stackHelper = new StackHelper(subtypeManager);
		IIngredientHelper<ItemStack> itemStackHelper = new ItemStackHelper(subtypeManager, stackHelper, colorHelper);

		IngredientManagerBuilder builder = new IngredientManagerBuilder(subtypeManager, colorHelper);
		builder.register(
			VanillaTypes.ITEM_STACK,
			List.of(stack),
			itemStackHelper,
			new NoOpItemStackRenderer()
		);
		IIngredientManager ingredientManager = builder.build();

		String itemStackAsJson = stack.save(new CompoundTag()).toString();
		IBookmark bookmark = BookmarkConfig.loadItemStackBookmark(itemStackHelper, ingredientManager, itemStackAsJson);

		Assertions.assertInstanceOf(IngredientBookmark.class, bookmark);
		IngredientBookmark<?> ingredientBookmark = (IngredientBookmark<?>) bookmark;
		ItemStack decodedStack = ingredientBookmark.getIngredient()
			.getItemStack()
			.orElseThrow();

		Assertions.assertTrue(ItemStack.matches(stack, decodedStack), "Decoded item stack should match the original");
		CompoundTag decodedTag = decodedStack.getTag();
		Assertions.assertNotNull(decodedTag);
		CompoundTag decodedCustomData = decodedTag.getCompound("custom");
		assertTagId(decodedCustomData, "byte", Tag.TAG_BYTE);
		assertTagId(decodedCustomData, "short", Tag.TAG_SHORT);
		assertTagId(decodedCustomData, "int", Tag.TAG_INT);
		assertTagId(decodedCustomData, "long", Tag.TAG_LONG);
		assertTagId(decodedCustomData, "float", Tag.TAG_FLOAT);
		assertTagId(decodedCustomData, "double", Tag.TAG_DOUBLE);
	}

	private static void assertTagId(CompoundTag tag, String key, int expectedId) {
		Tag value = tag.get(key);
		Assertions.assertNotNull(value, "Expected custom data to contain key: " + key);
		Assertions.assertEquals((byte) expectedId, value.getId(), "Unexpected NBT tag type for key: " + key);
	}

	private static class NoOpItemStackRenderer implements IIngredientRenderer<ItemStack> {
		@Override
		public void render(GuiGraphics guiGraphics, ItemStack ingredient) {
		}

		@SuppressWarnings("removal")
		@Override
		public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
			return List.of();
		}

		@Override
		public void getTooltip(ITooltipBuilder tooltip, ItemStack ingredient, TooltipFlag tooltipFlag) {
		}
	}
}
