package com.bog.calendar.app.ui;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates a class is a widget usable by application developers to create UI.
 * <p>
 * This must be used in cases where:
 * <ul>
 * <li>The widget is not in the package <code>android.widget</code></li>
 * <li>The widget extends <code>android.view.ViewGroup</code></li>
 * </ul>
 * @hide
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
public @interface Widget {
}
