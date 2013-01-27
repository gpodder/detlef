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

package at.ac.tuwien.detlef.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.domain.EpisodeSortChoice;
import at.ac.tuwien.detlef.settings.GpodderSettings;

public class EpisodeListSortDialogFragment extends DialogFragment {

    private RadioButton rbAscending;
    private RadioButton rbDescending;
    private RadioButton rbReleaseDate;
    private RadioButton rbPodcast;

    private GpodderSettings settings;

    // Use this instance of the interface to deliver action events
    private NoticeDialogListener mListener;

    /*
     * The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks. Each method
     * passes the DialogFragment in case the host needs to query it.
     */
    public interface NoticeDialogListener {
        void onEpisodeSortDialogPositiveClick(DialogFragment dialog, boolean ascending,
                                              EpisodeSortChoice choice);

        void onEpisodeSortDialogNegativeClick(DialogFragment dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        settings = DependencyAssistant.getDependencyAssistant().
                   getGpodderSettings(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.episode_sort_dialog_fragment, null);

        rbAscending = (RadioButton) dialoglayout.findViewById(R.id.rbAscending);
        rbDescending = (RadioButton) dialoglayout.findViewById(R.id.rbDescending);
        rbReleaseDate = (RadioButton) dialoglayout.findViewById(R.id.rbReleaseDate);
        rbPodcast = (RadioButton) dialoglayout.findViewById(R.id.rbPodcast);
        rbAscending.setChecked(settings.isAscending());
        rbDescending.setChecked(!settings.isAscending());
        if (settings.getSortChoice() == EpisodeSortChoice.ReleaseDate) {
            rbReleaseDate.setChecked(true);
            rbPodcast.setChecked(false);
        } else {
            rbReleaseDate.setChecked(false);
            rbPodcast.setChecked(true);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialoglayout);
        builder.setPositiveButton(R.string.button_ok, new
        DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // ok
                EpisodeSortChoice choice = EpisodeSortChoice.ReleaseDate;
                if (rbPodcast.isChecked()) {
                    choice = EpisodeSortChoice.Podcast;
                } else {
                    if (rbReleaseDate.isChecked()) {
                        choice = EpisodeSortChoice.ReleaseDate;
                    }
                }
                // change settings
                settings.setSortChoice(choice).setAscending(rbAscending.isChecked());
                // save settings
                DependencyAssistant.getDependencyAssistant()
                .getGpodderSettingsDAO(getActivity())
                .writeSettings(settings);

                mListener.onEpisodeSortDialogPositiveClick(
                    EpisodeListSortDialogFragment.this,
                    rbAscending.isChecked(), choice);
            }
        })
        .setNegativeButton(R.string.button_cancel, new
        DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // cancel
                mListener
                .onEpisodeSortDialogNegativeClick(EpisodeListSortDialogFragment.this);
            }
        })
        .setTitle("Sort Options");

        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the
    // NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the
            // host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                                         + " must implement NoticeDialogListener");
        }
    }

}
