package com.respiroc.webapp.config.navigation

import org.springframework.stereotype.Component

@Component
class NavigationProvider {
    fun getNavigationSections(): List<NavigationSection> {
        return listOf(
            NavigationSection(
                title = "Dashboard", icon = "dashboard",
                items = listOf(NavigationSectionItem(label = "Home", url = "/dashboard"))
            ),

            NavigationSection(
                title = "Users", icon = "file-text",
                items = listOf(
                    NavigationSectionItem(label = "User Manage", url = "/user"),
                )
            ),

            NavigationSection(
                title = "Vouchers", icon = "file-text",
                items = listOf(
                    NavigationSectionItem(label = "Overview", url = "/voucher/overview"),
                    NavigationSectionItem(label = "Advanced Voucher", url = "/voucher/new-advanced-voucher"),
                    NavigationSectionItem(label = "Reception", url = "/voucher-reception")
                )
            ),

            NavigationSection(
                title = "Accounts", icon = "receipt",
                items = listOf(
                    NavigationSectionItem(label = "General Ledger", url = "/ledger/general"),
                    NavigationSectionItem(label = "Chart of Accounts", url = "/ledger/chart-of-accounts"),
                )
            ),

            NavigationSection(
                title = "Reports", icon = "chart-simple",
                items = listOf(
                    NavigationSectionItem(label = "Trial Balance", url = "/report/trial-balance"),
                    NavigationSectionItem(label = "Profit & Loss", url = "/report/profit-loss"),
                    NavigationSectionItem(label = "Balance Sheet", url = "/report/balance-sheet")
                )
            ),

            NavigationSection(
                title = "Contact", icon = "users",
                items = listOf(
                    NavigationSectionItem(label = "Customers", url = "/contact/customer"),
                    NavigationSectionItem(label = "Suppliers", url = "/contact/supplier"),
                    NavigationSectionItem(label = "New Customer", url = "/contact/customer/new"),
                    NavigationSectionItem(label = "New Supplier", url = "/contact/supplier/new")
                )
            ),

            NavigationSection(
                title = "Bank", icon = "bank",
                items = listOf(
                    NavigationSectionItem(label = "Bank Accounts Overview", url = "/bank/account"),
                )
            )
        )
    }
}
