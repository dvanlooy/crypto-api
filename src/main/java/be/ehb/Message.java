package be.ehb;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    @JsonProperty("ciphertext")
    public String ciphertext;
    @JsonProperty("nonce")
    public String nonce;
    @JsonProperty("publickey")
    public String publickey;

    public Message() {
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public void setMessage(String ciphertext) {
        this.ciphertext = ciphertext;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getPublickey() {
        return publickey;
    }

    public void setPublickey(String publickey) {
        this.publickey = publickey;
    }
}