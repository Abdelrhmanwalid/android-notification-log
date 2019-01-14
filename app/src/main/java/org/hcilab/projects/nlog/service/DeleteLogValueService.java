package org.hcilab.projects.nlog.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;


public class DeleteLogValueService extends IntentService {

    public static final String ACTION_DELETE = "org.hcilab.projects.nlog.service.action.delete";

    public static final String EXTRA_ID = "org.hcilab.projects.nlog.service.extra.ID";

    public DeleteLogValueService() {
        super("DeleteLogValueService");
    }

    public static void deleteValue(Context context, long id) {
        Intent intent = new Intent(context, DeleteLogValueService.class);
        intent.setAction(ACTION_DELETE);
        intent.putExtra(EXTRA_ID, id);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DELETE.equals(action)) {
                final long id = intent.getLongExtra(EXTRA_ID, -1);
                deleteLoggedValue(id);
            }
        }
    }

    private void deleteLoggedValue(long id) {
        NotificationHandler notificationHandler = new NotificationHandler(this);
        notificationHandler.deleteLoggedValue(id);
    }
}
