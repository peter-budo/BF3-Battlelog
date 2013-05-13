/*
    This file is part of BF3 Battlelog

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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.ninetwozero.bf3droid.R;
import com.ninetwozero.bf3droid.adapter.ForumListAdapter;
import com.ninetwozero.bf3droid.datatype.DefaultFragment;
import com.ninetwozero.bf3droid.datatype.ForumData;
import com.ninetwozero.bf3droid.http.ForumClient;
import com.ninetwozero.bf3droid.jsonmodel.forum.ForumCategories;
import com.ninetwozero.bf3droid.misc.Constants;
import com.ninetwozero.bf3droid.misc.DataBank;

import java.util.ArrayList;
import java.util.List;

public class BoardFragment extends ListFragment implements DefaultFragment {

    private Context context;
    private LayoutInflater inflater;
    private ListView listView;
    private TextView textTitle;
    private String locale;
    private List<ForumData> forumsList;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.inflater = inflater;

        View view = this.inflater.inflate(R.layout.activity_board, container, false);

        locale = sharedPreferences.getString(Constants.SP_BL_FORUM_LOCALE, "en");

        forumsList = new ArrayList<ForumData>();
        initFragment(view);
        return view;
    }

    public void initFragment(View v) {
        textTitle = (TextView) v.findViewById(R.id.text_board_title);
        v.findViewById(R.id.wrap_top).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View sv) {
                generateDialogLanguageList(context, DataBank.getLanguages(), DataBank.getLocales()).show();
            }
        });

        listView = (ListView) v.findViewById(android.R.id.list);
        listView.setAdapter(new ForumListAdapter(context, forumsList, inflater));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (forumsList == null || forumsList.isEmpty()) {
            reload();
        }
    }

    public void reload() {
        if (forumsList == null) {
            new AsyncGetForums(context).execute();
        } else {
            new AsyncGetForums(null).execute();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        ForumActivity parent = (ForumActivity) getActivity();

        parent.openForum(
                new Intent().putExtra("forumId", id)
                        .putExtra("forumTitle", ((ForumData) v.getTag()).getTitle())
        );
    }

    private class AsyncGetForums extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private ProgressDialog progressDialog;
        private String title;

        public AsyncGetForums(Context c) {
            context = c;
        }

        @Override
        protected void onPreExecute() {
            if (context != null) {
                progressDialog = new ProgressDialog(this.context);
                progressDialog.setTitle(R.string.general_wait);
                progressDialog.setMessage("Downloading the forums...");
                progressDialog.show();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                ForumCategories forumCategories = new ForumClient().getForums(locale);
                title = "";//(String) result[0];
                forumsList = new ArrayList<ForumData>();//(List<ForumData>) result[1];
                return (forumsList != null);
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean results) {
            if (context != null && this.progressDialog != null) {
                this.progressDialog.dismiss();
            }

            textTitle.setText(title);
            if (listView.getAdapter() != null) {
                ((ForumListAdapter) listView.getAdapter()).setItemArray(forumsList);
            }
        }
    }

    public Dialog generateDialogLanguageList(final Context context, final String[] languages, final String[] locales) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.info_forum_lang);
        builder.setSingleChoiceItems(languages, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                sharedPreferences.edit()
                        .putString(Constants.SP_BL_FORUM_LOCALE, locales[item])
                        .commit();
                locale = locales[item];
                reload();
                dialog.dismiss();
            }
        }
        );
        return builder.create();
    }

    @Override
    public Menu prepareOptionsMenu(Menu menu) {
        return menu;
    }

    @Override
    public boolean handleSelectedOption(MenuItem item) {
        // TODO Auto-generated method stub
        return false;
    }
}
