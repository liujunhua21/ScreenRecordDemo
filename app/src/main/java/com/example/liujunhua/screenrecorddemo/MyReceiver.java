package com.example.liujunhua.screenrecorddemo;

/**
 * Created by liujunhua on 18-1-9.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("_notifi_start".equals(action)) {
            //在这里做需要做的事情
            //recordService.startRecord();
             Toast.makeText(context, "录制开始", Toast.LENGTH_SHORT).show();
        }

        if ("_notifi_pause".equals(action)) {
            //在这里做需要做的事情
            //recordService.stopRecord();
        }
    }
}
