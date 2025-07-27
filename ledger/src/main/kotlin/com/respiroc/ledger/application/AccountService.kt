package com.respiroc.ledger.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.respiroc.ledger.domain.model.Account
import jakarta.annotation.PostConstruct
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service

@Service
class AccountService {

    private lateinit var accounts: Map<String, Account>

    @PostConstruct
    fun loadChartOfAccounts() {
        val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        val inputStream = ClassPathResource("data/chart-of-accounts.yaml").inputStream

        val data: Map<String, List<Account>> = mapper.readValue(inputStream)
        val accountList = data["accounts"] ?: emptyList()
        accounts = accountList.associateBy { it.noAccountNumber }
    }


    fun findAccountByNumber(noAccountNumber: String): Account? {
        return accounts[noAccountNumber]
    }

    fun findAllAccounts(): Collection<Account> {
        return accounts.values
    }
}