package discordAuthme;

import fr.xephi.authme.api.v3.AuthMeApi;

public class AuthMeHook {

    public static AuthMeApi authMeApi = null;

    public void initializeAuthMeHook() {
        authMeApi = AuthMeApi.getInstance();
    }

    public void removeAuthMeHook() {
        authMeApi = null;
    }

    public boolean isHookActive() {
        return authMeApi != null;
    }

    public boolean isNameRegistered(String name) {
        return authMeApi != null && authMeApi.isRegistered(name);
    }

}