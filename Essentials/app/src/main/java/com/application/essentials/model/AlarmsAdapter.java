package com.application.essentials.model;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.application.essentials.AlarmsFragment;
import com.application.essentials.R;
import com.application.essentials.db.AlarmEntity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AlarmsAdapter extends RecyclerView.Adapter<AlarmsAdapter.MyViewHolder> {

    AdapterFragmentInterface alarmsFragmentCallback;

    public List<AlarmEntity> mAlarmEntityList = new ArrayList<>();
    public AlarmEntity alarmEntity;
    final public String[] mShortWeekDayStrings = {"M", "T", "W", "T", "F", "S", "S"};
    public TextView tvDay;


    public AlarmsAdapter(AlarmsFragment alarmsFragment) {
        alarmsFragmentCallback = alarmsFragment;
        setHasStableIds(true);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView tvTime;
        private LinearLayout linearDaysArrayVar;
        public TextView tvDay;
        private ImageButton imgButtonAlarm;
        private ImageButton toggleIconRepeatWeekly;


        private View.OnClickListener rowOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.e("onRowClick",""+mAlarmEntityList.get(getAdapterPosition()));
                alarmsFragmentCallback.launchSetAlarmActivity(mAlarmEntityList.get(getAdapterPosition()), 2);
            }
        };


        private View.OnLongClickListener rowOnLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                alarmsFragmentCallback.showDeleteDialog(getAdapterPosition(), getItemCount(), mAlarmEntityList.get(getAdapterPosition()));
                return true;
            }
        };

        // Todo: use alarmEntity object to set alarm on/off directly, instead of creating new instance
        private View.OnClickListener imgButtonAlarmOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                AlarmEntity alarmEntity = mAlarmEntityList.get(position);
                        // had made a mistake here. referenced it directly. Java is pass by value (of reference)!
                AlarmEntity alarmEntityAtPosition = new AlarmEntity(alarmEntity.getAlarmId(), alarmEntity.getHourOfDay(), alarmEntity.getMinute(), alarmEntity.getLabel(), alarmEntity.getAlarmOn(), alarmEntity.getRepeatWeekly(), alarmEntity.getDaysArr(), alarmEntity.getIntervalTime(), alarmEntity.getDurationTime(), alarmEntity.getEndAtTimeHour(), alarmEntity.getEndAtTimeMinute(), alarmEntity.getAlarmStartTimeInMillis(), alarmEntity.getAlarmEndTimeInMillis(), alarmEntity.getVibrate(), alarmEntity.getUpcomingAlarmTime(), alarmEntity.getAscendingVolumeMax());
                alarmEntityAtPosition.setAlarmOn(!alarmEntityAtPosition.getAlarmOn());
                alarmsFragmentCallback.saveAlarm(alarmEntityAtPosition, 3);
            }
        };


        private MyViewHolder(@NonNull View view) {
            super(view);
            this.view = view;
            this.view.setOnClickListener(rowOnClickListener);
            this.view.setOnLongClickListener(rowOnLongClickListener);

            tvTime = (TextView) view.findViewById(R.id.tvTime);
            linearDaysArrayVar = (LinearLayout) view.findViewById(R.id.linearDaysArray);
            imgButtonAlarm = view.findViewById(R.id.imgClock);
            toggleIconRepeatWeekly = (ImageButton) view.findViewById(R.id.toggleIconRepeatWeekly);
            imgButtonAlarm.setOnClickListener(imgButtonAlarmOnClickListener);
        }
    }


    public class DiffUtilCallBack extends DiffUtil.Callback {
        private final List<AlarmEntity> oldList;
        private final List<AlarmEntity> newList;

        public DiffUtilCallBack(List<AlarmEntity> oldList, List<AlarmEntity> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldPosition, int newPosition) {
            // oldPosition, newPosition is the old and new position of the item
            Boolean itemsSameBool = oldList.get(oldPosition).getAlarmId() ==
                    newList.get(newPosition).getAlarmId();
            return itemsSameBool;
        }

        @Override
        public boolean areContentsTheSame(int oldPosition, int newPosition) {
            AlarmEntity old2 = oldList.get(oldPosition);
            AlarmEntity new2 = newList.get(newPosition);

            Boolean contentsSame = old2.toString().equals(new2.toString());
            return contentsSame;
        }


        @Override
        public Object getChangePayload(int oldPosition, int newPosition) {
            Bundle payload = new Bundle();
            AlarmEntity payloadOldAlarmEntity = oldList.get(oldPosition);
            AlarmEntity payloadNewAlarmEntity = newList.get(newPosition);

            //Log.e("oldList",""+oldList.get(oldPosition));
            //Log.e("newList",""+newList.get(newPosition));
            if (payloadOldAlarmEntity.getAlarmOn() != payloadNewAlarmEntity.getAlarmOn()) {
                payload.putString("ALARM_ON_CHANGE", "changed");
            }

            if (payloadOldAlarmEntity.getHourOfDay() != payloadNewAlarmEntity.getHourOfDay() ||
                    payloadOldAlarmEntity.getMinute() != payloadNewAlarmEntity.getMinute()) {

                DecimalFormat df = new DecimalFormat("00");
                String hour = df.format(payloadNewAlarmEntity.getHourOfDay());
                String minute = df.format(payloadNewAlarmEntity.getMinute());
                String time = hour + ":" + minute;
                payload.putString("TIME_CHANGE", time);
            }

            if (!payloadOldAlarmEntity.getDaysArr().toString().equals(payloadNewAlarmEntity.getDaysArr().toString())) {
                payload.putIntegerArrayList("DAYS_ARR_CHANGE",(ArrayList<Integer>)payloadNewAlarmEntity.getDaysArr());
            }

            if (payloadOldAlarmEntity.getRepeatWeekly() != payloadNewAlarmEntity.getRepeatWeekly()) {
                payload.putString("REPEAT_WEEKLY_CHANGE", "changed");
            }


            if (payload.isEmpty()) {
                Log.e("payload", "payload empty" + payload);
                return null;
            } else {
                Log.e("payload", "payload not empty" + payload);
                return payload;
            }
        }
    }


    public void setAlarmEntity(final List<AlarmEntity> newAlarmEntityList) {
        if (this.mAlarmEntityList == null) {
            Log.e("livedata", "alarmEntityList null");
            this.mAlarmEntityList = newAlarmEntityList;
            notifyItemRangeInserted(0, this.mAlarmEntityList.size());
        }
        else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtilCallBack(mAlarmEntityList, newAlarmEntityList));

            result.dispatchUpdatesTo(this);
            mAlarmEntityList.clear();
            Log.e("newAlarmEntityList","before addAll: "+newAlarmEntityList);
            mAlarmEntityList.addAll(newAlarmEntityList);
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_layout, viewGroup, false);

        return new MyViewHolder(view);
    }


    public void inflateDays(MyViewHolder viewHolder, List<Integer> daysArr) {
        if (viewHolder.linearDaysArrayVar != null) {
            viewHolder.linearDaysArrayVar.removeAllViews();
        }

        for (int i = 0; i < 7; i++) {
            final View tvLayout = LayoutInflater.from(viewHolder.linearDaysArrayVar.getContext()).inflate(R.layout.day_textview,
                    viewHolder.linearDaysArrayVar, false);

            CheckedTextView tvDay = tvLayout.findViewById(R.id.tvDay);
            tvDay.setText(mShortWeekDayStrings[i]);
            tvDay.setChecked(daysArr.get(i) == 1);

            if (tvDay.getParent() != null) {
                ((LinearLayout) tvDay.getParent()).removeView(tvDay);
            }
            viewHolder.linearDaysArrayVar.addView(tvDay);
        }
    }


    private void checkAlarmEnded(final MyViewHolder viewHolder, AlarmEntity alarmEntity) {
        if (System.currentTimeMillis() > alarmEntity.getLastAlarmTimeInMillis()) {
            if (alarmEntity.getAlarmOn() && !alarmEntity.getRepeatWeekly()
                    && (System.currentTimeMillis()) > alarmEntity.getAlarmEndTimeInMillis()) {         // check if no remaining alarms in entity
                alarmEntity.setAlarmOn(false);
                alarmsFragmentCallback.saveAlarm(alarmEntity, 3);
                viewHolder.imgButtonAlarm.setSelected(false);
            }
            else { viewHolder.imgButtonAlarm.setSelected(alarmEntity.getAlarmOn()); }
        }
        else {
            viewHolder.imgButtonAlarm.setSelected(alarmEntity.getAlarmOn());
        }


        if (alarmEntity.getLabel().equalsIgnoreCase("TEMP") && !alarmEntity.getRepeatWeekly()) {
            if (System.currentTimeMillis() > alarmEntity.getLastAlarmTimeInMillis()) {
                if (System.currentTimeMillis() > (alarmEntity.getAlarmEndTimeInMillis())) {
                    alarmsFragmentCallback.deleteAlarm(alarmEntity);
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder viewHolder, int position) {
        alarmEntity = this.mAlarmEntityList.get(position);

        DecimalFormat df = new DecimalFormat("00");
        String hour = df.format(alarmEntity.getHourOfDay());
        String minute = df.format(alarmEntity.getMinute());

        viewHolder.tvTime.setText(hour + ":" + minute);      // this worked: VH, Adapter, CardView with RecyclerView and Adapter (no db): 25/12, 1-day
        inflateDays(viewHolder, alarmEntity.getDaysArr());

        checkAlarmEnded(viewHolder, alarmEntity);
        viewHolder.toggleIconRepeatWeekly.setSelected(alarmEntity.getRepeatWeekly());
    }


    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder viewHolder, int position, List<Object> payloads) {
        if (!payloads.isEmpty()) {
            Log.e("payloads","not null");
            Bundle bundle = (Bundle) payloads.get(0);
            String alarmOnString = (String) bundle.get("ALARM_ON_CHANGE");
            String time = (String)bundle.get("TIME_CHANGE");
            List<Integer> daysArr = (List<Integer>) bundle.getIntegerArrayList("DAYS_ARR_CHANGE");
            String repeatWeeklyString = (String) bundle.get("REPEAT_WEEKLY_CHANGE");

            if (alarmOnString != null) {
                if (viewHolder.imgButtonAlarm.isSelected()) {
                    viewHolder.imgButtonAlarm.setSelected(false);
                    //viewHolder.view.setAlpha(0.5f);
                } else {
                    viewHolder.imgButtonAlarm.setSelected(true);
                   // viewHolder.view.setAlpha(1.0f);
                }
            }

            if (time != null) { viewHolder.tvTime.setText(time); }

            if (daysArr != null) { inflateDays(viewHolder, daysArr); }

            if (repeatWeeklyString != null) {
                if (viewHolder.toggleIconRepeatWeekly.isSelected()) {
                    viewHolder.toggleIconRepeatWeekly.setSelected(false);
                } else {
                    viewHolder.toggleIconRepeatWeekly.setSelected(true);
                }
            }
        }
        else {
            super.onBindViewHolder(viewHolder, position, null);
//            if (!alarmEntity.getAlarmOn()) {
//                viewHolder.view.setAlpha(0.5f);
//                Log.e("setAlpha","setAlpha called");
//            }
            Log.e("payloads","null");
        }
    }


    @Override
    public int getItemCount() {
        return ( mAlarmEntityList == null ? 0 : mAlarmEntityList.size() );
    }

    @Override
    public long getItemId(int position) {
       return mAlarmEntityList.get(position).getAlarmId();
    }
}