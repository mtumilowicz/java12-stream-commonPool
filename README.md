# java12-stream-commonPool

* https://www.youtube.com/watch?v=IwJ-SCfXoAU
* https://blog.krecan.net/2014/03/18/how-to-specify-thread-pool-for-java-8-parallel-streams/
* http://coopsoft.com/ar/Calamity2Article.html#submit

## preface
* https://github.com/mtumilowicz/fork-join-find-minimum
* https://github.com/mtumilowicz/java11-spliterator-forkjoin
* `ForkJoinPool` - `ExecutorService` for running `ForkJoinTasks`
    * is used by any `ForkJoinTask` that is not explicitly submitted to a specified pool
* `ForkJoinTask` - is a thread-like entity that is much lighter weight than a normal thread
    * is a lightweight form of `Future`
    * computational task calculating pure functions or operating on purely isolated objects
    * primary coordination mechanisms are 
        * `fork` - arranges to asynchronously execute this task in the pool the current task is running 
                       in, if applicable, or using the `ForkJoinPool.commonPool()` if not in `ForkJoinPool`
        * `join` - doesn't proceed until the task's result has been computed

## project description
* common `ForkJoinPool` supports parallel streams and `CompletableFuture`
* common pool is common for the whole application, so there is possibility of saturation
* common pool size vs available processors
    ```
    given:
    def processors = Runtime.getRuntime().availableProcessors()
    def poolSize = ForkJoinPool.commonPool().parallelism

    expect: 'differs at 1 thread, main'
    processors - 1 == poolSize
    ```
* parallel - using fork-join pool
    ```
    given:
    def nums = 1..10

    when:
    def threads = nums.stream()
            .parallel()
            .map { extractThreads() }
            .collect Collectors.toSet()
    
    then:
    println threads
    ```
    produces (on my pc):
    ```
    Thread[ForkJoinPool.commonPool-worker-7,5,main]
    Thread[Test worker,5,main] // note that main thread interferes
    Thread[ForkJoinPool.commonPool-worker-13,5,main]
    Thread[ForkJoinPool.commonPool-worker-11,5,main]
    Thread[ForkJoinPool.commonPool-worker-9,5,main]
    Thread[ForkJoinPool.commonPool-worker-5,5,main]
    Thread[ForkJoinPool.commonPool-worker-15,5,main]
    Thread[ForkJoinPool.commonPool-worker-3,5,main]
    ```
* drawbacks of mixing threads from a pool with a submitting thread:
    * http://coopsoft.com/ar/Calamity2Article.html#submit
    * submitting thread’s stack is contaminated with work that should be independent of it
    * this practice violates a fundamental principle of good programming in not separating a caller from the 
    external processing
* `-Djava.util.concurrent.ForkJoinPool.common.parallelism=100`
* we could run stream on a dedicated thread pool
    ```
    given:
    def nums = 1..10
  
    and:
    def stream = nums.stream()
            .parallel()
            .map { printThreads() }
    
    when:
    process(stream) // it doesn't really matter where you create the stream but where you invoke terminal operation
  
    then 'on my pc':
    Thread[ForkJoinPool-1-worker-101,5,main]
    Thread[ForkJoinPool-1-worker-17,5,main]
    Thread[ForkJoinPool-1-worker-3,5,main]
    Thread[ForkJoinPool-1-worker-59,5,main]
    Thread[ForkJoinPool-1-worker-73,5,main]
    Thread[ForkJoinPool-1-worker-45,5,main]
    Thread[ForkJoinPool-1-worker-75,5,main]
    Thread[ForkJoinPool-1-worker-87,5,main]
    Thread[ForkJoinPool-1-worker-89,5,main]
    Thread[ForkJoinPool-1-worker-117,5,main]
    ```
    where:
    ```
    static void process(stream) throws InterruptedException {
        def pool = new ForkJoinPool(50)
        pool.submit { stream.forEach {} }
        pool.shutdown()
        pool.awaitTermination(30, TimeUnit.SECONDS)
    }
    ```