package io.github.broskipoker.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import io.github.broskipoker.game.Card;

import java.util.HashSet;
import java.util.Set;

public class SoundManager implements Disposable {
    private static SoundManager instance;

    private Sound cardSound;
    private boolean soundEnabled = true;

    // Track which cards have already played sounds
    private final Set<String> cardsPlayed = new HashSet<>();

    private SoundManager() {
        cardSound = Gdx.audio.newSound(Gdx.files.internal("sounds/card1.ogg"));
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void playCardSound(Card card, int position) {
        if (soundEnabled) {
            // Create a unique identifier for this card
            String cardId = (card != null) ?
                card.getSuit() + "_" + card.getRank() + "_" + position :
                "null_" + position;

            // Only play sound if this card hasn't been played before
            if (!cardsPlayed.contains(cardId)) {
                cardSound.play(0.5f);
                cardsPlayed.add(cardId);
            }
        }
    }

    // Call this method when starting a new hand
    public void resetCardSounds() {
        cardsPlayed.clear();
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    @Override
    public void dispose() {
        cardSound.dispose();
    }
}
