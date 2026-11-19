package com.prarambha.hisabsathi.domain.service

import com.prarambha.hisabsathi.domain.model.rule.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleTemplateService @Inject constructor() {

    fun getDefaultRuleTemplates(): List<TransactionRule> {
        return listOf(
            // Just one simple example rule to get users started
            createSmallPaymentsToFoodRule()
        )
    }

    private fun createSmallPaymentsToFoodRule(): TransactionRule {
        return TransactionRule(
            id = UUID.randomUUID().toString(),
            name = "Small Payments to Food",
            description = "Categorize small expense payments (under 200) as Food & Drinks",
            priority = 100,
            conditions = listOf(
                RuleCondition(
                    field = TransactionField.AMOUNT,
                    operator = ConditionOperator.LESS_THAN,
                    value = "200",
                    logicalOperator = LogicalOperator.AND
                ),
                RuleCondition(
                    field = TransactionField.TYPE,
                    operator = ConditionOperator.EQUALS,
                    value = "EXPENSE",
                    logicalOperator = LogicalOperator.AND
                )
            ),
            actions = listOf(
                RuleAction(
                    field = TransactionField.CATEGORY,
                    actionType = ActionType.SET,
                    value = "Food & Drinks"
                )
            ),
            isActive = false, // Users can enable this
            isSystemTemplate = true
        )
    }

    private fun createSalaryDetectionRule(): TransactionRule {
        return TransactionRule(
            id = UUID.randomUUID().toString(),
            name = "Salary Detection",
            description = "Detect salary credits based on keywords",
            priority = 75,
            conditions = listOf(
                RuleCondition(
                    field = TransactionField.TYPE,
                    operator = ConditionOperator.EQUALS,
                    value = "INCOME",
                    logicalOperator = LogicalOperator.AND
                ),
                RuleCondition(
                    field = TransactionField.SMS_TEXT,
                    operator = ConditionOperator.REGEX_MATCHES,
                    value = "(?i)(salary|sal|stipend|wages|payroll)",
                    logicalOperator = LogicalOperator.OR
                ),
                RuleCondition(
                    field = TransactionField.NARRATION,
                    operator = ConditionOperator.REGEX_MATCHES,
                    value = "(?i)(salary|sal|stipend|wages|payroll)",
                    logicalOperator = LogicalOperator.AND
                )
            ),
            actions = listOf(
                RuleAction(
                    field = TransactionField.CATEGORY,
                    actionType = ActionType.SET,
                    value = "Salary"
                )
            ),
            isActive = false
        )
    }

    private fun createRentPaymentRule(): TransactionRule {
        return TransactionRule(
            id = UUID.randomUUID().toString(),
            name = "Rent Payment Detection",
            description = "Identify rent payments based on keywords and amount patterns",
            priority = 80,
            conditions = listOf(
                RuleCondition(
                    field = TransactionField.TYPE,
                    operator = ConditionOperator.EQUALS,
                    value = "EXPENSE",
                    logicalOperator = LogicalOperator.AND
                ),
                RuleCondition(
                    field = TransactionField.SMS_TEXT,
                    operator = ConditionOperator.REGEX_MATCHES,
                    value = "(?i)(rent|landlord|house owner|flat|apartment)",
                    logicalOperator = LogicalOperator.OR
                ),
                RuleCondition(
                    field = TransactionField.NARRATION,
                    operator = ConditionOperator.REGEX_MATCHES,
                    value = "(?i)(rent|landlord|house owner)",
                    logicalOperator = LogicalOperator.AND
                )
            ),
            actions = listOf(
                RuleAction(
                    field = TransactionField.CATEGORY,
                    actionType = ActionType.SET,
                    value = "Home"
                )
            ),
            isActive = false
        )
    }

    private fun createInvestmentDetectionRule(): TransactionRule {
        return TransactionRule(
            id = UUID.randomUUID().toString(),
            name = "Investment Detection",
            description = "Categorize mutual funds, stocks, and other investments",
            priority = 85,
            conditions = listOf(
                RuleCondition(
                    field = TransactionField.SMS_TEXT,
                    operator = ConditionOperator.REGEX_MATCHES,
                    value = "(?i)(mutual fund|sip|share|shares|demat|meroshare|mero share|cdsc|nepse|ipo|fpo|broker|capital)",
                    logicalOperator = LogicalOperator.OR
                ),
                RuleCondition(
                    field = TransactionField.MERCHANT,
                    operator = ConditionOperator.REGEX_MATCHES,
                    value = "(?i)(meroshare|cdsc|nepse|nabil invest|nibl ace|laxmi sunrise capital|siddhartha capital|ns capital)",
                    logicalOperator = LogicalOperator.AND
                )
            ),
            actions = listOf(
                RuleAction(
                    field = TransactionField.CATEGORY,
                    actionType = ActionType.SET,
                    value = "Investment"
                )
            ),
            isActive = false
        )
    }

    private fun createEmiDetectionRule(): TransactionRule {
        return TransactionRule(
            id = UUID.randomUUID().toString(),
            name = "EMI Detection",
            description = "Identify EMI payments",
            priority = 90,
            conditions = listOf(
                RuleCondition(
                    field = TransactionField.TYPE,
                    operator = ConditionOperator.EQUALS,
                    value = "EXPENSE",
                    logicalOperator = LogicalOperator.AND
                ),
                RuleCondition(
                    field = TransactionField.SMS_TEXT,
                    operator = ConditionOperator.REGEX_MATCHES,
                    value = "(?i)(emi|equated monthly|installment|loan)",
                    logicalOperator = LogicalOperator.OR
                ),
                RuleCondition(
                    field = TransactionField.NARRATION,
                    operator = ConditionOperator.CONTAINS,
                    value = "EMI",
                    logicalOperator = LogicalOperator.AND
                )
            ),
            actions = listOf(
                RuleAction(
                    field = TransactionField.CATEGORY,
                    actionType = ActionType.SET,
                    value = "EMI"
                )
            ),
            isActive = false
        )
    }

    private fun createTransferCategorizationRule(): TransactionRule {
        return TransactionRule(
            id = UUID.randomUUID().toString(),
            name = "Transfer Detection",
            description = "Mark contra and transfer transactions",
            priority = 95,
            conditions = listOf(
                RuleCondition(
                    field = TransactionField.NARRATION,
                    operator = ConditionOperator.REGEX_MATCHES,
                    value = "(?i)(contra|transfer|trf|self)",
                    logicalOperator = LogicalOperator.OR
                ),
                RuleCondition(
                    field = TransactionField.SMS_TEXT,
                    operator = ConditionOperator.REGEX_MATCHES,
                    value = "(?i)(transfer to self|own account|linked account)",
                    logicalOperator = LogicalOperator.AND
                )
            ),
            actions = listOf(
                RuleAction(
                    field = TransactionField.TYPE,
                    actionType = ActionType.SET,
                    value = "transfer"
                ),
                RuleAction(
                    field = TransactionField.CATEGORY,
                    actionType = ActionType.SET,
                    value = "Self Transfer"
                )
            ),
            isActive = false
        )
    }

    private fun createSubscriptionDetectionRule(): TransactionRule {
        return TransactionRule(
            id = UUID.randomUUID().toString(),
            name = "Subscription Detection",
            description = "Identify recurring subscriptions like Netflix, Spotify, etc.",
            priority = 105,
            conditions = listOf(
                RuleCondition(
                    field = TransactionField.MERCHANT,
                    operator = ConditionOperator.REGEX_MATCHES,
                    value = "(?i)(netflix|spotify|youtube|apple|google|microsoft|adobe|worldlink|vianet|classic tech|dishhome)",
                    logicalOperator = LogicalOperator.OR
                ),
                RuleCondition(
                    field = TransactionField.SMS_TEXT,
                    operator = ConditionOperator.REGEX_MATCHES,
                    value = "(?i)(subscription|recurring|auto-debit|mandate)",
                    logicalOperator = LogicalOperator.AND
                )
            ),
            actions = listOf(
                RuleAction(
                    field = TransactionField.CATEGORY,
                    actionType = ActionType.SET,
                    value = "Entertainment"
                )
            ),
            isActive = false
        )
    }

    private fun createFuelDetectionRule(): TransactionRule {
        return TransactionRule(
            id = UUID.randomUUID().toString(),
            name = "Fuel/Petrol Detection",
            description = "Categorize fuel and petrol pump transactions",
            priority = 110,
            conditions = listOf(
                RuleCondition(
                    field = TransactionField.MERCHANT,
                    operator = ConditionOperator.REGEX_MATCHES,
                    value = "(?i)(nepal oil|noc|petrol|diesel|fuel|pump|filling station|sajha)",
                    logicalOperator = LogicalOperator.OR
                ),
                RuleCondition(
                    field = TransactionField.SMS_TEXT,
                    operator = ConditionOperator.REGEX_MATCHES,
                    value = "(?i)(petrol|diesel|fuel|pump|filling station)",
                    logicalOperator = LogicalOperator.AND
                )
            ),
            actions = listOf(
                RuleAction(
                    field = TransactionField.CATEGORY,
                    actionType = ActionType.SET,
                    value = "Transport"
                )
            ),
            isActive = false
        )
    }

    private fun createHealthcareDetectionRule(): TransactionRule {
        return TransactionRule(
            id = UUID.randomUUID().toString(),
            name = "Healthcare Detection",
            description = "Identify medical and healthcare expenses",
            priority = 115,
            conditions = listOf(
                RuleCondition(
                    field = TransactionField.MERCHANT,
                    operator = ConditionOperator.REGEX_MATCHES,
                    value = "(?i)(norvic|grande|mediciti|hams|bir hospital|teaching hospital|b&b|om hospital|hospital|clinic|pharmacy|medical|pharma|aushadhi)",
                    logicalOperator = LogicalOperator.OR
                ),
                RuleCondition(
                    field = TransactionField.SMS_TEXT,
                    operator = ConditionOperator.REGEX_MATCHES,
                    value = "(?i)(hospital|doctor|medical|medicine|pharmacy|health|diagnostic|lab|test)",
                    logicalOperator = LogicalOperator.AND
                )
            ),
            actions = listOf(
                RuleAction(
                    field = TransactionField.CATEGORY,
                    actionType = ActionType.SET,
                    value = "Medical"
                )
            ),
            isActive = false
        )
    }
}