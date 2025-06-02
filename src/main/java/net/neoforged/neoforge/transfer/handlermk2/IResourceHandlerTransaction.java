package net.neoforged.neoforge.transfer.handlermk2;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Range;

public interface IResourceHandlerTransaction<T extends IResource> {
    /**
     * An index in synonymous with "slot", "tank", "buffer", etc.
     *
     * @return The number of indices this handler manages.
     */
    int size();

    /**
     * @param index The index to get the resource from.
     * @return The resource at the given index.
     */
    T getResource(int index);

    /**
     * @param index The index to get the amount from.
     * @return The amount of the resource at the given index. A range of 0 to {@value ResourceHandlerUtil#MAX_RESOURCE_SIZE}
     */
    int getAmount(int index);

    /**
     * Gets the theoretical maximum amount that the given index can hold of "any" resource. If there is something in the slot, it is valid to use its max bounds.
     * <p>
     * Needed method given that some resources may not have an "empty" instance.
     *
     * @param index The index to get the limit from.
     * @return The limit at the given index. A range of 0 to {@value ResourceHandlerUtil#MAX_RESOURCE_SIZE}
     */
    int getCapacity(int index);

    /**
     * Gets the maximum amount that the given index can have of the given resource. If your capacity is constant, no matter
     * the resource, you can just return the result of {@link #getCapacity(int)}. This is historically the case for fluids,
     * but not for items.
     *
     * @param index    The index to get the limit from.
     * @param resource The resource to get the limit for. If empty, this should defer to {@link #getCapacity(int)}
     * @return The limit of the resource at the given index. A range of 0 to {@value ResourceHandlerUtil#MAX_RESOURCE_SIZE}
     */
    int getCapacity(int index, T resource);

    /**
     * Checks if the given resource is allowed to be inserted into the handler at the given index. This is typically called in the {@link #insert(int, IResource, int, TransferAction)} implementation.
     *
     * @param index    The index to check.
     * @param resource The resource to check.
     * @return True if the resource can be inserted, false otherwise.
     */
    boolean isValid(int index, T resource);

    /**
     * Checks if the given index allows insertion of a resource, regardless of the state of the handler. Also meaning this value is non-dynamic.
     * <p>
     * Intended use is for something like a pipe graph lookup to be able to reduce the runtime workload on handlers that can never do a specific operation.
     * <p>
     * As long as the handler could, under the right conditions, allow a resource to be inserted into the given index,
     * this should return true. To be clear, this value is assumed to be constant throughout the life-time of the handler and does <b>not</b> control the handler's logic in any way.
     * <h5>IMPORTANT:</h5>
     * Returning false, will not inherently prevent something from calling insert or change the result of that call,
     * so you will still need to handle those scenarios.
     * This is to allow things like logistics (pipes, searches, etc.) to be able to infer what it can do with the handler
     * well before actually operating.
     * <p>
     * It is also advised to not use the result of this call in insert if the lookup is complex.
     * <p>
     * If your handler can change size dynamically, then it may be wise to return true for this unless you know for certain a particular index would never be insertable to.
     *
     * @param index The index to check.
     * @return True if the resource can be inserted, false otherwise.
     */
    boolean allowsInsertion(int index);

