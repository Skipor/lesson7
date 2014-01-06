package ru.skipor.RssReader;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by Vladimir Skipor on 1/5/14.
 * Email: vladimirskipor@gmail.com
 */
public class AppResultReceiver extends ResultReceiver{
    private final static String TAG = "AppResultReceiver";

        public interface Receiver {
            public void onReceiveResult(int resultCode, Bundle data);
        }

        private Receiver mReceiver;

        public AppResultReceiver(Handler handler) {
            super(handler);
        }

        public void setReceiver(Receiver receiver) {
            mReceiver = receiver;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (mReceiver != null) {
                mReceiver.onReceiveResult(resultCode, resultData);
            }
        }
}
