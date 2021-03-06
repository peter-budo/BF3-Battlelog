/*
    This file is part of BF3 Droid

    BF3 Droid is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BF3 Droid is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 */

package com.ninetwozero.bf3droid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ninetwozero.bf3droid.R;
import com.ninetwozero.bf3droid.datatype.ProfileData;

import java.util.List;

public class FriendListAdapter extends BaseAdapter {

    private Context context;
    private List<ProfileData> profileArray;
    private LayoutInflater layoutInflater;
    private TextView textUser, textStatus;

    public FriendListAdapter(Context c, List<ProfileData> p, LayoutInflater l) {
        context = c;
        profileArray = p;
        layoutInflater = l;
    }

    @Override
    public int getCount() {
        return (profileArray != null) ? profileArray.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getId() == 0) {
            return 2;
        } else if (!getItem(position).isFriend()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public ProfileData getItem(int position) {
        return profileArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return profileArray.get(position).getId();
    }

    public long getPersonaId(int position) {
        return profileArray.get(position).getPersona(0).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ProfileData currentProfile = getItem(position);
        if (getItemViewType(position) == 2) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_friends_divider, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.text_title)).setText(currentProfile.getUsername());
            convertView.setOnClickListener(null);
            convertView.setOnLongClickListener(null);

        } else if (getItemViewType(position) == 1) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_item_request, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.text_user)).setText(currentProfile.getUsername());

        } else {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_item_friend, parent, false);
            }

            textUser = (TextView) convertView.findViewById(R.id.text_user);
            textUser.setText(currentProfile.getUsername());
            textStatus = (TextView) convertView.findViewById(R.id.text_status);

            if (currentProfile.isPlaying() && currentProfile.isOnline()) {
                textUser.setTextColor(context.getResources().getColor( R.color.blue));
                textStatus.setText(R.string.label_playing);
                textStatus.setTextColor(context.getResources().getColor(R.color.blue));
            } else if (currentProfile.isOnline()) {
                textUser.setTextColor(context.getResources().getColor(R.color.green));
                textStatus.setText(R.string.label_online);
                textStatus.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                textUser.setTextColor(context.getResources().getColor(R.color.grey));
                textStatus.setText(R.string.label_offline);
                textStatus.setTextColor(context.getResources().getColor(R.color.grey));
            }
        }

        convertView.setTag(currentProfile);
        return convertView;
    }

    public void setItemArray(List<ProfileData> data) {
        profileArray = data;
        notifyDataSetChanged();
    }
}
