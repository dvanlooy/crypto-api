package be.ehb;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sign {
    @JsonProperty("message")
    public String message;

    public Sign() {
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public String getMessage() {

        return message;
    }
}
