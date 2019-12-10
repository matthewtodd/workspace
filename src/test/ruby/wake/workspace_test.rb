require 'rubygems'
require 'minitest'
require 'wake'

class WorkspaceTest < Minitest::Test
  def test_navigates_up_deps
    # a      b      g
    #       / \    / \
    #      c   f  h   i
    #     / \
    #    d   e
    workspace = Wake::Workspace.new do |builder|
      builder.load_package('', <<~END)
        ruby_lib(name: 'a', srcs: ['a.rb'])
        ruby_lib(name: 'b', srcs: ['b.rb'], deps: ['//:c', '//:f'])
        ruby_lib(name: 'c', srcs: ['c.rb'], deps: ['//:d', '//:e'])
        ruby_lib(name: 'd', srcs: ['d.rb'])
        ruby_lib(name: 'e', srcs: ['e.rb'])
        ruby_lib(name: 'f', srcs: ['f.rb'])
        ruby_lib(name: 'g', srcs: ['g.rb'], deps: ['//:h', '//:i'])
        ruby_lib(name: 'h', srcs: ['h.rb'])
        ruby_lib(name: 'i', srcs: ['i.rb'])
      END
    end

    assert_equal %w{//:a //:d //:e //:f //:h //:i //:c //:g //:b},
      workspace.enum_for(:each).map { |label, _| label.to_s }
  end
end
