package com.thelioncompany.smssyncer.sender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.util.ArrayList;
import java.util.Collection;

public class IncomingSmsListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            return;
        }

        Collection<Notification> notis = new ArrayList<>();
        for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            notis.add(new Notification(smsMessage.getOriginatingAddress(), smsMessage.getMessageBody()));
        }

        // Send Notis
        final PendingResult result = goAsync();
        Thread thread = new Thread() {
            public void run() {
                FirebaseMessageSender sender = new FirebaseMessageSender(context);
                for (Notification noti : notis) {
                    sender.pushNoti(noti);
                }
                // finish processing
                result.finish();
            }
        };
        thread.start();


    }
}
