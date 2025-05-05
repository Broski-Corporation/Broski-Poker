package io.github.broskipoker.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import io.github.broskipoker.game.Card;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SoundManager implements Disposable {
    private static SoundManager instance;

    private Sound cardSound;
    private Sound chipSound;
    private Sound buttonSound;
    private Sound winSound;
    private Sound loseSound;
    private boolean soundEnabled = true;

    // Track which cards have already played sounds
    private final Set<String> cardsPlayed = new HashSet<>();

    // Track player bet amounts to detect changes
    private final Map<Integer, Integer> playerBets = new HashMap<>();

    private boolean showdownSoundPlayed = false;

    private SoundManager() {
        cardSound = Gdx.audio.newSound(Gdx.files.internal("sounds/card1.ogg"));
        chipSound = Gdx.audio.newSound(Gdx.files.internal("sounds/chips1.ogg"));
        buttonSound = Gdx.audio.newSound(Gdx.files.internal("sounds/button.ogg"));
        winSound = Gdx.audio.newSound(Gdx.files.internal("sounds/win.ogg"));
        loseSound = Gdx.audio.newSound(Gdx.files.internal("sounds/negative.ogg"));
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

    public void playChipSound(int betAmount, int playerIndex) {
        if (soundEnabled) {
            // Only play sound when bet amount changes
            Integer previousBet = playerBets.get(playerIndex);
            if (previousBet == null || previousBet != betAmount) {
                // Calculate how many chip sounds to play based on bet size
                int soundCount = 1;  // Default minimum

                // Scale sound count based on bet amount
                if (betAmount > 500) {
                    soundCount = 4;  // Lots of chips
                } else if (betAmount > 200) {
                    soundCount = 3;  // Medium-large stack
                } else if (betAmount > 50) {
                    soundCount = 2;  // Small-medium stack
                }

                // Play the chip sound multiple times with slight volume variation
                for (int i = 0; i < soundCount; i++) {
                    // Slightly randomize volume for more natural effect
                    float volume = 0.3f + (float)(Math.random() * 0.2f);

                    // Add small delay between sounds
                    final int soundIndex = i;
                    final float soundVolume = volume;

                    Gdx.app.postRunnable(() -> {
                        // Small delay between chip sounds (20ms per chip)
                        try {
                            Thread.sleep(soundIndex * 25);
                        } catch (InterruptedException e) {
                            // Ignore interruption
                        }
                        chipSound.play(soundVolume);
                    });
                }

                // Update the stored bet amount
                playerBets.put(playerIndex, betAmount);
            }
        }
    }

    public void playButtonSound() {
        if (soundEnabled) {
            buttonSound.play(0.4f);
        }
    }

    public void playWinSound() {
        if (soundEnabled && winSound != null && !showdownSoundPlayed) {
            winSound.play(0.6f);
            showdownSoundPlayed = true;
        }
    }

    public void playLoseSound() {
        if (soundEnabled && loseSound != null && !showdownSoundPlayed) {
            loseSound.play(0.6f);
            showdownSoundPlayed = true;
        }
    }

    // Call this method when starting a new hand
    public void resetCardSounds() {
        cardsPlayed.clear();
        playerBets.clear();
        showdownSoundPlayed = false;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    @Override
    public void dispose() {
        cardSound.dispose();
        chipSound.dispose();
        buttonSound.dispose();
        if (winSound != null) winSound.dispose();
        if (loseSound != null) loseSound.dispose();
    }

    public boolean isShowdownSoundPlayed() {
        return showdownSoundPlayed;
    }
}
