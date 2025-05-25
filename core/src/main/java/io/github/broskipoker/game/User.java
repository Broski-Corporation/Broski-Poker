package io.github.broskipoker.game;

public class User {
    private int id;
    private String username;
    private String email;
    private long chips;
    private int gamesPlayed;
    private int wins;
    private int losses;

    public User(int id, String username, String email, long chips, int gamesPlayed, int wins, int losses) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.chips = chips;
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
        this.losses = losses;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public long getChips() {
        return chips;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setChips(long chips) {
        this.chips = chips;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    // Utility methods
    public double getWinRate() {
        if (gamesPlayed == 0) {
            return 0.0;
        }
        return (double) wins / gamesPlayed * 100;
    }

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", username='" + username + '\'' +
            ", email='" + email + '\'' +
            ", chips=" + chips +
            ", gamesPlayed=" + gamesPlayed +
            ", wins=" + wins +
            ", losses=" + losses +
            '}';
    }
}
