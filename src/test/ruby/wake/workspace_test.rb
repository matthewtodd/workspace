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
      builder.load_package('a', <<~END)
        ruby_lib(
          name: 'a',
          srcs: ['a.rb'],
        )
      END

      builder.load_package('b', <<~END)
        ruby_lib(
          name: 'b',
          srcs: ['b.rb'],
          deps: ['//c:c', '//f:f'],
        )
      END

      builder.load_package('c', <<~END)
        ruby_lib(
          name: 'c',
          srcs: ['c.rc'],
          deps: ['//d:d', '//e:e'],
        )
      END

      builder.load_package('d', <<~END)
        ruby_lib(
          name: 'd',
          srcs: ['d.rb'],
        )
      END

      builder.load_package('e', <<~END)
        ruby_lib(
          name: 'e',
          srcs: ['e.rb'],
        )
      END

      builder.load_package('f', <<~END)
        ruby_lib(
          name: 'f',
          srcs: ['f.rb'],
        )
      END

      builder.load_package('g', <<~END)
        ruby_lib(
          name: 'g',
          srcs: ['g.rb'],
          deps: ['//h:h', '//i:i'],
        )
      END

      builder.load_package('h', <<~END)
        ruby_lib(
          name: 'h',
          srcs: ['h.rb'],
        )
      END

      builder.load_package('i', <<~END)
        ruby_lib(
          name: 'i',
          srcs: ['i.rb'],
        )
      END
    end

    assert_equal %w{//a:a //d:d //e:e //f:f //h:h //i:i //c:c //g:g //b:b},
      workspace.enum_for(:each).map { |label, _| label.to_s }
  end
end
