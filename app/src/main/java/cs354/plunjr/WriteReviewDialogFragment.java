package cs354.plunjr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;


public class WriteReviewDialogFragment extends DialogFragment {

    // Used to notify parent activity of a positive click
    public interface WriteReviewDialogListener {
        public void onDialogPositiveClick();
    }

    WriteReviewDialogListener mWriteReviewDialogListener;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mWriteReviewDialogListener = (WriteReviewDialogListener) activity;
        } catch(ClassCastException e) {
            // Parent activity must implement WriteReviewDialogListener interface
        }
    }

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

                        String address = ((EditText) dialogView.findViewById(R.id.dialog_address)).getText().toString();
                        String review  = ((EditText) dialogView.findViewById(R.id.dialog_review)).getText().toString();
                        String title   = ((EditText) dialogView.findViewById(R.id.dialog_title)).getText().toString();
                        String rating  = String.valueOf(((RatingBar) dialogView.findViewById(R.id.dialog_rating)).getRating());

                        // Post review if input is valid
                        if(validateInput(dialogView)) {
                            PlunjrAPIClient client = new PlunjrAPIClient();
                            client.postReview(getActivity(), address, getString(R.string.default_user), rating, title, review);
                            mWriteReviewDialogListener.onDialogPositiveClick();
                            d.dismiss();
                        }
                    }
                });
            }
        });
        // Set address to current user location on button click
        ImageButton userLocation = (ImageButton) dialogView.findViewById(R.id.dialog_button_user_location);
        userLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddressAutoCompleteTextView addressView = (AddressAutoCompleteTextView) dialogView.findViewById(R.id.dialog_address);
                addressView.setDropdownContents(AddressUtils.getUserAddresses(getActivity()));
                addressView.showDropDown();
            }
        });
        return d;
    }

    private boolean validateInput(View v) {
        EditText address = (EditText)  v.findViewById(R.id.dialog_address);
        EditText desc    = (EditText)  v.findViewById(R.id.dialog_review);
        EditText title   = (EditText)  v.findViewById(R.id.dialog_title);
        RatingBar rating = (RatingBar) v.findViewById(R.id.dialog_rating);
        boolean valid = true;

        View addressWarning = v.findViewById(R.id.dialog_address_warning);
        View ratingWarning  = v.findViewById(R.id.dialog_rating_warning);
        View titleWarning   = v.findViewById(R.id.dialog_title_warning);

        // Validate address
        if(!AddressUtils.isAddressValid(getActivity(), address.getText().toString())) {
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
        // Validate title + description (must have both if there is a title)
        if(title.getText().toString().length() > 0 && desc.getText().toString().length() <= 0) {
            titleWarning.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            titleWarning.setVisibility(View.GONE);
        }
        return valid;
    }
}
