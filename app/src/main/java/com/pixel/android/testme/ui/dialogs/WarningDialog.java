package com.pixel.android.testme.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

public class WarningDialog extends DialogFragment {
    private static final String MESSAGE_KEY = "message";
    private static final String TAG = "warning_dialog";

    public static void createDialog(FragmentManager manager, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE_KEY, message);
        ErrorDialog dialog = new ErrorDialog();
        dialog.setArguments(bundle);
        dialog.show(manager, TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Varovanie").setMessage(getArguments().getString(MESSAGE_KEY))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        return builder.create();
    }
}
