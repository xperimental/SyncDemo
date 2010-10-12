package net.sourcewalker.syncdemo.sync;

import java.util.ArrayList;

import net.sourcewalker.syncdemo.data.Numbers;
import net.sourcewalker.syncdemo.server.NumbersClient;
import net.sourcewalker.syncdemo.server.NumbersData;
import net.sourcewalker.syncdemo.server.ServerException;
import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;

public class NumbersSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final int DATA_NONE = 0;
    private static final int DATA_LOCAL = 1;
    private static final int DATA_REMOTE = 2;
    private static final int DATA_BOTH = 3;

    private final Context context;

    public NumbersSyncAdapter(Context context) {
        super(context, true);

        this.context = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        final String username = account.name;
        final ContentResolver cr = context.getContentResolver();
        Cursor localData = null;
        try {
            final NumbersClient client = new NumbersClient(username);
            final NumbersData serverData = client.getNumbers();
            localData = cr.query(Numbers.CONTENT_URI_ALL, new String[] {
                    Numbers._ID, Numbers.STATUS }, null, null, null);
            synchronizeLists(localData, serverData, cr, client, syncResult);
        } catch (ServerException e) {
            syncResult.stats.numIoExceptions++;
        } finally {
            if (localData != null) {
                localData.close();
            }
        }
    }

    private int[] toArray(ArrayList<Integer> list) {
        int[] result = new int[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    private void synchronizeLists(Cursor localData, NumbersData serverData,
            ContentResolver cr, NumbersClient client, SyncResult syncResult)
            throws ServerException {
        int serverIndex = 0;
        ArrayList<Integer> resultList = new ArrayList<Integer>();
        int dataLeft;
        localData.moveToFirst();
        do {
            dataLeft = dataLeft(localData, serverData, serverIndex);
            switch (dataLeft) {
            case DATA_BOTH:
                int localNumber = localData.getInt(0);
                String localStatus = localData.getString(1);
                int remoteNumber = serverData.numbers[serverIndex];
                if (localNumber == remoteNumber) {
                    if (Numbers.STATUS_DELETED.equals(localStatus)) {
                        syncResult.stats.numDeletes++;
                    } else if (Numbers.STATUS_LOCAL.equals(localStatus)) {
                        updateLocalStatus(cr, localNumber,
                                Numbers.STATUS_REMOTE);
                        resultList.add(remoteNumber);
                        syncResult.stats.numUpdates++;
                    } else if (Numbers.STATUS_REMOTE.equals(localStatus)) {
                        resultList.add(remoteNumber);
                    }
                    serverIndex++;
                    localData.moveToNext();
                } else if (localNumber < remoteNumber) {
                    copyLocalToServer(localData, cr, syncResult, resultList);
                } else {
                    serverIndex = copyServerToLocal(serverData, serverIndex,
                            cr, syncResult, resultList);
                }
                break;
            case DATA_REMOTE:
                serverIndex = copyServerToLocal(serverData, serverIndex, cr,
                        syncResult, resultList);
                break;
            case DATA_LOCAL:
                copyLocalToServer(localData, cr, syncResult, resultList);
                break;
            case DATA_NONE:
                break;
            }
        } while (dataLeft > DATA_NONE);
        serverData.numbers = toArray(resultList);
        client.saveNumbers(serverData);
    }

    private void copyLocalToServer(Cursor localData, ContentResolver cr,
            SyncResult syncResult, ArrayList<Integer> resultList) {
        int localNumber = localData.getInt(0);
        String localStatus = localData.getString(1);
        if (Numbers.STATUS_DELETED.equals(localStatus)) {
            cr.delete(ContentUris.withAppendedId(Numbers.CONTENT_URI,
                    localNumber), null, null);
            syncResult.stats.numDeletes++;
        } else {
            if (Numbers.STATUS_REMOTE.equals(localStatus)) {
                updateLocalStatus(cr, localNumber, Numbers.STATUS_DELETED);
                syncResult.stats.numUpdates++;
            } else {
                resultList.add(localNumber);
                updateLocalStatus(cr, localNumber, Numbers.STATUS_REMOTE);
                syncResult.stats.numInserts++;
            }
        }
        localData.moveToNext();
    }

    private int copyServerToLocal(NumbersData serverData, int serverIndex,
            ContentResolver cr, SyncResult syncResult,
            ArrayList<Integer> resultList) {
        int remoteNumber = serverData.numbers[serverIndex];
        ContentValues values = new ContentValues();
        values.put(Numbers._ID, remoteNumber);
        values.put(Numbers.STATUS, Numbers.STATUS_REMOTE);
        cr.insert(Numbers.CONTENT_URI, values);
        resultList.add(remoteNumber);
        syncResult.stats.numInserts++;
        return serverIndex + 1;
    }

    private int dataLeft(Cursor localData, NumbersData serverData,
            int serverIndex) {
        return (localData.isAfterLast() ? DATA_NONE : DATA_LOCAL)
                + (serverIndex < serverData.numbers.length ? DATA_REMOTE
                        : DATA_NONE);
    }

    private void updateLocalStatus(ContentResolver cr, int id, String newStatus) {
        ContentValues values = new ContentValues();
        values.put(Numbers.STATUS, newStatus);
        cr.update(ContentUris.withAppendedId(Numbers.CONTENT_URI, id), values,
                null, null);
    }
}
