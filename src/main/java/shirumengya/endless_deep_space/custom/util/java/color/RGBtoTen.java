package shirumengya.endless_deep_space.custom.util.java.color;

public class RGBtoTen {
	public static final String[] ROMAN_VALUES = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
	public static final int[] ARABIC_VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};

	public static int OutputResult (int r, int g, int b) {
		int result = (r * 256 * 256) + (g * 256) + b;
		return result;
	}

	public static String convertToRoman(int number) {
		StringBuilder roman = new StringBuilder();
		int index = 0;
		while (number > 0) {
			while (number >= ARABIC_VALUES[index]) {
				number -= ARABIC_VALUES[index];
				roman.append(ROMAN_VALUES[index]);
			}
			index++;
		}
		return roman.toString();
	}
}
