package at.ac.tuwien.detlef.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

public class PlayerFragment extends Fragment {

    private Episode activeEpisode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Podcast p1 = new Podcast();
        p1.setTitle("My Podcast 1");
        Episode e1 = new Episode();
        e1.setPodcast(p1);
        e1.setTitle("Episode 1");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.player_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textView =
                (TextView) getActivity().findViewById(
                        R.id.playerEpisodeDescription);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    public void setEpisode(Episode episode) {
        activeEpisode = episode;
    }

    public void startPlaying() {
        // TODO
    }

    public void pausePlaying() {
        // TODO
    }

    public void nextEpisode() {
        // TODO
    }

    public void rewindEpisode() {
        // TODO
    }

    public void stopEpisode() {
        // TODO
    }

    public void setPlayingTime(long playingTime) {
        // TODO
    }
}
