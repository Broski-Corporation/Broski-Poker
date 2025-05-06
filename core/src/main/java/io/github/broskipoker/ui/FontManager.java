package io.github.broskipoker.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;
import java.util.HashMap;

/**
 * Manages the creation, caching, and disposal of FreeType fonts.
 */
public class FontManager implements Disposable {
    private static FontManager instance;
    private final FreeTypeFontGenerator generator;
    private final HashMap<String, BitmapFont> fonts;

    private FontManager() {
        // Load the font file
        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/NotoSans-Bold.ttf"));
        fonts = new HashMap<>();
    }

    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }

    /**
     * Gets or creates a font with the specified size and color
     * @param size the font size
     * @param color the font color
     * @return a BitmapFont created with FreeType
     */
    public BitmapFont getFont(int size, Color color) {
        String key = size + "_" + color.toString();
        if (fonts.containsKey(key)) {
            return fonts.get(key);
        }

        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.borderWidth = 0;
        parameter.minFilter = parameter.magFilter; // Maintain smooth scaling
        parameter.genMipMaps = true;

        BitmapFont font = generator.generateFont(parameter);
        fonts.put(key, font);
        return font;
    }

    @Override
    public void dispose() {
        for (BitmapFont font : fonts.values()) {
            font.dispose();
        }
        generator.dispose();
    }
}
