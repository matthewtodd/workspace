module Wake
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
end
