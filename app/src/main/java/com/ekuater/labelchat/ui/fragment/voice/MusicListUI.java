package com.ekuater.labelchat.ui.fragment.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Music;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.widget.CircleImageView;

import java.util.ArrayList;


/**
 * Created by Administrator on 2015/5/12.
 *
 * @author FanChong
 */
public class MusicListUI extends BackIconActivity {
    private static final int MSG_QUERY_MUSIC_RESULT = 101;
    private static final int MSG_QUERY_MORE_MUSIC_RESULT = 102;
    private ListView mListView;
    private EditText mEditText;
    private Button searchBtn;
    private MusicListAdapter adapter;
    private LabelStoryManager mLabelStoryManager;
    private String mSearchName;
    private int mRequestTime;
    private boolean mRemaining;
    private boolean mLoading;

    private static class QueryResult {

        public final ArrayList<Music> musicList;
        public final boolean remaining;

        public QueryResult(ArrayList<Music> musicList, boolean remaining) {
            this.musicList = musicList;
            this.remaining = remaining;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_QUERY_MUSIC_RESULT:
                    handlerMusicQueryResult((QueryResult) msg.obj);
                    break;
                case MSG_QUERY_MORE_MUSIC_RESULT:
                    handlerMusicQueryMoreResult((QueryResult) msg.obj);
                    break;
            }
        }
    };

    private View footView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_music_list);
        findViewById(R.id.search_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        LayoutInflater mInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        footView = mInflater.inflate(R.layout.load_more_music, null);
        footView.setVisibility(View.GONE);
        adapter = new MusicListAdapter(this);
        mLabelStoryManager = LabelStoryManager.getInstance(this);
        mEditText = (EditText) findViewById(R.id.keyword);
        searchBtn = (Button) findViewById(R.id.search);
//        findViewById(R.id.icon).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
        mListView = (ListView) findViewById(R.id.music_list);
        mListView.setAdapter(adapter);
        mListView.addFooterView(footView);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchName = mEditText.getText().toString();
                loadMusic();
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music music = adapter.getItem(position);
                Intent intent = new Intent();
                intent.putExtra(Music.class.getSimpleName(), music);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        footView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreMusic();
            }
        });
    }

    private void loadMusic() {
        showProgressDialog();
        LabelStoryManager.MusicQueryObserver observer = new LabelStoryManager.MusicQueryObserver() {
            @Override
            public void onQueryResult(int result, Music[] music, boolean remaining) {
                ArrayList<Music> musicList = new ArrayList<>();
                if (music != null && music.length > 0) {
                    for (Music m : music) {
                        musicList.add(m);
                    }
                }
                Message message = mHandler.obtainMessage(MSG_QUERY_MUSIC_RESULT, new QueryResult(musicList, remaining));
                mHandler.sendMessage(message);
            }
        };
        mRequestTime = 1;
        mRemaining = false;
        mLoading = true;
        mLabelStoryManager.musicListQuery(mSearchName, mRequestTime, observer);

    }

    private void loadMoreMusic() {
        if (mLoading || !mRemaining) {
            return;
        }
        showProgressDialog();
        LabelStoryManager.MusicQueryObserver observer = new LabelStoryManager.MusicQueryObserver() {
            @Override
            public void onQueryResult(int result, Music[] music, boolean remaining) {
                ArrayList<Music> musicList = new ArrayList<>();
                if (music != null && music.length > 0) {
                    for (Music m : music) {
                        musicList.add(m);
                    }
                }
                Message message = mHandler.obtainMessage(MSG_QUERY_MORE_MUSIC_RESULT, new QueryResult(musicList, remaining));
                mHandler.sendMessage(message);
            }
        };
        mRemaining = false;
        mLabelStoryManager.musicListQuery(mSearchName, mRequestTime, observer);

    }


    private void handlerMusicQueryResult(QueryResult queryResult) {
        dismissProgressDialog();
        QueryResult result = queryResult;
        adapter.updateData(result.musicList);
        mRemaining = result.remaining;
        mLoading = false;
        mRequestTime += mRemaining ? 1 : 0;
        footView.setVisibility(mRemaining ? View.VISIBLE : View.GONE);
    }

    private void handlerMusicQueryMoreResult(QueryResult queryResult) {
        dismissProgressDialog();
        QueryResult result = queryResult;
        adapter.addMoreData(result.musicList);
        mRemaining = result.remaining;
        mLoading = false;
        mRequestTime += mRemaining ? 1 : 0;
        footView.setVisibility(mRemaining ? View.VISIBLE : View.GONE);
    }

    private SimpleProgressDialog mProgressDialog;

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = SimpleProgressDialog.newInstance();
            mProgressDialog.show(getSupportFragmentManager(), "SimpleProgressDialog");
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private class MusicListAdapter extends BaseAdapter {
        private AvatarManager mAvatarManager;
        private LayoutInflater mInflater;
        private ArrayList<Music> mMusicList;


        public MusicListAdapter(Context context) {
            mAvatarManager = AvatarManager.getInstance(context);
            mMusicList = new ArrayList<>();
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public synchronized void updateData(ArrayList<Music> musics) {
            mMusicList = musics;
            notifyDataSetChanged();
        }

        public synchronized void addMoreData(ArrayList<Music> musics) {
            mMusicList.addAll(musics);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mMusicList == null ? 0 : mMusicList.size();
        }

        @Override
        public Music getItem(int position) {
            return mMusicList == null ? null : mMusicList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newView(parent);
            }
            bindView(position, convertView);
            return convertView;
        }

        private View newView(ViewGroup parent) {
            View view = mInflater.inflate(R.layout.fragment_music_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.singerPic = (CircleImageView) view.findViewById(R.id.singer_pic);
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.duration = (TextView) view.findViewById(R.id.duration);
            view.setTag(holder);
            return view;
        }

        private void bindView(int position, View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            Music music = getItem(position);

            if (music != null) {
                mAvatarManager.displaySingerAvatar(music.getSingerPic(), holder.singerPic, R.drawable.ic_sound_pic_normal);
                if (!TextUtils.isEmpty(music.getSingerName()) && !TextUtils.isEmpty(music.getAlbumName())) {
                    holder.title.setText(getString(R.string.singer_song_album_name, music.getSingerName(), music.getSongName(), music.getAlbumName()));
                } else if (!TextUtils.isEmpty(music.getSingerName()) && TextUtils.isEmpty(music.getAlbumName())) {
                    holder.title.setText(getString(R.string.singer_song_name, music.getSingerName(), music.getSongName()));
                } else if (TextUtils.isEmpty(music.getSingerName()) && !TextUtils.isEmpty(music.getAlbumName())) {
                    holder.title.setText(getString(R.string.song_album_name, music.getSongName(), music.getAlbumName()));
                } else {
                    holder.title.setText(music.getSongName());
                }
                holder.duration.setText(getDuration(music.getDuration()));
            }
        }

        private String getDuration(long duration) {
            if (duration != 0) {
                return VoiceTimeUtils.convertMilliSecondToMinute2((int) duration);
            } else {
                return getString(R.string.unknown);
            }
        }

        class ViewHolder {
            CircleImageView singerPic;
            TextView title, duration;
        }
    }
}
