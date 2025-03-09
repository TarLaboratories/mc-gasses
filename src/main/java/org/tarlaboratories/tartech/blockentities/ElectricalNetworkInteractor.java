package org.tarlaboratories.tartech.blockentities;

import net.minecraft.block.entity.BlockEntity;
import org.tarlaboratories.tartech.ElectricalNetwork;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An interface that should be implemented by inheritors of {@link BlockEntity} if they want to interact with {@link ElectricalNetwork}
 */
public interface ElectricalNetworkInteractor {
    double getPowerDraw();

    default void receivePayload(Object payload) {}

    /**
     * Should set the function to be called when the power draw of this interactor is modified
     * @param callback the function that should be called, pass {@code this} as the parameter
     */
    void setModifiedCallback(Consumer<ElectricalNetworkInteractor> callback);

    /**
     * Should set the function to be used to get the {@link ElectricalNetwork}
     * @param getter the function that returns the network object
     */
    void setElectricalNetworkGetter(Supplier<ElectricalNetwork> getter);
}
