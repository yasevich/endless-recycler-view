package com.github.yasevich.endlessrecyclerview.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Asha on 15-8-26.
 * Asha ashqalcn@gmail.com
 */
public class DemoActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
    }

    public void onStaggeredClicked(View view)
    {
        MainActivity.lanuchActivity(this,MainActivity.LAYOUT_STAGGERED);
    }

    public void onLinearClicked(View view)
    {
        MainActivity.lanuchActivity(this,MainActivity.LAYOUT_LINEAR);
    }
}
