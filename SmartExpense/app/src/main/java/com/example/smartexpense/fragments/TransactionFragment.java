package com.example.smartexpense.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartexpense.R;
import com.example.smartexpense.adapters.TransactionAdapter;
import com.example.smartexpense.api.ApiClient;
import com.example.smartexpense.models.Transaction;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionFragment extends Fragment {

    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        rvTransactions = view.findViewById(R.id.rv_transactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(transactionList);
        rvTransactions.setAdapter(adapter);

        loadTransactions();

        return view;
    }

    private void loadTransactions() {
        ApiClient.getApiService().getTransactions(1).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    transactionList.clear();
                    transactionList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {}
        });
    }
}
