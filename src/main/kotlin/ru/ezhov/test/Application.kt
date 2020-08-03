package ru.ezhov.test

import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
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

            this.add(textPanel, BorderLayout.CENTER)
            this.add(infoPanel, BorderLayout.SOUTH)

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
        textPane.addKeyListener(this.keyListener)
    }
}

class InfoPanel : JPanel {
    private val textExtractor: TextExtractor
    private val labelInfo: JLabel = JLabel()

    constructor(textExtractor: TextExtractor) {
        this.layout = BorderLayout()
        this.add(labelInfo, BorderLayout.CENTER)
        this.textExtractor = textExtractor
    }

    fun calculate(text: String) {
        val extract = textExtractor.extract(text)
        this.setText(extract.vowelsCount(), extract.consonantsCount(), extract.symbolsCount())
    }

    private fun setText(vowels: Int? = 0, consonants: Int? = 0, symbols: Int? = 0) {
        labelInfo.text = """<html> 
                vowels: $vowels <br/>
                consonants: $consonants <br/>
                symbols: $symbols <br/>
            """.trimMargin()
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
}