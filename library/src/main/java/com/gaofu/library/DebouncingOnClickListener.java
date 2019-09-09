package com.gaofu.library;

import android.view.View;

/**
 * @author Gaofu
 * Time 2019-09-06 14:40
 */
public abstract class DebouncingOnClickListener implements View.OnClickListener {

    @Override
    public void onClick(View view) {
        doClick(view);
    }

    public abstract void doClick(View v);

}
