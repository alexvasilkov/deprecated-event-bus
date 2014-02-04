package com.azcltd.fluffyevents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.os.Bundle;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class EventsBus {

    private static final String RND = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));

    private static final String ACTION_NOTIFIER_PREFIX = "action_fluffy_events_" + RND + "_";
    private static final String BASE_MIME_TYPE = "vnd.fluffy.events." + RND;
    private static final String EVENT_MIME_TYPE_ = BASE_MIME_TYPE + "/event_";

    private static final String EXTRA_EVENT_ID = "extra_event_id_" + RND;
    private static final String EXTRA_RECEIVER_ID = "extra_receiver_id_" + RND;

    private static int sRegistrationId;

    private static Context sAppContext;
    private static String sIntentAction;
    private static final SparseArray<EventsReceiver> sReceiversMap = new SparseArray<EventsReceiver>();
    private static final Map<String, Intent> sStickyEventsMap = new HashMap<String, Intent>();

    public static void init(Context context) {
        if (sAppContext == null) {
            sAppContext = context.getApplicationContext();
            sIntentAction = ACTION_NOTIFIER_PREFIX + sAppContext.getPackageName();
        }
    }

    private static void check() {
        if (sAppContext == null) throw new RuntimeException("EventsBus was not initialized with init(Context) method");
    }

    /**
     * @return Registration id to use in unregister(regId) method
     */
    public static synchronized int register(EventsListener listener) {
        return register(null, listener);
    }

    /**
     * @return Registration id to use in unregister(regId) method
     */
    public static synchronized int register(String receiverId, EventsListener listener) {
        check();

        if (listener == null) throw new NullPointerException("Listener cannot be null");

        EventsReceiver receiver = new EventsReceiver();
        receiver.mReceiverId = receiverId;
        receiver.mListener = listener;

        try {
            sAppContext.registerReceiver(receiver, new IntentFilter(sIntentAction, BASE_MIME_TYPE + "/*"));
        } catch (MalformedMimeTypeException e) {
            e.printStackTrace();
        }

        sReceiversMap.put(++sRegistrationId, receiver);

        for (Intent sticky : sStickyEventsMap.values()) {
            receiver.onReceive(sAppContext, sticky);
        }

        return sRegistrationId;
    }

    public static synchronized void unregister(int regId) {
        check();
        EventsReceiver receiver = sReceiversMap.get(regId);
        sReceiversMap.delete(regId);
        if (receiver != null) sAppContext.unregisterReceiver(receiver);
    }

    public static void send(int eventId) {
        send(eventId, null, false, null);
    }

    public static void send(int eventId, Bundle params) {
        send(eventId, params, false, null);
    }

    public static void send(int eventId, String receiverId) {
        send(eventId, null, false, receiverId);
    }

    public static void send(int eventId, String receiverId, Bundle params) {
        send(eventId, params, false, receiverId);
    }

    public static void sendSticky(int eventId) {
        send(eventId, null, true, null);
    }

    public static void sendSticky(int eventId, Bundle params) {
        send(eventId, params, true, null);
    }

    public static void sendSticky(int eventId, String receiverId) {
        send(eventId, null, true, receiverId);
    }

    public static void sendSticky(int eventId, String receiverId, Bundle params) {
        send(eventId, params, true, receiverId);
    }

    private synchronized static void send(int eventId, Bundle params, boolean sticky, String receiverId) {
        check();
        String mimeType = buildMimeType(eventId, receiverId);
        Intent intent = new Intent(sIntentAction).setType(mimeType);

        if (params != null) intent.putExtras(params);
        intent.putExtra(EXTRA_EVENT_ID, eventId);
        if (receiverId != null) intent.putExtra(EXTRA_RECEIVER_ID, receiverId);

        if (sticky) sStickyEventsMap.put(mimeType, intent);

        sAppContext.sendBroadcast(intent);
    }

    /**
     * Removes sticky event for given id.<br/>
     * Notification with event id = "-eventId" will be send, if sticky event was successfully removed.
     */
    public static void removeSticky(int eventId) {
        removeSticky(eventId, null);
    }

    /**
     * Removes sticky event for given id and given receiver.<br/>
     * Notification with event id = "-eventId" will be send, if sticky event was successfully removed.
     */
    public synchronized static void removeSticky(int eventId, String receiverId) {
        check();

        String mimeType = buildMimeType(eventId, receiverId);
        Intent sticky = sStickyEventsMap.remove(mimeType);
        if (sticky != null) send(-eventId, receiverId, sticky.getExtras());
    }

    private static String buildMimeType(int eventId, String receiverId) {
        return EVENT_MIME_TYPE_ + eventId + (receiverId == null ? "" : "_" + receiverId);
    }

    private static class EventsReceiver extends BroadcastReceiver {

        private String mReceiverId;
        private EventsListener mListener;

        @Override
        public final void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(EXTRA_EVENT_ID)) {
                Bundle params = intent.getExtras();
                String targetReceiverId = params.getString(EXTRA_RECEIVER_ID);

                if (targetReceiverId == null || targetReceiverId.equals(mReceiverId)) {
                    mListener.onEvent(params.getInt(EXTRA_EVENT_ID), params, targetReceiverId == null);
                }
            }
        }

    }

}
