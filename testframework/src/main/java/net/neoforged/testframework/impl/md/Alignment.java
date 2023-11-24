package net.neoforged.testframework.impl.md;

public enum Alignment {
    CENTER {
        @Override
        public String align(String str) {
            return ":" + str + ":";
        }

        @Override
        public String fillAligned(String value, String fill, int length) {
            if (value.length() >= length) return value;
            boolean left = true;
            while (value.length() < length) {
                if (left) {
                    value = LEFT.fillAligned(value, fill, value.length() + 1);
                } else {
                    value = RIGHT.fillAligned(value, fill, value.length() + 1);
                }
                left = !left;
            }
            return value;
        }
    },
    LEFT {
        @Override
        public String align(String str) {
            return Table.surroundWith(str, Table.WHITESPACE);
        }

        @Override
        public String fillAligned(String value, String fill, int length) {
            final int valLength = value.length();
            if (valLength >= length) return value;
            return value + fill.repeat(length - valLength);
        }
    },
    RIGHT {
        @Override
        public String align(String str) {
            return Table.WHITESPACE + str + ":";
        }

        @Override
        public String fillAligned(String value, String fill, int length) {
            final int valLength = value.length();
            if (valLength >= length) return value;
            return fill.repeat(length - valLength) + value;
        }
    };

    public abstract String align(String str);
    public abstract String fillAligned(String value, String fill, int length);
}
