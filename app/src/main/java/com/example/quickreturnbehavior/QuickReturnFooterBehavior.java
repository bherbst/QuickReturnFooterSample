package com.example.quickreturnbehavior;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * CoordinatorLayout Behavior for a quick return footer
 * <p>
 * When a nested ScrollView is scrolled down, the quick return view will disappear.
 * When the ScrollView is scrolled back up, the quick return view will reappear.
 *
 * @author bherbst
 */
@SuppressWarnings("unused")
public class QuickReturnFooterBehavior extends CoordinatorLayout.Behavior<View> {
    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();

    private static final int ANIM_STATE_NONE = 0;
    private static final int ANIM_STATE_HIDING = 1;
    private static final int ANIM_STATE_SHOWING = 2;

    private int animState = ANIM_STATE_NONE;

    private int mDySinceDirectionChange;

    public QuickReturnFooterBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
        if (dy > 0 && mDySinceDirectionChange < 0
                || dy < 0 && mDySinceDirectionChange > 0) {
            // We detected a direction change -- reset our cumulative delta Y
            mDySinceDirectionChange = 0;
        }

        mDySinceDirectionChange += dy;

        if (mDySinceDirectionChange > child.getHeight() && !isOrWillBeHidden(child)) {
            hide(child);
        } else if (mDySinceDirectionChange < 0 && !isOrWillBeShown(child)) {
            show(child);
        }
    }

    private boolean isOrWillBeHidden(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            return animState == ANIM_STATE_HIDING;
        } else {
            return animState != ANIM_STATE_SHOWING;
        }
    }

    private boolean isOrWillBeShown(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            return animState == ANIM_STATE_SHOWING;
        } else {
            return animState != ANIM_STATE_HIDING;
        }
    }

    /**
     * Hide the quick return view.
     * <p>
     * Animates hiding the view, with the view sliding down and out of the screen.
     * After the view has disappeared, its visibility will change to GONE.
     *
     * @param view The quick return view
     */
    private void hide(final View view) {
        view.animate().cancel();

        view.animate()
                .translationY(view.getHeight())
                .setInterpolator(INTERPOLATOR)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    private boolean isCanceled = false;

                    @Override public void onAnimationStart(Animator animation) {
                        animState = ANIM_STATE_HIDING;
                        isCanceled = false;
                        view.setVisibility(View.VISIBLE);
                    }

                    @Override public void onAnimationCancel(Animator animation) {
                        isCanceled = true;
                    }

                    @Override public void onAnimationEnd(Animator animation) {
                        animState = ANIM_STATE_NONE;
                        if (!isCanceled) {
                            view.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    /**
     * Show the quick return view.
     * <p>
     * Animates showing the view, with the view sliding up from the bottom of the screen.
     * After the view has reappeared, its visibility will change to VISIBLE.
     *
     * @param view The quick return view
     */
    private void show(final View view) {
        view.animate().cancel();

        view.animate()
                .translationY(0f)
                .setInterpolator(INTERPOLATOR)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationStart(Animator animator) {
                        animState = ANIM_STATE_SHOWING;
                        view.setVisibility(View.VISIBLE);
                    }

                    @Override public void onAnimationEnd(Animator animator) {
                        animState = ANIM_STATE_NONE;
                    }
                });
    }
}