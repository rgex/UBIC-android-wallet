package network.ubic.ubic;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class BalanceListAdapter extends ArrayAdapter<BalanceListItem> {
    private final Context context;
    private final ArrayList<BalanceListItem> data;
    private final int layoutResourceId;

    public BalanceListAdapter(Context context, int layoutResourceId, ArrayList<BalanceListItem> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.data = data;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.countryCode = (ImageView)row.findViewById(R.id.countryCode);
            holder.balanceAmount = (TextView)row.findViewById(R.id.balanceAmount);

            row.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)row.getTag();
        }

        BalanceListItem balanceListItem = data.get(position);

        holder.balanceAmount.setText(balanceListItem.getBalanceAmount());
        holder.countryCode.setImageResource(context.getResources().getIdentifier(balanceListItem.getCurrencyCode().toLowerCase(), "mipmap", context.getPackageName()));

        return row;
    }

    static class ViewHolder
    {
        ImageView countryCode;
        TextView balanceAmount;
    }
}
