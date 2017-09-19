package com.invisionapp.projects.musicdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by KienPC on 09/18/17.
 */

public class ListSongAdapter extends BaseAdapter {

    Context context;
    ArrayList<Song> listSongs;
    LayoutInflater inflater;

    public ListSongAdapter(Context context, ArrayList<Song> listSongs) {
        this.context = context;
        this.listSongs = listSongs;
        inflater = (LayoutInflater) LayoutInflater.from(context).getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return listSongs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = inflater.inflate(R.layout.song_item,null);
        TextView tv_title = (TextView)view.findViewById(R.id.tv_title);
        TextView tv_artist = (TextView)view.findViewById(R.id.tv_artist);
        Song currentSong = listSongs.get(position);
        tv_title.setText(currentSong.getTitle());
        tv_artist.setText(currentSong.getArtist());
        view.setTag(position);
        return view;
    }
}
