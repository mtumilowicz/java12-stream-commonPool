import spock.lang.Specification

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import java.util.stream.Stream

class StreamsTest extends Specification {

    def 'common pool size vs available processors'() {
        given:
        def processors = Runtime.getRuntime().availableProcessors()
        def poolSize = ForkJoinPool.commonPool().parallelism

        expect: 'differs at 1 thread, main'
        processors - 1 == poolSize
    }

    def 'single threaded - using only main'() {
        given:
        def nums = 1..10

        when:
        def threads = nums.stream()
                .map { extractThreads() }
                .collect Collectors.toSet()

        then:
        threads.each { println it }

        then:
        1 == 1
    }

    def 'parallel - using fork-join pool'() {
        given:
        def nums = 1..10

        when:
        def threads = nums.stream()
                .parallel()
                .map { extractThreads() }
                .collect Collectors.toSet()

        then:
        threads.each { println it }

        then:
        1 == 1
    }

    def 'dedicated thread pool'() {
        given:
        def nums = 1..10

        and:
        def stream = nums.stream()
                .parallel()
                .map { printThreads() }

        when:
        process(stream) // it doesn't really matter where you create the stream but where you invoke terminal operation

        then:
        1 == 1
    }

    static void process(stream) throws InterruptedException {
        def pool = new ForkJoinPool(50)

        pool.submit { stream.forEach {} }

        pool.shutdown()

        pool.awaitTermination(30, TimeUnit.SECONDS)
    }

    static def extractThreads() {
        '' + Thread.currentThread()
    }

    static def printThreads() {
        println Thread.currentThread()
    }
}
