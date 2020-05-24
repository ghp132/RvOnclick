package com.example.RvOnclick.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ConfirmDialog {
    public interface OnConfirmationListener {
        void onConfirm(int requestCode);
    }

    public interface OnCancelledListener {
        void onCancelled(int requestCode);
    }

    public void showConfirmationDialog(Context ctx, String dialogTitle, String dialogMessage,
                                       String positiveButtonText, String negativeButtonText,
                                       final int requestCode, final OnConfirmationListener confirmationListener,
                                       final OnCancelledListener cancelledListener) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(ctx, android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmationListener.onConfirm(requestCode);
                    }
                })
                .setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelledListener.onCancelled(requestCode);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
