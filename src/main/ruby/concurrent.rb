module Concurrent
  class CountDownLatch
    def initialize
      @count = 0
      @mutex = Mutex.new
      @condition = ConditionVariable.new
    end

    def increment
      @mutex.synchronize do
        @count += 1
      end
    end

    def decrement
      @mutex.synchronize do
        @count -= 1 if @count > 0
        @condition.broadcast if @count.zero?
      end
    end

    def await
      @mutex.synchronize do
        @condition.wait(@mutex) if @count > 0
      end
    end
  end

  class ExecutorService
    def initialize
      @latch = CountDownLatch.new
      @tasks = Queue.new
      @workers = 10.times.map do
        Thread.new do
          while task = @tasks.pop
            task.run
            @latch.decrement
          end
        end
      end
    end

    def submit(command, &output_processor)
      @latch.increment
      @tasks.push(Task.new(command, output_processor))
    end

    def drain
      @latch.await
    end

    private

    class Task
      def initialize(command, output_processor)
        @command = command
        @output_processor = output_processor
      end

      def run
        IO.pipe do |my_stdout, child_stdout|
          pid = Process.spawn(@command, out: child_stdout)
          child_stdout.close
          until my_stdout.eof?
            @output_processor.call(my_stdout.readline.chomp)
          end
          Process.waitpid(pid)
        end
      end
    end
  end
end

