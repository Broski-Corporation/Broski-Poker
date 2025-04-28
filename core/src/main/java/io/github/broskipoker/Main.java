package io.github.broskipoker;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.Stage;

import javax.swing.*;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;    // The SpriteBatch to draw the background
    private Stage stage;          // The Stage to handle UI elements
    private Menu menu;            // The Menu class instance to manage the menu UI
    private Texture backgroundTexture;  // The background texture for the game
    private boolean gameStarted = false;  // Flag to check if the game has started

    @Override
    public void create() {
        // Create background texture
        backgroundTexture = new Texture("pokerTable.png");

        // Create a new SpriteBatch
        batch = new SpriteBatch();

        // Create a new Stage for managing UI components
        stage = new Stage(new ScreenViewport());

        // Set the Stage as the input processor
        Gdx.input.setInputProcessor(stage);

        // Create the menu instance
        menu = new Menu(stage);

    }

    @Override
    public void render() {
        // Clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Check if the game has started
        if (menu.isGameStarted()) {
            // If the game has started, draw the background
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.begin();
            batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.end();

            // Hide the menu buttons
            for(TextButton button: menu.getButtons()) {
                button.setVisible(false);
            }
        } else {
            // If the game has not started, draw the menu
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
        }

        batch.begin();
        batch.end();
    }

    @Override
    public void dispose() {
        // Dispose of resources when done
        batch.dispose();
        stage.dispose();

    }
}
