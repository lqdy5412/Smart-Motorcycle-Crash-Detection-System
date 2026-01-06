package com.aya.motorcyclealertreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null && pdus.length > 0) {

                SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdus[0]);
                String messageBody = msg.getMessageBody();

                Intent i = new Intent(context, MainActivity.class);
                i.putExtra("sms_body", messageBody);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }
}
