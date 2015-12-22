package mezz.jei.debug;

import java.util.Random;

public class RandomStringMaker {

	private static final char[] chars;

	static {
		StringBuilder stringBuilder = new StringBuilder();
		for (char ch = 'a'; ch <= 'z'; ++ch) {
			stringBuilder.append(ch);
		}
		chars = stringBuilder.toString().toCharArray();
	}

	private final Random random = new Random();
	private final char[] charBuffer;

	public RandomStringMaker(int length) {
		if (length < 1) {
			throw new IllegalArgumentException("length < 1: " + length);
		}
		charBuffer = new char[length];
	}

	public String nextString() {
		for (int i = 0; i < charBuffer.length; i++) {
			int charIndex = random.nextInt(chars.length);
			charBuffer[i] = chars[charIndex];
		}
		return new String(charBuffer);
	}
}
