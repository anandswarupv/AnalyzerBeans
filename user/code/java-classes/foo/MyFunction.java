package foo; import org.eobjects.analyzer.reference.Function; public class MyFunction implements Function<String,String> { public String run(String str) { return "" + str.length(); } }