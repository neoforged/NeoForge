package net.neoforged.neoforge.client.event;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired in {@linkplain MouseHandler#turnPlayer() MouseHandler#turnPlayer()} when retrieving the values of {@linkplain Options#sensitivity() mouse sensitivity} and {@linkplain Options#smoothCamera cinematic camera}, prior to running calculations on these values.
 * 
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class CalculateTurnPlayerValuesEvent extends Event {

    private double mouseSensitivity;
    private boolean cinematicCameraEnabled;

    @ApiStatus.Internal
    public CalculateTurnPlayerValuesEvent(double mouseSensitivity, boolean cinematicCameraEnabled) {
        setMouseSensitivity(mouseSensitivity);
        setCinematicCameraEnabled(cinematicCameraEnabled);
    }

    /**
     * @return the raw {@linkplain Options#sensitivity() mouse sensitivity} value
     */
    public double getMouseSensitivity() {
        return mouseSensitivity;
    }

    /**
     * Sets the {@linkplain Options#sensitivity() mouse sensitivity} value.
     * 
     * @param mouseSensitivity the new {@linkplain Options#sensitivity() mouse sensitivity} value
     */
    public void setMouseSensitivity(double mouseSensitivity) {
        this.mouseSensitivity = mouseSensitivity;
    }

    /**
     * @return the raw {@linkplain Options#smoothCamera cinematic camera} value
     */
    public boolean getCinematicCameraEnabled() {
        return cinematicCameraEnabled;
    }

    /**
     * Sets the {@linkplain Options#smoothCamera cinematic camera} value.
     *
     * @param cinematicCameraEnabled the new {@linkplain Options#smoothCamera cinematic camera} value
     */
    public void setCinematicCameraEnabled(boolean cinematicCameraEnabled) {
        this.cinematicCameraEnabled = cinematicCameraEnabled;
    }

}
