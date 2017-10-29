import java.io.File

/**
 * Created by Simas on 2017 Oct 29.
 */
private const val RULE_PREFIX = "1) Rules"
private const val FACT_PREFIX = "2) Facts"
private const val TARGET_PREFIX = "3) Target"
class ForwardChaining {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      ForwardChaining()
    }
  }

  class Rule(val name: String, val sources: MutableList<String>, val destination: String) {
    override fun toString(): String {
      return name
    }
  }

  private lateinit var target: String

  private val appliedRules = mutableListOf<Rule>()
  private val rules = mutableListOf<Rule>()
  private val facts = mutableListOf<String>()

  init {
    parseInput()

    println("Rules: $rules")
    println("Facts: $facts")
    println("Target: $target")
  }

  private fun parseInput() {
    val input = File("input")
    if (!input.exists()) {
      throw IllegalStateException("Input file must exist!")
    }

    val lines = input.readLines()
    val lineCount = lines.size
    if (lineCount <= 1) {
      throw IllegalStateException("Input file must be separated by new lines is empty!")
    }

    // Fetch indexes
    val ruleIndex = lines.indexOfFirst { it.startsWith(RULE_PREFIX) }
    if (ruleIndex == -1) throw IllegalStateException("Rules were not found in the input file!")
    val factIndex = lines.indexOfFirst { it.startsWith(FACT_PREFIX) }
    if (factIndex == -1) throw IllegalStateException("Facts were not found in the input file!")
    val targetIndex = lines.indexOfFirst { it.startsWith(TARGET_PREFIX) }
    if (targetIndex == -1) throw IllegalStateException("Target wasn't not found in the input file!")

    // Rules
    // Extract rules
    val ruleLines = lines.subList(ruleIndex+1, factIndex)

    // Remove comments and trim
    val cleanRules = ruleLines.map {
      it.replace(Regex("//.*"), "").trim()
    }.toMutableList()

    // Remove empty lines
    cleanRules.removeIf {
      it.isEmpty()
    }

    // Extract arguments into a Rule class
    cleanRules.forEach { rule ->
      val split = rule.split(' ')
      if (split.size <= 1) {
        throw IllegalStateException("Incorrect rule arguments found! Line was $rule.")
      }

      val name = "R${rules.size+1}"
      val sources = split.subList(1, split.size)
      val destination = split[0]

      rules.add(Rule(name, sources.toMutableList(), destination))
    }

    // Facts
    val factLine = lines[factIndex+1]
    facts.addAll(factLine.split(' '))

    // Target
    target = lines[targetIndex+1]
  }

  private fun execute() {
    // Remove rules whose destination is an already known fact
    rules.removeAll {
      facts.contains(it.destination)
    }

    while (true) {
      // Make sure the target is not yet reached
      if (facts.contains(target)) {
        println("Finished. Found $target by applying rules: $appliedRules")
        return
      }

      // Find a rule for which all sources are known as facts
      val applicableRule = rules.firstOrNull {rule ->
        facts.containsAll(rule.sources)
      }
      if (applicableRule == null) {
        println("No applicable rule was found. Terminating.")
        return
      }
      // Apply rule
      facts.add(applicableRule.destination)
      rules -= applicableRule
      appliedRules += applicableRule
    }
  }

}