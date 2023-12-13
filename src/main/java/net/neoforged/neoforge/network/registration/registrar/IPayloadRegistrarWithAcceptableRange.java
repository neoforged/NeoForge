package net.neoforged.neoforge.network.registration.registrar;

public interface IPayloadRegistrarWithAcceptableRange extends IVersionedPayloadRegistrar {
    
    IPayloadRegistrarWithAcceptableRange withMinimalVersion(int min);
    
    IPayloadRegistrarWithAcceptableRange withMaximalVersion(int max);
    
    IPayloadRegistrarWithAcceptableRange optional();
    
    default IPayloadRegistrarWithAcceptableRange withAcceptableRange(int min, int max) {
        return withMinimalVersion(min).withMaximalVersion(max);
    }
}
