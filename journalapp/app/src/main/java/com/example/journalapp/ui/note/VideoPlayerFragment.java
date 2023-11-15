package com.example.journalapp.ui.note;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.example.journalapp.R;

public class VideoPlayerFragment extends DialogFragment {
    private ExoPlayer player;
    private PlayerView playerview;
    private Uri videoUri;

    /**
     * Creates a new instance of the fragment using VideoURI
     * @param videoUri
     * @return
     */
    public static VideoPlayerFragment newInstance(String videoUri) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putString("videoUri", videoUri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            videoUri = Uri.parse(getArguments().getString("videoUri"));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // inflate layout
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);
        playerview = view.findViewById(R.id.player_view);

        // initialize exoplayer
        initializePlayer();
        return view;
    }

    private void initializePlayer(){
        if (player == null){
            player = new ExoPlayer.Builder(requireContext()).build();
            playerview.setPlayer(player);
            MediaItem mediaItem = MediaItem.fromUri(videoUri);
            player.setMediaItem(mediaItem);
        }
        player.prepare();
        player.play();
        playerview.setUseController(true);
    }

    public void onDestroyView(){
        super.onDestroyView();
        if (player != null){
            player.release();
            player = null;
        }
    }
}
