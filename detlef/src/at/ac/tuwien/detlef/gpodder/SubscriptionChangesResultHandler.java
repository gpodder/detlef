package at.ac.tuwien.detlef.gpodder;

import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;

/**
 * This is the callback interface for jobs which return an object of type {@link
 * EnhancedSubscriptionChanges} on success.
 * @author ondra
 */
public interface SubscriptionChangesResultHandler extends ResultHandler {
    /**
     * Called to handle a successful fetching of subscription changes.
     * @param chgs Subscription changes that have been successfully fetched.
     */
    void handleSuccess(EnhancedSubscriptionChanges chgs);
}
