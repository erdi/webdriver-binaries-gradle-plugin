package com.energizedwork.gradle.webdriver;

public class StringUtils {
    /**
     * Convenience method to uncapitalize the first letter of a CharSequence
     * (typically the first letter of a word)
     *
     * @param self The CharSequence to uncapitalize
     * @return A String containing the uncapitalized toString() of the CharSequence
     */
    public static String uncapitalize(CharSequence self) {
        if ( self == null ) { return null; }
        if (self.length() == 0) return "";
        return "" + Character.toLowerCase(self.charAt(0)) + self.subSequence(1, self.length());
    }
}
