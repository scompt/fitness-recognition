package com.scompt.fitnessrecognition.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity implements WearableListView.ClickListener {

    private static final List<String> MENU_ITEMS = Collections.unmodifiableList(Arrays.asList("Record", "Track Pushups"));
    private static final List<Class<? extends Activity>> MENU_ACTIVITIES = Collections.unmodifiableList(Arrays.asList(RecordActivity.class, TrackPushupsActivity.class));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WearableListView mListView = (WearableListView) findViewById(R.id.wearable_list);

        mListView.setAdapter(new Adapter(this));
        mListView.setClickListener(this);
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Adapter.ItemViewHolder itemViewHolder = (Adapter.ItemViewHolder) viewHolder;
        startActivity(new Intent(this, itemViewHolder.mActivityClass));
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    private static final class Adapter extends WearableListView.Adapter {
        private final LayoutInflater mInflater;

        public Adapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public static class ItemViewHolder extends WearableListView.ViewHolder {
            private TextView textView;
            private Class<? extends Activity> mActivityClass;

            public ItemViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.name);
            }
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemViewHolder(mInflater.inflate(R.layout.list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder,
                                     int position) {

            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            TextView view = itemHolder.textView;
            view.setText(MENU_ITEMS.get(position));
            itemHolder.mActivityClass = MENU_ACTIVITIES.get(position);
        }

        @Override
        public int getItemCount() {
            return MENU_ITEMS.size();
        }
    }
}
