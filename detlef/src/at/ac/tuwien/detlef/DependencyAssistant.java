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



package at.ac.tuwien.detlef;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import at.ac.tuwien.detlef.db.EpisodeDBAssistant;
import at.ac.tuwien.detlef.db.EpisodeDBAssistantImpl;
import at.ac.tuwien.detlef.db.PodcastDBAssistant;
import at.ac.tuwien.detlef.db.PodcastDBAssistantImpl;
import at.ac.tuwien.detlef.domain.DeviceId;
import at.ac.tuwien.detlef.download.DetlefDownloadManager;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.settings.ConnectionTester;
import at.ac.tuwien.detlef.settings.DeviceRegistrator;
import at.ac.tuwien.detlef.settings.DeviceRegistratorException;
import at.ac.tuwien.detlef.settings.GpodderConnectionException;
import at.ac.tuwien.detlef.settings.GpodderSettings;
import at.ac.tuwien.detlef.settings.GpodderSettingsDAO;
import at.ac.tuwien.detlef.settings.GpodderSettingsDAOAndroid;
import at.ac.tuwien.detlef.util.GUIUtils;

/**
 * This class acts as a central point for setting and retrieving service
 * classes.
 * 
 * @author johannes
 */
public class DependencyAssistant {

    private static DependencyAssistant dependencyAssistant =
            new DependencyAssistant();

    private static final EpisodeDBAssistant EPISODE_DB_ASSISTANT =
            new EpisodeDBAssistantImpl();

    private static final PodcastDBAssistant PODCAST_DB_ASSISTANT =
            new PodcastDBAssistantImpl();

    private static DetlefDownloadManager downloadManager = null;

    private static final GUIUtils GUI_UTILS = new GUIUtils();

    private static final String TAG = DependencyAssistant.class.getCanonicalName();

    public GUIUtils getGuiUtils() {
        return GUI_UTILS;
    }

    /**
     * @return Gets the quasi-singleton GPodderSync instance for this program.
     */
    public GPodderSync getGPodderSync() {
        return null;
    }

    /**
     * @return Gets the quasi-singleton PodcastDBAssistant instance for this
     *         program.
     */
    public PodcastDBAssistant getPodcastDBAssistant() {
        return PODCAST_DB_ASSISTANT;
    }

    /**
     * @return Gets the quasi-singleton EpisodeDBAssistant instance for this
     *         program.
     */
    public EpisodeDBAssistant getEpisodeDBAssistant() {
        return EPISODE_DB_ASSISTANT;
    }

    /**
     * Returns the download manager instance.
     */
    public synchronized DetlefDownloadManager getDownloadManager(Context context) {
        if (downloadManager == null) {
            downloadManager = new DetlefDownloadManager(context);
        }
        return downloadManager;
    }

    /**
     * @param context The {@link Context}. This is needed in order to be able to
     *            access Android's system settings. Usually this will be the
     *            current {@link Activity}.
     * @return Gets the {@link GpodderSettings gpodder.net settings instance}
     *         that provides the user name, password and device name settings.
     */
    public GpodderSettings getGpodderSettings(Context context) {


        return getGpodderSettingsDAO(context).getSettings();

    }

    /**
     * The DAO class that is used to store and retrieve {@link GpodderSettings}.
     * @param context The current {@link Context}.
     * @return an implementation of {@link GpodderSettingsDAO}.
     * @throws IllegalArgumentException In case context is null.
     */
    public GpodderSettingsDAO getGpodderSettingsDAO(Context context) {

        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }

        Log.d(TAG, "sharedPreferences:" + PreferenceManager.getDefaultSharedPreferences(context));

        HashMap<String, Object> dependencies = new HashMap<String, Object>();
        dependencies.put(
            "sharedPreferences",
            PreferenceManager.getDefaultSharedPreferences(context)
        );

        GpodderSettingsDAO gpodderSettingsDAO = new GpodderSettingsDAOAndroid();
        gpodderSettingsDAO.setDependencies(dependencies);

        return gpodderSettingsDAO;
    }

    /**
     *
     * @return TODO right now this is only a mock.
     */
    public DeviceRegistrator getDeviceRegistrator() {
        return new DeviceRegistrator() {

            @Override
            public DeviceRegistrator registerNewDeviceId(DeviceId deviceId)
                    throws DeviceRegistratorException {

                try {
                    Thread.sleep(3333);
                } catch (InterruptedException e) {
                }

                return this;
            }

        };
    }

    /**
     * @return The {@link ConnectionTester} that verifies a set of of
     *         {@link GpodderSettings}.
     */
    public ConnectionTester getConnectionTester() {
        //return new ConnectionTesterGpodderNet().setContext(Detlef
        //        .getAppContext());

        return new ConnectionTester() {

            @Override
            public boolean testConnection(GpodderSettings settings) throws InterruptedException,
                    GpodderConnectionException {
                return true;
            }
        };

    }

    public static DependencyAssistant getDependencyAssistant() {
        return dependencyAssistant;
    }

    /**
     * Overwrites the default {@link DependencyAssistant} with a custom one. By
     * using this method you can easily replace parts of the Application with
     * Mocks which is useful for testing.
     * 
     * @param pDependencyAssistant
     */
    public static void setDependencyAssistant(
            DependencyAssistant pDependencyAssistant) {
        dependencyAssistant = pDependencyAssistant;
    }

}
