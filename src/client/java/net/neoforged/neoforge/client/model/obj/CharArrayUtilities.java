package net.neoforged.neoforge.client.model.obj;

final class CharArrayUtilities {
    private CharArrayUtilities() { }

    /// Finds the first whitespace character within the given character array.
    /// The array will be indexed from `start` to `start + length`.
    ///
    /// @param buffer The buffer to search in
    /// @param start The index to start searching from
    /// @param length The maximum number of characters to read
    public static int findFirstWhitespace(char[] buffer, int start, int length) {
        var maxIndex = start + length;

        if (maxIndex > buffer.length)
            throw new IllegalArgumentException(
                "The maximum possible index (" + maxIndex +
                    ") is larger than the length of the array (" +
                    buffer.length + ")");

        var foundIndex = -1;
        for (int i = start; i < maxIndex; i++) {
            if (buffer[i] == ' ') {
                foundIndex = i;
                break;
            }
        }

        return foundIndex;
    }

    /// Finds the first non-digit character within the given character array.
    /// The array will be indexed from `start` to `start + length`.
    ///
    /// @param buffer The buffer to search in
    /// @param start The index to start searching from
    /// @param length The maximum number of characters to read
    public static int findFirstNonDigit(char[] buffer, int start, int length) {
        var maxIndex = start + length;

        if (maxIndex > buffer.length)
            throw new IllegalArgumentException(
                "The maximum possible index (" + maxIndex +
                    ") is larger than the length of the array (" +
                    buffer.length + ")");

        var foundIndex = -1;
        for (int i = start; i < maxIndex; i++) {
            // ASCII puts the '/' and '.' characters between '-' and the digits 0
            // through 9, so we have to check for it in addition here.
            if (buffer[i] < '-' || buffer[i] > '9' || buffer[i] == '/' || buffer[i] == '.') {
                foundIndex = i;
                break;
            }
        }

        return foundIndex;
    }

    /// Finds the first non-float character within the given character array.
    /// The array will be indexed from `start` to `start + length`.
    ///
    /// @param buffer The buffer to search in
    /// @param start The index to start searching from
    /// @param length The maximum number of characters to read
    public static int findFirstNonFloat(char[] buffer, int start, int length) {
        var maxIndex = start + length;

        if (maxIndex > buffer.length)
            throw new IllegalArgumentException(
                "The maximum possible index (" + maxIndex +
                    ") is larger than the length of the array (" +
                    buffer.length + ")");

        var foundIndex = -1;
        for (int i = start; i < maxIndex; i++) {
            // ASCII puts the '/' character between '-', '.' and the digits 0
            // through 9, so we have to check for it in addition here.
            if (buffer[i] < '-' || buffer[i] > '9' || buffer[i] == '/') {
                foundIndex = i;
                break;
            }
        }

        return foundIndex;
    }
}