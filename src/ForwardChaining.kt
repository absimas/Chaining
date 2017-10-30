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
    var used = false

    override fun toString(): String {
      return "$name: ${sources.toPrettyString()} -> $destination"
    }
  }

  private lateinit var target: String

  private val appliedRulesNames = mutableListOf<String>()
  private val rules = mutableListOf<Rule>()
  private val facts = mutableListOf<String>()
  private var initialFactCount = 0
  private var i = 0

  init {
    println("1 PART. Data")
    parseInput()

    println("1) Rules")
    rules.forEach {
      println("  $it")
    }

    println("2) Facts")
    println("  ${facts.toPrettyString()}")

    println("3) Target")
    println("  $target")

    if (facts.contains(target)) {
      println("Target ($target) already in facts (${facts.toPrettyString()}).")
      System.exit(0)
    }

    println("2 PART. Execution")
    execute()

    println("3 PART. Results")
    if (!facts.contains(target)) {
      println("  1) Target $target not derived.")
    } else {
      println("  1) Target $target derived.")
      if (appliedRulesNames.isNotEmpty()) {
        println("  2) Path: ${appliedRulesNames.toPrettyString()}.")
      }
    }
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
    initialFactCount = facts.size

    // Target
    target = lines[targetIndex+1]
  }

  private fun execute() {
    println()
    println("  ${++i} ITERATION")

    rules.forEach { rule ->
      if (!facts.containsAll(rule.sources)) {
        println("    $rule skipped. Lacks ${(rule.sources-facts).toPrettyString()}.")
      } else if (rule.used) {
        println("    $rule skipped. Already  used  (flag1).")
      } else if (facts.contains(rule.destination)) {
        println("    $rule skipped. Already a fact (flag2).")
      } else {
        rule.used = true
        facts += rule.destination
        appliedRulesNames += rule.name
        println("    $rule used. Raise flag1. Facts are now ${getFacts()}.")

        if (facts.contains(target)) {
          println("    Target reached.")
          return
        } else {
          return execute()
        }
      }
    }

    println("    No applicable rule was found. Terminating.")
  }

  private fun getFacts(): String {
    return "${facts.subList(0, initialFactCount).toPrettyString()} and ${facts.subList(initialFactCount, facts.size).toPrettyString()}"
  }

}

private fun <T> List<T>.toPrettyString(): String {
  return toString().removeSurrounding("[", "]")
}