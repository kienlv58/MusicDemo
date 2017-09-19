package com.invisionapp.projects.musicdemo;

import android.content.Context;
import android.content.Intent;

/**
 * Created by KienPC on 09/19/17.
 */

public class NavigationUtils {



    public static Intent getNowPlayingIntent(Context context) {

        final Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_NOWPLAYING);
        return intent;
    }
}
