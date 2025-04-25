package io.github.broskipoker.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.broskipoker.Main;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        // This handles macOS support and helps on Windows.
        if (StartupHelper.startNewJvmIfRequired()) return;
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        // Create the application with the default configuration
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        // Create default configuration
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();

        // App name
        configuration.setTitle("Broski Poker");

        // Enable vsync
        configuration.useVsync(true);

        // Set foreground FPS to the refresh rate of the display
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);

        // Set window size
        configuration.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());

        // Set window icon
        configuration.setWindowIcon("gameIcon.png");


        return configuration;
    }
}
