/* *************************************************************************
 *  Copyright 2012 The detlef developers                                   *
 *                                                                         *
 *  This program is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU General Public License as published by   *
 *  the Free Software Foundation, either version 2 of the License, or      *
 *  (at your option) any later version.                                    *
 *                                                                         *
 *  This program is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *  GNU General Public License for more details.                           *
 *                                                                         *
 *  You should have received a copy of the GNU General Public License      *
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
 ************************************************************************* */


package at.ac.tuwien.detlef.settings;

import android.content.Context;
import android.util.Log;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.gpodder.NoDataResultHandler;
import at.ac.tuwien.detlef.gpodder.ReliableResultHandler;
import at.ac.tuwien.detlef.gpodder.responders.SynchronousSyncResponder;

/**
 * A {@link ConnectionTester} that verifies {@link GpodderSettings} data against
 * the public API of gpodder.net.
 * 
 * <p>This implementation needs a {@link Context} to be injected via {@link #setContext(Context)} in
 * order to work properly.</p>
 * 
 * @author moe
 *
 */
public class ConnectionTesterGpodderNet implements ConnectionTester {

    /** Tag for logging. */
    private static final String TAG = "ConnectionTesterGpodderNet"; 
    
    /**    The result of the authentication against the gpodder.net service. */
    private boolean gpodderNetResult = false;
    
    /**
     * The raw error string that is returned by the {@link NoDataResultHandler}.
     */
    private String resultErrStr = "";
    
    /**
     * This implementation relies on {@link SynchronousSyncResponder}. Therefore we need
     * the application context for this to work.
     */
    private Context context;
    
    
    public boolean testConnection(GpodderSettings settings)
            throws InterruptedException, GpodderConnectionException {
        
        Log.d(TAG, "testConnection(" + settings + ")");
        
        SynchronousSyncResponder syncResponder = new SynchronousSyncResponder(getContext());

        GPodderSync gpodderSync = new GPodderSync(syncResponder);
        
        gpodderSync.setHostname(settings.getApiHostname());
        
        ConnectionTesterResultHandler ctrh = new ConnectionTesterResultHandler();
        ctrh.registerReceiver(this);

        gpodderSync.addAuthCheckJob(
            settings.getUsername(),
            settings.getPassword(),
            ctrh
        );
        
        Log.d(TAG, "wating for completion ...");
        syncResponder.waitForCompletion();
        Log.d(TAG, "done.");
        
        if (is401Unauthorized()) {
            throw new GpodderConnectionException();
        }
        
        return gpodderNetResult;
    }

    private static class ConnectionTesterResultHandler
    extends ReliableResultHandler<ConnectionTesterGpodderNet>
    implements NoDataResultHandler<ConnectionTesterGpodderNet> {

        @Override
        public void handleFailure(int errCode, String errStr) {
            Log.d(TAG, String.format("failure! errCode: %d errStr: %s", errCode, errStr));
            
            getRcv().resultErrStr = errStr;
            getRcv().gpodderNetResult = false;
        }

        @Override
        public void handleSuccess() {
            Log.d(TAG, String.format("success!"));
            getRcv().gpodderNetResult = true;
        }
    }

    /**
     * @return true if the response from gpodder.net was 401 UNAUTHORIZED,
     *     false else.
     */
    private boolean is401Unauthorized() {
        return !gpodderNetResult && resultErrStr.toLowerCase().contains("401 unauthorized");
    }

    /**
     * @return The context set by#{@link ConnectionTesterGpodderNet#setContext(Context)} or null
     *     if no context has been set.
     */
    public Context getContext() {
        return context;
    }
    
    /**
     * Sets the {@link Context} which is necessary for the authentication check.
     * @param pContext
     * @return Fluent Interface
     */
    public ConnectionTesterGpodderNet setContext(Context pContext) {
        context = pContext;
        return this;
    }

}
