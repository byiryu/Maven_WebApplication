package kr.byiryu.db.password;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordMatcher {

    PasswordEncoding pwEncoder;
    PasswordEncoding bCryptEncoder;

    public PasswordMatcher(){
        SHAPasswordEncoder shaPasswordEncoder = new SHAPasswordEncoder(512);
        shaPasswordEncoder.setEncodeHashAsBase64(true);
        pwEncoder = new PasswordEncoding(shaPasswordEncoder);
        bCryptEncoder = new PasswordEncoding(new BCryptPasswordEncoder());
    }

    public String encryptPassword(String password){
        // sha256 후, bCrypt.
        String send = bCryptEncoder.encode(pwEncoder.encode(password));
        return send;
    }

    public boolean isMatches(String origin, String password){
        return bCryptEncoder.matches(pwEncoder.encode(password), origin);
    }
}
