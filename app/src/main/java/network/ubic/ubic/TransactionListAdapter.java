package network.ubic.ubic;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TransactionListAdapter extends ArrayAdapter<TransactionListItem> {
    private final Context context;
    private final ArrayList<TransactionListItem> data;
    private final int layoutResourceId;

    public TransactionListAdapter(Context context, int layoutResourceId, ArrayList<TransactionListItem> data) {
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
            holder.transactionSign = (TextView)row.findViewById(R.id.transactionSign);
            holder.transactionAmount = (TextView)row.findViewById(R.id.transactionAmount);
            holder.transactionDate = (TextView)row.findViewById(R.id.transactionDate);

            row.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)row.getTag();
        }

        TransactionListItem transactionListItem = data.get(position);
        System.out.println("transactionListItem.getTransactionType():" + transactionListItem.getTransactionType());

        if(transactionListItem.getTransactionType().equals("registerPassport")) {
            holder.transactionSign.setText("+");
            holder.transactionAmount.setText("Register passport");
            holder.transactionSign.setTextColor(ContextCompat.getColor(getContext(), R.color.ubicGreen));
        } else if(transactionListItem.getTransactionSign() == -1) {
            holder.transactionSign.setText("-");
            holder.transactionSign.setTextColor(ContextCompat.getColor(getContext(), R.color.ubicPink));
            holder.transactionSign.setTypeface(Typeface.DEFAULT_BOLD);
            holder.transactionAmount.setText(transactionListItem.getTransactionAmount());
        } else if(transactionListItem.getTransactionSign()== 1) {
            holder.transactionSign.setText("+");
            holder.transactionSign.setTextColor(ContextCompat.getColor(getContext(), R.color.ubicGreen));
            holder.transactionAmount.setText(transactionListItem.getTransactionAmount());
        } else {
            holder.transactionSign.setText(" ");
            holder.transactionAmount.setText(transactionListItem.getTransactionAmount());
        }

        holder.transactionDate.setText(transactionListItem.getTransactionDate());

        return row;
    }

    static class ViewHolder
    {
        TextView transactionSign;
        TextView transactionAmount;
        TextView transactionDate;
    }
}
