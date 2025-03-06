package org.tarlaboratories.tartech.blocks.cables;

public interface ElectricalNetworkInteractor extends CableConnectable {
    double getPowerDraw();
    default void receivePayload(Object payload) {}
}
