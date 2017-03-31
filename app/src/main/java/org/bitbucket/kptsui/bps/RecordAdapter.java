package org.bitbucket.kptsui.bps;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView carLotId, payment, parkedTime, checkinTime, checkoutTime;
        public ViewHolder(View rootView) {
            super(rootView);
            carLotId = (TextView) rootView.findViewById(R.id.carLotId);
            payment = (TextView) rootView.findViewById(R.id.payment);
            parkedTime = (TextView) rootView.findViewById(R.id.parked_Time);
            checkinTime = (TextView) rootView.findViewById(R.id.in_Time);
            checkoutTime = (TextView) rootView.findViewById(R.id.out_Time);
        }
    }

    private Activity activity;
    private ArrayList<Record> records;

    public RecordAdapter(Activity activity, ArrayList<Record> records) {
        this.records = records;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_viewholder_record, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Record record = records.get(position);
        holder.carLotId.setText(record.carLotId);
        holder.payment.setText("$" + record.payment);
        holder.parkedTime.setText(record.parkedTime + "hr");
        holder.checkinTime.setText(record.checkinTime);
        holder.checkoutTime.setText(record.checkoutTime);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }
}
