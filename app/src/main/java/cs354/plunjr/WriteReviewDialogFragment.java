package cs354.plunjr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.gms.maps.model.LatLng;
import com.whinc.widget.ratingbar.RatingBar;


public class WriteReviewDialogFragment extends DialogFragment {

    private static final int INVALID_LATLNG = 200;  // Valid Lat/Lng must be below this value
    WriteReviewDialogListener mWriteReviewDialogListener;
    int selectedRating;
    double latValue;
    double lngValue;

    // Used to notify parent activity of a positive click
    public interface WriteReviewDialogListener {
        void onDialogPositiveClick();
        void onDialogNegativeClick();
    }

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
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mWriteReviewDialogListener.onDialogNegativeClick();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mWriteReviewDialogListener.onDialogNegativeClick();
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        selectedRating = 0;
        latValue = INVALID_LATLNG;
        lngValue = INVALID_LATLNG;

        if (args != null) {
            selectedRating = getArguments().getInt("rating");
            latValue = getArguments().getDouble("lat");
            lngValue = getArguments().getDouble("lng");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.post_review_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set initial selected rating in review creation dialog
        ((RatingBar) dialogView.findViewById(R.id.dialog_rating)).setCount(selectedRating);

        // Remove address block if latitude and longitude have already been provided
        if (latValue < INVALID_LATLNG) {
            ((LinearLayout) dialogView.findViewById(R.id.dialog_address_block)).setVisibility(View.GONE);
        }

        final AlertDialog d = builder.setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.dialog_submit, null)
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
                        String rating  = String.valueOf(((RatingBar) dialogView.findViewById(R.id.dialog_rating)).getCount());
                        String user    = d.getContext().getString(R.string.default_user); // TODO: get actual user name when such a thing exists

                        String lat = "";
                        String lng = "";

                        // Get lat and long of restroom as a string
                        if (latValue < INVALID_LATLNG) {
                            lat = String.valueOf(latValue);
                            lng = String.valueOf(lngValue);
                        } else {
                            LatLng rrLatLng = AddressUtils.getAddressLatLng(getActivity(), address);
                            if(rrLatLng != null) {
                                lat = String.valueOf(rrLatLng.latitude);
                                lng = String.valueOf(rrLatLng.longitude);
                            }
                        }

                        String name = AddressUtils.getAddressFeatureName(getActivity(), address);
                        name = name != null ? name : address;

                        // Post review if input is valid
                        if(validateInput(dialogView)) {
                            new PlunjrAPIClient(d.getContext()).postReview(address, user, rating, title, review, name, lat, lng);
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
        if(latValue >= INVALID_LATLNG && !AddressUtils.isAddressValid(getActivity(), address.getText().toString())) {
            addressWarning.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            addressWarning.setVisibility(View.GONE);
        }
        // Validate rating
        if(rating.getCount() <= 0) {
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
