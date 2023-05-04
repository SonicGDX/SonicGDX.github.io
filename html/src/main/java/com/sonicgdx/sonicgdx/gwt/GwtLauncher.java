package com.sonicgdx.sonicgdx.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.sonicgdx.sonicgdx.Init;

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {
        @Override
        public GwtApplicationConfiguration getConfig () {
            /* Resizable application, uses available space in browser with no padding:
            GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(true);
            cfg.padVertical = 0;
            cfg.padHorizontal = 0;
            return cfg;*/

            // Fixed resolution
            return new GwtApplicationConfiguration(1280, 720);
        }

        @Override
        public ApplicationListener createApplicationListener () {
            return new Init();
        }
}