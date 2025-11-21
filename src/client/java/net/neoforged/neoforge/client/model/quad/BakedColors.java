package net.neoforged.neoforge.client.model.quad;

public sealed interface BakedColors {
    BakedColors DEFAULT = new Slim(0xFFFFFFFF);

    int color0();

    int color1();

    int color2();

    int color3();

    default int color(int vert) {
        return switch (vert) {
            case 0 -> color0();
            case 1 -> color1();
            case 2 -> color2();
            case 3 -> color3();
            default -> throw new IndexOutOfBoundsException(vert);
        };
    }

    record Slim(int color) implements BakedColors {
        @Override
        public int color0() {
            return color;
        }

        @Override
        public int color1() {
            return color;
        }

        @Override
        public int color2() {
            return color;
        }

        @Override
        public int color3() {
            return color;
        }
    }

    record Full(int color0, int color1, int color2, int color3) implements BakedColors { }

    static BakedColors of(int color0, int color1, int color2, int color3) {
        if (color0 == color1 && color0 == color2 && color0 == color3) {
            return of(color0);
        }
        return new Full(color0, color1, color2, color3);
    }

    static BakedColors of(int color) {
        return color == 0xFFFFFFFF ? DEFAULT : new Slim(color);
    }
}
