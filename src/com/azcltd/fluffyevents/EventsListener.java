package com.azcltd.fluffyevents;

import android.os.Bundle;

public interface EventsListener {

	/**
	 * @param eventId
	 * @param params
	 * @param isBroadcasted
	 *            Whether this event was send directly to this listener or broadcasted for all.
	 */
	void onEvent(int eventId, Bundle params, boolean isBroadcasted);

}
