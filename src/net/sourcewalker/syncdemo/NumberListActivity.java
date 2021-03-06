package net.sourcewalker.syncdemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import net.sourcewalker.syncdemo.auth.NumbersAuthenticator;
import net.sourcewalker.syncdemo.data.Numbers;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class NumberListActivity extends ListActivity implements OnClickListener {

    private static final int DIALOG_NUMBER = 100;
    private static final Random RANDOM = new Random();
    private ArrayList<String> suggestions;
    private AccountManager accountManager;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.numbers);
        registerForContextMenu(getListView());

        suggestions = new ArrayList<String>();

        Cursor c = managedQuery(Numbers.CONTENT_URI,
                Numbers.DEFAULT_PROJECTION, null, null, null);
        setListAdapter(new SimpleCursorAdapter(this,
                android.R.layout.two_line_list_item, c, new String[] {
                        Numbers._ID, Numbers.STATUS }, new int[] {
                        android.R.id.text1, android.R.id.text2 }));

        accountManager = AccountManager.get(this);
        Account[] accounts = accountManager
                .getAccountsByType(NumbersAuthenticator.TYPE);
        if (accounts.length == 0) {
            startCreateAccount();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.numbers_add:
            showDialog(DIALOG_NUMBER);
            break;
        case R.id.numbers_refresh:
            Account[] accounts = accountManager
                    .getAccountsByType(NumbersAuthenticator.TYPE);
            if (accounts.length == 0) {
                Toast.makeText(this, R.string.numbers_toast_noaccount,
                        Toast.LENGTH_LONG).show();
            } else {
                ContentResolver.requestSync(accounts[0], Numbers.AUTHORITY,
                        new Bundle());
            }
            break;
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_NUMBER:
            suggestions.clear();
            for (int i = 0; i < 5; i++) {
                suggestions.add(Integer.toString(RANDOM.nextInt(100) + 1));
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_title);
            builder.setItems(suggestions.toArray(new String[0]), this);
            return builder.create();
        default:
            throw new IllegalArgumentException("Unknown dialog: " + id);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        removeDialog(DIALOG_NUMBER);
        ContentValues values = new ContentValues();
        values.put(Numbers._ID, Integer.parseInt(suggestions.get(which)));
        values.put(Numbers.STATUS, Numbers.STATUS_LOCAL);
        getContentResolver().insert(Numbers.CONTENT_URI, values);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.context_delete:
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                    .getMenuInfo();
            ContentValues values = new ContentValues();
            values.put(Numbers.STATUS, Numbers.STATUS_DELETED);
            getContentResolver().update(
                    ContentUris.withAppendedId(Numbers.CONTENT_URI, info.id),
                    values, null, null);
            return true;
        default:
            return false;
        }
    }

    private void startCreateAccount() {
        AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {

            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                boolean created = false;
                try {
                    Bundle result = future.getResult();
                    String username = (String) result
                            .get(AccountManager.KEY_ACCOUNT_NAME);
                    if (username != null) {
                        created = true;
                    }
                } catch (OperationCanceledException e) {
                } catch (AuthenticatorException e) {
                } catch (IOException e) {
                }
                if (!created) {
                    Toast.makeText(NumberListActivity.this,
                            R.string.numbers_toast_needaccount,
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        };
        accountManager.addAccount(NumbersAuthenticator.TYPE, null, null, null,
                this, callback, null);
    }

}
