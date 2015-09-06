addCommandAlias("validate", ";compile;test;runExamples")

val runExamplesStr =
  ";examples/run-main org.w3.banana.examples.GraphExampleWithJena" +
  ";examples/run-main org.w3.banana.examples.GraphExampleWithSesame" +
  ";examples/run-main org.w3.banana.examples.IOExampleWithJena" +
  ";examples/run-main org.w3.banana.examples.IOExampleWithSesame" +
  ";examples/run-main org.w3.banana.examples.SPARQLExampleWithJena"

addCommandAlias("runExamples", runExamplesStr)
