package ru.ezhov.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.lang.StringBuilder
import javax.swing.*
import javax.swing.text.JTextComponent

fun main() {
    SwingUtilities.invokeLater {
        JFrame().apply {
            this.title = "Hello Kotlin"
            this.setSize(700, 500)
            this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            this.setLocationRelativeTo(null)

            val textExtractor = TextExtractorImpl()
            val infoPanel = InfoPanel(textExtractor)

            val keyListener: KeyListener = object : KeyAdapter() {
                override fun keyReleased(e: KeyEvent?) {
                    val component = e!!.component;
                    if (component != null && component is JTextComponent) {
                        SwingUtilities.invokeLater {
                            infoPanel.calculate(component.text)
                        }
                    }
                }
            }
            val textPanel = TextPanel(keyListener)

            val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)
            splitPane.topComponent = textPanel
            splitPane.bottomComponent = infoPanel

            this.add(splitPane, BorderLayout.CENTER)

            this.isVisible = true
        }
    }
}

class TextPanel : JPanel {
    private val textPane: JTextPane = JTextPane()
    private val keyListener: KeyListener

    constructor(keyListener: KeyListener) {
        this.layout = BorderLayout()
        this.add(JScrollPane(textPane), BorderLayout.CENTER)
        this.keyListener = keyListener
        this.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        textPane.addKeyListener(this.keyListener)
    }
}

class InfoPanel : JPanel {
    private val textExtractor: TextExtractor
    private val labelInfo: JLabel = JLabel()

    constructor(textExtractor: TextExtractor) {
        this.layout = BorderLayout()
        this.add(JScrollPane(labelInfo), BorderLayout.CENTER)
        this.textExtractor = textExtractor
        this.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
    }

    fun calculate(text: String) {
        this.setText(textExtractor.extract(text))
    }

    private fun setText(textStatistics: TextStatistics) {
        labelInfo.text = """<html> 
            <p>
                vowels: ${textStatistics.vowelsCount()} <br/>
                consonants: ${textStatistics.consonantsCount()} <br/>
                symbols: ${textStatistics.symbolsCount()}
            </p>
                ${asTable(textStatistics.counts())}
            """.trimMargin()
    }

    private fun asTable(counts: Map<Char, Int>): String {
        return StringBuilder().run {
            append("<table>")
            append("<caption>counts</caption>")
            append("<tr>")

            var counter: Int = 0

            counts.forEach {
                if (counter != 0 && counter % 3 == 0) {
                    append("</tr>")
                    append("<tr>")
                }
                append("<td>")
                append("${it.key} = ${it.value}").append("</td>")
                counter++
            }

            append("</tr>")
            append("</table>")
            toString()
        }
    }
}

class TextExtractorImpl() : TextExtractor {
    private val VOWELS = listOf("A", "E", "I", "O", "U")
    private val CONSONANTS = listOf("B", "C", "D", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "V", "W", "X", "Y", "Z")

    override fun extract(text: String): TextStatistics {
        val originalList = text.toList()
        var vowelsCount = 0
        var consonantsCount = 0
        var symbolsCount = 0

        val eachCountMap = text.toList().groupingBy { it }.eachCount()
        val sortedMap = eachCountMap.toSortedMap();

        for (char in originalList) {
            val charUpperCase = char.toUpperCase().toString()

            if (" " != charUpperCase) {
                when {
                    VOWELS.contains(charUpperCase) -> {
                        vowelsCount++
                    }
                    CONSONANTS.contains(charUpperCase) -> {
                        consonantsCount++
                    }
                    else -> {
                        symbolsCount++
                    }
                }
            }
        }

        return object : TextStatistics {
            override fun vowelsCount(): Int {
                return vowelsCount
            }

            override fun consonantsCount(): Int {
                return consonantsCount
            }

            override fun symbolsCount(): Int {
                return symbolsCount
            }

            override fun counts(): Map<Char, Int> {
                return sortedMap
            }

        }
    }
}

interface TextExtractor {
    fun extract(text: String): TextStatistics
}

interface TextStatistics {
    fun vowelsCount(): Int
    fun consonantsCount(): Int
    fun symbolsCount(): Int
    fun counts(): Map<Char, Int>
}

//suspend fun showSomeData() = coroutineScope {
//    val data = async(Dispatchers.IO) { // <- extension on current scope
//     ... load some UI data for the Main thread ...
//    }
//
//    withContext(Dispatchers.Main) {
//        doSomeWork()
//        val result = data.await()
//        display(result)
//    }
//}