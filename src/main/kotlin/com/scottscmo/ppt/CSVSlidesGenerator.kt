package com.scottscmo.ppt

import org.apache.commons.csv.CSVFormat
import org.apache.poi.xslf.usermodel.XMLSlideShow
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.nio.file.Path

object CSVSlidesGenerator {
    @Throws(IOException::class)
    fun generate(dataFilePath: String, headers: Array<String>, tmplFilePath: String, outputFilePath: String) {
        val title = Path.of(dataFilePath).fileName.toString().split(".")[0]
        val records = CSVFormat.DEFAULT
            .withHeader(*headers)
            .withFirstRecordAsHeader()
            .parse(FileReader(dataFilePath))
            .toList()

        // Duplicate slide to match number of records.
        // Cannot modify here since the copies are using the same reference.
        // Modify after writing the copies.
        FileInputStream(tmplFilePath).use { inStream ->
            val ppt = XMLSlideShow(inStream)
            val srcSlide = ppt.slides[0]
            for (i in records.indices) {
                val slide = ppt.createSlide(srcSlide.slideLayout)
                slide.importContent(srcSlide)
            }
            ppt.removeSlide(0)
            FileOutputStream(outputFilePath).use { outStream -> ppt.write(outStream) }
            ppt.close()
        }

        // Replace text for each slide.
        // Each slide's replacements corresponds to each item in data.
        FileInputStream(outputFilePath).use { inStream ->
            val ppt = XMLSlideShow(inStream)
            ppt.slides.zip(records).forEach { (slide, record) ->
                val values = headers.associate {
                    "{$it}" to record.get(it)
                }.toMutableMap()
                values["{title}"] = title
                TemplatingUtil.replaceText(slide, values)
            }
            FileOutputStream(outputFilePath).use { outStream -> ppt.write(outStream) }
            ppt.close()
        }
    }
}
