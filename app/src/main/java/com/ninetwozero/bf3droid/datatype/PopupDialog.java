package com.ninetwozero.bf3droid.datatype;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import com.ninetwozero.bf3droid.R;

public class PopupDialog extends Dialog {

    public PopupDialog(Context context, int theme) {

        super(context, theme);

    }

    @Override
    public void onCreate(final Bundle icicle) {

        super.onCreate(icicle);
        setContentView(R.layout.popup_dialog_view);

    }

}
