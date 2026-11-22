package mezz.jei.test;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.common.chat.JeiChatItemLinks;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.common.util.StackHelper;
import mezz.jei.library.ingredients.IngredientManager;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.load.registration.SubtypeRegistration;
import mezz.jei.library.plugins.vanilla.ingredients.ItemStackCodecs;
import mezz.jei.library.plugins.vanilla.ingredients.ItemStackHelper;
import mezz.jei.library.render.ItemStackRenderer;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentAwareIngredientListTest {
	@BeforeAll
	static void bootstrap() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		Bootstrap.bootStrap();
		BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(VanillaRegistries.createWorldLookup())
			.forEach(DataComponentInitializers.PendingComponents::apply);
	}

	@BeforeEach
	void setRegistryAccess() {
		RegistryUtil.setRegistryAccess(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
	}

	@AfterEach
	void clearRegistryAccess() {
		RegistryUtil.setRegistryAccess(null);
	}

	@Test
	void plainItemLinksFitChatInputAndResolveAfterFraming() {
		ItemStack diamond = new ItemStack(Items.DIAMOND);
		Fixture fixture = fixture(List.of(diamond), new SubtypeRegistration());
		var ingredient = fixture.manager.createTypedIngredient(VanillaTypes.ITEM_STACK, diamond, false).orElseThrow();
		String marker = JeiChatItemLinks.createLinkMarker(ingredient, fixture.manager);
		assertTrue(marker.length() <= 256, () -> "Marker exceeds the chat input limit: " + marker.length());
		var parsed = JeiChatItemLinks.parse(marker, link -> Optional.of("Diamond")).getSiblings().getFirst();
		var command = assertInstanceOf(ClickEvent.RunCommand.class, parsed.getStyle().getClickEvent());
		var link = JeiChatItemLinks.parseShowRecipeCommand(command.command()).orElseThrow();
		var resolved = JeiChatItemLinks.resolveTypedIngredient(link, fixture.manager).orElseThrow().getItemStack().orElseThrow();
		assertTrue(ItemStack.isSameItemSameComponents(diamond, resolved));
	}

	@Test
	void legacyPlainItemLinksResolveToRegisteredItems() {
		ItemStack diamond = new ItemStack(Items.DIAMOND);
		Fixture fixture = fixture(List.of(diamond), new SubtypeRegistration());
		var link = JeiChatItemLinks.parseCommandArgument("v1:item_stack;minecraft:diamond").orElseThrow();
		var resolved = JeiChatItemLinks.resolveTypedIngredient(link, fixture.manager).orElseThrow().getItemStack().orElseThrow();
		assertTrue(ItemStack.isSameItemSameComponents(diamond, resolved));
	}

	@ParameterizedTest
	@MethodSource("chatSubtypeKeys")
	void chatLinksPreserveArbitrarySubtypeKeys(Object subtypeKey) {
		SubtypeRegistration registration = new SubtypeRegistration();
		registration.registerSubtypeInterpreter(Items.APPLE, (stack, context) -> subtypeKey);
		ItemStack sample = custom("chat]link%5D");
		sample.set(DataComponents.CUSTOM_NAME, Component.literal("Blueberry [Linked] %5D"));
		Fixture fixture = fixture(List.of(sample), registration);
		var ingredient = fixture.manager.createTypedIngredient(VanillaTypes.ITEM_STACK, sample, false).orElseThrow();

		String marker = JeiChatItemLinks.createLinkMarker(ingredient, fixture.manager);
		var parsed = JeiChatItemLinks.parse(marker, link -> Optional.of(sample.getHoverName().getString())).getSiblings().getFirst();
		assertEquals("[" + sample.getHoverName().getString() + "]", parsed.getString());
		var command = assertInstanceOf(ClickEvent.RunCommand.class, parsed.getStyle().getClickEvent());
		var link = JeiChatItemLinks.parseShowRecipeCommand(command.command()).orElseThrow();
		var resolved = JeiChatItemLinks.resolveTypedIngredient(link, fixture.manager).orElseThrow().getItemStack().orElseThrow();
		assertTrue(ItemStack.isSameItemSameComponents(sample, resolved));
	}

	private static Stream<Object> chatSubtypeKeys() {
		ItemStack sample = custom("chat]link%5D");
		return Stream.of(
			"text] with literal %5D + and 雪",
			List.of("nested]", "literal%5D", List.of("+", "雪")),
			sample.getComponents(),
			sample.getComponentsPatch()
		);
	}

	private static Fixture fixture(List<ItemStack> items, SubtypeRegistration registration) {
		SubtypeManager subtypes = new SubtypeManager(registration.getInterpreters());
		StackHelper stackHelper = new StackHelper(subtypes);
		ItemStackHelper helper = new ItemStackHelper(stackHelper, DummyColorHelper.INSTANCE);
		return new Fixture(builder(subtypes, helper, items).build(), helper, stackHelper);
	}

	private static IngredientManagerBuilder builder(SubtypeManager subtypes, ItemStackHelper helper, List<ItemStack> items) {
		IngredientManagerBuilder builder = new IngredientManagerBuilder(subtypes, DummyColorHelper.INSTANCE, ContextMap.EMPTY);
		builder.register(VanillaTypes.ITEM_STACK, items, helper, new ItemStackRenderer(), ItemStackCodecs.createStrictSingleItemCodec());
		return builder;
	}

	private static ItemStack stack(Item item) {
		// Use direct holders because unit tests do not bind the game's default item components.
		return new ItemStack(Holder.direct(item, DataComponents.COMMON_ITEM_COMPONENTS));
	}

	private static ItemStack custom(String variant) {
		ItemStack stack = stack(Items.APPLE);
		stack.set(DataComponents.CUSTOM_NAME, Component.literal("Blueberry"));
		CompoundTag data = new CompoundTag();
		data.putString("variant", variant);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
		return stack;
	}

	private record Fixture(IngredientManager manager, ItemStackHelper helper, StackHelper stackHelper) {
	}
}
