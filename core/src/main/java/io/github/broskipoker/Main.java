package io.github.broskipoker;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Stage stage;
    private Menu menu;
    private Texture backgroundTexture;

    @Override
    public void create() {
        backgroundTexture = new Texture("pokerTable.png");
        batch = new SpriteBatch();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        menu = new Menu(stage);
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (menu.isGameStarted()) {
            batch.begin();
            batch.draw(
                backgroundTexture,
                0,
                0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
            );
            batch.end();

            for (TextButton button : menu.getButtons()) {
                button.setVisible(false);
            }
        } else {
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        backgroundTexture.dispose();
        menu.dispose();
    }
}
