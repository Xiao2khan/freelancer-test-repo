package com.respiroc.supplier.application

import com.respiroc.common.payload.NewContactPayload
import com.respiroc.common.service.BaseService
import com.respiroc.supplier.domain.model.Supplier
import com.respiroc.supplier.domain.repository.SupplierRepository
import com.respiroc.util.context.ContextAwareApi
import com.respiroc.util.exception.ResourceAlreadyExistsException
import com.respiroc.util.exception.ResourceNotFoundException
import org.springframework.stereotype.Service

@Service
class SupplierService(
    private val supplierRepository: SupplierRepository,
    private val baseService: BaseService
) : ContextAwareApi {

    fun createNewSupplier(
        payload: NewContactPayload
    ): Supplier {
        return if (payload.privateContact) createPrivateSupplier(payload)
        else createCompanySupplier(payload)
    }

    private fun createPrivateSupplier(payload: NewContactPayload): Supplier {
        val person = baseService.getOrCreatePerson(payload)
        if (supplierRepository.existsSuppliersByPerson_Name(person.name))
            throw ResourceAlreadyExistsException("Supplier already exists")
        return supplierRepository.save(
            Supplier().apply {
                this.person = person
            }
        )
    }

    private fun createCompanySupplier(payload: NewContactPayload): Supplier {
        val company = baseService.getOrCreateCompany(payload)
        if (supplierRepository.existsSuppliersByCompany_NameAndCompany_OrganizationNumber(
                company.name, company.organizationNumber
            )
        ) {
            throw ResourceAlreadyExistsException("Supplier already exists")
        }
        return supplierRepository.save(
            Supplier().apply {
                this.company = company
            }
        )
    }

    fun deleteById(id: Long) {
        val exists = supplierRepository.existsById(id)
        if (!exists)
            throw ResourceNotFoundException("Supplier with id=$id and tenantId=${tenantId()} not found.")
        supplierRepository.deleteById(id)
    }

    fun findAllSupplier(): List<Supplier> {
        return supplierRepository.findSuppliers()
    }

    fun findByNameContaining(name: String): List<Supplier> {
        return supplierRepository.findSuppliersByNameContainingIgnoreCase(name)
    }
}