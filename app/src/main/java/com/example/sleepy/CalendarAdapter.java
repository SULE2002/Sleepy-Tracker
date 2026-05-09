package com.example.sleepy;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(DayItem dayItem);
    }

    private final List<DayItem> days;
    private final OnDayClickListener listener;

    public CalendarAdapter(List<DayItem> days, OnDayClickListener listener) {
        this.days = days;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayItem day = days.get(position);

        if (day.getDayNumber() <= 0) {
            holder.tvDay.setText("");
            holder.viewDot.setVisibility(View.INVISIBLE);
            holder.selectedBg.setVisibility(View.GONE);
            holder.streakBg.setVisibility(View.GONE);
            holder.tvFire.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            return;
        }

        holder.tvDay.setText(String.valueOf(day.getDayNumber()));

        holder.selectedBg.setVisibility(day.isSelected() ? View.VISIBLE : View.GONE);
        holder.tvDay.setTextColor(day.isSelected() ? 0xFFFFFFFF : 0xFF111111);

        if (day.hasSavedEntry()) {
            holder.viewDot.setVisibility(View.VISIBLE);
            GradientDrawable bg = (GradientDrawable) holder.viewDot.getBackground().mutate();
            if (day.getDotType() == 0) {
                bg.setColor(0xFFE74C3C);
            } else if (day.getDotType() == 1) {
                bg.setColor(0xFFF1C40F);
            } else {
                bg.setColor(0xFF2ECC71);
            }
        } else {
            holder.viewDot.setVisibility(View.INVISIBLE);
        }

        if (day.isStreakDay()) {
            holder.streakBg.setVisibility(View.VISIBLE);
            holder.tvFire.setVisibility(View.VISIBLE);
        } else {
            holder.streakBg.setVisibility(View.GONE);
            holder.tvFire.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            for (DayItem item : days) {
                item.setSelected(false);
            }
            day.setSelected(true);
            notifyDataSetChanged();

            if (listener != null) {
                listener.onDayClick(day);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        TextView tvFire;
        View viewDot;
        View selectedBg;
        View streakBg;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvFire = itemView.findViewById(R.id.tvFire);
            viewDot = itemView.findViewById(R.id.viewDot);
            selectedBg = itemView.findViewById(R.id.selectedBg);
            streakBg = itemView.findViewById(R.id.streakBg);
        }
    }
}