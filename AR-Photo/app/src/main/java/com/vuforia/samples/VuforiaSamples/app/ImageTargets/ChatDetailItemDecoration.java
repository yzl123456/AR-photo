package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by 泽林 on 2018/6/15.
 */

public class ChatDetailItemDecoration extends RecyclerView.ItemDecoration  {
    private int space;

    public ChatDetailItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;

//        if (parent.getChildPosition(view) == 0)
//            outRect.top = space;
    }
}
