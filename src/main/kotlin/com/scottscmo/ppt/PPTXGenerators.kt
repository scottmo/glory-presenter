package com.scottscmo.ppt

import com.scottscmo.model.song.adapters.SongYAMLAdapter
import org.apache.poi.xslf.usermodel.XMLSlideShow
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object PPTXGenerators {
    @Throws(IOException::class)
    fun generate(dataFilePath: String, tmplFilePath: String, outputDirPath: String) {
        val inputContent = Files.readString(Path.of(dataFilePath))
        val input = SongYAMLAdapter.deserialize(inputContent)

        input?.sections?.let { sections ->
            val title = input.title
            val outputFilePath = Path.of(outputDirPath, "$title.pptx").toString()

            // Duplicate slide to match number of records.
            // Cannot modify here since the copies are using the same reference.
            // Modify after writing the copies.
            FileInputStream(tmplFilePath).use { inStream ->
                val ppt = XMLSlideShow(inStream)
                val srcSlide = ppt.slides[0]
                for (i in sections.indices) {
                    val slide = ppt.createSlide(srcSlide.slideLayout)
                    slide.importContent(srcSlide)
                }
                ppt.removeSlide(0)
                FileOutputStream(outputFilePath).use {
                        outStream -> ppt.write(outStream)
                }
                ppt.close()
            }

            // Replace text for each slide.
            // Each slide's replacements corresponds to each item in data.
            FileInputStream(outputFilePath).use { inStream ->
                val ppt = XMLSlideShow(inStream)
                ppt.slides.zip(sections).forEach { (slide, section) ->
                    val values = section.text.entries.associate {
                        "{${it.key}}" to it.value
                    }.toMutableMap()
                    values["{title}"] = title
                    TemplatingUtil.replaceText(slide, values)
                }
                FileOutputStream(outputFilePath).use {
                        outStream -> ppt.write(outStream)
                }
                ppt.close()
            }
        }
    }
}
