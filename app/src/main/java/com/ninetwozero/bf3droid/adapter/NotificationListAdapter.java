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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.ninetwozero.bf3droid.R;
import com.ninetwozero.bf3droid.datatype.NotificationData;
import com.ninetwozero.bf3droid.misc.PublicUtils;

import java.util.List;

public class NotificationListAdapter extends BaseAdapter {

    // Attributes
    private Context context;
    private List<NotificationData> itemArray;
    private LayoutInflater layoutInflater;
    private long activeUserId;

    // Construct
    public NotificationListAdapter(Context c, List<NotificationData> fi,
                                   LayoutInflater l, long auid) {

        context = c;
        itemArray = fi;
        layoutInflater = l;
        activeUserId = auid;

    }

    @Override
    public int getCount() {

        return (itemArray != null) ? itemArray.size() : 0;

    }

    @Override
    public NotificationData getItem(int position) {

        return itemArray.get(position);

    }

    @Override
    public long getItemId(int position) {

        return itemArray.get(position).getItemId();

    }

    public void setItemArray(List<NotificationData> ia) {
        itemArray = ia;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the current item
        NotificationData currentItem = getItem(position);

        // Recycle
        if (convertView == null) {

            convertView = layoutInflater.inflate(R.layout.list_item_notification, parent, false);

        }

        // Set the views
        ((TextView) convertView.findViewById(R.id.text_message)).setText(Html
                .fromHtml(currentItem.getMessage(context, activeUserId)));
        ((TextView) convertView.findViewById(R.id.text_date))
                .setText(PublicUtils.getRelativeDate(context,
                        currentItem.getDate()));

        // Hook it up on the tag
        convertView.setTag(currentItem);

        // Send it back
        return convertView;
    }

}
