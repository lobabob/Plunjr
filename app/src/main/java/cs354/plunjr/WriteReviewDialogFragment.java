package cs354.plunjr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;


/**
 * Created by Christian on 3/31/2016.
 */
public class WriteReviewDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.post_review_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final AlertDialog d = builder.setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.dialog_submit, null)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setView(dialogView)
                .create();

        // Change positive button listener to validate input before closing dialog
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(validateInput(dialogView)) {
                            // TODO: send form data to server
                            d.dismiss();
                        }
                    }
                });
            }
        });
        return d;
    }

    private boolean validateInput(View v) {
        EditText address = (EditText)  v.findViewById(R.id.dialog_address);
        RatingBar rating = (RatingBar) v.findViewById(R.id.dialog_rating);
        boolean valid = true;

        View addressWarning = v.findViewById(R.id.dialog_address_warning);
        View ratingWarning  = v.findViewById(R.id.dialog_rating_warning);

        // Validate address
        if(address.getText().toString().length() <= 0) {
            addressWarning.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            addressWarning.setVisibility(View.GONE);
        }
        // Validate rating
        if(rating.getRating() <= 0) {
            ratingWarning.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            ratingWarning.setVisibility(View.GONE);
        }
        return valid;
    }
}
