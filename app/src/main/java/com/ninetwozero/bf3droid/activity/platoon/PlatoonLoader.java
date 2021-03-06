package com.ninetwozero.bf3droid.activity.platoon;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.ninetwozero.bf3droid.loader.Bf3Loader;
import com.ninetwozero.bf3droid.loader.CompletedTask;
import com.ninetwozero.bf3droid.misc.HttpHeaders;
import com.ninetwozero.bf3droid.server.Bf3ServerCall;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;

import java.net.URI;

public class PlatoonLoader implements LoaderManager.LoaderCallbacks<CompletedTask> {
    private final Callback callback;
    private final Context context;
    private final URI uri;
    private final LoaderManager loaderManager;
    private final int loaderId;

    public PlatoonLoader(Callback callback, Context context, URI uri, LoaderManager loaderManager, int loaderId) {
        this.callback = callback;
        this.context = context;
        this.uri = uri;
        this.loaderManager = loaderManager;
        this.loaderId = loaderId;
    }

    public interface Callback {
        void onLoadFinished(CompletedTask completedTask);
    }

    public void restart() {
        loaderManager.restartLoader(loaderId, new Bundle(), this);
    }

    @Override
    public Loader<CompletedTask> onCreateLoader(int i, Bundle bundle) {
        return new Bf3Loader(context, httpDataStats());
    }

    private Bf3ServerCall.HttpData httpDataStats() {
        Header[] requestHeaders = HttpHeaders.GET_HEADERS.get(HttpHeaders.AJAX_GET_HEADER);
        return new Bf3ServerCall.HttpData(uri, HttpGet.METHOD_NAME, true, requestHeaders );
    }

    @Override
    public void onLoadFinished(Loader<CompletedTask> completedTaskLoader, CompletedTask completedTask) {
        if(isTaskSuccess(completedTask.result)){
            completedTask.setLoaderID(loaderId);
            callback.onLoadFinished(completedTask);
        }else{
            Log.e(PlatoonLoader.class.getSimpleName(), "Platoon data extraction failed for " + uri);
        }
    }

    @Override
    public void onLoaderReset(Loader<CompletedTask> completedTaskLoader) {

    }

    private boolean isTaskSuccess(CompletedTask.Result result) {
        return result == CompletedTask.Result.SUCCESS;
    }
}
