package at.ac.tuwien.detlef.gpodder.plumbing;

import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.gpodder.plumbing.GpoNetClientInfo;
import at.ac.tuwien.detlef.gpodder.plumbing.PodderServiceCallback;

/**
 * Interface to connect to the {@link PodderService}.
 */
oneway interface PodderServiceInterface
{
    /**
     * Download a list of a user's podcasts from a gpodder.net-compatible service.
     * @param cb Callback object to send responses to.
     * @param reqId The ID of the request; will be passed to the callback unchanged.
     * @param cinfo The information about the client. Must contain username, password, hostname and
     * deviceName.
     */
    void downloadPodcastList(in PodderServiceCallback cb, int reqId, in GpoNetClientInfo cinfo);

    /**
     * Download a list of a user's subscription changes since a specific timestamp from a
     * gpodder.net-compatible service.
     * @param cb Callback object to send responses to.
     * @param reqId The ID of the request; will be passed to the callback unchanged.
     * @param cinfo The information about the client. Must contain username, password, hostname and
     * deviceName.
     * @param ts Timestamp since when to look for changes.
     */
    void downloadChangesSince(in PodderServiceCallback cb, int reqId, in GpoNetClientInfo cinfo, long ts);

    /**
     * Update a user's subscriptions on a gpodder.net-compatible service.
     * @param cb Callback object to send responses to.
     * @param reqId The ID of the request; will be passed to the callback unchanged.
     * @param cinfo The information about the client. Must contain username, password, hostname and deviceName.
     * @param changes The changes to push to the gpodder.net-compatible service.
     */
    void updateSubscriptions(in PodderServiceCallback cb, int reqId, in GpoNetClientInfo cinfo,
        in EnhancedSubscriptionChanges changes);
        
    /**
     * Gets a Podcast object for a podcast
     *
     * @param urls The urls of the podcast to fetch
     */
     void getPodcastInfo(in PodderServiceCallback cb, int reqId, in GpoNetClientInfo cinfo, in List<String> urls);
}
