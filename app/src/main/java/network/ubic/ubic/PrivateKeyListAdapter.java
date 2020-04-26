package network.ubic.ubic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import network.ubic.ubic.Fragments.PrivateKeyFragment;

public class PrivateKeyListAdapter extends ArrayAdapter<PrivateKeyListItem> {
    private final Context context;
    private final ArrayList<PrivateKeyListItem> data;
    private final MainActivity mainActivity;
    private final int layoutResourceId;

    public PrivateKeyListAdapter(Context context, int layoutResourceId, ArrayList<PrivateKeyListItem> data, MainActivity mainActivity) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.mainActivity = mainActivity;
        this.data = data;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            final View finalRow = row;

            holder = new ViewHolder();
            holder.address = (TextView)row.findViewById(R.id.address_preview);
            holder.privateKey = (TextView)row.findViewById(R.id.private_key_preview_1);

            row.findViewById(R.id.setAsDefault).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PrivateKeyStore privateKeyStore = new PrivateKeyStore();
                            privateKeyStore.setAsDefault(context, ((TextView)finalRow.findViewById(R.id.private_key_preview_1)).getText().toString());
                            new AlertDialog.Builder(mainActivity)
                                    .setTitle(mainActivity.getResources().getString(R.string.success))
                                    .setMessage(R.string.successfully_set_the_new_default_privatekey)
                                    .setNegativeButton(mainActivity.getResources().getString(R.string.ok),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    dialog.cancel();
                                                }
                                            })
                                    .setCancelable(true).create().show();
                            mainActivity.goToNavPrivateKey();
                        }
                    }
            );

            row.findViewById(R.id.copyPrivateKeyToClipboard).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            int sdk = android.os.Build.VERSION.SDK_INT;
                            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext()
                                        .getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboard.setText(((TextView)finalRow.findViewById(R.id.private_key_preview_1)).getText());
                            } else {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext()
                                        .getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData
                                        .newPlainText(view.getResources().getString(R.string.message), ((TextView)finalRow.findViewById(R.id.private_key_preview_1)).getText());
                                clipboard.setPrimaryClip(clip);
                            }

                            Toast.makeText(mainActivity, view.getResources().getString(R.string.copied_to_clipboard),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
            );

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
