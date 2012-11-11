package at.ac.tuwien.detlef;

import android.app.Application;
import android.content.Context;

/**
 * Detlef.
 * 
 * <ol>
 * 	<li>A given name for males mainly used in Germany.</li>
 * 	<li>The code name for a gpodder.net client running on Android.</li>
 * </ol>
 * 
 * @author INSO_ASE_GROUP1 WS2012
 *
 */
public class Detlef extends Application {

	/** App context.	 */
    private static Context context;

    public void onCreate() {
        super.onCreate();
        Detlef.context = getApplicationContext();
    }
    
    /**
     * Provides a mean to make the application context available via a static method.
	 * http://stackoverflow.com/questions/2002288/static-way-to-get-context-on-android
     * @return The Application's context.
     */
    public static Context getAppContext() {
        return Detlef.context;
    }
	
}
