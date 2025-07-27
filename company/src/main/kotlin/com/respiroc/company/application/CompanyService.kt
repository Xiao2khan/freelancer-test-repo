package com.respiroc.company.application

import com.respiroc.util.payload.CreateCompanyPayload
import com.respiroc.company.domain.model.Company
import com.respiroc.company.domain.repository.CompanyRepository
import com.respiroc.util.currency.CurrencyService
import com.respiroc.util.domain.address.Address
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CompanyService(
    private val companyRepository: CompanyRepository,
    private val currencyService: CurrencyService,
    private val entityManager: EntityManager
) {

    fun getOrCreateCompany(payload: CreateCompanyPayload): Company {
        var company = companyRepository.findCompanyByOrganizationNumberAndName(payload.organizationNumber, payload.name)
        if (company == null)
            company = createCompany(payload)
        else if (company.addressId == null) {
            company.address = getOrCreateAddress(payload)
            company = companyRepository.save(company)
        }
        company.currencyCode = currencyService.getCompanyCurrency(company.countryCode)
        return company
    }

    private fun createCompany(payload: CreateCompanyPayload): Company {
        val address = getOrCreateAddress(payload)
        val company = Company()
        company.name = payload.name
        company.organizationNumber = payload.organizationNumber
        company.countryCode = payload.countryCode
        company.address = address
        return companyRepository.save(company)
    }

    private fun getOrCreateAddress(payload: CreateCompanyPayload): Address? {
        if (!isValidAddress(payload)) return null
        val address = Address(
            city = payload.city!!,
            addressPart1 = payload.addressPart1!!,
            postalCode = payload.postalCode,
            countryIsoCode = payload.addressCountryCode!!,
            addressPart2 = payload.addressPart2,
            administrativeDivisionCode = payload.administrativeDivisionCode
        )
        return Address.upsertAddress(entityManager, address)
    }

    private fun isValidAddress(payload: CreateCompanyPayload): Boolean {
        return !(payload.addressPart1 == null || payload.city == null || payload.addressCountryCode == null)
    }
}