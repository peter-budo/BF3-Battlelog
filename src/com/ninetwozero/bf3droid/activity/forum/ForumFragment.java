/*
    context file is part of BF3 Battlelog

    BF3 Battlelog is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BF3 Battlelog is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 */

package com.ninetwozero.bf3droid.activity.forum;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.*;

import com.ninetwozero.bf3droid.R;
import com.ninetwozero.bf3droid.adapter.ThreadListAdapter;
import com.ninetwozero.bf3droid.asynctask.AsyncCreateNewThread;
import com.ninetwozero.bf3droid.datatype.DefaultFragment;
import com.ninetwozero.bf3droid.datatype.ForumData;
import com.ninetwozero.bf3droid.datatype.ForumThreadData;
import com.ninetwozero.bf3droid.http.ForumClient;
import com.ninetwozero.bf3droid.misc.BBCodeUtils;
import com.ninetwozero.bf3droid.misc.Constants;

import java.util.List;

public class ForumFragment extends ListFragment implements DefaultFragment {

    private Context context;
    private LayoutInflater inflater;
    private SharedPreferences sharedPreferences;
    private ForumData forumData;
    private ForumClient forumHandler;

    private ListView listView;
    private ThreadListAdapter threadListAdapter;
    private TextView textView;
    private RelativeLayout wrapLoader;
    private Button buttonMore;
    private EditText textareaTitle;
    private EditText textareaContent;

    private long forumId;
    private long lastRefresh;
    private int currentPage;
    private boolean loadFresh;
    private String locale;
    private Intent storedRequest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.inflater = inflater;

        View view = this.inflater.inflate(R.layout.activity_forum, container, false);
        locale = sharedPreferences.getString(Constants.SP_BL_FORUM_LOCALE, "en");
        forumHandler = new ForumClient();

        initFragment(view);
        return view;
    }

    public void initFragment(View v) {
        textView = (TextView) v.findViewById(R.id.text_title);
        listView = (ListView) v.findViewById(android.R.id.list);
        threadListAdapter = new ThreadListAdapter(context, null, inflater);
        listView.setAdapter(threadListAdapter);
        getActivity().registerForContextMenu(listView);
        Button buttonPost = (Button) v.findViewById(R.id.button_new);
        buttonMore = (Button) v.findViewById(R.id.button_more);
        textareaTitle = (EditText) v.findViewById(R.id.textarea_title);
        textareaContent = (EditText) v.findViewById(R.id.textarea_content);

        buttonMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View sv) {
                if ((currentPage - 1) == forumData.getNumPages()) {
                    sv.setVisibility(View.GONE);
                }
                new AsyncLoadMore(context, forumId).execute(++currentPage);
            }
        });

        buttonPost.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = textareaTitle.getText().toString();
                String content = textareaContent.getText().toString();

                if ("".equals(title)) {
                    Toast.makeText(context, "You need to enter a title for your thread.", Toast.LENGTH_SHORT).show();
                } else if ("".equals(content)) {
                    Toast.makeText(context, "You need to enter some content for your thread.", Toast.LENGTH_SHORT).show();
                }

                content = BBCodeUtils.toBBCode(content, null);

                new AsyncCreateNewThread(context, forumId).execute(title,
                        content, sharedPreferences.getString(
                        Constants.SP_BL_PROFILE_CHECKSUM, ""));
            }
        });

        wrapLoader = (RelativeLayout) v.findViewById(R.id.wrap_loader);
        currentPage = 1;
        loadFresh = false;

        if (storedRequest != null) {
            openForum(storedRequest);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    public void reload() {
        if (forumId > 0) {
            long now = System.currentTimeMillis() / 1000;
            if (forumData == null || loadFresh) {
                new AsyncGetThreads(context, listView).execute(forumId);
                loadFresh = false;
            } else {
                if ((lastRefresh + 300) < now) {
                    new AsyncGetThreads(null, listView).execute(forumId);
                }
            }
            lastRefresh = now;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        ForumActivity parent = (ForumActivity) getActivity();
        parent.openThread(
                new Intent().putExtra("threadId", id)
                        .putExtra("threadTitle", ((ForumThreadData) v.getTag()).getTitle()
                        ));
    }

    private class AsyncGetThreads extends AsyncTask<Long, Void, Boolean> {
        private Context context;
        private ListView list;
        private RotateAnimation rotateAnimation;

        public AsyncGetThreads(Context c, ListView l) {
            context = c;
            list = l;
        }

        @Override
        protected void onPreExecute() {
            if (context != null) {
                rotateAnimation = new RotateAnimation(0, 359,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setDuration(1600);
                rotateAnimation.setRepeatCount(RotateAnimation.INFINITE);
                wrapLoader.setVisibility(View.VISIBLE);
                wrapLoader.findViewById(R.id.image_loader).setAnimation(rotateAnimation);
                rotateAnimation.start();
            }
        }

        @Override
        protected Boolean doInBackground(Long... arg0) {
            try {
                forumData = new ForumClient().getThreads(locale, arg0[0]);
                return (forumData != null);
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean results) {
            if (context != null) {
                if (forumData.getNumPages() > 1) {
                    buttonMore.setVisibility(View.VISIBLE);
                    buttonMore.setText(R.string.info_xml_feed_button_pagination);
                } else {
                    buttonMore.setVisibility(View.GONE);
                }

                if (results) {
                    textView.setText(forumData.getTitle());
                    ((ThreadListAdapter) list.getAdapter()).set(forumData.getThreads());
                    listView.post(new Runnable() {
                        @Override
                        public void run() {
                            listView.setSelection(0);
                            wrapLoader.setVisibility(View.GONE);
                            rotateAnimation.reset();
                        }
                    });
                }
            }
        }
    }

    private class AsyncLoadMore extends AsyncTask<Integer, Void, Boolean> {
        private Context context;
        private long forumId;
        private int page;
        private List<ForumThreadData> threads;

        public AsyncLoadMore(Context c, long f) {
            this.context = c;
            this.forumId = f;
        }

        @Override
        protected void onPreExecute() {
            buttonMore.setText(R.string.label_downloading);
            buttonMore.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Integer... arg0) {
            try {
                page = arg0[0];
                forumHandler.setForumId(forumId);
                threads = forumHandler.getThreads(locale, page);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean results) {
            if (context instanceof ForumActivity) {
                if (results) {
                    threadListAdapter.add(threads);
                    buttonMore.setText(R.string.info_xml_feed_button_pagination);
                } else {
                    Toast.makeText(context, R.string.info_xml_threads_more_false, Toast.LENGTH_SHORT).show();
                }
                buttonMore.setEnabled(true);
            }
        }
    }

    public void openForum(Intent data) {
        if (textView == null) {
            storedRequest = data;
        } else {
            forumId = data.getLongExtra("forumId", 0);
            textView.setText(data.getStringExtra("forumTitle"));
            loadFresh = true;
            reload();
        }
    }

    @Override
    public Menu prepareOptionsMenu(Menu menu) {
        return menu;
    }

    @Override
    public boolean handleSelectedOption(MenuItem item) {
        return false;
    }
}
