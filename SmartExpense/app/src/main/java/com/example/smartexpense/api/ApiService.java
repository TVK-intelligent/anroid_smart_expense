package com.example.smartexpense.api;

import com.example.smartexpense.models.AnomalyResponse;
import com.example.smartexpense.models.BudgetDetailResponse;
import com.example.smartexpense.models.BurnRateResponse;
import com.example.smartexpense.models.RecurringTransaction;
import com.example.smartexpense.models.SavingsSuggestion;
import com.example.smartexpense.models.Wallet;
import com.example.smartexpense.models.Transaction;
import com.example.smartexpense.models.Notification;
import com.example.smartexpense.models.Category;
import com.example.smartexpense.models.User;
import com.example.smartexpense.models.dashboard.DashboardResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {

    @GET("api/analytics/anomaly-check")
    Call<AnomalyResponse> checkAnomaly(
            @Query("userId") Integer userId,
            @Query("categoryId") Integer categoryId,
            @Query("amount") BigDecimal amount
    );

    @GET("api/analytics/burn-rate")
    Call<BurnRateResponse> checkBurnRate(
            @Query("userId") Integer userId,
            @Query("categoryId") Integer categoryId
    );

    @GET("api/analytics/savings-suggestions")
    Call<List<SavingsSuggestion>> getSavingsSuggestions(
            @Query("userId") Integer userId
    );

    @GET("api/wallets")
    Call<List<Wallet>> getWallets(
            @Query("userId") Integer userId
    );

    @POST("api/wallets")
    Call<Wallet> createWallet(
            @Body Wallet wallet
    );

    @GET("api/wallets/total-balance")
    Call<Map<String, BigDecimal>> getTotalWalletBalance(
            @Query("userId") Integer userId
    );

    @PUT("api/wallets/{walletId}")
    Call<Wallet> updateWallet(
            @retrofit2.http.Path("walletId") Integer walletId,
            @Query("userId") Integer userId,
            @Body Wallet wallet
    );

    @DELETE("api/wallets/{walletId}")
    Call<Void> deleteWallet(
            @retrofit2.http.Path("walletId") Integer walletId,
            @Query("userId") Integer userId
    );

    @GET("api/notifications")
    Call<List<Notification>> getNotifications(
            @Query("userId") Integer userId
    );

    @GET("api/transactions")
    Call<List<Transaction>> getTransactions(
            @Query("userId") Integer userId
    );

    @POST("api/transactions")
    Call<Transaction> createTransaction(
            @Body Transaction transaction
    );

    @GET("api/transactions/recent")
    Call<List<Transaction>> getRecentTransactions(
            @Query("userId") Integer userId,
            @Query("limit") Integer limit
    );

    @GET("api/transactions/filter")
    Call<List<Transaction>> filterTransactions(
            @Query("userId") Integer userId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("walletId") Integer walletId,
            @Query("categoryId") Integer categoryId,
            @Query("type") String type
    );

    @PUT("api/transactions/{transactionId}")
    Call<Transaction> updateTransaction(
            @retrofit2.http.Path("transactionId") Integer transactionId,
            @Query("userId") Integer userId,
            @Body Transaction transaction
    );

    @DELETE("api/transactions/{transactionId}")
    Call<Void> deleteTransaction(
            @retrofit2.http.Path("transactionId") Integer transactionId,
            @Query("userId") Integer userId
    );

    @GET("api/categories")
    Call<List<Category>> getCategories(
            @Query("userId") Integer userId
    );

    @GET("api/dashboard")
    Call<DashboardResponse> getDashboard(
            @Query("userId") Integer userId
    );

    @GET("api/budgets")
    Call<List<com.example.smartexpense.models.Budget>> getBudgets(
            @Query("userId") Integer userId
    );

    @GET("api/budgets/details")
    Call<List<BudgetDetailResponse>> getBudgetDetails(
            @Query("userId") Integer userId
    );

    @POST("api/budgets")
    Call<com.example.smartexpense.models.Budget> createBudget(
            @Body com.example.smartexpense.models.Budget budget
    );

    @GET("api/savings-goals")
    Call<List<com.example.smartexpense.models.SavingsGoal>> getSavingsGoals(
            @Query("userId") Integer userId
    );

    @POST("api/savings-goals")
    Call<com.example.smartexpense.models.SavingsGoal> createSavingsGoal(
            @Body com.example.smartexpense.models.SavingsGoal goal
    );

    @POST("api/savings-goals/{goalId}/add-funds")
    Call<Map<String, Object>> addFundsToGoal(
            @retrofit2.http.Path("goalId") Integer goalId,
            @Query("amount") java.math.BigDecimal amount
    );

    @GET("api/recurring-transactions")
    Call<List<RecurringTransaction>> getRecurringTransactions(
            @Query("userId") Integer userId
    );

    @POST("api/recurring-transactions")
    Call<RecurringTransaction> createRecurringTransaction(
            @Body RecurringTransaction rt
    );

    @PUT("api/recurring-transactions/{id}/status")
    Call<Void> updateRecurringTransactionStatus(
            @retrofit2.http.Path("id") Integer id,
            @Query("userId") Integer userId,
            @Query("active") boolean active
    );

    @DELETE("api/recurring-transactions/{id}")
    Call<Void> deleteRecurringTransaction(
            @retrofit2.http.Path("id") Integer id,
            @Query("userId") Integer userId
    );

    @POST("api/seed")
    Call<Map<String, Object>> seedDatabase();

    @POST("api/users/register")
    Call<User> register(@Body User user);

    @POST("api/users/login")
    Call<User> login(@Body Map<String, String> credentials);
}
