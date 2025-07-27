package com.respiroc.attachment.application

import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam


@Service
class AttachmentService {
    private val imageMimeTypes = setOf(
        "image/png", "image/jpeg", "image/jpg",
        "image/bmp", "image/gif"
    )

    fun convertToPdf(
        fileData: ByteArray,
        filename: String,
        mimeType: String
    ): Triple<ByteArray, String, String> {

        /* 1. short‑circuit if it is already a PDF ----------------- */
        if (mimeType.equals("application/pdf", true)) {
            return Triple(fileData, ensurePdfExt(filename), "application/pdf")
        }

        /* 2. only PNG/JPEG/BMP/GIF accepted ----------------------- */
        val lowerMime = mimeType.lowercase()
        require(lowerMime in imageMimeTypes) {
            "Unsupported MIME type “$mimeType”. Allowed: PNG, JPEG, BMP, GIF, or PDF."
        }

        /* 3. decode to BufferedImage ------------------------------ */
        val buffered = ImageIO.read(ByteArrayInputStream(fileData))
            ?: throw IllegalArgumentException("Unable to decode image data for $mimeType")

        /* 4. convert → JPEG bytes in standard sRGB ---------------- */
        val jpegBytes = bufferedToJpegBytes(buffered)

        /* 5. wrap JPEG in a single‑page PDF ----------------------- */
        val pdfBaos = ByteArrayOutputStream()
        val writer = PdfWriter(pdfBaos, WriterProperties().setCompressionLevel(9))
        val pdfDoc = PdfDocument(writer)

        val pageSize = PageSize.LETTER
        val document = Document(pdfDoc, pageSize)
            .apply { setMargins(0f, 0f, 0f, 0f) }   // ← **no margins**

        val imgData = ImageDataFactory.create(jpegBytes)
        val image = Image(imgData).apply {
            scaleToFit(pageSize.width, pageSize.height)
            /* centre on the page (optional but nice) */
            val x = (pageSize.width - imageScaledWidth) / 2
            val y = (pageSize.height - imageScaledHeight) / 2
            setFixedPosition(x, y)
        }

        document.add(image)
        document.close() // flush & close everything

        return Triple(pdfBaos.toByteArray(), ensurePdfExt(filename), "application/pdf")
    }

    private fun bufferedToJpegBytes(src: BufferedImage): ByteArray {
        // --- force sRGB without alpha ------------------------------------------
        val rgbImg = if (src.type == BufferedImage.TYPE_3BYTE_BGR ||
            src.type == BufferedImage.TYPE_INT_RGB
        ) {
            src
        } else {
            val converted = BufferedImage(
                src.width, src.height,
                BufferedImage.TYPE_3BYTE_BGR
            )
            val g = converted.createGraphics()
            g.drawImage(src, 0, 0, java.awt.Color.WHITE, null) // fills alpha w/ white
            g.dispose()
            converted
        }

        val quality = 0.7f
        ByteArrayOutputStream().use { baos ->
            val writer = ImageIO.getImageWritersByFormatName("jpg").next()
            ImageIO.createImageOutputStream(baos).use { ios ->
                writer.output = ios
                writer.defaultWriteParam.apply {
                    if (canWriteCompressed()) {
                        compressionMode = ImageWriteParam.MODE_EXPLICIT
                        compressionQuality = quality
                    }
                }.also { param ->
                    writer.write(null, IIOImage(rgbImg, null, null), param)
                }
                writer.dispose()
            }
            return baos.toByteArray()
        }
    }

    private fun ensurePdfExt(name: String): String =
        if (name.lowercase().endsWith(".pdf")) name else "${name.substringBeforeLast('.', name)}.pdf"
}