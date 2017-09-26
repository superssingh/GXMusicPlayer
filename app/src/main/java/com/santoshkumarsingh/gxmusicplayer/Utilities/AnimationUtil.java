package com.santoshkumarsingh.gxmusicplayer.Utilities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;

public class AnimationUtil {
    public static void animate(RecyclerView.ViewHolder holder, boolean down) {
        AnimatorSet animationSet = new AnimatorSet();
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(holder.itemView,
                "translationY",
                down ? 200 : -200, 0);
        animatorY.setDuration(1000);
        animationSet.playTogether(animatorY);
        animationSet.start();
    }

}
