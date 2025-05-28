package io.github.broskipoker.shared;

public class LoginRequest {
    public String username;
    public String password;

    // Empty constructor required for Kryo serialization
    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
