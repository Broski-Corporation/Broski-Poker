package io.github.broskipoker.ui;

public class EmojiDisplayData {
    public int emojiIndex;
    public long displayStartTime;

    public EmojiDisplayData(int emojiIndex, long displayStartTime) {
        this.emojiIndex = emojiIndex;
        this.displayStartTime = displayStartTime;
    }

    public float getAlpha() {
        long elapsed = System.currentTimeMillis() - displayStartTime;
        if (elapsed >= GameRenderer.EMOJI_DISPLAY_DURATION) return 0f;
        return 1f - (float) elapsed / GameRenderer.EMOJI_DISPLAY_DURATION;
    }
}

