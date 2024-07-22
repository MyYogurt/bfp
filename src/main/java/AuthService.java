import com.bfp.auth.AuthHandler;
import com.bfp.auth.model.InitiateAuthRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthService {

    private final AuthHandler authHandler;

    @Autowired
    public AuthService(AuthHandler authHandler) {
        this.authHandler = authHandler;
    }

    @GetMapping("/authenticate")
    public void authenticate(@NonNull InitiateAuthRequest initiateAuthRequest) {
        authHandler.authenticate(initiateAuthRequest);
    }
}
