package net.sourcewalker.syncdemo.auth;

import net.sourcewalker.syncdemo.R;
import net.sourcewalker.syncdemo.server.NumbersClient;
import net.sourcewalker.syncdemo.server.ServerException;
import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class NumbersAuthenticator extends AbstractAccountAuthenticator {

    public static final String TYPE = "net.sourcewalker.syncdemo";

    public static final String TOKEN_TYPE = TYPE + ".token";

    private final Context context;
    private final AccountManager accountManager;

    public NumbersAuthenticator(Context context) {
        super(context);

        this.context = context;
        this.accountManager = AccountManager.get(context);
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
            String accountType, String authTokenType,
            String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
        if (accountType.equals(TYPE) == false) {
            throw new IllegalArgumentException("Invalid account type: "
                    + accountType);
        }
        final Bundle bundle = new Bundle();
        final Intent intent = new Intent(context, AuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                response);
        if (accountManager.getAccountsByType(accountType).length > 0) {
            intent.setAction(AuthenticatorActivity.ACTION_ERROR);
            intent.putExtra(AccountManager.KEY_ERROR_MESSAGE, context
                    .getString(R.string.auth_toast_onlyone));
        }
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
            Account account, Bundle options) throws NetworkErrorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
            String accountType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {
        String username = account.name;
        Bundle result = new Bundle();
        try {
            NumbersClient client = new NumbersClient(username);
            if (client.exists()) {
                result.putString(AccountManager.KEY_ACCOUNT_NAME, username);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, account.name);
            } else {
                result.putInt(AccountManager.KEY_ERROR_CODE, 404);
                result.putString(AccountManager.KEY_ERROR_MESSAGE,
                        "User not found!");
            }
        } catch (ServerException e) {
            throw new NetworkErrorException(e);
        }
        return result;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
            Account account, String[] features) throws NetworkErrorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {
        // TODO Auto-generated method stub
        return null;
    }

}
