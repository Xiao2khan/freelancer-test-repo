package com.respiroc.ledger.domain.repository

import com.respiroc.ledger.domain.model.Voucher
import com.respiroc.util.repository.CustomJpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface VoucherRepository : CustomJpaRepository<Voucher, Long> {

    @Query("SELECT v FROM Voucher v WHERE v.date BETWEEN :startDate AND :endDate ORDER BY v.number DESC")
    fun findVoucherSummariesDateRange(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Voucher>

    @Query("SELECT v FROM Voucher v LEFT JOIN FETCH v.postings WHERE v.id = :id")
    fun findByIdWithPostings(@Param("id") id: Long): Voucher?

    @Query("SELECT COALESCE(MAX(v.number), 0) FROM Voucher v WHERE EXTRACT(YEAR FROM v.date) = :year")
    fun findMaxVoucherNumberForYear(@Param("year") year: Int): Short

    @Query(
        """
        SELECT v FROM Voucher v 
        WHERE NOT EXISTS (SELECT 1 FROM Posting post WHERE post.voucherId = v.id)
        ORDER BY v.createdAt DESC LIMIT 1
    """
    )
    fun findFirstEmptyVoucher(): Voucher?
    
    fun findFirstByOrderByCreatedAtDesc(): Voucher?
}