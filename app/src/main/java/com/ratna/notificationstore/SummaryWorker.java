package com.ratna.notificationstore;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SummaryWorker extends Worker {

    public SummaryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Intent intent = new Intent("com.ratna.notificationstore.SEND_SUMMARY");
        getApplicationContext().sendBroadcast(intent);
        return Result.success();
    }
}
