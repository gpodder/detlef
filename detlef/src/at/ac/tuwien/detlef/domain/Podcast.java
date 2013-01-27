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

package at.ac.tuwien.detlef.domain;

import java.io.Serializable;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;

import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * Dummy class to display initial test podcast content until the actual podcast
 * classes are available.
 */
public class Podcast implements IPodcast, Serializable, Parcelable {

    /**
     * default serialUID added.
     */
    private static final long serialVersionUID = 1L;

    private static int ICON_WIDTH = 80;
    private static int ICON_HEIGHT = 80;

    private long id;

    private String title = "This is a podcast title";

    private String description = "This is a podcast description.";

    private String logoUrl = "www.dummyurl.com";

    private String logoFilePath;

    private String url = "www.dummyurl.com";

    private long lastUpdate;

    private boolean localAdd;

    private boolean localDel;

    private transient Drawable logoIcon;

    private int logoDownloaded = 0;

    public Podcast() {
    }

    public Podcast(IPodcast p) {
        setUrl(p.getUrl());
        setTitle(p.getTitle());
        setDescription(p.getDescription());
        setLogoUrl(p.getLogoUrl());
    }

    /**
     * Creates the logo icon.
     *
     * @return
     */
    private Drawable createLogoIcon() {
        if (getLogoFilePath() != null) {
            Drawable image = Drawable.createFromPath(getLogoFilePath());
            if (image != null) {
                Bitmap d = ((BitmapDrawable) image).getBitmap();
                Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, ICON_WIDTH, ICON_HEIGHT, false);
                return new BitmapDrawable(Detlef.getAppContext().getResources(), bitmapOrig);
            }
        }
        return Detlef.getAppContext().getResources().getDrawable(R.drawable.ic_feed_icon);
    }

    /**
     * returns the icon for the podcast.
     *
     * @return
     */
    public Drawable getLogoIcon() {
        if (logoIcon == null) {
            logoIcon = createLogoIcon();
        }
        return logoIcon;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLogoUrl() {
        return logoUrl;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public Podcast setUrl(String urlIn) {
        this.url = urlIn;
        return this;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;

    }

    @Override
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public long getId() {
        return id;
    }

    public Podcast setId(long idIn) {
        this.id = idIn;
        return this;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public Podcast setLastUpdate(long lastUpdateIn) {
        this.lastUpdate = lastUpdateIn;
        return this;
    }

    public String getLogoFilePath() {
        return logoFilePath;
    }

    public Podcast setLogoFilePath(String logoFilePathIn) {
        this.logoFilePath = logoFilePathIn;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isLocalAdd() {
        return localAdd;
    }

    public void setLocalAdd(boolean localAdd) {
        this.localAdd = localAdd;
    }

    public boolean isLocalDel() {
        return localDel;
    }

    public void setLocalDel(boolean localDel) {
        this.localDel = localDel;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(logoUrl);
        dest.writeString(logoFilePath);
        dest.writeString(url);
        dest.writeLong(lastUpdate);
        dest.writeBooleanArray(new boolean[] {
                localAdd, localDel
        });
    }

    public int getLogoDownloaded() {
        return logoDownloaded;
    }

    public Podcast setLogoDownloaded(int logoDownloadedIn) {
        this.logoDownloaded = logoDownloadedIn;
        logoIcon = createLogoIcon();
        return this;
    }

    public static final Parcelable.Creator<Podcast> CREATOR = new Creator<Podcast>() {
        @Override
        public Podcast[] newArray(int size) {
            return new Podcast[size];
        }

        @Override
        public Podcast createFromParcel(Parcel source) {
            Podcast ret = new Podcast();
            ret.setId(source.readLong());
            ret.setTitle(source.readString());
            ret.setDescription(source.readString());
            ret.setLogoUrl(source.readString());
            ret.setLogoFilePath(source.readString());
            ret.setUrl(source.readString());
            ret.setLastUpdate(source.readLong());
            boolean localAddDel[] = new boolean[2];
            source.readBooleanArray(localAddDel);
            ret.setLocalAdd(localAddDel[0]);
            ret.setLocalDel(localAddDel[1]);
            return ret;
        }
    };
}