    /**
     * Checks if the handler allows insertion into at least one index, regardless of the state of the handler. Also meaning this value is non-dynamic.
     * <h5>IMPORTANT:</h5> This does not control your handler's logic in any way. Returning false, will not inherently prevent something from calling insert or change the result of that call,
     * so you will still need to handle those scenarios. This is to allow things like logistics (pipes, searches, etc.) to be able to infer what it can do with the handler
     * before actually operating.
     * <p>
     * It is also advised to not use the result of this call in insert.
     *
     * @return True if a resource can be inserted, false otherwise.
     */
    default boolean allowsInsertion() {
        for (int i = 0; i < size(); i++) {
            if (allowsInsertion(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given index allows extraction of a resource, regardless of the state of the handler. Also meaning this value is non-dynamic.
     * <p>
     * As long as the handler could, under the right conditions, allow a resource to be extracted from the given index,
     * this should return true.
     * <p>
     * <h5>IMPORTANT:</h5> This does not control your handler's logic in any way. Returning false, will not inherently prevent something from calling extract or change the result of that call,
     * so you will still need to handle those scenarios. This is to allow things like logistics (pipes, searches, etc.) to be able to infer what it can do with the handler
     * before actually operating.
     * <p>
     * It is also advised to not use the result of this call in extract if the lookup is complex.
     * *<p>
     * If your handler can change size dynamically, then it may be wise to return true for this unless you know for certain a particular index would never be extractable from.
     *
     * @param index The index to check.
     * @return True if the resource can be extracted, false otherwise.
     */
    boolean allowsExtraction(int index);

    /**
     * Checks if the handler allows extraction from at least one index, regardless of the state of the handler. Also meaning this value is non-dynamic.
     * <h5>IMPORTANT:</h5> This does not control your handler's logic in any way. Returning false, will not inherently prevent something from calling extract or change the result of that call,
     * so you will still need to handle those scenarios. This is to allow things like logistics (pipes, searches, etc.) to be able to infer what it can do with the handler
     * before actually operating.
     * <p>
     * It is also advised to not use the result of this call in extract if the lookup is complex.
     *
     * @return True if a resource can be extracted, false otherwise.
     */
    default boolean allowsExtraction() {
        for (int i = 0; i < size(); i++) {
            if (allowsExtraction(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Inserts a given amount of the resource into the handler at the given index.
     *
     * @param index    The index to insert the resource into.
     * @param resource The resource to insert.
     * @param amount   The amount of the resource to insert. A range of 1 to {@value ResourceHandlerUtil#MAX_RESOURCE_SIZE}
     * @param action   The kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount of the resource that was (or would have been, if simulated) inserted. A range of 0 to {@value ResourceHandlerUtil#MAX_RESOURCE_SIZE}
     */
    int insert(int index, T resource, @Range(from = 1, to = ResourceHandlerUtil.MAX_RESOURCE_SIZE) int amount, Transaction action);

    /**
     * Inserts a given amount of the resource into the handler. Distribution of the resource is up to the handler.
     * <p>
     * Implementation advice, don't just have this call {@link #insert(int, IResource, int, TransferAction)}, as you may needlessly re-check validations.
     * See {@link ResourceStorageHandler#insert(IResource, int, TransferAction) ResourceStorage.insertBehaviour} for an example.
     *
     * @param resource The resource to insert.
     * @param amount   The amount of the resource to insert. A range of 1 to {@value ResourceHandlerUtil#MAX_RESOURCE_SIZE}
     * @param action   The kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount (range from 0 to {@value ResourceHandlerUtil#MAX_RESOURCE_SIZE}) of the resource that was (or would have been, if simulated) inserted.
     */
    int insert(T resource, @Range(from = 1, to = ResourceHandlerUtil.MAX_RESOURCE_SIZE) int amount, Transaction action);

    /**
     * Extracts a given amount of the resource from the handler at the given index.
     *
     * @param index    The index to extract the resource from.
     * @param resource The resource to extract.
     * @param amount   The amount of the resource to extract. A range of 1 to {@value ResourceHandlerUtil#MAX_RESOURCE_SIZE}
     * @param action   The kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount (range from 0 to {@value ResourceHandlerUtil#MAX_RESOURCE_SIZE}) of the resource that was (or would have been, if simulated) extracted.
     */
    int extract(int index, T resource, @Range(from = 1, to = ResourceHandlerUtil.MAX_RESOURCE_SIZE) int amount, Transaction action);

    /**
     * Extracts a given amount of the resource from the handler. Distribution of the resource is up to the handler.
     * <p>
     * Implementation advice, don't just have this call {@link #extract(int, IResource, int, TransferAction)}, as you may needlessly re-check validations.
     * See {@link ResourceStorageHandler#extract(IResource, int, TransferAction) ResourceStorage.extractBehaviour} for an example.
     *
     * @param resource The resource to extract.
     * @param amount   The amount of the resource to extract. A range of 1 to {@value ResourceHandlerUtil#MAX_RESOURCE_SIZE}
     * @param action   The kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount (range from 0 to {@value ResourceHandlerUtil#MAX_RESOURCE_SIZE}) of the resource that was (or would have been, if simulated) extracted.
     */
    int extract(T resource, @Range(from = 1, to = ResourceHandlerUtil.MAX_RESOURCE_SIZE) int amount, Transaction action);


    static <T extends IResource> Class<IResourceHandlerTransaction<T>> asClass() {
        //noinspection unchecked
        return (Class< IResourceHandlerTransaction<T>>) (Object) IResourceHandlerTransaction.class;
    }
}
