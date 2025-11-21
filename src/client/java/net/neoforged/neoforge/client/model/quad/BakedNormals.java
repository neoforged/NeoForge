package net.neoforged.neoforge.client.model.quad;

public sealed interface BakedNormals {
    BakedNormals UNSPECIFIED = new Slim(0);

    int normals0();

    int normals1();

    int normals2();

    int normals3();

    default int normals(int vert) {
        return switch (vert) {
            case 0 -> normals0();
            case 1 -> normals1();
            case 2 -> normals2();
            case 3 -> normals3();
            default -> throw new IndexOutOfBoundsException(vert);
        };
    }

    record Slim(int normals) implements BakedNormals {
        @Override
        public int normals0() {
            return normals;
        }

        @Override
        public int normals1() {
            return normals;
        }

        @Override
        public int normals2() {
            return normals;
        }

        @Override
        public int normals3() {
            return normals;
        }
    }

    record Full(int normals0, int normals1, int normals2, int normals3) implements BakedNormals { }

    static BakedNormals of(int normals0, int normals1, int normals2, int normals3) {
        if (normals0 == normals1 && normals0 == normals2 && normals0 == normals3) {
            return of(normals0);
        }
        return new Full(normals0, normals1, normals2, normals3);
    }

    static BakedNormals of(int normals) {
        return normals == 0 ? UNSPECIFIED : new Slim(normals);
    }
}
