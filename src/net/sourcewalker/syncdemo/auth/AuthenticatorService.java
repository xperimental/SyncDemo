package net.sourcewalker.syncdemo.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {

    private NumbersAuthenticator authenticator;

    @Override
    public void onCreate() {
        super.onCreate();

        authenticator = new NumbersAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return authenticator.getIBinder();
    }

}
