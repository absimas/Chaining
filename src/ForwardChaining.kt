import java.io.File

/**
 * Created by Simas on 2017 Oct 29.
 */
private const val RULE_PREFIX = "1) Rules"
private const val FACT_PREFIX = "2) Facts"
private const val TARGET_PREFIX = "3) Target"
private const val INPUT_FILENAME_PREFIX = "fc"
class ForwardChaining(val input: File) {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {

      val projectRoot = File(".")
      if (!projectRoot.exists()) {
        throw IllegalStateException("Root doesn't exist!")
      }

      // Filter (fc*), sort (ascending names) and execute input files
      projectRoot.listFiles { _, name ->
        name.startsWith(INPUT_FILENAME_PREFIX)
      }.sortedBy {
        it.name
      }.forEach {
        println("WORK WITH $it")
        ForwardChaining(it)
      }
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
    execute(1)

    println("3 PART. Results")
    if (!facts.contains(target)) {
      println("  1) Target $target not derived.")
    } else {
      println("  1) Target $target derived.")
      println("  2) Path: ${appliedRulesNames.toPrettyString()}.")
    }
  }

  private fun parseInput() {
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

    // Extract rules
    val ruleLines = lines.subList(ruleIndex+1, factIndex)

    // Remove comments, trim and leave only non-empty lines
    val cleanRules = ruleLines.map {
      it.replace(Regex("//.*"), "").trim()
    }.filter {
      it.isNotEmpty()
    }

    // Extract arguments into a Rule class
    cleanRules.forEach { rule ->
      val split = rule.split(' ')
      if (split.size <= 1) {
        throw IllegalStateException("Incorrect rule arguments found! Line was $rule.")
      }

      val sources = split.subList(1, split.size).toMutableList()
      rules.add(Rule("R${rules.size+1}", sources, split[0]))
    }

    // Facts
    facts.addAll(lines[factIndex+1].split(' '))
    initialFactCount = facts.size

    // Target
    target = lines[targetIndex+1]
  }

  private fun execute(iteration: Int) {
    println("  $iteration ITERATION")

    rules.forEach { rule ->
      when {
        !facts.containsAll(rule.sources) -> println("    $rule skipped. Lacks ${(rule.sources-facts).toPrettyString()}.")
        rule.used -> println("    $rule skipped. Already  used  (flag1).")
        facts.contains(rule.destination) -> println("    $rule skipped. Already a fact (flag2).")
        else -> {
          rule.used = true
          facts += rule.destination
          appliedRulesNames += rule.name
          println("    $rule used. Raise flag1. Facts are now ${getFacts()}.")

          if (facts.contains(target)) {
            println("    Target reached.")
            return
          } else {
            return execute(iteration + 1)
          }
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