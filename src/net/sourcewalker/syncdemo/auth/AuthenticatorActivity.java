package net.sourcewalker.syncdemo.auth;

import net.sourcewalker.syncdemo.R;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AuthenticatorActivity extends AccountAuthenticatorActivity
        implements OnClickListener {

    public static final String ACTION_EDIT = "net.sourcewalker.syncdemo.auth.AuthenticatorActivity.EDIT";

    public static final String ACTION_ERROR = "net.sourcewalker.syncdemo.auth.AuthenticatorActivity.ERROR";

    private AccountManager accountManager;
    private EditText usernameField;
    private Button cancelButton;
    private Button commitButton;
    private Account editAccount = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authenticator);

        accountManager = AccountManager.get(this);

        usernameField = (EditText) findViewById(R.id.auth_username);
        cancelButton = (Button) findViewById(R.id.auth_cancel);
        cancelButton.setOnClickListener(this);
        commitButton = (Button) findViewById(R.id.auth_commit);
        commitButton.setOnClickListener(this);

        if (ACTION_ERROR.equals(getIntent().getAction())) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String message = extras
                        .getString(AccountManager.KEY_ERROR_MESSAGE);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
            finish();
        }

        if (ACTION_EDIT.equals(getIntent().getAction())) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                editAccount = (Account) extras.get("account");
                if (editAccount != null) {
                    usernameField.setText(editAccount.name);
                    usernameField.setEnabled(false);
                } else {
                    finish();
                }
            } else {
                finish();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.auth_cancel:
            cancelAuth();
            break;
        case R.id.auth_commit:
            commitAuth();
            break;
        default:
            throw new IllegalArgumentException("Unknown view clicked: " + view);
        }
    }

    private void cancelAuth() {
        Toast.makeText(this, R.string.auth_toast_cancelled, Toast.LENGTH_LONG)
                .show();
        finish();
    }

    private void commitAuth() {
        if (editAccount == null) {
            createAccount();
        } else {
            editAccount();
        }
    }

    private void editAccount() {
        // Change account options (e.g. password)
        Toast.makeText(this, R.string.auth_toast_edited, Toast.LENGTH_LONG)
                .show();
        finish();
    }

    private void createAccount() {
        String username = usernameField.getText().toString();
        Account account = new Account(username, NumbersAuthenticator.TYPE);
        boolean accountCreated = accountManager.addAccountExplicitly(account,
                null, null);
        if (accountCreated) {
            // ContentResolver.setSyncAutomatically(account,
            // Numbers.AUTHORITY, true);

            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, username);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE,
                    NumbersAuthenticator.TYPE);
            setAccountAuthenticatorResult(result);
            Toast
                    .makeText(this, R.string.auth_toast_created,
                            Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, R.string.auth_toast_createfailed,
                    Toast.LENGTH_LONG).show();
        }
    }

}
