package com.groupagendas.groupagenda.calendar;

import android.view.View;

/**
 * Allows to easily use default LTR and RTL swipe animations.<BR>
 * <BR>
 * Currently only animations over the same view are available.
 * 
 * @author Tadas
 */
public class GenericSwipeAnimator {
	public static void startAnimation(final View view, final boolean rtl, final Runnable viewUpdater) {
		// anims temporarily removed, so
		viewUpdater.run();
		
		/*
		final Context ctx = view.getContext();
		Animation anim = AnimationUtils.loadAnimation(ctx,
				rtl ? R.anim.swipe_rtl_leave : R.anim.swipe_ltr_leave);
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				Animation anim = AnimationUtils.loadAnimation(ctx,
						rtl ? R.anim.swipe_rtl_enter : R.anim.swipe_ltr_enter);
				anim.setFillAfter(true);
				view.startAnimation(anim);
				view.post(viewUpdater);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// do nothing
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// do nothing
			}
		});
		view.startAnimation(anim);
		*/
	}
}
