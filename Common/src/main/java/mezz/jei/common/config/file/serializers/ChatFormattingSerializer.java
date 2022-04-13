package mezz.jei.common.config.file.serializers;

import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class ChatFormattingSerializer implements IConfigValueSerializer<List<ChatFormatting>> {
    public static final ChatFormattingSerializer INSTANCE = new ChatFormattingSerializer();

    private ChatFormattingSerializer() {}

    @Override
    public String serialize(List<ChatFormatting> value) {
        return value.stream()
            .map(ChatFormatting::name)
            .collect(Collectors.joining(" "));
    }

    @Override
    public DeserializeResult<List<ChatFormatting>> deserialize(String string) {
        string = string.trim();
        if (string.startsWith("\"") && string.endsWith("\"")) {
            string = string.substring(1, string.length() - 1);
        }
        if (string.isEmpty()) {
            return new DeserializeResult<>(List.of());
        }
        List<String> errors = new ArrayList<>();
        String[] strings = string.split(" ");
        List<ChatFormatting> result = Arrays.stream(strings)
            .<ChatFormatting>mapMulti((s, c) -> {
                ChatFormatting chatFormatting = ChatFormatting.getByName(s);
                if (chatFormatting != null) {
                    c.accept(chatFormatting);
                } else {
                    errors.add("No Chat Formatting found for name: '%s'".formatted(s));
                }
            })
            .toList();
        return new DeserializeResult<>(result, errors);
    }

    @Override
    public String getValidValuesDescription() {
        EnumSet<ChatFormatting> validFormatting = EnumSet.allOf(ChatFormatting.class);
        validFormatting.remove(ChatFormatting.RESET);

        StringJoiner validColorsJoiner = new StringJoiner(", ");
        StringJoiner validFormatsJoiner = new StringJoiner(", ");

        for (ChatFormatting chatFormatting : validFormatting) {
            String name = chatFormatting.getName();
            if (chatFormatting.isColor()) {
                validColorsJoiner.add(name);
            } else if (chatFormatting.isFormat()) {
                validFormatsJoiner.add(name);
            }
        }
        String validColors = validColorsJoiner.toString();
        String validFormats = validFormatsJoiner.toString();

        return """
            A chat formatting string.
            Use these formatting colors:
            %s
            With these formatting options:
            %s""".formatted(validColors, validFormats);
    }
}
