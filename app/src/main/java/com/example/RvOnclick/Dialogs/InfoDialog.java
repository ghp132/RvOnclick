package com.example.RvOnclick.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class InfoDialog {
    public interface OnInfoDialogOKListener {
        void onInfoOkClicked();
    }

    public void showInfoDialog(Context ctx, String dialogTitle, String dialogMsg) {

        AlertDialog.Builder builder;
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        builder = new AlertDialog.Builder(ctx, android.R.style.Theme_Material_Dialog_Alert);
        //} else {
        //builder = new AlertDialog.Builder(ctx);
        //}
        builder.setTitle(dialogTitle)
                .setMessage(dialogMsg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //listener.onInfoOkClicked();

                    }
                })
                /*.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })*/
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
