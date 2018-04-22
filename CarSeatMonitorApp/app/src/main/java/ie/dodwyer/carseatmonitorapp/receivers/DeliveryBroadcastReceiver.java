package ie.dodwyer.carseatmonitorapp.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import ie.dodwyer.carseatmonitorapp.activities.Base;
import ie.dodwyer.carseatmonitorapp.main.CarSeatMonitorApp;


/*
* This class was based on code from the following web page:
* https://www.androidauthority.com/how-to-create-an-sms-app-721438/
* */
public class DeliveryBroadcastReceiver extends BroadcastReceiver {
    private static final String ACTION_SMS_DELIVERED = "SMS_DELIVERED";
    protected Base activity;
    CarSeatMonitorApp app;
    @Override
    public void onReceive(Context context, Intent intent) {
        app = ((CarSeatMonitorApp) context.getApplicationContext());
        String action = intent.getAction();

        if (action.equals(ACTION_SMS_DELIVERED)) {

            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Bundle b = intent.getExtras();
                    if (b != null) {
                        String contact = b.getString("contact");
                        Toast.makeText(context, "The alert was delivered to '"+contact+"'.", Toast.LENGTH_LONG).show();
                    }

                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(context, "The alert was not delivered.",
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

}