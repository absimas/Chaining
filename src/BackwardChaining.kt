import java.io.File

/**
 * Created by Simas on 2017 Nov 26.
 */
private const val RULE_PREFIX = "1) Rules"
private const val FACT_PREFIX = "2) Facts"
private const val TARGET_PREFIX = "3) Target"
private const val INPUT_FILENAME_PREFIX = "bc"
class BackwardChaining(val input: File) {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {

      val projectRoot = File(".")
      if (!projectRoot.exists()) {
        throw IllegalStateException("Root doesn't exist!")
      }

      // Filter (bc*), sort (ascending names) and execute input files
      projectRoot.listFiles { _, name ->
        name.startsWith(INPUT_FILENAME_PREFIX)
      }.sortedBy {
        it.name
      }.forEach {
        println("WORK WITH $it")
        BackwardChaining(it)
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

  private var appliedRuleNames = mutableListOf<String>()
  private var iteration = 0
  private val rules = mutableListOf<Rule>()
  private var facts = mutableListOf<String>()
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
      println("3 PART. Results")
      println("Target ($target) already in facts (${facts.toPrettyString()}). Path is empty.")
    } else {
      println("2 PART. Execution")
      execute(target)

      println("3 PART. Results")
      if (!facts.contains(target)) {
        println("  1) Target $target not derived.")
      } else {
        println("  1) Target $target derived.")
        println("  2) Path: ${appliedRuleNames.toPrettyString()}.")
      }
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

  private fun execute(vararg targets: String): Boolean {
    val lastTarget = targets.last()
    val depth = targets.size - 1

    if (facts.contains(targets.last())) {
      if (facts.indexOf(targets.last()) < initialFactCount) {
        println(String.format("%3d) %sTarget %s. Fact is given. Facts %s. Success.", ++iteration, "-".repeat(depth), lastTarget, getFacts()))
      } else {
        println(String.format("%3d) %sTarget %s. Fact was given. Facts %s. Success.", ++iteration, "-".repeat(depth), lastTarget, getFacts()))
      }
      return true
    }

    val rules = rules.filter {
      !it.used && it.destination == targets.last()
    }
    if (rules.isEmpty()) {
      println(String.format("%3d) %sTarget %s. Deriving rule not found! FAIL.", ++iteration, "-".repeat(depth), lastTarget))
      return false
    }

    rules.forEach { rule ->
      if (targets.dropLast(1).contains(rule.destination)) {
        println(String.format("%3d) %sTarget %s. Loop. FAIL.", ++iteration, "-".repeat(depth), lastTarget))
        return false
      }

      println(String.format("%3d) %sTarget %s. Found rule %s. New targets %s. Success.", ++iteration, "-".repeat(depth), lastTarget, rule, rule.sources.toPrettyString()))
      var found = true

      // Save facts and applied rules
      val facts = facts.toMutableList()
      val appliedRuleNames = appliedRuleNames.toMutableList()

      // Prevent rule from being used again
      rule.used = true

      rule.sources.forEach sourceLoop@ {
        // Recursively call with existing targets and one of the selected rule's source
        if (!execute(*targets, it)) {
          found = false
          // Restore saved facts and applied rules in case one of the rule's source couldn't be derived
          this.facts = facts
          this.appliedRuleNames = appliedRuleNames
          rule.used = false
          return@sourceLoop
        }
      }

      // If all rule sources were derived, destination can be added to facts
      if (found) {
        this.facts.add(lastTarget)
        this.appliedRuleNames.add(rule.name)
        println(String.format("%3d) %sTarget %s. Fact now derived. Facts %s. Success.", ++iteration, "-".repeat(depth), lastTarget, getFacts()))
        return true
      }
    }

    println(String.format("%3d) %sTarget %s. No more rules. FAIL.", ++iteration, "-".repeat(depth), lastTarget))

    return false
  }

  private fun getFacts(): String {
    return if (facts.size <= initialFactCount) {
      facts.subList(0, initialFactCount).toPrettyString()
    } else {
      "${facts.subList(0, initialFactCount).toPrettyString()} and ${facts.subList(initialFactCount, facts.size).toPrettyString()}"
    }
  }

}

private fun <T> List<T>.toPrettyString(): String {
  return toString().removeSurrounding("[", "]")
}