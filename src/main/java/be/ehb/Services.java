package be.ehb;

import com.goterl.lazycode.lazysodium.LazySodium;
import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.Sodium;
import com.goterl.lazycode.lazysodium.SodiumJava;
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.utils.Key;
import com.goterl.lazycode.lazysodium.utils.KeyPair;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class Services extends LazySodium{

    private LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());
    private KeyPair alice;
    private KeyPair bob;
    private KeyPair alicebox;
    private KeyPair bobbox;
    private KeyPair signing;
    {
        try {
            alice = lazySodium.cryptoBoxKeypair();
            bob = lazySodium.cryptoBoxKeypair();
            signing = lazySodium.cryptoSignKeypair();
        } catch (SodiumException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/publickey", method = GET, produces = "application/json")
    @ResponseBody
    public String publickey(@RequestParam("type") String type) {
        if (type.equals("signing")){
            //return Base64.getUrlEncoder().withoutPadding().encodeToString(signing.getPublicKey().getAsBytes());
            try {
                return Base64.getUrlEncoder().withoutPadding().encodeToString(Hex.decodeHex(signing.getPublicKey().getAsHexString()));
            } catch (DecoderException e) {
                e.printStackTrace();
                return null;
            }
        }else if (type.equals("encryption")){
            try {
                return Base64.getUrlEncoder().withoutPadding().encodeToString(bob.getPublicKey().getAsBytes());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }else{
            return null;
        }
    }

    @RequestMapping(value = "/sign", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String sign(@RequestBody Sign signObject) throws DecoderException {
        try {
            String hexString = lazySodium.cryptoSign(signObject.getMessage(),signing.getSecretKey().getAsHexString());
            byte[] byteString = Hex.decodeHex(hexString.toCharArray());
            String returnSTring = Base64.getUrlEncoder().withoutPadding().encodeToString(byteString);
            return returnSTring;
        } catch (SodiumException e) {
            e.printStackTrace();
            System.out.println(e.getLocalizedMessage());
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/signOpen", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String signOpen(@RequestBody Sign signObject) {
        return lazySodium.cryptoSignOpen(signObject.getMessage(),signing.getPublicKey());
    }

    @RequestMapping(value = "/decrypt", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String decrypt(@RequestBody Message messageObject) {
        try {
            byte[] nonce = Base64.getUrlDecoder().decode(messageObject.getNonce());
            byte[] publicKey = Base64.getUrlDecoder().decode(messageObject.getPublickey());
            Key publicKeyFromPost = Key.fromBytes(publicKey);
            bobbox = new KeyPair(publicKeyFromPost, bob.getSecretKey());
            byte[] decodedMessage = Base64.getUrlDecoder().decode(messageObject.getCiphertext());
            String hexStringMessage = Hex.encodeHexString(decodedMessage);
            String returnText = lazySodium.cryptoBoxOpenEasy(hexStringMessage,nonce,bobbox );
            return returnText;
        } catch (SodiumException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/encrypt", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Message encrypt(@RequestBody Sign textObject) {
        alicebox = new KeyPair(bob.getPublicKey(), alice.getSecretKey());
        Message returnMessage = new Message();
        byte[] nonce = lazySodium.nonce(64);
        String nonceString = Base64.getUrlEncoder().withoutPadding().encodeToString(nonce);
        try {
            try {
                returnMessage.setMessage(Base64.getUrlEncoder().withoutPadding().encodeToString(Hex.decodeHex(lazySodium.cryptoBoxEasy(textObject.getMessage(),nonce,alicebox))));
            } catch (DecoderException e) {
                e.printStackTrace();
            }
        } catch (SodiumException e) {
            e.printStackTrace();
        }
        returnMessage.setNonce(nonceString);
        returnMessage.setPublickey(this.publickey("encryption"));
        return returnMessage;
    }

    @Override
    public Sodium getSodium() {
        return null;
    }
}
