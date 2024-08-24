package com.example.progetto_mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BambinoRankingAdapter extends RecyclerView.Adapter<BambinoRankingAdapter.BambinoViewHolder> {

    private List<Child> bambinoList;

    public BambinoRankingAdapter(List<Child> bambinoList) {
        this.bambinoList = bambinoList;
    }

    @NonNull
    @Override
    public BambinoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bambino_ranking, parent, false);
        return new BambinoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BambinoViewHolder holder, int position) {
        Child bambino = bambinoList.get(position);
        holder.rankTextView.setText(String.valueOf(position + 1));
        holder.nameTextView.setText(bambino.getNome());
        holder.coinsTextView.setText(String.valueOf(bambino.getCoins()));
    }

    @Override
    public int getItemCount() {
        return bambinoList.size();
    }

    static class BambinoViewHolder extends RecyclerView.ViewHolder {
        TextView rankTextView, nameTextView, coinsTextView;

        BambinoViewHolder(@NonNull View itemView) {
            super(itemView);
            rankTextView = itemView.findViewById(R.id.rankTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            coinsTextView = itemView.findViewById(R.id.coinsTextView);
        }
    }
}
