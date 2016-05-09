package cs354.plunjr.UI;

import android.content.Context;
import android.content.res.TypedArray;
import android.location.Address;
import android.location.Geocoder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cs354.plunjr.Util.AddressUtil;
import cs354.plunjr.R;

public class AddressAutoCompleteTextView extends AutoCompleteTextView {

    private AutoCompleteAdapter mAdapter;
    private int mMaxSuggestions;


    public AddressAutoCompleteTextView(Context context) {
        this(context, null);
    }

    public AddressAutoCompleteTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.autoCompleteTextViewStyle);
    }

    public AddressAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAdapter = new AutoCompleteAdapter(context);
        setAdapter(mAdapter);

        // Obtain custom attributes
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AddressAutoCompleteTextView, 0, 0);
        try {
            mMaxSuggestions = a.getInt(R.styleable.AddressAutoCompleteTextView_max_suggestions, 1);
        } finally {
            a.recycle();
        }
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    public void setDropdownContents(Collection<Address> addresses) {
        mAdapter.clear();
        mAdapter.addAll(addresses);
        mAdapter.notifyDataSetChanged();
    }

    /*
     * Taken from http://android.foxykeep.com/dev/how-to-add-autocompletion-to-an-edittext
     */
    private class AutoCompleteAdapter extends ArrayAdapter<Address> implements Filterable {

        private LayoutInflater mInflater;
        private Geocoder mGeocoder;

        public AutoCompleteAdapter(final Context context) {
            super(context, -1);
            mInflater = LayoutInflater.from(context);
            mGeocoder = new Geocoder(context);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final TextView tv;
            if (convertView != null) {
                tv = (TextView) convertView;
            } else {
                tv = (TextView) mInflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            }
            tv.setText(AddressUtil.formatAsString(getItem(position)));
            return tv;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(final CharSequence constraint) {
                    List<Address> addressList = null;
                    if (constraint != null) {
                        try {
                            addressList = mGeocoder.getFromLocationName((String) constraint, mMaxSuggestions);
                        } catch (IOException e) {
                        }
                    }
                    if (addressList == null) {
                        addressList = new ArrayList<>();
                    }
                    final FilterResults filterResults = new Filter.FilterResults();
                    filterResults.values = addressList;
                    filterResults.count = addressList.size();

                    return filterResults;
                }

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(final CharSequence constraint, final FilterResults results) {
                    clear();
                    for (Address address : (List<Address>) results.values) {
                        add(address);
                    }
                    if (results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }

                @Override
                public CharSequence convertResultToString(final Object resultValue) {
                    return resultValue == null ? "" : AddressUtil.formatAsString((Address) resultValue);
                }
            };
        }
    }
}
