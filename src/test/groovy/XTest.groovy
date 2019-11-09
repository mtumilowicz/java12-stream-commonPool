import spock.lang.Specification

import java.util.concurrent.ForkJoinPool

class XTest extends Specification {

    def "xxx"() {
        def nums = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

        nums.stream().map({ X.transform(it) }).forEach({})
        nums.stream().parallel().map({ X.transform(it) }).forEach({}) // work stealing, main thread participates in

        println "DONE " + Thread.currentThread()

        println Runtime.getRuntime().availableProcessors()
        println ForkJoinPool.commonPool()

        expect:
        1 == 1
    }
}
