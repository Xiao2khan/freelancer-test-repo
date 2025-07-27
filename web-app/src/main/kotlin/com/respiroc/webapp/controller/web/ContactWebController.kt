package com.respiroc.webapp.controller.web

import com.respiroc.customer.application.CustomerService
import com.respiroc.customer.domain.model.ContactType
import com.respiroc.customer.domain.model.Customer
import com.respiroc.supplier.application.SupplierService
import com.respiroc.supplier.domain.model.Supplier
import com.respiroc.webapp.controller.BaseController
import com.respiroc.webapp.controller.request.CreateCustomerRequest
import com.respiroc.webapp.controller.request.toPayload
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.util.Locale.getDefault

@Controller
@RequestMapping(value = ["/contact"])
class ContactWebController(
    private val customerService: CustomerService,
    private val supplierService: SupplierService,
) : BaseController() {

    @GetMapping("/customer")
    fun getCustomers(model: Model): String {
        val customers = customerService.findAllCustomer()
        model.addAttribute("contacts", customers)
        model.addAttribute("type", ContactType.CUSTOMER.type)
        addCommonAttributesForCurrentTenant(model, ContactType.CUSTOMER.type)
        return "contact/contact"
    }

    @GetMapping("/{type}/new")
    fun getContactForm(@PathVariable type: String, model: Model): String {
        val contactType = ContactType.valueOf(type.uppercase(getDefault()))
        model.addAttribute(
            "contact",
            CreateCustomerRequest("", "", false)
        )
        addCommonAttributesForCurrentTenant(model, "New ${contactType.type}")
        model.addAttribute("type", contactType.type)
        return "contact/contact-form"
    }

    @DeleteMapping("/customer/{id}")
    @ResponseBody
    fun deleteCustomer(@PathVariable(name = "id") id: Long) {
        customerService.deleteById(id)
    }

    @GetMapping("/supplier")
    fun getSupplier(model: Model): String {
        val suppliers = supplierService.findAllSupplier()
        model.addAttribute("contacts", suppliers)
        model.addAttribute("type", ContactType.SUPPLIER.type)
        addCommonAttributesForCurrentTenant(model, ContactType.SUPPLIER.type)
        return "contact/contact"
    }

    @DeleteMapping("/supplier/{id}")
    @ResponseBody
    fun deleteSupplier(@PathVariable(name = "id") id: Long) {
        supplierService.deleteById(id)
    }
}

@Controller
@RequestMapping(value = ["/htmx/contact"])
class ContactHTMxController(
    private val customerService: CustomerService,
    private val supplierService: SupplierService,
) : BaseController() {

    @GetMapping("/customer/search")
    fun searchCustomers(@RequestParam("name") name: String, model: Model): String {
        val isExistName = !(name.isBlank())
        val customers: List<Customer> = if (isExistName)
            customerService.findByNameContaining(name)
        else
            customerService.findAllCustomer()
        model.addAttribute("contacts", customers)
        model.addAttribute("type", ContactType.CUSTOMER.type)
        return "fragments/contact-table"
    }

    @GetMapping("/supplier/search")
    fun searchSuppliers(@RequestParam("name") name: String, model: Model): String {
        val tenantId = tenantId()
        val isExistName = !(name.isBlank())
        val suppliers: List<Supplier> = if (isExistName)
            supplierService.findByNameContaining(name)
        else
            supplierService.findAllSupplier()
        model.addAttribute("contacts", suppliers)
        model.addAttribute("type", ContactType.SUPPLIER.type)
        return "fragments/contact-table"
    }

    @GetMapping("/{type}/new")
    @HxRequest
    fun getForm(
        model: Model,
        @PathVariable type: String,
        @RequestParam("privateContact", required = false) privateContact: Boolean = false,
    ): String {
        val contactType = ContactType.valueOf(type.uppercase(getDefault()))
        model.addAttribute(
            "contact",
            CreateCustomerRequest("", "", privateContact)
        )
        model.addAttribute("type", contactType.type)
        return "contact/contact-form :: contactFormFields"
    }

    @PostMapping("/customer")
    fun createCustomer(@ModelAttribute contact: CreateCustomerRequest): String {
        // TODO: add controller adviser
        // TODO: Fix callout fragment to display error messages without refresh page
        customerService.createNewCustomer(contact.toPayload())
        return "redirect:htmx:/contact/customer"
    }

    @PostMapping("/supplier")
    fun createSupplier(@ModelAttribute contact: CreateCustomerRequest): String {
        // TODO: add controller adviser
        // TODO: Fix callout fragment to display error messages without refresh page
        supplierService.createNewSupplier(contact.toPayload())
        return "redirect:htmx:/contact/supplier"
    }
}
