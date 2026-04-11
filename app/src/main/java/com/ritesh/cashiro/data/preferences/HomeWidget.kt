package com.ritesh.cashiro.data.preferences

enum class HomeWidget(val displayName: String, val defaultOrder: Int) {
    NETWORTH_SUMMARY("Net Worth", 0),
    ACCOUNT_CAROUSEL("Accounts", 1),
    UPCOMING_SUBSCRIPTIONS("Upcoming Subscriptions", 2),
    RECENT_TRANSACTIONS("Recent Transactions", 3),
    BUDGET_CAROUSEL("Budgets", 4),
    TRANSACTION_HEATMAP("Activity Heatmap", 5);
    
    companion object {
        fun fromName(name: String): HomeWidget? {
            return entries.find { it.name == name }
        }
    }
}
