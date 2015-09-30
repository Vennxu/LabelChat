package com.ekuater.labelchat.ui.fragment.mixdynamic.audioplay;

import android.content.Context;

import com.ekuater.httpfileloader.assist.FailReason;
import com.ekuater.httpfileloader.listener.FileLoadingListener;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.ui.util.ShowToast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by Leo on 2015/4/25.
 *
 * @author LinYong
 */
public class AudioPlayMediator {

    private final Context context;
    private final Map<String, Entity> entityMap;
    private final LabelStoryManager storyManager;
    private final EventBus eventBus;

    public AudioPlayMediator(Context context, EventBus eventBus) {
        this.context = context;
        this.entityMap = new HashMap<>();
        this.storyManager = LabelStoryManager.getInstance(context);
        this.eventBus = eventBus;
    }

    public void init() {
        eventBus.register(this);
    }

    public void deInit() {
        eventBus.unregister(this);
        for (Entity entity : entityMap.values()) {
            entity.stop();
        }
        entityMap.clear();
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(AudioBindEvent event) {
        Entity entity = entityMap.get(event.getId());
        event.setEntity(entity != null ? entity.entity : null);
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(AudioPlayEvent event) {
        AudioEntity audioEntity = event.getEntity();
        Entity entity = entityMap.get(audioEntity.getId());

        if (entity != null) {
            if (entity.entity.getState() == AudioState.PLAYING) {
                entity.stop();
            }
        } else {
            entity = new Entity(event.getEntity());
            entity.start();
        }
    }

    private void postNotifyEvent(AudioNotifyEvent.NotifyType type, AudioEntity entity) {
        eventBus.post(new AudioNotifyEvent(type, entity));
    }

    private class Entity implements PlayListener, FileLoadingListener {

        public final AudioEntity entity;
        public final Player player;

        private boolean removed;

        public Entity(AudioEntity entity) {
            this.entity = entity;
            this.player = new Player();
            this.player.setPlayListener(this);
            this.removed = true;
        }

        public void start() {
            entityMap.put(entity.getId(), this);
            removed = false;
            if (LabelStory.TYPE_ONLINEAUDIO.equals(entity.getType())) {
                storyManager.loadOnlineAudioFile(entity.getMedia(), this);
            }else{
                storyManager.loadAudioFile(entity.getMedia(), this);
            }
            updateState(AudioState.LOADING);
        }

        public void stop() {
            player.release();
            updateState(AudioState.STOPPED);
        }

        @Override
        public void onPrepared() {
            updateState(AudioState.PLAYING);
        }

        @Override
        public void onCompletion() {
            player.release();
            updateState(AudioState.STOPPED);
        }

        @Override
        public void onTimeChanged(String time) {
            entity.setTime(time);
            postNotifyEvent(AudioNotifyEvent.NotifyType.TIME_NOTIFY, entity);
        }

        @Override
        public void onLoadingStarted(String fileUri) {
        }

        @Override
        public void onLoadingFailed(String fileUri, FailReason failReason) {
            updateState(AudioState.STOPPED);
            ShowToast.makeText(context, R.drawable.emoji_sad,
                    context.getString(R.string.voice_load_failed)).show();
        }

        @Override
        public void onLoadingComplete(String fileUri, File file) {
            if (file != null && file.exists()) {
                if (entity.getState() == AudioState.LOADING) {
                    player.playUrl(file.getAbsolutePath());
                }
            } else {
                updateState(AudioState.STOPPED);
                ShowToast.makeText(context, R.drawable.emoji_sad,
                        context.getString(R.string.voice_load_failed)).show();
            }
        }

        @Override
        public void onLoadingCancelled(String fileUri) {
            updateState(AudioState.STOPPED);
        }

        private void updateState(AudioState state) {
            boolean removed = this.removed;

            if (state == AudioState.STOPPED) {
                if (!removed) {
                    entityMap.remove(entity.getId());
                    removed = true;
                }
                entity.setTime(null);
            }

            entity.setState(state);
            if (!this.removed) {
                postNotifyEvent(AudioNotifyEvent.NotifyType.STATE_NOTIFY, entity);
            }
            this.removed = removed;
        }
    }
}
