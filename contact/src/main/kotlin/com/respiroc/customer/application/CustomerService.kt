package com.respiroc.customer.application

import com.respiroc.common.payload.NewContactPayload
import com.respiroc.common.service.BaseService
import com.respiroc.customer.domain.model.Customer
import com.respiroc.customer.domain.repository.CustomerRepository
import com.respiroc.util.context.ContextAwareApi
import com.respiroc.util.exception.ResourceAlreadyExistsException
import com.respiroc.util.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val baseService: BaseService
): ContextAwareApi {

    fun createNewCustomer(
        payload: NewContactPayload
    ): Customer {
        return if (payload.privateContact) createPrivateCustomer(payload)
        else createCompanyCustomer(payload)
    }

    private fun createPrivateCustomer(payload: NewContactPayload): Customer {
        val person = baseService.getOrCreatePerson(payload)
        if (customerRepository.existsCustomersByPerson_Name(person.name))
            throw ResourceAlreadyExistsException("Customer already exists")
        return customerRepository.save(
            Customer().apply {
                this.person = person
            }
        )
    }

    private fun createCompanyCustomer(payload: NewContactPayload): Customer {
        val company = baseService.getOrCreateCompany(payload)
        if (customerRepository.existsCustomersByCompany_NameAndCompany_OrganizationNumber(
                company.name, company.organizationNumber
            )
        ) {
            throw ResourceAlreadyExistsException("Customer already exists")
        }
        return customerRepository.save(
            Customer().apply {
                this.company = company
            }
        )
    }

    fun deleteById(id: Long) {
        val exists = customerRepository.existsById(id)
        if (!exists)
            throw ResourceNotFoundException("Customer with id=$id and tenantId=${tenantId()} not found.")
        customerRepository.deleteById(id)
    }

    fun findAllCustomer(): List<Customer> {
        return customerRepository.findCustomers()
    }

    fun findByNameContaining(name: String): List<Customer> {
        return customerRepository.findCustomersByNameContainingIgnoreCase(name)
    }
}