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

package com.ninetwozero.bf3droid.asynctask;

import android.os.AsyncTask;
import com.ninetwozero.bf3droid.datatype.PostData;
import com.ninetwozero.bf3droid.datatype.WebsiteHandlerException;
import com.ninetwozero.bf3droid.http.COMClient;

public class AsyncSessionSetActive extends
        AsyncTask<PostData, Integer, Boolean> {

    @Override
    protected Boolean doInBackground(PostData... arg0) {

        try {

            return COMClient.setActive();

        } catch (WebsiteHandlerException e) {

            return false;

        }

    }

}
