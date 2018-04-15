package ie.dodwyer.carseatmonitorapp.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

/**
 * Created by User on 4/17/2017.
 */

public class SendBroadcastReceiver extends BroadcastReceiver {
    private static final String ACTION_SMS_SENT = "SMS_SENT";
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (action.equals(ACTION_SMS_SENT)) {

            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Bundle b = intent.getExtras();
                    if (b != null) {

                        String contact = b.getString("contact");
                        String alertType = b.getString("alertType");
                        String message = "A '"+alertType+"' alert has been sent to secondary Contact: "+contact+"'.";
                        Toast.makeText(context, message,
                                Toast.LENGTH_SHORT).show();
                    }

                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "The alert has not been sent due to 'generic failure' error.", Toast.LENGTH_LONG)
                            .show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "The alert has not been sent due to 'no service' error.", Toast.LENGTH_LONG)
                            .show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "The alert has not been sent due to 'null PDU' error.", Toast.LENGTH_LONG).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "The alert has not been sent due to 'radio off' error.", Toast.LENGTH_LONG).show();
                    break;
            }

        }
    }

}