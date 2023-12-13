package net.neoforged.neoforge.network.registration.registrar;

public interface IVersionedPayloadRegistrar extends IFlowBasedPayloadRegistrar {
    
    IPayloadRegistrarWithAcceptableRange withVersion(int version);
}
