require 'rubygems'
require 'minitest'
require 'wake'

class WorkspaceTest < Minitest::Test
  def test_navigates_up_deps
    skip 'WIP'
    workspace = Wake::Workspace.new do |builder|
      builder.load_package('a', <<~END)
        ruby_lib(
          name: 'a',
          srcs: ['a.rb'],
          deps: ['//b:b']
        )
      END

      builder.load_package('b', <<~END)
        ruby_lib(
          name: 'b',
          srcs: ['b.rb'],
          deps: ['//c:c'],
        )
      END

      builder.load_package('c', <<~END)
        ruby_lib(
          name: 'c',
          srcs: ['c.rb'],
        )
      END
    end

    assert_equal ['//c:c', '//b:b', '//a:a'],
      workspace.enum_for(:each).map { |label, _| label.to_s }
  end
end
