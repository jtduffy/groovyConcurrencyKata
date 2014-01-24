import java.util.concurrent.*
class RunningSumsStreamAction extends RecursiveAction {
    String stream
    def results = []
    Integer numberStreamLength
    
    RunningSumsStreamAction(String stream) {
        this.stream = stream
        this.numberStreamLength = stream.length()
    }

    protected void compute() {
        def matcher = (stream =~ /[1-8]+9/)
        if (matcher.size() < 2) {
            parseRunningSums()
            println "Chunk: ${stream} ---  ${results}"
        } else {
            matcher.reset()
            matcher.find()
            RunningSumsStreamAction sums1 = new RunningSumsStreamAction(stream.substring(matcher.start(), matcher.end()))
            RunningSumsStreamAction sums2 = new RunningSumsStreamAction(stream.substring(matcher.end()))
            invokeAll(sums1, sums2)
            results += sums1.results + sums2.results
        } 
        
    }

    def parseRunningSums() {
        Integer sumTargetIdx = numberStreamLength - 1
        while (sumTargetIdx >= 2) {
    
            Integer runningTotal = Integer.parseInt(stream[sumTargetIdx-1])
            Integer targetTotal = Integer.parseInt(stream[sumTargetIdx])
            Integer currentIdx = sumTargetIdx - 2
            while (runningTotal != targetTotal && runningTotal < targetTotal) {
                runningTotal += Integer.parseInt(stream[currentIdx])
                currentIdx--
            }
            
            if (runningTotal == targetTotal && (sumTargetIdx - currentIdx > 2)) {
                results << stream.substring(currentIdx+1, sumTargetIdx+1)
            }
            
            sumTargetIdx--
        }
        
        results = results.reverse()
    }
}

import java.util.Random
class RandomSumsStreamGenerator {

    static generateStream(Integer size) {
        def generator = new Random()
        def stream = ""
        
        size.times {
            stream += (generator.nextInt(9)+1).toString()
        }
        
        stream
    }
}


private void runConcurrent(String stream) {
    println stream
    println "Available processors: ${Runtime.getRuntime().availableProcessors()}";
    ForkJoinPool pool = new ForkJoinPool();
    RunningSumsStreamAction sums = new RunningSumsStreamAction(stream)
    Long start = System.currentTimeMillis()
    pool.invoke(sums)
    Long end = System.currentTimeMillis()
    println sums.results
    println "Execution time: ${end - start} ms\n\n"
}

runConcurrent(RandomSumsStreamGenerator.generateStream(50))
runConcurrent("8745648184845171326578518184151512461752149647129746915414816354846454")
runConcurrent("1239632237176453697341812369")
runConcurrent("12345678987654321")
