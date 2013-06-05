### Fluffy Events ###

Simple events bus ([publish–subscribe](http://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern)) implementation based on regular Android BroadcastReceivers mechanism.

#### Usage ####

First of all you should initialize EventsBus inside your Application class:

    public final class MyApplication extends Application {
        @Override
        public void onCreate() {
            super.onCreate();
            EventsBus.init(this);
        }
    }

To receive events you'll need to register a listener:

    int regId = EventsBus.register(listener);

When you're done listen to the events you need to unregister from events bus using `regId` value received from `register()` method:

    EventsBus.unregister(regId);

For activities it is recommended to register in the `onCreate()` (or in `onPostCreate()`) method and unregister in `onDestroy()` method.  
For fragments it is `onCreateView()` (`onViewCreated()`) and `onDestroyView()` methods respectively.  

Fluffy Events uses integer identifiers for events and Bundle as parameters holder.  
So it is recommended to declare you events ids and events parameters keys in `res/events.xml`:

    <?xml version="1.0" encoding="utf-8"?>
    <resources>

        <!-- Events ids -->
        <item type="id" name="my_event1"/>
        <item type="id" name="my_event2"/>

        <!-- Params keys -->
        <string name="param_message">message</string>

    </resources>

or to create separate java class with constants:

    public final class Events {

        public static final int MY_EVENT1 = 1;
        public static final int MY_EVENT2 = 2;

        public static final String PARAM_MESSAGE = "message";

    }

Now you can start using bus to send events:

    Bundle params = new Bundle();
    params.putString(Events.PARAM_MESSAGE, "Hello");
    EventsBus.send(Events.MY_EVENT1, params);

and to receive them in your registered listener:

    @Override
    public void onEvent(int eventId, Bundle params, boolean isBroadcasted) {
        switch (eventId) {
            case Events.MY_EVENT1:
                String msg = params.getString(Events.PARAM_MESSAGE);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                break;
        }
    }

Available methods:

    send(int eventId);

    send(int eventId, Bundle params);

    send(int eventId, String receiverId);

    send(int eventId, String receiverId, Bundle params);

    sendSticky(int eventId);

    sendSticky(int eventId, Bundle params);

    sendSticky(int eventId, String receiverId);

    sendSticky(int eventId, String receiverId, Bundle params);

Note: in order to send sticky events you'll need to declare `android.permission.BROADCAST_STICKY` permission in AndroidManifest.xml file.

#### How to build ####

You need [Maven](http://maven.apache.org/) to build the project. Just run `mvn clean install` from project's root, jar file will be generated into `target` folder.

#### License ####

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
