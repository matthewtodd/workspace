require 'rubygems'
require 'minitest'
require 'wake/label'

class LabelTest < Minitest::Test
  def test_parse_workspace_package
    label = Wake::Label.parse('//foo:bar')
    assert_equal 'foo', label.package
    assert_equal 'bar', label.name
  end
end
