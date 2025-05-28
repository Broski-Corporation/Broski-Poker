package io.github.broskipoker.shared;

public class LoginResponse {
    public boolean success;
    public String message;
    public int playerId;

    // Empty constructor required for Kryo serialization
    public LoginResponse() {}

    public LoginResponse(boolean success, String message, int playerId) {
        this.success = success;
        this.message = message;
        this.playerId = playerId;
    }
}
