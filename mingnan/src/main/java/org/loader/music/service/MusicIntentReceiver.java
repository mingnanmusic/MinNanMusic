package org.loader.music.service;

/**
 * Created by Administrator on 2017/8/5 0005.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * Receives broadcasted intents. In particular, we are interested in the
 * android.media.AUDIO_BECOMING_NOISY and android.intent.action.MEDIA_BUTTON intents, which is
 * broadcast, for example, when the user disconnects the headphones. This class works because we are
 * declaring it in a &lt;receiver&gt; tag in AndroidManifest.xml.
 */
public class MusicIntentReceiver extends BroadcastReceiver {
    public static final String ACTION_TOGGLE_PLAYBACK =
            "com.example.android.musicplayer.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "com.example.android.musicplayer.action.PLAY";
    public static final String ACTION_PAUSE = "com.example.android.musicplayer.action.PAUSE";
    public static final String ACTION_STOP = "com.example.android.musicplayer.action.STOP";
    public static final String ACTION_SKIP = "com.example.android.musicplayer.action.SKIP";
    public static final String ACTION_REWIND = "com.example.android.musicplayer.action.REWIND";
    public static final String ACTION_URL = "com.example.android.musicplayer.action.URL";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent sendIntent = new Intent(PlayService.class.getSimpleName());
        if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            Toast.makeText(context, "Headphones disconnected.", Toast.LENGTH_SHORT).show();

            // send an intent to our MusicService to telling it to pause the audio
            context.startService(new Intent(ACTION_PAUSE));

        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                  //  context.startService(new Intent(ACTION_TOGGLE_PLAYBACK));
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    sendIntent.putExtra("BUTTON_NOTI", 2);
                 //   context.startService(new Intent(ACTION_PLAY));
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    sendIntent.putExtra("BUTTON_NOTI", 2);
                  //  context.startService(new Intent(ACTION_PAUSE));
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:  //退出
                 //   context.startService(new Intent(ACTION_STOP));
                    sendIntent.putExtra("BUTTON_NOTI", 4);
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT://下一个
                    sendIntent.putExtra("BUTTON_NOTI", 3);
                   // context.startService(new Intent(ACTION_SKIP));
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:  // 上一个
                    // TODO: ensure that doing this in rapid succession actually plays the
                    // previous song
                 //   context.startService(new Intent(ACTION_REWIND));
                    sendIntent.putExtra("BUTTON_NOTI", 1);
                    break;
            }

        }
        context.sendBroadcast(sendIntent);
    }
}