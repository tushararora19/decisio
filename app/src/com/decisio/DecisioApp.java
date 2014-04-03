package com.decisio;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.Parse;
import com.parse.ParseObject;

public class DecisioApp extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        DecisioApp.context = this;

        ParseObject.registerSubclass(com.decisio.models.LocationPoint.class);
        ParseObject.registerSubclass(com.decisio.models.ManagerQuestions.class);
        ParseObject.registerSubclass(com.decisio.models.Passcode.class);
        ParseObject.registerSubclass(com.decisio.models.CafeMood.class);
        Parse.initialize(context, "ZxwAAbMRQViR3ZYOTdahS1C7ebKCtEgwb3LSt22t", "ck7xByadnQv3bJcfVW4WX0VuescG4qh4qjTT3PSy");

        // Create global configuration and initialize ImageLoader with this configuration
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().
                cacheInMemory().cacheOnDisc().build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .defaultDisplayImageOptions(defaultOptions)
        .build();
        ImageLoader.getInstance().init(config);
    }

    public static Context getContext() {
        return context;
    }
}
