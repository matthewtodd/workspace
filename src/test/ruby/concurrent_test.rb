require 'rubygems'
require 'minitest'
require 'concurrent'

class ConcurrentTest < Minitest::Test
  def test_zero
    Concurrent::CountDownLatch.new.await
  end

  def test_typical_usage
    latch = Concurrent::CountDownLatch.new
    10.times { latch.increment }
    10.times { Thread.new { latch.decrement } }
    latch.await
  end

  def test_reuse
    latch = Concurrent::CountDownLatch.new
    10.times { latch.increment }
    10.times { Thread.new { latch.decrement } }
    latch.await

    queue = Queue.new
    10.times { latch.increment; queue.enq :foo }
    assert !queue.empty?
    10.times { Thread.new { latch.decrement; queue.deq } }
    latch.await
    assert queue.empty?
  end
end
