package network.ubic.ubic;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PrivateKeyListAdapter extends ArrayAdapter<PrivateKeyListItem> {
    private final Context context;
    private final ArrayList<PrivateKeyListItem> data;
    private final int layoutResourceId;

    public PrivateKeyListAdapter(Context context, int layoutResourceId, ArrayList<PrivateKeyListItem> data) {
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
            holder.address = (TextView)row.findViewById(R.id.address_preview);
            holder.privateKey = (TextView)row.findViewById(R.id.private_key_preview_1);

            row.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)row.getTag();
        }

        PrivateKeyListItem privateKeyListItem = data.get(position);

        holder.address.setText(privateKeyListItem.getAddress());
        holder.privateKey.setText(privateKeyListItem.getPrivateKey());

        return row;
    }

    static class ViewHolder
    {

        TextView address;
        TextView privateKey;
    }
}
