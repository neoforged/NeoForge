package net.neoforged.testframework.conf;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ClientConfiguration(int toggleOverlayKey, int openManagerKey) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int toggleOverlayKey;
        private int openManagerKey;

        public Builder toggleOverlayKey(int toggleOverlayKey) {
            this.toggleOverlayKey = toggleOverlayKey;
            return this;
        }

        public Builder openManagerKey(int openManagerKey) {
            this.openManagerKey = openManagerKey;
            return this;
        }

        public ClientConfiguration build() {
            return new ClientConfiguration(toggleOverlayKey, openManagerKey);
        }
    }
}
