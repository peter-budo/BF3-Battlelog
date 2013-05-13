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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ninetwozero.bf3droid.R;
import com.ninetwozero.bf3droid.datatype.DefaultFragment;
import com.ninetwozero.bf3droid.misc.Constants;
import com.ninetwozero.bf3droid.misc.DataBank;

import java.util.HashMap;
import java.util.Map;

public class MenuForumFragment extends Fragment implements DefaultFragment {

    private Context context;
    private Map<Integer, Intent> MENU_INTENTS;
    private SharedPreferences sharedPreferences;

    private RelativeLayout wrapLanguage;
    private TextView textLanguage;
    private ImageView imageLanguage;

    private String selectedLocale;
    private String selectedLanguage;
    private int selectedPosition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        selectedLocale = sharedPreferences.getString(Constants.SP_BL_FORUM_LOCALE, "en");
        selectedPosition = sharedPreferences.getInt(Constants.SP_BL_FORUM_LOCALE_POSITION, 0);
        selectedLanguage = DataBank.getLanguage(selectedPosition);

        View view = inflater.inflate(R.layout.tab_content_dashboard_forum, container, false);
        initFragment(view);
        return view;
    }

    public void initFragment(View view) {
        wrapLanguage = (RelativeLayout) view.findViewById(R.id.wrap_language);
        wrapLanguage.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        generateDialogLanguageList(context, DataBank.getLanguages(), DataBank.getLocales()).show();
                    }
                });
        imageLanguage = (ImageView) wrapLanguage.findViewById(R.id.image_language);
        textLanguage = (TextView) wrapLanguage.findViewById(R.id.text_language);
        textLanguage.setSelected(true);

        setupLanguageBox();

        MENU_INTENTS = new HashMap<Integer, Intent>();
        MENU_INTENTS.put(R.id.button_view, new Intent(context, ForumActivity.class));
        MENU_INTENTS.put(R.id.button_search, new Intent(context, ForumSearchActivity.class));
        MENU_INTENTS.put(R.id.button_saved, new Intent(context, ForumSavedActivity.class));

        final OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MENU_INTENTS.get(v.getId()));
            }
        };

        for (int key : MENU_INTENTS.keySet()) {
            view.findViewById(key).setOnClickListener(onClickListener);
        }
    }

    @Override
    public void reload() {
    }

    @Override
    public Menu prepareOptionsMenu(Menu menu) {
        return menu;
    }

    @Override
    public boolean handleSelectedOption(MenuItem item) {
        return false;
    }

    public Dialog generateDialogLanguageList(final Context context, final String[] languages, final String[] locales) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.info_forum_lang);
        builder.setSingleChoiceItems(languages, -1, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                selectedPosition = item;
                selectedLocale = locales[selectedPosition];
                selectedLanguage = languages[selectedPosition];

                SharedPreferences.Editor spEdit = sharedPreferences.edit();
                spEdit.putString(Constants.SP_BL_FORUM_LOCALE, selectedLocale);
                spEdit.putInt(Constants.SP_BL_FORUM_LOCALE_POSITION, selectedPosition);
                spEdit.commit();

                setupLanguageBox();
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    public void setupLanguageBox() {
        textLanguage.setText(selectedLanguage);
        imageLanguage.setImageResource(R.drawable.locale_us);
    }
}
