package com.example.gopal.inventoryrealm;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Created by Gopal on 8/24/2019.
 */

public class RestoreFileFoundEvent {

    private GoogleSignInAccount googleSignInAccount;

    public GoogleSignInAccount getGoogleSignInAccount() {
        return googleSignInAccount;
    }

    public void setGoogleSignInAccount(GoogleSignInAccount googleSignInAccount) {
        this.googleSignInAccount = googleSignInAccount;
    }
}
